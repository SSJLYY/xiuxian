import { gameAPI } from '../../core/api/GameApi.js';

export class CultivateService {
    async getProfile() {
        const response = await gameAPI.getCurrentPlayerProfile();
        if (!response.success) throw new Error(response.message || '获取玩家资料失败');
        return response.data;
    }

    async startCultivation() {
        const profile = await this.getProfile();
        if (profile.isCultivating) {
            return { alreadyCultivating: true, profile };
        }
        const response = await gameAPI.startCultivation();
        if (!response.success) throw new Error(response.message || '开始修炼失败');
        return { alreadyCultivating: false, data: response.data };
    }

    async stopCultivation() {
        const profile = await this.getProfile();
        if (!profile.isCultivating) {
            return { alreadyStopped: true, profile };
        }
        const response = await gameAPI.stopCultivation();
        if (!response.success) throw new Error(response.message || '停止修炼失败');
        return response.data || {};
    }

    async claimOfflineRewards() {
        const response = await gameAPI.claimOfflineRewards();
        if (!response.success) throw new Error(response.message || '领取离线奖励失败');
        const reward = response.data;
        if (!reward?.hasReward) {
            return reward;
        }
        if (!reward.rewardId) {
            throw new Error('离线奖励记录缺失，无法领取');
        }
        const claimResponse = await gameAPI.claimOfflineRewardById(reward.rewardId);
        if (!claimResponse.success) throw new Error(claimResponse.message || '领取离线奖励失败');
        return { ...reward, claimResult: claimResponse.data || {} };
    }

    async resetCultivation() {
        const response = await gameAPI.resetCultivation();
        if (!response.success) throw new Error(response.message || '重置修炼状态失败');
        return response.data;
    }

    formatOutcomeToast(title, core, extra = '') {
        return extra ? `${title} | ${core} | ${extra}` : `${title} | ${core}`;
    }
}

export const cultivateService = new CultivateService();
