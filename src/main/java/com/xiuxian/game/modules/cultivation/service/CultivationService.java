package com.xiuxian.game.modules.cultivation.service;

import com.xiuxian.game.modules.cultivation.entity.CultivationLog;
import com.xiuxian.game.modules.cultivation.mapper.CultivationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CultivationService {

    private final CultivationLogMapper cultivationLogMapper;

    public static final long MAX_CULTIVATION_DURATION_MS = 24 * 60 * 60 * 1000L;

    @Transactional
    public CultivationLog recordCultivation(Integer playerId, Long expGained, Integer spiritStonesGained, 
                                            Long cultivationDuration, Boolean isOffline) {
        CultivationLog cultivationLog = CultivationLog.builder()
                .playerId(playerId)
                .expGained(expGained)
                .spiritStonesGained(spiritStonesGained)
                .cultivationDuration(cultivationDuration)
                .isOffline(isOffline != null ? isOffline : false)
                .createdAt(LocalDateTime.now())
                .build();
        
        cultivationLogMapper.insert(cultivationLog);
        log.info("记录修炼日志: playerId={}, exp={}, spiritStones={}, duration={}ms, isOffline={}",
                playerId, expGained, spiritStonesGained, cultivationDuration, isOffline);
        
        return cultivationLog;
    }

    public List<CultivationLog> getPlayerCultivationLogs(Integer playerId, int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }
        return cultivationLogMapper.selectByPlayerId(playerId, limit);
    }

    public CultivationLog getLatestCultivationLog(Integer playerId) {
        List<CultivationLog> logs = cultivationLogMapper.selectByPlayerId(playerId, 1);
        return logs.isEmpty() ? null : logs.get(0);
    }

    public long getTotalCultivationTime(Integer playerId) {
        List<CultivationLog> logs = cultivationLogMapper.selectAllByPlayerId(playerId);
        return logs.stream()
                .mapToLong(log -> log.getCultivationDuration() != null ? log.getCultivationDuration() : 0L)
                .sum();
    }

    public long getTotalExpFromCultivation(Integer playerId) {
        List<CultivationLog> logs = cultivationLogMapper.selectAllByPlayerId(playerId);
        return logs.stream()
                .mapToLong(log -> log.getExpGained() != null ? log.getExpGained() : 0L)
                .sum();
    }

    public long getTotalSpiritStonesFromCultivation(Integer playerId) {
        List<CultivationLog> logs = cultivationLogMapper.selectAllByPlayerId(playerId);
        return logs.stream()
                .mapToLong(log -> log.getSpiritStonesGained() != null ? log.getSpiritStonesGained() : 0L)
                .sum();
    }
}
