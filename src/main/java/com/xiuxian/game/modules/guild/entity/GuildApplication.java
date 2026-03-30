package com.xiuxian.game.modules.guild.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宗门申请实体类
 *
 * <p>表示玩家申请加入宗门的记录</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>申请管理 - 记录玩家的宗门加入申请</li>
 *   <li>状态跟踪 - 跟踪申请的处理状态</li>
 *   <li>审批记录 - 记录审批人和审批时间</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see Guild
 * @see GuildMember
 */
@Data
@TableName("guild_applications")
public class GuildApplication {
    
    /** 申请记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 宗门ID，关联guilds表 */
    private Integer guildId;
    
    /** 玩家ID，关联player_profiles表 */
    private Integer playerId;
    
    /**
     * 申请状态
     * <ul>
     *   <li>PENDING - 待审批</li>
     *   <li>APPROVED - 已批准</li>
     *   <li>REJECTED - 已拒绝</li>
     * </ul>
     */
    private String status;
    
    /** 申请留言，玩家申请时填写的留言 */
    private String message;
    
    /** 申请时间，玩家提交申请的时间 */
    @TableField(value = "applied_at", fill = FieldFill.INSERT)
    private LocalDateTime appliedAt;
    
    /** 处理人ID，审批此申请的管理员ID */
    private Integer handledBy;
    
    /** 处理时间，申请被审批的时间 */
    private LocalDateTime handledAt;
}

