/**
 * 前端错误监控工具
 * 
 * <p>捕获和上报前端运行时错误、Promise 错误和性能数据</p>
 * 
 * 功能特性：
 * - 全局 JavaScript 错误捕获
 * - Promise unhandled rejection 捕获
 * - 页面加载性能监控
 * - API 请求性能监控
 * - 用户行为追踪（可选）
 * 
 * @author xiuxian-game-team
 * @version 1.0
 * @since 2026-04-20
 */
class ErrorMonitor {
    constructor(options = {}) {
        this.apiUrl = options.apiUrl || '/api/monitor/error';
        this.enabled = options.enabled !== false;
        this.debug = options.debug || false;
        this.maxQueueSize = options.maxQueueSize || 10;
        this.errorQueue = [];
        
        if (this.enabled) {
            this.install();
            if (this.debug) {
                console.log('[ErrorMonitor] 已启用');
            }
        }
    }

    /**
     * 安装监控器
     */
    install() {
        // 1. 全局 JavaScript 错误捕获
        window.addEventListener('error', (e) => {
            this.handleError({
                type: 'js_error',
                message: e.message,
                filename: e.filename,
                lineno: e.lineno,
                colno: e.colno,
                stack: e.error?.stack,
                url: window.location.href,
                userAgent: navigator.userAgent,
                timestamp: Date.now()
            });
        });

        // 2. Promise unhandled rejection 捕获
        window.addEventListener('unhandledrejection', (e) => {
            this.handleError({
                type: 'promise_rejection',
                reason: e.reason?.message || String(e.reason),
                stack: e.reason?.stack,
                url: window.location.href,
                userAgent: navigator.userAgent,
                timestamp: Date.now()
            });
            e.preventDefault(); // 阻止默认输出到控制台
        });

        // 3. 页面加载性能监控
        window.addEventListener('load', () => {
            setTimeout(() => {
                const timing = performance.timing;
                const pageLoadTime = timing.loadEventEnd - timing.navigationStart;
                
                // 只上报性能较差的情况（>3 秒）
                if (pageLoadTime > 3000) {
                    this.handlePerformance({
                        type: 'performance',
                        pageLoadTime,
                        dnsTime: timing.domainLookupEnd - timing.domainLookupStart,
                        tcpTime: timing.connectEnd - timing.connectStart,
                        responseTime: timing.responseEnd - timing.requestStart,
                        domTime: timing.domComplete - timing.domLoading,
                        url: window.location.href,
                        timestamp: Date.now()
                    });
                }
            }, 0);
        });

        // 4. 长时间任务监控
        if ('PerformanceObserver' in window) {
            try {
                const observer = new PerformanceObserver((list) => {
                    list.getEntries().forEach((entry) => {
                        if (entry.duration > 500) { // 超过 500ms 的长任务
                            this.handleLongTask(entry);
                        }
                    });
                });
                observer.observe({ entryTypes: ['longtask'] });
            } catch (e) {
                // 浏览器不支持
            }
        }
    }

    /**
     * 处理错误
     */
    handleError(error) {
        if (this.debug) {
            console.error('[ErrorMonitor] 检测到错误:', error);
        }
        
        this.addToQueue(error);
        this.flush();
    }

    /**
     * 处理性能数据
     */
    handlePerformance(perf) {
        if (this.debug) {
            console.log('[ErrorMonitor] 性能数据:', perf);
        }
        
        this.addToQueue(perf);
        this.flush();
    }

    /**
     * 处理长任务
     */
    handleLongTask(entry) {
        this.addToQueue({
            type: 'longtask',
            name: entry.name,
            startTime: entry.startTime,
            duration: entry.duration,
            url: window.location.href,
            timestamp: Date.now()
        });
        this.flush();
    }

    /**
     * 添加到队列
     */
    addToQueue(data) {
        this.errorQueue.push(data);
        
        // 队列满了，只保留最新的
        if (this.errorQueue.length > this.maxQueueSize) {
            this.errorQueue.shift();
        }
    }

    /**
     * 上报数据
     */
    flush() {
        if (this.errorQueue.length === 0) return;

        const data = [...this.errorQueue];
        this.errorQueue = [];

        // 使用 sendBeacon 确保数据发送成功（即使页面关闭）
        const blob = new Blob([JSON.stringify({
            errors: data,
            appVersion: '1.0.0',
            screen: `${window.screen.width}x${window.screen.height}`,
            language: navigator.language
        })], { type: 'application/json' });

        try {
            navigator.sendBeacon(this.apiUrl, blob);
        } catch (e) {
            // sendBeacon 失败，尝试 fetch
            fetch(this.apiUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ errors: data })
            }).catch(() => {});
        }
    }

    /**
     * 手动上报错误
     */
    report(error) {
        this.handleError({
            type: 'manual',
            ...error,
            url: window.location.href,
            userAgent: navigator.userAgent,
            timestamp: Date.now()
        });
    }

    /**
     * 设置用户标识（用于追踪特定用户的问题）
     */
    setUser(userId, extra = {}) {
        this.userId = userId;
        this.userExtra = extra;
    }

    /**
     * 禁用监控
     */
    disable() {
        this.enabled = false;
    }

    /**
     * 启用监控
     */
    enable() {
        this.enabled = true;
    }
}

// 自动初始化（生产环境）
if (typeof window !== 'undefined') {
    window.errorMonitor = new ErrorMonitor({
        apiUrl: '/api/monitor/error',
        enabled: true,
        debug: false // 生产环境关闭调试
    });
}

export default ErrorMonitor;
