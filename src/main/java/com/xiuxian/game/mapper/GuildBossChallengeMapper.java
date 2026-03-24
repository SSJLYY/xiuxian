package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.GuildBossChallenge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 宗门BOSS挑战记录 Mapper
 */
@Mapper
public interface GuildBossChallengeMapper extends BaseMapper<GuildBossChallenge> {

    @Select("SELECT * FROM guild_boss_challenges WHERE boss_id = #{bossId} AND player_id = #{playerId} LIMIT 1")
    GuildBossChallenge findByBossAndPlayer(Integer bossId, Integer playerId);

    /** 按伤害排序的参战成员列表（BOSS讨伐贡献榜） */
    @Select("SELECT * FROM guild_boss_challenges WHERE boss_id = #{bossId} ORDER BY damage_dealt DESC")
    List<GuildBossChallenge> findByBossIdOrderByDamage(Integer bossId);
}
