package com.xiuxian.game.modules.combat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.combat.entity.MapMonster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地图怪物配置数据访问层
 *
 * <p>提供地图怪物配置的CRUD操作，继承MyBatis-Plus的BaseMapper。</p>
 *
 * @author LevelDesigner
 * @version 1.0.0
 * @since 2026-03-23
 */
@Mapper
public interface MapMonsterMapper extends BaseMapper<MapMonster> {
    
    /**
     * 根据地图ID查询怪物配置
     *
     * @param mapId 地图ID
     * @return 地图怪物配置列表
     */
    @Select("SELECT * FROM map_monsters WHERE map_id = #{mapId}")
    List<MapMonster> selectByMapId(@Param("mapId") Integer mapId);
    
    /**
     * 查询地图的普通怪物
     *
     * @param mapId 地图ID
     * @return 普通怪物配置列表
     */
    @Select("SELECT * FROM map_monsters WHERE map_id = #{mapId} AND is_elite = 0")
    List<MapMonster> selectNormalMonsters(@Param("mapId") Integer mapId);
    
    /**
     * 查询地图的精英怪物
     *
     * @param mapId 地图ID
     * @return 精英怪物配置列表
     */
    @Select("SELECT * FROM map_monsters WHERE map_id = #{mapId} AND is_elite = 1")
    List<MapMonster> selectEliteMonsters(@Param("mapId") Integer mapId);
}

