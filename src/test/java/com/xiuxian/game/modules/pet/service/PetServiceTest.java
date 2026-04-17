package com.xiuxian.game.modules.pet.service;

import com.xiuxian.game.modules.pet.entity.Pet;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import com.xiuxian.game.modules.pet.mapper.PlayerPetMapper;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PetService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PetService 单元测试")
class PetServiceTest {

    @Mock
    private PlayerPetMapper playerPetMapper;

    @InjectMocks
    private PetService petService;

    private PlayerPet testPet;

    @BeforeEach
    void setUp() {
        testPet = new PlayerPet();
        testPet.setPlayerPetId(1L);
        testPet.setPetId(1L);
        testPet.setNickname("小灵猫");
        testPet.setLevel(5);
        testPet.setLoyalty(80);
        testPet.setHunger(90);
        testPet.setIsActive(true);
    }

    @Test
    @DisplayName("获取玩家宠物 - 成功")
    void getPlayerPet_Success() {
        // Given
        Long playerPetId = 1L;
        when(playerPetMapper.selectById(playerPetId)).thenReturn(testPet);

        // When
        PlayerPet result = petService.getPlayerPet(playerPetId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("小灵猫");
        verify(playerPetMapper, times(1)).selectById(playerPetId);
    }

    @Test
    @DisplayName("获取玩家宠物 - 不存在")
    void getPlayerPet_NotFound() {
        // Given
        Long playerPetId = 999L;
        when(playerPetMapper.selectById(playerPetId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> petService.getPlayerPet(playerPetId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PET_NOT_FOUND);
    }

    @Test
    @DisplayName("提升宠物饱食度 - 成功")
    void increasePetHunger_Success() {
        // Given
        Long playerPetId = 1L;
        int foodValue = 20;
        testPet.setHunger(80);
        when(playerPetMapper.selectById(playerPetId)).thenReturn(testPet);
        when(playerPetMapper.updateById(any(PlayerPet.class))).thenReturn(1);

        // When
        petService.increasePetHunger(playerPetId, foodValue);

        // Then
        verify(playerPetMapper, times(1)).updateById(argThat(pet ->
            pet.getHunger() == 100 // 80 + 20 = 100, capped at 100
        ));
    }

    @Test
    @DisplayName("宠物训练 - 饱食度不足")
    void trainPet_HungerTooLow() {
        // Given
        Long playerPetId = 1L;
        testPet.setHunger(30); // 低于阈值 50
        when(playerPetMapper.selectById(playerPetId)).thenReturn(testPet);

        // When & Then
        assertThatThrownBy(() -> petService.trainPet(playerPetId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PET_HUNGER_TOO_LOW);
    }

    @Test
    @DisplayName("设置出战宠物 - 成功")
    void setActivePet_Success() {
        // Given
        Long playerId = 1L;
        Long playerPetId = 1L;
        when(playerPetMapper.selectById(playerPetId)).thenReturn(testPet);
        when(playerPetMapper.updateById(any(PlayerPet.class))).thenReturn(1);

        // When
        petService.setActivePet(playerId, playerPetId);

        // Then
        verify(playerPetMapper, times(2)).updateById(any(PlayerPet.class));
    }
}
