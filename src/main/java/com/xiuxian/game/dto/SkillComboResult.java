package com.xiuxian.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 技能连招结果DTO
 * 用于返回连招触发结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillComboResult {

    /**
     * 是否触发连招
     */
    private boolean triggered;

    /**
     * 触发的连招名称
     */
    private String comboName;

    /**
     * 连招描述
     */
    private String comboDescription;

    /**
     * 伤害加成百分比
     */
    private double bonusPercent;

    /**
     * 额外伤害值
     */
    private int bonusDamage;

    /**
     * 连招加成后的最终伤害
     */
    private int finalDamage;

    /**
     * 连招图标类型（用于前端显示）
     */
    private String comboIcon;
}
