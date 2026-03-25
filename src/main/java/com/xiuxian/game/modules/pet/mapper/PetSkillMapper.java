package com.xiuxian.game.modules.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.pet.entity.PetSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PetSkillMapper extends BaseMapper<PetSkill> {

    /**
     * 根据技能类型查询
     */
    @Select("SELECT * FROM pet_skills WHERE skill_type = #{skillType} AND active = true")
    List<PetSkill> selectByType(@Param("skillType") String skillType);

    /**
     * 查询宠物可学习的技能
     */
    @Select("SELECT * FROM pet_skills WHERE unlock_pet_level <= #{petLevel} AND active = true")
    List<PetSkill> selectAvailableSkills(@Param("petLevel") Integer petLevel);
}
