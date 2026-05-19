package com.xiuxian.game.modules.activity.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.activity.entity.Activity;
import com.xiuxian.game.modules.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Activity> createActivity(@RequestBody Activity activity) {
        try {
            log.info("创建活动: name={}, type={}", activity.getName(), activity.getActivityType());
            activityService.save(activity);
            log.info("创建活动成功: activityId={}", activity.getId());
            return ApiResponse.success("创建成功", activity);
        } catch (Exception e) {
            log.error("创建活动失败: error={}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Activity> updateActivity(@PathVariable Integer id, @RequestBody Activity activity) {
        try {
            log.info("更新活动: activityId={}", id);
            activity.setId(id);
            boolean updated = activityService.updateById(activity);
            if (!updated) {
                log.warn("更新活动失败，活动不存在: activityId={}", id);
                return ApiResponse.error("活动不存在");
            }
            log.info("更新活动成功: activityId={}", id);
            return ApiResponse.success("更新成功", activity);
        } catch (Exception e) {
            log.error("更新活动失败: activityId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteActivity(@PathVariable Integer id) {
        try {
            log.info("删除活动: activityId={}", id);
            boolean removed = activityService.removeById(id);
            if (!removed) {
                log.warn("删除活动失败，活动不存在: activityId={}", id);
                return ApiResponse.error("活动不存在");
            }
            log.info("删除活动成功: activityId={}", id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除活动失败: activityId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Activity>> getAllActivities() {
        try {
            log.debug("获取所有活动");
            List<Activity> activities = activityService.list();
            log.debug("获取所有活动成功: count={}", activities.size());
            return ApiResponse.success("获取成功", activities);
        } catch (Exception e) {
            log.error("获取所有活动失败: error={}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Activity> getActivity(@PathVariable Integer id) {
        try {
            log.debug("获取活动详情: activityId={}", id);
            Activity activity = activityService.getById(id);
            if (activity == null) {
                return ApiResponse.error("活动不存在");
            }
            log.debug("获取活动详情成功: activityId={}", id);
            return ApiResponse.success("获取成功", activity);
        } catch (Exception e) {
            log.error("获取活动详情失败: activityId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}