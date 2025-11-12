// 主应用程序
class App {
    constructor() {
        this.isInitialized = false;
        this.performanceMetrics = {
            pageLoadTime: 0,
            apiResponseTimes: [],
            resourceLoadTimes: []
        };
        this.currentTooltip = null;
        this.notificationQueue = [];
        this.isShowingNotification = false;
        this.fpsCounter = null;
        this.init();
    }

    // 初始化应用
    init() {
        if (this.isInitialized) return;

        this.performanceMetrics.pageLoadTime = performance.now();

        this.bindGlobalEvents();
        this.setupErrorHandling();
        this.setupAutoSave();
        this.setupPerformanceMonitoring();

        this.isInitialized = true;

        // 记录页面加载时间
        window.addEventListener('load', () => {
            this.performanceMetrics.pageLoadTime = performance.now() - this.performanceMetrics.pageLoadTime;
            console.log(`页面加载完成，耗时: ${this.performanceMetrics.pageLoadTime.toFixed(2)}ms`);
        });
    }

    // 绑定全局事件
    bindGlobalEvents() {
        // 页面生命周期事件
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.onDOMContentLoaded());
        } else {
            this.onDOMContentLoaded();
        }

        window.addEventListener('beforeunload', (e) => this.onBeforeUnload(e));
        window.addEventListener('pagehide', () => this.onPageHide());

        // 网络状态事件
        window.addEventListener('online', () => this.onNetworkStatusChange(true));
        window.addEventListener('offline', () => this.onNetworkStatusChange(false));

        // 可见性变化事件
        document.addEventListener('visibilitychange', () => this.onVisibilityChange());

        // 键盘事件
        document.addEventListener('keydown', (e) => this.onKeyDown(e));

        // 错误事件 - 修复递归调用
        window.addEventListener('error', (e) => this.onGlobalError(e));
        window.addEventListener('unhandledrejection', (e) => this.onUnhandledRejection(e));
    }

    // DOM内容加载完成
    onDOMContentLoaded() {
        console.log('修仙挂机游戏初始化完成');

        this.initUI();
        this.checkBrowserCompatibility();
        this.setupShortcuts();
        this.startBackgroundTasks();
    }

    // 初始化UI组件
    initUI() {
        this.initTooltips();
        this.initModals();
        this.initNotifications();
        this.initLoadingIndicator();
    }

    // 初始化工具提示
    initTooltips() {
        // 使用事件委托处理工具提示
        document.addEventListener('mouseover', (e) => {
            const target = e.target;
            const tooltip = target.getAttribute('data-tooltip') || target.title;

            if (tooltip && !target.hasAttribute('data-tooltip-initialized')) {
                target.setAttribute('data-tooltip-initialized', 'true');
                target.addEventListener('mouseenter', (e) => this.showTooltip(e));
                target.addEventListener('mouseleave', () => this.hideTooltip());
            }
        });
    }

    // 显示工具提示
    showTooltip(e) {
        const target = e.target;
        const tooltipText = target.getAttribute('data-tooltip') || target.title;

        if (!tooltipText) return;

        this.hideTooltip();

        const tooltip = document.createElement('div');
        tooltip.className = 'advanced-tooltip';
        tooltip.textContent = tooltipText;

        Object.assign(tooltip.style, {
            position: 'fixed',
            background: 'rgba(0, 0, 0, 0.8)',
            color: 'white',
            padding: '8px 12px',
            borderRadius: '4px',
            fontSize: '12px',
            zIndex: '10000',
            pointerEvents: 'none',
            whiteSpace: 'nowrap',
            maxWidth: '200px',
            wordWrap: 'break-word'
        });

        document.body.appendChild(tooltip);
        this.currentTooltip = tooltip;

        this.positionTooltip(tooltip, target);
    }

    // 定位工具提示
    positionTooltip(tooltip, target) {
        const rect = target.getBoundingClientRect();
        const tooltipRect = tooltip.getBoundingClientRect();

        let top = rect.top - tooltipRect.height - 5;
        let left = rect.left + (rect.width - tooltipRect.width) / 2;

        // 确保不会超出视口
        if (top < 0) {
            top = rect.bottom + 5;
        }
        if (left < 0) {
            left = 0;
        }
        if (left + tooltipRect.width > window.innerWidth) {
            left = window.innerWidth - tooltipRect.width - 5;
        }

        tooltip.style.top = top + 'px';
        tooltip.style.left = left + 'px';
    }

    // 隐藏工具提示
    hideTooltip() {
        if (this.currentTooltip && this.currentTooltip.parentElement) {
            this.currentTooltip.parentElement.removeChild(this.currentTooltip);
            this.currentTooltip = null;
        }
    }

    // 初始化模态框
    initModals() {
        // 关闭所有模态框的全局事件
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal-backdrop')) {
                this.closeAllModals();
            }
        });

        // ESC键关闭模态框
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeAllModals();
            }
        });
    }

    // 显示模态框
    showModal(modalId, options = {}) {
        const modal = document.getElementById(modalId);
        if (!modal) return;

        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';

        // 添加动画效果
        setTimeout(() => {
            modal.classList.add('show');
        }, 10);

        // 自动关闭设置
        if (options.autoClose) {
            setTimeout(() => {
                this.closeModal(modalId);
            }, options.autoClose);
        }
    }

    // 关闭模态框
    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) return;

        modal.classList.remove('show');
        setTimeout(() => {
            modal.style.display = 'none';
            document.body.style.overflow = '';
        }, 300);
    }

    // 关闭所有模态框
    closeAllModals() {
        document.querySelectorAll('.modal').forEach(modal => {
            this.closeModal(modal.id);
        });
    }

    // 初始化通知系统
    initNotifications() {
        this.notificationQueue = [];
        this.isShowingNotification = false;
    }

    // 显示通知
    showNotification(message, type = 'info', duration = 5000) {
        this.notificationQueue.push({ message, type, duration });
        this.processNotificationQueue();
    }

    // 处理通知队列
    processNotificationQueue() {
        if (this.isShowingNotification || this.notificationQueue.length === 0) {
            return;
        }

        this.isShowingNotification = true;
        const { message, type, duration } = this.notificationQueue.shift();

        this.createNotification(message, type, duration);
    }

    // 创建通知
    createNotification(message, type, duration) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-message">${message}</span>
                <button class="notification-close">×</button>
            </div>
        `;

        // 添加关闭事件
        notification.querySelector('.notification-close').addEventListener('click', () => {
            if (notification.parentElement) {
                notification.parentElement.removeChild(notification);
            }
            this.isShowingNotification = false;
            this.processNotificationQueue();
        });

        // 添加样式
        Object.assign(notification.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            background: this.getNotificationColor(type),
            color: 'white',
            padding: '15px 20px',
            borderRadius: '8px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
            zIndex: '10000',
            maxWidth: '300px',
            animation: 'slideInRight 0.3s ease'
        });

        document.body.appendChild(notification);

        // 自动关闭
        setTimeout(() => {
            if (notification.parentElement) {
                notification.style.animation = 'slideOutRight 0.3s ease';
                setTimeout(() => {
                    if (notification.parentElement) {
                        notification.parentElement.removeChild(notification);
                    }
                    this.isShowingNotification = false;
                    this.processNotificationQueue();
                }, 300);
            }
        }, duration);
    }

    // 获取通知颜色
    getNotificationColor(type) {
        const colors = {
            info: '#3498db',
            success: '#2ecc71',
            warning: '#f39c12',
            error: '#e74c3c'
        };
        return colors[type] || colors.info;
    }

    // 初始化加载指示器
    initLoadingIndicator() {
        // 创建全局加载指示器
        const loadingIndicator = document.createElement('div');
        loadingIndicator.id = 'global-loading';
        loadingIndicator.innerHTML = `
            <div class="loading-spinner"></div>
            <div class="loading-text">加载中...</div>
        `;

        Object.assign(loadingIndicator.style, {
            position: 'fixed',
            top: '0',
            left: '0',
            width: '100%',
            height: '100%',
            background: 'rgba(0, 0, 0, 0.7)',
            display: 'none',
            justifyContent: 'center',
            alignItems: 'center',
            flexDirection: 'column',
            zIndex: '9999',
            color: 'white'
        });

        document.body.appendChild(loadingIndicator);
    }

    // 显示加载指示器
    showGlobalLoading(show = true) {
        const loading = document.getElementById('global-loading');
        if (loading) {
            loading.style.display = show ? 'flex' : 'none';
        }
    }

    // 检查浏览器兼容性
    checkBrowserCompatibility() {
        const features = {
            'ES6': () => typeof Promise !== 'undefined',
            'Fetch': () => typeof fetch !== 'undefined',
            'LocalStorage': () => typeof Storage !== 'undefined',
            'CSS Grid': () => window.CSS && CSS.supports('display', 'grid'),
            'Flexbox': () => window.CSS && CSS.supports('display', 'flex')
        };

        const unsupported = Object.entries(features)
            .filter(([name, test]) => !test())
            .map(([name]) => name);

        if (unsupported.length > 0) {
            console.warn('不支持的浏览器特性:', unsupported);
            this.showNotification(
                `您的浏览器缺少某些功能: ${unsupported.join(', ')}`,
                'warning',
                10000
            );
        }
    }

    // 设置快捷键
    setupShortcuts() {
        document.addEventListener('keydown', (e) => {
            // Ctrl + S: 快速保存
            if (e.ctrlKey && e.key === 's') {
                e.preventDefault();
                this.quickSave();
            }

            // F5: 刷新数据
            if (e.key === 'F5') {
                e.preventDefault();
                this.refreshGameData();
            }
        });
    }

    // 快速保存
    quickSave() {
        if (window.authManager && window.authManager.isAuthenticated) {
            window.authManager.saveGameState();
            this.showNotification('游戏已保存', 'success', 2000);
        }
    }

    // 刷新游戏数据
    refreshGameData() {
        if (window.authManager && window.authManager.isAuthenticated) {
            window.authManager.loadGameData();
            this.showNotification('数据刷新中...', 'info', 2000);
        }
    }

    // 设置错误处理 - 修复递归调用
    setupErrorHandling() {
        // 保存原始console.error
        const originalConsoleError = console.error;

        // 重写console.error，避免递归调用
        console.error = (...args) => {
            // 先调用原始方法
            originalConsoleError.apply(console, args);

            // 然后记录错误，但避免递归
            try {
                this.logError('Console Error', args.join(' '));
            } catch (e) {
                // 如果记录错误时发生错误，避免无限递归
                originalConsoleError('Error in error handler:', e);
            }
        };

        // 捕获资源加载错误
        window.addEventListener('error', (e) => {
            if (e.target && e.target.tagName) {
                this.logError('Resource Error', `Failed to load ${e.target.tagName.toLowerCase()}: ${e.target.src || e.target.href}`);
            }
        }, true);
    }

    // 记录错误 - 修复递归调用
    logError(type, message, extra = {}) {
        const errorLog = {
            type,
            message,
            timestamp: new Date().toISOString(),
            url: window.location.href,
            userAgent: navigator.userAgent,
            ...extra
        };

        // 直接使用console.log避免递归
        console.log('Error Log:', errorLog);

        // 存储到本地存储供后续分析
        try {
            const errors = JSON.parse(localStorage.getItem('app_errors') || '[]');
            errors.push(errorLog);
            localStorage.setItem('app_errors', JSON.stringify(errors.slice(-50))); // 只保留最近50个错误
        } catch (e) {
            console.log('Failed to save error log:', e);
        }
    }

    // 设置性能监控
    setupPerformanceMonitoring() {
        // 监控长任务
        if ('PerformanceObserver' in window) {
            const observer = new PerformanceObserver((list) => {
                list.getEntries().forEach(entry => {
                    if (entry.duration > 100) { // 超过100ms的任务
                        console.warn('长任务 detected:', entry);
                    }
                });
            });
            observer.observe({ entryTypes: ['longtask'] });
        }

        // 监控内存使用（Chrome only）
        if ('memory' in performance) {
            setInterval(() => {
                const memory = performance.memory;
                if (memory.usedJSHeapSize > memory.jsHeapSizeLimit * 0.8) {
                    console.warn('内存使用过高:', memory);
                }
            }, 30000);
        }
    }

    // 设置自动保存
    setupAutoSave() {
        setInterval(() => {
            if (window.authManager && window.authManager.isAuthenticated) {
                this.saveApplicationState();
            }
        }, 60000); // 每分钟自动保存
    }

    // 保存应用状态
    saveApplicationState() {
        const state = {
            timestamp: Date.now(),
            version: '1.0.0',
            user: window.authManager ? window.authManager.currentUser : null,
            page: window.location.href
        };

        try {
            localStorage.setItem('app_state', JSON.stringify(state));
        } catch (error) {
            console.error('自动保存失败:', error);
        }
    }

    // 初始化Service Worker（PWA支持）
    initServiceWorker() {
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker.register('/sw.js')
                .then(registration => {
                    console.log('Service Worker 注册成功:', registration);
                })
                .catch(error => {
                    console.log('Service Worker 注册失败:', error);
                });
        }
    }

    // 启动后台任务
    startBackgroundTasks() {
        // 每5秒更新一次UI
        this.uiUpdateInterval = setInterval(() => this.updateUI(), 5000);

        // 每30秒检查一次网络状态
        this.networkCheckInterval = setInterval(() => this.checkNetworkStatus(), 30000);

        // 每分钟清理一次缓存
        this.cacheCleanupInterval = setInterval(() => this.cleanupCache(), 60000);
    }

    // 更新UI
    updateUI() {
        if (window.authManager && window.authManager.isAuthenticated) {
            // 更新在线时间
            this.updateOnlineTime();

            // 更新性能指标
            this.updatePerformanceMetrics();
        }
    }

    // 更新在线时间
    updateOnlineTime() {
        const onlineTimeElement = document.getElementById('onlineTime');
        if (onlineTimeElement && window.authManager && window.authManager.loginTime) {
            const onlineTime = Math.floor((Date.now() - window.authManager.loginTime) / 1000);
            onlineTimeElement.textContent = this.formatTime(onlineTime);
        }
    }

    // 更新性能指标
    updatePerformanceMetrics() {
        const fpsElement = document.getElementById('fpsCounter');
        if (fpsElement) {
            this.fpsCounter = this.fpsCounter || { frameCount: 0, lastTime: performance.now() };

            this.fpsCounter.frameCount++;
            const currentTime = performance.now();

            if (currentTime >= this.fpsCounter.lastTime + 1000) {
                const fps = Math.round((this.fpsCounter.frameCount * 1000) / (currentTime - this.fpsCounter.lastTime));
                fpsElement.textContent = `FPS: ${fps}`;
                this.fpsCounter.frameCount = 0;
                this.fpsCounter.lastTime = currentTime;
            }
        }
    }

    // 检查网络状态
    checkNetworkStatus() {
        if (!navigator.onLine) {
            this.showNotification('网络连接已断开', 'warning', 5000);
        }
    }

    // 清理缓存
    cleanupCache() {
        if (window.cacheManager) {
            window.cacheManager.cleanup();
        }
    }

    // 页面卸载前
    onBeforeUnload(e) {
        if (window.authManager && window.authManager.isAuthenticated) {
            this.saveApplicationState();

            // 如果有未保存的数据，提示用户
            if (this.hasUnsavedData()) {
                e.preventDefault();
                e.returnValue = '您有未保存的更改，确定要离开吗？';
                return e.returnValue;
            }
        }
    }

    // 页面隐藏
    onPageHide() {
        this.saveApplicationState();
    }

    // 网络状态变化
    onNetworkStatusChange(online) {
        const message = online ? '网络连接已恢复' : '网络连接已断开';
        const type = online ? 'success' : 'warning';
        this.showNotification(message, type, 3000);
    }

    // 页面可见性变化
    onVisibilityChange() {
        if (!document.hidden) {
            // 页面变为可见，刷新数据
            this.onPageVisible();
        }
    }

    // 页面可见
    onPageVisible() {
        if (window.authManager && window.authManager.isAuthenticated) {
            // 如果超过5分钟没有更新，刷新数据
            const lastUpdate = window.authManager.lastDataUpdate || 0;
            if (Date.now() - lastUpdate > 5 * 60 * 1000) {
                window.authManager.loadGameData();
            }
        }
    }

    // 键盘事件
    onKeyDown(e) {
        // 防止F5刷新
        if (e.key === 'F5') {
            e.preventDefault();
            this.refreshGameData();
        }

        // Ctrl+Shift+D: 打开调试面板
        if (e.ctrlKey && e.shiftKey && e.key === 'D') {
            e.preventDefault();
            this.toggleDebugPanel();
        }
    }

    // 全局错误处理
    onGlobalError(event) {
        this.logError('Global Error', event.message, {
            filename: event.filename,
            lineno: event.lineno,
            colno: event.colno
        });
    }

    // 未处理的Promise拒绝
    onUnhandledRejection(event) {
        this.logError('Unhandled Promise Rejection', event.reason?.message || 'Unknown error', {
            reason: event.reason
        });
    }

    // 切换调试面板
    toggleDebugPanel() {
        let debugPanel = document.getElementById('debug-panel');

        if (!debugPanel) {
            debugPanel = document.createElement('div');
            debugPanel.id = 'debug-panel';
            debugPanel.innerHTML = `
                <div class="debug-header">
                    <h3>调试面板</h3>
                    <button class="debug-close">关闭</button>
                </div>
                <div class="debug-content">
                    <pre id="debug-info"></pre>
                </div>
            `;

            // 添加关闭事件
            debugPanel.querySelector('.debug-close').addEventListener('click', () => {
                this.closeDebugPanel();
            });

            Object.assign(debugPanel.style, {
                position: 'fixed',
                top: '0',
                right: '0',
                width: '300px',
                height: '100%',
                background: 'white',
                borderLeft: '1px solid #ccc',
                zIndex: '10000',
                overflow: 'auto',
                padding: '10px'
            });

            document.body.appendChild(debugPanel);
            this.updateDebugInfo();
        } else {
            debugPanel.style.display = debugPanel.style.display === 'none' ? 'block' : 'none';
        }
    }

    // 更新调试信息
    updateDebugInfo() {
        const debugInfo = document.getElementById('debug-info');
        if (!debugInfo) return;

        const info = {
            timestamp: new Date().toISOString(),
            user: window.authManager ? window.authManager.currentUser : null,
            performance: this.performanceMetrics,
            memory: performance.memory ? {
                used: Math.round(performance.memory.usedJSHeapSize / 1024 / 1024) + 'MB',
                total: Math.round(performance.memory.totalJSHeapSize / 1024 / 1024) + 'MB',
                limit: Math.round(performance.memory.jsHeapSizeLimit / 1024 / 1024) + 'MB'
            } : 'N/A',
            network: navigator.onLine ? 'online' : 'offline',
            userAgent: navigator.userAgent
        };

        debugInfo.textContent = JSON.stringify(info, null, 2);
    }

    // 关闭调试面板
    closeDebugPanel() {
        const debugPanel = document.getElementById('debug-panel');
        if (debugPanel) {
            debugPanel.style.display = 'none';
        }
    }

    // 检查是否有未保存的数据
    hasUnsavedData() {
        // 这里可以添加检查逻辑
        return false;
    }

    // 格式化时间
    formatTime(seconds) {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;

        if (hours > 0) {
            return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
        } else {
            return `${minutes}:${secs.toString().padStart(2, '0')}`;
        }
    }

    // 显示消息（兼容旧代码）
    showToast(message, type = 'info', duration = 3000) {
        this.showNotification(message, type, duration);
    }

    // 显示加载（兼容旧代码）
    showLoading(show = true) {
        this.showGlobalLoading(show);
    }
}

// 工具函数
const Utils = {
    // 防抖函数
    debounce(func, wait, immediate = false) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                timeout = null;
                if (!immediate) func.apply(this, args);
            };
            const callNow = immediate && !timeout;
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
            if (callNow) func.apply(this, args);
        };
    },

    // 节流函数
    throttle(func, limit) {
        let inThrottle;
        return function(...args) {
            if (!inThrottle) {
                func.apply(this, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    },

    // 格式化数字
    formatNumber(num) {
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    },

    // 格式化时间
    formatTime(seconds) {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;

        if (hours > 0) {
            return `${hours}小时${minutes}分钟${secs}秒`;
        } else if (minutes > 0) {
            return `${minutes}分钟${secs}秒`;
        } else {
            return `${secs}秒`;
        }
    },

    // 生成随机ID
    generateId() {
        return Date.now().toString(36) + Math.random().toString(36).substr(2);
    },

    // 深度克隆对象
    deepClone(obj) {
        return JSON.parse(JSON.stringify(obj));
    },

    // 检查对象是否为空
    isEmpty(obj) {
        return Object.keys(obj).length === 0;
    },

    // 数组去重
    unique(arr) {
        return [...new Set(arr)];
    },

    // 获取随机数
    random(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    },

    // 延迟函数
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    },

    // 验证邮箱
    validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    },

    // 验证手机号
    validatePhone(phone) {
        const re = /^1[3-9]\d{9}$/;
        return re.test(phone);
    },

    // 本地存储封装
    storage: {
        set(key, value) {
            try {
                localStorage.setItem(key, JSON.stringify(value));
                return true;
            } catch (e) {
                console.error('存储失败:', e);
                return false;
            }
        },

        get(key, defaultValue = null) {
            try {
                const item = localStorage.getItem(key);
                return item ? JSON.parse(item) : defaultValue;
            } catch (e) {
                console.error('读取失败:', e);
                return defaultValue;
            }
        },

        remove(key) {
            try {
                localStorage.removeItem(key);
                return true;
            } catch (e) {
                console.error('删除失败:', e);
                return false;
            }
        },

        clear() {
            try {
                localStorage.clear();
                return true;
            } catch (e) {
                console.error('清空失败:', e);
                return false;
            }
        }
    }
};

// 创建应用实例
const app = new App();

// 全局工具函数
window.Utils = Utils;

// 全局函数
window.showToast = (message, type = 'info', duration = 3000) => {
    app.showToast(message, type, duration);
};

window.showLoading = (show = true) => {
    app.showLoading(show);
};

window.confirm = (message, callback) => {
    if (window.confirm(message)) {
        callback();
    }
};

// 全局游戏函数
window.startCultivation = () => {
    if (window.gameManager) {
        window.gameManager.startCultivation();
    } else {
        console.error('GameManager not initialized');
    }
};

window.stopCultivation = () => {
    if (window.gameManager) {
        window.gameManager.stopCultivation();
    } else {
        console.error('GameManager not initialized');
    }
};

window.toggleCultivation = () => {
    if (window.gameManager) {
        window.gameManager.toggleCultivation();
    } else {
        console.error('GameManager not initialized');
    }
};

window.claimOfflineRewards = () => {
    if (window.gameManager) {
        window.gameManager.claimOfflineRewards();
    } else {
        console.error('GameManager not initialized');
    }
};

// 导出到全局作用域
window.app = app;

console.log('修仙挂机游戏初始化完成');