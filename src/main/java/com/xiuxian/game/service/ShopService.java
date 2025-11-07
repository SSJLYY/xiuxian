package com.xiuxian.game.service;

import com.xiuxian.game.entity.Item;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.ShopItem;
import com.xiuxian.game.dto.response.ShopItemResponse;
import com.xiuxian.game.repository.ItemRepository;
import com.xiuxian.game.repository.ShopItemRepository;
import com.xiuxian.game.repository.EquipmentRepository;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.xiuxian.game.util.Java8Compatibility;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ItemRepository itemRepository;
    private final ShopItemRepository shopItemRepository;
    private final PlayerService playerService;
    private final InventoryService inventoryService;
    private final EquipmentService equipmentService;
    private final EquipmentRepository equipmentRepository;

    public List<ShopItemResponse> getShopItems() {
        return shopItemRepository.findByIsAvailable(true).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ShopItemResponse> getShopItemsByType(String shopType) {
        return shopItemRepository.findByShopTypeAndIsAvailable(shopType, true).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取商店分类信息
     */
    public List<String> getShopCategories() {
        return Java8Compatibility.listOf(
            "general",     // 消耗品
            "equipment",   // 装备
            "materials",   // 材料
            "special"      // 特殊物品
        );
    }

    /**
     * 获取分类对应的中文名称
     */
    public String getCategoryDisplayName(String category) {
        switch (category) {
            case "general": return "消耗品";
            case "equipment": return "装备";
            case "materials": return "材料";
            case "special": return "特殊物品";
            default: return "未知分类";
        }
    }

    /**
     * 获取所有商店商品按分类分组
     */
    public java.util.Map<String, List<ShopItemResponse>> getShopItemsGroupedByCategory() {
        return getShopItems().stream()
                .collect(java.util.stream.Collectors.groupingBy(ShopItemResponse::getShopType));
    }

    /**
     * 获取商店商品统计信息
     */
    public java.util.Map<String, Object> getShopStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 按分类统计商品数量
        java.util.Map<String, Long> categoryCount = shopItemRepository.findByIsAvailable(true).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    ShopItem::getShopType, 
                    java.util.stream.Collectors.counting()
                ));
        
        stats.put("totalItems", shopItemRepository.count());
        stats.put("availableItems", shopItemRepository.findByIsAvailable(true).size());
        stats.put("categoryCount", categoryCount);
        
        return stats;
    }

    @Transactional
    public void buyItem(Integer shopItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 数量必须大于0");
        }

        PlayerProfile player = playerService.getCurrentPlayerProfile();
        ShopItem shopItem = shopItemRepository.findById(shopItemId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_ITEM_NOT_FOUND + ": 商店物品不存在"));

        if (!shopItem.getIsAvailable()) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 该物品不可购买");
        }

        // 检查库存
        if (shopItem.getStock() >= 0 && shopItem.getStock() < quantity) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + ": 库存不足");
        }

        // 计算总价格
        long totalCost = (long) shopItem.getPriceSpiritStones() * quantity;
        if (player.getSpiritStones() < totalCost) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + ": 灵石不足");
        }

        // 扣除灵石
        player.setSpiritStones(player.getSpiritStones() - totalCost);
        playerService.savePlayerProfile(player);

        // 更新库存
        if (shopItem.getStock() >= 0) {
            shopItem.setStock(shopItem.getStock() - quantity);
            shopItemRepository.save(shopItem);
        }

        // 根据商品类型处理购买
        if (shopItem.getEquipment() != null) {
            // 装备商品 - 直接添加到玩家装备库
            buyEquipment(player, shopItem.getEquipment(), quantity);
        } else if (shopItem.getItem() != null) {
            // 普通物品 - 添加到背包
            inventoryService.addItemToInventory(shopItem.getItem().getId(), quantity);
        }
    }

    /**
     * 购买装备
     */
    @Transactional
    public void buyEquipment(PlayerProfile player, com.xiuxian.game.entity.Equipment equipment, Integer quantity) {
        for (int i = 0; i < quantity; i++) {
            // 检查等级要求
            if (player.getLevel() < equipment.getRequiredLevel()) {
                throw new IllegalArgumentException("等级不足，无法购买此装备（需要等级" + equipment.getRequiredLevel() + "）");
            }
            
            // 获取装备并添加到玩家装备库
            equipmentService.acquireEquipment(equipment.getId());
        }
    }

    /**
     * 购买并自动装备装备（如果是第一次购买且玩家等级足够）
     */
    @Transactional
    public void buyAndEquipItem(Integer shopItemId) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        ShopItem shopItem = shopItemRepository.findById(shopItemId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_ITEM_NOT_FOUND + ": 商店物品不存在"));

        if (shopItem.getEquipment() == null) {
            throw new IllegalArgumentException("该商品不是装备，无法自动装备");
        }

        // 检查等级要求
        if (player.getLevel() < shopItem.getEquipment().getRequiredLevel()) {
            throw new IllegalArgumentException("等级不足，无法购买此装备（需要等级" + shopItem.getEquipment().getRequiredLevel() + "）");
        }

        // 购买装备
        buyItem(shopItemId, 1);

        // 自动装备（如果玩家该槽位没有装备）
        // 这里简化处理，实际应该检查玩家是否已拥有该装备
    }

    /**
     * 检查玩家是否可以购买某个商店物品
     */
    public boolean canPlayerBuyItem(Integer shopItemId) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        ShopItem shopItem = shopItemRepository.findById(shopItemId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_ITEM_NOT_FOUND + ": 商店物品不存在"));

        if (!shopItem.getIsAvailable()) {
            return false;
        }

        // 检查库存
        if (shopItem.getStock() >= 0 && shopItem.getStock() <= 0) {
            return false;
        }

        // 检查灵石
        if (player.getSpiritStones() < shopItem.getPriceSpiritStones()) {
            return false;
        }

        // 如果是装备，检查等级要求
        if (shopItem.getEquipment() != null) {
            if (player.getLevel() < shopItem.getEquipment().getRequiredLevel()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取玩家可购买的装备列表
     */
    public List<ShopItemResponse> getAvailableEquipmentForPlayer() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return shopItemRepository.findByShopTypeAndIsAvailable("equipment", true).stream()
                .filter(shopItem -> {
                    if (shopItem.getEquipment() == null) {
                        return false;
                    }
                    return player.getLevel() >= shopItem.getEquipment().getRequiredLevel();
                })
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void sellItem(Long playerItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 数量必须大于0");
        }

        PlayerProfile player = playerService.getCurrentPlayerProfile();
        
        // 这里简化实现，实际应该从PlayerItem实体获取物品信息
        // 计算售价（通常为购买价的一半）
        // 注意：这里需要从PlayerItemRepository直接查询
        // 暂时跳过具体实现
        throw new UnsupportedOperationException("出售功能待实现");
    }

    @Transactional
    public void initializeDefaultShopItems() {
        // 检查是否已经有商店物品数据
        if (shopItemRepository.count() > 0) {
            return; // 已有数据，不需要重复初始化
        }

        // 初始化基础物品商店
        initializeItemShop();
        
        // 初始化装备商店
        initializeEquipmentShop();
        
        // 初始化材料商店
        initializeMaterialShop();
    }

    private void initializeItemShop() {
        // 基础丹药商店
        createShopItem(1, "general", 50, 0, 100);  // 聚气丹
        createShopItem(3, "general", 100, 0, 50);  // 强化石
        createShopItem(4, "general", 80, 0, 20);   // 回春丹
        createShopItem(5, "general", 80, 0, 20);   // 小型生命药剂
        createShopItem(6, "general", 80, 0, 20);   // 小型灵力药剂
        createShopItem(7, "general", 200, 0, 10);  // 初级经验丹
        createShopItem(8, "general", 900, 0, 5);   // 小型灵石袋
    }

    private void initializeEquipmentShop() {
        // 装备商店 - 武器类
        createEquipmentShopItem(1, "equipment", 100, 0, 10);   // 木剑
        createEquipmentShopItem(4, "equipment", 500, 0, 5);    // 铁剑
        createEquipmentShopItem(6, "equipment", 1200, 0, 2);   // 法杖
        
        // 装备商店 - 防具类
        createEquipmentShopItem(2, "equipment", 150, 0, 10);   // 布袍
        createEquipmentShopItem(5, "equipment", 600, 0, 5);    // 皮甲
        
        // 装备商店 - 饰品类
        createEquipmentShopItem(3, "equipment", 300, 0, 5);    // 玉符
    }

    private void initializeMaterialShop() {
        // 材料商店
        createShopItem(9, "materials", 10, 0, 1000);  // 铁矿石
        createShopItem(10, "materials", 10, 0, 1000); // 灵草叶
    }

    private void createShopItem(Integer itemId, String shopType, Integer priceSpiritStones, Integer priceContributionPoints, Integer stock) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在: " + itemId));
        
        ShopItem shopItem = new ShopItem();
        shopItem.setItem(item);
        shopItem.setShopType(shopType);
        shopItem.setPriceSpiritStones(priceSpiritStones);
        shopItem.setPriceContributionPoints(priceContributionPoints);
        shopItem.setStock(stock);
        shopItem.setIsAvailable(true);
        
        shopItemRepository.save(shopItem);
    }

    private void createEquipmentShopItem(Integer equipmentId, String shopType, Integer priceSpiritStones, Integer priceContributionPoints, Integer stock) {
        // 使用EquipmentRepository查找装备
        com.xiuxian.game.entity.Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("装备不存在: " + equipmentId));
        
        ShopItem shopItem = new ShopItem();
        shopItem.setEquipment(equipment);
        shopItem.setShopType(shopType);
        shopItem.setPriceSpiritStones(priceSpiritStones);
        shopItem.setPriceContributionPoints(priceContributionPoints);
        shopItem.setStock(stock);
        shopItem.setIsAvailable(true);
        
        shopItemRepository.save(shopItem);
    }

    private ShopItemResponse convertToResponse(ShopItem shopItem) {
        String itemName, itemDescription, itemType;
        Integer itemQuality;
        
        if (shopItem.getItem() != null) {
            // 普通物品
            itemName = shopItem.getItem().getName();
            itemDescription = shopItem.getItem().getDescription();
            itemType = shopItem.getItem().getType();
            itemQuality = shopItem.getItem().getQuality();
        } else if (shopItem.getEquipment() != null) {
            // 装备
            itemName = shopItem.getEquipment().getName();
            itemDescription = shopItem.getEquipment().getDescription();
            itemType = "equipment";
            itemQuality = shopItem.getEquipment().getQuality();
        } else {
            itemName = "未知物品";
            itemDescription = "物品描述";
            itemType = "unknown";
            itemQuality = 1;
        }
        
        return ShopItemResponse.builder()
                .id(shopItem.getId().longValue())
                .itemId(shopItem.getItem() != null ? shopItem.getItem().getId().longValue() : null)
                .equipmentId(shopItem.getEquipment() != null ? shopItem.getEquipment().getId().longValue() : null)
                .itemName(itemName)
                .itemDescription(itemDescription)
                .itemType(itemType)
                .itemQuality(itemQuality)
                .shopType(shopItem.getShopType())
                .priceSpiritStones(shopItem.getPriceSpiritStones())
                .priceContributionPoints(shopItem.getPriceContributionPoints())
                .stock(shopItem.getStock())
                .isAvailable(shopItem.getIsAvailable())
                .createdAt(shopItem.getCreatedAt())
                .updatedAt(shopItem.getUpdatedAt())
                .build();
    }
}