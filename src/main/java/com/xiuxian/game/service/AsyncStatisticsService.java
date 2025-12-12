package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.entity.DailyStatistics;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.RechargeRecord;
import com.xiuxian.game.mapper.DailyStatisticsMapper;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.RechargeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncStatisticsService {
    
    private final DailyStatisticsMapper dailyStatisticsMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final RechargeRecordMapper rechargeRecordMapper;
    
    /**
     * 每天凌晨1点执行统计数据聚合
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Async("statisticsTaskExecutor")
    public void aggregateDailyStatistics() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            log.info("开始聚合每日统计数据: date={}", yesterday);
            
            // 检查是否已经存在统计数据
            QueryWrapper<DailyStatistics> existsWrapper = new QueryWrapper<>();
            existsWrapper.eq("stat_date", yesterday);
            DailyStatistics existingStats = dailyStatisticsMapper.selectOne(existsWrapper);
            
            if (existingStats != null) {
                log.info("统计数据已存在，跳过聚合: date={}", yesterday);
                return;
            }
            
            // 计算各项统计数据
            DailyStatistics stats = new DailyStatistics();
            stats.setStatDate(yesterday);
            
            // 新增玩家数
            int newPlayers = countNewPlayers(yesterday);
            stats.setNewPlayers(newPlayers);
            
            // 活跃玩家数
            int activePlayers = countActivePlayers(yesterday);
            stats.setActivePlayers(activePlayers);
            
            // 充值统计
            RechargeStats rechargeStats = calculateRechargeStats(yesterday);
            stats.setTotalRecharge((int) rechargeStats.getTotalAmount());
            stats.setPayingPlayers(rechargeStats.getPayingPlayers());
            
            // 计算ARPU和ARPPU
            if (activePlayers > 0) {
                BigDecimal arpu = BigDecimal.valueOf(rechargeStats.getTotalAmount())
                        .divide(BigDecimal.valueOf(activePlayers), 2, BigDecimal.ROUND_HALF_UP);
                stats.setArpu(arpu);
            }
            
            if (rechargeStats.getPayingPlayers() > 0) {
                BigDecimal arppu = BigDecimal.valueOf(rechargeStats.getTotalAmount())
                        .divide(BigDecimal.valueOf(rechargeStats.getPayingPlayers()), 2, BigDecimal.ROUND_HALF_UP);
                stats.setArppu(arppu);
            }
            
            // 保存统计数据
            dailyStatisticsMapper.insert(stats);
            
            log.info("每日统计数据聚合完成: date={}, newPlayers={}, activeUsers={}, totalRecharge={}", 
                    yesterday, newPlayers, activePlayers, rechargeStats.getTotalAmount());
            
        } catch (Exception e) {
            log.error("聚合每日统计数据失败", e);
        }
    }
    
    /**
     * 异步计算玩家留存率
     */
    @Async("statisticsTaskExecutor")
    public CompletableFuture<Double> calculateRetentionRateAsync(LocalDate date, int days) {
        try {
            log.debug("开始计算留存率: date={}, days={}", date, days);
            
            // 获取指定日期的新用户
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();
            
            QueryWrapper<PlayerProfile> newUsersWrapper = new QueryWrapper<>();
            newUsersWrapper.between("created_at", startTime, endTime);
            Long newUsersCount = playerProfileMapper.selectCount(newUsersWrapper);
            
            if (newUsersCount == 0) {
                return CompletableFuture.completedFuture(0.0);
            }
            
            // 获取在指定天数后仍然活跃的用户
            LocalDate retentionDate = date.plusDays(days);
            LocalDateTime retentionStartTime = retentionDate.atStartOfDay();
            LocalDateTime retentionEndTime = retentionDate.plusDays(1).atStartOfDay();
            
            QueryWrapper<PlayerProfile> retainedUsersWrapper = new QueryWrapper<>();
            retainedUsersWrapper.between("created_at", startTime, endTime)
                              .between("last_login_at", retentionStartTime, retentionEndTime);
            Long retainedUsersCount = playerProfileMapper.selectCount(retainedUsersWrapper);
            
            double retentionRate = (double) retainedUsersCount / newUsersCount * 100;
            
            log.debug("留存率计算完成: date={}, days={}, rate={}%", date, days, retentionRate);
            return CompletableFuture.completedFuture(retentionRate);
            
        } catch (Exception e) {
            log.error("计算留存率失败: date={}, days={}", date, days, e);
            return CompletableFuture.completedFuture(0.0);
        }
    }
    
    /**
     * 异步生成统计报表
     */
    @Async("statisticsTaskExecutor")
    public CompletableFuture<String> generateStatisticsReportAsync(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("开始生成统计报表: startDate={}, endDate={}", startDate, endDate);
            
            StringBuilder report = new StringBuilder();
            report.append("统计报表\n");
            report.append("时间范围: ").append(startDate).append(" 至 ").append(endDate).append("\n\n");
            
            // 获取期间的统计数据
            QueryWrapper<DailyStatistics> wrapper = new QueryWrapper<>();
            wrapper.between("stat_date", startDate, endDate)
                   .orderByAsc("stat_date");
            
            List<DailyStatistics> dailyStats = dailyStatisticsMapper.selectList(wrapper);
            
            int totalNewPlayers = 0;
            long totalRecharge = 0;
            int totalPayingPlayers = 0;
            
            for (DailyStatistics stats : dailyStats) {
                totalNewPlayers += stats.getNewPlayers();
                totalRecharge += stats.getTotalRecharge();
                totalPayingPlayers += stats.getPayingPlayers();
                
                report.append(String.format("%s: 新增用户=%d, 活跃用户=%d, 充值金额=%d, 付费用户=%d\n",
                        stats.getStatDate(), stats.getNewPlayers(), stats.getActivePlayers(),
                        stats.getTotalRecharge(), stats.getPayingPlayers()));
            }
            
            report.append("\n汇总数据:\n");
            report.append("总新增用户: ").append(totalNewPlayers).append("\n");
            report.append("总充值金额: ").append(totalRecharge).append("\n");
            report.append("总付费用户: ").append(totalPayingPlayers).append("\n");
            
            if (totalNewPlayers > 0) {
                double payRate = (double) totalPayingPlayers / totalNewPlayers * 100;
                report.append("付费率: ").append(String.format("%.2f%%", payRate)).append("\n");
            }
            
            String reportContent = report.toString();
            log.info("统计报表生成完成: 长度={}", reportContent.length());
            
            return CompletableFuture.completedFuture(reportContent);
            
        } catch (Exception e) {
            log.error("生成统计报表失败: startDate={}, endDate={}", startDate, endDate, e);
            return CompletableFuture.completedFuture("报表生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算新增玩家数
     */
    private int countNewPlayers(LocalDate date) {
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();
        
        QueryWrapper<PlayerProfile> wrapper = new QueryWrapper<>();
        wrapper.between("created_at", startTime, endTime);
        
        return Math.toIntExact(playerProfileMapper.selectCount(wrapper));
    }
    
    /**
     * 计算活跃玩家数
     */
    private int countActivePlayers(LocalDate date) {
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();
        
        QueryWrapper<PlayerProfile> wrapper = new QueryWrapper<>();
        wrapper.between("last_login_at", startTime, endTime);
        
        return Math.toIntExact(playerProfileMapper.selectCount(wrapper));
    }
    
    /**
     * 计算充值统计
     */
    private RechargeStats calculateRechargeStats(LocalDate date) {
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();
        
        QueryWrapper<RechargeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "SUCCESS")
               .between("completed_at", startTime, endTime);
        
        List<RechargeRecord> rechargeRecords = rechargeRecordMapper.selectList(wrapper);
        
        long totalAmount = 0;
        int payingPlayers = 0;
        
        if (!rechargeRecords.isEmpty()) {
            totalAmount = rechargeRecords.stream()
                    .mapToLong(record -> record.getAmount())
                    .sum();
            
            payingPlayers = (int) rechargeRecords.stream()
                    .mapToInt(record -> record.getPlayerId())
                    .distinct()
                    .count();
        }
        
        return new RechargeStats(totalAmount, payingPlayers);
    }
    
    /**
     * 充值统计内部类
     */
    private static class RechargeStats {
        private final long totalAmount;
        private final int payingPlayers;
        
        public RechargeStats(long totalAmount, int payingPlayers) {
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