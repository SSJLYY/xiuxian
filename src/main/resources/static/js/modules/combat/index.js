/**
 * 战斗模块入口
 * 导出模块的公共接口
 */
export { combatService } from './CombatService.js';
export { combatUI } from './CombatUI.js';

export function mountCombatGlobals() {
    window.combatUI = combatUI;

    window.generateMonster = async function(mapId) {
        return combatUI.generateMonster(mapId);
    };

    window.fightOnce = async function() {
        return combatUI.fightOnce();
    };

    window.fight50Times = async function() {
        return combatUI.batchFight(50);
    };

    window.fight100Times = async function() {
        return combatUI.batchFight(100);
    };

    window.initCombatModule = function() {
        return combatUI.init();
    };

    return combatUI;
}
