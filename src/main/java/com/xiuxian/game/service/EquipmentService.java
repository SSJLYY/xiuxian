package com.xiuxian.game.service;

import com.xiuxian.game.entity.Equipment;
import com.xiuxian.game.entity.PlayerEquipment;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.repository.EquipmentRepository;
import com.xiuxian.game.repository.PlayerEquipmentRepository;
import com.xiuxian.game.repository.PlayerProfileRepository;
import com.xiuxian.game.util.GameConstants;
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
    private final PlayerProfileRepository playerProfileRepository;

    public List<Equipment> getAllEquipments() {
        return equipmentRepository.findAll();
    }

    public List<Equipment> getAvailableEquipments(Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));
        return equipmentRepository.findByRequiredLevelLessThanEqual(player.getLevel());
    }

    public List<PlayerEquipment> getPlayerEquipments(Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));
        return playerEquipmentRepository.findByPlayer(player);
    }

    public List<PlayerEquipment> getEquippedItems(Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));
        return playerEquipmentRepository.findByPlayerAndEquipped(player, true);
    }

    @Transactional
    public PlayerEquipment acquireEquipment(Integer equipmentId, Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("装备不存在"));

        // 检查等级要求
        if (player.getLevel() < equipment.getRequiredLevel()) {
            throw new IllegalArgumentException("等级不足，无法获取此装备");
        }

        // 检查是否已拥有该装备
        List<PlayerEquipment> playerEquipments = playerEquipmentRepository.findByPlayer(player);
        boolean alreadyOwned = playerEquipments.stream()
                .anyMatch(pe -> pe.getEquipment().getId().equals(equipmentId));

        if (alreadyOwned) {
            throw new IllegalArgumentException("已经拥有该装备");
        }

        PlayerEquipment playerEquipment = PlayerEquipment.builder()
                .player(player)
                .equipment(equipment)
                .equipped(false)
                .slot("")
                .durability(100)
                .maxDurability(100)
                .build();

        return playerEquipmentRepository.save(playerEquipment);
    }

    @Transactional
    public PlayerEquipment equipItem(Integer playerEquipmentId, String slot, Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));

        PlayerEquipment playerEquipment = playerEquipmentRepository.findById(playerEquipmentId)
                .orElseThrow(() -> new IllegalArgumentException("玩家装备不存在"));

        if (!playerEquipment.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException("无权操作该装备");
        }

        // 检查等级要求
        if (player.getLevel() < playerEquipment.getEquipment().getRequiredLevel()) {
            throw new IllegalArgumentException("等级不足，无法装备此物品");
        }

        // 如果同槽位已有装备，则卸下
        Optional<PlayerEquipment> existingInSlot = playerEquipmentRepository.findByPlayerAndSlot(player, slot);
        if (existingInSlot.isPresent()) {
            PlayerEquipment equippedItem = existingInSlot.get();
            if (equippedItem.getEquipped()) {
                equippedItem.setEquipped(false);
                playerEquipmentRepository.save(equippedItem);
                // 移除旧装备的属性加成
                removeEquipmentBonuses(player, equippedItem.getEquipment());
            }
        }

        // 装备新物品
        playerEquipment.setEquipped(true);
        playerEquipment.setSlot(slot);
        PlayerEquipment savedEquipment = playerEquipmentRepository.save(playerEquipment);

        // 添加新装备的属性加成
        addEquipmentBonuses(player, playerEquipment.getEquipment());

        // 更新玩家属性
        playerProfileRepository.save(player);

        return savedEquipment;
    }

    @Transactional
    public PlayerEquipment unequipItem(Integer playerEquipmentId, Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));

        PlayerEquipment playerEquipment = playerEquipmentRepository.findById(playerEquipmentId)
                .orElseThrow(() -> new IllegalArgumentException("玩家装备不存在"));

        if (!playerEquipment.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException("无权操作该装备");
        }

        if (playerEquipment.getEquipped()) {
            // 移除装备的属性加成
            removeEquipmentBonuses(player, playerEquipment.getEquipment());
        }

        playerEquipment.setEquipped(false);
        playerEquipment.setSlot("");
        PlayerEquipment savedEquipment = playerEquipmentRepository.save(playerEquipment);

        // 更新玩家属性
        playerProfileRepository.save(player);

        return savedEquipment;
    }

    @Transactional
    public PlayerEquipment repairEquipment(Integer playerEquipmentId, Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));

        PlayerEquipment playerEquipment = playerEquipmentRepository.findById(playerEquipmentId)
                .orElseThrow(() -> new IllegalArgumentException("玩家装备不存在"));

        if (!playerEquipment.getPlayer().getId().equals(playerId)) {
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
     * 初始化默认装备
     */
    @Transactional
    public void initializeDefaultEquipments() {
        if (equipmentRepository.count() == 0) {
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
                    .build();

            equipmentRepository.save(woodenSword);
            equipmentRepository.save(clothArmor);
        }
    }
}