// 认证管理
class AuthManager {
    constructor() {
        this.currentUser = null;
        this.player = null;
        this.isAuthenticated = false;
        this.init();
    }

    // 初始化认证状态
    async init() {
        // 默认显示登录页面
        this.showLoginPage();
        
        const token = localStorage.getItem('authToken');
        if (token) {
            try {
                await this.loadUserData();
                this.showGamePage();
            } catch (error) {
                console.error('Failed to load user data:', error);
                this.logout();
            }
        }
    }

    // 加载用户数据
    async loadUserData() {
        try {
            const userResponse = await gameAPI.getCurrentUser();
            if (userResponse.success && userResponse.data) {
                this.currentUser = userResponse.data;
                this.isAuthenticated = true;

                // 加载玩家数据
                const playerResponse = await gameAPI.getPlayerProfile();
                if (playerResponse.success && playerResponse.data) {
                    this.player = playerResponse.data;
                    this.updatePlayerUI();
                }
            } else {
                throw new Error('获取用户信息失败');
            }
        } catch (error) {
            console.error('Failed to load user data:', error);
            throw error;
        }
    }

    // 更新玩家UI
    updatePlayerUI() {
        if (!this.player || !this.currentUser) {
            console.error('Missing player or currentUser data');
            return;
        }

        try {
            // 更新顶部玩家信息
            const playerNameElement = document.getElementById('playerName');
            if (playerNameElement) {
                playerNameElement.textContent = this.currentUser.username || '未知用户';
            }
            
            const playerLevelElement = document.getElementById('playerLevel');
            if (playerLevelElement) {
                playerLevelElement.textContent = `等级: ${this.player.level || 1}`;
            }
            
            const playerRealmElement = document.getElementById('playerRealm');
            if (playerRealmElement) {
                playerRealmElement.textContent = `境界: ${this.player.realm || '练气期'}`;
            }
            
            const playerExpElement = document.getElementById('playerExp');
            if (playerExpElement) {
                const currentExp = this.player.currentExp || 0;
                const expToNext = this.player.expToNext || 100;
                playerExpElement.textContent = `经验: ${currentExp}/${expToNext}`;
            }
            
            const playerSpiritStonesElement = document.getElementById('playerSpiritStones');
            if (playerSpiritStonesElement) {
                playerSpiritStonesElement.textContent = `灵石: ${this.player.spiritStones || 0}`;
            }

            // 更新修炼进度
            const expProgressElement = document.getElementById('expProgress');
            const expTextElement = document.getElementById('expText');
            if (expProgressElement && expTextElement) {
                const currentExp = this.player.currentExp || 0;
                const expToNext = this.player.expToNext || 100;
                const expPercent = Math.min((currentExp / expToNext) * 100, 100);
                expProgressElement.style.width = expPercent + '%';
                expTextElement.textContent = `${currentExp}/${expToNext}`;
            }

            // 更新属性
            const playerHealthElement = document.getElementById('playerHealth');
            const playerManaElement = document.getElementById('playerMana');
            const playerAttackElement = document.getElementById('playerAttack');
            const playerDefenseElement = document.getElementById('playerDefense');
            
            if (playerHealthElement) playerHealthElement.textContent = this.player.maxHealth || 100;
            if (playerManaElement) playerManaElement.textContent = this.player.maxMana || 50;
            if (playerAttackElement) playerAttackElement.textContent = this.player.attack || 10;
            if (playerDefenseElement) playerDefenseElement.textContent = this.player.defense || 5;
        } catch (error) {
            console.error('Error updating player UI:', error);
        }
    }

    // 显示游戏页面
    showGamePage() {
        document.getElementById('loginPage').style.display = 'none';
        document.getElementById('gamePage').style.display = 'block';
        
        // 加载游戏数据
        this.loadGameData();
    }

    // 显示登录页面
    showLoginPage() {
        document.getElementById('gamePage').style.display = 'none';
        document.getElementById('loginPage').style.display = 'block';
    }

