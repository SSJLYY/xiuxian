package com.xiuxian.game.modules.ranking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.ranking.entity.Ranking;
import com.xiuxian.game.modules.ranking.mapper.RankingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingRefreshService {

    private final RankingMapper rankingMapper;
    private final PlayerService playerService;

    @Transactional(rollbackFor = Exception.class)
    public void refreshAllRankingsInTx() {
        updateLevelRanking();
        updateSpiritStonesRanking();
        updateCombatPowerRanking();
    }

    private void updateLevelRanking() {
        rankingMapper.delete(new QueryWrapper<Ranking>().eq("ranking_type", "LEVEL"));

        List<PlayerProfile> players = playerService.getTopPlayersByLevel(100);
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
    }

    private void updateSpiritStonesRanking() {
        rankingMapper.delete(new QueryWrapper<Ranking>().eq("ranking_type", "SPIRIT_STONES"));

        List<PlayerProfile> players = playerService.getTopPlayersBySpiritStones(100);
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
    }

    private void updateCombatPowerRanking() {
        rankingMapper.delete(new QueryWrapper<Ranking>().eq("ranking_type", "COMBAT_POWER"));

        List<PlayerProfile> players = playerService.getTopPlayersByCombatPower(100);
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
    }
}
