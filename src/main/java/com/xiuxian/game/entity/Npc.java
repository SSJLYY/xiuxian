package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NPC基础数据实体
 */
@TableName("npcs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Npc {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("title")
    private String title;

    @TableField("faction")
    private String faction;

    @TableField("role_type")
    private String roleType;

    @TableField("description")
    private String description;

    @TableField("personality_traits")
    private String personalityTraits;

    @TableField("location")
    private String location;

    @TableField("min_level")
    private Integer minLevel;

    @TableField("icon")
    private String icon;

    @TableField("active")
    private Boolean active;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
