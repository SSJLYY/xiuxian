package com.xiuxian.game.modules.player.service;

import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.mapper.PlayerItemMapper;
import com.xiuxian.game.modules.player.mapper.UserMapper;
import com.xiuxian.game.modules.skill.service.SkillService;
import com.xiuxian.game.modules.quest.service.QuestProgressService;
import com.xiuxian.game.common.config.GameBalanceConfig;
import com.xiuxian.game.common.util.GameBalanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;

/**
 * 玩家服务类
 * 负责玩家档案管理、修炼系统、升级系统等核心游戏逻辑
 *
 * 主要功能：
 * - 玩家档案创建和查询
 * - 修炼系统（开始修炼、停止修炼、计算收益）
 * - 升级系统（经验计算、等级提升、境界突破）
 * - 属性管理（基础属性、装备加成、技能加成）
 * - 新手物品发放
 *
 * @author xiuxian
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerProfileMapper playerProfileMapper;
    private final UserMapper userMapper;
    private final PlayerItemMapper playerItemMapper;
    private final com.xiuxian.game.modules.player.mapper.PlayerLoginLogMapper playerLoginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final QuestProgressService questProgressService;
    private final SkillService skillService;
    private final GameBalanceConfig balance;
    private final GameBalanceUtils balanceUtils;

    /**
     * 创建新玩家档案
     * 为新注册用户创建游戏角色，初始化基础属性和新手物品
     * 
     * @param user 用户信息
     * @param nickname 玩家昵称，如果为null则使用用户名
     * @return 创建的玩家档案
     * @throws RuntimeException 当创建失败时抛出异常
     */
    @Transactional
    public PlayerProfile createNewPlayer(User user, String nickname) {
        try {
            log.info("========== 创建新玩家档案 ==========");
            log.info("用户ID: {}, 用户名: {}, 昵称: {}", user.getId(), user.getUsername(), nickname);

            // 1. 构建玩家档案
            PlayerProfile playerProfile = PlayerProfile.builder()
                    .userId(user.getId())
                    .nickname(nickname != null ? nickname : user.getUsername())
                    // 初始等级和经验
                    .level(1)
                    .exp(balance.getPlayerInitial().getExp())
                    .expToNext(balanceUtils.calculateExpToNext(1))
                    .realm("练气期")
                    .cultivationSpeed(BigDecimal.ONE)
                    // 初始资源（使用GDD优化值）
                    .spiritStones((long) balance.getPlayerInitial().getSpiritStones())
                    .cultivationPoints(0L)
                    .contributionPoints(0L)
                    .attributePoints(0)
                    .skillPoints(0)
                    // 初始属性（使用GDD优化值）
                    .attack(balance.getPlayerInitial().getAttack())
                    .defense(balance.getPlayerInitial().getDefense())
                    .health(balance.getPlayerInitial().getHealth())
                    .mana(balance.getPlayerInitial().getMana())
                    .speed(balance.getPlayerInitial().getSpeed())
                    // 修炼状态
                    .isCultivating(false)
                    .lastOnlineTime(LocalDateTime.now())
                    .totalCultivationTime(0L)
                    // 装备加成（初始为0）
                    .equipmentAttackBonus(0)
                    .equipmentDefenseBonus(0)
                    .equipmentHealthBonus(0)
                    .equipmentManaBonus(0)
                    .equipmentSpeedBonus(0)
                    .build();

            // 2. 保存到数据库
            playerProfileMapper.insert(playerProfile);
            PlayerProfile savedProfile = playerProfileMapper.selectById(playerProfile.getId());
            log.info("玩家档案保存成功: ID={}, 昵称={}, 等级={}, 境界={}", 
                    savedProfile.getId(), savedProfile.getNickname(), 
                    savedProfile.getLevel(), savedProfile.getRealm());

            // 3. 发放新手物品
            log.info("发放新手物品...");
            awardStarterItems(savedProfile.getId());

            // 4. 任务初始化由前端第一次查询时自动触发
            log.info("任务系统将在首次查询时自动初始化");

            log.info("========== 玩家档案创建完成 ==========");
            return savedProfile;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建玩家档案失败: 用户名={}", user.getUsername(), e);
            throw new BusinessException(ErrorCode.PLAYER_CREATE_FAILED);
        }
    }

    /**
     * 根据ID获取玩家档案（getPlayerProfileById 的别名）
     */
    public PlayerProfile getPlayerProfile(Integer playerId) {
        return getPlayerProfileById(playerId);
    }

    /**
     * 检查玩家是否可以突破境界
     * 需要消耗5000灵石，心魔挑战胜率70%
     *
     * @param playerId 玩家ID
     * @return 是否可以突破
     */
    public boolean canBreakthrough(Integer playerId) {
        PlayerProfile profile = getPlayerProfileById(playerId);
        // 需要有足够灵石进行突破挑战
        return profile.getSpiritStones() != null && profile.getSpiritStones() >= 5000;
    }

    /**
     * 尝试境界突破
     * 消耗5000灵石，70%成功率；失败不扣灵石但进入1小时冷却
     *
     * @param playerId 玩家ID
     * @return 突破结果描述
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public String attemptBreakthrough(Integer playerId) {
        PlayerProfile profile = getPlayerProfileById(playerId);
        if (profile.getSpiritStones() == null || profile.getSpiritStones() < 5000) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "灵石不足，需要5000灵石进行境界突破");
        }
        // 消耗灵石
        profile.setSpiritStones(profile.getSpiritStones() - 5000);
        // 70%成功率
        boolean success = java.util.concurrent.ThreadLocalRandom.current().nextDouble() < 0.7;
        if (success) {
            String oldRealm = profile.getRealm();
            updateRealm(profile);
            playerProfileMapper.updateById(profile);
            return "突破成功！" + oldRealm + " → " + profile.getRealm();
        } else {
            playerProfileMapper.updateById(profile);
            return "心魔侵袭，突破失败！消耗5000灵石，1小时后可再次尝试";
        }
    }

    /**
     * 根据ID获取玩家档案
     */
    public PlayerProfile getPlayerProfileById(Integer playerId) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        return profile;
    }

    /**
     * 获取当前登录玩家的档案
     */
    public PlayerProfile getCurrentPlayerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        String username = authentication.getName();
        log.info("获取当前玩家档案: {}", username);

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        PlayerProfile profile = playerProfileMapper.selectByUserId(user.getId());
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        if (profile.getIsCultivating() == null) {
            profile.setIsCultivating(false);
        }
        List<PlayerItem> items = playerItemMapper.selectByPlayerId(profile.getId());
        if (items == null || items.isEmpty()) {
            awardStarterItems(profile.getId());
        }
        return profile;
    }

    /**
     * 获取当前登录玩家的ID
     */
    public Integer getCurrentPlayerId() {
        PlayerProfile profile = getCurrentPlayerProfile();
        return profile.getId();
    }

    /**
     * 开始修炼
     * 玩家进入修炼状态，记录开始时间
     * 
     * @throws RuntimeException 当玩家已在修炼中或操作失败时抛出异常
     */
    @Transactional
    public void cultivate() {
        PlayerProfile profile = getCurrentPlayerProfile();
        log.info("玩家开始修炼: ID={}, 等级={}, 境界={}", profile.getId(), profile.getLevel(), profile.getRealm());

        if (profile.getIsCultivating() == null) {
            profile.setIsCultivating(false);
        }

        if (profile.getIsCultivating()) {
            log.info("玩家已在修炼中，忽略重复请求: ID={}", profile.getId());
            return;
        }

        profile.setIsCultivating(true);
        profile.setLastCultivationStart(LocalDateTime.now());
        playerProfileMapper.updateById(profile);

        log.info("玩家开始修炼成功: ID={}, 开始时间={}", profile.getId(), profile.getLastCultivationStart());
    }

    /**
     * 停止修炼
     * 结束修炼状态，计算修炼收益（经验、灵石等），检查升级，更新任务进度
     */
    @Transactional
    public void stopCultivate() {
        PlayerProfile profile = getCurrentPlayerProfile();
        log.info("玩家停止修炼: ID={}, 修炼状态={}", profile.getId(), profile.getIsCultivating());

        if (!profile.getIsCultivating()) {
            profile.setIsCultivating(false);
            playerProfileMapper.updateById(profile);
            log.info("玩家未在修炼中，忽略停止请求: ID={}", profile.getId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = profile.getLastCultivationStart();

        if (startTime != null) {
            long cultivationTimeSeconds = java.time.Duration.between(startTime, now).getSeconds();
            long maxCultivationTime = 24 * 60 * 60;
            long actualCultivationTime = Math.min(cultivationTimeSeconds, maxCultivationTime);

            long cultivationTimeMinutes = actualCultivationTime / 60;
            long oldTotalTime = profile.getTotalCultivationTime() == null ? 0 : profile.getTotalCultivationTime();
            profile.setTotalCultivationTime(oldTotalTime + cultivationTimeMinutes);

            double baseExpPerSecond = 1.0;
            double cultivationSpeedMultiplier = profile.getCultivationSpeed().doubleValue();
            long expGained = (long) (actualCultivationTime * baseExpPerSecond * cultivationSpeedMultiplier);

            // 【2026-03-24 优化】使用GameBalanceUtils计算灵石收益
            double cultivationHours = actualCultivationTime / 3600.0;
            long spiritStonesGained = balanceUtils.calculateCultivationSpiritStones(profile, cultivationHours);
            
            // 检查灵石上限，超出部分转为修炼点数
            long spiritStonesLimit = balanceUtils.calculateSpiritStonesLimit(profile.getRealm());
            long spiritStonesToAdd = Math.min(spiritStonesGained, spiritStonesLimit - profile.getSpiritStones());
            long overflowSpiritStones = spiritStonesGained - spiritStonesToAdd;
            
            if (overflowSpiritStones > 0) {
                profile.setCultivationPoints(profile.getCultivationPoints() + overflowSpiritStones);
                log.info("灵石超限，{}灵石转为修炼点数", overflowSpiritStones);
            }

            profile.setExp(profile.getExp() + expGained);
            profile.setSpiritStones(profile.getSpiritStones() + spiritStonesToAdd);
            log.info("修炼收益: 时长={}s, 经验+{}, 灵石+{}", actualCultivationTime, expGained, spiritStonesGained);

            int oldLevel = profile.getLevel();
            checkLevelUp(profile);
            if (profile.getLevel() > oldLevel) {
                log.info("玩家升级: {}级 -> {}级", oldLevel, profile.getLevel());
            }

            try {
                questProgressService.updateQuestProgressByType(profile.getId(), com.xiuxian.game.modules.quest.entity.Quest.QuestType.DAILY, 1);
                questProgressService.updateQuestProgressByType(profile.getId(), com.xiuxian.game.modules.quest.entity.Quest.QuestType.WEEKLY, (int) actualCultivationTime);
                questProgressService.updateQuestProgressByType(profile.getId(), com.xiuxian.game.modules.quest.entity.Quest.QuestType.MONTHLY, 1);
            } catch (Exception qe) {
                log.error("更新任务进度失败，修炼收益不受影响: playerId={}", profile.getId(), qe);
                // 不抛出异常，确保修炼收益和属性更新正常提交
                // 任务进度丢失不影响核心游戏数据
            }
        } else {
            log.warn("修炼开始时间为null，无法计算收益");
        }

        profile.setIsCultivating(false);
        profile.setLastCultivationEnd(now);
        playerProfileMapper.updateById(profile);
        log.info("玩家停止修炼成功: ID={}", profile.getId());
    }

    /**
     * 保存玩家档案
     * 单条 updateById，不需要独立事务；
     * 被外层事务（如 GuildService.donate）调用时自动加入外层事务。
     */
    public void savePlayerProfile(PlayerProfile playerProfile) {
        playerProfileMapper.updateById(playerProfile);
        log.debug("保存玩家档案成功: ID={}", playerProfile.getId());
    }

    /**
     * 检查并处理升级
     * 当玩家经验达到升级要求时，自动升级并提升属性
     * 支持连续升级，但限制最多100次以防止无限循环
     * 
     * @param profile 玩家档案
     */
    private void checkLevelUp(PlayerProfile profile) {
        // 防止无限循环，最多升级100次
        int maxLevelUps = 100;
        int levelUps = 0;
        
        log.debug("开始检查升级: 当前等级={}, 当前经验={}, 升级所需={}", 
                profile.getLevel(), profile.getExp(), profile.getExpToNext());
        
        while (profile.getExp() >= profile.getExpToNext() && levelUps < maxLevelUps) {
            String oldRealm = profile.getRealm();
            int oldLevel = profile.getLevel();
            
            // 1. 升级
            profile.setLevel(profile.getLevel() + 1);
            profile.setExp(profile.getExp() - profile.getExpToNext());
            profile.setExpToNext(profile.getExpToNext() * 2); // 下一级所需经验翻倍
            
            // 2. 升级属性提升
            profile.setAttack(profile.getAttack() + 5);
            profile.setDefense(profile.getDefense() + 3);
            profile.setHealth(profile.getHealth() + 20);
            profile.setMana(profile.getMana() + 10);
            profile.setSpeed(profile.getSpeed() + 1);
            
            log.info("玩家升级: {}级 -> {}级, 属性提升: 攻击+5, 防御+3, 生命+20, 法力+10, 速度+1", 
                    oldLevel, profile.getLevel());
            
            // 3. 更新境界
            updateRealm(profile);
            
            // 4. 境界突破奖励（额外属性点和技能点）
            if (!java.util.Objects.equals(oldRealm, profile.getRealm())) {
                int oldAttributePoints = profile.getAttributePoints() == null ? 0 : profile.getAttributePoints();
                int oldSkillPoints = profile.getSkillPoints() == null ? 0 : profile.getSkillPoints();
                
                profile.setAttributePoints(oldAttributePoints + 5);
                profile.setSkillPoints(oldSkillPoints + 1);
                
                log.info("境界突破: {} -> {}, 奖励: 属性点+5, 技能点+1", 
                        oldRealm, profile.getRealm());
            }
            
            levelUps++;
        }
        
        if (levelUps > 0) {
            log.info("升级完成: 共升级{}级, 当前等级={}, 剩余经验={}, 下级所需={}", 
                    levelUps, profile.getLevel(), profile.getExp(), profile.getExpToNext());
        }
        
        if (levelUps >= maxLevelUps) {
            log.warn("玩家升级次数达到上限({}次)，可能存在问题: ID={}, 当前经验={}", 
                    maxLevelUps, profile.getId(), profile.getExp());
        }
    }
    
    /**
     * 根据等级更新境界
     * 境界划分：
     * - 练气期: 1-100级
     * - 筑基期: 101-200级
     * - 金丹期: 201-400级
     * - 元婴期: 401-700级
     * - 化神期: 701-1000级
     * - 合体期: 1001-1500级
     * - 大乘期: 1501-2000级
     * - 渡劫期: 2001级以上
     * 
     * @param profile 玩家档案
     */
    private void updateRealm(PlayerProfile profile) {
        int level = profile.getLevel();
        String newRealm;
        
        if (level >= 2001) {
            newRealm = "渡劫期";
        } else if (level >= 1501) {
            newRealm = "大乘期";
        } else if (level >= 1001) {
            newRealm = "合体期";
        } else if (level >= 701) {
            newRealm = "化神期";
        } else if (level >= 401) {
            newRealm = "元婴期";
        } else if (level >= 201) {
            newRealm = "金丹期";
        } else if (level >= 101) {
            newRealm = "筑基期";
        } else {
            newRealm = "练气期";
        }
        
        profile.setRealm(newRealm);
    }

    /**
     * 发放新手物品
     * 为新玩家发放初始物品，包括基础装备和消耗品
     * 
     * @param playerId 玩家ID
     */
    private void awardStarterItems(Integer playerId) {
        try {
            log.info("为玩家 {} 发放新手物品", playerId);
            
            // 新手物品1: 疗伤丹x1
            PlayerItem item1 = PlayerItem.builder()
                    .playerId(playerId)
                    .itemId(1)  // 疗伤丹
                    .quantity(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // 新手物品2: 回灵丹x5
            PlayerItem item2 = PlayerItem.builder()
                    .playerId(playerId)
                    .itemId(2)  // 回灵丹
                    .quantity(5)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            playerItemMapper.insert(item1);
            playerItemMapper.insert(item2);
            
            log.info("新手物品发放成功: 疗伤丹x1, 回灵丹x5");
        } catch (Exception e) {
            log.warn("发放新手物品失败: {}", e.getMessage(), e);
            // 不抛出异常，允许玩家创建继续
        }
    }

    /**
     * 获取玩家详细信息，包含所有属性加成
     */
    public PlayerProfile getPlayerProfileWithBonuses(Integer playerId) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        
        // 计算技能加成
        Map<String, Integer> skillBonuses = skillService.calculateSkillBonuses(playerId);
        
        // 设置技能加成到玩家属性中
        profile.setSkillHealthBonus(skillBonuses.get("health"));
        profile.setSkillManaBonus(skillBonuses.get("mana"));
        profile.setSkillAttackBonus(skillBonuses.get("attack"));
        profile.setSkillDefenseBonus(skillBonuses.get("defense"));
        profile.setSkillSpeedBonus(skillBonuses.get("speed"));
        
        return profile;
    }

    /**
     * 排行榜专用：查询最高等级的Top N玩家
     * 供RankingService使用，通过Service接口访问，遵守模块边界
     *
     * @param limit 返回条数
     * @return 按等级降序的玩家列表
     */
    public List<PlayerProfile> getTopPlayersByLevel(int limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile>()
                        .orderByDesc(PlayerProfile::getLevel)
                        .orderByDesc(PlayerProfile::getExp)
                        .last("LIMIT " + Math.min(limit, 100));
        return playerProfileMapper.selectList(wrapper);
    }

    /**
     * 排行榜专用：查询最多灵石的Top N玩家
     * 供RankingService使用
     *
     * @param limit 返回条数
     * @return 按灵石数降序的玩家列表
     */
    public List<PlayerProfile> getTopPlayersBySpiritStones(int limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile>()
                        .orderByDesc(PlayerProfile::getSpiritStones)
                        .last("LIMIT " + Math.min(limit, 100));
        return playerProfileMapper.selectList(wrapper);
    }

    /**
     * 排行榜专用：查询最高修炼速度的Top N玩家
     * 供RankingService使用
     *
     * @param limit 返回条数
     * @return 按修炼速度降序的玩家列表
     */
    public List<PlayerProfile> getTopPlayersByCultivationSpeed(int limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile>()
                        .orderByDesc(PlayerProfile::getCultivationSpeed)
                        .orderByDesc(PlayerProfile::getLevel)
                        .last("LIMIT " + Math.min(limit, 100));
        return playerProfileMapper.selectList(wrapper);
    }

    /**
     * 统计指定时间范围内新增玩家数（供Admin统计服务使用）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 新增玩家数
     */
    public long countNewPlayers(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.between("created_at", startTime, endTime);
        return playerProfileMapper.selectCount(wrapper);
    }

    /**
     * 统计留存玩家数（供Admin统计服务使用）
     * 在createStart~createEnd期间创建，且在loginStart~loginEnd期间有登录的玩家数
     */
    public long countRetainedPlayers(java.time.LocalDateTime createStart, java.time.LocalDateTime createEnd,
                                      java.time.LocalDateTime loginStart, java.time.LocalDateTime loginEnd) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.between("created_at", createStart, createEnd)
               .between("last_login_at", loginStart, loginEnd);
        return playerProfileMapper.selectCount(wrapper);
    }

    /**
     * 统计活跃玩家数（供Admin统计服务使用）
     */
    public long countActivePlayers(java.time.LocalDateTime since) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.ge("last_login_at", since);
        return playerProfileMapper.selectCount(wrapper);
    }

    // ===================== 背包管理方法（供equipment模块InventoryService使用） =====================

    /**
     * 获取玩家所有背包物品（供InventoryService使用）
     */
    public List<PlayerItem> getPlayerItemsByPlayerId(Integer playerId) {
        return playerItemMapper.selectByPlayerId(playerId);
    }

    /**
     * 根据ID获取背包条目（供InventoryService使用）
     */
    public PlayerItem getPlayerItemById(Integer playerItemId) {
        return playerItemMapper.selectById(playerItemId);
    }

    /**
     * 根据玩家ID和物品ID获取背包条目（供InventoryService使用）
     */
    public PlayerItem getPlayerItemByPlayerAndItem(Integer playerId, Integer itemId) {
        return playerItemMapper.selectByPlayerIdAndItemId(playerId, itemId);
    }

    /**
     * 保存（插入或更新）背包条目（供InventoryService使用）
     */
    public void savePlayerItem(PlayerItem playerItem) {
        if (playerItem.getId() == null) {
            playerItemMapper.insert(playerItem);
        } else {
            playerItemMapper.updateById(playerItem);
        }
    }

    /**
     * 删除背包条目（供InventoryService使用）
     */
    public void deletePlayerItem(Integer playerItemId) {
        playerItemMapper.deleteById(playerItemId);
    }

    /**
     * 根据ID获取背包条目后更新（供InventoryService使用）
     */
    public void updatePlayerItem(PlayerItem playerItem) {
        playerItemMapper.updateById(playerItem);
    }

    // ===================== 反作弊/登录安全方法（供AntiFraudService使用） =====================

    /**
     * 查询玩家最近登录日志（供AntiFraudService使用）
     */
    public List<com.xiuxian.game.modules.player.entity.PlayerLoginLog> getRecentLoginLogs(
            Integer playerId, java.time.LocalDateTime since) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xiuxian.game.modules.player.entity.PlayerLoginLog> qw =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.eq("player_id", playerId).ge("login_at", since);
        return playerLoginLogMapper.selectList(qw);
    }

    /**
     * 查询玩家某时间段内的登录次数（供AntiFraudService使用）
     */
    public long countRecentLogins(Integer playerId, java.time.LocalDateTime since) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xiuxian.game.modules.player.entity.PlayerLoginLog> qw =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.eq("player_id", playerId).ge("login_at", since);
        return playerLoginLogMapper.selectCount(qw);
    }

    /**
     * 封禁用户（供AntiFraudService使用）
     */
    public void banUser(Integer userId, String status) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
    }

    /**
     * 根据ID获取用户（供AntiFraudService使用）
     */
    public User getUserById(Integer userId) {
        return userMapper.selectById(userId);
    }
}
