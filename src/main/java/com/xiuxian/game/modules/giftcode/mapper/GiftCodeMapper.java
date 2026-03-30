package com.xiuxian.game.modules.giftcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.giftcode.entity.GiftCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 礼包码数据访问层
 *
 * <p>提供礼包码的CRUD操作，继承MyBatis-Plus的BaseMapper。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see GiftCode
 */
@Mapper
public interface GiftCodeMapper extends BaseMapper<GiftCode> {
}

