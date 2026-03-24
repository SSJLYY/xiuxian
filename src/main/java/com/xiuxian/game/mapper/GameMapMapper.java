package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.GameMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 游戏地图Mapper
 * 
 * @author LevelDesigner
 * @since 2026-03-23
 */
@Mapper
public interface GameMapMapper extends BaseMapper<GameMap> {
    
    /**
     * 根据区域查询地图
     */
    @Select("SELECT * FROM game_maps WHERE region = #{region} AND active = 1 ORDER BY required_level")
    List<GameMap> selectByRegion(@Param("region") String region);
    
    /**
     * 查询所有激活的地图
     */
    @Select("SELECT * FROM game_maps WHERE active = 1 ORDER BY required_level, position_x, position_y")
    List<GameMap> selectAllActive();
    
    /**
     * 根据类型查询地图
     */
    @Select("SELECT * FROM game_maps WHERE map_type = #{mapType} AND active = 1 ORDER BY required_level")
    List<GameMap> selectByType(@Param("mapType") String mapType);
    
    /**
     * 查询玩家可进入的地图（满足等级和境界要求）
     */
    @Select("SELECT * FROM game_maps WHERE required_level <= #{level} AND active = 1 ORDER BY required_level")
    List<GameMap> selectAccessibleMaps(@Param("level") int level);
    
    /**
     * 查询起始地图（新手村）
     */
    @Select("SELECT * FROM game_maps WHERE id = 1")
    GameMap selectStartingMap();
}
