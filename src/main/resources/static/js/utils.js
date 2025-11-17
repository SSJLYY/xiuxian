// 认证管理
class AuthManager {
    constructor() {
        this.currentUser = null;
        this.player = null;
        this.isAuthenticated = false;
        this.isLoading = false;
        this.dataLoaders = new Map();
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
        const token = localStorage.getItem('authToken');
        if (!token) {
            this.showLoginPage();
            return;
        }

        try {
            // 设置token
            if (window.api) {
                window.api.setToken(token);
            }

            await this.loadUserData();
            this.showGamePage();
        } catch (error) {
            console.error('自动登录失败:', error);
            this.logout();
        }
    }

    // 加载用户数据
    async loadUserData() {
        if (this.isLoading) return;

        this.isLoading = true;
        try {
            // 先检查API是否可用
            if (!window.gameAPI) {
                throw new Error('API未初始化');
            }

            const response = await gameAPI.getCurrentUser();
            if (response && response.success && response.data) {
                this.currentUser = response.data;
                this.isAuthenticated = true;

                // 加载玩家数据
                await this.loadPlayerProfile();
            } else {
                throw new Error(response?.message || '获取用户信息失败');
            }
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
            const response = await gameAPI.getCurrentPlayerProfile();
            if (response && response.success && response.data) {
                this.player = response.data;
                this.updatePlayerUI();
                return true;
            } else {
                throw new Error(response?.message || '获取玩家资料失败');
            }
        } catch (error) {
            console.error('加载玩家资料失败:', error);
            this.showToast('加载玩家资料失败: ' + error.message, 'error');
            throw error;
        }
    }

    

    // 登录
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
            // 检查API是否可用
            if (!window.gameAPI) {
                throw new Error('系统初始化中，请稍后重试');
            }

            const response = await gameAPI.login(username, password);

