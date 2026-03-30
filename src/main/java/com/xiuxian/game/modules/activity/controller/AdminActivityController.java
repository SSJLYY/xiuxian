package com.xiuxian.game.modules.activity.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.activity.entity.Activity;
import com.xiuxian.game.modules.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员活动控制器
 *
 * <p>处理管理员活动管理相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>创建活动</li>
 *   <li>更新活动</li>
 *   <li>删除活动</li>
 *   <li>获取所有活动列表</li>
 *   <li>获取活动详情</li>
 * </ul>
 *
 * <p>所有接口都需要ADMIN角色权限。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;

    /**
     * 创建活动
     *
     * <p>管理员创建新的活动。</p>
     *
     * @param activity 活动信息
     * @return 创建的活动
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Activity> createActivity(@RequestBody Activity activity) {
        try {
            log.info("管理员创建活动: name={}, type={}", activity.getName(), activity.getActivityType());
            
            activityService.save(activity);
            
            log.info("管理员创建活动成功: activityId={}", activity.getId());
            return ApiResponse.success("创建成功", activity);
        } catch (Exception e) {
            log.error("管理员创建活动失败: error={}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新活动
     *
     * <p>管理员更新活动信息。</p>
     *
     * @param id 活动ID
     * @param activity 活动信息
     * @return 更新后的活动
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Activity> updateActivity(@PathVariable Integer id, @RequestBody Activity activity) {
        try {
            log.info("管理员更新活动: activityId={}", id);
            
            activity.setId(id);
            activityService.updateById(activity);
            
            log.info("管理员更新活动成功: activityId={}", id);
            return ApiResponse.success("更新成功", activity);
        } catch (Exception e) {
            log.error("管理员更新活动失败: activityId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除活动
     *
     * <p>管理员删除活动。</p>
     *
     * @param id 活动ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteActivity(@PathVariable Integer id) {
        try {
            log.info("管理员删除活动: activityId={}", id);
            
            activityService.removeById(id);
            
            log.info("管理员删除活动成功: activityId={}", id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("管理员删除活动失败: activityId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有活动（管理员）
     *
     * <p>管理员查看所有活动列表。</p>
     *
     * @return 活动列表
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Activity>> getAllActivities() {
        try {
            log.debug("管理员获取所有活动");
            
            List<Activity> activities = activityService.list();
            
            log.debug("管理员获取所有活动成功: count={}", activities.size());
            return ApiResponse.success("获取成功", activities);
        } catch (Exception e) {
            log.error("管理员获取所有活动失败: error={}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取单个活动详情
     *
     * <p>管理员查看指定活动详情。</p>
     *
     * @param id 活动ID
     * @return 活动详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Activity> getActivity(@PathVariable Integer id) {
        try {
            log.debug("管理员获取活动详情: activityId={}", id);
            
            Activity activity = activityService.getById(id);
            if (activity == null) {
                return ApiResponse.error("活动不存在");
            }
            
            log.debug("管理员获取活动详情成功: activityId={}", id);
            return ApiResponse.success("获取成功", activity);
        } catch (Exception e) {
            log.error("管理员获取活动详情失败: activityId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
