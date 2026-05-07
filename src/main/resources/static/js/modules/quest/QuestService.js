import { gameAPI } from '../../core/api/GameApi.js';

export class QuestService {
    async getQuestsByTab(tab) {
        const mapping = {
            daily: () => gameAPI.getDailyQuests(),
            weekly: () => gameAPI.getWeeklyQuests(),
            monthly: () => gameAPI.getMonthlyQuests(),
            main: () => gameAPI.getQuests()
        };
        const response = await (mapping[tab] || mapping.daily)();
        if (!response?.success) {
            throw new Error(response?.message || '加载任务失败');
        }
        return response.data || [];
    }

    async claimQuest(questId) {
        const response = await gameAPI.claimQuestReward(questId);
        if (!response?.success) {
            throw new Error(response?.message || '领取奖励失败');
        }
        return response.data;
    }
}

export const questService = new QuestService();
