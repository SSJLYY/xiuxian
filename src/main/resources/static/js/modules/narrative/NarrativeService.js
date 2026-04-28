import { gameAPI } from '../../core/api/GameApi.js';

export class NarrativeService {
    normalizeNpc(npc) {
        return {
            ...npc,
            npcType: npc?.npcType || npc?.roleType || 'NPC',
            npcTypeName: npc?.npcTypeName || npc?.title || npc?.roleType || 'NPC',
            dailyDialogue: npc?.dailyDialogue || npc?.dailyDialogueText || ''
        };
    }

    async getNpcList() {
        const response = await gameAPI.get('/npc/list');
        if (!response?.success) throw new Error(response?.message || '加载 NPC 列表失败');
        return (response.data || []).map(npc => this.normalizeNpc(npc));
    }

    async getNpcDetail(npcId) {
        const response = await gameAPI.get(`/npc/${npcId}`);
        if (!response?.success) throw new Error(response?.message || '加载 NPC 详情失败');
        return response.data ? this.normalizeNpc(response.data) : null;
    }

    async getNpcRelations() {
        const response = await gameAPI.get('/npc/relations');
        if (!response?.success) throw new Error(response?.message || '加载 NPC 关系失败');
        return response.data || [];
    }

    async getLoreProgress() {
        const response = await gameAPI.get('/lore/progress');
        if (!response?.success) throw new Error(response?.message || '加载图鉴进度失败');
        return response.data || {};
    }

    async getLoreEntries(filter = 'all') {
        const endpoint = filter === 'discovered' ? '/lore/discovered' : '/lore/entries';
        const response = await gameAPI.get(endpoint);
        if (!response?.success) throw new Error(response?.message || '加载图鉴条目失败');
        const entries = (response.data || []).map(entry => ({
            ...entry,
            isDiscovered: Boolean(entry?.isDiscovered ?? entry?.discovered),
            description: entry?.description || entry?.content || ''
        }));
        return filter === 'hidden' ? entries.filter(entry => !entry.isDiscovered) : entries;
    }
}

export const narrativeService = new NarrativeService();
