package com.xiuxian.game.modules.combat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 怪物实体
 *
 * <p>对应 monsters 表，存储怪物模板数据。</p>
 *
 * <p>怪物类型：</p>
 * <ul>
 *   <li>普通 - 普通怪物</li>
 *   <li>精英 - 精英怪物，属性更强</li>
 *   <li>BOSS - BOSS怪物，最强属性</li>
 * </ul>
 *
 * <p>属性说明：</p>
 * <ul>
 *   <li>health - 生命值</li>
 *   <li>attack - 攻击力</li>
 *   <li>defense - 防御力</li>
 *   <li>speed - 速度</li>
 *   <li>expReward - 经验奖励</li>
 *   <li>spiritStonesReward - 灵石奖励</li>
 *   <li>dropRate - 掉落率（百分比）</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@TableName("monsters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Monster {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    @TableField(value = "level")
    @Builder.Default
    private Integer level = 1;

    @TableField(value = "type")
    @Builder.Default
    private String type = "普通"; // 普通、精英、BOSS

    @TableField(value = "health")
    @Builder.Default
    private Integer health = 100;

    @TableField(value = "attack")
    @Builder.Default
    private Integer attack = 10;

    @TableField(value = "defense")
    @Builder.Default
    private Integer defense = 5;

    @TableField(value = "speed")
    @Builder.Default
    private Integer speed = 10;

    @TableField(value = "exp_reward")
    @Builder.Default
    private Integer expReward = 50;

    @TableField(value = "spirit_stones_reward")
    @Builder.Default
    private Integer spiritStonesReward = 10;

    @TableField(value = "drop_rate")
    @Builder.Default
    private Integer dropRate = 10; // 掉落率 百分比

    @TableField(value = "drop_equipment_id")
    private Integer dropEquipmentId; // 可能掉落的装备ID

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    // 业务方法
    public boolean isBoss() {
        return "BOSS".equals(type);
    }

    public boolean isElite() {
        return "精英".equals(type);
    }

    public int getPowerRating() {
        return attack * 2 + defense + health / 10;
    }
}