    // 加载游戏数据
    async loadGameData() {
        try {
            // 并行加载所有游戏数据
            await Promise.all([
                this.loadSkills(),
                this.loadEquipment(),
                this.loadInventory(),
                this.loadQuests()
            ]);
        } catch (error) {
            console.error('Failed to load game data:', error);
        }
    }

    // 加载技能数据
    async loadSkills() {
        try {
            const response = await gameAPI.getSkills();
            if (response.success) {
                this.renderSkills(response.data);
            }
        } catch (error) {
            console.error('Failed to load skills:', error);
        }
    }

    // 渲染技能列表
    renderSkills(skills) {
        const skillsList = document.getElementById('skillsList');
        skillsList.innerHTML = '';

        skills.forEach(skill => {
            const skillElement = document.createElement('div');
            skillElement.className = 'skill-item';
            skillElement.innerHTML = `
                <div class="skill-header">
                    <span class="skill-name">${skill.name}</span>
                    <span class="skill-level">Lv.${skill.level}</span>
                </div>
                <div class="skill-description">${skill.description}</div>
                <div class="skill-stats">
                    <span>伤害: ${skill.baseDamage}</span>
                    <span>冷却: ${skill.cooldown}s</span>
                    <span>消耗: ${skill.manaCost}</span>
                </div>
            `;
            skillsList.appendChild(skillElement);
        });
    }

    // 加载装备数据
    async loadEquipment() {
        try {
            const response = await gameAPI.getEquipment();
            if (response.success) {
                this.renderEquipment(response.data);
            }
        } catch (error) {
            console.error('Failed to load equipment:', error);
        }
    }

    // 渲染装备
    renderEquipment(equipment) {
        const slots = ['weapon', 'chest', 'helmet', 'boots', 'shield', 'ring'];
        
        slots.forEach(slotType => {
            const slotElement = document.getElementById(slotType + 'Slot');
            const equippedItem = equipment.find(item => item.slotType === slotType);
            
            if (equippedItem) {
                slotElement.innerHTML = `
                    <div class="equipped-item">
                        <i class="fas fa-${this.getItemIcon(slotType)}"></i>
                        <div class="item-name">${equippedItem.name}</div>
                        <div class="item-stats">
                            ${equippedItem.attackBonus > 0 ? `攻击+${equippedItem.attackBonus}` : ''}
                            ${equippedItem.defenseBonus > 0 ? `防御+${equippedItem.defenseBonus}` : ''}
                        </div>
                    </div>
                `;
                slotElement.classList.add('has-item');
            } else {
                slotElement.innerHTML = '<span>未装备</span>';
                slotElement.classList.remove('has-item');
            }
        });
    }

    // 获取物品图标
    getItemIcon(slotType) {
        const icons = {
            weapon: 'sword',
            chest: 'tshirt',
            helmet: 'hat-wizard',
            boots: 'shoe-prints',
            shield: 'shield-alt',
            ring: 'ring'
        };
        return icons[slotType] || 'question';
    }

    // 加载背包数据
    async loadInventory() {
        try {
            const response = await gameAPI.getInventory();
            if (response.success) {
                this.renderInventory(response.data);
            }
        } catch (error) {
            console.error('Failed to load inventory:', error);
        }
    }

    // 渲染背包
    renderInventory(items) {
        const inventoryGrid = document.getElementById('inventoryGrid');
        inventoryGrid.innerHTML = '';

        // 生成40个背包格子
        for (let i = 0; i < 40; i++) {
            const slot = document.createElement('div');
            slot.className = 'inventory-slot';
            slot.dataset.index = i;

            const item = items[i];
            if (item) {
                slot.classList.add('has-item');
                slot.innerHTML = `
                    <div class="item-icon">
                        <i class="fas fa-${this.getItemIcon(item.type)}"></i>
                    </div>
                    <div class="item-quantity">${item.quantity}</div>
                `;
                slot.title = `${item.name}\n${item.description}`;
            } else {
                slot.innerHTML = '<span style="color: #cbd5e0;">+</span>';
            }

            inventoryGrid.appendChild(slot);
        }
    }

