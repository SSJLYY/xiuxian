package com.xiuxian.game.service;

import com.xiuxian.game.entity.Item;
import com.xiuxian.game.entity.PlayerItem;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.dto.response.PlayerItemResponse;
import com.xiuxian.game.repository.ItemRepository;
import com.xiuxian.game.repository.PlayerItemRepository;
import com.xiuxian.game.repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ItemRepository itemRepository;
    private final PlayerItemRepository playerItemRepository;
    private final PlayerProfileRepository playerProfileRepository;

    // 修复1：确保方法签名与调用一致
    public List<PlayerItemResponse> getPlayerInventory(Integer playerId, String type, String search, String sortBy, String order) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));

        List<PlayerItem> playerItems = playerItemRepository.findByPlayer(player);

        // 过滤逻辑
        if (type != null && !type.isEmpty()) {
            playerItems = playerItems.stream()
                    .filter(pi -> pi.getItem().getType().equals(type))
                    .collect(Collectors.toList());
        }

        // 搜索逻辑
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            playerItems = playerItems.stream()
                    .filter(pi -> pi.getItem().getName().toLowerCase().contains(searchLower) ||
                            pi.getItem().getDescription().toLowerCase().contains(searchLower))
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

    // 修复2：添加重载方法，支持不同的参数组合
    public List<PlayerItemResponse> getPlayerInventory(Integer playerId) {
        return getPlayerInventory(playerId, null, null, null, null);
    }

    // 修复3：添加正确的 addItemToInventory 方法
    @Transactional
    public PlayerItemResponse addItemToInventory(Integer playerId, Integer itemId, Integer quantity) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        // 检查是否已存在该物品
        Optional<PlayerItem> existingItem = playerItemRepository.findByPlayerAndItem(player, item);

        if (existingItem.isPresent()) {
            // 如果可堆叠，增加数量
            PlayerItem playerItem = existingItem.get();
            if (item.getStackable()) {
                playerItem.setQuantity(playerItem.getQuantity() + quantity);
                playerItemRepository.save(playerItem);
                return convertToResponse(playerItem);
            }
        }

        // 创建新物品
        PlayerItem newPlayerItem = PlayerItem.builder()
                .player(player)
                .item(item)
                .quantity(quantity)
                .build();

        PlayerItem savedItem = playerItemRepository.save(newPlayerItem);
        return convertToResponse(savedItem);
    }

    // 修复4：添加正确的 removeItemFromInventory 方法（3个参数）
    @Transactional
    public void removeItemFromInventory(Integer playerItemId, Integer quantity, Integer playerId) {
        PlayerItem playerItem = playerItemRepository.findById(playerItemId)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        // 验证物品所有权
        if (!playerItem.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException("无权操作该物品");
        }

        if (playerItem.getQuantity() <= quantity) {
            playerItemRepository.delete(playerItem);
        } else {
            playerItem.setQuantity(playerItem.getQuantity() - quantity);
            playerItemRepository.save(playerItem);
        }
    }

    // 修复5：添加正确的 useItem 方法（3个参数）
    @Transactional
    public Map<String, Object> useItem(Integer playerItemId, Integer quantity, Integer playerId) {
        PlayerItem playerItem = playerItemRepository.findById(playerItemId)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        // 验证物品所有权
        if (!playerItem.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException("无权操作该物品");
        }

        Item item = playerItem.getItem();
        if (!item.getUsable()) {
            throw new IllegalArgumentException("该物品不可使用");
        }

        if (playerItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("物品数量不足");
        }

        // 应用物品效果
        PlayerProfile player = playerItem.getPlayer();
        for (int i = 0; i < quantity; i++) {
            applyItemEffect(player, item);
        }

        // 更新数量
        if (playerItem.getQuantity() == quantity) {
            playerItemRepository.delete(playerItem);
        } else {
            playerItem.setQuantity(playerItem.getQuantity() - quantity);
            playerItemRepository.save(playerItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "成功使用 " + item.getName() + " x" + quantity);
        result.put("item", convertToResponse(playerItem));

        return result;
    }

    // 辅助方法
    private Comparator<PlayerItem> getSortComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "name":
                return Comparator.comparing(pi -> pi.getItem().getName());
            case "type":
                return Comparator.comparing(pi -> pi.getItem().getType());
            case "quality":
                return Comparator.comparing(pi -> pi.getItem().getQuality());
            case "quantity":
                return Comparator.comparing(PlayerItem::getQuantity);
            default:
                return Comparator.comparing(PlayerItem::getId);
        }
    }

    private void applyItemEffect(PlayerProfile player, Item item) {
        // 根据物品类型应用效果
        switch (item.getType()) {
            case "恢复药剂":
                player.setHealth(Math.min(player.getHealth() + 100, player.getTotalHealth()));
                break;
            case "经验丹":
                player.setExp(player.getExp() + 500);
                break;
            default:
                // 默认效果
                break;
        }
        playerProfileRepository.save(player);
    }

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
                .createdAt(playerItem.getCreatedAt())
                .updatedAt(playerItem.getUpdatedAt())
                .build();
    }
}