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
 * 玩家叙事标记（flag系统）实体
 */
@TableName("player_narrative_flags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerNarrativeFlag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("player_id")
    private Integer playerId;

    @TableField("flag_key")
    private String flagKey;

    @TableField("flag_value")
    private String flagValue;

    @TableField("source")
    private String source;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
