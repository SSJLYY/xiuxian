/**
 * 签到模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class CheckinService {
    constructor() {
        this.checkinStatus = null;
        this.calendarData = null;
    }

    async getCheckinStatus() {
        try {
            const response = await gameAPI.getCheckinStatus();
            if (response.success) {
                this.checkinStatus = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取签到状态失败: ' + error.message);
            throw error;
        }
    }

    async getCalendarData(month, year) {
        try {
            const response = await gameAPI.getCheckinCalendar(month, year);
            if (response.success) {
                this.calendarData = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取日历数据失败: ' + error.message);
            throw error;
        }
    }

    async doCheckin() {
        try {
            const response = await gameAPI.doCheckin();
            if (response.success) {
                toast.success('签到成功!');
                await this.getCheckinStatus();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('签到失败: ' + error.message);
            throw error;
        }
    }

    async getCheckinRewards() {
        try {
            const response = await gameAPI.getCheckinRewards();
            if (response.success) {
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取奖励列表失败: ' + error.message);
            throw error;
        }
    }
}

export const checkinService = new CheckinService();
