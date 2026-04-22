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
                    <h4>${escapeHtml(mail.title)}</h4>
                    <span class="mail-time">${this.formatDate(mail.createdAt)}</span>
                </div>
                <p class="mail-content">${escapeHtml(mail.content)}</p>
                ${mail.hasAttachment ? '<div class="mail-attachment"><i class="fas fa-paperclip"></i> 有附件</div>' : ''}
                ${!mail.isRead ? '<div class="unread-indicator">未读</div>' : ''}
            </div>
        `).join('');
    }

    async loadAuctionData() {
        console.log('加载拍卖行数据');
        try {
            // 更新灵石余额
            const ssEl = document.getElementById('auction-ss-balance');
            if (ssEl) {
                const ss = document.getElementById('playerSpiritStones');
                ssEl.textContent = ss ? ss.textContent : '0';
            }
            await window.loadAuctionItems();
        } catch (error) {
            console.error('加载拍卖行失败:', error);
        }
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
        if (window.gameManager) {
            await window.gameManager.loadInventory();
        }
    }

    async loadQuestsData() {
        console.log('加载任务数据');
        if (window.gameManager) {
            await window.gameManager.loadQuestTabs();
        }
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
        if (window.gameManager) {
            await window.gameManager.loadShopItems();
            await window.gameManager.loadSkillShop();
        }
        // 更新灵石余额显示
        this.updatePlayerSpiritStones();
    }

    updatePlayerSpiritStones() {
        const el = document.getElementById('shop-spirit-stones');
        if (el) {
            const ss = document.getElementById('playerSpiritStones');
            if (ss) el.textContent = ss.textContent;
        }
    }

    async loadCombatData() {
        console.log('加载战斗数据');
        // 战斗数据加载逻辑
    }

    async loadGuildData() {
        console.log('加载宗门数据');
        try {
            // 先检查是否有宗门
            const myGuildRes = await api.get('/guild/my');
            if (myGuildRes.success && myGuildRes.data) {
                renderMyGuild(myGuildRes.data);
                document.getElementById('guild-my-tab-btn').style.display = '';
            } else {
                document.getElementById('guild-my-tab-btn').style.display = 'none';
                document.getElementById('guild-my-info').style.display = 'none';
                document.getElementById('guild-actions').style.display = 'none';
            }
            // 默认显示宗门列表
            switchGuildTab('list');
        } catch (error) {
            console.error('加载宗门数据失败:', error);
        }
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
                <h3>${escapeHtml(mail.title)}</h3>
                <button class="close" onclick="this.closest('.modal').remove()">&times;</button>
            </div>
            <div class="modal-body">
                <p><strong>发送时间:</strong> ${moduleManager.formatDate(mail.createdAt)}</p>
                <div class="mail-content">${escapeHtml(mail.content)}</div>
                ${mail.attachments && mail.attachments.length > 0 ? `
                    <div class="mail-attachments">
                        <h4>附件:</h4>
                        ${mail.attachments.map(att => `
                            <div class="attachment-item">
                                <span>${escapeHtml(att.itemName)} x${att.quantity}</span>
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

// ==================== 模块切换辅助函数 ====================

// 背包标签切换
window.switchInventoryTab = function(tab) {
    document.querySelectorAll('#inventory-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.invTab === tab);
    });
    const grid = document.getElementById('inventoryGrid');
    if (!grid) return;
    const items = grid.querySelectorAll('.inventory-cell');
    items.forEach(cell => {
        const type = cell.dataset.itemType || 'all';
        if (tab === 'all' || type === tab) {
            cell.style.display = '';
        } else {
            cell.style.display = 'none';
        }
    });
};

// 商城标签切换
window.switchShopTab = function(tab) {
    document.querySelectorAll('#shop-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.shopTab === tab);
    });
    document.getElementById('shop-general-panel').style.display = (tab === 'general') ? '' : 'none';
    document.getElementById('shop-skill-panel').style.display = (tab === 'skill') ? '' : 'none';
};

// 刷新商城商品
window.refreshShopItems = async function() {
    if (window.gameManager) {
        await window.gameManager.loadShopItems();
        moduleManager.showToast('商城已刷新', 'success');
    }
};

// 任务标签切换
window.switchQuestTab = function(tab) {
    document.querySelectorAll('#quests-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.questTab === tab);
    });
    if (!window.gameManager) return;
    const list = document.getElementById('questsList');
    if (!list) return;
    list.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载中...</p></div>';
    
    const loadFn = {
        daily: () => gameAPI.getDailyQuests(),
        weekly: () => gameAPI.getWeeklyQuests(),
        monthly: () => gameAPI.getMonthlyQuests(),
        main: () => gameAPI.getQuests()
    }[tab];
    
    if (loadFn) {
        loadFn().then(res => {
            if (!res?.success) { list.innerHTML = '<div class="empty-state">加载失败</div>'; return; }
            renderQuestList(list, res.data, tab);
        }).catch(() => { list.innerHTML = '<div class="empty-state">加载失败</div>'; });
    }
};

function renderQuestList(container, quests, tab) {
    if (!quests || quests.length === 0) {
        container.innerHTML = '<div class="empty-state">暂无任务</div>';
        updateQuestStats([]);
        return;
    }
    
    // 统一格式（主线任务返回的是PlayerQuest[]，日常返回的也可能是）
    const normalized = quests.map(q => ({
        quest: q.quest || q,
        completed: q.completed || false,
        rewardClaimed: q.rewardClaimed || false,
        currentProgress: q.currentProgress || 0,
        id: q.id || q.quest?.id
    }));
    
    container.innerHTML = normalized.map(q => {
        const prog = Math.min(q.currentProgress || 0, q.quest.requiredAmount || 1);
        const pct = Math.round((prog / (q.quest.requiredAmount || 1)) * 100);
        const done = !!q.completed;
        const typeLabel = { daily: '每日', weekly: '每周', monthly: '每月', main: '主线' }[tab] || '';
        return `
            <div class="quest-item ${done ? 'completed' : ''}" style="background:rgba(255,255,255,0.05);padding:12px;border-radius:8px;">
                <div class="flex items-center justify-between mb-2">
                    <div class="font-semibold">${escapeHtml(q.quest.title)} <span class="text-xs text-muted">[${typeLabel}]</span></div>
                    ${done && !q.rewardClaimed 
                        ? `<button class="btn btn-primary btn-sm" onclick="claimQuest(${q.id})">领取奖励</button>` 
                        : done 
                            ? `<span class="text-green-400 text-sm">已完成</span>` 
                            : ''}
                </div>
                <div class="text-sm text-muted mb-2">${escapeHtml(q.quest.description || '')}</div>
                <div class="flex items-center gap-2 mb-1">
                    <div class="flex-1 bg-white/10 rounded-full h-2">
                        <div class="bg-accent h-2 rounded-full" style="width:${pct}%"></div>
                    </div>
                    <span class="text-xs text-muted">${prog}/${q.quest.requiredAmount}</span>
                </div>
                <div class="flex gap-4 text-xs text-muted">
                    <span>奖励：经验 ${q.quest.rewardExp || 0}，灵石 ${q.quest.rewardSpiritStones || 0}</span>
                </div>
            </div>
        `;
    }).join('');
    
    updateQuestStats(normalized);
}

function updateQuestStats(normalized) {
    const total = normalized.length;
    const completed = normalized.filter(q => q.completed).length;
    const claimable = normalized.filter(q => q.completed && !q.rewardClaimed).length;
    const el1 = document.getElementById('quest-completed-count');
    const el2 = document.getElementById('quest-claimable-count');
    const el3 = document.getElementById('quest-total-count');
    if (el1) el1.textContent = completed;
    if (el2) el2.textContent = claimable;
    if (el3) el3.textContent = total;
}

// ==================== 宗门系统辅助函数 ====================

window.switchGuildTab = function(tab) {
    document.querySelectorAll('#guild-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.guildTab === tab);
    });
    document.getElementById('guild-list-panel').style.display = (tab === 'list') ? '' : 'none';
    document.getElementById('guild-my-panel').style.display = (tab === 'my') ? '' : 'none';
    if (tab === 'list') loadGuildList();
    if (tab === 'my') loadMyGuildDetail();
};

async function loadGuildList() {
    const panel = document.getElementById('guild-list-panel');
    if (!panel) return;
    panel.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载宗门列表...</p></div>';
    try {
        const res = await api.get('/guild/list');
        if (!res.success) throw new Error(res.message);
        const { guilds } = res.data;
        if (!guilds || guilds.length === 0) {
            panel.innerHTML = '<div class="empty-state">暂无宗门，创建一个吧！</div>';
        } else {
            panel.innerHTML = `
                <div class="guild-create-bar mb-4 p-3 rounded flex items-center justify-between" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                    <div class="text-sm text-muted">创建宗门需要 10000 灵石</div>
                    <button class="btn btn-sm btn-primary" onclick="showCreateGuildForm()"><i class="fa-solid fa-plus"></i> 创建宗门</button>
                </div>
                <div class="guild-grid" style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:15px;">
                    ${guilds.map(g => `
                        <div class="guild-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                            <div class="flex items-center justify-between mb-2">
                                <h4 class="font-bold">${escapeHtml(g.name)}</h4>
                                <span class="text-xs text-muted">Lv.${g.level || 1}</span>
                            </div>
                            <div class="text-sm text-muted mb-2">${escapeHtml(g.description || '暂无描述')}</div>
                            <div class="flex gap-4 text-xs text-muted mb-3">
                                <span><i class="fa-solid fa-users"></i> ${g.memberCount || 0}/${g.maxMembers || 50}</span>
                                <span><i class="fa-solid fa-gem"></i> ${g.treasury || 0}</span>
                            </div>
                            <button class="btn btn-sm w-full" onclick="applyToGuild(${g.id})">申请加入</button>
                        </div>
                    `).join('')}
                </div>
            `;
        }
    } catch (e) {
        panel.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
}

function renderMyGuild(guild) {
    const infoEl = document.getElementById('guild-my-info');
    const actionsEl = document.getElementById('guild-actions');
    if (!infoEl || !actionsEl) return;
    infoEl.style.display = '';
    infoEl.style.cssText += 'background:linear-gradient(135deg,rgba(212,175,55,0.08),rgba(212,175,55,0.03));border:1px solid rgba(212,175,55,0.2);';
    infoEl.innerHTML = `
        <div class="flex items-center justify-between mb-2">
            <h3 class="font-bold" style="color:var(--accent-gold);"><i class="fa-solid fa-chess-rook"></i> ${escapeHtml(guild.name)}</h3>
            <span class="text-sm text-muted">等级 ${guild.level || 1}</span>
        </div>
        <div class="text-sm text-muted mb-2">${escapeHtml(guild.description || '暂无描述')}</div>
        <div class="flex gap-4 text-xs">
            <span class="text-muted"><i class="fa-solid fa-users"></i> 成员 ${guild.memberCount || 0}/${guild.maxMembers || 50}</span>
            <span class="text-muted"><i class="fa-solid fa-gem"></i> 宗门资金 ${guild.treasury || 0}</span>
            <span class="text-muted"><i class="fa-solid fa-star"></i> 贡献 ${guild.myContribution || 0}</span>
        </div>
    `;
    actionsEl.style.display = '';
    actionsEl.innerHTML = `
        <button class="btn btn-sm" onclick="showDonateForm()"><i class="fa-solid fa-donate"></i> 捐献灵石</button>
        <button class="btn btn-sm" onclick="switchGuildTab('my')"><i class="fa-solid fa-users"></i> 宗门成员</button>
        <button class="btn btn-sm btn-danger" onclick="leaveGuild()"><i class="fa-solid fa-sign-out-alt"></i> 退出宗门</button>
    `;
}

async function loadMyGuildDetail() {
    const panel = document.getElementById('guild-my-panel');
    if (!panel) return;
    panel.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载中...</p></div>';
    try {
        const res = await api.get('/guild/my');
        if (!res.success || !res.data) {
            panel.innerHTML = '<div class="empty-state">您还没有加入宗门</div>';
            return;
        }
        const guild = res.data;
        // 获取成员列表
        const detailRes = await api.get(`/guild/${guild.id}`);
        const members = detailRes.success ? (detailRes.data?.members || []) : [];
        panel.innerHTML = `
            <div class="mb-4">
                <h4 class="font-bold mb-3"><i class="fa-solid fa-users"></i> 宗门成员</h4>
                <div style="display:flex;flex-direction:column;gap:8px;">
                    ${members.map(m => `
                        <div class="flex items-center justify-between p-3 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                            <div class="flex items-center gap-2">
                                <div class="font-semibold">${escapeHtml(m.playerName || '成员')}</div>
                                <span class="text-xs text-muted">Lv.${m.level || '?'}</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="text-xs text-accent">贡献 ${m.contribution || 0}</span>
                                <span class="text-xs ${m.role === 'LEADER' ? 'text-yellow-400' : 'text-muted'}">${m.role === 'LEADER' ? '宗主' : m.role === 'ELDER' ? '长老' : '成员'}</span>
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    } catch (e) {
        panel.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
}

window.applyToGuild = async function(guildId) {
    try {
        const res = await api.post(`/guild/apply/${guildId}`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('申请已提交，等待宗主审核', 'success');
    } catch (e) {
        moduleManager.showToast('申请失败: ' + e.message, 'error');
    }
};

window.leaveGuild = async function() {
    if (!confirm('确定要退出宗门吗？')) return;
    try {
        const res = await api.post('/guild/leave');
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('已退出宗门', 'info');
        document.getElementById('guild-my-info').style.display = 'none';
        document.getElementById('guild-actions').style.display = 'none';
        document.getElementById('guild-my-tab-btn').style.display = 'none';
        switchGuildTab('list');
    } catch (e) {
        moduleManager.showToast('退出失败: ' + e.message, 'error');
    }
};

window.showCreateGuildForm = function() {
    const name = prompt('宗门名称:');
    if (!name) return;
    const desc = prompt('宗门描述 (可选):') || '';
    api.post('/guild/create', { guildName: name, description: desc }).then(res => {
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('宗门创建成功！', 'success');
        moduleManager.loadGuildData();
        if (window.authManager?.loadPlayerProfile) window.authManager.loadPlayerProfile();
    }).catch(e => moduleManager.showToast('创建失败: ' + e.message, 'error'));
};

window.showDonateForm = function() {
    const amount = prompt('捐献灵石数量:');
    if (!amount || isNaN(amount) || parseInt(amount) <= 0) return;
    api.post('/guild/donate', { amount: parseInt(amount) }).then(res => {
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('捐献成功！', 'success');
        moduleManager.loadGuildData();
        if (window.authManager?.loadPlayerProfile) window.authManager.loadPlayerProfile();
    }).catch(e => moduleManager.showToast('捐献失败: ' + e.message, 'error'));
};

// ==================== 拍卖行系统辅助函数 ====================

window.switchAuctionTab = function(tab) {
    document.querySelectorAll('#auction-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.auctionTab === tab);
    });
    document.getElementById('auction-browse-panel').style.display = (tab === 'browse') ? '' : 'none';
    document.getElementById('auction-mine-panel').style.display = (tab === 'mine') ? '' : 'none';
    if (tab === 'browse') loadAuctionItems();
    if (tab === 'mine') loadMyAuctionItems();
};

window.loadAuctionItems = async function() {
    const panel = document.getElementById('auction-items-list');
    if (!panel) return;
    panel.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载拍卖品...</p></div>';
    try {
        const typeFilter = document.getElementById('auction-type-filter')?.value || '';
        const minPrice = document.getElementById('auction-min-price')?.value || '';
        const maxPrice = document.getElementById('auction-max-price')?.value || '';
        const params = {};
        if (typeFilter) params.itemType = typeFilter;
        if (minPrice) params.minPrice = minPrice;
        if (maxPrice) params.maxPrice = maxPrice;
        const res = await api.get('/auction/items', params);
        if (!res.success) throw new Error(res.message);
        const records = res.data?.records || [];
        if (records.length === 0) {
            panel.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">拍卖市场暂无商品</div>';
        } else {
            panel.innerHTML = records.map(item => renderAuctionCard(item, 'browse')).join('');
            bindAuctionBuyButtons();
        }
    } catch (e) {
        panel.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

async function loadMyAuctionItems() {
    const panel = document.getElementById('auction-my-items-list');
    if (!panel) return;
    panel.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载中...</p></div>';
    try {
        const res = await api.get('/auction/my-items');
        if (!res.success) throw new Error(res.message);
        const items = res.data || [];
        if (items.length === 0) {
            panel.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">您暂无拍卖中的物品</div>';
        } else {
            panel.innerHTML = items.map(item => renderAuctionCard(item, 'mine')).join('');
            bindAuctionMineButtons();
        }
    } catch (e) {
        panel.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
}

function renderAuctionCard(item, mode) {
    const isActive = item.status === 'ACTIVE';
    const price = item.currentPrice || item.startPrice || 0;
    const sellerName = item.sellerName || item.sellerId || '神秘人';
    const itemName = item.itemName || '未知物品';
    const itemType = item.itemType || 'OTHER';
    const typeLabel = { EQUIPMENT: '装备', CONSUMABLE: '消耗品', MATERIAL: '材料', SKILL: '技能', PET: '宠物', GEM: '宝石', OTHER: '其他' }[itemType] || '其他';
    return `
        <div class="auction-item-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
            <div class="flex items-center justify-between mb-2">
                <h4 class="font-semibold">${escapeHtml(itemName)}</h4>
                <span class="text-xs px-2 py-1 rounded" style="background:rgba(255,255,255,0.1);color:var(--text-muted);">${typeLabel}</span>
            </div>
            <div class="text-sm text-muted mb-2">
                ${item.description ? escapeHtml(item.description) : '无描述'}
            </div>
            <div class="mb-2">
                <div class="text-sm text-muted">卖家: ${escapeHtml(String(sellerName))}</div>
                <div class="font-bold mt-1" style="color:var(--accent-gold);font-size:1.1rem;">
                    <i class="fa-solid fa-gem"></i> ${price}
                </div>
            </div>
            <div class="text-xs text-muted mb-2">
                ${item.endTime ? `剩余时间: ${formatAuctionTime(item.endTime)}` : ''}
            </div>
            ${mode === 'browse' ? `
                <button class="btn btn-sm w-full btn-primary"
                        data-auction-id="${item.id}"
                        ${!isActive ? 'disabled' : ''}>
                    ${!isActive ? '已结束' : '竞拍/购买'}
                </button>
            ` : `
                <button class="btn btn-sm w-full btn-danger"
                        data-auction-cancel="${item.id}"
                        ${!isActive ? 'disabled' : ''}>
                    ${!isActive ? '已结束' : '取消拍卖'}
                </button>
            `}
        </div>
    `;
}

function bindAuctionBuyButtons() {
    document.querySelectorAll('[data-auction-id]').forEach(btn => {
        btn.addEventListener('click', async function() {
            if (this.disabled) return;
            const id = this.getAttribute('data-auction-id');
            const orig = this.textContent;
            this.disabled = true; this.textContent = '购买中...';
            try {
                const res = await api.post(`/auction/buy/${id}`);
                if (!res.success) throw new Error(res.message);
                moduleManager.showToast('购买成功！', 'success');
                if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
                await loadAuctionItems();
                await loadMyAuctionItems();
            } catch (e) {
                moduleManager.showToast('购买失败: ' + e.message, 'error');
                this.disabled = false; this.textContent = orig;
            }
        });
    });
}

function bindAuctionMineButtons() {
    document.querySelectorAll('[data-auction-cancel]').forEach(btn => {
        btn.addEventListener('click', async function() {
            if (this.disabled) return;
            if (!confirm('确定要取消拍卖吗？')) return;
            const id = this.getAttribute('data-auction-cancel');
            this.disabled = true; this.textContent = '取消中...';
            try {
                const res = await api.post(`/auction/cancel/${id}`);
                if (!res.success) throw new Error(res.message);
                moduleManager.showToast('已取消拍卖', 'info');
                await loadMyAuctionItems();
            } catch (e) {
                moduleManager.showToast('取消失败: ' + e.message, 'error');
                this.disabled = false; this.textContent = '取消拍卖';
            }
        });
    });
}

function formatAuctionTime(endTime) {
    if (!endTime) return '-';
    const now = Date.now();
    const end = new Date(endTime).getTime();
    const diff = Math.max(0, Math.floor((end - now) / 1000));
    const h = Math.floor(diff / 3600);
    const m = Math.floor((diff % 3600) / 60);
    const s = diff % 60;
    if (h > 0) return `${h}小时${m}分`;
    if (m > 0) return `${m}分${s}秒`;
    return `${s}秒`;
}

// ==================== 宠物系统辅助函数 ====================

window.switchPetTab = function(tab) {
    document.querySelectorAll('#pets-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.petTab === tab);
    });
    document.getElementById('pets-my-panel').style.display = (tab === 'my') ? '' : 'none';
    document.getElementById('pets-available-panel').style.display = (tab === 'available') ? '' : 'none';
    if (tab === 'my') loadMyPets();
    if (tab === 'available') loadAvailablePets();
};

window.loadMyPets = async function() {
    const container = document.getElementById('myPetsList');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载宠物...</p></div>';
    try {
        const res = await api.get('/pets/my');
        if (!res.success) throw new Error(res.message);
        const pets = res.data || [];
        renderMyPets(pets);
        loadActivePetInfo();
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

function renderMyPets(pets) {
    const container = document.getElementById('myPetsList');
    const select = document.getElementById('evolution-pet-select');
    if (!container) return;
    
    // 更新进化下拉框
    if (select) {
        const defaultOpt = select.querySelector('option[value=""]');
        select.innerHTML = defaultOpt ? defaultOpt.outerHTML : '<option value="">-- 请选择宠物 --</option>';
        pets.forEach(pet => {
            if (pet.level >= 10) { // 只有10级以上才能进化
                const opt = document.createElement('option');
                opt.value = pet.id;
                opt.textContent = `${pet.name || pet.petName} (Lv.${pet.level || 1})`;
                select.appendChild(opt);
            }
        });
    }
    
    if (pets.length === 0) {
        container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">您还没有宠物，快去捕捉吧！</div>';
        return;
    }
    
    container.innerHTML = pets.map(pet => {
        const qualityColors = { NORMAL: '#aaa', GOOD: '#5ba85b', RARE: '#4a90d9', EPIC: '#9b59b6', LEGENDARY: '#f39c12' };
        const qualityColor = qualityColors[pet.quality] || '#aaa';
        const isActive = pet.isActive || false;
        const isLocked = pet.isLocked || false;
        return `
            <div class="pet-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${isActive ? qualityColor : 'rgba(255,255,255,0.1)'};">
                <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                        <span class="pet-icon" style="font-size:1.5rem;">${getPetEmoji(pet.type || 'NORMAL')}</span>
                        <div>
                            <h4 class="font-semibold">${escapeHtml(pet.name || pet.petName || '宠物')}</h4>
                            <span class="text-xs" style="color:${qualityColor};">${pet.quality || '普通'}</span>
                        </div>
                    </div>
                    ${isActive ? '<span class="text-xs px-2 py-1 rounded" style="background:rgba(46,204,113,0.2);color:#2ecc71;">出战中</span>' : ''}
                    ${isLocked ? '<i class="fa-solid fa-lock text-muted"></i>' : ''}
                </div>
                <div class="text-xs text-muted mb-2">
                    等级 ${pet.level || 1} | 经验 ${pet.exp || 0}
                </div>
                <div class="text-xs text-muted mb-2">
                    攻击 ${pet.attack || 0} | 防御 ${pet.defense || 0} | 生命 ${pet.maxHp || pet.hp || 0}
                </div>
                <div class="flex gap-2 flex-wrap mt-3">
                    ${!isActive ? `<button class="btn btn-sm btn-primary" onclick="activatePet(${pet.id})"><i class="fa-solid fa-play"></i> 出战</button>` : ''}
                    <button class="btn btn-sm" onclick="feedPet(${pet.id})"><i class="fa-solid fa-utensils"></i> 喂食</button>
                    <button class="btn btn-sm" onclick="trainPet(${pet.id})"><i class="fa-solid fa-dumbbell"></i> 训练</button>
                    <button class="btn btn-sm" onclick="togglePetLock(${pet.id})"><i class="fa-solid fa-${isLocked ? 'unlock' : 'lock'}"></i></button>
                    ${!isLocked ? `<button class="btn btn-sm btn-danger" onclick="releasePet(${pet.id})"><i class="fa-solid fa-trash"></i></button>` : ''}
                </div>
            </div>
        `;
    }).join('');
}

function getPetEmoji(type) {
    const emojis = { NORMAL: '🐾', FIRE: '🔥', WATER: '💧', GRASS: '🌿', THUNDER: '⚡', ICE: '❄️', DARK: '🌙', LIGHT: '☀️', DRAGON: '🐉' };
    return emojis[type] || '🐾';
}

async function loadActivePetInfo() {
    const infoEl = document.getElementById('active-pet-info');
    if (!infoEl) return;
    try {
        const res = await api.get('/pets/active');
        if (res.success && res.data) {
            const pet = res.data;
            infoEl.style.display = '';
            infoEl.innerHTML = `
                <div class="flex items-center gap-4">
                    <span style="font-size:2rem;">${getPetEmoji(pet.type || 'NORMAL')}</span>
                    <div class="flex-1">
                        <div class="font-bold" style="color:var(--accent-gold);">${escapeHtml(pet.name || pet.petName || '出战宠物')}</div>
                        <div class="text-sm text-muted">等级 ${pet.level || 1} | 战力评估中...</div>
                    </div>
                    <span class="text-sm text-green-400">战斗中...</span>
                </div>
            `;
        } else {
            infoEl.style.display = 'none';
        }
    } catch (e) {
        infoEl.style.display = 'none';
    }
}

window.loadAvailablePets = async function() {
    const container = document.getElementById('availablePetsList');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载可捕获宠物...</p></div>';
    try {
        const res = await api.get('/pets/available');
        if (!res.success) throw new Error(res.message);
        const pets = res.data || [];
        if (pets.length === 0) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">当前没有可捕获的宠物</div>';
            return;
        }
        container.innerHTML = pets.map(pet => {
            const qualityColors = { NORMAL: '#aaa', GOOD: '#5ba85b', RARE: '#4a90d9', EPIC: '#9b59b6', LEGENDARY: '#f39c12' };
            const qualityColor = qualityColors[pet.quality] || '#aaa';
            return `
                <div class="pet-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${qualityColor};">
                    <div class="flex items-center gap-2 mb-2">
                        <span style="font-size:1.5rem;">${getPetEmoji(pet.type || 'NORMAL')}</span>
                        <div>
                            <h4 class="font-semibold">${escapeHtml(pet.name || '野生宠物')}</h4>
                            <span class="text-xs" style="color:${qualityColor};">${pet.quality || '普通'}</span>
                        </div>
                    </div>
                    <div class="text-xs text-muted mb-2">
                        等级 ${pet.minLevel || 1} - ${pet.maxLevel || 10} | 攻击 ${pet.attack || 0}
                    </div>
                    <div class="text-xs text-muted mb-3">${escapeHtml(pet.description || '野生宠物，出没于野外')}</div>
                    <button class="btn btn-sm w-full btn-primary" onclick="capturePet(${pet.id})">
                        <i class="fa-solid fa-hand-sparkles"></i> 捕捉
                    </button>
                </div>
            `;
        }).join('');
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

window.activatePet = async function(playerPetId) {
    try {
        const res = await api.post(`/pets/activate/${playerPetId}`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('设置出战成功！', 'success');
        await loadMyPets();
    } catch (e) {
        moduleManager.showToast('设置失败: ' + e.message, 'error');
    }
};

window.feedPet = async function(playerPetId) {
    try {
        const res = await api.post(`/pets/feed/${playerPetId}`);
        if (!res.success) throw new Error(res.message);
        sessionStorage.setItem('tutorial_pet_fed_once', 'true');
        moduleManager.showToast('喂食成功！宠物很开心！', 'success');
        await loadMyPets();
        if (window.tutorialSystem && typeof window.tutorialSystem.checkProgress === 'function') {
            window.tutorialSystem.checkProgress();
        }
    } catch (e) {
        moduleManager.showToast('喂食失败: ' + e.message, 'error');
    }
};

window.trainPet = async function(playerPetId) {
    const types = ['ATTACK', 'DEFENSE', 'HP'];
    const type = prompt('选择训练类型 (ATTACK/DEFENSE/HP):');
    if (!types.includes(type?.toUpperCase())) {
        moduleManager.showToast('无效的训练类型', 'error');
        return;
    }
    try {
        const res = await api.post(`/pets/train/${playerPetId}`, { trainingType: type.toUpperCase() });
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('训练成功！属性提升了！', 'success');
        await loadMyPets();
    } catch (e) {
        moduleManager.showToast('训练失败: ' + e.message, 'error');
    }
};

window.togglePetLock = async function(playerPetId) {
    try {
        const res = await api.post(`/pets/toggle-lock/${playerPetId}`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('锁定状态已切换', 'success');
        await loadMyPets();
    } catch (e) {
        moduleManager.showToast('操作失败: ' + e.message, 'error');
    }
};

window.releasePet = async function(playerPetId) {
    if (!confirm('确定要放生这只宠物吗？此操作不可撤销！')) return;
    try {
        const res = await api.delete(`/pets/release/${playerPetId}`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('宠物已放生', 'info');
        await loadMyPets();
    } catch (e) {
        moduleManager.showToast('放生失败: ' + e.message, 'error');
    }
};

window.capturePet = async function(petId) {
    try {
        const res = await api.post(`/pets/capture/${petId}`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('捕捉成功！获得新宠物！', 'success');
        await loadMyPets();
        await loadAvailablePets();
    } catch (e) {
        moduleManager.showToast('捕捉失败: ' + e.message, 'error');
    }
};

// ==================== 技能系统辅助函数 ====================

window.switchSkillTab = function(tab) {
    document.querySelectorAll('#skills-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.skillTab === tab);
    });
    document.getElementById('skills-learned-panel').style.display = (tab === 'learned') ? '' : 'none';
    document.getElementById('skills-available-panel').style.display = (tab === 'available') ? '' : 'none';
    if (tab === 'learned') loadMySkills();
    if (tab === 'available') loadAvailableSkills();
};

window.loadMySkills = async function() {
    const container = document.getElementById('mySkillsList');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载技能...</p></div>';
    try {
        const res = await api.get('/skills/player');
        if (!res.success) throw new Error(res.message);
        const skills = res.data || [];
        renderMySkills(skills);
        updateSkillStats(skills);
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

function renderMySkills(skills) {
    const container = document.getElementById('mySkillsList');
    if (!container) return;
    if (skills.length === 0) {
        container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">您还没有学会任何技能</div>';
        return;
    }
    container.innerHTML = skills.map(skill => {
        const elementColors = { FIRE: '#e74c3c', WATER: '#3498db', GRASS: '#27ae60', THUNDER: '#f1c40f', ICE: '#00bcd4', DARK: '#9b59b6', LIGHT: '#f39c12', PHYSICAL: '#95a5a6' };
        const elementColor = elementColors[skill.elementType] || '#aaa';
        const isEquipped = skill.equippedSlot != null;
        return `
            <div class="skill-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${isEquipped ? elementColor : 'rgba(255,255,255,0.1)'};">
                <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                        <span class="skill-icon" style="font-size:1.3rem;">${getSkillEmoji(skill.elementType)}</span>
                        <div>
                            <h4 class="font-semibold">${escapeHtml(skill.name)}</h4>
                            <span class="text-xs" style="color:${elementColor};">${skill.elementTypeName || skill.elementType || '物理'}</span>
                        </div>
                    </div>
                    ${isEquipped ? `<span class="text-xs px-2 py-1 rounded" style="background:rgba(46,204,113,0.2);color:#2ecc71;">已装备 #${skill.equippedSlot + 1}</span>` : ''}
                </div>
                <div class="text-sm text-muted mb-2">${escapeHtml(skill.description || '无描述')}</div>
                <div class="flex gap-2 text-xs text-muted mb-3">
                    <span>等级 ${skill.level || 1}</span>
                    <span>|</span>
                    <span>伤害 ${skill.damage || skill.baseDamage || 0}</span>
                    <span>|</span>
                    <span>冷却 ${skill.cooldown || 0}秒</span>
                </div>
                <div class="flex gap-2 flex-wrap mt-3">
                    ${!isEquipped ? `<button class="btn btn-sm btn-primary" onclick="equipSkill(${skill.id || skill.playerSkillId}, 0)"><i class="fa-solid fa-hand-sparkles"></i> 装备</button>` : ''}
                    ${isEquipped ? `<button class="btn btn-sm" onclick="unequipSkill(${skill.id || skill.playerSkillId})"><i class="fa-solid fa-hand"></i> 卸下</button>` : ''}
                    <button class="btn btn-sm" onclick="upgradeSkill(${skill.id || skill.playerSkillId})"><i class="fa-solid fa-arrow-up"></i> 升级</button>
                </div>
            </div>
        `;
    }).join('');
}

function getSkillEmoji(element) {
    const emojis = { FIRE: '🔥', WATER: '💧', GRASS: '🌿', THUNDER: '⚡', ICE: '❄️', DARK: '🌙', LIGHT: '☀️', PHYSICAL: '⚔️' };
    return emojis[element] || '⚔️';
}

function updateSkillStats(skills) {
    const el1 = document.getElementById('skill-points-value');
    const el2 = document.getElementById('equipped-skills-count');
    if (el1) el1.textContent = skills.filter(s => s.equippedSlot != null).length;
    if (el2) el2.textContent = skills.filter(s => s.equippedSlot != null).length;
}

window.loadAvailableSkills = async function() {
    const container = document.getElementById('skillsAvailableList');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载可学习技能...</p></div>';
    try {
        const res = await api.get('/skills/available');
        if (!res.success) throw new Error(res.message);
        const skills = res.data || [];
        if (skills.length === 0) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">当前没有可学习的技能</div>';
            return;
        }
        container.innerHTML = skills.map(skill => {
            const elementColors = { FIRE: '#e74c3c', WATER: '#3498db', GRASS: '#27ae60', THUNDER: '#f1c40f', ICE: '#00bcd4', DARK: '#9b59b6', LIGHT: '#f39c12', PHYSICAL: '#95a5a6' };
            const elementColor = elementColors[skill.elementType] || '#aaa';
            return `
                <div class="skill-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${elementColor};">
                    <div class="flex items-center gap-2 mb-2">
                        <span style="font-size:1.3rem;">${getSkillEmoji(skill.elementType)}</span>
                        <div>
                            <h4 class="font-semibold">${escapeHtml(skill.name)}</h4>
                            <span class="text-xs" style="color:${elementColor};">${skill.elementTypeName || skill.elementType || '物理'}</span>
                        </div>
                    </div>
                    <div class="text-sm text-muted mb-2">${escapeHtml(skill.description || '无描述')}</div>
                    <div class="text-xs text-muted mb-3">
                        等级需求 ${skill.requiredLevel || 1} | 伤害 ${skill.damage || skill.baseDamage || 0} | 冷却 ${skill.cooldown || 0}秒
                    </div>
                    <div class="text-sm mb-3">
                        <span class="text-muted">学习费用:</span>
                        <span class="font-bold" style="color:var(--accent-gold);"><i class="fa-solid fa-gem"></i> ${skill.price || 0}</span>
                    </div>
                    <button class="btn btn-sm w-full btn-primary" onclick="learnSkill(${skill.id})">
                        <i class="fa-solid fa-graduation-cap"></i> 学习技能
                    </button>
                </div>
            `;
        }).join('');
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

window.learnSkill = async function(skillId) {
    try {
        const res = await api.post(`/skills/learn/${skillId}`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('技能学习成功！', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        await loadMySkills();
        await loadAvailableSkills();
    } catch (e) {
        moduleManager.showToast('学习失败: ' + e.message, 'error');
    }
};

window.equipSkill = async function(playerSkillId, slotNumber) {
    try {
        const res = await api.post(`/skills/equip/${playerSkillId}/${slotNumber}`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('技能装备成功！', 'success');
        await loadMySkills();
    } catch (e) {
        moduleManager.showToast('装备失败: ' + e.message, 'error');
    }
};

window.unequipSkill = async function(playerSkillId) {
    try {
        const res = await api.post(`/skills/unequip/${playerSkillId}`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('技能已卸下', 'success');
        await loadMySkills();
    } catch (e) {
        moduleManager.showToast('卸下失败: ' + e.message, 'error');
    }
};

window.upgradeSkill = async function(playerSkillId) {
    try {
        const res = await api.post(`/skills/${playerSkillId}/upgrade`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('技能升级成功！', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        await loadMySkills();
    } catch (e) {
        moduleManager.showToast('升级失败: ' + e.message, 'error');
    }
};

// ==================== 仙界人物（NPC）辅助函数 ====================

window.switchNarrativeTab = function(tab) {
    document.querySelectorAll('#narrative-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.narrativeTab === tab);
    });
    document.getElementById('narrative-npc-panel').style.display = (tab === 'npc') ? '' : 'none';
    document.getElementById('narrative-relations-panel').style.display = (tab === 'relations') ? '' : 'none';
    if (tab === 'npc') loadNpcList();
    if (tab === 'relations') loadNpcRelations();
};

window.loadNpcList = async function() {
    const container = document.getElementById('npcList');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载NPC...</p></div>';
    try {
        const res = await api.get('/npc/list');
        if (!res.success) throw new Error(res.message);
        const npcs = res.data || [];
        if (npcs.length === 0) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">暂无NPC数据</div>';
            return;
        }
        container.innerHTML = npcs.map(npc => {
            const typeIcons = { MERCHANT: '💰', QUEST_GIVER: '📜', TRAINER: '⚔️', QUEST: '📜', ELDER: '🧙', BOSS: '👹', NORMAL: '👤' };
            const icon = typeIcons[npc.npcType] || '👤';
            return `
                <div class="npc-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);cursor:pointer;" onclick="showNpcDetail(${npc.id})">
                    <div class="flex items-center gap-3 mb-2">
                        <span style="font-size:2rem;">${icon}</span>
                        <div>
                            <h4 class="font-semibold">${escapeHtml(npc.name || '神秘人物')}</h4>
                            <span class="text-xs text-muted">${npc.npcTypeName || npc.npcType || 'NPC'}</span>
                        </div>
                    </div>
                    <div class="text-sm text-muted">${escapeHtml(npc.description || '一位神秘的修仙者')}</div>
                </div>
            `;
        }).join('');
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

window.showNpcDetail = async function(npcId) {
    try {
        const res = await api.get(`/npc/${npcId}`);
        if (!res.success) throw new Error(res.message);
        const npc = res.data;
        alert(`【${npc.name}】\n\n${npc.description || '无描述'}\n\n${npc.dailyDialogue ? '日常对话: ' + npc.dailyDialogue : ''}`);
    } catch (e) {
        moduleManager.showToast('加载NPC详情失败: ' + e.message, 'error');
    }
};

window.loadNpcRelations = async function() {
    const container = document.getElementById('npcRelationsList');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载关系...</p></div>';
    try {
        const res = await api.get('/npc/relations');
        if (!res.success) throw new Error(res.message);
        const relations = res.data || [];
        if (relations.length === 0) {
            container.innerHTML = '<div class="empty-state">您还没有与任何NPC建立关系</div>';
            return;
        }
        container.innerHTML = relations.map(rel => {
            const relationColors = { HOSTILE: '#e74c3c', NEUTRAL: '#95a5a6', FRIENDLY: '#27ae60', ALLIED: '#3498db' };
            const color = relationColors[rel.relation] || '#95a5a6';
            return `
                <div class="relation-item p-4 rounded" style="background:rgba(255,255,255,0.05);border-left:3px solid ${color};">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center gap-2">
                            <h4 class="font-semibold">${escapeHtml(rel.npcName || '神秘人物')}</h4>
                            <span class="text-xs px-2 py-1 rounded" style="background:${color}22;color:${color};">${rel.relationName || rel.relation}</span>
                        </div>
                        <span class="text-sm text-muted">好感度 ${rel.affinity || 0}</span>
                    </div>
                </div>
            `;
        }).join('');
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

// ==================== 传说图鉴辅助函数 ====================

window.switchLoreTab = function(tab) {
    document.querySelectorAll('#lore-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.loreTab === tab);
    });
    loadLoreEntries(tab);
};

window.loadLoreEntries = async function(filter = 'all') {
    const container = document.getElementById('loreContent');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载图鉴...</p></div>';
    try {
        // 获取进度
        const progressRes = await api.get('/lore/progress');
        if (progressRes.success) {
            const prog = progressRes.data;
            const el1 = document.getElementById('lore-progress-text');
            const el2 = document.getElementById('lore-progress-bar');
            if (el1) el1.textContent = `${prog.discoveredCount || 0}/${prog.totalCount || 0}`;
            if (el2) el2.style.width = `${prog.discoveredCount && prog.totalCount ? (prog.discoveredCount / prog.totalCount * 100) : 0}%`;
        }
        
        // 获取条目
        let entriesRes;
        if (filter === 'discovered') {
            entriesRes = await api.get('/lore/discovered');
        } else {
            entriesRes = await api.get('/lore/entries');
        }
        if (!entriesRes.success) throw new Error(entriesRes.message);
        let entries = entriesRes.data || [];
        
        // 过滤
        if (filter === 'hidden') {
            entries = entries.filter(e => !e.isDiscovered);
        }
        
        if (entries.length === 0) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">暂无图鉴条目</div>';
            return;
        }
        
        container.innerHTML = entries.map(entry => {
            const opacity = entry.isDiscovered ? '' : 'opacity:0.4;filter:grayscale(1);';
            return `
                <div class="lore-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);${opacity}">
                    <div class="flex items-center gap-2 mb-2">
                        <span class="text-2xl">${entry.icon || '📖'}</span>
                        <h4 class="font-semibold">${escapeHtml(entry.title || '未知条目')}</h4>
                    </div>
                    <div class="text-sm text-muted">${entry.isDiscovered ? escapeHtml(entry.description || '无描述') : '???'}</div>
                    <div class="text-xs text-muted mt-2">类型: ${entry.category || '通用'}</div>
                </div>
            `;
        }).join('');
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

// ==================== 技能连招辅助函数 ====================

window.switchComboTab = function(tab) {
    document.querySelectorAll('#combos-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.comboTab === tab);
    });
    loadCombos(tab === 'available');
};

async function loadCombos(availableOnly = true) {
    const container = document.getElementById('comboContent');
    const totalEl = document.getElementById('combo-total-count');
    const masteredEl = document.getElementById('combo-mastered-count');
    const rateEl = document.getElementById('combo-use-rate');
    if (!container) return;
    
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载连招...</p></div>';
    try {
        const endpoint = availableOnly ? '/skills/combos/available' : '/skills/combos/all';
        const res = await api.get(endpoint);
        if (!res.success) throw new Error(res.message);
        const combos = res.data || [];
        
        // 获取统计
        const statsRes = await api.get('/skills/combos/stats');
        if (statsRes.success && statsRes.data) {
            const stats = statsRes.data;
            if (totalEl) totalEl.textContent = stats.totalAvailable || combos.length;
            if (masteredEl) masteredEl.textContent = stats.masteredCount || 0;
            if (rateEl) rateEl.textContent = stats.usageRate || '0%';
        }
        
        if (combos.length === 0) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">暂无连招数据</div>';
            return;
        }
        
        container.innerHTML = combos.map(combo => {
            const isActive = combo.isAvailable || combo.isMastered;
            return `
                <div class="combo-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${isActive ? 'var(--accent-gold)' : 'rgba(255,255,255,0.1)'};">
                    <div class="flex items-center justify-between mb-2">
                        <h4 class="font-semibold">${escapeHtml(combo.name || '连招')}</h4>
                        <span class="text-xs px-2 py-1 rounded" style="background:${isActive ? 'rgba(212,175,55,0.2)' : 'rgba(255,255,255,0.1)'};color:${isActive ? 'var(--accent-gold)' : 'var(--text-muted)'};">
                            ${combo.isMastered ? '已掌握' : combo.isAvailable ? '可用' : '未解锁'}
                        </span>
                    </div>
                    <div class="text-sm text-muted mb-2">${escapeHtml(combo.description || '无描述')}</div>
                    <div class="text-xs text-muted mb-3">
                        技能序列: ${(combo.skillSequence || []).map(s => escapeHtml(s)).join(' → ')}
                    </div>
                    <div class="text-xs text-muted">
                        伤害加成: ${combo.damageMultiplier ? (combo.damageMultiplier * 100).toFixed(0) : 100}%
                    </div>
                </div>
            `;
        }).join('');
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
}

// ==================== 宠物进化辅助函数 ====================

window.loadEvolutionInfo = async function() {
    const select = document.getElementById('evolution-pet-select');
    const container = document.getElementById('petEvolutionList');
    if (!select || !container) return;
    
    const petId = select.value;
    if (!petId) {
        container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">请选择要进化的宠物</div>';
        return;
    }
    
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>检查进化条件...</p></div>';
    try {
        const res = await api.get(`/pets/evolution/info/${petId}`);
        if (!res.success) throw new Error(res.message);
        const info = res.data;
        
        if (!info.canEvolve) {
            container.innerHTML = `
                <div class="evolution-info p-4 rounded" style="grid-column:1/-1;background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                    <div class="text-center mb-4">
                        <span style="font-size:3rem;">${info.currentIcon || '🐾'}</span>
                        <span style="font-size:2rem;margin:0 10px;">→</span>
                        <span style="font-size:3rem;">${info.evolutionIcon || '❓'}</span>
                    </div>
                    <h4 class="text-center font-bold mb-2">${escapeHtml(info.currentPetName || '宠物')} → ${escapeHtml(info.evolutionPetName || '进化形态')}</h4>
                    <div class="text-sm text-muted text-center mb-4">${info.currentQuality || '普通'} → ${info.evolutionQuality || '未知'}</div>
                    <div class="text-sm text-red-400 text-center">
                        ${info.reason || '暂不满足进化条件'}
                    </div>
                </div>
            `;
        } else {
            container.innerHTML = `
                <div class="evolution-ready p-4 rounded" style="grid-column:1/-1;background:rgba(46,204,113,0.05);border:1px solid rgba(46,204,113,0.3);">
                    <div class="text-center mb-4">
                        <span style="font-size:3rem;">${info.currentIcon || '🐾'}</span>
                        <span style="font-size:2rem;margin:0 10px;color:#2ecc71;">→</span>
                        <span style="font-size:3rem;">${info.evolutionIcon || '✨'}</span>
                    </div>
                    <h4 class="text-center font-bold mb-2" style="color:#2ecc71;">${escapeHtml(info.currentPetName || '宠物')} → ${escapeHtml(info.evolutionPetName || '进化形态')}</h4>
                    <div class="text-sm text-muted text-center mb-4">${info.currentQuality || '普通'} → ${info.evolutionQuality || '优秀'}</div>
                    <div class="mb-3">
                        <div class="text-sm mb-2">进化条件:</div>
                        <div class="text-xs text-muted mb-1">等级: ${info.currentLevel || 1} / ${info.requiredLevel || 10}</div>
                        ${info.requiredItems ? `<div class="text-xs text-muted">材料: ${info.requiredItems.map(i => `${i.name} x${i.count}`).join(', ')}</div>` : ''}
                    </div>
                    <button class="btn btn-lg w-full" style="background:#2ecc71;color:#fff;" onclick="doEvolution(${petId})">
                        <i class="fa-solid fa-wand-magic-sparkles"></i> 开始进化
                    </button>
                </div>
            `;
        }
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

window.doEvolution = async function(petId) {
    if (!confirm('确定要进化这只宠物吗？进化成功后宠物将获得新的形态！')) return;
    try {
        const res = await api.post(`/pets/evolution/evolve/${petId}`);
        if (!res.success) throw new Error(res.message);
        const result = res.data;
        if (result.isSuccess) {
            moduleManager.showToast(`进化成功！恭喜获得 ${result.newPetName || '新形态'}！`, 'success');
        } else {
            moduleManager.showToast(`进化失败: ${result.message || '材料不足'}`, 'error');
        }
        await loadEvolutionInfo();
        if (window.loadMyPets) await window.loadMyPets();
    } catch (e) {
        moduleManager.showToast('进化失败: ' + e.message, 'error');
    }
};

// ==================== 世界地图辅助函数 ====================

window.switchMapTab = function(tab) {
    document.querySelectorAll('#map-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.mapTab === tab);
    });
    document.getElementById('map-explore-panel').style.display = (tab === 'explore') ? '' : 'none';
    document.getElementById('map-list-panel').style.display = (tab === 'list') ? '' : 'none';
    if (tab === 'explore') loadCurrentMap();
    if (tab === 'list') loadMapList();
};

window.loadCurrentMap = async function() {
    const infoEl = document.getElementById('current-map-info');
    const exploreBtn = document.getElementById('explore-btn');
    if (!infoEl) return;
    
    try {
        const res = await api.get('/maps/current');
        if (res.success && res.data) {
            const map = res.data;
            infoEl.style.display = '';
            infoEl.innerHTML = `
                <div class="flex items-center gap-4">
                    <span style="font-size:2rem;">${map.icon || '🗺️'}</span>
                    <div class="flex-1">
                        <h3 class="font-bold">${escapeHtml(map.name || '未知地图')}</h3>
                        <div class="text-sm text-muted">${escapeHtml(map.description || '')}</div>
                    </div>
                    <span class="text-xs px-3 py-1 rounded" style="background:${map.mapType === 'SAFE' ? 'rgba(46,204,113,0.2)' : 'rgba(231,76,60,0.2)'};color:${map.mapType === 'SAFE' ? '#2ecc71' : '#e74c3c'};">
                        ${map.mapType === 'SAFE' ? '安全区' : '危险区'}
                    </span>
                </div>
                ${map.monsterLevel ? `<div class="text-xs text-muted mt-2">怪物等级: ${map.monsterLevel}</div>` : ''}
            `;
            if (exploreBtn) {
                exploreBtn.disabled = map.mapType === 'SAFE';
                if (map.mapType === 'SAFE') {
                    exploreBtn.innerHTML = '<i class="fa-solid fa-shield-halved"></i> 安全区无法探索';
                } else {
                    exploreBtn.disabled = false;
                    exploreBtn.innerHTML = '<i class="fa-solid fa-compass"></i> 开始探索';
                }
            }
        } else {
            infoEl.style.display = 'none';
            if (exploreBtn) {
                exploreBtn.disabled = true;
                exploreBtn.innerHTML = '<i class="fa-solid fa-map-location-dot"></i> 请先进入地图';
            }
        }
    } catch (e) {
        infoEl.style.display = 'none';
    }
};

window.loadMapList = async function() {
    const container = document.getElementById('mapList');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载地图...</p></div>';
    try {
        const res = await api.get('/maps');
        if (!res.success) throw new Error(res.message);
        const maps = res.data || [];
        if (maps.length === 0) {
            container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">暂无地图数据</div>';
            return;
        }
        container.innerHTML = maps.map(map => {
            const isCurrent = map.isCurrent || false;
            const isLocked = map.isLocked || false;
            return `
                <div class="map-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid ${isCurrent ? 'var(--accent-gold)' : 'rgba(255,255,255,0.1)'};">
                    <div class="flex items-center gap-2 mb-2">
                        <span style="font-size:1.5rem;">${map.icon || '🗺️'}</span>
                        <div class="flex-1">
                            <h4 class="font-semibold">${escapeHtml(map.name || '未知')}</h4>
                            <span class="text-xs text-muted">Lv.${map.requiredLevel || 1}+</span>
                        </div>
                        ${isCurrent ? '<span class="text-xs px-2 py-1 rounded" style="background:rgba(212,175,55,0.2);color:var(--accent-gold);">当前</span>' : ''}
                        ${isLocked ? '<i class="fa-solid fa-lock text-muted"></i>' : ''}
                    </div>
                    <div class="text-xs text-muted mb-3">${escapeHtml(map.description || '无描述')}</div>
                    <button class="btn btn-sm w-full ${isCurrent ? '' : 'btn-primary'}" 
                            onclick="enterMap(${map.id})"
                            ${isCurrent || isLocked ? 'disabled' : ''}>
                        ${isCurrent ? '当前所在' : isLocked ? '等级不足' : '进入'}
                    </button>
                </div>
            `;
        }).join('');
    } catch (e) {
        container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
    }
};

window.enterMap = async function(mapId) {
    try {
        const res = await api.post(`/maps/enter/${mapId}`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast(`已进入地图！`, 'success');
        await loadCurrentMap();
        await loadMapList();
    } catch (e) {
        moduleManager.showToast('进入地图失败: ' + e.message, 'error');
    }
};

window.exploreMap = async function() {
    const btn = document.getElementById('explore-btn');
    if (btn) { btn.disabled = true; btn.textContent = '探索中...'; }
    try {
        const res = await api.get('/maps/explore');
        if (!res.success) throw new Error(res.message);
        const encounter = res.data;
        moduleManager.showToast(`遭遇 ${encounter.monsterName || '怪物'}！`, 'info');
        // 可以跳转到战斗界面
        if (confirm(`遭遇了 ${encounter.monsterName || '怪物'}！开始战斗？`)) {
            showModule('combat');
        }
    } catch (e) {
        moduleManager.showToast('探索失败: ' + e.message, 'error');
    } finally {
        await loadCurrentMap();
    }
};

// ==================== 每日签到辅助函数 ====================

window.checkInSystem = {
    currentMonth: new Date(),
    
    init: async function() {
        await this.loadStatus();
    },
    
    loadStatus: async function() {
        try {
            const res = await api.get('/checkin/status');
            if (!res.success) throw new Error(res.message);
            const status = res.data;
            this.renderCalendar(status);
            this.updateStats(status);
            
            // 更新签到按钮
            const btn = document.getElementById('checkin-btn');
            if (btn) {
                if (status.todayChecked) {
                    btn.disabled = true;
                    btn.innerHTML = '<i class="fa-solid fa-check"></i> 今日已签到';
                } else {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="fa-solid fa-calendar-check"></i> 今日签到';
                }
            }
        } catch (e) {
            moduleManager.showToast('加载签到状态失败: ' + e.message, 'error');
        }
    },
    
    renderCalendar: function(status) {
        const grid = document.getElementById('checkin-days-grid');
        const title = document.getElementById('checkin-month-title');
        if (!grid) return;
        
        const now = this.currentMonth;
        if (title) title.textContent = `${now.getFullYear()}年${now.getMonth() + 1}月`;
        
        const year = now.getFullYear();
        const month = now.getMonth();
        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const today = new Date().getDate();
        const checkedDays = status.checkedDays || [];
        
        let html = '';
        // 空白
        for (let i = 0; i < firstDay; i++) {
            html += '<div></div>';
        }
        // 日期
        for (let d = 1; d <= daysInMonth; d++) {
            const isToday = d === today && now.getMonth() === new Date().getMonth() && now.getFullYear() === new Date().getFullYear();
            const isChecked = checkedDays.includes(d);
            const dayNames = ['ATTACK', 'DEFENSE', 'HP', 'ATTACK', 'DEFENSE', 'HP', 'ATTACK'];
            const rewardIcons = { ATTACK: '⚔️', DEFENSE: '🛡️', HP: '❤️' };
            const rewardType = dayNames[(d - 1) % 7];
            
            html += `
                <div class="checkin-day ${isToday ? 'today' : ''} ${isChecked ? 'checked' : ''}" 
                     style="aspect-ratio:1;display:flex;flex-direction:column;align-items:center;justify-content:center;border-radius:8px;
                            background:${isChecked ? 'rgba(46,204,113,0.15)' : isToday ? 'rgba(212,175,55,0.15)' : 'rgba(255,255,255,0.03)'};
                            border:1px solid ${isChecked ? 'rgba(46,204,113,0.3)' : isToday ? 'rgba(212,175,55,0.3)' : 'transparent'};">
                    <span class="text-sm ${isToday ? 'font-bold' : ''}" style="color:${isToday ? 'var(--accent-gold)' : 'var(--text-light)'};">${d}</span>
                    ${isChecked ? `<span class="text-xs" style="font-size:0.8rem;">✓</span>` : `<span class="text-xs" style="opacity:0.5;">${rewardIcons[rewardType]}</span>`}
                </div>
            `;
        }
        grid.innerHTML = html;
    },
    
    updateStats: function(status) {
        const el1 = document.getElementById('checkin-consecutive-days');
        const el2 = document.getElementById('checkin-total-days');
        const el3 = document.getElementById('checkin-today-reward');
        if (el1) el1.textContent = status.consecutiveDays || 0;
        if (el2) el2.textContent = status.totalDays || 0;
        if (el3) el3.textContent = status.todayReward?.spiritStones || 0;
    }
};

window.changeCheckinMonth = function(delta) {
    checkInSystem.currentMonth.setMonth(checkInSystem.currentMonth.getMonth() + delta);
    checkInSystem.loadStatus();
};

window.doCheckIn = async function() {
    const btn = document.getElementById('checkin-btn');
    if (btn) { btn.disabled = true; btn.textContent = '签到中...'; }
    try {
        const res = await api.post('/checkin/do');
        if (!res.success) throw new Error(res.message);
        const result = res.data;
        moduleManager.showToast(result.message || '签到成功！', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        await checkInSystem.loadStatus();
    } catch (e) {
        moduleManager.showToast('签到失败: ' + e.message, 'error');
        if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fa-solid fa-calendar-check"></i> 今日签到'; }
    }
};

// ==================== 成就系统辅助函数 ====================

window.achievementPanel = {
    currentFilter: 'all',
    
    init: async function() {
        await this.loadAchievements();
        await this.loadProgress();
    },
    
    loadProgress: async function() {
        try {
            const res = await api.get('/achievement/progress');
            if (!res.success) return;
            const prog = res.data;
            const el1 = document.getElementById('achievement-completed');
            const el2 = document.getElementById('achievement-claimed');
            const el3 = document.getElementById('achievement-total');
            const el4 = document.getElementById('achievement-rate');
            if (el1) el1.textContent = prog.completedCount || 0;
            if (el2) el2.textContent = prog.claimedCount || 0;
            if (el3) el3.textContent = prog.totalCount || 0;
            if (el4) el4.textContent = prog.completionRate || '0%';
        } catch (e) {
            console.error('加载成就进度失败:', e);
        }
    },
    
    loadAchievements: async function() {
        const container = document.getElementById('achievementsList');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载成就...</p></div>';
        try {
            const res = await api.get('/achievement/list');
            if (!res.success) throw new Error(res.message);
            let achievements = res.data || [];
            
            // 过滤
            if (this.currentFilter !== 'all') {
                achievements = achievements.filter(a => 
                    a.achievementType?.toLowerCase() === this.currentFilter.toLowerCase()
                );
            }
            
            if (achievements.length === 0) {
                container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">暂无成就数据</div>';
                return;
            }
            
            container.innerHTML = achievements.map(ach => {
                const isCompleted = ach.isCompleted;
                const isClaimed = ach.isClaimed;
                const progress = ach.progress || 0;
                const target = ach.conditionValue || 1;
                const pct = Math.min(100, Math.round(progress / target * 100));
                const typeColors = { LEVEL: '#3498db', COMBAT: '#e74c3c', CULTIVATION: '#27ae60', COLLECTION: '#9b59b6' };
                const typeColor = typeColors[ach.achievementType] || '#95a5a6';
                
                return `
                    <div class="achievement-card p-4 rounded ${isCompleted ? 'completed' : ''}" 
                         style="background:rgba(255,255,255,0.05);border:1px solid ${isCompleted ? '#2ecc71' : 'rgba(255,255,255,0.1)'};">
                        <div class="flex items-start gap-3 mb-2">
                            <span style="font-size:1.5rem;${!isCompleted ? 'opacity:0.5;filter:grayscale(1);' : ''}">${ach.icon || '🏆'}</span>
                            <div class="flex-1">
                                <h4 class="font-semibold">${escapeHtml(ach.name || '成就')}</h4>
                                <span class="text-xs px-2 py-1 rounded" style="background:${typeColor}22;color:${typeColor};">${ach.achievementTypeName || ach.achievementType || '其他'}</span>
                            </div>
                            ${isCompleted && !isClaimed ? 
                                `<button class="btn btn-sm" style="background:#2ecc71;color:#fff;" onclick="claimAchievement(${ach.id})">领取</button>` : 
                                isClaimed ? '<span class="text-xs text-green-400">已领取</span>' : ''}
                        </div>
                        <div class="text-sm text-muted mb-2">${escapeHtml(ach.description || '无描述')}</div>
                        <div class="flex items-center gap-2 mb-1">
                            <div class="flex-1 bg-white/10 rounded-full h-2">
                                <div class="h-2 rounded-full" style="width:${pct}%;background:${isCompleted ? '#2ecc71' : 'var(--accent-gold)'};"></div>
                            </div>
                            <span class="text-xs text-muted">${progress}/${target}</span>
                        </div>
                        <div class="flex gap-3 text-xs text-muted">
                            ${ach.rewardExp ? `<span>经验 +${ach.rewardExp}</span>` : ''}
                            ${ach.rewardSpiritStones ? `<span style="color:var(--accent-gold);"><i class="fa-solid fa-gem"></i> +${ach.rewardSpiritStones}</span>` : ''}
                            ${ach.rewardTitle ? `<span>称号: ${ach.rewardTitle}</span>` : ''}
                        </div>
                    </div>
                `;
            }).join('');
        } catch (e) {
            container.innerHTML = `<div class="empty-state">加载失败: ${e.message}</div>`;
        }
    }
};

window.switchAchievementTab = function(tab) {
    document.querySelectorAll('#achievements-module .tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.achievementTab === tab);
    });
    achievementPanel.currentFilter = tab;
    achievementPanel.loadAchievements();
};

window.claimAchievement = async function(id) {
    try {
        const res = await api.post(`/achievement/${id}/claim`);
        if (!res.success) throw new Error(res.message);
        moduleManager.showToast('奖励领取成功！', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        await achievementPanel.init();
    } catch (e) {
        moduleManager.showToast('领取失败: ' + e.message, 'error');
    }
};
