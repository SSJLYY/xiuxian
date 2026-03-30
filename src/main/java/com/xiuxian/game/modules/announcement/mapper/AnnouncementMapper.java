package com.xiuxian.game.modules.announcement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.announcement.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告数据访问层
 *
 * <p>提供公告的CRUD操作，继承MyBatis-Plus的BaseMapper。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}

