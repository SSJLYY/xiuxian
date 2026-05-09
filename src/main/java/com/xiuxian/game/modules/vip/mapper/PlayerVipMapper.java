package com.xiuxian.game.modules.vip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.vip.entity.PlayerVip;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PlayerVipMapper extends BaseMapper<PlayerVip> {

    @Select("SELECT * FROM player_vip WHERE player_id = #{playerId} LIMIT 1")
    PlayerVip selectByPlayerId(@Param("playerId") Integer playerId);

    @Select("SELECT * FROM player_vip WHERE player_id = #{playerId} FOR UPDATE")
    PlayerVip selectByPlayerIdForUpdate(@Param("playerId") Integer playerId);

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

