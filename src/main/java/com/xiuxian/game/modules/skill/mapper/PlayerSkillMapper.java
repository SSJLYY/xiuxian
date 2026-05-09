package com.xiuxian.game.modules.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.skill.entity.PlayerSkill;
import org.apache.ibatis.annotations.Insert;
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

    @Select("SELECT * FROM player_skills WHERE id = #{id} FOR UPDATE")
    PlayerSkill selectByIdForUpdate(@Param("id") Integer id);

    @Select("SELECT * FROM player_skills WHERE player_id = #{playerId} FOR UPDATE")
    List<PlayerSkill> selectByPlayerIdForUpdate(@Param("playerId") Integer playerId);

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

    @Insert("INSERT INTO player_skills (player_id, skill_id, level, experience, equipped, slot_number, created_at, updated_at) " +
            "SELECT #{playerId}, #{skillId}, #{level}, #{experience}, #{equipped}, #{slotNumber}, #{createdAt}, #{updatedAt} FROM DUAL " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM player_skills WHERE player_id = #{playerId} AND skill_id = #{skillId}" +
            ")")
    int insertIfAbsent(@Param("playerId") Integer playerId,
                       @Param("skillId") Integer skillId,
                       @Param("level") Integer level,
                       @Param("experience") Integer experience,
                       @Param("equipped") Boolean equipped,
                       @Param("slotNumber") Integer slotNumber,
                       @Param("createdAt") java.time.LocalDateTime createdAt,
                       @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
