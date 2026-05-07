import { loreService } from './LoreService.js';

function escapeText(value) {
    return window.escapeHtml ? window.escapeHtml(value) : String(value ?? '');
}

export class LoreUI {
    constructor() {
        this.currentFilter = 'all';
    }

    async init() {
        return this.loadEntries(this.currentFilter);
    }

    async switchTab(filter) {
        this.currentFilter = filter;
        document.querySelectorAll('#lore-module .tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.loreTab === filter);
        });
        return this.loadEntries(filter);
    }

    async loadEntries(filter = this.currentFilter) {
        const container = document.getElementById('loreContent');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载图鉴中...</p></div>';
        try {
            const [progress, entries] = await Promise.all([
                loreService.getProgress(),
                loreService.getEntries(filter)
            ]);
            const el1 = document.getElementById('lore-progress-text');
            const el2 = document.getElementById('lore-progress-bar');
            if (el1) el1.textContent = `${progress.discoveredCount || 0}/${progress.totalCount || 0}`;
            if (el2) el2.style.width = `${progress.totalCount ? ((progress.discoveredCount || 0) / progress.totalCount) * 100 : 0}%`;
            if (!entries.length) {
                container.innerHTML = '<div class="empty-state">暂无图鉴条目</div>';
                return;
            }
            container.innerHTML = entries.map(entry => {
                const opacity = entry.isDiscovered ? '' : 'opacity:0.4;filter:grayscale(1);';
                return `
                    <div class="lore-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);${opacity}">
                        <div class="flex items-center gap-2 mb-2">
                            <span class="text-2xl">${entry.icon || '📖'}</span>
                            <h4 class="font-semibold">${escapeText(entry.title || '未知条目')}</h4>
                        </div>
                        <div class="text-sm text-muted">${entry.isDiscovered ? escapeText(entry.description || '暂无描述') : '???'}</div>
                        <div class="text-xs text-muted mt-2">类型: ${escapeText(entry.category || '通用')}</div>
                    </div>
                `;
            }).join('');
        } catch (error) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }
}

export const loreUI = new LoreUI();
