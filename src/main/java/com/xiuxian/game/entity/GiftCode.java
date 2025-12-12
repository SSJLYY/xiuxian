package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 礼包码实体类
 */
@Data
@TableName("gift_codes")
public class GiftCode {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String code;
    
    private String name;
    
    private String codeType; // UNIVERSAL/UNIQUE
    
    private Integer maxUsage;
    
    private Integer usedCount;
    
    private Integer minLevel;
    
    private String rewards; // JSON格式
    
    private String status; // ACTIVE/DISABLED/EXPIRED
    
    private LocalDateTime expireAt;
    
    private Integer createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
