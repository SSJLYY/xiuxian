package com.xiuxian.game.modules.giftcode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 礼包码使用记录实体类
 *
 * <p>记录玩家使用礼包码的历史，用于追踪和防止重复使用。</p>
 *
 * <p>主要用途：</p>
 * <ul>
 *   <li>记录哪个玩家在什么时间使用了哪个礼包码</li>
 *   <li>用于UNIQUE类型礼包码的唯一性校验</li>
 *   <li>用于管理员查看礼包码的使用情况</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see GiftCode
 */
@Data
@TableName("gift_code_usage")
public class GiftCodeUsage {
    
    /** 使用记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 礼包码ID，关联gift_codes表 */
    private Long giftCodeId;
    
    /** 玩家ID，关联player_profiles表 */
    private Integer playerId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime usedAt;
}

