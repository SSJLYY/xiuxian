package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.GuildApplication;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 宗门申请数据访问层
 *
 * <p>提供宗门申请相关的数据库操作，继承MyBatis-Plus的BaseMapper</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>基础CRUD操作 - 继承BaseMapper提供的增删改查</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see GuildApplication
 */
@Mapper
public interface GuildApplicationMapper extends BaseMapper<GuildApplication> {

    @Insert("INSERT INTO guild_applications (guild_id, player_id, status, applied_at) " +
            "SELECT #{guildId}, #{playerId}, 'PENDING', #{appliedAt} FROM DUAL " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM guild_applications " +
            "WHERE guild_id = #{guildId} AND player_id = #{playerId} AND status = 'PENDING'" +
            ")")
    int insertPendingIfAbsent(@Param("guildId") Integer guildId,
                              @Param("playerId") Integer playerId,
                              @Param("appliedAt") java.time.LocalDateTime appliedAt);

    @Update("UPDATE guild_applications " +
            "SET status = #{status}, handled_by = #{handledBy}, handled_at = #{handledAt} " +
            "WHERE id = #{applicationId} AND status = 'PENDING'")
    int handlePendingApplication(@Param("applicationId") Long applicationId,
                                 @Param("status") String status,
                                 @Param("handledBy") Integer handledBy,
                                 @Param("handledAt") java.time.LocalDateTime handledAt);
}

