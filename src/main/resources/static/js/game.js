// 游戏管理器 - 修复版，完全使用真实API，禁止降级
class GameManager {
    constructor() {
        this.isCultivating = false;
        this.cultivationTimer = null;
        this.cultivationTime = 0;
        this.cycleDurationMs = 10000;
        this.cycleStart = null;
        this.cycleCount = 0;
        this.lastCultivationStart = null;
        this.isInitialized = false;
        this.dataRefreshTimer = null;
    }

    // 初始化 - 只在认证后调用
    async init() {
        if (this.isInitialized) return;
        
        this.bindEvents();
        await this.initCultivationStatus();
        await this.loadQuests();
        await this.loadSkillShop();
        await this.loadInventory();
        await this.loadShopItems();
        await this.loadQuestTabs();
        this.isInitialized = true;
    }

    // 初始化修炼状态 - 页面刷新时自动停止修炼
    async initCultivationStatus() {
        try {
            const profileResponse = await gameAPI.getCurrentPlayerProfile();
            console.log('初始化修炼状态响应:', profileResponse);
            
            if (!profileResponse.success) {
                throw new Error('获取玩家资料失败: ' + profileResponse.message);
            }
            
            const profile = profileResponse.data;
            console.log('初始化修炼状态数据:', profile);
            this.isCultivating = profile.isCultivating || false;
            
            console.log('后端修炼状态:', this.isCultivating);
            
            if (this.isCultivating) {
                // 页面刷新时自动停止修炼
                console.log('检测到玩家正在修炼中，自动停止修炼');
                try {
                    await this.stopCultivation();
                    console.log('自动停止修炼成功');
                } catch (stopError) {
                    console.error('自动停止修炼失败:', stopError);
                    
                    // 如果停止失败，尝试重置后端修炼状态
                    try {
                        console.log('尝试重置后端修炼状态');
                        await gameAPI.resetCultivation();
                        console.log('后端修炼状态重置成功');
                    } catch (resetError) {
                        console.error('重置后端修炼状态失败:', resetError);
                    }
                    
                    // 强制重置前端状态
                    this.isCultivating = false;
                    this.stopCultivationTimer();
                    this.cultivationTime = 0;
                    this.updateCultivationStatus('点击开始修炼');
                    
                    // 强制更新按钮状态
                    const button = document.getElementById('cultivation-btn');
                    if (button) {
                        button.innerHTML = '<i class="fas fa-play"></i> 开始修炼';
                        button.className = 'btn btn-success';
                        button.onclick = () => this.startCultivation();
                    }
                    
                    // 显示状态重置提示
                    this.showToast('修炼状态已重置，可以重新开始修炼', 'info');
                }
            } else {
                this.updateCultivationStatus('点击开始修炼');
            }
        } catch (error) {
            console.error('初始化修炼状态失败:', error);
            this.isCultivating = false;
            this.updateCultivationStatus('点击开始修炼');
        }
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

    // 绑定事件
    bindEvents() {
        const cultivationBtn = document.getElementById('cultivation-btn');
        if (cultivationBtn) {
            cultivationBtn.addEventListener('click', () => this.toggleCultivation());
        }
    }

    // 开始修炼 - 完全使用真实API
    async startCultivation() {
        try {
            // 先检查当前修炼状态
            const profileResponse = await gameAPI.getCurrentPlayerProfile();
            console.log('开始修炼前检查后端状态响应:', profileResponse);
            
            if (!profileResponse.success) {
                throw new Error('获取玩家资料失败: ' + profileResponse.message);
            }
            
            const profile = profileResponse.data;
            console.log('开始修炼前检查后端状态数据:', profile);
            console.log('开始修炼前检查后端状态:', profile.isCultivating);
            
            if (profile.isCultivating) {
                // 如果后端显示正在修炼，但前端状态不一致，强制同步
                console.log('后端显示正在修炼中，强制同步前端状态');
                this.isCultivating = true;
                this.startCultivationTimer();
                this.updateCultivationStatus('修炼中...');
                throw new Error('已经在修炼中');
            }

            const response = await gameAPI.startCultivation();

            if (!response.success) {
                throw new Error(response.message || '开始修炼失败');
            }

            this.isCultivating = true;
            this.cultivationTime = 0;
            this.lastCultivationStart = Date.now();

            this.startCultivationTimer();
            this.updateCultivationStatus('修炼中...');

            this.showToast('开始修炼成功', 'success');

        } catch (error) {
            console.error('开始修炼失败:', error);
            this.showToast('开始修炼失败: ' + error.message, 'error');
            throw error; // 禁止降级，直接报错
        }
    }

    // 停止修炼 - 完全使用真实API
    async stopCultivation() {
        try {
            // 先检查当前修炼状态
            const profileResponse = await gameAPI.getCurrentPlayerProfile();
            
            if (!profileResponse.success) {
                throw new Error('获取玩家资料失败: ' + profileResponse.message);
            }
            
            const profile = profileResponse.data;
            
            if (!profile.isCultivating) {
                this.isCultivating = false;
                this.stopCultivationTimer();
                this.cultivationTime = 0;
                this.updateCultivationStatus('点击开始修炼');
                console.log('玩家当前没有在修炼，直接更新状态');
                return;
            }

            const response = await gameAPI.stopCultivation();

            if (!response.success) {
                throw new Error(response.message || '停止修炼失败');
            }

            this.isCultivating = false;
            this.stopCultivationTimer();
            this.cultivationTime = 0;
            this.updateCultivationStatus('点击开始修炼');

            // 停止修炼后刷新玩家数据，显示经验变化
            if (window.authManager && window.authManager.loadPlayerProfile) {
                await window.authManager.loadPlayerProfile();
            }

            this.showToast('停止修炼成功', 'info');

        } catch (error) {
            console.error('停止修炼失败:', error);
            // 即使后端停止失败，也要强制更新前端状态
            this.isCultivating = false;
            this.stopCultivationTimer();
            this.cultivationTime = 0;
            this.updateCultivationStatus('点击开始修炼');
            
            this.showToast('停止修炼失败: ' + error.message, 'error');
            throw error; // 禁止降级，直接报错
        }
    }

    // 切换修炼状态
    async toggleCultivation() {
        if (this.isCultivating) {
            await this.stopCultivation();
        } else {
            await this.startCultivation();
        }
    }

    // 开始修炼计时器
    startCultivationTimer() {
        this.cycleStart = Date.now();
        this.cultivationTimer = setInterval(() => {
            const elapsed = Date.now() - this.cycleStart;
            const percent = Math.min(100, Math.floor((elapsed / this.cycleDurationMs) * 100));
            this.updateCycleProgress(percent);
            if (elapsed >= this.cycleDurationMs) {
                this.cycleStart = Date.now();
                this.cycleCount++;
                this.updateCultivationDisplay();
                this.refreshPlayerData();
            }
        }, 100);
    }

    // 停止修炼计时器
    stopCultivationTimer() {
        if (this.cultivationTimer) {
            clearInterval(this.cultivationTimer);
            this.cultivationTimer = null;
        }
        
        // 停止数据刷新计时器
        if (this.dataRefreshTimer) {
            clearInterval(this.dataRefreshTimer);
            this.dataRefreshTimer = null;
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

    // 更新修炼显示
    updateCultivationDisplay() {
        const statusElement = document.getElementById('cultivationStatus');
        if (statusElement) {
            statusElement.textContent = `修炼中... 小周期 ${this.cycleCount} 次`;
        }
    }

    updateCycleProgress(percent) {
        const bar = document.getElementById('expProgress');
        if (bar) bar.style.width = `${percent}%`;
    }

    // 更新修炼状态
    updateCultivationStatus(status) {
        const statusElement = document.getElementById('cultivationStatus');
        const button = document.getElementById('cultivation-btn');
        const timeElement = document.getElementById('cultivationTime');

        if (statusElement) {
            statusElement.textContent = status;
        }

        if (timeElement) {
            if (this.isCultivating) {
                timeElement.textContent = this.cultivationTime;
            } else {
                timeElement.textContent = '0';
            }
        }

        if (button) {
            if (this.isCultivating) {
                button.innerHTML = '<i class="fa-solid fa-stop"></i> 停止修炼';
                button.className = 'btn btn-danger';
                button.onclick = () => this.stopCultivation();
            } else {
                button.innerHTML = '<i class="fa-solid fa-play"></i> 开始修炼';
                button.className = 'btn btn-success';
                button.onclick = () => this.startCultivation();
            }
        }
    }

    // 添加修炼日志
    addCultivationLog(message) {
        this.showToast(message, 'info');
    }

    // 领取离线奖励 - 使用后端存在的API
    async claimOfflineRewards() {
        try {
            const response = await gameAPI.claimOfflineRewards();

            if (!response.success) {
                throw new Error(response.message || '领取离线奖励失败');
            }

            this.showToast('离线奖励领取成功', 'success');

            // 刷新玩家数据
            if (window.authManager) {
                await window.authManager.loadPlayerProfile();
            }

        } catch (error) {
            console.error('领取离线奖励失败:', error);
            this.showToast('领取离线奖励失败: ' + error.message, 'error');
            throw error; // 禁止降级，直接报错
        }
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
        
        // 设置全局函数
        window.startCultivation = () => gameManager.startCultivation();
        window.stopCultivation = () => gameManager.stopCultivation();
        window.toggleCultivation = () => gameManager.toggleCultivation();
        window.claimOfflineRewards = () => gameManager.claimOfflineRewards();
        window.resetCultivation = () => gameManager.resetCultivation();
        
        // 导出到全局作用域
        window.gameManager = gameManager;
    }
    return gameManager;
};

// 增加重置修炼状态方法
GameManager.prototype.resetCultivation = async function() {
    try {
        const response = await gameAPI.resetCultivation();
        if (!response.success) {
            throw new Error(response.message || '重置修炼状态失败');
        }
        this.isCultivating = false;
        this.stopCultivationTimer();
        this.cultivationTime = 0;
        this.updateCultivationStatus('点击开始修炼');
        await this.loadQuests();
        if (window.authManager && window.authManager.loadPlayerProfile) {
            await window.authManager.loadPlayerProfile();
        }
        this.showToast('修炼状态已重置', 'info');
    } catch (error) {
        console.error('重置修炼状态失败:', error);
        this.showToast('重置修炼状态失败: ' + error.message, 'error');
        throw error;
    }
};

// 获取游戏管理器实例
window.getGameManager = function() {
    return gameManager;
};

// ============================================================================
// 战斗系统 - game.html 战斗模块专用
// ============================================================================

// 战斗日志
let combatLogs = [];

function addCombatLog(message) {
    combatLogs.push({ message: message, timestamp: new Date() });
    if (combatLogs.length > 50) combatLogs.shift();
    updateCombatLogDisplay();
}

function updateCombatLogDisplay() {
    const logContainer = document.getElementById('combat-log-container');
    if (!logContainer) return;
    if (combatLogs.length === 0) {
        logContainer.innerHTML = '<p class="text-secondary italic">暂无战斗记录</p>';
        return;
    }
    logContainer.innerHTML = combatLogs.slice().reverse().map(log =>
        `<p>[${log.timestamp.toLocaleTimeString()}] ${log.message}</p>`
    ).join('');
    logContainer.scrollTop = logContainer.scrollHeight;
}

// 生成怪物
async function generateMonster() {
    try {
        const mapSelector = document.getElementById('combat-map-selector');
        const mapId = mapSelector ? parseInt(mapSelector.value) : 1;
        const response = await gameAPI.generateMonster(mapId);
        if (!response?.success) throw new Error(response?.message || '生成怪物失败');
        const monster = response.data;
        updateMonsterDisplay(monster);
        return monster;
    } catch (error) {
        addCombatLog(`<span class="text-red-600">生成怪物失败: ${error.message}</span>`);
        return null;
    }
}

// 更新怪物显示
function updateMonsterDisplay(monster) {
    if (!monster) return;
    const setText = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
    setText('combat-monster-name', monster.name);
    setText('combat-monster-level', `等级${monster.level}`);
    setText('combat-monster-type', monster.type);
    setText('combat-monster-health', monster.health);
    setText('combat-monster-attack', monster.attack);
    setText('combat-monster-defense', monster.defense);
    setText('combat-monster-speed', monster.speed);
    const healthBar = document.getElementById('combat-monster-health-bar');
    if (healthBar) healthBar.style.width = '100%';
}

// 战斗一次
async function fightOnce() {
    try {
        const mapSelector = document.getElementById('combat-map-selector');
        const mapId = mapSelector ? parseInt(mapSelector.value) : 1;
        const monsterResponse = await gameAPI.generateMonster(mapId);
        if (!monsterResponse?.success) throw new Error(monsterResponse?.message || '生成怪物失败');
        const monster = monsterResponse.data;
        addCombatLog(`遭遇 ${monster.name} (等级${monster.level} ${monster.type})`);
        const combatResponse = monster.id
            ? await gameAPI.startCombatWithMap(monster.id, mapId)
            : await gameAPI.startCombatGenerateWithMap(mapId);
        if (!combatResponse?.success) throw new Error(combatResponse?.message || '战斗失败');
        const result = combatResponse.data;
        if (result.result === 'WIN') {
            addCombatLog(`<span class="text-green-600">战斗胜利！获得经验: ${result.totalExpGained}, 灵石: ${result.totalSpiritStonesGained}</span>`);
            if (result.droppedEquipmentId) {
                addCombatLog(`<span class="text-blue-600">获得装备掉落!</span>`);
            }
        } else {
            addCombatLog(`<span class="text-red-600">战斗失败!</span>`);
        }
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
    } catch (error) {
        addCombatLog(`<span class="text-red-600">战斗失败: ${error.message}</span>`);
    }
}

// 战斗50次 / 100次
async function fight50Times() { await batchFight(50); }
async function fight100Times() { await batchFight(100); }

async function batchFight(times) {
    const btns = {
        once: document.getElementById('combat-fight-once-btn'),
        btn50: document.getElementById('combat-fight-50-btn'),
        btn100: document.getElementById('combat-fight-100-btn')
    };
    const mapSelector = document.getElementById('combat-map-selector');
    const mapId = mapSelector ? parseInt(mapSelector.value) : 1;
    Object.values(btns).forEach(b => { if (b) b.disabled = true; });
    const targetBtn = times === 50 ? btns.btn50 : btns.btn100;
    const origText = targetBtn ? targetBtn.innerHTML : '';
    if (targetBtn) targetBtn.innerHTML = '<i class="fa fa-spinner fa-spin mr-1"></i> 战斗中...';
    try {
        addCombatLog(`<span class="text-blue-600">开始连续战斗${times}次...</span>`);
        const response = await gameAPI.batchCombat(times, { mapId });
        if (!response?.success) throw new Error(response?.message || '批量战斗失败');
        const result = response.data;
        addCombatLog(`<span class="text-green-600">连续战斗结束！</span>`);
        addCombatLog(`总战斗次数: ${result.totalBattles}，胜利: ${result.wins}次，失败: ${result.losses}次`);
        addCombatLog(`胜率: ${(result.winRate * 100).toFixed(2)}%`);
        addCombatLog(`获得经验: ${result.totalExpGained}，灵石: ${result.totalSpiritStonesGained}`);
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
    } catch (error) {
        addCombatLog(`<span class="text-red-600">批量战斗失败: ${error.message}</span>`);
    } finally {
        Object.values(btns).forEach(b => { if (b) b.disabled = false; });
        if (targetBtn) targetBtn.innerHTML = origText;
    }
}

// 初始化战斗模块
window.initCombatModule = function() {
    generateMonster();
    const fightOnceBtn = document.getElementById('combat-fight-once-btn');
    const fight50Btn = document.getElementById('combat-fight-50-btn');
    const fight100Btn = document.getElementById('combat-fight-100-btn');
    if (fightOnceBtn) fightOnceBtn.addEventListener('click', fightOnce);
    if (fight50Btn) fight50Btn.addEventListener('click', fight50Times);
    if (fight100Btn) fight100Btn.addEventListener('click', fight100Times);
};

// 模块切换时重新绑定（使用 ModuleManager 的 showModule）
window._origShowModule = window.showModule;
window.showModule = function(moduleName) {
    if (window._origShowModule) window._origShowModule(moduleName);
    if (moduleName === 'combat') {
        setTimeout(() => window.initCombatModule(), 50);
    }
};

// 物品类型名称映射
function getItemTypeName(type) {
    const map = {
        EQUIPMENT: '装备', CONSUMABLE: '消耗品', MATERIAL: '材料',
        QUEST: '任务物品', GEM: '宝石', OTHER: '其他'
    };
    return map[type] || '其他';
}
