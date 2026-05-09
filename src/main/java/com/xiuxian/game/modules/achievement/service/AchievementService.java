package com.xiuxian.game.modules.achievement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.achievement.entity.Achievement;
import com.xiuxian.game.modules.achievement.entity.PlayerAchievement;
import com.xiuxian.game.modules.achievement.mapper.AchievementMapper;
import com.xiuxian.game.modules.achievement.mapper.PlayerAchievementMapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final PlayerAchievementMapper playerAchievementMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;

    public List<Achievement> getAllAchievements() {
        return achievementMapper.selectList(
                new QueryWrapper<Achievement>()
                        .orderByAsc("achievement_type")
                        .orderByAsc("sort_order")
                        .orderByAsc("id"));
    }

    public List<PlayerAchievement> getPlayerAchievements(Integer playerId) {
        return playerAchievementMapper.selectList(
                new QueryWrapper<PlayerAchievement>().eq("player_id", playerId));
    }

    @Transactional
    public void updateAchievementProgress(Integer playerId, Long achievementId, int progress) {
        Achievement achievement = achievementMapper.selectById(achievementId);
        if (achievement == null) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_NOT_FOUND);
        }

        int normalizedProgress = Math.max(progress, 0);
        int completedFlag = normalizedProgress >= achievement.getConditionValue() ? 1 : 0;
        LocalDateTime now = LocalDateTime.now();
        playerAchievementMapper.upsertProgress(
                playerId,
                achievementId.intValue(),
                normalizedProgress,
                completedFlag,
                now);

        if (completedFlag == 1) {
            log.info("玩家成就达标: playerId={}, achievementId={}, progress={}",
                    playerId, achievementId, normalizedProgress);
        }
    }

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
        if (playerAchievement == null || !Boolean.TRUE.equals(playerAchievement.getIsCompleted())) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_NOT_COMPLETED);
        }
        if (Boolean.TRUE.equals(playerAchievement.getIsClaimed())) {
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

        if (achievement.getRewardSpiritStones() != null && achievement.getRewardSpiritStones() > 0) {
            profile.setSpiritStones(defaultLong(profile.getSpiritStones()) + achievement.getRewardSpiritStones());
        }
        if (achievement.getRewardExp() != null && achievement.getRewardExp() > 0) {
            profile.setExp(defaultLong(profile.getExp()) + achievement.getRewardExp());
        }
        playerService.applyLevelUpsWithoutCommit(profile, 100);
        playerService.savePlayerProfile(profile);

        log.info("玩家领取成就奖励: playerId={}, achievementId={}", playerId, achievementId);
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
