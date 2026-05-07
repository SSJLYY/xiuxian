import { narrativeService } from './NarrativeService.js';

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

function hasGameLayout() {
    return !!document.getElementById('narrative-module');
}

export class NarrativeUI {
    async init() {
        return hasGameLayout() ? this.initGameLayout() : this.initStandaloneLayout();
    }

    async initGameLayout() {
        await this.switchGameTab('npc');
    }

    async initStandaloneLayout() {
        const container = document.getElementById('chapterList');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载剧情中...</p></div>';
        try {
            const npcs = await narrativeService.getNpcList();
            container.innerHTML = npcs.length === 0
                ? '<div class="empty-state">暂无剧情人物</div>'
                : npcs.map(npc => `
                    <div class="narrative-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);margin-bottom:12px;">
                        <div class="font-semibold mb-1">${escapeText(npc.name || '神秘人物')}</div>
                        <div class="text-sm text-muted mb-2">${escapeText(npc.npcTypeName || npc.npcType || 'NPC')}</div>
                        <div class="text-sm">${escapeText(npc.description || '暂无描述')}</div>
                    </div>
                `).join('');
        } catch (error) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    async switchGameTab(tab) {
        document.querySelectorAll('#narrative-module .tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.narrativeTab === tab);
        });
        const npcPanel = document.getElementById('narrative-npc-panel');
        const relationPanel = document.getElementById('narrative-relations-panel');
        if (npcPanel) npcPanel.style.display = tab === 'npc' ? '' : 'none';
        if (relationPanel) relationPanel.style.display = tab === 'relations' ? '' : 'none';
        return tab === 'npc' ? this.loadNpcList() : this.loadNpcRelations();
    }

    async loadNpcList() {
        const container = document.getElementById('npcList');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载 NPC 中...</p></div>';
        try {
            const npcs = await narrativeService.getNpcList();
            if (npcs.length === 0) {
                container.innerHTML = '<div class="empty-state" style="grid-column:1/-1;text-align:center;padding:2rem;">暂无 NPC 数据</div>';
                return;
            }
            container.innerHTML = npcs.map(npc => {
                const typeIcons = { MERCHANT: '🛒', QUEST_GIVER: '📜', TRAINER: '⚔️', QUEST: '📜', ELDER: '🧙', BOSS: '👹', NORMAL: '👤' };
                const icon = typeIcons[npc.npcType] || '👤';
                return `
                    <div class="npc-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);cursor:pointer;" onclick="showNpcDetail(${npc.id})">
                        <div class="flex items-center gap-3 mb-2">
                            <span style="font-size:2rem;">${icon}</span>
                            <div>
                                <h4 class="font-semibold">${escapeText(npc.name || '神秘人物')}</h4>
                                <span class="text-xs text-muted">${escapeText(npc.npcTypeName || npc.npcType || 'NPC')}</span>
                            </div>
                        </div>
                        <div class="text-sm text-muted">${escapeText(npc.description || '一位神秘的修仙者')}</div>
                    </div>
                `;
            }).join('');
        } catch (error) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    async showNpcDetail(npcId) {
        try {
            const npc = await narrativeService.getNpcDetail(npcId);
            alert(`【${npc?.name || '神秘人物'}】\n\n${npc?.description || '暂无描述'}\n\n${npc?.dailyDialogue ? `日常对话: ${npc.dailyDialogue}` : ''}`);
        } catch (error) {
            showToast(`加载 NPC 详情失败: ${error.message}`, 'error');
        }
    }

    async loadNpcRelations() {
        const container = document.getElementById('npcRelationsList');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载关系中...</p></div>';
        try {
            const relations = await narrativeService.getNpcRelations();
            if (relations.length === 0) {
                container.innerHTML = '<div class="empty-state">您还没有与任何 NPC 建立关系</div>';
                return;
            }
            container.innerHTML = relations.map(rel => {
                const relationColors = { HOSTILE: '#e74c3c', NEUTRAL: '#95a5a6', FRIENDLY: '#27ae60', ALLIED: '#3498db' };
                const color = relationColors[rel.relation] || '#95a5a6';
                return `
                    <div class="relation-item p-4 rounded" style="background:rgba(255,255,255,0.05);border-left:3px solid ${color};">
                        <div class="flex items-center justify-between">
                            <div class="flex items-center gap-2">
                                <h4 class="font-semibold">${escapeText(rel.npcName || '神秘人物')}</h4>
                                <span class="text-xs px-2 py-1 rounded" style="background:${color}22;color:${color};">${escapeText(rel.relationName || rel.relation)}</span>
                            </div>
                            <span class="text-sm text-muted">好感度: ${rel.affinity || 0}</span>
                        </div>
                    </div>
                `;
            }).join('');
        } catch (error) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }
}

export const narrativeUI = new NarrativeUI();
