import { gameAPI } from '../../core/api/GameApi.js';

export class NarrativeService {
    async getNpcList() {
        const response = await gameAPI.get('/npc/list');
        if (!response?.success) throw new Error(response?.message || '加载NPC列表失败');
        return response.data || [];
    }

    async getNpcDetail(npcId) {
        const response = await gameAPI.get(`/npc/${npcId}`);
        if (!response?.success) throw new Error(response?.message || '加载NPC详情失败');
        return response.data || null;
    }

    async getNpcRelations() {
        const response = await gameAPI.get('/npc/relations');
        if (!response?.success) throw new Error(response?.message || '加载NPC关系失败');
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
        const entries = response.data || [];
        return filter === 'hidden' ? entries.filter(entry => !entry.isDiscovered) : entries;
    }
}

export const narrativeService = new NarrativeService();
