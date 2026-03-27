// 认证管理 - 修复认证流程
class AuthManager {
    constructor() {
        console.log('AuthManager构造函数开始');
        this.currentUser = null;
        this.player = null;
        this.isAuthenticated = false;
        this.isLoading = false;
        this.loginTime = null;
        this.lastDataUpdate = null;
        this.token = localStorage.getItem('authToken');
        console.log('token:', this.token);
        
        // 设置token到API实例
        if (this.token && window.api) {
            window.api.setToken(this.token);
        }
        
        this.init();
        console.log('AuthManager构造函数结束');
    }

    // 初始化认证状态
    async init() {
        this.bindEvents();
        // 强制设置初始表单显示状态
        this.switchForm('login');
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

        // 安全获取当前页面路径
        const currentPage = window.location.pathname || '';
        const isLoginPage = currentPage.includes('login.html') || currentPage === '/' || currentPage === '';
        const isGamePage = currentPage.includes('game.html');

        console.log('当前页面:', currentPage, 'isLoginPage:', isLoginPage, 'isGamePage:', isGamePage);

        if (!this.token) {
            // 没有token
            if (isGamePage) {
                window.location.href = 'login.html';
            }
            return;
        }

        try {
            // 验证token有效性
            console.log('验证token，当前token:', this.token);
            const validationResponse = await gameAPI.validateToken();
            console.log('validateToken响应:', validationResponse);
            if (!validationResponse || !validationResponse.success) {
                throw new Error('Token验证失败');
            }

            this.isAuthenticated = true;
            console.log('准备加载用户数据');
            await this.loadUserData();
            console.log('用户数据加载完成');
            
            // 如果在登录页面，有token就跳转到游戏页面
            if (isLoginPage) {
                console.log('登录页面有token，跳转到游戏页面');
                window.location.href = 'game.html';
                return;
            }
            
            console.log('自动登录成功');

        } catch (error) {
            console.error('自动登录失败:', error);
            this.clearAuthData();
            if (isGamePage) {
                window.location.href = 'login.html';
            }
            this.showToast('认证失败: ' + error.message, 'error');
        }
    }

    // 加载用户数据 - 必须从后端加载，不允许降级
    async loadUserData() {
        if (this.isLoading) return;

        this.isLoading = true;
        try {
            console.log('开始加载用户数据');
            console.log('api.token:', api.token);
            console.log('localStorage token:', localStorage.getItem('authToken'));

            // 获取当前用户信息 - 必须成功
            const userResponse = await gameAPI.getCurrentUser();
            console.log('getCurrentUser响应:', userResponse);
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
            console.log('Token:', this.token);
            console.log('保存到localStorage...');
            localStorage.setItem('authToken', this.token);
            console.log('localStorage中的token:', localStorage.getItem('authToken'));
            this.showToast('登录成功', 'success');

            // 根据用户角色跳转到相应的页面
            console.log('准备跳转到游戏页面');
            this.showGamePage();
            this.updatePlayerUI();

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
                
                localStorage.setItem('authToken', this.token);

                setTimeout(() => {
                    this.showGamePage();
                    if (this.player) {
                        this.updatePlayerUI();
                    }
                }, 1000);
            } else {
                // 如果没有返回 token，切换到登录表单
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
        // 直接跳转到游戏页面
        console.log('跳转到游戏页面');
        window.location.href = 'game.html';
    }

    // 显示登录页面
    showLoginPage() {
        // 直接跳转到登录页面
        const currentPage = window.location.pathname || '';
        if (!currentPage.includes('login.html')) {
            window.location.href = 'login.html';
        }
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
                if (!currentPath.endsWith('/game.html') && !currentPath.endsWith('game.html')) {
                    console.log('普通用户，跳转到游戏页面');
                    window.location.href = 'game.html';
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
        
        // 处理使用data-tab属性的标签（index.html中的标签）
        const loginTabData = document.querySelector('.tab-btn[data-tab="login"]');
        const registerTabData = document.querySelector('.tab-btn[data-tab="register"]');

        if (formType === 'login') {
            if (loginForm) {
                loginForm.style.display = 'block';
                loginForm.classList.add('active');
            }
            if (registerForm) {
                registerForm.style.display = 'none';
                registerForm.classList.remove('active');
            }
            if (loginTab) loginTab.classList.add('active');
            if (registerTab) registerTab.classList.remove('active');
            if (loginTabData) loginTabData.classList.add('active');
            if (registerTabData) registerTabData.classList.remove('active');
        } else {
            if (loginForm) {
                loginForm.style.display = 'none';
                loginForm.classList.remove('active');
            }
            if (registerForm) {
                registerForm.style.display = 'block';
                registerForm.classList.add('active');
            }
            if (loginTab) loginTab.classList.remove('active');
            if (registerTab) registerTab.classList.add('active');
            if (loginTabData) loginTabData.classList.remove('active');
            if (registerTabData) registerTabData.classList.add('active');
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

// 立即初始化 AuthManager
var authManager = new AuthManager();
window.authManagerInstance = authManager;
window.authManager = authManager;
console.log('AuthManager立即初始化完成');

// 全局函数
window.login = function(event) {
    if (event) event.preventDefault();
    if (authManager) authManager.login();
};

window.register = function(event) {
    if (event) event.preventDefault();
    if (authManager) authManager.register();
};

window.logout = function() {
    if (authManager) authManager.logout();
};

window.showLogin = function() {
    if (authManager) authManager.showLoginForm();
};

window.showRegister = function() {
    if (authManager) authManager.showRegisterForm();
};

window.showModule = function(moduleName) {
    if (window.moduleManager) window.moduleManager.showModule(moduleName);
};
