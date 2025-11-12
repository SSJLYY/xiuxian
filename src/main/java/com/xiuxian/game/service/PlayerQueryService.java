package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerQueryService {

    private final PlayerProfileRepository playerProfileRepository;

    /**
     * 查找在线玩家（最近5分钟内活跃）
     */
    public List<PlayerProfile> findOnlinePlayers() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return playerProfileRepository.findByLastOnlineTimeAfter(fiveMinutesAgo);
    }

    /**
     * 查找不活跃玩家（30天未登录）
     */
    public List<PlayerProfile> findInactivePlayers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return playerProfileRepository.findByLastOnlineTimeBefore(thirtyDaysAgo);
    }

    /**
     * 查找需要清理的长时间离线玩家（90天未登录）
     */
    public List<PlayerProfile> findPlayersForCleanup() {
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        return playerProfileRepository.findInactivePlayers(ninetyDaysAgo);
    }
}