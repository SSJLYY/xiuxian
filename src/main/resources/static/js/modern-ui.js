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
                console.log('加载修炼模块数据');
                break;
            case 'combat':
                console.log('加载战斗模块数据');
                this.loadCombatModule();
                break;
            case 'inventory':
                console.log('加载背包模块数据');
                this.loadInventoryModule();
                break;
            case 'skills':
                console.log('加载技能模块数据');
                this.loadSkillsModule();
                break;
            case 'pets':
                console.log('加载宠物模块数据');
                this.loadPetsModule();
                break;
            case 'shop':
                console.log('加载商城模块数据');
                this.loadShopModule();
                break;
            case 'quests':
                console.log('加载任务模块数据');
                this.loadQuestsModule();
                break;
        }
    }

    async loadCombatModule() {
        try {
            const response = await gameAPI.getCurrentPlayerProfile();
            if (response.success) {
                this.renderCombatModule(response.data);
            }
        } catch (error) {
            console.error('加载战斗模块失败:', error);
        }
    }

    renderCombatModule(player) {
        const module = document.getElementById('combat-module');
        if (!module) return;
        module.innerHTML = `
            <h2>战斗系统</h2>
            <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:20px;margin-top:20px;">
                <div style="background:linear-gradient(135deg,#667eea,#764ba2);padding:20px;border-radius:12px;color:white;">
                    <h3>角色属性</h3>
                    <p>等级: ${player.level || 1}</p>
                    <p>境界: ${player.realm || '练气期'}</p>
                    <p>生命值: ${player.health || 100}</p>
                    <p>攻击力: ${player.attack || 10}</p>
                    <p>防御力: ${player.defense || 5}</p>
                </div>
                <div style="background:linear-gradient(135deg,#f093fb,#f5576c);padding:20px;border-radius:12px;color:white;">
                    <h3>挑战副本</h3>
                    <p>挑战BOSS获得丰厚奖励</p>
                    <button class="btn btn-primary" style="margin-top:10px;">开始挑战</button>
                </div>
                <div style="background:linear-gradient(135deg,#4facfe,#00f2fe);padding:20px;border-radius:12px;color:white;">
                    <h3>PVP对战</h3>
                    <p>与其他玩家一决高下</p>
                    <button class="btn btn-primary" style="margin-top:10px;">匹配对手</button>
                </div>
            </div>
        `;
    }

    async loadInventoryModule() {
        try {
            const response = await gameAPI.getCurrentPlayerProfile();
            if (response.success) {
                this.renderInventoryModule(response.data);
            }
        } catch (error) {
            console.error('加载背包模块失败:', error);
        }
    }

    renderInventoryModule(player) {
        const module = document.getElementById('inventory-module');
        if (!module) return;
        module.innerHTML = `
            <h2>背包系统</h2>
            <div style="margin-top:20px;">
                <div style="background:rgba(255,255,255,0.1);padding:15px;border-radius:8px;margin-bottom:15px;">
                    <h3>灵石</h3>
                    <p style="font-size:24px;color:#ffd700;">${player.spiritStones || 0}</p>
                </div>
                <div style="background:rgba(255,255,255,0.1);padding:15px;border-radius:8px;">
                    <h3>背包物品</h3>
                    <p style="color:#aaa;">背包功能开发中...</p>
                </div>
            </div>
        `;
    }

    async loadShopModule() {
        try {
            const response = await gameAPI.getCurrentPlayerProfile();
            if (response.success) {
                this.renderShopModule(response.data);
            }
        } catch (error) {
            console.error('加载商城模块失败:', error);
        }
    }

    renderShopModule(player) {
        const module = document.getElementById('shop-module');
        if (!module) return;
        const items = [
            {name: '灵石袋(100)', price: 10, desc: '获得100灵石'},
            {name: '灵石袋(1000)', price: 90, desc: '获得1000灵石'},
            {name: '经验丹', price: 50, desc: '获得大量经验'},
            {name: '修炼加速', price: 30, desc: '修炼速度提升50%'},
        ];
        module.innerHTML = `
            <h2>商城系统</h2>
            <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:15px;margin-top:20px;">
                ${items.map(item => `
                    <div style="background:rgba(255,255,255,0.1);padding:15px;border-radius:8px;text-align:center;">
                        <h4>${item.name}</h4>
                        <p style="color:#aaa;font-size:12px;">${item.desc}</p>
                        <p style="color:#ffd700;font-size:18px;margin:10px 0;">${item.price} 灵石</p>
                        <button class="btn btn-primary" onclick="alert('购买成功!')">购买</button>
                    </div>
                `).join('')}
            </div>
        `;
    }

    async loadQuestsModule() {
        try {
            const response = await gameAPI.getCurrentPlayerProfile();
            if (response.success) {
                this.renderQuestsModule(response.data);
            }
        } catch (error) {
            console.error('加载任务模块失败:', error);
        }
    }

    renderQuestsModule(player) {
        const module = document.getElementById('quests-module');
        if (!module) return;
        const quests = [
            {name: '每日修炼', desc: '修炼10分钟', reward: 100, progress: 0, target: 10},
            {name: '击败怪物', desc: '击败5只怪物', reward: 200, progress: 0, target: 5},
            {name: '收集资源', desc: '收集100灵石', reward: 50, progress: 0, target: 100},
        ];
        module.innerHTML = `
            <h2>任务系统</h2>
            <div style="margin-top:20px;">
                ${quests.map(q => `
                    <div style="background:rgba(255,255,255,0.1);padding:15px;border-radius:8px;margin-bottom:10px;">
                        <div style="display:flex;justify-content:space-between;align-items:center;">
                            <div>
                                <h4>${q.name}</h4>
                                <p style="color:#aaa;font-size:12px;">${q.desc}</p>
                            </div>
                            <div style="text-align:right;">
                                <p style="color:#ffd700;">奖励: ${q.reward}灵石</p>
                                <p style="font-size:12px;">进度: ${q.progress}/${q.target}</p>
                            </div>
                        </div>
                        <div style="background:rgba(255,255,255,0.2);height:8px;border-radius:4px;margin-top:10px;">
                            <div style="background:linear-gradient(90deg,#667eea,#764ba2);height:100%;width:${(q.progress/q.target)*100}%;border-radius:4px;"></div>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    async loadSkillsModule() {
        try {
            const response = await gameAPI.getPlayerSkills();
            if (response.success) {
                const skills = response.data || [];
                this.renderSkills(skills);
            }
        } catch (error) {
            console.error('加载技能数据失败:', error);
        }
    }

    renderSkills(skills) {
        const container = document.getElementById('mySkillsList');
        if (!container) {
            // 如果找不到容器，创建一个
            const module = document.getElementById('skills-module');
            if (module && !document.getElementById('mySkillsList')) {
                const skillsList = document.createElement('div');
                skillsList.id = 'mySkillsList';
                skillsList.className = 'skills-grid';
                module.appendChild(skillsList);
            }
        }
        
        const list = document.getElementById('mySkillsList');
        if (!list) return;
        
        if (skills.length === 0) {
            list.innerHTML = '<p style="text-align:center;padding:40px;">你还没有技能</p>';
            return;
        }
        
        list.innerHTML = skills.map(skill => `
            <div class="skill-card" style="background:#16213e;padding:15px;border-radius:8px;margin:10px;">
                <h4 style="margin:10px 0;">${skill.skillName || '未知技能'}</h4>
                <p>等级: ${skill.level || 1}</p>
                <p>伤害: ${skill.damage || 0}</p>
            </div>
        `).join('');
    }

    async loadPetsModule() {
        try {
            const response = await gameAPI.getMyPets();
            if (response.success) {
                const pets = response.data || [];
                this.renderPets(pets);
            }
        } catch (error) {
            console.error('加载宠物数据失败:', error);
        }
    }

    renderPets(pets) {
        const module = document.getElementById('pets-module');
        if (!module) return;
        
        // 检查是否已有宠物列表容器
        let list = document.getElementById('myPetsList');
        if (!list) {
            list = document.createElement('div');
            list.id = 'myPetsList';
            list.className = 'pets-grid';
            module.appendChild(list);
        }
        
        if (pets.length === 0) {
            list.innerHTML = '<p style="text-align:center;padding:40px;">你还没有宠物</p>';
            return;
        }
        
        list.innerHTML = pets.map(pet => `
            <div class="pet-card" style="background:#16213e;padding:15px;border-radius:8px;margin:10px;">
                <h4 style="margin:10px 0;">${pet.nickname || '未命名'}</h4>
                <p>等级: ${pet.level || 1}</p>
            </div>
        `).join('');
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