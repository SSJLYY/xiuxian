package com.xiuxian.game.modules.combat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.combat.entity.CombatLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 战斗日志数据访问层
 *
 * <p>提供战斗日志的CRUD操作，继承MyBatis-Plus的BaseMapper。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Mapper
public interface CombatLogMapper extends BaseMapper<CombatLog> {
    
    /**
     * 查询玩家最近的战斗记录
     *
     * @param playerId 玩家ID
     * @param limit 返回数量限制
     * @return 战斗日志列表
     */
    @Select("SELECT * FROM combat_logs WHERE player_id = #{playerId} ORDER BY created_at DESC LIMIT #{limit}")
    List<CombatLog> selectRecentByPlayerId(Integer playerId, Integer limit);
    
    /**
     * 查询玩家的胜利战斗记录
     *
     * @param playerId 玩家ID
     * @return 胜利战斗日志列表
     */
    @Select("SELECT * FROM combat_logs WHERE player_id = #{playerId} AND result = 'WIN' ORDER BY created_at DESC")
    List<CombatLog> selectWinsByPlayerId(Integer playerId);
    
    /**
     * 统计玩家的战斗次数
     *
     * @param playerId 玩家ID
     * @return 战斗次数
     */
    @Select("SELECT COUNT(*) FROM combat_logs WHERE player_id = #{playerId}")
    long countByPlayerId(Integer playerId);
}

