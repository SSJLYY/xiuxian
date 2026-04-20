/**
 * 宠物模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class PetsService {
    constructor() {
        this.myPets = [];
        this.availablePets = [];
    }

    async loadMyPets() {
        try {
            const response = await gameAPI.getMyPets();
            if (response.success) {
                this.myPets = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载宠物失败: ' + error.message);
            throw error;
        }
    }

    async loadAvailablePets() {
        try {
            const response = await gameAPI.getAvailablePets();
            if (response.success) {
                this.availablePets = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载可用宠物失败: ' + error.message);
            throw error;
        }
    }

    async capturePet(monsterId) {
        try {
            const response = await gameAPI.capturePet(monsterId);
            if (response.success) {
                toast.success('捕获成功');
                await this.loadMyPets();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('捕获失败: ' + error.message);
            throw error;
        }
    }

    async feedPet(petId) {
        try {
            const response = await gameAPI.feedPet(petId);
            if (response.success) {
                toast.success('喂养成功');
                await this.loadMyPets();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('喂养失败：' + error.message);
            throw error;
        }
    }

    async activatePet(playerPetId) {
        try {
            const response = await gameAPI.activatePet(playerPetId);
            if (response.success) {
                toast.success('激活宠物成功');
                await this.loadMyPets();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('激活宠物失败：' + error.message);
            throw error;
        }
    }

    async trainPet(playerPetId) {
        try {
            const response = await gameAPI.trainPet(playerPetId);
            if (response.success) {
                toast.success('训练宠物成功');
                await this.loadMyPets();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('训练宠物失败：' + error.message);
            throw error;
        }
    }

    async renamePet(playerPetId, newName) {
        try {
            const response = await gameAPI.renamePet(playerPetId, newName);
            if (response.success) {
                toast.success('宠物改名成功');
                await this.loadMyPets();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('宠物改名失败：' + error.message);
            throw error;
        }
    }
}

export const petsService = new PetsService();
