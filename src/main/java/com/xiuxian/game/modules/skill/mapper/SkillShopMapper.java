package com.xiuxian.game.modules.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.skill.entity.SkillShopItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SkillShopMapper extends BaseMapper<SkillShopItem> {
    @Select("SELECT * FROM skill_shop WHERE available = 1")
    List<SkillShopItem> selectAvailable();
}
