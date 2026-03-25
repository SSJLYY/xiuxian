package com.xiuxian.game.modules.pet.entity;

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
 * 玩家宠物技能实体类
 * 对应数据库 player_pet_skills 表
 */
@TableName("player_pet_skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPetSkill {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_pet_id")
    private Integer playerPetId;

    @TableField(value = "pet_skill_id")
    private Integer petSkillId;

    @TableField(value = "skill_level")
    @Builder.Default
    private Integer skillLevel = 1;

    @TableField(value = "learned_at")
    private LocalDateTime learnedAt;
}
