package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerQuest;
import com.xiuxian.game.entity.Quest;
import com.xiuxian.game.dto.response.PlayerQuestDetailResponse;
import com.xiuxian.game.dto.response.QuestResponse;
import com.xiuxian.game.mapper.PlayerQuestMapper;
import com.xiuxian.game.mapper.QuestMapper;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.util.GameCalculator;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value = "app.features.quests.enabled", havingValue = "true")
@RequiredArgsConstructor
public class QuestService {

    private final QuestMapper questMapper;
    private final PlayerQuestMapper playerQuestMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final GameCalculator gameCalculator;
    private final Random random = new Random();

    public List<PlayerQuest> getPlayerDailyQuests(Integer playerId) {
        List<PlayerQuest> allQuests = playerQuestMapper.selectByPlayerId(playerId);
        
        // 过滤出日常任务
        List<PlayerQuest> dailyQuests = allQuests.stream()
                .filter(pq -> {
                    Quest quest = questMapper.selectById(pq.getQuestId());
                    return quest != null && "DAILY".equals(quest.getType());
                })
                .collect(Collectors.toList());
        
        // 如果玩家没有日常任务，自动生成
        if (dailyQuests == null || dailyQuests.isEmpty()) {
            return generateDailyQuestsForPlayer(playerId);
        }
        
        // 检查是否需要刷新日常任务（每天凌晨刷新）
        if (needsRefreshDailyQuests(dailyQuests)) {
            return refreshDailyQuests(playerId);
        }
        
        return dailyQuests;
    }

    public List<PlayerQuest> getPlayerWeeklyQuests(Integer playerId) {
        List<PlayerQuest> allQuests = playerQuestMapper.selectByPlayerId(playerId);
        List<PlayerQuest> weeklyQuests = allQuests.stream()
                .filter(pq -> {
                    Quest quest = questMapper.selectById(pq.getQuestId());
                    return quest != null && "WEEKLY".equals(quest.getType());
                })
                .collect(Collectors.toList());
        if (weeklyQuests == null || weeklyQuests.isEmpty()) {
            return generateWeeklyQuestsForPlayer(playerId);
        }
        if (needsRefreshWeeklyQuests(weeklyQuests)) {
            return refreshWeeklyQuests(playerId);
        }
        return weeklyQuests;
    }

    public List<PlayerQuest> getPlayerMonthlyQuests(Integer playerId) {
        List<PlayerQuest> allQuests = playerQuestMapper.selectByPlayerId(playerId);
        List<PlayerQuest> monthlyQuests = allQuests.stream()
                .filter(pq -> {
                    Quest quest = questMapper.selectById(pq.getQuestId());
                    return quest != null && "MONTHLY".equals(quest.getType());
                })
                .collect(Collectors.toList());
        if (monthlyQuests == null || monthlyQuests.isEmpty()) {
            return generateMonthlyQuestsForPlayer(playerId);
        }
        if (needsRefreshMonthlyQuests(monthlyQuests)) {
            return refreshMonthlyQuests(playerId);
        }
        return monthlyQuests;
    }
    
    public List<PlayerQuest> getPlayerAllQuests(Integer playerId) {
        return playerQuestMapper.selectByPlayerId(playerId);
    }

    public List<PlayerQuestDetailResponse> getPlayerAllQuestsDetail(Integer playerId) {
        List<PlayerQuest> list = playerQuestMapper.selectByPlayerId(playerId);
        return list.stream().map(this::toDetail).collect(Collectors.toList());
    }
    
    private boolean needsRefreshDailyQuests(List<PlayerQuest> dailyQuests) {
        if (dailyQuests == null || dailyQuests.isEmpty()) {
            return false;
        }
        
        // 检查所有日常任务，如果有任何一个过期了，就需要刷新
        LocalDateTime today = LocalDate.now().atStartOfDay();
        return dailyQuests.stream()
                .map(pq -> pq.getCreatedAt().toLocalDate().atStartOfDay())
                .anyMatch(date -> date.isBefore(today));
    }

    private boolean needsRefreshWeeklyQuests(List<PlayerQuest> weeklyQuests) {
        if (weeklyQuests == null || weeklyQuests.isEmpty()) {
            return false;
        }
        // 检查所有周常任务，如果有任何一个过期了，就需要刷新
        LocalDateTime startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        return weeklyQuests.stream()
                .map(pq -> pq.getCreatedAt().toLocalDate().atStartOfDay())
                .anyMatch(date -> date.isBefore(startOfWeek));
    }

