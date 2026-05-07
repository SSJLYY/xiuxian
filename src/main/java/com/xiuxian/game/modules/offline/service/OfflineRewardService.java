package com.xiuxian.game.modules.offline.service;

import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.offline.entity.OfflineReward;
import com.xiuxian.game.modules.offline.mapper.OfflineRewardMapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class OfflineRewardService {

    private final OfflineRewardMapper offlineRewardMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;

    private static final int MAX_OFFLINE_HOURS = 12;
    private static final int BASE_EXP_PER_HOUR = 50;
    private static final int BASE_SPIRIT_STONES_PER_HOUR = 10;
    private static final int MAX_OFFLINE_LEVEL_UPS = 5;

    @Transactional
    public Map<String, Object> calculateOfflineReward(Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        OfflineReward existingReward = offlineRewardMapper.selectLatestUnclaimedByPlayerId(playerId);
        if (existingReward != null) {
            return buildPendingRewardResponse(existingReward);
        }

        LocalDateTime lastLoginTime = player.getLastLoginAt();
        if (lastLoginTime == null) {
            lastLoginTime = player.getCreatedAt();
        }

        LocalDateTime now = LocalDateTime.now();
        long offlineMinutes = Duration.between(lastLoginTime, now).toMinutes();
        if (offlineMinutes < 10) {
            Map<String, Object> result = new HashMap<>();
            result.put("hasReward", false);
            result.put("message", "离线时间不足 10 分钟");
            return result;
        }

        long effectiveMinutes = Math.min(offlineMinutes, MAX_OFFLINE_HOURS * 60L);
        int playerLevel = defaultInt(player.getLevel(), 1);
        int expPerHour = BASE_EXP_PER_HOUR + playerLevel * 5;
        int spiritStonesPerHour = BASE_SPIRIT_STONES_PER_HOUR + playerLevel * 2;
        int totalExp = (int) (expPerHour * effectiveMinutes / 60.0);
        int totalSpiritStones = (int) (spiritStonesPerHour * effectiveMinutes / 60.0);

        OfflineReward reward = OfflineReward.builder()
                .playerId(playerId)
                .offlineMinutes((int) effectiveMinutes)
                .expGained(totalExp)
                .spiritStonesGained(totalSpiritStones)
                .claimed(false)
                .createdAt(now)
                .build();
        offlineRewardMapper.insert(reward);

        return buildPendingRewardResponse(reward);
    }

    @Transactional
    public Map<String, Object> claimOfflineReward(Integer playerId, Integer rewardId) {
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        OfflineReward reward = offlineRewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "离线奖励不存在");
        }
        if (!reward.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权领取该离线奖励");
        }
        if (Boolean.TRUE.equals(reward.getClaimed())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "离线奖励已领取");
        }

        LocalDateTime claimedAt = LocalDateTime.now();
        int claimedRows = offlineRewardMapper.claimRewardIfUnclaimed(rewardId, playerId, claimedAt);
        if (claimedRows == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "离线奖励已领取");
        }

        player.setExp(defaultLong(player.getExp()) + defaultInt(reward.getExpGained(), 0));
        player.setSpiritStones(defaultLong(player.getSpiritStones()) + defaultInt(reward.getSpiritStonesGained(), 0));

        int oldLevel = defaultInt(player.getLevel(), 1);
        int levelUps = playerService.applyLevelUpsWithoutCommit(player, MAX_OFFLINE_LEVEL_UPS);
        boolean leveledUp = levelUps > 0;

        player.setLastLoginAt(claimedAt);
        playerService.savePlayerProfile(player);

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

    public List<OfflineReward> getUnclaimedRewards(Integer playerId) {
        return offlineRewardMapper.selectUnclaimedByPlayerId(playerId);
    }

    private Map<String, Object> buildPendingRewardResponse(OfflineReward reward) {
        Map<String, Object> result = new HashMap<>();
        result.put("hasReward", true);
        result.put("offlineMinutes", reward.getOfflineMinutes());
        result.put("offlineHours", String.format("%.1f", reward.getOfflineMinutes() / 60.0));
        result.put("expGained", reward.getExpGained());
        result.put("spiritStonesGained", reward.getSpiritStonesGained());
        result.put("rewardId", reward.getId());
        result.put("message", "离线" + (reward.getOfflineMinutes() / 60) + "小时" +
                (reward.getOfflineMinutes() % 60) + "分钟");
        return result;
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
