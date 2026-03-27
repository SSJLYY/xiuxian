package com.xiuxian.game.dto;

import com.xiuxian.game.modules.pet.entity.PetEvolution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 宠物进化结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetEvolutionResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 进化后的名称
     */
    private String newName;

    /**
     * 进化后的等级
     */
    private Integer newLevel;

    /**
     * 进化后的攻击
     */
    private Integer newAttack;

    /**
     * 进化后的防御
     */
    private Integer newDefense;

    /**
     * 进化后的生命
     */
    private Integer newHealth;

    /**
     * 进化后的速度
     */
    private Integer newSpeed;

    /**
     * 新解锁的能力名称
     */
    private String newAbilityName;

    /**
     * 外观变化描述
     */
    private String appearanceChange;

    /**
     * 进化后的当前阶段
     */
    private Integer currentStage;

    /**
     * 关联的进化配置（transient，不序列化到数据库）
     */
    private transient PetEvolution evolution;

    // ─── 静态工厂方法 ─────────────────────────────────────────

    /**
     * 创建失败结果
     */
    public static PetEvolutionResult fail(String message) {
        return PetEvolutionResult.builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 创建成功结果（携带进化配置）
     */
    public static PetEvolutionResult success(String message, PetEvolution evolution) {
        return PetEvolutionResult.builder()
                .success(true)
                .message(message)
                .evolution(evolution)
                .build();
    }
}

