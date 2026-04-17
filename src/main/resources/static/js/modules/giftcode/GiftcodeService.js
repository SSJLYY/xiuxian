/**
 * 兑换码模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class GiftcodeService {
    constructor() {
        this.myCodes = [];
    }

    async redeemCode(code) {
        try {
            const response = await gameAPI.redeemGiftcode(code);
            if (response.success) {
                toast.success('兑换成功!');
                await this.getMyCodes();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('兑换失败: ' + error.message);
            throw error;
        }
    }

    async getMyCodes() {
        try {
            const response = await gameAPI.getCheckinStatus();
            if (response.success) {
                this.myCodes = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载兑换记录失败: ' + error.message);
            throw error;
        }
    }

    async getAvailableCodes() {
        try {
            const response = await gameAPI.getActivities();
            if (response.success) {
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载可用兑换码失败: ' + error.message);
            throw error;
        }
    }
}

export const giftcodeService = new GiftcodeService();
