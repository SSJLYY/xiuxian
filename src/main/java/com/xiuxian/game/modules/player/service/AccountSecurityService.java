package com.xiuxian.game.modules.player.service;

import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.modules.player.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountSecurityService {

    private final UserMapper userMapper;

    private final ConcurrentHashMap<Integer, UserSession> activeSessions = new ConcurrentHashMap<>();
    private final Set<String> blacklistedIps = ConcurrentHashMap.newKeySet();

    public boolean isIpBlacklisted(String ipAddress) {
        return blacklistedIps.contains(normalizeIp(ipAddress));
    }

    public void addToBlacklist(String ipAddress, String reason) {
        String normalizedIp = normalizeIp(ipAddress);
        blacklistedIps.add(normalizedIp);
        log.warn("IP地址已加入黑名单: ip={}, reason={}", normalizedIp, reason);
    }

    public void removeFromBlacklist(String ipAddress) {
        String normalizedIp = normalizeIp(ipAddress);
        boolean removed = blacklistedIps.remove(normalizedIp);
        if (!removed) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "IP不在黑名单中");
        }
        log.info("IP地址已从黑名单移除: ip={}", normalizedIp);
    }

    public Set<String> getBlacklistedIps() {
        return new HashSet<>(blacklistedIps);
    }

    public boolean checkSingleSignOn(Integer userId, String sessionId, String ipAddress) {
        UserSession existingSession = activeSessions.get(userId);
        if (existingSession != null && !existingSession.getSessionId().equals(sessionId)) {
            log.warn("检测到重复登录，强制下线旧会话: userId={}", userId);
            return false;
        }
        activeSessions.put(userId, new UserSession(sessionId, ipAddress, LocalDateTime.now()));
        return true;
    }

    public void removeUserSession(Integer userId) {
        activeSessions.remove(userId);
    }

    public void forceLogout(Integer userId, String reason) {
        UserSession session = activeSessions.remove(userId);
        if (session != null) {
            log.warn("强制用户下线: userId={}, reason={}", userId, reason);
        }
    }

    public boolean isAccountValid(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        return !"BANNED".equals(user.getStatus()) && !"LOCKED".equals(user.getStatus());
    }

    public void banAccount(Integer userId, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setStatus("BANNED");
        userMapper.updateById(user);
        forceLogout(userId, "账号被封禁: " + reason);
        log.warn("账号已封禁: userId={}, reason={}", userId, reason);
    }

    public void unbanAccount(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!"BANNED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只能解封已封禁的账号");
        }
        user.setStatus("ACTIVE");
        userMapper.updateById(user);
        log.info("账号已解封: userId={}", userId);
    }

    public int getOnlineUserCount() {
        return activeSessions.size();
    }

    public void cleanupExpiredSessions() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(24);
        activeSessions.entrySet().removeIf(entry ->
                entry.getValue().getLastActiveTime().isBefore(expireTime));
    }

    public String generateDeviceFingerprint(String userAgent, String acceptLanguage, String acceptEncoding) {
        String combined = (userAgent != null ? userAgent : "") +
                (acceptLanguage != null ? acceptLanguage : "") +
                (acceptEncoding != null ? acceptEncoding : "");
        return String.valueOf(combined.hashCode());
    }

    public boolean detectAbnormalDevice(Integer userId, String deviceFingerprint) {
        UserSession session = activeSessions.get(userId);
        if (session != null && session.getDeviceFingerprint() != null) {
            return !session.getDeviceFingerprint().equals(deviceFingerprint);
        }
        return false;
    }

    private String normalizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "IP地址不能为空");
        }

        String candidate = ipAddress.trim();
        if (!looksLikeIpLiteral(candidate)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "IP地址格式无效");
        }

        try {
            return InetAddress.getByName(candidate).getHostAddress();
        } catch (UnknownHostException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "IP地址格式无效");
        }
    }

    private boolean looksLikeIpLiteral(String candidate) {
        if (candidate.contains(".")) {
            String[] parts = candidate.split("\\.", -1);
            if (parts.length != 4) {
                return false;
            }
            for (String part : parts) {
                if (part.isEmpty() || part.length() > 3 || !part.chars().allMatch(Character::isDigit)) {
                    return false;
                }
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            }
            return true;
        }

        if (candidate.contains(":")) {
            return candidate.chars().allMatch(ch -> Character.digit(ch, 16) != -1 || ch == ':');
        }

        return false;
    }

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