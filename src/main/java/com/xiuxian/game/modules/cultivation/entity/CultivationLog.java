package com.xiuxian.game.modules.cultivation.entity;

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
 * 修炼日志实体类
 *
 * <p>记录玩家每次修炼的收益信息，包括在线修炼和离线修炼。</p>
 *
 * <p>主要字段说明：</p>
 * <ul>
 *   <li>playerId - 玩家ID，关联玩家表</li>
 *   <li>expGained - 本次修炼获得的经验值</li>
 *   <li>spiritStonesGained - 本次修炼获得的灵石数量</li>
 *   <li>cultivationDuration - 修炼时长（毫秒）</li>
 *   <li>isOffline - 是否为离线修炼，true表示离线修炼，false表示在线修炼</li>
 *   <li>createdAt - 记录创建时间</li>
 * </ul>
 *
 * <p>业务场景：</p>
 * <ul>
 *   <li>在线修炼：玩家主动点击修炼按钮，实时获得收益</li>
 *   <li>离线修炼：玩家离线期间，系统自动计算修炼收益</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see com.xiuxian.game.modules.cultivation.mapper.CultivationLogMapper
 */
@TableName("cultivation_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CultivationLog {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 玩家ID
     *
     * <p>关联玩家表的主键，用于标识是哪个玩家的修炼记录。</p>
     */
    @TableField(value = "player_id")
    private Integer playerId;

    /**
     * 获得的经验值
     *
     * <p>本次修炼获得的经验值，用于提升玩家等级。</p>
     * <p>经验值计算公式：基础收益 * 修炼时长 * 效率加成</p>
     */
    @TableField(value = "exp_gained")
    private Long expGained;

    /**
     * 获得的灵石数量
     *
     * <p>本次修炼获得的灵石，是游戏中的主要货币。</p>
     * <p>灵石可用于购买装备、技能书等道具。</p>
     */
    @TableField(value = "spirit_stones_gained")
    private Integer spiritStonesGained;

    /**
     * 修炼时长（毫秒）
     *
     * <p>本次修炼的持续时间，单位为毫秒。</p>
     * <p>用于计算收益和统计玩家修炼时长。</p>
     */
    @TableField(value = "cultivation_duration")
    private Long cultivationDuration;

    /**
     * 是否为离线修炼
     *
     * <p>true表示离线修炼，false表示在线修炼。</p>
     * <p>离线修炼的收益计算可能与在线修炼有所不同。</p>
     */
    @TableField(value = "is_offline")
    @Builder.Default
    private Boolean isOffline = false;

    /**
     * 记录创建时间
     *
     * <p>修炼记录的创建时间，用于数据统计和查询。</p>
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
}
