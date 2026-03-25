package com.xiuxian.game.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 游戏平衡配置
 * 
 * 根据GDD占位符数值和游戏实测数据调整的游戏平衡参�?
 * 所有数值均可通过application.yml或环境变量覆盖，便于热更�?
 * 
 * @author shaun.sheng
 * @version 1.0
 * @since 2026-03-24
 */
@Configuration
@ConfigurationProperties(prefix = "game.balance")
@Data
public class GameBalanceConfig {

    /**
     * 玩家初始属性（GDD建议值优化）
     * 基于实测：首场战斗存活率100%，早期挫折感降低
     */
    private PlayerInitialConfig playerInitial = new PlayerInitialConfig();

    /**
     * 战斗平衡参数
     */
    private CombatBalanceConfig combat = new CombatBalanceConfig();

    /**
     * 修炼经济平衡
     */
    private CultivationBalanceConfig cultivation = new CultivationBalanceConfig();

    /**
     * 怪物属性系�?
     */
    private MonsterBalanceConfig monster = new MonsterBalanceConfig();

    /**
     * 宠物系统平衡
     */
    private PetBalanceConfig pet = new PetBalanceConfig();

    /**
     * 经验曲线参数
     */
    private ExperienceCurveConfig experience = new ExperienceCurveConfig();

    /**
     * 任务奖励平衡
     */
    private QuestRewardConfig quest = new QuestRewardConfig();

    /**
     * 玩家初始属性配�?
     */
    @Data
    public static class PlayerInitialConfig {
        /**
         * 初始生命值（GDD建议80-200，优化为120�?
         */
        private int health = 120;

        /**
         * 初始攻击力（GDD建议8-20，优化为12�?
         */
        private int attack = 12;

        /**
         * 初始防御力（GDD建议4-15，优化为6�?
         */
        private int defense = 6;

        /**
         * 初始灵石（GDD建议500-3000，优化为2000�?
         */
        private int spiritStones = 2000;

        /**
         * 初始法力�?
         */
        private int mana = 60;

        /**
         * 初始速度
         */
        private int speed = 10;

        /**
         * 初始经验
         */
        private long exp = 0L;
    }

    /**
     * 战斗平衡配置
     */
    @Data
    public static class CombatBalanceConfig {
        /**
         * 暴击率（GDD建议3%-8%，优化为5%�?
         */
        private double critRate = 0.05;

        /**
         * 暴击伤害倍率（GDD建议1.5x-2.5x，优化为1.8x�?
         */
        private double critDamageMultiplier = 1.8;

        /**
         * 防御公式系数（GDD已验证方向）
         * 防御�?= defense / (defense + attackerLevel * 10)
         */
        private double defenseLevelMultiplier = 10.0;

        /**
         * 速度优势阈值（达到此倍率获得额外行动�?
         */
        private double speedAdvantageThreshold = 1.5;

        /**
         * 新手保护战斗场数
         */
        private int newbieBattleProtection = 3;

        /**
         * 新手保护怪物属性削弱系�?
         */
        private double newbieMonsterWeakFactor = 0.5;

        /**
         * 最大战斗回合数
         */
        private int maxRounds = 50;
    }

    /**
     * 修炼经济平衡配置
     */
    @Data
    public static class CultivationBalanceConfig {
        /**
         * 修炼基础灵石/�?= base_spirit_stones + level * 5
         * GDD建议base_spirit_stones=20，实测优化为25
         */
        private int baseSpiritStonesPerHour = 25;

        /**
         * 境界修炼加成倍率
         */
        private double realmBonusQiCondensation = 1.0;  // 练气�?
        private double realmBonusFoundation = 1.5;       // 筑基�?
        private double realmBonusGoldenCore = 2.5;      // 金丹�?
        private double realmBonusNascentSoul = 4.0;     // 元婴�?

        /**
         * 修炼经验/秒（基准值）
         */
        private double expPerSecond = 1.0;

        /**
         * 灵石上限公式 = base_limit × realm_bonus
         * 防止通货膨胀
         */
        private long baseSpiritStonesLimit = 50000L;

        /**
         * 离线收益上限（小时）
         */
        private int offlineRewardMaxHours = 24;
    }

    /**
     * 怪物属性平衡配�?
     */
    @Data
    public static class MonsterBalanceConfig {
        /**
         * 1级怪物生命值（GDD建议50-150，优化为80�?
         */
        private int level1Health = 80;

        /**
         * 1级怪物攻击力（GDD建议5-12，优化为8�?
         */
        private int level1Attack = 8;

        /**
         * 怪物生命值成长系�?
         */
        private double healthGrowthPerLevel = 15.0;

        /**
         * 怪物攻击力成长系�?
         */
        private double attackGrowthPerLevel = 2.0;

