package com.xiuxian.game.modules.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.pet.entity.PlayerPetEvolution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 玩家宠物进化记录Mapper
 */
@Mapper
public interface PlayerPetEvolutionMapper extends BaseMapper<PlayerPetEvolution> {

    /**
     * 根据玩家宠物ID查询进化记录
     */
    @Select("SELECT * FROM player_pet_evolution WHERE player_pet_id = #{playerPetId} LIMIT 1")
    PlayerPetEvolution selectByPlayerPetId(@Param("playerPetId") Integer playerPetId);
}

