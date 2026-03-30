/**
 * 路由系统
 * 实现SPA页面切换和模块懒加载
 */
import { toast } from '../components/Toast.js';
import { loading } from '../components/Loading.js';

export class Router {
    constructor() {
        this.routes = new Map();
        this.currentRoute = null;
        this.currentModule = null;
        this.modulesLoaded = new Set();
        this.contentContainer = null;
        this.moduleInstances = new Map();
    }

    /**
     * 初始化路由
     */
    init() {
        this.contentContainer = document.getElementById('content-container');
        if (!this.contentContainer) {
            console.error('未找到内容容器 #content-container');
            return;
        }

        // 注册路由
        this.registerRoutes();

        // 监听路由变化
        window.addEventListener('hashchange', () => this.handleRouteChange());

        // 监听导航点击
        this.bindNavigation();

        // 初始化默认路由
        this.handleRouteChange();
    }

    /**
     * 注册路由
     */
    registerRoutes() {
        // 游戏模块路由
        this.routes.set('#player', {
            path: '#player',
            module: 'player',
            title: '玩家信息',
            container: 'player-section'
        });

        this.routes.set('#combat', {
            path: '#combat',
            module: 'combat',
            title: '战斗',
            container: 'combat-section'
        });

        this.routes.set('#inventory', {
            path: '#inventory',
            module: 'inventory',
            title: '背包',
            container: 'inventory-section'
        });

        this.routes.set('#equipment', {
            path: '#equipment',
            module: 'equipment',
            title: '装备',
            container: 'equipment-section'
        });

        this.routes.set('#skills', {
            path: '#skills',
            module: 'skills',
            title: '技能',
            container: 'skills-section'
        });

        this.routes.set('#pets', {
            path: '#pets',
            module: 'pets',
            title: '宠物',
            container: 'pets-section'
        });

        this.routes.set('#cultivate', {
            path: '#cultivate',
            module: 'cultivate',
            title: '修炼',
            container: 'cultivate-section'
        });

        this.routes.set('#guild', {
            path: '#guild',
            module: 'guild',
            title: '宗门',
            container: 'guild-section'
        });

        this.routes.set('#auction', {
            path: '#auction',
            module: 'auction',
            title: '拍卖行',
            container: 'auction-section'
        });

        this.routes.set('#mail', {
            path: '#mail',
            module: 'mail',
            title: '邮件',
            container: 'mail-section'
        });

        this.routes.set('#ranking', {
            path: '#ranking',
            module: 'ranking',
            title: '排行榜',
            container: 'ranking-section'
        });

        this.routes.set('#achievement', {
            path: '#achievement',
            module: 'achievement',
            title: '成就',
            container: 'achievement-section'
        });

        this.routes.set('#checkin', {
            path: '#checkin',
            module: 'checkin',
            title: '签到',
            container: 'checkin-section'
        });

        this.routes.set('#vip', {
            path: '#vip',
            module: 'vip',
            title: 'VIP',
            container: 'vip-section'
        });

        this.routes.set('#activity', {
            path: '#activity',
            module: 'activity',
            title: '活动',
            container: 'activity-section'
        });

        this.routes.set('#narrative', {
            path: '#narrative',
            module: 'narrative',
            title: '叙事',
            container: 'narrative-section'
        });

        this.routes.set('#map', {
            path: '#map',
            module: 'map',
            title: '地图',
            container: 'map-section'
        });
    }

    /**
     * 绑定导航点击事件
     */
    bindNavigation() {
        document.querySelectorAll('[data-nav]').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const target = e.target.getAttribute('data-nav');
                this.navigateTo(`#${target}`);
            });
        });
    }

    /**
     * 导航到指定路由
     */
    navigateTo(hash) {
        window.location.hash = hash;
    }

    /**
     * 处理路由变化
     */
    async handleRouteChange() {
        const hash = window.location.hash || '#player';
        const route = this.routes.get(hash);

        if (!route) {
            console.error('路由不存在:', hash);
            toast.error('页面不存在');
            return;
        }

        // 如果是当前路由,不做任何操作
        if (this.currentRoute === hash) {
            return;
        }

        this.currentRoute = hash;

        try {
            loading.show();

            // 加载模块
            await this.loadModule(route.module);

            // 显示页面内容
            await this.showPage(route);

            // 更新页面标题
            document.title = `修仙挂机游戏 - ${route.title}`;

            // 更新导航高亮
            this.updateNavigation(hash);

            loading.hide();
        } catch (error) {
            loading.hide();
            console.error('路由切换失败:', error);
            toast.error('页面加载失败: ' + error.message);
        }
    }

    /**
     * 加载模块
     */
    async loadModule(moduleName) {
        // 如果模块已加载,直接返回
        if (this.modulesLoaded.has(moduleName)) {
            return;
        }

        try {
            // 动态导入模块
            const module = await import(`../modules/${moduleName}/index.js`);

            // 保存模块实例
            if (module[`${moduleName}UI`]) {
                this.moduleInstances.set(moduleName, module[`${moduleName}UI`]);
            }

            // 标记模块已加载
            this.modulesLoaded.add(moduleName);

            console.log(`模块 ${moduleName} 加载成功`);
        } catch (error) {
            console.error(`模块 ${moduleName} 加载失败:`, error);
            throw new Error(`模块 ${moduleName} 加载失败`);
        }
    }

    /**
     * 显示页面内容
     */
    async showPage(route) {
        // 清空内容容器
        this.contentContainer.innerHTML = '';

        // 创建页面元素
        const pageSection = document.createElement('section');
        pageSection.id = route.container;
        pageSection.className = 'module-section';
        pageSection.style.display = 'block';

        // 加载页面HTML
        const pageHtml = await this.loadPageHtml(route.module);
        pageSection.innerHTML = pageHtml;

        // 添加到容器
        this.contentContainer.appendChild(pageSection);

        // 初始化模块UI
        const moduleUI = this.moduleInstances.get(route.module);
        if (moduleUI) {
            moduleUI.init();
        }
    }

    /**
     * 加载页面HTML
     */
    async loadPageHtml(moduleName) {
        try {
            const response = await fetch(`/pages/game/${moduleName}.html`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.text();
        } catch (error) {
            console.error(`加载 ${moduleName}.html 失败:`, error);
            throw new Error(`页面 ${moduleName}.html 加载失败`);
        }
    }

    /**
     * 更新导航高亮
     */
    updateNavigation(hash) {
        // 移除所有高亮
        document.querySelectorAll('[data-nav]').forEach(link => {
            link.classList.remove('active');
        });

        // 添加当前路由高亮
        const moduleName = hash.replace('#', '');
        const activeLink = document.querySelector(`[data-nav="${moduleName}"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }
    }

    /**
     * 获取当前路由
     */
    getCurrentRoute() {
        return this.currentRoute;
    }

    /**
     * 获取当前模块
     */
    getCurrentModule() {
        return this.currentModule;
    }

    /**
     * 检查模块是否已加载
     */
    isModuleLoaded(moduleName) {
        return this.modulesLoaded.has(moduleName);
    }

    /**
     * 预加载模块
     */
    async preloadModule(moduleName) {
        if (!this.isModuleLoaded(moduleName)) {
            await this.loadModule(moduleName);
        }
    }

    /**
     * 预加载所有模块
     */
    async preloadAllModules() {
        const moduleNames = Array.from(this.routes.values()).map(route => route.module);
        await Promise.all(moduleNames.map(name => this.preloadModule(name)));
    }
}

// 导出单例
export const router = new Router();
