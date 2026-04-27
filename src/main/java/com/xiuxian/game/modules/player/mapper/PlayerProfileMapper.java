package com.xiuxian.game.modules.player.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PlayerProfileMapper extends BaseMapper<PlayerProfile> {
    
    /**
     * 根据用户 ID 查找玩家档案
     */
    @Select("SELECT * FROM player_profiles WHERE user_id = #{userId}")
    PlayerProfile selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据昵称查找玩家档案
     */
    @Select("SELECT * FROM player_profiles WHERE nickname = #{nickname}")
    PlayerProfile selectByNickname(@Param("nickname") String nickname);
    @Select("SELECT * FROM player_profiles WHERE id = #{playerId} FOR UPDATE")
    PlayerProfile selectByIdForUpdate(@Param("playerId") Integer playerId);
}
