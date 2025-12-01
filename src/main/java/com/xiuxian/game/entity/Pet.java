package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物模板实体类
 * 对应数据库 pets 表
 */
@TableName("pets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pet {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    @TableField(value = "type")
    private String type; // 灵兽、妖兽、神兽

    @TableField(value = "rarity")
    @Builder.Default
    private Integer rarity = 1; // 1-普通, 2-稀有, 3-史诗, 4-传说, 5-神话

    @TableField(value = "base_attack")
    @Builder.Default
    private Integer baseAttack = 0;

    @TableField(value = "base_defense")
    @Builder.Default
    private Integer baseDefense = 0;

    @TableField(value = "base_health")
    @Builder.Default
    private Integer baseHealth = 0;

    @TableField(value = "base_speed")
    @Builder.Default
    private Integer baseSpeed = 0;

    @TableField(value = "growth_rate")
    @Builder.Default
    private BigDecimal growthRate = BigDecimal.ONE;

    @TableField(value = "skill_id")
    private Integer skillId;

    @TableField(value = "unlock_level")
    @Builder.Default
    private Integer unlockLevel = 1;

    @TableField(value = "capture_rate")
    @Builder.Default
    private BigDecimal captureRate = new BigDecimal("50.00");

    @TableField(value = "icon")
    private String icon;

    @TableField(value = "active")
    @Builder.Default
    private Boolean active = true;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 获取稀有度名称
     */
    public String getRarityName() {
        switch (rarity) {
            case 1: return "普通";
            case 2: return "稀有";
            case 3: return "史诗";
            case 4: return "传说";
            case 5: return "神话";
            default: return "未知";
        }
    }
}
