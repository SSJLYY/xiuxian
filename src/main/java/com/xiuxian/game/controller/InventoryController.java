package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.PlayerItemResponse;
import com.xiuxian.game.service.InventoryService;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlayerItemResponse>>> getInventory(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerItemResponse> inventory = inventoryService.getPlayerInventory(playerId, type, search, sortBy, order);
            return ResponseEntity.ok(ApiResponse.success("获取成功", inventory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<PlayerItemResponse>> addItem(
            @RequestParam Integer itemId,
            @RequestParam Integer quantity) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerItemResponse playerItem = inventoryService.addItemToInventory(playerId, itemId, quantity);
            return ResponseEntity.ok(ApiResponse.success("添加成功", playerItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 修复6：Controller 调用 Service 方法时传递正确的参数数量
    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @RequestParam Integer playerItemId,
            @RequestParam Integer quantity) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            inventoryService.removeItemFromInventory(playerItemId, quantity, playerId);
            return ResponseEntity.ok(ApiResponse.success("移除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 修复7：Controller 调用 Service 方法时传递正确的参数数量
    @PostMapping("/use")
    public ResponseEntity<ApiResponse<Map<String, Object>>> useItem(
            @RequestParam Integer playerItemId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> result = inventoryService.useItem(playerItemId, quantity, playerId);
            return ResponseEntity.ok(ApiResponse.success("使用成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/capacity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCapacity() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> capacity = new HashMap<>();
            capacity.put("maxSlots", 100);
            capacity.put("usedSlots", 10);
            capacity.put("availableSlots", 90);
            return ResponseEntity.ok(ApiResponse.success("获取成功", capacity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}