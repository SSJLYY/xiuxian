package com.xiuxian.game.util;

public class GameConstants {

    // 修炼相关常量
    public static final long BASE_EXP_PER_SECOND = 10L;
    public static final long BASE_SPIRIT_STONES_PER_SECOND = 1L;
    public static final long MAX_OFFLINE_TIME_SECONDS = 24 * 60 * 60; // 24小时
    public static final long MIN_OFFLINE_TIME_FOR_REWARD = 60; // 1分钟

    // 等级相关常量
    public static final int BASE_EXP_PER_LEVEL = 100;
    public static final int REALM_CHANGE_INTERVAL = 100; // 每100级提升一个境界

    // 境界配置
    public static final String[] REALMS = {
        "练气期", "筑基期", "金丹期", "元婴期", 
        "化神期", "合体期", "大乘期", "渡劫期"
    };

    public static final int[] REALM_LEVEL_THRESHOLDS = {
        1, 101, 201, 401, 701, 1001, 1501, 2001
    };

    // 装备相关常量
    public static final String[] EQUIPMENT_SLOTS = {
        "weapon", "armor", "helmet", "boots", "gloves", "accessory"
    };

    public static final int[] EQUIPMENT_QUALITY_COLORS = {
        0x9CA3AF, // 灰色 (普通)
        0x22C55E, // 绿色 (优秀)
        0x3B82F6, // 蓝色 (稀有)
        0xA855F7, // 紫色 (史诗)
        0xF59E0B, // 橙色 (传说)
        0xEF4444  // 红色 (神话)
    };

    // 技能相关常量
    public static final String[] SKILL_TYPES = {
        "cultivation", "attack", "defense", "heal", "buff", "debuff"
    };

    public static final String[] ELEMENTS = {
        "无", "金", "木", "水", "火", "土", "风", "雷", "冰", "光", "暗"
    };

    // 物品相关常量
    public static final String[] ITEM_TYPES = {
        "pill", "material", "equipment", "treasure", "quest_item"
    };

    public static final int DEFAULT_STACK_SIZE = 99;

    // 任务相关常量
    public static final String[] QUEST_TYPES = {
        "MAIN", "DAILY", "WEEKLY", "ACHIEVEMENT", "EVENT"
    };

    public static final String[] QUEST_STATUS = {
        "NOT_STARTED", "IN_PROGRESS", "COMPLETED", "REWARDED"
    };

    // 商店相关常量
    public static final String[] SHOP_TYPES = {
        "general", "equipment", "materials", "special"
    };

    // 成就相关常量
    public static final String[] ACHIEVEMENT_TYPES = {
        "level", "realm", "cultivation_time", "equipment", "skill", "quest"
    };

    // 游戏配置
    public static final int MAX_PLAYER_LEVEL = 9999;
    public static final int MAX_SKILL_LEVEL = 100;
    public static final int MAX_EQUIPMENT_ENHANCE_LEVEL = 20;
    public static final int MAX_INVENTORY_SLOTS = 200;

    // 错误消息
    public static final String ERROR_PLAYER_NOT_FOUND = "玩家不存在";
    public static final String ERROR_INSUFFICIENT_RESOURCES = "资源不足";
    public static final String ERROR_INVALID_OPERATION = "无效操作";
    public static final String ERROR_ITEM_NOT_FOUND = "物品不存在";
    public static final String ERROR_SKILL_NOT_FOUND = "技能不存在";
    public static final String ERROR_EQUIPMENT_NOT_FOUND = "装备不存在";
    public static final String ERROR_QUEST_NOT_FOUND = "任务不存在";
    public static final String ERROR_ALREADY_COMPLETED = "已完成";
    public static final String ERROR_REQUIREMENTS_NOT_MET = "条件不满足";

    // 成功消息
    public static final String SUCCESS_CULTIVATION = "修炼成功";
    public static final String SUCCESS_LEVEL_UP = "升级成功";
    public static final String SUCCESS_REALM_BREAKTHROUGH = "境界突破成功";
    public static final String SUCCESS_SKILL_LEARNED = "技能学习成功";
    public static final String SUCCESS_SKILL_UPGRADED = "技能升级成功";
    public static final String SUCCESS_EQUIPMENT_EQUIPPED = "装备成功";
    public static final String SUCCESS_ITEM_USED = "物品使用成功";
    public static final String SUCCESS_QUEST_COMPLETED = "任务完成";
    public static final String SUCCESS_PURCHASE = "购买成功";
}
