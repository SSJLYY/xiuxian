package com.xiuxian.game.modules.checkin.service;

import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.common.util.LogUtils;
import com.xiuxian.game.modules.checkin.entity.PlayerCheckIn;
import com.xiuxian.game.modules.checkin.mapper.PlayerCheckInMapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final PlayerCheckInMapper checkInMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;

    private static final int[][] CONSECUTIVE_REWARDS = {
            {200, 500},
            {250, 600},
            {300, 800},
            {350, 1000},
            {400, 1200},
            {450, 1500},
            {800, 3000},
            {350, 1000},
            {1500, 5000},
            {400, 1200},
            {3000, 10000}
    };

    @Transactional
    public CheckInResult checkIn(Integer playerId) {
        LocalDate today = LocalDate.now();
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        PlayerCheckIn existing = checkInMapper.findByPlayerAndDate(playerId, today);
        if (existing != null) {
            throw new BusinessException(ErrorCode.CHECK_IN_ALREADY_DONE);
        }

        int consecutiveDays = calculateConsecutiveDays(playerId, today);
        int[] reward = getRewardForDay(consecutiveDays);
        int stones = reward[0];
        int exp = reward[1];
        boolean isMilestone = consecutiveDays % 7 == 0;

        PlayerCheckIn record = new PlayerCheckIn();
        record.setPlayerId(playerId);
        record.setCheckInDate(today.atStartOfDay());
        record.setConsecutiveDays(consecutiveDays);
        record.setRewardSpiritStones(stones);
        record.setRewardExp(exp);
        record.setIsMakeup(false);
        record.setCreatedAt(LocalDateTime.now());
        checkInMapper.insert(record);

        player.setSpiritStones(defaultLong(player.getSpiritStones()) + stones);
        player.setExp(defaultLong(player.getExp()) + exp);
        playerService.savePlayerProfile(player);

        log.info("[CheckIn] playerId={} consecutiveDays={} stones={} exp={}",
                playerId, consecutiveDays, stones, exp);
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

    public CheckInStatus getStatus(Integer playerId) {
        LocalDate today = LocalDate.now();
        return getStatus(playerId, today.getYear(), today.getMonthValue());
    }

    public CheckInStatus getStatus(Integer playerId, Integer year, Integer month) {
        LocalDate today = LocalDate.now();
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(year, month);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "月份参数不合法");
        }

        List<PlayerCheckIn> monthRecords = checkInMapper.findByPlayerAndMonth(
                playerId, yearMonth.getYear(), yearMonth.getMonthValue());

        Set<Integer> checkedDays = new HashSet<>();
        for (PlayerCheckIn record : monthRecords) {
            checkedDays.add(record.getCheckInDate().getDayOfMonth());
        }

        boolean isCurrentMonth = yearMonth.getYear() == today.getYear()
                && yearMonth.getMonthValue() == today.getMonthValue();
        boolean checkedToday = isCurrentMonth && checkedDays.contains(today.getDayOfMonth());
        int consecutiveDays = checkedToday
                ? calculateConsecutiveDays(playerId, today)
                : calculateConsecutiveDays(playerId, today.minusDays(1));

        int nextConsecutive = isCurrentMonth
                ? (checkedToday ? consecutiveDays : consecutiveDays + 1)
                : consecutiveDays;
        int[] todayReward = getRewardForDay(nextConsecutive);

        List<Map<String, Object>> calendar = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            Map<String, Object> cell = new HashMap<>();
            cell.put("day", day);
            cell.put("checked", checkedDays.contains(day));
            cell.put("today", isCurrentMonth && day == today.getDayOfMonth());
            cell.put("future", isCurrentMonth && day > today.getDayOfMonth());
            cell.put("milestone", day == 7 || day == 14 || day == 30);
            int[] reward = getRewardForDay(day);
            cell.put("previewStones", reward[0]);
            cell.put("previewExp", reward[1]);
            calendar.add(cell);
        }

        return CheckInStatus.builder()
                .checkedToday(checkedToday)
                .consecutiveDays(consecutiveDays)
                .totalCheckedThisMonth(checkedDays.size())
                .daysInMonth(yearMonth.lengthOfMonth())
                .todayRewardStones(todayReward[0])
                .todayRewardExp(todayReward[1])
                .calendar(calendar)
                .build();
    }

    private int calculateConsecutiveDays(Integer playerId, LocalDate targetDate) {
        int streak = 0;
        LocalDate checkingDate = targetDate.minusDays(1);
        while (streak < 365) {
            PlayerCheckIn record = checkInMapper.findByPlayerAndDate(playerId, checkingDate);
            if (record == null) {
                break;
            }
            streak++;
            checkingDate = checkingDate.minusDays(1);
        }
        return streak + 1;
    }

    private int[] getRewardForDay(int consecutiveDays) {
        if (consecutiveDays >= 30) {
            return CONSECUTIVE_REWARDS[10];
        }
        if (consecutiveDays >= 15) {
            return CONSECUTIVE_REWARDS[9];
        }
        if (consecutiveDays == 14) {
            return CONSECUTIVE_REWARDS[8];
        }
        if (consecutiveDays >= 8) {
            return CONSECUTIVE_REWARDS[7];
        }
        return CONSECUTIVE_REWARDS[Math.max(0, consecutiveDays - 1)];
    }

    private Map<String, Integer> getNextDayPreview(int currentConsecutive) {
        int[] reward = getRewardForDay(currentConsecutive + 1);
        Map<String, Integer> preview = new HashMap<>();
        preview.put("stones", reward[0]);
        preview.put("exp", reward[1]);
        return preview;
    }

    private String buildMilestoneMsg(int days) {
        switch (days) {
            case 7:
                return "连续签到7天，获得额外奖励。";
            case 14:
                return "连续签到14天，获得进阶奖励。";
            case 30:
                return "连续签到30天，获得月度大奖励。";
            default:
                return "里程碑达成。";
        }
    }

    @Data
    @lombok.Builder
    public static class CheckInResult {
        private Boolean success;
        private Integer consecutiveDays;
        private Integer rewardSpiritStones;
        private Integer rewardExp;
        private Boolean isMilestone;
        private String milestoneMessage;
        private Map<String, Integer> nextDayPreview;
    }

    @Data
    @lombok.Builder
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
