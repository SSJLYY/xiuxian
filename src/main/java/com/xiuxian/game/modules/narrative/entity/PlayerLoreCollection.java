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
 * 玩家传说收集记录实体
 */
@TableName("player_lore_collection")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLoreCollection {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("player_id")
    private Integer playerId;

    @TableField("lore_entry_id")
    private Integer loreEntryId;

    @TableField("discovered_at")
    private LocalDateTime discoveredAt;

    @TableField("source")
    private String source;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

