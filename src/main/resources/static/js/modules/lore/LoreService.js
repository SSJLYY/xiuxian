import { gameAPI } from '../../core/api/GameApi.js';

export class LoreService {
    async getProgress() {
        const response = await gameAPI.get('/lore/progress');
        if (!response?.success) throw new Error(response?.message || '加载图鉴进度失败');
        return response.data || {};
    }

    async getEntries(filter = 'all') {
        const endpoint = filter === 'discovered' ? '/lore/discovered' : '/lore/entries';
        const response = await gameAPI.get(endpoint);
        if (!response?.success) throw new Error(response?.message || '加载图鉴条目失败');
        const entries = response.data || [];
        return filter === 'hidden' ? entries.filter(entry => !entry.isDiscovered) : entries;
    }
}

export const loreService = new LoreService();
