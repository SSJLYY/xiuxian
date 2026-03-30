package com.xiuxian.game.modules.auction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.request.ListAuctionRequest;
import com.xiuxian.game.modules.auction.entity.AuctionItem;
import com.xiuxian.game.modules.auction.service.AuctionService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拍卖行控制器
 *
 * <p>处理玩家拍卖行相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>上架物品到拍卖行</li>
 *   <li>购买拍卖物品</li>
 *   <li>取消拍卖</li>
 *   <li>获取拍卖物品列表</li>
 *   <li>获取玩家的拍卖物品列表</li>
 * </ul>
 *
 * <p>所有接口都需要JWT Token认证，确保只有登录用户才能访问。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/auction")
@RequiredArgsConstructor
public class AuctionController {
    
    private final AuctionService auctionService;
    private final PlayerService playerService;
    
    /**
     * 上架物品到拍卖行
     *
     * <p>玩家将物品上架到拍卖行进行出售。</p>
     *
     * @param request 上架请求
     * @return 上架的拍卖物品
     */
    @PostMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AuctionItem> listItem(@RequestBody ListAuctionRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家上架物品到拍卖行: playerId={}, itemType={}, itemId={}",
                    playerId, request.getItemType(), request.getItemId());
            
            AuctionItem auctionItem = auctionService.listItem(playerId, request);
            
            log.info("玩家上架物品成功: playerId={}, auctionItemId={}", playerId, auctionItem.getId());
            return ApiResponse.success("物品上架成功", auctionItem);
        } catch (Exception e) {
            log.error("玩家上架物品失败: playerId={}, error={}",
                    playerService.getCurrentPlayerId(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 购买拍卖物品
     *
     * <p>玩家购买拍卖行中的物品。</p>
     *
     * @param auctionItemId 拍卖物品ID
     * @return 购买的拍卖物品
     */
    @PostMapping("/buy/{auctionItemId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AuctionItem> buyItem(@PathVariable Long auctionItemId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家购买拍卖物品: playerId={}, auctionItemId={}", playerId, auctionItemId);
            
            AuctionItem auctionItem = auctionService.buyItem(playerId, auctionItemId);
            
            log.info("玩家购买拍卖物品成功: playerId={}, auctionItemId={}", playerId, auctionItemId);
            return ApiResponse.success("购买成功", auctionItem);
        } catch (Exception e) {
            log.error("玩家购买拍卖物品失败: playerId={}, auctionItemId={}, error={}",
                    playerService.getCurrentPlayerId(), auctionItemId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 取消拍卖
     *
     * <p>玩家取消自己上架的拍卖物品。</p>
     *
     * @param auctionItemId 拍卖物品ID
     * @return 取消的拍卖物品
     */
    @PostMapping("/cancel/{auctionItemId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AuctionItem> cancelAuction(@PathVariable Long auctionItemId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家取消拍卖: playerId={}, auctionItemId={}", playerId, auctionItemId);
            
            AuctionItem auctionItem = auctionService.cancelAuction(playerId, auctionItemId);
            
            log.info("玩家取消拍卖成功: playerId={}, auctionItemId={}", playerId, auctionItemId);
            return ApiResponse.success("拍卖已取消", auctionItem);
        } catch (Exception e) {
            log.error("玩家取消拍卖失败: playerId={}, auctionItemId={}, error={}",
                    playerService.getCurrentPlayerId(), auctionItemId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取拍卖物品列表
     *
     * <p>获取拍卖行中的物品列表，支持分页和筛选。</p>
     *
     * @param page 页码
     * @param size 每页数量
     * @param itemType 物品类型筛选
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 拍卖物品列表
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
            log.debug("获取拍卖物品列表: page={}, size={}, itemType={}", page, size, itemType);
            
            IPage<AuctionItem> auctionItems = auctionService.getAuctionItems(page, size, itemType, minPrice, maxPrice);
            
            log.debug("获取拍卖物品列表成功: total={}", auctionItems.getTotal());
            return ApiResponse.success("获取成功", auctionItems);
        } catch (Exception e) {
            log.error("获取拍卖物品列表失败: error={}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取玩家的拍卖物品列表
     *
     * <p>获取当前玩家的拍卖物品列表。</p>
     *
     * @param status 状态筛选
     * @return 拍卖物品列表
     */
    @GetMapping("/my-items")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AuctionItem>> getMyAuctions(@RequestParam(required = false) String status) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.debug("获取玩家拍卖物品列表: playerId={}, status={}", playerId, status);
            
            List<AuctionItem> auctionItems = auctionService.getPlayerAuctions(playerId, status);
            
            log.debug("获取玩家拍卖物品列表成功: playerId={}, count={}", playerId, auctionItems.size());
            return ApiResponse.success("获取成功", auctionItems);
        } catch (Exception e) {
            log.error("获取玩家拍卖物品列表失败: playerId={}, error={}",
                    playerService.getCurrentPlayerId(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
