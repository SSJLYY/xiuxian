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
            // 使用新的方法获取包含技能加成的玩家信息
            PlayerProfile profile = playerService.getPlayerProfileWithBonuses(
                playerService.getCurrentPlayerProfile().getId()
            );
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

    @PostMapping("/attributes/allocate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerProfile>> allocateAttributes(@RequestBody java.util.Map<String, Integer> payload) {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            int points = profile.getAttributePoints() == null ? 0 : profile.getAttributePoints();
            int spend = 0;
            for (String key : new String[]{"attack","defense","health","mana","speed"}) {
                Integer v = payload.get(key);
                if (v != null && v > 0) spend += v;
            }
            if (spend <= 0) throw new RuntimeException("未提供加点");
            if (spend > points) throw new RuntimeException("属性点不足");
            profile.setAttack(profile.getAttack() + (payload.getOrDefault("attack",0)));
            profile.setDefense(profile.getDefense() + (payload.getOrDefault("defense",0)));
            profile.setHealth(profile.getHealth() + (payload.getOrDefault("health",0)));
            profile.setMana(profile.getMana() + (payload.getOrDefault("mana",0)));
            profile.setSpeed(profile.getSpeed() + (payload.getOrDefault("speed",0)));
            profile.setAttributePoints(points - spend);
            playerService.savePlayerProfile(profile);
            return ResponseEntity.ok(ApiResponse.success("加点成功", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/claim-offline-rewards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> claimOfflineRewards() {
        try {
            // 直接调用OfflineRewardController的方法
            return ResponseEntity.ok(ApiResponse.success("请使用 /api/offline-reward 接口", null));
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