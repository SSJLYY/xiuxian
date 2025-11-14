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

@TableName("cultivation_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CultivationLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_id")
    private Integer playerId;

    @TableField(value = "exp_gained")
    private Long expGained;

    @TableField(value = "spirit_stones_gained")
    private Integer spiritStonesGained;

    @TableField(value = "cultivation_duration")
    private Long cultivationDuration;

    @TableField(value = "is_offline")
    @Builder.Default
    private Boolean isOffline = false;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;
}