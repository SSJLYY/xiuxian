package com.xiuxian.game.modules.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.equipment.entity.PlayerEquipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerEquipmentMapper extends BaseMapper<PlayerEquipment> {
    
    /**
    /**
     * 查询所有玩家拥有的装备
     */
    List<PlayerEquipment> selectByPlayerId(@Param("playerId") Integer playerId);
    
    /**
     * 查询玩家已穿戴的装备
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId} AND is_equipped = true")
    List<PlayerEquipment> selectEquippedByPlayerId(@Param("playerId") Integer playerId);
    
    /**
     * 查询玩家指定位置的背包
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId} AND slot = #{slot}")
    PlayerEquipment selectByPlayerIdAndSlot(@Param("playerId") Integer playerId, @Param("slot") String slot);
    
    /**
     * 查询玩家指定位置已穿戴的装备
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId} AND slot = #{slot} AND is_equipped = true")
    PlayerEquipment selectEquippedBySlot(@Param("playerId") Integer playerId, @Param("slot") String slot);
}

