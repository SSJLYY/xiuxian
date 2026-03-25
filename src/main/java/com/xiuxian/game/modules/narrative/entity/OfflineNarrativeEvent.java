package com.xiuxian.game.modules.narrative.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 离线事件叙事实体
 */
@TableName("offline_narrative_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineNarrativeEvent {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("event_key")
    private String eventKey;

    @TableField("title")
    private String title;

    @TableField("narrative")
    private String narrative;

    @TableField("probability")
    private BigDecimal probability;

    @TableField("min_offline_hours")
    private Integer minOfflineHours;

    @TableField("max_offline_hours")
    private Integer maxOfflineHours;

    @TableField("min_realm")
    private String minRealm;

    @TableField("min_level")
    private Integer minLevel;

    @TableField("reward_type")
    private String rewardType;

    @TableField("reward_data")
    private String rewardData;

    @TableField("set_flag")
    private String setFlag;

    @TableField("unlock_dialogue_key")
    private String unlockDialogueKey;

    @TableField("npc_relation_change")
    private String npcRelationChange;

    @TableField("active")
    private Boolean active;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

