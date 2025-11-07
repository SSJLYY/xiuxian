package com.xiuxian.game.repository;

import com.xiuxian.game.entity.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Integer> {
    Optional<PlayerProfile> findByUserId(Integer userId);
}
