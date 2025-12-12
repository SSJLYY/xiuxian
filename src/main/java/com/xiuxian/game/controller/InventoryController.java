package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.PlayerItemResponse;
import com.xiuxian.game.service.EnhancedInventoryService;
import com.xiuxian.game.service.InventoryService;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.features.inventory.enabled", havingValue = "true")
public class InventoryController {

    private final InventoryService inventoryService;
    private final EnhancedInventoryService enhancedInventoryService;
    private final PlayerService playerService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerItemResponse>>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerItemResponse> items = enhancedInventoryService.getPlayerInventory(playerId, type, sortBy, order);
            return ResponseEntity.ok(ApiResponse.success("获取背包成功", items));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/categorized")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, List<PlayerItemResponse>>>> listCategorized() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, List<PlayerItemResponse>> items = enhancedInventoryService.getPlayerInventoryByCategory(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取背包成功", items));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/organize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> organize() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            enhancedInventoryService.organizeInventory(playerId);
            return ResponseEntity.ok(ApiResponse.success("背包整理完成", null));
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
            EnhancedInventoryService.ItemUseRequest request = new EnhancedInventoryService.ItemUseRequest();
            request.setPlayerItemId(playerItemId);
            request.setQuantity(quantity);
            
            Map<String, Object> result = enhancedInventoryService.useItems(playerId, Collections.singletonList(request));
            return ResponseEntity.ok(ApiResponse.success("使用物品成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/use/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> useItemsBatch(
            @RequestBody List<EnhancedInventoryService.ItemUseRequest> requests) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> result = enhancedInventoryService.useItems(playerId, requests);
            return ResponseEntity.ok(ApiResponse.success("批量使用物品成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/sell/{playerItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sellItem(
            @PathVariable Integer playerItemId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            EnhancedInventoryService.ItemSellRequest request = new EnhancedInventoryService.ItemSellRequest();
            request.setPlayerItemId(playerItemId);
            request.setQuantity(quantity);
            
            Map<String, Object> result = enhancedInventoryService.sellItems(playerId, Collections.singletonList(request));
            return ResponseEntity.ok(ApiResponse.success("出售物品成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/sell/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sellItemsBatch(
            @RequestBody List<EnhancedInventoryService.ItemSellRequest> requests) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> result = enhancedInventoryService.sellItems(playerId, requests);
            return ResponseEntity.ok(ApiResponse.success("批量出售物品成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/details/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> getItemDetails(@PathVariable Integer itemId) {
        try {
            Object item = enhancedInventoryService.getItemDetails(itemId);
            return ResponseEntity.ok(ApiResponse.success("获取物品详情成功", item));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/lock/{playerItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> toggleItemLock(@PathVariable Integer playerItemId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            enhancedInventoryService.toggleItemLock(playerId, playerItemId);
            return ResponseEntity.ok(ApiResponse.success("物品锁定状态已切换", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}