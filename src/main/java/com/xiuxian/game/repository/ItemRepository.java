package com.xiuxian.game.repository;

import com.xiuxian.game.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByType(String type);
    List<Item> findByQualityGreaterThanEqual(Integer quality);
    List<Item> findBySellable(Boolean sellable);
}