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
            const response = await gameAPI.getAllActivities();
            if (!response?.success) {
                throw new Error(response?.message || '加载活动列表失败');
            }
            this.activities = response.data || [];
            return this.activities;
        } catch (error) {
            toast.error('加载活动列表失败: ' + error.message);
            throw error;
        }
    }

    async getMyActivities() {
        try {
            const response = await gameAPI.getMyActivityProgress();
            if (!response?.success) {
                throw new Error(response?.message || '加载我的活动失败');
            }
            this.myActivities = response.data || [];
            return this.myActivities;
        } catch (error) {
            toast.error('加载我的活动失败: ' + error.message);
            throw error;
        }
    }

    async participateActivity(activityId) {
        try {
            const response = await gameAPI.participateActivity(activityId);
            if (!response?.success) {
                throw new Error(response?.message || '参与活动失败');
            }
            toast.success('参与活动成功');
            await Promise.all([this.getActivities(), this.getMyActivities()]);
            return response.data;
        } catch (error) {
            toast.error('参与活动失败: ' + error.message);
            throw error;
        }
    }

    async claimReward(activityId) {
        try {
            const response = await gameAPI.submitActivityScore(activityId, 100);
            if (!response?.success) {
                throw new Error(response?.message || '领取活动奖励失败');
            }
            toast.success('领取奖励成功');
            await Promise.all([this.getActivities(), this.getMyActivities()]);
            return response.data;
        } catch (error) {
            toast.error('领取奖励失败: ' + error.message);
            throw error;
        }
    }

    getActivityById(activityId) {
        return this.activities.find(activity => activity.id === activityId) || null;
    }
}

export const activityService = new ActivityService();
