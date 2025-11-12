package com.xiuxian.game.repository;

import com.xiuxian.game.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Integer> {
    List<ShopItem> findByShopTypeAndIsAvailable(String shopType, Boolean isAvailable);
    List<ShopItem> findByIsAvailable(Boolean isAvailable);

    // 添加以下常用查询方法
    List<ShopItem> findByItemId(Integer itemId);
    List<ShopItem> findByEquipmentId(Integer equipmentId);
    Optional<ShopItem> findByItemIdAndIsAvailable(Integer itemId, Boolean isAvailable);
    Optional<ShopItem> findByEquipmentIdAndIsAvailable(Integer equipmentId, Boolean isAvailable);
}