import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class RankingService {
    async getRanking(type = 'level') {
        try {
            const response = await gameAPI.getRanking(type);
            if (response.success) return response.data;
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载排行榜失败: ' + error.message);
            throw error;
        }
    }
}

export const rankingService = new RankingService();
