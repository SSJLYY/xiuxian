package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.CultivationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CultivationLogMapper extends BaseMapper<CultivationLog> {
    
    /**
     * 查询玩家的修炼日志
     */
    @Select("SELECT * FROM cultivation_logs WHERE player_id = #{playerId} ORDER BY created_at DESC LIMIT #{limit}")
    List<CultivationLog> selectByPlayerId(@Param("playerId") Integer playerId, @Param("limit") int limit);
}
