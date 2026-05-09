package com.xiuxian.game.modules.vip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.vip.entity.RechargeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface RechargeRecordMapper extends BaseMapper<RechargeRecord> {

    @Update("UPDATE recharge_records " +
            "SET status = 'SUCCESS', completed_at = #{completedAt} " +
            "WHERE id = #{orderId} AND status = 'PENDING'")
    int markRechargeSuccessIfPending(@Param("orderId") Long orderId,
                                     @Param("completedAt") LocalDateTime completedAt);

    @Update("UPDATE recharge_records " +
            "SET status = 'FAILED', completed_at = #{completedAt} " +
            "WHERE id = #{orderId} AND status = 'PENDING'")
    int markRechargeFailedIfPending(@Param("orderId") Long orderId,
                                    @Param("completedAt") LocalDateTime completedAt);
}

