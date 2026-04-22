import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class AchievementService {
    async getAchievements() {
        const response = await gameAPI.getAchievements();
        if (!response?.success) throw new Error(response?.message || '加载成就失败');
        return response.data || [];
    }

    async getProgress() {
        const response = await gameAPI.getAchievementProgress();
        if (!response?.success) throw new Error(response?.message || '加载成就统计失败');
        return response.data || {};
    }

    async claimAchievement(achievementId) {
        const response = await gameAPI.claimAchievement(achievementId);
        if (!response?.success) throw new Error(response?.message || '领取失败');
        return response.data;
    }
}

export const achievementService = new AchievementService();
