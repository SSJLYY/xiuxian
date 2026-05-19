package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import com.xiuxian.game.modules.admin.service.AdminOperationLogService;
import com.xiuxian.game.modules.player.service.AccountSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminSecurityController {

    private final AccountSecurityService accountSecurityService;
    private final AdminOperationLogService adminOperationLogService;
    private final AdminAuthService adminAuthService;

    private Integer getAdminId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 0;
        }
        String token = authHeader.substring(7);
        return adminAuthService.isValidAdminToken(token) ? 1 : 0;
    }

    @GetMapping("/blacklist")
    public ResponseEntity<ApiResponse<Set<String>>> getBlacklist() {
        try {
            Set<String> blacklist = accountSecurityService.getBlacklistedIps();
            return ResponseEntity.ok(ApiResponse.success(blacklist));
        } catch (Exception e) {
            log.error("查询 IP 黑名单失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("查询失败"));
        }
    }

    @PostMapping("/blacklist")
    public ResponseEntity<ApiResponse<Void>> addToBlacklist(
            @RequestParam @NotBlank String ipAddress,
            @RequestParam @NotBlank String reason,
            HttpServletRequest request) {
        try {
            accountSecurityService.addToBlacklist(ipAddress, reason);
            adminOperationLogService.recordOperation(getAdminId(request), "ADD_IP_BLACKLIST", "IP",
                    ipAddress, "将 IP 加入黑名单: " + reason, request);
            return ResponseEntity.ok(ApiResponse.success("加入黑名单成功", null));
        } catch (Exception e) {
            log.error("将 IP 加入黑名单失败: ip={}", ipAddress, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("操作失败"));
        }
    }

    @DeleteMapping("/blacklist")
    public ResponseEntity<ApiResponse<Void>> removeFromBlacklist(
            @RequestParam @NotBlank String ipAddress,
            HttpServletRequest request) {
        try {
            accountSecurityService.removeFromBlacklist(ipAddress);
            adminOperationLogService.recordOperation(getAdminId(request), "REMOVE_IP_BLACKLIST", "IP",
                    ipAddress, "将 IP 从黑名单移除", request);
            return ResponseEntity.ok(ApiResponse.success("移除黑名单成功", null));
        } catch (Exception e) {
            log.error("将 IP 从黑名单移除失败: ip={}", ipAddress, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("操作失败"));
        }
    }

    @PostMapping("/ban-account")
    public ResponseEntity<ApiResponse<Void>> banAccount(
            @RequestParam @Min(1) Integer userId,
            @RequestParam @NotBlank String reason,
            HttpServletRequest request) {
        try {
            accountSecurityService.banAccount(userId, reason);
            adminOperationLogService.recordOperation(getAdminId(request), "BAN_ACCOUNT", "USER",
                    userId.toString(), "封禁账号: " + reason, request);
            return ResponseEntity.ok(ApiResponse.success("账号封禁成功", null));
        } catch (Exception e) {
            log.error("封禁账号失败: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("封禁失败"));
        }
    }

    @PostMapping("/unban-account")
    public ResponseEntity<ApiResponse<Void>> unbanAccount(
            @RequestParam @Min(1) Integer userId,
            HttpServletRequest request) {
        try {
            accountSecurityService.unbanAccount(userId);
            adminOperationLogService.recordOperation(getAdminId(request), "UNBAN_ACCOUNT", "USER",
                    userId.toString(), "解封账号", request);
            return ResponseEntity.ok(ApiResponse.success("账号解封成功", null));
        } catch (Exception e) {
            log.error("解封账号失败: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("解封失败"));
        }
    }

    @PostMapping("/force-logout")
    public ResponseEntity<ApiResponse<Void>> forceLogout(
            @RequestParam @Min(1) Integer userId,
            @RequestParam @NotBlank String reason,
            HttpServletRequest request) {
        try {
            accountSecurityService.forceLogout(userId, reason);
            adminOperationLogService.recordOperation(getAdminId(request), "FORCE_LOGOUT", "USER",
                    userId.toString(), "强制下线: " + reason, request);
            return ResponseEntity.ok(ApiResponse.success("强制下线成功", null));
        } catch (Exception e) {
            log.error("强制下线失败: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("操作失败"));
        }
    }

    @GetMapping("/online-count")
    public ResponseEntity<ApiResponse<Integer>> getOnlineCount() {
        try {
            int count = accountSecurityService.getOnlineUserCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("获取在线用户数失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("查询失败"));
        }
    }
}