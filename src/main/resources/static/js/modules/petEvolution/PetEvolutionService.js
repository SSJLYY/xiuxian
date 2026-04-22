import { gameAPI } from '../../core/api/GameApi.js';

export class PetEvolutionService {
    async getEvolutionInfo(petId) {
        const response = await gameAPI.get(`/pets/evolution/info/${petId}`);
        if (!response?.success) throw new Error(response?.message || '加载进化信息失败');
        return response.data || {};
    }

    async evolvePet(petId) {
        const response = await gameAPI.post(`/pets/evolution/evolve/${petId}`);
        if (!response?.success) throw new Error(response?.message || '进化失败');
        return response.data || {};
    }
}

export const petEvolutionService = new PetEvolutionService();
