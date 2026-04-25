package com.xiuxian.game.modules.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.skill.entity.SkillCombo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 技能连招Mapper
 */
@Mapper
public interface SkillComboMapper extends BaseMapper<SkillCombo> {

    /**
     * 获取玩家可用的连招（等级满足的激活连招）
     */
    @Select("SELECT * FROM skill_combos WHERE active = true AND required_level <= #{playerLevel} ORDER BY LENGTH(skill_sequence) DESC, id ASC")
    List<SkillCombo> selectAvailableCombos(@Param("playerLevel") Integer playerLevel);

    /**
     * 获取所有激活的连招
     */
    @Select("SELECT * FROM skill_combos WHERE active = true")
    List<SkillCombo> selectActiveCombos();
}

