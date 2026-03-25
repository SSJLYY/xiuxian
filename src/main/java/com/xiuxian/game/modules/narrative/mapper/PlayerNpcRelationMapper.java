package com.xiuxian.game.modules.narrative.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.narrative.entity.PlayerNpcRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerNpcRelationMapper extends BaseMapper<PlayerNpcRelation> {

    @Select("SELECT * FROM player_npc_relations WHERE player_id = #{playerId}")
    List<PlayerNpcRelation> selectByPlayerId(@Param("playerId") Integer playerId);

    @Select("SELECT * FROM player_npc_relations WHERE player_id = #{playerId} AND npc_id = #{npcId}")
    PlayerNpcRelation selectByPlayerAndNpc(@Param("playerId") Integer playerId, @Param("npcId") Integer npcId);
}

