import { gameAPI } from '../../core/api/GameApi.js';

export class PetsService {
    async getMyPets() {
        const res = await gameAPI.getMyPets();
        if (!res.success) throw new Error(res.message);
        return res.data || [];
    }

    async getAvailablePets() {
        const res = await gameAPI.getAvailablePets();
        if (!res.success) throw new Error(res.message);
        return res.data || [];
    }

    async getActivePet() {
        const res = await gameAPI.getActivePet();
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async activatePet(playerPetId) {
        const res = await gameAPI.activatePet(playerPetId);
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async feedPet(playerPetId) {
        const res = await gameAPI.feedPet(playerPetId);
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async trainPet(playerPetId, trainingType) {
        const res = await gameAPI.trainPet(playerPetId, { trainingType });
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async toggleLockPet(playerPetId) {
        const res = await gameAPI.toggleLockPet(playerPetId);
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async releasePet(playerPetId) {
        const res = await gameAPI.releasePet(playerPetId);
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async capturePet(petId) {
        const res = await gameAPI.capturePet(petId);
        if (!res.success) throw new Error(res.message);
        return res.data;
    }
}

export const petsService = new PetsService();
