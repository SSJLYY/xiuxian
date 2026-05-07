package com.xiuxian.game.modules.mail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 邮件附件实体类
 */
@Data
@TableName("mail_attachments")
public class MailAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long mailId;

    private String itemType; // ITEM/EQUIPMENT/SPIRIT_STONES/EXP

    private Integer itemId;

    private Integer quantity;

    @TableField(exist = false)
    private String itemName;
}
