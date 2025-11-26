package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.PlayerItemResponse;
import com.xiuxian.game.entity.PlayerItem;
import com.xiuxian.game.service.InventoryService;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.features.inventory.enabled", havingValue = "true")
public class InventoryController {

    private final InventoryService inventoryService;
    private final PlayerService playerService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerItemResponse>>> list() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerItemResponse> items = inventoryService.getPlayerInventory(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取背包成功", items));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/use/{playerItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> useItem(
            @PathVariable Integer playerItemId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> result = inventoryService.useItem(playerItemId, quantity, playerId);
            return ResponseEntity.ok(ApiResponse.success("使用物品成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/sell/{playerItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> sellItem(
            @PathVariable Integer playerItemId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            inventoryService.sellItem(playerId, playerItemId, quantity);
            return ResponseEntity.ok(ApiResponse.success("出售物品成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
