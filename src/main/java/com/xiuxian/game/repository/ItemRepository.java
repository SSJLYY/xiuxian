package com.xiuxian.game.repository;

import com.xiuxian.game.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByType(String type);
    List<Item> findByQualityGreaterThanEqual(Integer quality);
    List<Item> findBySellable(Boolean sellable);

    // 添加一些常用的查询方法
    Optional<Item> findByName(String name);
    List<Item> findByUsable(Boolean usable);
    List<Item> findByStackable(Boolean stackable);
    List<Item> findByPriceLessThanEqual(Integer price);
    List<Item> findByTypeAndQualityGreaterThanEqual(String type, Integer quality);

    // 添加分页查询（可选）
    // Page<Item> findByType(String type, Pageable pageable);
}