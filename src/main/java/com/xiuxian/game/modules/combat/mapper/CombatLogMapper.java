package com.xiuxian.game.modules.combat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.combat.entity.CombatLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CombatLogMapper extends BaseMapper<CombatLog> {
    
    @Select("SELECT * FROM combat_logs WHERE player_id = #{playerId} ORDER BY created_at DESC LIMIT #{limit}")
    List<CombatLog> selectRecentByPlayerId(Integer playerId, Integer limit);
    
    @Select("SELECT * FROM combat_logs WHERE player_id = #{playerId} AND result = 'WIN' ORDER BY created_at DESC")
    List<CombatLog> selectWinsByPlayerId(Integer playerId);
    
    @Select("SELECT COUNT(*) FROM combat_logs WHERE player_id = #{playerId}")
    long countByPlayerId(Integer playerId);
}

