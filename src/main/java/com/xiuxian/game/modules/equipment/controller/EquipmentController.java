package com.xiuxian.game.modules.equipment.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.PlayerEquipmentResponse;
import com.xiuxian.game.modules.equipment.entity.Equipment;
import com.xiuxian.game.modules.equipment.entity.PlayerEquipment;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.features.equipment.enabled", havingValue = "true")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final PlayerService playerService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerEquipment>>> getPlayerEquipment() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerEquipment> equipment = equipmentService.getPlayerEquipments(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerEquipmentResponse>>> getPlayerEquipmentWithDetails() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerEquipmentResponse> equipment = equipmentService.getPlayerEquipmentsWithDetails(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/equipped")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerEquipment>>> getEquippedEquipment() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerEquipment> equipment = equipmentService.getEquippedItems(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/equipped/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerEquipmentResponse>>> getEquippedEquipmentWithDetails() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerEquipmentResponse> equipment = equipmentService.getEquippedItemsWithDetails(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Equipment>>> getAvailableEquipment() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<Equipment> equipment = equipmentService.getAvailableEquipments(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Equipment>>> getAllEquipment() {
        try {
            List<Equipment> equipment = equipmentService.getAllEquipments();
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/acquire")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> acquireEquipment(@RequestParam Integer equipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerEquipment playerEquipment = equipmentService.acquireEquipment(equipmentId, playerId);
            return ResponseEntity.ok(ApiResponse.success("获取装备成功", playerEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/equip")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> equipEquipment(
            @RequestParam Integer playerEquipmentId,
            @RequestParam String slot) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerEquipment playerEquipment = equipmentService.equipItem(playerEquipmentId, slot, playerId);
            return ResponseEntity.ok(ApiResponse.success("装备成功", playerEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/unequip")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> unequipEquipment(
            @RequestParam Integer playerEquipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerEquipment playerEquipment = equipmentService.unequipItem(playerEquipmentId, playerId);
            return ResponseEntity.ok(ApiResponse.success("卸下装备成功", playerEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/repair")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> repairEquipment(
            @RequestParam Integer playerEquipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerEquipment playerEquipment = equipmentService.repairEquipment(playerEquipmentId, playerId);
            return ResponseEntity.ok(ApiResponse.success("修复装备成功", playerEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 强化装备
     */
    @PostMapping("/enhance/{playerEquipmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerEquipment>> enhanceEquipment(
            @PathVariable Integer playerEquipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerEquipment playerEquipment = equipmentService.enhanceEquipment(playerEquipmentId, playerId);
            return ResponseEntity.ok(ApiResponse.success("强化成功", playerEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取强化信息
     */
    @GetMapping("/enhance-info/{playerEquipmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEnhanceInfo(
            @PathVariable Integer playerEquipmentId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> info = equipmentService.getEnhanceInfo(playerEquipmentId, playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", info));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

