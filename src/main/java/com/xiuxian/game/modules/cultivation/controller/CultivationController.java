package com.xiuxian.game.modules.cultivation.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.cultivation.entity.CultivationLog;
import com.xiuxian.game.modules.cultivation.service.CultivationService;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cultivation")
@RequiredArgsConstructor
public class CultivationController {

    private final CultivationService cultivationService;
    private final com.xiuxian.game.modules.player.service.PlayerService playerService;

    @GetMapping("/logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CultivationLog>>> getCultivationLogs(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Integer playerId = getCurrentPlayerId();
            List<CultivationLog> logs = cultivationService.getPlayerCultivationLogs(playerId, limit);

            return ResponseEntity.ok(ApiResponse.success("获取修炼日志成功", logs));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCultivationStats() {
        try {
            Integer playerId = getCurrentPlayerId();
            com.xiuxian.game.modules.player.entity.PlayerProfile profile = playerService.getPlayerProfileById(playerId);

            long totalTime = cultivationService.getTotalCultivationTime(playerId);
            long totalExp = cultivationService.getTotalExpFromCultivation(playerId);
            long totalSpiritStones = cultivationService.getTotalSpiritStonesFromCultivation(playerId);
            CultivationLog latestLog = cultivationService.getLatestCultivationLog(playerId);

            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalCultivationTime", totalTime);
            stats.put("totalCultivationTimeMinutes", totalTime / 60000);
            stats.put("profileTotalCultivationMinutes", profile.getTotalCultivationTime());
            stats.put("totalExp", totalExp);
            stats.put("totalSpiritStones", totalSpiritStones);
            stats.put("latestCultivation", latestLog);

            return ResponseEntity.ok(ApiResponse.success("获取修炼统计成功", stats));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Integer getCurrentPlayerId() {
        try {
            return playerService.getCurrentPlayerId();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
    }
}
