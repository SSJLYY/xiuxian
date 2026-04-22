package com.xiuxian.game.modules.pet.service;

import com.xiuxian.game.dto.PetEvolutionResult;
// pet module entities
import com.xiuxian.game.modules.pet.entity.Pet;
import com.xiuxian.game.dto.response.PetCombatBonus;
import com.xiuxian.game.modules.pet.entity.PetEvolution;
import com.xiuxian.game.modules.pet.entity.PetSkill;
import com.xiuxian.game.modules.pet.entity.PetTrainingLog;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import com.xiuxian.game.modules.pet.entity.PlayerPetEvolution;
import com.xiuxian.game.modules.pet.entity.PlayerPetSkill;
// pet module mappers
import com.xiuxian.game.modules.pet.mapper.PetEvolutionMapper;
import com.xiuxian.game.modules.pet.mapper.PetMapper;
import com.xiuxian.game.modules.pet.mapper.PetSkillMapper;
import com.xiuxian.game.modules.pet.mapper.PetTrainingLogMapper;
import com.xiuxian.game.modules.pet.mapper.PlayerPetEvolutionMapper;
import com.xiuxian.game.modules.pet.mapper.PlayerPetMapper;
import com.xiuxian.game.modules.pet.mapper.PlayerPetSkillMapper;
// cross-module entities accessed via Service interfaces
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.shop.entity.Item;
// cross-module services accessed via Service, not Mapper
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

