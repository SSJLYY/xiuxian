package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
    
    /**
     * 根据类型查询装备
     */
    @Select("SELECT * FROM equipments WHERE type = #{type}")
    List<Equipment> selectByType(@Param("type") String type);
    
    /**
     * 根据品质查询装备
     */
    @Select("SELECT * FROM equipments WHERE quality = #{quality}")
    List<Equipment> selectByQuality(@Param("quality") Integer quality);
    
    /**
     * 根据所需等级查询装备
     */
    @Select("SELECT * FROM equipments WHERE required_level <= #{level} ORDER BY required_level DESC")
    List<Equipment> selectByRequiredLevel(@Param("level") Integer level);
}
