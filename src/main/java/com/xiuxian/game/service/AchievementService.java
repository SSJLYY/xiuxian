package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.entity.Achievement;
import com.xiuxian.game.entity.PlayerAchievement;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.exception.BusinessException;
import com.xiuxian.game.exception.ErrorCode;
import com.xiuxian.game.mapper.AchievementMapper;
import com.xiuxian.game.mapper.PlayerAchievementMapper;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 成就服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final PlayerAchievementMapper playerAchievementMapper;
    private final PlayerProfileMapper playerProfileMapper;

    /**
     * 获取所有成就
     */
    public List<Achievement> getAllAchievements() {
        return achievementMapper.selectList(null);
    }

    /**
     * 获取玩家成就列表
     */
    public List<PlayerAchievement> getPlayerAchievements(Integer playerId) {
        return playerAchievementMapper.selectList(
                new QueryWrapper<PlayerAchievement>().eq("player_id", playerId));
    }

    /**
     * 更新成就进度
     */
    @Transactional
    public void updateAchievementProgress(Integer playerId, Long achievementId, int progress) {
        PlayerAchievement playerAchievement = playerAchievementMapper.selectOne(
                new QueryWrapper<PlayerAchievement>()
                        .eq("player_id", playerId)
                        .eq("achievement_id", achievementId));
        
        if (playerAchievement == null) {
            playerAchievement = new PlayerAchievement();
            playerAchievement.setPlayerId(playerId);
            playerAchievement.setAchievementId(achievementId.intValue());
            playerAchievement.setProgress(progress);
            playerAchievement.setIsCompleted(false);
            playerAchievement.setIsClaimed(false);
            playerAchievementMapper.insert(playerAchievement);
        } else {
            playerAchievement.setProgress(progress);
            playerAchievementMapper.updateById(playerAchievement);
        }
        
        // 检查是否完成
        Achievement achievement = achievementMapper.selectById(achievementId);
        if (achievement != null && progress >= achievement.getConditionValue() && !playerAchievement.getIsCompleted()) {
            playerAchievement.setIsCompleted(true);
            playerAchievement.setCompletedAt(LocalDateTime.now());
            playerAchievementMapper.updateById(playerAchievement);
            log.info("玩家完成成就: playerId={}, achievementId={}", playerId, achievementId);
        }
    }

    /**
     * 领取成就奖励
     */
    @Transactional
    public void claimAchievementReward(Integer playerId, Long achievementId) {
        PlayerAchievement playerAchievement = playerAchievementMapper.selectOne(
                new QueryWrapper<PlayerAchievement>()
                        .eq("player_id", playerId)
                        .eq("achievement_id", achievementId));
        
        if (playerAchievement == null || !playerAchievement.getIsCompleted()) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_NOT_COMPLETED);
        }
        
        if (playerAchievement.getIsClaimed()) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_ALREADY_CLAIMED);
        }
        
        Achievement achievement = achievementMapper.selectById(achievementId);
        if (achievement == null) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_NOT_FOUND);
        }
        
        // 发放奖励
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (achievement.getRewardSpiritStones() != null && achievement.getRewardSpiritStones() > 0) {
            profile.setSpiritStones(profile.getSpiritStones() + achievement.getRewardSpiritStones());
        }
        if (achievement.getRewardExp() != null && achievement.getRewardExp() > 0) {
            profile.setExp(profile.getExp() + achievement.getRewardExp());
        }
        playerProfileMapper.updateById(profile);
        
        // 标记为已领取
        playerAchievement.setIsClaimed(true);
        playerAchievement.setClaimedAt(LocalDateTime.now());
        playerAchievementMapper.updateById(playerAchievement);
        
        log.info("玩家领取成就奖励: playerId={}, achievementId={}", playerId, achievementId);
    }
}
