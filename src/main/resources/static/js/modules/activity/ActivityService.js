/**
 * 活动模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class ActivityService {
    constructor() {
        this.activities = [];
        this.myActivities = [];
    }

    async getActivities() {
        try {
            const response = await gameAPI.activity.getList();
            if (response.success) {
                this.activities = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载活动列表失败: ' + error.message);
            throw error;
        }
    }

    async getMyActivities() {
        try {
            const response = await gameAPI.activity.getMyActivities();
            if (response.success) {
                this.myActivities = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载我的活动失败: ' + error.message);
            throw error;
        }
    }

    async participateActivity(activityId) {
        try {
            const response = await gameAPI.activity.participate(activityId);
            if (response.success) {
                toast.success('参与活动成功!');
                await this.getActivities();
                await this.getMyActivities();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('参与活动失败: ' + error.message);
            throw error;
        }
    }

    async claimReward(activityId) {
        try {
            const response = await gameAPI.activity.claimReward(activityId);
            if (response.success) {
                toast.success('领取奖励成功!');
                await this.getActivities();
                await this.getMyActivities();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('领取奖励失败: ' + error.message);
            throw error;
        }
    }

    getActivityById(activityId) {
        return this.activities.find(a => a.id === activityId) || null;
    }
}

export const activityService = new ActivityService();
