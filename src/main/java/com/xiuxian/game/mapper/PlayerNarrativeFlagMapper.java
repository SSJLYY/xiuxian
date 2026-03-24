package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.PlayerNarrativeFlag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerNarrativeFlagMapper extends BaseMapper<PlayerNarrativeFlag> {

    @Select("SELECT * FROM player_narrative_flags WHERE player_id = #{playerId}")
    List<PlayerNarrativeFlag> selectByPlayerId(@Param("playerId") Integer playerId);

    @Select("SELECT * FROM player_narrative_flags WHERE player_id = #{playerId} AND flag_key = #{flagKey}")
    PlayerNarrativeFlag selectByPlayerAndKey(@Param("playerId") Integer playerId, @Param("flagKey") String flagKey);

    @Select("SELECT flag_key FROM player_narrative_flags WHERE player_id = #{playerId}")
    List<String> selectFlagKeysByPlayerId(@Param("playerId") Integer playerId);
}
