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

/**
 * 宠物训练日志实体类
 * 对应数据库 pet_training_logs 表
 */
@TableName("pet_training_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetTrainingLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_pet_id")
    private Integer playerPetId;

    @TableField(value = "training_type")
    private String trainingType; // 攻击、防御、速度

    @TableField(value = "exp_gained")
    @Builder.Default
    private Integer expGained = 0;

    @TableField(value = "attribute_improved")
    private String attributeImproved;

    @TableField(value = "improvement_value")
    @Builder.Default
    private Integer improvementValue = 0;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;
}
