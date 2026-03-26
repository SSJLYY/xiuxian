package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.Guild;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GuildMapper extends BaseMapper<Guild> {

    /**
     * 原子增加宗门成员数量
     * @return 影响行数
     */
    @Update("UPDATE guilds SET member_count = member_count + 1, updated_at = NOW() WHERE id = #{guildId} AND member_count < max_members")
    int incrementMemberCount(@Param("guildId") Integer guildId);

    /**
     * 原子减少宗门成员数量
     * @return 影响行数
     */
    @Update("UPDATE guilds SET member_count = GREATEST(0, member_count - 1), updated_at = NOW() WHERE id = #{guildId}")
    int decrementMemberCount(@Param("guildId") Integer guildId);

    /**
     * 原子增加宗门资金
     * @return 影响行数
     */
    @Update("UPDATE guilds SET guild_funds = guild_funds + #{amount}, updated_at = NOW() WHERE id = #{guildId}")
    int addGuildFunds(@Param("guildId") Integer guildId, @Param("amount") long amount);
}

