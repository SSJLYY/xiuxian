package com.xiuxian.game.modules.offline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.offline.entity.OfflineReward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OfflineRewardMapper extends BaseMapper<OfflineReward> {
    
    @Select("SELECT * FROM offline_rewards WHERE player_id = #{playerId} AND claimed = 0 ORDER BY created_at DESC")
    List<OfflineReward> selectUnclaimedByPlayerId(Integer playerId);
    
    @Select("SELECT * FROM offline_rewards WHERE player_id = #{playerId} ORDER BY created_at DESC LIMIT 1")
    OfflineReward selectLatestByPlayerId(Integer playerId);
}

