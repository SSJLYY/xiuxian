package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 玩家参与宗门BOSS挑战记录
 */
@Data
@TableName("guild_boss_challenges")
public class GuildBossChallenge {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 宗门BOSS ID */
    @TableField(value = "boss_id")
    private Integer bossId;

    /** 玩家ID */
    @TableField(value = "player_id")
    private Integer playerId;

    /** 本次造成的伤害 */
    @TableField(value = "damage_dealt")
    private Long damageDealt;

    /** 今日累计挑战次数 */
    @TableField(value = "today_attempts")
    private Integer todayAttempts;

    /** 最后挑战时间 */
    @TableField(value = "last_challenge_at")
    private LocalDateTime lastChallengeAt;

    /** 是否已领取奖励 */
    @TableField(value = "reward_claimed")
    private Boolean rewardClaimed;

    /** 个人伤害奖励灵石 */
    @TableField(value = "personal_reward_stones")
    private Integer personalRewardStones;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
