package com.xiuxian.game.repository;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerSkill;
import com.xiuxian.game.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerSkillRepository extends JpaRepository<PlayerSkill, Integer> {
    List<PlayerSkill> findByPlayer(PlayerProfile player);
    List<PlayerSkill> findByPlayerAndEquipped(PlayerProfile player, Boolean equipped);
    Optional<PlayerSkill> findByPlayerAndSkill(PlayerProfile player, Skill skill);
    List<PlayerSkill> findByPlayerAndSlotNumberGreaterThanEqual(PlayerProfile player, Integer slotNumber);
}