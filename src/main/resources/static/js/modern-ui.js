// 简化的现代UI交互脚本

class SimpleUI {
    constructor() {
        this.currentModule = 'dashboard';
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupTabSwitching();
        this.setupNavigation();
    }

    setupEventListeners() {
        // 登录表单
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleLogin();
            });
        }

        // 注册表单
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleRegister();
            });
        }
    }

    setupTabSwitching() {
        const tabButtons = document.querySelectorAll('.tab-button');
        tabButtons.forEach(button => {
            button.addEventListener('click', () => {
                const tab = button.dataset.tab;
                this.switchAuthTab(tab);
            });
        });
    }

    switchAuthTab(tab) {
        // 更新按钮状态
        document.querySelectorAll('.tab-button').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelector(`[data-tab="${tab}"]`).classList.add('active');

        // 切换表单
        document.querySelectorAll('.auth-form').forEach(form => {
            form.classList.remove('active');
        });
        document.getElementById(`${tab}Form`).classList.add('active');
    }

    setupNavigation() {
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => {
            const button = item.querySelector('.nav-button');
            if (button) {
                button.addEventListener('click', (e) => {
                    e.preventDefault();
                    const module = item.dataset.module;
                    console.log('导航点击:', module);
                    this.showModule(module);
                });
            }
        });
    }

    showModule(moduleName) {
        console.log('切换模块:', moduleName);
        
        // 更新导航状态
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });
        
        const activeNavItem = document.querySelector(`[data-module="${moduleName}"]`);
        if (activeNavItem) {
            activeNavItem.classList.add('active');
        }

        // 切换模块内容
        document.querySelectorAll('.module').forEach(module => {
            module.classList.remove('active');
        });
        
        const targetModule = document.getElementById(`${moduleName}-module`);
        if (targetModule) {
            targetModule.classList.add('active');
            this.currentModule = moduleName;
            console.log('模块切换成功:', moduleName);
            
            // 根据模块加载相应数据
            this.loadModuleData(moduleName);
        } else {
            console.error('找不到模块:', `${moduleName}-module`);
        }
    }

    loadModuleData(moduleName) {
        switch(moduleName) {
            case 'dashboard':
                // 修炼模块数据已经在主循环中更新
                console.log('加载修炼模块数据');
                break;
            case 'combat':
                console.log('加载战斗模块数据');
                break;
            case 'inventory':
                console.log('加载背包模块数据');
                break;
            case 'skills':
                console.log('加载技能模块数据');
                break;
            case 'shop':
                console.log('加载商城模块数据');
                break;
            case 'quests':
                console.log('加载任务模块数据');
                break;
        }
    }

    showNotification(message, type = 'info') {
        const container = document.getElementById('notification-container');
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;
        
        container.appendChild(notification);
        
        // 3秒后自动移除
        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    showLoading(show = true) {
        const loading = document.getElementById('loading');
        if (show) {
            loading.classList.add('active');
        } else {
            loading.classList.remove('active');
        }
    }

    handleLogin() {
        const username = document.getElementById('loginUsername').value;
        const password = document.getElementById('loginPassword').value;

        if (!username || !password) {
            this.showNotification('请填写用户名和密码', 'error');
            return;
        }

        this.showLoading(true);

        // 使用现有的登录系统
        if (window.authManager && typeof window.authManager.login === 'function') {
            // 设置表单值并调用现有的登录函数
            document.getElementById('loginUsername').value = username;
            document.getElementById('loginPassword').value = password;
            
            // 调用现有的登录函数
            window.authManager.login()
                .then(() => {
                    this.showLoading(false);
                    // 登录成功后会自动切换页面
                })
                .catch(error => {
                    this.showLoading(false);
                    this.showNotification('登录失败: ' + error.message, 'error');
                });
        } else if (window.login) {
            // 调用全局登录函数
            window.login({ preventDefault: () => {} });
            this.showLoading(false);
        } else {
            this.showLoading(false);
            this.showNotification('登录系统未初始化', 'error');
        }
    }

    handleRegister() {
        const username = document.getElementById('registerUsername').value;
        const nickname = document.getElementById('registerNickname').value;
        const email = document.getElementById('registerEmail').value;
        const password = document.getElementById('registerPassword').value;
        const confirmPassword = document.getElementById('registerConfirmPassword').value;

        if (!username || !nickname || !email || !password || !confirmPassword) {
            this.showNotification('请填写所有字段', 'error');
            return;
        }

        if (password !== confirmPassword) {
            this.showNotification('两次输入的密码不一致', 'error');
            return;
        }

        this.showLoading(true);

        // 使用现有的注册系统
        if (window.authManager && typeof window.authManager.register === 'function') {
            // 设置表单值并调用现有的注册函数
            document.getElementById('registerUsername').value = username;
            document.getElementById('registerNickname').value = nickname;
            document.getElementById('registerEmail').value = email;
            document.getElementById('registerPassword').value = password;
            document.getElementById('registerConfirmPassword').value = confirmPassword;
            
            // 调用现有的注册函数
            window.authManager.register()
                .then(() => {
                    this.showLoading(false);
                    this.showNotification('注册成功！请登录', 'success');
                    this.switchAuthTab('login');
                    document.getElementById('loginUsername').value = username;
                })
                .catch(error => {
                    this.showLoading(false);
                    this.showNotification('注册失败: ' + error.message, 'error');
                });
        } else if (window.register) {
            // 调用全局注册函数
            window.register({ preventDefault: () => {} });
            this.showLoading(false);
        } else {
            this.showLoading(false);
            this.showNotification('注册系统未初始化', 'error');
        }
    }

    switchToGamePage() {
        console.log('切换到游戏页面');
        const loginPage = document.getElementById('loginPage');
        const gamePage = document.getElementById('gamePage');
        
        if (loginPage) {
            loginPage.style.display = 'none';
            console.log('隐藏登录页面');
        }
        
        if (gamePage) {
            gamePage.classList.add('active');
            console.log('显示游戏页面');
        }
        
        document.body.classList.add('game-mode');
        console.log('添加游戏模式样式');
        
        this.loadPlayerData();
    }

    switchToLoginPage() {
        document.getElementById('gamePage').classList.remove('active');
        document.getElementById('loginPage').style.display = 'flex';
        document.body.classList.remove('game-mode');
    }

    loadPlayerData() {
        // 加载玩家数据
        if (window.authManager && typeof window.authManager.loadPlayerProfile === 'function') {
            window.authManager.loadPlayerProfile();
        } else if (typeof loadPlayerData === 'function') {
            loadPlayerData();
        }
    }

    startCultivation() {
        const cultivationBtn = document.getElementById('cultivation-btn');
        const stopBtn = document.getElementById('stop-cultivation-btn');
        const statusElement = document.getElementById('cultivationStatus');

        cultivationBtn.style.display = 'none';
        stopBtn.style.display = 'inline-block';
        statusElement.textContent = '修炼中...';
        
        this.showNotification('开始修炼', 'success');
        this.addCultivationLog('开始修炼，感悟天地之道...');

        // 调用API
        if (window.gameAPI && window.gameAPI.startCultivation) {
            window.gameAPI.startCultivation();
        }
    }

    stopCultivation() {
        const cultivationBtn = document.getElementById('cultivation-btn');
        const stopBtn = document.getElementById('stop-cultivation-btn');
        const statusElement = document.getElementById('cultivationStatus');

        cultivationBtn.style.display = 'inline-block';
        stopBtn.style.display = 'none';
        statusElement.textContent = '点击开始修炼';
        
        this.showNotification('停止修炼', 'info');
        this.addCultivationLog('停止修炼，收功完毕。');

        // 调用API
        if (window.gameAPI && window.gameAPI.stopCultivation) {
            window.gameAPI.stopCultivation();
        }
    }

    addCultivationLog(message) {
        const logContent = document.getElementById('cultivation-log');
        if (logContent) {
            const logEntry = document.createElement('div');
            logEntry.className = 'log-entry';
            logEntry.textContent = message;
            
            logContent.appendChild(logEntry);
            logContent.scrollTop = logContent.scrollHeight;
            
            // 限制日志条数
            const entries = logContent.querySelectorAll('.log-entry');
            if (entries.length > 20) {
                entries[0].remove();
            }
        }
    }

    logout() {
        if (confirm('确定要退出登录吗？')) {
            if (window.gameAPI && window.gameAPI.logout) {
                window.gameAPI.logout();
            }
            this.switchToLoginPage();
            this.showNotification('已退出登录', 'info');
        }
    }
}

// 初始化UI
let simpleUI;

document.addEventListener('DOMContentLoaded', () => {
    console.log('Modern UI 初始化开始');
    simpleUI = new SimpleUI();
    
    // 暴露全局方法
    window.simpleUI = simpleUI;
    window.showModule = (module) => simpleUI.showModule(module);
    window.startCultivation = () => simpleUI.startCultivation();
    window.stopCultivation = () => simpleUI.stopCultivation();
    window.logout = () => simpleUI.logout();
    window.testGamePage = () => simpleUI.switchToGamePage();
    
    console.log('Modern UI 初始化完成，全局方法已暴露');
    
    // 监听现有认证系统的登录成功事件
    // 如果已经登录，直接显示游戏界面
    setTimeout(() => {
        // 检查是否有token来判断登录状态
        const token = localStorage.getItem('authToken');
        console.log('检查登录状态，token存在:', !!token);
        
        if (token && window.authManager && window.authManager.isAuthenticated) {
            console.log('检测到已登录，切换到游戏界面');
            simpleUI.switchToGamePage();
        } else if (token) {
            console.log('有token但authManager未准备好，等待认证系统初始化');
        }
    }, 1000);
});