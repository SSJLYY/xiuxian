package com.xiuxian.game.modules.equipment.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.PlayerItemResponse;
import com.xiuxian.game.modules.equipment.service.EnhancedInventoryService;
import com.xiuxian.game.modules.equipment.service.InventoryService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 背包控制器
 *
 * <p>提供背包相关的REST API接口</p>
 *
 * <p>主要接口：</p>
 * <ul>
 *   <li>GET /api/inventory - 获取背包物品列表</li>
 *   <li>GET /api/inventory/categorized - 获取分类背包物品</li>
 *   <li>POST /api/inventory/organize - 整理背包</li>
 *   <li>POST /api/inventory/use/{playerItemId} - 使用物品</li>
 *   <li>POST /api/inventory/use/batch - 批量使用物品</li>
 *   <li>POST /api/inventory/sell/{playerItemId} - 出售物品</li>
 *   <li>POST /api/inventory/sell/batch - 批量出售物品</li>
 *   <li>GET /api/inventory/details/{itemId} - 获取物品详情</li>
 *   <li>POST /api/inventory/lock/{playerItemId} - 切换物品锁定状态</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.features.inventory.enabled", havingValue = "true")
public class InventoryController {

    private final InventoryService inventoryService;
    private final EnhancedInventoryService enhancedInventoryService;
    private final PlayerService playerService;