            if (response && response.success) {
                this.showToast('登录成功', 'success');

                // 设置认证状态
                this.isAuthenticated = true;
                this.currentUser = response.data?.user || { username: username };

                // 加载玩家数据
                await this.loadPlayerProfile();

                this.showGamePage();
                await this.loadGameData();
            } else {
                throw new Error(response?.message || '登录失败');
            }
        } catch (error) {
            console.error('登录错误:', error);
            this.showToast('登录失败: ' + error.message, 'error');
            this.showLoginPage();
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

            if (response && response.success) {
                this.showToast('注册成功，请登录', 'success');
                this.showLoginForm();
            } else {
                throw new Error(response?.message || '注册失败');
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

        if (loginPage) loginPage.style.display = 'none';
        if (gamePage) {
            gamePage.style.display = 'block';
            gamePage.classList.add('active');
        }
    }

    // 显示登录页面
    showLoginPage() {
        const gamePage = document.getElementById('gamePage');
        const loginPage = document.getElementById('loginPage');

        if (gamePage) gamePage.style.display = 'none';
        if (loginPage) {
            loginPage.style.display = 'block';
            loginPage.classList.add('active');
        }
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

    // 加载游戏数据 - 修复认证检查逻辑
    async loadGameData() {
        if (!this.isAuthenticated) {
            this.showToast('请先登录', 'warning');
            return;
        }

        this.showLoading(true);

        try {
            await this.loadPlayerProfile();
            await Promise.all([
                this.loadSkills(),
                this.loadInventory(),
                this.loadQuests()
            ]);
            this.updatePlayerUI();
            this.showToast('游戏数据加载完成', 'success');
        } catch (error) {
            this.showToast('加载游戏数据失败: ' + error.message, 'error');
        } finally {
            this.showLoading(false);
        }
    }

    // 加载技能数据
    async loadSkills() {
        try {
            const response = await gameAPI.getSkills();
            if (response && response.success) {
                this.renderSkills(response.data);
                this.dataLoaders.set('skills', Date.now());
            } else {
                throw new Error(response?.message || '获取技能失败');
            }
        } catch (error) {
            console.error('加载技能失败:', error);
            throw new Error('技能API不可用: ' + error.message);
        }
    }

    // 加载装备数据
    async loadEquipment() {
        try {
            const response = await gameAPI.getEquipment();
            if (response && response.success) {
                this.renderEquipment(response.data);
                this.dataLoaders.set('equipment', Date.now());
            } else {
                throw new Error(response?.message || '获取装备失败');
            }
        } catch (error) {
            console.error('加载装备失败:', error);
            throw new Error('装备API不可用: ' + error.message);
        }
    }

    // 加载背包数据
    async loadInventory() {
        try {
            const response = await gameAPI.getInventory();
            if (response && response.success) {
                this.renderInventory(response.data);
                this.dataLoaders.set('inventory', Date.now());
            } else {
                throw new Error(response?.message || '获取背包失败');
            }
        } catch (error) {
            console.error('加载背包失败:', error);
            throw new Error('背包API不可用: ' + error.message);
        }
    }

    // 加载任务数据
    async loadQuests() {
        try {
            const response = await gameAPI.getQuests();
            if (response && response.success) {
                this.renderQuests(response.data);
                this.dataLoaders.set('quests', Date.now());
            } else {
                throw new Error(response?.message || '获取任务失败');
            }
        } catch (error) {
            console.error('加载任务失败:', error);
            throw new Error('任务API不可用: ' + error.message);
        }
    }

    // 渲染技能列表（Tailwind风格）
    renderSkills(skills) {
        const skillsList = document.getElementById('skillsList');
        if (!skillsList || !skills) return;

        skillsList.innerHTML = skills.map(s => `
            <div class="bg-white/90 rounded-lg border border-gray-200 p-4 shadow hover:shadow-md transition transform hover:-translate-y-0.5" data-skill-id="${s.id}">
                <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                        <i class="fa fa-magic text-indigo-500"></i>
                        <span class="font-semibold text-gray-800">${s.skill?.name || s.name || ''}</span>
                    </div>
                    <span class="text-xs px-2 py-1 rounded bg-indigo-50 text-indigo-600">Lv.${s.level}</span>
                </div>
                <p class="text-sm text-gray-600 mb-2">${s.skill?.description || s.description || ''}</p>
                <div class="flex items-center gap-4 text-sm text-gray-500">
                    <span><i class="fa fa-clock-o mr-1"></i>${s.cooldown || 0}s</span>
                    <span><i class="fa fa-fire mr-1"></i>${s.manaCost || s.cost || 0}</span>
                </div>
                <button class="mt-3 px-3 py-1 text-sm rounded bg-indigo-500 text-white hover:bg-indigo-600" onclick="useSkill(${s.id})">使用</button>
            </div>
        `).join('');
    }

    // 渲染装备
    renderEquipment(equipment) {
        const equipmentGrid = document.getElementById('equipmentGrid');
        if (!equipmentGrid || !equipment) return;

        equipmentGrid.innerHTML = equipment.map(item => `
            <div class="equipment-item">
                <div class="equipment-name">${item.name}</div>
                <div class="equipment-type">${item.type}</div>
                <div class="equipment-stats">
                    ${item.attackBonus > 0 ? `<span>攻击+${item.attackBonus}</span>` : ''}
                    ${item.defenseBonus > 0 ? `<span>防御+${item.defenseBonus}</span>` : ''}
                </div>
                <button class="btn btn-sm" onclick="equipItem(${item.id})">装备</button>
            </div>
        `).join('');
    }

    // 渲染背包（Tailwind风格）
    renderInventory(items) {
        const inventoryGrid = document.getElementById('inventoryGrid');
        if (!inventoryGrid || !items) return;

        inventoryGrid.innerHTML = items.map(item => `
            <div class="bg-white/90 rounded-lg border border-gray-200 p-4 shadow hover:shadow-md transition">
                <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                        <i class="fa fa-archive text-amber-600"></i>
                        <span class="font-semibold text-gray-800">${item.itemName || item.name}</span>
                    </div>
                    <span class="text-xs px-2 py-1 rounded bg-amber-50 text-amber-700">x${item.quantity}</span>
                </div>
                <p class="text-sm text-gray-600 mb-2">${item.itemDescription || item.description || ''}</p>
                <div class="text-xs text-gray-500">${item.itemType || item.type}</div>
                <button class="mt-3 px-3 py-1 text-sm rounded bg-amber-600 text-white hover:bg-amber-700" onclick="useItem(${item.id})">使用</button>
            </div>
        `).join('');
    }

    // 渲染任务列表（Tailwind风格）
    renderQuests(quests) {
        const questsList = document.getElementById('questsList');
        if (!questsList || !quests) return;

        questsList.innerHTML = quests.map(pq => {
            const q = pq.quest;
            const progress = pq.currentProgress || 0;
            const required = q.requiredAmount || 1;
            const progressPercent = Math.min((progress / required) * 100, 100);
            const isCompleted = pq.completed;
            const isClaimed = pq.rewardClaimed;

            const statusText = isClaimed
                ? '<span class="text-xs px-2 py-1 rounded bg-green-50 text-green-700">已领取</span>'
                : isCompleted
                    ? '<span class="text-xs px-2 py-1 rounded bg-green-50 text-green-700">已完成</span>'
                    : `<span class=\"text-xs px-2 py-1 rounded bg-blue-50 text-blue-700\">进行中 ${progress}/${required}</span>`;
            const button = isClaimed || !isCompleted
                ? `<button class=\"mt-3 px-3 py-1 text-sm rounded bg-gray-300 text-gray-600 cursor-not-allowed\" disabled>进行中</button>`
                : `<button class=\"mt-3 px-3 py-1 text-sm rounded bg-green-600 text-white hover:bg-green-700\" onclick=\"claimQuest(${pq.id})\">领取奖励</button>`;

            return `
                <div class="bg-white/90 rounded-lg border border-gray-200 p-4 shadow hover:shadow-md transition">
                    <div class="flex items-center justify-between mb-2">
                        <div class="flex items-center gap-2">
                            <i class="fa fa-tasks text-blue-600"></i>
                            <span class="font-semibold text-gray-800">${q.title}</span>
                        </div>
                        ${statusText}
                    </div>
                    <p class="text-sm text-gray-600 mb-2">${q.description}</p>
                    <div class="flex items-center gap-3">
                        <div class="w-full h-2 bg-gray-200 rounded overflow-hidden">
                            <div class="h-2 bg-gradient-to-r from-amber-700 to-red-500" style="width: ${progressPercent}%"></div>
                        </div>
                        <span class="text-xs text-gray-500">${progress}/${required}</span>
                    </div>
                    <div class="mt-2 text-sm text-gray-700">奖励: ${q.rewardExp}经验 + ${q.rewardSpiritStones}灵石</div>
                    ${button}
                </div>
            `;
        }).join('');
    }

    // 更新玩家UI
    updatePlayerUI() {
        if (!this.player) {
            console.warn('玩家数据为空，无法更新UI');
            return;
        }

        const elements = {
            'playerName': this.player.nickname || this.player.username,
            'playerLevel': this.player.level,
            'playerRealm': this.player.realm,
            'playerExp': this.player.exp || this.player.currentExp || 0,
            'expToNext': this.player.expToNext || 100,
            'playerSpiritStones': this.player.spiritStones || 0,
            'playerHealth': this.player.health || 100,
            'playerMana': this.player.mana || 50,
            'playerAttack': this.player.attack || 10,
            'playerDefense': this.player.defense || 5
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
            const currentExp = this.player.exp || this.player.currentExp || 0;
            const expToNext = this.player.expToNext || 100;
            const expPercent = Math.min((currentExp / expToNext) * 100, 100);

            expProgress.style.width = expPercent + '%';
            expText.textContent = `${currentExp}/${expToNext}`;
        }
    }

    // 显示消息提示
    showToast(message, type = 'info', duration = 3000) {
        const toast = document.createElement('div');
        const count = document.querySelectorAll('.toast-bubble').length;
        const bottom = 10 + count * 36;
        toast.className = `toast-bubble ${type}`;
        toast.style.cssText = `
            position: fixed;
            bottom: ${bottom}px;
            right: 16px;
            background: ${this.getToastColor(type)};
            color: #fff;
            padding: 6px 10px;
            border-radius: 9999px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.12);
            z-index: 10001;
            max-width: 220px;
            font-size: 12px;
            line-height: 1.2;
            opacity: 0;
            transform: translateY(8px);
            transition: all 0.25s ease;
        `;
        toast.textContent = message;

        document.body.appendChild(toast);
        requestAnimationFrame(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateY(0)';
        });
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(8px)';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
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
        // 创建简单的加载指示器
        let loading = document.getElementById('loading');

        if (show) {
            if (!loading) {
                loading = document.createElement('div');
                loading.id = 'loading';
                loading.innerHTML = '加载中...';
                loading.style.cssText = `
                    position: fixed;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    background: rgba(0,0,0,0.7);
                    color: white;
                    padding: 20px;
                    border-radius: 4px;
                    z-index: 10000;
                `;
                document.body.appendChild(loading);
            }
        } else {
            if (loading && loading.parentNode) {
                loading.parentNode.removeChild(loading);
            }
        }
    }

    

    // 保存游戏状态
    saveGameState() {
        const gameState = {
            timestamp: Date.now(),
            player: this.player,
            isAuthenticated: this.isAuthenticated
        };

        try {
            localStorage.setItem('xiuxian_game_state', JSON.stringify(gameState));
        } catch (error) {
            console.error('保存游戏状态失败:', error);
        }
    }

    // 加载游戏状态
    loadGameState() {
        try {
            const saved = localStorage.getItem('xiuxian_game_state');
            if (saved) {
                const gameState = JSON.parse(saved);

                // 检查是否过期（24小时）
                if (Date.now() - gameState.timestamp < 24 * 60 * 60 * 1000) {
                    this.player = gameState.player;
                    this.isAuthenticated = gameState.isAuthenticated;
                    return true;
                }
            }
        } catch (error) {
            console.error('加载游戏状态失败:', error);
        }
        return false;
    }

    // 登出
    async logout() {
        try {
            if (window.gameAPI) {
                await gameAPI.logout();
            }
        } catch (error) {
            console.warn('登出请求失败:', error);
        } finally {
            if (window.api) {
                window.api.clearToken();
            }
            this.currentUser = null;
            this.player = null;
            this.isAuthenticated = false;
            this.dataLoaders.clear();
            this.showLoginPage();
            this.showToast('已成功登出', 'info');
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
