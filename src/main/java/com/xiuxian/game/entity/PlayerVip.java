package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 玩家VIP实体类
 */
@Data
@TableName("player_vip")
public class PlayerVip {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer playerId;
    
    private Integer vipLevel;
    
    private Integer totalRecharge;
    
    private Integer yuanbao;
    
    private LocalDateTime lastDailyRewardAt;
}
