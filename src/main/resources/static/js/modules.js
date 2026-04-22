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
        if (window.loadMyPets) {
            await window.loadMyPets();
        }
    }

    async loadSkillsData() {
        console.log('加载技能数据');
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
        if (window.combatUI?.init) {
            window.combatUI.init();
        }
    }

    async loadGuildData() {
        console.log('加载宗门数据');
        if (window.guildUI?.init) {
            await window.guildUI.init();
        }
    }

    async loadRankingData() {
        console.log('加载排行榜数据');
        if (window.rankingUI?.init) {
            await window.rankingUI.init();
        }
    }

    async loadAchievementsData() {
        console.log('加载成就数据');
        if (window.achievementUI?.init) {
            await window.achievementUI.init();
        }
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
        if (window.narrativeUI?.init) {
            await window.narrativeUI.init();
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
        if (window.mapUI?.init) {
            await window.mapUI.init();
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
        if (window.achievementUI?.init) {
            await window.achievementUI.init();
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

// ==================== 宗门系统辅助函数（委托到模块化实现） ====================

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

// ==================== 宠物系统辅助函数（委托到模块化实现） ====================

function getPetsModule() {
    if (!window.petsUI) {
        throw new Error('宠物模块尚未初始化');
    }
    return window.petsUI;
}

window.switchPetTab = function(tab) {
    return getPetsModule().switchTab(tab);
};

window.loadMyPets = async function() {
    return getPetsModule().loadMyPets();
};

window.loadAvailablePets = async function() {
    return getPetsModule().loadAvailablePets();
};

window.activatePet = async function(playerPetId) {
    return getPetsModule().activatePet(playerPetId);
};

window.feedPet = async function(playerPetId) {
    return getPetsModule().feedPet(playerPetId);
};

window.trainPet = async function(playerPetId) {
    const types = ['普通训练', '强化训练', '特训'];
    const type = prompt('选择训练类型（普通训练/强化训练/特训）:');
    if (!types.includes(type?.trim())) {
        moduleManager.showToast('无效的训练类型', 'error');
        return;
    }
    return getPetsModule().trainPet(playerPetId, type.trim());
};

window.togglePetLock = async function(playerPetId) {
    return getPetsModule().togglePetLock(playerPetId);
};

window.releasePet = async function(playerPetId) {
    if (!confirm('确定要放生这只宠物吗？此操作不可撤销！')) return;
    return getPetsModule().releasePet(playerPetId);
};

window.capturePet = async function(petId) {
    return getPetsModule().capturePet(petId);
};

// ==================== 技能系统辅助函数（委托到模块化实现） ====================

function getSkillsModule() {
    if (!window.skillsUI) {
        throw new Error('技能模块尚未初始化');
    }
    return window.skillsUI;
}

window.switchSkillTab = function(tab) {
    return getSkillsModule().switchSkillTab(tab);
};

window.loadMySkills = async function() {
    return getSkillsModule().loadMySkills();
};

window.loadAvailableSkills = async function() {
    return getSkillsModule().loadAvailableSkills();
};

window.learnSkill = async function(skillId) {
    return getSkillsModule().learnSkill(skillId);
};

window.equipSkill = async function(playerSkillId, slotNumber) {
    return getSkillsModule().equipSkill(playerSkillId, slotNumber);
};

window.unequipSkill = async function(playerSkillId) {
    return getSkillsModule().unequipSkill(playerSkillId);
};

window.upgradeSkill = async function(playerSkillId) {
    return getSkillsModule().upgradeSkill(playerSkillId);
};

// ==================== 修炼/挂机辅助函数（委托到模块化实现） ====================

function getCultivateModule() {
    if (!window.cultivateUI) {
        throw new Error('修炼模块尚未初始化');
    }
    return window.cultivateUI;
}

window.startCultivation = async function() {
    return getCultivateModule().startCultivation();
};

window.stopCultivation = async function() {
    return getCultivateModule().stopCultivation();
};

window.toggleCultivation = async function() {
    return getCultivateModule().toggleCultivation();
};

window.claimOfflineRewards = async function() {
    return getCultivateModule().claimOfflineRewards();
};

window.resetCultivation = async function() {
    return getCultivateModule().resetCultivation();
};

// ==================== 仙界人物（NPC）辅助函数（委托到模块化实现） ====================

function getNarrativeModule() {
    if (!window.narrativeUI) {
        throw new Error('叙事模块尚未初始化');
    }
    return window.narrativeUI;
}

window.switchNarrativeTab = function(tab) {
    return getNarrativeModule().switchGameTab(tab);
};

window.loadNpcList = async function() {
    return getNarrativeModule().loadNpcList();
};

window.loadNpcRelations = async function() {
    return getNarrativeModule().loadNpcRelations();
};

window.showNpcDetail = async function(npcId) {
    return getNarrativeModule().showNpcDetail(npcId);
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

// ==================== 技能连招辅助函数（委托到模块化实现） ====================

window.switchComboTab = function(tab) {
    return getSkillsModule().switchComboTab(tab);
};

window.loadCombos = async function(availableOnly = true) {
    return getSkillsModule().loadCombos(availableOnly);
};

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

// ==================== 世界地图辅助函数（委托到模块化实现） ====================

function getMapModule() {
    if (!window.mapUI) {
        throw new Error('地图模块尚未初始化');
    }
    return window.mapUI;
}

window.switchMapTab = function(tab) {
    return getMapModule().switchGameTab(tab);
};

window.loadCurrentMap = async function() {
    return getMapModule().loadCurrentMap();
};

window.loadMapList = async function() {
    return getMapModule().loadMapList();
};

window.enterMap = async function(mapId) {
    return getMapModule().enterMap(mapId);
};

window.exploreMap = async function() {
    return getMapModule().exploreMap();
};

// ==================== 每日签到辅助函数 ====================

window.checkInSystem = {
    currentMonth: new Date(),
    
    init: async function() {
        await this.loadStatus();
    },
    
    loadStatus: async function() {
        try {
            const year = this.currentMonth.getFullYear();
            const month = this.currentMonth.getMonth() + 1;
            const res = await api.get(`/checkin/status?year=${year}&month=${month}`);
            if (!res.success) throw new Error(res.message);
            const status = res.data;
            this.renderCalendar(status);
            this.updateStats(status);
            
            // 更新签到按钮
            const btn = document.getElementById('checkin-btn');
            if (btn) {
                if (status.checkedToday) {
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
        const checkedDays = (status.calendar || [])
            .filter(cell => cell.checked)
            .map(cell => cell.day);
        
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
                    ${isChecked ? `<span class="text-xs checkin-mark">✓</span>` : `<span class="text-xs checkin-reward-icon">${rewardIcons[rewardType]}</span>`}
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
        if (el2) el2.textContent = status.totalCheckedThisMonth || 0;
        if (el3) el3.textContent = status.todayRewardStones || 0;
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
        moduleManager.showToast(res.message || '签到成功！', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        await checkInSystem.loadStatus();
    } catch (e) {
        moduleManager.showToast('签到失败: ' + e.message, 'error');
        if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fa-solid fa-calendar-check"></i> 今日签到'; }
    }
};

window.switchAchievementTab = function(tab) {
    if (!window.achievementUI) {
        throw new Error('成就模块尚未初始化');
    }
    return window.achievementUI.switchTab(tab);
};

window.claimAchievement = async function(id) {
    if (!window.achievementUI) {
        throw new Error('成就模块尚未初始化');
    }
    return window.achievementUI.claimAchievement(id);
};

window.switchRankingTab = function(tab) {
    if (!window.rankingUI) {
        throw new Error('排行榜模块尚未初始化');
    }
    return window.rankingUI.switchTab(tab);
};
