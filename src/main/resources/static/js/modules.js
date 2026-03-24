// 模块管理和导航系统
class ModuleManager {
    constructor() {
        this.currentModule = 'dashboard';
        this.navigationMode = 'drawer'; // 'drawer' 或 'tabs'
        this.modules = [
            'dashboard', 'combat', 'inventory', 'pets', 'quests', 'skills', 
            'shop', 'mail', 'guild', 'ranking', 'achievements', 
            'auction', 'vip', 'activity', 'narrative', 'lore', 'combos', 'petEvolution', 'map',
            'checkin'
        ];
        this.init();
    }

    init() {
        // 从localStorage恢复导航模式
        const savedMode = localStorage.getItem('navigationMode');
        if (savedMode) {
            this.navigationMode = savedMode;
            this.applyNavigationMode();
        }
        
        // 绑定事件
        this.bindEvents();
        
        // 初始化模块
        this.showModule('dashboard');
    }

    bindEvents() {
        // 绑定抽屉切换
        window.toggleDrawer = () => this.toggleDrawer();
        
        // 绑定导航模式切换
        window.toggleNavigationMode = () => this.toggleNavigationMode();
        
        // 绑定模块切换
        window.showModule = (moduleName) => this.showModule(moduleName);
        
        // 绑定各种标签页切换
        window.showInventoryTab = (tab) => this.showTab('inventory', tab);
        window.showShopTab = (tab) => this.showTab('shop', tab);
        window.showRankingTab = (tab) => this.showTab('ranking', tab);
        window.showAuctionTab = (tab) => this.showTab('auction', tab);
        window.showVipTab = (tab) => this.showTab('vip', tab);
        window.showActivityTab = (tab) => this.showTab('activity', tab);
    }

    toggleNavigationMode() {
        this.navigationMode = this.navigationMode === 'drawer' ? 'tabs' : 'drawer';
        localStorage.setItem('navigationMode', this.navigationMode);
        this.applyNavigationMode();
    }

    applyNavigationMode() {
        const body = document.body;
        const tabNavigation = document.getElementById('tabNavigation');
        const drawerToggle = document.getElementById('drawerToggle');
        const navModeText = document.getElementById('navModeText');

        if (this.navigationMode === 'tabs') {
            body.classList.add('tab-navigation-mode');
            if (tabNavigation) tabNavigation.style.display = 'block';
            if (drawerToggle) drawerToggle.style.display = 'none';
            if (navModeText) navModeText.textContent = '切换到抽屉';
        } else {
            body.classList.remove('tab-navigation-mode');
            if (tabNavigation) tabNavigation.style.display = 'none';
            if (drawerToggle) drawerToggle.style.display = 'block';
            if (navModeText) navModeText.textContent = '切换到标签页';
        }
    }

    toggleDrawer() {
        const drawer = document.getElementById('drawer');
        if (drawer) {
            drawer.classList.toggle('open');
        }
    }

    showModule(moduleName) {
        console.log('切换到模块:', moduleName);
        
        // 隐藏所有模块
        this.modules.forEach(module => {
            const element = document.getElementById(`${module}-module`);
            if (element) {
                element.style.display = 'none';
                element.classList.remove('active');
            }
        });

        // 显示目标模块
        const targetModule = document.getElementById(`${moduleName}-module`);
        if (targetModule) {
            targetModule.style.display = 'block';
            targetModule.classList.add('active');
        }

        // 更新导航状态
        this.updateNavigationState(moduleName);
        
        // 加载模块数据
        this.loadModuleData(moduleName);
        
        // 关闭抽屉（移动端）
        const drawer = document.getElementById('drawer');
        if (drawer) {
            drawer.classList.remove('open');
        }

        this.currentModule = moduleName;
    }

    updateNavigationState(moduleName) {
        // 更新抽屉导航状态
        document.querySelectorAll('.drawer-item').forEach(item => {
            item.classList.remove('active');
            if (item.dataset.module === moduleName) {
                item.classList.add('active');
            }
        });

        // 更新标签页导航状态
        document.querySelectorAll('.tab-navigation .tab-btn').forEach(btn => {
            btn.classList.remove('active');
            if (btn.dataset.module === moduleName) {
                btn.classList.add('active');
            }
        });
    }

