package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.QuestMapper;
import com.xiuxian.game.mapper.UserMapper;
import com.xiuxian.game.mapper.PlayerItemMapper;
import com.xiuxian.game.service.SkillService;
import com.xiuxian.game.service.QuestProgressService;
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
    private final QuestMapper questMapper;
    private final UserMapper userMapper;
    private final PlayerItemMapper playerItemMapper;
    private final PasswordEncoder passwordEncoder;
    private final QuestProgressService questProgressService;
    private final SkillService skillService;

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
                    .exp(0L)
                    .expToNext(100L)
                    .realm("练气期")
                    .cultivationSpeed(BigDecimal.ONE)
                    // 初始资源
                    .spiritStones(1000L)  // 初始灵石
                    .cultivationPoints(0L)
                    .contributionPoints(0L)
                    .attributePoints(0)
                    .skillPoints(0)
                    // 初始属性
                    .attack(10)
                    .defense(5)
                    .health(100)
                    .mana(50)
                    .speed(10)
                    // 修炼状态
                    .isCultivating(false)
                    .lastOnlineTime(LocalDateTime.now())
                    .totalCultivationTime(0L)
                    // 装备和技能加成（初始为0）
                    .equipmentAttackBonus(0)
                    .equipmentDefenseBonus(0)
                    .equipmentHealthBonus(0)
                    .equipmentManaBonus(0)
                    .equipmentSpeedBonus(0)
                    .skillAttackBonus(0)
                    .skillDefenseBonus(0)
                    .skillHealthBonus(0)
                    .skillManaBonus(0)
                    .skillSpeedBonus(0)
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

        } catch (Exception e) {
            log.error("创建玩家档案失败: 用户名={}", user.getUsername(), e);
            throw new RuntimeException("创建玩家档案失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取玩家档案
     */
    public PlayerProfile getPlayerProfileById(Integer playerId) {
        try {
            log.info("获取玩家档案: ID={}", playerId);
            PlayerProfile profile = playerProfileMapper.selectById(playerId);
            if (profile == null) {
                throw new RuntimeException("玩家档案不存在");
            }
            return profile;
        } catch (Exception e) {
            log.error("获取玩家档案失败: ID={}", playerId, e);
            throw new RuntimeException("获取玩家档案失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录玩家的档案
     */
    public PlayerProfile getCurrentPlayerProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("用户未登录");
            }

            String username = authentication.getName();
            log.info("获取当前玩家档案: {}", username);

            // 先通过用户名获取用户信息
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
        // 然后通过用户ID获取玩家档案
        PlayerProfile profile = playerProfileMapper.selectByUserId(user.getId());
        if (profile == null) {
            throw new RuntimeException("玩家档案不存在");
        }
        if (profile.getIsCultivating() == null) {
            profile.setIsCultivating(false);
        }
        java.util.List<com.xiuxian.game.entity.PlayerItem> items = playerItemMapper.selectByPlayerId(profile.getId());
        if (items == null || items.isEmpty()) {
            awardStarterItems(profile.getId());
        }
        return profile;
        } catch (Exception e) {
            log.error("获取当前玩家档案失败", e);
            throw new RuntimeException("获取当前玩家档案失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录玩家的ID
     */
    public Integer getCurrentPlayerId() {
        try {
            PlayerProfile profile = getCurrentPlayerProfile();
            return profile.getId();
        } catch (Exception e) {
            log.error("获取当前玩家ID失败", e);
            throw new RuntimeException("获取当前玩家ID失败: " + e.getMessage());
        }
    }

    /**
     * 开始修炼
     * 玩家进入修炼状态，记录开始时间
     * 
     * @throws RuntimeException 当玩家已在修炼中或操作失败时抛出异常
     */
    @Transactional
    public void cultivate() {
        try {
            PlayerProfile profile = getCurrentPlayerProfile();
            log.info("========== 开始修炼 ==========");
            log.info("玩家ID: {}, 昵称: {}, 当前等级: {}, 境界: {}", 
                    profile.getId(), profile.getNickname(), profile.getLevel(), profile.getRealm());
            log.info("当前修炼状态: {}", profile.getIsCultivating());

            // 确保isCultivating不为null，防止空指针异常
            if (profile.getIsCultivating() == null) {
                log.warn("修炼状态为null，自动设置为false");
                profile.setIsCultivating(false);
            }
            
            // 检查是否已在修炼中
            if (profile.getIsCultivating()) {
                log.info("玩家已在修炼中，忽略重复请求: ID={}", profile.getId());
                return;
            }

            // 设置修炼状态
            profile.setIsCultivating(true);
            profile.setLastCultivationStart(LocalDateTime.now());
            playerProfileMapper.updateById(profile);
            
            log.info("玩家开始修炼成功: ID={}, 开始时间={}", 
                    profile.getId(), profile.getLastCultivationStart());
            log.info("========== 修炼开始完成 ==========");
        } catch (Exception e) {
            log.error("开始修炼失败", e);
            throw new RuntimeException("开始修炼失败: " + e.getMessage());
        }
    }

    /**
     * 停止修炼
     * 结束修炼状态，计算修炼收益（经验、灵石等），检查升级，更新任务进度
     * 
     * @throws RuntimeException 当操作失败时抛出异常
     */
    @Transactional
    public void stopCultivate() {
        try {
            PlayerProfile profile = getCurrentPlayerProfile();
            log.info("========== 停止修炼 ==========");
            log.info("玩家ID: {}, 昵称: {}, 当前修炼状态: {}", 
                    profile.getId(), profile.getNickname(), profile.getIsCultivating());

            // 检查是否在修炼中
            if (!profile.getIsCultivating()) {
                profile.setIsCultivating(false);
                playerProfileMapper.updateById(profile);
                log.info("玩家未在修炼中，忽略停止请求: ID={}", profile.getId());
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = profile.getLastCultivationStart();
            
            if (startTime != null) {
                // 1. 计算修炼时间（秒）
                long cultivationTimeSeconds = java.time.Duration.between(startTime, now).getSeconds();
                log.info("修炼时长: {}秒 ({}分钟)", cultivationTimeSeconds, cultivationTimeSeconds / 60);
                
                // 2. 限制最大修炼时间为24小时（防止异常情况）
                long maxCultivationTime = 24 * 60 * 60; // 24小时
                long actualCultivationTime = Math.min(cultivationTimeSeconds, maxCultivationTime);
                
                if (actualCultivationTime < cultivationTimeSeconds) {
                    log.warn("修炼时间超过24小时，限制为24小时: 原始={}秒, 实际={}秒", 
                            cultivationTimeSeconds, actualCultivationTime);
                }
                
                // 3. 更新总修炼时间（分钟）
                long cultivationTimeMinutes = actualCultivationTime / 60;
                long oldTotalTime = profile.getTotalCultivationTime();
                profile.setTotalCultivationTime(oldTotalTime + cultivationTimeMinutes);
                log.info("总修炼时间更新: {}分钟 -> {}分钟", oldTotalTime, profile.getTotalCultivationTime());
                
                // 4. 计算修炼收益（每秒获得基础经验 * 修炼速度）
                double baseExpPerSecond = 1.0; // 每秒获得1经验
                double cultivationSpeedMultiplier = profile.getCultivationSpeed().doubleValue();
                long expGained = (long) (actualCultivationTime * baseExpPerSecond * cultivationSpeedMultiplier);
                
                // 5. 限制单次修炼最大经验获得
                long maxExpPerCultivation = 3600; // 单次修炼最多获得3600经验（1小时）
                if (expGained > maxExpPerCultivation) {
                    log.warn("单次修炼经验超过上限，限制为{}: 原始={}, 实际={}", 
                            maxExpPerCultivation, expGained, maxExpPerCultivation);
                    expGained = maxExpPerCultivation;
                }
                
                // 6. 增加经验
                long oldExp = profile.getExp();
                profile.setExp(oldExp + expGained);
                log.info("经验增加: {} -> {} (+{})", oldExp, profile.getExp(), expGained);
                
                // 7. 检查是否升级
                int oldLevel = profile.getLevel();
                String oldRealm = profile.getRealm();
                checkLevelUp(profile);
                if (profile.getLevel() > oldLevel) {
                    log.info("玩家升级: {}级 -> {}级, 境界: {} -> {}", 
                            oldLevel, profile.getLevel(), oldRealm, profile.getRealm());
                }

                // 8. 更新任务进度
                try {
                    log.info("更新任务进度...");
                    // 更新每日修炼任务进度（完成1次修炼）
                    questProgressService.updateQuestProgressByType(
                            profile.getId(), com.xiuxian.game.entity.Quest.QuestType.DAILY, 1);
                    // 更新每周修炼进度（累计修炼时间，单位：秒）
                    questProgressService.updateQuestProgressByType(
                            profile.getId(), com.xiuxian.game.entity.Quest.QuestType.WEEKLY, (int) actualCultivationTime);
                    // 更新每月任务进度（完成1次修炼）
                    questProgressService.updateQuestProgressByType(
                            profile.getId(), com.xiuxian.game.entity.Quest.QuestType.MONTHLY, 1);
                    log.info("任务进度更新成功");
                } catch (Exception qe) {
                    log.error("更新任务进度失败: {}", qe.getMessage(), qe);
                }
            } else {
                log.warn("修炼开始时间为null，无法计算收益");
            }

            // 9. 更新修炼状态
            profile.setIsCultivating(false);
            profile.setLastCultivationEnd(now);
            playerProfileMapper.updateById(profile);
            
            log.info("玩家停止修炼成功: ID={}, 结束时间={}", profile.getId(), now);
            log.info("========== 修炼停止完成 ==========");
        } catch (Exception e) {
            log.error("停止修炼失败", e);
            throw new RuntimeException("停止修炼失败: " + e.getMessage());
        }
    }

    /**
     * 保存玩家档案
     */
    @Transactional
    public void savePlayerProfile(PlayerProfile playerProfile) {
        try {
            playerProfileMapper.updateById(playerProfile);
            log.info("保存玩家档案成功: ID={}", playerProfile.getId());
        } catch (Exception e) {
            log.error("保存玩家档案失败: ID={}", playerProfile.getId(), e);
            throw new RuntimeException("保存玩家档案失败: " + e.getMessage());
        }
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
            
            // 新手物品1: 疗伤丹 x1
            com.xiuxian.game.entity.PlayerItem item1 = com.xiuxian.game.entity.PlayerItem.builder()
                    .playerId(playerId)
                    .itemId(1)  // 疗伤丹
                    .quantity(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // 新手物品2: 回灵丹 x5
            com.xiuxian.game.entity.PlayerItem item2 = com.xiuxian.game.entity.PlayerItem.builder()
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
            throw new IllegalArgumentException("玩家不存在");
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
}