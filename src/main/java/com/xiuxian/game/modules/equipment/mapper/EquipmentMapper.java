package com.xiuxian.game.modules.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.equipment.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 装备数据访问层
 *
 * <p>提供装备表的CRUD操作和自定义查询方法。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>继承MyBatis-Plus的BaseMapper获得基础CRUD能力</li>
 *   <li>提供按类型、品质、等级等条件的装备查询</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see Equipment
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
    
    /**
     * 根据类型查询装备
     *
     * <p>查询指定类型的所有装备，用于装备商店展示和装备筛选。</p>
     *
     * @param type 装备类型（武器、防具、饰品等）
     * @return 符合条件的装备列表
     */
    @Select("SELECT * FROM equipments WHERE type = #{type}")
    List<Equipment> selectByType(@Param("type") String type);
    
    /**
     * 根据品质查询装备
     *
     * <p>查询指定品质的所有装备，用于装备筛选和品质统计。</p>
     *
     * @param quality 装备品质（1-普通, 2-精良, 3-稀有, 4-史诗, 5-传说）
     * @return 符合条件的装备列表
     */
    @Select("SELECT * FROM equipments WHERE quality = #{quality}")
    List<Equipment> selectByQuality(@Param("quality") Integer quality);
    
    /**
     * 根据所需等级查询装备
     *
     * <p>查询玩家当前等级可穿戴的所有装备，按所需等级降序排列。</p>
     * <p>用于装备推荐和装备商店展示。</p>
     *
     * @param level 玩家当前等级
     * @return 符合条件的装备列表，按所需等级降序排列
     */
    @Select("SELECT * FROM equipments WHERE required_level <= #{level} ORDER BY required_level DESC")
    List<Equipment> selectByRequiredLevel(@Param("level") Integer level);
}

