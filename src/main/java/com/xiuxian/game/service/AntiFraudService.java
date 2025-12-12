package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.entity.PlayerLoginLog;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.mapper.PlayerLoginLogMapper;
import com.xiuxian.game.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反作弊服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiFraudService {
    
    private final PlayerLoginLogMapper loginLogMapper;
    private final UserMapper userMapper;
    private final AdminOperationLogService adminOperationLogService;
    
    // 异常行为计数器
    private final ConcurrentHashMap<Integer, AbnormalBehaviorCounter> behaviorCounters = new ConcurrentHashMap<>();
    
    /**
     * 检测登录异常
     */
    @Async
    public void detectLoginAbnormal(Integer playerId, String ipAddress) {
        try {
            // 检测短时间内多IP登录
            if (detectMultipleIpLogin(playerId)) {
                recordAbnormalBehavior(playerId, "MULTIPLE_IP_LOGIN", "短时间内多IP登录", ipAddress);
            }
            
            // 检测异常登录频率
            if (detectHighFrequencyLogin(playerId)) {
                recordAbnormalBehavior(playerId, "HIGH_FREQUENCY_LOGIN", "异常登录频率", ipAddress);
            }
            
        } catch (Exception e) {
            log.error("检测登录异常失败: playerId={}", playerId, e);
        }
    }
    
    /**
     * 检测资源异常增长
     */
    @Async
    public void detectResourceAbnormal(Integer playerId, String resourceType, long oldValue, long newValue) {
        try {
            long increase = newValue - oldValue;
            
            // 检测资源异常增长
            if (isAbnormalResourceIncrease(resourceType, increase)) {
                recordAbnormalBehavior(playerId, "ABNORMAL_RESOURCE_INCREASE", 
                        String.format("资源异常增长: %s从%d增加到%d", resourceType, oldValue, newValue), null);
            }
            
        } catch (Exception e) {
            log.error("检测资源异常失败: playerId={}, resourceType={}", playerId, resourceType, e);
        }
    }
    
    /**
     * 检测操作频率异常
     */
    @Async
    public void detectOperationFrequencyAbnormal(Integer playerId, String operationType) {
        try {
            AbnormalBehaviorCounter counter = behaviorCounters.computeIfAbsent(playerId, 
                    k -> new AbnormalBehaviorCounter());
            
            if (counter.incrementAndCheck(operationType)) {
                recordAbnormalBehavior(playerId, "HIGH_FREQUENCY_OPERATION", 
                        "操作频率异常: " + operationType, null);
            }
            
        } catch (Exception e) {
            log.error("检测操作频率异常失败: playerId={}, operationType={}", playerId, operationType, e);
        }
    }
    
    /**
     * 检测等级异常提升
     */
    @Async
    public void detectLevelAbnormal(Integer playerId, int oldLevel, int newLevel) {
        try {
            int levelIncrease = newLevel - oldLevel;
            
            // 检测等级异常提升（1小时内提升超过10级）
            if (levelIncrease > 10) {
                recordAbnormalBehavior(playerId, "ABNORMAL_LEVEL_INCREASE", 
                        String.format("等级异常提升: 从%d级提升到%d级", oldLevel, newLevel), null);
            }
            
        } catch (Exception e) {
            log.error("检测等级异常失败: playerId={}", playerId, e);
        }
    }
    
    /**
     * 自动封禁处理
     */
    private void handleAutoBan(Integer playerId, String reason) {
        try {
            // 更新用户状态为封禁
            User user = userMapper.selectById(playerId);
            if (user != null) {
                user.setStatus("BANNED");
                userMapper.updateById(user);
                
                // 记录管理员操作日志
                adminOperationLogService.recordOperation(0, "AUTO_BAN", "USER", 
                        playerId.toString(), "系统自动封禁: " + reason, null);
                
                log.warn("系统自动封禁用户: playerId={}, reason={}", playerId, reason);
            }
            
        } catch (Exception e) {
            log.error("自动封禁处理失败: playerId={}", playerId, e);
        }
    }
    
    /**
     * 记录异常行为
     */
    private void recordAbnormalBehavior(Integer playerId, String behaviorType, String description, String ipAddress) {
        try {
            AbnormalBehaviorCounter counter = behaviorCounters.computeIfAbsent(playerId, 
                    k -> new AbnormalBehaviorCounter());
            
            counter.addAbnormalBehavior(behaviorType);
            
            // 记录风控日志
            log.warn("检测到异常行为: playerId={}, type={}, description={}, ip={}", 
                    playerId, behaviorType, description, ipAddress);
            
            // 如果异常行为次数过多，自动封禁
            if (counter.getTotalAbnormalCount() >= 5) {
                handleAutoBan(playerId, "多次异常行为: " + description);
            }
            
        } catch (Exception e) {
            log.error("记录异常行为失败: playerId={}", playerId, e);
        }
    }
    
    /**
     * 检测短时间内多IP登录
     */
    private boolean detectMultipleIpLogin(Integer playerId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        QueryWrapper<PlayerLoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId)
                   .ge("login_at", oneHourAgo)
                   .groupBy("ip_address");
        
        List<PlayerLoginLog> logs = loginLogMapper.selectList(queryWrapper);
        return logs.size() > 3; // 1小时内超过3个不同IP登录
    }
    
    /**
     * 检测异常登录频率
     */
    private boolean detectHighFrequencyLogin(Integer playerId) {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        
        QueryWrapper<PlayerLoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", playerId)
                   .ge("login_at", tenMinutesAgo);
        
        Long count = loginLogMapper.selectCount(queryWrapper);
        return count > 20; // 10分钟内登录超过20次
    }
    
    /**
     * 检测资源异常增长
     */
    private boolean isAbnormalResourceIncrease(String resourceType, long increase) {
        Map<String, Long> thresholds = new HashMap<>();
        thresholds.put("SPIRIT_STONES", 100000L); // 灵石一次增长超过10万
        thresholds.put("EXP", 50000L); // 经验一次增长超过5万
        thresholds.put("YUANBAO", 10000L); // 元宝一次增长超过1万
        
        Long threshold = thresholds.get(resourceType);
        return threshold != null && increase > threshold;
    }
    
    /**
     * 清理过期的行为计数器
     */
    @Async
    public void cleanupExpiredCounters() {
        try {
            behaviorCounters.entrySet().removeIf(entry -> {
                AbnormalBehaviorCounter counter = entry.getValue();
                return counter.isExpired();
            });
            log.debug("清理过期的异常行为计数器");
        } catch (Exception e) {
            log.error("清理异常行为计数器失败", e);
        }
    }
    
    /**
     * 异常行为计数器
     */
    private static class AbnormalBehaviorCounter {
        private final Map<String, Integer> operationCounts = new ConcurrentHashMap<>();
        private final Map<String, Integer> abnormalCounts = new ConcurrentHashMap<>();
        private volatile long lastUpdateTime = System.currentTimeMillis();
        
        public boolean incrementAndCheck(String operationType) {
            lastUpdateTime = System.currentTimeMillis();
            int count = operationCounts.merge(operationType, 1, Integer::sum);
            
            // 不同操作类型的频率阈值
            Map<String, Integer> thresholds = new HashMap<>();
            thresholds.put("CLAIM_MAIL", 50); // 1小时内领取邮件超过50次
            thresholds.put("SHOP_BUY", 100); // 1小时内购买超过100次
            thresholds.put("COMBAT", 200); // 1小时内战斗超过200次
            
            Integer threshold = thresholds.getOrDefault(operationType, 100);
            return count > threshold;
        }
        
        public void addAbnormalBehavior(String behaviorType) {
            lastUpdateTime = System.currentTimeMillis();
            abnormalCounts.merge(behaviorType, 1, Integer::sum);
        }
        
        public int getTotalAbnormalCount() {
            return abnormalCounts.values().stream().mapToInt(Integer::intValue).sum();
        }
        
        public boolean isExpired() {
            // 2小时未更新则认为过期
            return System.currentTimeMillis() - lastUpdateTime > 2 * 60 * 60 * 1000;
        }
    }
}