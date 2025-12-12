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
            // 验证token有效性 - 必须从后端验证，不允许降级
            const validationResponse = await gameAPI.validateToken();
            if (!validationResponse || !validationResponse.success) {
                throw new Error('Token验证失败，请重新登录');
            }

            await this.loadUserData();
            // 不再自动跳转，loadUserData中会处理跳转逻辑
            
            console.log('自动登录成功');

        } catch (error) {
            console.error('自动登录失败:', error);
            this.clearAuthData();
            this.showLoginPage();
            this.showToast('认证失败: ' + error.message, 'error');
        }
    }

    // 加载用户数据 - 必须从后端加载，不允许降级
    async loadUserData() {
        if (this.isLoading) return;

        this.isLoading = true;
        try {
            console.log('开始加载用户数据');

            // 获取当前用户信息 - 必须成功
            const userResponse = await gameAPI.getCurrentUser();
            if (!userResponse || !userResponse.success) {
                throw new Error(userResponse?.message || '获取用户信息失败，请检查网络连接');
            }

            this.currentUser = userResponse.data;
            if (!this.currentUser) {
                throw new Error('用户数据为空');
            }

            this.isAuthenticated = true;
            console.log('用户数据加载成功:', this.currentUser.username, '角色:', this.currentUser.role);

            // 根据用户角色跳转到相应的页面
            this.redirectToAppropriatePage();

            // 加载玩家资料 - 必须成功
            await this.loadPlayerProfile();

        } catch (error) {
            console.error('加载用户数据失败:', error);
            this.showToast('加载失败: ' + error.message, 'error');
            throw error;
        } finally {
            this.isLoading = false;
        }
    }

    // 加载玩家资料 - 必须从后端加载，不允许降级
    async loadPlayerProfile() {
        try {
            console.log('开始加载玩家资料');

            const response = await gameAPI.getCurrentPlayerProfile();
            if (!response || !response.success) {
                throw new Error(response?.message || '获取玩家资料失败，请检查后端服务');
            }

            this.player = response.data;
            if (!this.player) {
                throw new Error('玩家资料数据为空');
            }

            this.loginTime = Date.now();
            console.log('玩家资料加载成功:', this.player.nickname, '等级:', this.player.level);
            
            this.updatePlayerUI();

        } catch (error) {
            console.error('加载玩家资料失败:', error);
            this.showToast('加载玩家资料失败: ' + error.message, 'error');
            throw error;
        }
    }

    // 登录 - 必须从后端认证，不允许降级
    async login() {
        if (this.isLoading) return;

        const username = document.getElementById('loginUsername')?.value.trim();
        const password = document.getElementById('loginPassword')?.value;
        const userType = 'player'; // 普通用户登录页面固定为player类型

        if (!username || !password) {
            this.showToast('请输入用户名和密码', 'warning');
            return;
        }

        this.isLoading = true;
        this.showLoading(true);

        try {
            console.log('开始登录:', username, '用户类型:', userType);

            const response = await gameAPI.login(username, password, userType);

            if (!response || !response.success) {
                throw new Error(response?.message || '登录失败，请检查用户名和密码');
            }

            if (!response.data || !response.data.token) {
                throw new Error('登录响应数据异常，缺少token');
            }

            // 设置认证状态
            this.token = response.data.token;
            this.currentUser = response.data.user;
            this.player = response.data.player;
            this.isAuthenticated = true;
            this.loginTime = Date.now();

            // 保存token到localStorage和API实例
            if (window.api) {
                window.api.setToken(this.token);
            }

            console.log('登录成功，用户:', username, '玩家:', this.player?.nickname, '角色:', this.currentUser.role);
            this.showToast('登录成功', 'success');

            // 根据用户角色跳转到相应的页面
            setTimeout(() => {
                console.log('准备显示游戏页面');
                this.showGamePage();
                this.updatePlayerUI();
                
                // 通知现代UI系统登录成功
                if (window.simpleUI && typeof window.simpleUI.switchToGamePage === 'function') {
                    console.log('通知现代UI系统切换到游戏页面');
                    window.simpleUI.switchToGamePage();
                }
            }, 500);

        } catch (error) {
            console.error('登录错误:', error);
            this.showToast('登录失败: ' + error.message, 'error');
            this.clearAuthData();
        } finally {
            this.isLoading = false;
            this.showLoading(false);
        }
    }

    // 注册 - 必须从后端注册，不允许降级
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

        if (username.length < 3) {
            this.showToast('用户名长度至少3位', 'warning');
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
            console.log('开始注册:', username);

            const response = await gameAPI.register({
                username,
                nickname,
                email,
                password
            });

            if (!response || !response.success) {
                throw new Error(response?.message || '注册失败，请检查网络连接');
            }

            console.log('注册成功:', username);
            this.showToast('注册成功！正在自动登录...', 'success');
            
            // 注册成功后自动登录
            if (response.data && response.data.token) {
                this.token = response.data.token;
                this.currentUser = response.data.user;
                this.player = response.data.player;
                this.isAuthenticated = true;
                this.loginTime = Date.now();

                if (window.api) {
                    window.api.setToken(this.token);
                }

                setTimeout(() => {
                    this.showGamePage();
                    this.updatePlayerUI();
                }, 1000);
            } else {
                // 如果没有返回token，切换到登录表单
                this.showLoginForm();
            }

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
            gamePage.style.display = '';  // 清除内联样式
            gamePage.classList.add('active');
            window.scrollTo(0, 0);
        }

        // 添加游戏模式样式
        document.body.classList.add('game-mode');

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
        } else {
            window.location.href = '/login.html';
        }

        // 移除游戏模式样式
        document.body.classList.remove('game-mode');

        console.log('显示登录页面');
    }

    // 跳转到相应的页面（根据用户角色）
    redirectToAppropriatePage() {
        try {
            const currentPath = window.location.pathname || '';
            
            // 如果用户是管理员，跳转到管理员页面
            if (this.currentUser && this.currentUser.role === 'ADMIN') {
                if (!currentPath.endsWith('/admin.html') && !currentPath.endsWith('admin.html')) {
                    console.log('管理员用户，跳转到管理后台');
                    window.location.href = 'admin.html';
                    return;
                }
            }
            // 如果用户是普通用户，跳转到游戏主页
            else {
                if (!currentPath.endsWith('/index.html') && !currentPath.endsWith('index.html') && currentPath !== '/') {
                    console.log('普通用户，跳转到游戏主页');
                    window.location.href = 'index.html';
                    return;
                }
            }
        } catch (e) {
            console.error('页面跳转失败:', e);
            // 默认跳转到登录页面
            window.location.href = 'login.html';
        }
    }

    // 跳转到修炼页面（保持向后兼容）
    redirectToCultivate() {
        // 调用新的跳转方法
        this.redirectToAppropriatePage();
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
            window.location.href = '/login.html';
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
            'playerSpiritStones': this.player.spiritStones || 0,
            'attributePoints': this.player.attributePoints || 0,
            'playerHealth': this.player.health || 100,
            'playerMana': this.player.mana || 50,
            'playerAttack': this.player.attack || 10,
            'playerDefense': this.player.defense || 5,
            'playerSpeed': this.player.speed || 10
        };

        // 更新文本内容
        Object.entries(elements).forEach(([id, value]) => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = value;
            }
        });

        // 更新经验条
        const expProgress = document.getElementById('expProgress');
        const expText = document.getElementById('expText');
        if (expProgress && expText) {
            const currentExp = this.player.exp || 0;
            const expToNext = this.player.expToNext || 100;
            const expPercent = Math.min((currentExp / expToNext) * 100, 100);

            expProgress.style.width = expPercent + '%';
            expText.textContent = `${currentExp}/${expToNext}`;
        }

        console.log('玩家UI更新完成');
    }

    // 显示消息提示
    showToast(message, type = 'info', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `toast-bubble ${type}`;
        toast.textContent = message;
        const count = document.querySelectorAll('.toast-bubble').length;
        const bottom = 10 + count * 36;
        Object.assign(toast.style, {
            position: 'fixed',
            bottom: `${bottom}px`,
            right: '16px',
            background: this.getToastColor(type),
            color: '#fff',
            padding: '6px 10px',
            borderRadius: '9999px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
            zIndex: '10001',
            maxWidth: '220px',
            fontSize: '12px',
            lineHeight: '1.2',
            opacity: '0',
            transform: 'translateY(8px)',
            transition: 'all 0.25s ease'
        });
        document.body.appendChild(toast);
        requestAnimationFrame(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateY(0)';
        });
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(8px)';
            setTimeout(() => {
                if (toast.parentElement) {
                    toast.parentElement.removeChild(toast);
                }
            }, 250);
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

    // 加载游戏数据 - 必须从后端加载，不允许降级
    async loadGameData() {
        if (!this.isAuthenticated) {
            console.warn('用户未认证，无法加载游戏数据');
            this.showToast('请先登录', 'warning');
            this.showLoginPage();
            return;
        }

        this.showLoading(true);

        try {
            console.log('开始加载游戏数据');
            
            // 加载玩家资料数据 - 必须成功
            const profileResponse = await gameAPI.getCurrentPlayerProfile();
            if (!profileResponse || !profileResponse.success) {
                throw new Error(profileResponse?.message || '加载玩家资料失败');
            }

            this.player = profileResponse.data;
            if (!this.player) {
                throw new Error('玩家资料数据为空');
            }

            console.log('玩家资料加载成功:', this.player.nickname);
            this.updatePlayerUI();
            
            // 加载未读邮件数量
            this.loadUnreadMailCount();
            
            this.showToast('游戏数据加载完成', 'success');

        } catch (error) {
            console.error('加载游戏数据失败:', error);
            this.showToast('加载游戏数据失败: ' + error.message, 'error');
            // 加载失败时清除认证状态并返回登录页
            this.clearAuthData();
            this.showLoginPage();
        } finally {
            this.showLoading(false);
        }
    }

    // 加载未读邮件数量
    async loadUnreadMailCount() {
        try {
            const response = await fetch('/api/mail/unread-count', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data > 0) {
                    const badge = document.getElementById('mailBadge');
                    if (badge) {
                        badge.textContent = result.data;
                        badge.style.display = 'inline';
                    }
                } else if (result.success && result.data === 0) {
                    const badge = document.getElementById('mailBadge');
                    if (badge) {
                        badge.style.display = 'none';
                    }
                }
            }
        } catch (error) {
            console.error('加载未读邮件数量失败:', error);
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
