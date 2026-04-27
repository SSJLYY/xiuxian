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
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.mail.service.MailService;

/**
 * 礼包码服务
 *
 * <p>提供礼包码的兑换、创建和管理功能</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>礼包码兑换 - 玩家使用礼包码领取奖励</li>
 *   <li>礼包码创建 - 管理员创建新的礼包码</li>
 *   <li>使用记录查询 - 查询礼包码的使用历史</li>
 * </ul>
 *
 * <p>礼包码类型：</p>
 * <ul>
 *   <li>UNIQUE - 每个玩家只能使用一次</li>
 *   <li>SHARED - 多个玩家可以使用，有总次数限制</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCodeService extends ServiceImpl<GiftCodeMapper, GiftCode> {

    private final GiftCodeMapper giftCodeMapper;
    private final GiftCodeUsageMapper giftCodeUsageMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据
    private final MailService mailService;
    private final ObjectMapper objectMapper;

    /**
     * 兑换礼包码
     *
     * <p>玩家使用礼包码领取奖励，支持唯一码和共享码两种类型</p>
     *
     * <p>兑换流程：</p>
     * <ol>
     *   <li>验证礼包码是否存在</li>
     *   <li>验证礼包码状态和有效期</li>
     *   <li>验证玩家等级是否满足要求</li>
     *   <li>验证玩家是否已使用过（唯一码）</li>
     *   <li>验证礼包码使用次数是否已满</li>
     *   <li>记录使用情况并发放奖励</li>
     * </ol>
     *
     * @param playerId 玩家ID
     * @param code     礼包码
     * @return 兑换结果
     * @throws BusinessException 当礼包码不存在、已失效、已过期、等级不足、已使用或已用完时抛出
     */
    @Transactional
    public boolean redeemGiftCode(Integer playerId, String code) {
        log.info("玩家兑换礼包码, playerId={}, code={}", playerId, code);
        
        // 模块边界：通过PlayerService访问玩家数据
        QueryWrapper<GiftCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        GiftCode giftCode = giftCodeMapper.selectOne(queryWrapper);
        LocalDateTime now = LocalDateTime.now();
        PlayerProfile lockedPlayer = playerProfileMapper.selectByIdForUpdate(playerId);
        if (lockedPlayer == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        // 检查礼包码是否存在
        if (giftCode == null) {
            log.warn("礼包码不存在, code={}", code);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码不存在");
        }

        // 模块边界：通过PlayerService访问玩家数据
        if (!"ACTIVE".equals(giftCode.getStatus())) {
            log.warn("礼包码已失效, code={}, status={}", code, giftCode.getStatus());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已失效");
        }

        // 检查过期时间 - 使用isAfter确保包含过期当天
        if (giftCode.getExpireAt() != null && !giftCode.getExpireAt().isAfter(now)) {
            giftCode.setStatus("EXPIRED");
            giftCodeMapper.updateById(giftCode);
            log.warn("礼包码已过期, code={}, expireAt={}", code, giftCode.getExpireAt());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已过期");
        }

        if (lockedPlayer.getLevel() < giftCode.getMinLevel()) {
            log.warn("玩家等级不足, playerId={}, playerLevel={}, minLevel={}",
                    playerId, lockedPlayer.getLevel(), giftCode.getMinLevel());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家等级不足，需要达到" + giftCode.getMinLevel() + "级");
        }

        // 模块边界：通过PlayerService访问玩家数据
        if ("UNIQUE".equals(giftCode.getCodeType())) {
            QueryWrapper<GiftCodeUsage> usageQuery = new QueryWrapper<>();
            usageQuery.eq("gift_code_id", giftCode.getId());
            usageQuery.eq("player_id", playerId);
            if (giftCodeUsageMapper.selectCount(usageQuery) > 0) {
                log.warn("玩家已使用过此礼包码, playerId={}, giftCodeId={}", playerId, giftCode.getId());
                throw new BusinessException(ErrorCode.PARAM_ERROR, "您已经使用过此礼包码");
            }
        }

        int consumedRows = giftCodeMapper.consumeUsageIfAvailable(giftCode.getId(), now);
        if (consumedRows == 0) {
            GiftCode latestGiftCode = giftCodeMapper.selectById(giftCode.getId());
            if (latestGiftCode != null
                    && latestGiftCode.getExpireAt() != null
                    && !latestGiftCode.getExpireAt().isAfter(now)) {
                latestGiftCode.setStatus("EXPIRED");
                giftCodeMapper.updateById(latestGiftCode);
                log.warn("礼包码已过期, code={}, expireAt={}", code, latestGiftCode.getExpireAt());
                throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已过期");
            }
            if (latestGiftCode != null && !"ACTIVE".equals(latestGiftCode.getStatus())) {
                log.warn("礼包码已失效, code={}, status={}", code, latestGiftCode.getStatus());
                throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已失效");
            }
            log.warn("礼包码已被使用完, code={}", code);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "礼包码已被使用完");
        }

        // 记录使用情况
        GiftCodeUsage usage = new GiftCodeUsage();
        usage.setGiftCodeId(giftCode.getId());
        usage.setPlayerId(playerId);
        giftCodeUsageMapper.insert(usage);
        log.info("记录礼包码使用情况, giftCodeId={}, playerId={}", giftCode.getId(), playerId);

        // 发放奖励
        distributeRewards(lockedPlayer, giftCode.getRewards());
        
        log.info("礼包码兑换成功, playerId={}, code={}", playerId, code);
        return true;
    }

    /**
     * 发放奖励
     *
     * <p>根据礼包码配置的奖励内容，通过邮件系统发放给玩家</p>
     *
     * <p>奖励类型：</p>
     * <ul>
     *   <li>SPIRIT_STONES - 灵石，直接添加到玩家账户</li>
     *   <li>ITEM - 物品，通过邮件发送</li>
     *   <li>EQUIPMENT - 装备，通过邮件发送</li>
     *   <li>其他类型 - 通过邮件发送</li>
     * </ul>
     *
     * @param playerId 玩家ID
     * @param rewards  奖励内容（JSON格式）
     */
    private void distributeRewards(PlayerProfile player, String rewards) {
        Integer playerId = player.getId();
        log.info("开始发放礼包码奖励, playerId={}", playerId);
        try {
            List<Map<String, Object>> rewardList = objectMapper.readValue(rewards, new TypeReference<List<Map<String, Object>>>() {});
            log.debug("解析奖励列表, rewardCount={}", rewardList.size());
            
            for (Map<String, Object> reward : rewardList) {
                String type = (String) reward.get("type");
                Integer id = (Integer) reward.get("id");
                Integer quantity = (Integer) reward.get("quantity");
                
                log.debug("处理奖励项, type={}, id={}, quantity={}", type, id, quantity);

                switch (type) {
                    case "SPIRIT_STONES":
                        // 发放灵石
                        player.setSpiritStones(player.getSpiritStones() + quantity);
                        playerService.savePlayerProfile(player);
                        log.info("发放灵石奖励, playerId={}, quantity={}", playerId, quantity);
                        break;
                    case "ITEM":
                        // 模块边界：通过PlayerService访问玩家数据
                        mailService.sendSystemMail(playerId, "礼包码奖励", "您通过礼包码获得了物品奖励", "ITEM", id, quantity);
                        log.info("发送物品奖励邮件, playerId={}, itemId={}, quantity={}", playerId, id, quantity);
                        break;
                    case "EQUIPMENT":
                        // 模块边界：通过PlayerService访问玩家数据
                        mailService.sendSystemMail(playerId, "礼包码奖励", "您通过礼包码获得了装备奖励", "EQUIPMENT", id, quantity);
                        log.info("发送装备奖励邮件, playerId={}, equipmentId={}, quantity={}", playerId, id, quantity);
                        break;
                    default:
                        // 模块边界：通过PlayerService访问玩家数据
                        mailService.sendSystemMail(playerId, "礼包码奖励", "您通过礼包码获得了奖励", type, id, quantity);
                        log.info("发送其他类型奖励邮件, playerId={}, type={}, id={}, quantity={}", playerId, type, id, quantity);
                        break;
                }
            }
            log.info("礼包码奖励发放完成, playerId={}", playerId);
        } catch (Exception e) {
            log.error("发放礼包码奖励失败, playerId={}, error={}", playerId, e.getMessage(), e);
            throw new com.xiuxian.game.common.exception.BusinessException(
                    com.xiuxian.game.common.exception.ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 创建礼包码
     *
     * <p>管理员创建新的礼包码，设置初始状态为ACTIVE</p>
     *
     * <p>创建流程：</p>
     * <ol>
     *   <li>设置使用次数为0</li>
     *   <li>设置状态为ACTIVE</li>
     *   <li>设置创建时间</li>
     *   <li>插入数据库</li>
     * </ol>
     *
     * @param giftCode 礼包码信息
     * @return 创建的礼包码
     */
    @Transactional
    public GiftCode createGiftCode(GiftCode giftCode) {
        log.info("创建礼包码, code={}, codeType={}", giftCode.getCode(), giftCode.getCodeType());
        giftCode.setUsedCount(0);
        giftCode.setStatus("ACTIVE");
        giftCode.setCreatedAt(LocalDateTime.now());
        giftCodeMapper.insert(giftCode);
        log.info("礼包码创建成功, id={}, code={}", giftCode.getId(), giftCode.getCode());
        return giftCode;
    }

    /**
     * 获取礼包码使用记录
     *
     * <p>查询指定礼包码的使用历史记录，按使用时间降序排列</p>
     *
     * @param giftCodeId 礼包码ID
     * @return 使用记录列表
     */
    public List<GiftCodeUsage> getGiftCodeUsageHistory(Long giftCodeId) {
        log.info("查询礼包码使用记录, giftCodeId={}", giftCodeId);
        QueryWrapper<GiftCodeUsage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("gift_code_id", giftCodeId);
        queryWrapper.orderByDesc("used_at");
        List<GiftCodeUsage> usageList = giftCodeUsageMapper.selectList(queryWrapper);
        log.info("查询礼包码使用记录完成, giftCodeId={}, count={}", giftCodeId, usageList.size());
        return usageList;
    }
}
