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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

/**
 * 背包服务
 *
 * <p>提供背包管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>背包物品查询（支持筛选、搜索、排序）</li>
 *   <li>物品添加与移除</li>
 *   <li>物品使用与出售</li>
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
@ConditionalOnProperty(value = "app.features.inventory.enabled", havingValue = "true")
@RequiredArgsConstructor
public class InventoryService {

    private final ItemService itemService;
    private final PlayerService playerService;

    /**
     * 获取玩家背包物品列表
     *
     * <p>支持按类型筛选、按名称/描述搜索、按指定字段排序</p>
     *
     * @param playerId 玩家ID
     * @param type 物品类型筛选（可选）
     * @param search 搜索关键词（可选）
     * @param sortBy 排序字段（可选）
     * @param order 排序方向（可选）
     * @return 背包物品列表
     */
    public List<PlayerItemResponse> getPlayerInventory(Integer playerId, String type, String search, String sortBy, String order) {
        log.debug("查询玩家背包物品, playerId={}, type={}, search={}, sortBy={}, order={}", playerId, type, search, sortBy, order);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        List<PlayerItem> playerItems = playerService.getPlayerItemsByPlayerId(playerId);

        // 过滤与搜索逻辑优化：批量获取所有涉及的物品模板，避免N+1查询
        List<Integer> itemIds = playerItems.stream()
                .map(PlayerItem::getItemId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Integer, Item> itemMap = new HashMap<>();
        if (!itemIds.isEmpty()) {
            List<Item> items = itemService.getItemsByIds(itemIds);
            itemMap = items.stream()
                    .collect(Collectors.toMap(Item::getId, item -> item, (a, b) -> a));
        }
        final Map<Integer, Item> finalItemMap = itemMap;
        
        // 按类型过滤
        if (type != null && !type.isEmpty()) {
            playerItems = playerItems.stream()
                    .filter(pi -> {
                        Item item = finalItemMap.get(pi.getItemId());
                        return item != null && item.getType().equals(type);
                    })
                    .collect(Collectors.toList());
        }

        // 搜索逻辑
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            playerItems = playerItems.stream()
                    .filter(pi -> {
                        Item item = finalItemMap.get(pi.getItemId());
                        return item != null && (item.getName().toLowerCase().contains(searchLower) ||
                                item.getDescription().toLowerCase().contains(searchLower));
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
        }

        List<PlayerItemResponse> result = playerItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        log.debug("查询玩家背包物品成功, playerId={}, count={}", playerId, result.size());
        return result;
    }

    /**
     * 获取玩家背包物品列表（简化版本）
     *
     * @param playerId 玩家ID
     * @return 背包物品列表
     */
    public List<PlayerItemResponse> getPlayerInventory(Integer playerId) {
        log.debug("查询玩家背包物品（简化版本）, playerId={}", playerId);
        return getPlayerInventory(playerId, null, null, null, null);
    }

    /**
     * 添加物品到背包
     *
     * <p>如果物品可堆叠且已存在，则增加数量；否则创建新物品</p>
     *
     * @param playerId 玩家ID
     * @param itemId 物品ID
     * @param quantity 数量
     * @return 添加的物品
     */
    @Transactional
    public PlayerItemResponse addItemToInventory(Integer playerId, Integer itemId, Integer quantity) {
        log.info("添加物品到背包, playerId={}, itemId={}, quantity={}", playerId, itemId, quantity);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        Item item = itemService.getItemById(itemId);
        if (item == null) {
            log.warn("物品不存在, itemId={}", itemId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品不存在");
        }

        // 检查是否已存在该物品
        PlayerItem existingItem = playerService.getPlayerItemByPlayerAndItem(playerId, itemId);

        if (existingItem != null) {
            // 如果可堆叠，增加数量
            if (item.getStackable()) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                existingItem.setUpdatedAt(LocalDateTime.now());
                playerService.updatePlayerItem(existingItem);
                log.info("物品堆叠成功, playerId={}, itemId={}, quantity={}", playerId, itemId, existingItem.getQuantity());
                return convertToResponse(existingItem);
            }
        }

        // 创建新物品
        PlayerItem newPlayerItem = PlayerItem.builder()
                .playerId(playerId)
                .itemId(itemId)
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        playerService.savePlayerItem(newPlayerItem);
        PlayerItem savedItem = playerService.getPlayerItemById(newPlayerItem.getId());
        log.info("添加物品到背包成功, playerId={}, itemId={}, quantity={}", playerId, itemId, quantity);
        return convertToResponse(savedItem);
    }

    /**
     * 从背包移除物品
     *
     * @param playerId 玩家ID
     * @param itemId 物品ID
     * @param quantity 数量
     * @return 移除后的物品（如果数量为0则返回null）
     */
    @Transactional
    public PlayerItemResponse removeItemFromInventory(Integer playerId, Integer itemId, Integer quantity) {
        log.info("从背包移除物品, playerId={}, itemId={}, quantity={}", playerId, itemId, quantity);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        PlayerItem playerItem = playerService.getPlayerItemByPlayerAndItem(playerId, itemId);
        if (playerItem == null) {
            log.warn("玩家没有该物品, playerId={}, itemId={}", playerId, itemId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家没有该物品");
        }

        if (playerItem.getQuantity() < quantity) {
            log.warn("物品数量不足, playerId={}, itemId={}, have={}, need={}", playerId, itemId, playerItem.getQuantity(), quantity);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品数量不足");
        }

        playerItem.setQuantity(playerItem.getQuantity() - quantity);
        playerItem.setUpdatedAt(LocalDateTime.now());

        if (playerItem.getQuantity() <= 0) {
            playerService.deletePlayerItem(playerItem.getId());
            log.info("物品已全部移除, playerId={}, itemId={}", playerId, itemId);
            return null;
        } else {
            playerService.updatePlayerItem(playerItem);
            log.info("物品移除成功, playerId={}, itemId={}, remaining={}", playerId, itemId, playerItem.getQuantity());
            return convertToResponse(playerItem);
        }
    }

    /**
     * 使用物品
     *
     * @param playerId 玩家ID
     * @param itemId 物品ID
     * @return 使用后的物品
     */
    @Transactional
    public PlayerItemResponse useItem(Integer playerId, Integer itemId) {
        log.info("使用物品, playerId={}, itemId={}", playerId, itemId);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        Item item = itemService.getItemById(itemId);
        if (item == null) {
            log.warn("物品不存在, itemId={}", itemId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品不存在");
        }

        if (!item.getUsable()) {
            log.warn("物品不可使用, itemId={}", itemId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该物品不可使用");
        }

        PlayerItem playerItem = playerService.getPlayerItemByPlayerAndItem(playerId, itemId);
        if (playerItem == null) {
            log.warn("玩家没有该物品, playerId={}, itemId={}", playerId, itemId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家没有该物品");
        }

        // 应用物品效果（简化处理）
        applyItemEffect(player, item);

        // 减少物品数量
        PlayerItemResponse result = removeItemFromInventory(playerId, itemId, 1);
        log.info("使用物品成功, playerId={}, itemId={}", playerId, itemId);
        return result;
    }

    /**
     * 使用物品（重载方法）
     *
     * <p>Controller调用的重载方法，支持指定数量</p>
     *
     * @param playerItemId 玩家物品ID
     * @param quantity 数量
     * @param playerId 玩家ID
     * @return 使用结果
     */
    @Transactional
    public Map<String, Object> useItem(Integer playerItemId, Integer quantity, Integer playerId) {
        log.info("使用物品（重载）, playerId={}, playerItemId={}, quantity={}", playerId, playerItemId, quantity);
        // 根据playerItemId获取itemId
        PlayerItem playerItem = playerService.getPlayerItemById(playerItemId);
        if (playerItem == null) {
            log.warn("物品不存在, playerItemId={}", playerItemId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品不存在");
        }

        if (playerItem.getQuantity() < quantity) {
            log.warn("物品数量不足, playerItemId={}, have={}, need={}", playerItemId, playerItem.getQuantity(), quantity);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品数量不足");
        }

        Item item = itemService.getItemById(playerItem.getItemId());
        if (!item.getUsable()) {
            log.warn("物品不可使用, itemId={}", playerItem.getItemId());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该物品不可使用");
        }

        PlayerProfile player = playerService.getPlayerProfileById(playerId);

        // 使用指定数量的物品
        for (int i = 0; i < quantity; i++) {
            applyItemEffect(player, item);
        }

        // 减少物品数量
        playerItem.setQuantity(playerItem.getQuantity() - quantity);
        if (playerItem.getQuantity() <= 0) {
            playerService.deletePlayerItem(playerItem.getId());
        } else {
            playerItem.setUpdatedAt(LocalDateTime.now());
            playerService.updatePlayerItem(playerItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("used", quantity);
        result.put("remaining", Math.max(0, playerItem.getQuantity()));
        log.info("使用物品成功（重载）, playerId={}, playerItemId={}, quantity={}", playerId, playerItemId, quantity);
        return result;
    }

    /**
     * 出售物品
     *
     * @param playerId 玩家ID
     * @param playerItemId 玩家物品ID
     * @param quantity 数量
     */
    @Transactional
    public void sellItem(Integer playerId, Integer playerItemId, Integer quantity) {
        log.info("出售物品, playerId={}, playerItemId={}, quantity={}", playerId, playerItemId, quantity);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        PlayerItem playerItem = playerService.getPlayerItemById(playerItemId);
        if (playerItem == null) {
            log.warn("物品不存在, playerItemId={}", playerItemId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品不存在");
        }

        if (!playerItem.getPlayerId().equals(playerId)) {
            log.warn("物品不属于当前玩家, playerId={}, playerItemId={}", playerId, playerItemId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该物品不属于当前玩家");
        }

        Item item = itemService.getItemById(playerItem.getItemId());
        if (item == null || !item.getSellable()) {
            log.warn("物品不可出售, itemId={}", playerItem.getItemId());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该物品不可出售");
        }

        if (playerItem.getQuantity() < quantity) {
            log.warn("物品数量不足, playerItemId={}, have={}, need={}", playerItemId, playerItem.getQuantity(), quantity);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品数量不足");
        }

        // 计算出售价格（通常是购买价格的50%）
        long sellPrice = ((long) item.getPrice() * quantity) / 2;

        // 增加玩家灵石
        player.setSpiritStones(player.getSpiritStones() + sellPrice);
        playerService.savePlayerProfile(player);

        // 减少物品数量
        playerItem.setQuantity(playerItem.getQuantity() - quantity);
        if (playerItem.getQuantity() <= 0) {
            playerService.deletePlayerItem(playerItem.getId());
        } else {
            playerItem.setUpdatedAt(LocalDateTime.now());
            playerService.updatePlayerItem(playerItem);
        }
        log.info("出售物品成功, playerId={}, playerItemId={}, quantity={}, sellPrice={}", playerId, playerItemId, quantity, sellPrice);
    }

    private void applyItemEffect(PlayerProfile player, Item item) {
        // 简化的物品效果应用
        // 实际应该根据item.effect来处理
        playerService.savePlayerProfile(player);
    }

    private Comparator<PlayerItem> getSortComparator(String sortBy) {
        switch (sortBy) {
            case "quantity":
                return Comparator.comparing(PlayerItem::getQuantity);
            case "created":
                return Comparator.comparing(PlayerItem::getCreatedAt);
            default:
                return Comparator.comparing(PlayerItem::getId);
        }
    }

    private PlayerItemResponse convertToResponse(PlayerItem playerItem) {
        Item item = itemService.getItemById(playerItem.getItemId());

        PlayerItemResponse response = new PlayerItemResponse();
        response.setId(playerItem.getId());
        response.setItemId(playerItem.getItemId());
        response.setQuantity(playerItem.getQuantity());
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
}
