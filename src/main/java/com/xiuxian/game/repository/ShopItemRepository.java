package com.xiuxian.game.repository;

import com.xiuxian.game.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Integer> {
    List<ShopItem> findByShopTypeAndIsAvailable(String shopType, Boolean isAvailable);
    List<ShopItem> findByIsAvailable(Boolean isAvailable);
}
