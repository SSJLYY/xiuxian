package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.PlayerPetSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerPetSkillMapper extends BaseMapper<PlayerPetSkill> {

    /**
     * 查询宠物的所有技能
     */
    @Select("SELECT * FROM player_pet_skills WHERE player_pet_id = #{playerPetId}")
    List<PlayerPetSkill> selectByPlayerPetId(@Param("playerPetId") Integer playerPetId);

    /**
     * 查询宠物是否已学习某技能
     */
    @Select("SELECT * FROM player_pet_skills WHERE player_pet_id = #{playerPetId} AND pet_skill_id = #{petSkillId} LIMIT 1")
    PlayerPetSkill selectByPlayerPetIdAndSkillId(@Param("playerPetId") Integer playerPetId, @Param("petSkillId") Integer petSkillId);
}
