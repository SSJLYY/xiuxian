// 移动端交互和用户体验增强
class MobileInteraction {
    constructor() {
        this.overlay = null;
        this.sidebar = null;
        this.menuBtn = null;
        this.isMenuOpen = false;

        this.init();
    }

    init() {
        // 创建覆盖层
        this.createOverlay();

        // 获取元素
        this.sidebar = document.getElementById('sidebar');
        this.menuBtn = document.querySelector('.mobile-menu-btn');

        // 绑定事件
        this.bindEvents();

        // 初始化触摸手势
        this.initGestures();

        // 初始化页面切换动画
        this.initPageTransitions();

        // 初始化加载状态管理
        this.initLoadingStates();
    }

    createOverlay() {
        this.overlay = document.createElement('div');
        this.overlay.className = 'mobile-menu-overlay';
        this.overlay.addEventListener('click', () => this.closeMenu());
        document.body.appendChild(this.overlay);
    }

    bindEvents() {
        // 移动端菜单按钮点击事件
        if (this.menuBtn) {
            this.menuBtn.addEventListener('click', () => this.toggleMenu());
        }

        // 监听窗口大小变化
        window.addEventListener('resize', () => this.handleResize());

        // 监听页面可见性变化
        document.addEventListener('visibilitychange', () => this.handleVisibilityChange());

        // 键盘导航
        document.addEventListener('keydown', (e) => this.handleKeydown(e));
    }

    toggleMenu() {
        if (this.isMenuOpen) {
            this.closeMenu();
        } else {
            this.openMenu();
        }
    }

    openMenu() {
        this.isMenuOpen = true;
        this.sidebar.classList.add('mobile-expanded');
        this.overlay.classList.add('active');

        // 阻止背景滚动
        document.body.style.overflow = 'hidden';

        // 更新菜单按钮状态
        if (this.menuBtn) {
            this.menuBtn.innerHTML = '<i class="fa-solid fa-times"></i>';
            this.menuBtn.setAttribute('aria-expanded', 'true');
        }

        // 播放开启动画
        this.playSound('menuOpen');
    }

    closeMenu() {
        this.isMenuOpen = false;
        this.sidebar.classList.remove('mobile-expanded');
        this.overlay.classList.remove('active');

        // 恢复背景滚动
        document.body.style.overflow = '';

        // 更新菜单按钮状态
        if (this.menuBtn) {
            this.menuBtn.innerHTML = '<i class="fa-solid fa-bars"></i>';
            this.menuBtn.setAttribute('aria-expanded', 'false');
        }

        // 播放关闭动画
        this.playSound('menuClose');
    }

    handleResize() {
        // 在大屏上自动关闭移动菜单
        if (window.innerWidth >= 1024 && this.isMenuOpen) {
            this.closeMenu();
        }
    }

    handleVisibilityChange() {
        // 页面隐藏时关闭菜单
        if (document.hidden && this.isMenuOpen) {
            this.closeMenu();
        }
    }

    handleKeydown(e) {
        // ESC键关闭菜单
        if (e.key === 'Escape' && this.isMenuOpen) {
            this.closeMenu();
        }
    }

    // 初始化触摸手势
    initGestures() {
        let startX = 0;
        let currentX = 0;
        let isDragging = false;

        document.addEventListener('touchstart', (e) => {
            startX = e.touches[0].clientX;
            isDragging = false;
        }, { passive: true });

        document.addEventListener('touchmove', (e) => {
            if (!this.isMenuOpen) {
                // 从左边缘向右滑动打开菜单
                if (startX < 20) {
                    currentX = e.touches[0].clientX;
                    if (currentX - startX > 50) {
                        this.openMenu();
                    }
                }
            } else {
                // 向左滑动关闭菜单
                currentX = e.touches[0].clientX;
                if (startX - currentX > 50) {
                    this.closeMenu();
                }
            }
        }, { passive: true });

        document.addEventListener('touchend', () => {
            startX = 0;
            currentX = 0;
            isDragging = false;
        }, { passive: true });
    }

