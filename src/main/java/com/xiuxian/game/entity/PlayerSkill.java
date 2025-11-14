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

@TableName("player_skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSkill {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_id")
    private Integer playerId;

    @TableField(value = "skill_id")
    private Integer skillId;

    @TableField(value = "level")
    @Builder.Default
    private Integer level = 1;

    @TableField(value = "experience")
    @Builder.Default
    private Integer experience = 0;

    @TableField(value = "equipped")
    @Builder.Default
    private Boolean equipped = false;

    @TableField(value = "slot_number")
    @Builder.Default
    private Integer slotNumber = 0;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}