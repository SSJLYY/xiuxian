package com.xiuxian.game.modules.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("skill_shop")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillShopItem {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("skill_id")
    private Integer skillId;

    @TableField("price")
    private Integer price;

    @TableField("required_level")
    private Integer requiredLevel;

    @TableField("available")
    private Boolean available;
}
