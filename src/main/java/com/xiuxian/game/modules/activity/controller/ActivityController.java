package com.xiuxian.game.modules.activity.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.activity.entity.Activity;
import com.xiuxian.game.modules.activity.entity.PlayerActivityProgress;
import com.xiuxian.game.modules.activity.service.ActivityService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 活动控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final PlayerService playerService;

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Activity>> getActiveActivities() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.debug("获取活动列表: playerId={}", playerId);

            List<Activity> activities = activityService.getActiveActivities();

            log.debug("获取活动列表成功: playerId={}, count={}", playerId, activities.size());
            return ApiResponse.success("获取成功", activities);
        } catch (Exception e) {
            log.error("获取活动列表失败: error={}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Activity>> getAllActivities() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.debug("获取所有活动: playerId={}", playerId);

            List<Activity> activities = activityService.getAllActivities();

            log.debug("获取所有活动成功: playerId={}, count={}", playerId, activities.size());
            return ApiResponse.success("获取成功", activities);
        } catch (Exception e) {
            log.error("获取所有活动失败: error={}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/my-progress")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PlayerActivityProgress>> getMyActivityProgress() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.debug("获取玩家活动进度: playerId={}", playerId);

            List<PlayerActivityProgress> progress = activityService.getPlayerActivityProgress(playerId);

            log.debug("获取玩家活动进度成功: playerId={}, count={}", playerId, progress.size());
            return ApiResponse.success("获取成功", progress);
        } catch (Exception e) {
            log.error("获取玩家活动进度失败: error={}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{activityId}/participate")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PlayerActivityProgress> participateInActivity(@PathVariable Integer activityId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家参与活动: playerId={}, activityId={}", playerId, activityId);

            PlayerActivityProgress progress = activityService.participateInActivity(playerId, activityId);

            log.info("玩家参与活动成功: playerId={}, activityId={}", playerId, activityId);
            return ApiResponse.success("参与成功", progress);
        } catch (Exception e) {
            log.error("玩家参与活动失败: playerId={}, activityId={}, error={}",
                    playerService.getCurrentPlayerId(), activityId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{activityId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PlayerActivityProgress> updateActivityProgress(
            @PathVariable Integer activityId,
            @RequestBody UpdateProgressRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("更新活动进度: playerId={}, activityId={}, increment={}",
                    playerId, activityId, request.getIncrement());

            PlayerActivityProgress progress = activityService.updateActivityProgress(
                    playerId, activityId, request.getIncrement());

            log.info("更新活动进度成功: playerId={}, activityId={}", playerId, activityId);
            return ApiResponse.success("更新成功", progress);
        } catch (Exception e) {
            log.error("更新活动进度失败: playerId={}, activityId={}, error={}",
                    playerService.getCurrentPlayerId(), activityId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{activityId}/score")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PlayerActivityProgress> updateActivityScore(
            @PathVariable Integer activityId,
            @RequestBody UpdateScoreRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("更新活动积分: playerId={}, activityId={}, score={}",
                    playerId, activityId, request.getScore());

            PlayerActivityProgress progress = activityService.updateActivityScore(
                    playerId, activityId, request.getScore());

            log.info("更新活动积分成功: playerId={}, activityId={}", playerId, activityId);
            return ApiResponse.success("更新成功", progress);
        } catch (Exception e) {
            log.error("更新活动积分失败: playerId={}, activityId={}, error={}",
                    playerService.getCurrentPlayerId(), activityId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{activityId}/claim")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> claimActivityReward(@PathVariable Integer activityId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("领取活动奖励: playerId={}, activityId={}", playerId, activityId);

            Map<String, Object> result = activityService.claimActivityReward(playerId, activityId);

            log.info("领取活动奖励成功: playerId={}, activityId={}", playerId, activityId);
            return ApiResponse.success("领取成功", result);
        } catch (Exception e) {
            log.error("领取活动奖励失败: activityId={}, error={}", activityId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{activityId}/ranking")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PlayerActivityProgress>> getActivityRanking(
            @PathVariable Integer activityId,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.debug("获取活动排名: playerId={}, activityId={}, limit={}", playerId, activityId, limit);

            List<PlayerActivityProgress> ranking = activityService.getActivityRanking(activityId, limit);

            log.debug("获取活动排名成功: playerId={}, activityId={}, count={}", playerId, activityId, ranking.size());
            return ApiResponse.success("获取成功", ranking);
        } catch (Exception e) {
            log.error("获取活动排名失败: activityId={}, error={}", activityId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    public static class UpdateProgressRequest {
        private Integer increment;
        private Integer progress;

        public int getIncrement() {
            if (increment != null) {
                return increment;
            }
            return progress != null ? progress : 0;
        }

        public void setIncrement(Integer increment) {
            this.increment = increment;
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }
    }

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
