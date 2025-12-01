package com.xiuxian.game.service;

import com.xiuxian.game.entity.*;
import com.xiuxian.game.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 宠物服务类
 * 负责宠物系统的所有业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetMapper petMapper;
    private final PlayerPetMapper playerPetMapper;
    private final PetSkillMapper petSkillMapper;
    private final PlayerPetSkillMapper playerPetSkillMapper;
    private final PetTrainingLogMapper petTrainingLogMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final Random random = new Random();

    /**
     * 获取所有宠物模板
     */
    public List<Pet> getAllPets() {
        return petMapper.selectList(null);
    }

    /**
     * 获取玩家可捕获的宠物列表
     */
    public List<Pet> getAvailablePets(Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }
        return petMapper.selectAvailablePets(player.getLevel());
    }

    /**
     * 获取玩家的所有宠物
     */
    public List<PlayerPet> getPlayerPets(Integer playerId) {
        return playerPetMapper.selectByPlayerId(playerId);
    }

    /**
     * 获取玩家的出战宠物
     */
    public PlayerPet getActivePet(Integer playerId) {
        return playerPetMapper.selectActivePet(playerId);
    }

    /**
     * 捕获宠物
     */
    @Transactional
    public PlayerPet capturePet(Integer playerId, Integer petId) {
        log.info("========== 捕获宠物 ==========");
        log.info("玩家ID: {}, 宠物ID: {}", playerId, petId);

        // 1. 验证玩家
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        // 2. 验证宠物模板
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new IllegalArgumentException("宠物不存在");
        }

        // 3. 检查等级要求
        if (player.getLevel() < pet.getUnlockLevel()) {
            throw new IllegalArgumentException("等级不足，需要" + pet.getUnlockLevel() + "级");
        }

        // 4. 检查是否已拥有
        int count = playerPetMapper.countByPlayerIdAndPetId(playerId, petId);
        if (count >= 3) {
            throw new IllegalArgumentException("同种宠物最多拥有3只");
        }

        // 5. 计算捕获成功率
        double captureChance = pet.getCaptureRate().doubleValue();
        double roll = random.nextDouble() * 100;
        
        log.info("捕获概率: {}%, 随机数: {}", captureChance, roll);
        
        if (roll > captureChance) {
            log.info("捕获失败");
            throw new IllegalArgumentException("捕获失败，请再试一次");
        }

        // 6. 创建玩家宠物
        PlayerPet playerPet = PlayerPet.builder()
                .playerId(playerId)
                .petId(petId)
                .nickname(pet.getName())
                .level(1)
                .exp(0L)
                .expToNext(100L)
                .attack(pet.getBaseAttack())
                .defense(pet.getBaseDefense())
                .health(pet.getBaseHealth())
                .maxHealth(pet.getBaseHealth())
                .speed(pet.getBaseSpeed())
                .loyalty(50)
                .hunger(100)
                .isActive(false)
                .isLocked(false)
                .totalBattles(0)
                .totalWins(0)
                .capturedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        playerPetMapper.insert(playerPet);
        PlayerPet savedPet = playerPetMapper.selectById(playerPet.getId());

        // 7. 学习基础技能
        PetSkill basicSkill = petSkillMapper.selectById(1); // 撕咬技能
        if (basicSkill != null) {
            PlayerPetSkill playerPetSkill = PlayerPetSkill.builder()
                    .playerPetId(savedPet.getId())
                    .petSkillId(basicSkill.getId())
                    .skillLevel(1)
                    .learnedAt(LocalDateTime.now())
                    .build();
            playerPetSkillMapper.insert(playerPetSkill);
            log.info("宠物学习基础技能: {}", basicSkill.getName());
        }

        log.info("捕获成功: 宠物ID={}, 昵称={}", savedPet.getId(), savedPet.getNickname());
        log.info("========== 捕获宠物完成 ==========");
        
        return savedPet;
    }

    /**
     * 设置出战宠物
     */
    @Transactional
    public void setActivePet(Integer playerId, Integer playerPetId) {
        log.info("设置出战宠物: 玩家ID={}, 宠物ID={}", playerId, playerPetId);

        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("宠物不存在或不属于该玩家");
        }

        // 取消所有出战状态
        playerPetMapper.deactivateAllPets(playerId);

        // 设置新的出战宠物
        playerPet.setIsActive(true);
        playerPetMapper.updateById(playerPet);

        log.info("出战宠物设置成功: {}", playerPet.getNickname());
    }

    /**
     * 喂食宠物
     */
    @Transactional
    public void feedPet(Integer playerId, Integer playerPetId) {
        log.info("喂食宠物: 玩家ID={}, 宠物ID={}", playerId, playerPetId);

        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("宠物不存在或不属于该玩家");
        }

        // 检查是否需要喂食
        if (playerPet.getHunger() >= 90) {
            throw new IllegalArgumentException("宠物不饿，无需喂食");
        }

        // 恢复饱食度
        int oldHunger = playerPet.getHunger();
        playerPet.setHunger(Math.min(100, oldHunger + 30));
        
        // 提升忠诚度
        int oldLoyalty = playerPet.getLoyalty();
        playerPet.setLoyalty(Math.min(100, oldLoyalty + 5));
        
        playerPet.setLastFeedTime(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);

        log.info("喂食成功: 饱食度 {} -> {}, 忠诚度 {} -> {}", 
                oldHunger, playerPet.getHunger(), oldLoyalty, playerPet.getLoyalty());
    }

    /**
     * 训练宠物
     */
    @Transactional
    public void trainPet(Integer playerId, Integer playerPetId, String trainingType) {
        log.info("========== 训练宠物 ==========");
        log.info("玩家ID: {}, 宠物ID: {}, 训练类型: {}", playerId, playerPetId, trainingType);

        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("宠物不存在或不属于该玩家");
        }

        // 检查饱食度
        if (playerPet.getHunger() < 20) {
            throw new IllegalArgumentException("宠物太饿了，无法训练");
        }

        // 消耗饱食度
        playerPet.setHunger(Math.max(0, playerPet.getHunger() - 10));

        // 根据训练类型提升属性
        int improvementValue = 0;
        String attributeImproved = "";
        
        switch (trainingType) {
            case "攻击":
                improvementValue = random.nextInt(3) + 1;
                playerPet.setAttack(playerPet.getAttack() + improvementValue);
                attributeImproved = "attack";
                break;
            case "防御":
                improvementValue = random.nextInt(3) + 1;
                playerPet.setDefense(playerPet.getDefense() + improvementValue);
                attributeImproved = "defense";
                break;
            case "速度":
                improvementValue = random.nextInt(2) + 1;
                playerPet.setSpeed(playerPet.getSpeed() + improvementValue);
                attributeImproved = "speed";
                break;
            default:
                throw new IllegalArgumentException("无效的训练类型");
        }

        // 获得经验
        int expGained = random.nextInt(20) + 10;
        playerPet.setExp(playerPet.getExp() + expGained);

        // 检查升级
        checkPetLevelUp(playerPet);

        // 提升忠诚度
        playerPet.setLoyalty(Math.min(100, playerPet.getLoyalty() + 2));
        
        playerPet.setLastTrainTime(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);

        // 记录训练日志
        PetTrainingLog log = PetTrainingLog.builder()
                .playerPetId(playerPetId)
                .trainingType(trainingType)
                .expGained(expGained)
                .attributeImproved(attributeImproved)
                .improvementValue(improvementValue)
                .createdAt(LocalDateTime.now())
                .build();
        petTrainingLogMapper.insert(log);

        this.log.info("训练完成: {}+{}, 经验+{}", trainingType, improvementValue, expGained);
        this.log.info("========== 训练宠物完成 ==========");
    }

    /**
     * 检查宠物升级
     */
    private void checkPetLevelUp(PlayerPet playerPet) {
        int levelUps = 0;
        int maxLevelUps = 50;

        while (playerPet.getExp() >= playerPet.getExpToNext() && levelUps < maxLevelUps) {
            playerPet.setLevel(playerPet.getLevel() + 1);
            playerPet.setExp(playerPet.getExp() - playerPet.getExpToNext());
            playerPet.setExpToNext((long) (playerPet.getExpToNext() * 1.5));

            // 升级属性提升
            Pet petTemplate = petMapper.selectById(playerPet.getPetId());
            if (petTemplate != null) {
                double growthRate = petTemplate.getGrowthRate().doubleValue();
                playerPet.setAttack(playerPet.getAttack() + (int) (3 * growthRate));
                playerPet.setDefense(playerPet.getDefense() + (int) (2 * growthRate));
                playerPet.setMaxHealth(playerPet.getMaxHealth() + (int) (10 * growthRate));
                playerPet.setHealth(playerPet.getMaxHealth());
                playerPet.setSpeed(playerPet.getSpeed() + (int) (1 * growthRate));
            }

            levelUps++;
            log.info("宠物升级: {}级 -> {}级", playerPet.getLevel() - 1, playerPet.getLevel());
        }
    }

    /**
     * 重命名宠物
     */
    @Transactional
    public void renamePet(Integer playerId, Integer playerPetId, String newNickname) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("宠物不存在或不属于该玩家");
        }

        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }

        if (newNickname.length() > 20) {
            throw new IllegalArgumentException("昵称过长，最多20个字符");
        }

        String oldNickname = playerPet.getNickname();
        playerPet.setNickname(newNickname.trim());
        playerPetMapper.updateById(playerPet);

        log.info("宠物重命名: {} -> {}", oldNickname, newNickname);
    }

    /**
     * 释放宠物
     */
    @Transactional
    public void releasePet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("宠物不存在或不属于该玩家");
        }

        if (playerPet.getIsLocked()) {
            throw new IllegalArgumentException("宠物已锁定，无法释放");
        }

        if (playerPet.getIsActive()) {
            throw new IllegalArgumentException("出战中的宠物无法释放");
        }

        playerPetMapper.deleteById(playerPetId);
        log.info("释放宠物: ID={}, 昵称={}", playerPetId, playerPet.getNickname());
    }

    /**
     * 锁定/解锁宠物
     */
    @Transactional
    public void toggleLockPet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("宠物不存在或不属于该玩家");
        }

        playerPet.setIsLocked(!playerPet.getIsLocked());
        playerPetMapper.updateById(playerPet);

        log.info("宠物锁定状态切换: {}", playerPet.getIsLocked() ? "已锁定" : "已解锁");
    }

    /**
     * 获取宠物的训练记录
     */
    public List<PetTrainingLog> getTrainingLogs(Integer playerPetId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return petTrainingLogMapper.selectByPlayerPetId(playerPetId, limit);
    }
}
