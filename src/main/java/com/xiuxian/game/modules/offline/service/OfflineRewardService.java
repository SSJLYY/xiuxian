package com.xiuxian.game.modules.offline.service;

import com.xiuxian.game.modules.offline.entity.OfflineReward;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.offline.mapper.OfflineRewardMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfflineRewardService {

    private final OfflineRewardMapper offlineRewardMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据
    
    // 离线收益配置
    private static final int MAX_OFFLINE_HOURS = 12; // 模块边界：通过PlayerService访问玩家数据
    private static final int BASE_EXP_PER_HOUR = 50;
    private static final int BASE_SPIRIT_STONES_PER_HOUR = 10;

    /**
     * 计算离线收益
     */
    @Transactional
    public Map<String, Object> calculateOfflineReward(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        // 获取上次登录时间
        LocalDateTime lastLoginTime = player.getLastLoginAt();
        if (lastLoginTime == null) {
            lastLoginTime = player.getCreatedAt();
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(lastLoginTime, now);
        long offlineMinutes = duration.toMinutes();

        // 模块边界：通过PlayerService访问玩家数据
        if (offlineMinutes < 10) {
            Map<String, Object> result = new HashMap<>();
            result.put("hasReward", false);
            result.put("message", "离线时间不足10分钟");
            return result;
        }

        // 模块边界：通过PlayerService访问玩家数据
        long maxMinutes = MAX_OFFLINE_HOURS * 60;
        long effectiveMinutes = Math.min(offlineMinutes, maxMinutes);
        
        // 检查是否已经领取过这个时间段的奖励（防刷）
        OfflineReward existingReward = offlineRewardMapper.selectLatestByPlayerId(playerId);
        if (existingReward != null && !existingReward.getClaimed()) {
            // 如果有未领取的奖励，直接返回
            Map<String, Object> result = new HashMap<>();
            result.put("hasReward", true);
            result.put("offlineMinutes", existingReward.getOfflineMinutes());
            result.put("expGained", existingReward.getExpGained());
            result.put("spiritStonesGained", existingReward.getSpiritStonesGained());
            result.put("rewardId", existingReward.getId());
            return result;
        }

        // 根据玩家等级计算收益
        int expPerHour = BASE_EXP_PER_HOUR + player.getLevel() * 5;
        int spiritStonesPerHour = BASE_SPIRIT_STONES_PER_HOUR + player.getLevel() * 2;
        
        // 模块边界：通过PlayerService访问玩家数据
        int totalExp = (int)(expPerHour * effectiveMinutes / 60.0);
        int totalSpiritStones = (int)(spiritStonesPerHour * effectiveMinutes / 60.0);
        
        // 创建离线奖励记录
        OfflineReward reward = OfflineReward.builder()
                .playerId(playerId)
                .offlineMinutes((int)effectiveMinutes)
                .expGained(totalExp)
                .spiritStonesGained(totalSpiritStones)
                .claimed(false)
                .createdAt(now)
                .build();
        
        offlineRewardMapper.insert(reward);

        // 模块边界：通过PlayerService访问玩家数据
        player.setLastLoginAt(now);
        playerService.savePlayerProfile(player);

        Map<String, Object> result = new HashMap<>();
        result.put("hasReward", true);
        result.put("offlineMinutes", effectiveMinutes);
        result.put("offlineHours", String.format("%.1f", effectiveMinutes / 60.0));
        result.put("expGained", totalExp);
        result.put("spiritStonesGained", totalSpiritStones);
        result.put("rewardId", reward.getId());
        result.put("message", "离线" + (effectiveMinutes / 60) + "小时" + (effectiveMinutes % 60) + "分钟");
        
        return result;
    }

    /**
     * 领取离线收益
     */
    @Transactional
    public Map<String, Object> claimOfflineReward(Integer playerId, Integer rewardId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        OfflineReward reward = offlineRewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "奖励不存在");
        }

        if (!reward.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权领取该奖励");
        }

        if (reward.getClaimed()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "奖励已被领取");
        }

        // 发放奖励
        player.setExp(player.getExp() + reward.getExpGained());
        player.setSpiritStones(player.getSpiritStones() + reward.getSpiritStonesGained());
        
        // 模块边界：通过PlayerService访问玩家数据
        boolean leveledUp = false;
        int oldLevel = player.getLevel();
        while (player.getExp() >= player.getExpToNext()) {
            player.setExp(player.getExp() - player.getExpToNext());
            player.setLevel(player.getLevel() + 1);
            player.setExpToNext((long)(player.getExpToNext() * 1.5));
            
            // 模块边界：通过PlayerService访问玩家数据
            player.setHealth(player.getHealth() + 10);
            player.setMana(player.getMana() + 5);
            player.setAttack(player.getAttack() + 2);
            player.setDefense(player.getDefense() + 1);
            player.setAttributePoints(player.getAttributePoints() + 5);
            leveledUp = true;
        }
        
        playerService.savePlayerProfile(player);

        // 模块边界：通过PlayerService访问玩家数据
        reward.setClaimed(true);
        reward.setClaimedAt(LocalDateTime.now());
        offlineRewardMapper.updateById(reward);

        Map<String, Object> result = new HashMap<>();
        result.put("expGained", reward.getExpGained());
        result.put("spiritStonesGained", reward.getSpiritStonesGained());
        result.put("currentExp", player.getExp());
        result.put("currentSpiritStones", player.getSpiritStones());
        result.put("leveledUp", leveledUp);
        if (leveledUp) {
            result.put("oldLevel", oldLevel);
            result.put("newLevel", player.getLevel());
        }
        
        return result;
    }

    /**
     * 获取未领取的离线奖励
     */
    public List<OfflineReward> getUnclaimedRewards(Integer playerId) {
        return offlineRewardMapper.selectUnclaimedByPlayerId(playerId);
    }
}

