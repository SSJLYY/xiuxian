package com.xiuxian.game.modules.equipment.service;

import com.xiuxian.game.dto.response.PlayerEquipmentResponse;
import com.xiuxian.game.modules.equipment.entity.Equipment;
import com.xiuxian.game.modules.equipment.entity.PlayerEquipment;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.equipment.mapper.EquipmentMapper;
import com.xiuxian.game.modules.equipment.mapper.PlayerEquipmentMapper;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

/**
 * 装备服务
 *
 * <p>提供装备管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>装备查询（所有装备、可用装备、玩家装备）</li>
 *   <li>装备穿戴与卸下</li>
 *   <li>装备获取与强化</li>
 *   <li>装备耐久度管理</li>
 * </ul>
 *
 * <p>模块边界：通过PlayerService访问玩家数据，不直接操作玩家模块</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@ConditionalOnProperty(value = "app.features.equipment.enabled", havingValue = "true")
@RequiredArgsConstructor
public class EquipmentService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentService.class);

    private final EquipmentMapper equipmentMapper;
    private final PlayerEquipmentMapper playerEquipmentMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据

    // 使用 ThreadLocalRandom 替代共享 Random 实例（单例安全问题）
    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

    /**
     * 获取所有装备
     *
     * @return 所有装备列表
     */
    public List<Equipment> getAllEquipments() {
        log.debug("查询所有装备");
        List<Equipment> equipments = equipmentMapper.selectList(null);
        log.debug("查询到 {} 件装备", equipments.size());
        return equipments;
    }

    /**
     * 获取玩家可用的装备
     *
     * <p>根据玩家等级筛选可穿戴的装备</p>
     *
     * @param playerId 玩家ID
     * @return 可用装备列表
     */
    public List<Equipment> getAvailableEquipments(Integer playerId) {
        log.debug("查询玩家可用装备, playerId={}", playerId);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        List<Equipment> equipments = equipmentMapper.selectByRequiredLevel(player.getLevel());
        log.debug("玩家 {} 可用装备数量: {}", playerId, equipments.size());
        return equipments;
    }

    /**
     * 获取玩家拥有的所有装备
     *
     * @param playerId 玩家ID
     * @return 玩家装备列表
     */
    public List<PlayerEquipment> getPlayerEquipments(Integer playerId) {
        log.debug("查询玩家装备, playerId={}", playerId);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        List<PlayerEquipment> playerEquipments = playerEquipmentMapper.selectByPlayerId(playerId);
        log.debug("玩家 {} 拥有 {} 件装备", playerId, playerEquipments.size());
        return playerEquipments;
    }

    /**
     * 获取玩家装备详情
     *
     * <p>返回包含装备模板信息的完整装备数据</p>
     *
     * @param playerId 玩家ID
     * @return 玩家装备详情列表
     */
    public List<PlayerEquipmentResponse> getPlayerEquipmentsWithDetails(Integer playerId) {
        log.debug("查询玩家装备详情, playerId={}", playerId);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        
        List<PlayerEquipment> playerEquipments = playerEquipmentMapper.selectByPlayerId(playerId);
        if (playerEquipments.isEmpty()) {
            log.debug("玩家 {} 没有装备", playerId);
            return new ArrayList<>();
        }

        // 批量加载装备信息（避免N+1查询）
        List<Integer> equipmentIds = playerEquipments.stream()
                .map(PlayerEquipment::getEquipmentId).distinct().collect(Collectors.toList());
        Map<Integer, Equipment> equipmentMap = equipmentMapper.selectBatchIds(equipmentIds)
                .stream().collect(Collectors.toMap(Equipment::getId, e -> e, (a, b) -> a));

        List<PlayerEquipmentResponse> responses = new ArrayList<>();
        for (PlayerEquipment pe : playerEquipments) {
            Equipment equipment = equipmentMap.get(pe.getEquipmentId());
            if (equipment == null) {
                continue;
            }
            responses.add(buildEquipmentResponse(pe, equipment));
        }
        
        return responses;
    }

    /**
     * 获取玩家已穿戴的装备
     *
     * @param playerId 玩家ID
     * @return 已穿戴装备列表
     */
    public List<PlayerEquipment> getEquippedItems(Integer playerId) {
        log.debug("查询玩家已穿戴装备, playerId={}", playerId);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        List<PlayerEquipment> equippedItems = playerEquipmentMapper.selectEquippedByPlayerId(playerId);
        log.debug("玩家 {} 已穿戴 {} 件装备", playerId, equippedItems.size());
        return equippedItems;
    }

    /**
     * 获取玩家已穿戴装备详情
     *
     * <p>返回包含装备模板信息的完整已穿戴装备数据</p>
     *
     * @param playerId 玩家ID
     * @return 已穿戴装备详情列表
     */
    public List<PlayerEquipmentResponse> getEquippedItemsWithDetails(Integer playerId) {
        log.debug("查询玩家已穿戴装备详情, playerId={}", playerId);
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        
        List<PlayerEquipment> playerEquipments = playerEquipmentMapper.selectEquippedByPlayerId(playerId);
        if (playerEquipments.isEmpty()) {
            log.debug("玩家 {} 没有穿戴装备", playerId);
            return new ArrayList<>();
        }

        // 批量加载装备信息（避免N+1查询）
        List<Integer> equipmentIds = playerEquipments.stream()
                .map(PlayerEquipment::getEquipmentId).distinct().collect(Collectors.toList());
        Map<Integer, Equipment> equipmentMap = equipmentMapper.selectBatchIds(equipmentIds)
                .stream().collect(Collectors.toMap(Equipment::getId, e -> e, (a, b) -> a));

        List<PlayerEquipmentResponse> responses = new ArrayList<>();
        for (PlayerEquipment pe : playerEquipments) {
            Equipment equipment = equipmentMap.get(pe.getEquipmentId());
            if (equipment == null) {
                continue;
            }
            responses.add(buildEquipmentResponse(pe, equipment));
        }
        
        return responses;
    }

    /**
     * 将 PlayerEquipment + Equipment 装配为 Response DTO（消除重复代码）
     */
    private PlayerEquipmentResponse buildEquipmentResponse(PlayerEquipment pe, Equipment equipment) {
        return PlayerEquipmentResponse.builder()
                .id(pe.getId())
                .playerId(pe.getPlayerId())
                .equipmentId(pe.getEquipmentId())
                .slot(pe.getSlot())
                .equipped(Boolean.TRUE.equals(pe.getEquipped()))
                .durability(pe.getDurability())
                .maxDurability(pe.getMaxDurability())
                .enhanceLevel(pe.getEnhanceLevel())
                .enhanceAttackBonus(pe.getEnhanceAttackBonus())
                .enhanceDefenseBonus(pe.getEnhanceDefenseBonus())
                .enhanceHealthBonus(pe.getEnhanceHealthBonus())
                .createdAt(pe.getCreatedAt())
                .updatedAt(pe.getUpdatedAt())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .type(equipment.getType())
                .level(equipment.getLevel())
                .quality(equipment.getQuality())
                .attackBonus(equipment.getAttackBonus())
                .defenseBonus(equipment.getDefenseBonus())
                .healthBonus(equipment.getHealthBonus())
                .manaBonus(equipment.getManaBonus())
                .speedBonus(equipment.getSpeedBonus())
                .requiredLevel(equipment.getRequiredLevel())
                .price(equipment.getPrice())
                .build();
    }

    /**
     * 获取装备
     *
     * <p>玩家获取新装备，需要满足等级要求且未拥有该装备</p>
     *
     * @param equipmentId 装备ID
     * @param playerId 玩家ID
     * @return 获取的玩家装备
     */
    @Transactional
    public PlayerEquipment acquireEquipment(Integer equipmentId, Integer playerId) {
        log.info("玩家获取装备, playerId={}, equipmentId={}", playerId, equipmentId);
        try {
            PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
            if (player == null) {
                log.warn("玩家不存在, playerId={}", playerId);
                throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
            }

            Equipment equipment = equipmentMapper.selectById(equipmentId);
            if (equipment == null) {
                log.warn("装备不存在, equipmentId={}", equipmentId);
                throw new BusinessException(ErrorCode.PARAM_ERROR, "装备不存在");
            }

            // 检查等级要求
            if (defaultInt(player.getLevel(), 1) < equipment.getRequiredLevel()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "等级不足，无法获取此装备");
            }

            // 检查是否已拥有该装备
            List<PlayerEquipment> playerEquipments = playerEquipmentMapper.selectByPlayerId(playerId);
            boolean alreadyOwned = playerEquipments.stream()
                    .anyMatch(pe -> pe.getEquipmentId().equals(equipmentId));

            if (alreadyOwned) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "已经拥有该装备");
            }

            PlayerEquipment playerEquipment = PlayerEquipment.builder()
                    .playerId(playerId)
                    .equipmentId(equipmentId)
                    .equipped(false)
                    .slot("")
                    .durability(100)
                    .maxDurability(100)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            playerEquipmentMapper.insert(playerEquipment);
            return playerEquipmentMapper.selectById(playerEquipment.getId());
        } catch (Exception e) {
            // 记录详细错误信息
            log.error("获取装备失败 - 装备ID: {}, 玩家ID: {}, 错误: {}", equipmentId, playerId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 穿戴装备
     *
     * <p>将装备穿戴到指定槽位，如果槽位已有装备则自动卸下</p>
     *
     * @param playerEquipmentId 玩家装备ID
     * @param slot 装备槽位
     * @param playerId 玩家ID
     * @return 更新后的玩家装备
     */
    @Transactional
    public PlayerEquipment equipItem(Integer playerEquipmentId, String slot, Integer playerId) {
        log.info("玩家穿戴装备, playerId={}, playerEquipmentId={}, slot={}", playerId, playerEquipmentId, slot);
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家装备不存在");
        }

        if (!playerEquipment.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该装备");
        }

        Equipment equipment = equipmentMapper.selectById(playerEquipment.getEquipmentId());
        
        // 检查等级要求
        if (defaultInt(player.getLevel(), 1) < equipment.getRequiredLevel()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "等级不足，无法装备此物品");
        }

        // 如果同槽位已有装备，则卸下
        PlayerEquipment existingInSlot = playerEquipmentMapper.selectEquippedBySlot(playerId, slot);
        if (existingInSlot != null) {
            if (Boolean.TRUE.equals(existingInSlot.getEquipped())) {
                existingInSlot.setEquipped(false);
                existingInSlot.setUpdatedAt(LocalDateTime.now());
                playerEquipmentMapper.updateById(existingInSlot);
                // 移除旧装备的属性加成
                Equipment oldEquipment = equipmentMapper.selectById(existingInSlot.getEquipmentId());
                removeEquipmentBonuses(player, oldEquipment);
            }
        }

        // 装备新物品
        playerEquipment.setEquipped(true);
        playerEquipment.setSlot(slot);
        playerEquipment.setUpdatedAt(LocalDateTime.now());
        playerEquipmentMapper.updateById(playerEquipment);

        // 添加新装备的属性加成
        addEquipmentBonuses(player, equipment);

        // 更新玩家属性
        playerService.savePlayerProfile(player);

        return playerEquipmentMapper.selectById(playerEquipmentId);
    }

    /**
     * 卸下装备
     *
     * @param playerEquipmentId 玩家装备ID
     * @param playerId 玩家ID
     * @return 更新后的玩家装备
     */
    @Transactional
    public PlayerEquipment unequipItem(Integer playerEquipmentId, Integer playerId) {
        log.info("玩家卸下装备, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家装备不存在");
        }

        if (!playerEquipment.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该装备");
        }

        if (Boolean.TRUE.equals(playerEquipment.getEquipped())) {
            Equipment equipment = equipmentMapper.selectById(playerEquipment.getEquipmentId());
            // 移除装备的属性加成
            removeEquipmentBonuses(player, equipment);
        }

        playerEquipment.setEquipped(false);
        playerEquipment.setSlot("");
        playerEquipment.setUpdatedAt(LocalDateTime.now());
        playerEquipmentMapper.updateById(playerEquipment);

        // 更新玩家属性
        playerService.savePlayerProfile(player);

        return playerEquipmentMapper.selectById(playerEquipmentId);
    }

    /**
     * 修复装备
     *
     * <p>将装备耐久度恢复到最大值</p>
     *
     * @param playerEquipmentId 玩家装备ID
     * @param playerId 玩家ID
     * @return 更新后的玩家装备
     */
    @Transactional
    public PlayerEquipment repairEquipment(Integer playerEquipmentId, Integer playerId) {
        log.info("玩家修复装备, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家装备不存在");
        }

        if (!playerEquipment.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该装备");
        }

        playerEquipment.setDurability(playerEquipment.getMaxDurability());
        playerEquipment.setUpdatedAt(LocalDateTime.now());
        playerEquipmentMapper.updateById(playerEquipment);
        return playerEquipmentMapper.selectById(playerEquipmentId);
    }

    /**
     * 添加装备属性加成到玩家
     */
    private void addEquipmentBonuses(PlayerProfile player, Equipment equipment) {
        player.setEquipmentAttackBonus(defaultInt(player.getEquipmentAttackBonus(), 0) + defaultInt(equipment.getAttackBonus(), 0));
        player.setEquipmentDefenseBonus(defaultInt(player.getEquipmentDefenseBonus(), 0) + defaultInt(equipment.getDefenseBonus(), 0));
        player.setEquipmentHealthBonus(defaultInt(player.getEquipmentHealthBonus(), 0) + defaultInt(equipment.getHealthBonus(), 0));
        player.setEquipmentManaBonus(defaultInt(player.getEquipmentManaBonus(), 0) + defaultInt(equipment.getManaBonus(), 0));
        player.setEquipmentSpeedBonus(defaultInt(player.getEquipmentSpeedBonus(), 0) + defaultInt(equipment.getSpeedBonus(), 0));
    }

    /**
     * 移除装备属性加成从玩家
     */
    private void removeEquipmentBonuses(PlayerProfile player, Equipment equipment) {
        player.setEquipmentAttackBonus(defaultInt(player.getEquipmentAttackBonus(), 0) - defaultInt(equipment.getAttackBonus(), 0));
        player.setEquipmentDefenseBonus(defaultInt(player.getEquipmentDefenseBonus(), 0) - defaultInt(equipment.getDefenseBonus(), 0));
        player.setEquipmentHealthBonus(defaultInt(player.getEquipmentHealthBonus(), 0) - defaultInt(equipment.getHealthBonus(), 0));
        player.setEquipmentManaBonus(defaultInt(player.getEquipmentManaBonus(), 0) - defaultInt(equipment.getManaBonus(), 0));
        player.setEquipmentSpeedBonus(defaultInt(player.getEquipmentSpeedBonus(), 0) - defaultInt(equipment.getSpeedBonus(), 0));
    }

    /**
     * 初始化默认装备
     *
     * <p>系统启动时创建基础装备数据</p>
     */
    @Transactional
    public void initializeDefaultEquipments() {
        log.info("初始化默认装备");
        long count = equipmentMapper.selectList(null).size();
        if (count == 0) {
            log.info("创建默认装备数据");
            // 创建基础装备
            Equipment woodenSword = Equipment.builder()
                    .name("木剑")
                    .description("新手使用的木制长剑")
                    .type("武器")
                    .level(1)
                    .quality(1)
                    .attackBonus(5)
                    .defenseBonus(0)
                    .healthBonus(0)
                    .manaBonus(0)
                    .speedBonus(0)
                    .requiredLevel(1)
                    .price(100)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Equipment clothArmor = Equipment.builder()
                    .name("布甲")
                    .description("普通的布制护甲")
                    .type("胸甲")
                    .level(1)
                    .quality(1)
                    .attackBonus(0)
                    .defenseBonus(3)
                    .healthBonus(10)
                    .manaBonus(0)
                    .speedBonus(0)
                    .requiredLevel(1)
                    .price(80)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            equipmentMapper.insert(woodenSword);
            equipmentMapper.insert(clothArmor);
        }
    }

    /**
     * 强化装备
     */
    /**
     * 强化装备
     *
     * <p>消耗灵石强化装备，提升装备属性</p>
     *
     * @param playerEquipmentId 玩家装备ID
     * @param playerId 玩家ID
     * @return 更新后的玩家装备
     */
    @Transactional(noRollbackFor = EquipmentEnhanceFailedException.class)
    public PlayerEquipment enhanceEquipment(Integer playerEquipmentId, Integer playerId) {
        log.info("玩家强化装备, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            log.warn("玩家不存在, playerId={}", playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家装备不存在");
        }

        if (!playerEquipment.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该装备");
        }

        Equipment equipment = equipmentMapper.selectById(playerEquipment.getEquipmentId());
        int currentLevel = playerEquipment.getEnhanceLevel();
        
        // 强化上限为20级
        if (currentLevel >= 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "装备已达最大强化等级");
        }

        // 计算强化所需灵石：基础100 + 等级*50 + 品质*100
        int enhanceCost = 100 + currentLevel * 50 + equipment.getQuality() * 100;
        
        if (defaultLong(player.getSpiritStones()) < enhanceCost) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "灵石不足，需要 " + enhanceCost + " 灵石");
        }

        // 强化成功率：100% - 强化等级*3%
        int successRate = Math.max(50, 100 - currentLevel * 3);
        boolean success = rng().nextInt(100) < successRate;

        // 扣除灵石
        player.setSpiritStones(defaultLong(player.getSpiritStones()) - enhanceCost);

        if (success) {
            // 强化成功
            playerEquipment.setEnhanceLevel(currentLevel + 1);
            
            // 每级强化增加属性：基础属性的 5%
            int attackBonus = (int)(equipment.getAttackBonus() * 0.05);
            int defenseBonus = (int)(equipment.getDefenseBonus() * 0.05);
            int healthBonus = (int)(equipment.getHealthBonus() * 0.05);
            
            playerEquipment.setEnhanceAttackBonus(defaultInt(playerEquipment.getEnhanceAttackBonus(), 0) + attackBonus);
            playerEquipment.setEnhanceDefenseBonus(defaultInt(playerEquipment.getEnhanceDefenseBonus(), 0) + defenseBonus);
            playerEquipment.setEnhanceHealthBonus(defaultInt(playerEquipment.getEnhanceHealthBonus(), 0) + healthBonus);
            
            playerEquipment.setUpdatedAt(LocalDateTime.now());
            playerEquipmentMapper.updateById(playerEquipment);
            
            // 如果装备已装备，更新玩家属性
            if (Boolean.TRUE.equals(playerEquipment.getEquipped())) {
                player.setEquipmentAttackBonus(defaultInt(player.getEquipmentAttackBonus(), 0) + attackBonus);
                player.setEquipmentDefenseBonus(defaultInt(player.getEquipmentDefenseBonus(), 0) + defenseBonus);
                player.setEquipmentHealthBonus(defaultInt(player.getEquipmentHealthBonus(), 0) + healthBonus);
            }
            playerService.savePlayerProfile(player);
        } else {
            // 强化失败，不降级，但消耗灵石
            playerService.savePlayerProfile(player);
            throw new EquipmentEnhanceFailedException(enhanceCost);
        }

        return playerEquipmentMapper.selectById(playerEquipmentId);
    }

    /**
     * 获取强化所需成本和成功率
     */
    /**
     * 获取强化信息
     *
     * <p>返回装备强化所需的成本和成功率</p>
     *
     * @param playerEquipmentId 玩家装备ID
     * @param playerId 玩家ID
     * @return 强化信息
     */
    public Map<String, Object> getEnhanceInfo(Integer playerEquipmentId, Integer playerId) {
        log.debug("查询强化信息, playerId={}, playerEquipmentId={}", playerId, playerEquipmentId);
        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null || !playerEquipment.getPlayerId().equals(playerId)) {
            log.warn("装备不存在或无权访问, playerEquipmentId={}, playerId={}", playerEquipmentId, playerId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "装备不存在或无权访问");
        }

        Equipment equipment = equipmentMapper.selectById(playerEquipment.getEquipmentId());
        int currentLevel = playerEquipment.getEnhanceLevel();
        
        int enhanceCost = 100 + currentLevel * 50 + equipment.getQuality() * 100;
        int successRate = Math.max(50, 100 - currentLevel * 3);
        
        Map<String, Object> info = new HashMap<>();
        info.put("currentLevel", currentLevel);
        info.put("maxLevel", 20);
        info.put("cost", enhanceCost);
        info.put("successRate", successRate);
        info.put("canEnhance", currentLevel < 20);
        
        return info;
    }

    /**
     * 直接发放装备给玩家
     *
     * <p>不做等级和重复校验，供邮件/活动奖励使用</p>
     * <p>模块边界：mail模块通过此接口为玩家发放装备，不直接操作PlayerEquipmentMapper</p>
     *
     * @param playerId 玩家ID
     * @param equipmentId 装备ID
     * @return 创建的玩家装备
     */
    @Transactional
    public PlayerEquipment grantEquipmentDirectly(Integer playerId, Integer equipmentId) {
        log.info("直接发放装备, playerId={}, equipmentId={}", playerId, equipmentId);
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "装备不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        PlayerEquipment pe = PlayerEquipment.builder()
                .playerId(playerId)
                .equipmentId(equipmentId)
                .slot("")
                .equipped(false)
                .durability(100)
                .maxDurability(100)
                .enhanceLevel(0)
                .enhanceAttackBonus(0)
                .enhanceDefenseBonus(0)
                .enhanceHealthBonus(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        playerEquipmentMapper.insert(pe);
        log.info("装备发放成功, playerEquipmentId={}", pe.getId());
        return pe;
    }

    // ===================== 供 AuctionService 使用的接口（模块边界规范） =====================

    /**
     * 根据装备模板ID获取装备信息
     *
     * <p>供AuctionService使用</p>
     *
     * @param equipmentId 装备ID
     * @return 装备信息
     */
    public Equipment getEquipmentById(Integer equipmentId) {
        log.debug("查询装备信息, equipmentId={}", equipmentId);
        return equipmentMapper.selectById(equipmentId);
    }

    /**
     * 根据 PlayerEquipment 主键获取玩家装备记录（供 AuctionService 使用）
     */
    public PlayerEquipment getPlayerEquipmentById(Long playerEquipmentId) {
        return playerEquipmentMapper.selectById(playerEquipmentId);
    }

    /**
     * 删除玩家装备记录（供 AuctionService 上架/取消拍卖使用）
     */
    @Transactional
    public void deletePlayerEquipment(Long playerEquipmentId) {
        playerEquipmentMapper.deleteById(playerEquipmentId);
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private static final class EquipmentEnhanceFailedException extends BusinessException {
        private EquipmentEnhanceFailedException(int enhanceCost) {
            super(ErrorCode.PARAM_ERROR, "强化失败！已消耗 " + enhanceCost + " 灵石");
        }
    }
}
