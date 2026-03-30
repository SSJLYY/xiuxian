package com.xiuxian.game.modules.guild.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宗门成员实体类
 *
 * <p>表示宗门中的成员关系，记录玩家在宗门中的角色和贡献</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>成员管理 - 记录玩家与宗门的关联</li>
 *   <li>角色管理 - 宗主、长老、成员等不同职位</li>
 *   <li>贡献统计 - 记录玩家对宗门的贡献值</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see Guild
 */
@Data
@TableName("guild_members")
public class GuildMember {

    /** 成员记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 宗门ID，关联guilds表 */
    @TableField(value = "guild_id")
    private Integer guildId;

    /** 玩家ID，关联player_profiles表 */
    @TableField(value = "player_id")
    private Integer playerId;

    /**
     * 职位
     * <ul>
     *   <li>LEADER - 宗主，拥有宗门所有权限</li>
     *   <li>OFFICER - 长老，可以审批申请和管理成员</li>
     *   <li>MEMBER - 普通成员，可以参与宗门活动</li>
     * </ul>
     */
    private String role;

    /** 贡献值，通过捐献和参与宗门活动获得 */
    private Integer contribution;

    /** 加入时间，玩家加入宗门的时间 */
    @TableField(value = "joined_at", fill = FieldFill.INSERT)
    private LocalDateTime joinedAt;
}