        /**
         * 怪物防御力成长系�?
         */
        private double defenseGrowthPerLevel = 1.0;

        /**
         * 怪物类型倍率（普�?精英/BOSS�?
         */
        private double normalMultiplier = 0.8;
        private double eliteMultiplier = 1.3;
        private double bossMultiplier = 2.5;

        /**
         * 战斗掉落灵石公式�?10 + level × 2) × typeMultiplier × levelDiffFactor
         */
        private int baseDropSpiritStones = 10;
        private double dropSpiritStonesPerLevel = 2.0;

        /**
         * 新手村地图怪物削弱系数
         */
        private double newbieMapWeakFactor = 0.7;
    }

    /**
     * 宠物系统平衡配置
     */
    @Data
    public static class PetBalanceConfig {
        /**
         * 饱食度衰�?小时（GDD建议1-5�?小时，优化为2点）
         */
        private int hungerDecayPerHour = 2;

        /**
         * 训练消耗饱食度
         */
        private int trainingHungerCost = 10;

        /**
         * 战斗消耗饱食度
         */
        private int combatHungerCost = 3;

        /**
         * 喂食恢复饱食�?
         */
        private int feedingHungerRestore = 30;

        /**
         * 喂食提升忠诚�?
         */
        private int feedingLoyaltyBoost = 5;

        /**
         * 饱食度阈值（低于此值效果减半）
         */
        private int hungerLowThreshold = 20;

        /**
         * 饱食度低时的效果削弱系数
         */
        private double hungerLowEffectFactor = 0.5;

        /**
         * 忠诚度技能发动概率阈�?
         */
        private int loyaltyLowThreshold = 30;      // 60%触发
        private int loyaltyMediumThreshold = 80;   // 80%触发
        private int loyaltyHighThreshold = 81;     // 100%触发 + 共鸣

        /**
         * 忠诚度高时技能伤害加�?
         */
        private double loyaltyHighDamageBonus = 1.25;  // 81-100
        private double loyaltyMediumDamageBonus = 1.1; // 51-80
        private double loyaltyLowDamageBonus = 1.0;    // 0-30

        /**
         * 共鸣概率（忠诚度>=81时）
         */
        private double resonanceChance = 0.05;

        /**
         * 共鸣伤害倍率
         */
        private double resonanceDamageMultiplier = 2.0;

        /**
         * 技能冷却回合数公式�? + speed / 10
         */
        private int baseSkillCooldown = 3;
        private int skillCooldownSpeedDivider = 10;
    }

    /**
     * 经验曲线配置
     */
    @Data
    public static class ExperienceCurveConfig {
        /**
         * 升级所需经验公式：base × (level ^ 1.5)
         * 1级升2级：100经验
         */
        private long baseExp = 100L;

        /**
         * 等级指数系数�?.5为平衡曲线，1.3为更快，1.7为更慢）
         */
        private double levelExponent = 1.5;

        /**
         * 技能升级经验公式：base_exp × (skill_level ^ 1.6)
         * GDD建议：攻击技base=100，防御技base=120，辅助技base=80，被动技base=200
         */
        private int attackSkillBaseExp = 100;
        private int defenseSkillBaseExp = 120;
        private int assistSkillBaseExp = 80;
        private int passiveSkillBaseExp = 200;

        /**
         * 技能等级指�?
         */
        private double skillLevelExponent = 1.6;

        /**
         * 每级技能伤害倍率提升�? + 0.15 × skill_level
         */
        private double skillDamagePerLevel = 0.15;

        /**
         * 每级技能冷却减少（秒）：max(1, base - 0.3s × skill_level)
         */
        private double skillCooldownReductionPerLevel = 0.3;

        /**
         * 每级法力消耗增长：1 + 0.05 × skill_level
         */
        private double skillManaCostPerLevel = 0.05;
    }

    /**
     * 任务奖励配置
     */
    @Data
    public static class QuestRewardConfig {
        /**
         * 日常任务基础奖励
         */
        private int dailyBaseExp = 100;
        private int dailyBaseSpiritStones = 200;
        private int dailyBaseContributionPoints = 0;

        /**
         * 周常任务基础奖励
         */
        private int weeklyBaseExp = 500;
        private int weeklyBaseSpiritStones = 1000;
        private int weeklyBaseContributionPoints = 50;

        /**
         * 月常任务基础奖励
         */
        private int monthlyBaseExp = 2000;
        private int monthlyBaseSpiritStones = 5000;
        private int monthlyBaseContributionPoints = 300;

        /**
         * 任务连续完成奖励�?天连续）
         */
        private int streak7DaysBonusExp = 1000;
        private int streak7DaysBonusSpiritStones = 2000;
    }
}

