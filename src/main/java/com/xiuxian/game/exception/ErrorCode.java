package com.xiuxian.game.exception;

/**
 * 错误码枚举
 */
public enum ErrorCode {
    
    // 通用错误 1000-1999
    SUCCESS(0, "成功"),
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    UNAUTHORIZED(1002, "未授权"),
    FORBIDDEN(1003, "无权限"),
    NOT_FOUND(1004, "资源不存在"),
    
    // 邮件系统 2000-2099
    MAIL_NOT_FOUND(2000, "邮件不存在"),
    MAIL_ALREADY_CLAIMED(2001, "附件已领取"),
    MAIL_NO_ATTACHMENT(2002, "邮件无附件"),
    MAIL_BOX_FULL(2003, "邮箱已满"),
    MAIL_ACCESS_DENIED(2004, "无权访问该邮件"),
    
    // 公告系统 2100-2199
    ANNOUNCEMENT_NOT_FOUND(2100, "公告不存在"),
    ANNOUNCEMENT_EXPIRED(2101, "公告已过期"),
    
    // 排行榜系统 2200-2299
    RANKING_TYPE_INVALID(2200, "排行榜类型无效"),
    
    // 成就系统 2300-2399
    ACHIEVEMENT_NOT_FOUND(2300, "成就不存在"),
    ACHIEVEMENT_NOT_COMPLETED(2301, "成就未完成"),
    ACHIEVEMENT_ALREADY_CLAIMED(2302, "成就奖励已领取"),
    
    // 宗门系统 2400-2499
    GUILD_NAME_EXISTS(2400, "宗门名称已存在"),
    GUILD_NOT_FOUND(2401, "宗门不存在"),
    GUILD_FULL(2402, "宗门人数已满"),
    GUILD_ALREADY_JOINED(2403, "已加入其他宗门"),
    GUILD_NOT_MEMBER(2404, "未加入宗门"),
    INSUFFICIENT_GUILD_FUNDS(2405, "宗门资金不足"),
    GUILD_NO_PERMISSION(2406, "无宗门权限"),
    GUILD_APPLICATION_EXISTS(2407, "已有待处理的申请"),
    GUILD_APPLICATION_NOT_FOUND(2408, "申请不存在"),
    GUILD_APPLICATION_ALREADY_HANDLED(2409, "申请已被处理"),
    GUILD_LEADER_CANNOT_LEAVE(2410, "宗主不能退出宗门，请先转让宗主"),
    GUILD_INSUFFICIENT_CONTRIBUTION(2411, "宗门贡献不足"),
    GUILD_SKILL_NOT_FOUND(2412, "宗门技能不存在"),
    GUILD_SKILL_ALREADY_LEARNED(2413, "已学习该技能"),
    GUILD_LEVEL_NOT_ENOUGH(2414, "宗门等级不足"),
    
    // 拍卖行系统 2500-2599
    AUCTION_ITEM_NOT_FOUND(2500, "拍卖物品不存在"),
    AUCTION_ITEM_SOLD(2501, "物品已售出"),
    INSUFFICIENT_SPIRIT_STONES(2502, "灵石不足"),
    CANNOT_BUY_OWN_ITEM(2503, "不能购买自己的物品"),
    
    // VIP系统 2600-2699
    VIP_REWARD_ALREADY_CLAIMED(2600, "今日VIP奖励已领取"),
    RECHARGE_FAILED(2601, "充值失败"),
    
    // 活动系统 2700-2799
    ACTIVITY_NOT_FOUND(2700, "活动不存在"),
    ACTIVITY_NOT_STARTED(2701, "活动未开始"),
    ACTIVITY_ENDED(2702, "活动已结束"),
    ACTIVITY_REWARD_CLAIMED(2703, "活动奖励已领取"),
    
    // 礼包码系统 2800-2899
    GIFT_CODE_INVALID(2800, "礼包码无效"),
    GIFT_CODE_EXPIRED(2801, "礼包码已过期"),
    GIFT_CODE_USED(2802, "礼包码已使用"),
    GIFT_CODE_LEVEL_NOT_ENOUGH(2803, "等级不足"),
    
    // 管理员系统 2900-2999
    ADMIN_OPERATION_FAILED(2900, "管理员操作失败"),
    PLAYER_NOT_FOUND(2901, "玩家不存在"),
    PLAYER_ALREADY_BANNED(2902, "玩家已被封禁");
    
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