    private boolean needsRefreshMonthlyQuests(List<PlayerQuest> monthlyQuests) {
        if (monthlyQuests == null || monthlyQuests.isEmpty()) {
            return false;
        }
        // 检查所有月常任务，如果有任何一个过期了，就需要刷新
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return monthlyQuests.stream()
                .map(pq -> pq.getCreatedAt().toLocalDate().atStartOfDay())
                .anyMatch(date -> date.isBefore(startOfMonth));
    }

    @Transactional
    public List<PlayerQuest> refreshDailyQuests(Integer playerId) {
        return generateDailyQuestsForPlayer(playerId);
    }

    @Transactional
    public List<PlayerQuest> refreshWeeklyQuests(Integer playerId) {
        return generateWeeklyQuestsForPlayer(playerId);
    }

    @Transactional
    public List<PlayerQuest> refreshMonthlyQuests(Integer playerId) {
        return generateMonthlyQuestsForPlayer(playerId);
    }
    
    @Transactional
    public List<PlayerQuest> generateDailyQuestsForPlayer(Integer playerId) {
        // 删除当前所有日常任务
        List<PlayerQuest> existingDailyQuests = playerQuestMapper.selectByPlayerId(playerId);
        for (PlayerQuest pq : existingDailyQuests) {
            Quest quest = questMapper.selectById(pq.getQuestId());
            if (quest != null && "DAILY".equals(quest.getType())) {
                playerQuestMapper.deleteById(pq.getId());
            }
        }
        
        // 获取所有日常任务模板
        List<Quest> dailyQuestTemplates = questMapper.selectByType("DAILY");
        
        if (dailyQuestTemplates == null || dailyQuestTemplates.isEmpty()) {
            // 如果没有任务模板，先初始化默认任务
            initializeDefaultQuests();
            dailyQuestTemplates = questMapper.selectByType("DAILY");
        }
        
        // 随机选择3-5个日常任务
        int questCount = Math.min(dailyQuestTemplates.size(), random.nextInt(3) + 3);
        List<Quest> selectedQuests = dailyQuestTemplates.stream()
                .sorted((a, b) -> random.nextInt() - random.nextInt())
                .limit(questCount)
                .collect(Collectors.toList());
        
        // 为玩家分配新的日常任务
        List<PlayerQuest> newQuests = selectedQuests.stream()
                .map(quest -> {
                    PlayerQuest pq = PlayerQuest.builder()
                            .playerId(playerId)
                            .questId(quest.getId())
                            .currentProgress(0)
                            .completed(false)
                            .rewardClaimed(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    playerQuestMapper.insert(pq);
                    return playerQuestMapper.selectById(pq.getId());
                })
                .collect(Collectors.toList());
        
        return newQuests;
    }

    @Transactional
    public List<PlayerQuest> generateWeeklyQuestsForPlayer(Integer playerId) {
        List<PlayerQuest> existing = playerQuestMapper.selectByPlayerId(playerId);
        for (PlayerQuest pq : existing) {
            Quest quest = questMapper.selectById(pq.getQuestId());
            if (quest != null && "WEEKLY".equals(quest.getType())) {
                playerQuestMapper.deleteById(pq.getId());
            }
        }
        List<Quest> templates = questMapper.selectByType("WEEKLY");
        if (templates == null || templates.isEmpty()) {
            initializeDefaultQuests();
            templates = questMapper.selectByType("WEEKLY");
        }
        return templates.stream()
                .map(quest -> {
                    PlayerQuest pq = PlayerQuest.builder()
                        .playerId(playerId)
                        .questId(quest.getId())
                            .currentProgress(0)
                            .completed(false)
                            .rewardClaimed(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    playerQuestMapper.insert(pq);
                    return playerQuestMapper.selectById(pq.getId());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PlayerQuest> generateMonthlyQuestsForPlayer(Integer playerId) {
        List<PlayerQuest> existing = playerQuestMapper.selectByPlayerId(playerId);
        for (PlayerQuest pq : existing) {
            Quest quest = questMapper.selectById(pq.getQuestId());
            if (quest != null && "MONTHLY".equals(quest.getType())) {
                playerQuestMapper.deleteById(pq.getId());
            }
        }
        List<Quest> templates = questMapper.selectByType("MONTHLY");
        if (templates == null || templates.isEmpty()) {
            initializeDefaultQuests();
            templates = questMapper.selectByType("MONTHLY");
        }
        return templates.stream()
                .map(quest -> {
                    PlayerQuest pq = PlayerQuest.builder()
                        .playerId(playerId)
                        .questId(quest.getId())
                            .currentProgress(0)
                            .completed(false)
                            .rewardClaimed(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    playerQuestMapper.insert(pq);
                    return playerQuestMapper.selectById(pq.getId());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void initializeDefaultQuests() {
        java.util.List<Quest> all = questMapper.selectList(null);

        boolean hasDaily = all.stream().anyMatch(q -> "DAILY".equals(q.getType()));
        boolean hasWeekly = all.stream().anyMatch(q -> "WEEKLY".equals(q.getType()));
        boolean hasMonthly = all.stream().anyMatch(q -> "MONTHLY".equals(q.getType()));

        if (!hasDaily) {
            Quest quest1 = Quest.builder()
                    .title("每日修炼")
                    .description("完成一次修炼")
                    .type("DAILY")
                    .requiredAmount(1)
                    .rewardExp(100)
                    .rewardSpiritStones(50)
                    .rewardContributionPoints(10)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            questMapper.insert(quest1);
            
            Quest quest2 = Quest.builder()
                    .title("每日收集灵石")
                    .description("获得100灵石")
                    .type("DAILY")
                    .requiredAmount(100)
                    .rewardExp(120)
                    .rewardSpiritStones(80)
                    .rewardContributionPoints(12)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            questMapper.insert(quest2);
        }

        if (!hasWeekly) {
            Quest w1 = Quest.builder()
                    .title("每周勤修")
                    .description("累计修炼300秒")
                    .type("WEEKLY")
                    .requiredAmount(300)
                    .rewardExp(800)
                    .rewardSpiritStones(500)
                    .rewardContributionPoints(50)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            questMapper.insert(w1);
            
            Quest w2 = Quest.builder()
                    .title("每周升级一次")
                    .description("提升1级")
                    .type("WEEKLY")
                    .requiredAmount(1)
                    .rewardExp(1000)
                    .rewardSpiritStones(600)
                    .rewardContributionPoints(60)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            questMapper.insert(w2);
        }

        if (!hasMonthly) {
            Quest m1 = Quest.builder()
                    .title("每月突破")
                    .description("完成10次修炼")
                    .type("MONTHLY")
                    .requiredAmount(10)
                    .rewardExp(10000)
                    .rewardSpiritStones(3000)
                    .rewardContributionPoints(300)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            questMapper.insert(m1);
        }
    }

    @Transactional
    public PlayerQuest updateQuestProgress(Integer playerId, Integer questId, Integer progress) {
        return updateQuestProgressInternal(playerId, questId, progress);
    }
    
    private PlayerQuest updateQuestProgressInternal(Integer playerId, Integer questId, Integer progress) {
        PlayerQuest playerQuest = playerQuestMapper.selectByPlayerIdAndQuestId(playerId, questId);
        if (playerQuest == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        Quest quest = questMapper.selectById(questId);
        playerQuest.setCurrentProgress(playerQuest.getCurrentProgress() + progress);
        
        if (playerQuest.getCurrentProgress() >= quest.getRequiredAmount()) {
            playerQuest.setCompleted(true);
        }
        
        playerQuest.setUpdatedAt(LocalDateTime.now());
        playerQuestMapper.updateById(playerQuest);
        return playerQuestMapper.selectById(playerQuest.getId());
    }

    @Transactional
    public void claimQuestReward(Integer playerId, Integer questId) {
        PlayerQuest playerQuest = playerQuestMapper.selectByPlayerIdAndQuestId(playerId, questId);
        if (playerQuest == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        if (!playerQuest.getCompleted()) {
            throw new IllegalArgumentException("任务未完成");
        }

        if (playerQuest.getRewardClaimed()) {
            throw new IllegalArgumentException("奖励已领取");
        }

        Quest quest = questMapper.selectById(questId);
        PlayerProfile player = playerProfileMapper.selectById(playerId);

        // 发放奖励
        player.setExp(player.getExp() + quest.getRewardExp());
        player.setSpiritStones(player.getSpiritStones() + quest.getRewardSpiritStones());
        player.setContributionPoints(player.getContributionPoints() + quest.getRewardContributionPoints());
        
        // 增加属性点奖励（根据任务奖励设置）
        int attributePointsReward = quest.getRewardExp() / 100; // 每100经验奖励1属性点
        if (attributePointsReward > 0) {
            player.setAttributePoints(player.getAttributePoints() + attributePointsReward);
        }
        
        playerProfileMapper.updateById(player);

        // 标记奖励已领取
        playerQuest.setRewardClaimed(true);
        playerQuest.setUpdatedAt(LocalDateTime.now());
        playerQuestMapper.updateById(playerQuest);
    }

    @Transactional
    public void claimQuestRewardByPlayerQuestId(Integer playerId, Integer playerQuestId) {
        PlayerQuest playerQuest = playerQuestMapper.selectById(playerQuestId);
        if (playerQuest == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        if (!playerQuest.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("无权操作其他玩家的任务");
        }
        claimQuestReward(playerId, playerQuest.getQuestId());
    }

    // 根据类型获取玩家任务
    public List<PlayerQuest> getPlayerQuestsByType(Integer playerId, Quest.QuestType type) {
        List<PlayerQuest> allQuests = playerQuestMapper.selectByPlayerId(playerId);
        List<PlayerQuest> filtered = allQuests.stream()
                .filter(pq -> {
                    Quest quest = questMapper.selectById(pq.getQuestId());
                    return quest != null && quest.getType().equals(type.toString());
                })
                .collect(Collectors.toList());

        if (filtered == null || filtered.isEmpty()) {
            if (type == Quest.QuestType.DAILY) {
                filtered = generateDailyQuestsForPlayer(playerId);
            } else if (type == Quest.QuestType.WEEKLY) {
                filtered = generateWeeklyQuestsForPlayer(playerId);
            } else if (type == Quest.QuestType.MONTHLY) {
                filtered = generateMonthlyQuestsForPlayer(playerId);
            }
        }
        return filtered;
    }

    public List<PlayerQuestDetailResponse> getPlayerQuestsDetailByType(Integer playerId, Quest.QuestType type) {
        return getPlayerQuestsByType(playerId, type).stream().map(this::toDetail).collect(Collectors.toList());
    }

    // 批量领取所有已完成任务的奖励
    @Transactional
    public int claimAllCompletedQuestRewards(Integer playerId) {
        List<PlayerQuest> allQuests = playerQuestMapper.selectByPlayerId(playerId);
        
        int claimedCount = 0;
        for (PlayerQuest pq : allQuests) {
            if (pq.getCompleted() && !pq.getRewardClaimed()) {
                claimQuestReward(playerId, pq.getQuestId());
                claimedCount++;
            }
        }
        
        return claimedCount;
    }

    // 检查是否有未完成的任务
    public boolean hasIncompleteQuests(Integer playerId) {
        List<PlayerQuest> allQuests = playerQuestMapper.selectByPlayerId(playerId);
        
        return allQuests.stream().anyMatch(pq -> !pq.getCompleted());
    }

    // 获取未领取奖励的已完成任务数量
    public long getUnclaimedCompletedQuestsCount(Integer playerId) {
        List<PlayerQuest> allQuests = playerQuestMapper.selectByPlayerId(playerId);
        
        return allQuests.stream()
                .filter(pq -> pq.getCompleted() && !pq.getRewardClaimed())
                .count();
    }

    // 根据任务类型更新进度
    @Transactional
    public void updateQuestProgressByType(Integer playerId, Quest.QuestType questType, int progress) {
        List<PlayerQuest> quests = getPlayerQuestsByType(playerId, questType);
        
        for (PlayerQuest pq : quests) {
            if (!pq.getCompleted()) {
                updateQuestProgressInternal(playerId, pq.getQuestId(), progress);
            }
        }
    }

    private PlayerQuestDetailResponse toDetail(PlayerQuest pq) {
        Quest q = questMapper.selectById(pq.getQuestId());
        QuestResponse qr = QuestResponse.builder()
                .id(q.getId().longValue())
                .title(q.getTitle())
                .description(q.getDescription())
                .type(q.getType())
                .requiredAmount(q.getRequiredAmount())
                .rewardExp(q.getRewardExp())
                .rewardSpiritStones(q.getRewardSpiritStones())
                .rewardContributionPoints(q.getRewardContributionPoints())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
        return PlayerQuestDetailResponse.builder()
                .id(pq.getId().longValue())
                .currentProgress(pq.getCurrentProgress())
                .completed(pq.getCompleted())
                .rewardClaimed(pq.getRewardClaimed())
                .quest(qr)
                .build();
    }
}
