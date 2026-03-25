package com.xiuxian.game.common.exception;

/**
 * 错误码枚�?
 */
public enum ErrorCode {
    
    // 通用错误 1000-1999
    SUCCESS(0, "成功"),
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    UNAUTHORIZED(1002, "未授�?),
    FORBIDDEN(1003, "无权�?),
    NOT_FOUND(1004, "资源不存�?),

    // 用户/认证系统 1100-1199
    USER_NOT_FOUND(1100, "用户不存�?),
    USER_NOT_LOGIN(1101, "用户未登�?),
    USERNAME_ALREADY_EXISTS(1102, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(1103, "邮箱已被使用"),
    INVALID_CREDENTIALS(1104, "用户名或密码错误"),
    ACCOUNT_LOCKED(1105, "账号已被锁定"),
    REGISTER_FAILED(1106, "注册失败"),
    LOGIN_FAILED(1107, "登录失败"),
    
    // 邮件系统 2000-2099
    MAIL_NOT_FOUND(2000, "邮件不存�?),
    MAIL_ALREADY_CLAIMED(2001, "附件已领�?),
    MAIL_NO_ATTACHMENT(2002, "邮件无附�?),
    MAIL_BOX_FULL(2003, "邮箱已满"),
    MAIL_ACCESS_DENIED(2004, "无权访问该邮�?),
    
    // 公告系统 2100-2199
    ANNOUNCEMENT_NOT_FOUND(2100, "公告不存�?),
    ANNOUNCEMENT_EXPIRED(2101, "公告已过�?),
    
    // 排行榜系�?2200-2299
    RANKING_TYPE_INVALID(2200, "排行榜类型无�?),
    
    // 成就系统 2300-2399
    ACHIEVEMENT_NOT_FOUND(2300, "成就不存�?),
    ACHIEVEMENT_NOT_COMPLETED(2301, "成就未完�?),
    ACHIEVEMENT_ALREADY_CLAIMED(2302, "成就奖励已领�?),
    
    // 宗门系统 2400-2499
    GUILD_NAME_EXISTS(2400, "宗门名称已存�?),
    GUILD_NOT_FOUND(2401, "宗门不存�?),
    GUILD_FULL(2402, "宗门人数已满"),
    GUILD_ALREADY_JOINED(2403, "已加入其他宗�?),
    GUILD_NOT_MEMBER(2404, "未加入宗�?),
    INSUFFICIENT_GUILD_FUNDS(2405, "宗门资金不足"),
    GUILD_NO_PERMISSION(2406, "无宗门权�?),
    GUILD_APPLICATION_EXISTS(2407, "已有待处理的申请"),
    GUILD_APPLICATION_NOT_FOUND(2408, "申请不存�?),
    GUILD_APPLICATION_ALREADY_HANDLED(2409, "申请已被处理"),
    GUILD_LEADER_CANNOT_LEAVE(2410, "宗主不能退出宗门，请先转让宗主"),
    GUILD_INSUFFICIENT_CONTRIBUTION(2411, "宗门贡献不足"),
    GUILD_SKILL_NOT_FOUND(2412, "宗门技能不存在"),
    GUILD_SKILL_ALREADY_LEARNED(2413, "已学习该技�?),
    GUILD_LEVEL_NOT_ENOUGH(2414, "宗门等级不足"),
    
    // 拍卖行系�?2500-2599
    AUCTION_ITEM_NOT_FOUND(2500, "拍卖物品不存�?),
    AUCTION_ITEM_SOLD(2501, "物品已售�?),
    INSUFFICIENT_SPIRIT_STONES(2502, "灵石不足"),
    CANNOT_BUY_OWN_ITEM(2503, "不能购买自己的物�?),
    
    // VIP系统 2600-2699
    VIP_REWARD_ALREADY_CLAIMED(2600, "今日VIP奖励已领�?),
    RECHARGE_FAILED(2601, "充值失�?),
    RECHARGE_ORDER_NOT_FOUND(2602, "充值订单不存在"),
    RECHARGE_ORDER_STATUS_INVALID(2603, "充值订单状态异�?),
    
    // 活动系统 2700-2799
    ACTIVITY_NOT_FOUND(2700, "活动不存�?),
    ACTIVITY_NOT_STARTED(2701, "活动未开�?),
    ACTIVITY_ENDED(2702, "活动已结�?),
    ACTIVITY_REWARD_CLAIMED(2703, "活动奖励已领�?),
    
    // 礼包码系�?2800-2899
    GIFT_CODE_INVALID(2800, "礼包码无�?),
    GIFT_CODE_EXPIRED(2801, "礼包码已过期"),
    GIFT_CODE_USED(2802, "礼包码已使用"),
    GIFT_CODE_LEVEL_NOT_ENOUGH(2803, "等级不足"),
    
    // 商店系统 2850-2899
    SHOP_ITEM_NOT_FOUND(2850, "商品不存�?),
    SHOP_ITEM_NOT_AVAILABLE(2851, "商品不可�?),
    SHOP_ITEM_OUT_OF_STOCK(2852, "库存不足"),
    SHOP_INSUFFICIENT_SPIRIT_STONES(2853, "灵石不足"),
    SHOP_INSUFFICIENT_CONTRIBUTION(2854, "贡献点不�?),
    SHOP_SKILL_ALREADY_OWNED(2855, "已拥有该技�?),
    SHOP_SKILL_LEVEL_NOT_ENOUGH(2856, "等级不足，无法购买技�?),
    
    // 管理员系�?2900-2999
    ADMIN_OPERATION_FAILED(2900, "管理员操作失�?),
    PLAYER_NOT_FOUND(2901, "玩家不存�?),
    PLAYER_ALREADY_BANNED(2902, "玩家已被封禁"),
    PLAYER_CREATE_FAILED(2903, "创建玩家档案失败"),
    PLAYER_GET_FAILED(2904, "获取玩家档案失败"),

    // 叙事系统 3000-3099
    NPC_NOT_FOUND(3000, "NPC不存�?),
    DIALOGUE_NOT_FOUND(3001, "对话不存�?),
    DIALOGUE_PREREQUISITES_NOT_MET(3002, "不满足对话前置条�?),
    DIALOGUE_ALREADY_COMPLETED(3003, "对话已完�?),
    DIALOGUE_NOT_IN_PROGRESS(3004, "对话未在进行�?),
    LORE_NOT_FOUND(3005, "传说条目不存�?),
    LORE_ALREADY_DISCOVERED(3006, "传说已被发现"),
    
    // 地图/关卡系统 3100-3199
    MAP_NOT_FOUND(3100, "地图不存�?),
    MAP_NOT_UNLOCKED(3101, "地图未解�?),
    MAP_REQUIREMENTS_NOT_MET(3102, "不满足地图进入条�?),
    MAP_ALREADY_CURRENT(3103, "已在该地图中"),
    OFFLINE_HANGING_NOT_ALLOWED(3104, "当前地图不允许离线挂�?),
    PET_HUNGER_TOO_LOW(3105, "宠物饱食度过低，无法在高风险地图挂机"),

    // 宗门BOSS系统 3200-3299
    GUILD_BOSS_NOT_FOUND(3200, "宗门BOSS不存�?),
    GUILD_BOSS_ALREADY_DEFEATED(3201, "宗门BOSS已被击败，等待下次刷�?),
    GUILD_BOSS_DAILY_LIMIT_REACHED(3202, "今日挑战次数已用�?),
    GUILD_BOSS_NO_CONTRIBUTION(3203, "你未参与本次BOSS讨伐"),
    GUILD_BOSS_REWARD_CLAIMED(3204, "BOSS奖励已领�?),
    GUILD_BOSS_NOT_DEFEATED(3205, "BOSS尚未被击�?),

    // 签到系统 3300-3399
    CHECK_IN_ALREADY_DONE(3300, "今日已签�?),
    CHECK_IN_MAKEUP_NOT_ALLOWED(3301, "补签功能未开�?),

    // 限流/系统保护 3400-3499
    SERVER_BUSY(3400, "系统繁忙，请稍后重试"),
    RATE_LIMIT_EXCEEDED(3401, "请求过于频繁，请稍后重试"),
    SERVICE_DEGRADED(3402, "服务已降级，部分功能暂时不可�?),
    CACHE_UNAVAILABLE(3403, "缓存服务暂时不可�?);

    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

