package com.xiuxian.game.repository;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerQuest;
import com.xiuxian.game.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerQuestRepository extends JpaRepository<PlayerQuest, Integer> {
    List<PlayerQuest> findByPlayer(PlayerProfile player);
    List<PlayerQuest> findByPlayerAndQuestType(PlayerProfile player, Quest.QuestType type);
    Optional<PlayerQuest> findByPlayerAndQuest(PlayerProfile player, Quest quest);
    void deleteByPlayerAndQuestType(PlayerProfile player, Quest.QuestType type);
    
    // 查询已完成但未领取奖励的任务
    @Query("SELECT pq FROM PlayerQuest pq WHERE pq.player = :player AND pq.completed = :completed AND pq.rewardClaimed = :rewardClaimed")
    List<PlayerQuest> findByPlayerAndCompletedAndRewardClaimed(@Param("player") PlayerProfile player, 
                                                               @Param("completed") boolean completed, 
                                                               @Param("rewardClaimed") boolean rewardClaimed);
    
    // 检查玩家是否有未完成的任务
    boolean existsByPlayerAndCompleted(PlayerProfile player, boolean completed);
    
    // 统计已完成但未领取奖励的任务数量
    @Query("SELECT COUNT(pq) FROM PlayerQuest pq WHERE pq.player = :player AND pq.completed = :completed AND pq.rewardClaimed = :rewardClaimed")
    long countByPlayerAndCompletedAndRewardClaimed(@Param("player") PlayerProfile player, 
                                                   @Param("completed") boolean completed, 
                                                   @Param("rewardClaimed") boolean rewardClaimed);
}