    /**
     * 获取背包物品列表
     *
     * <p>获取当前玩家的背包物品，支持筛选和排序</p>
     *
     * @param type 物品类型筛选（可选）
     * @param sortBy 排序字段（可选）
     * @param order 排序方向（可选）
     * @return 背包物品列表
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PlayerItemResponse>>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取背包物品列表, playerId={}, type={}, sortBy={}, order={}", playerId, type, sortBy, order);
            List<PlayerItemResponse> items = enhancedInventoryService.getPlayerInventory(playerId, type, sortBy, order);
            log.info("获取背包物品列表成功, playerId={}, count={}", playerId, items.size());
            return ResponseEntity.ok(ApiResponse.success("获取背包成功", items));
        } catch (Exception e) {
            log.error("获取背包物品列表失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取分类背包物品
     *
     * <p>获取当前玩家的背包物品，按类型分类</p>
     *
     * @return 分类背包物品
     */
    @GetMapping("/categorized")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, List<PlayerItemResponse>>>> listCategorized() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取分类背包物品, playerId={}", playerId);
            Map<String, List<PlayerItemResponse>> items = enhancedInventoryService.getPlayerInventoryByCategory(playerId);
            log.info("获取分类背包物品成功, playerId={}", playerId);
            return ResponseEntity.ok(ApiResponse.success("获取背包成功", items));
        } catch (Exception e) {
            log.error("获取分类背包物品失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 整理背包
     *
     * <p>整理当前玩家的背包物品</p>
     *
     * @return 整理结果
     */
    @PostMapping("/organize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> organize() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("整理背包, playerId={}", playerId);
            enhancedInventoryService.organizeInventory(playerId);
            log.info("整理背包成功, playerId={}", playerId);
            return ResponseEntity.ok(ApiResponse.success("背包整理完成", null));
        } catch (Exception e) {
            log.error("整理背包失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 使用物品
     *
     * <p>使用指定数量的物品</p>
     *
     * @param playerItemId 玩家物品ID
     * @param quantity 使用数量
     * @return 使用结果
     */
    @PostMapping("/use/{playerItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> useItem(
            @PathVariable Integer playerItemId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("使用物品, playerId={}, playerItemId={}, quantity={}", playerId, playerItemId, quantity);
            EnhancedInventoryService.ItemUseRequest request = new EnhancedInventoryService.ItemUseRequest();
            request.setPlayerItemId(playerItemId);
            request.setQuantity(quantity);
            
            Map<String, Object> result = enhancedInventoryService.useItems(playerId, Collections.singletonList(request));
            log.info("使用物品成功, playerId={}, playerItemId={}, quantity={}", playerId, playerItemId, quantity);
            return ResponseEntity.ok(ApiResponse.success("使用物品成功", result));
        } catch (Exception e) {
            log.error("使用物品失败, playerItemId={}, quantity={}, error={}", playerItemId, quantity, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 批量使用物品
     *
     * <p>批量使用多个物品</p>
     *
     * @param requests 物品使用请求列表
     * @return 使用结果
     */
    @PostMapping("/use/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> useItemsBatch(
            @RequestBody List<EnhancedInventoryService.ItemUseRequest> requests) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("批量使用物品, playerId={}, count={}", playerId, requests.size());
            Map<String, Object> result = enhancedInventoryService.useItems(playerId, requests);
            log.info("批量使用物品成功, playerId={}, count={}", playerId, requests.size());
            return ResponseEntity.ok(ApiResponse.success("批量使用物品成功", result));
        } catch (Exception e) {
            log.error("批量使用物品失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 出售物品
     *
     * <p>出售指定数量的物品</p>
     *
     * @param playerItemId 玩家物品ID
     * @param quantity 出售数量
     * @return 出售结果
     */
    @PostMapping("/sell/{playerItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sellItem(
            @PathVariable Integer playerItemId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("出售物品, playerId={}, playerItemId={}, quantity={}", playerId, playerItemId, quantity);
            EnhancedInventoryService.ItemSellRequest request = new EnhancedInventoryService.ItemSellRequest();
            request.setPlayerItemId(playerItemId);
            request.setQuantity(quantity);
            
            Map<String, Object> result = enhancedInventoryService.sellItems(playerId, Collections.singletonList(request));
            log.info("出售物品成功, playerId={}, playerItemId={}, quantity={}", playerId, playerItemId, quantity);
            return ResponseEntity.ok(ApiResponse.success("出售物品成功", result));
        } catch (Exception e) {
            log.error("出售物品失败, playerItemId={}, quantity={}, error={}", playerItemId, quantity, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 批量出售物品
     *
     * <p>批量出售多个物品</p>
     *
     * @param requests 物品出售请求列表
     * @return 出售结果
     */
    @PostMapping("/sell/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sellItemsBatch(
            @RequestBody List<EnhancedInventoryService.ItemSellRequest> requests) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("批量出售物品, playerId={}, count={}", playerId, requests.size());
            Map<String, Object> result = enhancedInventoryService.sellItems(playerId, requests);
            log.info("批量出售物品成功, playerId={}, count={}", playerId, requests.size());
            return ResponseEntity.ok(ApiResponse.success("批量出售物品成功", result));
        } catch (Exception e) {
            log.error("批量出售物品失败, error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取物品详情
     *
     * <p>获取指定物品的详细信息</p>
     *
     * @param itemId 物品ID
     * @return 物品详情
     */
    @GetMapping("/details/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> getItemDetails(@PathVariable Integer itemId) {
        try {
            log.info("获取物品详情, itemId={}", itemId);
            Object item = enhancedInventoryService.getItemDetails(itemId);
            log.info("获取物品详情成功, itemId={}", itemId);
            return ResponseEntity.ok(ApiResponse.success("获取物品详情成功", item));
        } catch (Exception e) {
            log.error("获取物品详情失败, itemId={}, error={}", itemId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 切换物品锁定状态
     *
     * <p>切换指定物品的锁定状态</p>
     *
     * @param playerItemId 玩家物品ID
     * @return 切换结果
     */
    @PostMapping("/lock/{playerItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> toggleItemLock(@PathVariable Integer playerItemId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("切换物品锁定状态, playerId={}, playerItemId={}", playerId, playerItemId);
            enhancedInventoryService.toggleItemLock(playerId, playerItemId);
            log.info("切换物品锁定状态成功, playerId={}, playerItemId={}", playerId, playerItemId);
            return ResponseEntity.ok(ApiResponse.success("物品锁定状态已切换", null));
        } catch (Exception e) {
            log.error("切换物品锁定状态失败, playerItemId={}, error={}", playerItemId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
