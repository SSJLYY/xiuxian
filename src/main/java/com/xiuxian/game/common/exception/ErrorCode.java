package com.xiuxian.game.common.exception;

/**
 * 错误码枚举
 */
public enum ErrorCode {

    // 通用错误码 1000-1999
    SUCCESS(0, "成功"),
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    UNAUTHORIZED(1002, "未授权"),
    FORBIDDEN(1003, "禁止访问"),
    NOT_FOUND(1004, "资源不存在"),

    // 用户/玩家相关错误码 1100-1199
    USER_NOT_FOUND(1100, "用户不存在"),
    USER_NOT_LOGIN(1101, "用户未登录"),
    USERNAME_ALREADY_EXISTS(1102, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(1103, "邮箱已被注册"),
    INVALID_CREDENTIALS(1104, "用户名或密码错误"),
    ACCOUNT_LOCKED(1105, "账号已被锁定"),
    REGISTER_FAILED(1106, "注册失败"),
    LOGIN_FAILED(1107, "登录失败"),

    // 邮件相关错误码 2000-2099
    MAIL_NOT_FOUND(2000, "邮件不存在"),
    MAIL_ALREADY_CLAIMED(2001, "邮件已领取"),
    MAIL_NO_ATTACHMENT(2002, "邮件无附件"),
    MAIL_BOX_FULL(2003, "邮箱已满"),
    MAIL_ACCESS_DENIED(2004, "无权访问该邮件"),

    // 公告相关错误码 2100-2199
    ANNOUNCEMENT_NOT_FOUND(2100, "公告不存在"),
    ANNOUNCEMENT_EXPIRED(2101, "公告已过期"),

    // 排行榜相关错误码 2200-2299
    RANKING_TYPE_INVALID(2200, "排行榜类型无效"),

    // 成就相关错误码 2300-2399
    ACHIEVEMENT_NOT_FOUND(2300, "成就不存在"),
    ACHIEVEMENT_NOT_COMPLETED(2301, "成就未完成"),
    ACHIEVEMENT_ALREADY_CLAIMED(2302, "成就奖励已领取"),

    // 公会相关错误码 2400-2499
    GUILD_NAME_EXISTS(2400, "公会名称已存在"),
    GUILD_NOT_FOUND(2401, "公会不存在"),
    GUILD_FULL(2402, "公会人数已满"),
    GUILD_ALREADY_JOINED(2403, "玩家已加入公会"),
    GUILD_NOT_MEMBER(2404, "不是公会成员"),
    INSUFFICIENT_GUILD_FUNDS(2405, "公会资金不足"),
    GUILD_NO_PERMISSION(2406, "无公会操作权限"),
    GUILD_APPLICATION_EXISTS(2407, "已有待处理的申请"),
    GUILD_APPLICATION_NOT_FOUND(2408, "申请不存在"),
    GUILD_APPLICATION_ALREADY_HANDLED(2409, "申请已处理"),
    GUILD_LEADER_CANNOT_LEAVE(2410, "会长不能直接退出公会，请先转让会长职位"),
    GUILD_INSUFFICIENT_CONTRIBUTION(2411, "公会贡献不足"),
    GUILD_SKILL_NOT_FOUND(2412, "公会技能不存在"),
    GUILD_SKILL_ALREADY_LEARNED(2413, "玩家已学习该技能"),
    GUILD_LEVEL_NOT_ENOUGH(2414, "公会等级不足"),

    // 拍卖行相关错误码 2500-2599
    AUCTION_ITEM_NOT_FOUND(2500, "拍卖物品不存在"),
    AUCTION_ITEM_SOLD(2501, "物品已售出"),
    INSUFFICIENT_SPIRIT_STONES(2502, "灵石不足"),
    CANNOT_BUY_OWN_ITEM(2503, "不能购买自己的物品"),

    // VIP相关错误码 2600-2699
    VIP_REWARD_ALREADY_CLAIMED(2600, "VIP奖励已领取"),
    RECHARGE_FAILED(2601, "充值失败"),
    RECHARGE_ORDER_NOT_FOUND(2602, "充值订单不存在"),
    RECHARGE_ORDER_STATUS_INVALID(2603, "充值订单状态无效"),

    // 活动相关错误码 2700-2799
    ACTIVITY_NOT_FOUND(2700, "活动不存在"),
    ACTIVITY_NOT_STARTED(2701, "活动未开始"),
    ACTIVITY_ENDED(2702, "活动已结束"),
    ACTIVITY_REWARD_CLAIMED(2703, "活动奖励已领取"),

    // 礼包码相关错误码 2800-2899
    GIFT_CODE_INVALID(2800, "礼包码无效"),
    GIFT_CODE_EXPIRED(2801, "礼包码已过期"),
    GIFT_CODE_USED(2802, "礼包码已使用"),
    GIFT_CODE_LEVEL_NOT_ENOUGH(2803, "等级不足"),

    // 商店相关错误码 2850-2899
    SHOP_ITEM_NOT_FOUND(2850, "商品不存在"),
    SHOP_ITEM_NOT_AVAILABLE(2851, "商品不可购买"),
    SHOP_ITEM_OUT_OF_STOCK(2852, "商品库存不足"),
    SHOP_INSUFFICIENT_SPIRIT_STONES(2853, "灵石不足"),
    SHOP_INSUFFICIENT_CONTRIBUTION(2854, "贡献值不足"),
    SHOP_SKILL_ALREADY_OWNED(2855, "玩家已拥有该技能"),
    SHOP_SKILL_LEVEL_NOT_ENOUGH(2856, "等级不足，无法购买该技能"),

    // 管理员操作错误码 2900-2999
    ADMIN_OPERATION_FAILED(2900, "管理员操作失败"),
    PLAYER_NOT_FOUND(2901, "玩家不存在"),
    PLAYER_ALREADY_BANNED(2902, "玩家已被封禁"),
    PLAYER_CREATE_FAILED(2903, "创建玩家失败"),
    PLAYER_GET_FAILED(2904, "获取玩家失败"),

    // NPC相关错误码 3000-3099
    NPC_NOT_FOUND(3000, "NPC不存在"),
    DIALOGUE_NOT_FOUND(3001, "对话不存在"),
    DIALOGUE_PREREQUISITES_NOT_MET(3002, "未满足对话前置条件"),
    DIALOGUE_ALREADY_COMPLETED(3003, "对话已完成"),
    DIALOGUE_NOT_IN_PROGRESS(3004, "对话未进行中"),
    LORE_NOT_FOUND(3005, "世界观条目不存在"),
    LORE_ALREADY_DISCOVERED(3006, "世界观条目已发现"),

    // 地图、离线挂机相关错误码 3100-3199
    MAP_NOT_FOUND(3100, "地图不存在"),
    MAP_NOT_UNLOCKED(3101, "地图未解锁"),
    MAP_REQUIREMENTS_NOT_MET(3102, "未满足地图解锁条件"),
    MAP_ALREADY_CURRENT(3103, "玩家已在该地图"),
    OFFLINE_HANGING_NOT_ALLOWED(3104, "当前地图不允许离线挂机"),
    PET_HUNGER_TOO_LOW(3105, "宠物饥饿度过低，请先喂食宠物再进行离线挂机"),
    PET_NOT_FOUND(3106, "宠物不存在"),
    PET_CAPTURE_FAILED(3107, "宠物捕获失败"),
    PET_ALREADY_MAX(3108, "宠物数量已达上限"),
    PET_LEVEL_LOCKED(3109, "宠物等级不足"),

    // 公会BOSS相关错误码 3200-3299
    GUILD_BOSS_NOT_FOUND(3200, "公会BOSS不存在"),
    GUILD_BOSS_ALREADY_DEFEATED(3201, "公会BOSS今日已被击败，明日重置"),
    GUILD_BOSS_DAILY_LIMIT_REACHED(3202, "今日挑战次数已达上限"),
    GUILD_BOSS_NO_CONTRIBUTION(3203, "未对BOSS造成伤害"),
    GUILD_BOSS_REWARD_CLAIMED(3204, "BOSS奖励已领取"),
    GUILD_BOSS_NOT_DEFEATED(3205, "BOSS尚未被击败"),

    // 签到相关错误码 3300-3399
    CHECK_IN_ALREADY_DONE(3300, "今日已签到"),
    CHECK_IN_MAKEUP_NOT_ALLOWED(3301, "补签不可用"),

    // 系统/限流相关错误码 3400-3499
    SERVER_BUSY(3400, "服务器繁忙，请稍后重试"),
    RATE_LIMIT_EXCEEDED(3401, "请求频率超限，请稍后重试"),
    SERVICE_DEGRADED(3402, "服务降级，功能暂时不可用"),
    CACHE_UNAVAILABLE(3403, "缓存服务不可用");

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
