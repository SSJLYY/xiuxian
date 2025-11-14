package com.xiuxian.game.service;

import com.xiuxian.game.entity.Equipment;
import com.xiuxian.game.entity.Item;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.ShopItem;
import com.xiuxian.game.dto.response.ShopItemResponse;
import com.xiuxian.game.mapper.ItemMapper;
import com.xiuxian.game.mapper.ShopItemMapper;
import com.xiuxian.game.mapper.EquipmentMapper;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

@Service
@ConditionalOnProperty(value = "app.features.shop.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ShopService {

    private final ItemMapper itemMapper;
    private final ShopItemMapper shopItemMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final InventoryService inventoryService;
    private final EquipmentService equipmentService;
    private final EquipmentMapper equipmentMapper;

    public List<ShopItemResponse> getShopItems() {
        return shopItemMapper.selectAvailableItems().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ShopItemResponse> getShopItemsByType(String shopType) {
        return shopItemMapper.selectByShopType(shopType).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<String> getShopCategories() {
        return Arrays.asList(
                "general",     // 消耗品
                "equipment",   // 装备
                "materials",   // 材料
                "special"      // 特殊物品
        );
    }

    public String getCategoryDisplayName(String category) {
        switch (category) {
            case "general": return "消耗品";
            case "equipment": return "装备";
            case "materials": return "材料";
            case "special": return "特殊物品";
            default: return "未知分类";
        }
    }

    public Map<String, List<ShopItemResponse>> getShopItemsGroupedByCategory() {
        return getShopItems().stream()
                .collect(Collectors.groupingBy(ShopItemResponse::getShopType));
    }

    public Map<String, Object> getShopStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<ShopItem> availableItems = shopItemMapper.selectAvailableItems();

        // 按分类统计商品数量
        Map<String, Long> categoryCount = availableItems.stream()
                .collect(Collectors.groupingBy(
                        ShopItem::getShopType,
                        Collectors.counting()
                ));

        stats.put("totalItems", shopItemMapper.selectList(null).size());
        stats.put("availableItems", availableItems.size());
        stats.put("categoryCount", categoryCount);

        return stats;
    }

    @Transactional
    public void buyShopItem(Integer playerId, Integer shopItemId, Integer quantity) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        ShopItem shopItem = shopItemMapper.selectById(shopItemId);
        if (shopItem == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        if (!shopItem.getIsAvailable()) {
            throw new IllegalArgumentException("商品已下架");
        }

        // 检查库存
        if (!shopItem.hasStock(quantity)) {
            throw new IllegalArgumentException("库存不足");
        }

        // 计算总价
        long totalPrice = shopItem.getTotalPrice(quantity);
        
        // 检查玩家灵石
        if (player.getSpiritStones() < totalPrice) {
            throw new IllegalArgumentException("灵石不足");
        }

        // 扣除灵石
        player.setSpiritStones(player.getSpiritStones() - totalPrice);
        playerProfileMapper.updateById(player);

        // 减少库存
        shopItem.decreaseStock(quantity);
        shopItemMapper.updateById(shopItem);

        // 添加物品到背包或装备
        if (shopItem.getItemId() != null) {
            inventoryService.addItemToInventory(playerId, shopItem.getItemId(), quantity);
        } else if (shopItem.getEquipmentId() != null) {
            equipmentService.acquireEquipment(shopItem.getEquipmentId(), playerId);
        }
    }

    // Controller调用的buyItem方法
    @Transactional
    public void buyItem(Integer shopItemId, Integer quantity, Integer playerId) {
        buyShopItem(playerId, shopItemId, quantity);
    }

    // 出售物品
    @Transactional
    public void sellItem(Long playerItemId, Integer quantity, Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        // 调用背包服务出售物品
        inventoryService.sellItem(playerId, Integer.valueOf(playerItemId.intValue()), quantity);
    }

    @Transactional
    public void initializeShopItems() {
        long count = shopItemMapper.selectList(null).size();
        if (count == 0) {
            // 从items表获取物品并添加到商店
            List<Item> items = itemMapper.selectList(null);
            for (Item item : items) {
                ShopItem shopItem = ShopItem.builder()
                        .itemId(item.getId())
                        .shopType("general")
                        .priceSpiritStones(item.getPrice())
                        .priceContributionPoints(0)
                        .stock(-1)
                        .isAvailable(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                shopItemMapper.insert(shopItem);
            }

            // 从equipments表获取装备并添加到商店
            List<Equipment> equipments = equipmentMapper.selectList(null);
            for (Equipment equipment : equipments) {
                ShopItem shopItem = ShopItem.builder()
                        .equipmentId(equipment.getId())
                        .shopType("equipment")
                        .priceSpiritStones(equipment.getPrice())
                        .priceContributionPoints(0)
                        .stock(-1)
                        .isAvailable(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                shopItemMapper.insert(shopItem);
            }
        }
    }

    private ShopItemResponse convertToResponse(ShopItem shopItem) {
        ShopItemResponse response = new ShopItemResponse();
        response.setId(shopItem.getId().longValue());
        response.setShopType(shopItem.getShopType());
        response.setPriceSpiritStones(shopItem.getPriceSpiritStones());
        response.setPriceContributionPoints(shopItem.getPriceContributionPoints());
        response.setStock(shopItem.getStock());
        response.setIsAvailable(shopItem.getIsAvailable());

        if (shopItem.getItemId() != null) {
            Item item = itemMapper.selectById(shopItem.getItemId());
            if (item != null) {
                response.setItemId(item.getId().longValue());
                response.setItemName(item.getName());
                response.setItemDescription(item.getDescription());
                response.setItemType(item.getType());
                response.setItemQuality(item.getQuality());
            }
        }

        if (shopItem.getEquipmentId() != null) {
            Equipment equipment = equipmentMapper.selectById(shopItem.getEquipmentId());
            if (equipment != null) {
                response.setEquipmentId(equipment.getId().longValue());
                response.setEquipmentName(equipment.getName());
                response.setEquipmentDescription(equipment.getDescription());
                response.setEquipmentType(equipment.getType());
                response.setEquipmentQuality(equipment.getQuality());
                response.setRequiredLevel(equipment.getRequiredLevel());
            }
        }

        return response;
    }
}
