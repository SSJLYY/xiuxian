package com.xiuxian.game.modules.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.activity.entity.PlayerActivityProgress;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PlayerActivityProgressMapper extends BaseMapper<PlayerActivityProgress> {

    @Select("SELECT * FROM player_activities WHERE player_id = #{playerId} AND activity_id = #{activityId} FOR UPDATE")
    PlayerActivityProgress selectByPlayerAndActivityForUpdate(@Param("playerId") Integer playerId,
                                                              @Param("activityId") Integer activityId);

    @Insert("INSERT INTO player_activities (activity_id, player_id, progress, is_completed, is_rewarded, updated_at) " +
            "SELECT #{activityId}, #{playerId}, #{progress}, #{completed}, #{rewarded}, #{updatedAt} FROM DUAL " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM player_activities WHERE activity_id = #{activityId} AND player_id = #{playerId}" +
            ")")
    int insertIfAbsent(@Param("activityId") Integer activityId,
                       @Param("playerId") Integer playerId,
                       @Param("progress") String progress,
                       @Param("completed") Boolean completed,
                       @Param("rewarded") Boolean rewarded,
                       @Param("updatedAt") java.time.LocalDateTime updatedAt);
}

