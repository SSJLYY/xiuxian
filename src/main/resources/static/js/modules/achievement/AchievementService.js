import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class AchievementService {
    async getAchievements() {
        try {
            const response = await gameAPI.achievement.getList();
            if (response.success) return response.data;
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载成就失败: ' + error.message);
            throw error;
        }
    }

    async claimAchievement(achievementId) {
        try {
            const response = await gameAPI.achievement.claim(achievementId);
            if (response.success) {
                toast.success('领取成功');
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('领取失败: ' + error.message);
            throw error;
        }
    }
}

export const achievementService = new AchievementService();
