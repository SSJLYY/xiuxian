package com.xiuxian.game.modules.narrative.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.narrative.entity.PlayerLoreCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerLoreCollectionMapper extends BaseMapper<PlayerLoreCollection> {

    @Select("SELECT * FROM player_lore_collection WHERE player_id = #{playerId} ORDER BY discovered_at DESC")
    List<PlayerLoreCollection> selectByPlayerId(@Param("playerId") Integer playerId);

    @Select("SELECT lore_entry_id FROM player_lore_collection WHERE player_id = #{playerId}")
    List<Integer> selectDiscoveredIds(@Param("playerId") Integer playerId);

    @Select("SELECT * FROM player_lore_collection WHERE player_id = #{playerId} AND lore_entry_id = #{loreEntryId}")
    PlayerLoreCollection selectByPlayerAndLore(@Param("playerId") Integer playerId, @Param("loreEntryId") Integer loreEntryId);
}

