package com.xiuxian.game.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.player.entity.PlayerLoginLog;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.player.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 反作弊服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiFraudService {
    
    // ========== 风控阈值常量 ==========
    
    /** 1小时内不同IP登录数量阈值 */
    private static final int MULTIPLE_IP_THRESHOLD = 3;
    /** 10分钟内登录次数阈值 */
    private static final int HIGH_FREQUENCY_LOGIN_THRESHOLD = 20;
    /** 等级异常提升阈值（1小时内） */
    private static final int LEVEL_INCREASE_THRESHOLD = 10;
    /** 异常行为触发自动封禁的累计次数 */
    private static final int AUTO_BAN_THRESHOLD = 5;
    /** 行为计数器过期时间（毫秒）：2小时 */
    private static final long COUNTER_EXPIRE_MS = 2L * 60 * 60 * 1000;
    
    /** 资源异常增长阈值（单位：单次增长量） */
    private static final Map<String, Long> RESOURCE_THRESHOLDS = Collections.unmodifiableMap(new HashMap<String, Long>() {{
        put("SPIRIT_STONES", 100000L); // 灵石一次增长超过10万
        put("EXP", 50000L);            // 经验一次增长超过5万
        put("YUANBAO", 10000L);        // 元宝一次增长超过1万
    }});
    
    /** 操作频率阈值（单位：指定时间窗口内次数） */
    private static final Map<String, Integer> OPERATION_FREQUENCY_THRESHOLDS = Collections.unmodifiableMap(new HashMap<String, Integer>() {{
        put("CLAIM_MAIL", 50);  // 1小时内领取邮件超过50次
        put("SHOP_BUY", 100);   // 1小时内购买超过100次
        put("COMBAT", 200);     // 1小时内战斗超过200次
    }});
    
    /** 操作频率默认阈值 */
    private static final int DEFAULT_OPERATION_FREQUENCY_THRESHOLD = 100;
    
    // ========== 依赖注入 ==========
    
    private final PlayerService playerService; // module boundary: access player data via PlayerService
    private final AdminOperationLogService adminOperationLogService;
    
    // 异常行为计数器
    private final ConcurrentHashMap<Integer, AbnormalBehaviorCounter> behaviorCounters = new ConcurrentHashMap<>();
    
    // 已封禁玩家缓存，防止重复封禁
    private final ConcurrentHashMap<Integer, AtomicBoolean> bannedPlayers = new ConcurrentHashMap<>();
    
    /**
     * 检测登录异常
     */
    @Async
    public void detectLoginAbnormal(Integer playerId, String ipAddress) {
        try {
            if (playerId == null || playerId <= 0) {
                return;
            }

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
            if (playerId == null || playerId <= 0 || resourceType == null || resourceType.trim().isEmpty()) {
                return;
            }

            // 资源减少或不变，不视为异常增长
            if (newValue <= oldValue) {
                return;
            }

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
            if (playerId == null || playerId <= 0 || operationType == null || operationType.trim().isEmpty()) {
                return;
            }

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
            if (playerId == null || playerId <= 0 || oldLevel < 0 || newLevel < 0) {
                return;
            }

            // 等级未变化或下降，不视为异常提升
            if (newLevel <= oldLevel) {
                return;
            }

            int levelIncrease = newLevel - oldLevel;
            
            // 检测等级异常提升（1小时内提升超过阈值）
            if (levelIncrease > LEVEL_INCREASE_THRESHOLD) {
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
            if (playerId == null || playerId <= 0 || reason == null || reason.trim().isEmpty()) {
                log.warn("自动封禁参数无效，跳过: playerId={}, reason={}", playerId, reason);
                return;
            }

            // 幂等性检查：防止重复封禁
            AtomicBoolean alreadyBanned = bannedPlayers.computeIfAbsent(playerId, k -> new AtomicBoolean(false));
            if (!alreadyBanned.compareAndSet(false, true)) {
                log.debug("玩家已被封禁，跳过重复操作: playerId={}", playerId);
                return;
            }

            // 更新用户状态为封禁
            playerService.banUser(playerId, "BANNED");
            User user = playerService.getUserById(playerId);
            if (user != null) {
                
                // 记录管理员操作日志
                adminOperationLogService.recordOperation(0, "AUTO_BAN", "USER", 
                        playerId.toString(), "系统自动封禁: " + reason, null);
                
                log.warn("系统自动封禁用户: playerId={}, reason={}", playerId, reason);
            }
            
        } catch (Exception e) {
            AtomicBoolean marker = bannedPlayers.get(playerId);
            if (marker != null) {
                marker.set(false);
            }
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
            if (counter.getTotalAbnormalCount() >= AUTO_BAN_THRESHOLD) {
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
        
        List<PlayerLoginLog> logs = playerService.getRecentLoginLogs(playerId, oneHourAgo);
        long distinctIps = logs.stream().map(PlayerLoginLog::getIpAddress).distinct().count();
        return distinctIps > MULTIPLE_IP_THRESHOLD;
    }
    
    /**
     * 检测异常登录频率
     */
    private boolean detectHighFrequencyLogin(Integer playerId) {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        
        long count = playerService.countRecentLogins(playerId, tenMinutesAgo);
        return count > HIGH_FREQUENCY_LOGIN_THRESHOLD;
    }
    
    /**
     * 检测资源异常增长（传入已校验的 resourceType）
     */
    private boolean isAbnormalResourceIncrease(String resourceType, long increase) {
        Long threshold = RESOURCE_THRESHOLDS.get(resourceType);
        return threshold != null && increase > threshold;
    }
    
    /**
     * 清理过期的计数器（定时任务）
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredCounters() {
        try {
            behaviorCounters.entrySet().removeIf(entry -> {
                AbnormalBehaviorCounter counter = entry.getValue();
                return counter.isExpired();
            });
            log.debug("清理过期的行为计数器");
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
            
            int threshold = OPERATION_FREQUENCY_THRESHOLDS.getOrDefault(operationType, DEFAULT_OPERATION_FREQUENCY_THRESHOLD);
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
            return System.currentTimeMillis() - lastUpdateTime > COUNTER_EXPIRE_MS;
        }
    }
}
