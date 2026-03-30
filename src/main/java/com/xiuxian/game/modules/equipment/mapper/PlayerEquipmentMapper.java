package com.xiuxian.game.modules.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.equipment.entity.PlayerEquipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 玩家装备数据访问层
 *
 * <p>提供玩家装备表的CRUD操作和自定义查询方法。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>继承MyBatis-Plus的BaseMapper获得基础CRUD能力</li>
 *   <li>提供按玩家ID、装备槽位等条件的装备查询</li>
 *   <li>支持查询已穿戴和未穿戴的装备</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see PlayerEquipment
 */
@Mapper
public interface PlayerEquipmentMapper extends BaseMapper<PlayerEquipment> {
    
    /**
     * 查询所有玩家拥有的装备
     *
     * <p>查询指定玩家拥有的所有装备，包括已穿戴和未穿戴的。</p>
     * <p>用于玩家装备界面展示和装备管理。</p>
     *
     * @param playerId 玩家ID
     * @return 玩家拥有的所有装备列表
     */
    List<PlayerEquipment> selectByPlayerId(@Param("playerId") Integer playerId);
    
    /**
     * 查询玩家已穿戴的装备
     *
     * <p>查询指定玩家当前已穿戴的所有装备。</p>
     * <p>用于角色属性计算和装备效果展示。</p>
     *
     * @param playerId 玩家ID
     * @return 玩家已穿戴的装备列表
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId} AND is_equipped = true")
    List<PlayerEquipment> selectEquippedByPlayerId(@Param("playerId") Integer playerId);
    
    /**
     * 查询玩家指定位置的背包
     *
     * <p>查询指定玩家在指定装备槽位的所有装备。</p>
     * <p>用于装备槽位展示和装备切换。</p>
     *
     * @param playerId 玩家ID
     * @param slot 装备槽位（weapon、armor、accessory等）
     * @return 该槽位的装备列表
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId} AND slot = #{slot}")
    PlayerEquipment selectByPlayerIdAndSlot(@Param("playerId") Integer playerId, @Param("slot") String slot);
    
    /**
     * 查询玩家指定位置已穿戴的装备
     *
     * <p>查询指定玩家在指定装备槽位当前已穿戴的装备。</p>
     * <p>用于装备效果计算和属性加成。</p>
     *
     * @param playerId 玩家ID
     * @param slot 装备槽位（weapon、armor、accessory等）
     * @return 该槽位已穿戴的装备，如果没有则返回null
     */
    @Select("SELECT * FROM player_equipment WHERE player_id = #{playerId} AND slot = #{slot} AND is_equipped = true")
    PlayerEquipment selectEquippedBySlot(@Param("playerId") Integer playerId, @Param("slot") String slot);
}

