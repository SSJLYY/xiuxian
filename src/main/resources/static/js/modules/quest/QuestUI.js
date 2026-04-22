import { questService } from './QuestService.js';

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

export class QuestUI {
    constructor() {
        this.currentTab = 'daily';
    }

    async init() {
        return this.switchTab(this.currentTab);
    }

    async switchTab(tab) {
        this.currentTab = tab;
        document.querySelectorAll('#quests-module .tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.questTab === tab);
        });
        const list = document.getElementById('questsList') || document.getElementById('questList');
        if (!list) return;
        list.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载中...</p></div>';
        try {
            const quests = await questService.getQuestsByTab(tab);
            this.renderQuestList(list, quests, tab);
        } catch (error) {
            list.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    renderQuestList(container, quests, tab) {
        if (!quests.length) {
            container.innerHTML = '<div class="empty-state">暂无任务</div>';
            this.updateStats([]);
            return;
        }
        const normalized = quests.map(q => ({
            quest: q.quest || q,
            completed: !!q.completed,
            rewardClaimed: !!q.rewardClaimed,
            currentProgress: q.currentProgress || 0,
            id: q.id || q.quest?.id
        }));
        const typeLabel = { daily: '每日', weekly: '每周', monthly: '每月', main: '主线' }[tab] || '';
        container.innerHTML = normalized.map(q => {
            const prog = Math.min(q.currentProgress || 0, q.quest.requiredAmount || 1);
            const pct = Math.round((prog / (q.quest.requiredAmount || 1)) * 100);
            return `
                <div class="quest-item ${q.completed ? 'completed' : ''}" style="background:rgba(255,255,255,0.05);padding:12px;border-radius:8px;">
                    <div class="flex items-center justify-between mb-2">
                        <div class="font-semibold">${escapeText(q.quest.title || q.quest.name || '任务')} <span class="text-xs text-muted">[${typeLabel}]</span></div>
                        ${q.completed && !q.rewardClaimed ? `<button class="btn btn-primary btn-sm" onclick="claimQuest(${q.id})">领取奖励</button>` : q.completed ? `<span class="text-green-400 text-sm">已完成</span>` : ''}
                    </div>
                    <div class="text-sm text-muted mb-2">${escapeText(q.quest.description || '')}</div>
                    <div class="flex items-center gap-2 mb-1">
                        <div class="flex-1 bg-white/10 rounded-full h-2"><div class="bg-accent h-2 rounded-full" style="width:${pct}%"></div></div>
                        <span class="text-xs text-muted">${prog}/${q.quest.requiredAmount || 1}</span>
                    </div>
                    <div class="flex gap-4 text-xs text-muted"><span>奖励：经验 ${q.quest.rewardExp || 0}，灵石 ${q.quest.rewardSpiritStones || 0}</span></div>
                </div>
            `;
        }).join('');
        this.updateStats(normalized);
    }

    updateStats(normalized) {
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

    async claimQuest(questId) {
        await questService.claimQuest(questId);
        showToast('任务奖励领取成功', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        return this.switchTab(this.currentTab);
    }
}

export const questUI = new QuestUI();
