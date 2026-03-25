package com.xiuxian.game.modules.map.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.map.entity.PlayerMapProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 玩家地图进度Mapper
 *
 * @author LevelDesigner
 * @since 2026-03-23
 */
@Mapper
public interface PlayerMapProgressMapper extends BaseMapper<PlayerMapProgress> {

    /**
     * 查询玩家的所有地图进度
     */
    @Select("SELECT * FROM player_map_progress WHERE player_id = #{playerId}")
    List<PlayerMapProgress> selectByPlayerId(@Param("playerId") Integer playerId);

    /**
     * 查询玩家当前所在的地图
     */
    @Select("SELECT * FROM player_map_progress WHERE player_id = #{playerId} AND is_current = 1 LIMIT 1")
    PlayerMapProgress selectCurrentMap(@Param("playerId") Integer playerId);

    /**
     * 查询玩家已解锁的地图
     */
    @Select("SELECT * FROM player_map_progress WHERE player_id = #{playerId} AND is_unlocked = 1")
    List<PlayerMapProgress> selectUnlockedMaps(@Param("playerId") Integer playerId);

    /**
     * 查询特定地图的进度
     */
    @Select("SELECT * FROM player_map_progress WHERE player_id = #{playerId} AND map_id = #{mapId}")
    PlayerMapProgress selectByPlayerAndMap(@Param("playerId") Integer playerId, @Param("mapId") Integer mapId);

    /**
     * 清除玩家的当前地图标记
     */
    @Update("UPDATE player_map_progress SET is_current = 0 WHERE player_id = #{playerId}")
    int clearCurrentMap(@Param("playerId") Integer playerId);

    /**
     * 设置当前地图
     */
    @Update("UPDATE player_map_progress SET is_current = 1, last_enter_at = NOW() WHERE player_id = #{playerId} AND map_id = #{mapId}")
    int setCurrentMap(@Param("playerId") Integer playerId, @Param("mapId") Integer mapId);

    /**
     * 解锁地图
     */
    @Update("UPDATE player_map_progress SET is_unlocked = 1, first_enter_at = NOW() WHERE player_id = #{playerId} AND map_id = #{mapId}")
    int unlockMap(@Param("playerId") Integer playerId, @Param("mapId") Integer mapId);
}
