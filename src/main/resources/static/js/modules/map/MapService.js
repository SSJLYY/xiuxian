import { gameAPI } from '../../core/api/GameApi.js';

export class MapService {
    normalizeMap(map) {
        const isCurrent = Boolean(map?.isCurrent ?? map?.current);
        const isUnlocked = map?.isLocked != null ? !map.isLocked : Boolean(map?.unlocked ?? false);
        return {
            ...map,
            isCurrent,
            isLocked: !isUnlocked && !isCurrent,
            icon: map?.icon || (map?.mapType === 'SAFE' ? '🏯' : '🗺️'),
            monsterLevel: map?.monsterLevel ?? map?.dangerLevel ?? 0
        };
    }

    async getCurrentMap() {
        const response = await gameAPI.getCurrentMap();
        if (!response?.success) return null;
        return response.data ? this.normalizeMap(response.data) : null;
    }

    async getMapList() {
        const response = await gameAPI.getMaps();
        if (!response?.success) throw new Error(response?.message || '加载地图列表失败');
        return (response.data || []).map(map => this.normalizeMap(map));
    }

    async enterMap(mapId) {
        const response = await gameAPI.enterMap(mapId);
        if (!response?.success) throw new Error(response?.message || '进入地图失败');
        return response.data;
    }

    async exploreMap() {
        const response = await gameAPI.exploreMap();
        if (!response?.success) throw new Error(response?.message || '探索失败');
        return response.data;
    }
}

export const mapService = new MapService();
