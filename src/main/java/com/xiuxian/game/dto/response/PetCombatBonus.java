package com.xiuxian.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 宠物参战增益 DTO
 * GDD设计：宠物在战斗中提供额外伤害和技能支持
 * 
 * 设计原则：
 * - 忠诚度影响技能发动概率
 * - 饱食度影响参战效果
 * - 存在"共鸣"高光时刻
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetCombatBonus {
    
    /**
     * 宠物ID
     */
    private Integer petId;
    
    /**
     * 宠物名称
     */
    private String petName;
    
    /**
     * 技能发动概率（0.0-1.0）
     */
    private double skillTriggerChance;
    
    /**
     * 技能伤害
     */
    private int skillDamage;
    
    /**
     * 技能触发间隔（回合数）
     */
    private int skillCooldown;
    
    /**
     * 是否触发共鸣（高光时刻，伤害×2）
     */
    private boolean resonance;
    
    /**
     * 饱食度因子（<20时为0.5，否则为1.0）
     */
    private double hungerFactor;
    
    /**
     * 当前忠诚度
     */
    private int loyalty;
    
    /**
     * 当前饱食度
     */
    private int hunger;
    
    /**
     * 是否有参战资格（饱食度>0）
     */
    public boolean isEligible() {
        return hunger > 0;
    }
}
