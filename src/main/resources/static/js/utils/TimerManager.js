/**
 * 定时器管理器
 * 
 * 统一管理所有 setInterval 和 setTimeout，防止内存泄漏
 * 
 * 主要功能：
 * - 统一创建和管理定时器
 * - 支持按 ID 清除单个定时器
 * - 支持批量清除所有定时器
 * - 自动记录定时器信息便于调试
 * 
 * @example
 * const timerManager = new TimerManager();
 * timerManager.setInterval('refresh', () => loadData(), 5000);
 * timerManager.setTimeout('timeout', () => handleTimeout(), 30000);
 * // 清理时
 * timerManager.clearAll();
 */
class TimerManager {
    constructor() {
        this.timers = new Map();
        this.createdCount = 0;
    }

    /**
     * 创建 setInterval 定时器
     * 
     * @param {string} id - 定时器唯一标识
     * @param {Function} callback - 回调函数
     * @param {number} delay - 延迟时间（毫秒）
     * @returns {number} 定时器 ID
     */
    setInterval(id, callback, delay) {
        // 清除同名的旧定时器
        this.clearTimer(id);
        
        const timerId = setInterval(() => {
            try {
                callback();
            } catch (error) {
                console.error(`Timer [${id}] execution error:`, error);
            }
        }, delay);
        
        this.timers.set(id, {
            type: 'interval',
            id: timerId,
            createdAt: Date.now(),
            delay: delay
        });
        
        this.createdCount++;
        
        if (typeof logger !== 'undefined') {
            logger.debug(`[Timer] Created interval: ${id}, delay: ${delay}ms`);
        }
        
        return timerId;
    }

    /**
     * 创建 setTimeout 定时器
     * 
     * @param {string} id - 定时器唯一标识
     * @param {Function} callback - 回调函数
     * @param {number} delay - 延迟时间（毫秒）
     * @returns {number} 定时器 ID
     */
    setTimeout(id, callback, delay) {
        // 清除同名的旧定时器
        this.clearTimer(id);
        
        const timerId = setTimeout(() => {
            try {
                callback();
            } catch (error) {
                console.error(`Timer [${id}] execution error:`, error);
            } finally {
                this.timers.delete(id);
            }
        }, delay);
        
        this.timers.set(id, {
            type: 'timeout',
            id: timerId,
            createdAt: Date.now(),
            delay: delay
        });
        
        this.createdCount++;
        
        if (typeof logger !== 'undefined') {
            logger.debug(`[Timer] Created timeout: ${id}, delay: ${delay}ms`);
        }
        
        return timerId;
    }

    /**
     * 清除指定定时器
     * 
     * @param {string} id - 定时器标识
     */
    clearTimer(id) {
        const timer = this.timers.get(id);
        if (timer) {
            if (timer.type === 'interval') {
                clearInterval(timer.id);
            } else {
                clearTimeout(timer.id);
            }
            
            const duration = Date.now() - timer.createdAt;
            if (typeof logger !== 'undefined') {
                logger.debug(`[Timer] Cleared ${timer.type}: ${id}, duration: ${duration}ms`);
            }
            
            this.timers.delete(id);
        }
    }

    /**
     * 清除所有定时器
     */
    clearAll() {
        let count = 0;
        for (const [id, timer] of this.timers.entries()) {
            if (timer.type === 'interval') {
                clearInterval(timer.id);
            } else {
                clearTimeout(timer.id);
            }
            count++;
        }
        
        this.timers.clear();
        
        if (typeof logger !== 'undefined') {
            logger.info(`[Timer] Cleared all ${count} timers. Total created: ${this.createdCount}`);
        }
    }

    /**
     * 获取定时器统计信息
     * 
     * @returns {Object} 统计信息
     */
    getStats() {
        let intervalCount = 0;
        let timeoutCount = 0;
        
        for (const timer of this.timers.values()) {
            if (timer.type === 'interval') {
                intervalCount++;
            } else {
                timeoutCount++;
            }
        }
        
        return {
            total: this.timers.size,
            intervals: intervalCount,
            timeouts: timeoutCount,
            created: this.createdCount
        };
    }

    /**
     * 列出所有活动定时器
     * 
     * @returns {Array} 定时器列表
     */
    list() {
        const list = [];
        for (const [id, timer] of this.timers.entries()) {
            list.push({
                id: id,
                type: timer.type,
                delay: timer.delay,
                createdAt: timer.createdAt,
                runningTime: Date.now() - timer.createdAt
            });
        }
        return list;
    }

    /**
     * 检查定时器是否存在
     * 
     * @param {string} id - 定时器标识
     * @returns {boolean} 是否存在
     */
    exists(id) {
        return this.timers.has(id);
    }

    /**
     * 重启定时器（清除后重新创建）
     * 
     * @param {string} id - 定时器标识
     * @param {Function} callback - 回调函数
     * @param {number} delay - 延迟时间（毫秒）
     */
    restart(id, callback, delay) {
        const timer = this.timers.get(id);
        if (timer && timer.type === 'interval') {
            this.clearTimer(id);
            this.setInterval(id, callback, delay);
        } else {
            throw new Error(`Cannot restart: Timer ${id} is not an interval`);
        }
    }
}

export default TimerManager;
