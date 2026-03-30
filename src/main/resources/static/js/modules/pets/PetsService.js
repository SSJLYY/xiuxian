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
            const response = await gameAPI.pets.getMyPets();
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
            const response = await gameAPI.pets.getAvailablePets();
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
            const response = await gameAPI.pets.capture(monsterId);
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

    async feedPet(petId, foodId) {
        try {
            const response = await gameAPI.pets.feed(petId, foodId);
            if (response.success) {
                toast.success('喂养成功');
                await this.loadMyPets();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('喂养失败: ' + error.message);
            throw error;
        }
    }
}

export const petsService = new PetsService();
