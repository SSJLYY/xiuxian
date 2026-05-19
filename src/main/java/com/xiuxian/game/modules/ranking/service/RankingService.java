package com.xiuxian.game.modules.ranking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.admin.service.CacheService;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.ranking.entity.Ranking;
import com.xiuxian.game.modules.ranking.mapper.RankingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final RankingMapper rankingMapper;
    private final PlayerService playerService;
    private final CacheService cacheService;
    private final RankingRefreshService rankingRefreshService;

    @SuppressWarnings("unchecked")
    public List<Ranking> getRankingList(String rankingType, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        String cacheKey = CacheService.CacheKeys.rankingKey(rankingType + ":" + safeLimit);

        List<Ranking> cachedRankings = cacheService.get(cacheKey);
        if (cachedRankings != null) {
            return cachedRankings;
        }

        LambdaQueryWrapper<Ranking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ranking::getRankingType, rankingType)
                .orderByAsc(Ranking::getRank)
                .last("LIMIT " + safeLimit);

        List<Ranking> rankings = rankingMapper.selectList(wrapper);
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

        cacheService.put(cacheKey, rankings, 1800);
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

    public Integer getPlayerRank(Integer playerId, String rankingType) {
        Ranking ranking = rankingMapper.selectOne(
                new QueryWrapper<Ranking>()
                        .eq("player_id", playerId)
                        .eq("ranking_type", rankingType));
        return ranking != null ? ranking.getRank() : null;
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Async("rankingTaskExecutor")
    public void updateRankings() {
        log.info("开始更新排行榜");
        rankingRefreshService.refreshAllRankingsInTx();
        clearRankingCache();
        log.info("排行榜更新完成");
    }

    public void refreshRankings() {
        log.info("手动刷新排行榜");
        rankingRefreshService.refreshAllRankingsInTx();
        clearRankingCache();
        log.info("手动刷新排行榜完成");
    }

    private void clearRankingCache() {
        String[] rankingTypes = {"LEVEL", "SPIRIT_STONES", "COMBAT_POWER"};
        for (String type : rankingTypes) {
            cacheService.removeByPrefix(CacheService.CacheKeys.rankingKey(type + ":"));
        }
    }
}
