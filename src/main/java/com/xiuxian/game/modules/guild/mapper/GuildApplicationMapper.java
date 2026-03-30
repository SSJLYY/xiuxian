package com.xiuxian.game.modules.guild.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.guild.entity.GuildApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宗门申请数据访问层
 *
 * <p>提供宗门申请相关的数据库操作，继承MyBatis-Plus的BaseMapper</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>基础CRUD操作 - 继承BaseMapper提供的增删改查</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see GuildApplication
 */
@Mapper
public interface GuildApplicationMapper extends BaseMapper<GuildApplication> {
}

