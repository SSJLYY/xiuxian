package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("player_equipment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEquipment {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_id")
    private Integer playerId;

    @TableField(value = "equipment_id")
    private Integer equipmentId;

    @TableField(value = "slot")
    private String slot;

    @TableField(value = "is_equipped")
    @Builder.Default
    private Boolean equipped = false;

    @TableField(value = "durability")
    @Builder.Default
    private Integer durability = 100;

    @TableField(value = "max_durability")
    @Builder.Default
    private Integer maxDurability = 100;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}