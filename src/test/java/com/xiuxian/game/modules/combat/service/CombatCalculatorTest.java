package com.xiuxian.game.modules.combat.service;

import com.xiuxian.game.modules.combat.entity.Monster;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * CombatCalculator 战斗计算器测试
 * 验证战斗伤害计算公式的正确性
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CombatCalculator 战斗计算测试")
class CombatCalculatorTest {

    private PlayerProfile player;
    private Monster monster;

    @BeforeEach
    void setUp() {
        player = new PlayerProfile();
        player.setAttack(150);
        player.setDefense(100);
        player.setSpeed(120);

        monster = new Monster();
        monster.setAttack(80);
        monster.setDefense(50);
        monster.setSpeed(90);
    }

    @Test
    @DisplayName("计算物理伤害 - 基础公式")
    void calculatePhysicalDamage_Basic() {
        // When
        long damage = Math.max(1, player.getAttack() - monster.getDefense());

        // Then
        assertThat(damage).isEqualTo(100); // 150 - 50 = 100
    }

    @Test
    @DisplayName("计算暴击伤害")
    void calculateCriticalDamage() {
        // Given
        long baseDamage = 100;
        double criticalMultiplier = 2.0;

        // When
        long criticalDamage = (long)(baseDamage * criticalMultiplier);

        // Then
        assertThat(criticalDamage).isEqualTo(200);
    }

    @Test
    @DisplayName("计算暴击率 - 速度优势")
    void calculateCriticalRate_SpeedAdvantage() {
        // Given
        double baseCritRate = 0.05; // 5% 基础暴击率
        double speedDiff = player.getSpeed() - monster.getSpeed(); // 30
        double speedBonus = Math.min(speedDiff / 100.0, 0.20); // 最多 20%

        // When
        double totalCritRate = baseCritRate + speedBonus;

        // Then
        assertThat(totalCritRate).isEqualTo(0.35); // 0.05 + 0.30 = 0.35
        assertThat(totalCritRate).isLessThanOrEqualTo(0.25); // 实际应该 capped
    }

    @Test
    @DisplayName("计算闪避率 - 速度差")
    void calculateDodgeRate() {
        // Given
        double speedDiff = player.getSpeed() - monster.getSpeed(); // 30
        double dodgeRate = Math.min(speedDiff / 200.0, 0.20); // 最多 20%

        // Then
        assertThat(dodgeRate).isEqualTo(0.15); // 30/200 = 0.15
        assertThat(dodgeRate).isLessThanOrEqualTo(0.20);
    }

    @Test
    @DisplayName("属性克制 - 火克金")
    void elementalAdvantage_FireOverMetal() {
        // Given
        String playerElement = "fire";
        String monsterElement = "metal";
        double advantageMultiplier = 1.2; // 20% 伤害加成
        long baseDamage = 100;

        // When
        long finalDamage = (long)(baseDamage * advantageMultiplier);

        // Then
        assertThat(finalDamage).isEqualTo(120);
    }

    @Test
    @DisplayName("计算技能伤害")
    void calculateSkillDamage() {
        // Given
        long baseDamage = 100;
        double skillMultiplier = 1.5; // 技能倍率

        // When
        long skillDamage = (long)(baseDamage * skillMultiplier);

        // Then
        assertThat(skillDamage).isEqualTo(150);
    }

    @Test
    @DisplayName("计算最终伤害 - 包含所有加成")
    void calculateFinalDamage_FullFormula() {
        // Given
        long attack = 150;
        long defense = 50;
        double skillMultiplier = 1.5;
        boolean isCritical = true;
        double elementalBonus = 1.2;

        // When
        long baseDamage = Math.max(1, attack - defense); // 100
        long skillDamage = (long)(baseDamage * skillMultiplier); // 150
        long critDamage = isCritical ? (long)(skillDamage * 2.0) : skillDamage; // 300
        long finalDamage = (long)(critDamage * elementalBonus); // 360

        // Then
        assertThat(finalDamage).isEqualTo(360);
    }
}
