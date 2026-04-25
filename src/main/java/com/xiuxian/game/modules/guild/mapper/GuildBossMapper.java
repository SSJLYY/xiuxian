package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.GuildBoss;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 宗门BOSS Mapper
 */
@Mapper
public interface GuildBossMapper extends BaseMapper<GuildBoss> {

    @Select("SELECT * FROM guild_bosses WHERE guild_id = #{guildId} AND status = 'ALIVE' LIMIT 1")
    GuildBoss findAliveByGuildId(Integer guildId);

    @Select("SELECT * FROM guild_bosses WHERE guild_id = #{guildId} ORDER BY id DESC LIMIT 1")
    GuildBoss findLatestByGuildId(Integer guildId);

    /**
     * 原子扣减BOSS血量，防止并发挑战伤害丢失
     * @return 影响行数；bossId, damage, now(1) 为输入参数
     */
    @Update("UPDATE guild_bosses SET current_health = GREATEST(0, current_health - #{damage}), " +
            "status = CASE WHEN current_health - #{damage} <= 0 THEN 'DEFEATED' ELSE status END, " +
            "defeated_at = CASE WHEN current_health - #{damage} <= 0 THEN #{now} ELSE defeated_at END, " +
            "updated_at = #{now} " +
            "WHERE id = #{bossId} AND status = 'ALIVE'")
    int atomicDamage(@Param("bossId") Integer bossId, @Param("damage") long damage, @Param("now") LocalDateTime now);

    /**
     * 查询扣血后的BOSS状态（用于返回剩余血量等信息）
     */
    @Select("SELECT * FROM guild_bosses WHERE id = #{bossId}")
    GuildBoss selectBossById(@Param("bossId") Integer bossId);
}

