package com.xiuxian.game.modules.combat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiuxian.game.modules.combat.entity.CombatLog;
import com.xiuxian.game.modules.combat.entity.Monster;
import com.xiuxian.game.modules.combat.mapper.CombatLogMapper;
import com.xiuxian.game.modules.combat.mapper.MonsterMapper;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import com.xiuxian.game.modules.pet.service.PetService;
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.skill.entity.PlayerSkill;
import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.service.SkillService;
import com.xiuxian.game.dto.SkillComboResult;
import com.xiuxian.game.dto.response.PetCombatBonus;
import com.xiuxian.game.modules.shop.entity.Item;
import com.xiuxian.game.modules.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

/**
 * 增强战斗服务类
 * 支持技能、道具参战的增强战斗系统
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedCombatService {

    private final PlayerService playerService;
    private final SkillService skillService;
    private final ItemService itemService;
    private final MonsterMapper monsterMapper;
    private final CombatLogMapper combatLogMapper;
    private final EquipmentService equipmentService;
    private final PetService petService;
    private final ObjectMapper objectMapper;
    private final CombatService combatService;

    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

    private static class TurnOutcome {
        private final int damage;
        private final int heal;
        private final int manaCost;

        private TurnOutcome(int damage, int heal, int manaCost) {
            this.damage = damage;
            this.heal = heal;
            this.manaCost = manaCost;
        }
    }

    /**
     * 增强战斗 - 支持技能、道具、宠物参战
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> enhancedCombat(Integer playerId, Monster monster, Integer skillId, Integer itemId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        // 获取玩家属性（含装备加成）
        int playerHealth = player.getHealth() + player.getEquipmentHealthBonus();
        int playerAttack = player.getAttack() + player.getEquipmentAttackBonus();
        int playerDefense = player.getDefense() + player.getEquipmentDefenseBonus();
        int playerSpeed = player.getSpeed() + player.getEquipmentSpeedBonus();
        int playerMana = player.getMana() == null ? 0 : player.getMana();

        // 获取宠物加成
        PlayerPet activePet = petService.getActivePet(playerId);
        PetCombatBonus petBonus = petService.calculatePetCombatBonus(activePet);
        boolean petEligible = activePet != null && petBonus != null && petBonus.isEligible();
        if (petEligible) {
            playerHealth += activePet.getHealth();
            playerAttack += activePet.getAttack();
            playerDefense += activePet.getDefense();
            playerSpeed += activePet.getSpeed();
        }

        int monsterHealth = monster.getHealth();
        int monsterAttack = monster.getAttack();
        int monsterDefense = monster.getDefense();
        int monsterSpeed = monster.getSpeed();

        // 战斗日志
        List<String> battleLog = new ArrayList<>();
        battleLog.add("战斗开始！" + player.getNickname() + " VS " + monster.getName());

        // 添加宠物参战日志
        if (petEligible) {
            battleLog.add(player.getNickname() + "的宠物" + activePet.getNickname() + "加入战斗");
        } else if (activePet != null) {
            battleLog.add(activePet.getNickname() + "因饱食度不足，无法参战");
        }

        int currentPlayerHealth = playerHealth;
        int currentMonsterHealth = monsterHealth;
        int currentPlayerMana = playerMana;
        int rounds = 0;
        int maxRounds = 50; // 防止无限循环

        // 战斗循环
        while (currentPlayerHealth > 0 && currentMonsterHealth > 0 && rounds < maxRounds) {
            rounds++;

            CombatService.ActionPlan actionPlan = combatService.createActionPlan(playerSpeed, monsterSpeed);

            if (actionPlan.playerFirst) {
                // 玩家回合
                for (int i = 0; i < actionPlan.playerActions && currentMonsterHealth > 0; i++) {
                    TurnOutcome playerAction = executePlayerTurn(player, playerAttack, playerSpeed,
                            skillId, itemId, currentPlayerMana, monster, currentMonsterHealth,
                            monsterDefense, battleLog);
                    currentPlayerMana = Math.max(0, currentPlayerMana - playerAction.manaCost);
                    currentMonsterHealth -= playerAction.damage;
                    currentPlayerHealth = Math.min(playerHealth, currentPlayerHealth + playerAction.heal);
                }

                currentMonsterHealth -= combatService.applyPetSkillDamage(rounds, petEligible ? petBonus : null, battleLog);

                if (currentMonsterHealth <= 0) break;

                // 怪物回合
                for (int i = 0; i < actionPlan.monsterActions && currentPlayerHealth > 0; i++) {
                    TurnOutcome monsterAction = executeMonsterTurn(monster, player, currentPlayerHealth, playerDefense, playerSpeed, battleLog);
                    currentPlayerHealth -= monsterAction.damage;
                }
            } else {
                // 怪物先手
                for (int i = 0; i < actionPlan.monsterActions && currentPlayerHealth > 0; i++) {
                    TurnOutcome monsterAction = executeMonsterTurn(monster, player, currentPlayerHealth, playerDefense, playerSpeed, battleLog);
                    currentPlayerHealth -= monsterAction.damage;
                }

                if (currentPlayerHealth <= 0) break;

                // 玩家回击
                for (int i = 0; i < actionPlan.playerActions && currentMonsterHealth > 0; i++) {
                    TurnOutcome playerAction = executePlayerTurn(player, playerAttack, playerSpeed,
                            skillId, itemId, currentPlayerMana, monster, currentMonsterHealth,
                            monsterDefense, battleLog);
                    currentPlayerMana = Math.max(0, currentPlayerMana - playerAction.manaCost);
                    currentMonsterHealth -= playerAction.damage;
                    currentPlayerHealth = Math.min(playerHealth, currentPlayerHealth + playerAction.heal);
                }

                currentMonsterHealth -= combatService.applyPetSkillDamage(rounds, petEligible ? petBonus : null, battleLog);
            }
        }

        if (petEligible) {
            petService.consumePetHungerAfterCombat(playerId);
        }

        // 判断战斗结果
        boolean playerWon = currentMonsterHealth <= 0;
        String result = playerWon ? "WIN" : "LOSE";

        int expGained = 0;
        int spiritStonesGained = 0;
        Integer droppedEquipmentId = null;

        // 累加 totalBattles（无论输赢）
        player.setTotalBattles(player.getTotalBattles() + 1);

        if (playerWon) {
            battleLog.add("战斗胜利！");

            // 计算奖励
            expGained = calculateExpReward(monster, player.getLevel());
            spiritStonesGained = calculateSpiritStonesReward(monster, player.getLevel());

            // 检查装备掉落
            if (rng().nextInt(100) < monster.getDropRate() && monster.getDropEquipmentId() != null) {
                droppedEquipmentId = monster.getDropEquipmentId();
                try {
                    equipmentService.acquireEquipment(droppedEquipmentId, playerId);
                    battleLog.add("获得装备掉落！");
                } catch (Exception e) {
                    log.warn("装备掉落失败: {}", e.getMessage());
                }
            }

            // 更新玩家数据
            player.setExp(player.getExp() + expGained);
            player.setSpiritStones(player.getSpiritStones() + spiritStonesGained);

            int oldLevel = player.getLevel();
            int levelUps = playerService.applyLevelUpsWithoutCommit(player, 100);
            if (levelUps > 0) {
                battleLog.add("升级！当前等级" + player.getLevel());
            }
            if (oldLevel < 1000 && player.getLevel() >= 1000) {
                battleLog.add("已达到最高等级1000");
            }

            battleLog.add("获得经验" + expGained + "，灵石" + spiritStonesGained);
        } else {
            battleLog.add("战斗失败...");
            long currentSpiritStones = player.getSpiritStones();
            long lostSpiritStones = Math.max(1L, currentSpiritStones / 100);
            if (currentSpiritStones >= lostSpiritStones) {
                player.setSpiritStones(currentSpiritStones - lostSpiritStones);
                battleLog.add("损失灵石" + lostSpiritStones);
            }
        }
        // 统一保存 - 使用独立事务方法
        player.setHealth(Math.max(0, Math.min(currentPlayerHealth, player.getMaxHealth())));
        player.setMana(Math.max(0, Math.min(currentPlayerMana, player.getMaxMana())));
        saveCombatResult(player, playerId, monster.getId(), result, rounds, expGained, 
                spiritStonesGained, droppedEquipmentId, battleLog);

        // 返回战斗结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", result);
        resultMap.put("rounds", rounds);
        resultMap.put("battleLog", battleLog);
        resultMap.put("expGained", expGained);
        resultMap.put("spiritStonesGained", spiritStonesGained);
        resultMap.put("droppedEquipment", droppedEquipmentId);
        resultMap.put("playerLevel", player.getLevel());
        resultMap.put("playerExp", player.getExp());
        resultMap.put("playerSpiritStones", player.getSpiritStones());
        resultMap.put("playerCurrentHealth", currentPlayerHealth);
        resultMap.put("playerMaxHealth", playerHealth);
        resultMap.put("monsterCurrentHealth", currentMonsterHealth);
        resultMap.put("monsterMaxHealth", monsterHealth);

        return resultMap;
    }

    private void saveCombatResult(PlayerProfile player, Integer playerId, Integer monsterId,
                                     String result, int rounds, int expGained, int spiritStonesGained,
                                     Integer droppedEquipmentId, List<String> battleLog) {
        playerService.savePlayerProfile(player);

        String battleDetailsJson = "";
        try {
            battleDetailsJson = objectMapper.writeValueAsString(battleLog);
        } catch (JsonProcessingException e) {
            log.error("战斗日志序列化失败", e);
        }

        CombatLog combatRecord = CombatLog.builder()
                .playerId(playerId)
                .monsterId(monsterId)
                .result(result)
                .rounds(rounds)
                .expGained(expGained)
                .spiritStonesGained(spiritStonesGained)
                .equipmentDropped(droppedEquipmentId)
                .battleDetails(battleDetailsJson)
                .createdAt(LocalDateTime.now())
                .build();

        combatLogMapper.insert(combatRecord);
    }

    /**
     * 执行玩家回合（技能/道具/普通攻击）
     */
    private TurnOutcome executePlayerTurn(PlayerProfile player, int playerAttack, int playerSpeed,
                                    Integer skillId, Integer playerItemId, int currentMana,
                                    Monster monster, int monsterHealth, int monsterDefense,
                                    List<String> battleLog) {
        // 检查是否使用技能
        if (skillId != null && skillId > 0) {
            PlayerSkill playerSkill = skillService.getPlayerSkillByPlayerAndSkill(player.getId(), skillId);
            if (playerSkill != null) {
                Skill skill = skillService.getSkillById(skillId);
                if (skill != null && currentMana >= skill.getManaCost()) {
                    // 计算技能伤害
                    double skillDamage = skill.getBaseDamage() + (skill.getDamagePerLevel() * playerSkill.getLevel());
                    int baseDamage = combatService.calculateDamage((int) skillDamage, monsterDefense,
                            player.getLevel(), monster.getLevel(), playerSpeed, monster.getSpeed(),
                            true, battleLog);
                    SkillComboResult comboResult = skillService.calculateCombatSkillDamageWithCombo(player.getId(), skill.getId(), baseDamage);
                    int damage = comboResult.getFinalDamage() > 0 ? comboResult.getFinalDamage() : baseDamage;
                    if (comboResult.isTriggered()) {
                        battleLog.add("⚡ 触发连招【" + comboResult.getComboName() + "】，额外伤害+" + comboResult.getBonusDamage());
                    }

                    // 检查暴击
                    boolean isCritical = checkCriticalHit(player);
                    if (isCritical) {
                        damage = (int) (damage * 1.8);
                        battleLog.add(player.getNickname() + "施放技能【" + skill.getName() + "】造成" + damage + "点暴击伤害");
                        return new TurnOutcome(damage, 0, skill.getManaCost());
                    } else {
                        battleLog.add(player.getNickname() + "施放技能【" + skill.getName() + "】造成" + damage + "点伤害");
                        return new TurnOutcome(damage, 0, skill.getManaCost());
                    }
                }
            }
        }

        // 检查是否使用道具
        if (playerItemId != null && playerItemId > 0) {
            PlayerItem playerItem = getPlayerItem(player.getId(), playerItemId);
            if (playerItem != null) {
                Item item = itemService.getItemById(playerItem.getItemId());
                if (item != null) {
                    // 消耗道具
                    consumeItem(player.getId(), playerItemId);

                    // 应用道具效果
                    if (item.getEffect() != null && item.getEffect().contains("恢复生命")) {
                        int healAmount = 50; // 简化恢复量
                        battleLog.add(player.getNickname() + "使用道具【" + item.getName() + "】恢复了" + healAmount + "点生命");
                        return new TurnOutcome(0, healAmount, 0);
                    }
                }
            }
        }

        // 普通攻击
        int damage = combatService.calculateDamage(playerAttack, monsterDefense, player.getLevel(),
                monster.getLevel(), playerSpeed, monster.getSpeed(), true, battleLog);

        // 检查暴击
        boolean isCritical = checkCriticalHit(player);
        if (isCritical) {
            damage = (int) (damage * 1.8);
            battleLog.add(player.getNickname() + "造成" + damage + "点暴击伤害");
            return new TurnOutcome(damage, 0, 0);
        } else {
            battleLog.add(player.getNickname() + "攻击造成" + damage + "点伤害");
            return new TurnOutcome(damage, 0, 0);
        }
    }

    /**
     * 执行怪物回合
     */
    private TurnOutcome executeMonsterTurn(Monster monster, PlayerProfile player, int playerHealth,
                                           int playerDefense, int playerSpeed, List<String> battleLog) {
        // 怪物普通攻击
        int damage = combatService.calculateDamage(monster.getAttack(), playerDefense, monster.getLevel(),
                player.getLevel(), monster.getSpeed(), playerSpeed, false, battleLog);
        battleLog.add(monster.getName() + "攻击造成" + damage + "点伤害");
        return new TurnOutcome(damage, 0, 0);
    }

    private boolean checkCriticalHit(PlayerProfile player) {
        return rng().nextInt(100) < 5;
    }

    /**
     * 获取玩家道具
     */
    private PlayerItem getPlayerItem(Integer playerId, Integer playerItemId) {
        // playerItemId here is the PlayerItem primary key
        PlayerItem item = playerService.getPlayerItemById(playerItemId);
        return (item != null && item.getPlayerId().equals(playerId)) ? item : null;
    }

    /**
     * 消耗道具
     */
    private void consumeItem(Integer playerId, Integer playerItemId) {
        PlayerItem playerItem = getPlayerItem(playerId, playerItemId);
        if (playerItem != null) {
            if (playerItem.getQuantity() > 1) {
                playerItem.setQuantity(playerItem.getQuantity() - 1);
                playerService.updatePlayerItem(playerItem);
            } else {
                playerService.deletePlayerItem(playerItem.getId());
            }
        }
    }


    /**
     * 计算经验奖励（含等级差修正）
     */
    private int calculateExpReward(Monster monster, int playerLevel) {
        int baseExp = monster.getExpReward();
        int levelDiff = playerLevel - monster.getLevel();

        // 等级差超过怪物等级时降低奖励，超过5级只给10%
        if (levelDiff > 5) {
            baseExp = (int) (baseExp * 0.1);
        } else if (levelDiff > 0) {
            baseExp = (int) (baseExp * (1.0 - levelDiff * 0.1));
        } else if (levelDiff < -5) {
            baseExp = (int) (baseExp * 2.0); // 越级击杀加倍
        }

        return Math.max(1, baseExp);
    }

    /**
     * 计算灵石奖励
     */
    private int calculateSpiritStonesReward(Monster monster, int playerLevel) {
        int baseReward = monster.getSpiritStonesReward();
        int levelDiff = playerLevel - monster.getLevel();

        // 等级差影响掉落
        if (levelDiff > 10) {
            baseReward = (int) (baseReward * 0.5);
        }

        return Math.max(1, baseReward);
    }
}
