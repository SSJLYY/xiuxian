package com.xiuxian.game.modules.combat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
// combat module entities (same module -- OK)
import com.xiuxian.game.modules.combat.entity.CombatLog;
import com.xiuxian.game.modules.combat.entity.Monster;
// combat module mappers (same module -- OK)
import com.xiuxian.game.modules.combat.mapper.CombatLogMapper;
import com.xiuxian.game.modules.combat.mapper.MonsterMapper;
// cross-module entities accessed via Service interfaces
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.skill.entity.PlayerSkill;
import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.shop.entity.Item;
// cross-module services (module boundary)
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.skill.service.SkillService;
import com.xiuxian.game.modules.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedCombatService {

    // module boundary: access player/skill/item data via Service
    private final com.xiuxian.game.modules.player.service.PlayerService playerService;
    private final com.xiuxian.game.modules.skill.service.SkillService skillService;
    private final com.xiuxian.game.modules.shop.service.ItemService itemService;
    private final MonsterMapper monsterMapper;
    private final CombatLogMapper combatLogMapper;
    private final EquipmentService equipmentService;
    // playerEquipmentMapper removed (cross-module, unused)
    // playerSkillMapper replaced by skillService
    // skillMapper replaced by skillService
    private final PetService petService;
    // playerPetMapper removed (cross-module, unused)
    // itemMapper replaced by itemService
    // playerItemMapper replaced by playerService
    private final ObjectMapper objectMapper;

    // 【修复】使�?ThreadLocalRandom 替代共享 Random 实例�?
    // EnhancedCombatService �?Spring 单例，所有并发请求共享同一实例�?
    // ThreadLocalRandom 每个线程独立，无锁竞争，性能更优�?
    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

    /**
     * 增强战斗主逻辑 - 支持技能、宠物、道�?
     */
    @Transactional
    public Map<String, Object> enhancedCombat(Integer playerId, Monster monster, Integer skillId, Integer itemId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存�?);
        }

        // 获取玩家总属性（基础+装备�?
        int playerHealth = player.getHealth() + player.getEquipmentHealthBonus();
        int playerAttack = player.getAttack() + player.getEquipmentAttackBonus();
        int playerDefense = player.getDefense() + player.getEquipmentDefenseBonus();
        int playerSpeed = player.getSpeed() + player.getEquipmentSpeedBonus();
        int playerMana = player.getMana();

        // 获取宠物属性加�?
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
        
        // 添加宠物出场信息
        if (activePet != null) {
            battleLog.add(player.getNickname() + "的宠�? + activePet.getNickname() + "加入战斗�?);
        }

        int currentPlayerHealth = playerHealth;
        int currentMonsterHealth = monsterHealth;
        int currentPlayerMana = playerMana;
        int rounds = 0;
        int maxRounds = 50; // 防止无限循环

        // 战斗循环
        while (currentPlayerHealth > 0 && currentMonsterHealth > 0 && rounds < maxRounds) {
            rounds++;
            
            // 根据速度决定先手
            boolean playerFirst = playerSpeed >= monsterSpeed;
            
            if (playerFirst) {
                // 玩家回合
                String playerAction = executePlayerTurn(player, skillId, itemId, currentPlayerMana, 
                        monster, currentMonsterHealth, monsterDefense, battleLog);
                
                // 更新玩家法力�?
                if (playerAction.startsWith("使用技�?)) {
                    Skill skill = skillService.getSkillById(skillId);
                    if (skill != null) {
                        currentPlayerMana = Math.max(0, currentPlayerMana - skill.getManaCost());
                    }
                }
                
                // 解析造成的伤�?
                int playerDamage = parseDamageFromLog(playerAction);
                currentMonsterHealth -= playerDamage;
                
                if (currentMonsterHealth <= 0) break;
                
                // 怪物回合
                String monsterAction = executeMonsterTurn(monster, currentPlayerHealth, playerDefense, battleLog);
                int monsterDamage = parseDamageFromLog(monsterAction);
                currentPlayerHealth -= monsterDamage;
            } else {
                // 怪物先攻�?
                String monsterAction = executeMonsterTurn(monster, currentPlayerHealth, playerDefense, battleLog);
                int monsterDamage = parseDamageFromLog(monsterAction);
                currentPlayerHealth -= monsterDamage;
                
                if (currentPlayerHealth <= 0) break;
                
                // 玩家回合
                String playerAction = executePlayerTurn(player, skillId, itemId, currentPlayerMana, 
                        monster, currentMonsterHealth, monsterDefense, battleLog);
                
                // 更新玩家法力�?
                if (playerAction.startsWith("使用技�?)) {
                    Skill skill = skillService.getSkillById(skillId);
                    if (skill != null) {
                        currentPlayerMana = Math.max(0, currentPlayerMana - skill.getManaCost());
                    }
                }
                
                // 解析造成的伤�?
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
            battleLog.add("战斗胜利�?);
            
            // 计算奖励
            expGained = calculateExpReward(monster, player.getLevel());
            spiritStonesGained = calculateSpiritStonesReward(monster, player.getLevel());
            
            // 检查装备掉�?
            if (random.nextInt(100) < monster.getDropRate() && monster.getDropEquipmentId() != null) {
                droppedEquipmentId = monster.getDropEquipmentId();
                try {
                    // 使用非事务方式获取装备，避免影响主事�?
                    equipmentService.acquireEquipment(droppedEquipmentId, playerId);
                    battleLog.add("获得装备掉落�?);
                } catch (Exception e) {
                    log.warn("装备掉落失败: {}", e.getMessage());
                    // 不中断主流程，继续处理其他奖�?
                }
            }
            
            // 更新玩家数据
            player.setExp(player.getExp() + expGained);
            player.setSpiritStones(player.getSpiritStones() + spiritStonesGained);
            
            // 检查升�?
            while (player.getExp() >= player.getExpToNext()) {
                player.setExp(player.getExp() - player.getExpToNext());
                player.setLevel(player.getLevel() + 1);
                player.setExpToNext((long)(player.getExpToNext() * 1.5));
                
                // 升级属性增�?
                player.setHealth(player.getHealth() + 10);
                player.setMana(player.getMana() + 5);
                player.setAttack(player.getAttack() + 2);
                player.setDefense(player.getDefense() + 1);
                player.setAttributePoints(player.getAttributePoints() + 5);
                
                battleLog.add("恭喜升级！当前等级：" + player.getLevel());
            }
            
            playerService.savePlayerProfile(player);
            
            battleLog.add("获得经验�? + expGained + "，灵石：" + spiritStonesGained);
        } else {
            battleLog.add("战斗失败...");
            // 失败惩罚（可选）
            int lostSpiritStones = spiritStonesGained / 10;
            if (player.getSpiritStones() >= lostSpiritStones) {
                player.setSpiritStones(player.getSpiritStones() - lostSpiritStones);
                playerService.savePlayerProfile(player);
                battleLog.add("损失灵石�? + lostSpiritStones);
            }
        }

        // 保存战斗日志
        String battleDetailsJson = "";
        try {
            battleDetailsJson = objectMapper.writeValueAsString(battleLog);
        } catch (JsonProcessingException e) {
            log.error("战斗日志序列化失�?, e);
        }
        
        CombatLog combatLog = CombatLog.builder()
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
        
        combatLogMapper.insert(combatLog);

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
     * 执行玩家回合
     */
    private String executePlayerTurn(PlayerProfile player, Integer skillId, Integer itemId, 
                                   int currentMana, Monster monster, int monsterHealth, 
                                   int monsterDefense, List<String> battleLog) {
        // 检查是否使用技�?
        if (skillId != null && skillId > 0) {
            PlayerSkill playerSkill = skillService.getPlayerSkillByPlayerAndSkill(player.getId(), skillId);
            if (playerSkill != null) {
                Skill skill = skillService.getSkillById(skillId);
                if (skill != null && currentMana >= skill.getManaCost()) {
                    // 计算技能伤�?
                    double skillDamage = skill.getBaseDamage() + (skill.getDamagePerLevel() * playerSkill.getLevel());
                    int damage = calculateDamage((int)skillDamage, monsterDefense, player.getLevel(), monster.getLevel());
                    
                    // 检查暴�?
                    boolean isCritical = checkCriticalHit(player);
                    if (isCritical) {
                        damage = (int)(damage * 1.5);
                        battleLog.add(player.getNickname() + "使用技能�? + skill.getName() + "】造成�? + damage + "点暴击伤害！");
                        return "使用技能暴击造成" + damage + "点伤�?;
                    } else {
                        battleLog.add(player.getNickname() + "使用技能�? + skill.getName() + "】造成�? + damage + "点伤�?);
                        return "使用技能造成" + damage + "点伤�?;
                    }
                }
            }
        }
        
        // 检查是否使用道�?
        if (itemId != null && itemId > 0) {
            PlayerItem playerItem = getPlayerItem(player.getId(), itemId);
            if (playerItem != null) {
                Item item = itemService.getItemById(playerItem.getItemId());
                if (item != null) {
                    // 消耗道�?
                    consumeItem(player.getId(), itemId);
                    
                    // 应用道具效果
                    if (item.getEffect() != null && item.getEffect().contains("恢复生命")) {
                        int healAmount = 50; // 简化处�?
                        battleLog.add(player.getNickname() + "使用道具�? + item.getName() + "】恢复了" + healAmount + "点生命�?);
                        return "使用道具恢复" + healAmount + "点生�?;
                    }
                }
            }
        }
        
        // 普通攻�?
        int damage = calculateDamage(player.getAttack(), monsterDefense, player.getLevel(), monster.getLevel());
        
        // 检查暴�?
        boolean isCritical = checkCriticalHit(player);
        if (isCritical) {
            damage = (int)(damage * 1.5);
            battleLog.add(player.getNickname() + "造成�? + damage + "点暴击伤害！");
            return "普通攻击暴击造成" + damage + "点伤�?;
        } else {
            battleLog.add(player.getNickname() + "造成�? + damage + "点伤�?);
            return "普通攻击造成" + damage + "点伤�?;
        }
    }

    /**
     * 执行怪物回合
     */
    private String executeMonsterTurn(Monster monster, int playerHealth, int playerDefense, List<String> battleLog) {
        // 怪物普通攻�?
        int damage = calculateDamage(monster.getAttack(), playerDefense, monster.getLevel(), 1); // 简化处理玩家等级为1
        battleLog.add(monster.getName() + "造成�? + damage + "点伤�?);
        return "怪物攻击造成" + damage + "点伤�?;
    }

    /**
     * 检查是否暴�?
     */
    private boolean checkCriticalHit(PlayerProfile player) {
        // 10%基础暴击率，暂时不考虑装备加成
        return rng().nextInt(100) < 10;
    }

    /**
     * 从战斗日志解析伤害数�?
     */
    private int parseDamageFromLog(String logEntry) {
        // 简化处理，实际应该用正则表达式提取数字
        try {
            String[] parts = logEntry.split("造成");
            if (parts.length > 1) {
                String damagePart = parts[1].split("�?)[0];
                return Integer.parseInt(damagePart);
            }
        } catch (Exception e) {
            // 解析失败，返回默认�?
        }
        return 0;
    }

    /**
     * 获取玩家道具
     */
    private PlayerItem getPlayerItem(Integer playerId, Integer itemId) {
        // itemId here is PlayerItem's primary key (not item template id)
        PlayerItem item = playerService.getPlayerItemById(itemId);
        return (item != null && item.getPlayerId().equals(playerId)) ? item : null;
    }

    /**
     * 消耗道�?
     */
    private void consumeItem(Integer playerId, Integer itemId) {
        PlayerItem playerItem = getPlayerItem(playerId, itemId);
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
     * 计算伤害 - 优化后的平衡公式
     */
    private int calculateDamage(int attack, int defense, int attackerLevel, int defenderLevel) {
        // 等级压制（降低影响）
        double levelFactor = 1.0 + (attackerLevel - defenderLevel) * 0.03;
        levelFactor = Math.max(0.7, Math.min(1.3, levelFactor));
        
        // 防御减伤（优化公式，降低防御影响�?
        // 新公式：减伤 = 防御 / (防御 + 200)，最大减�?0%
        double damageReduction = Math.min(0.5, defense / (defense + 200.0));
        
        // 基础伤害
        int baseDamage = (int)(attack * levelFactor);
        
        // 应用防御
        int finalDamage = (int)(baseDamage * (1 - damageReduction));
        
        // 随机波动 ±15%（增加随机性）
        int variance = (int)(finalDamage * 0.15);
        if (variance > 0) {
            finalDamage += rng().nextInt(variance * 2 + 1) - variance;
        }
        
        // 保证最小伤害（至少造成攻击力的20%�?
        int minDamage = Math.max(1, attack / 5);
        return Math.max(minDamage, finalDamage);
    }

    /**
     * 计算经验奖励
     */
    private int calculateExpReward(Monster monster, int playerLevel) {
        int baseExp = monster.getExpReward();
        
        // 等级差经验衰�?
        int levelDiff = playerLevel - monster.getLevel();
        if (levelDiff > 5) {
            baseExp = (int)(baseExp * 0.1); // 等级高太多，经验大幅降低
        } else if (levelDiff > 0) {
            baseExp = (int)(baseExp * (1 - levelDiff * 0.1));
        } else if (levelDiff < -5) {
            baseExp = (int)(baseExp * 2.0); // 越级挑战，经验翻�?
        }
        
        return Math.max(1, baseExp);
    }

    /**
     * 计算灵石奖励
     */
    private int calculateSpiritStonesReward(Monster monster, int playerLevel) {
        int baseReward = monster.getSpiritStonesReward();
        
        // 等级差影响较�?
        int levelDiff = playerLevel - monster.getLevel();
        if (levelDiff > 10) {
            baseReward = (int)(baseReward * 0.5);
        }
        
        return Math.max(1, baseReward);
    }
}

