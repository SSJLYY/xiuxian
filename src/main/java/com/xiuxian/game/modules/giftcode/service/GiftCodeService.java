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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCodeService extends ServiceImpl<GiftCodeMapper, GiftCode> {

    private static final List<String> ALLOWED_CODE_TYPES = Arrays.asList("UNIVERSAL", "UNIQUE");
    private static final List<String> ALLOWED_STATUSES = Arrays.asList("ACTIVE", "DISABLED", "EXPIRED");

    private final GiftCodeMapper giftCodeMapper;
    private final GiftCodeUsageMapper giftCodeUsageMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;
    private final MailService mailService;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Transactional
    public boolean redeemGiftCode(Integer playerId, String code) {
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
            markGiftCodeExpiredOutsideTransaction(giftCode.getId());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已过期");
        }
        if (giftCode.getMinLevel() != null && defaultInt(lockedPlayer.getLevel(), 1) < giftCode.getMinLevel()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    "玩家等级不足，需达到 " + giftCode.getMinLevel() + " 级");
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
                markGiftCodeExpiredOutsideTransaction(latestGiftCode.getId());
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

    @Transactional
    public GiftCode createGiftCode(GiftCode giftCode) {
        GiftCode normalized = normalizeGiftCodeForWrite(giftCode, true);
        normalized.setUsedCount(0);
        normalized.setStatus("ACTIVE");
        normalized.setCreatedAt(LocalDateTime.now());
        giftCodeMapper.insert(normalized);
        return normalized;
    }

    @Transactional
    public GiftCode updateGiftCode(Long id, GiftCode giftCode) {
        GiftCode existing = giftCodeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "礼包码不存在");
        }

        GiftCode normalized = normalizeGiftCodeForWrite(giftCode, false);
        normalized.setId(id);
        normalized.setUsedCount(existing.getUsedCount());
        if (normalized.getStatus() == null || normalized.getStatus().trim().isEmpty()) {
            normalized.setStatus(existing.getStatus());
        }
        if (normalized.getCreatedAt() == null) {
            normalized.setCreatedAt(existing.getCreatedAt());
        }

        boolean updated = this.updateById(normalized);
        if (!updated) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_FAILED, "礼包码更新失败");
        }
        return giftCodeMapper.selectById(id);
    }

    @Transactional
    public void deleteGiftCode(Long id) {
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "礼包码不存在");
        }
    }

    public List<PlayerGiftCodeRecord> getPlayerGiftCodeHistory(Integer playerId) {
        QueryWrapper<GiftCodeUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId).orderByDesc("used_at");

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
        queryWrapper.eq("status", "ACTIVE").orderByDesc("created_at");

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
                    ? giftCode.getName() : giftCode.getCode());
            info.setDescription(buildAvailableDescription(giftCode));
            info.setRewardDescription(buildRewardSummary(rewards));
            info.setExpiryDate(giftCode.getExpireAt());
            availableCodes.add(info);
        }
        return availableCodes;
    }

    public List<GiftCodeUsage> getGiftCodeUsageHistory(Long giftCodeId) {
        QueryWrapper<GiftCodeUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("gift_code_id", giftCodeId).orderByDesc("used_at");
        return giftCodeUsageMapper.selectList(queryWrapper);
    }

    private boolean hasPlayerRedeemed(Integer playerId, Long giftCodeId) {
        QueryWrapper<GiftCodeUsage> usageQuery = new QueryWrapper<>();
        usageQuery.eq("gift_code_id", giftCodeId).eq("player_id", playerId);
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
                    sendGiftCodeRewardMail(playerId, "ITEM", id, resolvedQuantity, "礼包码奖励", "您通过礼包码获得了物品奖励");
                    break;
                case "EQUIPMENT":
                    sendGiftCodeRewardMail(playerId, "EQUIPMENT", id, resolvedQuantity, "礼包码奖励", "您通过礼包码获得了装备奖励");
                    break;
                default:
                    sendGiftCodeRewardMail(playerId, type, id, resolvedQuantity, "礼包码奖励", "您通过礼包码获得了奖励");
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
            return objectMapper.readValue(rewardsJson, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Parse gift code rewards failed: playerId={}, error={}", playerId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private void sendGiftCodeRewardMail(Integer playerId, String type, Integer id, int quantity,
                                        String subject, String content) {
        mailService.sendSystemMail(playerId, subject, content, type, id, quantity);
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
                    rewardsJson, new TypeReference<List<Map<String, Object>>>() {
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
            log.warn("Parse gift code reward summary failed: rewards={}", rewardsJson, e);
            return Collections.emptyList();
        }
    }

    private GiftCode normalizeGiftCodeForWrite(GiftCode giftCode, boolean creating) {
        if (giftCode == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码不能为空");
        }

        GiftCode normalized = new GiftCode();
        normalized.setId(giftCode.getId());
        normalized.setCode(requireNonBlank(giftCode.getCode(), "礼包码不能为空"));
        normalized.setName(trimToNull(giftCode.getName()));
        normalized.setCodeType(normalizeCodeType(giftCode.getCodeType()));
        normalized.setMaxUsage(giftCode.getMaxUsage());
        normalized.setUsedCount(giftCode.getUsedCount());
        normalized.setMinLevel(giftCode.getMinLevel());
        normalized.setRewards(requireValidRewards(giftCode.getRewards()));
        normalized.setStatus(normalizeStatus(giftCode.getStatus(), creating));
        normalized.setExpireAt(giftCode.getExpireAt());
        normalized.setCreatedBy(giftCode.getCreatedBy());
        normalized.setCreatedAt(giftCode.getCreatedAt());

        if (normalized.getMaxUsage() != null && normalized.getMaxUsage() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "最大使用次数必须大于0");
        }
        if (normalized.getMinLevel() != null && normalized.getMinLevel() < 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "最低等级必须大于0");
        }
        if ("UNIQUE".equalsIgnoreCase(normalized.getCodeType()) && normalized.getMaxUsage() == null) {
            normalized.setMaxUsage(1);
        }
        return normalized;
    }

    private String requireValidRewards(String rewardsJson) {
        String normalized = requireNonBlank(rewardsJson, "奖励内容不能为空");
        try {
            List<Map<String, Object>> rewards = objectMapper.readValue(
                    normalized, new TypeReference<List<Map<String, Object>>>() {
                    });
            if (rewards.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "奖励内容不能为空");
            }
            for (Map<String, Object> reward : rewards) {
                String type = reward.get("type") == null ? null : String.valueOf(reward.get("type")).trim();
                Integer quantity = toInteger(reward.get("quantity"));
                if (type == null || type.isEmpty()) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "奖励类型不能为空");
                }
                if (quantity == null || quantity <= 0) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "奖励数量必须大于0");
                }
                if (("ITEM".equalsIgnoreCase(type) || "EQUIPMENT".equalsIgnoreCase(type))
                        && toInteger(reward.get("id")) == null) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "物品或装备奖励必须提供ID");
                }
            }
            return normalized;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "奖励内容必须是有效JSON");
        }
    }

    private String normalizeCodeType(String codeType) {
        String normalized = requireNonBlank(codeType, "礼包码类型不能为空").toUpperCase();
        if (!ALLOWED_CODE_TYPES.contains(normalized)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码类型不合法");
        }
        return normalized;
    }

    private String normalizeStatus(String status, boolean creating) {
        if (creating && (status == null || status.trim().isEmpty())) {
            return "ACTIVE";
        }
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码状态不合法");
        }
        return normalized;
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void markGiftCodeExpiredOutsideTransaction(Long giftCodeId) {
        if (giftCodeId == null) {
            return;
        }
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.executeWithoutResult(status -> {
            GiftCode code = giftCodeMapper.selectById(giftCodeId);
            if (code != null && !"EXPIRED".equalsIgnoreCase(code.getStatus())) {
                code.setStatus("EXPIRED");
                giftCodeMapper.updateById(code);
            }
        });
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

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
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
}
