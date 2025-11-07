package com.xiuxian.game.repository;

import com.xiuxian.game.entity.CultivationLog;
import com.xiuxian.game.entity.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CultivationLogRepository extends JpaRepository<CultivationLog, Integer> {
    List<CultivationLog> findByPlayerProfileOrderByCreatedAtDesc(PlayerProfile playerProfile);
    List<CultivationLog> findByPlayerProfileAndCreatedAtAfter(PlayerProfile playerProfile, LocalDateTime after);
}
