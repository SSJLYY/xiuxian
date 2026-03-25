package com.xiuxian.game.modules.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.pet.entity.PetEvolution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 宠物进化Mapper
 */
@Mapper
public interface PetEvolutionMapper extends BaseMapper<PetEvolution> {

    /**
     * 获取宠物所有进化阶�?
     */
    @Select("SELECT * FROM pet_evolution WHERE pet_id = #{petId} ORDER BY evolution_stage ASC")
    List<PetEvolution> selectByPetId(@Param("petId") Integer petId);

    /**
     * 获取宠物特定进化阶段
     */
    @Select("SELECT * FROM pet_evolution WHERE pet_id = #{petId} AND evolution_stage = #{stage}")
    PetEvolution selectByPetIdAndStage(@Param("petId") Integer petId, @Param("stage") Integer stage);

    /**
     * 获取下一个可用的进化阶段
     */
    @Select("SELECT * FROM pet_evolution WHERE pet_id = #{petId} AND evolution_stage > #{currentStage} ORDER BY evolution_stage ASC LIMIT 1")
    PetEvolution selectNextEvolution(@Param("petId") Integer petId, @Param("currentStage") Integer currentStage);
}

