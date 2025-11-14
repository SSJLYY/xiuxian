package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.PlayerEquipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerEquipmentMapper extends BaseMapper<PlayerEquipment> {
    
    /**
     * 查询玩家的所有装备
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId}")
    List<PlayerEquipment> selectByPlayerId(@Param("playerId") Integer playerId);
    
    /**
     * 查询玩家已装备的装备
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId} AND is_equipped = true")
    List<PlayerEquipment> selectEquippedByPlayerId(@Param("playerId") Integer playerId);
    
    /**
     * 查询玩家指定槽位的装备
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId} AND slot = #{slot}")
    PlayerEquipment selectByPlayerIdAndSlot(@Param("playerId") Integer playerId, @Param("slot") String slot);
    
    /**
     * 查询玩家指定槽位已装备的装备
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId} AND slot = #{slot} AND is_equipped = true")
    PlayerEquipment selectEquippedBySlot(@Param("playerId") Integer playerId, @Param("slot") String slot);
}
