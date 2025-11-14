package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.mapper.PlayerProfileMapper;
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

    /**
     * 查找在线玩家（最近5分钟内活跃）
     */
    public List<PlayerProfile> findOnlinePlayers() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        QueryWrapper<PlayerProfile> wrapper = new QueryWrapper<>();
        wrapper.gt("last_online_time", fiveMinutesAgo);
        return playerProfileMapper.selectList(wrapper);
    }

    /**
     * 查找不活跃玩家（30天未登录）
     */
    public List<PlayerProfile> findInactivePlayers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        QueryWrapper<PlayerProfile> wrapper = new QueryWrapper<>();
        wrapper.lt("last_online_time", thirtyDaysAgo);
        return playerProfileMapper.selectList(wrapper);
    }

    /**
     * 查找需要清理的长时间离线玩家（90天未登录）
     */
    public List<PlayerProfile> findPlayersForCleanup() {
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        QueryWrapper<PlayerProfile> wrapper = new QueryWrapper<>();
        wrapper.lt("last_online_time", ninetyDaysAgo);
        return playerProfileMapper.selectList(wrapper);
    }
}