    // 加载任务数据
    async loadQuests() {
        try {
            const response = await gameAPI.getQuests();
            if (response.success) {
                this.renderQuests(response.data);
            }
        } catch (error) {
            console.error('Failed to load quests:', error);
        }
    }

    // 渲染任务列表
    renderQuests(quests) {
        const questsList = document.getElementById('questsList');
        questsList.innerHTML = '';

        quests.forEach(quest => {
            const questElement = document.createElement('div');
            questElement.className = 'quest-item';
            
            const progress = quest.currentProgress || 0;
            const target = quest.targetProgress || 1;
            const progressPercent = Math.min((progress / target) * 100, 100);

            questElement.innerHTML = `
                <div class="quest-header">
                    <span class="quest-title">${quest.title}</span>
                    <span class="quest-type">${quest.type}</span>
                </div>
                <div class="quest-description">${quest.description}</div>
                <div class="quest-progress">
                    <span>进度: ${progress}/${target}</span>
                    <span>${progressPercent.toFixed(0)}%</span>
                </div>
                <div class="progress-bar" style="margin-top: 8px;">
                    <div class="progress-fill" style="width: ${progressPercent}%"></div>
                </div>
            `;
            questsList.appendChild(questElement);
        });
    }

    // 登出
    logout() {
        api.clearToken();
        this.currentUser = null;
        this.player = null;
        this.isAuthenticated = false;
        this.showLoginPage();
        showToast('已成功登出', 'info');
    }

    // 刷新玩家数据
    async refreshPlayer() {
        try {
            const response = await gameAPI.getPlayerProfile();
            if (response.success) {
                this.player = response.data;
                this.updatePlayerUI();
            }
        } catch (error) {
            console.error('Failed to refresh player data:', error);
        }
    }
}

// 创建认证管理器实例
const authManager = new AuthManager();

// 登录函数
async function login(event) {
    event.preventDefault();
    
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    
    if (!username || !password) {
        showToast('请填写用户名和密码', 'warning');
        return;
    }

    try {
        const response = await gameAPI.login(username, password);
        if (response.success) {
            showToast('登录成功', 'success');
            await authManager.loadUserData();
            authManager.showGamePage();
        }
    } catch (error) {
        showToast('登录失败: ' + error.message, 'error');
    }
}

// 注册函数
async function register(event) {
    event.preventDefault();
    
    const username = document.getElementById('registerUsername').value;
    const nickname = document.getElementById('registerNickname').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    const confirmPassword = document.getElementById('registerConfirmPassword').value;
    
    if (!username || !nickname || !email || !password || !confirmPassword) {
        showToast('请填写所有字段', 'warning');
        return;
    }

    if (password.length < 6) {
        showToast('密码长度至少6位', 'warning');
        return;
    }

    if (password !== confirmPassword) {
        showToast('两次输入的密码不一致', 'warning');
        return;
    }

    try {
        const response = await gameAPI.register(username, nickname, email, password);
        if (response.success) {
            showToast('注册成功，请登录', 'success');
            showLogin(); // 切换到登录表单
        }
    } catch (error) {
        showToast('注册失败: ' + error.message, 'error');
    }
}

// 登出函数
function logout() {
    authManager.logout();
}

// 切换到登录表单
function showLogin() {
    document.getElementById('loginForm').style.display = 'block';
    document.getElementById('registerForm').style.display = 'none';
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelector('.tab-btn:first-child').classList.add('active');
}

// 切换到注册表单
function showRegister() {
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('registerForm').style.display = 'block';
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelector('.tab-btn:last-child').classList.add('active');
}