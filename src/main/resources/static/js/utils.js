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
            const response = await gameAPI.getPlayerProfile();
            if (response && response.success && response.data) {
                this.player = response.data;
                this.updatePlayerUI();
                return true;
            } else {
                throw new Error(response?.message || '获取玩家资料失败');
            }
        } catch (error) {
            console.error('加载玩家资料失败:', error);

            // 如果获取玩家资料失败，创建默认玩家数据
            this.createDefaultPlayer();
            return false;
        }
    }

    // 创建默认玩家数据
    createDefaultPlayer() {
        const username = document.getElementById('loginUsername')?.value || '玩家';
        this.player = {
            id: 1,
            username: username,
            nickname: username,
            level: 1,
            exp: 0,
            expToNext: 100,
            realm: '练气期',
            spiritStones: 1000,
            health: 100,
            mana: 50,
            attack: 10,
            defense: 5,
            currentExp: 0
        };
        this.updatePlayerUI();
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

            // 登录失败时也显示游戏界面，但使用本地数据
            this.fallbackToLocalMode(username);
        } finally {
            this.isLoading = false;
            this.showLoading(false);
        }
    }

    // 降级到本地模式
    fallbackToLocalMode(username) {
        console.warn('API不可用，切换到本地模式');

        this.isAuthenticated = true;
        this.currentUser = { username: username };
        this.createDefaultPlayer();

        this.showGamePage();
        this.loadLocalGameData();
    }

    // 加载本地游戏数据
    loadLocalGameData() {
        this.showLoading(true);

        try {
            // 使用本地数据
            this.renderSkills(this.getDefaultSkills());
            this.renderEquipment(this.getDefaultEquipment());
            this.renderInventory(this.getDefaultInventory());
            this.renderQuests(this.getDefaultQuests());

            this.updatePlayerUI();
            this.showToast('本地游戏数据加载完成', 'success');
        } catch (error) {
            console.error('加载本地数据失败:', error);
            this.showToast('加载数据失败: ' + error.message, 'error');
        } finally {
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
        // 修复：检查认证状态，但允许降级到本地模式
        if (!this.isAuthenticated) {
            console.warn('用户未认证，无法加载游戏数据');

            // 检查是否有本地保存的状态
            if (this.loadGameState()) {
                console.log('使用本地保存的游戏状态');
                this.isAuthenticated = true;
            } else {
                this.showToast('请先登录', 'warning');
                return;
            }
        }

        this.showLoading(true);

        try {
            // 尝试从API加载数据
            const loaders = [
                this.loadSkills(),
                this.loadEquipment(),
                this.loadInventory(),
                this.loadQuests()
            ];

            await Promise.allSettled(loaders);

            this.updatePlayerUI();
            this.showToast('游戏数据加载完成', 'success');

        } catch (error) {
            console.error('加载游戏数据失败:', error);
            this.showToast('加载游戏数据失败: ' + error.message, 'error');

            // API失败时使用本地数据
            this.loadLocalGameData();
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

    // 渲染技能列表
    renderSkills(skills) {
        const skillsList = document.getElementById('skillsList');
        if (!skillsList || !skills) return;

        skillsList.innerHTML = skills.map(skill => `
            <div class="skill-item" data-skill-id="${skill.id}">
                <div class="skill-header">
                    <span class="skill-name">${skill.name}</span>
                    <span class="skill-level">Lv.${skill.level}</span>
                </div>
                <div class="skill-description">${skill.description}</div>
                <div class="skill-stats">
                    <span>伤害: ${skill.damage || 0}</span>
                    <span>冷却: ${skill.cooldown || 0}s</span>
                    <span>消耗: ${skill.cost || 0}</span>
                </div>
                <button class="btn btn-sm" onclick="useSkill(${skill.id})">使用</button>
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

    // 渲染背包
    renderInventory(items) {
        const inventoryGrid = document.getElementById('inventoryGrid');
        if (!inventoryGrid || !items) return;

        inventoryGrid.innerHTML = items.map(item => `
            <div class="inventory-item">
                <div class="item-name">${item.name}</div>
                <div class="item-type">${item.type}</div>
                <div class="item-quantity">数量: ${item.quantity}</div>
                <button class="btn btn-sm" onclick="useItem(${item.id})">使用</button>
            </div>
        `).join('');
    }

    // 渲染任务列表
    renderQuests(quests) {
        const questsList = document.getElementById('questsList');
        if (!questsList || !quests) return;

        questsList.innerHTML = quests.map(playerQuest => {
            const quest = playerQuest.quest;
            const progress = playerQuest.currentProgress || 0;
            const required = quest.requiredAmount || 1;
            const progressPercent = Math.min((progress / required) * 100, 100);
            const isCompleted = playerQuest.completed;
            const isClaimed = playerQuest.rewardClaimed;
            
            let statusText = '';
            let buttonText = '';
            let buttonClass = 'btn btn-sm';
            let buttonAction = '';
            
            if (isClaimed) {
                statusText = '<span class="quest-status completed">已领取</span>';
                buttonText = '已领取';
                buttonClass += ' btn-secondary disabled';
            } else if (isCompleted) {
                statusText = '<span class="quest-status completed">已完成</span>';
                buttonText = '领取奖励';
                buttonClass += ' btn-success';
                buttonAction = `claimQuest(${playerQuest.id})`;
            } else {
                statusText = `<span class="quest-status in-progress">进行中 ${progress}/${required}</span>`;
                buttonText = '进行中';
                buttonClass += ' btn-secondary disabled';
            }
            
            return `
                <div class="quest-item ${isCompleted ? 'completed' : ''}">
                    <div class="quest-header">
                        <span class="quest-title">${quest.title}</span>
                        ${statusText}
                    </div>
                    <div class="quest-description">${quest.description}</div>
                    <div class="quest-progress">
                        <div class="progress-bar">
                            <div class="progress" style="width: ${progressPercent}%"></div>
                        </div>
                        <span class="progress-text">${progress}/${required}</span>
                    </div>
                    <div class="quest-reward">奖励: ${quest.rewardExp}经验 + ${quest.rewardSpiritStones}灵石</div>
                    <button class="${buttonClass}" onclick="${buttonAction}" ${isClaimed || !isCompleted ? 'disabled' : ''}>${buttonText}</button>
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
        // 创建简单的toast
        const toast = document.createElement('div');
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${this.getToastColor(type)};
            color: white;
            padding: 12px 20px;
            border-radius: 4px;
            z-index: 10000;
            max-width: 300px;
        `;
        toast.textContent = message;

        document.body.appendChild(toast);

        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
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

    // 默认数据
    getDefaultSkills() {
        return [
            {
                id: 1,
                name: '基础修炼法',
                level: 1,
                description: '提升基础修炼速度',
                damage: 0,
                cooldown: 0,
                cost: 0
            }
        ];
    }

    getDefaultEquipment() {
        return [
            {
                id: 1,
                name: '木剑',
                type: '武器',
                attackBonus: 5
            }
        ];
    }

    getDefaultInventory() {
        return [
            {
                id: 1,
                name: '灵石',
                type: '货币',
                quantity: 1000
            }
        ];
    }

    getDefaultQuests() {
        return [
            {
                id: 1,
                title: '初次修炼',
                description: '完成一次修炼来获得经验',
                reward: '100经验'
            }
        ];
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