package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerVip;
import com.xiuxian.game.entity.RechargeRecord;
import com.xiuxian.game.exception.BusinessException;
import com.xiuxian.game.exception.ErrorCode;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.RechargeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RechargeService extends ServiceImpl<RechargeRecordMapper, RechargeRecord> {
    
    private final RechargeRecordMapper rechargeRecordMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final VipService vipService;
    
    /**
     * 创建充值订单
     * @param playerId 玩家ID
     * @param amount 充值金额(单位:分)
     * @return 充值记录
     */
    public RechargeRecord createRechargeOrder(Integer playerId, Integer amount) {
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
        
        // 更新订单状态
        record.setStatus("SUCCESS");
        record.setCompletedAt(LocalDateTime.now());
        
        // 更新玩家VIP信息和元宝数量
        PlayerVip playerVip = vipService.updateVipInfo(record.getPlayerId(), record.getAmount());
        
        // 计算本次充值获得的元宝数
        Integer previousTotalRecharge = playerVip.getTotalRecharge() - record.getAmount();
        Integer previousYuanbao = previousTotalRecharge * 10;
        
        // 检查是否为首充
        boolean isFirstRecharge = previousTotalRecharge == 0;
        long yuanbaoToAdd = record.getAmount() * 10;
        if (isFirstRecharge) {
            // 首充额外奖励50%元宝
            yuanbaoToAdd = yuanbaoToAdd * 3 / 2;
        }
        
        record.setYuanbao((int) yuanbaoToAdd);
        
        // 更新玩家元宝数量
        PlayerProfile playerProfile = playerProfileMapper.selectById(record.getPlayerId());
        playerProfile.setSpiritStones(playerProfile.getSpiritStones() + yuanbaoToAdd);
        playerProfileMapper.updateById(playerProfile);
        
        rechargeRecordMapper.updateById(record);
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
        
        // 更新订单状态
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
        // 查询玩家最近的充值记录
        QueryWrapper<RechargeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("LIMIT " + limit);
        
        return rechargeRecordMapper.selectList(queryWrapper);
    }
    
    /**
     * 生成订单号
     * @return 订单号
     */
    private String generateOrderNo() {
        return "RECHARGE_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(10000);
    }
}