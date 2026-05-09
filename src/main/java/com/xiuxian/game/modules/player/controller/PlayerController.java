package com.xiuxian.game.modules.player.controller;

import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private static final long BREAKTHROUGH_IDEMPOTENT_WINDOW_MS = 3000L;
    private static final ConcurrentHashMap<Integer, Long> BREAKTHROUGH_REQUEST_GUARD = new ConcurrentHashMap<>();

    private boolean isBreakthroughRequestDuplicate(Integer playerId, long now) {
        Long lastRequestAt = BREAKTHROUGH_REQUEST_GUARD.get(playerId);
        if (lastRequestAt != null && now - lastRequestAt < BREAKTHROUGH_IDEMPOTENT_WINDOW_MS) {
            return true;
        }
        BREAKTHROUGH_REQUEST_GUARD.put(playerId, now);
        return false;
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerProfile>> getProfile() {
        try {
            PlayerProfile currentProfile = playerService.getCurrentPlayerProfile();
            PlayerProfile profile = playerService.getPlayerProfileWithBonuses(currentProfile.getId());
            LogUtils.logUserAction(null, profile.getId(), "GET_PROFILE", "获取玩家档案信息");
            return ResponseEntity.ok(ApiResponse.success("获取成功", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/profile/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerProfile>> updateProfile(@RequestBody Map<String, Object> data) {
        try {
            PlayerProfile currentProfile = playerService.getCurrentPlayerProfile();
            PlayerProfile profile = playerService.updateCurrentPlayerProfile(
                    data.containsKey("nickname") ? (String) data.get("nickname") : null,
                    data.containsKey("avatar") ? (String) data.get("avatar") : null);
            LogUtils.logUserAction(null, profile.getId(), "UPDATE_PROFILE", "更新玩家档案：" + data.keySet());
            
            return ResponseEntity.ok(ApiResponse.success("更新成功", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/cultivate/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerProfile>> getCultivateInfo() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            LogUtils.logUserAction(null, profile.getId(), "GET_CULTIVATE_INFO", "获取修炼信息");
            return ResponseEntity.ok(ApiResponse.success("获取修炼信息成功", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cultivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cultivate(@RequestBody(required = false) Map<String, String> params) {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            String type = params != null ? params.getOrDefault("type", "normal") : "normal";
            playerService.cultivate(type);
            LogUtils.logUserAction(null, profile.getId(), "START_CULTIVATION", "玩家开始修炼，类型：" + type);
            return ResponseEntity.ok(ApiResponse.success("修炼成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cultivate/stop")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stopCultivate() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            Map<String, Object> result = playerService.stopCultivate();
            LogUtils.logUserAction(null, profile.getId(), "STOP_CULTIVATION", "玩家停止修炼");
            return ResponseEntity.ok(ApiResponse.success("停止修炼成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/attributes/allocate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerProfile>> allocateAttributes(@RequestBody Map<String, Integer> payload) {
        try {
            PlayerProfile currentProfile = playerService.getCurrentPlayerProfile();
            PlayerProfile profile = playerService.allocateCurrentPlayerAttributes(payload);
            LogUtils.logUserAction(null, profile.getId(), "ALLOCATE_ATTRIBUTES", "分配属性点: " + payload);

            return ResponseEntity.ok(ApiResponse.success("加点成功", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/claim-offline-rewards")
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<ApiResponse<?>> claimOfflineRewards() {
        return ResponseEntity.ok(ApiResponse.success("请使用 /api/offline-reward 接口", null));
    }

    @PostMapping("/reset-cultivation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> resetCultivation() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            playerService.resetCurrentPlayerCultivation();
            LogUtils.logUserAction(null, profile.getId(), "RESET_CULTIVATION", "重置修炼状态");
            return ResponseEntity.ok(ApiResponse.success("修炼状态已重置", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/breakthrough/can")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> canBreakthrough() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            boolean can = playerService.canBreakthrough(profile.getId());
            return ResponseEntity.ok(ApiResponse.success(can));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/breakthrough")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> attemptBreakthrough() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            long now = System.currentTimeMillis();
            if (isBreakthroughRequestDuplicate(profile.getId(), now)) {
                return ResponseEntity.ok(ApiResponse.error("请求过于频繁，请稍后再试"));
            }
            String result = playerService.attemptBreakthrough(profile.getId());
            LogUtils.logUserAction(null, profile.getId(), "BREAKTHROUGH", result);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
