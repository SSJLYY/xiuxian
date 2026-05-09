package com.xiuxian.game.modules.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.achievement.entity.PlayerAchievement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 玩家成就数据访问层
 *
 * <p>提供玩家成就进度的CRUD操作，继承MyBatis-Plus的BaseMapper。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Mapper
public interface PlayerAchievementMapper extends BaseMapper<PlayerAchievement> {

    @Insert("INSERT INTO player_achievements " +
            "(player_id, achievement_id, progress, is_completed, is_claimed, completed_at) " +
            "VALUES (#{playerId}, #{achievementId}, #{progress}, #{completedFlag}, 0, " +
            "CASE WHEN #{completedFlag} = 1 THEN #{completedAt} ELSE NULL END) " +
            "ON DUPLICATE KEY UPDATE " +
            "progress = GREATEST(progress, VALUES(progress)), " +
            "is_completed = CASE WHEN is_completed = 1 OR #{completedFlag} = 1 THEN 1 ELSE 0 END, " +
            "completed_at = CASE WHEN #{completedFlag} = 1 THEN COALESCE(completed_at, #{completedAt}) ELSE completed_at END")
    int upsertProgress(@Param("playerId") Integer playerId,
                       @Param("achievementId") Integer achievementId,
                       @Param("progress") Integer progress,
                       @Param("completedFlag") Integer completedFlag,
                       @Param("completedAt") LocalDateTime completedAt);

    @Update("UPDATE player_achievements " +
            "SET is_claimed = 1, claimed_at = #{claimedAt} " +
            "WHERE id = #{playerAchievementId} AND is_claimed = 0")
    int claimAchievementIfUnclaimed(@Param("playerAchievementId") Long playerAchievementId,
                                    @Param("claimedAt") LocalDateTime claimedAt);
}

