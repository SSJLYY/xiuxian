package com.xiuxian.game.modules.equipment.service;

import com.xiuxian.game.dto.response.PlayerEquipmentResponse;
import com.xiuxian.game.modules.equipment.entity.Equipment;
import com.xiuxian.game.modules.equipment.entity.PlayerEquipment;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.equipment.mapper.EquipmentMapper;
import com.xiuxian.game.modules.equipment.mapper.PlayerEquipmentMapper;
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

@Service
@ConditionalOnProperty(value = "app.features.equipment.enabled", havingValue = "true")
@RequiredArgsConstructor
public class EquipmentService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentService.class);

    private final EquipmentMapper equipmentMapper;
    private final PlayerEquipmentMapper playerEquipmentMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据

    // 使用 ThreadLocalRandom 替代共享 Random 实例（单例安全问题）
    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

    public List<Equipment> getAllEquipments() {
        return equipmentMapper.selectList(null);
    }

    public List<Equipment> getAvailableEquipments(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        return equipmentMapper.selectByRequiredLevel(player.getLevel());
    }

    public List<PlayerEquipment> getPlayerEquipments(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        return playerEquipmentMapper.selectByPlayerId(playerId);
    }

    public List<PlayerEquipmentResponse> getPlayerEquipmentsWithDetails(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        
        List<PlayerEquipment> playerEquipments = playerEquipmentMapper.selectByPlayerId(playerId);
        if (playerEquipments.isEmpty()) return new ArrayList<>();

        // 批量加载装备信息（避免N+1查询）
        List<Integer> equipmentIds = playerEquipments.stream()
                .map(PlayerEquipment::getEquipmentId).distinct().collect(Collectors.toList());
        Map<Integer, Equipment> equipmentMap = equipmentMapper.selectBatchIds(equipmentIds)
                .stream().collect(Collectors.toMap(Equipment::getId, e -> e));

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

    public List<PlayerEquipment> getEquippedItems(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        return playerEquipmentMapper.selectEquippedByPlayerId(playerId);
    }

    public List<PlayerEquipmentResponse> getEquippedItemsWithDetails(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }
        
        List<PlayerEquipment> playerEquipments = playerEquipmentMapper.selectEquippedByPlayerId(playerId);
        if (playerEquipments.isEmpty()) return new ArrayList<>();

        // 批量加载装备信息（避免N+1查询）
        List<Integer> equipmentIds = playerEquipments.stream()
                .map(PlayerEquipment::getEquipmentId).distinct().collect(Collectors.toList());
        Map<Integer, Equipment> equipmentMap = equipmentMapper.selectBatchIds(equipmentIds)
                .stream().collect(Collectors.toMap(Equipment::getId, e -> e));

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
                .equipped(pe.getEquipped())
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

    @Transactional
    public PlayerEquipment acquireEquipment(Integer equipmentId, Integer playerId) {
        try {
            PlayerProfile player = playerService.getPlayerProfileById(playerId);
            if (player == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
            }

            Equipment equipment = equipmentMapper.selectById(equipmentId);
            if (equipment == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "装备不存在");
            }

            // 检查等级要求
            if (player.getLevel() < equipment.getRequiredLevel()) {
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

    @Transactional
    public PlayerEquipment equipItem(Integer playerEquipmentId, String slot, Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
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
        if (player.getLevel() < equipment.getRequiredLevel()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "等级不足，无法装备此物品");
        }

        // 如果同槽位已有装备，则卸下
        PlayerEquipment existingInSlot = playerEquipmentMapper.selectEquippedBySlot(playerId, slot);
        if (existingInSlot != null) {
            if (existingInSlot.getEquipped()) {
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

    @Transactional
    public PlayerEquipment unequipItem(Integer playerEquipmentId, Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家装备不存在");
        }

        if (!playerEquipment.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该装备");
        }

        if (playerEquipment.getEquipped()) {
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

    @Transactional
    public PlayerEquipment repairEquipment(Integer playerEquipmentId, Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
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
        player.setEquipmentAttackBonus(player.getEquipmentAttackBonus() + equipment.getAttackBonus());
        player.setEquipmentDefenseBonus(player.getEquipmentDefenseBonus() + equipment.getDefenseBonus());
        player.setEquipmentHealthBonus(player.getEquipmentHealthBonus() + equipment.getHealthBonus());
        player.setEquipmentManaBonus(player.getEquipmentManaBonus() + equipment.getManaBonus());
        player.setEquipmentSpeedBonus(player.getEquipmentSpeedBonus() + equipment.getSpeedBonus());
    }

    /**
     * 移除装备属性加成从玩家
     */
    private void removeEquipmentBonuses(PlayerProfile player, Equipment equipment) {
        player.setEquipmentAttackBonus(player.getEquipmentAttackBonus() - equipment.getAttackBonus());
        player.setEquipmentDefenseBonus(player.getEquipmentDefenseBonus() - equipment.getDefenseBonus());
        player.setEquipmentHealthBonus(player.getEquipmentHealthBonus() - equipment.getHealthBonus());
        player.setEquipmentManaBonus(player.getEquipmentManaBonus() - equipment.getManaBonus());
        player.setEquipmentSpeedBonus(player.getEquipmentSpeedBonus() - equipment.getSpeedBonus());
    }

    /**
     * 初始化默认装备
     */
    @Transactional
    public void initializeDefaultEquipments() {
        long count = equipmentMapper.selectList(null).size();
        if (count == 0) {
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
    @Transactional
    public PlayerEquipment enhanceEquipment(Integer playerEquipmentId, Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
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
        
        if (player.getSpiritStones() < enhanceCost) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "灵石不足，需要 " + enhanceCost + " 灵石");
        }

        // 强化成功率：100% - 强化等级*3%
        int successRate = Math.max(50, 100 - currentLevel * 3);
        boolean success = rng().nextInt(100) < successRate;

        // 扣除灵石
        player.setSpiritStones(player.getSpiritStones() - enhanceCost);
        playerService.savePlayerProfile(player);

        if (success) {
            // 强化成功
            playerEquipment.setEnhanceLevel(currentLevel + 1);
            
            // 每级强化增加属性：基础属性的 5%
            int attackBonus = (int)(equipment.getAttackBonus() * 0.05);
            int defenseBonus = (int)(equipment.getDefenseBonus() * 0.05);
            int healthBonus = (int)(equipment.getHealthBonus() * 0.05);
            
            playerEquipment.setEnhanceAttackBonus(playerEquipment.getEnhanceAttackBonus() + attackBonus);
            playerEquipment.setEnhanceDefenseBonus(playerEquipment.getEnhanceDefenseBonus() + defenseBonus);
            playerEquipment.setEnhanceHealthBonus(playerEquipment.getEnhanceHealthBonus() + healthBonus);
            
            playerEquipment.setUpdatedAt(LocalDateTime.now());
            playerEquipmentMapper.updateById(playerEquipment);
            
            // 如果装备已装备，更新玩家属性
            if (playerEquipment.getEquipped()) {
                player.setEquipmentAttackBonus(player.getEquipmentAttackBonus() + attackBonus);
                player.setEquipmentDefenseBonus(player.getEquipmentDefenseBonus() + defenseBonus);
                player.setEquipmentHealthBonus(player.getEquipmentHealthBonus() + healthBonus);
                playerService.savePlayerProfile(player);
            }
        } else {
            // 强化失败，不降级，但消耗灵石
            throw new BusinessException(ErrorCode.PARAM_ERROR, "强化失败！已消耗 " + enhanceCost + " 灵石");
        }

        return playerEquipmentMapper.selectById(playerEquipmentId);
    }

    /**
     * 获取强化所需成本和成功率
     */
    public Map<String, Object> getEnhanceInfo(Integer playerEquipmentId, Integer playerId) {
        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null || !playerEquipment.getPlayerId().equals(playerId)) {
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
     * 直接发放装备给玩家（不做等级和重复校验，供邮件/活动奖励使用）
     * 模块边界：mail模块通过此接口为玩家发放装备，不直接操作PlayerEquipmentMapper
     */
    @Transactional
    public PlayerEquipment grantEquipmentDirectly(Integer playerId, Integer equipmentId) {
        PlayerEquipment pe = new PlayerEquipment();
        pe.setPlayerId(playerId);
        pe.setEquipmentId(equipmentId);
        pe.setEquipped(false);
        pe.setCreatedAt(java.time.LocalDateTime.now());
        playerEquipmentMapper.insert(pe);
        return pe;
    }

    // ===================== 供 AuctionService 使用的接口（模块边界规范） =====================

    /**
     * 根据装备模板ID获取装备信息（供 AuctionService 使用）
     */
    public Equipment getEquipmentById(Integer equipmentId) {
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
}
