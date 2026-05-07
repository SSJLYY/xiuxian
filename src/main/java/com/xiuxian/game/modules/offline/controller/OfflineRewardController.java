package com.xiuxian.game.modules.offline.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.offline.entity.OfflineReward;
import com.xiuxian.game.modules.offline.service.OfflineRewardService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offline-reward")
@RequiredArgsConstructor
public class OfflineRewardController {

    private final OfflineRewardService offlineRewardService;
    private final PlayerService playerService;

    @GetMapping("/calculate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateOfflineRewardGet() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> result = offlineRewardService.calculateOfflineReward(playerId);
            return ResponseEntity.ok(ApiResponse.success("离线收益计算完成", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/calculate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateOfflineRewardPost() {
        return calculateOfflineRewardGet();
    }

    @PostMapping("/claim/{rewardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> claimOfflineReward(@PathVariable Integer rewardId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> result = offlineRewardService.claimOfflineReward(playerId, rewardId);
            return ResponseEntity.ok(ApiResponse.success("离线收益领取成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/unclaimed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OfflineReward>>> getUnclaimedRewards() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<OfflineReward> rewards = offlineRewardService.getUnclaimedRewards(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取未领取奖励成功", rewards));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
