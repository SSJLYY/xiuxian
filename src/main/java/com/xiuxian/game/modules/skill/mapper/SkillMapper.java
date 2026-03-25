package com.xiuxian.game.modules.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.skill.entity.Skill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SkillMapper extends BaseMapper<Skill> {

    @Select("SELECT * FROM skills WHERE unlock_level <= #{level}")
    List<Skill> selectByUnlockLevelLessThanEqual(@Param("level") Integer level);
    
    /**
     * 查询玩家可用的技�?
     */
    @Select("SELECT * FROM skills WHERE unlock_level <= #{playerLevel} AND active = true ORDER BY unlock_level ASC")
    List<Skill> findAvailableSkills(@Param("playerLevel") Integer playerLevel);
    
    /**
     * 根据类型查询技�?
     */
    @Select("SELECT * FROM skills WHERE skill_type = #{skillType} AND active = true")
    List<Skill> selectByType(@Param("skillType") String skillType);
}



