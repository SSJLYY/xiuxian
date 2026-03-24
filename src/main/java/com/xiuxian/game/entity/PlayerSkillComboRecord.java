package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 玩家技能连招记录实体
 * 追踪玩家最近的技能使用序列，用于检测连招触发
 */
@Data
@TableName("player_skill_combo_records")
public class PlayerSkillComboRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 玩家ID
     */
    private Integer playerId;

    /**
     * 技能ID
     */
    private Integer skillId;

    /**
     * 使用时间
     */
    private LocalDateTime usedAt;

    /**
     * 是否触发连招
     */
    private Boolean triggeredCombo;

    /**
     * 触发的连招ID
     */
    private Integer comboId;
}
