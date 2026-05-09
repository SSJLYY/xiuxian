package com.xiuxian.game.modules.giftcode.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.giftcode.entity.GiftCode;
import com.xiuxian.game.modules.giftcode.entity.GiftCodeUsage;
import com.xiuxian.game.modules.giftcode.mapper.GiftCodeMapper;
import com.xiuxian.game.modules.giftcode.mapper.GiftCodeUsageMapper;
import com.xiuxian.game.modules.mail.service.MailService;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCodeService extends ServiceImpl<GiftCodeMapper, GiftCode> {

    private final GiftCodeMapper giftCodeMapper;
    private final GiftCodeUsageMapper giftCodeUsageMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;
    private final MailService mailService;
    private final ObjectMapper objectMapper;

    @Transactional
    public boolean redeemGiftCode(Integer playerId, String code) {
        log.info("玩家兑换礼包码: playerId={}, code={}", playerId, code);

        QueryWrapper<GiftCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        GiftCode giftCode = giftCodeMapper.selectOne(queryWrapper);
        LocalDateTime now = LocalDateTime.now();
        PlayerProfile lockedPlayer = playerProfileMapper.selectByIdForUpdate(playerId);
        if (lockedPlayer == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        if (giftCode == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码不存在");
        }
        if (!"ACTIVE".equalsIgnoreCase(giftCode.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已失效");
        }
        if (giftCode.getExpireAt() != null && !giftCode.getExpireAt().isAfter(now)) {
            giftCode.setStatus("EXPIRED");
            giftCodeMapper.updateById(giftCode);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已过期");
        }
        if (giftCode.getMinLevel() != null && defaultInt(lockedPlayer.getLevel(), 1) < giftCode.getMinLevel()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    "玩家等级不足，需要达到" + giftCode.getMinLevel() + "级");
        }

        if ("UNIQUE".equalsIgnoreCase(giftCode.getCodeType()) && hasPlayerRedeemed(playerId, giftCode.getId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "您已经使用过此礼包码");
        }

        int consumedRows = giftCodeMapper.consumeUsageIfAvailable(giftCode.getId(), now);
        if (consumedRows == 0) {
            GiftCode latestGiftCode = giftCodeMapper.selectById(giftCode.getId());
            if (latestGiftCode != null
                    && latestGiftCode.getExpireAt() != null
                    && !latestGiftCode.getExpireAt().isAfter(now)) {
                latestGiftCode.setStatus("EXPIRED");
                giftCodeMapper.updateById(latestGiftCode);
                throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已过期");
            }
            if (latestGiftCode != null && !"ACTIVE".equalsIgnoreCase(latestGiftCode.getStatus())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已失效");
            }
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已被使用完");
        }

        if ("UNIQUE".equalsIgnoreCase(giftCode.getCodeType())) {
            int insertedRows = giftCodeUsageMapper.insertIfAbsent(giftCode.getId(), playerId);
            if (insertedRows == 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "您已经使用过此礼包码");
            }
        } else {
            GiftCodeUsage usage = new GiftCodeUsage();
            usage.setGiftCodeId(giftCode.getId());
            usage.setPlayerId(playerId);
            giftCodeUsageMapper.insert(usage);
        }

        distributeRewards(lockedPlayer, giftCode.getRewards());
        return true;
    }

    private boolean hasPlayerRedeemed(Integer playerId, Long giftCodeId) {
        QueryWrapper<GiftCodeUsage> usageQuery = new QueryWrapper<>();
        usageQuery.eq("gift_code_id", giftCodeId);
        usageQuery.eq("player_id", playerId);
        return giftCodeUsageMapper.selectCount(usageQuery) > 0;
    }

    private void distributeRewards(PlayerProfile player, String rewardsJson) {
        Integer playerId = player.getId();
        List<Map<String, Object>> rewardList = parseRewardPayload(playerId, rewardsJson);
        boolean profileChanged = false;

        for (Map<String, Object> reward : rewardList) {
            String type = reward.get("type") == null ? "" : String.valueOf(reward.get("type"));
            Integer id = toInteger(reward.get("id"));
            Integer quantity = toInteger(reward.get("quantity"));
            int resolvedQuantity = quantity == null ? 0 : quantity;

            switch (type.toUpperCase()) {
                case "SPIRIT_STONES":
                    player.setSpiritStones(defaultLong(player.getSpiritStones()) + resolvedQuantity);
                    profileChanged = true;
                    break;
                case "EXP":
                    player.setExp(defaultLong(player.getExp()) + resolvedQuantity);
                    profileChanged = true;
                    break;
                case "ITEM":
                    sendGiftCodeRewardMail(playerId, "ITEM", id, resolvedQuantity,
                            "礼包码奖励", "您通过礼包码获得了物品奖励");
                    break;
                case "EQUIPMENT":
                    sendGiftCodeRewardMail(playerId, "EQUIPMENT", id, resolvedQuantity,
                            "礼包码奖励", "您通过礼包码获得了装备奖励");
                    break;
                default:
                    sendGiftCodeRewardMail(playerId, type, id, resolvedQuantity,
                            "礼包码奖励", "您通过礼包码获得了奖励");
                    break;
            }
        }

        if (profileChanged) {
            playerService.applyLevelUpsWithoutCommit(player, 100);
            playerService.savePlayerProfile(player);
        }
    }

    private List<Map<String, Object>> parseRewardPayload(Integer playerId, String rewardsJson) {
        try {
            return objectMapper.readValue(
                    rewardsJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (JsonProcessingException e) {
            log.error("发放礼包码奖励失败: playerId={}, error={}", playerId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private void sendGiftCodeRewardMail(Integer playerId, String type, Integer id, int quantity,
                                        String subject, String content) {
        mailService.sendSystemMail(playerId, subject, content, type, id, quantity);
    }

    @Transactional
    public GiftCode createGiftCode(GiftCode giftCode) {
        giftCode.setUsedCount(0);
        giftCode.setStatus("ACTIVE");
        giftCode.setCreatedAt(LocalDateTime.now());
        giftCodeMapper.insert(giftCode);
        return giftCode;
    }

    public List<PlayerGiftCodeRecord> getPlayerGiftCodeHistory(Integer playerId) {
        QueryWrapper<GiftCodeUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.orderByDesc("used_at");

        List<GiftCodeUsage> usageList = giftCodeUsageMapper.selectList(queryWrapper);
        List<PlayerGiftCodeRecord> records = new ArrayList<>(usageList.size());

        for (GiftCodeUsage usage : usageList) {
            GiftCode giftCode = giftCodeMapper.selectById(usage.getGiftCodeId());
            if (giftCode == null) {
                continue;
            }

            PlayerGiftCodeRecord record = new PlayerGiftCodeRecord();
            record.setCodeMasked(maskCode(giftCode.getCode()));
            record.setRedeemedAt(usage.getUsedAt());
            record.setRewards(parseRewardItems(giftCode.getRewards()));
            records.add(record);
        }

        return records;
    }

    public List<AvailableGiftCodeInfo> getAvailableGiftCodes(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<GiftCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE");
        queryWrapper.orderByDesc("created_at");

        List<GiftCode> giftCodes = giftCodeMapper.selectList(queryWrapper);
        List<AvailableGiftCodeInfo> availableCodes = new ArrayList<>();

        for (GiftCode giftCode : giftCodes) {
            if (!isGiftCodeAvailableForPlayer(giftCode, playerId, player.getLevel(), now)) {
                continue;
            }

            List<RewardItemDto> rewards = parseRewardItems(giftCode.getRewards());

            AvailableGiftCodeInfo info = new AvailableGiftCodeInfo();
            info.setId(giftCode.getId());
            info.setTitle(giftCode.getName() != null && !giftCode.getName().trim().isEmpty()
                    ? giftCode.getName()
                    : giftCode.getCode());
            info.setDescription(buildAvailableDescription(giftCode));
            info.setRewardDescription(buildRewardSummary(rewards));
            info.setExpiryDate(giftCode.getExpireAt());
            availableCodes.add(info);
        }

        return availableCodes;
    }

    public List<GiftCodeUsage> getGiftCodeUsageHistory(Long giftCodeId) {
        QueryWrapper<GiftCodeUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("gift_code_id", giftCodeId);
        queryWrapper.orderByDesc("used_at");
        return giftCodeUsageMapper.selectList(queryWrapper);
    }

    private boolean isGiftCodeAvailableForPlayer(GiftCode giftCode, Integer playerId, Integer playerLevel,
                                                 LocalDateTime now) {
        if (giftCode == null || !"ACTIVE".equalsIgnoreCase(giftCode.getStatus())) {
            return false;
        }
        if (giftCode.getExpireAt() != null && !giftCode.getExpireAt().isAfter(now)) {
            return false;
        }
        if (giftCode.getMinLevel() != null && playerLevel < giftCode.getMinLevel()) {
            return false;
        }
        if (giftCode.getMaxUsage() != null && giftCode.getUsedCount() != null
                && giftCode.getUsedCount() >= giftCode.getMaxUsage()) {
            return false;
        }
        return !"UNIQUE".equalsIgnoreCase(giftCode.getCodeType()) || !hasPlayerRedeemed(playerId, giftCode.getId());
    }

    private List<RewardItemDto> parseRewardItems(String rewardsJson) {
        if (rewardsJson == null || rewardsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<Map<String, Object>> rewardList = objectMapper.readValue(
                    rewardsJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            List<RewardItemDto> rewards = new ArrayList<>(rewardList.size());

            for (Map<String, Object> reward : rewardList) {
                String type = reward.get("type") == null ? "UNKNOWN" : String.valueOf(reward.get("type"));
                Integer itemId = toInteger(reward.get("id"));
                Integer quantityValue = toInteger(reward.get("quantity"));
                int quantity = quantityValue == null ? 0 : quantityValue;

                RewardItemDto rewardItem = new RewardItemDto();
                rewardItem.setType(type);
                rewardItem.setId(itemId);
                rewardItem.setQuantity(quantity);
                rewardItem.setDescription(buildRewardDescription(type, itemId, quantity));
                rewards.add(rewardItem);
            }

            return rewards;
        } catch (Exception e) {
            log.warn("解析礼包码奖励失败: rewards={}", rewardsJson, e);
            return Collections.emptyList();
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildRewardSummary(List<RewardItemDto> rewards) {
        if (rewards.isEmpty()) {
            return "暂无奖励";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rewards.size(); i++) {
            if (i > 0) {
                builder.append("、");
            }
            builder.append(rewards.get(i).getDescription());
        }
        return builder.toString();
    }

    private String buildRewardDescription(String type, Integer itemId, int quantity) {
        switch (type.toUpperCase()) {
            case "SPIRIT_STONES":
                return "灵石 x" + quantity;
            case "EXP":
                return "经验 x" + quantity;
            case "ITEM":
                return itemId == null ? "道具 x" + quantity : "道具#" + itemId + " x" + quantity;
            case "EQUIPMENT":
                return itemId == null ? "装备 x" + quantity : "装备#" + itemId + " x" + quantity;
            default:
                return type + " x" + quantity;
        }
    }

    private String buildAvailableDescription(GiftCode giftCode) {
        StringBuilder builder = new StringBuilder();
        builder.append("礼包码类型：")
                .append("UNIQUE".equalsIgnoreCase(giftCode.getCodeType()) ? "唯一码" : "通用码");

        if (giftCode.getMinLevel() != null && giftCode.getMinLevel() > 0) {
            builder.append("，最低等级：").append(giftCode.getMinLevel());
        }
        if (giftCode.getMaxUsage() != null && giftCode.getMaxUsage() > 0) {
            builder.append("，最大使用次数：").append(giftCode.getMaxUsage());
        }

        return builder.toString();
    }

    private String maskCode(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }
        if (code.length() <= 4) {
            return code.charAt(0) + "***";
        }
        return code.substring(0, 2) + "***" + code.substring(code.length() - 2);
    }

    @Data
    public static class PlayerGiftCodeRecord {
        private String codeMasked;
        private LocalDateTime redeemedAt;
        private List<RewardItemDto> rewards = Collections.emptyList();
    }

    @Data
    public static class RewardItemDto {
        private String type;
        private Integer id;
        private int quantity;
        private String description;
    }

    @Data
    public static class AvailableGiftCodeInfo {
        private Long id;
        private String title;
        private String description;
        private String rewardDescription;
        private LocalDateTime expiryDate;
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
