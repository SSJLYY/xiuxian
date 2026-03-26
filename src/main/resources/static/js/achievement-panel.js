/**
 * 成就系统 - game.html 内嵌版本
 * 提供成就徽章展示、分类过滤、进度追踪、徽章墙
 */

class AchievementPanel {
    constructor() {
        this.achievements = [];
        this.stats = null;
        this.currentFilter = 'all';
    }

    async init() {
        await Promise.all([this.loadAchievements(), this.loadStats()]);
        this.render();
    }

    async loadAchievements() {
        try {
            const res = await api.get('/achievement/list');
            if (res.success) this.achievements = res.data || [];
        } catch (e) { console.error('[Achievement] 加载成就列表失败:', e); }
    }

    async loadStats() {
        try {
            const res = await api.get('/achievement/progress');
            if (res.success) this.stats = res.data;
        } catch (e) { console.error('[Achievement] 加载统计失败:', e); }
    }

    render() {
        const container = document.getElementById('achievements-module');
        if (!container) return;

        const s = this.stats || {};
        const completed = s.completedCount || 0;
        const total = s.totalCount || this.achievements.length;
        const rate = total > 0 ? Math.round(completed / total * 100) : 0;

        container.innerHTML = `
            <div class="achievement-panel">
                <!-- 成就统计栏 -->
                <div class="achievement-stats-bar">
                    <div class="stat-card">
                        <span class="stat-num">${total}</span>
                        <span class="stat-desc">总成就</span>
                    </div>
                    <div class="stat-card completed">
                        <span class="stat-num">${completed}</span>
                        <span class="stat-desc">已完成</span>
                    </div>
                    <div class="stat-card claimable">
                        <span class="stat-num">${(s.completedCount || 0) - (s.claimedCount || 0)}</span>
                        <span class="stat-desc">可领取</span>
                    </div>
                    <div class="stat-card rate">
                        <span class="stat-num">${rate}%</span>
                        <span class="stat-desc">完成率</span>
                    </div>
                </div>

                <!-- 总进度条 -->
                <div class="achievement-global-progress">
                    <div class="progress-track">
                        <div class="progress-fill" style="width:${rate}%"></div>
                    </div>
                    <span class="progress-text">${completed}/${total}</span>
                </div>

                <!-- 成就徽章墙（已完成的） -->
                ${this.renderBadgeWall()}

                <!-- 筛选标签 -->
                <div class="achievement-filter-tabs">
                    ${['all','completed','unclaimed','LEVEL','COMBAT','CULTIVATION','COLLECTION'].map(f => `
                        <button class="filter-tab ${f === this.currentFilter ? 'active' : ''}"
                                onclick="achievementPanel.filterBy('${f}')">
                            ${this.getFilterLabel(f)}
                        </button>
                    `).join('')}
                </div>

                <!-- 成就列表 -->
                <div class="achievement-grid" id="achievementGrid">
                    ${this.renderAchievementGrid(this.getFilteredAchievements())}
                </div>
            </div>
        `;
    }

    renderBadgeWall() {
        const earned = this.achievements.filter(a => a.isCompleted);
        if (earned.length === 0) return '';

        return `
            <div class="badge-wall">
                <h4 class="badge-wall-title">🏆 已获得徽章 <span>(${earned.length})</span></h4>
                <div class="badge-wall-grid">
                    ${earned.map(a => `
                        <div class="achievement-badge ${a.isClaimed ? 'claimed' : 'unclaimed'}"
                             title="${escapeHtml(a.name)}: ${escapeHtml(a.description)}"
                             onclick="achievementPanel.showBadgeDetail(${a.id})">
                            <div class="badge-icon">${this.getAchievementEmoji(a.achievementType)}</div>
                            <div class="badge-name">${escapeHtml(a.name)}</div>
                            ${!a.isClaimed ? '<div class="badge-claim-dot"></div>' : ''}
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }

