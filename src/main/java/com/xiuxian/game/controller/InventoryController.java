package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.PlayerItemResponse;
import com.xiuxian.game.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlayerItemResponse>>> getInventory() {
        try {
            List<PlayerItemResponse> inventory = inventoryService.getPlayerInventory(null, null, null, null);
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
            PlayerItemResponse playerItem = inventoryService.addItemToInventory(itemId, quantity);
            return ResponseEntity.ok(ApiResponse.success("添加成功", playerItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @RequestParam Integer playerItemId,
            @RequestParam Integer quantity) {
        try {
            inventoryService.removeItemFromInventory(playerItemId, quantity);
            return ResponseEntity.ok(ApiResponse.success("移除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/use")
    public ResponseEntity<ApiResponse<Map<String, Object>>> useItem(
            @RequestParam Integer playerItemId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Map<String, Object> result = inventoryService.useItem(playerItemId, quantity);
            return ResponseEntity.ok(ApiResponse.success("使用成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}