import { gameAPI } from '../../core/api/GameApi.js';

export class SkillsService {
    async getMySkills() {
        const res = await gameAPI.getPlayerSkills();
        if (!res.success) throw new Error(res.message);
        return res.data || [];
    }

    async getAvailableSkills() {
        const res = await gameAPI.getAvailableSkills();
        if (!res.success) throw new Error(res.message);
        return res.data || [];
    }

    async learnSkill(skillId) {
        const res = await gameAPI.learnSkill(skillId);
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async equipSkill(playerSkillId, slotNumber) {
        const res = await gameAPI.equipSkill(playerSkillId, slotNumber);
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async unequipSkill(playerSkillId) {
        const res = await gameAPI.unequipSkill(playerSkillId);
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async upgradeSkill(playerSkillId) {
        const res = await gameAPI.upgradeSkill(playerSkillId);
        if (!res.success) throw new Error(res.message);
        return res.data;
    }

    async getCombos(availableOnly = true) {
        const endpoint = availableOnly ? '/skills/combos/available' : '/skills/combos/all';
        const res = await window.api.get(endpoint);
        if (!res.success) throw new Error(res.message);
        return res.data || [];
    }

    async getComboStats() {
        const res = await gameAPI.getComboStats();
        if (!res.success) throw new Error(res.message);
        return res.data || {};
    }
}

export const skillsService = new SkillsService();
