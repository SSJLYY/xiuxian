package com.xiuxian.game.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日统计数据实体
 *
 * @author shaun.sheng
 */
@Data
@TableName("daily_statistics")
public class DailyStatistics {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private LocalDate statDate;

    private Integer newPlayers;

    private Integer activePlayers;

    private Integer totalRecharge;

    private Integer payingPlayers;

    private BigDecimal arpu;

    private BigDecimal arppu;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
