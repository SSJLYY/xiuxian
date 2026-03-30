package com.xiuxian.game.modules.equipment.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.PlayerEquipmentResponse;
import com.xiuxian.game.modules.equipment.entity.Equipment;
import com.xiuxian.game.modules.equipment.entity.PlayerEquipment;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 装备控制器
 *
 * <p>提供装备相关的REST API接口</p>
 *
 * <p>主要接口：</p>
 * <ul>
 *   <li>GET /api/equipment - 获取玩家装备列表</li>
 *   <li>GET /api/equipment/details - 获取玩家装备详情</li>
 *   <li>GET /api/equipment/equipped - 获取已穿戴装备</li>
 *   <li>GET /api/equipment/equipped/details - 获取已穿戴装备详情</li>
 *   <li>GET /api/equipment/available - 获取可用装备</li>
 *   <li>GET /api/equipment/all - 获取所有装备</li>
 *   <li>POST /api/equipment/acquire - 获取装备</li>
 *   <li>POST /api/equipment/equip - 穿戴装备</li>
 *   <li>POST /api/equipment/unequip - 卸下装备</li>
 *   <li>POST /api/equipment/repair - 修复装备</li>
 *   <li>POST /api/equipment/enhance/{playerEquipmentId} - 强化装备</li>
 *   <li>GET /api/equipment/enhance-info/{playerEquipmentId} - 获取强化信息</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.features.equipment.enabled", havingValue = "true")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final PlayerService playerService;

    /**
     * 获取玩家装备列表
     *
     * <p>获取当前玩家的所有装备</p>
     *
     * @return 玩家装备列表
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerEquipment>>> getPlayerEquipment() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取玩家装备列表, playerId={}", playerId);
            List<PlayerEquipment> equipment = equipmentService.getPlayerEquipments(playerId);
            log.info("获取玩家装备列表成功, playerId={}, count={}", playerId, equipment.size());
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            log.error("获取玩家装备列表失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取玩家装备详情
     *
     * <p>获取当前玩家的所有装备详情，包括装备模板信息</p>
     *
     * @return 玩家装备详情列表
     */
    @GetMapping("/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerEquipmentResponse>>> getPlayerEquipmentWithDetails() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取玩家装备详情, playerId={}", playerId);
            List<PlayerEquipmentResponse> equipment = equipmentService.getPlayerEquipmentsWithDetails(playerId);
            log.info("获取玩家装备详情成功, playerId={}, count={}", playerId, equipment.size());
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            log.error("获取玩家装备详情失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取已穿戴装备
     *
     * <p>获取当前玩家已穿戴的装备</p>
     *
     * @return 已穿戴装备列表
     */
    @GetMapping("/equipped")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerEquipment>>> getEquippedEquipment() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取已穿戴装备, playerId={}", playerId);
            List<PlayerEquipment> equipment = equipmentService.getEquippedItems(playerId);
            log.info("获取已穿戴装备成功, playerId={}, count={}", playerId, equipment.size());
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            log.error("获取已穿戴装备失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取已穿戴装备详情
     *
     * <p>获取当前玩家已穿戴的装备详情，包括装备模板信息</p>
     *
     * @return 已穿戴装备详情列表
     */
    @GetMapping("/equipped/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerEquipmentResponse>>> getEquippedEquipmentWithDetails() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取已穿戴装备详情, playerId={}", playerId);
            List<PlayerEquipmentResponse> equipment = equipmentService.getEquippedItemsWithDetails(playerId);
            log.info("获取已穿戴装备详情成功, playerId={}, count={}", playerId, equipment.size());
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            log.error("获取已穿戴装备详情失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取可用装备
     *
     * <p>获取当前玩家可用的装备（根据玩家等级筛选）</p>
     *
     * @return 可用装备列表
     */
    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Equipment>>> getAvailableEquipment() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取可用装备, playerId={}", playerId);
            List<Equipment> equipment = equipmentService.getAvailableEquipments(playerId);
            log.info("获取可用装备成功, playerId={}, count={}", playerId, equipment.size());
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            log.error("获取可用装备失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取所有装备
     *
     * <p>获取所有装备模板</p>
     *
     * @return 所有装备列表
     */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Equipment>>> getAllEquipment() {
        try {
            log.info("获取所有装备");
            List<Equipment> equipment = equipmentService.getAllEquipments();
            log.info("获取所有装备成功, count={}", equipment.size());
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            log.error("获取所有装备失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取装备
     *
     * <p>玩家获取新装备</p>
     *
     * @param equipmentId 装备ID
     * @return 获取的玩家装备
     */
    @PostMapping("/acquire")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> acquireEquipment(@RequestParam Integer equipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家获取装备, playerId={}, equipmentId={}", playerId, equipmentId);
            PlayerEquipment playerEquipment = equipmentService.acquireEquipment(equipmentId, playerId);
            log.info("玩家获取装备成功, playerId={}, equipmentId={}, playerEquipmentId={}", playerId, equipmentId, playerEquipment.getId());
            return ResponseEntity.ok(ApiResponse.success("获取装备成功", playerEquipment));
        } catch (Exception e) {
            log.error("玩家获取装备失败, equipmentId={}, error={}", equipmentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 穿戴装备
     *
     * <p>将装备穿戴到指定槽位</p>
     *
     * @param playerEquipmentId 玩家装备ID
     * @param slot 装备槽位
     * @return 穿戴后的玩家装备
     */
    @PostMapping("/equip")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> equipEquipment(
            @RequestParam Integer playerEquipmentId,
            @RequestParam String slot) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家穿戴装备, playerId={}, playerEquipmentId={}, slot={}", playerId, playerEquipmentId, slot);
            PlayerEquipment playerEquipment = equipmentService.equipItem(playerEquipmentId, slot, playerId);
            log.info("玩家穿戴装备成功, playerId={}, playerEquipmentId={}, slot={}", playerId, playerEquipmentId, slot);
            return ResponseEntity.ok(ApiResponse.success("装备成功", playerEquipment));
        } catch (Exception e) {
            log.error("玩家穿戴装备失败, playerEquipmentId={}, slot={}, error={}", playerEquipmentId, slot, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 卸下装备
     *
     * <p>卸下指定装备</p>
     *
     * @param playerEquipmentId 玩家装备ID
     * @return 卸下后的玩家装备
     */
    @PostMapping("/unequip")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> unequipEquipment(
            @RequestParam Integer playerEquipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家卸下装备, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
            PlayerEquipment playerEquipment = equipmentService.unequipItem(playerEquipmentId, playerId);
            log.info("玩家卸下装备成功, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
            return ResponseEntity.ok(ApiResponse.success("卸下装备成功", playerEquipment));
        } catch (Exception e) {
            log.error("玩家卸下装备失败, playerEquipmentId={}, error={}", playerEquipmentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 修复装备
     *
     * <p>修复指定装备，恢复耐久度</p>
     *
     * @param playerEquipmentId 玩家装备ID
     * @return 修复后的玩家装备
     */
    @PostMapping("/repair")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> repairEquipment(
            @RequestParam Integer playerEquipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家修复装备, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
            PlayerEquipment playerEquipment = equipmentService.repairEquipment(playerEquipmentId, playerId);
            log.info("玩家修复装备成功, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
            return ResponseEntity.ok(ApiResponse.success("修复装备成功", playerEquipment));
        } catch (Exception e) {
            log.error("玩家修复装备失败, playerEquipmentId={}, error={}", playerEquipmentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 强化装备
     *
     * <p>强化指定装备，提升装备属性</p>
     *
     * @param playerEquipmentId 玩家装备ID
     * @return 强化后的玩家装备
     */
    @PostMapping("/enhance/{playerEquipmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> enhanceEquipment(
            @PathVariable Integer playerEquipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家强化装备, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
            PlayerEquipment playerEquipment = equipmentService.enhanceEquipment(playerEquipmentId, playerId);
            log.info("玩家强化装备成功, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
            return ResponseEntity.ok(ApiResponse.success("强化成功", playerEquipment));
        } catch (Exception e) {
            log.error("玩家强化装备失败, playerEquipmentId={}, error={}", playerEquipmentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取强化信息
     *
     * <p>获取指定装备的强化信息，包括强化成本和成功率</p>
     *
     * @param playerEquipmentId 玩家装备ID
     * @return 强化信息
     */
    @GetMapping("/enhance-info/{playerEquipmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEnhanceInfo(
            @PathVariable Integer playerEquipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取强化信息, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
            Map<String, Object> info = equipmentService.getEnhanceInfo(playerEquipmentId, playerId);
            log.info("获取强化信息成功, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", info));
        } catch (Exception e) {
            log.error("获取强化信息失败, playerEquipmentId={}, error={}", playerEquipmentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

