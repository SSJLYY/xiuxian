// 管理员认证管理器 - 独立于游戏认证系统
class AdminAuthManager {
    constructor() {
        this.currentAdmin = null;
        this.isAuthenticated = false;
        this.isLoading = false;
        this.loginTime = null;
        this.token = localStorage.getItem('adminToken'); // 使用独立的adminToken
        
        // 设置token到管理员API实例
        if (this.token && window.adminApi) {
            window.adminApi.setToken(this.token);
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
        // 管理员登录表单
        const adminLoginForm = document.getElementById('adminLoginForm');
        if (adminLoginForm) {
            adminLoginForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.login();
            });
        }
    }

    // 检查认证状态
    async checkAuthStatus() {
        console.log('检查管理员认证状态，token存在:', !!this.token);

        if (!this.token) {
            this.showAdminLoginPage();
            return;
        }

        try {
            // 验证管理员token有效性
            const validationResponse = await adminAPI.validateToken();
            if (!validationResponse || !validationResponse.success) {
                throw new Error('管理员Token验证失败，请重新登录');
            }

            // 设置认证状态
            this.currentAdmin = validationResponse.data.admin;
            this.isAuthenticated = true;
            
            console.log('管理员自动登录成功:', this.currentAdmin.username);
            
            // 如果当前在登录页面，跳转到管理后台
            if (window.location.pathname.endsWith('adminLogin.html')) {
                window.location.href = 'admin.html';
            }

        } catch (error) {
            console.error('管理员自动登录失败:', error);
            this.clearAuthData();
            this.showAdminLoginPage();
            this.showToast('管理员认证失败: ' + error.message, 'error');
        }
    }

    // 管理员登录
    async login() {
        if (this.isLoading) return;

        const username = document.getElementById('adminUsername')?.value.trim();
        const password = document.getElementById('adminPassword')?.value;

        if (!username || !password) {
            this.showToast('请输入管理员用户名和密码', 'warning');
            return;
        }

        this.isLoading = true;
        this.showLoading(true);

        try {
            console.log('开始管理员登录:', username);

            const response = await adminAPI.login(username, password);

            if (!response || !response.success) {
                throw new Error(response?.message || '管理员登录失败，请检查用户名和密码');
            }

            if (!response.data || !response.data.token) {
                throw new Error('管理员登录响应数据异常，缺少token');
            }

            // 设置认证状态
            this.token = response.data.token;
            this.currentAdmin = response.data.admin;
            this.isAuthenticated = true;
            this.loginTime = Date.now();

            // 保存token到localStorage和API实例
            if (window.adminApi) {
                window.adminApi.setToken(this.token);
            }

            console.log('管理员登录成功:', username, '角色:', this.currentAdmin.role);
            this.showToast('管理员登录成功', 'success');

            // 跳转到管理后台
            setTimeout(() => {
                window.location.href = 'admin.html';
            }, 500);

        } catch (error) {
            console.error('管理员登录错误:', error);
            this.showToast('管理员登录失败: ' + error.message, 'error');
            this.clearAuthData();
        } finally {
            this.isLoading = false;
            this.showLoading(false);
        }
    }

    // 显示管理员登录页面
    showAdminLoginPage() {
        // 如果不在管理员登录页面，跳转过去
        if (!window.location.pathname.endsWith('adminLogin.html')) {
            window.location.href = 'adminLogin.html';
        }
        console.log('显示管理员登录页面');
    }

    // 清除认证数据
    clearAuthData() {
        this.currentAdmin = null;
        this.isAuthenticated = false;
        this.token = null;
        this.loginTime = null;

        if (window.adminApi) {
            window.adminApi.clearToken();
        }

        localStorage.removeItem('adminToken');
    }

    // 管理员登出
    async logout() {
        try {
            await adminAPI.logout();
        } catch (error) {
            console.warn('管理员登出请求失败:', error);
        } finally {
            this.clearAuthData();
            window.location.href = 'adminLogin.html';
            this.showToast('管理员已成功登出', 'info');
        }
    }

    // 显示消息提示
    showToast(message, type = 'info', duration = 3000) {
        // 移除现有的toast
        const existingToasts = document.querySelectorAll('.admin-toast');
        existingToasts.forEach(toast => toast.remove());

        const toast = document.createElement('div');
        toast.className = `admin-toast admin-toast-${type}`;
        toast.textContent = message;
        
        Object.assign(toast.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            background: this.getToastColor(type),
            color: '#fff',
            padding: '12px 20px',
            borderRadius: '6px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
            zIndex: '10001',
            maxWidth: '300px',
            fontSize: '14px',
            lineHeight: '1.4',
            opacity: '0',
            transform: 'translateY(-10px)',
            transition: 'all 0.3s ease'
        });
        
        document.body.appendChild(toast);
        
        // 显示动画
        requestAnimationFrame(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateY(0)';
        });
        
        // 自动隐藏
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(-10px)';
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
        let loading = document.getElementById('adminLoading');
        
        if (show && !loading) {
            loading = document.createElement('div');
            loading.id = 'adminLoading';
            loading.innerHTML = `
                <div style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; 
                           background: rgba(0,0,0,0.5); display: flex; align-items: center; 
                           justify-content: center; z-index: 9999;">
                    <div style="background: white; padding: 20px; border-radius: 8px; 
                               display: flex; align-items: center; gap: 12px;">
                        <div style="width: 20px; height: 20px; border: 2px solid #8B4513; 
                                   border-top: 2px solid transparent; border-radius: 50%; 
                                   animation: spin 1s linear infinite;"></div>
                        <span style="color: #8B4513; font-weight: 500;">管理员登录中...</span>
                    </div>
                </div>
                <style>
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                </style>
            `;
            document.body.appendChild(loading);
        } else if (!show && loading) {
            loading.remove();
        }
    }

    // 检查是否已认证
    isAdminAuthenticated() {
        return this.isAuthenticated && this.currentAdmin && this.token;
    }

    // 获取当前管理员信息
    getCurrentAdmin() {
        return this.currentAdmin;
    }
}

// 创建管理员认证管理器实例
const adminAuthManager = new AdminAuthManager();

// 全局函数
window.adminLogin = (event) => {
    if (event) event.preventDefault();
    adminAuthManager.login();
};

window.adminLogout = () => adminAuthManager.logout();

// 导出到全局作用域
window.adminAuthManager = adminAuthManager;