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
import com.xiuxian.game.modules.shop.entity.Item;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.modules.pet.service.PetService;
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.service.SkillService;
import com.xiuxian.game.modules.shop.entity.Item;
import com.xiuxian.game.modules.shop.service.ItemService;
import com.xiuxian.game.modules.player.service.PlayerService;
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

    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

    /**
     * 增强战斗 - 支持技能、道具、宠物参战
     */
    @Transactional
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
        int playerMana = player.getMana();

        // 获取宠物加成
        PlayerPet activePet = petService.getActivePet(playerId);
        if (activePet != null) {
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
        if (activePet != null) {
            battleLog.add(player.getNickname() + "的宠物" + activePet.getNickname() + "加入战斗");
        }

        int currentPlayerHealth = playerHealth;
        int currentMonsterHealth = monsterHealth;
        int currentPlayerMana = playerMana;
        int rounds = 0;
        int maxRounds = 50; // 防止无限循环

        // 战斗循环
        while (currentPlayerHealth > 0 && currentMonsterHealth > 0 && rounds < maxRounds) {
            rounds++;

            // 根据速度决定先后手
            boolean playerFirst = playerSpeed >= monsterSpeed;

            if (playerFirst) {
                // 玩家回合
                String playerAction = executePlayerTurn(player, skillId, itemId, currentPlayerMana,
                        monster, currentMonsterHealth, monsterDefense, battleLog);

                // 更新玩家法力消耗
                if (playerAction.startsWith("使用技能")) {
                    Skill skill = skillService.getSkillById(skillId);
                    if (skill != null) {
                        currentPlayerMana = Math.max(0, currentPlayerMana - skill.getManaCost());
                    }
                }

                // 解析造成的伤害
                int playerDamage = parseDamageFromLog(playerAction);
                currentMonsterHealth -= playerDamage;

                if (currentMonsterHealth <= 0) break;

                // 怪物回合
                String monsterAction = executeMonsterTurn(monster, currentPlayerHealth, playerDefense, battleLog);
                int monsterDamage = parseDamageFromLog(monsterAction);
                currentPlayerHealth -= monsterDamage;
            } else {
                // 怪物先手
                String monsterAction = executeMonsterTurn(monster, currentPlayerHealth, playerDefense, battleLog);
                int monsterDamage = parseDamageFromLog(monsterAction);
                currentPlayerHealth -= monsterDamage;

                if (currentPlayerHealth <= 0) break;

                // 玩家回击
                String playerAction = executePlayerTurn(player, skillId, itemId, currentPlayerMana,
                        monster, currentMonsterHealth, monsterDefense, battleLog);

                // 更新法力消耗
                if (playerAction.startsWith("使用技能")) {
                    Skill skill = skillService.getSkillById(skillId);
                    if (skill != null) {
                        currentPlayerMana = Math.max(0, currentPlayerMana - skill.getManaCost());
                    }
                }

                // 解析造成的伤害
                int playerDamage = parseDamageFromLog(playerAction);
                currentMonsterHealth -= playerDamage;
            }
        }

        // 判断战斗结果
        boolean playerWon = currentMonsterHealth <= 0;
        String result = playerWon ? "WIN" : "LOSE";

        int expGained = 0;
        int spiritStonesGained = 0;
        Integer droppedEquipmentId = null;

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

            // 检查升级
            while (player.getExp() >= player.getExpToNext()) {
                player.setExp(player.getExp() - player.getExpToNext());
                player.setLevel(player.getLevel() + 1);
                player.setExpToNext((long)(player.getExpToNext() * 1.5));
                player.setHealth(player.getHealth() + 10);
                player.setMana(player.getMana() + 5);
                player.setAttack(player.getAttack() + 2);
                player.setDefense(player.getDefense() + 1);
                player.setAttributePoints(player.getAttributePoints() + 5);
                battleLog.add("升级！当前等级" + player.getLevel());
            }

            playerService.savePlayerProfile(player);
            battleLog.add("获得经验" + expGained + "，灵石" + spiritStonesGained);
        } else {
            battleLog.add("战斗失败...");
            // 失败惩罚（可选）
            int lostSpiritStones = spiritStonesGained / 10;
            if (player.getSpiritStones() >= lostSpiritStones) {
                player.setSpiritStones(player.getSpiritStones() - lostSpiritStones);
                playerService.savePlayerProfile(player);
                battleLog.add("损失灵石" + lostSpiritStones);
            }
        }

        // 保存战斗记录
        String battleDetailsJson = "";
        try {
            battleDetailsJson = objectMapper.writeValueAsString(battleLog);
        } catch (JsonProcessingException e) {
            log.error("战斗日志序列化失败", e);
        }

        CombatLog combatRecord = CombatLog.builder()
                .playerId(playerId)
                .monsterId(monster.getId())
                .result(result)
                .rounds(rounds)
                .expGained(expGained)
                .spiritStonesGained(spiritStonesGained)
                .equipmentDropped(droppedEquipmentId)
                .battleDetails(battleDetailsJson)
                .createdAt(LocalDateTime.now())
                .build();

        combatLogMapper.insert(combatRecord);

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

    /**
     * 执行玩家回合（技能/道具/普通攻击）
     */
    private String executePlayerTurn(PlayerProfile player, Integer skillId, Integer itemId,
                                   int currentMana, Monster monster, int monsterHealth,
                                   int monsterDefense, List<String> battleLog) {
        // 检查是否使用技能
        if (skillId != null && skillId > 0) {
            PlayerSkill playerSkill = skillService.getPlayerSkillByPlayerAndSkill(player.getId(), skillId);
            if (playerSkill != null) {
                Skill skill = skillService.getSkillById(skillId);
                if (skill != null && currentMana >= skill.getManaCost()) {
                    // 计算技能伤害
                    double skillDamage = skill.getBaseDamage() + (skill.getDamagePerLevel() * playerSkill.getLevel());
                    int damage = calculateDamage((int) skillDamage, monsterDefense, player.getLevel(), monster.getLevel());

                    // 检查暴击
                    boolean isCritical = checkCriticalHit(player);
                    if (isCritical) {
                        damage = (int) (damage * 1.5);
                        battleLog.add(player.getNickname() + "施放技能【" + skill.getName() + "】造成" + damage + "点暴击伤害");
                        return "暴击伤害" + damage;
                    } else {
                        battleLog.add(player.getNickname() + "施放技能【" + skill.getName() + "】造成" + damage + "点伤害");
                        return "技能伤害" + damage;
                    }
                }
            }
        }

        // 检查是否使用道具
        if (itemId != null && itemId > 0) {
            PlayerItem playerItem = getPlayerItem(player.getId(), itemId);
            if (playerItem != null) {
                Item item = itemService.getItemById(playerItem.getItemId());
                if (item != null) {
                    // 消耗道具
                    consumeItem(player.getId(), itemId);

                    // 应用道具效果
                    if (item.getEffect() != null && item.getEffect().contains("恢复生命")) {
                        int healAmount = 50; // 简化恢复量
                        battleLog.add(player.getNickname() + "使用道具【" + item.getName() + "】恢复了" + healAmount + "点生命");
                        return "恢复生命" + healAmount;
                    }
                }
            }
        }

        // 普通攻击
        int damage = calculateDamage(player.getAttack(), monsterDefense, player.getLevel(), monster.getLevel());

        // 检查暴击
        boolean isCritical = checkCriticalHit(player);
        if (isCritical) {
            damage = (int) (damage * 1.5);
            battleLog.add(player.getNickname() + "造成" + damage + "点暴击伤害");
            return "暴击伤害" + damage;
        } else {
            battleLog.add(player.getNickname() + "攻击造成" + damage + "点伤害");
            return "造成" + damage + "伤害";
        }
    }

    /**
     * 执行怪物回合
     */
    private String executeMonsterTurn(Monster monster, int playerHealth, int playerDefense, List<String> battleLog) {
        // 怪物普通攻击
        int damage = calculateDamage(monster.getAttack(), playerDefense, monster.getLevel(), 1); // 玩家等级传1简化
        battleLog.add(monster.getName() + "攻击造成" + damage + "点伤害");
        return "造成" + damage + "伤害";
    }

    /**
     * 检查是否暴击
     */
    private boolean checkCriticalHit(PlayerProfile player) {
        // 10%基础暴击率，暂不考虑装备加成
        return rng().nextInt(100) < 10;
    }

    /**
     * 从战斗日志中解析伤害数值
     */
    private int parseDamageFromLog(String logEntry) {
        try {
            String[] parts = logEntry.split("造成");
            if (parts.length > 1) {
                String damagePart = parts[1].split("点")[0];
                return Integer.parseInt(damagePart.trim());
            }
        } catch (Exception e) {
            // 解析失败，返回0
        }
        return 0;
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
     * 计算减伤后的最终伤害
     */
    private int calculateDamage(int attack, int defense, int attackerLevel, int defenderLevel) {
        // 等级差影响（降级压制，高等级加成）
        double levelFactor = 1.0 + (attackerLevel - defenderLevel) * 0.03;
        levelFactor = Math.max(0.7, Math.min(1.3, levelFactor));

        // 防御减伤（递减公式，最低减伤10%）
        double damageReduction = Math.min(0.5, defense / (defense + 200.0));

        // 基础伤害
        int baseDamage = (int) (attack * levelFactor);
        // 应用防御
        int finalDamage = (int) (baseDamage * (1.0 - damageReduction));

        // 随机波动±15%
        int variance = (int) (finalDamage * 0.15);
        if (variance > 0) {
            finalDamage += rng().nextInt(variance * 2 + 1) - variance;
        }

        // 保证最低伤害（至少造成攻击力的20%）
        int minDamage = Math.max(1, attack / 5);
        return Math.max(minDamage, finalDamage);
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
