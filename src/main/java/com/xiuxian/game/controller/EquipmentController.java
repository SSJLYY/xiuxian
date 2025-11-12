package com.xiuxian.game.controller;

import com.xiuxian.game.entity.Equipment;
import com.xiuxian.game.entity.PlayerEquipment;
import com.xiuxian.game.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<List<PlayerEquipment>> getPlayerEquipment(@RequestParam Integer playerId) {
        return ResponseEntity.ok(equipmentService.getPlayerEquipments(playerId));
    }

    @GetMapping("/equipped")
    public ResponseEntity<List<PlayerEquipment>> getEquippedEquipment(@RequestParam Integer playerId) {
        return ResponseEntity.ok(equipmentService.getEquippedItems(playerId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Equipment>> getAvailableEquipment(@RequestParam Integer playerId) {
        return ResponseEntity.ok(equipmentService.getAvailableEquipments(playerId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipments());
    }

    @PostMapping("/acquire")
    public ResponseEntity<PlayerEquipment> acquireEquipment(
            @RequestParam Integer equipmentId,
            @RequestParam Integer playerId) {
        return ResponseEntity.ok(equipmentService.acquireEquipment(equipmentId, playerId));
    }

    @PostMapping("/equip")
    public ResponseEntity<PlayerEquipment> equipEquipment(
            @RequestParam Integer playerEquipmentId,
            @RequestParam String slot,
            @RequestParam Integer playerId) {
        return ResponseEntity.ok(equipmentService.equipItem(playerEquipmentId, slot, playerId));
    }

    @PostMapping("/unequip")
    public ResponseEntity<PlayerEquipment> unequipEquipment(
            @RequestParam Integer playerEquipmentId,
            @RequestParam Integer playerId) {
        return ResponseEntity.ok(equipmentService.unequipItem(playerEquipmentId, playerId));
    }

    @PostMapping("/repair")
    public ResponseEntity<PlayerEquipment> repairEquipment(
            @RequestParam Integer playerEquipmentId,
            @RequestParam Integer playerId) {
        return ResponseEntity.ok(equipmentService.repairEquipment(playerEquipmentId, playerId));
    }
}