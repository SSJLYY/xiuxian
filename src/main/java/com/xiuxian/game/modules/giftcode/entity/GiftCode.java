package com.xiuxian.game.modules.giftcode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 礼包码实体类
 *
 * <p>用于管理游戏中的礼包码，支持通用码和唯一码两种类型。</p>
 *
 * <p>礼包码类型说明：</p>
 * <ul>
 *   <li>UNIVERSAL - 通用码，所有玩家都可以使用，有总次数限制</li>
 *   <li>UNIQUE - 唯一码，每个玩家只能使用一次</li>
 * </ul>
 *
 * <p>礼包码状态说明：</p>
 * <ul>
 *   <li>ACTIVE - 激活状态，可以正常使用</li>
 *   <li>DISABLED - 禁用状态，已被使用完或手动禁用</li>
 *   <li>EXPIRED - 过期状态，已超过有效期</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see GiftCodeUsage
 */
@Data
@TableName("gift_codes")
public class GiftCode {
    
    /** 礼包码ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 礼包码字符串，玩家输入的兑换码 */
    private String code;
    
    /** 礼包码名称，用于管理后台显示 */
    private String name;
    
    /**
     * 礼包码类型
     * <ul>
     *   <li>UNIVERSAL - 通用码，所有玩家都可以使用，有总次数限制</li>
     *   <li>UNIQUE - 唯一码，每个玩家只能使用一次</li>
     * </ul>
     */
    private String codeType; // UNIVERSAL/UNIQUE
    
    /** 最大使用次数，UNIVERSAL类型时有效 */
    private Integer maxUsage;
    
    /** 已使用次数 */
    private Integer usedCount;
    
    /** 最低等级要求，玩家等级需达到此值才能使用 */
    private Integer minLevel;
    
    /**
     * 奖励内容，JSON格式
     * <p>示例：[{"type":"ITEM","id":1,"quantity":10},{"type":"EQUIPMENT","id":5,"quantity":1}]</p>
     */
    private String rewards; // JSON格式
    
    /**
     * 礼包码状态
     * <ul>
     *   <li>ACTIVE - 激活状态，可以正常使用</li>
     *   <li>DISABLED - 禁用状态，已被使用完或手动禁用</li>
     *   <li>EXPIRED - 过期状态，已超过有效期</li>
     * </ul>
     */
    private String status; // ACTIVE/DISABLED/EXPIRED
    
    /** 过期时间，超过此时间后礼包码将自动变为EXPIRED状态 */
    private LocalDateTime expireAt;
    
    /** 创建者ID，管理员ID */
    private Integer createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

