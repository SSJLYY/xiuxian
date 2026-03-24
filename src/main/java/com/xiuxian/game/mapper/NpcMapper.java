package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.Npc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NpcMapper extends BaseMapper<Npc> {

    @Select("SELECT * FROM npcs WHERE active = 1 ORDER BY sort_order ASC")
    List<Npc> selectAllActive();

    @Select("SELECT * FROM npcs WHERE active = 1 AND min_level <= #{level} ORDER BY sort_order ASC")
    List<Npc> selectByLevel(Integer level);

    @Select("SELECT * FROM npcs WHERE faction = #{faction} AND active = 1 ORDER BY sort_order ASC")
    List<Npc> selectByFaction(String faction);
}
