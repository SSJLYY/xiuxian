package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.GuildMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GuildMemberMapper extends BaseMapper<GuildMember> {
}