    // 初始化页面切换动画
    initPageTransitions() {
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    const target = mutation.target;
                    if (target.classList.contains('module') && target.classList.contains('active')) {
                        this.animatePageEntry(target);
                    }
                }
            });
        });

        // 观察所有模块
        document.querySelectorAll('.module').forEach((module) => {
            observer.observe(module, { attributes: true, attributeFilter: ['class'] });
        });
    }

    animatePageEntry(element) {
        // 添加入场动画
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';

        requestAnimationFrame(() => {
            element.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        });

        // 动画完成后清理
        setTimeout(() => {
            element.style.transition = '';
        }, 400);
    }

    // 初始化加载状态管理
    initLoadingStates() {
        // 监听所有带loading属性的元素
        document.querySelectorAll('[loading]').forEach((element) => {
            element.classList.add('will-animate');
        });
    }

    // 播放音效
    playSound(type) {
        if (typeof gameAudio !== 'undefined' && gameAudio.play) {
            gameAudio.play(type);
        }
    }

    // 添加触摸反馈
    addTouchFeedback(element) {
        element.addEventListener('touchstart', () => {
            element.style.transform = 'scale(0.95)';
        }, { passive: true });

        element.addEventListener('touchend', () => {
            element.style.transform = 'scale(1)';
        }, { passive: true });
    }

    // 添加长按菜单
    addLongPressMenu(element, callback) {
        let pressTimer;
        let isLongPress = false;

        element.addEventListener('touchstart', () => {
            isLongPress = false;
            pressTimer = setTimeout(() => {
                isLongPress = true;
                callback();
            }, 500);
        }, { passive: true });

        element.addEventListener('touchend', () => {
            clearTimeout(pressTimer);
        }, { passive: true });

        element.addEventListener('touchmove', () => {
            clearTimeout(pressTimer);
        }, { passive: true });
    }
}

// 通知系统增强
class NotificationSystem {
    constructor() {
        this.container = document.getElementById('notification-container');
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.className = 'notification-container';
            document.body.appendChild(this.container);
        }
    }

    show(message, type = 'info', duration = 3000) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <i class="notification-icon">${this.getIcon(type)}</i>
            <span class="notification-message">${message}</span>
            <button class="notification-close" aria-label="关闭">
                <i class="fa-solid fa-times"></i>
            </button>
        `;

        this.container.appendChild(notification);

        // 触发入场动画
        requestAnimationFrame(() => {
            notification.classList.add('show');
        });

        // 关闭按钮事件
        notification.querySelector('.notification-close').addEventListener('click', () => {
            this.hide(notification);
        });

        // 自动关闭
        if (duration > 0) {
            setTimeout(() => {
                this.hide(notification);
            }, duration);
        }

        return notification;
    }

    hide(notification) {
        notification.classList.remove('show');
        notification.classList.add('hide');

        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }

    getIcon(type) {
        const icons = {
            success: '<i class="fa-solid fa-check-circle"></i>',
            error: '<i class="fa-solid fa-exclamation-circle"></i>',
            warning: '<i class="fa-solid fa-exclamation-triangle"></i>',
            info: '<i class="fa-solid fa-info-circle"></i>'
        };
        return icons[type] || icons.info;
    }
}

// 加载状态管理器
class LoadingStateManager {
    constructor() {
        this.loadingOverlay = document.getElementById('loading');
        this.activeRequests = new Map();
    }

    show() {
        if (this.loadingOverlay) {
            this.loadingOverlay.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    }

    hide() {
        if (this.loadingOverlay) {
            this.loadingOverlay.style.display = 'none';
            document.body.style.overflow = '';
        }
    }

    async executeWithLoading(promise) {
        const requestId = Date.now();
        this.activeRequests.set(requestId, true);

        if (this.activeRequests.size === 1) {
            this.show();
        }

        try {
            const result = await promise;
            return result;
        } finally {
            this.activeRequests.delete(requestId);

            if (this.activeRequests.size === 0) {
                this.hide();
            }
        }
    }
}

// 创建全局实例
const mobileInteraction = new MobileInteraction();
const notificationSystem = new NotificationSystem();
const loadingStateManager = new LoadingStateManager();

// 导出全局函数
window.toggleMobileMenu = () => mobileInteraction.toggleMenu();

// 替换原有的showToast函数
window.showToast = (message, type = 'info', duration = 3000) => {
    notificationSystem.show(message, type, duration);
};

// 性能监控（开发环境）
if (typeof performance !== 'undefined' && 'getEntriesByType' in performance) {
    window.addEventListener('load', () => {
        setTimeout(() => {
            const perfData = performance.getEntriesByType('navigation')[0];
            if (perfData) {
                console.log('页面性能数据:', {
                    DNS查询: perfData.domainLookupEnd - perfData.domainLookupStart,
                    TCP连接: perfData.connectEnd - perfData.connectStart,
                    请求响应: perfData.responseEnd - perfData.responseStart,
                    DOM解析: perfData.domContentLoadedEventEnd - perfData.responseEnd,
                    页面加载: perfData.loadEventEnd - perfData.fetchStart
                });
            }
        }, 0);
    });
}

// 初始化完成后打印
console.log('📱 移动端交互系统已加载');
console.log('🎯 用户交互增强已启用');
