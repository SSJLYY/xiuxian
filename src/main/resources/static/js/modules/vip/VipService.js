/**
 * VIP 模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class VipService {
    constructor() {
        this.vipInfo = null;
        this.vipLevels = [];
        this.vipBenefits = [];
    }

    isSameDay(dateValue) {
        if (!dateValue) return false;
        const value = new Date(dateValue);
        const now = new Date();
        return value.getFullYear() === now.getFullYear()
            && value.getMonth() === now.getMonth()
            && value.getDate() === now.getDate();
    }

    normalizeVipInfo(info) {
        if (!info) return null;
        return {
            ...info,
            level: info.vipLevel ?? info.level ?? 0,
            currentExp: info.totalRecharge ?? info.currentExp ?? 0,
            claimedDailyReward: this.isSameDay(info.lastDailyRewardAt)
        };
    }

    normalizeVipLevel(level) {
        const benefits = [
            `每日灵石 +${level.dailySpiritStones || 0}`,
            `修炼速度 +${level.cultivationSpeedBonus || 0}%`,
            `经验加成 +${level.expBonus || 0}%`,
            `商店折扣 ${level.shopDiscount || 100}%`
        ];
        return {
            ...level,
            requiredExp: level.requiredRecharge ?? level.requiredExp ?? 0,
            benefits
        };
    }

    rebuildBenefits() {
        const currentLevel = this.vipInfo?.level || 0;
        const current = this.vipLevels.find(level => level.level === currentLevel);
        this.vipBenefits = current
            ? current.benefits.map((description, index) => ({
                name: `VIP 特权 ${index + 1}`,
                description
            }))
            : [];
    }

    async getVipInfo() {
        try {
            const response = await gameAPI.getVipInfo();
            if (response.success) {
                this.vipInfo = this.normalizeVipInfo(response.data);
                this.rebuildBenefits();
                return this.vipInfo;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取 VIP 信息失败: ' + error.message);
            throw error;
        }
    }

    async getVipLevels() {
        try {
            const response = await gameAPI.getVipLevels();
            if (response.success) {
                this.vipLevels = (response.data || []).map(level => this.normalizeVipLevel(level));
                const nextLevel = this.vipLevels.find(level => level.level === (this.vipInfo?.level || 0) + 1);
                if (this.vipInfo) {
                    this.vipInfo.nextLevelExp = nextLevel?.requiredExp ?? this.vipInfo.currentExp ?? 0;
                }
                this.rebuildBenefits();
                return this.vipLevels;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取 VIP 等级失败: ' + error.message);
            throw error;
        }
    }

    async getVipBenefits() {
        if (this.vipLevels.length === 0) {
            await this.getVipLevels();
        }
        if (!this.vipInfo) {
            await this.getVipInfo();
        }
        this.rebuildBenefits();
        return this.vipBenefits;
    }

    async claimDailyReward() {
        try {
            const response = await gameAPI.claimDailyVipReward();
            if (response.success) {
                toast.success('领取 VIP 每日奖励成功');
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
                toast.success('充值成功');
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
