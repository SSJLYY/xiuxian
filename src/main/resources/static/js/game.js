// 游戏管理器 - 修复版，完全使用真实API，禁止降级
class GameManager {
    constructor() {
        this.isInitialized = false;
    }

    // 初始化 - 只在认证后调用
    async init() {
        if (this.isInitialized) return;
        await this.loadQuests();
        await this.loadSkillShop();
        await this.loadInventory();
        await this.loadShopItems();
        await this.loadQuestTabs();
        this.isInitialized = true;
    }

    async loadSkillShop() {
        try {
            const res = await gameAPI.getSkillShop();
            if (!res || !res.success) return;
            const list = document.getElementById('skillsList');
            if (!list) return;
            list.innerHTML = '';
            const items = res.data || [];
            if (items.length === 0) {
                list.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;color:#888;">暂无可购买技能</div>';
                return;
            }
            const playerLevel = parseInt(document.getElementById('playerLevel')?.textContent) || 1;
            const playerSS = parseInt(document.getElementById('playerSpiritStones')?.textContent) || 0;
            items.forEach(it => {
                const canBuy = playerSS >= it.price && playerLevel >= (it.requiredLevel || 1);
                const el = document.createElement('div');
                el.className = 'skill-shop-item';
                el.style.cssText = 'background:rgba(255,255,255,0.05);padding:15px;border-radius:10px;border:1px solid rgba(255,255,255,0.1);';
                el.innerHTML = `
                    <div class="font-semibold mb-1"><i class="fa-solid fa-wand-magic-sparkles"></i> ${escapeHtml(it.skillName || '技能#'+it.skillId)}</div>
                    <div class="flex items-center gap-3 text-sm text-muted mb-2">
                        <span style="color:var(--accent-gold);"><i class="fa-solid fa-gem"></i> ${it.price}</span>
                        <span class="text-xs">要求等级: ${it.requiredLevel || 1}</span>
                    </div>
                    <button class="btn btn-sm w-full ${canBuy ? 'btn-primary' : 'btn-disabled'}" 
                            data-shop="${escapeHtml(String(it.id))}"
                            ${!canBuy ? 'disabled' : ''}>
                        ${!canBuy ? '等级不足或灵石不够' : '购买并学习'}
                    </button>
                `;
                list.appendChild(el);
            });
            list.querySelectorAll('button[data-shop]').forEach(btn => {
                btn.addEventListener('click', async (e) => {
                    if (btn.disabled) return;
                    const id = e.currentTarget.getAttribute('data-shop');
                    const orig = btn.textContent;
                    btn.disabled = true; btn.textContent = '学习中...';
                    try {
                        const r = await gameAPI.buySkill(id);
                        if (!r || !r.success) throw new Error(r?.message||'购买失败');
                        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
                        btn.textContent = '学习成功';
                        setTimeout(() => { btn.textContent = orig; btn.disabled = false; }, 1000);
                        this.showToast('技能学习成功！','success');
                    } catch(err){ 
                        btn.textContent = '学习失败';
                        setTimeout(() => { btn.textContent = orig; btn.disabled = false; }, 1000);
                        this.showToast('学习失败: '+err.message,'error'); 
                    }
                });
            });
        } catch (error) { console.error('[Shop] 技能商店加载失败:', error); }
    }

    async loadShopItems() {
        try {
            const res = await gameAPI.getShopItems('general');
            if (!res || !res.success) {
                throw new Error(res?.message || '获取商城失败');
            }
            const list = document.getElementById('shopList');
            if (!list) return;
            list.innerHTML = '';
            const items = res.data || [];
            if (items.length === 0) {
                list.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;color:#888;">商城暂无商品</div>';
                return;
            }
            items.forEach(it => {
                const inStock = (it.stock === null || it.stock === undefined || it.stock > 0);
                const el = document.createElement('div');
                el.className = 'shop-item';
                el.style.cssText = 'background:rgba(255,255,255,0.05);padding:15px;border-radius:10px;border:1px solid rgba(255,255,255,0.1);';
                el.innerHTML = `
                    <div class="font-semibold mb-1">${escapeHtml(it.itemName || '未知物品')}</div>
                    <div class="text-sm text-muted mb-2">${escapeHtml(it.description || '无描述')}</div>
                    <div class="flex items-center justify-between mt-2">
                        <div class="text-accent font-bold" style="color:var(--accent-gold);">
                            <i class="fa-solid fa-gem"></i> ${it.priceSpiritStones}
                        </div>
                        <div class="text-xs text-muted">库存: ${it.stock == null ? '无限' : it.stock}</div>
                    </div>
                    <button class="btn btn-primary btn-sm w-full mt-2" 
                            data-id="${escapeHtml(String(it.id))}"
                            ${!inStock ? 'disabled' : ''}>
                        ${!inStock ? '已售罄' : '购买'}
                    </button>
                `;
                list.appendChild(el);
            });
            list.querySelectorAll('button[data-id]').forEach(btn => {
                btn.addEventListener('click', async (e) => {
                    if (btn.disabled) return;
                    const id = e.currentTarget.getAttribute('data-id');
                    const origText = btn.textContent;
                    btn.disabled = true; btn.textContent = '购买中...';
                    try {
                        const r = await gameAPI.buyItem(id, 1);
                        if (!r || !r.success) throw new Error(r?.message || '购买失败');
                        await this.loadInventory();
                        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
                        btn.textContent = '购买成功';
                        setTimeout(() => { btn.textContent = origText; btn.disabled = false; }, 1000);
                        this.showToast('购买成功！','success');
                    } catch(err){ 
                        btn.textContent = '购买失败';
                        setTimeout(() => { btn.textContent = origText; btn.disabled = false; }, 1000);
                        this.showToast('购买失败: '+err.message,'error'); 
                    }
                });
            });
            // 更新灵石余额
            const ssEl = document.getElementById('shop-spirit-stones');
            if (ssEl) {
                const ss = document.getElementById('playerSpiritStones');
                ssEl.textContent = ss ? ss.textContent : '0';
            }
        } catch (e) { this.showToast('获取商城失败: '+e.message,'error'); throw e; }
    }

