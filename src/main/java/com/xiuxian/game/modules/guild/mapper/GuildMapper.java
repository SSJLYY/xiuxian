package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.Guild;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 宗门数据访问层
 *
 * <p>提供宗门相关的数据库操作，继承MyBatis-Plus的BaseMapper</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>基础CRUD操作 - 继承BaseMapper提供的增删改查</li>
 *   <li>原子操作 - 成员数量增减、资金增加等并发安全操作</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see Guild
 */
@Mapper
public interface GuildMapper extends BaseMapper<Guild> {

    /**
     * 原子增加宗门成员数量
     *
     * <p>使用原子操作增加宗门成员数量，防止并发问题</p>
     *
     * @param guildId 宗门ID
     * @return 影响行数，0表示宗门已满或不存在
     */
    @Update("UPDATE guilds SET member_count = member_count + 1, updated_at = NOW() WHERE id = #{guildId} AND member_count < max_members")
    int incrementMemberCount(@Param("guildId") Integer guildId);

    /**
     * 原子减少宗门成员数量
     *
     * <p>使用原子操作减少宗门成员数量，防止并发问题</p>
     *
     * @param guildId 宗门ID
     * @return 影响行数
     */
    @Update("UPDATE guilds SET member_count = GREATEST(0, member_count - 1), updated_at = NOW() WHERE id = #{guildId}")
    int decrementMemberCount(@Param("guildId") Integer guildId);

    /**
     * 原子增加宗门资金
     *
     * <p>使用原子操作增加宗门资金，防止并发问题</p>
     *
     * @param guildId 宗门ID
     * @param amount 增加金额
     * @return 影响行数
     */
    @Update("UPDATE guilds SET guild_funds = guild_funds + #{amount}, updated_at = NOW() WHERE id = #{guildId}")
    int addGuildFunds(@Param("guildId") Integer guildId, @Param("amount") long amount);
}

