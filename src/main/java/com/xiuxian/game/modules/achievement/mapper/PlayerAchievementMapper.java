package com.xiuxian.game.modules.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.achievement.entity.PlayerAchievement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 玩家成就数据访问层
 *
 * <p>提供玩家成就进度的CRUD操作，继承MyBatis-Plus的BaseMapper。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Mapper
public interface PlayerAchievementMapper extends BaseMapper<PlayerAchievement> {
}

