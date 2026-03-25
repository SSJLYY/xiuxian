package com.xiuxian.game.modules.giftcode.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xiuxian.game.modules.giftcode.entity.GiftCode;
import com.xiuxian.game.modules.giftcode.entity.GiftCodeUsage;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.giftcode.mapper.GiftCodeMapper;
import com.xiuxian.game.modules.giftcode.mapper.GiftCodeUsageMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GiftCodeService extends ServiceImpl<GiftCodeMapper, GiftCode> {

    private final GiftCodeMapper giftCodeMapper;
    private final GiftCodeUsageMapper giftCodeUsageMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据
    private final MailService mailService;
    private final ObjectMapper objectMapper;

    /**
     * 兑换礼包码
     *
     * @param playerId 玎家ID
     * @param code     礼包码
     * @return 兑换结果
     */
    @Transactional
    public boolean redeemGiftCode(Integer playerId, String code) {
        // 模块边界：通过PlayerService访问玩家数据
        QueryWrapper<GiftCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        GiftCode giftCode = giftCodeMapper.selectOne(queryWrapper);

        // 检查礼包码是否存在
        if (giftCode == null) {
            throw new IllegalArgumentException("礼包码不存在");
        }

        // 模块边界：通过PlayerService访问玩家数据
        if (!"ACTIVE".equals(giftCode.getStatus())) {
            throw new IllegalArgumentException("礼包码已失效");
        }

        // 模块边界：通过PlayerService访问玩家数据
        if (giftCode.getExpireAt() != null && giftCode.getExpireAt().isBefore(LocalDateTime.now())) {
            // 模块边界：通过PlayerService访问玩家数据
            giftCode.setStatus("EXPIRED");
            giftCodeMapper.updateById(giftCode);
            throw new IllegalArgumentException("礼包码已过期");
        }

        // 模块边界：通过PlayerService访问玩家数据
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        if (player.getLevel() < giftCode.getMinLevel()) {
            throw new IllegalArgumentException("玩家等级不足，需要达到" + giftCode.getMinLevel() + "级");
        }

        // 模块边界：通过PlayerService访问玩家数据
        if ("UNIQUE".equals(giftCode.getCodeType())) {
            QueryWrapper<GiftCodeUsage> usageQuery = new QueryWrapper<>();
            usageQuery.eq("gift_code_id", giftCode.getId());
            usageQuery.eq("player_id", playerId);
            if (giftCodeUsageMapper.selectCount(usageQuery) > 0) {
                throw new IllegalArgumentException("您已经使用过此礼包码");
            }
        }

        // 模块边界：通过PlayerService访问玩家数据
        if (giftCode.getUsedCount() >= giftCode.getMaxUsage()) {
            giftCode.setStatus("DISABLED");
            giftCodeMapper.updateById(giftCode);
            throw new IllegalArgumentException("礼包码已被使用完");
        }

        // 记录使用情况
        GiftCodeUsage usage = new GiftCodeUsage();
        usage.setGiftCodeId(giftCode.getId());
        usage.setPlayerId(playerId);
        giftCodeUsageMapper.insert(usage);

        // 增加使用次数
        giftCode.setUsedCount(giftCode.getUsedCount() + 1);
        // 模块边界：通过PlayerService访问玩家数据
        if (giftCode.getUsedCount() >= giftCode.getMaxUsage()) {
            giftCode.setStatus("DISABLED");
        }
        giftCodeMapper.updateById(giftCode);

        // 发放奖励
        distributeRewards(playerId, giftCode.getRewards());

        return true;
    }

    /**
     * 发放奖励
     *
     * @param playerId 玎家ID
     * @param rewards  奖励内容（JSON格式）
     */
    private void distributeRewards(Integer playerId, String rewards) {
        try {
            List<Map<String, Object>> rewardList = objectMapper.readValue(rewards, new TypeReference<List<Map<String, Object>>>() {});
            
            for (Map<String, Object> reward : rewardList) {
                String type = (String) reward.get("type");
                Integer id = (Integer) reward.get("id");
                Integer quantity = (Integer) reward.get("quantity");

                switch (type) {
                    case "SPIRIT_STONES":
                        // 发放灵石
                        PlayerProfile player = playerService.getPlayerProfileById(playerId);
                        player.setSpiritStones(player.getSpiritStones() + quantity);
                        playerService.savePlayerProfile(player);
                        break;
                    case "ITEM":
                        // 模块边界：通过PlayerService访问玩家数据
                        mailService.sendSystemMail(playerId, "礼包码奖励", "您通过礼包码获得了物品奖励", "ITEM", id, quantity);
                        break;
                    case "EQUIPMENT":
                        // 模块边界：通过PlayerService访问玩家数据
                        mailService.sendSystemMail(playerId, "礼包码奖励", "您通过礼包码获得了装备奖励", "EQUIPMENT", id, quantity);
                        break;
                    default:
                        // 模块边界：通过PlayerService访问玩家数据
                        mailService.sendSystemMail(playerId, "礼包码奖励", "您通过礼包码获得了奖励", type, id, quantity);
                        break;
                }
            }
        } catch (Exception e) {
            throw new com.xiuxian.game.exception.BusinessException(
                    com.xiuxian.game.exception.ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 创建礼包码
     *
     * @param giftCode 礼包码信息
     * @return 创建的礼包码
     */
    @Transactional
    public GiftCode createGiftCode(GiftCode giftCode) {
        giftCode.setUsedCount(0);
        giftCode.setStatus("ACTIVE");
        giftCode.setCreatedAt(LocalDateTime.now());
        giftCodeMapper.insert(giftCode);
        return giftCode;
    }

    /**
     * 获取礼包码使用记录
     *
     * @param giftCodeId 礼包码ID
     * @return 使用记录列表
     */
    public List<GiftCodeUsage> getGiftCodeUsageHistory(Long giftCodeId) {
        QueryWrapper<GiftCodeUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("gift_code_id", giftCodeId);
        queryWrapper.orderByDesc("used_at");
        return giftCodeUsageMapper.selectList(queryWrapper);
    }
}
