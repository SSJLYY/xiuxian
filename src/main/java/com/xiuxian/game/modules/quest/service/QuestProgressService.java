package com.xiuxian.game.modules.quest.service;

import com.xiuxian.game.modules.quest.entity.Quest;
import com.xiuxian.game.modules.quest.mapper.PlayerQuestMapper;
import com.xiuxian.game.modules.quest.mapper.QuestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestProgressService {

    private final PlayerQuestMapper playerQuestMapper;
    private final QuestMapper questMapper;

    public void updateQuestProgressByType(Integer playerId, Quest.QuestType questType, int progressIncrement) {
        try {
            if (progressIncrement <= 0) {
                return;
            }

            List<com.xiuxian.game.modules.quest.entity.PlayerQuest> playerQuests = playerQuestMapper.selectByPlayerId(playerId);

            if (playerQuests == null || playerQuests.isEmpty()) {
                return;
            }

            List<Integer> questIds = playerQuests.stream()
                    .map(com.xiuxian.game.modules.quest.entity.PlayerQuest::getQuestId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Quest> quests = questMapper.selectBatchIds(questIds);
            if (quests == null || quests.isEmpty()) {
                return;
            }
            Map<Integer, Quest> questMap = quests.stream()
                    .collect(Collectors.toMap(Quest::getId, q -> q));

            for (com.xiuxian.game.modules.quest.entity.PlayerQuest playerQuest : playerQuests) {
                try {
                    Quest quest = questMap.get(playerQuest.getQuestId());
                    if (quest == null || !questType.name().equals(quest.getType())) {
                        continue;
                    }

                    int newProgress = Math.min(playerQuest.getCurrentProgress() + progressIncrement,
                                               quest.getRequiredAmount());
                    playerQuest.setCurrentProgress(newProgress);

                    if (newProgress >= quest.getRequiredAmount() && !playerQuest.getCompleted()) {
                        playerQuest.setCompleted(true);
                        playerQuest.setCompletedAt(LocalDateTime.now());
                        log.debug("玩家 {} 完成任务 {}", playerId, playerQuest.getQuestId());
                    }

                    playerQuestMapper.updateById(playerQuest);
                } catch (Exception e) {
                    log.error("更新任务 {} 进度失败: {}", playerQuest.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("更新玩家 {} 的任务进度失败: {}", playerId, e.getMessage(), e);
        }
    }
}
