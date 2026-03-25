package com.xiuxian.game.modules.vip.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * VIP等级配置实体类
 */
@Data
@TableName("vip_levels")
public class VipLevel {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer level;
    
    private Integer requiredRecharge;
    
    private Integer dailySpiritStones;
    
    private BigDecimal cultivationSpeedBonus;
    
    private BigDecimal expBonus;
    
    private BigDecimal shopDiscount;
}

