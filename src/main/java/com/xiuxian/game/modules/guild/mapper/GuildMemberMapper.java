package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.GuildMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 宗门成员数据访问层
 *
 * <p>提供宗门成员相关的数据库操作，继承MyBatis-Plus的BaseMapper</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>基础CRUD操作 - 继承BaseMapper提供的增删改查</li>
 *   <li>原子操作 - 贡献值增加等并发安全操作</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see GuildMember
 */
@Mapper
public interface GuildMemberMapper extends BaseMapper<GuildMember> {

    /**
     * 原子增加成员贡献值
     *
     * <p>使用原子操作增加成员贡献值，防止并发问题</p>
     *
     * @param memberId 成员记录ID
     * @param amount 增加的贡献值
     * @return 影响行数
     */
    @Update("UPDATE guild_members SET contribution = contribution + #{amount} WHERE id = #{memberId}")
    int addContribution(@Param("memberId") Long memberId, @Param("amount") int amount);
}

