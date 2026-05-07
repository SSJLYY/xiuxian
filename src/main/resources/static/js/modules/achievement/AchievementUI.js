import { achievementService } from './AchievementService.js';

function escapeText(value) {
    return window.escapeHtml ? window.escapeHtml(value) : String(value ?? '');
}

function showToast(message, type = 'info') {
    if (window.moduleManager?.showToast) {
        window.moduleManager.showToast(message, type);
        return;
    }
    if (window.authManager?.showToast) {
        window.authManager.showToast(message, type);
        return;
    }
    console.log(`[${type}] ${message}`);
}

function getTypeEmoji(type) {
    const map = {
        LEVEL: '⭐',
        COMBAT: '⚔️',
        CULTIVATION: '🌟',
        COLLECTION: '📚'
    };
    return map[type] || '🏆';
}

function hasGameLayout() {
    return !!document.getElementById('achievements-module');
}

export class AchievementUI {
    constructor() {
        this.achievements = [];
        this.progress = {};
        this.currentFilter = 'all';
    }

    async init() {
        await this.loadAchievements();
        return hasGameLayout() ? this.renderGameLayout() : this.renderStandaloneLayout();
    }

    async loadAchievements() {
        const [achievements, progress] = await Promise.all([
            achievementService.getAchievements(),
            achievementService.getProgress().catch(() => ({}))
        ]);
        this.achievements = achievements || [];
        this.progress = progress || {};
    }

    getFilteredAchievements(filter = this.currentFilter) {
        if (filter === 'all') {
            return this.achievements;
        }
        return this.achievements.filter(item => (item.achievementType || '').toLowerCase() === filter.toLowerCase());
    }

    renderGameLayout() {
        const completed = this.progress.completedCount || 0;
        const claimed = this.progress.claimedCount || 0;
        const total = this.progress.totalCount || this.achievements.length;
        const rate = total > 0 ? Math.round(completed / total * 100) : 0;
        const elCompleted = document.getElementById('achievement-completed');
        const elClaimed = document.getElementById('achievement-claimed');
        const elTotal = document.getElementById('achievement-total');
        const elRate = document.getElementById('achievement-rate');
        if (elCompleted) elCompleted.textContent = completed;
        if (elClaimed) elClaimed.textContent = claimed;
        if (elTotal) elTotal.textContent = total;
        if (elRate) elRate.textContent = `${rate}%`;
        this.renderAchievementList('achievementsList');
    }

    renderStandaloneLayout() {
        const completed = this.progress.completedCount || 0;
        const total = this.progress.totalCount || this.achievements.length;
        const elCompleted = document.getElementById('completedCount');
        const elTotal = document.getElementById('totalCount');
        if (elCompleted) elCompleted.textContent = completed;
        if (elTotal) elTotal.textContent = total;
        this.renderAchievementList('achievementList');
    }

    renderAchievementList(containerId) {
        const container = document.getElementById(containerId);
        if (!container) {
            return;
        }
        const list = this.getFilteredAchievements();
        if (list.length === 0) {
            container.innerHTML = '<div class="empty-state">暂无成就数据</div>';
            return;
        }
        container.innerHTML = list.map(item => {
            const target = item.conditionValue || 1;
            const progress = item.progress || 0;
            const pct = Math.min(100, Math.round(progress / target * 100));
            const canClaim = Boolean(item.canClaim) || (item.isCompleted && !item.isClaimed);
            return `
                <div class="achievement-card ${item.isCompleted ? 'completed' : ''}" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);padding:16px;border-radius:12px;">
                    <div class="flex items-center gap-3 mb-2">
                        <span style="font-size:1.5rem;">${getTypeEmoji(item.achievementType)}</span>
                        <div>
                            <h4 class="font-semibold">${escapeText(item.name)}</h4>
                            <div class="text-xs text-muted">${escapeText(item.achievementType || '成就')}</div>
                        </div>
                    </div>
                    <div class="text-sm text-muted mb-2">${escapeText(item.description)}</div>
                    <div class="text-xs text-muted mb-2">${progress}/${target}</div>
                    <div class="progress" style="height:8px;background:rgba(255,255,255,0.08);border-radius:999px;overflow:hidden;">
                        <div class="progress-bar" style="width:${pct}%;height:100%;background:var(--accent-gold);"></div>
                    </div>
                    ${canClaim ? `<button class="btn btn-primary mt-3" onclick="claimAchievement(${item.id})">领取</button>` : ''}
                </div>
            `;
        }).join('');
    }

    async switchTab(tab) {
        this.currentFilter = tab;
        this.renderGameLayout();
    }

    async claimAchievement(id) {
        await achievementService.claimAchievement(id);
        showToast('成就奖励领取成功', 'success');
        if (window.authManager?.loadPlayerProfile) {
            await window.authManager.loadPlayerProfile();
        }
        await this.loadAchievements();
        return hasGameLayout() ? this.renderGameLayout() : this.renderStandaloneLayout();
    }
}

export const achievementUI = new AchievementUI();
