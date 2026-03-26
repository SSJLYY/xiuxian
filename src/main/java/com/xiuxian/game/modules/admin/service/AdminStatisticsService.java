package com.xiuxian.game.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.admin.entity.DailyStatistics;
import com.xiuxian.game.modules.admin.mapper.DailyStatisticsMapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.modules.player.entity.PlayerLoginLog;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.mapper.UserMapper;
import com.xiuxian.game.modules.player.mapper.PlayerLoginLogMapper;
import com.xiuxian.game.modules.vip.entity.RechargeRecord;
import com.xiuxian.game.modules.vip.mapper.RechargeRecordMapper;
import com.xiuxian.game.modules.auction.entity.AuctionItem;
import com.xiuxian.game.modules.auction.mapper.AuctionItemMapper;
import com.xiuxian.game.modules.mail.entity.PlayerMail;
import com.xiuxian.game.modules.mail.mapper.PlayerMailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Admin 统计服务（聚合层）
 * 提供总体统计、收入统计、玩家增长等多维度运营报表。
 * admin 聚合层务实例外，允许直接访问各模块 Mapper。
 *
 * @author shaun.sheng
 */
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
     * 获取全局统计概览
     *
     * @return 包含玩家数、在线数、收入、拍卖行、邮件等维度的统计 Map
     */
    public Map<String, Object> getOverallStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总注册用户
        long totalPlayers = userMapper.selectCount(null);

        // 在线人数（5 分钟内有心跳）
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        QueryWrapper<PlayerProfile> onlineQuery = new QueryWrapper<>();
        onlineQuery.gt("last_online_time", fiveMinutesAgo);
        long onlinePlayers = playerProfileMapper.selectCount(onlineQuery);

        // 今日新增用户
        LocalDateTime today = LocalDate.now().atStartOfDay();
        QueryWrapper<User> newUsersQuery = new QueryWrapper<>();
        newUsersQuery.gt("created_at", today);
        long newUsersToday = userMapper.selectCount(newUsersQuery);

        // 今日活跃玩家
        QueryWrapper<PlayerLoginLog> activeTodayQuery = new QueryWrapper<>();
        activeTodayQuery.gt("login_at", today);
        long activeToday = playerLoginLogMapper.selectCount(activeTodayQuery);

        // 累计总收入
        QueryWrapper<RechargeRecord> totalIncomeQuery = new QueryWrapper<>();
        totalIncomeQuery.eq("status", "SUCCESS");
        List<RechargeRecord> allRechargeRecords = rechargeRecordMapper.selectList(totalIncomeQuery);
        long totalIncome = allRechargeRecords.stream()
                .mapToLong(RechargeRecord::getAmount)
                .sum();

        // 今日收入
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
     * 获取近 N 天的每日统计数据
     *
     * @param days 天数
     * @return 每日统计数据列表（倒序）
     */
    public List<DailyStatistics> getRecentStats(int days) {
        QueryWrapper<DailyStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("stat_date");
        queryWrapper.last("LIMIT " + days);
        return dailyStatisticsMapper.selectList(queryWrapper);
    }

    /**
     * 获取收入统计（按天分组）
     *
     * @param days 统计天数
     * @return 包含每日收入 Map 的统计结果
     */
    public Map<String, Object> getRevenueStats(int days) {
        Map<String, Object> stats = new HashMap<>();

        // 查询指定日期范围内的成功充值记录
        LocalDateTime startDate = LocalDate.now().minusDays(days - 1).atStartOfDay();
        QueryWrapper<RechargeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "SUCCESS");
        queryWrapper.ge("completed_at", startDate);
        queryWrapper.orderByAsc("completed_at");

        List<RechargeRecord> records = rechargeRecordMapper.selectList(queryWrapper);

        // 初始化每天收入为 0
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
     * 获取玩家增长统计（按天分组）
     *
     * @param days 统计天数
     * @return 包含每日新增/活跃玩家 Map 的统计结果
     */
    public Map<String, Object> getPlayerGrowthStats(int days) {
        Map<String, Object> stats = new HashMap<>();

        // 从每日统计快照中读取数据
        QueryWrapper<DailyStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("stat_date");
        queryWrapper.last("LIMIT " + days);

        List<DailyStatistics> dailyStats = dailyStatisticsMapper.selectList(queryWrapper);

        // 初始化默认值为 0
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
