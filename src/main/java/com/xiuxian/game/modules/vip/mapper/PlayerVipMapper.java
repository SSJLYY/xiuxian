package com.xiuxian.game.modules.vip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.vip.entity.PlayerVip;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerVipMapper extends BaseMapper<PlayerVip> {

    @Insert("INSERT INTO player_vip (player_id, vip_level, total_recharge, yuanbao, last_daily_reward_at) " +
            "SELECT #{playerId}, #{vipLevel}, #{totalRecharge}, #{yuanbao}, #{lastDailyRewardAt} FROM DUAL " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM player_vip WHERE player_id = #{playerId}" +
            ")")
    int insertIfAbsent(@Param("playerId") Integer playerId,
                       @Param("vipLevel") Integer vipLevel,
                       @Param("totalRecharge") Integer totalRecharge,
                       @Param("yuanbao") Integer yuanbao,
                       @Param("lastDailyRewardAt") java.time.LocalDateTime lastDailyRewardAt);
}

