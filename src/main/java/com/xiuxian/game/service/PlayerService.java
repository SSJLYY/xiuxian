package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerProfileMapper playerProfileMapper;
    private final UserMapper userMapper;

    /**
     * 创建新玩家档案
     */
    @Transactional
    public PlayerProfile createNewPlayer(User user, String nickname) {
        try {
            log.info("为用户创建玩家档案: {}", user.getUsername());

            PlayerProfile playerProfile = PlayerProfile.builder()
                    .userId(user.getId())
                    .nickname(nickname != null ? nickname : user.getUsername())
                    .level(1)
                    .exp(0L)
                    .expToNext(100L)
                    .realm("练气期")
                    .cultivationSpeed(BigDecimal.ONE)
                    .spiritStones(1000L)
                    .cultivationPoints(0L)
                    .contributionPoints(0L)
                    .attack(10)
                    .defense(5)
                    .health(100)
                    .mana(50)
                    .speed(10)
                    .isCultivating(false)
                    .lastOnlineTime(LocalDateTime.now())
                    .totalCultivationTime(0L)
                    .build();

            playerProfileMapper.insert(playerProfile);
            PlayerProfile savedProfile = playerProfileMapper.selectById(playerProfile.getId());
            log.info("玩家档案创建成功: ID={}", savedProfile.getId());

            return savedProfile;

        } catch (Exception e) {
            log.error("创建玩家档案失败: {}", user.getUsername(), e);
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
     */
    @Transactional
    public void cultivate() {
        try {
            PlayerProfile profile = getCurrentPlayerProfile();
            log.info("开始修炼前，玩家 {} 的修炼状态: {}", profile.getId(), profile.getIsCultivating());

            // 确保isCultivating不为null
            if (profile.getIsCultivating() == null) {
                profile.setIsCultivating(false);
            }
            
            if (profile.getIsCultivating()) {
                throw new RuntimeException("已经在修炼中");
            }

            profile.setIsCultivating(true);
            profile.setLastCultivationStart(LocalDateTime.now());
            playerProfileMapper.updateById(profile);
            
            log.info("玩家开始修炼: ID={}", profile.getId());
        } catch (Exception e) {
            log.error("开始修炼失败", e);
            throw new RuntimeException("开始修炼失败: " + e.getMessage());
        }
    }

    /**
     * 停止修炼
     */
    @Transactional
    public void stopCultivate() {
        try {
            PlayerProfile profile = getCurrentPlayerProfile();
            log.info("停止修炼前，玩家 {} 的修炼状态: {}", profile.getId(), profile.getIsCultivating());

            if (!profile.getIsCultivating()) {
                // 即使状态已经是false，也尝试重置以防万一
                profile.setIsCultivating(false);
                playerProfileMapper.updateById(profile);
                throw new RuntimeException("当前没有在修炼");
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = profile.getLastCultivationStart();
            
            if (startTime != null) {
                // 计算修炼时间（秒）
                long cultivationTimeSeconds = java.time.Duration.between(startTime, now).getSeconds();
                
                // 限制最大修炼时间为24小时（防止异常情况）
                long maxCultivationTime = 24 * 60 * 60; // 24小时
                long actualCultivationTime = Math.min(cultivationTimeSeconds, maxCultivationTime);
                
                // 转换为分钟用于总时间统计
                long cultivationTimeMinutes = actualCultivationTime / 60;
                profile.setTotalCultivationTime(profile.getTotalCultivationTime() + cultivationTimeMinutes);
                
                // 计算修炼收益（每秒获得基础经验 * 修炼速度）
                double baseExpPerSecond = 1.0; // 每秒获得1经验（加快速度）
                long expGained = (long) (actualCultivationTime * baseExpPerSecond * profile.getCultivationSpeed().doubleValue());
                
                // 限制单次修炼最大经验获得
                long maxExpPerCultivation = 3600; // 单次修炼最多获得3600经验（1小时）
                expGained = Math.min(expGained, maxExpPerCultivation);
                
                profile.setExp(profile.getExp() + expGained);
                
                log.info("玩家修炼完成: ID={}, 修炼时间={}秒, 获得经验={}", 
                        profile.getId(), actualCultivationTime, expGained);
                
                // 检查是否升级
                checkLevelUp(profile);
            }

            profile.setIsCultivating(false);
            profile.setLastCultivationEnd(now);
            playerProfileMapper.updateById(profile);
            
            log.info("玩家停止修炼: ID={}", profile.getId());
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
     */
    private void checkLevelUp(PlayerProfile profile) {
        // 防止无限循环，最多升级100次
        int maxLevelUps = 100;
        int levelUps = 0;
        
        while (profile.getExp() >= profile.getExpToNext() && levelUps < maxLevelUps) {
            profile.setLevel(profile.getLevel() + 1);
            profile.setExp(profile.getExp() - profile.getExpToNext());
            profile.setExpToNext(profile.getExpToNext() * 2); // 下一级所需经验翻倍
            
            // 升级属性提升
            profile.setAttack(profile.getAttack() + 5);
            profile.setDefense(profile.getDefense() + 3);
            profile.setHealth(profile.getHealth() + 20);
            profile.setMana(profile.getMana() + 10);
            profile.setSpeed(profile.getSpeed() + 1);
            
            // 更新境界
            updateRealm(profile);
            
            log.info("玩家升级: ID={}, 新等级={}, 新境界={}", profile.getId(), profile.getLevel(), profile.getRealm());
            levelUps++;
        }
        
        if (levelUps >= maxLevelUps) {
            log.warn("玩家升级次数过多，可能存在问题: ID={}", profile.getId());
        }
    }
    
    /**
     * 根据等级更新境界
     */
    private void updateRealm(PlayerProfile profile) {
        int level = profile.getLevel();
        if (level >= 2001) profile.setRealm("渡劫期");
        else if (level >= 1501) profile.setRealm("大乘期");
        else if (level >= 1001) profile.setRealm("合体期");
        else if (level >= 701) profile.setRealm("化神期");
        else if (level >= 401) profile.setRealm("元婴期");
        else if (level >= 201) profile.setRealm("金丹期");
        else if (level >= 101) profile.setRealm("筑基期");
        else profile.setRealm("练气期");
    }
}