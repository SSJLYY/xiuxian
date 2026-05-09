package com.xiuxian.game.modules.combat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiuxian.game.common.config.GameBalanceConfig;
import com.xiuxian.game.common.util.GameBalanceUtils;
import com.xiuxian.game.dto.response.CombatResult;
import com.xiuxian.game.dto.response.PetCombatBonus;
import com.xiuxian.game.modules.combat.entity.CombatLog;
import com.xiuxian.game.modules.combat.entity.MapMonster;
import com.xiuxian.game.modules.combat.entity.Monster;
import com.xiuxian.game.modules.combat.mapper.CombatLogMapper;
import com.xiuxian.game.modules.combat.mapper.MapMonsterMapper;
import com.xiuxian.game.modules.combat.mapper.MonsterMapper;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import com.xiuxian.game.modules.pet.service.PetService;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

/**
 * 战斗服务类
 * 
 * <p>提供战斗相关的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>回合制战斗计算 - 玩家 vs 怪物</li>
 *   <li>连击机制 - 根据速度属性触发多次攻击</li>
 *   <li>掉落计算 - 基于幸运值和掉落率</li>
 *   <li>战斗日志记录 - 详细战斗过程追踪</li>
 *   <li>宠物战斗加成 - 宠物参战机制</li>
 *   <li>装备属性加成 - 战斗中的装备效果</li>
 * </ul>
 * 
 * <p>战斗流程：</p>
 * <ol>
 *   <li>选择怪物并检测冲突</li>
 *   <li>计算双方属性（含装备/技能/宠物加成）</li>
 *   <li>回合制战斗模拟</li>
 *   <li>计算战利品掉落</li>
 *   <li>记录战斗日志</li>
 * </ol>
 * 
 * @author xiuxian-game-team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    static class ActionPlan {
        final int playerActions;
        final int monsterActions;
        final boolean playerFirst;

        ActionPlan(int playerActions, int monsterActions, boolean playerFirst) {
            this.playerActions = playerActions;
            this.monsterActions = monsterActions;
            this.playerFirst = playerFirst;
        }
    }

    private final PlayerService playerService;       // 模块边界：通过PlayerService访问玩家数据
    private final PlayerProfileMapper playerProfileMapper;
    private final MapMonsterMapper mapMonsterMapper;
    private final MonsterMapper monsterMapper;
    private final CombatLogMapper combatLogMapper;
    private final EquipmentService equipmentService;
    private final ObjectMapper objectMapper;
    private final PetService petService;             // GDD: 宠物参战机制
    private final GameBalanceConfig balance;
    private final GameBalanceUtils balanceUtils;

    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

    /**
     * 生成怪物（默认地图1）
     */
    public Monster generateMonster(Integer playerLevel) {
        return generateMonsterByMap(playerLevel, 1);
    }

    /**
     * 根据地图生成怪物
     *
     * @param playerLevel 玩家等级
     * @param mapId       地图ID (1=新手村, 2=野外)
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
        return Monster.builder()
                .id(monster.getId())
                .name(monster.getName())
                .description(monster.getDescription())
                .level(monster.getLevel())
                .type(monster.getType())
                .health((int)(defaultInt(monster.getHealth()) * factor))
                .attack((int)(defaultInt(monster.getAttack()) * factor))
                .defense((int)(defaultInt(monster.getDefense()) * factor))
                .speed(defaultInt(monster.getSpeed()))
                .expReward(monster.getExpReward())
                .spiritStonesReward(monster.getSpiritStonesReward())
                .dropRate(monster.getDropRate())
                .dropEquipmentId(monster.getDropEquipmentId())
                .createdAt(monster.getCreatedAt())
                .updatedAt(monster.getUpdatedAt())
                .build();
    }

    /**
     * 生成临时怪物（当数据库没有对应怪物时）- 优化平衡性
     */
    private Monster generateTemporaryMonster(Integer level, String type) {
        String[] normalNames = {"野狼", "山虎", "妖鬼", "邪修", "恶灵"};
        String[] eliteNames = {"狂暴野狼", "山虎头目", "千年妖鬼", "邪道长老", "厉鬼"};
        String[] bossNames = {"狼王", "寇主", "妖王", "邪道护法", "炼王"};

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
     * <p>【性能优化】战斗计算（纯CPU操作）不持有DB事务，仅在最终写库阶段开启事务，
     * 减少DB连接持有时间（原长事务可能持有数十毫秒）。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public CombatResult startCombat(Integer playerId, Monster monster) {
        // 阶段1：参数校验 + 新手保护 + 宠物准备
        CombatContext ctx = prepareCombat(playerId, monster);

        // 阶段2：执行战斗主循环
        executeBattleLoop(ctx);

        // 阶段3：处理战斗结果（奖励/升级/惩罚）
        processBattleOutcome(ctx);

        // 阶段4：持久化日志 + 构建返回
        String result = ctx.playerWon ? "WIN" : "LOSE";
        saveCombatLog(playerId, monster, result, ctx.rounds,
                ctx.expGained, ctx.spiritStonesGained, ctx.droppedEquipmentId, ctx.battleLog);

        return CombatResult.builder()
                .result(result)
                .rounds(ctx.rounds)
                .totalBattles(1)
                .wins(ctx.playerWon ? 1 : 0)
                .losses(ctx.playerWon ? 0 : 1)
                .winRate(ctx.playerWon ? 1.0 : 0.0)
                .averageRounds(ctx.rounds)
                .totalExpGained(ctx.expGained)
                .totalSpiritStonesGained(ctx.spiritStonesGained)
                .droppedEquipmentId(ctx.droppedEquipmentId)
                .battleLog(ctx.battleLog)
                .monsterName(monster.getName())
                .monsterLevel(monster.getLevel())
                .monsterType(monster.getType())
                .playerLevel(defaultInt(ctx.player.getLevel()))
                .playerExp(defaultLong(ctx.player.getExp()))
                .playerSpiritStones(defaultLong(ctx.player.getSpiritStones()))
                .build();
    }

    // ==================== 战斗子方法（从 startCombat 拆分） ====================

    /** 战斗上下文：保存战斗过程中的可变状态 */
    private static class CombatContext {
        PlayerProfile player;
        Monster monster;
        int playerAttack, playerDefense, playerSpeed;
        int playerHealth;
        int monsterHealth, monsterAttack, monsterDefense, monsterSpeed;
        int currentPlayerHealth, currentMonsterHealth;
        int rounds;
        double speedRatio;
        PetCombatBonus petBonus;
        List<String> battleLog;
        boolean playerWon;
        long expGained, spiritStonesGained;
        Integer droppedEquipmentId;
    }

    /** 阶段1：校验 + 新手保护 + 速度计算 + 宠物准备 */
    private CombatContext prepareCombat(Integer playerId, Monster monster) {
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        CombatContext ctx = new CombatContext();
        ctx.player = player;
        ctx.monster = monster;
        ctx.playerAttack = player.getTotalAttack();
        ctx.playerDefense = player.getTotalDefense();
        ctx.playerSpeed = player.getTotalSpeed();
        ctx.playerHealth = player.getTotalHealth();
        ctx.monsterHealth = defaultInt(monster.getHealth());
        ctx.monsterAttack = defaultInt(monster.getAttack());
        ctx.monsterDefense = defaultInt(monster.getDefense());
        ctx.monsterSpeed = defaultInt(monster.getSpeed());
        ctx.battleLog = new ArrayList<>();

        // 新手保护
        long battleCount = combatLogMapper.countByPlayerId(playerId);
        if (battleCount < balance.getCombat().getNewbieBattleProtection()) {
            double factor = balance.getCombat().getNewbieMonsterWeakFactor();
            ctx.monsterHealth = (int)(ctx.monsterHealth * factor);
            ctx.monsterAttack = (int)(ctx.monsterAttack * factor);
            ctx.monsterDefense = (int)(ctx.monsterDefense * factor);
            ctx.battleLog.add("🌟 新手保护中！怪物属性降低" + (int)((1 - factor) * 100) + "%，助你轻松获胜！");
        }

        ctx.battleLog.add("⚔️ 战斗开始！" + player.getNickname() + " VS " + monster.getName());
        ctx.currentPlayerHealth = ctx.playerHealth;
        ctx.currentMonsterHealth = ctx.monsterHealth;
        ctx.rounds = 0;

        // 速度优势计算
        int playerSpeedActions = balanceUtils.calculateSpeedAdvantageActions(ctx.playerSpeed, ctx.monsterSpeed);
        ctx.speedRatio = (ctx.monsterSpeed > 0) ? (double) ctx.playerSpeed / ctx.monsterSpeed : 2.0;

        if (playerSpeedActions > 1) {
            ctx.battleLog.add("🚀 速度优势！你的速度是怪物的" + String.format("%.1f", ctx.speedRatio) + "倍，每回合可行动" +
                (ctx.speedRatio >= 2.0 ? "3次" : "2次") + "！");
        } else if (balanceUtils.calculateSpeedAdvantageActions(ctx.monsterSpeed, ctx.playerSpeed) > 1) {
            ctx.battleLog.add("⚠️ 速度劣势！怪物速度远高于你，当心！");
        }

        // 宠物参战准备
        ctx.petBonus = petService.calculatePetCombatBonus(petService.getActivePet(playerId));
        if (ctx.petBonus != null && ctx.petBonus.isEligible()) {
            ctx.battleLog.add("🐾 灵兽助战！你的" + ctx.petBonus.getPetName() + "准备参战！");
            if (ctx.petBonus.getHunger() < 20) {
                ctx.battleLog.add("⚠️ 警告：宠物饥饿，战斗效果降低50%！");
            }
        } else if (ctx.petBonus != null) {
            ctx.battleLog.add("💙 宠物因饥饿无法参战，快去喂食吧！");
            ctx.petBonus = null;
        }

        return ctx;
    }

    /** 阶段2：战斗主循环 */
    private void executeBattleLoop(CombatContext ctx) {
        final int maxRounds = balance.getCombat().getMaxRounds();
        PetCombatBonus petBonus = ctx.petBonus;

        while (ctx.currentPlayerHealth > 0 && ctx.currentMonsterHealth > 0 && ctx.rounds < maxRounds) {
            ctx.rounds++;

            ActionPlan actionPlan = createActionPlan(ctx.playerSpeed, ctx.monsterSpeed);

            if (actionPlan.playerFirst) {
                executePlayerTurn(ctx, actionPlan.playerActions);
                ctx.currentMonsterHealth -= applyPetSkillDamage(ctx.rounds, petBonus, ctx.battleLog);
                if (ctx.currentMonsterHealth <= 0) break;
                executeMonsterTurn(ctx, actionPlan.monsterActions);
            } else {
                executeMonsterTurn(ctx, actionPlan.monsterActions);
                if (ctx.currentPlayerHealth <= 0) break;
                executePlayerTurn(ctx, actionPlan.playerActions);
                ctx.currentMonsterHealth -= applyPetSkillDamage(ctx.rounds, petBonus, ctx.battleLog);
            }
        }

        // 战后宠物饱食度消耗
        if (petBonus != null) {
            petService.consumePetHungerAfterCombat(ctx.player.getId());
            PlayerPet activePet = petService.getActivePet(ctx.player.getId());
            if (activePet != null && defaultInt(activePet.getHunger()) < 20) {
                ctx.battleLog.add("💙 战后宠物饥饿加剧，当前饱食度：" + activePet.getHunger() + "，快去喂食吧！");
            }
        }
    }

    /** 玩家行动 */
    private void executePlayerTurn(CombatContext ctx, int actions) {
        for (int i = 0; i < actions && ctx.currentMonsterHealth > 0; i++) {
            int damage = calculateDamage(ctx.playerAttack, ctx.monsterDefense,
                    Math.max(1, defaultInt(ctx.player.getLevel())),
                    Math.max(1, defaultInt(ctx.monster.getLevel())),
                    ctx.playerSpeed, ctx.monsterSpeed, true, ctx.battleLog);
            ctx.currentMonsterHealth -= damage;
            String marker = actions > 1 ? "【连击" + (i + 1) + "】" : "";
            ctx.battleLog.add("第" + ctx.rounds + "回合: " + marker + ctx.player.getNickname() + "造成了" + damage + "点伤害");
        }
    }

    /** 怪物行动 */
    private void executeMonsterTurn(CombatContext ctx, int actions) {
        for (int i = 0; i < actions && ctx.currentPlayerHealth > 0; i++) {
            int damage = calculateDamage(ctx.monsterAttack, ctx.playerDefense,
                    Math.max(1, defaultInt(ctx.monster.getLevel())),
                    Math.max(1, defaultInt(ctx.player.getLevel())),
                    ctx.monsterSpeed, ctx.playerSpeed, false, ctx.battleLog);
            ctx.currentPlayerHealth -= damage;
            String marker = actions > 1 ? "【连击" + (i + 1) + "】" : "";
            ctx.battleLog.add("第" + ctx.rounds + "回合: " + marker + ctx.monster.getName() + "造成了" + damage + "点伤害");
        }
    }

    ActionPlan createActionPlan(int playerSpeed, int monsterSpeed) {
        double speedRatio = monsterSpeed > 0 ? (double) playerSpeed / monsterSpeed : 2.0;
        int playerActions = 1;
        int monsterActions = 1;
        if (speedRatio >= 2.0) { playerActions = 3; }
        else if (speedRatio >= 1.5) { playerActions = 2; }
        else if (1.0 / speedRatio >= 2.0) { monsterActions = 3; }
        else if (1.0 / speedRatio >= 1.5) { monsterActions = 2; }
        return new ActionPlan(playerActions, monsterActions, playerSpeed >= monsterSpeed);
    }

    int applyPetSkillDamage(int round, PetCombatBonus petBonus, List<String> battleLog) {
        if (petBonus == null || round % petBonus.getSkillCooldown() != 0) {
            return 0;
        }
        if (rng().nextDouble() < petBonus.getSkillTriggerChance()) {
            int petDamage = petBonus.getSkillDamage();
            if (petBonus.isResonance()) {
                petDamage *= 2;
            }
            String resonanceMsg = petBonus.isResonance() ? "【共鸣迸发】" : "";
            battleLog.add("🐾 " + resonanceMsg + petBonus.getPetName() + "发动灵兽技能！造成了" + petDamage + "点伤害！");
            return petDamage;
        } else {
            battleLog.add("🐾 " + petBonus.getPetName() + "准备发动技能，但还未准备好...");
            return 0;
        }
    }

    /** 阶段3：处理战斗结果（奖励/升级/惩罚） */
    private void processBattleOutcome(CombatContext ctx) {
        ctx.playerWon = ctx.currentMonsterHealth <= 0;

        // 累加总战斗次数（无论输赢都计数），复用 processBattleOutcome 中的 savePlayerProfile
        if (ctx.player.getTotalBattles() == null) {
            ctx.player.setTotalBattles(1);
        } else {
            ctx.player.setTotalBattles(ctx.player.getTotalBattles() + 1);
        }

        if (ctx.playerWon) {
            ctx.battleLog.add("战斗胜利！");
            ctx.expGained = calculateExpReward(ctx.monster, Math.max(1, defaultInt(ctx.player.getLevel())));
            ctx.spiritStonesGained = calculateSpiritStonesReward(ctx.monster, Math.max(1, defaultInt(ctx.player.getLevel())));

            // 装备掉落
            if (rng().nextInt(100) < defaultInt(ctx.monster.getDropRate()) && ctx.monster.getDropEquipmentId() != null) {
                ctx.droppedEquipmentId = ctx.monster.getDropEquipmentId();
                try {
                    equipmentService.acquireEquipment(ctx.droppedEquipmentId, ctx.player.getId());
                    ctx.battleLog.add("获得装备掉落！");
                } catch (Exception e) {
                    log.warn("装备掉落失败: {}", e.getMessage());
                }
            }

            ctx.player.setExp(defaultLong(ctx.player.getExp()) + ctx.expGained);
            ctx.player.setSpiritStones(defaultLong(ctx.player.getSpiritStones()) + ctx.spiritStonesGained);
            processLevelUp(ctx);
            ctx.battleLog.add("获得经验：" + ctx.expGained + "，灵石：" + ctx.spiritStonesGained);
        } else {
            ctx.battleLog.add("战斗失败...");
            long currentSpiritStones = defaultLong(ctx.player.getSpiritStones());
            long lostSpiritStones = Math.max(1, currentSpiritStones / 100);
            long actualLoss = Math.min(currentSpiritStones, Math.max(0, lostSpiritStones));
            if (actualLoss > 0) {
                ctx.player.setSpiritStones(currentSpiritStones - actualLoss);
                ctx.battleLog.add("损失灵石：" + actualLoss);
            }
        }
        ctx.player.setHealth(Math.max(0, Math.min(ctx.currentPlayerHealth, defaultInt(ctx.player.getMaxHealth()))));
        // 统一保存：累加了 totalBattles，并持久化 if/else 中修改的属性
        playerService.savePlayerProfile(ctx.player);
    }

    /** 升级循环 */
    private void processLevelUp(CombatContext ctx) {
        int levelUps = playerService.applyLevelUpsWithoutCommit(ctx.player, 100);
        if (levelUps > 0) {
            ctx.battleLog.add("恭喜升级！当前等级：" + defaultInt(ctx.player.getLevel()));
        }
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
    int calculateDamage(int attack, int defense, int attackerLevel, int defenderLevel,
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
        long baseExp = defaultInt(monster.getExpReward());
        int levelDiff = playerLevel - Math.max(1, defaultInt(monster.getLevel()));
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
        int monsterLevel = Math.max(1, defaultInt(monster.getLevel()));
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
            levelFactor = 1.25; // 低5级加成25%（风险补偿）
        }

        long finalReward = (long)(baseReward * typeMultiplier * levelFactor);
        log.debug("灵石计算: 基础={}, 类型倍率={}, 等级因子={}, 最终={}",
                baseReward, typeMultiplier, levelFactor, finalReward);

        return Math.max(1, finalReward);
    }

    /**
     * 持久化战斗日志（抽取复用）
     */
    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    void saveCombatLog(Integer playerId, Monster monster, String result, int rounds,
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

    /**
     * 根据怪物ID获取怪物
     */
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
     * <p>新实现：逐次执行战斗并累计结果，只在批次结束后统一落库，避免把单次结果线性放大。</p>
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

        if (times <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "战斗次数必须大于0");
        }

        int actualTimes = Math.min(times, 100);

        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在");
        }

        int wins = 0;
        long totalExpGained = 0;
        long totalSpiritStonesGained = 0;
        long totalRounds = 0;
        List<String> battleLog = new ArrayList<>();
        Monster lastMonster = null;

        for (int i = 0; i < actualTimes; i++) {
            Monster monster = generateMonster(Math.max(1, defaultInt(player.getLevel())), mapId);
            lastMonster = monster;
            log.debug("第{}场生成怪物: {}(Lv.{} {})", i + 1, monster.getName(), monster.getLevel(), monster.getType());

            SingleCombatOutcome outcome = simulateSingleCombat(player, monster);
            totalRounds += outcome.rounds;
            player.setTotalBattles((player.getTotalBattles() == null ? 0 : player.getTotalBattles()) + 1);
            battleLog.add("第" + (i + 1) + "场: " + monster.getName() + "(Lv." + monster.getLevel() + ") -> " + (outcome.playerWon ? "胜利" : "失败"));

            if (outcome.playerWon) {
                wins++;
                totalExpGained += outcome.expGained;
                totalSpiritStonesGained += outcome.spiritStonesDelta;
                player.setExp(defaultLong(player.getExp()) + outcome.expGained);
                player.setSpiritStones(defaultLong(player.getSpiritStones()) + outcome.spiritStonesDelta);

                int levelUps = playerService.applyLevelUpsWithoutCommit(player, 100);
                if (levelUps > 0) {
                    battleLog.add("第" + (i + 1) + "场后升级至 Lv." + player.getLevel());
                }
            } else {
                totalSpiritStonesGained += outcome.spiritStonesDelta;
                long currentSpiritStones = defaultLong(player.getSpiritStones());
                long actualLoss = Math.min(currentSpiritStones, Math.max(0, -outcome.spiritStonesDelta));
                if (actualLoss > 0) {
                    player.setSpiritStones(currentSpiritStones - actualLoss);
                    battleLog.add("第" + (i + 1) + "场失败，损失灵石：" + actualLoss);
                }
            }

            player.setHealth(Math.max(0, Math.min(outcome.currentPlayerHealth, defaultInt(player.getMaxHealth()))));
        }

        PlayerPet activePet = petService.getActivePet(playerId);
        if (activePet != null && activePet.getHunger() != null && activePet.getHunger() > 0) {
            int newHunger = Math.max(0, defaultInt(activePet.getHunger()) - actualTimes * 10);
            activePet.setHunger(newHunger);
            petService.updatePlayerPet(activePet);
            if (newHunger < 20) {
                battleLog.add("💙 连续战斗后宠物饥饿加剧，当前饱食度：" + newHunger);
            }
        }

        saveBatchCombatResult(player, playerId, lastMonster, wins > 0 ? "WIN" : "LOSE",
                (int) totalRounds, totalExpGained, totalSpiritStonesGained, battleLog);

        log.info("批量战斗完成: wins={}/{}, exp={}, stones={}", wins, actualTimes, totalExpGained, totalSpiritStonesGained);

        return CombatResult.builder()
                .totalBattles(actualTimes)
                .wins(wins)
                .losses(actualTimes - wins)
                .winRate(actualTimes > 0 ? (double) wins / actualTimes : 0.0)
                .averageRounds(actualTimes > 0 ? (double) totalRounds / actualTimes : 0.0)
                .totalExpGained(totalExpGained)
                .totalSpiritStonesGained(totalSpiritStonesGained)
                .battleLog(battleLog)
                .monsterName(lastMonster != null ? lastMonster.getName() : null)
                .monsterLevel(lastMonster != null ? lastMonster.getLevel() : null)
                .monsterType(lastMonster != null ? lastMonster.getType() : null)
                .playerLevel(defaultInt(player.getLevel()))
                .playerExp(defaultLong(player.getExp()))
                .playerSpiritStones(defaultLong(player.getSpiritStones()))
                .build();
    }

    private void saveBatchCombatResult(PlayerProfile player, Integer playerId, Monster monster,
                                          String result, int rounds, long totalExpGained, 
                                          long totalSpiritStonesGained, List<String> battleLog) {
        playerService.savePlayerProfile(player);
        saveCombatLog(playerId, monster, result, rounds, totalExpGained, totalSpiritStonesGained, null, battleLog);
    }

    // =====================================================================
    // 内部辅助：不写库的单次战斗模拟（用于 batchCombat 计算）
    // =====================================================================

    /** 单次战斗模拟结果（内部使用） */
    private static class SingleCombatOutcome {
        boolean playerWon;
        int rounds;
        long expGained;
        long spiritStonesDelta;
        int currentPlayerHealth;
    }

    /**
     * 模拟一次战斗并返回结果，不写数据库
     * batchCombat 用此方法获取基础数据，再统一乘以倍数后一次写库
     */
    private SingleCombatOutcome simulateSingleCombat(PlayerProfile player, Monster monster) {
        int playerAttack = defaultInt(player.getAttack()) + defaultInt(player.getEquipmentAttackBonus());
        int playerDefense = defaultInt(player.getDefense()) + defaultInt(player.getEquipmentDefenseBonus());
        int playerSpeed = defaultInt(player.getSpeed()) + defaultInt(player.getEquipmentSpeedBonus());
        int playerLevel = Math.max(1, defaultInt(player.getLevel()));
        int monsterLevel = Math.max(1, defaultInt(monster.getLevel()));
        int monsterAttack = defaultInt(monster.getAttack());
        int monsterDefense = defaultInt(monster.getDefense());
        int monsterSpeed = defaultInt(monster.getSpeed());

        PlayerPet activePet = petService.getActivePet(player.getId());
        PetCombatBonus petBonus = petService.calculatePetCombatBonus(activePet);
        if (petBonus != null && !petBonus.isEligible()) {
            petBonus = null;
        } else if (activePet != null && petBonus != null) {
            playerAttack += defaultInt(activePet.getAttack());
            playerDefense += defaultInt(activePet.getDefense());
            playerSpeed += defaultInt(activePet.getSpeed());
        }

        int currentPlayerHealth = defaultInt(player.getHealth()) + defaultInt(player.getEquipmentHealthBonus())
                + (activePet != null && petBonus != null ? defaultInt(activePet.getHealth()) : 0);
        int currentMonsterHealth = defaultInt(monster.getHealth());

        List<String> log = new ArrayList<>();
        log.add("战斗开始！" + player.getNickname() + " VS " + monster.getName());

        int rounds = 0;
        boolean playerFirst = playerSpeed >= monsterSpeed;
        double speedRatio = monsterSpeed > 0 ? (double) playerSpeed / monsterSpeed : 2.0;

        while (currentPlayerHealth > 0 && currentMonsterHealth > 0 && rounds < 50) {
            rounds++;

            int playerActions = 1, monsterActions = 1;
            if (speedRatio >= 2.0) { playerActions = 3; }
            else if (speedRatio >= 1.5) { playerActions = 2; }
            else if (1.0 / speedRatio >= 2.0) { monsterActions = 3; }
            else if (1.0 / speedRatio >= 1.5) { monsterActions = 2; }

            if (playerFirst) {
                for (int i = 0; i < playerActions && currentMonsterHealth > 0; i++) {
                    int dmg = calculateDamage(playerAttack, monsterDefense, playerLevel, monsterLevel);
                    currentMonsterHealth -= dmg;
                    log.add("第" + rounds + "回合: " + player.getNickname() + "造成了" + dmg + "点伤害");
                }
                if (petBonus != null && rounds % petBonus.getSkillCooldown() == 0 && rng().nextDouble() < petBonus.getSkillTriggerChance()) {
                    currentMonsterHealth -= petBonus.getSkillDamage();
                    log.add("🐾 " + petBonus.getPetName() + "发动灵兽技能，造成" + petBonus.getSkillDamage() + "点伤害");
                }
                if (currentMonsterHealth <= 0) break;
                for (int i = 0; i < monsterActions && currentPlayerHealth > 0; i++) {
                    int mDmg = calculateDamage(monsterAttack, playerDefense, monsterLevel, playerLevel);
                    currentPlayerHealth -= mDmg;
                    log.add("第" + rounds + "回合: " + monster.getName() + "造成了" + mDmg + "点伤害");
                }
            } else {
                for (int i = 0; i < monsterActions && currentPlayerHealth > 0; i++) {
                    int mDmg = calculateDamage(monsterAttack, playerDefense, monsterLevel, playerLevel);
                    currentPlayerHealth -= mDmg;
                    log.add("第" + rounds + "回合: " + monster.getName() + "造成了" + mDmg + "点伤害");
                }
                if (currentPlayerHealth <= 0) break;
                for (int i = 0; i < playerActions && currentMonsterHealth > 0; i++) {
                    int dmg = calculateDamage(playerAttack, monsterDefense, playerLevel, monsterLevel);
                    currentMonsterHealth -= dmg;
                    log.add("第" + rounds + "回合: " + player.getNickname() + "造成了" + dmg + "点伤害");
                }
                if (petBonus != null && rounds % petBonus.getSkillCooldown() == 0 && rng().nextDouble() < petBonus.getSkillTriggerChance()) {
                    currentMonsterHealth -= petBonus.getSkillDamage();
                    log.add("🐾 " + petBonus.getPetName() + "发动灵兽技能，造成" + petBonus.getSkillDamage() + "点伤害");
                }
            }
        }

        SingleCombatOutcome result = new SingleCombatOutcome();
        result.playerWon = currentMonsterHealth <= 0;
        result.rounds = rounds;
        result.currentPlayerHealth = currentPlayerHealth;

        if (result.playerWon) {
            result.expGained = calculateExpReward(monster, playerLevel);
            result.spiritStonesDelta = calculateSpiritStonesReward(monster, playerLevel);
            log.add("战斗胜利！获得经验：" + result.expGained + "，灵石：" + result.spiritStonesDelta);
        } else {
            long currentSpiritStones = player.getSpiritStones() == null ? 0L : player.getSpiritStones();
            result.spiritStonesDelta = -Math.min(currentSpiritStones, Math.max(1, currentSpiritStones / 100));
            log.add("战斗失败...");
        }

        return result;
    }

    // ===================== Map module interface (module boundary) =====================

    /**
     * 根据地图ID获取地图怪物列表（供 GameMapService 使用）
     */
    public List<MapMonster> getMapMonsters(Integer mapId) {
        return mapMonsterMapper.selectByMapId(mapId);
    }

    /**
     * 根据ID获取怪物模板（供 GameMapService 使用）
     */
    public Monster getMonsterTemplateById(Integer monsterId) {
        return monsterMapper.selectById(monsterId);
    }
}
