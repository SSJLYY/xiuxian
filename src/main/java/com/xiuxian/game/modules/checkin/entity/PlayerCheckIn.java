package com.xiuxian.game.modules.checkin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 每日签到记录
 */
@Data
@TableName("player_check_ins")
public class PlayerCheckIn {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 玩家ID */
    @TableField(value = "player_id")
    private Integer playerId;

    /** 签到日期（yyyy-MM-dd 格式存储�?LocalDateTime 00:00:00�?*/
    @TableField(value = "check_in_date")
    private LocalDateTime checkInDate;

    /** 连续签到天数（签到当天计算） */
    @TableField(value = "consecutive_days")
    private Integer consecutiveDays;

    /** 本次签到获得灵石 */
    @TableField(value = "reward_spirit_stones")
    private Integer rewardSpiritStones;

    /** 本次签到获得经验 */
    @TableField(value = "reward_exp")
    private Integer rewardExp;

    /** 是否补签（消耗补签卡�?*/
    @TableField(value = "is_makeup")
    private Boolean isMakeup;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

