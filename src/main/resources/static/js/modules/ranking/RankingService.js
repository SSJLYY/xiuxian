import { gameAPI } from '../../core/api/GameApi.js';

export class RankingService {
    async getRanking(type = 'level') {
        const mapping = {
            level: () => gameAPI.getLevelRanking(),
            power: () => gameAPI.getPowerRanking(),
            wealth: () => gameAPI.getWealthRanking(),
            pet: () => gameAPI.getPetRanking()
        };
        const response = await (mapping[type] || mapping.level)();
        if (!response?.success) throw new Error(response?.message || '加载排行榜失败');
        return response.data || [];
    }

    async getMyRank(type = 'level') {
        const response = await gameAPI.getMyRanking(type);
        if (!response?.success) return null;
        return response.data || null;
    }
}

export const rankingService = new RankingService();
