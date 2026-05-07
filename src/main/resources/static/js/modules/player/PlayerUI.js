import { playerService } from './PlayerService.js';
import { loading } from '../../components/Loading.js';
import { FormatUtils } from '../../core/utils/FormatUtils.js';

class PlayerUI {
    constructor() {
        this.currentPlayer = null;
        this.refreshTimer = null;
        this.isInitialized = false;
    }

    async init() {
        if (this.isInitialized) return;

        try {
            this.bindElements();
            await this.loadPlayerInfo();
            this.startAutoRefresh();
            this.isInitialized = true;
            console.log('玩家 UI 初始化成功');
        } catch (error) {
            console.error('玩家 UI 初始化失败:', error);
        }
    }

    bindElements() {
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

    async loadPlayerInfo() {
        try {
            loading.showPage('加载中...');
            this.currentPlayer = await playerService.getCurrentPlayer();
            const displayData = playerService.formatPlayerInfo(this.currentPlayer);
            this.updatePlayerDisplay(displayData);
        } catch (error) {
            console.error('加载玩家信息失败:', error);
        } finally {
            loading.hidePage();
        }
    }

    updatePlayerDisplay(data) {
        if (!data) return;

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

    updateSpiritStones(amount) {
        if (this.elements.playerSpiritStones) {
            this.elements.playerSpiritStones.textContent = FormatUtils.formatSpiritStones(amount);
        }

        if (this.currentPlayer) {
            this.currentPlayer.spiritStones = amount;
        }
    }

    updateLevel(level) {
        if (this.elements.playerLevel) {
            this.elements.playerLevel.textContent = level;
        }

        if (this.currentPlayer) {
            this.currentPlayer.level = level;
        }
    }

    updateRealm(realm) {
        if (this.elements.playerRealm) {
            this.elements.playerRealm.textContent = playerService.getRealmName(realm);
        }

        if (this.currentPlayer) {
            this.currentPlayer.realm = realm;
        }
    }

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

    updateHealth(health, maxHealth) {
        if (this.elements.playerHealth) {
            this.elements.playerHealth.textContent = `${health}/${maxHealth}`;
        }

        if (this.currentPlayer) {
            this.currentPlayer.health = health;
            this.currentPlayer.maxHealth = maxHealth;
        }
    }

    getCurrentPlayer() {
        return this.currentPlayer;
    }

    startAutoRefresh(interval = 5000) {
        this.stopAutoRefresh();
        this.refreshTimer = setInterval(() => {
            this.loadPlayerInfo().catch(error => {
                console.error('自动刷新玩家信息失败:', error);
            });
        }, interval);
    }

    stopAutoRefresh() {
        if (this.refreshTimer) {
            clearInterval(this.refreshTimer);
            this.refreshTimer = null;
        }
    }

    destroy() {
        this.stopAutoRefresh();
        this.elements = null;
        this.currentPlayer = null;
        this.isInitialized = false;
    }
}

const playerUI = new PlayerUI();

export { PlayerUI, playerUI };
export default playerUI;

if (typeof module !== 'undefined' && module.exports) {
    module.exports = { PlayerUI, playerUI, default: playerUI };
}
