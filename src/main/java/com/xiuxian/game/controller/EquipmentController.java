package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.Equipment;
import com.xiuxian.game.entity.PlayerEquipment;
import com.xiuxian.game.service.EquipmentService;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.features.equipment.enabled", havingValue = "true")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlayerEquipment>>> getPlayerEquipment() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerEquipment> equipment = equipmentService.getPlayerEquipments(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/equipped")
    public ResponseEntity<ApiResponse<List<PlayerEquipment>>> getEquippedEquipment() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerEquipment> equipment = equipmentService.getEquippedItems(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/available")
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
    public ResponseEntity<ApiResponse<List<Equipment>>> getAllEquipment() {
        try {
            List<Equipment> equipment = equipmentService.getAllEquipments();
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/acquire")
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
}
