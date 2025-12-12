package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.entity.GameConfig;
import com.xiuxian.game.mapper.GameConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameConfigService {
    
    private final GameConfigMapper gameConfigMapper;
    
    // 配置缓存
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    /**
     * 初始化配置缓存
     */
    @PostConstruct
    public void initConfigCache() {
        try {
            refreshCache();
            log.info("游戏配置缓存初始化完成");
        } catch (Exception e) {
            log.error("初始化游戏配置缓存失败", e);
        }
    }
    
    /**
     * 刷新配置缓存
     */
    public void refreshCache() {
        List<GameConfig> configs = gameConfigMapper.selectList(null);
        configCache.clear();
        
        for (GameConfig config : configs) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
        
        log.info("配置缓存已刷新，共加载 {} 个配置项", configs.size());
    }
    
    /**
     * 获取字符串配置
     */
    public String getString(String key, String defaultValue) {
        return configCache.getOrDefault(key, defaultValue);
    }
    
    /**
     * 获取整数配置
     */
    public int getInt(String key, int defaultValue) {
        try {
            String value = configCache.get(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 {} 不是有效的整数，使用默认值 {}", key, configCache.get(key), defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取长整数配置
     */
    public long getLong(String key, long defaultValue) {
        try {
            String value = configCache.get(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 {} 不是有效的长整数，使用默认值 {}", key, configCache.get(key), defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取浮点数配置
     */
    public double getDouble(String key, double defaultValue) {
        try {
            String value = configCache.get(key);
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 {} 不是有效的浮点数，使用默认值 {}", key, configCache.get(key), defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取BigDecimal配置
     */
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        try {
            String value = configCache.get(key);
            return value != null ? new BigDecimal(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 {} 不是有效的数字，使用默认值 {}", key, configCache.get(key), defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = configCache.get(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }
    
    /**
     * 设置配置值
     */
    public void setConfig(String key, String value, String description, String category) {
        GameConfig config = getConfigByKey(key);
        
        if (config == null) {
            // 创建新配置
            config = new GameConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType(determineConfigType(value));
            config.setDescription(description);
            config.setCategory(category);
            gameConfigMapper.insert(config);
        } else {
            // 更新现有配置
            config.setConfigValue(value);
            config.setConfigType(determineConfigType(value));
            if (description != null) {
                config.setDescription(description);
            }
            if (category != null) {
                config.setCategory(category);
            }
            gameConfigMapper.updateById(config);
        }
        
        // 更新缓存
        configCache.put(key, value);
        
        log.info("配置项已更新: key={}, value={}", key, value);
    }
    
    /**
     * 删除配置
     */
    public void deleteConfig(String key) {
        QueryWrapper<GameConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", key);
        gameConfigMapper.delete(queryWrapper);
        
        configCache.remove(key);
        
        log.info("配置项已删除: key={}", key);
    }
    
    /**
     * 获取所有配置
     */
    public List<GameConfig> getAllConfigs() {
        return gameConfigMapper.selectList(null);
    }
    
    /**
     * 按分类获取配置
     */
    public List<GameConfig> getConfigsByCategory(String category) {
        QueryWrapper<GameConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category", category);
        return gameConfigMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据key获取配置对象
     */
    private GameConfig getConfigByKey(String key) {
        QueryWrapper<GameConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", key);
        return gameConfigMapper.selectOne(queryWrapper);
    }
    
    /**
     * 判断配置值的类型
     */
    private String determineConfigType(String value) {
        if (value == null) {
            return "STRING";
        }
        
        // 尝试解析为整数
        try {
            Integer.parseInt(value);
            return "INTEGER";
        } catch (NumberFormatException ignored) {}
        
        // 尝试解析为浮点数
        try {
            Double.parseDouble(value);
            return "DOUBLE";
        } catch (NumberFormatException ignored) {}
        
        // 检查布尔值
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) ||
            "1".equals(value) || "0".equals(value) ||
            "yes".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
            return "BOOLEAN";
        }
        
        return "STRING";
    }
    
    /**
     * 游戏配置常量
     */
    public static class ConfigKeys {
        // 经验相关
        public static final String EXP_MULTIPLIER = "exp.multiplier";
        public static final String CULTIVATION_EXP_BASE = "cultivation.exp.base";
        
        // 掉落相关
        public static final String DROP_RATE_MULTIPLIER = "drop.rate.multiplier";
        public static final String RARE_DROP_RATE = "drop.rare.rate";
        
        // 商店相关
        public static final String SHOP_DISCOUNT_RATE = "shop.discount.rate";
        public static final String SHOP_REFRESH_COST = "shop.refresh.cost";
        
        // 境界相关
        public static final String REALM_BREAKTHROUGH_COST_MULTIPLIER = "realm.breakthrough.cost.multiplier";
        public static final String REALM_LEVEL_REQUIREMENT_MULTIPLIER = "realm.level.requirement.multiplier";
        
        // 宠物相关
        public static final String PET_CAPTURE_BASE_RATE = "pet.capture.base.rate";
        public static final String PET_TRAINING_COST_MULTIPLIER = "pet.training.cost.multiplier";
        
        // 活动相关
        public static final String DOUBLE_EXP_ENABLED = "activity.double.exp.enabled";
        public static final String DOUBLE_DROP_ENABLED = "activity.double.drop.enabled";
        
        // 新手相关
        public static final String NEWBIE_GIFT_ENABLED = "newbie.gift.enabled";
        public static final String NEWBIE_PROTECTION_LEVEL = "newbie.protection.level";
        
        // 系统相关
        public static final String MAINTENANCE_MODE = "system.maintenance.mode";
        public static final String MAX_ONLINE_USERS = "system.max.online.users";
    }
}