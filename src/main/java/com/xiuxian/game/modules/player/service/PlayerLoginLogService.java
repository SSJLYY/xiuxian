package com.xiuxian.game.modules.player.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.modules.player.entity.PlayerLoginLog;
import com.xiuxian.game.modules.player.mapper.PlayerLoginLogMapper;
import com.xiuxian.game.modules.admin.service.AntiFraudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 玩家登录日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerLoginLogService {
    
    private final PlayerLoginLogMapper loginLogMapper;
    private final AntiFraudService antiFraudService;
    
    /**
     * 记录玩家登录日志
     */
    @Async
    public void recordLogin(Integer playerId, HttpServletRequest request) {
        try {
            PlayerLoginLog loginLog = new PlayerLoginLog();
            loginLog.setPlayerId(playerId);
            loginLog.setIpAddress(getClientIpAddress(request));
            loginLog.setDeviceInfo(getUserAgent(request));
            loginLog.setLoginAt(LocalDateTime.now());
            
            loginLogMapper.insert(loginLog);
            log.info("记录玩家登录日志: playerId={}, ip={}", playerId, loginLog.getIpAddress());
            
            // 检测登录异常
            antiFraudService.detectLoginAbnormal(playerId, loginLog.getIpAddress());
        } catch (Exception e) {
            log.error("记录登录日志失败: playerId={}", playerId, e);
        }
    }
    
    /**
     * 分页查询登录日志
     */
    public Page<PlayerLoginLog> getLoginLogs(Integer playerId, LocalDateTime startTime, 
                                           LocalDateTime endTime, int page, int size) {
        Page<PlayerLoginLog> pageParam = new Page<>(page, size);
        QueryWrapper<PlayerLoginLog> queryWrapper = new QueryWrapper<>();
        
        if (playerId != null) {
            queryWrapper.eq("player_id", playerId);
        }
        if (startTime != null) {
            queryWrapper.ge("login_at", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("login_at", endTime);
        }
        
        queryWrapper.orderByDesc("login_at");
        return loginLogMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 获取玩家最近登录记录
     */
    public List<PlayerLoginLog> getRecentLogins(Integer playerId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        QueryWrapper<PlayerLoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId)
                   .orderByDesc("login_at")
                   .last("LIMIT " + safeLimit);
        return loginLogMapper.selectList(queryWrapper);
    }
    
    /**
     * 统计玩家登录次数
     */
    public Long countPlayerLogins(Integer playerId, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<PlayerLoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId);
        
        if (startTime != null) {
            queryWrapper.ge("login_at", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("login_at", endTime);
        }
        
        return loginLogMapper.selectCount(queryWrapper);
    }
    
    /**
     * 清理过期日志（保留90天）
     */
    @Async
    public void cleanExpiredLogs() {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(90);
            QueryWrapper<PlayerLoginLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.lt("login_at", expireTime);
            
            int deletedCount = loginLogMapper.delete(queryWrapper);
            log.info("清理过期登录日志: {} 条", deletedCount);
        } catch (Exception e) {
            log.error("清理过期登录日志失败", e);
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 获取用户代理信息
     */
    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 200) {
            userAgent = userAgent.substring(0, 200);
        }
        return userAgent;
    }
}

