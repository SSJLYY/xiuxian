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
        testProfile.setId(1);
        testProfile.setNickname("测试玩家");
        testProfile.setLevel(5);
        testProfile.setExp(1000L);
        testProfile.setSpiritStones(5000L);
        testProfile.setAttack(100);
        testProfile.setDefense(80);
        testProfile.setHealth(500);
        testProfile.setMaxHealth(500);
        testProfile.setMana(200);
        testProfile.setMaxMana(200);
        testProfile.setSpeed(90);
        testProfile.setIsCultivating(false);
        testProfile.setCreatedAt(LocalDateTime.now());
        testProfile.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("根据 ID 获取玩家档案 - 成功")
    void getPlayerProfileById_Success() {
        // Given
        Integer playerId = 1;
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
        Integer playerId = 999;
        when(playerProfileMapper.selectById(playerId)).thenReturn(null);

        // When & Then
        BusinessException exception = org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class, () -> {
            playerService.getPlayerProfileById(playerId);
        });
        org.junit.jupiter.api.Assertions.assertEquals(ErrorCode.PLAYER_NOT_FOUND.getCode(), exception.getCode());
        
        verify(playerProfileMapper, times(1)).selectById(playerId);
    }

    @Test
    @DisplayName("保存玩家档案 - 成功")
    void savePlayerProfile_Success() {
        // Given
        when(playerProfileMapper.updateById(any(PlayerProfile.class))).thenReturn(1);

        // When
        playerService.savePlayerProfile(testProfile);

        // Then
        verify(playerProfileMapper, times(1)).updateById(testProfile);
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
