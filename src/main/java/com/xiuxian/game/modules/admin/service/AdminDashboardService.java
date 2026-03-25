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

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final PlayerProfileMapper playerProfileMapper;
    private final UserMapper userMapper;
    private final RechargeRecordMapper rechargeRecordMapper;
    private final DailyStatisticsMapper dailyStatisticsMapper;
    private final PlayerLoginLogMapper playerLoginLogMapper;

    /**
     * 获取仪表板统计数�?
     *
     * @return 仪表板统计数�?
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取在线玩家数（最�?分钟内活跃）
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        QueryWrapper<PlayerProfile> onlineQuery = new QueryWrapper<>();
        onlineQuery.gt("last_online_time", fiveMinutesAgo);
        Long onlinePlayers = playerProfileMapper.selectCount(onlineQuery);

        // 今日注册�?
        LocalDateTime today = LocalDate.now().atStartOfDay();
        QueryWrapper<User> newUsersQuery = new QueryWrapper<>();
        newUsersQuery.gt("created_at", today);
        Long newUsersToday = userMapper.selectCount(newUsersQuery);

        // 今日活跃数（今日登录过的玩家�?
        QueryWrapper<PlayerLoginLog> activeTodayQuery = new QueryWrapper<>();
        activeTodayQuery.gt("login_at", today);
        Long activeToday = playerLoginLogMapper.selectCount(activeTodayQuery);

        // 总收入和今日收入
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

        // 今日统计数据
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
}

