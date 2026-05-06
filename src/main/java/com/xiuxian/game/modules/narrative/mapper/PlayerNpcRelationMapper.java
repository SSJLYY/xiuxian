package com.xiuxian.game.modules.narrative.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.narrative.entity.PlayerNpcRelation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerNpcRelationMapper extends BaseMapper<PlayerNpcRelation> {

    @Insert("INSERT INTO player_npc_relations " +
            "(player_id, npc_id, affinity, relationship_level, first_met_at, last_interact_at, total_interactions, created_at, updated_at) " +
            "SELECT #{playerId}, #{npcId}, #{affinity}, #{relationshipLevel}, #{firstMetAt}, #{lastInteractAt}, #{totalInteractions}, #{createdAt}, #{updatedAt} FROM DUAL " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM player_npc_relations WHERE player_id = #{playerId} AND npc_id = #{npcId}" +
            ")")
    int insertIfAbsent(@Param("playerId") Integer playerId,
                       @Param("npcId") Integer npcId,
                       @Param("affinity") Integer affinity,
                       @Param("relationshipLevel") String relationshipLevel,
                       @Param("firstMetAt") java.time.LocalDateTime firstMetAt,
                       @Param("lastInteractAt") java.time.LocalDateTime lastInteractAt,
                       @Param("totalInteractions") Integer totalInteractions,
                       @Param("createdAt") java.time.LocalDateTime createdAt,
                       @Param("updatedAt") java.time.LocalDateTime updatedAt);

    @Select("SELECT * FROM player_npc_relations WHERE player_id = #{playerId}")
    List<PlayerNpcRelation> selectByPlayerId(@Param("playerId") Integer playerId);

    @Select("SELECT * FROM player_npc_relations WHERE player_id = #{playerId} AND npc_id = #{npcId}")
    PlayerNpcRelation selectByPlayerAndNpc(@Param("playerId") Integer playerId, @Param("npcId") Integer npcId);
}

