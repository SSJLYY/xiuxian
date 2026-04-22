import { gameAPI } from '../../core/api/GameApi.js';

export class CheckinService {
    async getStatus(year = null, month = null) {
        const response = await gameAPI.getCheckinStatus(year, month);
        if (!response?.success) throw new Error(response?.message || '加载签到状态失败');
        return response.data || {};
    }

    async doCheckin() {
        const response = await gameAPI.doCheckin();
        if (!response?.success) throw new Error(response?.message || '签到失败');
        return response;
    }
}

export const checkinService = new CheckinService();
