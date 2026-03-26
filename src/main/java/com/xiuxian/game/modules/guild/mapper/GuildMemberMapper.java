package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.GuildMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GuildMemberMapper extends BaseMapper<GuildMember> {

    /**
     * 原子增加成员贡献值
     * @return 影响行数
     */
    @Update("UPDATE guild_members SET contribution = contribution + #{amount} WHERE id = #{memberId}")
    int addContribution(@Param("memberId") Long memberId, @Param("amount") int amount);
}

