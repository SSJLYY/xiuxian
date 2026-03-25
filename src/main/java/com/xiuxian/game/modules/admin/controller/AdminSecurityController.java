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
 * 管理员安全管理控制器
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
     * 获取IP黑名�?
     */
    @GetMapping("/blacklist")
    public ApiResponse<Set<String>> getBlacklist() {
        try {
            Set<String> blacklist = accountSecurityService.getBlacklistedIps();
            return ApiResponse.success(blacklist);
        } catch (Exception e) {
            log.error("获取IP黑名单失�?, e);
            return ApiResponse.error("获取失败");
        }
    }
    
    /**
     * 添加IP到黑名单
     */
    @PostMapping("/blacklist")
    public ApiResponse<Void> addToBlacklist(@RequestParam String ipAddress, 
                                          @RequestParam String reason,
                                          HttpServletRequest request) {
        try {
            accountSecurityService.addToBlacklist(ipAddress, reason);
            
            // 记录管理员操作日�?
            adminOperationLogService.recordOperation(1, "ADD_IP_BLACKLIST", "IP", 
                    ipAddress, "添加IP到黑名单: " + reason, request);
            
            return ApiResponse.success("添加成功", null);
        } catch (Exception e) {
            log.error("添加IP到黑名单失败: ip={}", ipAddress, e);
            return ApiResponse.error("添加失败");
        }
    }
    
    /**
     * 从黑名单移除IP
     */
    @DeleteMapping("/blacklist")
    public ApiResponse<Void> removeFromBlacklist(@RequestParam String ipAddress,
                                               HttpServletRequest request) {
        try {
            accountSecurityService.removeFromBlacklist(ipAddress);
            
            // 记录管理员操作日�?
            adminOperationLogService.recordOperation(1, "REMOVE_IP_BLACKLIST", "IP", 
                    ipAddress, "从黑名单移除IP", request);
            
            return ApiResponse.success("移除成功", null);
        } catch (Exception e) {
            log.error("从黑名单移除IP失败: ip={}", ipAddress, e);
            return ApiResponse.error("移除失败");
        }
    }
    
    /**
     * 封禁账号
     */
    @PostMapping("/ban-account")
    public ApiResponse<Void> banAccount(@RequestParam Integer userId, 
                                      @RequestParam String reason,
                                      HttpServletRequest request) {
        try {
            accountSecurityService.banAccount(userId, reason);
            
            // 记录管理员操作日�?
            adminOperationLogService.recordOperation(1, "BAN_ACCOUNT", "USER", 
                    userId.toString(), "封禁账号: " + reason, request);
            
            return ApiResponse.success("封禁成功", null);
        } catch (Exception e) {
            log.error("封禁账号失败: userId={}", userId, e);
            return ApiResponse.error("封禁失败");
        }
    }
    
    /**
     * 解封账号
     */
    @PostMapping("/unban-account")
    public ApiResponse<Void> unbanAccount(@RequestParam Integer userId,
                                        HttpServletRequest request) {
        try {
            accountSecurityService.unbanAccount(userId);
            
            // 记录管理员操作日�?
            adminOperationLogService.recordOperation(1, "UNBAN_ACCOUNT", "USER", 
                    userId.toString(), "解封账号", request);
            
            return ApiResponse.success("解封成功", null);
        } catch (Exception e) {
            log.error("解封账号失败: userId={}", userId, e);
            return ApiResponse.error("解封失败");
        }
    }
    
    /**
     * 强制用户下线
     */
    @PostMapping("/force-logout")
    public ApiResponse<Void> forceLogout(@RequestParam Integer userId, 
                                       @RequestParam String reason,
                                       HttpServletRequest request) {
        try {
            accountSecurityService.forceLogout(userId, reason);
            
            // 记录管理员操作日�?
            adminOperationLogService.recordOperation(1, "FORCE_LOGOUT", "USER", 
                    userId.toString(), "强制下线: " + reason, request);
            
            return ApiResponse.success("操作成功", null);
        } catch (Exception e) {
            log.error("强制下线失败: userId={}", userId, e);
            return ApiResponse.error("操作失败");
        }
    }
    
    /**
     * 获取在线用户数量
     */
    @GetMapping("/online-count")
    public ApiResponse<Integer> getOnlineCount() {
        try {
            int count = accountSecurityService.getOnlineUserCount();
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("获取在线用户数量失败", e);
            return ApiResponse.error("获取失败");
        }
    }
}
