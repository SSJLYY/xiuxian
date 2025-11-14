package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.PlayerSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerSkillMapper extends BaseMapper<PlayerSkill> {

    @Select("SELECT * FROM player_skills WHERE player_id = #{playerId}")
    List<PlayerSkill> selectByPlayerId(@Param("playerId") Integer playerId);

    @Select("SELECT * FROM player_skills WHERE player_id = #{playerId} AND equipped = #{equipped}")
    List<PlayerSkill> selectByPlayerIdAndEquipped(@Param("playerId") Integer playerId, @Param("equipped") Boolean equipped);

    @Select("SELECT * FROM player_skills WHERE player_id = #{playerId} AND skill_id = #{skillId} LIMIT 1")
    PlayerSkill selectByPlayerIdAndSkillId(@Param("playerId") Integer playerId, @Param("skillId") Integer skillId);
    
    /**
     * 查询玩家的所有技能
     */
    @Select("SELECT * FROM player_skills WHERE player_id = #{playerId}")
    List<PlayerSkill> findByPlayerId(@Param("playerId") Integer playerId);
    
    /**
     * 查询玩家的指定技能
     */
    @Select("SELECT * FROM player_skills WHERE player_id = #{playerId} AND skill_id = #{skillId}")
    PlayerSkill findByPlayerIdAndSkillId(@Param("playerId") Integer playerId, @Param("skillId") Integer skillId);
    
    /**
     * 查询玩家已装备的技能
     */
    @Select("SELECT * FROM player_skills WHERE player_id = #{playerId} AND equipped = true")
    List<PlayerSkill> findEquippedSkills(@Param("playerId") Integer playerId);
}


