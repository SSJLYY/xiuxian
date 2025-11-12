package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerProfile>> getProfile() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            // 确保isCultivating字段不为null
            if (profile.getIsCultivating() == null) {
                profile.setIsCultivating(false);
            }
            return ResponseEntity.ok(ApiResponse.success("获取成功", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cultivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cultivate() {
        try {
            playerService.cultivate();
            return ResponseEntity.ok(ApiResponse.success("修炼成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cultivate/stop")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> stopCultivate() {
        try {
            playerService.stopCultivate();
            return ResponseEntity.ok(ApiResponse.success("停止修炼成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/claim-offline-rewards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> claimOfflineRewards() {
        try {
            // 实现领取离线奖励逻辑
            return ResponseEntity.ok(ApiResponse.success("领取离线奖励成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-cultivation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> resetCultivation() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            profile.setIsCultivating(false);
            playerService.savePlayerProfile(profile);
            return ResponseEntity.ok(ApiResponse.success("修炼状态已重置", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}