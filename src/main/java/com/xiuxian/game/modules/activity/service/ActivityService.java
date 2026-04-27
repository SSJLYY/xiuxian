package com.xiuxian.game.modules.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiuxian.game.modules.activity.entity.Activity;
import com.xiuxian.game.modules.activity.entity.PlayerActivityProgress;
import com.xiuxian.game.modules.activity.mapper.ActivityMapper;
import com.xiuxian.game.modules.activity.mapper.PlayerActivityProgressMapper;
import com.xiuxian.game.modules.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

/**
 * 活动服务类
 *
 * <p>提供活动相关的业务逻辑，包括：</p>
 * <ul>
 *   <li>活动查询</li>
 *   <li>活动参与</li>
 *   <li>进度更新</li>
 *   <li>奖励发放</li>
 *   <li>状态管理</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {

    private static final Pattern VALUE_PATTERN = Pattern.compile("\"value\"\\s*:\\s*(-?\\d+)");
    private static final Pattern SCORE_PATTERN = Pattern.compile("\"score\"\\s*:\\s*(-?\\d+)");

    private final ActivityMapper activityMapper;
    private final PlayerActivityProgressMapper playerActivityProgressMapper;
    private final MailService mailService;

    /**
     * 获取所有正在进行的活动
     *
     * <p>返回当前时间范围内正在进行的活动列表。</p>
     *
     * @return 活动列表
     */
    public List<Activity> getActiveActivities() {
        log.debug("获取正在进行的活动");
        
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE");
        queryWrapper.le("start_time", LocalDateTime.now());
        queryWrapper.ge("end_time", LocalDateTime.now());
        List<Activity> activities = activityMapper.selectList(queryWrapper);
        
        log.debug("获取正在进行的活动成功: count={}", activities.size());
        return activities;
    }

    /**
     * 获取所有活动（包括已结束的）
     *
     * <p>返回所有活动列表，包括已结束的活动。</p>
     *
     * @return 活动列表
     */
    public List<Activity> getAllActivities() {
        log.debug("获取所有活动");
        
        List<Activity> activities = activityMapper.selectList(null);
        
        log.debug("获取所有活动成功: count={}", activities.size());
        return activities;
    }

    /**
     * 获取玩家参与的活动进度
     *
     * <p>返回指定玩家参与的所有活动进度。</p>
     *
     * @param playerId 玩家ID
     * @return 活动进度列表
     */
    public List<PlayerActivityProgress> getPlayerActivityProgress(Integer playerId) {
        log.debug("获取玩家活动进度: playerId={}", playerId);
        
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        List<PlayerActivityProgress> progresses = playerActivityProgressMapper.selectList(queryWrapper);
        
        log.debug("获取玩家活动进度成功: playerId={}, count={}", playerId, progresses.size());
        return progresses;
    }

    /**
     * 参与活动
     *
     * <p>玩家参与指定活动，创建活动进度记录。</p>
     *
     * @param playerId 玩家ID
     * @param activityId 活动ID
     * @return 活动进度记录
     */
    @Transactional
    public PlayerActivityProgress participateInActivity(Integer playerId, Integer activityId) {
        log.info("玩家参与活动: playerId={}, activityId={}", playerId, activityId);
        
        // 检查活动是否存在且处于活跃状态
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在: activityId={}", activityId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "活动不存在");
        }

        if (!"ACTIVE".equals(activity.getStatus()) ||
            activity.getStartTime().isAfter(LocalDateTime.now()) ||
            activity.getEndTime().isBefore(LocalDateTime.now())) {
            log.warn("活动不在进行中: activityId={}, status={}", activityId, activity.getStatus());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "活动不在进行中");
        }

        // 检查玩家是否已有进度记录
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.eq("activity_id", activityId);
        PlayerActivityProgress progress = playerActivityProgressMapper.selectOne(queryWrapper);

        if (progress == null) {
            // 创建新的进度记录
            progress = new PlayerActivityProgress();
            progress.setActivityId(activityId);
            progress.setPlayerId(playerId);
            progress.setProgress(buildProgressJson(0, 0));
            progress.setCompleted(false);
            progress.setRewarded(false);
            playerActivityProgressMapper.insert(progress);
            log.info("创建活动进度记录: playerId={}, activityId={}", playerId, activityId);
        }

        log.info("玩家参与活动成功: playerId={}, activityId={}", playerId, activityId);
        return progress;
    }

    /**
     * 更新玩家活动进度
     *
     * <p>更新玩家在指定活动中的进度。进度存储为JSON格式：{"value": 100}</p>
     *
     * @param playerId 玩家ID
     * @param activityId 活动ID
     * @param increment 进度增量
     * @return 更新后的活动进度
     */
    @Transactional
    public PlayerActivityProgress updateActivityProgress(Integer playerId, Integer activityId, int increment) {
        log.info("更新活动进度: playerId={}, activityId={}, increment={}", playerId, activityId, increment);
        
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.eq("activity_id", activityId);
        PlayerActivityProgress progress = playerActivityProgressMapper.selectOne(queryWrapper);

        if (progress == null) {
            log.warn("玩家未参与活动: playerId={}, activityId={}", playerId, activityId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家未参与该活动");
        }

        // 解析JSON进度并更新
        int currentValue = parseProgressValue(progress.getProgress());
        int currentScore = parseScoreFromProgress(progress.getProgress());
        currentValue += increment;
        progress.setProgress(buildProgressJson(currentValue, currentScore));
        progress.setUpdatedAt(LocalDateTime.now());
        playerActivityProgressMapper.updateById(progress);

        log.info("更新活动进度成功: playerId={}, activityId={}, newValue={}", playerId, activityId, currentValue);
        return progress;
    }

    /**
     * 解析进度JSON中的数值
     */
    private int parseProgressValue(String progressJson) {
        if (progressJson == null || progressJson.isEmpty()) {
            return 0;
        }
        try {
            // 简单解析 {"value": 100}
            Integer value = extractFieldValue(progressJson, VALUE_PATTERN);
            if (value != null) {
                return value;
            }
            String trimmed = progressJson.trim();
            return trimmed.matches("-?\\d+") ? Integer.parseInt(trimmed) : 0;
        } catch (NumberFormatException e) {
            log.warn("解析进度失败: {}", progressJson);
            return 0;
        }
    }

    /**
     * 更新玩家活动积分
     *
     * <p>更新玩家在指定活动中的积分。积分存储为JSON格式：{"score": 100}</p>
     *
     * @param playerId 玩家ID
     * @param activityId 活动ID
     * @param score 积分值（绝对值，会覆盖原有积分）
     * @return 更新后的活动进度
     */
    @Transactional
    public PlayerActivityProgress updateActivityScore(Integer playerId, Integer activityId, int score) {
        log.info("更新活动积分: playerId={}, activityId={}, score={}", playerId, activityId, score);
        
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.eq("activity_id", activityId);
        PlayerActivityProgress progress = playerActivityProgressMapper.selectOne(queryWrapper);

        if (progress == null) {
            log.warn("玩家未参与活动: playerId={}, activityId={}", playerId, activityId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家未参与该活动");
        }

        // 解析现有progress JSON，更新score字段
        String currentProgress = progress.getProgress();
        int currentValue = parseProgressValue(currentProgress);
        progress.setProgress(buildProgressJson(currentValue, score));
        progress.setUpdatedAt(LocalDateTime.now());
        playerActivityProgressMapper.updateById(progress);

        log.info("更新活动积分成功: playerId={}, activityId={}, newScore={}", playerId, activityId, score);
        return progress;
    }

    /**
     * 从进度JSON中解析积分
     */
    private int parseScoreFromProgress(String progressJson) {
        if (progressJson == null || progressJson.isEmpty()) {
            return 0;
        }
        try {
            Integer score = extractFieldValue(progressJson, SCORE_PATTERN);
            if (score != null) {
                return score;
            }
            return 0;
        } catch (NumberFormatException e) {
            log.warn("解析积分失败: {}", progressJson);
            return 0;
        }
    }

    private Integer extractFieldValue(String json, Pattern pattern) {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private String buildProgressJson(int value, int score) {
        return String.format("{\"value\": %d, \"score\": %d}", value, score);
    }

    /**
     * 发放活动奖励
     *
     * <p>向参与指定活动的所有玩家发放奖励。</p>
     *
     * @param activityId 活动ID
     */
    @Transactional
    public void distributeActivityRewards(Integer activityId) {
        log.info("发放活动奖励: activityId={}", activityId);
        
        // 获取活动信息
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在: activityId={}", activityId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "活动不存在");
        }

        // 获取所有参与该活动的玩家进度
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id", activityId);
        List<PlayerActivityProgress> progresses = playerActivityProgressMapper.selectList(queryWrapper);

        // 发放奖励（这里只是一个示例，实际奖励逻辑会更复杂）
        for (PlayerActivityProgress progress : progresses) {
            // 发送邮件通知玩家活动结束和奖励
            String subject = "活动结束通知";
            String content = String.format("活动【%s】已结束，感谢您的参与！您的最终积分为：%s，排名将在稍后公布。",
                    activity.getName(), progress.getProgress()); // 使用progress字段代替score

            mailService.sendSystemMail(progress.getPlayerId(), subject, content, null, null, 0);
            log.debug("发送活动奖励邮件: playerId={}, activityId={}", progress.getPlayerId(), activityId);
        }
        
        log.info("发放活动奖励完成: activityId={}, count={}", activityId, progresses.size());
    }

    /**
     * 自动检查和更新活动状态
     *
     * <p>定时任务，每分钟检查一次活动状态。</p>
     *
     * <p>事务保护：活动状态更新（DRAFT→ACTIVE、ACTIVE→ENDED）与奖励分发在同一事务中，
     * 避免状态已变但奖励未发出的不一致问题。</p>
     *
     * <p>幂等性：状态更新是幂等的（重复设置同一状态无副作用）；
     * 奖励分发通过 MailService.sendSystemMail 有邮箱容量校验天然防重。</p>
     */
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpdateActivityStatus() {
        log.debug("检查活动状态");
        
        LocalDateTime now = LocalDateTime.now();

        // 检查需要开始的活动
        QueryWrapper<Activity> startQuery = new QueryWrapper<>();
        startQuery.eq("status", "DRAFT");
        startQuery.le("start_time", now);
        List<Activity> activitiesToStart = activityMapper.selectList(startQuery);

        for (Activity activity : activitiesToStart) {
            activity.setStatus("ACTIVE");
            activityMapper.updateById(activity);
            log.debug("活动开始: activityId={}, name={}", activity.getId(), activity.getName());
        }

        // 检查需要结束的活动
        QueryWrapper<Activity> endQuery = new QueryWrapper<>();
        endQuery.eq("status", "ACTIVE");
        endQuery.lt("end_time", now);
        List<Activity> activitiesToEnd = activityMapper.selectList(endQuery);

        for (Activity activity : activitiesToEnd) {
            activity.setStatus("ENDED");
            activityMapper.updateById(activity);
            log.debug("活动结束: activityId={}, name={}", activity.getId(), activity.getName());

            // 发放奖励（同一事务内，distributeActivityRewards 的 @Transactional 会加入当前事务）
            distributeActivityRewards(activity.getId());
        }
        
        log.debug("检查活动状态完成: started={}, ended={}", activitiesToStart.size(), activitiesToEnd.size());
    }

    /**
     * 获取活动排名
     *
     * <p>获取指定活动的玩家排名列表。由于progress是JSON格式，在内存中进行排序。</p>
     *
     * @param activityId 活动ID
     * @param limit 返回数量限制
     * @return 排名列表
     */
    public List<PlayerActivityProgress> getActivityRanking(Integer activityId, int limit) {
        log.debug("获取活动排名: activityId={}, limit={}", activityId, limit);
        
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id", activityId);
        // 不在SQL中排序，因为在内存中按score排序
        List<PlayerActivityProgress> ranking = playerActivityProgressMapper.selectList(queryWrapper);
        
        // 按积分在内存中排序
        ranking.sort((a, b) -> {
            int scoreA = parseScoreFromProgress(a.getProgress());
            int scoreB = parseScoreFromProgress(b.getProgress());
            return Integer.compare(scoreB, scoreA); // 降序
        });
        
        // 限制返回数量
        if (ranking.size() > limit) {
            ranking = ranking.subList(0, limit);
        }
        
        log.debug("获取活动排名成功: activityId={}, count={}", activityId, ranking.size());
        return ranking;
    }
}
