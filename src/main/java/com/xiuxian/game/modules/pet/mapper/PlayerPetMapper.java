package com.xiuxian.game.modules.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PlayerPetMapper extends BaseMapper<PlayerPet> {

    /**
     * 查询玩家的所有宠�?
     */
    @Select("SELECT * FROM player_pets WHERE player_id = #{playerId} ORDER BY is_active DESC, level DESC")
    List<PlayerPet> selectByPlayerId(@Param("playerId") Integer playerId);

    /**
     * 查询玩家的出战宠�?
     */
    @Select("SELECT * FROM player_pets WHERE player_id = #{playerId} AND is_active = true LIMIT 1")
    PlayerPet selectActivePet(@Param("playerId") Integer playerId);

    /**
     * 取消所有出战状�?
     */
    @Update("UPDATE player_pets SET is_active = false WHERE player_id = #{playerId}")
    int deactivateAllPets(@Param("playerId") Integer playerId);

    /**
     * 查询玩家是否拥有某个宠物
     */
    @Select("SELECT COUNT(*) FROM player_pets WHERE player_id = #{playerId} AND pet_id = #{petId}")
    int countByPlayerIdAndPetId(@Param("playerId") Integer playerId, @Param("petId") Integer petId);
}

