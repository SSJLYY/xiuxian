package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.ShopItemResponse;
import com.xiuxian.game.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.features.shop.enabled", havingValue = "true")
public class ShopController {

    private final ShopService shopService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getShopItems() {
        try {
            List<ShopItemResponse> shopItems = shopService.getShopItems();
            return ResponseEntity.ok(ApiResponse.success("获取成功", shopItems));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{shopType}")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getShopItemsByType(@PathVariable String shopType) {
        try {
            List<ShopItemResponse> shopItems = shopService.getShopItemsByType(shopType);
            return ResponseEntity.ok(ApiResponse.success("获取成功", shopItems));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<Void>> buyItem(
            @RequestParam Integer shopItemId,
            @RequestParam Integer quantity,
            @RequestParam Integer playerId) { // 添加 playerId 参数
        try {
            shopService.buyItem(shopItemId, quantity, playerId); // 传递 playerId
            return ResponseEntity.ok(ApiResponse.success("购买成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<Void>> sellItem(
            @RequestParam Long playerItemId,
            @RequestParam Integer quantity,
            @RequestParam Integer playerId) { // 添加 playerId 参数
        try {
            shopService.sellItem(playerItemId, quantity, playerId); // 传递 playerId
            return ResponseEntity.ok(ApiResponse.success("出售成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
