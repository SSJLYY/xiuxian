/**
 * 战斗模块 - 业务逻辑层
 * 对齐当前后端接口，只负责发起完整战斗并缓存结果。
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class CombatService {
    constructor() {
        this.currentCombat = null;
        this.combatHistory = [];
    }

    async startCombat(monsterId = null) {
        try {
            const response = await gameAPI.startCombat(monsterId);
            if (!response.success) {
                throw new Error(response.message || '战斗开始失败');
            }

            this.currentCombat = response.data;
            this.combatHistory.unshift({
                timestamp: new Date().toISOString(),
                monsterId,
                result: response.data?.result || 'unknown'
            });
            return response.data;
        } catch (error) {
            toast.error('战斗开始失败: ' + error.message);
            throw error;
        }
    }

    async executeAttack(skillId = null) {
        if (!this.currentCombat) {
            toast.error('当前没有进行中的战斗');
            return null;
        }

        try {
            const response = await gameAPI.startEnhancedCombat({ skillId });
            if (!response.success) {
                throw new Error(response.message || '攻击失败');
            }
            this.currentCombat = response.data;
            return response.data;
        } catch (error) {
            toast.error('攻击失败: ' + error.message);
            throw error;
        }
    }

    async useItem(itemId) {
        if (!this.currentCombat) {
            toast.error('当前没有进行中的战斗');
            return null;
        }

        try {
            const response = await gameAPI.startEnhancedCombat({ itemId });
            if (!response.success) {
                throw new Error(response.message || '道具使用失败');
            }
            this.currentCombat = response.data;
            return response.data;
        } catch (error) {
            toast.error('道具使用失败: ' + error.message);
            throw error;
        }
    }

    async flee() {
        this.currentCombat = null;
        return { fled: true };
    }

    async getCombatHistory() {
        try {
            const response = await gameAPI.getCombatHistory();
            if (response.success) {
                this.combatHistory = response.data || [];
                return this.combatHistory;
            }
            return [];
        } catch (error) {
            toast.error('获取战斗历史失败: ' + error.message);
            return [];
        }
    }

    formatCombatData(combat) {
        if (!combat) return null;

        return {
            player: {
                name: combat.playerName || '玩家',
                health: combat.playerCurrentHealth ?? combat.playerHealth ?? 0,
                maxHealth: combat.playerMaxHealth ?? 1,
                healthPercent: Math.round(((combat.playerCurrentHealth ?? combat.playerHealth ?? 0) / (combat.playerMaxHealth ?? 1)) * 100)
            },
            monster: {
                name: combat.monsterName || '怪物',
                health: combat.monsterCurrentHealth ?? combat.monsterHealth ?? 0,
                maxHealth: combat.monsterMaxHealth ?? 1,
                healthPercent: Math.round(((combat.monsterCurrentHealth ?? combat.monsterHealth ?? 0) / (combat.monsterMaxHealth ?? 1)) * 100),
                image: combat.monsterImage
            },
            turn: combat.rounds || combat.turn || 1,
            status: combat.result === 'WIN' ? 'victory' : combat.result === 'LOSE' ? 'defeat' : 'ongoing'
        };
    }
}

export const combatService = new CombatService();
