/**
 * 宗门模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class GuildService {
    constructor() {
        this.myGuild = null;
        this.guildList = [];
    }

    async loadGuildList() {
        try {
            const response = await gameAPI.guild.list();
            if (response.success) {
                this.guildList = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载宗门列表失败: ' + error.message);
            throw error;
        }
    }

    async loadMyGuild() {
        try {
            const response = await gameAPI.guild.getMyGuild();
            if (response.success) {
                this.myGuild = response.data;
                return response.data;
            }
            return null;
        } catch (error) {
            console.error('加载我的宗门失败:', error);
            return null;
        }
    }

    async createGuild(name, description) {
        try {
            const response = await gameAPI.guild.create(name, description);
            if (response.success) {
                toast.success('创建宗门成功');
                await this.loadMyGuild();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('创建宗门失败: ' + error.message);
            throw error;
        }
    }

    async joinGuild(guildId) {
        try {
            const response = await gameAPI.guild.apply(guildId);
            if (response.success) {
                toast.success('申请已提交');
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('申请失败: ' + error.message);
            throw error;
        }
    }

    async leaveGuild() {
        try {
            const response = await gameAPI.guild.leave();
            if (response.success) {
                toast.success('已退出宗门');
                this.myGuild = null;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('退出失败: ' + error.message);
            throw error;
        }
    }
}

export const guildService = new GuildService();
