import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class GiftcodeService {
    constructor() {
        this.myCodes = [];
        this.availableCodes = [];
    }

    async redeemCode(code) {
        try {
            const response = await gameAPI.redeemGiftcode(code);
            if (!response?.success) {
                throw new Error(response?.message || '兑换失败');
            }
            toast.success('兑换成功');
            await Promise.all([this.getMyCodes(), this.getAvailableCodes()]);
            return response.data;
        } catch (error) {
            toast.error('兑换失败: ' + error.message);
            throw error;
        }
    }

    async getMyCodes() {
        try {
            const response = await gameAPI.getMyGiftcodes();
            if (!response?.success) {
                throw new Error(response?.message || '加载兑换记录失败');
            }
            this.myCodes = response.data || [];
            return this.myCodes;
        } catch (error) {
            toast.error('加载兑换记录失败: ' + error.message);
            throw error;
        }
    }

    async getAvailableCodes() {
        try {
            const response = await gameAPI.getAvailableGiftcodes();
            if (!response?.success) {
                throw new Error(response?.message || '加载可用礼包码失败');
            }
            this.availableCodes = response.data || [];
            return this.availableCodes;
        } catch (error) {
            toast.error('加载可用礼包码失败: ' + error.message);
            throw error;
        }
    }
}

export const giftcodeService = new GiftcodeService();
export default giftcodeService;
