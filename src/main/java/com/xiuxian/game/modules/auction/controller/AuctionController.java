package com.xiuxian.game.modules.auction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.request.ListAuctionRequest;
import com.xiuxian.game.modules.auction.entity.AuctionItem;
import com.xiuxian.game.modules.auction.service.AuctionService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auction")
@RequiredArgsConstructor
public class AuctionController {
    
    private final AuctionService auctionService;
    private final PlayerService playerService;
    
    /**
     * 上架物品到拍卖行
     */
    @PostMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AuctionItem> listItem(@RequestBody ListAuctionRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            AuctionItem auctionItem = auctionService.listItem(playerId, request);
            return ApiResponse.success("物品上架成功", auctionItem);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 购买拍卖物品
     */
    @PostMapping("/buy/{auctionItemId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AuctionItem> buyItem(@PathVariable Long auctionItemId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            AuctionItem auctionItem = auctionService.buyItem(playerId, auctionItemId);
            return ApiResponse.success("购买成功", auctionItem);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 取消拍卖
     */
    @PostMapping("/cancel/{auctionItemId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AuctionItem> cancelAuction(@PathVariable Long auctionItemId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            AuctionItem auctionItem = auctionService.cancelAuction(playerId, auctionItemId);
            return ApiResponse.success("拍卖已取消", auctionItem);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取拍卖物品列表
     */
    @GetMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<IPage<AuctionItem>> getAuctionItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice) {
        try {
            IPage<AuctionItem> auctionItems = auctionService.getAuctionItems(page, size, itemType, minPrice, maxPrice);
            return ApiResponse.success("获取成功", auctionItems);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取玩家的拍卖物品列表
     */
    @GetMapping("/my-items")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AuctionItem>> getMyAuctions(@RequestParam(required = false) String status) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<AuctionItem> auctionItems = auctionService.getPlayerAuctions(playerId, status);
            return ApiResponse.success("获取成功", auctionItems);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
