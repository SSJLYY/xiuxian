package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.player.service.AccountSecurityService;
import com.xiuxian.game.modules.admin.service.AdminOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * 管理后台安全管理 Controller
 * 提供 IP 黑名单管理、账号封禁/解封、强制下线等安全操作接口
 *
 * @author shaun.sheng
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSecurityController {
    
    private final AccountSecurityService accountSecurityService;
    private final AdminOperationLogService adminOperationLogService;
    
    /**
     * 查询 IP 黑名单列表
     *
     * @return 当前所有被封禁的 IP 集合
     */
    @GetMapping("/blacklist")
    public ApiResponse<Set<String>> getBlacklist() {
        try {
            Set<String> blacklist = accountSecurityService.getBlacklistedIps();
            return ApiResponse.success(blacklist);
        } catch (Exception e) {
            log.error("查询 IP 黑名单失败", e);
            return ApiResponse.error("查询失败");
        }
    }
    
    /**
     * 将 IP 加入黑名单
     *
     * @param ipAddress IP 地址
     * @param reason    封禁原因
     * @param request   HTTP 请求（用于记录操作日志）
     * @return 操作结果
     */
    @PostMapping("/blacklist")
    public ApiResponse<Void> addToBlacklist(@RequestParam String ipAddress, 
                                          @RequestParam String reason,
                                          HttpServletRequest request) {
        try {
            accountSecurityService.addToBlacklist(ipAddress, reason);
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "ADD_IP_BLACKLIST", "IP", 
                    ipAddress, "将 IP 加入黑名单: " + reason, request);
            
            return ApiResponse.success("加入黑名单成功", null);
        } catch (Exception e) {
            log.error("将 IP 加入黑名单失败: ip={}", ipAddress, e);
            return ApiResponse.error("操作失败");
        }
    }
    
    /**
     * 将 IP 从黑名单移除
     *
     * @param ipAddress IP 地址
     * @param request   HTTP 请求（用于记录操作日志）
     * @return 操作结果
     */
    @DeleteMapping("/blacklist")
    public ApiResponse<Void> removeFromBlacklist(@RequestParam String ipAddress,
                                               HttpServletRequest request) {
        try {
            accountSecurityService.removeFromBlacklist(ipAddress);
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "REMOVE_IP_BLACKLIST", "IP", 
                    ipAddress, "将 IP 从黑名单移除", request);
            
            return ApiResponse.success("移除黑名单成功", null);
        } catch (Exception e) {
            log.error("将 IP 从黑名单移除失败: ip={}", ipAddress, e);
            return ApiResponse.error("操作失败");
        }
    }
    
    /**
     * 封禁账号
     *
     * @param userId  用户 ID
     * @param reason  封禁原因
     * @param request HTTP 请求（用于记录操作日志）
     * @return 操作结果
     */
    @PostMapping("/ban-account")
    public ApiResponse<Void> banAccount(@RequestParam Integer userId, 
                                      @RequestParam String reason,
                                      HttpServletRequest request) {
        try {
            accountSecurityService.banAccount(userId, reason);
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "BAN_ACCOUNT", "USER", 
                    userId.toString(), "封禁账号: " + reason, request);
            
            return ApiResponse.success("账号封禁成功", null);
        } catch (Exception e) {
            log.error("封禁账号失败: userId={}", userId, e);
            return ApiResponse.error("封禁失败");
        }
    }
    
    /**
     * 解封账号
     *
     * @param userId  用户 ID
     * @param request HTTP 请求（用于记录操作日志）
     * @return 操作结果
     */
    @PostMapping("/unban-account")
    public ApiResponse<Void> unbanAccount(@RequestParam Integer userId,
                                        HttpServletRequest request) {
        try {
            accountSecurityService.unbanAccount(userId);
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "UNBAN_ACCOUNT", "USER", 
                    userId.toString(), "解封账号", request);
            
            return ApiResponse.success("账号解封成功", null);
        } catch (Exception e) {
            log.error("解封账号失败: userId={}", userId, e);
            return ApiResponse.error("解封失败");
        }
    }
    
    /**
     * 强制下线指定用户
     *
     * @param userId  用户 ID
     * @param reason  强制下线原因
     * @param request HTTP 请求（用于记录操作日志）
     * @return 操作结果
     */
    @PostMapping("/force-logout")
    public ApiResponse<Void> forceLogout(@RequestParam Integer userId, 
                                       @RequestParam String reason,
                                       HttpServletRequest request) {
        try {
            accountSecurityService.forceLogout(userId, reason);
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "FORCE_LOGOUT", "USER", 
                    userId.toString(), "强制下线: " + reason, request);
            
            return ApiResponse.success("强制下线成功", null);
        } catch (Exception e) {
            log.error("强制下线失败: userId={}", userId, e);
            return ApiResponse.error("操作失败");
        }
    }
    
    /**
     * 获取当前在线用户数
     *
     * @return 在线用户数
     */
    @GetMapping("/online-count")
    public ApiResponse<Integer> getOnlineCount() {
        try {
            int count = accountSecurityService.getOnlineUserCount();
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("获取在线用户数失败", e);
            return ApiResponse.error("查询失败");
        }
    }
}
