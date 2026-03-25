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
 * 玩家-NPC好感度实�?
 */
@TableName("player_npc_relations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerNpcRelation {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("player_id")
    private Integer playerId;

    @TableField("npc_id")
    private Integer npcId;

    @TableField("affinity")
    private Integer affinity;

    @TableField("relationship_level")
    private String relationshipLevel;

    @TableField("first_met_at")
    private LocalDateTime firstMetAt;

    @TableField("last_interact_at")
    private LocalDateTime lastInteractAt;

    @TableField("total_interactions")
    private Integer totalInteractions;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 关系等级常量
     */
    public static final String LEVEL_STRANGER = "陌生";
    public static final String LEVEL_ACQUAINTANCE = "认识";
    public static final String LEVEL_FAMILIAR = "熟悉";
    public static final String LEVEL_TRUST = "信任";
    public static final String LEVEL_SOULMATE = "至交";

    /**
     * 根据好感度获取关系等�?
     */
    public static String getRelationshipLevel(int affinity) {
        if (affinity >= 81) return LEVEL_SOULMATE;
        if (affinity >= 61) return LEVEL_TRUST;
        if (affinity >= 41) return LEVEL_FAMILIAR;
        if (affinity >= 21) return LEVEL_ACQUAINTANCE;
        return LEVEL_STRANGER;
    }
}

