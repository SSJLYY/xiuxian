/**
 * 任务模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class QuestService {
    constructor() {
        this.quests = [];
        this.myQuests = [];
    }

    async getQuestList(type = 'all') {
        try {
            const response = await gameAPI.quest.getList(type);
            if (response.success) {
                this.quests = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载任务列表失败: ' + error.message);
            throw error;
        }
    }

    async getMyQuests() {
        try {
            const response = await gameAPI.quest.getMyQuests();
            if (response.success) {
                this.myQuests = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载我的任务失败: ' + error.message);
            throw error;
        }
    }

    async acceptQuest(questId) {
        try {
            const response = await gameAPI.quest.accept(questId);
            if (response.success) {
                toast.success('接受任务成功!');
                await Promise.all([
                    this.getQuestList(),
                    this.getMyQuests()
                ]);
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('接受任务失败: ' + error.message);
            throw error;
        }
    }

    async completeQuest(questId) {
        try {
            const response = await gameAPI.quest.complete(questId);
            if (response.success) {
                toast.success('完成任务成功!');
                await Promise.all([
                    this.getQuestList(),
                    this.getMyQuests()
                ]);
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('完成任务失败: ' + error.message);
            throw error;
        }
    }

    async claimReward(questId) {
        try {
            const response = await gameAPI.quest.claim(questId);
            if (response.success) {
                toast.success('领取奖励成功!');
                await Promise.all([
                    this.getQuestList(),
                    this.getMyQuests()
                ]);
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('领取奖励失败: ' + error.message);
            throw error;
        }
    }

    getQuestById(questId) {
        return this.quests.find(q => q.id === questId) || null;
    }
}

export const questService = new QuestService();
