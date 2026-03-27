package com.xiuxian.game.modules.combat.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.CombatResult;
import com.xiuxian.game.modules.combat.entity.CombatLog;
import com.xiuxian.game.modules.combat.entity.Monster;
import com.xiuxian.game.modules.combat.service.CombatService;
import com.xiuxian.game.modules.combat.service.EnhancedCombatService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 战斗控制器
 * 提供战斗相关的 REST API：生成怪物、单次战斗、批量战斗、战斗历史
 */
@Slf4j
@RestController
@RequestMapping("/api/combat")
@RequiredArgsConstructor
public class CombatController {

    private final CombatService combatService;
    private final EnhancedCombatService enhancedCombatService;
    private final PlayerService playerService;

    /**
     * 生成怪物
     */
    @GetMapping("/generate-monster")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Monster>> generateMonster(@RequestParam(required = false) Integer mapId) {
        try {
            Integer playerLevel = playerService.getCurrentPlayerProfile().getLevel();
            Monster monster = combatService.generateMonster(playerLevel, mapId);
            return ResponseEntity.ok(ApiResponse.success("怪物生成成功", monster));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 开始战斗（随机生成怪物）
     */
    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CombatResult>> startCombat(@RequestBody Map<String, Object> request) {
        try {
            Integer mapId = extractMapId(request);
            Integer playerId = playerService.getCurrentPlayerId();
            Integer playerLevel = playerService.getCurrentPlayerProfile().getLevel();
            Monster monster = combatService.generateMonster(playerLevel, mapId);
            CombatResult result = combatService.startCombat(playerId, monster);
            return ResponseEntity.ok(ApiResponse.success("战斗完成", result));
        } catch (Exception e) {
            log.error("战斗失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 指定怪物开始战斗
     */
    @PostMapping("/start/{monsterId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CombatResult>> startCombatWithMonster(@PathVariable Integer monsterId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Monster monster = combatService.getMonsterById(monsterId);
            if (monster == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("怪物不存在或不可战斗"));
            }
            CombatResult result = combatService.startCombat(playerId, monster);
            return ResponseEntity.ok(ApiResponse.success("战斗完成", result));
        } catch (Exception e) {
            log.error("指定怪物战斗失败: monsterId={}", monsterId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 增强战斗 - 支持技能、宠物、道具
     */
    @PostMapping("/enhanced")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enhancedCombat(@RequestBody EnhancedCombatRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Integer playerLevel = playerService.getCurrentPlayerProfile().getLevel();
            Monster monster = combatService.generateMonster(playerLevel, request.getMapId());
            Map<String, Object> result = enhancedCombatService.enhancedCombat(
                    playerId, monster, request.getSkillId(), request.getItemId());
            return ResponseEntity.ok(ApiResponse.success("战斗完成", result));
        } catch (Exception e) {
            log.error("增强战斗失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 批量战斗 - 一次执行多次战斗并返回汇总结果
     */
    @PostMapping("/batch/{times}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CombatResult>> batchCombat(
            @PathVariable Integer times,
            @RequestBody Map<String, Object> request) {
        try {
            int maxTimes = Math.min(times, 100);
            Integer playerId = playerService.getCurrentPlayerId();
            Integer playerLevel = playerService.getCurrentPlayerProfile().getLevel();
            Integer mapId = extractMapId(request);
            CombatResult result = combatService.batchCombat(playerId, playerLevel, mapId, maxTimes);
            return ResponseEntity.ok(ApiResponse.success("批量战斗完成", result));
        } catch (Exception e) {
            log.error("批量战斗失败: times={}", times, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取战斗历史
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CombatLog>>> getCombatHistory(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<CombatLog> logs = combatService.getCombatHistory(playerId, limit);
            return ResponseEntity.ok(ApiResponse.success("获取战斗历史成功", logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // =====================================================================
    // 私有辅助
    // =====================================================================

    /**
     * 从请求体中安全解析 mapId
     */
    private Integer extractMapId(Map<String, Object> request) {
        if (request == null || !request.containsKey("mapId")) return null;
        Object val = request.get("mapId");
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    /**
     * 增强战斗请求 DTO
     */
    public static class EnhancedCombatRequest {
        private Integer mapId;
        private Integer skillId;
        private Integer itemId;

        public Integer getMapId() { return mapId; }
        public void setMapId(Integer mapId) { this.mapId = mapId; }
        public Integer getSkillId() { return skillId; }
        public void setSkillId(Integer skillId) { this.skillId = skillId; }
        public Integer getItemId() { return itemId; }
        public void setItemId(Integer itemId) { this.itemId = itemId; }
    }
}
