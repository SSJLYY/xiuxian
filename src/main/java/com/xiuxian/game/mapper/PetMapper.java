package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {

    /**
     * 根据类型查询宠物
     */
    @Select("SELECT * FROM pets WHERE type = #{type} AND active = true")
    List<Pet> selectByType(@Param("type") String type);

    /**
     * 根据稀有度查询宠物
     */
    @Select("SELECT * FROM pets WHERE rarity = #{rarity} AND active = true")
    List<Pet> selectByRarity(@Param("rarity") Integer rarity);

    /**
     * 查询玩家可捕获的宠物（根据等级）
     */
    @Select("SELECT * FROM pets WHERE unlock_level <= #{playerLevel} AND active = true ORDER BY rarity DESC, unlock_level ASC")
    List<Pet> selectAvailablePets(@Param("playerLevel") Integer playerLevel);
}
