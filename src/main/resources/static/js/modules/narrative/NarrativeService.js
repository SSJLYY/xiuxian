/**
 * 叙事模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class NarrativeService {
    constructor() {
        this.npcs = [];
        this.myRelations = [];
    }

    async getNpcList() {
        try {
            const response = await gameAPI.npc.getList();
            if (response.success) {
                this.npcs = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载NPC列表失败: ' + error.message);
            throw error;
        }
    }

    async getNpcRelations() {
        try {
            const response = await gameAPI.npc.getMyRelations();
            if (response.success) {
                this.myRelations = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载NPC关系失败: ' + error.message);
            throw error;
        }
    }

    async interactWithNpc(npcId) {
        try {
            const response = await gameAPI.npc.interact(npcId);
            if (response.success) {
                toast.success('交互成功');
                await this.getNpcRelations();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('交互失败: ' + error.message);
            throw error;
        }
    }

    async startQuest(npcId, questId) {
        try {
            const response = await gameAPI.npc.startQuest(npcId, questId);
            if (response.success) {
                toast.success('任务已开始');
                await this.getNpcRelations();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('开始任务失败: ' + error.message);
            throw error;
        }
    }

    getNpcById(npcId) {
        return this.npcs.find(npc => npc.id === npcId) || null;
    }

    getRelationByNpcId(npcId) {
        return this.myRelations.find(rel => rel.npcId === npcId) || null;
    }
}

export const narrativeService = new NarrativeService();
