// 主应用程序
class App {
    constructor() {
        this.init();
    }

    // 初始化应用
    init() {
        this.bindGlobalEvents();
        this.setupAutoSave();
        this.startBackgroundTasks();
    }

    // 绑定全局事件
    bindGlobalEvents() {
        // 页面加载完成后的初始化
        document.addEventListener('DOMContentLoaded', () => {
            this.onPageLoad();
        });

        // 页面卸载前的清理
        window.addEventListener('beforeunload', () => {
            this.onPageUnload();
        });

        // 网络状态监听
        window.addEventListener('online', () => {
            showToast('网络连接已恢复', 'success');
        });

        window.addEventListener('offline', () => {
            showToast('网络连接已断开', 'warning');
        });
    }

    // 页面加载完成
    onPageLoad() {
        console.log('修仙挂机游戏加载完成');
        
        // 设置页面标题
        document.title = '修仙挂机游戏';
        
        // 初始化工具提示
        this.initTooltips();
        
        // 检查浏览器兼容性
        this.checkBrowserCompatibility();
    }

    // 页面卸载
    onPageUnload() {
        // 清理定时器
        if (gameManager.cultivationTimer) {
            clearInterval(gameManager.cultivationTimer);
        }
        
        // 保存游戏状态
        this.saveGameState();
    }

    // 检查浏览器兼容性
    checkBrowserCompatibility() {
        const requiredFeatures = [
            'fetch',
            'Promise',
            'localStorage',
            'JSON'
        ];

        const missingFeatures = requiredFeatures.filter(feature => {
            if (feature === 'fetch') {
                return typeof fetch === 'undefined';
            }
            return typeof window[feature] === 'undefined';
        });

        if (missingFeatures.length > 0) {
            showToast('您的浏览器版本过低，可能无法正常运行游戏', 'error');
            console.warn('Missing browser features:', missingFeatures);
        }
    }

    // 初始化工具提示
    initTooltips() {
        // 为有title属性的元素添加提示
        document.addEventListener('mouseover', (e) => {
            if (e.target.title) {
                this.showTooltip(e.target, e.target.title);
            }
        });

        document.addEventListener('mouseout', (e) => {
            if (e.target.title) {
                this.hideTooltip();
            }
        });
    }

    // 显示工具提示
    showTooltip(element, text) {
        const tooltip = document.createElement('div');
        tooltip.className = 'tooltip';
        tooltip.textContent = text;
        tooltip.style.cssText = `
            position: absolute;
            background: #333;
            color: white;
            padding: 5px 10px;
            border-radius: 4px;
            font-size: 12px;
            z-index: 1000;
            pointer-events: none;
            white-space: nowrap;
        `;

        document.body.appendChild(tooltip);

        const rect = element.getBoundingClientRect();
        tooltip.style.left = rect.left + 'px';
        tooltip.style.top = (rect.top - tooltip.offsetHeight - 5) + 'px';

        this.currentTooltip = tooltip;
    }

    // 隐藏工具提示
    hideTooltip() {
        if (this.currentTooltip) {
            document.body.removeChild(this.currentTooltip);
            this.currentTooltip = null;
        }
    }

    // 设置自动保存
    setupAutoSave() {
        // 每30秒自动保存一次
        setInterval(() => {
            this.saveGameState();
        }, 30000);
    }

    // 保存游戏状态
    saveGameState() {
        try {
            const gameState = {
                timestamp: Date.now(),
                player: authManager.player,
                cultivationTime: gameManager.cultivationTime,
                isCultivating: gameManager.isCultivating
            };

            localStorage.setItem('xiuxianGameState', JSON.stringify(gameState));
        } catch (error) {
            console.error('Failed to save game state:', error);
        }
    }

    // 加载游戏状态
    loadGameState() {
        try {
            const savedState = localStorage.getItem('xiuxianGameState');
            if (savedState) {
                const gameState = JSON.parse(savedState);
                
                // 检查保存时间，如果超过7天则忽略
                const daysSinceSave = (Date.now() - gameState.timestamp) / (1000 * 60 * 60 * 24);
                if (daysSinceSave > 7) {
                    localStorage.removeItem('xiuxianGameState');
                    return;
                }

                // 恢复修炼状态
                if (gameState.isCultivating) {
                    const offlineTime = (Date.now() - gameState.timestamp) / 1000;
                    gameManager.cultivationTime = gameState.cultivationTime + offlineTime;
                    
                    if (offlineTime > 60) { // 离线超过1分钟
                        showToast(`检测到离线修炼 ${Math.floor(offlineTime / 60)} 分钟`, 'info');
                    }
                }
            }
        } catch (error) {
            console.error('Failed to load game state:', error);
        }
    }

    // 启动后台任务
    startBackgroundTasks() {
        // 每5秒更新一次UI
        setInterval(() => {
            this.updateUI();
        }, 5000);

        // 每分钟检查一次网络状态
        setInterval(() => {
            this.checkNetworkStatus();
        }, 60000);
    }

    // 更新UI
    updateUI() {
        if (authManager.isAuthenticated && authManager.player) {
            // 更新修炼时间显示
            if (gameManager.isCultivating) {
                const timeElement = document.getElementById('cultivationTime');
                if (timeElement) {
                    timeElement.textContent = Math.floor(gameManager.cultivationTime);
                }
            }

            // 更新当前时间显示
            const now = new Date();
            const timeString = now.toLocaleTimeString('zh-CN');
            const timeElement = document.getElementById('currentTime');
            if (timeElement) {
                timeElement.textContent = timeString;
            }
        }
    }

    // 检查网络状态
    checkNetworkStatus() {
        if (!navigator.onLine) {
            showToast('网络连接异常，部分功能可能无法使用', 'warning');
        }
    }
}

// 工具函数
const Utils = {
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
            return `${hours}小时${minutes}分钟`;
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

    // 防抖函数
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    // 节流函数
    throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
};

// 全局工具函数
window.Utils = Utils;

// 显示消息提示
function showToast(message, type = 'info', duration = 3000) {
    const toast = document.getElementById('toast');
    const messageElement = document.getElementById('toastMessage');
    
    if (!toast || !messageElement) {
        console.warn('Toast elements not found');
        return;
    }

    // 设置消息内容
    messageElement.textContent = message;
    
    // 设置样式
    toast.className = `toast ${type}`;
    
    // 显示提示
    toast.classList.remove('hidden');
    
    // 自动隐藏
    setTimeout(() => {
        toast.classList.add('hidden');
    }, duration);
}

// 显示/隐藏加载动画
function showLoading(show = true) {
    const loading = document.getElementById('loading');
    if (loading) {
        if (show) {
            loading.classList.remove('hidden');
        } else {
            loading.classList.add('hidden');
        }
    }
}

// 确认对话框
function confirm(message, callback) {
    if (window.confirm(message)) {
        callback();
    }
}

// 提示对话框
function alert(message) {
    window.alert(message);
}

// 创建应用实例
const app = new App();

// 导出到全局作用域
window.app = app;
window.showToast = showToast;
window.showLoading = showLoading;
window.confirm = confirm;
window.alert = alert;