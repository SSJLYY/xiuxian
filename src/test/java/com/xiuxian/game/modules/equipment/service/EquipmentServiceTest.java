package com.xiuxian.game.modules.equipment.service;

import com.xiuxian.game.modules.equipment.entity.PlayerEquipment;
import com.xiuxian.game.modules.equipment.mapper.PlayerEquipmentMapper;
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
 * EquipmentService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentService 单元测试")
class EquipmentServiceTest {

    @Mock
    private PlayerEquipmentMapper playerEquipmentMapper;

    @InjectMocks
    private EquipmentService equipmentService;

    private PlayerEquipment testEquipment;

    @BeforeEach
    void setUp() {
        testEquipment = new PlayerEquipment();
        testEquipment.setEquipmentId(1L);
        testEquipment.setItemId(100L);
        testEquipment.setEnhanceLevel(0);
        testEquipment.setAttackBonus(50);
        testEquipment.setDefenseBonus(30);
    }

    @Test
    @DisplayName("装备物品 - 成功")
    void equipItem_Success() {
        // Given
        Long playerId = 1L;
        Long itemId = 100L;
        when(playerEquipmentMapper.selectOne(any())).thenReturn(null);
        when(playerEquipmentMapper.insert(any(PlayerEquipment.class))).thenReturn(1);

        // When
        equipmentService.equipItem(playerId, itemId);

        // Then
        verify(playerEquipmentMapper, times(1)).insert(any(PlayerEquipment.class));
    }

    @Test
    @DisplayName("卸下装备 - 成功")
    void unequipItem_Success() {
        // Given
        Long playerId = 1L;
        Long equipmentId = 1L;
        when(playerEquipmentMapper.selectById(equipmentId)).thenReturn(testEquipment);
        when(playerEquipmentMapper.deleteById(equipmentId)).thenReturn(1);

        // When
        equipmentService.unequipItem(playerId, equipmentId);

        // Then
        verify(playerEquipmentMapper, times(1)).deleteById(equipmentId);
    }

    @Test
    @DisplayName("强化装备 - 成功")
    void enhanceEquipment_Success() {
        // Given
        Long equipmentId = 1L;
        testEquipment.setEnhanceLevel(0);
        when(playerEquipmentMapper.selectById(equipmentId)).thenReturn(testEquipment);
        when(playerEquipmentMapper.updateById(any(PlayerEquipment.class))).thenReturn(1);

        // When
        equipmentService.enhanceEquipment(equipmentId);

        // Then
        verify(playerEquipmentMapper, times(1)).updateById(argThat(equip ->
            equip.getEnhanceLevel() == 1
        ));
    }

    @Test
    @DisplayName("强化装备 - 已达上限")
    void enhanceEquipment_MaxLevel() {
        // Given
        Long equipmentId = 1L;
        testEquipment.setEnhanceLevel(15); // 最大强化等级
        when(playerEquipmentMapper.selectById(equipmentId)).thenReturn(testEquipment);

        // When & Then
        assertThatThrownBy(() -> equipmentService.enhanceEquipment(equipmentId))
            .isInstanceOf(BusinessException.class);
    }
}