    showTab(moduleType, tabName) {
        // 隐藏所有标签页内容
        document.querySelectorAll(`#${moduleType}-module .tab-content, #${moduleType}-module [class$="-tab-content"]`).forEach(tab => {
            tab.style.display = 'none';
        });

        // 显示目标标签页
        const targetTab = document.getElementById(`${tabName}-${moduleType}-tab`) || 
                         document.getElementById(`${tabName}-tab`);
        if (targetTab) {
            targetTab.style.display = 'block';
        }

        // 更新标签按钮状态
        document.querySelectorAll(`#${moduleType}-module .tab-btn`).forEach(btn => {
            btn.classList.remove('active');
        });
        
        event.target.classList.add('active');
    }

    async loadModuleData(moduleName) {
        try {
            switch (moduleName) {
                case 'dashboard':
                    await this.loadDashboardData();
                    break;
                case 'mail':
                    await this.loadMailData();
                    break;
                case 'auction':
                    await this.loadAuctionData();
                    break;
                case 'vip':
                    await this.loadVipData();
                    break;
                case 'activity':
                    await this.loadActivityData();
                    break;
                case 'inventory':
                    await this.loadInventoryData();
                    break;
                case 'quests':
                    await this.loadQuestsData();
                    break;
                case 'pets':
                    await this.loadPetsData();
                    break;
                case 'skills':
                    await this.loadSkillsData();
                    break;
                case 'shop':
                    await this.loadShopData();
                    break;
                case 'combat':
                    await this.loadCombatData();
                    break;
                case 'guild':
                    await this.loadGuildData();
                    break;
                case 'ranking':
                    await this.loadRankingData();
                    break;
                case 'achievements':
                    await this.loadAchievementsData();
                    break;
                case 'combos':
                    await this.loadCombosData();
                    break;
                case 'petEvolution':
                    await this.loadPetEvolutionData();
                    break;
                case 'narrative':
                    await this.loadNarrativeData();
                    break;
                case 'lore':
                    await this.loadLoreData();
                    break;
                case 'map':
                    await this.loadMapData();
                    break;
                case 'checkin':
                    await this.loadCheckInData();
                    break;
                case 'achievements':
                    await this.loadAchievementsData();
                    break;
            }
        } catch (error) {
            console.error(`加载${moduleName}模块数据失败:`, error);
        }
    }

    async loadDashboardData() {
        // 修炼模块数据已在main.js中处理
        console.log('加载修炼模块数据');
    }

    async loadMailData() {
        console.log('加载邮件数据');
        try {
            const response = await api.get('/mail/list');
            if (response.success) {
                this.displayMails(response.data);
            }
        } catch (error) {
            console.error('加载邮件失败:', error);
        }
    }

    displayMails(mails) {
        const mailList = document.getElementById('mailList');
        if (!mailList) return;

        if (!mails || mails.length === 0) {
            mailList.innerHTML = '<div class="empty-state">暂无邮件</div>';
            return;
        }

        mailList.innerHTML = mails.map(mail => `
            <div class="mail-item ${mail.isRead ? '' : 'unread'}" onclick="openMail(${mail.id})">
                <div class="mail-header">
                    <h4>${mail.title}</h4>
                    <span class="mail-time">${this.formatDate(mail.createdAt)}</span>
                </div>
                <p class="mail-content">${mail.content}</p>
                ${mail.hasAttachment ? '<div class="mail-attachment"><i class="fas fa-paperclip"></i> 有附件</div>' : ''}
                ${!mail.isRead ? '<div class="unread-indicator">未读</div>' : ''}
            </div>
        `).join('');
    }

    async loadAuctionData() {
        console.log('加载拍卖行数据');
        // 实现拍卖行数据加载
    }

    async loadVipData() {
        console.log('加载VIP数据');
        // 实现VIP数据加载
    }

    async loadActivityData() {
        console.log('加载活动数据');
        // 实现活动数据加载
    }

    async loadInventoryData() {
        console.log('加载背包数据');
        // 背包数据加载逻辑
    }

    async loadQuestsData() {
        console.log('加载任务数据');
        // 任务数据加载逻辑
    }

    async loadPetsData() {
        console.log('加载宠物数据');
        // 宠物数据加载逻辑
        if (window.loadMyPets) {
            await window.loadMyPets();
        }
    }

    async loadSkillsData() {
        console.log('加载技能数据');
        // 技能数据加载逻辑
        if (window.loadMySkills) {
            await window.loadMySkills();
        }
    }

    async loadShopData() {
        console.log('加载商城数据');
        // 商城数据加载逻辑
    }

    async loadCombatData() {
        console.log('加载战斗数据');
        // 战斗数据加载逻辑
    }

