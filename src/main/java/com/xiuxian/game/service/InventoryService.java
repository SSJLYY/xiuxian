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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value = "app.features.inventory.enabled", havingValue = "true")
@RequiredArgsConstructor
public class InventoryService {

    private final ItemMapper itemMapper;
    private final PlayerItemMapper playerItemMapper;
    private final PlayerProfileMapper playerProfileMapper;

    public List<PlayerItemResponse> getPlayerInventory(Integer playerId, String type, String search, String sortBy, String order) {
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

        // 搜索逻辑
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            playerItems = playerItems.stream()
                    .filter(pi -> {
                        Item item = itemMapper.selectById(pi.getItemId());
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

        return playerItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PlayerItemResponse> getPlayerInventory(Integer playerId) {
        return getPlayerInventory(playerId, null, null, null, null);
    }

    @Transactional
    public PlayerItemResponse addItemToInventory(Integer playerId, Integer itemId, Integer quantity) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }

        // 检查是否已存在该物品
        PlayerItem existingItem = playerItemMapper.selectByPlayerIdAndItemId(playerId, itemId);

        if (existingItem != null) {
            // 如果可堆叠，增加数量
            if (item.getStackable()) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                existingItem.setUpdatedAt(LocalDateTime.now());
                playerItemMapper.updateById(existingItem);
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

        playerItemMapper.insert(newPlayerItem);
        PlayerItem savedItem = playerItemMapper.selectById(newPlayerItem.getId());
        return convertToResponse(savedItem);
    }

    @Transactional
    public PlayerItemResponse removeItemFromInventory(Integer playerId, Integer itemId, Integer quantity) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        PlayerItem playerItem = playerItemMapper.selectByPlayerIdAndItemId(playerId, itemId);
        if (playerItem == null) {
            throw new IllegalArgumentException("玩家没有该物品");
        }

        if (playerItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("物品数量不足");
        }

        playerItem.setQuantity(playerItem.getQuantity() - quantity);
        playerItem.setUpdatedAt(LocalDateTime.now());

        if (playerItem.getQuantity() <= 0) {
            playerItemMapper.deleteById(playerItem.getId());
            return null;
        } else {
            playerItemMapper.updateById(playerItem);
            return convertToResponse(playerItem);
        }
    }

    @Transactional
    public PlayerItemResponse useItem(Integer playerId, Integer itemId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }

        if (!item.getUsable()) {
            throw new IllegalArgumentException("该物品不可使用");
        }

        PlayerItem playerItem = playerItemMapper.selectByPlayerIdAndItemId(playerId, itemId);
        if (playerItem == null) {
            throw new IllegalArgumentException("玩家没有该物品");
        }

        // 应用物品效果(简化处理)
        applyItemEffect(player, item);

        // 减少物品数量
        return removeItemFromInventory(playerId, itemId, 1);
    }

    // Controller调用的重载方法
    @Transactional
    public Map<String, Object> useItem(Integer playerItemId, Integer quantity, Integer playerId) {
        // 根据playerItemId获取itemId
        PlayerItem playerItem = playerItemMapper.selectById(playerItemId);
        if (playerItem == null) {
            throw new IllegalArgumentException("物品不存在");
        }

        if (playerItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("物品数量不足");
        }

        Item item = itemMapper.selectById(playerItem.getItemId());
        if (!item.getUsable()) {
            throw new IllegalArgumentException("该物品不可使用");
        }

        PlayerProfile player = playerProfileMapper.selectById(playerId);
        
        // 使用指定数量的物品
        for (int i = 0; i < quantity; i++) {
            applyItemEffect(player, item);
        }

        // 减少物品数量
        playerItem.setQuantity(playerItem.getQuantity() - quantity);
        if (playerItem.getQuantity() <= 0) {
            playerItemMapper.deleteById(playerItem.getId());
        } else {
            playerItem.setUpdatedAt(LocalDateTime.now());
            playerItemMapper.updateById(playerItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("used", quantity);
        result.put("remaining", Math.max(0, playerItem.getQuantity()));
        return result;
    }

    // 出售物品
    @Transactional
    public void sellItem(Integer playerId, Integer playerItemId, Integer quantity) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        PlayerItem playerItem = playerItemMapper.selectById(playerItemId);
        if (playerItem == null) {
            throw new IllegalArgumentException("物品不存在");
        }

        if (!playerItem.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("该物品不属于当前玩家");
        }

        Item item = itemMapper.selectById(playerItem.getItemId());
        if (item == null || !item.getSellable()) {
            throw new IllegalArgumentException("该物品不可出售");
        }

        if (playerItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("物品数量不足");
        }

        // 计算出售价格（通常是购买价格的50%）
        long sellPrice = (item.getPrice() * quantity) / 2;
        
        // 增加玩家灵石
        player.setSpiritStones(player.getSpiritStones() + sellPrice);
        playerProfileMapper.updateById(player);

        // 减少物品数量
        playerItem.setQuantity(playerItem.getQuantity() - quantity);
        if (playerItem.getQuantity() <= 0) {
            playerItemMapper.deleteById(playerItem.getId());
        } else {
            playerItem.setUpdatedAt(LocalDateTime.now());
            playerItemMapper.updateById(playerItem);
        }
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
            default:
                return Comparator.comparing(PlayerItem::getId);
        }
    }

    private PlayerItemResponse convertToResponse(PlayerItem playerItem) {
        Item item = itemMapper.selectById(playerItem.getItemId());
        
        PlayerItemResponse response = new PlayerItemResponse();
        response.setId(playerItem.getId().longValue());
        response.setItemId(playerItem.getItemId().longValue());
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
