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
 * 对话树实体
 */
@TableName("dialogue_trees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueTree {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("npc_id")
    private Integer npcId;

    @TableField("dialogue_key")
    private String dialogueKey;

    @TableField("title")
    private String title;

    @TableField("scene")
    private String scene;

    @TableField("mood")
    private String mood;

    @TableField("min_level")
    private Integer minLevel;

    @TableField("max_level")
    private Integer maxLevel;

    @TableField("required_realm")
    private String requiredRealm;

    @TableField("required_quest_chain_id")
    private Integer requiredQuestChainId;

    @TableField("required_flags")
    private String requiredFlags;

    @TableField("is_repeatable")
    private Boolean isRepeatable;

    @TableField("priority")
    private Integer priority;

    @TableField("active")
    private Boolean active;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
