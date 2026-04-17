package com.xiuxian.game.modules.cultivation.controller;

import com.xiuxian.game.modules.cultivation.entity.CultivationLog;
import com.xiuxian.game.modules.cultivation.service.CultivationService;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity<Map<String, Object>> getCultivationLogs(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Integer playerId = getCurrentPlayerId();
            List<CultivationLog> logs = cultivationService.getPlayerCultivationLogs(playerId, limit);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", logs);
            return ResponseEntity.ok(result);
        } catch (BusinessException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(400).body(result);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCultivationStats() {
        try {
            Integer playerId = getCurrentPlayerId();
            
            long totalTime = cultivationService.getTotalCultivationTime(playerId);
            long totalExp = cultivationService.getTotalExpFromCultivation(playerId);
            long totalSpiritStones = cultivationService.getTotalSpiritStonesFromCultivation(playerId);
            CultivationLog latestLog = cultivationService.getLatestCultivationLog(playerId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCultivationTime", totalTime);
            stats.put("totalExp", totalExp);
            stats.put("totalSpiritStones", totalSpiritStones);
            stats.put("latestCultivation", latestLog);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", stats);
            return ResponseEntity.ok(result);
        } catch (BusinessException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(400).body(result);
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
