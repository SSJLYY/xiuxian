package com.xiuxian.game.modules.vip.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.mail.service.MailService;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.vip.entity.PlayerVip;
import com.xiuxian.game.modules.vip.entity.VipLevel;
import com.xiuxian.game.modules.vip.mapper.PlayerVipMapper;
import com.xiuxian.game.modules.vip.mapper.VipLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VipService extends ServiceImpl<PlayerVipMapper, PlayerVip> {

    private final PlayerVipMapper playerVipMapper;
    private final VipLevelMapper vipLevelMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final MailService mailService;

    public PlayerVip getPlayerVip(Integer playerId) {
        PlayerVip playerVip = playerVipMapper.selectOne(new QueryWrapper<PlayerVip>().eq("player_id", playerId));
        if (playerVip == null) {
            LocalDateTime defaultRewardTime = LocalDateTime.now().minusDays(1);
            playerVipMapper.insertIfAbsent(playerId, 0, 0, 0, defaultRewardTime);
            playerVip = playerVipMapper.selectOne(new QueryWrapper<PlayerVip>().eq("player_id", playerId));
        }
        if (playerVip == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        normalizePlayerVip(playerVip);
        return playerVip;
    }

    public List<VipLevel> getAllVipLevels() {
        return vipLevelMapper.selectList(new QueryWrapper<VipLevel>().orderByAsc("level"));
    }

    public Integer calculateVipLevel(Integer totalRecharge) {
        List<VipLevel> vipLevels = getAllVipLevels();
        int safeRecharge = defaultInt(totalRecharge);
        Integer vipLevel = 0;

        for (int i = vipLevels.size() - 1; i >= 0; i--) {
            VipLevel level = vipLevels.get(i);
            if (safeRecharge >= level.getRequiredRecharge()) {
                vipLevel = level.getLevel();
                break;
            }
        }

        return vipLevel;
    }

    @Transactional(rollbackFor = Exception.class)
    public PlayerVip updateVipInfo(Integer playerId, Integer rechargeAmount) {
        PlayerProfile lockedPlayer = playerProfileMapper.selectByIdForUpdate(playerId);
        if (lockedPlayer == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        if (rechargeAmount == null || rechargeAmount <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "充值金额必须大于0");
        }

        PlayerVip playerVip = getPlayerVip(playerId);
        int currentRecharge = defaultInt(playerVip.getTotalRecharge());
        int oldVipLevel = defaultInt(playerVip.getVipLevel());

        playerVip.setTotalRecharge(currentRecharge + rechargeAmount);

        Integer newVipLevel = calculateVipLevel(playerVip.getTotalRecharge());
        playerVip.setVipLevel(newVipLevel);

        int yuanbaoToAdd = rechargeAmount * 10;
        if (currentRecharge == 0) {
            yuanbaoToAdd = yuanbaoToAdd * 3 / 2;
        }

        playerVip.setYuanbao(defaultInt(playerVip.getYuanbao()) + yuanbaoToAdd);
        playerVipMapper.updateById(playerVip);

        if (newVipLevel > oldVipLevel) {
            sendVipUpgradeReward(playerId, newVipLevel);
        }

        return playerVip;
    }

    private void sendVipUpgradeReward(Integer playerId, Integer newVipLevel) {
        String subject = "VIP等级提升奖励";
        String content = String.format("恭喜您的VIP等级提升到%d级，奖励已发放。", newVipLevel);
        mailService.sendSystemMail(playerId, subject, content, null, null, 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean claimDailyReward(Integer playerId) {
        PlayerProfile lockedPlayer = playerProfileMapper.selectByIdForUpdate(playerId);
        if (lockedPlayer == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        PlayerVip playerVip = getPlayerVip(playerId);
        LocalDateTime lastRewardTime = playerVip.getLastDailyRewardAt();
        LocalDateTime now = LocalDateTime.now();
        if (lastRewardTime != null && lastRewardTime.toLocalDate().equals(now.toLocalDate())) {
            return false;
        }

        VipLevel currentVipLevel = getVipLevelConfig(playerVip.getVipLevel());
        if (currentVipLevel != null && currentVipLevel.getDailySpiritStones() > 0) {
            playerVip.setLastDailyRewardAt(now);
            playerVipMapper.updateById(playerVip);

            String subject = "VIP每日奖励";
            String content = String.format("VIP%d每日奖励已发放，包含%d灵石。",
                    defaultInt(playerVip.getVipLevel()),
                    currentVipLevel.getDailySpiritStones());
            mailService.sendSystemMail(playerId, subject, content, "SPIRIT_STONES", null,
                    currentVipLevel.getDailySpiritStones());
            return true;
        }

        return false;
    }

    public VipLevel getVipLevelConfig(Integer level) {
        return vipLevelMapper.selectOne(new QueryWrapper<VipLevel>().eq("level", level));
    }

    public boolean hasVipPrivilege(Integer playerId, Integer requiredVipLevel) {
        PlayerVip playerVip = getPlayerVip(playerId);
        return defaultInt(playerVip.getVipLevel()) >= requiredVipLevel;
    }

    private void normalizePlayerVip(PlayerVip playerVip) {
        if (playerVip.getVipLevel() == null) {
            playerVip.setVipLevel(0);
        }
        if (playerVip.getTotalRecharge() == null) {
            playerVip.setTotalRecharge(0);
        }
        if (playerVip.getYuanbao() == null) {
            playerVip.setYuanbao(0);
        }
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
