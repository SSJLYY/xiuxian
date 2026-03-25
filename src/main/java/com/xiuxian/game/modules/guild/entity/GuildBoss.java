package com.xiuxian.game.modules.guild.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 宗门BOSS实体
 * 宗门成员协作击败的强力BOSS，每周刷新一�?
 */
@Data
@TableName("guild_bosses")
public class GuildBoss {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** BOSS名称 */
    private String name;

    /** BOSS描述 */
    private String description;

    /** BOSS等级 */
    private Integer level;

    /** BOSS最大生命�?*/
    @TableField(value = "max_health")
    private Long maxHealth;

    /** BOSS当前生命�?*/
    @TableField(value = "current_health")
    private Long currentHealth;

    /** BOSS攻击�?*/
    private Integer attack;

    /** BOSS防御�?*/
    private Integer defense;

    /** 所属宗门ID */
    @TableField(value = "guild_id")
    private Integer guildId;

    /** BOSS状�? ALIVE/DEFEATED */
    private String status;

    /** 掉落灵石奖励（总） */
    @TableField(value = "reward_spirit_stones")
    private Integer rewardSpiritStones;

    /** 掉落经验奖励（总） */
    @TableField(value = "reward_exp")
    private Integer rewardExp;

    /** 特殊奖励道具ID */
    @TableField(value = "reward_item_id")
    private Integer rewardItemId;

    /** 本轮BOSS刷新时间 */
    @TableField(value = "spawned_at")
    private LocalDateTime spawnedAt;

    /** BOSS被击败时�?*/
    @TableField(value = "defeated_at")
    private LocalDateTime defeatedAt;

    /** 下次刷新时间 */
    @TableField(value = "next_spawn_at")
    private LocalDateTime nextSpawnAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

