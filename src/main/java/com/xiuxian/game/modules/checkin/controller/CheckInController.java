package com.xiuxian.game.modules.checkin.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.checkin.service.CheckInService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 每日签到控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;
    private final PlayerService playerService;

    /**
     * 执行今日签到
     * POST /api/checkin/do
     */
    @PostMapping("/do")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CheckInService.CheckInResult> doCheckIn() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            CheckInService.CheckInResult result = checkInService.checkIn(playerId);
            return ApiResponse.success(result.getIsMilestone()
                    ? result.getMilestoneMessage() : "签到成功！连续" + result.getConsecutiveDays() + "天", result);
        } catch (Exception e) {
            log.error("签到失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取签到状态（月历）
     * GET /api/checkin/status
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CheckInService.CheckInStatus> getStatus() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            CheckInService.CheckInStatus status = checkInService.getStatus(playerId);
            return ApiResponse.success("获取成功", status);
        } catch (Exception e) {
            log.error("获取签到状态失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}

