package com.xiuxian.game.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.admin.entity.DailyStatistics;
import com.xiuxian.game.modules.admin.mapper.DailyStatisticsMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.vip.entity.RechargeRecord;
import com.xiuxian.game.modules.vip.service.RechargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步统计服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncStatisticsService {

    private final DailyStatisticsMapper dailyStatisticsMapper;
    private final PlayerService playerService;
    private final RechargeService rechargeService;

    /**
     * 每天凌晨 1 点聚合前一天的统计数据。
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void aggregateDailyStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始聚合每日统计数据: date={}", yesterday);

        try {
            DailyStatistics stats = new DailyStatistics();
            stats.setStatDate(yesterday);

            int newPlayers = countNewPlayers(yesterday);
            int activePlayers = countActivePlayers(yesterday);
            RechargeStats rechargeStats = calculateRechargeStats(yesterday);
            long totalRechargeAmount = rechargeStats.getTotalAmount();

            stats.setNewPlayers(newPlayers);
            stats.setActivePlayers(activePlayers);
            stats.setPayingPlayers(rechargeStats.getPayingPlayers());
            stats.setTotalRecharge((int) Math.min(totalRechargeAmount, Integer.MAX_VALUE));
            stats.setArpu(BigDecimal.ZERO);
            stats.setArppu(BigDecimal.ZERO);

            if (activePlayers > 0) {
                stats.setArpu(BigDecimal.valueOf(totalRechargeAmount)
                        .divide(BigDecimal.valueOf(activePlayers), 2, RoundingMode.HALF_UP));
            }

            if (rechargeStats.getPayingPlayers() > 0) {
                stats.setArppu(BigDecimal.valueOf(totalRechargeAmount)
                        .divide(BigDecimal.valueOf(rechargeStats.getPayingPlayers()), 2, RoundingMode.HALF_UP));
            }

            dailyStatisticsMapper.insert(stats);
            log.info("每日统计数据聚合完成: date={}, newPlayers={}, activePlayers={}, totalRecharge={}",
                    yesterday, newPlayers, activePlayers, totalRechargeAmount);
        } catch (DuplicateKeyException e) {
            log.info("统计数据已存在，跳过聚合: date={}", yesterday);
        } catch (Exception e) {
            log.error("聚合每日统计数据失败", e);
        }
    }

    /**
     * 异步计算留存率。
     */
    @Async("statisticsTaskExecutor")
    public CompletableFuture<Double> calculateRetentionRateAsync(LocalDate date, int days) {
        try {
            log.debug("开始计算留存率: date={}, days={}", date, days);

            LocalDateTime[] dateRange = getDateRange(date);
            Long newUsersCount = playerService.countNewPlayers(dateRange[0], dateRange[1]);
            if (newUsersCount == 0) {
                return CompletableFuture.completedFuture(0.0);
            }

            LocalDate retentionDate = date.plusDays(days);
            LocalDateTime[] retentionRange = getDateRange(retentionDate);
            Long retainedUsersCount = playerService.countRetainedPlayers(
                    dateRange[0], dateRange[1], retentionRange[0], retentionRange[1]);

            double retentionRate = (double) retainedUsersCount / newUsersCount * 100;
            log.debug("留存率计算完成: date={}, days={}, rate={}%", date, days, retentionRate);
            return CompletableFuture.completedFuture(retentionRate);
        } catch (Exception e) {
            log.error("计算留存率失败: date={}, days={}", date, days, e);
            return CompletableFuture.completedFuture(0.0);
        }
    }

    /**
     * 异步生成统计报表。
     */
    @Async("statisticsTaskExecutor")
    public CompletableFuture<String> generateStatisticsReportAsync(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("开始生成统计报表: startDate={}, endDate={}", startDate, endDate);

            QueryWrapper<DailyStatistics> wrapper = new QueryWrapper<>();
            wrapper.between("stat_date", startDate, endDate).orderByAsc("stat_date");
            List<DailyStatistics> dailyStats = dailyStatisticsMapper.selectList(wrapper);

            StringBuilder report = new StringBuilder();
            report.append("统计报表").append(System.lineSeparator());
            report.append("时间范围: ").append(startDate).append(" 至 ").append(endDate)
                    .append(System.lineSeparator()).append(System.lineSeparator());

            int totalNewPlayers = 0;
            long totalRecharge = 0;
            int totalPayingPlayers = 0;

            for (DailyStatistics stats : dailyStats) {
                totalNewPlayers += stats.getNewPlayers();
                totalRecharge += stats.getTotalRecharge();
                totalPayingPlayers += stats.getPayingPlayers();

                report.append(String.format(
                        "%s: 新增用户=%d, 活跃用户=%d, 充值金额=%d, 付费用户=%d%n",
                        stats.getStatDate(),
                        stats.getNewPlayers(),
                        stats.getActivePlayers(),
                        stats.getTotalRecharge(),
                        stats.getPayingPlayers()));
            }

            report.append(System.lineSeparator())
                    .append("汇总数据").append(System.lineSeparator())
                    .append("总新增用户: ").append(totalNewPlayers).append(System.lineSeparator())
                    .append("总充值金额: ").append(totalRecharge).append(System.lineSeparator())
                    .append("总付费用户: ").append(totalPayingPlayers).append(System.lineSeparator());

            if (totalNewPlayers > 0) {
                double payRate = (double) totalPayingPlayers / totalNewPlayers * 100;
                report.append("付费率: ").append(String.format("%.2f%%", payRate)).append(System.lineSeparator());
            }

            String reportContent = report.toString();
            log.info("统计报表生成完成: 长度={}", reportContent.length());
            return CompletableFuture.completedFuture(reportContent);
        } catch (Exception e) {
            log.error("生成统计报表失败: startDate={}, endDate={}", startDate, endDate, e);
            return CompletableFuture.completedFuture("报表生成失败: " + e.getMessage());
        }
    }

    private LocalDateTime[] getDateRange(LocalDate date) {
        return new LocalDateTime[]{date.atStartOfDay(), date.plusDays(1).atStartOfDay()};
    }

    private int countNewPlayers(LocalDate date) {
        LocalDateTime[] range = getDateRange(date);
        return Math.toIntExact(playerService.countNewPlayers(range[0], range[1]));
    }

    private int countActivePlayers(LocalDate date) {
        LocalDateTime[] range = getDateRange(date);
        return Math.toIntExact(playerService.countActivePlayers(range[0]));
    }

    private RechargeStats calculateRechargeStats(LocalDate date) {
        LocalDateTime[] range = getDateRange(date);
        List<RechargeRecord> rechargeRecords = rechargeService.getSuccessRechargesByDateRange(range[0], range[1]);

        long totalAmount = 0;
        int payingPlayers = 0;
        if (!rechargeRecords.isEmpty()) {
            totalAmount = rechargeRecords.stream()
                    .mapToLong(record -> {
                        Integer amount = record.getAmount();
                        return amount != null ? amount.longValue() : 0L;
                    })
                    .sum();

            payingPlayers = (int) rechargeRecords.stream()
                    .mapToInt(record -> {
                        Integer playerId = record.getPlayerId();
                        return playerId != null ? playerId : 0;
                    })
                    .distinct()
                    .count();
        }

        return new RechargeStats(totalAmount, payingPlayers);
    }

    private static class RechargeStats {
        private final long totalAmount;
        private final int payingPlayers;

        private RechargeStats(long totalAmount, int payingPlayers) {
            this.totalAmount = totalAmount;
            this.payingPlayers = payingPlayers;
        }

        public long getTotalAmount() {
            return totalAmount;
        }

        public int getPayingPlayers() {
            return payingPlayers;
        }
    }
}
