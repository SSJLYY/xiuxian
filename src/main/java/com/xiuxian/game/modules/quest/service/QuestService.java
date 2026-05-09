package com.xiuxian.game.modules.quest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.quest.entity.PlayerQuest;
import com.xiuxian.game.modules.quest.entity.Quest;
import com.xiuxian.game.dto.response.PlayerQuestDetailResponse;
import com.xiuxian.game.dto.response.QuestResponse;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.quest.mapper.PlayerQuestMapper;
import com.xiuxian.game.modules.quest.mapper.QuestMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.util.GameCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

/**
 * 任务服务类
 * 负责任务系统的所有业务逻辑
 * 
 * 主要功能：
 * - 任务生成和刷新（日常、周常、月常）
 * - 任务进度更新
 * - 任务奖励发放
 * - 任务完成检测
 * 
 * @author xiuxian
 * @version 1.0
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "app.features.quests.enabled", havingValue = "true")
@RequiredArgsConstructor
public class QuestService {

    private final QuestMapper questMapper;
    private final PlayerQuestMapper playerQuestMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;  // 模块边界：通过PlayerService访问玩家数据
    private final GameCalculator gameCalculator;

    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

    public List<PlayerQuest> getPlayerDailyQuests(Integer playerId) {
        // 使用带type过滤的查询，避免N+1（不在内存循环调用questMapper.selectById）
        List<PlayerQuest> dailyQuests = playerQuestMapper.selectByPlayerIdAndQuestType(playerId, "DAILY");
        
        // 如果没有日常任务，生成新的
        if (dailyQuests == null || dailyQuests.isEmpty()) {
            return generateDailyQuestsForPlayer(playerId);
        }
        
        // 检查是否需要刷新
        if (needsRefreshDailyQuests(dailyQuests)) {
            return refreshDailyQuests(playerId);
        }
        
        return dailyQuests;
    }

    public List<PlayerQuest> getPlayerWeeklyQuests(Integer playerId) {
        List<PlayerQuest> weeklyQuests = playerQuestMapper.selectByPlayerIdAndQuestType(playerId, "WEEKLY");
        if (weeklyQuests == null || weeklyQuests.isEmpty()) {
            return generateWeeklyQuestsForPlayer(playerId);
        }
        if (needsRefreshWeeklyQuests(weeklyQuests)) {
            return refreshWeeklyQuests(playerId);
        }
        return weeklyQuests;
    }

    public List<PlayerQuest> getPlayerMonthlyQuests(Integer playerId) {
        List<PlayerQuest> monthlyQuests = playerQuestMapper.selectByPlayerIdAndQuestType(playerId, "MONTHLY");
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
        if (list.isEmpty()) return new ArrayList<>();
        // 批量加载quest信息（避免N+1）
        List<Integer> questIds = list.stream().map(PlayerQuest::getQuestId).distinct().collect(Collectors.toList());
        Map<Integer, Quest> questMap = questMapper.selectBatchIds(questIds)
                .stream().collect(Collectors.toMap(Quest::getId, q -> q, (a, b) -> a));
        return list.stream().map(pq -> toDetail(pq, questMap.get(pq.getQuestId()))).collect(Collectors.toList());
    }
    
    private boolean needsRefreshDailyQuests(List<PlayerQuest> dailyQuests) {
        if (dailyQuests == null || dailyQuests.isEmpty()) {
            return false;
        }
        
        // 检查是否已过今天零点
        LocalDateTime today = LocalDate.now().atStartOfDay();
        return dailyQuests.stream()
                .map(pq -> pq.getCreatedAt().toLocalDate().atStartOfDay())
                .anyMatch(date -> date.isBefore(today));
    }

    private boolean needsRefreshWeeklyQuests(List<PlayerQuest> weeklyQuests) {
        if (weeklyQuests == null || weeklyQuests.isEmpty()) {
            return false;
        }
        // 检查是否已过本周一零点
        LocalDateTime startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        return weeklyQuests.stream()
                .map(pq -> pq.getCreatedAt().toLocalDate().atStartOfDay())
                .anyMatch(date -> date.isBefore(startOfWeek));
    }

    private boolean needsRefreshMonthlyQuests(List<PlayerQuest> monthlyQuests) {
        if (monthlyQuests == null || monthlyQuests.isEmpty()) {
            return false;
        }
        // 检查是否已过本月1号零点
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
        // 批量删除旧的日常任务（1条SQL，避免循环selectById+deleteById）
        playerQuestMapper.delete(new QueryWrapper<PlayerQuest>()
                .eq("player_id", playerId)
                .inSql("quest_id", "SELECT id FROM quests WHERE type = 'DAILY'"));
        
        // 获取日常任务模板
        List<Quest> dailyQuestTemplates = questMapper.selectByType("DAILY");
        
        if (dailyQuestTemplates == null || dailyQuestTemplates.isEmpty()) {
            // 初始化默认任务
            initializeDefaultQuests();
            dailyQuestTemplates = questMapper.selectByType("DAILY");
        }
        
        // 随机选取3-5个任务
        int questCount = Math.min(dailyQuestTemplates.size(), rng().nextInt(3) + 3);
        List<Quest> selectedQuests = dailyQuestTemplates.stream()
                .sorted((a, b) -> rng().nextInt() - rng().nextInt())
                .limit(questCount)
                .collect(Collectors.toList());
        
        // 批量构建并插入（避免循环insert+selectById）
        LocalDateTime now = LocalDateTime.now();
        List<PlayerQuest> newQuests = new ArrayList<>(selectedQuests.size());
        for (Quest quest : selectedQuests) {
            PlayerQuest pq = PlayerQuest.builder()
                    .playerId(playerId)
                    .questId(quest.getId())
                    .currentProgress(0)
                    .completed(false)
                    .rewardClaimed(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            playerQuestMapper.insert(pq);
            newQuests.add(pq);
        }
        
        return newQuests;
    }

    @Transactional
    public List<PlayerQuest> generateWeeklyQuestsForPlayer(Integer playerId) {
        // 批量删除旧的周常任务
        playerQuestMapper.delete(new QueryWrapper<PlayerQuest>()
                .eq("player_id", playerId)
                .inSql("quest_id", "SELECT id FROM quests WHERE type = 'WEEKLY'"));
        List<Quest> templates = questMapper.selectByType("WEEKLY");
        if (templates == null || templates.isEmpty()) {
            initializeDefaultQuests();
            templates = questMapper.selectByType("WEEKLY");
        }
        LocalDateTime now = LocalDateTime.now();
        List<PlayerQuest> result = new ArrayList<>(templates.size());
        for (Quest quest : templates) {
            PlayerQuest pq = PlayerQuest.builder()
                .playerId(playerId)
                .questId(quest.getId())
                .currentProgress(0)
                .completed(false)
                .rewardClaimed(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
            playerQuestMapper.insert(pq);
            result.add(pq);
        }
        return result;
    }

    @Transactional
    public List<PlayerQuest> generateMonthlyQuestsForPlayer(Integer playerId) {
        // 批量删除旧的月常任务
        playerQuestMapper.delete(new QueryWrapper<PlayerQuest>()
                .eq("player_id", playerId)
                .inSql("quest_id", "SELECT id FROM quests WHERE type = 'MONTHLY'"));
        List<Quest> templates = questMapper.selectByType("MONTHLY");
        if (templates == null || templates.isEmpty()) {
            initializeDefaultQuests();
            templates = questMapper.selectByType("MONTHLY");
        }
        LocalDateTime now = LocalDateTime.now();
        List<PlayerQuest> result = new ArrayList<>(templates.size());
        for (Quest quest : templates) {
            PlayerQuest pq = PlayerQuest.builder()
                .playerId(playerId)
                .questId(quest.getId())
                .currentProgress(0)
                .completed(false)
                .rewardClaimed(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
            playerQuestMapper.insert(pq);
            result.add(pq);
        }
        return result;
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
                    .description("累计修炼300次")
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

    public PlayerQuest acceptQuest(Integer playerId, Integer questId) {
        PlayerQuest playerQuest = playerQuestMapper.selectByPlayerIdAndQuestId(playerId, questId);
        if (playerQuest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "任务不存在");
        }
        return playerQuest;
    }

    @Transactional
    public void completeQuest(Integer playerId, Integer playerQuestId) {
        claimQuestRewardByPlayerQuestId(playerId, playerQuestId);
    }
    
    private PlayerQuest updateQuestProgressInternal(Integer playerId, Integer questId, Integer progress) {
        PlayerQuest playerQuest = playerQuestMapper.selectByPlayerIdAndQuestId(playerId, questId);
        if (playerQuest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "任务不存在");
        }

        Quest quest = questMapper.selectById(questId);
        LocalDateTime now = LocalDateTime.now();
        playerQuest.setCurrentProgress(defaultInt(playerQuest.getCurrentProgress()) + defaultInt(progress));
        
        if (playerQuest.getCurrentProgress() >= quest.getRequiredAmount()) {
            if (!Boolean.TRUE.equals(playerQuest.getCompleted()) && playerQuest.getCompletedAt() == null) {
                playerQuest.setCompletedAt(now);
            }
            playerQuest.setCompleted(true);
        }
        
        playerQuest.setUpdatedAt(now);
        playerQuestMapper.updateById(playerQuest);
        return playerQuest;
    }

    @Transactional
    public void claimQuestReward(Integer playerId, Integer questId) {
        PlayerQuest playerQuest = playerQuestMapper.selectByPlayerIdAndQuestId(playerId, questId);
        if (playerQuest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "任务不存在");
        }
        claimQuestRewardInternal(playerId, playerQuest.getId());
    }

    @Transactional
    public void claimQuestRewardByPlayerQuestId(Integer playerId, Integer playerQuestId) {
        PlayerQuest playerQuest = playerQuestMapper.selectById(playerQuestId);
        if (playerQuest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "任务不存在");
        }
        if (!playerQuest.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作其他玩家的任务");
        }
        claimQuestRewardInternal(playerId, playerQuestId);
    }

    private void claimQuestRewardInternal(Integer playerId, Integer playerQuestId) {
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        PlayerQuest playerQuest = playerQuestMapper.selectById(playerQuestId);
        if (playerQuest == null || !playerQuest.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "任务不存在");
        }
        if (!Boolean.TRUE.equals(playerQuest.getCompleted())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "任务未完成");
        }
        if (Boolean.TRUE.equals(playerQuest.getRewardClaimed())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "奖励已领取");
        }

        Quest quest = questMapper.selectById(playerQuest.getQuestId());
        if (quest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "任务不存在");
        }

        int claimedRows = playerQuestMapper.claimRewardIfUnclaimed(playerQuestId, playerId, LocalDateTime.now());
        if (claimedRows == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "奖励已领取");
        }

        applyQuestReward(player, quest);
        playerService.applyLevelUpsWithoutCommit(player, 100);
        playerService.savePlayerProfile(player);
    }

    private void applyQuestReward(PlayerProfile player, Quest quest) {
        player.setExp(defaultLong(player.getExp()) + defaultInt(quest.getRewardExp()));
        player.setSpiritStones(defaultLong(player.getSpiritStones()) + defaultInt(quest.getRewardSpiritStones()));
        player.setContributionPoints(defaultLong(player.getContributionPoints()) + defaultInt(quest.getRewardContributionPoints()));

        int attributePointsReward = defaultInt(quest.getRewardExp()) / 100;
        if (attributePointsReward > 0) {
            player.setAttributePoints(defaultInt(player.getAttributePoints()) + attributePointsReward);
        }
    }

    // 根据类型获取玩家任务（利用DB侧过滤，避免N+1）
    public List<PlayerQuest> getPlayerQuestsByType(Integer playerId, Quest.QuestType type) {
        List<PlayerQuest> filtered = playerQuestMapper.selectByPlayerIdAndQuestType(playerId, type.toString());

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
        List<PlayerQuest> list = getPlayerQuestsByType(playerId, type);
        if (list.isEmpty()) return new ArrayList<>();
        List<Integer> questIds = list.stream().map(PlayerQuest::getQuestId).distinct().collect(Collectors.toList());
        Map<Integer, Quest> questMap = questMapper.selectBatchIds(questIds)
                .stream().collect(Collectors.toMap(Quest::getId, q -> q, (a, b) -> a));
        return list.stream().map(pq -> toDetail(pq, questMap.get(pq.getQuestId()))).collect(Collectors.toList());
    }

    // 批量领取所有已完成任务奖励（内联逻辑，避免嵌套事务 + N+1）
    @Transactional
    public int claimAllCompletedQuestRewards(Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectByIdForUpdate(playerId);
        if (player == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        List<PlayerQuest> allQuests = playerQuestMapper.selectByPlayerId(playerId);
        List<PlayerQuest> claimable = allQuests.stream()
                .filter(pq -> Boolean.TRUE.equals(pq.getCompleted()) && !Boolean.TRUE.equals(pq.getRewardClaimed()))
                .collect(Collectors.toList());
        if (claimable.isEmpty()) return 0;

        // 批量加载quest（避免循环selectById）
        List<Integer> questIds = claimable.stream().map(PlayerQuest::getQuestId).distinct().collect(Collectors.toList());
        Map<Integer, Quest> questMap = questMapper.selectBatchIds(questIds)
                .stream().collect(Collectors.toMap(Quest::getId, q -> q, (a, b) -> a));

        LocalDateTime now = LocalDateTime.now();
        int claimedCount = 0;
        for (PlayerQuest pq : claimable) {
            Quest quest = questMap.get(pq.getQuestId());
            if (quest == null) continue;

            int updatedRows = playerQuestMapper.claimRewardIfUnclaimed(pq.getId(), playerId, now);
            if (updatedRows == 0) {
                continue;
            }

            applyQuestReward(player, quest);
            claimedCount++;
        }
        if (claimedCount > 0) {
            playerService.applyLevelUpsWithoutCommit(player, 100);
            playerService.savePlayerProfile(player);
        }
        return claimedCount;
    }

    // 检查是否有未完成的任务
    public boolean hasIncompleteQuests(Integer playerId) {
        List<PlayerQuest> allQuests = playerQuestMapper.selectByPlayerId(playerId);
        
        return allQuests.stream().anyMatch(pq -> !Boolean.TRUE.equals(pq.getCompleted()));
    }

    // 获取已完成但未领取奖励的任务数量（利用Mapper层的聚合查询）
    public long getUnclaimedCompletedQuestsCount(Integer playerId) {
        return playerQuestMapper.countByPlayerIdAndCompletedAndRewardClaimed(playerId, true, false);
    }

    // 根据任务类型更新进度
    @Transactional
    public void updateQuestProgressByType(Integer playerId, Quest.QuestType questType, int progress) {
        List<PlayerQuest> quests = getPlayerQuestsByType(playerId, questType);
        
        for (PlayerQuest pq : quests) {
            if (!Boolean.TRUE.equals(pq.getCompleted())) {
                updateQuestProgressInternal(playerId, pq.getQuestId(), progress);
            }
        }
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private PlayerQuestDetailResponse toDetail(PlayerQuest pq, Quest q) {
        if (q == null) return null;
        QuestResponse qr = QuestResponse.builder()
                .id(q.getId())
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
