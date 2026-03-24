package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 玩家地图进度实体
 * 对应 player_map_progress 表
 * 
 * @author LevelDesigner
 * @since 2026-03-23
 */
@Data
@TableName("player_map_progress")
public class PlayerMapProgress {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 玩家ID */
    private Integer playerId;
    
    /** 地图ID */
    private Integer mapId;
    
    /** 是否已解锁 */
    private Boolean isUnlocked;
    
    /** 是否为当前所在地图 */
    private Boolean isCurrent;
    
    /** 首次进入时间 */
    private LocalDateTime firstEnterAt;
    
    /** 最后进入时间 */
    private LocalDateTime lastEnterAt;
    
    /** 累计击杀数 */
    private Integer totalKills;
    
    /** 累计停留时间(分钟) */
    private Integer totalTimeSpent;
    
    /** 离线挂机开始时间 */
    private LocalDateTime offlineStartAt;
    
    // 非数据库字段
    
    /** 地图信息 */
    @TableField(exist = false)
    private GameMap gameMap;
    
    /**
     * 记录进入地图
     */
    public void recordEnter() {
        this.isCurrent = true;
        this.lastEnterAt = LocalDateTime.now();
        if (this.firstEnterAt == null) {
            this.firstEnterAt = LocalDateTime.now();
        }
    }
    
    /**
     * 记录离开地图
     */
    public void recordLeave() {
        this.isCurrent = false;
        if (this.lastEnterAt != null) {
            int minutes = (int) java.time.Duration.between(
                this.lastEnterAt, LocalDateTime.now()).toMinutes();
            this.totalTimeSpent += minutes;
        }
    }
    
    /**
     * 开始离线挂机
     */
    public void startOffline() {
        this.offlineStartAt = LocalDateTime.now();
    }
    
    /**
     * 计算离线时长（小时）
     */
    public int calculateOfflineHours() {
        if (this.offlineStartAt == null) {
            return 0;
        }
        return (int) java.time.Duration.between(
            this.offlineStartAt, LocalDateTime.now()).toHours();
    }
    
    /**
     * 增加击杀数
     */
    public void addKill() {
        this.totalKills++;
    }
}
