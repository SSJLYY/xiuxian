package com.xiuxian.game.service;

import com.xiuxian.game.dto.PetEvolutionResult;
import com.xiuxian.game.entity.*;
import com.xiuxian.game.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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
    private final PetEvolutionMapper petEvolutionMapper;
    private final PlayerPetEvolutionMapper playerPetEvolutionMapper;
    private final PlayerItemMapper playerItemMapper;
    private final ItemMapper itemMapper;

    // 【修复】使用 ThreadLocalRandom 替代共享 Random 实例。
    // PetService 是 Spring 单例，所有并发请求共享同一实例。
    // ThreadLocalRandom 每个线程独立，无锁竞争，性能更优。
    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

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
     * 获取PlayerPetMapper（供其他服务使用）
     */
    public PlayerPetMapper getPlayerPetMapper() {
        return playerPetMapper;
    }
    
    /**
     * GDD：战后宠物饱食度衰减
     * 每次战斗消耗balance.getPet().getCombatHungerCost()点饱食度
     */
    @Transactional
    public void consumePetHungerAfterCombat(Integer playerId) {
        PlayerPet activePet = playerPetMapper.selectActivePet(playerId);
        if (activePet != null && activePet.getHunger() > 0) {
            int cost = balance.getPet().getCombatHungerCost();
            int newHunger = Math.max(0, activePet.getHunger() - cost);
            activePet.setHunger(newHunger);
            playerPetMapper.updateById(activePet);
            log.info("战后宠物饱食度衰减: {} -> {}", newHunger + cost, newHunger);
        }
    }

    /**
     * GDD宠物参战机制：计算宠物战斗增益
     * 
     * 设计原则（GDD）：
     * - 忠诚度影响技能发动概率：0-30:60%, 31-80:80%, 81-100:100%（还有5%共鸣×2伤害）
     * - 饱食度影响参战效果：<20时降低50%
     * - 宠物每N回合发动技能（N = 3 + 宠物速度/10）
     * 
     * @return PetCombatBonus 包含技能发动概率、技能伤害加成、是否触发共鸣
     */
    public PetCombatBonus calculatePetCombatBonus(PlayerPet activePet) {
        if (activePet == null) {
            return null;
        }
        
        int loyalty = activePet.getLoyalty();
        int hunger = activePet.getHunger();
        
        // 忠诚度影响技能发动概率
        double skillTriggerChance;
        if (loyalty <= 30) {
            skillTriggerChance = 0.60;
        } else if (loyalty <= 80) {
            skillTriggerChance = 0.80;
        } else {
            skillTriggerChance = 1.00; // 81-100: 100% + 5%共鸣
        }
        
        // 饱食度影响（<20时降低50%效果）
        double hungerFactor = (hunger < 20) ? 0.5 : 1.0;
        
        // 计算宠物技能伤害（基于宠物基础属性）
        Pet pet = petMapper.selectById(activePet.getPetId());
        int basePetDamage = (pet != null) ? pet.getBaseAttack() : 10;
        int petLevel = activePet.getLevel();

        // 技能伤害 = 基础伤害 × 技能等级系数 × 忠诚度因子
        double loyaltyFactor = (loyalty >= 81) ? 1.25 : (loyalty >= 51 ? 1.1 : 1.0);
        int skillDamage = (int)(basePetDamage * (1 + petLevel * 0.1) * loyaltyFactor * hungerFactor);

        // 技能触发间隔（回合）
        int petSpeed = (pet != null) ? pet.getBaseSpeed() : 10;
        int skillCooldown = 3 + petSpeed / 10;

        // 是否触发共鸣（忠诚度81-100时5%概率）
        boolean resonance = (loyalty >= 81) && (rng().nextDouble() < 0.05);
        if (resonance) {
            skillDamage *= 2;
        }
        
        return PetCombatBonus.builder()
                .petId(activePet.getPetId())
                .petName(activePet.getNickname() != null ? activePet.getNickname() : (pet != null ? pet.getName() : "灵兽"))
                .skillTriggerChance(skillTriggerChance)
                .skillDamage(skillDamage)
                .skillCooldown(skillCooldown)
                .resonance(resonance)
                .hungerFactor(hungerFactor)
                .loyalty(loyalty)
                .hunger(hunger)
                .build();
    }
    
    /**
     * GDD：宠物饱食度自然衰减
     * 每小时衰减2点，训练消耗10点，战斗消耗3点
     * 
     * @param playerId 玩家ID
     * @return 衰减后的宠物列表
     */
    @Transactional
    public List<PlayerPet> applyHungerDecay(Integer playerId) {
        List<PlayerPet> pets = playerPetMapper.selectByPlayerId(playerId);
        LocalDateTime now = LocalDateTime.now();
        
        for (PlayerPet pet : pets) {
            if (pet.getIsActive() == null || !pet.getIsActive()) {
                continue; // 只处理active宠物
            }
            
            // 计算离线时间（小时）
            LocalDateTime lastUpdate = pet.getLastFeedTime() != null ? pet.getLastFeedTime() : now;
            long hoursSinceLastUpdate = java.time.Duration.between(lastUpdate, now).toHours();
            
            if (hoursSinceLastUpdate > 0) {
                int decay = (int)(hoursSinceLastUpdate * 2); // 每小时2点
                int oldHunger = pet.getHunger();
                pet.setHunger(Math.max(0, oldHunger - decay));
                
                // 饱食度0时忠诚度下降
                if (pet.getHunger() == 0 && pet.getLoyalty() > 0) {
                    pet.setLoyalty(Math.max(0, pet.getLoyalty() - (int)hoursSinceLastUpdate));
                }
                
                playerPetMapper.updateById(pet);
                log.info("宠物{}饱食度衰减: {} -> {}, 离线{}小时", 
                    pet.getNickname(), oldHunger, pet.getHunger(), hoursSinceLastUpdate);
            }
        }
        return pets;
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
        double roll = rng().nextDouble() * 100;
        
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
        playerPet.setHunger(Math.min(100, oldHunger + balance.getPet().getFeedingHungerRestore()));
        
        // 提升忠诚度
        int oldLoyalty = playerPet.getLoyalty();
        playerPet.setLoyalty(Math.min(100, oldLoyalty + balance.getPet().getFeedingLoyaltyBoost()));
        
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
                improvementValue = rng().nextInt(3) + 1;
                playerPet.setAttack(playerPet.getAttack() + improvementValue);
                attributeImproved = "attack";
                break;
            case "防御":
                improvementValue = rng().nextInt(3) + 1;
                playerPet.setDefense(playerPet.getDefense() + improvementValue);
                attributeImproved = "defense";
                break;
            case "速度":
                improvementValue = rng().nextInt(2) + 1;
                playerPet.setSpeed(playerPet.getSpeed() + improvementValue);
                attributeImproved = "speed";
                break;
            default:
                throw new IllegalArgumentException("无效的训练类型");
        }

        // 获得经验
        int expGained = rng().nextInt(20) + 10;
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

    // ==================== 宠物进化系统 ====================

    /**
     * GDD宠物进化系统：检查宠物是否可以进化
     * 进化条件：
     * 1. 宠物等级达到进化门槛
     * 2. 忠诚度 >= 80
     * 3. 拥有所需进化道具
     *
     * @param playerPetId 玩家宠物ID
     * @return 可进化的信息，null表示不可进化
     */
    public PetEvolutionResult checkEvolution(Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("宠物不存在")
                    .build();
        }

        // 获取当前进化阶段
        int currentStage = 1;
        PlayerPetEvolution evolution = playerPetEvolutionMapper.selectByPlayerPetId(playerPetId);
        if (evolution != null) {
            currentStage = evolution.getCurrentStage();
        }

        // 获取下一个可进化阶段
        PetEvolution nextEvolution = petEvolutionMapper.selectNextEvolution(playerPet.getPetId(), currentStage);
        if (nextEvolution == null) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("该宠物已达到最高进化阶段")
                    .currentStage(currentStage)
                    .build();
        }

        // 检查等级要求
        if (playerPet.getLevel() < nextEvolution.getRequiredLevel()) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("宠物等级不足，需要" + nextEvolution.getRequiredLevel() + "级（当前" + playerPet.getLevel() + "级）")
                    .currentStage(currentStage)
                    .build();
        }

        // 检查忠诚度要求
        if (playerPet.getLoyalty() < 80) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("宠物忠诚度不足，需要80点（当前" + playerPet.getLoyalty() + "点）")
                    .currentStage(currentStage)
                    .build();
        }

        // 检查进化道具
        if (nextEvolution.getRequiredItemId() != null) {
            PlayerItem item = playerItemMapper.selectByPlayerIdAndItemId(playerPet.getPlayerId(), nextEvolution.getRequiredItemId());
            int owned = (item != null) ? item.getQuantity() : 0;
            if (owned < nextEvolution.getRequiredItemQuantity()) {
                Item itemTemplate = itemMapper.selectById(nextEvolution.getRequiredItemId());
                String itemName = (itemTemplate != null) ? itemTemplate.getName() : "进化道具";
                return PetEvolutionResult.builder()
                        .success(false)
                        .message("缺少进化道具，需要" + itemName + "×" + nextEvolution.getRequiredItemQuantity() + "（拥有" + owned + "）")
                        .currentStage(currentStage)
                        .build();
            }
        }

        // 可以进化
        return PetEvolutionResult.builder()
                .success(true)
                .message("可以进化！")
                .newName(nextEvolution.getEvolutionName())
                .currentStage(nextEvolution.getEvolutionStage())
                .newAbilityName(nextEvolution.getNewAbilityId() != null ? "新能力" : null)
                .build();
    }

    /**
     * GDD宠物进化系统：执行宠物进化
     *
     * @param playerPetId 玩家宠物ID
     * @return 进化结果
     */
    @Transactional
    public PetEvolutionResult evolvePet(Integer playerPetId) {
        log.info("========== 宠物进化 ==========");
        log.info("玩家宠物ID: {}", playerPetId);

        // 验证宠物
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("宠物不存在")
                    .build();
        }

        // 检查是否可以进化
        PetEvolutionResult checkResult = checkEvolution(playerPetId);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }

        // 获取当前进化阶段
        int currentStage = 1;
        PlayerPetEvolution evolution = playerPetEvolutionMapper.selectByPlayerPetId(playerPetId);
        if (evolution != null) {
            currentStage = evolution.getCurrentStage();
        }

        // 获取下一个进化阶段
        PetEvolution nextEvolution = petEvolutionMapper.selectNextEvolution(playerPet.getPetId(), currentStage);
        if (nextEvolution == null) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("没有可用的进化阶段")
                    .build();
        }

        // 扣除进化道具
        if (nextEvolution.getRequiredItemId() != null) {
            PlayerItem item = playerItemMapper.selectByPlayerIdAndItemId(playerPet.getPlayerId(), nextEvolution.getRequiredItemId());
            if (item != null && item.getQuantity() >= nextEvolution.getRequiredItemQuantity()) {
                item.setQuantity(item.getQuantity() - nextEvolution.getRequiredItemQuantity());
                if (item.getQuantity() <= 0) {
                    playerItemMapper.deleteById(item.getId());
                } else {
                    playerItemMapper.updateById(item);
                }
                log.info("消耗进化道具: {} ×{}", nextEvolution.getRequiredItemId(), nextEvolution.getRequiredItemQuantity());
            }
        }

        // 应用进化加成
        playerPet.setAttack(playerPet.getAttack() + nextEvolution.getAttackBonus());
        playerPet.setDefense(playerPet.getDefense() + nextEvolution.getDefenseBonus());
        playerPet.setMaxHealth(playerPet.getMaxHealth() + nextEvolution.getHealthBonus());
        playerPet.setHealth(playerPet.getMaxHealth()); // 满血
        playerPet.setSpeed(playerPet.getSpeed() + nextEvolution.getSpeedBonus());
        playerPet.setNickname(nextEvolution.getEvolutionName());

        // 更新或创建进化记录
        if (evolution != null) {
            evolution.setCurrentStage(nextEvolution.getEvolutionStage());
            evolution.setEvolvedAt(LocalDateTime.now());
            playerPetEvolutionMapper.updateById(evolution);
        } else {
            evolution = PlayerPetEvolution.builder()
                    .playerPetId(playerPetId)
                    .currentStage(nextEvolution.getEvolutionStage())
                    .evolvedAt(LocalDateTime.now())
                    .build();
            playerPetEvolutionMapper.insert(evolution);
        }

        playerPetMapper.updateById(playerPet);

        log.info("宠物进化成功: {} -> {}, 等级{}",
                playerPet.getNickname(), nextEvolution.getEvolutionName(), playerPet.getLevel());
        log.info("属性提升: 攻击+{}, 防御+{}, 生命+{}, 速度+{}",
                nextEvolution.getAttackBonus(), nextEvolution.getDefenseBonus(),
                nextEvolution.getHealthBonus(), nextEvolution.getSpeedBonus());
        log.info("========== 宠物进化完成 ==========");

        return PetEvolutionResult.builder()
                .success(true)
                .message("进化成功！")
                .newName(nextEvolution.getEvolutionName())
                .newLevel(playerPet.getLevel())
                .newAttack(playerPet.getAttack())
                .newDefense(playerPet.getDefense())
                .newHealth(playerPet.getMaxHealth())
                .newSpeed(playerPet.getSpeed())
                .currentStage(nextEvolution.getEvolutionStage())
                .build();
    }

    /**
     * 获取宠物的进化信息
     */
    public Map<String, Object> getPetEvolutionInfo(Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null) {
            return Map.of("error", "宠物不存在");
        }

        // 获取当前进化阶段
        int currentStage = 1;
        PlayerPetEvolution evolution = playerPetEvolutionMapper.selectByPlayerPetId(playerPetId);
        if (evolution != null) {
            currentStage = evolution.getCurrentStage();
        }

        // 获取所有进化阶段
        List<PetEvolution> allEvolutions = petEvolutionMapper.selectByPetId(playerPet.getPetId());

        // 获取下一个可进化阶段
        PetEvolution nextEvolution = petEvolutionMapper.selectNextEvolution(playerPet.getPetId(), currentStage);

        return Map.of(
                "petId", playerPetId,
                "currentName", playerPet.getNickname(),
                "currentStage", currentStage,
                "currentLevel", playerPet.getLevel(),
                "currentLoyalty", playerPet.getLoyalty(),
                "allEvolutionStages", allEvolutions.size(),
                "hasNextEvolution", nextEvolution != null,
                "canEvolve", checkEvolution(playerPetId).isSuccess()
        );
    }
}
