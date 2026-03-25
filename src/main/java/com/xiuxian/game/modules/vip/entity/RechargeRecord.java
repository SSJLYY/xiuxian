package com.xiuxian.game.modules.vip.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 充值记录实体类
 */
@Data
@TableName("recharge_records")
public class RechargeRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer playerId;
    
    private Integer amount;
    
    private Integer yuanbao;
    
    private String orderNo;
    
    private String status; // PENDING/SUCCESS/FAILED
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
}

