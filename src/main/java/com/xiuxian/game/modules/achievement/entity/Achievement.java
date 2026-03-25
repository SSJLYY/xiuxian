package com.xiuxian.game.modules.achievement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 成就模板实体�?
 */
@Data
@TableName("achievements")
public class Achievement {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String name;
    
    private String description;
    
    @TableField(value = "achievement_type")
    private String achievementType; // LEVEL/COMBAT/CULTIVATION/COLLECTION
    
    @TableField(value = "condition_type")
    private String conditionType; // REACH_LEVEL/KILL_MONSTER/CULTIVATE_TIME
    
    @TableField(value = "condition_value")
    private Integer conditionValue;
    
    @TableField(value = "reward_exp")
    private Integer rewardExp;
    
    @TableField(value = "reward_spirit_stones")
    private Integer rewardSpiritStones;
    
    @TableField(value = "reward_title")
    private String rewardTitle;
    
    private String icon;
    
    @TableField(value = "sort_order")
    private Integer sortOrder;
}

