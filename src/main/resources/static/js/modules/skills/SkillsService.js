/**
 * 技能模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class SkillsService {
    constructor() {
        this.mySkills = [];
        this.availableSkills = [];
    }

    async loadMySkills() {
        try {
            const response = await gameAPI.getPlayerSkills();
            if (response.success) {
                this.mySkills = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载技能失败: ' + error.message);
            throw error;
        }
    }

    async loadAvailableSkills() {
        try {
            const response = await gameAPI.getAvailableSkills();
            if (response.success) {
                this.availableSkills = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载可用技能失败: ' + error.message);
            throw error;
        }
    }

    async learnSkill(skillId) {
        try {
            const response = await gameAPI.learnSkill(skillId);
            if (response.success) {
                toast.success('学习技能成功');
                await this.loadMySkills();
                await this.loadAvailableSkills();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('学习技能失败: ' + error.message);
            throw error;
        }
    }

    async upgradeSkill(skillId) {
        try {
            const response = await gameAPI.upgradeSkill(skillId);
            if (response.success) {
                toast.success('技能升级成功');
                await this.loadMySkills();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('技能升级失败：' + error.message);
            throw error;
        }
    }

    async equipSkill(playerSkillId, slotNumber) {
        try {
            const response = await gameAPI.equipSkill(playerSkillId, slotNumber);
            if (response.success) {
                toast.success('装备技能成功');
                await this.loadMySkills();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('装备技能失败：' + error.message);
            throw error;
        }
    }

    async unequipSkill(playerSkillId) {
        try {
            const response = await gameAPI.unequipSkill(playerSkillId);
            if (response.success) {
                toast.success('卸下技能成功');
                await this.loadMySkills();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('卸下技能失败：' + error.message);
            throw error;
        }
    }
}

export const skillsService = new SkillsService();
