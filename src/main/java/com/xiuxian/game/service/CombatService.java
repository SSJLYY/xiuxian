package com.xiuxian.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiuxian.game.dto.response.CombatResult;
import com.xiuxian.game.dto.response.PetCombatBonus;
import com.xiuxian.game.entity.*;
import com.xiuxian.game.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
    private final PetService petService;  // GDD: 宠物参战机制

    // 【修复 P0-3】使用 ThreadLocalRandom 替代共享 Random 实例。
    // CombatService 是 Spring 单例，所有并发请求共享同一实例。
    // ThreadLocalRandom 每个线程独立，无锁竞争，性能更优。
    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

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
            case 1: // 新手村 - 简单地图（怪物等级低于玩家）
                minLevel = Math.max(1, playerLevel - 5);
                maxLevel = Math.max(1, playerLevel - 2);
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
        int typeRoll = rng().nextInt(100);
        if (mapId == 1) {
            // 新手村：95%普通，5%精英，0%BOSS
            if (typeRoll < 95) {
                monsterType = "普通";
            } else {
                monsterType = "精英";
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
                monster = candidates.get(rng().nextInt(candidates.size()));
            } else {
                monster = monsterMapper.selectRandomByMaxLevel(maxLevel);
            }
        }
        
        // 如果还是没有怪物，生成临时怪物
        if (monster == null) {
            int targetLevel = (minLevel + maxLevel) / 2;
            monster = generateTemporaryMonster(targetLevel, monsterType);
        }
        
        // 对于新手村，进一步降低怪物属性（70%）
        if (mapId == 1) {
            monster = weakenMonster(monster, 0.7);
        }
        
        return monster;
    }
    
    /**
     * 削弱怪物属性
     */
    private Monster weakenMonster(Monster monster, double factor) {
        Monster weakened = Monster.builder()
                .id(monster.getId())
                .name(monster.getName())
                .description(monster.getDescription())
                .level(monster.getLevel())
                .type(monster.getType())
                .health((int)(monster.getHealth() * factor))
                .attack((int)(monster.getAttack() * factor))
                .defense((int)(monster.getDefense() * factor))
                .speed(monster.getSpeed())
                .expReward(monster.getExpReward())
                .spiritStonesReward(monster.getSpiritStonesReward())
                .dropRate(monster.getDropRate())
                .dropEquipmentId(monster.getDropEquipmentId())
                .createdAt(monster.getCreatedAt())
                .updatedAt(monster.getUpdatedAt())
                .build();
        return weakened;
    }

    /**
     * 生成临时怪物（当数据库没有对应怪物时）- 优化平衡性
     */
    private Monster generateTemporaryMonster(Integer level, String type) {
        String[] normalNames = {"野狼", "山贼", "妖怪", "邪修", "恶灵"};
        String[] eliteNames = {"狂暴野狼", "山贼头目", "千年妖怪", "邪道长老", "厉鬼"};
        String[] bossNames = {"狼王", "寨主", "妖王", "邪道护法", "鬼王"};
        
        String[] names = type.equals("BOSS") ? bossNames : 
                        type.equals("精英") ? eliteNames : normalNames;
        String name = names[rng().nextInt(names.length)];
        
        double typeMultiplier = type.equals("BOSS") ? 2.5 : 
                               type.equals("精英") ? 1.3 : 0.8;
        
        // 优化属性计算，降低怪物强度
        return Monster.builder()
                .name(name)
                .description("等级" + level + "的" + type + "怪物")
                .level(level)
                .type(type)
                .health((int)(80 + level * 15 * typeMultiplier))
                .attack((int)(8 + level * 2 * typeMultiplier))
                .defense((int)(3 + level * 1 * typeMultiplier))
                .speed(10 + level / 2)
                .expReward((int)(50 + level * 10 * typeMultiplier))
                .spiritStonesReward((int)(10 + level * 2 * typeMultiplier))
                .dropRate(type.equals("BOSS") ? 50 : type.equals("精英") ? 20 : 10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 战斗主逻辑
     *
     * <p>【P2-9 重构】返回值由 {@code Map<String, Object>} 改为强类型 {@link CombatResult}，
     * 消除弱类型带来的键名拼写风险和运行时 ClassCastException 隐患。</p>
     */
    @Transactional
    public CombatResult startCombat(Integer playerId, Monster monster) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        // 获取玩家总属性（基础+装备加成）
        int playerHealth = player.getHealth() + player.getEquipmentHealthBonus();
        int playerAttack = player.getAttack() + player.getEquipmentAttackBonus();
        int playerDefense = player.getDefense() + player.getEquipmentDefenseBonus();
        int playerSpeed = player.getSpeed() + player.getEquipmentSpeedBonus();

        int monsterHealth = monster.getHealth();
        int monsterAttack = monster.getAttack();
        int monsterDefense = monster.getDefense();
        int monsterSpeed = monster.getSpeed();

        // GDD新手保护：前3场战斗，怪物属性降低50%，确保首战必胜
        // 统计玩家战斗次数（从combat_logs表）
        long battleCount = combatLogMapper.countByPlayerId(playerId);
        boolean isNewPlayer = battleCount < 3;
        double newPlayerProtectionFactor = 0.5; // 新手怪物属性降至50%
        
        if (isNewPlayer) {
            monsterHealth = (int)(monsterHealth * newPlayerProtectionFactor);
            monsterAttack = (int)(monsterAttack * newPlayerProtectionFactor);
            monsterDefense = (int)(monsterDefense * newPlayerProtectionFactor);
            battleLog.add("🌟 新手保护中！怪物属性降低50%，助你轻松获胜！");
        }

        List<String> battleLog = new ArrayList<>();
        battleLog.add("⚔️ 战斗开始！" + player.getNickname() + " VS " + monster.getName());

        int currentPlayerHealth = playerHealth;
        int currentMonsterHealth = monsterHealth;
        int rounds = 0;
        final int maxRounds = 50;

        // GDD速度优势系统：速度>对方1.5倍时获得额外行动
        double speedRatio = (double) playerSpeed / Math.max(1, monsterSpeed);
        boolean playerHasSpeedAdvantage = speedRatio >= 1.5;
        boolean monsterHasSpeedAdvantage = (1.0 / speedRatio) >= 1.5;
        
        if (playerHasSpeedAdvantage) {
            battleLog.add("🚀 速度优势！你的速度是怪物的" + String.format("%.1f", speedRatio) + "倍，每回合可行动" + 
                (speedRatio >= 2.0 ? "3次" : "2次") + "！");
        } else if (monsterHasSpeedAdvantage) {
            battleLog.add("⚠️ 速度劣势！怪物速度远高于你，当心！");
        }

        // GDD宠物参战机制：获取出战宠物并计算战斗增益
        PetCombatBonus petBonus = petService.calculatePetCombatBonus(
            petService.getActivePet(playerId));
        
        if (petBonus != null && petBonus.isEligible()) {
            battleLog.add("🐾 灵兽助战！你的" + petBonus.getPetName() + "准备参战！");
            if (petBonus.getHunger() < 20) {
                battleLog.add("⚠️ 警告：宠物饥饿，战斗效果降低50%！");
            }
        } else if (petBonus != null) {
            battleLog.add("💤 宠物因饥饿无法参战，快去喂食吧！");
            petBonus = null; // 无法参战
        }

        // 战斗循环
        while (currentPlayerHealth > 0 && currentMonsterHealth > 0 && rounds < maxRounds) {
            rounds++;
            
            // GDD：速度优势时，玩家可获得额外行动
            // 速度比>=2.0: 玩家3次行动 vs 怪物1次
            // 速度比>=1.5: 玩家2次行动 vs 怪物1次
            // 正常: 交替进行
            
            int playerActionsThisRound = 1;
            int monsterActionsThisRound = 1;
            
            if (speedRatio >= 2.0) {
                playerActionsThisRound = 3;
                monsterActionsThisRound = 1;
            } else if (speedRatio >= 1.5) {
                playerActionsThisRound = 2;
                monsterActionsThisRound = 1;
            } else if (1.0 / speedRatio >= 2.0) {
                playerActionsThisRound = 1;
                monsterActionsThisRound = 3;
            } else if (1.0 / speedRatio >= 1.5) {
                playerActionsThisRound = 1;
                monsterActionsThisRound = 2;
            }
            
            // 根据速度决定先后手
            boolean playerFirst = playerSpeed >= monsterSpeed;

            if (playerFirst) {
                // 玩家行动
                for (int i = 0; i < playerActionsThisRound && currentMonsterHealth > 0; i++) {
                    int damage = calculateDamage(playerAttack, monsterDefense, player.getLevel(), 
                        monster.getLevel(), playerSpeed, monsterSpeed, true, battleLog);
                    currentMonsterHealth -= damage;
                    String actionMarker = playerActionsThisRound > 1 ? "【连击" + (i+1) + "】" : "";
                    battleLog.add("第" + rounds + "回合: " + actionMarker + player.getNickname() + 
                        "造成了" + damage + "点伤害");
                }
                
                // GDD宠物参战：每N回合宠物发动技能
                if (petBonus != null && rounds % petBonus.getSkillCooldown() == 0) {
                    // 检查是否触发技能
                    if (rng().nextDouble() < petBonus.getSkillTriggerChance()) {
                        int petDamage = petBonus.getSkillDamage();
                        currentMonsterHealth -= petDamage;
                        String resonanceMsg = petBonus.isResonance() ? "【共鸣爆发】" : "";
                        battleLog.add("🐾 " + resonanceMsg + petBonus.getPetName() + 
                            "发动灵兽技能！造成了" + petDamage + "点伤害！");
                    } else {
                        battleLog.add("🐾 " + petBonus.getPetName() + "准备发动技能，但还未准备好...");
                    }
                }
                
                if (currentMonsterHealth <= 0) break;

                // 怪物行动
                for (int i = 0; i < monsterActionsThisRound && currentPlayerHealth > 0; i++) {
                    int monsterDamage = calculateDamage(monsterAttack, playerDefense, 
                        monster.getLevel(), player.getLevel(), monsterSpeed, playerSpeed, false, battleLog);
                    currentPlayerHealth -= monsterDamage;
                    String actionMarker = monsterActionsThisRound > 1 ? "【连击" + (i+1) + "】" : "";
                    battleLog.add("第" + rounds + "回合: " + actionMarker + monster.getName() + 
                        "造成了" + monsterDamage + "点伤害");
                }
            } else {
                // 怪物先行动
                for (int i = 0; i < monsterActionsThisRound && currentPlayerHealth > 0; i++) {
                    int monsterDamage = calculateDamage(monsterAttack, playerDefense, 
                        monster.getLevel(), player.getLevel(), monsterSpeed, playerSpeed, false, battleLog);
                    currentPlayerHealth -= monsterDamage;
                    String actionMarker = monsterActionsThisRound > 1 ? "【连击" + (i+1) + "】" : "";
                    battleLog.add("第" + rounds + "回合: " + actionMarker + monster.getName() + 
                        "造成了" + monsterDamage + "点伤害");
                }
                
                if (currentPlayerHealth <= 0) break;

                // 玩家行动
                for (int i = 0; i < playerActionsThisRound && currentMonsterHealth > 0; i++) {
                    int damage = calculateDamage(playerAttack, monsterDefense, player.getLevel(), 
                        monster.getLevel(), playerSpeed, monsterSpeed, true, battleLog);
                    currentMonsterHealth -= damage;
                    String actionMarker = playerActionsThisRound > 1 ? "【连击" + (i+1) + "】" : "";
                    battleLog.add("第" + rounds + "回合: " + actionMarker + player.getNickname() + 
                        "造成了" + damage + "点伤害");
                }
                
                // GDD宠物参战：每N回合宠物发动技能
                if (petBonus != null && rounds % petBonus.getSkillCooldown() == 0) {
                    if (rng().nextDouble() < petBonus.getSkillTriggerChance()) {
                        int petDamage = petBonus.getSkillDamage();
                        currentMonsterHealth -= petDamage;
                        String resonanceMsg = petBonus.isResonance() ? "【共鸣爆发】" : "";
                        battleLog.add("🐾 " + resonanceMsg + petBonus.getPetName() + 
                            "发动灵兽技能！造成了" + petDamage + "点伤害！");
                    } else {
                        battleLog.add("🐾 " + petBonus.getPetName() + "准备发动技能，但还未准备好...");
                    }
                }
            }
        }

        // GDD：战斗结束后，宠物饱食度减少（每次战斗消耗3点）
        if (petBonus != null) {
            petService.consumePetHungerAfterCombat(playerId);
            PlayerPet activePet = petService.getActivePet(playerId);
            if (activePet != null && activePet.getHunger() < 20) {
                battleLog.add("💤 战后宠物饥饿加剧，当前饱食度：" + activePet.getHunger() + "，快去喂食吧！");
            }
        }

        boolean playerWon = currentMonsterHealth <= 0;
        String result = playerWon ? "WIN" : "LOSE";

        long expGained = 0;
        long spiritStonesGained = 0;
        Integer droppedEquipmentId = null;

        if (playerWon) {
            battleLog.add("战斗胜利！");
            expGained = calculateExpReward(monster, player.getLevel());
            spiritStonesGained = calculateSpiritStonesReward(monster, player.getLevel());

            // 装备掉落检查
            if (rng().nextInt(100) < monster.getDropRate() && monster.getDropEquipmentId() != null) {
                droppedEquipmentId = monster.getDropEquipmentId();
                try {
                    equipmentService.acquireEquipment(droppedEquipmentId, playerId);
                    battleLog.add("获得装备掉落！");
                } catch (Exception e) {
                    log.warn("装备掉落失败: {}", e.getMessage());
                }
            }

            // 更新玩家经验和灵石
            player.setExp(player.getExp() + expGained);
            player.setSpiritStones(player.getSpiritStones() + spiritStonesGained);

            // 升级检查
            int levelUps = 0;
            while (player.getExp() >= player.getExpToNext() && levelUps < 100) {
                player.setExp(player.getExp() - player.getExpToNext());
                player.setLevel(player.getLevel() + 1);
                player.setExpToNext((long)(player.getExpToNext() * 1.5));
                player.setHealth(player.getHealth() + 10);
                player.setMana(player.getMana() + 5);
                player.setAttack(player.getAttack() + 2);
                player.setDefense(player.getDefense() + 1);
                player.setAttributePoints(player.getAttributePoints() + 5);
                levelUps++;
            }
            if (levelUps > 0) {
                battleLog.add("恭喜升级！当前等级：" + player.getLevel());
            }

            playerProfileMapper.updateById(player);
            battleLog.add("获得经验：" + expGained + "，灵石：" + spiritStonesGained);
        } else {
            battleLog.add("战斗失败...");
            long lostSpiritStones = spiritStonesGained / 10;
            if (player.getSpiritStones() >= lostSpiritStones && lostSpiritStones > 0) {
                player.setSpiritStones(player.getSpiritStones() - lostSpiritStones);
                playerProfileMapper.updateById(player);
                battleLog.add("损失灵石：" + lostSpiritStones);
            }
        }

        // 持久化战斗日志
        saveCombatLog(playerId, monster, result, rounds, expGained, spiritStonesGained, droppedEquipmentId, battleLog);

        return CombatResult.builder()
                .result(result)
                .rounds(rounds)
                .totalBattles(1)
                .wins(playerWon ? 1 : 0)
                .losses(playerWon ? 0 : 1)
                .winRate(playerWon ? 1.0 : 0.0)
                .averageRounds(rounds)
                .totalExpGained(expGained)
                .totalSpiritStonesGained(spiritStonesGained)
                .droppedEquipmentId(droppedEquipmentId)
                .battleLog(battleLog)
                .monsterName(monster.getName())
                .monsterLevel(monster.getLevel())
                .monsterType(monster.getType())
                .playerLevel(player.getLevel())
                .playerExp(player.getExp())
                .playerSpiritStones(player.getSpiritStones())
                .build();
    }

    /**
     * 计算伤害 - GDD优化公式 v2
     * 包含：防御率机制、暴击系统、速度优势
     * 
     * GDD设计原则：
     * - 防御率 = defense / (defense + attackerLevel * 10)，让防御有真实价值
     * - 暴击率默认5%，暴击伤害1.8倍
     * - 速度>对方1.5倍时获得额外行动机会
     */
    private int calculateDamage(int attack, int defense, int attackerLevel, int defenderLevel, 
                                 int attackerSpeed, int defenderSpeed, boolean isPlayerAttacking, 
                                 List<String> battleLog) {
        // 1. 等级压制（降低影响，保持平衡）
        double levelFactor = 1.0 + (attackerLevel - defenderLevel) * 0.03;
        levelFactor = Math.max(0.7, Math.min(1.3, levelFactor));
        
        // 2. GDD防御率公式：让防御属性有意义
        // 防御率 = defense / (defense + attackerLevel * 10)
        double defenseRate = defense / (defense + attackerLevel * 10.0);
        defenseRate = Math.max(0, Math.min(0.7, defenseRate)); // 最多减伤70%
        
        // 3. 基础伤害
        int baseDamage = (int)(attack * levelFactor);
        
        // 4. 应用防御率
        int finalDamage = (int)(baseDamage * (1 - defenseRate));
        
        // 5. 暴击机制（GDD新增）
        // 默认暴击率5%，高风险玩家/特定技能可提升
        double critChance = 0.05; 
        boolean isCrit = rng().nextDouble() < critChance;
        
        if (isCrit) {
            finalDamage = (int)(finalDamage * 1.8); // 暴击伤害1.8倍
            if (battleLog != null) {
                battleLog.add((isPlayerAttacking ? "【暴击】" : "【怪物暴击】") + 
                    (isPlayerAttacking ? "你" : "怪物") + "触发了暴击！伤害大幅提升！");
            }
        }
        
        // 6. 随机波动 ±15%
        int variance = (int)(finalDamage * 0.15);
        if (variance > 0) {
            finalDamage += rng().nextInt(variance * 2 + 1) - variance;
        }
        
        // 7. 保证最小伤害（至少造成攻击力的10%）
        int minDamage = Math.max(1, attack / 10);
        return Math.max(minDamage, finalDamage);
    }
    
    /**
     * 简化的伤害计算（兼容旧调用）
     */
    private int calculateDamage(int attack, int defense, int attackerLevel, int defenderLevel) {
        return calculateDamage(attack, defense, attackerLevel, defenderLevel, 10, 10, true, null);
    }

    /**
     * 计算经验奖励
     */
    private long calculateExpReward(Monster monster, int playerLevel) {
        long baseExp = monster.getExpReward();
        int levelDiff = playerLevel - monster.getLevel();
        if (levelDiff > 5) {
            baseExp = (long)(baseExp * 0.1);
        } else if (levelDiff > 0) {
            baseExp = (long)(baseExp * (1 - levelDiff * 0.1));
        } else if (levelDiff < -5) {
            baseExp = (long)(baseExp * 2.0);
        }
        return Math.max(1, baseExp);
    }

    /**
     * 计算灵石奖励
     * GDD设计：战斗灵石 = (10 + 怪物等级 × 2) × 怪物类型倍率
     *   普通怪: ×1.0
     *   精英怪: ×2.5
     *   BOSS:   ×6.0
     */
    private long calculateSpiritStonesReward(Monster monster, int playerLevel) {
        int monsterLevel = monster.getLevel();
        String monsterType = monster.getType();

        // 计算基础灵石
        long baseReward = 10 + monsterLevel * 2;

        // 应用怪物类型倍率
        double typeMultiplier;
        if ("BOSS".equals(monsterType)) {
            typeMultiplier = 6.0;
        } else if ("精英".equals(monsterType)) {
            typeMultiplier = 2.5;
        } else {
            typeMultiplier = 1.0; // 普通怪
        }

        // 等级差惩罚：玩家比怪物高太多，奖励降低
        int levelDiff = playerLevel - monsterLevel;
        double levelFactor = 1.0;
        if (levelDiff > 10) {
            levelFactor = 0.5; // 高10级惩罚50%
        } else if (levelDiff > 5) {
            levelFactor = 0.75; // 高5级惩罚25%
        } else if (levelDiff < -5) {
            levelFactor = 1.25; // 低5级奖励+25%（风险补偿）
        }

        long finalReward = (long)(baseReward * typeMultiplier * levelFactor);
        log.debug("灵石计算: 基础={}, 类型倍率={}, 等级因子={}, 最终={}",
                baseReward, typeMultiplier, levelFactor, finalReward);

        return Math.max(1, finalReward);
    }

    /**
     * 持久化战斗日志（抽取复用）
     */
    private void saveCombatLog(Integer playerId, Monster monster, String result, int rounds,
                                long expGained, long spiritStonesGained,
                                Integer droppedEquipmentId, List<String> battleLog) {
        String battleDetailsJson = "[]";
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
                .expGained((int) Math.min(expGained, Integer.MAX_VALUE))
                .spiritStonesGained((int) Math.min(spiritStonesGained, Integer.MAX_VALUE))
                .equipmentDropped(droppedEquipmentId)
                .battleDetails(battleDetailsJson)
                .createdAt(LocalDateTime.now())
                .build();
        combatLogMapper.insert(combatLog);
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
    
    /**
     * 批量战斗
     *
     * <p>【P1-6 修复】原实现先调用 {@code startCombat()} 写入一次数据库，再用差值修正，
     * 存在两次写入之间发生崩溃导致数据不一致的风险。</p>
     *
     * <p>新实现：直接在本方法内完成所有计算，只写一次数据库，保证原子性。
     * 批量战斗基于单次战斗结果推算（胜则等比放大，败则不给奖励），逻辑与原版一致。</p>
     *
     * @param playerId    玩家ID
     * @param playerLevel 玩家等级（用于生成怪物）
     * @param mapId       地图ID
     * @param times       战斗次数（上限100）
     * @return 战斗汇总结果
     */
    @Transactional
    public CombatResult batchCombat(Integer playerId, Integer playerLevel, Integer mapId, int times) {
        log.info("批量战斗开始: playerId={}, times={}, mapId={}", playerId, times, mapId);

        // 上限保护
        int actualTimes = Math.min(times, 100);

        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        // 生成怪物
        Monster monster = generateMonster(playerLevel, mapId);
        log.debug("生成怪物: {}(Lv.{} {})", monster.getName(), monster.getLevel(), monster.getType());

        // --- 在不写库的情况下模拟一次战斗，得到基础结果 ---
        SingleCombatOutcome outcome = simulateSingleCombat(player, monster);

        int wins = 0;
        long totalExpGained = 0;
        long totalSpiritStonesGained = 0;
        List<String> battleLog = outcome.battleLog;

        if (outcome.playerWon) {
            // 胜利：倍数放大奖励，一次性写库
            wins = actualTimes;
            totalExpGained = (long) outcome.expGained * actualTimes;
            totalSpiritStonesGained = (long) outcome.spiritStonesGained * actualTimes;

            player.setExp(player.getExp() + totalExpGained);
            player.setSpiritStones(player.getSpiritStones() + totalSpiritStonesGained);

            // 升级检查
            int levelUps = 0;
            while (player.getExp() >= player.getExpToNext() && levelUps < 200) {
                player.setExp(player.getExp() - player.getExpToNext());
                player.setLevel(player.getLevel() + 1);
                player.setExpToNext((long)(player.getExpToNext() * 1.5));
                player.setHealth(player.getHealth() + 10);
                player.setMana(player.getMana() + 5);
                player.setAttack(player.getAttack() + 2);
                player.setDefense(player.getDefense() + 1);
                player.setAttributePoints(player.getAttributePoints() + 5);
                levelUps++;
            }
            if (levelUps > 0) {
                log.debug("玩家升级 {} 次，当前等级: {}", levelUps, player.getLevel());
            }

            // 一次写库（原子性保证）
            playerProfileMapper.updateById(player);
        }
        // 失败：不更新玩家数据，无需写库

        // 持久化一条代表性战斗日志
        saveCombatLog(playerId, monster,
                outcome.playerWon ? "WIN" : "LOSE",
                outcome.rounds,
                totalExpGained, totalSpiritStonesGained,
                null, battleLog);

        log.info("批量战斗完成: wins={}/{}, exp={}, stones={}", wins, actualTimes, totalExpGained, totalSpiritStonesGained);

        return CombatResult.builder()
                .totalBattles(actualTimes)
                .wins(wins)
                .losses(actualTimes - wins)
                .winRate(wins > 0 ? 1.0 : 0.0)
                .averageRounds((double) outcome.rounds)
                .totalExpGained(totalExpGained)
                .totalSpiritStonesGained(totalSpiritStonesGained)
                .battleLog(battleLog)
                .monsterName(monster.getName())
                .monsterLevel(monster.getLevel())
                .monsterType(monster.getType())
                .playerLevel(player.getLevel())
                .playerExp(player.getExp())
                .playerSpiritStones(player.getSpiritStones())
                .build();
    }

    // =====================================================================
    // 内部辅助：不写库的单次战斗模拟（用于 batchCombat 计算）
    // =====================================================================

    /** 单次战斗模拟结果（内部使用） */
    private static class SingleCombatOutcome {
        boolean playerWon;
        int rounds;
        long expGained;
        long spiritStonesGained;
        List<String> battleLog;
    }

    /**
     * 模拟一次战斗并返回结果，不写数据库
     * batchCombat 用此方法获取基础数据，再统一乘以倍数后一次写库
     */
    private SingleCombatOutcome simulateSingleCombat(PlayerProfile player, Monster monster) {
        int playerAttack = player.getAttack() + player.getEquipmentAttackBonus();
        int playerDefense = player.getDefense() + player.getEquipmentDefenseBonus();
        int playerSpeed = player.getSpeed() + player.getEquipmentSpeedBonus();

        int currentPlayerHealth = player.getHealth() + player.getEquipmentHealthBonus();
        int currentMonsterHealth = monster.getHealth();

        List<String> log = new ArrayList<>();
        log.add("战斗开始！" + player.getNickname() + " VS " + monster.getName());

        int rounds = 0;
        boolean playerFirst = playerSpeed >= monster.getSpeed();

        while (currentPlayerHealth > 0 && currentMonsterHealth > 0 && rounds < 50) {
            rounds++;
            if (playerFirst) {
                int dmg = calculateDamage(playerAttack, monster.getDefense(), player.getLevel(), monster.getLevel());
                currentMonsterHealth -= dmg;
                log.add("第" + rounds + "回合: " + player.getNickname() + "造成了" + dmg + "点伤害");
                if (currentMonsterHealth <= 0) break;
                int mDmg = calculateDamage(monster.getAttack(), playerDefense, monster.getLevel(), player.getLevel());
                currentPlayerHealth -= mDmg;
                log.add("第" + rounds + "回合: " + monster.getName() + "造成了" + mDmg + "点伤害");
            } else {
                int mDmg = calculateDamage(monster.getAttack(), playerDefense, monster.getLevel(), player.getLevel());
                currentPlayerHealth -= mDmg;
                log.add("第" + rounds + "回合: " + monster.getName() + "造成了" + mDmg + "点伤害");
                if (currentPlayerHealth <= 0) break;
                int dmg = calculateDamage(playerAttack, monster.getDefense(), player.getLevel(), monster.getLevel());
                currentMonsterHealth -= dmg;
                log.add("第" + rounds + "回合: " + player.getNickname() + "造成了" + dmg + "点伤害");
            }
        }

        SingleCombatOutcome result = new SingleCombatOutcome();
        result.playerWon = currentMonsterHealth <= 0;
        result.rounds = rounds;
        result.battleLog = log;

        if (result.playerWon) {
            result.expGained = calculateExpReward(monster, player.getLevel());
            result.spiritStonesGained = calculateSpiritStonesReward(monster, player.getLevel());
            log.add("战斗胜利！获得经验：" + result.expGained + "，灵石：" + result.spiritStonesGained);
        } else {
            log.add("战斗失败...");
        }

        return result;
    }
}

