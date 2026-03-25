package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.GuildBoss;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 宗门BOSS Mapper
 */
@Mapper
public interface GuildBossMapper extends BaseMapper<GuildBoss> {

    @Select("SELECT * FROM guild_bosses WHERE guild_id = #{guildId} AND status = 'ALIVE' LIMIT 1")
    GuildBoss findAliveByGuildId(Integer guildId);
}

