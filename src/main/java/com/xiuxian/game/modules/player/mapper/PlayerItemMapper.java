package com.xiuxian.game.modules.player.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.player.entity.PlayerItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerItemMapper extends BaseMapper<PlayerItem> {

    @Select("SELECT * FROM player_items WHERE player_id = #{playerId}")
    List<PlayerItem> selectByPlayerId(Integer playerId);

    @Select("SELECT * FROM player_items WHERE player_id = #{playerId} AND item_id = #{itemId} LIMIT 1")
    PlayerItem selectByPlayerIdAndItemId(Integer playerId, Integer itemId);

    @Select("SELECT * FROM player_items WHERE player_id = #{playerId} AND item_id = #{itemId} " +
            "AND (locked = 0 OR locked IS NULL) LIMIT 1")
    PlayerItem selectUnlockedByPlayerIdAndItemId(Integer playerId, Integer itemId);
}
