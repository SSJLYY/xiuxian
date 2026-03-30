package com.xiuxian.game.modules.combat.entity;

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
 * 战斗日志实体
 *
 * <p>记录每次战斗的结果、回合数、奖励等信息，用于战斗历史查询和统计分析。</p>
 *
 * <p>战斗结果类型：</p>
 * <ul>
 *   <li>WIN - 玩家胜利</li>
 *   <li>LOSE - 玩家失败</li>
 * </ul>
 *
 * <p>记录内容包括：</p>
 * <ul>
 *   <li>玩家ID和怪物ID</li>
 *   <li>战斗结果（胜利/失败）</li>
 *   <li>战斗回合数</li>
 *   <li>获得的经验值</li>
 *   <li>获得的灵石</li>
 *   <li>掉落的装备ID</li>
 *   <li>详细战斗记录（JSON格式）</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
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
