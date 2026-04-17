/**
 * 地图模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class MapService {
    constructor() {
        this.currentMap = null;
        this.availableMaps = [];
        this.exploredMaps = [];
    }

    async getCurrentMap() {
        try {
            const response = await gameAPI.getCurrentMap();
            if (response.success) {
                this.currentMap = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取当前地图失败: ' + error.message);
            throw error;
        }
    }

    async getMapList() {
        try {
            const response = await gameAPI.getMaps();
            if (response.success) {
                this.availableMaps = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取地图列表失败: ' + error.message);
            throw error;
        }
    }

    async getExploredMaps() {
        try {
            const response = await gameAPI.exploreMap();
            if (response.success) {
                this.exploredMaps = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('获取已探索地图失败: ' + error.message);
            throw error;
        }
    }

    async teleportToMap(mapId) {
        try {
            const response = await gameAPI.enterMap(mapId);
            if (response.success) {
                toast.success('传送成功!');
                await this.getCurrentMap();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('传送失败: ' + error.message);
            throw error;
        }
    }

    async exploreMap(mapId) {
        try {
            const response = await gameAPI.exploreMap();
            if (response.success) {
                toast.success('探索成功!');
                await this.getExploredMaps();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('探索失败：' + error.message);
            throw error;
        }
    }
            throw new Error(response.message);
        } catch (error) {
            toast.error('探索失败: ' + error.message);
            throw error;
        }
    }

    getMapById(mapId) {
        return this.availableMaps.find(m => m.id === mapId) || null;
    }
}

export const mapService = new MapService();
