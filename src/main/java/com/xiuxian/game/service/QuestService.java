package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerQuest;
import com.xiuxian.game.entity.Quest;
import com.xiuxian.game.repository.PlayerQuestRepository;
import com.xiuxian.game.repository.QuestRepository;
import com.xiuxian.game.util.GameCalculator;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import com.xiuxian.game.util.Java8Compatibility;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final PlayerQuestRepository playerQuestRepository;
    private final PlayerService playerService;
    private final GameCalculator gameCalculator;
    private final Random random = new Random();

    public List<PlayerQuest> getPlayerDailyQuests() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        List<PlayerQuest> dailyQuests = playerQuestRepository.findByPlayerAndQuestType(player, Quest.QuestType.DAILY);
        
        // 如果玩家没有日常任务，自动生成
        if (dailyQuests == null || dailyQuests.isEmpty()) {
            return generateDailyQuestsForPlayer(player);
        }
        
        // 检查是否需要刷新日常任务（每天凌晨刷新）
        if (needsRefreshDailyQuests(dailyQuests)) {
            return refreshDailyQuests();
        }
        
        return dailyQuests;
    }
    
    private boolean needsRefreshDailyQuests(List<PlayerQuest> dailyQuests) {
        if (dailyQuests == null || dailyQuests.isEmpty()) {
            return false;
        }
        
        LocalDateTime lastQuestDate = dailyQuests.get(0).getCreatedAt().toLocalDate().atStartOfDay();
        LocalDateTime today = LocalDate.now().atStartOfDay();
        
        return lastQuestDate.isBefore(today);
    }

    @Transactional
    public List<PlayerQuest> refreshDailyQuests() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return generateDailyQuestsForPlayer(player);
    }
    
    @Transactional
    public List<PlayerQuest> generateDailyQuestsForPlayer(PlayerProfile player) {
        // 删除当前所有日常任务
        playerQuestRepository.deleteByPlayerAndQuestType(player, Quest.QuestType.DAILY);
        
        // 获取所有日常任务模板
        List<Quest> dailyQuestTemplates = questRepository.findByType(Quest.QuestType.DAILY);
        
        if (dailyQuestTemplates == null || dailyQuestTemplates.isEmpty()) {
            // 如果没有任务模板，先初始化默认任务
            initializeDefaultQuests();
            dailyQuestTemplates = questRepository.findByType(Quest.QuestType.DAILY);
        }
        
        // 随机选择3-5个日常任务
        int questCount = Math.min(dailyQuestTemplates.size(), random.nextInt(3) + 3);
        List<Quest> selectedQuests = dailyQuestTemplates.stream()
                .sorted((a, b) -> random.nextInt() - random.nextInt())
                .limit(questCount)
                .collect(Collectors.toList());
        
        // 为玩家分配新的日常任务
        List<PlayerQuest> newQuests = selectedQuests.stream()
                .map(quest -> PlayerQuest.builder()
                        .player(player)
                        .quest(quest)
                        .currentProgress(0)
                        .completed(false)
                        .rewardClaimed(false)
                        .build())
                .collect(Collectors.toList());
        
        return playerQuestRepository.saveAll(newQuests);
    }

    @Transactional
    public PlayerQuest updateQuestProgress(Integer questId, int progress) { // 改回Integer类型
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        Quest quest = questRepository.findById(questId) // 直接使用 questId，不再需要转换
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_QUEST_NOT_FOUND + ": 任务不存在"));
        
        PlayerQuest playerQuest = playerQuestRepository.findByPlayerAndQuest(player, quest)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_QUEST_NOT_FOUND + ": 玩家任务不存在"));
        
        // 验证任务是否已完成且奖励已领取
        if (playerQuest.getRewardClaimed()) {
            throw new IllegalArgumentException(GameConstants.ERROR_ALREADY_COMPLETED + ": 任务已完成并领取奖励");
        }
        
        // 更新进度
        int newProgress = playerQuest.getCurrentProgress() + progress;
        playerQuest.setCurrentProgress(Math.max(0, newProgress));
        
        // 检查是否完成任务
        if (playerQuest.getCurrentProgress() >= quest.getRequiredAmount() && !playerQuest.getCompleted()) {
            playerQuest.setCompleted(true);
            playerQuest.setCompletedAt(LocalDateTime.now());
        }
        
        return playerQuestRepository.save(playerQuest);
    }
    
    /**
     * 根据任务类型更新进度
     */
    @Transactional
    public void updateQuestProgressByType(Quest.QuestType questType, int progress) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        List<PlayerQuest> playerQuests = playerQuestRepository.findByPlayerAndQuestType(player, questType);
        
        for (PlayerQuest playerQuest : playerQuests) {
            if (!playerQuest.getCompleted() && !playerQuest.getRewardClaimed()) {
                updateQuestProgress(playerQuest.getQuest().getId(), progress); // 直接传递 Long 类型 ID
            }
        }
    }

    @Transactional
    public void claimQuestReward(Integer playerQuestId) { // 改回Integer类型
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        PlayerQuest playerQuest = playerQuestRepository.findById(playerQuestId) // 直接使用 playerQuestId，不再需要转换
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_QUEST_NOT_FOUND + ": 玩家任务不存在"));
        
        // 验证是否是当前玩家的任务
        if (!playerQuest.getPlayer().getId().equals(player.getId())) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权领取该任务奖励");
        }
        
        // 验证任务是否已完成且奖励未领取
        if (!playerQuest.getCompleted()) {
            throw new IllegalArgumentException(GameConstants.ERROR_REQUIREMENTS_NOT_MET + ": 任务未完成，无法领取奖励");
        }
        
        if (playerQuest.getRewardClaimed()) {
            throw new IllegalArgumentException(GameConstants.ERROR_ALREADY_COMPLETED + ": 奖励已领取");
        }
        
        // 发放奖励
        Quest quest = playerQuest.getQuest();
        long oldExp = player.getExp();
        long oldLevel = player.getLevel();
        
        player.setExp(player.getExp() + quest.getRewardExp());
        player.setSpiritStones(player.getSpiritStones() + quest.getRewardSpiritStones());
        player.setContributionPoints(player.getContributionPoints() + quest.getRewardContributionPoints());
        
        // 检查升级
        gameCalculator.checkLevelUp(player);
        
        // 标记奖励已领取
        playerQuest.setRewardClaimed(true);
        playerQuestRepository.save(playerQuest);
        
        // 保存玩家资料
        playerService.savePlayerProfile(player);
    }
    
    /**
     * 批量领取已完成任务的奖励
     */
    @Transactional
    public int claimAllCompletedQuestRewards() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        List<PlayerQuest> completedQuests = playerQuestRepository.findByPlayerAndCompletedAndRewardClaimed(
                player, true, false);
        
        int claimedCount = 0;
        for (PlayerQuest playerQuest : completedQuests) {
            try {
                claimQuestReward(playerQuest.getId()); // 直接传递 Long 类型 ID
                claimedCount++;
            } catch (Exception e) {
                // 跳过有问题的任务，继续处理其他任务
                continue;
            }
        }
        
        return claimedCount;
    }

    // 初始化默认任务数据
    @Transactional
    public void initializeDefaultQuests() {
        // 检查是否已有任务数据
        if (questRepository.count() > 0) {
            return;
        }

        // 创建默认日常任务
        List<Quest> defaultQuests = Java8Compatibility.listOf(
                // 日常任务
                Quest.builder()
                        .title("每日修炼")
                        .description("完成10次修炼")
                        .type(Quest.QuestType.DAILY)
                        .requiredAmount(10)
                        .rewardExp(100)
                        .rewardSpiritStones(50)
                        .rewardContributionPoints(5)
                        .build(),
                Quest.builder()
                        .title("灵石收集")
                        .description("获得100灵石")
                        .type(Quest.QuestType.DAILY)
                        .requiredAmount(100)
                        .rewardExp(50)
                        .rewardSpiritStones(20)
                        .rewardContributionPoints(2)
                        .build(),
                Quest.builder()
                        .title("技能修炼")
                        .description("使用技能5次")
                        .type(Quest.QuestType.DAILY)
                        .requiredAmount(5)
                        .rewardExp(80)
                        .rewardSpiritStones(30)
                        .rewardContributionPoints(3)
                        .build(),
                Quest.builder()
                        .title("境界提升")
                        .description("提升1个境界等级")
                        .type(Quest.QuestType.DAILY)
                        .requiredAmount(1)
                        .rewardExp(200)
                        .rewardSpiritStones(100)
                        .rewardContributionPoints(10)
                        .build(),
                Quest.builder()
                        .title("在线时长")
                        .description("在线游戏30分钟")
                        .type(Quest.QuestType.DAILY)
                        .requiredAmount(30)
                        .rewardExp(60)
                        .rewardSpiritStones(25)
                        .rewardContributionPoints(2)
                        .build(),
                
                // 周常任务
                Quest.builder()
                        .title("周常修炼挑战")
                        .description("完成100次修炼")
                        .type(Quest.QuestType.WEEKLY)
                        .requiredAmount(100)
                        .rewardExp(1000)
                        .rewardSpiritStones(500)
                        .rewardContributionPoints(50)
                        .build(),
                Quest.builder()
                        .title("周常灵石收集")
                        .description("获得1000灵石")
                        .type(Quest.QuestType.WEEKLY)
                        .requiredAmount(1000)
                        .rewardExp(500)
                        .rewardSpiritStones(200)
                        .rewardContributionPoints(20)
                        .build(),
                
                // 主线任务
                Quest.builder()
                        .title("初入仙途")
                        .description("达到练气期三层")
                        .type(Quest.QuestType.MAIN)
                        .requiredAmount(3)
                        .rewardExp(500)
                        .rewardSpiritStones(200)
                        .rewardContributionPoints(20)
                        .build(),
                Quest.builder()
                        .title("初窥门径")
                        .description("达到练气期五层")
                        .type(Quest.QuestType.MAIN)
                        .requiredAmount(5)
                        .rewardExp(1000)
                        .rewardSpiritStones(500)
                        .rewardContributionPoints(50)
                        .build(),
                
                // 支线任务
                Quest.builder()
                        .title("助人为乐")
                        .description("帮助其他玩家完成1次修炼")
                        .type(Quest.QuestType.SIDE)
                        .requiredAmount(1)
                        .rewardExp(200)
                        .rewardSpiritStones(100)
                        .rewardContributionPoints(10)
                        .build(),
                Quest.builder()
                        .title("探索发现")
                        .description("发现3个新的修炼地点")
                        .type(Quest.QuestType.SIDE)
                        .requiredAmount(3)
                        .rewardExp(300)
                        .rewardSpiritStones(150)
                        .rewardContributionPoints(15)
                        .build()
        );

        questRepository.saveAll(defaultQuests);
    }
    
    /**
     * 为新玩家初始化任务
     */
    @Transactional
    public void initializePlayerQuests(PlayerProfile player) {
        // 确保任务模板已初始化
        initializeDefaultQuests();
        
        // 为新玩家生成初始日常任务
        generateDailyQuestsForPlayer(player);
    }
    
    /**
     * 获取玩家所有任务
     */
    public List<PlayerQuest> getPlayerAllQuests() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return playerQuestRepository.findByPlayer(player);
    }
    
    /**
     * 获取玩家指定类型的任务
     */
    public List<PlayerQuest> getPlayerQuestsByType(Quest.QuestType questType) {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return playerQuestRepository.findByPlayerAndQuestType(player, questType);
    }
    
    /**
     * 检查玩家是否有未完成的任务
     */
    public boolean hasIncompleteQuests() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return playerQuestRepository.existsByPlayerAndCompleted(player, false);
    }
    
    /**
     * 获取已完成但未领取奖励的任务数量
     */
    public long getUnclaimedCompletedQuestsCount() {
        PlayerProfile player = playerService.getCurrentPlayerProfile();
        return playerQuestRepository.countByPlayerAndCompletedAndRewardClaimed(player, true, false);
    }
}