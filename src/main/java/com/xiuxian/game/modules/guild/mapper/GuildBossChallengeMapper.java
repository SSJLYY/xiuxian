package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.GuildBossChallenge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /**
     * 批量更新 personal_reward_stones（性能优化：避免循环updateById）
     *
     * @param list 已计算好 personalRewardStones 的挑战记录列表
     */
    @Update("<script>" +
            "UPDATE guild_boss_challenges " +
            "SET personal_reward_stones = CASE id " +
            "<foreach collection='list' item='c'>" +
            "WHEN #{c.id} THEN #{c.personalRewardStones} " +
            "</foreach>" +
            "END " +
            "WHERE id IN " +
            "<foreach collection='list' item='c' open='(' separator=',' close=')'>" +
            "#{c.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateRewardStones(@Param("list") List<GuildBossChallenge> list);

    @Update("UPDATE guild_boss_challenges " +
            "SET reward_claimed = true, updated_at = NOW(), version = version + 1 " +
            "WHERE id = #{id} AND reward_claimed = false")
    int markRewardClaimed(@Param("id") Integer id);
}

