package com.xiuxian.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiuxian.game.entity.*;
import com.xiuxian.game.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    private final PlayerProfileMapper playerProfileMapper;
    private final MonsterMapper monsterMapper;
    private final CombatLogMapper combatLogMapper;
    private final EquipmentService equipmentService;
    private final PlayerEquipmentMapper playerEquipmentMapper;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    /**
     * 生成怪物
     */
    public Monster generateMonster(Integer playerLevel) {
        return generateMonsterByMap(playerLevel, 1); // 默认地图1
    }
    
    /**
     * 根据地图生成怪物
     * @param playerLevel 玩家等级
     * @param mapId 地图ID (1=新手村, 2=野外)
     */
    public Monster generateMonster(Integer playerLevel, Integer mapId) {
        return generateMonsterByMap(playerLevel, mapId != null ? mapId : 1);
    }
    
    private Monster generateMonsterByMap(Integer playerLevel, int mapId) {
        // 根据地图ID调整怪物等级范围
        int minLevel, maxLevel;
        String monsterType;
        
        switch (mapId) {
            case 1: // 新手村 - 简单地图
                minLevel = Math.max(1, playerLevel - 3);
                maxLevel = Math.max(1, playerLevel - 1);
                break;
            case 2: // 野外 - 中等难度地图
                minLevel = Math.max(1, playerLevel - 1);
                maxLevel = playerLevel + 1;
                break;
            default: // 默认情况
                minLevel = Math.max(1, playerLevel - 2);
                maxLevel = playerLevel + 2;
                break;
        }
        
        // 根据地图调整怪物类型概率
        int typeRoll = random.nextInt(100);
        if (mapId == 1) {
            // 新手村：90%普通，8%精英，2%BOSS
            if (typeRoll < 90) {
                monsterType = "普通";
            } else if (typeRoll < 98) {
                monsterType = "精英";
            } else {
                monsterType = "BOSS";
            }
        } else if (mapId == 2) {
            // 野外：70%普通，25%精英，5%BOSS
            if (typeRoll < 70) {
                monsterType = "普通";
            } else if (typeRoll < 95) {
                monsterType = "精英";
            } else {
                monsterType = "BOSS";
            }
        } else {
            // 默认：70%普通，25%精英，5%BOSS
            if (typeRoll < 70) {
                monsterType = "普通";
            } else if (typeRoll < 95) {
                monsterType = "精英";
            } else {
                monsterType = "BOSS";
            }
        }
        
        Monster monster = monsterMapper.selectRandomByLevelAndType(playerLevel, monsterType);
        
        // 如果数据库没有对应怪物，尝试在等级范围内随机选取，最后按最大等级兜底
        if (monster == null) {
            List<Monster> candidates = monsterMapper.selectByLevelRange(minLevel, maxLevel);
            if (candidates != null && !candidates.isEmpty()) {
                monster = candidates.get(random.nextInt(candidates.size()));
            } else {
                monster = monsterMapper.selectRandomByMaxLevel(maxLevel);
            }
        }
        
        return monster;
    }

    /**
     * 生成临时怪物（当数据库没有对应怪物时）
     */
    private Monster generateTemporaryMonster(Integer level, String type) {
        String[] normalNames = {"野狼", "山贼", "妖怪", "邪修", "恶灵"};
        String[] eliteNames = {"狂暴野狼", "山贼头目", "千年妖怪", "邪道长老", "厉鬼"};
        String[] bossNames = {"狼王", "寨主", "妖王", "邪道护法", "鬼王"};
        
        String[] names = type.equals("BOSS") ? bossNames : 
                        type.equals("精英") ? eliteNames : normalNames;
        String name = names[random.nextInt(names.length)];
        
        double typeMultiplier = type.equals("BOSS") ? 3.0 : 
                               type.equals("精英") ? 1.5 : 1.0;
        
        return Monster.builder()
                .name(name)
                .description("等级" + level + "的" + type + "怪物")
                .level(level)
                .type(type)
                .health((int)(100 + level * 20 * typeMultiplier))
                .attack((int)(10 + level * 3 * typeMultiplier))
                .defense((int)(5 + level * 2 * typeMultiplier))
                .speed(10 + level)
                .expReward((int)(50 + level * 10 * typeMultiplier))
                .spiritStonesReward((int)(10 + level * 2 * typeMultiplier))
                .dropRate(type.equals("BOSS") ? 50 : type.equals("精英") ? 20 : 10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 战斗主逻辑
     */
    @Transactional
    public Map<String, Object> startCombat(Integer playerId, Monster monster) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        // 获取玩家总属性（基础+装备）
        int playerHealth = player.getHealth() + player.getEquipmentHealthBonus();
        int playerAttack = player.getAttack() + player.getEquipmentAttackBonus();
        int playerDefense = player.getDefense() + player.getEquipmentDefenseBonus();
        int playerSpeed = player.getSpeed() + player.getEquipmentSpeedBonus();

        int monsterHealth = monster.getHealth();
        int monsterAttack = monster.getAttack();
        int monsterDefense = monster.getDefense();
        int monsterSpeed = monster.getSpeed();

        // 战斗日志
        List<String> battleLog = new ArrayList<>();
        battleLog.add("战斗开始！" + player.getNickname() + " VS " + monster.getName());
        
        int currentPlayerHealth = playerHealth;
        int currentMonsterHealth = monsterHealth;
        int rounds = 0;
        int maxRounds = 50; // 防止无限循环

        // 战斗循环
        while (currentPlayerHealth > 0 && currentMonsterHealth > 0 && rounds < maxRounds) {
            rounds++;
            
            // 根据速度决定先手
            boolean playerFirst = playerSpeed >= monsterSpeed;
            
            if (playerFirst) {
                // 玩家攻击
                int damage = calculateDamage(playerAttack, monsterDefense, player.getLevel(), monster.getLevel());
                currentMonsterHealth -= damage;
                battleLog.add("第" + rounds + "回合: " + player.getNickname() + "造成了" + damage + "点伤害");
                
                if (currentMonsterHealth <= 0) break;
                
                // 怪物反击
                int monsterDamage = calculateDamage(monsterAttack, playerDefense, monster.getLevel(), player.getLevel());
                currentPlayerHealth -= monsterDamage;
                battleLog.add("第" + rounds + "回合: " + monster.getName() + "造成了" + monsterDamage + "点伤害");
            } else {
                // 怪物先攻击
                int monsterDamage = calculateDamage(monsterAttack, playerDefense, monster.getLevel(), player.getLevel());
                currentPlayerHealth -= monsterDamage;
                battleLog.add("第" + rounds + "回合: " + monster.getName() + "造成了" + monsterDamage + "点伤害");
                
                if (currentPlayerHealth <= 0) break;
                
                // 玩家反击
                int damage = calculateDamage(playerAttack, monsterDefense, player.getLevel(), monster.getLevel());
                currentMonsterHealth -= damage;
                battleLog.add("第" + rounds + "回合: " + player.getNickname() + "造成了" + damage + "点伤害");
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
            if (random.nextInt(100) < monster.getDropRate() && monster.getDropEquipmentId() != null) {
                droppedEquipmentId = monster.getDropEquipmentId();
                try {
                    // 使用非事务方式获取装备，避免影响主事务
                    equipmentService.acquireEquipment(droppedEquipmentId, playerId);
                    battleLog.add("获得装备掉落！");
                } catch (Exception e) {
                    log.warn("装备掉落失败: {}", e.getMessage());
                    // 不中断主流程，继续处理其他奖励
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
                
                // 升级属性增长
                player.setHealth(player.getHealth() + 10);
                player.setMana(player.getMana() + 5);
                player.setAttack(player.getAttack() + 2);
                player.setDefense(player.getDefense() + 1);
                player.setAttributePoints(player.getAttributePoints() + 5);
                
                battleLog.add("恭喜升级！当前等级：" + player.getLevel());
            }
            
            playerProfileMapper.updateById(player);
            
            battleLog.add("获得经验：" + expGained + "，灵石：" + spiritStonesGained);
        } else {
            battleLog.add("战斗失败...");
            // 失败惩罚（可选）
            int lostSpiritStones = spiritStonesGained / 10;
            if (player.getSpiritStones() >= lostSpiritStones) {
                player.setSpiritStones(player.getSpiritStones() - lostSpiritStones);
                playerProfileMapper.updateById(player);
                battleLog.add("损失灵石：" + lostSpiritStones);
            }
        }

        // 保存战斗日志
        String battleDetailsJson = "";
        try {
            battleDetailsJson = objectMapper.writeValueAsString(battleLog);
        } catch (JsonProcessingException e) {
            log.error("战斗日志序列化失败", e);
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
        
        return resultMap;
    }

    /**
     * 计算伤害
     */
    private int calculateDamage(int attack, int defense, int attackerLevel, int defenderLevel) {
        // 等级压制
        double levelFactor = 1.0 + (attackerLevel - defenderLevel) * 0.05;
        levelFactor = Math.max(0.5, Math.min(1.5, levelFactor));
        
        // 防御减伤
        double damageReduction = defense / (defense + 100.0);
        
        // 基础伤害
        int baseDamage = (int)(attack * levelFactor);
        
        // 应用防御
        int finalDamage = (int)(baseDamage * (1 - damageReduction));
        
        // 随机波动 ±10%
        int variance = (int)(finalDamage * 0.1);
        finalDamage += random.nextInt(variance * 2 + 1) - variance;
        
        // 保证最小伤害
        return Math.max(1, finalDamage);
    }

    /**
     * 计算经验奖励
     */
    private int calculateExpReward(Monster monster, int playerLevel) {
        int baseExp = monster.getExpReward();
        
        // 等级差经验衰减
        int levelDiff = playerLevel - monster.getLevel();
        if (levelDiff > 5) {
            baseExp = (int)(baseExp * 0.1); // 等级高太多，经验大幅降低
        } else if (levelDiff > 0) {
            baseExp = (int)(baseExp * (1 - levelDiff * 0.1));
        } else if (levelDiff < -5) {
            baseExp = (int)(baseExp * 2.0); // 越级挑战，经验翻倍
        }
        
        return Math.max(1, baseExp);
    }

    /**
     * 计算灵石奖励
     */
    private int calculateSpiritStonesReward(Monster monster, int playerLevel) {
        int baseReward = monster.getSpiritStonesReward();
        
        // 等级差影响较小
        int levelDiff = playerLevel - monster.getLevel();
        if (levelDiff > 10) {
            baseReward = (int)(baseReward * 0.5);
        }
        
        return Math.max(1, baseReward);
    }

    public Monster getMonsterById(Integer monsterId) {
        if (monsterId == null) return null;
        try {
            return monsterMapper.selectById(monsterId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取战斗历史
     */
    public List<CombatLog> getCombatHistory(Integer playerId, Integer limit) {
        return combatLogMapper.selectRecentByPlayerId(playerId, limit != null ? limit : 10);
    }
}
