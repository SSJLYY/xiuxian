package com.xiuxian.game.modules.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.achievement.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成就模板数据访问层
 *
 * <p>提供成就模板的CRUD操作，继承MyBatis-Plus的BaseMapper。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {
}

