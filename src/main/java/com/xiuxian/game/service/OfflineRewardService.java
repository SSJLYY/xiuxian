package com.xiuxian.game.service;

import com.xiuxian.game.dto.response.OfflineRewardResponse;
import com.xiuxian.game.entity.CultivationLog;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.repository.CultivationLogRepository;
import com.xiuxian.game.repository.PlayerProfileRepository;
import com.xiuxian.game.util.GameCalculator;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfflineRewardService {

    private final PlayerService playerService;
    private final PlayerProfileRepository playerProfileRepository;
    private final CultivationLogRepository cultivationLogRepository;
    private final GameCalculator gameCalculator;

    @Transactional
    public OfflineRewardResponse calculateAndClaimOfflineRewards() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastOnlineTime = player.getLastOnlineTime();
        
        // 计算离线时间（秒）
        long offlineSeconds = Duration.between(lastOnlineTime, now).getSeconds();
        
        // 限制最大离线时间为24小时
        offlineSeconds = Math.min(offlineSeconds, GameConstants.MAX_OFFLINE_TIME_SECONDS);
        
        // 检查是否满足最小离线时间要求
        if (offlineSeconds < GameConstants.MIN_OFFLINE_TIME_FOR_REWARD) {
            return OfflineRewardResponse.builder()
                    .offlineMinutes(0)
                    .expGained(0L)
                    .spiritStonesGained(0L)
                    .cultivationPointsGained(0L)
                    .build();
        }
        
        // 使用GameCalculator计算离线收益
        long expGained = gameCalculator.calculateOfflineRewards(player, offlineSeconds);
        long spiritStonesGained = gameCalculator.calculateSpiritStonesPerSecond(player) * offlineSeconds;
        long cultivationPointsGained = offlineSeconds / 300; // 每5分钟获得1个修炼点
        
        // 更新玩家资料
        player.setExp(player.getExp() + expGained);
        player.setSpiritStones(player.getSpiritStones() + spiritStonesGained);
        player.setCultivationPoints(player.getCultivationPoints() + cultivationPointsGained);
        player.setTotalCultivationTime(player.getTotalCultivationTime() + offlineSeconds);
        player.setLastOnlineTime(now);
        
        // 检查升级
        gameCalculator.checkLevelUp(player);
        
        playerProfileRepository.save(player);
        
        // 记录修炼日志
        CultivationLog log = CultivationLog.builder()
                .playerProfile(player)
                .expGained(expGained)
                .spiritStonesGained((int)spiritStonesGained)
                .cultivationDuration(offlineSeconds)
                .isOffline(true)
                .build();
        cultivationLogRepository.save(log);
        
        // 返回离线收益信息
        return OfflineRewardResponse.builder()
                .offlineMinutes(offlineSeconds / 60)
                .expGained(expGained)
                .spiritStonesGained(spiritStonesGained)
                .cultivationPointsGained(cultivationPointsGained)
                .build();
    }

    public List<CultivationLog> getCultivationLogs() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return cultivationLogRepository.findByPlayerProfileOrderByCreatedAtDesc(player);
    }
}