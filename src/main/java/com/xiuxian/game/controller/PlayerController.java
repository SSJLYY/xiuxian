package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.OfflineRewardResponse;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.service.OfflineRewardService;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final OfflineRewardService offlineRewardService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<PlayerProfile>> getProfile() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            return ResponseEntity.ok(ApiResponse.success("获取成功", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cultivate")
    public ResponseEntity<ApiResponse<Void>> cultivate() {
        try {
            playerService.cultivate();
            return ResponseEntity.ok(ApiResponse.success("修炼成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/claim-offline-rewards")
    public ResponseEntity<ApiResponse<OfflineRewardResponse>> claimOfflineRewards() {
        try {
            OfflineRewardResponse rewards = offlineRewardService.calculateAndClaimOfflineRewards();
            return ResponseEntity.ok(ApiResponse.success("获取离线收益成功", rewards));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}