/**
 * 战斗模块 - 业务逻辑层
 * 负责战斗相关的业务逻辑和数据处理
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class CombatService {
    constructor() {
        this.currentCombat = null;
        this.combatHistory = [];
    }

    /**
     * 开始战斗
     * @param {number} monsterId - 怪物ID
     */
    async startCombat(monsterId) {
        try {
            const response = await gameAPI.startCombat(monsterId);
            if (response.success) {
                this.currentCombat = response.data;
                this.combatHistory.unshift({
                    timestamp: new Date().toISOString(),
                    monsterId: monsterId,
                    result: 'started'
                });
                return response.data;
            } else {
                toast.error('战斗开始失败: ' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('战斗开始失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 执行攻击
     * @param {number} skillId - 技能ID(可选)
     */
    async executeAttack(skillId = null) {
        if (!this.currentCombat) {
            toast.error('当前没有进行中的战斗');
            return null;
        }

        try {
            const response = await gameAPI.startCombat(skillId);
            if (response.success) {
                this.currentCombat = response.data.combat;
                return response.data;
            } else {
                toast.error('攻击失败: ' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('攻击失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 使用道具
     * @param {number} itemId - 道具ID
     */
    async useItem(itemId) {
        try {
            const response = await gameAPI.useItem(itemId);
            if (response.success) {
                this.currentCombat = response.data.combat;
                toast.success('道具使用成功');
                return response.data;
            } else {
                toast.error('道具使用失败: ' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('道具使用失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 逃跑
     */
    async flee() {
        if (!this.currentCombat) {
            toast.error('当前没有进行中的战斗');
            return null;
        }

        try {
            const response = await gameAPI.startCombat();
            if (response.success) {
                toast.success('逃跑成功');
                this.currentCombat = null;
                return response.data;
            } else {
                toast.error('逃跑失败: ' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('逃跑失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 获取战斗历史
     */
    async getCombatHistory() {
        try {
            const response = await gameAPI.getCombatHistory();
            if (response.success) {
                this.combatHistory = response.data;
                return response.data;
            }
            return [];
        } catch (error) {
            toast.error('获取战斗历史失败: ' + error.message);
            return [];
        }
    }

    /**
     * 格式化战斗数据
     */
    formatCombatData(combat) {
        if (!combat) return null;

        return {
            player: {
                name: combat.playerName,
                health: combat.playerHealth,
                maxHealth: combat.playerMaxHealth,
                healthPercent: Math.round((combat.playerHealth / combat.playerMaxHealth) * 100)
            },
            monster: {
                name: combat.monsterName,
                health: combat.monsterHealth,
                maxHealth: combat.monsterMaxHealth,
                healthPercent: Math.round((combat.monsterHealth / combat.monsterMaxHealth) * 100),
                image: combat.monsterImage
            },
            turn: combat.turn,
            status: combat.status
        };
    }

    /**
     * 计算战斗奖励
     */
    calculateRewards(combatResult) {
        const rewards = {
            exp: combatResult.exp || 0,
            spiritStones: combatResult.spiritStones || 0,
            items: combatResult.items || []
        };

        // 格式化奖励显示
        let rewardText = [];
        if (rewards.exp > 0) rewardText.push(`${rewards.exp}经验值`);
        if (rewards.spiritStones > 0) rewardText.push(`${rewards.spiritStones}灵石`);
        if (rewards.items.length > 0) rewardText.push(`${rewards.items.length}件物品`);

        return {
            ...rewards,
            text: rewardText.join(', ')
        };
    }
}

// 导出单例
export const combatService = new CombatService();
