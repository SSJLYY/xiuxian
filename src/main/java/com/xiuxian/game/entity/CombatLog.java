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

@TableName("combat_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombatLog {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_id")
    private Integer playerId;

    @TableField(value = "monster_id")
    private Integer monsterId;

    @TableField(value = "result")
    private String result; // WIN, LOSE

    @TableField(value = "rounds")
    @Builder.Default
    private Integer rounds = 0;

    @TableField(value = "exp_gained")
    @Builder.Default
    private Integer expGained = 0;

    @TableField(value = "spirit_stones_gained")
    @Builder.Default
    private Integer spiritStonesGained = 0;

    @TableField(value = "equipment_dropped")
    private Integer equipmentDropped;

    @TableField(value = "battle_details")
    private String battleDetails; // JSON格式的详细战斗记录

    @TableField(value = "created_at")
    private LocalDateTime createdAt;
}
