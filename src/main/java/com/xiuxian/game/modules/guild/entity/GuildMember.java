package com.xiuxian.game.modules.guild.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宗门成员实体�?
 */
@Data
@TableName("guild_members")
public class GuildMember {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 宗门ID
     */
    @TableField(value = "guild_id")
    private Integer guildId;
    
    /**
     * 玩家ID
     */
    @TableField(value = "player_id")
    private Integer playerId;
    
    /**
     * 职位：LEADER/OFFICER/MEMBER
     */
    private String role;
    
    /**
     * 贡献�?
     */
    private Integer contribution;
    
    /**
     * 加入时间
     */
    @TableField(value = "joined_at", fill = FieldFill.INSERT)
    private LocalDateTime joinedAt;
}

