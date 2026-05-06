package com.xiuxian.game.modules.equipment.service;

import com.xiuxian.game.modules.shop.entity.Item;
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.dto.response.PlayerItemResponse;
import com.xiuxian.game.modules.shop.service.ItemService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

/**
 * 增强版背包服务
 *
 * <p>提供背包管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>背包物品查询（支持分类、排序）</li>
 *   <li>物品整理与堆叠</li>
 *   <li>物品锁定与解锁</li>
 *   <li>批量使用与出售</li>
 * </ul>
 *
 * <p>模块边界：通过PlayerService访问玩家数据，通过ItemService访问物品数据</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedInventoryService {

    private final ItemService itemService;
    private final PlayerService playerService;
    private final EquipmentService equipmentService;

    /**
     * 获取玩家背包物品，支持分类和排序
     *
     * <p>支持按类型筛选、按指定字段排序</p>
     *
     * @param playerId 玩家ID
     * @param type 物品类型筛选（可选）
     * @param sortBy 排序字段（可选）
     * @param order 排序方向（可选）
     * @return 背包物品列表
     */
    public List<PlayerItemResponse> getPlayerInventory(Integer playerId, String type, String sortBy, String order) {
        log.debug("查询玩家背包物品, playerId={}, type={}, sortBy={}, order={}", playerId, type, sortBy, order);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        List<PlayerItem> playerItems = playerService.getPlayerItemsByPlayerId(playerId);

        // 过滤逻辑
        if (type != null && !type.isEmpty()) {
            playerItems = playerItems.stream()
                    .filter(pi -> {
                        Item item = itemService.getItemById(pi.getItemId());
                        return item != null && item.getType().equals(type);
                    })
                    .collect(Collectors.toList());
        }

        // 排序逻辑
        if (sortBy != null && order != null) {
            Comparator<PlayerItem> comparator = getSortComparator(sortBy);
            if ("desc".equalsIgnoreCase(order)) {
                comparator = comparator.reversed();
            }
            playerItems = playerItems.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        } else {
            // 默认按类型和品质排序
            playerItems = playerItems.stream()
                    .sorted(Comparator.comparing(this::getItemTypeOrder)
                            .thenComparing(this::getItemQuality)
                            .reversed())
                    .collect(Collectors.toList());
        }

        List<PlayerItemResponse> result = playerItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        log.debug("查询玩家背包物品成功, playerId={}, count={}", playerId, result.size());
        return result;
    }

    /**
     * 获取所有玩家背包物品（按类型分类）
     *
     * <p>将背包物品按类型分类返回</p>
     *
     * @param playerId 玩家ID
     * @return 分类后的背包物品
     */
    public Map<String, List<PlayerItemResponse>> getPlayerInventoryByCategory(Integer playerId) {
        log.debug("查询玩家背包物品（按类型分类）, playerId={}", playerId);
        List<PlayerItemResponse> allItems = getPlayerInventory(playerId, null, null, null);
        
        Map<String, List<PlayerItemResponse>> categorizedItems = new HashMap<>();
        
        for (PlayerItemResponse item : allItems) {
            String type = item.getItemType() != null ? item.getItemType() : "未知";
            categorizedItems.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
        }
        
        log.debug("查询玩家背包物品（按类型分类）成功, playerId={}", playerId);
        return categorizedItems;
    }

    /**
     * 整理背包 - 自动堆叠相同物品并按类型和品质排序
     *
     * <p>自动合并可堆叠物品并按类型和品质排序</p>
     *
     * @param playerId 玩家ID
     */
    @Transactional
    public void organizeInventory(Integer playerId) {
        log.info("整理背包, playerId={}", playerId);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        List<PlayerItem> playerItems = playerService.getPlayerItemsByPlayerId(playerId);
        
        // 按物品ID分组
        Map<Integer, List<PlayerItem>> groupedItems = new HashMap<>();
        for (PlayerItem item : playerItems) {
            groupedItems.computeIfAbsent(item.getItemId(), k -> new ArrayList<>()).add(item);
        }
        
        // 合并可堆叠的物品
        for (Map.Entry<Integer, List<PlayerItem>> entry : groupedItems.entrySet()) {
            List<PlayerItem> items = entry.getValue();
            if (items.size() > 1) {
                // 获取物品模板
                Item itemTemplate = itemService.getItemById(entry.getKey());
                if (itemTemplate != null && itemTemplate.getStackable()) {
                    // 合并数量
                    int totalQuantity = items.stream().mapToInt(PlayerItem::getQuantity).sum();
                    
                    // 保留第一个物品，更新数量
                    PlayerItem firstItem = items.get(0);
                    firstItem.setQuantity(totalQuantity);
                    firstItem.setUpdatedAt(LocalDateTime.now());
                    playerService.updatePlayerItem(firstItem);
                    
                    // 删除其他重复物品
                    for (int i = 1; i < items.size(); i++) {
                        playerService.deletePlayerItem(items.get(i).getId());
                    }
                }
            }
        }
        log.info("整理背包成功, playerId={}", playerId);
    }

    /**
     * 锁定/解锁物品
     *
     * <p>切换物品的锁定状态</p>
     *
     * @param playerId 玩家ID
     * @param playerItemId 玩家物品ID
     */
    @Transactional
    public void toggleItemLock(Integer playerId, Integer playerItemId) {
        log.info("切换物品锁定状态, playerId={}, playerItemId={}", playerId, playerItemId);
        PlayerItem playerItem = playerService.getPlayerItemById(playerItemId);
        if (playerItem == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品不存在");
        }
        
        if (!playerItem.getPlayerId().equals(playerId)) {
            log.warn("物品不属于当前玩家, playerId={}, playerItemId={}", playerId, playerItemId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该物品不属于当前玩家");
        }
        
        // 切换锁定状态
        playerItem.setLocked(!playerItem.getLocked());
        playerItem.setUpdatedAt(LocalDateTime.now());
        playerService.updatePlayerItem(playerItem);
        log.info("切换物品锁定状态成功, playerId={}, playerItemId={}, locked={}", playerId, playerItemId, playerItem.getLocked());
    }

    /**
     * 批量使用物品
     *
     * <p>批量使用多个物品</p>
     *
     * @param playerId 玩家ID
     * @param useRequests 使用请求列表
     * @return 使用结果
     */
    @Transactional
    public Map<String, Object> useItems(Integer playerId, List<ItemUseRequest> useRequests) {
        log.info("批量使用物品, playerId={}, count={}", playerId, useRequests.size());
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        Map<String, Object> result = new HashMap<>();
        int totalUsed = 0;
        
        for (ItemUseRequest request : useRequests) {
            PlayerItem playerItem = playerService.getPlayerItemById(request.getPlayerItemId());
            if (playerItem == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品不存在: " + request.getPlayerItemId());
            }

            if (!playerItem.getPlayerId().equals(playerId)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该物品不属于当前玩家: " + request.getPlayerItemId());
            }

            // 检查物品是否被锁定
            if (playerItem.getLocked()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "物品已被锁定，无法使用: " + request.getPlayerItemId());
            }

            Item item = itemService.getItemById(playerItem.getItemId());
            if (item == null || !item.getUsable()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该物品不可使用: " + request.getPlayerItemId());
            }

            if (playerItem.getQuantity() < request.getQuantity()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "物品数量不足: " + request.getPlayerItemId());
            }

            // 使用指定数量的物品
            for (int i = 0; i < request.getQuantity(); i++) {
                applyItemEffect(player, item);
            }
            totalUsed += request.getQuantity();

            // 减少物品数量
            playerItem.setQuantity(playerItem.getQuantity() - request.getQuantity());
            if (playerItem.getQuantity() <= 0) {
                playerService.deletePlayerItem(playerItem.getId());
            } else {
                playerItem.setUpdatedAt(LocalDateTime.now());
                playerService.updatePlayerItem(playerItem);
            }
        }
        
        playerService.savePlayerProfile(player);
        
        result.put("used", totalUsed);
        result.put("message", "成功使用 " + totalUsed + " 个物品");
        log.info("批量使用物品成功, playerId={}, totalUsed={}", playerId, totalUsed);
        return result;
    }

    /**
     * 批量出售物品
     *
     * <p>批量出售多个物品</p>
     *
     * @param playerId 玩家ID
     * @param sellRequests 出售请求列表
     * @return 出售结果
     */
    @Transactional
    public Map<String, Object> sellItems(Integer playerId, List<ItemSellRequest> sellRequests) {
        log.info("批量出售物品, playerId={}, count={}", playerId, sellRequests.size());
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        Map<String, Object> result = new HashMap<>();
        long totalEarned = 0;
        int totalSold = 0;
        
        for (ItemSellRequest request : sellRequests) {
            PlayerItem playerItem = playerService.getPlayerItemById(request.getPlayerItemId());
            if (playerItem == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品不存在: " + request.getPlayerItemId());
            }

            if (!playerItem.getPlayerId().equals(playerId)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该物品不属于当前玩家: " + request.getPlayerItemId());
            }

            // 检查物品是否被锁定
            if (playerItem.getLocked()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "物品已被锁定，无法出售: " + request.getPlayerItemId());
            }

            Item item = itemService.getItemById(playerItem.getItemId());
            if (item == null || !item.getSellable()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该物品不可出售: " + request.getPlayerItemId());
            }

            if (playerItem.getQuantity() < request.getQuantity()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "物品数量不足: " + request.getPlayerItemId());
            }

        // 计算出售价格（通常是购买价格的50%）
            long sellPrice = ((long)item.getPrice() * request.getQuantity()) / 2;
            totalEarned += sellPrice;
            totalSold += request.getQuantity();

            // 减少物品数量
            playerItem.setQuantity(playerItem.getQuantity() - request.getQuantity());
            if (playerItem.getQuantity() <= 0) {
                playerService.deletePlayerItem(playerItem.getId());
            } else {
                playerItem.setUpdatedAt(LocalDateTime.now());
                playerService.updatePlayerItem(playerItem);
            }
        }
        
        // 增加玩家灵石
        player.setSpiritStones(defaultLong(player.getSpiritStones()) + totalEarned);
        playerService.savePlayerProfile(player);
        
        result.put("sold", totalSold);
        result.put("earned", totalEarned);
        result.put("message", "成功出售 " + totalSold + " 个物品，获得 " + totalEarned + " 灵石");
        log.info("批量出售物品成功, playerId={}, totalSold={}, totalEarned={}", playerId, totalSold, totalEarned);
        return result;
    }

    /**
     * 检查背包是否已满
     *
     * <p>检查玩家背包是否已满</p>
     *
     * @param playerId 玩家ID
     * @param maxCapacity 最大容量
     * @return 是否已满
     */
    public boolean isInventoryFull(Integer playerId, int maxCapacity) {
        log.debug("检查背包是否已满, playerId={}, maxCapacity={}", playerId, maxCapacity);
        List<PlayerItem> playerItems = playerService.getPlayerItemsByPlayerId(playerId);
        boolean isFull = playerItems.size() >= maxCapacity;
        log.debug("检查背包是否已满, playerId={}, isFull={}", playerId, isFull);
        return isFull;
    }

    /**
     * 获取物品详情
     *
     * <p>获取物品的详细信息</p>
     *
     * @param itemId 物品ID
     * @return 物品详情
     */
    public Item getItemDetails(Integer itemId) {
        log.debug("获取物品详情, itemId={}", itemId);
        Item item = itemService.getItemById(itemId);
        log.debug("获取物品详情成功, itemId={}", itemId);
        return item;
    }

    private void applyItemEffect(PlayerProfile player, Item item) {
        // 简化的物品效果应用
        // 实际应该根据item.effect来处理
        // 注意: 不在这里保存,由调用方统一保存
    }

    private Comparator<PlayerItem> getSortComparator(String sortBy) {
        switch (sortBy) {
            case "quantity":
                return Comparator.comparing(PlayerItem::getQuantity);
            case "created":
                return Comparator.comparing(PlayerItem::getCreatedAt);
            case "quality":
                return Comparator.comparing(this::getItemQuality);
            default:
                return Comparator.comparing(PlayerItem::getId);
        }
    }

    private int getItemQuality(PlayerItem playerItem) {
        Item item = itemService.getItemById(playerItem.getItemId());
        return item != null ? item.getQuality() : 0;
    }

    private int getItemTypeOrder(PlayerItem playerItem) {
        Item item = itemService.getItemById(playerItem.getItemId());
        if (item == null) return 0;
        
        // 定义类型优先级
        switch (item.getType()) {
            case "装备": return 1;
            case "消耗品": return 2;
            case "材料": return 3;
            case "任务物品": return 4;
            default: return 5;
        }
    }

    private PlayerItemResponse convertToResponse(PlayerItem playerItem) {
        Item item = itemService.getItemById(playerItem.getItemId());
        
        PlayerItemResponse response = new PlayerItemResponse();
        response.setId(playerItem.getId());
        response.setItemId(playerItem.getItemId());
        response.setQuantity(playerItem.getQuantity());
        response.setLocked(playerItem.getLocked()); // 添加锁定状态
        response.setCreatedAt(playerItem.getCreatedAt());
        response.setUpdatedAt(playerItem.getUpdatedAt());
        
        if (item != null) {
            response.setItemName(item.getName());
            response.setItemDescription(item.getDescription());
            response.setItemType(item.getType());
            response.setItemQuality(item.getQuality());
            response.setStackable(item.getStackable());
            response.setMaxStack(item.getMaxStack());
            response.setUsable(item.getUsable());
            response.setSellable(item.getSellable());
            response.setPrice(item.getPrice());
        }
        
        return response;
    }

    /**
     * 物品使用请求DTO
     */
    public static class ItemUseRequest {
        private Integer playerItemId;
        private Integer quantity;

        public Integer getPlayerItemId() {
            return playerItemId;
        }

        public void setPlayerItemId(Integer playerItemId) {
            this.playerItemId = playerItemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    /**
     * 物品出售请求DTO
     */
    public static class ItemSellRequest {
        private Integer playerItemId;
        private Integer quantity;

        public Integer getPlayerItemId() {
            return playerItemId;
        }

        public void setPlayerItemId(Integer playerItemId) {
            this.playerItemId = playerItemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
