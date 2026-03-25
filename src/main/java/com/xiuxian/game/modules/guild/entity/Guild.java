package com.xiuxian.game.modules.guild.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宗门实体�?
 */
@Data
@TableName("guilds")
public class Guild {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 宗门名称
     */
    @TableField(value = "guild_name")
    private String guildName;
    
    /**
     * 宗门简�?
     */
    private String description;
    
    /**
     * 宗主ID
     */
    @TableField(value = "leader_id")
    private Integer leaderId;
    
    /**
     * 宗门等级
     */
    private Integer level;
    
    /**
     * 宗门经验
     */
    private Long exp;
    
    /**
     * 升级所需经验
     */
    @TableField(value = "exp_to_next")
    private Long expToNext;
    
    /**
     * 宗门资金
     */
    @TableField(value = "guild_funds")
    private Long guildFunds;
    
    /**
     * 成员数量
     */
    @TableField(value = "member_count")
    private Integer memberCount;
    
    /**
     * 最大成员数
     */
    @TableField(value = "max_members")
    private Integer maxMembers;
    
    /**
     * 宗门公告
     */
    private String announcement;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

