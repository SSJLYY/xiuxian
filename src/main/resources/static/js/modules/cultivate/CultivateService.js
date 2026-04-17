/**
 * 修炼模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class CultivateService {
    constructor() {
        this.cultivateInfo = null;
    }

    async getCultivateInfo() {
        try {
            const response = await gameAPI.getCultivateInfo();
            if (response.success) {
                this.cultivateInfo = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取修炼信息失败：' + error.message);
            throw error;
        }
    }

    async startCultivate(type = 'normal') {
        try {
            const response = await gameAPI.startCultivate(type);
            if (response.success) {
                toast.success('开始修炼!');
                await this.getCultivateInfo();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('开始修炼失败：' + error.message);
            throw error;
        }
    }

    async stopCultivate() {
        try {
            const response = await gameAPI.stopCultivate();
            if (response.success) {
                toast.success('停止修炼');
                await this.getCultivateInfo();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('停止修炼失败：' + error.message);
            throw error;
        }
    }

    async breakthrough() {
        try {
            const response = await gameAPI.breakthrough();
            if (response.success) {
                toast.success('境界突破成功!');
                await this.getCultivateInfo();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('突破失败：' + error.message);
            throw error;
        }
    }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取修炼信息失败: ' + error.message);
            throw error;
        }
    }

    async startCultivate(type = 'normal') {
        try {
            const response = await gameAPI.player.startCultivate(type);
            if (response.success) {
                toast.success('开始修炼!');
                await this.getCultivateInfo();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('开始修炼失败: ' + error.message);
            throw error;
        }
    }

    async stopCultivate() {
        try {
            const response = await gameAPI.player.stopCultivate();
            if (response.success) {
                toast.success('停止修炼');
                await this.getCultivateInfo();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('停止修炼失败: ' + error.message);
            throw error;
        }
    }

    async breakthrough() {
        try {
            const response = await gameAPI.player.breakthrough();
            if (response.success) {
                toast.success('境界突破成功!');
                await this.getCultivateInfo();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('突破失败: ' + error.message);
            throw error;
        }
    }

    getCultivationSpeed(realm) {
        const realmMultiplier = {
            '练气': 1.0,
            '筑基': 1.5,
            '金丹': 2.5,
            '元婴': 4.0
        };
        return realmMultiplier[realm] || 1.0;
    }
}

export const cultivateService = new CultivateService();
