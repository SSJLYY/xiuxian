package com.xiuxian.game.modules.narrative.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.narrative.entity.LoreEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LoreEntryMapper extends BaseMapper<LoreEntry> {

    @Select("SELECT * FROM lore_entries WHERE active = 1 ORDER BY sort_order ASC")
    List<LoreEntry> selectAllActive();

    @Select("SELECT * FROM lore_entries WHERE lore_layer = #{layer} AND active = 1 ORDER BY sort_order ASC")
    List<LoreEntry> selectByLayer(String layer);

    @Select("SELECT * FROM lore_entries WHERE category = #{category} AND active = 1 ORDER BY sort_order ASC")
    List<LoreEntry> selectByCategory(String category);

    @Select("SELECT * FROM lore_entries WHERE lore_key = #{loreKey}")
    LoreEntry selectByKey(String loreKey);
}

