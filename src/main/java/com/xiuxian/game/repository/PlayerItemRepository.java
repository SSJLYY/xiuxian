package com.xiuxian.game.repository;

import com.xiuxian.game.entity.Item;
import com.xiuxian.game.entity.PlayerItem;
import com.xiuxian.game.entity.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerItemRepository extends JpaRepository<PlayerItem, Integer> {
    List<PlayerItem> findByPlayer(PlayerProfile player);
    Optional<PlayerItem> findByPlayerAndItem(PlayerProfile player, Item item);
}