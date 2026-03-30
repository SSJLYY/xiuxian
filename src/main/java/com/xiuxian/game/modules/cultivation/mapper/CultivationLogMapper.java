package com.xiuxian.game.modules.cultivation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.cultivation.entity.CultivationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 修炼日志数据访问层
 *
 * <p>提供修炼日志的数据库操作接口，继承MyBatis-Plus的BaseMapper。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>继承BaseMapper获得基础CRUD操作</li>
 *   <li>提供按玩家ID查询修炼日志的自定义方法</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see com.xiuxian.game.modules.cultivation.entity.CultivationLog
 */
@Mapper
public interface CultivationLogMapper extends BaseMapper<CultivationLog> {
    
    /**
     * 查询玩家的修炼日志
     *
     * <p>根据玩家ID查询修炼日志，按创建时间降序排列，返回指定数量的记录。</p>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>玩家查看自己的修炼历史记录</li>
     *   <li>管理员查看玩家的修炼情况</li>
     *   <li>数据统计和分析</li>
     * </ul>
     *
     * @param playerId 玩家ID，不能为null
     * @param limit 返回记录数量限制，必须大于0
     * @return 修炼日志列表，按创建时间降序排列，如果没有记录则返回空列表
     * @throws IllegalArgumentException 当playerId为null或limit小于等于0时抛出
     */
    @Select("SELECT * FROM cultivation_logs WHERE player_id = #{playerId} ORDER BY created_at DESC LIMIT #{limit}")
    List<CultivationLog> selectByPlayerId(@Param("playerId") Integer playerId, @Param("limit") int limit);
}

