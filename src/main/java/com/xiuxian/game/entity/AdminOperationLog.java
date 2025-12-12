package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员操作日志实体类
 */
@Data
@TableName("admin_operation_logs")
public class AdminOperationLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer adminId;
    
    private String operationType;
    
    private String targetType;
    
    private String targetId;
    
    private String operationDetail;
    
    private String ipAddress;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
