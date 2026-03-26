package com.xiuxian.game.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 游戏平衡配置类
 *
 * 配置GDD文档中规定的各项游戏数值平衡参数，
 * 可在 application.yml 中覆盖默认值，实现热更新平衡参数。
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
     * 玩家初始属性配置
     */
    private PlayerInitialConfig playerInitial = new PlayerInitialConfig();

    /**
     * 战斗平衡配置
     */
    private CombatBalanceConfig combat = new CombatBalanceConfig();

    /**
     * 修炼平衡配置
     */
    private CultivationBalanceConfig cultivation = new CultivationBalanceConfig();

    /**
     * 怪物平衡配置
     */
    private MonsterBalanceConfig monster = new MonsterBalanceConfig();

    /**
     * 宠物平衡配置
     */
    private PetBalanceConfig pet = new PetBalanceConfig();

    /**
     * 经验曲线配置
     */
    private ExperienceCurveConfig experience = new ExperienceCurveConfig();

    /**
     * 任务奖励配置
     */
    private QuestRewardConfig quest = new QuestRewardConfig();

    /**
     * 玩家初始属性配置类
     */
    @Data
    public static class PlayerInitialConfig {
        /**
         * 初始生命值配置
         */
        private int health = 120;

        /**
         * 初始攻击力配置
         */
        private int attack = 12;

        /**
         * 初始防御力配置
         */
        private int defense = 6;

        /**
         * 初始灵石配置
         */
        private int spiritStones = 2000;

        /**
         * 初始法力配置
         */
        private int mana = 60;

        /**
         * 初始速度配置
         */
        private int speed = 10;

        /**
         * 初始经验配置
         */
        private long exp = 0L;
    }

    /**
     * 战斗平衡配置类
     */
    @Data
    public static class CombatBalanceConfig {
        /**
         * 基础暴击率配置
         */
        private double critRate = 0.05;

        /**
         * 暴击伤害倍率配置
         */
        private double critDamageMultiplier = 1.8;

        /**
         * 防御力减伤系数配置
         * 减伤比例 = defense / (defense + attackerLevel * defenseLevelMultiplier)
         */
        private double defenseLevelMultiplier = 10.0;

        /**
         * 速度优势阈值配置
         */
        private double speedAdvantageThreshold = 1.5;

        /**
         * 新手战斗保护回合数
         */
        private int newbieBattleProtection = 3;

        /**
         * 新手怪物削弱系数
         */
        private double newbieMonsterWeakFactor = 0.5;

        /**
         * 最大战斗回合数
         */
        private int maxRounds = 50;
    }

    /**
     * 修炼平衡配置类
     */
    @Data
    public static class CultivationBalanceConfig {
        /**
         * 每小时修炼消耗灵石数量
         */
        private int baseSpiritStonesPerHour = 25;

        /**
         * 各境界修炼效率加成
         */
        private double realmBonusQiCondensation = 1.0;
        private double realmBonusFoundation = 1.5;
        private double realmBonusGoldenCore = 2.5;
        private double realmBonusNascentSoul = 4.0;

        /**
         * 每秒修炼经验获取
         */
        private double expPerSecond = 1.0;

        /**
         * 灵石收益上限基础值
         */
        private long baseSpiritStonesLimit = 50000L;

        /**
         * 离线挂机奖励最大小时数
         */
        private int offlineRewardMaxHours = 24;
    }

    /**
     * 怪物平衡配置类
     */
    @Data
    public static class MonsterBalanceConfig {
        /**
         * 1级怪物生命值配置
         */
        private int level1Health = 80;

        /**
         * 1级怪物攻击力配置
         */
        private int level1Attack = 8;

        /**
         * 怪物生命成长系数
         */
        private double healthGrowthPerLevel = 15.0;

        /**
         * 怪物攻击成长系数
         */
        private double attackGrowthPerLevel = 2.0;

        /**
         * 怪物防御成长系数
         */
        private double defenseGrowthPerLevel = 1.0;

        /**
         * 怪物类型倍率配置
         */
        private double normalMultiplier = 0.8;
        private double eliteMultiplier = 1.3;
        private double bossMultiplier = 2.5;

        /**
         * 灵石掉落基础值
         */
        private int baseDropSpiritStones = 10;
        private double dropSpiritStonesPerLevel = 2.0;

        /**
         * 新手地图怪物削弱系数
         */
        private double newbieMapWeakFactor = 0.7;
    }

    /**
     * 宠物平衡配置类
     */
    @Data
    public static class PetBalanceConfig {
        /**
         * 每小时饥饿度下降值
         */
        private int hungerDecayPerHour = 2;

        /**
         * 训练消耗饥饿度
         */
        private int trainingHungerCost = 10;

        /**
         * 战斗消耗饥饿度
         */
        private int combatHungerCost = 3;

        /**
         * 喂食恢复饥饿度
         */
        private int feedingHungerRestore = 30;

        /**
         * 喂食增加忠诚度
         */
        private int feedingLoyaltyBoost = 5;

        /**
         * 饥饿度低阈值配置
         */
        private int hungerLowThreshold = 20;

        /**
         * 饥饿度低时效果系数
         */
        private double hungerLowEffectFactor = 0.5;

        /**
         * 忠诚度低阈值配置
         */
        private int loyaltyLowThreshold = 30;
        private int loyaltyMediumThreshold = 80;
        private int loyaltyHighThreshold = 81;

        /**
         * 忠诚度伤害加成系数
         */
        private double loyaltyHighDamageBonus = 1.25;
        private double loyaltyMediumDamageBonus = 1.1;
        private double loyaltyLowDamageBonus = 1.0;

        /**
         * 共鸣触发概率
         */
        private double resonanceChance = 0.05;

        /**
         * 共鸣伤害倍率
         */
        private double resonanceDamageMultiplier = 2.0;

        /**
         * 技能冷却时间配置
         */
        private int baseSkillCooldown = 3;
        private int skillCooldownSpeedDivider = 10;
    }

    /**
     * 经验曲线配置类
     */
    @Data
    public static class ExperienceCurveConfig {
        /**
         * 基础经验值配置
         */
        private long baseExp = 100L;

        /**
         * 等级经验指数配置
         */
        private double levelExponent = 1.5;

        /**
         * 技能经验基础值配置
         */
        private int attackSkillBaseExp = 100;
        private int defenseSkillBaseExp = 120;
        private int assistSkillBaseExp = 80;
        private int passiveSkillBaseExp = 200;

        /**
         * 技能等级指数配置
         */
        private double skillLevelExponent = 1.6;

        /**
         * 技能每级伤害加成
         */
        private double skillDamagePerLevel = 0.15;

        /**
         * 技能每级冷却缩减
         */
        private double skillCooldownReductionPerLevel = 0.3;

        /**
         * 技能每级法力消耗增加
         */
        private double skillManaCostPerLevel = 0.05;
    }

    /**
     * 任务奖励配置类
     */
    @Data
    public static class QuestRewardConfig {
        /**
         * 每日任务基础奖励
         */
        private int dailyBaseExp = 100;
        private int dailyBaseSpiritStones = 200;
        private int dailyBaseContributionPoints = 0;

        /**
         * 每周任务基础奖励
         */
        private int weeklyBaseExp = 500;
        private int weeklyBaseSpiritStones = 1000;
        private int weeklyBaseContributionPoints = 50;

        /**
         * 每月任务基础奖励
         */
        private int monthlyBaseExp = 2000;
        private int monthlyBaseSpiritStones = 5000;
        private int monthlyBaseContributionPoints = 300;

        /**
         * 连续7天签到额外奖励
         */
        private int streak7DaysBonusExp = 1000;
        private int streak7DaysBonusSpiritStones = 2000;
    }
}
