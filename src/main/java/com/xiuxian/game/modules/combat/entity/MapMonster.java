package com.xiuxian.game.modules.combat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 地图怪物配置实体
 * 对应 map_monsters 表
 *
 * @author LevelDesigner
 * @since 2026-03-23
 */
@Data
@TableName("map_monsters")
public class MapMonster {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 地图ID */
    private Integer mapId;

    /** 怪物ID */
    private Integer monsterId;

    /** 出现概率% */
    private BigDecimal spawnRate;

    /** 最小等级 */
    private Integer minLevel;

    /** 最大等级 */
    private Integer maxLevel;

    /** 是否为精英 */
    private Boolean isElite;

    /** 权重（用于随机选择） */
    private Integer spawnWeight;

    /** 遭遇描述文本 */
    private String encounterText;

    // 非数据库字段

    /** 怪物信息 */
    @TableField(exist = false)
    private Monster monster;

    /**
     * 根据玩家等级计算怪物实际等级
     */
    public int calculateLevel(int playerLevel, int mapBaseLevel) {
        // 动态等级计算
        int levelDiff = playerLevel - mapBaseLevel;
        int adjustedLevel = mapBaseLevel + (int)(levelDiff * 0.5);

        // 限制在最小和最大等级之间
        return Math.max(minLevel, Math.min(maxLevel, adjustedLevel));
    }

    /**
     * 计算怪物属性倍率
     */
    public double calculateStatMultiplier(int playerLevel, int recommendedLevel) {
        int levelDiff = playerLevel - recommendedLevel;

        if (levelDiff < -3) {
            // 新手保护
            return 0.8;
        } else if (levelDiff > 3) {
            // 挑战模式
            return 1.2;
        }
        // 标准难度
        return 1.0;
    }
}
