/**
 * 玩家模块 - 业务服务层
 * 负责处理玩家相关的业务逻辑
 */

import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';
import { FormatUtils } from '../../core/utils/FormatUtils.js';

class PlayerService {
    /**
     * 获取当前玩家信息
     * @returns {Promise<Object>} 玩家信息
     */
    async getCurrentPlayer() {
        try {
            const response = await gameAPI.getCurrentPlayer();
            if (!response.success) {
                throw new Error(response.message || '获取玩家信息失败');
            }
            return response.data;
        } catch (error) {
            console.error('获取玩家信息失败:', error);
            toast.error(error.message);
            throw error;
        }
    }

    /**
     * 获取玩家详细资料
     * @returns {Promise<Object>} 玩家资料
     */
    async getPlayerProfile() {
        try {
            const response = await gameAPI.getPlayerProfile();
            if (!response.success) {
                throw new Error(response.message || '获取玩家资料失败');
            }
            return response.data;
        } catch (error) {
            console.error('获取玩家资料失败:', error);
            toast.error(error.message);
            throw error;
        }
    }

    /**
     * 更新玩家资料
     * @param {Object} data - 更新数据
     * @returns {Promise<boolean>} 是否成功
     */
    async updateProfile(data) {
        try {
            const response = await gameAPI.updatePlayerProfile(data);
            if (!response.success) {
                throw new Error(response.message || '更新资料失败');
            }
            toast.success('资料更新成功');
            return true;
        } catch (error) {
            console.error('更新资料失败:', error);
            toast.error(error.message);
            throw error;
        }
    }

    /**
     * 获取玩家统计信息
     * @returns {Promise<Object>} 统计信息
     */
    async getPlayerStats() {
        try {
            const response = await gameAPI.getPlayerStats();
            if (!response.success) {
                throw new Error(response.message || '获取统计信息失败');
            }
            return response.data;
        } catch (error) {
            console.error('获取统计信息失败:', error);
            toast.error(error.message);
            throw error;
        }
    }

    /**
     * 刷新玩家信息并更新UI
     * @param {Function} callback - 更新UI的回调函数
     */
    async refreshPlayerInfo(callback) {
        try {
            const player = await this.getCurrentPlayer();
            if (callback && typeof callback === 'function') {
                callback(player);
            }
            return player;
        } catch (error) {
            console.error('刷新玩家信息失败:', error);
            throw error;
        }
    }

    /**
     * 格式化玩家信息用于显示
     * @param {Object} player - 玩家信息对象
     * @returns {Object} 格式化后的显示数据
     */
    formatPlayerInfo(player) {
        if (!player) return null;

        return {
            name: player.username || '未知玩家',
            level: player.level || 1,
            realm: this.getRealmName(player.realm) || '练气期',
            exp: FormatUtils.formatExp(player.exp || 0),
            expToNext: FormatUtils.formatExp(player.expToNext || 0),
            spiritStones: FormatUtils.formatSpiritStones(player.spiritStones || 0),
            attack: player.attack || 0,
            defense: player.defense || 0,
            health: player.health || 0,
            maxHealth: player.maxHealth || 0,
            healthPercent: player.maxHealth
                ? FormatUtils.formatPercent(player.health, player.maxHealth)
                : '0%',
            cultivationTime: FormatUtils.formatTime(player.cultivationTime || 0),
            isCultivating: player.isCultivating || false
        };
    }

    /**
     * 获取境界名称
     * @param {number} realm - 境界等级
     * @returns {string} 境界名称
     */
    getRealmName(realm) {
        const realmMap = {
            1: '练气期',
            2: '筑基期',
            3: '金丹期',
            4: '元婴期',
            5: '化神期',
            6: '炼虚期',
            7: '合体期',
            8: '大乘期',
            9: '渡劫期',
            10: '仙人'
        };
        return realmMap[realm] || '练气期';
    }

    /**
     * 检查玩家状态
     * @param {Object} player - 玩家信息
     * @returns {Object} 状态检查结果
     */
    checkPlayerStatus(player) {
        const checks = {
            isDead: player.health <= 0,
            isLowHealth: player.health < player.maxHealth * 0.3,
            canBreakthrough: false,
            needsRest: false
        };

        // 检查是否可以突破
        if (player.exp >= player.expToNext && player.realm < 10) {
            checks.canBreakthrough = true;
        }

        return checks;
    }
}

// 创建全局PlayerService实例
const playerService = new PlayerService();

// 导出服务
export { PlayerService, playerService };
export default playerService;

if (typeof module !== 'undefined' && module.exports) {
    module.exports = { PlayerService, playerService, default: playerService };
}
