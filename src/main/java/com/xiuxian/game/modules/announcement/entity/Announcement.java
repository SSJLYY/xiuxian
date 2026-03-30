package com.xiuxian.game.modules.announcement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告实体类
 *
 * <p>存储游戏公告信息，包括公告标题、内容、类型、优先级等。</p>
 *
 * <p>公告类型：</p>
 * <ul>
 *   <li>SYSTEM - 系统公告</li>
 *   <li>MAINTENANCE - 维护公告</li>
 *   <li>ACTIVITY - 活动公告</li>
 *   <li>UPDATE - 更新公告</li>
 * </ul>
 *
 * <p>优先级：</p>
 * <ul>
 *   <li>0 - 普通</li>
 *   <li>1 - 重要</li>
 *   <li>2 - 紧急</li>
 * </ul>
 *
 * <p>显示类型：</p>
 * <ul>
 *   <li>POPUP - 弹窗公告</li>
 *   <li>SCROLL - 滚动公告</li>
 *   <li>LIST - 列表公告</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
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
