package com.xiuxian.game.modules.announcement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Data
@TableName("announcements")
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String content;

    private String announcementType; // SYSTEM/MAINTENANCE/ACTIVITY/UPDATE

    private Integer priority; // 0-普通 1-重要 2-紧急

    private String displayType; // POPUP/SCROLL/LIST

    private String status; // DRAFT/PUBLISHED/REVOKED

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
