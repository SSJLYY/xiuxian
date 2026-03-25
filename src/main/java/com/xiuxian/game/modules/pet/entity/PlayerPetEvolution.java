package com.xiuxian.game.modules.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 玩家宠物进化记录实体
 * 记录玩家宠物的进化状�?
 */
@Data
@TableName("player_pet_evolution")
public class PlayerPetEvolution {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 玩家宠物ID
     */
    private Integer playerPetId;

    /**
     * 当前进化阶段
     */
    private Integer currentStage;

    /**
     * 进化时间
     */
    private LocalDateTime evolvedAt;
}

