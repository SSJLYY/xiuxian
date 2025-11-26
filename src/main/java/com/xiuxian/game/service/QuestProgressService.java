package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.Quest;
import com.xiuxian.game.mapper.PlayerQuestMapper;
import com.xiuxian.game.mapper.QuestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestProgressService {

    private final PlayerQuestMapper playerQuestMapper;
    private final QuestMapper questMapper;

    /**
     * 更新玩家任务进度
     * 
     * @param playerId 玩家ID
     * @param questType 任务类型
     * @param progressIncrement 进度增量
     */
    public void updateQuestProgressByType(Integer playerId, Quest.QuestType questType, int progressIncrement) {
        try {
            // 获取玩家的所有任务
            List<com.xiuxian.game.entity.PlayerQuest> playerQuests = playerQuestMapper.selectByPlayerId(playerId);
            
            if (playerQuests == null || playerQuests.isEmpty()) {
                return;
            }

            // 过滤出指定类型的任务
            playerQuests.stream()
                .filter(pq -> {
                    Quest quest = questMapper.selectById(pq.getQuestId());
                    return quest != null && questType.name().equals(quest.getType());
                })
                .forEach(playerQuest -> {
                    try {
                        Quest quest = questMapper.selectById(playerQuest.getQuestId());
                        if (quest == null) return;
                        
                        // 更新进度
                        int newProgress = Math.min(playerQuest.getCurrentProgress() + progressIncrement, 
                                                   quest.getRequiredAmount());
                        playerQuest.setCurrentProgress(newProgress);
                        
                        // 如果完成，设置完成状态
                        if (newProgress >= quest.getRequiredAmount() && !playerQuest.getCompleted()) {
                            playerQuest.setCompleted(true);
                            playerQuest.setCompletedAt(LocalDateTime.now());
                            log.info("玩家 {} 完成任务 {}", playerId, playerQuest.getQuestId());
                        }
                        
                        // 更新数据库
                        playerQuestMapper.updateById(playerQuest);
                    } catch (Exception e) {
                        log.error("更新任务 {} 进度失败: {}", playerQuest.getId(), e.getMessage(), e);
                    }
                });
        } catch (Exception e) {
            log.error("更新玩家 {} 的任务进度失败: {}", playerId, e.getMessage(), e);
        }
    }
}