package com.xiuxian.game.modules.ranking.service;

import com.xiuxian.game.modules.ranking.entity.Ranking;
import com.xiuxian.game.modules.ranking.mapper.RankingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RankingService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RankingService 单元测试")
class RankingServiceTest {

    @Mock
    private RankingMapper rankingMapper;

    @InjectMocks
    private RankingService rankingService;

    private List<Ranking> testRankings;

    @BeforeEach
    void setUp() {
        testRankings = Arrays.asList(
            createRanking(1L, "玩家 A", 1000),
            createRanking(2L, "玩家 B", 900),
            createRanking(3L, "玩家 C", 800)
        );
    }

    private Ranking createRanking(Long playerId, String playerName, Integer value) {
        Ranking ranking = new Ranking();
        ranking.setPlayerId(playerId);
        ranking.setPlayerName(playerName);
        ranking.setRankValue(value);
        return ranking;
    }

    @Test
    @DisplayName("获取战力排行榜 - 成功")
    void getCombatPowerRanking_Success() {
        // Given
        String rankType = "COMBAT_POWER";
        int page = 1;
        int size = 10;
        when(rankingMapper.selectByType(anyString(), anyInt(), anyInt())).thenReturn(testRankings);

        // When
        List<Ranking> result = rankingService.getRanking(rankType, page, size);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRankValue()).isEqualTo(1000);
        verify(rankingMapper, times(1)).selectByType(rankType, page, size);
    }

    @Test
    @DisplayName("获取玩家排名 - 成功")
    void getPlayerRanking_Success() {
        // Given
        Long playerId = 1L;
        String rankType = "COMBAT_POWER";
        when(rankingMapper.getPlayerRank(playerId, rankType)).thenReturn(1);

        // When
        Integer rank = rankingService.getPlayerRank(playerId, rankType);

        // Then
        assertThat(rank).isEqualTo(1);
        verify(rankingMapper, times(1)).getPlayerRank(playerId, rankType);
    }

    @Test
    @DisplayName("更新排行榜 - 成功")
    void updateRanking_Success() {
        // Given
        Long playerId = 1L;
        String rankType = "COMBAT_POWER";
        Integer value = 1000;
        when(rankingMapper.selectOne(any())).thenReturn(null);
        when(rankingMapper.insert(any(Ranking.class))).thenReturn(1);

        // When
        rankingService.updateRanking(playerId, rankType, value);

        // Then
        verify(rankingMapper, times(1)).insert(any(Ranking.class));
    }
}
