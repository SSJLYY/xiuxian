package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 礼包码使用记录实体类
 */
@Data
@TableName("gift_code_usage")
public class GiftCodeUsage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long giftCodeId;
    
    private Integer playerId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime usedAt;
}