    async loadInventory() {
        try {
            const res = await gameAPI.getInventory();
            if (!res || !res.success) {
                throw new Error(res?.message || '获取背包失败');
            }
            const grid = document.getElementById('inventoryGrid');
            if (!grid) return;
            grid.innerHTML = '';
            const items = res.data || [];
            
            // 更新灵石余额
            const ssEl = document.getElementById('inventory-spirit-stones');
            const usedEl = document.getElementById('inventory-used');
            if (ssEl) {
                const ss = document.getElementById('playerSpiritStones');
                ssEl.textContent = ss ? ss.textContent : '0';
            }
            if (usedEl) usedEl.textContent = items.length;
            
            if (items.length === 0) {
                grid.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;color:#888;">背包空空如也</div>';
                return;
            }
            items.forEach(it => {
                const cell = document.createElement('div');
                cell.className = 'inventory-cell';
                cell.dataset.itemType = it.itemType || 'material';
                cell.innerHTML = `
                    <div class="inventory-item-name font-semibold text-sm mb-1">${escapeHtml(it.itemName || '未知物品')}</div>
                    <div class="inventory-item-qty text-accent text-xs">x${it.quantity}</div>
                    <div class="inventory-item-type text-xs text-muted mt-1">${getItemTypeName(it.itemType)}</div>
                `;
                grid.appendChild(cell);
            });
        } catch (e) { this.showToast('获取背包失败: '+e.message,'error'); throw e; }
    }

