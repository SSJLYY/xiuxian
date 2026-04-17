package com.xiuxian.game.modules.skill.service;

import com.xiuxian.game.modules.skill.entity.PlayerSkill;
import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.mapper.PlayerSkillMapper;
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
 * SkillService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SkillService 单元测试")
class SkillServiceTest {

    @Mock
    private PlayerSkillMapper playerSkillMapper;

    @InjectMocks
    private SkillService skillService;

    private PlayerSkill testSkill;

    @BeforeEach
    void setUp() {
        testSkill = new PlayerSkill();
        testSkill.setPlayerSkillId(1L);
        testSkill.setSkillId(1L);
        testSkill.setLevel(3);
        testSkill.setExp(500);
    }

    @Test
    @DisplayName("学习技能 - 成功")
    void learnSkill_Success() {
        // Given
        Long playerId = 1L;
        Long skillId = 1L;
        
        when(playerSkillMapper.selectOne(any())).thenReturn(null);
        when(playerSkillMapper.insert(any(PlayerSkill.class))).thenReturn(1);

        // When
        skillService.learnSkill(playerId, skillId);

        // Then
        verify(playerSkillMapper, times(1)).insert(any(PlayerSkill.class));
    }

    @Test
    @DisplayName("学习技能 - 已学习")
    void learnSkill_AlreadyLearned() {
        // Given
        Long playerId = 1L;
        Long skillId = 1L;
        when(playerSkillMapper.selectOne(any())).thenReturn(testSkill);

        // When & Then
        assertThatThrownBy(() -> skillService.learnSkill(playerId, skillId))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("升级技能 - 成功")
    void upgradeSkill_Success() {
        // Given
        Long playerSkillId = 1L;
        testSkill.setLevel(3);
        when(playerSkillMapper.selectById(playerSkillId)).thenReturn(testSkill);
        when(playerSkillMapper.updateById(any(PlayerSkill.class))).thenReturn(1);

        // When
        skillService.upgradeSkill(playerSkillId);

        // Then
        verify(playerSkillMapper, times(1)).updateById(argThat(skill ->
            skill.getLevel() == 4
        ));
    }

    @Test
    @DisplayName("升级技能 - 已达上限")
    void upgradeSkill_MaxLevel() {
        // Given
        Long playerSkillId = 1L;
        testSkill.setLevel(10); // 最大等级
        when(playerSkillMapper.selectById(playerSkillId)).thenReturn(testSkill);

        // When & Then
        assertThatThrownBy(() -> skillService.upgradeSkill(playerSkillId))
            .isInstanceOf(BusinessException.class);
    }
}
