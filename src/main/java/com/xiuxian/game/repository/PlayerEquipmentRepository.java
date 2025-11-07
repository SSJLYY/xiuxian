package com.xiuxian.game.repository;

import com.xiuxian.game.entity.PlayerEquipment;
import com.xiuxian.game.entity.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerEquipmentRepository extends JpaRepository<PlayerEquipment, Integer> {
    List<PlayerEquipment> findByPlayer(PlayerProfile player);
    List<PlayerEquipment> findByPlayerAndEquipped(PlayerProfile player, Boolean equipped);
    Optional<PlayerEquipment> findByPlayerAndSlot(PlayerProfile player, String slot);
}