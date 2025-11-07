package com.xiuxian.game.service;

import com.xiuxian.game.entity.Item;
import com.xiuxian.game.entity.PlayerItem;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.dto.response.PlayerItemResponse;
import com.xiuxian.game.repository.ItemRepository;
import com.xiuxian.game.repository.PlayerItemRepository;
import com.xiuxian.game.util.GameCalculator;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import com.xiuxian.game.util.Java8Compatibility;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ItemRepository itemRepository;
    private final PlayerItemRepository playerItemRepository;
    private final PlayerService playerService;
    private final GameCalculator gameCalculator;

    /**
     * 获取玩家背包物品列表（支持过滤、搜索和排序）
     */
    public List<PlayerItemResponse> getPlayerInventory(String type, String search, String sortBy, String order) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        List<PlayerItem> playerItems = playerItemRepository.findByPlayer(player);
        
        // 过滤
        if (type != null && !type.isEmpty()) {
            playerItems = playerItems.stream()
                .filter(pi -> pi.getItem().getType().equals(type))
                .collect(Collectors.toList());
        }
        
        // 搜索
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            playerItems = playerItems.stream()
                .filter(pi -> pi.getItem().getName().toLowerCase().contains(searchLower) ||
                            pi.getItem().getDescription().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
        }
        
        // 排序
        if (sortBy != null && order != null) {
            Comparator<PlayerItem> comparator = getSortComparator(sortBy);
            if ("desc".equalsIgnoreCase(order)) {
                comparator = comparator.reversed();
            }
            playerItems = playerItems.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
        }
        
        return playerItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取背包统计信息
     */
    public Map<String, Object> getInventoryStats() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        List<PlayerItem> playerItems = playerItemRepository.findByPlayer(player);
        
        int maxSlots = gameCalculator.calculateMaxInventorySlots(player.getLevel());
        int usedSlots = playerItems.size();
        int totalItems = playerItems.stream()
            .mapToInt(PlayerItem::getQuantity)
            .sum();
        
        // 按类型统计
        Map<String, Integer> itemsByType = playerItems.stream()
            .collect(Collectors.groupingBy(
                pi -> pi.getItem().getType(),
                Collectors.summingInt(PlayerItem::getQuantity)
            ));
        
        return Java8Compatibility.mapOf(
            "maxSlots", maxSlots,
            "usedSlots", usedSlots,
            "availableSlots", maxSlots - usedSlots,
            "totalItems", totalItems,
            "itemsByType", itemsByType
        );
    }

    /**
     * 获取物品详细信息
     */
    public Map<String, Object> getItemDetails(Integer playerItemId) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        PlayerItem playerItem = playerItemRepository.findById(playerItemId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_ITEM_NOT_FOUND + ": 物品不存在"));

        if (!playerItem.getPlayer().getId().equals(player.getId())) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该物品");
        }

        Map<String, Object> details = new HashMap<>();
        details.put("playerItem", convertToResponse(playerItem));
        details.put("itemValue", calculateItemValue(playerItem));
        details.put("canStackMore", playerItem.getItem().getStackable() && 
            playerItem.getQuantity() < playerItem.getItem().getMaxStack());
        details.put("effectDetails", parseItemEffect(playerItem.getItem().getEffect()));
        
        return details;
    }

    /**
     * 获取所有物品类型
     */
    public List<String> getAllItemTypes() {
        return itemRepository.findAll().stream()
            .map(Item::getType)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * 移动物品到指定位置（背包内重新排序）
     */
    @Transactional
    public void moveItem(Integer playerItemId, Integer newPosition) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        List<PlayerItem> playerItems = playerItemRepository.findByPlayer(player);
        
        // 这里可以实现更复杂的背包位置管理
        // 目前简化为重新排序
        // 可以扩展为支持拖拽排序等功能
        // newPosition 参数可以用于指定新位置
        
        // 重新排序物品（按物品类型、质量、数量等）
        playerItems.sort(Comparator.comparing((PlayerItem pi) -> pi.getItem().getType())
            .thenComparing(pi -> pi.getItem().getQuality(), Comparator.reverseOrder())
            .thenComparing(pi -> pi.getItem().getName()));
        
        // 保存新的顺序
        playerItems.forEach(playerItemRepository::save);
    }

    /**
     * 批量使用物品
     */
    @Transactional
    public Map<String, Object> batchUseItems(List<Integer> playerItemIds, Integer quantity) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        Map<String, Object> result = new HashMap<>();
        List<String> usedItems = new ArrayList<>();
        List<String> failedItems = new ArrayList<>();
        
        for (Integer playerItemId : playerItemIds) {
            try {
                PlayerItem playerItem = playerItemRepository.findById(playerItemId)
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
                
                if (!playerItem.getPlayer().getId().equals(player.getId())) {
                    failedItems.add(playerItem.getItem().getName() + ": 无权操作");
                    continue;
                }
                
                if (!playerItem.getItem().getUsable()) {
                    failedItems.add(playerItem.getItem().getName() + ": 物品不可使用");
                    continue;
                }
                
                // 使用指定数量的物品
                int useQuantity = Math.min(quantity, playerItem.getQuantity());
                for (int i = 0; i < useQuantity; i++) {
                    applyItemEffect(player, playerItem.getItem());
                }
                
                // 减少物品数量
                if (playerItem.getQuantity() == useQuantity) {
                    playerItemRepository.delete(playerItem);
                } else {
                    playerItem.setQuantity(playerItem.getQuantity() - useQuantity);
                    playerItemRepository.save(playerItem);
                }
                
                usedItems.add(playerItem.getItem().getName() + " x" + useQuantity);
                
            } catch (Exception e) {
                failedItems.add("ID " + playerItemId + ": " + e.getMessage());
            }
        }
        
        result.put("usedItems", usedItems);
        result.put("failedItems", failedItems);
        result.put("successCount", usedItems.size());
        result.put("failedCount", failedItems.size());
        
        return result;
    }

    /**
     * 扩展背包容量
     */
    @Transactional
    public void expandInventory(int additionalSlots) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        
        // 检查玩家是否有足够的资源扩展背包
        long expansionCost = calculateExpansionCost(additionalSlots);
        if (player.getSpiritStones() < expansionCost) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + 
                ": 扩展背包需要 " + expansionCost + " 灵石，当前只有 " + player.getSpiritStones() + " 灵石");
        }
        
        // 扣除灵石
        player.setSpiritStones(player.getSpiritStones() - expansionCost);
        playerService.savePlayerProfile(player);
        
        // 这里可以扩展PlayerProfile表，添加inventoryExpansion字段
        // 或者通过其他方式记录扩展次数
    }

    /**
     * 自动整理背包（堆叠相同物品）
     */
    @Transactional
    public Map<String, Object> organizeInventory() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        List<PlayerItem> playerItems = playerItemRepository.findByPlayer(player);
        
        Map<String, Object> result = new HashMap<>();
        int stacksMerged = 0;
        int itemsRemoved = 0;
        
        // 按物品ID分组
        Map<Integer, List<PlayerItem>> itemsByType = playerItems.stream()
            .collect(Collectors.groupingBy(pi -> pi.getItem().getId()));
        
        for (List<PlayerItem> sameItems : itemsByType.values()) {
            if (sameItems.size() > 1 && sameItems.get(0).getItem().getStackable()) {
                // 合并可堆叠物品
                PlayerItem mainItem = sameItems.get(0);
                int totalQuantity = sameItems.stream()
                    .mapToInt(PlayerItem::getQuantity)
                    .sum();
                
                // 设置最大堆叠数量
                int maxStack = mainItem.getItem().getMaxStack();
                int fullStacks = totalQuantity / maxStack;
                int remainder = totalQuantity % maxStack;
                
                // 更新主物品数量
                mainItem.setQuantity(Math.min(remainder == 0 ? maxStack : remainder, maxStack));
                playerItemRepository.save(mainItem);
                
                // 删除其他物品
                for (int i = 1; i < sameItems.size(); i++) {
                    playerItemRepository.delete(sameItems.get(i));
                    itemsRemoved++;
                }
                
                stacksMerged++;
            }
        }
        
        result.put("stacksMerged", stacksMerged);
        result.put("itemsRemoved", itemsRemoved);
        result.put("message", "成功整理背包，合并了 " + stacksMerged + " 个堆叠，移除了 " + itemsRemoved + " 个重复物品");
        
        return result;
    }

    /**
     * 检查背包是否已满
     */
    public boolean isInventoryFull() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        int maxSlots = gameCalculator.calculateMaxInventorySlots(player.getLevel());
        int currentSlots = playerItemRepository.findByPlayer(player).size();
        return currentSlots >= maxSlots;
    }

    /**
     * 获取可用空间
     */
    public int getAvailableSpace() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        int maxSlots = gameCalculator.calculateMaxInventorySlots(player.getLevel());
        int currentSlots = playerItemRepository.findByPlayer(player).size();
        return Math.max(0, maxSlots - currentSlots);
    }

    /**
     * 添加物品到背包（改进版本，支持自动堆叠和容量检查）
     */
    @Transactional
    public PlayerItemResponse addItemToInventory(Integer itemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 数量必须大于0");
        }

        PlayerProfile player = playerService.getCurrentPlayerProfile();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_ITEM_NOT_FOUND + ": 物品不存在"));

        // 检查背包容量
        int maxSlots = gameCalculator.calculateMaxInventorySlots(player.getLevel());
        int currentSlots = playerItemRepository.findByPlayer(player).size();
        
        Optional<PlayerItem> existingItem = playerItemRepository.findByPlayerAndItem(player, item);
        
        if (existingItem.isPresent()) {
            PlayerItem playerItem = existingItem.get();
            if (item.getStackable()) {
                int newQuantity = playerItem.getQuantity() + quantity;
                if (newQuantity > item.getMaxStack()) {
                    throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 超过最大堆叠数量 " + item.getMaxStack());
                }
                playerItem.setQuantity(newQuantity);
                playerItem = playerItemRepository.save(playerItem);
                return convertToResponse(playerItem);
            } else {
                // 不可堆叠物品，检查是否有空位
                if (currentSlots >= maxSlots) {
                    throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 背包已满");
                }
                // 创建新的物品实例
                PlayerItem newPlayerItem = PlayerItem.builder()
                    .player(player)
                    .item(item)
                    .quantity(quantity)
                    .build();
                newPlayerItem = playerItemRepository.save(newPlayerItem);
                return convertToResponse(newPlayerItem);
            }
        } else {
            if (quantity > item.getMaxStack() && item.getStackable()) {
                throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 超过最大堆叠数量 " + item.getMaxStack());
            }
            
            // 检查是否有空位
            if (currentSlots >= maxSlots) {
                throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 背包已满，当前 " + currentSlots + "/" + maxSlots);
            }
            
            PlayerItem playerItem = PlayerItem.builder()
                    .player(player)
                    .item(item)
                    .quantity(quantity)
                    .build();
            
            playerItem = playerItemRepository.save(playerItem);
            return convertToResponse(playerItem);
        }
    }

    /**
     * 批量添加物品到背包
     */
    @Transactional
    public Map<String, Object> addItemsToInventory(Map<Integer, Integer> items) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        Map<String, Object> result = new HashMap<>();
        List<String> addedItems = new ArrayList<>();
        List<String> failedItems = new ArrayList<>();
        
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            try {
                PlayerItemResponse response = addItemToInventory(entry.getKey(), entry.getValue());
                addedItems.add(response.getItemName() + " x" + entry.getValue());
            } catch (Exception e) {
                failedItems.add("ID " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        result.put("addedItems", addedItems);
        result.put("failedItems", failedItems);
        result.put("successCount", addedItems.size());
        result.put("failedCount", failedItems.size());
        
        return result;
    }

    @Transactional
    public void removeItemFromInventory(Integer playerItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 数量必须大于0");
        }

        PlayerProfile player = playerService.getCurrentPlayerProfile();
        PlayerItem playerItem = playerItemRepository.findById(playerItemId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_ITEM_NOT_FOUND + ": 物品不存在"));

        if (!playerItem.getPlayer().getId().equals(player.getId())) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该物品");
        }

        if (playerItem.getQuantity() < quantity) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + ": 物品数量不足");
        }

        if (playerItem.getQuantity() == quantity) {
            playerItemRepository.delete(playerItem);
        } else {
            playerItem.setQuantity(playerItem.getQuantity() - quantity);
            playerItemRepository.save(playerItem);
        }
    }

    /**
     * 使用物品（改进版本，支持不同数量使用）
     */
    @Transactional
    public Map<String, Object> useItem(Integer playerItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 使用数量必须大于0");
        }

        PlayerProfile player = playerService.getCurrentPlayerProfile();
        PlayerItem playerItem = playerItemRepository.findById(playerItemId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_ITEM_NOT_FOUND + ": 物品不存在"));

        if (!playerItem.getPlayer().getId().equals(player.getId())) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该物品");
        }

        Item item = playerItem.getItem();
        if (!item.getUsable()) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 该物品不可使用");
        }

        if (playerItem.getQuantity() < quantity) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + ": 物品数量不足");
        }

        // 使用指定数量的物品
        for (int i = 0; i < quantity; i++) {
            applyItemEffect(player, item);
        }

        // 使用后减少数量
        if (playerItem.getQuantity() == quantity) {
            playerItemRepository.delete(playerItem);
        } else {
            playerItem.setQuantity(playerItem.getQuantity() - quantity);
            playerItemRepository.save(playerItem);
        }

        return Java8Compatibility.mapOf(
            "message", "成功使用 " + item.getName() + " x" + quantity,
            "effects", getItemEffectDescription(item, quantity)
        );
    }

    /**
     * 解析物品效果描述
     */
    private Map<String, Object> parseItemEffect(String effect) {
        Map<String, Object> effectDetails = new HashMap<>();
        if (effect == null || effect.isEmpty()) {
            return effectDetails;
        }
        
        // 简单的效果解析（可以扩展为更复杂的解析器）
        if (effect.contains("生命")) {
            effectDetails.put("type", "health");
            effectDetails.put("description", effect);
        } else if (effect.contains("灵力") || effect.contains("法力")) {
            effectDetails.put("type", "mana");
            effectDetails.put("description", effect);
        } else if (effect.contains("经验")) {
            effectDetails.put("type", "exp");
            effectDetails.put("description", effect);
        } else if (effect.contains("灵石")) {
            effectDetails.put("type", "spiritStones");
            effectDetails.put("description", effect);
        } else {
            effectDetails.put("type", "other");
            effectDetails.put("description", effect);
        }
        
        return effectDetails;
    }

    /**
     * 获取物品效果描述
     */
    private Map<String, Object> getItemEffectDescription(Item item, Integer quantity) {
        Map<String, Object> description = new HashMap<>();
        String effect = item.getEffect();
        
        if (effect != null && !effect.isEmpty()) {
            description.put("singleUse", effect);
            description.put("batchUse", effect + " (x" + quantity + ")");
        }
        
        return description;
    }

    /**
     * 计算物品价值
     */
    private Map<String, Object> calculateItemValue(PlayerItem playerItem) {
        Item item = playerItem.getItem();
        long totalValue = item.getPrice() * playerItem.getQuantity();
        
        return Java8Compatibility.mapOf(
            "unitPrice", item.getPrice(),
            "totalValue", totalValue,
            "qualityMultiplier", getQualityMultiplier(item.getQuality()),
            "sellable", item.getSellable()
        );
    }

    /**
     * 获取品质倍率
     */
    private double getQualityMultiplier(int quality) {
        switch (quality) {
            case 1: return 1.0;   // 普通
            case 2: return 1.5;   // 精良
            case 3: return 2.0;   // 稀有
            case 4: return 3.0;   // 史诗
            case 5: return 5.0;   // 传说
            default: return 1.0;
        }
    }

    /**
     * 计算扩展成本
     */
    private long calculateExpansionCost(int additionalSlots) {
        // 基础扩展成本：每个格子100灵石，成本递增
        return Math.round((double) additionalSlots * 100L * (1 + (additionalSlots - 1) * 0.5));
    }

    /**
     * 获取排序比较器
     */
    private Comparator<PlayerItem> getSortComparator(String sortBy) {
        String sortKey = sortBy.toLowerCase();
        switch (sortKey) {
            case "name": 
                return Comparator.comparing(pi -> pi.getItem().getName());
            case "type": 
                return Comparator.comparing(pi -> pi.getItem().getType());
            case "quality": 
                return Comparator.comparing(pi -> pi.getItem().getQuality(), Comparator.reverseOrder());
            case "quantity": 
                return Comparator.comparing(PlayerItem::getQuantity, Comparator.reverseOrder());
            case "created": 
                return Comparator.comparing(PlayerItem::getCreatedAt);
            case "updated": 
                return Comparator.comparing(PlayerItem::getUpdatedAt);
            default: 
                return Comparator.comparing(pi -> pi.getItem().getName()); // 默认按名称排序
        }
    }

    /**
     * 应用物品效果（改进版本）
     */
    private void applyItemEffect(PlayerProfile player, Item item) {
        // 根据物品类型应用不同效果（与当前PlayerProfile字段对齐）
        switch (item.getType()) {
            case "恢复药剂":
                // 恢复生命值和贡献点
                int healthRestore = 100;
                player.setHealth(Math.min(player.getHealth() + healthRestore, player.getTotalHealth()));
                player.setContributionPoints(player.getContributionPoints() + 50);
                break;
                
            case "灵力药剂":
                // 恢复灵力和修炼点数
                int manaRestore = 50;
                player.setMana(Math.min(player.getMana() + manaRestore, player.getTotalMana()));
                player.setCultivationPoints(player.getCultivationPoints() + 50);
                break;
                
            case "经验丹":
                long expGain = 500;
                player.setExp(player.getExp() + expGain);
                gameCalculator.checkLevelUp(player);
                break;
                
            case "灵石袋":
                long spiritStonesGain = 1000;
                player.setSpiritStones(player.getSpiritStones() + spiritStonesGain);
                break;
                
            case "攻击药剂":
                // 临时增加攻击力（可以扩展为临时buff系统）
                player.setEquipmentAttackBonus(player.getEquipmentAttackBonus() + 10);
                break;
                
            case "防御药剂":
                // 临时增加防御力
                player.setEquipmentDefenseBonus(player.getEquipmentDefenseBonus() + 10);
                break;
                
            case "速度药剂":
                // 临时增加速度
                player.setEquipmentSpeedBonus(player.getEquipmentSpeedBonus() + 5);
                break;
                
            case "全能药剂":
                // 增加所有属性
                player.setHealth(player.getHealth() + 20);
                player.setMana(player.getMana() + 20);
                player.setAttack(player.getAttack() + 5);
                player.setDefense(player.getDefense() + 5);
                player.setSpeed(player.getSpeed() + 3);
                break;
                
            default:
                // 其他类型物品暂不实现特殊效果
                player.setContributionPoints(player.getContributionPoints() + 10);
                break;
        }
        playerService.savePlayerProfile(player);
    }

    /**
     * 转换PlayerItem为响应DTO（增强版本）
     */
    private PlayerItemResponse convertToResponse(PlayerItem playerItem) {
        Item item = playerItem.getItem();
        return PlayerItemResponse.builder()
                .id(playerItem.getId().longValue())
                .itemId(item.getId().longValue())
                .itemName(item.getName())
                .itemDescription(item.getDescription())
                .itemType(item.getType())
                .itemQuality(item.getQuality())
                .quantity(playerItem.getQuantity())
                .maxStack(item.getMaxStack())
                .stackable(item.getStackable())
                .usable(item.getUsable())
                .effect(item.getEffect())
                .price(item.getPrice())
                .sellable(item.getSellable())
                .canUseMore(playerItem.getQuantity() > 0 && item.getUsable())
                .stackFull(playerItem.getQuantity() >= item.getMaxStack())
                .createdAt(playerItem.getCreatedAt())
                .updatedAt(playerItem.getUpdatedAt())
                .build();
    }

    /**
     * 初始化默认物品（扩展版本）
     */
    @Transactional
    public void initializeDefaultItems() {
        if (itemRepository.count() == 0) {
            // ===== 消耗品类 =====
            
            // 生命药剂系列
            Item smallHealthPotion = Item.builder()
                    .name("小型生命药剂")
                    .description("恢复100点生命值")
                    .type("恢复药剂")
                    .quality(1)
                    .stackable(true)
                    .maxStack(99)
                    .price(50)
                    .sellable(true)
                    .usable(true)
                    .effect("恢复100点生命值")
                    .build();
            
            Item largeHealthPotion = Item.builder()
                    .name("大型生命药剂")
                    .description("恢复300点生命值")
                    .type("恢复药剂")
                    .quality(2)
                    .stackable(true)
                    .maxStack(99)
                    .price(150)
                    .sellable(true)
                    .usable(true)
                    .effect("恢复300点生命值")
                    .build();
            
            // 灵力药剂系列
            Item smallManaPotion = Item.builder()
                    .name("小型灵力药剂")
                    .description("恢复50点灵力")
                    .type("灵力药剂")
                    .quality(1)
                    .stackable(true)
                    .maxStack(99)
                    .price(50)
                    .sellable(true)
                    .usable(true)
                    .effect("恢复50点灵力")
                    .build();
            
            Item largeManaPotion = Item.builder()
                    .name("大型灵力药剂")
                    .description("恢复150点灵力")
                    .type("灵力药剂")
                    .quality(2)
                    .stackable(true)
                    .maxStack(99)
                    .price(150)
                    .sellable(true)
                    .usable(true)
                    .effect("恢复150点灵力")
                    .build();
            
            // 经验丹系列
            Item smallExpPill = Item.builder()
                    .name("初级经验丹")
                    .description("增加500点经验值")
                    .type("经验丹")
                    .quality(2)
                    .stackable(true)
                    .maxStack(99)
                    .price(200)
                    .sellable(true)
                    .usable(true)
                    .effect("增加500点经验值")
                    .build();
            
            Item mediumExpPill = Item.builder()
                    .name("中级经验丹")
                    .description("增加1500点经验值")
                    .type("经验丹")
                    .quality(3)
                    .stackable(true)
                    .maxStack(99)
                    .price(600)
                    .sellable(true)
                    .usable(true)
                    .effect("增加1500点经验值")
                    .build();
            
            Item largeExpPill = Item.builder()
                    .name("高级经验丹")
                    .description("增加5000点经验值")
                    .type("经验丹")
                    .quality(4)
                    .stackable(true)
                    .maxStack(99)
                    .price(2000)
                    .sellable(true)
                    .usable(true)
                    .effect("增加5000点经验值")
                    .build();
            
            // 灵石袋系列
            Item smallSpiritStoneBag = Item.builder()
                    .name("小型灵石袋")
                    .description("获得1000灵石")
                    .type("灵石袋")
                    .quality(2)
                    .stackable(true)
                    .maxStack(99)
                    .price(900)
                    .sellable(true)
                    .usable(true)
                    .effect("获得1000灵石")
                    .build();
            
            Item mediumSpiritStoneBag = Item.builder()
                    .name("中型灵石袋")
                    .description("获得5000灵石")
                    .type("灵石袋")
                    .quality(3)
                    .stackable(true)
                    .maxStack(99)
                    .price(4500)
                    .sellable(true)
                    .usable(true)
                    .effect("获得5000灵石")
                    .build();
            
            // 属性药剂
            Item attackPotion = Item.builder()
                    .name("攻击药剂")
                    .description("临时增加10点攻击力")
                    .type("攻击药剂")
                    .quality(2)
                    .stackable(true)
                    .maxStack(50)
                    .price(300)
                    .sellable(true)
                    .usable(true)
                    .effect("临时增加10点攻击力")
                    .build();
            
            Item defensePotion = Item.builder()
                    .name("防御药剂")
                    .description("临时增加10点防御力")
                    .type("防御药剂")
                    .quality(2)
                    .stackable(true)
                    .maxStack(50)
                    .price(300)
                    .sellable(true)
                    .usable(true)
                    .effect("临时增加10点防御力")
                    .build();
            
            Item speedPotion = Item.builder()
                    .name("速度药剂")
                    .description("临时增加5点速度")
                    .type("速度药剂")
                    .quality(2)
                    .stackable(true)
                    .maxStack(50)
                    .price(250)
                    .sellable(true)
                    .usable(true)
                    .effect("临时增加5点速度")
                    .build();
            
            Item allStatsPotion = Item.builder()
                    .name("全能药剂")
                    .description("全面提升所有属性")
                    .type("全能药剂")
                    .quality(3)
                    .stackable(true)
                    .maxStack(20)
                    .price(1000)
                    .sellable(true)
                    .usable(true)
                    .effect("增加所有属性")
                    .build();
            
            // ===== 材料类 =====
            
            // 锻造材料
            Item ironOre = Item.builder()
                    .name("铁矿石")
                    .description("基础锻造材料")
                    .type("材料")
                    .quality(1)
                    .stackable(true)
                    .maxStack(999)
                    .price(10)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            Item copperOre = Item.builder()
                    .name("铜矿石")
                    .description("优质锻造材料")
                    .type("材料")
                    .quality(2)
                    .stackable(true)
                    .maxStack(999)
                    .price(25)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            Item silverOre = Item.builder()
                    .name("银矿石")
                    .description("高级锻造材料")
                    .type("材料")
                    .quality(3)
                    .stackable(true)
                    .maxStack(999)
                    .price(50)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            Item goldOre = Item.builder()
                    .name("金矿石")
                    .description("顶级锻造材料")
                    .type("材料")
                    .quality(4)
                    .stackable(true)
                    .maxStack(999)
                    .price(100)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            // 炼丹材料
            Item herbalLeaf = Item.builder()
                    .name("灵草叶")
                    .description("基础炼丹材料")
                    .type("材料")
                    .quality(1)
                    .stackable(true)
                    .maxStack(999)
                    .price(10)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            Item spiritGrass = Item.builder()
                    .name("灵芝草")
                    .description("优质炼丹材料")
                    .type("材料")
                    .quality(2)
                    .stackable(true)
                    .maxStack(999)
                    .price(30)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            Item celestialHerb = Item.builder()
                    .name("天材地宝")
                    .description("珍贵炼丹材料")
                    .type("材料")
                    .quality(4)
                    .stackable(true)
                    .maxStack(99)
                    .price(200)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            // 特殊材料
            Item demonCore = Item.builder()
                    .name("魔核")
                    .description("魔兽体内的能量核心")
                    .type("材料")
                    .quality(3)
                    .stackable(true)
                    .maxStack(99)
                    .price(150)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            Item phoenixFeather = Item.builder()
                    .name("凤凰羽毛")
                    .description("传说中的神鸟羽毛")
                    .type("材料")
                    .quality(5)
                    .stackable(true)
                    .maxStack(10)
                    .price(1000)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            // ===== 任务物品 =====
            
            Item ancientCoin = Item.builder()
                    .name("古钱币")
                    .description("古代流通的货币，可能有价值")
                    .type("任务物品")
                    .quality(2)
                    .stackable(true)
                    .maxStack(100)
                    .price(500)
                    .sellable(true)
                    .usable(false)
                    .effect("")
                    .build();
            
            Item mysteriousScroll = Item.builder()
                    .name("神秘卷轴")
                    .description("记载着古老秘密的卷轴")
                    .type("任务物品")
                    .quality(3)
                    .stackable(false)
                    .maxStack(1)
                    .price(0)
                    .sellable(false)
                    .usable(false)
                    .effect("")
                    .build();
            
            // 保存所有物品
            itemRepository.saveAll(Arrays.asList(
                // 消耗品
                smallHealthPotion, largeHealthPotion,
                smallManaPotion, largeManaPotion,
                smallExpPill, mediumExpPill, largeExpPill,
                smallSpiritStoneBag, mediumSpiritStoneBag,
                attackPotion, defensePotion, speedPotion, allStatsPotion,
                
                // 材料
                ironOre, copperOre, silverOre, goldOre,
                herbalLeaf, spiritGrass, celestialHerb,
                demonCore, phoenixFeather,
                
                // 任务物品
                ancientCoin, mysteriousScroll
            ));
        }
    }

    /**
     * 获取背包容量信息
     */
    public Map<String, Object> getInventoryCapacity() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        int maxSlots = gameCalculator.calculateMaxInventorySlots(player.getLevel());
        int usedSlots = playerItemRepository.findByPlayer(player).size();
        
        return Java8Compatibility.mapOf(
            "maxSlots", maxSlots,
            "usedSlots", usedSlots,
            "availableSlots", maxSlots - usedSlots,
            "usagePercentage", Math.round((double) usedSlots / maxSlots * 100)
        );
    }

    /**
     * 获取物品使用历史（简化版本）
     */
    public List<Map<String, Object>> getItemUsageHistory() {
        // 这里可以实现更复杂的物品使用历史记录
        // 目前返回空列表作为占位符
        return new ArrayList<>();
    }

    /**
     * 检查玩家是否拥有指定物品
     */
    public boolean hasItem(Integer itemId, Integer quantity) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        Optional<PlayerItem> playerItem = playerItemRepository.findByPlayerAndItem(player, 
            itemRepository.findById(itemId).orElse(null));
        
        return playerItem.map(pi -> pi.getQuantity() >= quantity).orElse(false);
    }

    /**
     * 获取指定类型的所有物品
     */
    public List<PlayerItemResponse> getItemsByType(String itemType) {
        return getPlayerInventory(itemType, null, null, null);
    }

    /**
     * 获取可使用的物品
     */
    public List<PlayerItemResponse> getUsableItems() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return playerItemRepository.findByPlayer(player).stream()
            .filter(pi -> pi.getItem().getUsable())
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * 获取可堆叠的物品
     */
    public List<PlayerItemResponse> getStackableItems() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return playerItemRepository.findByPlayer(player).stream()
            .filter(pi -> pi.getItem().getStackable() && pi.getQuantity() < pi.getItem().getMaxStack())
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * 清空背包（危险操作，需要确认）
     */
    @Transactional
    public void clearInventory() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        List<PlayerItem> playerItems = playerItemRepository.findByPlayer(player);
        playerItemRepository.deleteAll(playerItems);
    }

    /**
     * 获取物品统计信息
     */
    public Map<String, Object> getItemStatistics() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        List<PlayerItem> playerItems = playerItemRepository.findByPlayer(player);
        
        long totalValue = playerItems.stream()
            .mapToLong(pi -> pi.getItem().getPrice() * pi.getQuantity())
            .sum();
        
        Map<String, Long> itemsByType = playerItems.stream()
            .collect(Collectors.groupingBy(
                pi -> pi.getItem().getType(),
                Collectors.summingLong(pi -> (long) pi.getQuantity())
            ));
        
        Map<String, Long> itemsByQuality = playerItems.stream()
            .collect(Collectors.groupingBy(
                pi -> "品质" + pi.getItem().getQuality(),
                Collectors.summingLong(pi -> (long) pi.getQuantity())
            ));
        
        return Java8Compatibility.mapOf(
            "totalItems", playerItems.stream().mapToInt(PlayerItem::getQuantity).sum(),
            "totalUniqueItems", playerItems.size(),
            "totalValue", totalValue,
            "itemsByType", itemsByType,
            "itemsByQuality", itemsByQuality
        );
    }
}