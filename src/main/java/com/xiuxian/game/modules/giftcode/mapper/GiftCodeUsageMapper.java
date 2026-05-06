package com.xiuxian.game.modules.giftcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.giftcode.entity.GiftCodeUsage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 礼包码使用记录数据访问层
 *
 * <p>提供礼包码使用记录的CRUD操作，继承MyBatis-Plus的BaseMapper。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 * @see GiftCodeUsage
 */
@Mapper
public interface GiftCodeUsageMapper extends BaseMapper<GiftCodeUsage> {
    @Insert("INSERT INTO gift_code_usage (gift_code_id, player_id, used_at) " +
            "SELECT #{giftCodeId}, #{playerId}, NOW() FROM DUAL " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM gift_code_usage WHERE gift_code_id = #{giftCodeId} AND player_id = #{playerId})")
    int insertIfAbsent(@Param("giftCodeId") Long giftCodeId, @Param("playerId") Integer playerId);
}

