package com.xiuxian.game.modules.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 技能连招实体类
 * 定义技能组合及其加成效果
 */
@Data
@TableName("skill_combos")
public class SkillCombo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 连招名称
     */
    private String name;

    /**
     * 连招描述
     */
    private String description;

    /**
     * 技能序列（JSON数组格式，如：[4, 2] 表示先使用技能4再使用技能2）
     */
    private String skillSequence;

    /**
     * 连招加成百分比（如：50.00 表示增加50%伤害）
     */
    private BigDecimal comboBonus;

    /**
     * 需求等级
     */
    private Integer requiredLevel;

    /**
     * 是否启用
     */
    private Boolean active;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
