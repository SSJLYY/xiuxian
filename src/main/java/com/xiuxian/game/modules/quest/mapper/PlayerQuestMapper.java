package com.xiuxian.game.modules.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.quest.entity.PlayerQuest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PlayerQuestMapper extends BaseMapper<PlayerQuest> {

    @Select("SELECT * FROM player_quests WHERE player_id = #{playerId}")
    List<PlayerQuest> selectByPlayerId(Integer playerId);

    @Select("SELECT pq.* FROM player_quests pq JOIN quests q ON pq.quest_id = q.id WHERE pq.player_id = #{playerId} AND q.type = #{type}")
    List<PlayerQuest> selectByPlayerIdAndQuestType(Integer playerId, String type);

    @Select("SELECT COUNT(*) FROM player_quests WHERE player_id = #{playerId} AND completed = #{completed} AND reward_claimed = #{rewardClaimed}")
    long countByPlayerIdAndCompletedAndRewardClaimed(Integer playerId, Boolean completed, Boolean rewardClaimed);

    @Select("SELECT * FROM player_quests WHERE player_id = #{playerId} AND quest_id = #{questId} LIMIT 1")
    PlayerQuest selectByPlayerIdAndQuestId(Integer playerId, Integer questId);

    @Update("UPDATE player_quests " +
            "SET reward_claimed = 1, updated_at = #{updatedAt} " +
            "WHERE id = #{playerQuestId} AND player_id = #{playerId} AND completed = 1 AND reward_claimed = 0")
    int claimRewardIfUnclaimed(@Param("playerQuestId") Integer playerQuestId,
                               @Param("playerId") Integer playerId,
                               @Param("updatedAt") java.time.LocalDateTime updatedAt);
}

