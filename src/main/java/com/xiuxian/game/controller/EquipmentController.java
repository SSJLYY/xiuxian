package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.Equipment;
import com.xiuxian.game.entity.PlayerEquipment;
import com.xiuxian.game.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/equipments")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlayerEquipment>>> getPlayerEquipments() {
        try {
            List<PlayerEquipment> equipments = equipmentService.getPlayerEquipments();
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Equipment>>> getAvailableEquipments() {
        try {
            List<Equipment> equipments = equipmentService.getAvailableEquipments();
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/player")
    public ResponseEntity<ApiResponse<List<PlayerEquipment>>> getPlayerEquipments() {
        try {
            List<PlayerEquipment> equipments = equipmentService.getPlayerEquipments();
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/equipped")
    public ResponseEntity<ApiResponse<List<PlayerEquipment>>> getEquippedItems() {
        try {
            List<PlayerEquipment> equipments = equipmentService.getEquippedItems();
            return ResponseEntity.ok(ApiResponse.success("获取成功", equipments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/acquire/{equipmentId}")
    public ResponseEntity<ApiResponse<PlayerEquipment>> acquireEquipment(@PathVariable Integer equipmentId) {
        try {
            PlayerEquipment playerEquipment = equipmentService.acquireEquipment(equipmentId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", playerEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/equip/{playerEquipmentId}")
    public ResponseEntity<ApiResponse<PlayerEquipment>> equipItem(@PathVariable Integer playerEquipmentId) {
        try {
            PlayerEquipment playerEquipment = equipmentService.equipItem(playerEquipmentId);
            return ResponseEntity.ok(ApiResponse.success("装备成功", playerEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/unequip/{playerEquipmentId}")
    public ResponseEntity<ApiResponse<PlayerEquipment>> unequipItem(@PathVariable Integer playerEquipmentId) {
        try {
            PlayerEquipment playerEquipment = equipmentService.unequipItem(playerEquipmentId);
            return ResponseEntity.ok(ApiResponse.success("卸下成功", playerEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/repair/{playerEquipmentId}")
    public ResponseEntity<ApiResponse<PlayerEquipment>> repairEquipment(@PathVariable Integer playerEquipmentId) {
        try {
            PlayerEquipment playerEquipment = equipmentService.repairEquipment(playerEquipmentId);
            return ResponseEntity.ok(ApiResponse.success("修理成功", playerEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}