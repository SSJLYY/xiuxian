package com.xiuxian.game.modules.player.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 玩家登录日志实体类
 */
@Data
@TableName("player_login_logs")
public class PlayerLoginLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer playerId;
    
    private String ipAddress;
    
    private String deviceInfo;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime loginAt;
}
