/**
 * 应用主入口
 * 负责初始化应用和加载模块
 */

import { storage } from './core/storage/Storage.js';
import { AuthStorage } from './core/storage/AuthStorage.js';
import { toast } from './components/Toast.js';

class App {
    constructor() {
        this.isInitialized = false;
        this.modules = [];
        this.currentModule = null;
    }

    /**
     * 初始化应用
     */
    async init() {
        if (this.isInitialized) {
            console.log('应用已初始化');
            return;
        }

        console.log('正在初始化应用...');

        try {
            // 检查登录状态
            await this.checkAuth();

            // 加载用户设置
            this.loadSettings();

            // 初始化核心模块
            await this.initCoreModules();

            // 初始化业务模块
            await this.initBusinessModules();

            // 绑定全局事件
            this.bindGlobalEvents();

            this.isInitialized = true;
            console.log('应用初始化完成');

        } catch (error) {
            console.error('应用初始化失败:', error);
            toast.error('应用初始化失败,请刷新页面重试');
        }
    }

    /**
     * 检查认证状态
     */
    async checkAuth() {
        const isLoggedIn = AuthStorage.isLoggedIn();

        if (!isLoggedIn) {
            // 检查当前页面是否为登录页
            if (!this.isLoginPage()) {
                // 重定向到登录页
                window.location.href = '/login.html';
            }
            return false;
        }

        return true;
    }

    /**
     * 判断是否为登录页
     */
    isLoginPage() {
        return window.location.pathname.includes('login.html') ||
               window.location.pathname.includes('register.html');
    }

    /**
     * 加载用户设置
     */
    loadSettings() {
        const settings = AuthStorage.getSettings({
            theme: 'dark',
            language: 'zh-CN',
            soundEnabled: true
        });

        // 应用主题
        this.applyTheme(settings.theme);

        // 应用其他设置
        this.applySettings(settings);
    }

    /**
     * 应用主题
     * @param {string} theme - 主题名称
     */
    applyTheme(theme) {
        document.body.setAttribute('data-theme', theme);
    }

    /**
     * 应用设置
     * @param {Object} settings - 设置对象
     */
    applySettings(settings) {
        // 保存设置到全局
        window.appSettings = settings;
    }

    /**
     * 初始化核心模块
     */
    async initCoreModules() {
        console.log('初始化核心模块...');

        // 这里可以初始化一些核心功能模块
        // 例如: 音频引擎、性能监控等

        console.log('核心模块初始化完成');
    }

    /**
     * 初始化业务模块
     */
    async initBusinessModules() {
        console.log('初始化业务模块...');

        // 根据当前页面加载对应的模块
        const pageName = this.getCurrentPageName();

        switch (pageName) {
            case 'game':
                await this.initGameModule();
                break;
            case 'admin':
                await this.initAdminModule();
                break;
            default:
                console.log('未识别的页面:', pageName);
        }

        console.log('业务模块初始化完成');
    }

    /**
     * 获取当前页面名称
     */
    getCurrentPageName() {
        const path = window.location.pathname;

        if (path.includes('game.html')) {
            return 'game';
        } else if (path.includes('admin.html')) {
            return 'admin';
        } else if (path.includes('login.html')) {
            return 'login';
        }

        return 'unknown';
    }

    /**
     * 初始化游戏模块
     */
    async initGameModule() {
        // 动态导入游戏模块
        try {
            const { playerUI } = await import('./modules/player/index.js');
            await playerUI.init();
            this.modules.push(playerUI);
        } catch (error) {
            console.error('初始化游戏模块失败:', error);
        }
    }

    /**
     * 初始化管理模块
     */
    async initAdminModule() {
        // 动态导入管理模块
        try {
            // 这里可以添加管理模块的初始化逻辑
            console.log('管理模块初始化完成');
        } catch (error) {
            console.error('初始化管理模块失败:', error);
        }
    }

    /**
     * 绑定全局事件
     */
    bindGlobalEvents() {
        // 页面可见性变化
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.onPageHidden();
            } else {
                this.onPageVisible();
            }
        });

        // 网络状态变化
        window.addEventListener('online', () => this.onNetworkOnline());
        window.addEventListener('offline', () => this.onNetworkOffline());

        // 页面卸载
        window.addEventListener('beforeunload', () => this.onBeforeUnload());

        // 错误处理
        window.addEventListener('error', (event) => this.handleError(event));

        // Promise错误处理
        window.addEventListener('unhandledrejection', (event) => this.handleUnhandledRejection(event));
    }

    /**
     * 页面隐藏时的处理
     */
    onPageHidden() {
        console.log('页面隐藏');
        // 可以在这里暂停一些自动操作
    }

    /**
     * 页面可见时的处理
     */
    onPageVisible() {
        console.log('页面可见');
        // 可以在这里恢复一些自动操作
    }

    /**
     * 网络恢复时的处理
     */
    onNetworkOnline() {
        console.log('网络已恢复');
        toast.success('网络已恢复');
    }

    /**
     * 网络断开时的处理
     */
    onNetworkOffline() {
        console.log('网络已断开');
        toast.warning('网络已断开,请检查网络连接');
    }

    /**
     * 页面卸载前的处理
     */
    onBeforeUnload() {
        // 保存一些临时数据
        this.saveTempData();
    }

    /**
     * 保存临时数据
     */
    saveTempData() {
        // 可以在这里保存一些临时数据到storage
    }

    /**
     * 处理全局错误
     * @param {ErrorEvent} event - 错误事件
     */
    handleError(event) {
        console.error('全局错误:', event.error);
        // 可以在这里上报错误到服务器
    }

    /**
     * 处理未处理的Promise拒绝
     * @param {PromiseRejectionEvent} event - 拒绝事件
     */
    handleUnhandledRejection(event) {
        console.error('未处理的Promise拒绝:', event.reason);
        // 可以在这里上报错误到服务器
    }

    /**
     * 销毁应用
     */
    destroy() {
        console.log('正在销毁应用...');

        // 销毁所有模块
        this.modules.forEach(module => {
            if (module.destroy && typeof module.destroy === 'function') {
                module.destroy();
            }
        });

        this.modules = [];
        this.currentModule = null;
        this.isInitialized = false;

        console.log('应用已销毁');
    }
}

// 创建全局应用实例
const app = new App();

// 导出应用实例
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { App, app };
}
