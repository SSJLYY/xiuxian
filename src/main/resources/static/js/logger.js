/**
 * 前端日志工具
 * 
 * 提供统一的前端日志记录功能，支持不同级别的日志输出和格式化。
 * 
 * 功能特性：
 * - 支持多种日志级别（DEBUG, INFO, WARN, ERROR）
 * - 自动添加时间戳和模块标识
 * - 支持结构化日志记录
 * - 可配置的日志级别过滤
 * - 支持日志持久化到localStorage
 * - 性能监控和统计
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-11-28
 */

class Logger {
    constructor() {
        // 日志级别定义
        this.levels = {
            DEBUG: 0,
            INFO: 1,
            WARN: 2,
            ERROR: 3
        };
        
        // 当前日志级别（生产环境建议设置为INFO或WARN）
        this.currentLevel = this.levels.DEBUG;
        
        // 日志缓存（用于持久化）
        this.logCache = [];
        this.maxCacheSize = 1000;
        
        // 性能统计
        this.performanceStats = new Map();
        
        // 初始化
        this.init();
    }
    
    /**
     * 初始化日志系统
     */
    init() {
        // 从localStorage恢复日志级别设置
        const savedLevel = localStorage.getItem('logLevel');
        if (savedLevel && this.levels[savedLevel] !== undefined) {
            this.currentLevel = this.levels[savedLevel];
        }
        
        // 监听页面卸载事件，保存日志
        window.addEventListener('beforeunload', () => {
            this.saveLogs();
        });
        
        // 定期清理日志缓存
        setInterval(() => {
            this.cleanupLogs();
        }, 60000); // 每分钟清理一次
        
        this.info('Logger', '前端日志系统初始化完成', {
            level: Object.keys(this.levels)[this.currentLevel],
            cacheSize: this.maxCacheSize
        });
    }
    
    /**
     * 设置日志级别
     * @param {string} level 日志级别（DEBUG, INFO, WARN, ERROR）
     */
    setLevel(level) {
        if (this.levels[level] !== undefined) {
            this.currentLevel = this.levels[level];
            localStorage.setItem('logLevel', level);
            this.info('Logger', `日志级别已设置为: ${level}`);
        } else {
            this.warn('Logger', `无效的日志级别: ${level}`);
        }
    }
    
    /**
     * 检查是否应该输出指定级别的日志
     * @param {number} level 日志级别
     * @returns {boolean} 是否应该输出
     */
    shouldLog(level) {
        return level >= this.currentLevel;
    }
    
    /**
     * 格式化日志消息
     * @param {string} level 日志级别
     * @param {string} module 模块名称
     * @param {string} message 日志消息
     * @param {Object} data 附加数据
     * @returns {Object} 格式化后的日志对象
     */
    formatLog(level, module, message, data = null) {
        const timestamp = new Date().toISOString();
        const logEntry = {
            timestamp,
            level,
            module,
            message,
            data,
            url: window.location.href,
            userAgent: navigator.userAgent.substring(0, 100) // 截取前100个字符
        };
        
        // 添加用户信息（如果可用）
        if (window.authManager && window.authManager.currentUser) {
            logEntry.userId = window.authManager.currentUser.username;
        }
        if (window.authManager && window.authManager.player) {
            logEntry.playerId = window.authManager.player.id;
        }
        
        return logEntry;
    }
    
    /**
     * 输出日志到控制台
     * @param {Object} logEntry 日志条目
     */
    outputToConsole(logEntry) {
        const { timestamp, level, module, message, data } = logEntry;
        const timeStr = new Date(timestamp).toLocaleTimeString();
        const prefix = `[${timeStr}] [${level}] [${module}]`;
        
        switch (level) {
            case 'DEBUG':
                console.debug(prefix, message, data || '');
                break;
            case 'INFO':
                console.info(prefix, message, data || '');
                break;
            case 'WARN':
                console.warn(prefix, message, data || '');
                break;
            case 'ERROR':
                console.error(prefix, message, data || '');
                break;
            default:
                console.log(prefix, message, data || '');
        }
    }
    
    /**
     * 记录日志
     * @param {string} level 日志级别
     * @param {string} module 模块名称
     * @param {string} message 日志消息
     * @param {Object} data 附加数据
     */
    log(level, module, message, data = null) {
        const levelNum = this.levels[level];
        if (!this.shouldLog(levelNum)) {
            return;
        }
        
        const logEntry = this.formatLog(level, module, message, data);
        
        // 输出到控制台
        this.outputToConsole(logEntry);
        
        // 添加到缓存
        this.addToCache(logEntry);
        
        // 如果是错误日志，尝试发送到服务器
        if (level === 'ERROR') {
            this.sendErrorToServer(logEntry);
        }
    }
    
