package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.PetTrainingLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PetTrainingLogMapper extends BaseMapper<PetTrainingLog> {

    /**
     * 查询宠物的训练记录
     */
    @Select("SELECT * FROM pet_training_logs WHERE player_pet_id = #{playerPetId} ORDER BY created_at DESC LIMIT #{limit}")
    List<PetTrainingLog> selectByPlayerPetId(@Param("playerPetId") Integer playerPetId, @Param("limit") Integer limit);
}
