/**
 * 玩家模块 - 业务服务层
 * 负责处理玩家相关的业务逻辑
 */

import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';
import { FormatUtils } from '../../core/utils/FormatUtils.js';

class PlayerService {
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

    formatPlayerInfo(player) {
        if (!player) return null;

        return {
            name: player.nickname || player.username || '未知玩家',
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

    checkPlayerStatus(player) {
        const checks = {
            isDead: player.health <= 0,
            isLowHealth: player.health < player.maxHealth * 0.3,
            canBreakthrough: false,
            needsRest: false
        };

        if (player.exp >= player.expToNext && player.realm < 10) {
            checks.canBreakthrough = true;
        }

        return checks;
    }
}

const playerService = new PlayerService();

export { PlayerService, playerService };
export default playerService;

if (typeof module !== 'undefined' && module.exports) {
    module.exports = { PlayerService, playerService, default: playerService };
}
