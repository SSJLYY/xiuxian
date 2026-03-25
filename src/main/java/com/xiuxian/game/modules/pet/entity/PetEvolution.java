package com.xiuxian.game.modules.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物进化实体
 * 定义宠物的进化阶段和进化条件
 */
@Data
@TableName("pet_evolution")
public class PetEvolution {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 宠物模板ID
     */
    private Integer petId;

    /**
     * 进化阶段（1=第一次进化，3=第二次进化）
     */
    private Integer evolutionStage;

    /**
     * 进化名称
     */
    private String evolutionName;

    /**
     * 需求等级
     */
    private Integer requiredLevel;

    /**
     * 需求物品ID（进化道具）
     */
    private Integer requiredItemId;

    /**
     * 需求物品数量
     */
    private Integer requiredItemQuantity;

    /**
     * 生命加成
     */
    private Integer healthBonus;

    /**
     * 攻击加成
     */
    private Integer attackBonus;

    /**
     * 防御加成
     */
    private Integer defenseBonus;

    /**
     * 速度加成
     */
    private Integer speedBonus;

    /**
     * 新能力ID
     */
    private Integer newAbilityId;

    /**
     * 外观变化（JSON格式）
     */
    private String appearanceChange;
}
