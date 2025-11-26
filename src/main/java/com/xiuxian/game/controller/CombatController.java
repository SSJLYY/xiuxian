package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.CombatLog;
import com.xiuxian.game.entity.Monster;
import com.xiuxian.game.service.CombatService;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/combat")
@RequiredArgsConstructor
public class CombatController {

    private static final Logger log = LoggerFactory.getLogger(CombatController.class);

    private final CombatService combatService;
    private final PlayerService playerService;

    @GetMapping("/generate-monster")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Monster>> generateMonster(@RequestParam(required = false) Integer mapId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Integer playerLevel = playerService.getCurrentPlayerProfile().getLevel();
            Monster monster = combatService.generateMonster(playerLevel, mapId);
            return ResponseEntity.ok(ApiResponse.success("怪物生成成功", monster));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startCombat(@RequestBody Map<String, Object> request) {
        try {
            Integer mapId = null;
            if (request != null && request.containsKey("mapId")) {
                Object mapIdObj = request.get("mapId");
                if (mapIdObj instanceof Number) {
                    mapId = ((Number) mapIdObj).intValue();
                } else if (mapIdObj instanceof String) {
                    try {
                        mapId = Integer.parseInt((String) mapIdObj);
                    } catch (NumberFormatException e) {
                        // 忽略无效的mapId
                    }
                }
            }
            
            Integer playerId = playerService.getCurrentPlayerId();
            Integer playerLevel = playerService.getCurrentPlayerProfile().getLevel();
            Monster monster = combatService.generateMonster(playerLevel, mapId);
            Map<String, Object> result = combatService.startCombat(playerId, monster);
            return ResponseEntity.ok(ApiResponse.success("战斗完成", result));
        } catch (Exception e) {
            log.error("战斗开始失败 - 玩家ID: {}", playerService.getCurrentPlayerId(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/start/{monsterId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startCombatWithMonster(@PathVariable Integer monsterId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Monster monster = combatService.getMonsterById(monsterId);
            if (monster == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("怪物不存在或不可战斗"));
            }
            Map<String, Object> result = combatService.startCombat(playerId, monster);
            return ResponseEntity.ok(ApiResponse.success("战斗完成", result));
        } catch (Exception e) {
            log.error("指定怪物战斗失败 - 玩家ID: {}, 怪物ID: {}", playerService.getCurrentPlayerId(), monsterId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CombatLog>>> getCombatHistory(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<CombatLog> logs = combatService.getCombatHistory(playerId, limit);
            return ResponseEntity.ok(ApiResponse.success("获取战斗历史成功", logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}