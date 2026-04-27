package com.xiuxian.game.modules.giftcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.giftcode.entity.GiftCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

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

    @Update("UPDATE gift_codes " +
            "SET used_count = used_count + 1, " +
            "status = CASE WHEN max_usage IS NOT NULL AND used_count >= max_usage - 1 THEN 'DISABLED' ELSE status END " +
            "WHERE id = #{giftCodeId} " +
            "AND status = 'ACTIVE' " +
            "AND (expire_at IS NULL OR expire_at > #{now}) " +
            "AND (max_usage IS NULL OR used_count < max_usage)")
    int consumeUsageIfAvailable(@Param("giftCodeId") Long giftCodeId,
                                @Param("now") LocalDateTime now);
}

