package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.PlayerDialogueState;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PlayerDialogueStateMapper extends BaseMapper<PlayerDialogueState> {

    @Select("SELECT * FROM player_dialogue_state WHERE player_id = #{playerId} AND dialogue_tree_id = #{treeId}")
    PlayerDialogueState selectByPlayerAndTree(@Param("playerId") Integer playerId, @Param("treeId") Integer treeId);

    @Select("SELECT * FROM player_dialogue_state WHERE player_id = #{playerId} AND is_completed = 0")
    java.util.List<PlayerDialogueState> selectInProgress(@Param("playerId") Integer playerId);
}
