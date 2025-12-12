package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.Activity;
import com.xiuxian.game.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;

    /**
     * 创建活动
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Activity> createActivity(@RequestBody Activity activity) {
        try {
            activityService.save(activity);
            return ApiResponse.success("创建成功", activity);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Activity> updateActivity(@PathVariable Integer id, @RequestBody Activity activity) {
        try {
            activity.setId(id);
            activityService.updateById(activity);
            return ApiResponse.success("更新成功", activity);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteActivity(@PathVariable Integer id) {
        try {
            activityService.removeById(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有活动（管理员）
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Activity>> getAllActivities() {
        try {
            List<Activity> activities = activityService.list();
            return ApiResponse.success("获取成功", activities);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取单个活动详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Activity> getActivity(@PathVariable Integer id) {
        try {
            Activity activity = activityService.getById(id);
            if (activity == null) {
                return ApiResponse.error("活动不存在");
            }
            return ApiResponse.success("获取成功", activity);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}