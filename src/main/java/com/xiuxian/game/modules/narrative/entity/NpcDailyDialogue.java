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
 * NPC日常对话池实�?
 */
@TableName("npc_daily_dialogues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NpcDailyDialogue {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("npc_id")
    private Integer npcId;

    @TableField("text")
    private String text;

    @TableField("conditions")
    private String conditions;

    @TableField("priority")
    private Integer priority;

    @TableField("active")
    private Boolean active;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

