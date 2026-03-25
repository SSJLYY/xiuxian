package com.xiuxian.game.modules.narrative.entity;

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
 * 玩家对话状态实体类
 */
@TableName("player_dialogue_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDialogueState {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("player_id")
    private Integer playerId;

    @TableField("dialogue_tree_id")
    private Integer dialogueTreeId;

    @TableField("current_node_key")
    private String currentNodeKey;

    @TableField("is_completed")
    private Boolean isCompleted;

    @TableField("times_completed")
    private Integer timesCompleted;

    @TableField("last_choice_tag")
    private String lastChoiceTag;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("completed_at")
    private LocalDateTime completedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

