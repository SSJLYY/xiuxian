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

@Slf4j
@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

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

    @PostMapping("/cultivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cultivate() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            playerService.cultivate();
            LogUtils.logUserAction(null, profile.getId(), "START_CULTIVATION", "玩家开始修炼");
            return ResponseEntity.ok(ApiResponse.success("修炼成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cultivate/stop")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> stopCultivate() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            playerService.stopCultivate();
            LogUtils.logUserAction(null, profile.getId(), "STOP_CULTIVATION", "玩家停止修炼");
            return ResponseEntity.ok(ApiResponse.success("停止修炼成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/attributes/allocate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerProfile>> allocateAttributes(@RequestBody Map<String, Integer> payload) {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            int availablePoints = profile.getAttributePoints() == null ? 0 : profile.getAttributePoints();

            int totalSpend = 0;
            for (String attr : new String[]{"attack", "defense", "health", "mana", "speed"}) {
                Integer points = payload.get(attr);
                if (points != null && points > 0) totalSpend += points;
            }

            if (totalSpend <= 0) throw new BusinessException(ErrorCode.PARAM_ERROR, "未提供有效的加点方案");
            if (totalSpend > availablePoints) throw new BusinessException(ErrorCode.PARAM_ERROR, "属性点不足");

            profile.setAttack(profile.getAttack() + payload.getOrDefault("attack", 0));
            profile.setDefense(profile.getDefense() + payload.getOrDefault("defense", 0));
            profile.setHealth(profile.getHealth() + payload.getOrDefault("health", 0));
            profile.setMana(profile.getMana() + payload.getOrDefault("mana", 0));
            profile.setSpeed(profile.getSpeed() + payload.getOrDefault("speed", 0));
            profile.setAttributePoints(availablePoints - totalSpend);

            playerService.savePlayerProfile(profile);
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
            profile.setIsCultivating(false);
            playerService.savePlayerProfile(profile);
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
            String result = playerService.attemptBreakthrough(profile.getId());
            LogUtils.logUserAction(null, profile.getId(), "BREAKTHROUGH", result);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
