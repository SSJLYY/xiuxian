/**
 * 前端日志工具类
 * 
 * 根据环境自动启用/禁用日志输出
 * - 开发环境（localhost/127.0.0.1）：启用所有日志
 * - 生产环境：仅启用 error 级别日志
 * 
 * @example
 * logger.log('调试信息');
 * logger.error('错误信息');
 * logger.warn('警告信息');
 * logger.info('提示信息');
 */
class Logger {
    constructor() {
        // 判断是否为开发环境
        this.isDev = window.location.hostname === 'localhost' || 
                     window.location.hostname === '127.0.0.1' ||
                     window.location.hostname === '';
        
        // 检查 URL 参数是否强制启用日志 (?debug=true)
        const urlParams = new URLSearchParams(window.location.search);
        this.forceEnable = urlParams.get('debug') === 'true';
        
        this.enabled = this.isDev || this.forceEnable;
    }

    /**
     * 普通日志（开发环境启用）
     */
    log(...args) {
        if (this.enabled) {
            console.log(...args);
        }
    }

    /**
     * 错误日志（始终启用）
     */
    error(...args) {
        console.error(...args);
    }

    /**
     * 警告日志（开发环境启用）
     */
    warn(...args) {
        if (this.enabled) {
            console.warn(...args);
        }
    }

    /**
     * 信息日志（开发环境启用）
     */
    info(...args) {
        if (this.enabled) {
            console.info(...args);
        }
    }

    /**
     * 调试日志（开发环境启用）
     */
    debug(...args) {
        if (this.enabled) {
            console.debug(...args);
        }
    }

    /**
     * 表格日志（开发环境启用）
     */
    table(...args) {
        if (this.enabled) {
            console.table(...args);
        }
    }

    /**
     * 分组日志（开发环境启用）
     */
    group(label) {
        if (this.enabled && console.group) {
            console.group(label);
        }
    }

    /**
     * 结束分组（开发环境启用）
     */
    groupEnd() {
        if (this.enabled && console.groupEnd) {
            console.groupEnd();
        }
    }

    /**
     * 性能测试开始
     */
    time(label) {
        if (this.enabled) {
            console.time(label);
        }
    }

    /**
     * 性能测试结束
     */
    timeEnd(label) {
        if (this.enabled) {
            console.timeEnd(label);
        }
    }

    /**
     * 断言
     */
    assert(condition, ...args) {
        if (this.enabled || !condition) {
            console.assert(condition, ...args);
        }
    }

    /**
     * 追踪调用栈
     */
    trace() {
        if (this.enabled) {
            console.trace(...arguments);
        }
    }

    /**
     * 清除控制台
     */
    clear() {
        console.clear();
    }

    /**
     * 启用日志
     */
    enable() {
        this.enabled = true;
        this.log('Logger 已启用');
    }

    /**
     * 禁用日志
     */
    disable() {
        this.enabled = false;
    }

    /**
     * 切换日志状态
     */
    toggle() {
        this.enabled = !this.enabled;
        this.log(`Logger 已${this.enabled ? '启用' : '禁用'}`);
    }

    /**
     * 获取当前状态
     */
    getStatus() {
        return {
            enabled: this.enabled,
            isDev: this.isDev,
            forceEnable: this.forceEnable
        };
    }
}

// 创建全局 logger 实例
const logger = new Logger();

// 注册全局调试命令
if (typeof window !== 'undefined') {
    window.logger = logger;
    window.toggleLogs = () => logger.toggle();
}

export default logger;
