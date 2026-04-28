import { gameAPI } from '../../core/api/GameApi.js';

export class SkillsService {
    normalizeSkill(skill) {
        const skillInfo = skill?.skill || skill;
        const slotNumber = skill?.slotNumber;
        return {
            ...skill,
            ...skillInfo,
            playerSkillId: skill?.id ?? skill?.playerSkillId,
            id: skillInfo?.id ?? skill?.skillId ?? skill?.id,
            name: skillInfo?.name || skill?.skillName || skill?.name || '未知技能',
            description: skillInfo?.description || skill?.skillDescription || skill?.description || '',
            elementType: skillInfo?.elementType || skillInfo?.element || skill?.elementType || skill?.element || 'PHYSICAL',
            elementTypeName: skillInfo?.elementTypeName || skill?.elementTypeName || skillInfo?.element || skill?.element || 'PHYSICAL',
            damage: skillInfo?.damage ?? skillInfo?.baseDamage ?? skill?.damage ?? skill?.baseDamage ?? 0,
            baseDamage: skillInfo?.baseDamage ?? skill?.baseDamage ?? 0,
            cooldown: skill?.cooldown ?? skillInfo?.cooldown ?? 0,
            cost: skillInfo?.requiredSpiritStones ?? skill?.requiredSpiritStones ?? skill?.manaCost ?? skillInfo?.manaCost ?? 0,
            requiredSpiritStones: skillInfo?.requiredSpiritStones ?? skill?.requiredSpiritStones ?? 0,
            unlockLevel: skillInfo?.unlockLevel ?? skill?.unlockLevel ?? 1,
            equippedSlot: skill?.equipped === false || slotNumber == null ? null : slotNumber,
            level: skill?.level ?? skillInfo?.level ?? 1
        };
    }

    async getMySkills() {
        const res = await gameAPI.getPlayerSkills();
        if (!res.success) throw new Error(res.message);
        return (res.data || []).map(skill => this.normalizeSkill(skill));
    }

    async getAvailableSkills() {
        const res = await gameAPI.getAvailableSkills();
        if (!res.success) throw new Error(res.message);
        return (res.data || []).map(skill => this.normalizeSkill(skill));
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
