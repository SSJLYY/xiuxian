package com.xiuxian.game.modules.vip.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.vip.entity.PlayerVip;
import com.xiuxian.game.modules.vip.entity.RechargeRecord;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.player.service.PlayerService;
import java.util.concurrent.ThreadLocalRandom;
import com.xiuxian.game.modules.vip.mapper.RechargeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeService extends ServiceImpl<RechargeRecordMapper, RechargeRecord> {
    
    private final RechargeRecordMapper rechargeRecordMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据
    private final VipService vipService;
    
    /**
     * 创建充值订单
     * @param playerId 玩家ID
     * @param amount 充值金额（单位:元）
     * @return 充值记录
     */
    public RechargeRecord createRechargeOrder(Integer playerId, Integer amount) {
        // 校验充值金额
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "充值金额必须大于0");
        }
        if (amount > 100000) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "单笔充值金额不能超过100000元");
        }
        
        RechargeRecord record = new RechargeRecord();
        record.setPlayerId(playerId);
        record.setAmount(amount);
        // 元宝数量由VIP服务计算（包括首充奖励）
        record.setYuanbao(0);
        record.setOrderNo(generateOrderNo());
        record.setStatus("PENDING");
        record.setCreatedAt(LocalDateTime.now());
        
        rechargeRecordMapper.insert(record);
        return record;
    }
    
    /**
     * 处理充值成功回调
     * @param orderId 订单ID
     * @return 充值记录
     */
    @Transactional
    public RechargeRecord processRechargeSuccess(Long orderId) {
        RechargeRecord record = rechargeRecordMapper.selectById(orderId);
        if (record == null) {
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_NOT_FOUND);
        }

        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_STATUS_INVALID);
        }
        
        // 更新VIP信息（元宝已在VipService.updateVipInfo中发放到PlayerVip.yuanbao）
        PlayerVip playerVip = vipService.updateVipInfo(record.getPlayerId(), record.getAmount());
        
        // 检查是否为首充（仅用于记录）
        Integer previousTotalRecharge = playerVip.getTotalRecharge() - record.getAmount();
        boolean isFirstRecharge = previousTotalRecharge == 0;
        
        // 计算充值获得的元宝数量（仅用于记录，实际发放已在VipService中完成）
        long yuanbaoToAdd = record.getAmount() * 10L;
        if (isFirstRecharge) {
            yuanbaoToAdd = yuanbaoToAdd * 3 / 2; // 首充额外赠送50%
        }
        record.setYuanbao((int) Math.min(yuanbaoToAdd, Integer.MAX_VALUE));
        
        // 更新充值记录状态
        record.setStatus("SUCCESS");
        record.setCompletedAt(LocalDateTime.now());
        rechargeRecordMapper.updateById(record);
        
        log.info("充值处理成功: playerId={}, amount={}, yuanbao={}, isFirstRecharge={}", 
                record.getPlayerId(), record.getAmount(), yuanbaoToAdd, isFirstRecharge);
        return record;
    }
    
    /**
     * 处理充值失败回调
     * @param orderId 订单ID
     * @return 充值记录
     */
    public RechargeRecord processRechargeFailed(Long orderId) {
        RechargeRecord record = rechargeRecordMapper.selectById(orderId);
        if (record == null) {
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_NOT_FOUND);
        }

        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_STATUS_INVALID);
        }
        
        // 模块边界：通过PlayerService访问玩家数据
        record.setStatus("FAILED");
        record.setCompletedAt(LocalDateTime.now());
        rechargeRecordMapper.updateById(record);
        
        return record;
    }
    
    /**
     * 获取玩家充值记录
     * @param playerId 玩家ID
     * @param limit 数量限制
     * @return 充值记录列表
     */
    public List<RechargeRecord> getPlayerRechargeRecords(Integer playerId, int limit) {
        // 模块边界：通过PlayerService访问玩家数据
        QueryWrapper<RechargeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("LIMIT " + limit);
        
        return rechargeRecordMapper.selectList(queryWrapper);
    }
    
    /**
     * 查询指定时间段内成功的充值记录（供 AsyncStatisticsService 使用）
     * 模块边界：admin 模块通过此接口获取充值统计，不直接操作 RechargeRecordMapper
     *
     * @param startTime 开始时间（含）
     * @param endTime   结束时间（不含）
     * @return 成功充值记录列表
     */
    public List<RechargeRecord> getSuccessRechargesByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<RechargeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "SUCCESS")
               .between("completed_at", startTime, endTime);
        return rechargeRecordMapper.selectList(wrapper);
    }

    /**
     * 生成订单号
     * @return 订单号
     */
    private String generateOrderNo() {
        return "RECHARGE_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(10000);
    }
}
