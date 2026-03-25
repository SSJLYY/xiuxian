package com.xiuxian.game.modules.ranking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.ranking.entity.Ranking;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.ranking.mapper.RankingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 排行榜服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final RankingMapper rankingMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据
    private final CacheService cacheService;

    /**
     * 获取排行榜
     */
    @SuppressWarnings("unchecked")
    public List<Ranking> getRankingList(String rankingType, int limit) {
        String cacheKey = CacheService.CacheKeys.rankingKey(rankingType + ":" + limit);
        
        // 先从缓存获取
        List<Ranking> cachedRankings = cacheService.get(cacheKey);
        if (cachedRankings != null) {
            log.debug("从缓存获取排行榜: type={}, limit={}", rankingType, limit);
            return cachedRankings;
        }
        
        // 缓存未命中，从数据库查询
        QueryWrapper<Ranking> wrapper = new QueryWrapper<>();
        wrapper.eq("ranking_type", rankingType)
               .orderByAsc("rank")
               .last("LIMIT " + limit);
        
        List<Ranking> rankings = rankingMapper.selectList(wrapper);
        
        // 填充玩家信息
        for (Ranking ranking : rankings) {
            PlayerProfile player = playerService.getPlayerProfileById(ranking.getPlayerId());
            if (player != null) {
                ranking.setPlayerName(player.getNickname());
                ranking.setRealm(player.getRealm());
            }
        }
        
        // 存入缓存，缓存30分钟
        cacheService.put(cacheKey, rankings, 1800);
        log.debug("排行榜已缓存: type={}, limit={}", rankingType, limit);
        
        return rankings;
    }

    /**
     * 获取玩家排名
     */
    public Integer getPlayerRank(Integer playerId, String rankingType) {
        Ranking ranking = rankingMapper.selectOne(
                new QueryWrapper<Ranking>()
                        .eq("player_id", playerId)
                        .eq("ranking_type", rankingType));
        
        return ranking != null ? ranking.getRank() : null;
    }

    /**
     * 定时更新排行榜
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Async("rankingTaskExecutor")
    @Transactional
    public void updateRankings() {
        log.info("开始更新排行榜");
        
        try {
            updateLevelRanking();
            updateSpiritStonesRanking();
            updateCombatPowerRanking();
            
            // 清除排行榜缓存
            clearRankingCache();
            
            log.info("排行榜更新完成");
        } catch (Exception e) {
            log.error("更新排行榜失败", e);
        }
    }
    
    /**
     * 清除排行榜缓存
     */
    private void clearRankingCache() {
        String[] rankingTypes = {"LEVEL", "SPIRIT_STONES", "COMBAT_POWER"};
        int[] limits = {10, 50, 100};
        
        for (String type : rankingTypes) {
            for (int limit : limits) {
                String cacheKey = CacheService.CacheKeys.rankingKey(type + ":" + limit);
                cacheService.remove(cacheKey);
            }
        }
        
        log.debug("排行榜缓存已清除");
    }

    /**
     * 更新等级排行榜
     */
    private void updateLevelRanking() {
        log.debug("更新等级排行榜");
        
        // 删除旧数据
        rankingMapper.delete(new QueryWrapper<Ranking>().eq("ranking_type", "LEVEL"));
        
        // 获取前100名玩家
        // 通过PlayerService查询，遵守模块边界
        List<PlayerProfile> players = playerService.getTopPlayersByLevel(100);
        
        // 插入新排名
        int rank = 1;
        for (PlayerProfile player : players) {
            Ranking ranking = new Ranking();
            ranking.setPlayerId(player.getId());
            ranking.setPlayerName(player.getNickname());
            ranking.setRankingType("LEVEL");
            ranking.setRank(rank++);
            ranking.setScore(player.getLevel().longValue());
            ranking.setUpdatedAt(LocalDateTime.now());
            
            rankingMapper.insert(ranking);
        }
        
        log.debug("等级排行榜更新完成: {} 名玩家", players.size());
    }

    /**
     * 更新灵石排行榜
     */
    private void updateSpiritStonesRanking() {
        log.debug("更新灵石排行榜");
        
        // 删除旧数据
        rankingMapper.delete(new QueryWrapper<Ranking>().eq("ranking_type", "SPIRIT_STONES"));
        
        // 获取前100名玩家
        // 通过PlayerService查询，遵守模块边界
        List<PlayerProfile> players = playerService.getTopPlayersBySpiritStones(100);
        
        // 插入新排名
        int rank = 1;
        for (PlayerProfile player : players) {
            Ranking ranking = new Ranking();
            ranking.setPlayerId(player.getId());
            ranking.setPlayerName(player.getNickname());
            ranking.setRankingType("SPIRIT_STONES");
            ranking.setRank(rank++);
            ranking.setScore(player.getSpiritStones());
            ranking.setUpdatedAt(LocalDateTime.now());
            
            rankingMapper.insert(ranking);
        }
        
        log.debug("灵石排行榜更新完成: {} 名玩家", players.size());
    }

    /**
     * 更新战力排行榜
     */
    private void updateCombatPowerRanking() {
        log.debug("更新战力排行榜");
        
        // 删除旧数据
        rankingMapper.delete(new QueryWrapper<Ranking>().eq("ranking_type", "COMBAT_POWER"));
        
        // 获取前100名玩家（按综合战力排序）
        // 通过PlayerService查询，遵守模块边界
        List<PlayerProfile> players = playerService.getTopPlayersByCultivationSpeed(100);
        
        // 插入新排名
        int rank = 1;
        for (PlayerProfile player : players) {
            long combatPower = player.getAttack() + player.getDefense() + 
                              player.getHealth() + player.getMana() + player.getSpeed();
            
            Ranking ranking = new Ranking();
            ranking.setPlayerId(player.getId());
            ranking.setPlayerName(player.getNickname());
            ranking.setRankingType("COMBAT_POWER");
            ranking.setRank(rank++);
            ranking.setScore(combatPower);
            ranking.setUpdatedAt(LocalDateTime.now());
            
            rankingMapper.insert(ranking);
        }
        
        log.debug("战力排行榜更新完成: {} 名玩家", players.size());
    }

    /**
     * 手动刷新排行榜
     */
    @Transactional
    public void refreshRankings() {
        log.info("手动刷新排行榜");
        updateRankings();
    }
}