    /**
     * DEBUG级别日志
     * @param {string} module 模块名称
     * @param {string} message 日志消息
     * @param {Object} data 附加数据
     */
    debug(module, message, data = null) {
        this.log('DEBUG', module, message, data);
    }
    
    /**
     * INFO级别日志
     * @param {string} module 模块名称
     * @param {string} message 日志消息
     * @param {Object} data 附加数据
     */
    info(module, message, data = null) {
        this.log('INFO', module, message, data);
    }
    
    /**
     * WARN级别日志
     * @param {string} module 模块名称
     * @param {string} message 日志消息
     * @param {Object} data 附加数据
     */
    warn(module, message, data = null) {
        this.log('WARN', module, message, data);
    }
    
    /**
     * ERROR级别日志
     * @param {string} module 模块名称
     * @param {string} message 日志消息
     * @param {Object} data 附加数据
     */
    error(module, message, data = null) {
        this.log('ERROR', module, message, data);
    }
    
    /**
     * 记录API调用日志
     * @param {string} method HTTP方法
     * @param {string} url 请求URL
     * @param {number} status 响应状态码
     * @param {number} duration 请求耗时（毫秒）
     * @param {Object} requestData 请求数据
     * @param {Object} responseData 响应数据
     */
    logApiCall(method, url, status, duration, requestData = null, responseData = null) {
        const level = status >= 400 ? 'ERROR' : (duration > 2000 ? 'WARN' : 'INFO');
        const message = `API调用: ${method} ${url}`;
        const data = {
            method,
            url,
            status,
            duration,
            requestData: this.sanitizeData(requestData),
            responseData: this.sanitizeData(responseData)
        };
        
        this.log(level, 'API', message, data);
        
        // 更新性能统计
        this.updatePerformanceStats(`${method} ${url}`, duration);
    }
    
    /**
     * 记录用户操作日志
     * @param {string} action 操作名称
     * @param {string} description 操作描述
     * @param {Object} data 附加数据
     */
    logUserAction(action, description, data = null) {
        this.info('USER_ACTION', `${action}: ${description}`, data);
    }
    
    /**
     * 记录性能指标
     * @param {string} operation 操作名称
     * @param {number} duration 耗时（毫秒）
     * @param {Object} data 附加数据
     */
    logPerformance(operation, duration, data = null) {
        const level = duration > 1000 ? 'WARN' : 'INFO';
        const message = `性能监控: ${operation}`;
        const perfData = {
            operation,
            duration,
            ...data
        };
        
        this.log(level, 'PERFORMANCE', message, perfData);
        this.updatePerformanceStats(operation, duration);
    }
    
    /**
     * 更新性能统计
     * @param {string} operation 操作名称
     * @param {number} duration 耗时
     */
    updatePerformanceStats(operation, duration) {
        if (!this.performanceStats.has(operation)) {
            this.performanceStats.set(operation, {
                count: 0,
                totalTime: 0,
                minTime: Infinity,
                maxTime: 0
            });
        }
        
        const stats = this.performanceStats.get(operation);
        stats.count++;
        stats.totalTime += duration;
        stats.minTime = Math.min(stats.minTime, duration);
        stats.maxTime = Math.max(stats.maxTime, duration);
    }
    
    /**
     * 获取性能统计信息
     * @param {string} operation 操作名称（可选）
     * @returns {Object} 性能统计信息
     */
    getPerformanceStats(operation = null) {
        if (operation) {
            const stats = this.performanceStats.get(operation);
            if (stats) {
                return {
                    ...stats,
                    avgTime: stats.totalTime / stats.count
                };
            }
            return null;
        }
        
        const result = {};
        this.performanceStats.forEach((stats, op) => {
            result[op] = {
                ...stats,
                avgTime: stats.totalTime / stats.count
            };
        });
        return result;
    }
    