/**
 * 宠物服务类
 * 提供宠物相关的所有业务逻辑，包括宠物获取、喂养、训练、进化等核心功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private static final long PET_BASE_EXP_TO_NEXT = 100L;
    private static final long PET_TRAINING_COOLDOWN_MINUTES = 10L;
    private static final int DEFAULT_EVOLUTION_ITEM_QUANTITY = 1;

    private final PetMapper petMapper;
    private final PlayerPetMapper playerPetMapper;
    private final PetSkillMapper petSkillMapper;
    private final PlayerPetSkillMapper playerPetSkillMapper;
    private final PetTrainingLogMapper petTrainingLogMapper;
    private final PetEvolutionMapper petEvolutionMapper;
    private final PlayerPetEvolutionMapper playerPetEvolutionMapper;
    private final PlayerService playerService;
    private final ItemService itemService;

    /**
     * 获取所有宠物列表（后台管理用）
     */
    public List<Pet> getAllPets() {
        return petMapper.selectList(null);
    }

    /**
     * 获取玩家可捕捉的宠物列表
     */
    public List<Pet> getAvailablePets(Integer playerId) {
        // 根据玩家等级获取可解锁宠物，若无法获取等级则查询所有可用宠物
        try {
            PlayerProfile profile = playerService.getPlayerProfileById(playerId);
            int playerLevel = profile != null && profile.getLevel() != null ? profile.getLevel() : 1;
            return petMapper.selectAvailablePets(playerLevel);
        } catch (Exception e) {
            return petMapper.selectAvailablePets(1);
        }
    }

    /**
     * 获取玩家所有宠物
     */
    public List<PlayerPet> getPlayerPets(Integer playerId) {
        List<PlayerPet> pets = playerPetMapper.selectByPlayerId(playerId);
        LocalDateTime now = LocalDateTime.now();
        for (PlayerPet pet : pets) {
            if (pet.getLastTrainTime() != null) {
                pet.setTrainCooldownUntil(pet.getLastTrainTime().plusMinutes(PET_TRAINING_COOLDOWN_MINUTES));
            } else {
                pet.setTrainCooldownUntil(now.minusSeconds(1));
            }
        }
        return pets;
    }

    /**
     * 获取玩家当前出战宠物
     */
    public PlayerPet getActivePet(Integer playerId) {
        PlayerPet pet = playerPetMapper.selectActivePet(playerId);
        if (pet != null && pet.getLastTrainTime() != null) {
            pet.setTrainCooldownUntil(pet.getLastTrainTime().plusMinutes(PET_TRAINING_COOLDOWN_MINUTES));
        }
        return pet;
    }

    public PlayerPetMapper getPlayerPetMapper() {
        return playerPetMapper;
    }

    /**
     * 战斗后消耗宠物饱食度
     */
    public void consumePetHungerAfterCombat(Integer playerId) {
        PlayerPet activePet = getActivePet(playerId);
        if (activePet != null && activePet.getHunger() != null) {
            int newHunger = Math.max(0, activePet.getHunger() - 10);
            activePet.setHunger(newHunger);
            playerPetMapper.updateById(activePet);
            log.debug("战斗后宠物饱食度消耗: playerId={}, petId={}, hunger={}", playerId, activePet.getId(), newHunger);
        }
    }

    public void updatePlayerPet(PlayerPet playerPet) {
        if (playerPet != null) {
            playerPetMapper.updateById(playerPet);
        }
    }

    private PlayerPet getOwnedPlayerPet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "宠物不存在或不属于该玩家");
        }
        return playerPet;
    }

    /**
     * 计算宠物战斗加成
     * GDD设计：宠物在战斗中提供额外伤害、忠诚度影响技能发动概率、饱食度影响参战效果
     */
    public PetCombatBonus calculatePetCombatBonus(PlayerPet activePet) {
        if (activePet == null) {
            return PetCombatBonus.builder()
                    .skillTriggerChance(0.0)
                    .skillDamage(0)
                    .skillCooldown(3)
                    .resonance(false)
                    .hungerFactor(0.0)
                    .loyalty(0)
                    .hunger(0)
                    .build();
        }
        // 计算饱食度和忠诚度对战斗加成的影响
        int hunger = activePet.getHunger() != null ? activePet.getHunger() : 0;
        int loyalty = activePet.getLoyalty() != null ? activePet.getLoyalty() : 0;
        double hungerFactor = hunger < 20 ? 0.5 : 1.0;
        double loyaltyFactor = loyalty >= 80 ? 1.2 : (loyalty >= 50 ? 1.0 : 0.7);
        double levelFactor = 1.0 + activePet.getLevel() * 0.05;

        Pet pet = petMapper.selectById(activePet.getPetId());
        if (pet == null) {
            return PetCombatBonus.builder()
                    .skillTriggerChance(0.0)
                    .skillDamage(0)
                    .skillCooldown(3)
                    .resonance(false)
                    .hungerFactor(hungerFactor)
                    .loyalty(loyalty)
                    .hunger(hunger)
                    .build();
        }

        // 技能发动概率：忠诚度/100 × 基础概率
        double skillTriggerChance = (loyalty / 100.0) * 0.3 * loyaltyFactor;
        // 技能伤害：基于宠物攻击力和等级
        int skillDamage = (int) (pet.getBaseAttack() * levelFactor * hungerFactor * loyaltyFactor);
        // 共鸣：忠诚度100且低概率触发
        boolean resonance = loyalty >= 100 && ThreadLocalRandom.current().nextDouble() < 0.05;

        return PetCombatBonus.builder()
                .petId(pet.getId())
                .petName(pet.getName())
                .skillTriggerChance(skillTriggerChance)
                .skillDamage(skillDamage)
                .skillCooldown(3)
                .resonance(resonance)
                .hungerFactor(hungerFactor)
                .loyalty(loyalty)
                .hunger(hunger)
                .build();
    }

    /**
     * 应用饱食度衰减（批量update，避免循环updateById）
     */
    public List<PlayerPet> applyHungerDecay(Integer playerId) {
        List<PlayerPet> pets = getPlayerPets(playerId);
        List<PlayerPet> toUpdate = new ArrayList<>();
        for (PlayerPet pet : pets) {
            if (pet.getHunger() != null && pet.getHunger() > 0) {
                int newHunger = Math.max(0, pet.getHunger() - 5);
                pet.setHunger(newHunger);
                toUpdate.add(pet);
                log.debug("宠物饱食度衰减: petId={}, hunger={}", pet.getId(), newHunger);
            }
        }
        // 批量更新（MyBatis-Plus IService 默认批量大小1000）
        if (!toUpdate.isEmpty()) {
            playerPetMapper.updateHungerBatch(toUpdate);
        }
        return pets;
    }

    /**
     * 捕捉宠物
     */
    @Transactional
    public PlayerPet capturePet(Integer playerId, Integer petId) {
        PlayerProfile player = playerService.getPlayerProfile(playerId);
        Pet pet = petMapper.selectById(petId);

        if (player == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "玩家不存在");
        }
        if (pet == null) {
            throw new BusinessException(ErrorCode.PET_NOT_FOUND, "宠物不存在");
        }

        // 检查是否已达宠物上限
        List<PlayerPet> existingPets = getPlayerPets(playerId);
        if (existingPets.size() >= 10) {
            throw new BusinessException(ErrorCode.PET_ALREADY_MAX, "已达宠物数量上限（10只）");
        }

        // 捕捉成功率
        double captureRate = pet.getCaptureRate() != null ? pet.getCaptureRate().doubleValue() / 100.0 : 0.5;
        boolean success = ThreadLocalRandom.current().nextDouble() < captureRate;

        if (!success) {
            log.info("捕捉宠物失败: playerId={}, petId={}, rate={}", playerId, petId, captureRate);
            throw new BusinessException(ErrorCode.PET_CAPTURE_FAILED, "捕捉失败，宠物逃脱了");
        }

        // 创建玩家宠物记录
        PlayerPet playerPet = new PlayerPet();
        playerPet.setPlayerId(playerId);
        playerPet.setPetId(petId);
        playerPet.setNickname(pet.getName());
        playerPet.setLevel(1);
        playerPet.setExp(0L);
        playerPet.setExpToNext(PET_BASE_EXP_TO_NEXT);
        playerPet.setAttack(pet.getBaseAttack());
        playerPet.setDefense(pet.getBaseDefense());
        playerPet.setHealth(pet.getBaseHealth());
        playerPet.setMaxHealth(pet.getBaseHealth());
        playerPet.setSpeed(pet.getBaseSpeed());
        playerPet.setHunger(100);
        playerPet.setLoyalty(50);
        playerPet.setIsActive(false);
        playerPet.setIsLocked(false);
        playerPet.setCreatedAt(LocalDateTime.now());
        playerPet.setUpdatedAt(LocalDateTime.now());

        playerPetMapper.insert(playerPet);
        log.info("捕捉宠物成功: playerId={}, petId={}, playerPetId={}", playerId, petId, playerPet.getId());
        return playerPet;
    }

    /**
     * 设置出战宠物（用原子SQL批量取消，避免循环updateById）
     */
    public void setActivePet(Integer playerId, Integer playerPetId) {
        PlayerPet targetPet = getOwnedPlayerPet(playerId, playerPetId);
        // 原子SQL：取消所有出战状态（1次DB写，替代循环updateById）
        playerPetMapper.deactivateAllPets(playerId);
        // 设置新的出战宠物
        targetPet.setIsActive(true);
        targetPet.setUpdatedAt(LocalDateTime.now());
        playerPetMapper.updateById(targetPet);
        log.info("设置出战宠物: playerId={}, petId={}", playerId, playerPetId);
    }

    /**
     * 喂养宠物
     */
    @Transactional
    public void feedPet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = getOwnedPlayerPet(playerId, playerPetId);

        // 消耗食物道具（简化处理，直接增加饱食度）
        int newHunger = Math.min(100, playerPet.getHunger() + 20);
        playerPet.setHunger(newHunger);
        playerPet.setLoyalty(Math.min(100, playerPet.getLoyalty() + 5));
        playerPet.setLastFeedTime(LocalDateTime.now());
        playerPet.setUpdatedAt(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);
        log.info("喂养宠物: playerPetId={}, newHunger={}", playerPetId, newHunger);
    }

    /**
     * 训练宠物
     */
    @Transactional
    public void trainPet(Integer playerId, Integer playerPetId, String trainingType) {
        PlayerPet playerPet = getOwnedPlayerPet(playerId, playerPetId);

        LocalDateTime cooldownUntil = playerPet.getLastTrainTime() == null
                ? null
                : playerPet.getLastTrainTime().plusMinutes(PET_TRAINING_COOLDOWN_MINUTES);
        if (cooldownUntil != null && cooldownUntil.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "训练冷却中，请稍后再试");
        }

        int expGain = 0;
        int loyaltyCost = 0;

        switch (trainingType) {
            case "普通训练":
                expGain = 10;
                loyaltyCost = 5;
                break;
            case "强化训练":
                expGain = 30;
                loyaltyCost = 15;
                break;
            case "特训":
                expGain = 80;
                loyaltyCost = 30;
                break;
            default:
                expGain = 10;
                loyaltyCost = 5;
        }

        // 忠诚度不足无法训练
        if (playerPet.getLoyalty() < loyaltyCost) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "忠诚度不足，无法进行训练");
        }

        playerPet.setExp(playerPet.getExp() + expGain);
        playerPet.setLoyalty(Math.max(0, playerPet.getLoyalty() - loyaltyCost));
        playerPet.setLastTrainTime(LocalDateTime.now());
        playerPet.setUpdatedAt(LocalDateTime.now());

        // 检查升级
        checkPetLevelUp(playerPet);
        playerPetMapper.updateById(playerPet);

        // 记录训练日志
        PetTrainingLog log = new PetTrainingLog();
        log.setPlayerPetId(playerPetId);
        log.setTrainingType(trainingType);
        log.setExpGained(expGain);
        log.setCreatedAt(LocalDateTime.now());
        petTrainingLogMapper.insert(log);

        PetService.log.info("训练宠物: petId={}, type={}, expGain={}", playerPetId, trainingType, expGain);
    }

    /**
     * 检查宠物是否升级
     */
    private void checkPetLevelUp(PlayerPet playerPet) {
        int levelUps = 0;
        while (playerPet.getExp() >= playerPet.getExpToNext()) {
            playerPet.setExp(playerPet.getExp() - playerPet.getExpToNext());
            playerPet.setLevel(playerPet.getLevel() + 1);
            playerPet.setExpToNext(PET_BASE_EXP_TO_NEXT + (playerPet.getLevel() - 1L) * 20L);
            playerPet.setAttack(playerPet.getAttack() + 2);
            playerPet.setDefense(playerPet.getDefense() + 1);
            playerPet.setHealth(playerPet.getHealth() + 10);
            playerPet.setMaxHealth(playerPet.getMaxHealth() + 10);
            playerPet.setSpeed(playerPet.getSpeed() + 1);
            levelUps++;
        }
        if (levelUps > 0) {
            PetService.log.info("宠物升级: petId={}, levelUps={}, newLevel={}", playerPet.getId(), levelUps, playerPet.getLevel());
        }
    }

    /**
     * 重命名宠物
     */
    public void renamePet(Integer playerId, Integer playerPetId, String newNickname) {
        PlayerPet playerPet = getOwnedPlayerPet(playerId, playerPetId);
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "昵称不能为空");
        }
        String normalizedNickname = newNickname.trim();
        if (normalizedNickname.length() > 30) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "昵称长度不能超过30个字符");
        }
        playerPet.setNickname(normalizedNickname);
        playerPet.setUpdatedAt(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);
        log.info("重命名宠物: petId={}, newNickname={}", playerPetId, newNickname);
    }

    /**
     * 放生宠物
     */
    @Transactional
    public void releasePet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = getOwnedPlayerPet(playerId, playerPetId);
        if (Boolean.TRUE.equals(playerPet.getIsLocked())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "已加锁的宠物无法放生");
        }
        if (Boolean.TRUE.equals(playerPet.getIsActive())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "出战中的宠物无法放生，请先取消出战");
        }
        playerPetMapper.deleteById(playerPetId);
        log.info("放生宠物: playerId={}, petId={}", playerId, playerPetId);
    }

    /**
     * 加解锁宠物
     */
    public void toggleLockPet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = getOwnedPlayerPet(playerId, playerPetId);
        boolean newLock = !Boolean.TRUE.equals(playerPet.getIsLocked());
        playerPet.setIsLocked(newLock);
        playerPet.setUpdatedAt(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);
        log.info("切换宠物锁定状态: petId={}, locked={}", playerPetId, newLock);
    }

    /**
     * 获取训练日志
     */
    public List<PetTrainingLog> getTrainingLogs(Integer playerId, Integer playerPetId, Integer limit) {
        getOwnedPlayerPet(playerId, playerPetId);
        return petTrainingLogMapper.selectByPlayerPetId(playerPetId, limit);
    }

    /**
     * 检查宠物是否满足进化条件
     */
    public PetEvolutionResult checkEvolution(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = getOwnedPlayerPet(playerId, playerPetId);

        Pet pet = petMapper.selectById(playerPet.getPetId());
        if (pet == null) {
            return PetEvolutionResult.fail("宠物模板不存在");
        }

        List<PetEvolution> evolutions = petEvolutionMapper.selectByPetId(pet.getId());
        if (evolutions == null || evolutions.isEmpty()) {
            return PetEvolutionResult.fail("该宠物无法进化");
        }
        PetEvolution evolution = evolutions.get(0);

        boolean levelOk = playerPet.getLevel() >= (evolution.getRequiredLevel() != null ? evolution.getRequiredLevel() : 1);
        boolean loyaltyOk = evolution.getRequiredLoyalty() == null || playerPet.getLoyalty() >= evolution.getRequiredLoyalty();

        if (levelOk && loyaltyOk) {
            return PetEvolutionResult.success("满足进化条件", evolution);
        } else {
            String reason = (!levelOk ? "等级不足" : "") + (!loyaltyOk ? "忠诚度不足" : "");
            return PetEvolutionResult.fail(reason.trim());
        }
    }

    /**
     * 进化宠物
     */
    @Transactional
    public PetEvolutionResult evolvePet(Integer playerId, Integer playerPetId) {
        PetEvolutionResult check = checkEvolution(playerId, playerPetId);
        if (!check.isSuccess()) {
            return check;
        }

        PlayerPet playerPet = getOwnedPlayerPet(playerId, playerPetId);
        PetEvolution evolution = check.getEvolution();

        // 进化后的宠物
        Pet newPet = petMapper.selectById(evolution.getEvolvedPetId());
        if (newPet == null) {
            return PetEvolutionResult.fail("进化目标宠物不存在");
        }

        Integer requiredItemId = evolution.getRequiredItemId();
        int requiredItemQuantity = evolution.getRequiredItemQuantity() == null || evolution.getRequiredItemQuantity() <= 0
                ? DEFAULT_EVOLUTION_ITEM_QUANTITY
                : evolution.getRequiredItemQuantity();
        PlayerItem evolutionItem = requiredItemId == null
                ? null
                : playerService.getPlayerItemByPlayerAndItem(playerId, requiredItemId);
        if (requiredItemId != null && (evolutionItem == null || evolutionItem.getQuantity() == null || evolutionItem.getQuantity() < requiredItemQuantity)) {
            return PetEvolutionResult.fail("缺少进化丹，无法进化");
        }

        if (requiredItemId != null) {
            if (evolutionItem.getQuantity() == requiredItemQuantity) {
                playerService.deletePlayerItem(evolutionItem.getId());
            } else {
                evolutionItem.setQuantity(evolutionItem.getQuantity() - requiredItemQuantity);
                playerService.updatePlayerItem(evolutionItem);
            }
        }

        // 更新玩家宠物记录
        playerPet.setPetId(newPet.getId());
        playerPet.setLoyalty(Math.min(100, playerPet.getLoyalty() + 20));
        playerPet.setUpdatedAt(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);

        // 记录进化日志
        PlayerPetEvolution evolutionLog = new PlayerPetEvolution();
        evolutionLog.setPlayerPetId(playerPetId);
        evolutionLog.setCurrentStage(evolution.getEvolutionStage() != null ? evolution.getEvolutionStage() : 1);
        evolutionLog.setEvolvedAt(LocalDateTime.now());
        playerPetEvolutionMapper.insert(evolutionLog);

        PetService.log.info("宠物进化成功: petId={}, newPetId={}", playerPetId, newPet.getId());
        return PetEvolutionResult.success("进化成功", null);
    }

    /**
     * 获取宠物进化信息
     */
    public Map<String, Object> getPetEvolutionInfo(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = getOwnedPlayerPet(playerId, playerPetId);
        Pet pet = petMapper.selectById(playerPet.getPetId());
        List<PetEvolution> evolutions = petEvolutionMapper.selectByPetId(pet.getId());

        if (evolutions == null || evolutions.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该宠物无法进化");
        }
        PetEvolution evolution = evolutions.get(0);
        Pet evolvedPet = petMapper.selectById(evolution.getEvolvedPetId());

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("currentLevel", playerPet.getLevel());
        result.put("requiredLevel", evolution.getRequiredLevel());
        result.put("currentLoyalty", playerPet.getLoyalty());
        result.put("requiredLoyalty", evolution.getRequiredLoyalty());
        result.put("canEvolve", playerPet.getLevel() >= (evolution.getRequiredLevel() != null ? evolution.getRequiredLevel() : 1)
                && (evolution.getRequiredLoyalty() == null || playerPet.getLoyalty() >= evolution.getRequiredLoyalty()));
        result.put("evolvedPetId", evolution.getEvolvedPetId());
        Integer requiredItemId = evolution.getRequiredItemId();
        int requiredItemQuantity = evolution.getRequiredItemQuantity() == null || evolution.getRequiredItemQuantity() <= 0
                ? DEFAULT_EVOLUTION_ITEM_QUANTITY
                : evolution.getRequiredItemQuantity();
        result.put("requiredItemId", requiredItemId);
        result.put("requiredItemQuantity", requiredItemQuantity);
        result.put("currentPetName", pet.getName());
        result.put("currentPetNickname", playerPet.getNickname());
        result.put("targetPetName", evolvedPet != null ? evolvedPet.getName() : null);
        result.put("attackBonus", evolution.getAttackBonus());
        result.put("defenseBonus", evolution.getDefenseBonus());
        result.put("healthBonus", evolution.getHealthBonus());
        result.put("speedBonus", evolution.getSpeedBonus());
        PlayerItem evolutionItem = requiredItemId == null ? null : playerService.getPlayerItemByPlayerAndItem(playerId, requiredItemId);
        result.put("hasRequiredItem", requiredItemId == null || (evolutionItem != null && evolutionItem.getQuantity() != null && evolutionItem.getQuantity() >= requiredItemQuantity));
        return result;
    }

    /**
     * 根据ID获取宠物模板
     */
    public Pet getPetById(Integer petId) {
        return petMapper.selectById(petId);
    }

    /**
     * 根据ID获取玩家宠物
     */
    public PlayerPet getPlayerPetById(Long playerPetId) {
        return playerPetMapper.selectById(playerPetId);
    }

    /**
     * 直接授予宠物（后台管理用）
     */
    @Transactional
    public PlayerPet grantPetDirectly(Integer playerId, Integer petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "宠物模板不存在");
        }

        PlayerPet playerPet = new PlayerPet();
        playerPet.setPlayerId(playerId);
        playerPet.setPetId(petId);
        playerPet.setNickname(pet.getName());
        playerPet.setLevel(1);
        playerPet.setExp(0L);
        playerPet.setHunger(100);
        playerPet.setLoyalty(80);
        playerPet.setIsActive(false);
        playerPet.setIsLocked(false);
        playerPet.setCreatedAt(LocalDateTime.now());
        playerPet.setUpdatedAt(LocalDateTime.now());

        playerPetMapper.insert(playerPet);
        log.info("后台直接授予宠物: playerId={}, petId={}", playerId, petId);
        return playerPet;
    }

    /**
     * 删除玩家宠物（后台管理用）
     */
    @Transactional
    public void deletePlayerPet(Long playerPetId) {
        playerPetMapper.deleteById(playerPetId);
        log.info("后台删除玩家宠物: playerPetId={}", playerPetId);
    }
}
