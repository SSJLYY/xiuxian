package com.xiuxian.game.modules.vip.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.vip.entity.PlayerVip;
import com.xiuxian.game.modules.vip.entity.RechargeRecord;
import com.xiuxian.game.modules.vip.mapper.RechargeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeService extends ServiceImpl<RechargeRecordMapper, RechargeRecord> {

    private final RechargeRecordMapper rechargeRecordMapper;
    private final VipService vipService;

    public RechargeRecord createRechargeOrder(Integer playerId, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "充值金额必须大于0");
        }
        if (amount > 100000) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "单笔充值金额不能超过100000元");
        }

        RechargeRecord record = new RechargeRecord();
        record.setPlayerId(playerId);
        record.setAmount(amount);
        record.setYuanbao(0);
        record.setOrderNo(generateOrderNo());
        record.setStatus("PENDING");
        record.setCreatedAt(LocalDateTime.now());

        rechargeRecordMapper.insert(record);
        return record;
    }

    @Transactional
    public RechargeRecord processRechargeSuccess(Long orderId) {
        RechargeRecord record = rechargeRecordMapper.selectById(orderId);
        if (record == null) {
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_NOT_FOUND);
        }

        if ("SUCCESS".equals(record.getStatus())) {
            log.info("充值订单已完成，忽略重复成功回调: orderId={}", orderId);
            return record;
        }
        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_STATUS_INVALID);
        }

        LocalDateTime completedAt = LocalDateTime.now();
        int markedRows = rechargeRecordMapper.markRechargeSuccessIfPending(orderId, completedAt);
        if (markedRows == 0) {
            RechargeRecord latestRecord = rechargeRecordMapper.selectById(orderId);
            if (latestRecord != null && "SUCCESS".equals(latestRecord.getStatus())) {
                log.info("充值订单状态已变更，忽略重复成功回调: orderId={}, status={}",
                        orderId, latestRecord.getStatus());
                return latestRecord;
            }
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_STATUS_INVALID);
        }

        PlayerVip playerVip = vipService.updateVipInfo(record.getPlayerId(), record.getAmount());
        Integer previousTotalRecharge = playerVip.getTotalRecharge() - record.getAmount();
        boolean isFirstRecharge = previousTotalRecharge == 0;

        long yuanbaoToAdd = record.getAmount() * 10L;
        if (isFirstRecharge) {
            yuanbaoToAdd = yuanbaoToAdd * 3 / 2;
        }
        record.setYuanbao((int) Math.min(yuanbaoToAdd, Integer.MAX_VALUE));
        record.setStatus("SUCCESS");
        record.setCompletedAt(completedAt);
        rechargeRecordMapper.updateById(record);

        log.info("充值处理成功: playerId={}, amount={}, yuanbao={}, isFirstRecharge={}",
                record.getPlayerId(), record.getAmount(), yuanbaoToAdd, isFirstRecharge);
        return record;
    }

    @Transactional
    public RechargeRecord processRechargeFailed(Long orderId) {
        RechargeRecord record = rechargeRecordMapper.selectById(orderId);
        if (record == null) {
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_NOT_FOUND);
        }

        if ("FAILED".equals(record.getStatus())) {
            log.info("充值订单已失败，忽略重复失败回调: orderId={}", orderId);
            return record;
        }
        if ("SUCCESS".equals(record.getStatus())) {
            log.info("充值订单已成功，忽略失败回调: orderId={}", orderId);
            return record;
        }
        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_STATUS_INVALID);
        }

        LocalDateTime completedAt = LocalDateTime.now();
        int markedRows = rechargeRecordMapper.markRechargeFailedIfPending(orderId, completedAt);
        if (markedRows == 0) {
            RechargeRecord latestRecord = rechargeRecordMapper.selectById(orderId);
            if (latestRecord != null
                    && ("FAILED".equals(latestRecord.getStatus()) || "SUCCESS".equals(latestRecord.getStatus()))) {
                log.info("充值订单状态已变更，忽略失败回调: orderId={}, status={}",
                        orderId, latestRecord.getStatus());
                return latestRecord;
            }
            throw new BusinessException(ErrorCode.RECHARGE_ORDER_STATUS_INVALID);
        }

        record.setStatus("FAILED");
        record.setCompletedAt(completedAt);
        return record;
    }

    public List<RechargeRecord> getPlayerRechargeRecords(Integer playerId, int limit) {
        QueryWrapper<RechargeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("LIMIT " + limit);
        return rechargeRecordMapper.selectList(queryWrapper);
    }

    public List<RechargeRecord> getSuccessRechargesByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<RechargeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "SUCCESS")
                .between("completed_at", startTime, endTime);
        return rechargeRecordMapper.selectList(wrapper);
    }

    private String generateOrderNo() {
        return "RECHARGE_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(10000);
    }
}
