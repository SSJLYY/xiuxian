/**
 * VIP模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class VipService {
    constructor() {
        this.vipInfo = null;
        this.vipLevels = [];
    }

    async getVipInfo() {
        try {
            const response = await gameAPI.getVipInfo();
            if (response.success) {
                this.vipInfo = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取VIP信息失败: ' + error.message);
            throw error;
        }
    }

    async getVipLevels() {
        try {
            const response = await gameAPI.getVipLevels();
            if (response.success) {
                this.vipLevels = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取VIP等级失败: ' + error.message);
            throw error;
        }
    }

    async getVipBenefits() {
        try {
            const response = await gameAPI.getDailyVipReward();
            if (response.success) {
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取VIP特权失败: ' + error.message);
            throw error;
        }
    }

    async claimDailyReward() {
        try {
            const response = await gameAPI.getDailyVipReward();
            if (response.success) {
                toast.success('领取VIP每日奖励成功!');
                await this.getVipInfo();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('领取失败: ' + error.message);
            throw error;
        }
    }

    async recharge(amount) {
        try {
            const response = await gameAPI.rechargeVip(amount);
            if (response.success) {
                toast.success('充值成功!');
                await this.getVipInfo();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('充值失败: ' + error.message);
            throw error;
        }
    }
}

export const vipService = new VipService();
