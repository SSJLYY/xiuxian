import { gameAPI } from '../../core/api/GameApi.js';

export class PetsService {
    constructor() {
        this.petTemplates = null;
    }

    async ensurePetTemplates() {
        if (Array.isArray(this.petTemplates)) {
            return this.petTemplates;
        }
        const res = await gameAPI.get('/pets');
        if (!res.success) throw new Error(res.message);
        this.petTemplates = res.data || [];
        return this.petTemplates;
    }

    getPetTemplateMap(templates = []) {
        return new Map((templates || []).map(pet => [pet.id, pet]));
    }

    normalizeAvailablePet(pet) {
        const rawCaptureRate = Number(pet?.captureRate ?? 0);
        return {
            ...pet,
            name: pet?.name || 'Unknown Pet',
            petName: pet?.name || 'Unknown Pet',
            rarity: Number(pet?.rarity ?? 1),
            captureRate: pet?.captureRate != null
                ? Math.round(rawCaptureRate <= 1 ? rawCaptureRate * 100 : rawCaptureRate)
                : 0,
            type: pet?.type || 'NORMAL'
        };
    }

    normalizeOwnedPet(pet, template = null) {
        return {
            ...pet,
            petName: pet?.nickname || template?.name || pet?.name || 'Unknown Pet',
            name: pet?.nickname || template?.name || pet?.name || 'Unknown Pet',
            baseName: template?.name || pet?.name || '',
            description: pet?.description || template?.description || '',
            type: pet?.type || template?.type || 'NORMAL',
            rarity: Number(pet?.rarity ?? template?.rarity ?? 1),
            icon: pet?.icon || template?.icon || ''
        };
    }

    async getMyPets() {
        const templates = await this.ensurePetTemplates();
        const templateMap = this.getPetTemplateMap(templates);
        const res = await gameAPI.getMyPets();
        if (!res.success) throw new Error(res.message);
        return (res.data || []).map(pet => this.normalizeOwnedPet(pet, templateMap.get(pet?.petId)));
    }

    async getAvailablePets() {
        const res = await gameAPI.getAvailablePets();
        if (!res.success) throw new Error(res.message);
        return (res.data || []).map(pet => this.normalizeAvailablePet(pet));
    }

    async getActivePet() {
        const templates = await this.ensurePetTemplates();
        const templateMap = this.getPetTemplateMap(templates);
        const res = await gameAPI.getActivePet();
        if (!res.success) throw new Error(res.message);
        return res.data ? this.normalizeOwnedPet(res.data, templateMap.get(res.data?.petId)) : null;
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
