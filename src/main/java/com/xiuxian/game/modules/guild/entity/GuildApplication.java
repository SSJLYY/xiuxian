package com.xiuxian.game.modules.guild.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宗门申请实体�?
 */
@Data
@TableName("guild_applications")
public class GuildApplication {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 宗门ID
     */
    private Integer guildId;
    
    /**
     * 玩家ID
     */
    private Integer playerId;
    
    /**
     * 状态：PENDING/APPROVED/REJECTED
     */
    private String status;
    
    /**
     * 申请留言
     */
    private String message;
    
    /**
     * 申请时间
     */
    @TableField(value = "applied_at", fill = FieldFill.INSERT)
    private LocalDateTime appliedAt;
    
    /**
     * 处理人ID
     */
    private Integer handledBy;
    
    /**
     * 处理时间
     */
    private LocalDateTime handledAt;
}

