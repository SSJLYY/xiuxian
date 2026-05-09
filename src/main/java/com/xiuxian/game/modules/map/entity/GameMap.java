package com.xiuxian.game.modules.map.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.xiuxian.game.common.util.RealmUtil;
import com.xiuxian.game.modules.combat.entity.MapMonster;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏地图实体
 * 对应 game_maps 表
 *
 * @author LevelDesigner
 * @since 2026-03-23
 */
@Data
@TableName("game_maps")
public class GameMap {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 地图名称 */
    private String name;

    /** 地图描述 */
    private String description;

    /** 所属区域 */
    private String region;

    /** 地图类型：SAFE/NORMAL/DANGEROUS/DUNGEON/BOSS */
    private String mapType;

    /** 需求等级 */
    private Integer requiredLevel;

    /** 需求境界 */
    private String requiredRealm;

    /** 解锁条件描述 */
    private String unlockCondition;

    /** 前置地图ID */
    private Integer prevMapId;

    /** 基础灵石收益/小时 */
    private Integer baseSpiritStones;

    /** 经验倍率 */
    private BigDecimal expModifier;

    /** 危险等级1-5 */
    private Integer dangerLevel;

    /** 离线是否有风险 */
    private Boolean offlineRisk;

    /** 主题颜色 */
    private String themeColor;

    /** 环境氛围文本 */
    private String ambienceText;

    /** 进入场景文本 */
    private String enterText;

    /** 胜利场景文本 */
    private String victoryText;

    /** 地图坐标X */
    private Integer positionX;

    /** 地图坐标Y */
    private Integer positionY;

    /** 是否启用 */
    private Boolean active;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 非数据库字段

    /** 前置地图名称（查询用） */
    @TableField(exist = false)
    private String prevMapName;

    /** 是否已解锁（玩家视角） */
    @TableField(exist = false)
    private Boolean unlocked;

    /** 是否为当前地图（玩家视角） */
    @TableField(exist = false)
    private Boolean current;

    /** 怪物配置列表 */
    @TableField(exist = false)
    private List<MapMonster> monsters;

    /**
     * 地图类型枚举
     */
    public static class MapType {
        public static final String SAFE      = "SAFE";      // 安全区
        public static final String NORMAL    = "NORMAL";    // 普通区
        public static final String DANGEROUS = "DANGEROUS"; // 危险区
        public static final String DUNGEON   = "DUNGEON";   // 副本
        public static final String BOSS      = "BOSS";      // BOSS区
    }

    /**
     * 检查玩家是否满足进入条件
     */
    public boolean canEnter(int playerLevel, String playerRealm) {
        if (playerLevel < defaultInt(requiredLevel, 1)) {
            return false;
        }
        if (requiredRealm != null && !requiredRealm.isEmpty()) {
            // 境界比较逻辑
            return RealmUtil.isGreaterOrEqual(playerRealm, requiredRealm);
        }
        return true;
    }

    /**
     * 获取危险等级图标
     */
    public String getDangerIcon() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < defaultInt(dangerLevel, 0); i++) {
            sb.append("⚠️");
        }
        return sb.toString();
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
