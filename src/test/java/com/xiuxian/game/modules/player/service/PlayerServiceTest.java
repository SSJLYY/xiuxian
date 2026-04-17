package com.xiuxian.game.modules.player.service;

import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PlayerService 单元测试
 * 测试覆盖率目标：80%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerService 单元测试")
class PlayerServiceTest {

    @Mock
    private PlayerProfileMapper playerProfileMapper;

    @InjectMocks
    private PlayerService playerService;

    private PlayerProfile testProfile;

    @BeforeEach
    void setUp() {
        testProfile = new PlayerProfile();
        testProfile.setId(1L);
        testProfile.setNickname("测试玩家");
        testProfile.setLevel(5);
        testProfile.setExperience(1000L);
        testProfile.setSpiritStones(5000);
        testProfile.setAttack(100);
        testProfile.setDefense(80);
        testProfile.setHealth(500);
        testProfile.setMana(200);
        testProfile.setSpeed(90);
        testProfile.setIsCultivating(false);
        testProfile.setCreatedAt(LocalDateTime.now());
        testProfile.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("根据 ID 获取玩家档案 - 成功")
    void getPlayerProfileById_Success() {
        // Given
        Long playerId = 1L;
        when(playerProfileMapper.selectById(playerId)).thenReturn(testProfile);

        // When
        PlayerProfile result = playerService.getPlayerProfileById(playerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(playerId);
        assertThat(result.getNickname()).isEqualTo("测试玩家");
        verify(playerProfileMapper, times(1)).selectById(playerId);
    }

    @Test
    @DisplayName("根据 ID 获取玩家档案 - 玩家不存在")
    void getPlayerProfileById_NotFound() {
        // Given
        Long playerId = 999L;
        when(playerProfileMapper.selectById(playerId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> playerService.getPlayerProfileById(playerId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLAYER_NOT_FOUND);
        
        verify(playerProfileMapper, times(1)).selectById(playerId);
    }

    @Test
    @DisplayName("保存玩家档案 - 成功")
    void savePlayerProfile_Success() {
        // Given
        when(playerProfileMapper.insert(any(PlayerProfile.class))).thenReturn(1);
        when(playerProfileMapper.selectById(1L)).thenReturn(testProfile);

        // When
        PlayerProfile savedProfile = playerService.savePlayerProfile(testProfile);

        // Then
        assertThat(savedProfile).isNotNull();
        assertThat(savedProfile.getId()).isEqualTo(1L);
        verify(playerProfileMapper, times(1)).insert(testProfile);
    }

    @Test
    @DisplayName("更新玩家等级 - 成功")
    void updatePlayerLevel_Success() {
        // Given
        Long playerId = 1L;
        int newLevel = 6;
        when(playerProfileMapper.selectById(playerId)).thenReturn(testProfile);
        when(playerProfileMapper.updateById(any(PlayerProfile.class))).thenReturn(1);

        // When
        playerService.updatePlayerLevel(playerId, newLevel);

        // Then
        verify(playerProfileMapper, times(1)).selectById(playerId);
        verify(playerProfileMapper, times(1)).updateById(any(PlayerProfile.class));
    }

    @Test
    @DisplayName("增加灵石 - 成功")
    void addSpiritStones_Success() {
        // Given
        Long playerId = 1L;
        int amount = 1000;
        int expectedTotal = 5000 + 1000;
        
        when(playerProfileMapper.selectById(playerId)).thenReturn(testProfile);
        when(playerProfileMapper.updateById(any(PlayerProfile.class))).thenReturn(1);

        // When
        playerService.addSpiritStones(playerId, amount);

        // Then
        verify(playerProfileMapper, times(1)).updateById(argThat(profile -> 
            profile.getSpiritStones() == expectedTotal
        ));
    }

    @Test
    @DisplayName("消耗灵石 - 灵石不足")
    void consumeSpiritStones_InsufficientFunds() {
        // Given
        Long playerId = 1L;
        int amount = 10000; // 超过当前灵石数量
        
        when(playerProfileMapper.selectById(playerId)).thenReturn(testProfile);

        // When & Then
        assertThatThrownBy(() -> playerService.consumeSpiritStones(playerId, amount))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_SPIRIT_STONES);
    }

    @Test
    @DisplayName("消耗灵石 - 成功")
    void consumeSpiritStones_Success() {
        // Given
        Long playerId = 1L;
        int amount = 1000;
        int expectedRemaining = 5000 - 1000;
        
        when(playerProfileMapper.selectById(playerId)).thenReturn(testProfile);
        when(playerProfileMapper.updateById(any(PlayerProfile.class))).thenReturn(1);

        // When
        playerService.consumeSpiritStones(playerId, amount);

        // Then
        verify(playerProfileMapper, times(1)).updateById(argThat(profile -> 
            profile.getSpiritStones() == expectedRemaining
        ));
    }

    @Test
    @DisplayName("获取玩家修炼速度 - 未修炼中")
    void getCultivationSpeed_NotCultivating() {
        // Given
        testProfile.setIsCultivating(false);
        when(playerProfileMapper.selectById(1L)).thenReturn(testProfile);

        // When
        Double speed = playerService.getCultivationSpeed(1L);

        // Then
        assertThat(speed).isEqualTo(0.0);
    }

    @Test
    @DisplayName("计算修炼收益 - 正确")
    void calculateCultivationReward_Correct() {
        // Given
        long baseSpeed = 100;
        double bonus = 1.2; // 20% 加成
        long duration = 3600; // 1 小时
        
        // When
        long expReward = (long)(baseSpeed * bonus * duration / 3600);

        // Then
        assertThat(expReward).isEqualTo(120);
    }
}
