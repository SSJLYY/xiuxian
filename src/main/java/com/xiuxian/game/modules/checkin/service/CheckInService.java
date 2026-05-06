package com.xiuxian.game.modules.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiuxian.game.modules.checkin.entity.PlayerCheckIn;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.checkin.mapper.PlayerCheckInMapper;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.util.LogUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * 每日签到服务
 * 支持连续签到奖励递增、月历展示、补签（预留）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final PlayerCheckInMapper checkInMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据

    // ==================== 连续签到奖励配置 ====================
    // key = 连续天数区间（包含），value = [灵石, 经验]
    private static final int[][] CONSECUTIVE_REWARDS = {
        // day 1
        {200,  500},
        // day 2
        {250,  600},
        // day 3
        {300,  800},
        // day 4
        {350, 1000},
        // day 5
        {400, 1200},
        // day 6
        {450, 1500},
        // day 7 — 里程碑奖励
        {800, 3000},
        // day 8-13
        {350, 1000},
        // day 14 — 里程碑
        {1500, 5000},
        // day 15-29
        {400, 1200},
        // day 30 — 月签到里程碑
        {3000, 10000},
    };

    /**
     * 今日签到
     */
    @Transactional
    public CheckInResult checkIn(Integer playerId) {
        LocalDate today = LocalDate.now();
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        // 判断今天是否已签到
        PlayerCheckIn existing = checkInMapper.findByPlayerAndDate(playerId, today);
        if (existing != null) {
            throw new BusinessException(ErrorCode.CHECK_IN_ALREADY_DONE);
        }

        // 计算连续签到天数
        int consecutiveDays = calculateConsecutiveDays(playerId, today);

        // 计算奖励
        int[] reward = getRewardForDay(consecutiveDays);
        int stones = reward[0];
        int exp = reward[1];

        // 检查是否里程碑（每7天）
        boolean isMilestone = (consecutiveDays % 7 == 0);

        // 写入签到记录
        PlayerCheckIn record = new PlayerCheckIn();
        record.setPlayerId(playerId);
        record.setCheckInDate(today.atStartOfDay());
        record.setConsecutiveDays(consecutiveDays);
        record.setRewardSpiritStones(stones);
        record.setRewardExp(exp);
        record.setIsMakeup(false);
        record.setCreatedAt(LocalDateTime.now());
        checkInMapper.insert(record);

        // 发放奖励
        player.setSpiritStones(defaultLong(player.getSpiritStones()) + stones);
        player.setExp(defaultLong(player.getExp()) + exp);
        playerService.savePlayerProfile(player);

        log.info("[CheckIn] 玩家{}签到成功: 连续{}天, 灵石+{}, 经验+{}", playerId, consecutiveDays, stones, exp);
        LogUtils.logBusiness("CHECK_IN", "每日签到", "playerId", playerId,
                "consecutiveDays", consecutiveDays, "stones", stones);

        return CheckInResult.builder()
                .success(true)
                .consecutiveDays(consecutiveDays)
                .rewardSpiritStones(stones)
                .rewardExp(exp)
                .isMilestone(isMilestone)
                .milestoneMessage(isMilestone ? buildMilestoneMsg(consecutiveDays) : null)
                .nextDayPreview(getNextDayPreview(consecutiveDays))
                .build();
    }

    /**
     * 获取签到状态（月历数据）
     */
    public CheckInStatus getStatus(Integer playerId) {
        LocalDate today = LocalDate.now();
        return getStatus(playerId, today.getYear(), today.getMonthValue());
    }

    /**
     * 获取指定年月的签到状态（月历数据）
     */
    public CheckInStatus getStatus(Integer playerId, Integer year, Integer month) {
        LocalDate today = LocalDate.now();
        YearMonth ym;
        try {
            ym = YearMonth.of(year, month);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "月份参数不合法");
        }

        List<PlayerCheckIn> monthRecords = checkInMapper.findByPlayerAndMonth(
                playerId, ym.getYear(), ym.getMonthValue());

        Set<Integer> checkedDays = new HashSet<>();
        for (PlayerCheckIn r : monthRecords) {
            checkedDays.add(r.getCheckInDate().getDayOfMonth());
        }

        boolean isCurrentMonth = ym.getYear() == today.getYear() && ym.getMonthValue() == today.getMonthValue();
        boolean checkedToday = isCurrentMonth && checkedDays.contains(today.getDayOfMonth());
        int consecutiveDays = checkedToday ? calculateConsecutiveDays(playerId, today)
                : calculateConsecutiveDays(playerId, today.minusDays(1));

        // 预览今日奖励（未签到）
        int nextConsecutive = isCurrentMonth
                ? (checkedToday ? consecutiveDays : consecutiveDays + 1)
                : consecutiveDays;
        int[] todayReward = getRewardForDay(nextConsecutive);

        // 月历格子
        List<Map<String, Object>> calendar = new ArrayList<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            Map<String, Object> cell = new HashMap<>();
            cell.put("day", d);
            cell.put("checked", checkedDays.contains(d));
            cell.put("today", d == today.getDayOfMonth());
            cell.put("future", d > today.getDayOfMonth());
            cell.put("milestone", d == 7 || d == 14 || d == 30);
            int[] r = getRewardForDay(d);
            cell.put("previewStones", r[0]);
            calendar.add(cell);
        }

        return CheckInStatus.builder()
                .checkedToday(checkedToday)
                .consecutiveDays(consecutiveDays)
                .totalCheckedThisMonth(checkedDays.size())
                .daysInMonth(ym.lengthOfMonth())
                .todayRewardStones(todayReward[0])
                .todayRewardExp(todayReward[1])
                .calendar(calendar)
                .build();
    }

    // ==================== 私有方法 ====================

    private int calculateConsecutiveDays(Integer playerId, LocalDate targetDate) {
        int streak = 0;
        LocalDate checking = targetDate.minusDays(1);
        int maxDays = 365;
        
        while (streak < maxDays) {
            PlayerCheckIn rec = checkInMapper.findByPlayerAndDate(playerId, checking);
            if (rec == null) break;
            streak++;
            checking = checking.minusDays(1);
        }
        return streak + 1;
    }

    private int[] getRewardForDay(int consecutiveDays) {
        // day 30+
        if (consecutiveDays >= 30) return CONSECUTIVE_REWARDS[10];
        // day 15-29
        if (consecutiveDays >= 15) return CONSECUTIVE_REWARDS[9];
        // day 14
        if (consecutiveDays == 14) return CONSECUTIVE_REWARDS[8];
        // day 8-13
        if (consecutiveDays >= 8) return CONSECUTIVE_REWARDS[7];
        // day 1-7
        return CONSECUTIVE_REWARDS[Math.max(0, consecutiveDays - 1)];
    }

    private Map<String, Integer> getNextDayPreview(int currentConsecutive) {
        int[] r = getRewardForDay(currentConsecutive + 1);
        Map<String, Integer> preview = new HashMap<>();
        preview.put("stones", r[0]);
        preview.put("exp", r[1]);
        return preview;
    }

    private String buildMilestoneMsg(int days) {
        switch (days) {
            case 7: return "🎉 连续签到7天！获得丰厚额外奖励！";
            case 14: return "🌟 连续签到14天！信念坚定，加倍奖励！";
            case 30: return "🏆 连续签到30天！道心通明，获得传说级奖励！";
            default: return "🎊 里程碑达成！";
        }
    }

    // ==================== VO ====================

    @Data @lombok.Builder
    public static class CheckInResult {
        private Boolean success;
        private Integer consecutiveDays;
        private Integer rewardSpiritStones;
        private Integer rewardExp;
        private Boolean isMilestone;
        private String milestoneMessage;
        private Map<String, Integer> nextDayPreview;
    }

    @Data @lombok.Builder
    public static class CheckInStatus {
        private Boolean checkedToday;
        private Integer consecutiveDays;
        private Integer totalCheckedThisMonth;
        private Integer daysInMonth;
        private Integer todayRewardStones;
        private Integer todayRewardExp;
        private List<Map<String, Object>> calendar;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
