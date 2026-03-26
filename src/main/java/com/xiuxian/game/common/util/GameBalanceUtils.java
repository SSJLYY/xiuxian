package com.xiuxian.game.common.util;

import com.xiuxian.game.common.config.GameBalanceConfig;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 游戏平衡计算工具类
 * 
 * 统一管理所有数值计算，确保与GDD设计一致
 * 所有公式都基于GameBalanceConfig配置，便于调整
 * 
 * @author shaun.sheng
 * @version 1.0
 * @since 2026-03-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameBalanceUtils {

    private final GameBalanceConfig balance;

    /**
     * 计算玩家升级所需经验
     * 公式：base_exp × (level ^ 1.5)
     * 
     * @param level 当前等级
     * @return 升级所需经验
     */
    public long calculateExpToNext(int level) {
        return (long) (balance.getExperience().getBaseExp() * Math.pow(level, balance.getExperience().getLevelExponent()));
    }

    /**
     * 计算技能升级所需经验
     * 公式：base_exp × (skill_level ^ 1.6)
     * 根据技能类型调整base_exp
     * 
     * @param skillLevel 当前技能等级
     * @param skillType 技能类型（攻击/防御/辅助/被动）
     * @return 升级所需经验
     */
    public long calculateSkillExpToNext(int skillLevel, String skillType) {
        int baseExp;
        switch (skillType) {
            case "攻击":
                baseExp = balance.getExperience().getAttackSkillBaseExp();
                break;
            case "防御":
                baseExp = balance.getExperience().getDefenseSkillBaseExp();
                break;
            case "辅助":
                baseExp = balance.getExperience().getAssistSkillBaseExp();
                break;
            case "被动":
                baseExp = balance.getExperience().getPassiveSkillBaseExp();
                break;
            default:
                baseExp = 100;
        }
        return (long) (baseExp * Math.pow(skillLevel, balance.getExperience().getSkillLevelExponent()));
    }

    /**
     * 计算技能伤害（基于等级）
     * 公式：base_damage × (1 + 0.15 × skill_level)
     * 
     * @param baseDamage 基础伤害
     * @param skillLevel 技能等级
     * @return 实际伤害
     */
    public int calculateSkillDamage(int baseDamage, int skillLevel) {
        double multiplier = 1.0 + balance.getExperience().getSkillDamagePerLevel() * skillLevel;
        return (int) (baseDamage * multiplier);
    }

    /**
     * 计算技能冷却（基于等级）
     * 公式：max(1s, base_cool - 0.3s × skill_level)
     * 
     * @param baseCooldown 基础冷却（秒）
     * @param skillLevel 技能等级
     * @return 实际冷却（秒）
     */
    public int calculateSkillCooldown(int baseCooldown, int skillLevel) {
        double reduction = balance.getExperience().getSkillCooldownReductionPerLevel() * skillLevel;
        return Math.max(1, baseCooldown - (int) reduction);
    }

    /**
     * 计算技能法力消耗（基于等级）
     * 公式：base_cost × (1 + 0.05 × skill_level)
     * 
     * @param baseManaCost 基础法力消耗
     * @param skillLevel 技能等级
     * @return 实际法力消耗
     */
    public int calculateSkillManaCost(int baseManaCost, int skillLevel) {
        double multiplier = 1.0 + balance.getExperience().getSkillManaCostPerLevel() * skillLevel;
        return (int) (baseManaCost * multiplier);
    }

    /**
     * 计算修炼收益（灵石）
     * 公式：(20 + level × 5) × cultivation_speed × realm_bonus
     * 
     * @param player 玩家
     * @param hours 修炼时长（小时）
     * @return 获得的灵石
     */
    public long calculateCultivationSpiritStones(PlayerProfile player, double hours) {
        int level = player.getLevel();
        double cultivationSpeed = player.getCultivationSpeed().doubleValue();
        double realmBonus = getRealmBonus(player.getRealm());

        double baseSpiritStones = balance.getCultivation().getBaseSpiritStonesPerHour() + level * 5;
        double perHour = baseSpiritStones * cultivationSpeed * realmBonus;
        
        long totalSpiritStones = (long) (perHour * hours);
        log.debug("修炼收益计算: level={}, cultivationSpeed={}, realmBonus={}, hours={}, 灵石={}", 
            level, cultivationSpeed, realmBonus, hours, totalSpiritStones);
        
        return totalSpiritStones;
    }

    /**
     * 计算境界修炼加成
     * 
     * @param realm 境界名称
     * @return 境界加成倍率
     */
    public double getRealmBonus(String realm) {
        if (realm == null) return 1.0;
        
        switch (realm) {
            case "筑基期":
                return balance.getCultivation().getRealmBonusFoundation();
            case "金丹期":
                return balance.getCultivation().getRealmBonusGoldenCore();
            case "元婴期":
                return balance.getCultivation().getRealmBonusNascentSoul();
            default: // 练气期
                return balance.getCultivation().getRealmBonusQiCondensation();
        }
    }

    /**
     * 计算灵石上限
     * 公式：10000 × realm_bonus
     * 防止通货膨胀
     * 
     * @param realm 境界
     * @return 灵石上限
     */
    public long calculateSpiritStonesLimit(String realm) {
        return (long) (balance.getCultivation().getBaseSpiritStonesLimit() * getRealmBonus(realm));
    }

    /**
     * 计算防御率
     * 公式：defense / (defense + attackerLevel × 10)
     * GDD已验证公式方式
     * 
     * @param defense 防御值
     * @param attackerLevel 攻击者等级
     * @return 防御率（0.0-1.0）
     */
    public double calculateDefenseRate(int defense, int attackerLevel) {
        double divisor = defense + attackerLevel * balance.getCombat().getDefenseLevelMultiplier();
        return defense / Math.max(1.0, divisor);
    }

    /**
     * 判断是否暴击
     * 
     * @return 是否暴击
     */
    public boolean isCrit() {
        return ThreadLocalRandom.current().nextDouble() < balance.getCombat().getCritRate();
    }

    /**
     * 计算暴击伤害
     * 
     * @param baseDamage 基础伤害
     * @return 暴击伤害
     */
    public int calculateCritDamage(int baseDamage) {
        return (int) (baseDamage * balance.getCombat().getCritDamageMultiplier());
    }

    /**
     * 计算怪物属性（基于等级和类型）
     * 
     * @param level 怪物等级
     * @param type 怪物类型（普通/精英/BOSS）
     * @return [HP, 攻击, 防御]
     */
    public int[] calculateMonsterStats(int level, String type) {
        double typeMultiplier = getTypeMultiplier(type);
        
        int health = (int) (balance.getMonster().getLevel1Health() + 
                           balance.getMonster().getHealthGrowthPerLevel() * level);
        int attack = (int) (balance.getMonster().getLevel1Attack() + 
                           balance.getMonster().getAttackGrowthPerLevel() * level);
        int defense = (int) (level * balance.getMonster().getDefenseGrowthPerLevel());
        
        return new int[]{
            (int) (health * typeMultiplier),
            (int) (attack * typeMultiplier),
            (int) (defense * typeMultiplier)
        };
    }

    /**
     * 获取怪物类型倍率
     */
    private double getTypeMultiplier(String type) {
        if (type == null) return balance.getMonster().getNormalMultiplier();
        
        switch (type) {
            case "BOSS":
                return balance.getMonster().getBossMultiplier();
            case "精英":
                return balance.getMonster().getEliteMultiplier();
            default:
                return balance.getMonster().getNormalMultiplier();
        }
    }

    /**
     * 计算战斗掉落灵石
     * 公式：(10 + level × 2) × typeMultiplier × levelDiffFactor
     * 
     * @param monsterLevel 怪物等级
     * @param monsterType 怪物类型
     * @param playerLevel 玩家等级
     * @return 掉落灵石
     */
    public int calculateDropSpiritStones(int monsterLevel, String monsterType, int playerLevel) {
        double typeMultiplier = getTypeMultiplier(monsterType);
        
         // 等级差修正：怪物等级高于玩家时奖励增加，低于时减少
         int levelDiff = monsterLevel - playerLevel;
         double levelDiffFactor = 1.0 + levelDiff * 0.1; // 每级10%
         levelDiffFactor = Math.max(0.5, Math.min(2.0, levelDiffFactor)); // 限制在0.5-2.0之间
        
        int baseDrop = balance.getMonster().getBaseDropSpiritStones() + 
                       (int) (balance.getMonster().getDropSpiritStonesPerLevel() * monsterLevel);
        
        return (int) (baseDrop * typeMultiplier * levelDiffFactor);
    }

    /**
     * 计算宠物技能触发概率
     * 基于忠诚度：0-30:60%, 31-80:80%, 81-100:100%
     * 
     * @param loyalty 忠诚度
     * @return 触发概率（0.0-1.0）
     */
    public double calculatePetSkillTriggerChance(int loyalty) {
        if (loyalty <= balance.getPet().getLoyaltyLowThreshold()) {
            return 0.60;
        } else if (loyalty <= balance.getPet().getLoyaltyMediumThreshold()) {
            return 0.80;
        } else {
            return 1.00;
        }
    }

    /**
     * 计算宠物饱食度效果因子
     * 低于阈值时效果减半
     * 
     * @param hunger 饱食度
     * @return 效果因子（0.0-1.0）
     */
    public double calculatePetHungerFactor(int hunger) {
        return (hunger < balance.getPet().getHungerLowThreshold()) 
            ? balance.getPet().getHungerLowEffectFactor() 
            : 1.0;
    }

    /**
     * 计算宠物技能伤害
     * 公式：base_damage × (1 + level × 0.1) × loyalty_factor × hunger_factor
     * 
     * @param basePetDamage 宠物基础伤害
     * @param petLevel 宠物等级
     * @param loyalty 忠诚度
     * @param hunger 饱食度
     * @return 技能伤害
     */
    public int calculatePetSkillDamage(int basePetDamage, int petLevel, int loyalty, int hunger) {
        double levelFactor = 1.0 + petLevel * 0.1;
        double loyaltyFactor = (loyalty >= balance.getPet().getLoyaltyHighThreshold()) 
            ? balance.getPet().getLoyaltyHighDamageBonus()
            : (loyalty >= (balance.getPet().getLoyaltyLowThreshold() + balance.getPet().getLoyaltyMediumThreshold()) / 2 
                ? balance.getPet().getLoyaltyMediumDamageBonus() 
                : balance.getPet().getLoyaltyLowDamageBonus());
        double hungerFactor = calculatePetHungerFactor(hunger);
        
        return (int) (basePetDamage * levelFactor * loyaltyFactor * hungerFactor);
    }

    /**
     * 计算宠物技能冷却回合数
     * 公式：3 + speed / 10
     * 
     * @param petSpeed 宠物速度
     * @return 冷却回合数
     */
    public int calculatePetSkillCooldown(int petSpeed) {
        return balance.getPet().getBaseSkillCooldown() + petSpeed / balance.getPet().getSkillCooldownSpeedDivider();
    }

    /**
     * 判断宠物是否触发共鸣
     * 忠诚度>=81时有5%概率触发共鸣×2伤害
     * 
     * @param loyalty 忠诚度
     * @return 是否共鸣
     */
    public boolean isPetResonance(int loyalty) {
        return (loyalty >= balance.getPet().getLoyaltyHighThreshold()) && 
               (ThreadLocalRandom.current().nextDouble() < balance.getPet().getResonanceChance());
    }

    /**
     * 计算速度优势行动次数
     * 速度比>=2.0: 3次行动
     * 速度比>=1.5: 2次行动
     * 正常: 1次行动
     * 
     * @param playerSpeed 玩家速度
     * @param monsterSpeed 怪物速度
     * @return 行动次数
     */
    public int calculateSpeedAdvantageActions(int playerSpeed, int monsterSpeed) {
        double ratio = (double) playerSpeed / Math.max(1, monsterSpeed);
        
        if (ratio >= 2.0) {
            return 3;
        } else if (ratio >= balance.getCombat().getSpeedAdvantageThreshold()) {
            return 2;
        }
        return 1;
    }
}


