import { gameAPI } from '../../core/api/GameApi.js';

export class PetEvolutionService {
    async getEvolutionInfo(playerPetId) {
        const response = await gameAPI.get(`/pets/evolution/info/${playerPetId}`);
        if (!response?.success) {
            throw new Error(response?.message || '加载进化信息失败');
        }
        return response.data || {};
    }

    async evolvePet(playerPetId) {
        const response = await gameAPI.post(`/pets/evolution/evolve/${playerPetId}`);
        if (!response?.success) {
            throw new Error(response?.message || '进化失败');
        }
        return response.data || {};
    }
}

export const petEvolutionService = new PetEvolutionService();