    async loadQuestTabs() {
        const list = document.getElementById('questsList');
        if (!list) return;
        try {
            const daily = await gameAPI.getDailyQuests();
            if (!daily?.success) throw new Error(daily?.message || '每日任务获取失败');
            // 默认显示每日任务
            const renderQuests = (data, type) => {
                list.innerHTML = '';
                const quests = data || [];
                quests.forEach(q => {
                    const prog = Math.min(q.currentProgress || 0, q.quest.requiredAmount || 1);
                    const pct = Math.round((prog / (q.quest.requiredAmount || 1)) * 100);
                    const done = !!q.completed;
                    const typeLabel = { daily: '每日', weekly: '每周', monthly: '每月', main: '主线' }[type] || type;
                    const item = document.createElement('div');
                    item.className = 'quest-item ' + (done ? 'completed' : '');
                    item.style.cssText = 'background:rgba(255,255,255,0.05);padding:12px;border-radius:8px;';
                    item.innerHTML = `
                        <div class="flex items-center justify-between mb-2">
                            <div class="font-semibold">${escapeHtml(q.quest.title)} <span class="text-xs text-muted">[${typeLabel}]</span></div>
                            ${done && !q.rewardClaimed 
                                ? `<button class="btn btn-primary btn-sm" data-qid="${q.id}">领取奖励</button>` 
                                : done 
                                    ? `<span class="text-green-400 text-sm">已完成</span>` 
                                    : ''}
                        </div>
                        <div class="text-sm text-muted mb-2">${escapeHtml(q.quest.description || '')}</div>
                        <div class="flex items-center gap-2 mb-1">
                            <div class="flex-1 bg-white/10 rounded-full h-2">
                                <div class="h-2 rounded-full" style="width:${pct}%;background:linear-gradient(90deg,#667eea,#764ba2);"></div>
                            </div>
                            <span class="text-xs text-muted">${prog}/${q.quest.requiredAmount}</span>
                        </div>
                        <div class="flex gap-4 text-xs text-muted">
                            <span>奖励：经验 ${q.quest.rewardExp||0}，灵石 ${q.quest.rewardSpiritStones||0}</span>
                        </div>
                    `;
                    list.appendChild(item);
                });
                if (quests.length === 0) {
                    list.innerHTML = '<div class="empty-state" style="text-align:center;padding:2rem;color:#888;">暂无任务</div>';
                }
                // 绑定领取按钮
                list.querySelectorAll('button[data-qid]').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const qid = btn.getAttribute('data-qid');
                        btn.disabled = true; btn.textContent = '领取中...';
                        try {
                            const r = await gameAPI.claimQuestReward(qid);
                            if (!r?.success) throw new Error(r?.message || '领取失败');
                            await this.loadQuestTabs();
                            if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
                            this.showToast('奖励领取成功！','success');
                        } catch(err) {
                            btn.disabled = false; btn.textContent = '领取';
                            this.showToast('领取失败: '+err.message,'error');
                        }
                    });
                });
                // 更新统计
                const completed = quests.filter(q => q.completed).length;
                const claimable = quests.filter(q => q.completed && !q.rewardClaimed).length;
                const el1 = document.getElementById('quest-completed-count');
                const el2 = document.getElementById('quest-claimable-count');
                const el3 = document.getElementById('quest-total-count');
                if (el1) el1.textContent = completed;
                if (el2) el2.textContent = claimable;
                if (el3) el3.textContent = quests.length;
            };
            renderQuests(daily.data, 'daily');
        } catch(e) { this.showToast('任务列表加载失败: '+e.message,'error'); throw e; }
    }

    async loadQuests() {
        try {
            const res = await gameAPI.getQuests();
            if (!res || !res.success) {
                throw new Error(res?.message || '获取任务失败');
            }
            const list = document.getElementById('questsList');
            if (!list) return;
            list.innerHTML = '';
            const quests = res.data || [];
            quests.forEach(q => {
                const item = document.createElement('div');
                item.className = 'quest-item';
                const progress = Math.min(q.currentProgress || 0, q.quest.requiredAmount || 1);
                const done = !!q.completed;
                item.innerHTML = `
                    <div class="quest-header">
                        <strong>${escapeHtml(q.quest.title)}</strong>
                        <span class="quest-type">${escapeHtml(q.quest.type)}</span>
                    </div>
                    <div class="quest-desc">${escapeHtml(q.quest.description || '')}</div>
                    <div class="quest-progress">进度：${progress}/${q.quest.requiredAmount}</div>
                    <div class="quest-reward">奖励：经验${q.quest.rewardExp}，灵石${q.quest.rewardSpiritStones}，贡献${q.quest.rewardContributionPoints}</div>
                    <div class="quest-actions">
                        ${done && !q.rewardClaimed ? `<button class="btn btn-primary btn-sm" data-id="${q.id}">领取奖励</button>` : `<span class="status">${done ? '已完成' : '未完成'}</span>`}
                    </div>
                `;
                list.appendChild(item);
            });
            list.querySelectorAll('button[data-id]').forEach(btn => {
                btn.addEventListener('click', async (e) => {
                    const id = e.currentTarget.getAttribute('data-id');
                    try {
                        const r = await gameAPI.claimQuestReward(id);
                        if (!r || !r.success) {
                            throw new Error(r?.message || '领取失败');
                        }
                        await this.loadQuests();
                        if (window.authManager && window.authManager.loadPlayerProfile) {
                            await window.authManager.loadPlayerProfile();
                        }
                        this.showToast('任务奖励领取成功', 'success');
                    } catch (err) {
                        this.showToast('任务奖励领取失败: ' + err.message, 'error');
                        throw err;
                    }
                });
            });
        } catch (error) {
            this.showToast('获取任务失败: ' + error.message, 'error');
            throw error;
        }
    }

    // 刷新玩家数据
    async refreshPlayerData() {
        try {
            if (window.authManager && window.authManager.loadPlayerProfile) {
                await window.authManager.loadPlayerProfile();
                console.log('玩家数据已刷新');
            }
        } catch (error) {
            console.error('刷新玩家数据失败:', error);
        }
    }

    // 添加修炼日志
    addCultivationLog(message) {
        this.showToast(message, 'info');
    }

    // 显示消息提示
    showToast(message, type = 'info', duration = 3000) {
        if (window.authManager && window.authManager.showToast) {
            window.authManager.showToast(message, type, duration);
        } else {
            const toast = document.createElement('div');
            const count = document.querySelectorAll('.toast-bubble').length;
            const bottom = 10 + count * 36;
            toast.className = `toast-bubble ${type}`;
            toast.textContent = message;
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
                wordWrap: 'break-word',
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
}

    // 游戏管理器实例（延迟初始化）
    let gameManager = null;

// 初始化游戏管理器（在认证成功后调用）
window.initGameManager = async function() {
    if (!gameManager) {
        gameManager = new GameManager();
        await gameManager.init();

        // 导出到全局作用域
        window.gameManager = gameManager;
    }
    return gameManager;
};

// 获取游戏管理器实例
window.getGameManager = function() {
    return gameManager;
};

// ============================================================================
// 战斗系统 - game.html 战斗模块专用
// ============================================================================

// 物品类型名称映射
function getItemTypeName(type) {
    const map = {
        EQUIPMENT: '装备', CONSUMABLE: '消耗品', MATERIAL: '材料',
        QUEST: '任务物品', GEM: '宝石', OTHER: '其他'
    };
    return map[type] || '其他';
}
