/**
 * 玩家模块 - UI渲染层
 * 负责玩家信息的UI渲染和交互
 */

import { playerService } from './PlayerService.js';
import { loading } from '../../components/Loading.js';
import { FormatUtils } from '../../core/utils/FormatUtils.js';

class PlayerUI {
    constructor() {
        this.currentPlayer = null;
        this.refreshTimer = null;
        this.isInitialized = false;
    }

    /**
     * 初始化玩家UI
     */
    async init() {
        if (this.isInitialized) return;

        try {
            // 绑定UI元素
            this.bindElements();

            // 加载玩家信息
            await this.loadPlayerInfo();

            // 启动自动刷新
            this.startAutoRefresh();

            this.isInitialized = true;
            console.log('玩家UI初始化成功');
        } catch (error) {
            console.error('玩家UI初始化失败:', error);
        }
    }

    /**
     * 绑定UI元素
     */
    bindElements() {
        // 玩家信息显示元素
        this.elements = {
            playerName: document.getElementById('playerName'),
            playerLevel: document.getElementById('playerLevel'),
            playerRealm: document.getElementById('playerRealm'),
            playerSpiritStones: document.getElementById('playerSpiritStones'),
            playerExp: document.getElementById('playerExp'),
            playerHealth: document.getElementById('playerHealth'),
            playerAttack: document.getElementById('playerAttack'),
            playerDefense: document.getElementById('playerDefense')
        };
    }

    /**
     * 加载玩家信息
     */
    async loadPlayerInfo() {
        try {
            // 显示加载状态
            loading.showPage('加载中...');

            // 获取玩家信息
            this.currentPlayer = await playerService.getCurrentPlayer();

            // 格式化数据
            const displayData = playerService.formatPlayerInfo(this.currentPlayer);

            // 更新UI
            this.updatePlayerDisplay(displayData);

        } catch (error) {
            console.error('加载玩家信息失败:', error);
        } finally {
            loading.hidePage();
        }
    }

    /**
     * 更新玩家信息显示
     * @param {Object} data - 格式化后的玩家数据
     */
    updatePlayerDisplay(data) {
        if (!data) return;

        // 更新头部信息
        if (this.elements.playerName) {
            this.elements.playerName.textContent = data.name;
        }

        if (this.elements.playerLevel) {
            this.elements.playerLevel.textContent = data.level;
        }

        if (this.elements.playerRealm) {
            this.elements.playerRealm.textContent = data.realm;
        }

        if (this.elements.playerSpiritStones) {
            this.elements.playerSpiritStones.textContent = data.spiritStones;
        }

        // 更新详细信息
        if (this.elements.playerExp) {
            this.elements.playerExp.textContent = `${data.exp}/${data.expToNext}`;
        }

        if (this.elements.playerHealth) {
            this.elements.playerHealth.textContent = `${data.health}/${data.maxHealth}`;
        }

        if (this.elements.playerAttack) {
            this.elements.playerAttack.textContent = data.attack;
        }

        if (this.elements.playerDefense) {
            this.elements.playerDefense.textContent = data.defense;
        }
    }

    /**
     * 更新灵石显示
     * @param {number} amount - 灵石数量
     */
    updateSpiritStones(amount) {
        if (this.elements.playerSpiritStones) {
            this.elements.playerSpiritStones.textContent =
                FormatUtils.formatSpiritStones(amount);
        }

        // 更新当前玩家数据
        if (this.currentPlayer) {
            this.currentPlayer.spiritStones = amount;
        }
    }

    /**
     * 更新等级显示
     * @param {number} level - 等级
     */
    updateLevel(level) {
        if (this.elements.playerLevel) {
            this.elements.playerLevel.textContent = level;
        }

        if (this.currentPlayer) {
            this.currentPlayer.level = level;
        }
    }

    /**
     * 更新境界显示
     * @param {number} realm - 境界
     */
    updateRealm(realm) {
        if (this.elements.playerRealm) {
            this.elements.playerRealm.textContent =
                playerService.getRealmName(realm);
        }

        if (this.currentPlayer) {
            this.currentPlayer.realm = realm;
        }
    }

    /**
     * 更新经验显示
     * @param {number} exp - 当前经验
     * @param {number} expToNext - 升级所需经验
     */
    updateExp(exp, expToNext) {
        if (this.elements.playerExp) {
            this.elements.playerExp.textContent =
                `${FormatUtils.formatExp(exp)}/${FormatUtils.formatExp(expToNext)}`;
        }

        if (this.currentPlayer) {
            this.currentPlayer.exp = exp;
            this.currentPlayer.expToNext = expToNext;
        }
    }

    /**
     * 更新血量显示
     * @param {number} health - 当前血量
     * @param {number} maxHealth - 最大血量
     */
    updateHealth(health, maxHealth) {
        if (this.elements.playerHealth) {
            this.elements.playerHealth.textContent = `${health}/${maxHealth}`;
        }

        if (this.currentPlayer) {
            this.currentPlayer.health = health;
            this.currentPlayer.maxHealth = maxHealth;
        }
    }

    /**
     * 获取当前玩家信息
     * @returns {Object} 玩家信息
     */
    getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * 启动自动刷新
     * @param {number} interval - 刷新间隔(毫秒)
     */
    startAutoRefresh(interval = 5000) {
        // 清除现有定时器
        this.stopAutoRefresh();

        // 启动新的定时器
        this.refreshTimer = setInterval(() => {
            this.loadPlayerInfo().catch(error => {
                console.error('自动刷新玩家信息失败:', error);
            });
        }, interval);
    }

    /**
     * 停止自动刷新
     */
    stopAutoRefresh() {
        if (this.refreshTimer) {
            clearInterval(this.refreshTimer);
            this.refreshTimer = null;
        }
    }

    /**
     * 销毁玩家UI
     */
    destroy() {
        this.stopAutoRefresh();
        this.elements = null;
        this.currentPlayer = null;
        this.isInitialized = false;
    }
}

// 创建全局PlayerUI实例
const playerUI = new PlayerUI();

// 导出UI类
export { PlayerUI, playerUI };
export default playerUI;

if (typeof module !== 'undefined' && module.exports) {
    module.exports = { PlayerUI, playerUI, default: playerUI };
}
