package com.xiuxian.game.service;

import com.xiuxian.game.entity.Item;
import com.xiuxian.game.entity.PlayerItem;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.dto.response.PlayerItemResponse;
import com.xiuxian.game.mapper.ItemMapper;
import com.xiuxian.game.mapper.PlayerItemMapper;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnhancedInventoryService {

    private final ItemMapper itemMapper;
    private final PlayerItemMapper playerItemMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final EquipmentService equipmentService;

    /**
     * 获取玩家背包物品，支持分类和排序
     */
    public List<PlayerItemResponse> getPlayerInventory(Integer playerId, String type, String sortBy, String order) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        List<PlayerItem> playerItems = playerItemMapper.selectByPlayerId(playerId);

        // 过滤逻辑
        if (type != null && !type.isEmpty()) {
            playerItems = playerItems.stream()
                    .filter(pi -> {
                        Item item = itemMapper.selectById(pi.getItemId());
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

        return playerItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有玩家背包物品（按类型分类）
     */
    public Map<String, List<PlayerItemResponse>> getPlayerInventoryByCategory(Integer playerId) {
        List<PlayerItemResponse> allItems = getPlayerInventory(playerId, null, null, null);
        
        Map<String, List<PlayerItemResponse>> categorizedItems = new HashMap<>();
        
        for (PlayerItemResponse item : allItems) {
            String type = item.getItemType() != null ? item.getItemType() : "未知";
            categorizedItems.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
        }
        
        return categorizedItems;
    }

    /**
     * 整理背包 - 自动堆叠相同物品并按类型和品质排序
     */
    @Transactional
    public void organizeInventory(Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        List<PlayerItem> playerItems = playerItemMapper.selectByPlayerId(playerId);
        
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
                Item itemTemplate = itemMapper.selectById(entry.getKey());
                if (itemTemplate != null && itemTemplate.getStackable()) {
                    // 合并数量
                    int totalQuantity = items.stream().mapToInt(PlayerItem::getQuantity).sum();
                    
                    // 保留第一个物品，更新数量
                    PlayerItem firstItem = items.get(0);
                    firstItem.setQuantity(totalQuantity);
                    firstItem.setUpdatedAt(LocalDateTime.now());
                    playerItemMapper.updateById(firstItem);
                    
                    // 删除其他重复物品
                    for (int i = 1; i < items.size(); i++) {
                        playerItemMapper.deleteById(items.get(i).getId());
                    }
                }
            }
        }
    }

    /**
     * 锁定/解锁物品
     */
    @Transactional
    public void toggleItemLock(Integer playerId, Integer playerItemId) {
        PlayerItem playerItem = playerItemMapper.selectById(playerItemId);
        if (playerItem == null) {
            throw new IllegalArgumentException("物品不存在");
        }
        
        if (!playerItem.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("该物品不属于当前玩家");
        }
        
        // 切换锁定状态
        playerItem.setLocked(!playerItem.getLocked());
        playerItem.setUpdatedAt(LocalDateTime.now());
        playerItemMapper.updateById(playerItem);
    }

    /**
     * 批量使用物品
     */
    @Transactional
    public Map<String, Object> useItems(Integer playerId, List<ItemUseRequest> useRequests) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        Map<String, Object> result = new HashMap<>();
        int totalUsed = 0;
        
        for (ItemUseRequest request : useRequests) {
            PlayerItem playerItem = playerItemMapper.selectById(request.getPlayerItemId());
            if (playerItem == null) {
                throw new IllegalArgumentException("物品不存在: " + request.getPlayerItemId());
            }

            if (!playerItem.getPlayerId().equals(playerId)) {
                throw new IllegalArgumentException("该物品不属于当前玩家: " + request.getPlayerItemId());
            }

            // 检查物品是否被锁定
            if (playerItem.getLocked()) {
                throw new IllegalArgumentException("物品已被锁定，无法使用: " + request.getPlayerItemId());
            }

            Item item = itemMapper.selectById(playerItem.getItemId());
            if (item == null || !item.getUsable()) {
                throw new IllegalArgumentException("该物品不可使用: " + request.getPlayerItemId());
            }

            if (playerItem.getQuantity() < request.getQuantity()) {
                throw new IllegalArgumentException("物品数量不足: " + request.getPlayerItemId());
            }

            // 使用指定数量的物品
            for (int i = 0; i < request.getQuantity(); i++) {
                applyItemEffect(player, item);
            }
            totalUsed += request.getQuantity();

            // 减少物品数量
            playerItem.setQuantity(playerItem.getQuantity() - request.getQuantity());
            if (playerItem.getQuantity() <= 0) {
                playerItemMapper.deleteById(playerItem.getId());
            } else {
                playerItem.setUpdatedAt(LocalDateTime.now());
                playerItemMapper.updateById(playerItem);
            }
        }
        
        playerProfileMapper.updateById(player);
        
        result.put("used", totalUsed);
        result.put("message", "成功使用 " + totalUsed + " 个物品");
        return result;
    }

    /**
     * 批量出售物品
     */
    @Transactional
    public Map<String, Object> sellItems(Integer playerId, List<ItemSellRequest> sellRequests) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        Map<String, Object> result = new HashMap<>();
        long totalEarned = 0;
        int totalSold = 0;
        
        for (ItemSellRequest request : sellRequests) {
            PlayerItem playerItem = playerItemMapper.selectById(request.getPlayerItemId());
            if (playerItem == null) {
                throw new IllegalArgumentException("物品不存在: " + request.getPlayerItemId());
            }

            if (!playerItem.getPlayerId().equals(playerId)) {
                throw new IllegalArgumentException("该物品不属于当前玩家: " + request.getPlayerItemId());
            }

            // 检查物品是否被锁定
            if (playerItem.getLocked()) {
                throw new IllegalArgumentException("物品已被锁定，无法出售: " + request.getPlayerItemId());
            }

            Item item = itemMapper.selectById(playerItem.getItemId());
            if (item == null || !item.getSellable()) {
                throw new IllegalArgumentException("该物品不可出售: " + request.getPlayerItemId());
            }

            if (playerItem.getQuantity() < request.getQuantity()) {
                throw new IllegalArgumentException("物品数量不足: " + request.getPlayerItemId());
            }

            // 计算出售价格（通常是购买价格的50%）
            long sellPrice = ((long)item.getPrice() * request.getQuantity()) / 2;
            totalEarned += sellPrice;
            totalSold += request.getQuantity();

            // 减少物品数量
            playerItem.setQuantity(playerItem.getQuantity() - request.getQuantity());
            if (playerItem.getQuantity() <= 0) {
                playerItemMapper.deleteById(playerItem.getId());
            } else {
                playerItem.setUpdatedAt(LocalDateTime.now());
                playerItemMapper.updateById(playerItem);
            }
        }
        
        // 增加玩家灵石
        player.setSpiritStones(player.getSpiritStones() + totalEarned);
        playerProfileMapper.updateById(player);
        
        result.put("sold", totalSold);
        result.put("earned", totalEarned);
        result.put("message", "成功出售 " + totalSold + " 个物品，获得 " + totalEarned + " 灵石");
        return result;
    }

    /**
     * 检查背包是否已满
     */
    public boolean isInventoryFull(Integer playerId, int maxCapacity) {
        List<PlayerItem> playerItems = playerItemMapper.selectByPlayerId(playerId);
        return playerItems.size() >= maxCapacity;
    }

    /**
     * 获取物品详情
     */
    public Item getItemDetails(Integer itemId) {
        return itemMapper.selectById(itemId);
    }

    private void applyItemEffect(PlayerProfile player, Item item) {
        // 简化的物品效果应用
        // 实际应该根据item.effect来处理
        playerProfileMapper.updateById(player);
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
        Item item = itemMapper.selectById(playerItem.getItemId());
        return item != null ? item.getQuality() : 0;
    }

    private int getItemTypeOrder(PlayerItem playerItem) {
        Item item = itemMapper.selectById(playerItem.getItemId());
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
        Item item = itemMapper.selectById(playerItem.getItemId());
        
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
}