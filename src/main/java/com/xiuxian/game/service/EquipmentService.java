package com.xiuxian.game.service;

import com.xiuxian.game.entity.Equipment;
import com.xiuxian.game.entity.PlayerEquipment;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.repository.EquipmentRepository;
import com.xiuxian.game.repository.PlayerEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final PlayerEquipmentRepository playerEquipmentRepository;
    private final PlayerService playerService;

    public List<Equipment> getAllEquipments() {
        return equipmentRepository.findAll();
    }

    public List<Equipment> getAvailableEquipments() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return equipmentRepository.findByRequiredLevelLessThanEqual(player.getLevel());
    }

    public List<PlayerEquipment> getPlayerEquipments() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return playerEquipmentRepository.findByPlayer(player);
    }

    public List<PlayerEquipment> getEquippedItems() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return playerEquipmentRepository.findByPlayerAndEquipped(player, true);
    }

    @Transactional
    public PlayerEquipment acquireEquipment(Integer equipmentId) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("装备不存在"));

        // 检查等级要求
        if (player.getLevel() < equipment.getRequiredLevel()) {
            throw new IllegalArgumentException("等级不足，无法获取此装备");
        }

        PlayerEquipment playerEquipment = PlayerEquipment.builder()
                .player(player)
                .equipment(equipment)
                .equipped(false)
                .slot(equipment.getType()) // 使用装备类型作为槽位
                .durability(100)
                .maxDurability(100)
                .build();

        return playerEquipmentRepository.save(playerEquipment);
    }

    @Transactional
    public PlayerEquipment equipItem(Integer playerEquipmentId) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        PlayerEquipment playerEquipment = playerEquipmentRepository.findById(playerEquipmentId)
                .orElseThrow(() -> new IllegalArgumentException("玩家装备不存在"));

        if (!playerEquipment.getPlayer().getId().equals(player.getId())) {
            throw new IllegalArgumentException("无权操作该装备");
        }

        // 检查等级要求
        if (player.getLevel() < playerEquipment.getEquipment().getRequiredLevel()) {
            throw new IllegalArgumentException("等级不足，无法装备此物品");
        }

        // 如果同槽位已有装备，则卸下
        Optional<PlayerEquipment> existingInSlot = playerEquipmentRepository.findByPlayerAndSlot(player, playerEquipment.getSlot());
        existingInSlot.ifPresent(pe -> {
            if (Boolean.TRUE.equals(pe.getEquipped())) {
                pe.setEquipped(false);
                playerEquipmentRepository.save(pe);
                // 移除旧装备的属性加成
                removeEquipmentBonuses(player, pe.getEquipment());
            }
        });

        // 装备新物品
        playerEquipment.setEquipped(true);
        PlayerEquipment savedEquipment = playerEquipmentRepository.save(playerEquipment);
        
        // 添加新装备的属性加成
        addEquipmentBonuses(player, playerEquipment.getEquipment());
        
        // 更新玩家属性
        playerService.updatePlayerProfile(player);
        
        return savedEquipment;
    }

    @Transactional
    public PlayerEquipment unequipItem(Integer playerEquipmentId) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        PlayerEquipment playerEquipment = playerEquipmentRepository.findById(playerEquipmentId)
                .orElseThrow(() -> new IllegalArgumentException("玩家装备不存在"));

        if (!playerEquipment.getPlayer().getId().equals(player.getId())) {
            throw new IllegalArgumentException("无权操作该装备");
        }

        if (Boolean.TRUE.equals(playerEquipment.getEquipped())) {
            // 移除装备的属性加成
            removeEquipmentBonuses(player, playerEquipment.getEquipment());
        }

        playerEquipment.setEquipped(false);
        PlayerEquipment savedEquipment = playerEquipmentRepository.save(playerEquipment);
        
        // 更新玩家属性
        playerService.updatePlayerProfile(player);
        
        return savedEquipment;
    }

    @Transactional
    public PlayerEquipment repairEquipment(Integer playerEquipmentId) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        PlayerEquipment playerEquipment = playerEquipmentRepository.findById(playerEquipmentId)
                .orElseThrow(() -> new IllegalArgumentException("玩家装备不存在"));

        if (!playerEquipment.getPlayer().getId().equals(player.getId())) {
            throw new IllegalArgumentException("无权操作该装备");
        }

        playerEquipment.setDurability(playerEquipment.getMaxDurability());
        return playerEquipmentRepository.save(playerEquipment);
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
     * 计算玩家总属性（基础属性 + 装备加成）
     */
    public PlayerProfile calculateTotalAttributes(PlayerProfile player) {
        // 重新计算所有已装备物品的加成
        List<PlayerEquipment> equippedItems = getEquippedItems();
        player.setEquipmentAttackBonus(0);
        player.setEquipmentDefenseBonus(0);
        player.setEquipmentHealthBonus(0);
        player.setEquipmentManaBonus(0);
        player.setEquipmentSpeedBonus(0);

        for (PlayerEquipment equippedItem : equippedItems) {
            addEquipmentBonuses(player, equippedItem.getEquipment());
        }

        return player;
    }

    /**
     * 初始化玩家装备数据（首次获取装备时调用）
     */
    @Transactional
    public void initializePlayerEquipment(PlayerProfile player) {
        // 计算当前所有装备的加成
        calculateTotalAttributes(player);
        playerService.updatePlayerProfile(player);
    }
}