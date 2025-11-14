package com.xiuxian.game.service;

import com.xiuxian.game.entity.Equipment;
import com.xiuxian.game.entity.PlayerEquipment;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.mapper.EquipmentMapper;
import com.xiuxian.game.mapper.PlayerEquipmentMapper;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(value = "app.features.equipment.enabled", havingValue = "true")
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentMapper equipmentMapper;
    private final PlayerEquipmentMapper playerEquipmentMapper;
    private final PlayerProfileMapper playerProfileMapper;

    public List<Equipment> getAllEquipments() {
        return equipmentMapper.selectList(null);
    }

    public List<Equipment> getAvailableEquipments(Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }
        return equipmentMapper.selectByRequiredLevel(player.getLevel());
    }

    public List<PlayerEquipment> getPlayerEquipments(Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }
        return playerEquipmentMapper.selectByPlayerId(playerId);
    }

    public List<PlayerEquipment> getEquippedItems(Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }
        return playerEquipmentMapper.selectEquippedByPlayerId(playerId);
    }

    @Transactional
    public PlayerEquipment acquireEquipment(Integer equipmentId, Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("装备不存在");
        }

        // 检查等级要求
        if (player.getLevel() < equipment.getRequiredLevel()) {
            throw new IllegalArgumentException("等级不足，无法获取此装备");
        }

        // 检查是否已拥有该装备
        List<PlayerEquipment> playerEquipments = playerEquipmentMapper.selectByPlayerId(playerId);
        boolean alreadyOwned = playerEquipments.stream()
                .anyMatch(pe -> pe.getEquipmentId().equals(equipmentId));

        if (alreadyOwned) {
            throw new IllegalArgumentException("已经拥有该装备");
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
    }

    @Transactional
    public PlayerEquipment equipItem(Integer playerEquipmentId, String slot, Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null) {
            throw new IllegalArgumentException("玩家装备不存在");
        }

        if (!playerEquipment.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("无权操作该装备");
        }

        Equipment equipment = equipmentMapper.selectById(playerEquipment.getEquipmentId());
        
        // 检查等级要求
        if (player.getLevel() < equipment.getRequiredLevel()) {
            throw new IllegalArgumentException("等级不足，无法装备此物品");
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
        playerProfileMapper.updateById(player);

        return playerEquipmentMapper.selectById(playerEquipmentId);
    }

    @Transactional
    public PlayerEquipment unequipItem(Integer playerEquipmentId, Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null) {
            throw new IllegalArgumentException("玩家装备不存在");
        }

        if (!playerEquipment.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("无权操作该装备");
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
        playerProfileMapper.updateById(player);

        return playerEquipmentMapper.selectById(playerEquipmentId);
    }

    @Transactional
    public PlayerEquipment repairEquipment(Integer playerEquipmentId, Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerEquipmentId);
        if (playerEquipment == null) {
            throw new IllegalArgumentException("玩家装备不存在");
        }

        if (!playerEquipment.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("无权操作该装备");
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
}
