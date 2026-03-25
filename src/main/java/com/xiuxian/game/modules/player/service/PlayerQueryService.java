package com.xiuxian.game.modules.player.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(value = "app.features.player-query.enabled", havingValue = "true")
@RequiredArgsConstructor
public class PlayerQueryService {

    private final PlayerProfileMapper playerProfileMapper;

    public List<PlayerProfile> findOnlinePlayers() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        QueryWrapper<PlayerProfile> wrapper = new QueryWrapper<>();
        wrapper.gt("last_online_time", fiveMinutesAgo);
        return playerProfileMapper.selectList(wrapper);
    }

    public List<PlayerProfile> findInactivePlayers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        QueryWrapper<PlayerProfile> wrapper = new QueryWrapper<>();
        wrapper.lt("last_online_time", thirtyDaysAgo);
        return playerProfileMapper.selectList(wrapper);
    }

    public List<PlayerProfile> findPlayersForCleanup() {
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        QueryWrapper<PlayerProfile> wrapper = new QueryWrapper<>();
        wrapper.lt("last_online_time", ninetyDaysAgo);
        return playerProfileMapper.selectList(wrapper);
    }
}
