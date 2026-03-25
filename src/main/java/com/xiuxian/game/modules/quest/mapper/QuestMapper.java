package com.xiuxian.game.modules.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.quest.entity.Quest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestMapper extends BaseMapper<Quest> {

    @Select("SELECT * FROM quests WHERE type = #{type}")
    List<Quest> selectByType(String type);
}


