package com.xiuxian.game.modules.ranking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.xiuxian.game.modules.admin.service.CacheService;

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
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        String cacheKey = CacheService.CacheKeys.rankingKey(rankingType + ":" + safeLimit);
        
        // 先从缓存获取
        List<Ranking> cachedRankings = cacheService.get(cacheKey);
        if (cachedRankings != null) {
            log.debug("从缓存获取排行榜: type={}, limit={}", rankingType, limit);
            return cachedRankings;
        }
        
        // 缓存未命中，从数据库查询
        LambdaQueryWrapper<Ranking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ranking::getRankingType, rankingType)
               .orderByAsc(Ranking::getRank)
               .last("LIMIT " + safeLimit);
        
        List<Ranking> rankings = rankingMapper.selectList(wrapper);
        
        // 批量加载玩家信息（避免N+1查询）
        if (!rankings.isEmpty()) {
            List<Integer> playerIds = rankings.stream()
                    .map(Ranking::getPlayerId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, PlayerProfile> playerMap = playerService.getPlayerProfilesByIds(playerIds);
            for (Ranking ranking : rankings) {
                PlayerProfile player = playerMap.get(ranking.getPlayerId());
                if (player != null) {
                    ranking.setPlayerName(player.getNickname());
                    ranking.setRealm(player.getRealm());
                }
            }
        }
        
        // 存入缓存，缓存30分钟
        cacheService.put(cacheKey, rankings, 1800);
        log.debug("排行榜已缓存: type={}, limit={}", rankingType, limit);
        
        return rankings;
    }

    public List<Ranking> getRankingPage(String rankingType, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int limit = safePage * safeSize;
        List<Ranking> rankings = getRankingList(rankingType, limit);
        int fromIndex = Math.min((safePage - 1) * safeSize, rankings.size());
        int toIndex = Math.min(fromIndex + safeSize, rankings.size());
        return rankings.subList(fromIndex, toIndex);
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
     * 定时更新排行榜（每小时执行一次）
     * 注意：@Async + @Transactional 同时使用时，Spring 代理机制正常工作，
     * 因为定时触发走的是代理对象（非自调用）。保留 @Async 避免阻塞调度线程。
     * 注意：clearRankingCache()（Redis调用）在事务提交后执行，避免长事务占用DB连接。
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Async("rankingTaskExecutor")
    public void updateRankings() {
        log.info("开始更新排行榜");

        // 事务内仅执行DB操作
        doUpdateRankingsInTx();

        // 事务提交后再清除缓存（Redis操作不应在DB事务内）
        clearRankingCache();

        log.info("排行榜更新完成");
    }

    /**
     * 排行榜DB更新（独立事务，不含Redis调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void doUpdateRankingsInTx() {
        updateLevelRanking();
        updateSpiritStonesRanking();
        updateCombatPowerRanking();
    }
    
    /**
     * 清除排行榜缓存
     */
    private void clearRankingCache() {
        String[] rankingTypes = {"LEVEL", "SPIRIT_STONES", "COMBAT_POWER"};

        for (String type : rankingTypes) {
            cacheService.removeByPrefix(CacheService.CacheKeys.rankingKey(type + ":"));
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
        
        // 批量构建排名记录（避免循环insert）
        List<Ranking> rankings = new ArrayList<>(players.size());
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < players.size(); i++) {
            PlayerProfile player = players.get(i);
            Ranking ranking = new Ranking();
            ranking.setPlayerId(player.getId());
            ranking.setPlayerName(player.getNickname());
            ranking.setRankingType("LEVEL");
            ranking.setRank(i + 1);
            ranking.setScore((long) (player.getLevel() == null ? 0 : player.getLevel()));
            ranking.setUpdatedAt(now);
            rankings.add(ranking);
        }
        if (!rankings.isEmpty()) {
            rankingMapper.insertBatch(rankings);
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
        
        // 批量构建排名记录（避免循环insert）
        List<Ranking> rankings = new ArrayList<>(players.size());
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < players.size(); i++) {
            PlayerProfile player = players.get(i);
            Ranking ranking = new Ranking();
            ranking.setPlayerId(player.getId());
            ranking.setPlayerName(player.getNickname());
            ranking.setRankingType("SPIRIT_STONES");
            ranking.setRank(i + 1);
            ranking.setScore(player.getSpiritStones());
            ranking.setUpdatedAt(now);
            rankings.add(ranking);
        }
        if (!rankings.isEmpty()) {
            rankingMapper.insertBatch(rankings);
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
        List<PlayerProfile> players = playerService.getTopPlayersByCombatPower(100);
        
        // 批量构建排名记录（避免循环insert）
        List<Ranking> rankings = new ArrayList<>(players.size());
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < players.size(); i++) {
            PlayerProfile player = players.get(i);
            long combatPower = player.getTotalAttack() + player.getTotalDefense()
                    + player.getTotalHealth() + player.getTotalMana() + player.getTotalSpeed();
            
            Ranking ranking = new Ranking();
            ranking.setPlayerId(player.getId());
            ranking.setPlayerName(player.getNickname());
            ranking.setRankingType("COMBAT_POWER");
            ranking.setRank(i + 1);
            ranking.setScore(combatPower);
            ranking.setUpdatedAt(now);
            rankings.add(ranking);
        }
        if (!rankings.isEmpty()) {
            rankingMapper.insertBatch(rankings);
        }
        
        log.debug("战力排行榜更新完成: {} 名玩家", players.size());
    }

    /**
     * 手动刷新排行榜
     * 注意：自调用 updateRankings() 时 @Transactional 不通过代理生效，
     * 因此手动刷新时直接内联逻辑而非自调用。
     */
    public void refreshRankings() {
        log.info("手动刷新排行榜");
        doUpdateRankingsInTx();
        clearRankingCache();
        log.info("手动刷新排行榜完成");
    }
}
