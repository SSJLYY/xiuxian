package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.Activity;
import com.xiuxian.game.entity.PlayerActivityProgress;
import com.xiuxian.game.service.ActivityService;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final PlayerService playerService;

    /**
     * 获取所有正在进行的活动
     */
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Activity>> getActiveActivities() {
        try {
            List<Activity> activities = activityService.getActiveActivities();
            return ApiResponse.success("获取成功", activities);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有活动（包括已结束的）
     */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Activity>> getAllActivities() {
        try {
            List<Activity> activities = activityService.getAllActivities();
            return ApiResponse.success("获取成功", activities);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取玩家参与的活动进度
     */
    @GetMapping("/my-progress")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PlayerActivityProgress>> getMyActivityProgress() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerActivityProgress> progress = activityService.getPlayerActivityProgress(playerId);
            return ApiResponse.success("获取成功", progress);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 参与活动
     */
    @PostMapping("/{activityId}/participate")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PlayerActivityProgress> participateInActivity(@PathVariable Integer activityId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerActivityProgress progress = activityService.participateInActivity(playerId, activityId);
            return ApiResponse.success("参与成功", progress);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新活动进度
     */
    @PostMapping("/{activityId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PlayerActivityProgress> updateActivityProgress(
            @PathVariable Integer activityId,
            @RequestBody UpdateProgressRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerActivityProgress progress = activityService.updateActivityProgress(
                    playerId, activityId, request.getIncrement());
            return ApiResponse.success("更新成功", progress);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新活动积分
     */
    @PostMapping("/{activityId}/score")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PlayerActivityProgress> updateActivityScore(
            @PathVariable Integer activityId,
            @RequestBody UpdateScoreRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerActivityProgress progress = activityService.updateActivityScore(
                    playerId, activityId, request.getScore());
            return ApiResponse.success("更新成功", progress);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取活动排名
     */
    @GetMapping("/{activityId}/ranking")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PlayerActivityProgress>> getActivityRanking(
            @PathVariable Integer activityId,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<PlayerActivityProgress> ranking = activityService.getActivityRanking(activityId, limit);
            return ApiResponse.success("获取成功", ranking);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新进度请求DTO
     */
    public static class UpdateProgressRequest {
        private int increment;

        public int getIncrement() {
            return increment;
        }

        public void setIncrement(int increment) {
            this.increment = increment;
        }
    }

    /**
     * 更新积分请求DTO
     */
    public static class UpdateScoreRequest {
        private int score;

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }
}