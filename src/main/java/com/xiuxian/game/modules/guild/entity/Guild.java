package com.xiuxian.game.modules.guild.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宗门实体类
 *
 * <p>表示游戏中的宗门组织，玩家可以创建或加入宗门</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>宗门管理 - 创建、升级、解散宗门</li>
 *   <li>成员管理 - 添加、移除成员，设置职位</li>
 *   <li>资源管理 - 宗门资金、经验、等级</li>
 *   <li>公告管理 - 发布宗门公告</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see GuildMember
 * @see GuildApplication
 */
@Data
@TableName("guilds")
public class Guild {

    /** 宗门ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 宗门名称，玩家创建宗门时设置 */
    @TableField(value = "guild_name")
    private String guildName;

    /** 宗门简介，描述宗门的宗旨和特色 */
    private String description;

    /** 宗主ID，创建宗门的玩家ID */
    @TableField(value = "leader_id")
    private Integer leaderId;

    /** 宗门等级，影响宗门功能和成员上限 */
    private Integer level;

    /** 宗门经验，通过成员捐献和活动获得 */
    private Long exp;

    /** 升级所需经验，达到此经验后宗门可升级 */
    @TableField(value = "exp_to_next")
    private Long expToNext;

    /** 宗门资金，通过成员捐献获得，用于宗门建设 */
    @TableField(value = "guild_funds")
    private Long guildFunds;

    /** 成员数量，当前宗门的成员总数 */
    @TableField(value = "member_count")
    private Integer memberCount;

    /** 最大成员数，宗门等级越高，可容纳的成员越多 */
    @TableField(value = "max_members")
    private Integer maxMembers;

    /** 宗门公告，宗主和管理员可发布宗门公告 */
    private String announcement;

    /** 创建时间，宗门创建的时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，宗门信息最后更新的时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 乐观锁版本号，用于并发控制 */
    @Version
    private Integer version;
}
