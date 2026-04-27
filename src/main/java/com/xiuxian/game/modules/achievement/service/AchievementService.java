package com.xiuxian.game.modules.achievement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.achievement.entity.Achievement;
import com.xiuxian.game.modules.achievement.entity.PlayerAchievement;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.achievement.mapper.AchievementMapper;
import com.xiuxian.game.modules.achievement.mapper.PlayerAchievementMapper;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
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
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据

    /**
     * 获取所有成就
     */
    public List<Achievement> getAllAchievements() {
        return achievementMapper.selectList(
                new QueryWrapper<Achievement>()
                        .orderByAsc("achievement_type")
                        .orderByAsc("sort_order")
                        .orderByAsc("id")
        );
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
        
        // 模块边界：通过PlayerService访问玩家数据
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
        PlayerProfile profile = playerProfileMapper.selectByIdForUpdate(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

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
        
        int claimedRows = playerAchievementMapper.claimAchievementIfUnclaimed(
                playerAchievement.getId(), LocalDateTime.now());
        if (claimedRows == 0) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_ALREADY_CLAIMED);
        }

        // 发放奖励
        if (achievement.getRewardSpiritStones() != null && achievement.getRewardSpiritStones() > 0) {
            profile.setSpiritStones(profile.getSpiritStones() + achievement.getRewardSpiritStones());
        }
        if (achievement.getRewardExp() != null && achievement.getRewardExp() > 0) {
            profile.setExp(profile.getExp() + achievement.getRewardExp());
        }
        playerService.applyLevelUpsWithoutCommit(profile, 100);
        playerService.savePlayerProfile(profile);
        
        log.info("玩家领取成就奖励: playerId={}, achievementId={}", playerId, achievementId);
    }
}