    renderAchievementGrid(list) {
        if (!list || list.length === 0) {
            return '<div class="empty-achievements">暂无成就数据</div>';
        }

        return list.map(a => {
            const pct = a.conditionValue > 0 ? Math.min(Math.round(a.progress / a.conditionValue * 100), 100) : 0;
            const statusCls = a.isClaimed ? 'claimed' : a.isCompleted ? 'completed' : 'ongoing';

            return `
                <div class="ach-card ${statusCls}">
                    <div class="ach-card-left">
                        <div class="ach-emoji ${statusCls}">${this.getAchievementEmoji(a.achievementType)}</div>
                    </div>
                    <div class="ach-card-body">
                        <div class="ach-name">${escapeHtml(a.name)}</div>
                        <div class="ach-desc">${escapeHtml(a.description)}</div>
                        <div class="ach-progress-row">
                            <div class="ach-progress-bar">
                                <div class="ach-progress-fill ${statusCls}" style="width:${pct}%"></div>
                            </div>
                            <span class="ach-progress-num">${a.progress || 0}/${a.conditionValue}</span>
                        </div>
                    </div>
                    <div class="ach-card-right">
                        <div class="ach-rewards">
                            ${a.rewardSpiritStones > 0 ? `<span class="ach-reward stones">💎${this.fmt(a.rewardSpiritStones)}</span>` : ''}
                            ${a.rewardExp > 0 ? `<span class="ach-reward exp">⭐${this.fmt(a.rewardExp)}</span>` : ''}
                            ${a.rewardTitle ? `<span class="ach-reward title">🎖️</span>` : ''}
                        </div>
                        ${a.isCompleted && !a.isClaimed ? `
                            <button class="ach-claim-btn" onclick="achievementPanel.claim(${a.id})">
                                领取
                            </button>
                        ` : a.isClaimed ? `
                            <span class="ach-claimed-badge">✓已领</span>
                        ` : `
                            <span class="ach-pct-badge">${pct}%</span>
                        `}
                    </div>
                </div>
            `;
        }).join('');
    }

    showBadgeDetail(id) {
        const a = this.achievements.find(x => x.id === id);
        if (!a) return;
        this.showToast(`${a.name}: ${a.description}`, 'info');
    }

    filterBy(filter) {
        this.currentFilter = filter;
        const grid = document.getElementById('achievementGrid');
        if (grid) {
            grid.innerHTML = this.renderAchievementGrid(this.getFilteredAchievements());
        }
        // 更新标签状态
        document.querySelectorAll('.filter-tab').forEach(btn => {
            btn.classList.toggle('active', btn.textContent.trim() === this.getFilterLabel(filter));
        });
    }

    getFilteredAchievements() {
        switch (this.currentFilter) {
            case 'completed': return this.achievements.filter(a => a.isCompleted);
            case 'unclaimed': return this.achievements.filter(a => a.isCompleted && !a.isClaimed);
            case 'LEVEL': case 'COMBAT': case 'CULTIVATION': case 'COLLECTION':
                return this.achievements.filter(a => a.achievementType === this.currentFilter);
            default: return this.achievements;
        }
    }

    async claim(id) {
        try {
            const res = await api.post(`/achievement/${id}/claim`);
            if (res.success) {
                this.showToast('成就奖励领取成功！', 'success');
                await this.loadAchievements();
                await this.loadStats();
                this.render();
                if (window.authManager?.loadPlayerProfile) window.authManager.loadPlayerProfile();
            } else {
                this.showToast(res.message || '领取失败', 'error');
            }
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    }

    getAchievementEmoji(type) {
        const map = { LEVEL: '⭐', COMBAT: '⚔️', CULTIVATION: '🌿', COLLECTION: '📦' };
        return map[type] || '🏆';
    }

    getFilterLabel(f) {
        const labels = {
            all: '全部', completed: '已完成', unclaimed: '可领取',
            LEVEL: '⭐等级', COMBAT: '⚔️战斗', CULTIVATION: '🌿修炼', COLLECTION: '📦收集'
        };
        return labels[f] || f;
    }

    fmt(n) {
        n = Number(n) || 0;
        return n >= 10000 ? (n / 10000).toFixed(1) + 'w' : n.toString();
    }

    showToast(msg, type) {
        if (window.authManager?.showToast) window.authManager.showToast(msg, type);
    }
}

const achievementPanel = new AchievementPanel();
window.achievementPanel = achievementPanel;
console.log('[Achievement] 成就面板已加载');
