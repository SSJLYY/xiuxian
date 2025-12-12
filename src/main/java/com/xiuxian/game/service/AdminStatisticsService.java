package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.entity.*;
import com.xiuxian.game.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final DailyStatisticsMapper dailyStatisticsMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final UserMapper userMapper;
    private final RechargeRecordMapper rechargeRecordMapper;
    private final PlayerLoginLogMapper playerLoginLogMapper;
    private final AuctionItemMapper auctionItemMapper;
    private final PlayerMailMapper playerMailMapper;

    /**
     * 获取综合统计数据
     *
     * @return 统计数据
     */
    public Map<String, Object> getOverallStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总玩家数
        long totalPlayers = userMapper.selectCount(null);

        // 在线玩家数（最近5分钟）
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        QueryWrapper<PlayerProfile> onlineQuery = new QueryWrapper<>();
        onlineQuery.gt("last_online_time", fiveMinutesAgo);
        long onlinePlayers = playerProfileMapper.selectCount(onlineQuery);

        // 今日注册数
        LocalDateTime today = LocalDate.now().atStartOfDay();
        QueryWrapper<User> newUsersQuery = new QueryWrapper<>();
        newUsersQuery.gt("created_at", today);
        long newUsersToday = userMapper.selectCount(newUsersQuery);

        // 今日活跃数
        QueryWrapper<PlayerLoginLog> activeTodayQuery = new QueryWrapper<>();
        activeTodayQuery.gt("login_at", today);
        long activeToday = playerLoginLogMapper.selectCount(activeTodayQuery);

        // 总充值金额
        QueryWrapper<RechargeRecord> totalIncomeQuery = new QueryWrapper<>();
        totalIncomeQuery.eq("status", "SUCCESS");
        List<RechargeRecord> allRechargeRecords = rechargeRecordMapper.selectList(totalIncomeQuery);
        long totalIncome = allRechargeRecords.stream()
                .mapToLong(RechargeRecord::getAmount)
                .sum();

        // 今日充值金额
        QueryWrapper<RechargeRecord> todayIncomeQuery = new QueryWrapper<>();
        todayIncomeQuery.eq("status", "SUCCESS");
        todayIncomeQuery.gt("completed_at", today);
        List<RechargeRecord> todayRechargeRecords = rechargeRecordMapper.selectList(todayIncomeQuery);
        long todayIncome = todayRechargeRecords.stream()
                .mapToLong(RechargeRecord::getAmount)
                .sum();

        // 拍卖行统计
        long totalAuctions = auctionItemMapper.selectCount(null);
        QueryWrapper<AuctionItem> activeAuctionsQuery = new QueryWrapper<>();
        activeAuctionsQuery.eq("status", "ACTIVE");
        long activeAuctions = auctionItemMapper.selectCount(activeAuctionsQuery);

        // 邮件统计
        long totalMails = playerMailMapper.selectCount(null);
        QueryWrapper<PlayerMail> unreadMailsQuery = new QueryWrapper<>();
        unreadMailsQuery.eq("status", "UNREAD");
        long unreadMails = playerMailMapper.selectCount(unreadMailsQuery);

        stats.put("totalPlayers", totalPlayers);
        stats.put("onlinePlayers", onlinePlayers);
        stats.put("newUsersToday", newUsersToday);
        stats.put("activeToday", activeToday);
        stats.put("totalIncome", totalIncome);
        stats.put("todayIncome", todayIncome);
        stats.put("totalAuctions", totalAuctions);
        stats.put("activeAuctions", activeAuctions);
        stats.put("totalMails", totalMails);
        stats.put("unreadMails", unreadMails);

        return stats;
    }

    /**
     * 获取最近的统计数据
     *
     * @param days 天数
     * @return 统计数据列表
     */
    public List<DailyStatistics> getRecentStats(int days) {
        QueryWrapper<DailyStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("stat_date");
        queryWrapper.last("LIMIT " + days);
        return dailyStatisticsMapper.selectList(queryWrapper);
    }

    /**
     * 获取收入统计
     *
     * @param days 天数
     * @return 收入统计数据
     */
    public Map<String, Object> getRevenueStats(int days) {
        Map<String, Object> stats = new HashMap<>();

        // 获取最近几天的充值记录
        LocalDateTime startDate = LocalDate.now().minusDays(days - 1).atStartOfDay();
        QueryWrapper<RechargeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "SUCCESS");
        queryWrapper.ge("completed_at", startDate);
        queryWrapper.orderByAsc("completed_at");

        List<RechargeRecord> records = rechargeRecordMapper.selectList(queryWrapper);

        // 按日期分组统计
        Map<LocalDate, Long> dailyRevenue = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dailyRevenue.put(date, 0L);
        }

        for (RechargeRecord record : records) {
            LocalDate date = record.getCompletedAt().toLocalDate();
            dailyRevenue.merge(date, record.getAmount().longValue(), Long::sum);
        }

        stats.put("dailyRevenue", dailyRevenue);
        return stats;
    }

    /**
     * 获取玩家增长统计
     *
     * @param days 天数
     * @return 玩家增长统计数据
     */
    public Map<String, Object> getPlayerGrowthStats(int days) {
        Map<String, Object> stats = new HashMap<>();

        // 获取最近几天的每日统计数据
        QueryWrapper<DailyStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("stat_date");
        queryWrapper.last("LIMIT " + days);

        List<DailyStatistics> dailyStats = dailyStatisticsMapper.selectList(queryWrapper);

        // 按日期分组统计
        Map<LocalDate, Integer> dailyNewPlayers = new LinkedHashMap<>();
        Map<LocalDate, Integer> dailyActivePlayers = new LinkedHashMap<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dailyNewPlayers.put(date, 0);
            dailyActivePlayers.put(date, 0);
        }

        for (DailyStatistics stat : dailyStats) {
            dailyNewPlayers.put(stat.getStatDate(), stat.getNewPlayers());
            dailyActivePlayers.put(stat.getStatDate(), stat.getActivePlayers());
        }

        stats.put("dailyNewPlayers", dailyNewPlayers);
        stats.put("dailyActivePlayers", dailyActivePlayers);
        return stats;
    }
}