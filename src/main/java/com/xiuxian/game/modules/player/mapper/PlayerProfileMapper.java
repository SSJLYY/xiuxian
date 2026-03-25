package com.xiuxian.game.modules.player.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PlayerProfileMapper extends BaseMapper<PlayerProfile> {
    
    /**
     * 根据用户ID查找玩家档案
     */
    @Select("SELECT * FROM player_profiles WHERE user_id = #{userId}")
    PlayerProfile selectByUserId(@Param("userId") Integer userId);
}
