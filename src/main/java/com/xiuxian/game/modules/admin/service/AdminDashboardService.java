package com.xiuxian.game.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.modules.player.entity.PlayerLoginLog;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.mapper.UserMapper;
import com.xiuxian.game.modules.player.mapper.PlayerLoginLogMapper;
import com.xiuxian.game.modules.vip.entity.RechargeRecord;
import com.xiuxian.game.modules.vip.mapper.RechargeRecordMapper;
import com.xiuxian.game.modules.admin.entity.DailyStatistics;
import com.xiuxian.game.modules.admin.mapper.DailyStatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Admin 仪表板服务（聚合层）
 * 提供实时在线人数、今日新增用户、今日活跃、收入等核心运营指标。
 * admin 聚合层务实例外，允许直接访问各模块 Mapper。
 *
 * @author shaun.sheng
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final PlayerProfileMapper playerProfileMapper;
    private final UserMapper userMapper;
    private final RechargeRecordMapper rechargeRecordMapper;
    private final DailyStatisticsMapper dailyStatisticsMapper;
    private final PlayerLoginLogMapper playerLoginLogMapper;

    /**
     * 获取仪表板核心运营指标
     *
     * @return 包含在线人数、新增用户、活跃玩家、收入等统计数据的 Map
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 5 分钟内有心跳的玩家视为在线
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        QueryWrapper<PlayerProfile> onlineQuery = new QueryWrapper<>();
        onlineQuery.gt("last_online_time", fiveMinutesAgo);
        Long onlinePlayers = playerProfileMapper.selectCount(onlineQuery);

        // 今日新注册用户
        LocalDateTime today = LocalDate.now().atStartOfDay();
        QueryWrapper<User> newUsersQuery = new QueryWrapper<>();
        newUsersQuery.gt("created_at", today);
        Long newUsersToday = userMapper.selectCount(newUsersQuery);

        // 今日活跃玩家（今日有登录记录）
        QueryWrapper<PlayerLoginLog> activeTodayQuery = new QueryWrapper<>();
        activeTodayQuery.gt("login_at", today);
        Long activeToday = (long) playerLoginLogMapper.selectList(activeTodayQuery).stream()
                .map(PlayerLoginLog::getPlayerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .size();

        // 总收入与今日收入
        QueryWrapper<RechargeRecord> totalIncomeQuery = new QueryWrapper<>();
        totalIncomeQuery.eq("status", "SUCCESS");
        List<RechargeRecord> allRechargeRecords = rechargeRecordMapper.selectList(totalIncomeQuery);
        long totalIncome = allRechargeRecords.stream()
                .mapToLong(RechargeRecord::getAmount)
                .sum();

        QueryWrapper<RechargeRecord> todayIncomeQuery = new QueryWrapper<>();
        todayIncomeQuery.eq("status", "SUCCESS");
        todayIncomeQuery.gt("completed_at", today);
        List<RechargeRecord> todayRechargeRecords = rechargeRecordMapper.selectList(todayIncomeQuery);
        long todayIncome = todayRechargeRecords.stream()
                .mapToLong(RechargeRecord::getAmount)
                .sum();

        // 今日统计快照
        QueryWrapper<DailyStatistics> todayStatsQuery = new QueryWrapper<>();
        todayStatsQuery.eq("stat_date", LocalDate.now());
        DailyStatistics todayStats = dailyStatisticsMapper.selectOne(todayStatsQuery);

        stats.put("onlinePlayers", onlinePlayers);
        stats.put("newUsersToday", newUsersToday);
        stats.put("activeToday", activeToday);
        stats.put("totalIncome", totalIncome);
        stats.put("todayIncome", todayIncome);
        stats.put("todayStats", todayStats);

        return stats;
    }

    /**
     * 获取近 N 天的每日统计数据
     *
     * @param days 天数
     * @return 每日统计数据列表（倒序）
     */
    public List<DailyStatistics> getRecentStats(int days) {
        int safeDays = Math.min(Math.max(days, 1), 365);
        QueryWrapper<DailyStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("stat_date");
        queryWrapper.last("LIMIT " + safeDays);
        return dailyStatisticsMapper.selectList(queryWrapper);
    }
}
