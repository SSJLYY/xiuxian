// 认证管理 - 修复认证流程
class AuthManager {
    constructor() {
        this.currentUser = null;
        this.player = null;
        this.isAuthenticated = false;
        this.isLoading = false;
        this.loginTime = null;
        this.lastDataUpdate = null;
        this.token = localStorage.getItem('authToken');
        
        // 设置token到API实例
        if (this.token && window.api) {
            window.api.setToken(this.token);
        }
        
        this.init();
    }

    // 初始化认证状态
    async init() {
        this.bindEvents();
        await this.checkAuthStatus();
    }

    // 绑定事件
    bindEvents() {
        // 登录表单
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.login();
            });
        }

        // 注册表单
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.register();
            });
        }
    }

    // 检查认证状态
    async checkAuthStatus() {
        console.log('检查认证状态，token存在:', !!this.token);

        if (!this.token) {
            this.showLoginPage();
            return;
        }

        try {
            // 验证token有效性
            const validationResponse = await gameAPI.validateToken();
            if (!validationResponse.success) {
                throw new Error('Token无效');
            }

            await this.loadUserData();
            this.showGamePage();
            
            // 初始化游戏管理器
            if (window.initGameManager) {
                await window.initGameManager();
            }
            
            console.log('自动登录成功');

        } catch (error) {
            console.error('自动登录失败:', error);
            this.clearAuthData();
            this.showLoginPage();
        }
    }

    // 加载用户数据
    async loadUserData() {
        if (this.isLoading) return;

        this.isLoading = true;
        try {
            console.log('开始加载用户数据');

            // 获取当前用户信息
            const userResponse = await gameAPI.getCurrentUser();
            if (!userResponse.success) {
                throw new Error(userResponse.message || '获取用户信息失败');
            }

            this.currentUser = userResponse.data;
            this.isAuthenticated = true;

            console.log('用户数据加载成功:', this.currentUser.username);

            // 加载玩家资料
            await this.loadPlayerProfile();

        } catch (error) {
            console.error('加载用户数据失败:', error);
            throw error;
        } finally {
            this.isLoading = false;
        }
    }

    // 加载玩家资料
    async loadPlayerProfile() {
        try {
            console.log('开始加载玩家资料');

            const response = await gameAPI.getCurrentPlayerProfile();
            if (!response.success) {
                throw new Error(response.message || '获取玩家资料失败');
            }

            this.player = response.data;
            this.loginTime = Date.now();

            console.log('玩家资料加载成功:', this.player.nickname);
            this.updatePlayerUI();

        } catch (error) {
            console.error('加载玩家资料失败:', error);
            throw error;
        }
    }

    // 登录 - 修复认证流程
    async login() {
        if (this.isLoading) return;

        const username = document.getElementById('loginUsername')?.value.trim();
        const password = document.getElementById('loginPassword')?.value;

        if (!username || !password) {
            this.showToast('请输入用户名和密码', 'warning');
            return;
        }

        this.isLoading = true;
        this.showLoading(true);

        try {
            console.log('开始登录:', username);

            const response = await gameAPI.login(username, password);

            if (!response.success) {
                throw new Error(response.message || '登录失败');
            }

            if (!response.data) {
                throw new Error('登录响应数据为空');
            }

            // 设置认证状态
            this.isAuthenticated = true;
            this.currentUser = response.data.user;
            this.player = response.data.player;
            this.loginTime = Date.now();
            this.token = response.data.token;

            // 保存token到localStorage
            if (window.api) {
                window.api.setToken(this.token);
            }

            this.showToast('登录成功', 'success');
            console.log('登录成功，用户:', username);

            // 立即显示游戏页面
            this.showGamePage();

            // 初始化游戏管理器
            if (window.initGameManager) {
                await window.initGameManager();
            }

            // 加载游戏数据
            await this.loadGameData();

        } catch (error) {
            console.error('登录错误:', error);
            this.showToast('登录失败: ' + error.message, 'error');
            this.clearAuthData();
        } finally {
            this.isLoading = false;
            this.showLoading(false);
        }
    }

    // 注册
    async register() {
        if (this.isLoading) return;

        const username = document.getElementById('registerUsername')?.value.trim();
        const nickname = document.getElementById('registerNickname')?.value.trim();
        const email = document.getElementById('registerEmail')?.value.trim();
        const password = document.getElementById('registerPassword')?.value;
        const confirmPassword = document.getElementById('registerConfirmPassword')?.value;

        // 验证输入
        if (!username || !nickname || !email || !password || !confirmPassword) {
            this.showToast('请填写所有字段', 'warning');
            return;
        }

        if (password.length < 6) {
            this.showToast('密码长度至少6位', 'warning');
            return;
        }

        if (password !== confirmPassword) {
            this.showToast('两次输入的密码不一致', 'warning');
            return;
        }

        if (!this.validateEmail(email)) {
            this.showToast('请输入有效的邮箱地址', 'warning');
            return;
        }

        this.isLoading = true;
        this.showLoading(true);

        try {
            const response = await gameAPI.register({
                username,
                nickname,
                email,
                password
            });

            if (!response.success) {
                throw new Error(response.message || '注册失败');
            }

            this.showToast('注册成功，请登录', 'success');
            this.showLoginForm();

        } catch (error) {
            console.error('注册错误:', error);
            this.showToast('注册失败: ' + error.message, 'error');
        } finally {
            this.isLoading = false;
            this.showLoading(false);
        }
    }

    // 邮箱验证
    validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    // 显示游戏页面
    showGamePage() {
        const loginPage = document.getElementById('loginPage');
        const gamePage = document.getElementById('gamePage');

        if (loginPage) {
            loginPage.style.display = 'none';
            loginPage.classList.remove('active');
        }
        if (gamePage) {
            gamePage.style.display = 'flex';
            gamePage.classList.add('active');
            window.scrollTo(0, 0);
        }

        console.log('显示游戏页面');
    }

    // 显示登录页面
    showLoginPage() {
        const gamePage = document.getElementById('gamePage');
        const loginPage = document.getElementById('loginPage');

        if (gamePage) {
            gamePage.style.display = 'none';
            gamePage.classList.remove('active');
        }
        if (loginPage) {
            loginPage.style.display = 'flex';
            loginPage.classList.add('active');
            window.scrollTo(0, 0);
        }

        console.log('显示登录页面');
    }

    // 切换到登录表单
    showLoginForm() {
        this.switchForm('login');
    }

    // 切换到注册表单
    showRegisterForm() {
        this.switchForm('register');
    }

    // 切换表单
    switchForm(formType) {
        const loginForm = document.getElementById('loginForm');
        const registerForm = document.getElementById('registerForm');
        const loginTab = document.querySelector('.tab-btn[onclick*="showLogin"]');
        const registerTab = document.querySelector('.tab-btn[onclick*="showRegister"]');

        if (formType === 'login') {
            if (loginForm) loginForm.style.display = 'block';
            if (registerForm) registerForm.style.display = 'none';
            if (loginTab) loginTab.classList.add('active');
            if (registerTab) registerTab.classList.remove('active');
        } else {
            if (loginForm) loginForm.style.display = 'none';
            if (registerForm) registerForm.style.display = 'block';
            if (loginTab) loginTab.classList.remove('active');
            if (registerTab) registerTab.classList.add('active');
        }
    }

    // 清除认证数据
    clearAuthData() {
        this.currentUser = null;
        this.player = null;
        this.isAuthenticated = false;
        this.token = null;
        this.loginTime = null;

        if (window.api) {
            window.api.clearToken();
        }

        localStorage.removeItem('authToken');
    }

    // 登出
    async logout() {
        try {
            await gameAPI.logout();
        } catch (error) {
            console.warn('登出请求失败:', error);
        } finally {
            this.clearAuthData();
            this.showLoginPage();
            this.showToast('已成功登出', 'info');
        }
    }

    // 更新玩家UI
    updatePlayerUI() {
        if (!this.player) {
            console.warn('玩家数据为空，无法更新UI');
            return;
        }

        console.log('更新玩家UI:', this.player);

        const elements = {
            'playerName': this.player.nickname,
            'playerLevel': this.player.level,
            'playerRealm': this.player.realm,
            'playerExp': this.player.exp || 0,
            'expToNext': this.player.expToNext || 100,
            'playerSpiritStones': this.player.spiritStones || 0
        };

        // 更新文本内容
        Object.entries(elements).forEach(([id, value]) => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = value;
            }
        });

        console.log('玩家UI更新完成');
    }

    // 显示消息提示
    showToast(message, type = 'info', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;

        Object.assign(toast.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            background: this.getToastColor(type),
            color: 'white',
            padding: '15px 20px',
            borderRadius: '8px',
            boxShadow: '0 5px 15px rgba(0,0,0,0.2)',
            zIndex: '1001',
            animation: 'slideInRight 0.3s ease',
            maxWidth: '300px',
            wordWrap: 'break-word'
        });

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => {
                if (toast.parentElement) {
                    toast.parentElement.removeChild(toast);
                }
            }, 300);
        }, duration);
    }

    // 获取toast颜色
    getToastColor(type) {
        const colors = {
            info: '#3498db',
            success: '#2ecc71',
            warning: '#f39c12',
            error: '#e74c3c'
        };
        return colors[type] || colors.info;
    }

    // 显示加载指示器
    showLoading(show = true) {
        const loading = document.getElementById('loading');
        if (loading) {
            loading.style.display = show ? 'flex' : 'none';
        }
    }

    // 加载游戏数据
    async loadGameData() {
        if (!this.isAuthenticated) {
            console.warn('用户未认证，无法加载游戏数据');
            this.showToast('请先登录', 'warning');
            return;
        }

        this.showLoading(true);

        try {
            console.log('开始加载游戏数据');
            
            // 加载玩家资料数据
            const profileResponse = await gameAPI.getCurrentPlayerProfile();
            if (profileResponse && profileResponse.success) {
                console.log('玩家资料加载成功');
                this.player = profileResponse.data;
                this.updatePlayerUI();
            }

            this.showToast('游戏数据加载完成', 'success');
            console.log('游戏数据加载完成');

        } catch (error) {
            console.error('加载游戏数据失败:', error);
            this.showToast('加载游戏数据失败: ' + error.message, 'error');
        } finally {
            this.showLoading(false);
        }
    }
}

// 创建认证管理器实例
const authManager = new AuthManager();

// 全局函数
window.login = (event) => {
    if (event) event.preventDefault();
    authManager.login();
};

window.register = (event) => {
    if (event) event.preventDefault();
    authManager.register();
};

window.logout = () => authManager.logout();
window.showLogin = () => authManager.showLoginForm();
window.showRegister = () => authManager.showRegisterForm();

// 导出到全局作用域
window.authManager = authManager;