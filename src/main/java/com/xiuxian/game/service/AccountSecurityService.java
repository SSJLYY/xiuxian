package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 账号安全服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountSecurityService {
    
    private final UserMapper userMapper;
    
    // 在线用户会话管理
    private final ConcurrentHashMap<Integer, UserSession> activeSessions = new ConcurrentHashMap<>();
    
    // IP黑名单
    private final Set<String> blacklistedIps = ConcurrentHashMap.newKeySet();
    
    /**
     * 检查IP是否在黑名单中
     */
    public boolean isIpBlacklisted(String ipAddress) {
        return blacklistedIps.contains(ipAddress);
    }
    
    /**
     * 添加IP到黑名单
     */
    public void addToBlacklist(String ipAddress, String reason) {
        blacklistedIps.add(ipAddress);
        log.warn("IP地址已加入黑名单: ip={}, reason={}", ipAddress, reason);
    }
    
    /**
     * 从黑名单移除IP
     */
    public void removeFromBlacklist(String ipAddress) {
        blacklistedIps.remove(ipAddress);
        log.info("IP地址已从黑名单移除: ip={}", ipAddress);
    }
    
    /**
     * 获取黑名单IP列表
     */
    public Set<String> getBlacklistedIps() {
        return new HashSet<>(blacklistedIps);
    }
    
    /**
     * 检查单点登录
     */
    public boolean checkSingleSignOn(Integer userId, String sessionId, String ipAddress) {
        UserSession existingSession = activeSessions.get(userId);
        
        if (existingSession != null && !existingSession.getSessionId().equals(sessionId)) {
            // 存在其他会话，强制下线旧会话
            log.warn("检测到重复登录，强制下线旧会话: userId={}, oldSession={}, newSession={}", 
                    userId, existingSession.getSessionId(), sessionId);
            
            // 可以在这里添加通知旧会话下线的逻辑
            return false; // 表示存在重复登录
        }
        
        // 更新或创建会话
        UserSession newSession = new UserSession(sessionId, ipAddress, LocalDateTime.now());
        activeSessions.put(userId, newSession);
        
        return true;
    }
    
    /**
     * 用户登出时清理会话
     */
    public void removeUserSession(Integer userId) {
        UserSession removedSession = activeSessions.remove(userId);
        if (removedSession != null) {
            log.debug("用户会话已清理: userId={}, sessionId={}", userId, removedSession.getSessionId());
        }
    }
    
    /**
     * 强制用户下线
     */
    public void forceLogout(Integer userId, String reason) {
        UserSession session = activeSessions.remove(userId);
        if (session != null) {
            log.warn("强制用户下线: userId={}, reason={}, sessionId={}", 
                    userId, reason, session.getSessionId());
            
            // 这里可以添加通知客户端下线的逻辑
            // 比如通过WebSocket或者在下次请求时返回特定错误码
        }
    }
    
    /**
     * 检查账号状态
     */
    public boolean isAccountValid(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        // 检查账号是否被封禁
        if ("BANNED".equals(user.getStatus())) {
            log.warn("账号已被封禁: userId={}", userId);
            return false;
        }
        
        // 检查账号是否被锁定
        if ("LOCKED".equals(user.getStatus())) {
            log.warn("账号已被锁定: userId={}", userId);
            return false;
        }
        
        return true;
    }
    
    /**
     * 封禁账号
     */
    public void banAccount(Integer userId, String reason) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus("BANNED");
            userMapper.updateById(user);
            
            // 强制下线
            forceLogout(userId, "账号被封禁: " + reason);
            
            log.warn("账号已封禁: userId={}, reason={}", userId, reason);
        }
    }
    
    /**
     * 解封账号
     */
    public void unbanAccount(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus("ACTIVE");
            userMapper.updateById(user);
            
            log.info("账号已解封: userId={}", userId);
        }
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return activeSessions.size();
    }
    
    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(24); // 24小时过期
        
        activeSessions.entrySet().removeIf(entry -> {
            UserSession session = entry.getValue();
            boolean expired = session.getLastActiveTime().isBefore(expireTime);
            if (expired) {
                log.debug("清理过期会话: userId={}, sessionId={}", 
                        entry.getKey(), session.getSessionId());
            }
            return expired;
        });
    }
    
    /**
     * 检测设备指纹（简化版本）
     */
    public String generateDeviceFingerprint(String userAgent, String acceptLanguage, String acceptEncoding) {
        // 简单的设备指纹生成，实际项目中可以使用更复杂的算法
        String combined = (userAgent != null ? userAgent : "") + 
                         (acceptLanguage != null ? acceptLanguage : "") + 
                         (acceptEncoding != null ? acceptEncoding : "");
        
        return String.valueOf(combined.hashCode());
    }
    
    /**
     * 检测异常设备登录
     */
    public boolean detectAbnormalDevice(Integer userId, String deviceFingerprint) {
        UserSession session = activeSessions.get(userId);
        if (session != null && session.getDeviceFingerprint() != null) {
            return !session.getDeviceFingerprint().equals(deviceFingerprint);
        }
        return false;
    }
    
    /**
     * 用户会话信息
     */
    private static class UserSession {
        private final String sessionId;
        private final String ipAddress;
        private final LocalDateTime lastActiveTime;
        private String deviceFingerprint;
        
        public UserSession(String sessionId, String ipAddress, LocalDateTime lastActiveTime) {
            this.sessionId = sessionId;
            this.ipAddress = ipAddress;
            this.lastActiveTime = lastActiveTime;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public String getIpAddress() {
            return ipAddress;
        }
        
        public LocalDateTime getLastActiveTime() {
            return lastActiveTime;
        }
        
        public String getDeviceFingerprint() {
            return deviceFingerprint;
        }
        
        public void setDeviceFingerprint(String deviceFingerprint) {
            this.deviceFingerprint = deviceFingerprint;
        }
    }
}