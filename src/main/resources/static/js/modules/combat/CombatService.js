import { gameAPI } from '../../core/api/GameApi.js';

export class CombatService {
    async generateMonster(mapId = 1) {
        const response = await gameAPI.generateMonster(mapId);
        if (!response?.success) throw new Error(response?.message || '生成怪物失败');
        return response.data;
    }

    async fightOnce(mapId = 1) {
        const monster = await this.generateMonster(mapId);
        const response = monster.id
            ? await gameAPI.startCombatWithMap(monster.id, mapId)
            : await gameAPI.startCombatGenerateWithMap(mapId);
        if (!response?.success) throw new Error(response?.message || '战斗失败');
        return { monster, result: response.data };
    }

    async batchFight(times, mapId = 1) {
        const response = await gameAPI.batchCombat(times, { mapId });
        if (!response?.success) throw new Error(response?.message || '批量战斗失败');
        return response.data;
    }
}

export const combatService = new CombatService();
