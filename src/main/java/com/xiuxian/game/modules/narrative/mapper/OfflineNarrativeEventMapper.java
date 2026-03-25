package com.xiuxian.game.modules.narrative.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.narrative.entity.OfflineNarrativeEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OfflineNarrativeEventMapper extends BaseMapper<OfflineNarrativeEvent> {

    @Select("SELECT * FROM offline_narrative_events WHERE active = 1 ORDER BY sort_order ASC")
    List<OfflineNarrativeEvent> selectAllActive();
}

