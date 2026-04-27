import { gameAPI } from '../../core/api/GameApi.js';

export class RankingService {
    async getRanking(type = 'level') {
        if (type === 'pet') {
            throw new Error('宠物排行榜暂未开放');
        }
        const mapping = {
            level: () => gameAPI.getLevelRanking(),
            power: () => gameAPI.getPowerRanking(),
            wealth: () => gameAPI.getWealthRanking()
        };
        const response = await (mapping[type] || mapping.level)();
        if (!response?.success) throw new Error(response?.message || '加载排行榜失败');
        return response.data || [];
    }

    async getMyRank(type = 'level') {
        if (type === 'pet') {
            return null;
        }
        const response = await gameAPI.getMyRanking(type);
        if (!response?.success) return null;
        return response.data || null;
    }
}

export const rankingService = new RankingService();
