import { combatService } from './CombatService.js';

function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

function getMapSelector() {
    return document.getElementById('combat-map-selector') || document.getElementById('map-selector');
}

function getFightButton(id, legacyId) {
    return document.getElementById(id) || document.getElementById(legacyId);
}

export class CombatUI {
    init() {
        this.generateMonster();
        const fightOnceBtn = getFightButton('combat-fight-once-btn', 'fight-once-btn');
        const fight50Btn = getFightButton('combat-fight-50-btn', 'fight-50-btn');
        const fight100Btn = getFightButton('combat-fight-100-btn', 'fight-100-btn');
        if (fightOnceBtn) fightOnceBtn.onclick = () => this.fightOnce();
        if (fight50Btn) fight50Btn.onclick = () => this.batchFight(50);
        if (fight100Btn) fight100Btn.onclick = () => this.batchFight(100);
    }

    updateMonsterDisplay(monster) {
        if (!monster) return;
        setText('combat-monster-name', monster.name);
        setText('combat-monster-level', `等级${monster.level}`);
        setText('combat-monster-type', monster.type);
        setText('combat-monster-health', monster.health);
        setText('combat-monster-attack', monster.attack);
        setText('combat-monster-defense', monster.defense);
        setText('combat-monster-speed', monster.speed);
        const healthBar = document.getElementById('combat-monster-health-bar');
        if (healthBar) healthBar.style.width = '100%';
    }

    addCombatLog(message) {
        const container = document.getElementById('combat-log-container');
        if (!container) return;
        const entry = document.createElement('div');
        entry.innerHTML = message;
        container.appendChild(entry);
        container.scrollTop = container.scrollHeight;
    }

    async generateMonster(mapId = 1) {
        try {
            const monster = await combatService.generateMonster(mapId);
            this.updateMonsterDisplay(monster);
            return monster;
        } catch (error) {
            this.addCombatLog(`<span class="text-red-600">生成怪物失败: ${error.message}</span>`);
            return null;
        }
    }

    async fightOnce() {
        try {
            const mapSelector = getMapSelector();
            const mapId = mapSelector ? parseInt(mapSelector.value) : 1;
            const { monster, result } = await combatService.fightOnce(mapId);
            this.addCombatLog(`遭遇 ${monster.name} (等级${monster.level} ${monster.type})`);
            if (result.result === 'WIN') {
                this.addCombatLog(`<span class="text-green-600">战斗胜利！获得经验: ${result.totalExpGained}, 灵石: ${result.totalSpiritStonesGained}</span>`);
                if (result.droppedEquipmentId) {
                    this.addCombatLog(`<span class="text-blue-600">获得装备掉落!</span>`);
                }
            } else {
                this.addCombatLog(`<span class="text-red-600">战斗失败!</span>`);
            }
            if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        } catch (error) {
            this.addCombatLog(`<span class="text-red-600">战斗失败: ${error.message}</span>`);
        }
    }

    async batchFight(times) {
        const btns = {
            once: getFightButton('combat-fight-once-btn', 'fight-once-btn'),
            btn50: getFightButton('combat-fight-50-btn', 'fight-50-btn'),
            btn100: getFightButton('combat-fight-100-btn', 'fight-100-btn')
        };
        const mapSelector = getMapSelector();
        const mapId = mapSelector ? parseInt(mapSelector.value) : 1;
        Object.values(btns).forEach(b => { if (b) b.disabled = true; });
        const targetBtn = times === 50 ? btns.btn50 : btns.btn100;
        const origText = targetBtn ? targetBtn.innerHTML : '';
        if (targetBtn) targetBtn.innerHTML = '<i class="fa fa-spinner fa-spin mr-1"></i> 战斗中...';
        try {
            this.addCombatLog(`<span class="text-blue-600">开始连续战斗${times}次...</span>`);
            const result = await combatService.batchFight(times, mapId);
            this.addCombatLog(`<span class="text-green-600">连续战斗结束！</span>`);
            this.addCombatLog(`总战斗次数: ${result.totalBattles}，胜利: ${result.wins}次，失败: ${result.losses}次`);
            this.addCombatLog(`胜率: ${(result.winRate * 100).toFixed(2)}%`);
            this.addCombatLog(`获得经验: ${result.totalExpGained}，灵石: ${result.totalSpiritStonesGained}`);
            if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        } catch (error) {
            this.addCombatLog(`<span class="text-red-600">批量战斗失败: ${error.message}</span>`);
        } finally {
            Object.values(btns).forEach(b => { if (b) b.disabled = false; });
            if (targetBtn) targetBtn.innerHTML = origText;
        }
    }

}

export const combatUI = new CombatUI();
