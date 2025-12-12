package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 玩家邮件实体类
 */
@Data
@TableName("player_mails")
public class PlayerMail {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer playerId;
    
    private String title;
    
    private String content;
    
    private String mailType; // SYSTEM/REWARD/ACTIVITY
    
    private Boolean isRead;
    
    private Boolean hasAttachment;
    
    private Boolean isClaimed;
    
    private LocalDateTime expireAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
