package com.xiuxian.game.modules.quest.service;

import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.quest.entity.Quest;
import com.xiuxian.game.modules.quest.mapper.PlayerQuestMapper;
import com.xiuxian.game.modules.quest.mapper.QuestMapper;
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
     * 鏇存柊鐜╁浠诲姟杩涘害
     * 
     * @param playerId 鐜╁ID
     * @param questType 浠诲姟绫诲瀷
     * @param progressIncrement 杩涘害澧為噺
     */
    public void updateQuestProgressByType(Integer playerId, Quest.QuestType questType, int progressIncrement) {
        try {
            // 鑾峰彇鐜╁鐨勬墍鏈変换鍔?
            List<com.xiuxian.game.modules.quest.entity.PlayerQuest> playerQuests = playerQuestMapper.selectByPlayerId(playerId);
            
            if (playerQuests == null || playerQuests.isEmpty()) {
                return;
            }

            // 杩囨护鍑烘寚瀹氱被鍨嬬殑浠诲姟
            playerQuests.stream()
                .filter(pq -> {
                    Quest quest = questMapper.selectById(pq.getQuestId());
                    return quest != null && questType.name().equals(quest.getType());
                })
                .forEach(playerQuest -> {
                    try {
                        Quest quest = questMapper.selectById(playerQuest.getQuestId());
                        if (quest == null) return;
                        
                        // 鏇存柊杩涘害
                        int newProgress = Math.min(playerQuest.getCurrentProgress() + progressIncrement, 
                                                   quest.getRequiredAmount());
                        playerQuest.setCurrentProgress(newProgress);
                        
                        // 濡傛灉瀹屾垚锛岃缃畬鎴愮姸鎬?
                        if (newProgress >= quest.getRequiredAmount() && !playerQuest.getCompleted()) {
                            playerQuest.setCompleted(true);
                            playerQuest.setCompletedAt(LocalDateTime.now());
                            log.info("鐜╁ {} 瀹屾垚浠诲姟 {}", playerId, playerQuest.getQuestId());
                        }
                        
                        // 鏇存柊鏁版嵁搴?
                        playerQuestMapper.updateById(playerQuest);
                    } catch (Exception e) {
                        log.error("鏇存柊浠诲姟 {} 杩涘害澶辫触: {}", playerQuest.getId(), e.getMessage(), e);
                    }
                });
        } catch (Exception e) {
            log.error("鏇存柊鐜╁ {} 鐨勪换鍔¤繘搴﹀け璐? {}", playerId, e.getMessage(), e);
        }
    }
}
