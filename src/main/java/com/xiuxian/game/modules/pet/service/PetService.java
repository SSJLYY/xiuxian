package com.xiuxian.game.modules.pet.service;

import com.xiuxian.game.dto.PetEvolutionResult;
// pet module entities
import com.xiuxian.game.modules.pet.entity.Pet;
import com.xiuxian.game.modules.pet.entity.PetCombatBonus;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 宠物服务类
 * 提供宠物相关的所有业务逻辑，包括宠物获取、喂养、训练、进化等核心功能
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
        return petMapper.selectAvailablePets();
    }

    /**
     * 获取玩家所有宠物
     */
    public List<PlayerPet> getPlayerPets(Integer playerId) {
        return playerPetMapper.selectByPlayerId(playerId);
    }

    /**
     * 获取玩家当前出战宠物
     */
    public PlayerPet getActivePet(Integer playerId) {
        return playerPetMapper.selectActivePetByPlayerId(playerId);
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

    /**
     * 计算宠物战斗加成
     */
    public PetCombatBonus calculatePetCombatBonus(PlayerPet activePet) {
        if (activePet == null) {
            return PetCombatBonus.zero();
        }
        // 计算饱食度和忠诚度对战斗加成的影响
        double hungerFactor = activePet.getHunger() < 30 ? 0.5 : 1.0;
        double loyaltyFactor = activePet.getLoyalty() >= 80 ? 1.2 : (activePet.getLoyalty() >= 50 ? 1.0 : 0.7);
        double levelFactor = 1.0 + activePet.getLevel() * 0.05;

        Pet pet = petMapper.selectById(activePet.getPetId());
        if (pet == null) {
            return PetCombatBonus.zero();
        }

        int attackBonus = (int) (pet.getBaseAttack() * levelFactor * hungerFactor * loyaltyFactor);
        int defenseBonus = (int) (pet.getBaseDefense() * levelFactor * hungerFactor * loyaltyFactor);
        int critChanceBonus = (int) (pet.getCritChance() * 100 * loyaltyFactor);

        return new PetCombatBonus(attackBonus, defenseBonus, critChanceBonus);
    }

    /**
     * 应用饱食度衰减
     */
    public List<PlayerPet> applyHungerDecay(Integer playerId) {
        List<PlayerPet> pets = getPlayerPets(playerId);
        for (PlayerPet pet : pets) {
            if (pet.getHunger() != null && pet.getHunger() > 0) {
                int newHunger = Math.max(0, pet.getHunger() - 5);
                pet.setHunger(newHunger);
                playerPetMapper.updateById(pet);
                log.debug("宠物饱食度衰减: petId={}, hunger={}", pet.getId(), newHunger);
            }
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

        if (player == null || pet == null) {
            throw new RuntimeException("玩家或宠物不存在");
        }

        // 检查是否已达宠物上限
        List<PlayerPet> existingPets = getPlayerPets(playerId);
        if (existingPets.size() >= 10) {
            throw new RuntimeException("已达宠物数量上限（10只）");
        }

        // 捕捉成功率
        double captureRate = pet.getCaptureRate();
        boolean success = ThreadLocalRandom.current().nextDouble() < captureRate;

        if (!success) {
            log.info("捕捉宠物失败: playerId={}, petId={}, rate={}", playerId, petId, captureRate);
            throw new RuntimeException("捕捉失败，宠物逃脱了");
        }

        // 创建玩家宠物记录
        PlayerPet playerPet = new PlayerPet();
        playerPet.setPlayerId(playerId);
        playerPet.setPetId(petId);
        playerPet.setNickname(pet.getName());
        playerPet.setLevel(1);
        playerPet.setExp(0);
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
     * 设置出战宠物
     */
    public void setActivePet(Integer playerId, Integer playerPetId) {
        // 先取消所有出战状态
        List<PlayerPet> pets = getPlayerPets(playerId);
        for (PlayerPet pet : pets) {
            if (Boolean.TRUE.equals(pet.getIsActive())) {
                pet.setIsActive(false);
                playerPetMapper.updateById(pet);
            }
        }
        // 设置新的出战宠物
        PlayerPet targetPet = playerPetMapper.selectById(playerPetId);
        if (targetPet != null && targetPet.getPlayerId().equals(playerId)) {
            targetPet.setIsActive(true);
            playerPetMapper.updateById(targetPet);
            log.info("设置出战宠物: playerId={}, petId={}", playerId, playerPetId);
        }
    }

    /**
     * 喂养宠物
     */
    @Transactional
    public void feedPet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new RuntimeException("宠物不存在或不属于该玩家");
        }

        // 消耗食物道具（简化处理，直接增加饱食度）
        int newHunger = Math.min(100, playerPet.getHunger() + 20);
        playerPet.setHunger(newHunger);
        playerPet.setLoyalty(Math.min(100, playerPet.getLoyalty() + 5));
        playerPet.setUpdatedAt(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);
        log.info("喂养宠物: playerPetId={}, newHunger={}", playerPetId, newHunger);
    }

    /**
     * 训练宠物
     */
    @Transactional
    public void trainPet(Integer playerId, Integer playerPetId, String trainingType) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new RuntimeException("宠物不存在或不属于该玩家");
        }

        Pet pet = petMapper.selectById(playerPet.getPetId());
        if (pet == null) {
            throw new RuntimeException("宠物模板不存在");
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
            throw new RuntimeException("忠诚度不足，无法进行训练");
        }

        playerPet.setExp(playerPet.getExp() + expGain);
        playerPet.setLoyalty(Math.max(0, playerPet.getLoyalty() - loyaltyCost));
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
        int level = playerPet.getLevel();
        long exp = playerPet.getExp();
        // 简化：每100经验升1级
        int newLevel = level + (int) (exp / 100);
        if (newLevel > level) {
            playerPet.setLevel(newLevel);
            playerPet.setExp(exp % 100);
            PetService.log.info("宠物升级: petId={}, newLevel={}", playerPet.getId(), newLevel);
        }
    }

    /**
     * 重命名宠物
     */
    public void renamePet(Integer playerId, Integer playerPetId, String newNickname) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new RuntimeException("宠物不存在或不属于该玩家");
        }
        playerPet.setNickname(newNickname);
        playerPet.setUpdatedAt(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);
        log.info("重命名宠物: petId={}, newNickname={}", playerPetId, newNickname);
    }

    /**
     * 放生宠物
     */
    @Transactional
    public void releasePet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new RuntimeException("宠物不存在或不属于该玩家");
        }
        if (Boolean.TRUE.equals(playerPet.getIsLocked())) {
            throw new RuntimeException("已加锁的宠物无法放生");
        }
        playerPetMapper.deleteById(playerPetId);
        log.info("放生宠物: playerId={}, petId={}", playerId, playerPetId);
    }

    /**
     * 加解锁宠物
     */
    public void toggleLockPet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new RuntimeException("宠物不存在或不属于该玩家");
        }
        boolean newLock = !Boolean.TRUE.equals(playerPet.getIsLocked());
        playerPet.setIsLocked(newLock);
        playerPet.setUpdatedAt(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);
        log.info("切换宠物锁定状态: petId={}, locked={}", playerPetId, newLock);
    }

    /**
     * 获取训练日志
     */
    public List<PetTrainingLog> getTrainingLogs(Integer playerPetId, Integer limit) {
        return petTrainingLogMapper.selectByPlayerPetId(playerPetId, limit);
    }

    /**
     * 检查宠物是否满足进化条件
     */
    public PetEvolutionResult checkEvolution(Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null) {
            return PetEvolutionResult.fail("宠物不存在");
        }

        Pet pet = petMapper.selectById(playerPet.getPetId());
        if (pet == null) {
            return PetEvolutionResult.fail("宠物模板不存在");
        }

        PetEvolution evolution = petEvolutionMapper.selectByPetId(pet.getId());
        if (evolution == null) {
            return PetEvolutionResult.fail("该宠物无法进化");
        }

        boolean levelOk = playerPet.getLevel() >= evolution.getRequiredLevel();
        boolean loyaltyOk = playerPet.getLoyalty() >= evolution.getRequiredLoyalty();

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
    public PetEvolutionResult evolvePet(Integer playerPetId) {
        PetEvolutionResult check = checkEvolution(playerPetId);
        if (!check.isSuccess()) {
            return check;
        }

        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        PetEvolution evolution = check.getEvolution();

        // 进化后的宠物
        Pet newPet = petMapper.selectById(evolution.getEvolvedPetId());
        if (newPet == null) {
            return PetEvolutionResult.fail("进化目标宠物不存在");
        }

        // 更新玩家宠物记录
        playerPet.setPetId(newPet.getId());
        playerPet.setNickname(newPet.getName());
        playerPet.setLevel(1);
        playerPet.setExp(0);
        playerPet.setLoyalty(Math.min(100, playerPet.getLoyalty() + 20));
        playerPet.setUpdatedAt(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);

        // 记录进化日志
        PlayerPetEvolution log = new PlayerPetEvolution();
        log.setPlayerPetId(playerPetId);
        log.setBeforePetId(evolution.getPetId());
        log.setAfterPetId(newPet.getId());
        log.setEvolvedAt(LocalDateTime.now());
        playerPetEvolutionMapper.insert(log);

        PetService.log.info("宠物进化成功: petId={}, newPetId={}", playerPetId, newPet.getId());
        return PetEvolutionResult.success("进化成功", null);
    }

    /**
     * 获取宠物进化信息
     */
    public Map<String, Object> getPetEvolutionInfo(Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null) {
            throw new RuntimeException("宠物不存在");
        }
        Pet pet = petMapper.selectById(playerPet.getPetId());
        PetEvolution evolution = petEvolutionMapper.selectByPetId(pet.getId());

        if (evolution == null) {
            throw new RuntimeException("该宠物无法进化");
        }

        return Map.of(
            "currentLevel", playerPet.getLevel(),
            "requiredLevel", evolution.getRequiredLevel(),
            "currentLoyalty", playerPet.getLoyalty(),
            "requiredLoyalty", evolution.getRequiredLoyalty(),
            "canEvolve", playerPet.getLevel() >= evolution.getRequiredLevel() &&
                         playerPet.getLoyalty() >= evolution.getRequiredLoyalty(),
            "evolvedPetId", evolution.getEvolvedPetId()
        );
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
            throw new RuntimeException("宠物模板不存在");
        }

        PlayerPet playerPet = new PlayerPet();
        playerPet.setPlayerId(playerId);
        playerPet.setPetId(petId);
        playerPet.setNickname(pet.getName());
        playerPet.setLevel(1);
        playerPet.setExp(0);
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
