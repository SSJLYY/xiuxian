package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerVip;
import com.xiuxian.game.entity.VipLevel;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.PlayerVipMapper;
import com.xiuxian.game.mapper.VipLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VipService extends ServiceImpl<PlayerVipMapper, PlayerVip> {
    
    private final PlayerVipMapper playerVipMapper;
    private final VipLevelMapper vipLevelMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final MailService mailService;
    
    /**
     * 获取玩家VIP信息
     * @param playerId 玩家ID
     * @return 玩家VIP信息
     */
    public PlayerVip getPlayerVip(Integer playerId) {
        PlayerVip playerVip = playerVipMapper.selectOne(new QueryWrapper<PlayerVip>().eq("player_id", playerId));
        if (playerVip == null) {
            // 创建新的VIP记录
            playerVip = new PlayerVip();
            playerVip.setPlayerId(playerId);
            playerVip.setVipLevel(0);
            playerVip.setTotalRecharge(0);
            playerVip.setYuanbao(0);
            playerVip.setLastDailyRewardAt(LocalDateTime.now().minusDays(1)); // 设置为昨天，确保可以领取
            playerVipMapper.insert(playerVip);
        }
        return playerVip;
    }
    
    /**
     * 获取所有VIP等级配置
     * @return VIP等级列表
     */
    public List<VipLevel> getAllVipLevels() {
        return vipLevelMapper.selectList(new QueryWrapper<VipLevel>().orderByAsc("level"));
    }
    
    /**
     * 根据充值金额计算VIP等级
     * @param totalRecharge 累计充值金额
     * @return VIP等级
     */
    public Integer calculateVipLevel(Integer totalRecharge) {
        List<VipLevel> vipLevels = getAllVipLevels();
        Integer vipLevel = 0;
        
        // 从高到低查找合适的VIP等级
        for (int i = vipLevels.size() - 1; i >= 0; i--) {
            VipLevel level = vipLevels.get(i);
            if (totalRecharge >= level.getRequiredRecharge()) {
                vipLevel = level.getLevel();
                break;
            }
        }
        
        return vipLevel;
    }
    
    /**
     * 更新玩家VIP信息
     * @param playerId 玩家ID
     * @param rechargeAmount 充值金额
     * @return 更新后的玩家VIP信息
     */
    public PlayerVip updateVipInfo(Integer playerId, Integer rechargeAmount) {
        PlayerVip playerVip = getPlayerVip(playerId);
        
        // 更新累计充值金额
        playerVip.setTotalRecharge(playerVip.getTotalRecharge() + rechargeAmount);
        
        // 计算新的VIP等级
        Integer newVipLevel = calculateVipLevel(playerVip.getTotalRecharge());
        Integer oldVipLevel = playerVip.getVipLevel();
        
        // 更新VIP等级
        playerVip.setVipLevel(newVipLevel);
        
        // 增加元宝（假设1元=10元宝）
        Integer yuanbaoToAdd = rechargeAmount * 10;
        
        // 检查是否为首充（累计充值金额等于本次充值金额）
        boolean isFirstRecharge = playerVip.getTotalRecharge().equals(rechargeAmount);
        if (isFirstRecharge) {
            // 首充额外奖励50%元宝
            yuanbaoToAdd = yuanbaoToAdd * 3 / 2;
        }
        
        playerVip.setYuanbao(playerVip.getYuanbao() + yuanbaoToAdd);
        
        // 更新数据库
        playerVipMapper.updateById(playerVip);
        
        // 如果VIP等级提升，发送奖励邮件
        if (newVipLevel > oldVipLevel) {
            sendVipUpgradeReward(playerId, newVipLevel);
        }
        
        return playerVip;
    }
    
    /**
     * 发送VIP升级奖励邮件
     * @param playerId 玩家ID
     * @param newVipLevel 新的VIP等级
     */
    private void sendVipUpgradeReward(Integer playerId, Integer newVipLevel) {
        String subject = "VIP等级提升奖励";
        String content = String.format("恭喜您的VIP等级提升至%d级！获得了丰厚的奖励！", newVipLevel);
        
        // 发送邮件给玩家
        mailService.sendSystemMail(playerId, subject, content, null, null, 0);
    }
    
    /**
     * 领取VIP每日奖励
     * @param playerId 玩家ID
     * @return 是否成功领取
     */
    public boolean claimDailyReward(Integer playerId) {
        PlayerVip playerVip = getPlayerVip(playerId);
        
        // 检查今天是否已经领取过
        LocalDateTime lastRewardTime = playerVip.getLastDailyRewardAt();
        LocalDateTime now = LocalDateTime.now();
        
        // 如果是同一天，则不能重复领取
        if (lastRewardTime.toLocalDate().equals(now.toLocalDate())) {
            return false;
        }
        
        // 获取当前VIP等级对应的奖励
        VipLevel currentVipLevel = getVipLevelConfig(playerVip.getVipLevel());
        if (currentVipLevel != null && currentVipLevel.getDailySpiritStones() > 0) {
            // 发送每日奖励邮件
            String subject = "VIP每日奖励";
            String content = String.format("VIP%d每日奖励已发放，包括%d灵石。", 
                                         playerVip.getVipLevel(), 
                                         currentVipLevel.getDailySpiritStones());
            
            mailService.sendSystemMail(playerId, subject, content, "ITEM", 6, currentVipLevel.getDailySpiritStones());
            
            // 更新最后领取时间
            playerVip.setLastDailyRewardAt(now);
            playerVipMapper.updateById(playerVip);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取指定VIP等级的配置
     * @param level VIP等级
     * @return VIP等级配置
     */
    public VipLevel getVipLevelConfig(Integer level) {
        return vipLevelMapper.selectOne(new QueryWrapper<VipLevel>().eq("level", level));
    }
    
    /**
     * 检查玩家是否有指定VIP特权
     * @param playerId 玩家ID
     * @param requiredVipLevel 所需VIP等级
     * @return 是否有权限
     */
    public boolean hasVipPrivilege(Integer playerId, Integer requiredVipLevel) {
        PlayerVip playerVip = getPlayerVip(playerId);
        return playerVip.getVipLevel() >= requiredVipLevel;
    }
}