    /**
     * 清理敏感数据
     * @param {Object} data 原始数据
     * @returns {Object} 清理后的数据
     */
    sanitizeData(data) {
        if (!data || typeof data !== 'object') {
            return data;
        }
        
        const sensitiveKeys = ['password', 'token', 'secret', 'key', 'auth'];
        const sanitized = JSON.parse(JSON.stringify(data));
        
        const sanitizeObject = (obj) => {
            for (const key in obj) {
                if (obj.hasOwnProperty(key)) {
                    const lowerKey = key.toLowerCase();
                    if (sensitiveKeys.some(sensitive => lowerKey.includes(sensitive))) {
                        obj[key] = '[HIDDEN]';
                    } else if (typeof obj[key] === 'object' && obj[key] !== null) {
                        sanitizeObject(obj[key]);
                    }
                }
            }
        };
        
        sanitizeObject(sanitized);
        return sanitized;
    }
    
    /**
     * 添加日志到缓存
     * @param {Object} logEntry 日志条目
     */
    addToCache(logEntry) {
        this.logCache.push(logEntry);
        
        // 限制缓存大小
        if (this.logCache.length > this.maxCacheSize) {
            this.logCache.shift(); // 移除最旧的日志
        }
    }
    
    /**
     * 清理过期日志
     */
    cleanupLogs() {
        const now = Date.now();
        const maxAge = 24 * 60 * 60 * 1000; // 24小时
        
        this.logCache = this.logCache.filter(log => {
            const logTime = new Date(log.timestamp).getTime();
            return (now - logTime) < maxAge;
        });
    }
    
    /**
     * 保存日志到localStorage
     */
    saveLogs() {
        try {
            const logsToSave = this.logCache.slice(-100); // 只保存最近100条
            localStorage.setItem('gameLogs', JSON.stringify(logsToSave));
        } catch (error) {
            console.warn('保存日志到localStorage失败:', error);
        }
    }
    
    /**
     * 从localStorage加载日志
     */
    loadLogs() {
        try {
            const savedLogs = localStorage.getItem('gameLogs');
            if (savedLogs) {
                return JSON.parse(savedLogs);
            }
        } catch (error) {
            console.warn('从localStorage加载日志失败:', error);
        }
        return [];
    }
    
    /**
     * 获取日志缓存
     * @param {string} level 日志级别过滤（可选）
     * @param {number} limit 返回数量限制（可选）
     * @returns {Array} 日志数组
     */
    getLogs(level = null, limit = null) {
        let logs = [...this.logCache];
        
        if (level) {
            logs = logs.filter(log => log.level === level);
        }
        
        if (limit) {
            logs = logs.slice(-limit);
        }
        
        return logs;
    }
    
    /**
     * 发送错误日志到服务器
     * @param {Object} logEntry 错误日志条目
     */
    async sendErrorToServer(logEntry) {
        try {
            // 这里可以实现发送错误日志到服务器的逻辑
            // 例如调用专门的错误报告API
            console.debug('错误日志已记录，可考虑发送到服务器:', logEntry);
        } catch (error) {
            console.warn('发送错误日志到服务器失败:', error);
        }
    }
    
    /**
     * 导出日志为文本格式
     * @returns {string} 格式化的日志文本
     */
    exportLogs() {
        const logs = this.getLogs();
        const lines = logs.map(log => {
            const time = new Date(log.timestamp).toLocaleString();
            const dataStr = log.data ? JSON.stringify(log.data) : '';
            return `[${time}] [${log.level}] [${log.module}] ${log.message} ${dataStr}`;
        });
        
        return lines.join('\n');
    }
    
    /**
     * 清空日志缓存
     */
    clearLogs() {
        this.logCache = [];
        localStorage.removeItem('gameLogs');
        this.info('Logger', '日志缓存已清空');
    }
}

// 创建全局日志实例
const logger = new Logger();

// 导出到全局作用域
window.logger = logger;

// 兼容性方法（保持向后兼容）
window.log = {
    debug: (module, message, data) => logger.debug(module, message, data),
    info: (module, message, data) => logger.info(module, message, data),
    warn: (module, message, data) => logger.warn(module, message, data),
    error: (module, message, data) => logger.error(module, message, data)
};

// 捕获全局错误
window.addEventListener('error', (event) => {
    logger.error('GLOBAL_ERROR', '全局JavaScript错误', {
        message: event.message,
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
        stack: event.error ? event.error.stack : null
    });
});

// 捕获Promise拒绝
window.addEventListener('unhandledrejection', (event) => {
    logger.error('UNHANDLED_PROMISE', 'Promise拒绝未处理', {
        reason: event.reason,
        stack: event.reason ? event.reason.stack : null
    });
});

logger.info('Logger', '前端日志系统加载完成');