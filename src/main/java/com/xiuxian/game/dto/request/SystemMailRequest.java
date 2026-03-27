package com.xiuxian.game.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * 系统邮件发送请求
 * 封装 sendSystemMail 的6个参数
 *
 * @author xiuxian
 */
@Data
@Builder
public class SystemMailRequest {
    /** 玩家ID */
    private Integer playerId;
    /** 邮件标题 */
    private String title;
    /** 邮件内容 */
    private String content;
    /** 附件类型: SPIRIT_STONES/EXP/ITEM/EQUIPMENT (可为null) */
    private String itemType;
    /** 附件物品ID (可为null) */
    private Integer itemId;
    /** 附件数量 (可为null) */
    private Integer quantity;
}
