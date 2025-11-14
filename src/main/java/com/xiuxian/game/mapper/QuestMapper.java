package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.Quest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestMapper extends BaseMapper<Quest> {

    @Select("SELECT * FROM quests WHERE type = #{type}")
    List<Quest> selectByType(String type);
}

