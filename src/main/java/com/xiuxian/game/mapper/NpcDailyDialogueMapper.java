package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.NpcDailyDialogue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NpcDailyDialogueMapper extends BaseMapper<NpcDailyDialogue> {

    @Select("SELECT * FROM npc_daily_dialogues WHERE npc_id = #{npcId} AND active = 1 ORDER BY priority DESC")
    List<NpcDailyDialogue> selectByNpcId(Integer npcId);
}
