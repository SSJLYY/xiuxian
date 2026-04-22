import { gameAPI } from '../../core/api/GameApi.js';

export class MapService {
    async getCurrentMap() {
        const response = await gameAPI.getCurrentMap();
        if (!response?.success) return null;
        return response.data || null;
    }

    async getMapList() {
        const response = await gameAPI.getMaps();
        if (!response?.success) throw new Error(response?.message || '获取地图列表失败');
        return response.data || [];
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
