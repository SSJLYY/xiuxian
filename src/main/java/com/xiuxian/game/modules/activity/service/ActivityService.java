package com.xiuxian.game.modules.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiuxian.game.modules.activity.entity.Activity;
import com.xiuxian.game.modules.activity.entity.PlayerActivityProgress;
import com.xiuxian.game.modules.activity.mapper.ActivityMapper;
import com.xiuxian.game.modules.activity.mapper.PlayerActivityProgressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {

    private final ActivityMapper activityMapper;
    private final PlayerActivityProgressMapper playerActivityProgressMapper;
    private final MailService mailService;

    /**
     * 获取所有正在进行的活动
     */
    public List<Activity> getActiveActivities() {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE");
        queryWrapper.le("start_time", LocalDateTime.now());
        queryWrapper.ge("end_time", LocalDateTime.now());
        return activityMapper.selectList(queryWrapper);
    }

    /**
     * 获取所有活动（包括已结束的�?
     */
    public List<Activity> getAllActivities() {
        return activityMapper.selectList(null);
    }

    /**
     * 获取玩家参与的活动进�?
     */
    public List<PlayerActivityProgress> getPlayerActivityProgress(Integer playerId) {
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        return playerActivityProgressMapper.selectList(queryWrapper);
    }

    /**
     * 参与活动
     */
    @Transactional
    public PlayerActivityProgress participateInActivity(Integer playerId, Integer activityId) {
        // 检查活动是否存在且处于活跃状�?
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存�?);
        }

        if (!"ACTIVE".equals(activity.getStatus()) || 
            activity.getStartTime().isAfter(LocalDateTime.now()) ||
            activity.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("活动不在进行�?);
        }

        // 检查玩家是否已有进度记�?
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.eq("activity_id", activityId);
        PlayerActivityProgress progress = playerActivityProgressMapper.selectOne(queryWrapper);

        if (progress == null) {
            // 创建新的进度记录
            progress = new PlayerActivityProgress();
            progress.setActivityId(activityId);
            progress.setPlayerId(playerId);
            progress.setProgress("{\"value\": 0}"); // JSON格式的进度数�?
            progress.setCompleted(false);
            progress.setRewarded(false);
            playerActivityProgressMapper.insert(progress);
        }

        return progress;
    }

    /**
     * 更新玩家活动进度
     */
    @Transactional
    public PlayerActivityProgress updateActivityProgress(Integer playerId, Integer activityId, int increment) {
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.eq("activity_id", activityId);
        PlayerActivityProgress progress = playerActivityProgressMapper.selectOne(queryWrapper);

        if (progress == null) {
            throw new IllegalArgumentException("玩家未参与该活动");
        }

        // 更新进度
        progress.setProgress(progress.getProgress() + increment);
        progress.setUpdatedAt(LocalDateTime.now());
        playerActivityProgressMapper.updateById(progress);

        return progress;
    }

    /**
     * 更新玩家活动积分
     */
    @Transactional
    public PlayerActivityProgress updateActivityScore(Integer playerId, Integer activityId, int score) {
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        queryWrapper.eq("activity_id", activityId);
        PlayerActivityProgress progress = playerActivityProgressMapper.selectOne(queryWrapper);

        if (progress == null) {
            throw new IllegalArgumentException("玩家未参与该活动");
        }

        // 更新积分（从progress JSON中提取并更新�?
        // 注意：这里简化处理，实际应该解析JSON并更�?
        progress.setUpdatedAt(LocalDateTime.now());
        playerActivityProgressMapper.updateById(progress);

        return progress;
    }

    /**
     * 发放活动奖励
     */
    @Transactional
    public void distributeActivityRewards(Integer activityId) {
        // 获取活动信息
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存�?);
        }

        // 获取所有参与该活动的玩家进�?
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id", activityId);
        List<PlayerActivityProgress> progresses = playerActivityProgressMapper.selectList(queryWrapper);

        // 发放奖励（这里只是一个示例，实际奖励逻辑会更复杂�?
        for (PlayerActivityProgress progress : progresses) {
            // 发送邮件通知玩家活动结束和奖�?
            String subject = "活动结束通知";
            String content = String.format("活动�?s》已结束，感谢您的参与！您的最终积分为�?s，排名将在稍后公布�?,
                    activity.getName(), progress.getProgress()); // 使用progress字段代替score

            mailService.sendSystemMail(progress.getPlayerId(), subject, content, null, null, 0);
        }
    }

    /**
     * 自动检查和更新活动状�?
     */
    @Scheduled(fixedRate = 60000) // 每分钟检查一�?
    public void checkAndUpdateActivityStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 检查需要开始的活动
        QueryWrapper<Activity> startQuery = new QueryWrapper<>();
        startQuery.eq("status", "DRAFT");
        startQuery.le("start_time", now);
        List<Activity> activitiesToStart = activityMapper.selectList(startQuery);

        for (Activity activity : activitiesToStart) {
            activity.setStatus("ACTIVE");
            activityMapper.updateById(activity);
        }

        // 检查需要结束的活动
        QueryWrapper<Activity> endQuery = new QueryWrapper<>();
        endQuery.eq("status", "ACTIVE");
        endQuery.lt("end_time", now);
        List<Activity> activitiesToEnd = activityMapper.selectList(endQuery);

        for (Activity activity : activitiesToEnd) {
            activity.setStatus("ENDED");
            activityMapper.updateById(activity);

            // 发放奖励
            distributeActivityRewards(activity.getId());
        }
    }

    /**
     * 获取活动排名
     */
    public List<PlayerActivityProgress> getActivityRanking(Integer activityId, int limit) {
        QueryWrapper<PlayerActivityProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id", activityId);
        queryWrapper.orderByDesc("score");
        queryWrapper.last("LIMIT " + limit);
        return playerActivityProgressMapper.selectList(queryWrapper);
    }
}