    async loadGuildData() {
        console.log('加载宗门数据');
        // 宗门数据加载逻辑
    }

    async loadRankingData() {
        console.log('加载排行榜数据');
        // 排行榜数据加载逻辑
    }

    async loadAchievementsData() {
        console.log('加载成就数据');
        // 成就数据加载逻辑
    }

    async loadCombosData() {
        console.log('加载技能连招数据');
        try {
            if (window.skillComboSystem) {
                await window.skillComboSystem.loadCombos();
            }
        } catch (error) {
            console.error('加载连招数据失败:', error);
        }
    }

    async loadPetEvolutionData() {
        console.log('加载宠物进化数据');
        try {
            if (window.petEvolutionSystem) {
                await window.petEvolutionSystem.loadMyPetsEvolution();
            }
        } catch (error) {
            console.error('加载宠物进化数据失败:', error);
        }
    }

    async loadNarrativeData() {
        console.log('加载叙事数据');
        if (window.NarrativeUI && window.NarrativeUI.instance) {
            window.NarrativeUI.instance.loadNarratives();
        }
    }

    async loadLoreData() {
        console.log('加载传说数据');
        if (window.LoreUI && window.LoreUI.instance) {
            window.LoreUI.instance.loadLoreEntries();
        }
    }

    async loadMapData() {
        console.log('加载地图数据');
        if (window.gameMapSystem) {
            await window.gameMapSystem.init();
        }
    }

    async loadCheckInData() {
        console.log('加载签到数据');
        if (window.checkInSystem) {
            await window.checkInSystem.init();
        }
    }

    async loadAchievementsData() {
        console.log('加载成就数据');
        if (window.achievementPanel) {
            await window.achievementPanel.init();
        }
    }

    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    }

    showToast(message, type = 'info') {
        // 使用authManager的showToast方法
        if (window.authManager && window.authManager.showToast) {
            window.authManager.showToast(message, type);
        } else {
            console.log(`[${type.toUpperCase()}] ${message}`);
        }
    }
}

// 创建模块管理器实例
const moduleManager = new ModuleManager();

// 全局函数
window.refreshMails = async function() {
    await moduleManager.loadMailData();
    moduleManager.showToast('邮件已刷新', 'success');
};

window.markAllAsRead = async function() {
    try {
        const response = await api.post('/mail/mark-all-read');
        if (response.success) {
            await moduleManager.loadMailData();
            moduleManager.showToast('所有邮件已标记为已读', 'success');
        }
    } catch (error) {
        moduleManager.showToast('操作失败: ' + error.message, 'error');
    }
};

window.openMail = async function(mailId) {
    try {
        const response = await api.get(`/mail/${mailId}`);
        if (response.success) {
            // 显示邮件详情模态框
            showMailModal(response.data);
        }
    } catch (error) {
        moduleManager.showToast('打开邮件失败: ' + error.message, 'error');
    }
};

function showMailModal(mail) {
    // 创建邮件详情模态框
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>${mail.title}</h3>
                <button class="close" onclick="this.closest('.modal').remove()">&times;</button>
            </div>
            <div class="modal-body">
                <p><strong>发送时间:</strong> ${moduleManager.formatDate(mail.createdAt)}</p>
                <div class="mail-content">${mail.content}</div>
                ${mail.attachments && mail.attachments.length > 0 ? `
                    <div class="mail-attachments">
                        <h4>附件:</h4>
                        ${mail.attachments.map(att => `
                            <div class="attachment-item">
                                <span>${att.itemName} x${att.quantity}</span>
                                <button class="btn btn-primary btn-sm" onclick="claimAttachment(${mail.id}, ${att.id})">
                                    领取
                                </button>
                            </div>
                        `).join('')}
                    </div>
                ` : ''}
            </div>
        </div>
    `;
    document.body.appendChild(modal);
}

window.claimAttachment = async function(mailId, attachmentId) {
    try {
        const response = await api.post(`/mail/${mailId}/claim`);
        if (response.success) {
            moduleManager.showToast('附件领取成功', 'success');
            // 刷新邮件列表和玩家数据
            await moduleManager.loadMailData();
            if (window.authManager && window.authManager.loadPlayerProfile) {
                await window.authManager.loadPlayerProfile();
            }
            // 关闭模态框
            document.querySelector('.modal').remove();
        }
    } catch (error) {
        moduleManager.showToast('领取失败: ' + error.message, 'error');
    }
};

// 导出到全局
window.moduleManager = moduleManager;