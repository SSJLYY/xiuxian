package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.PlayerItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerItemMapper extends BaseMapper<PlayerItem> {

    @Select("SELECT * FROM player_items WHERE player_id = #{playerId}")
    List<PlayerItem> selectByPlayerId(Integer playerId);

    @Select("SELECT * FROM player_items WHERE player_id = #{playerId} AND item_id = #{itemId} LIMIT 1")
    PlayerItem selectByPlayerIdAndItemId(Integer playerId, Integer itemId);
}

