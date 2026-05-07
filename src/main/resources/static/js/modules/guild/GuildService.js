import { gameAPI } from '../../core/api/GameApi.js';

function unwrapGuildList(data) {
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.guilds)) return data.guilds;
    if (Array.isArray(data?.records)) return data.records;
    return [];
}

export class GuildService {
    normalizeGuild(guild, members = []) {
        const leaderMember = members.find(member => member.role === 'LEADER');
        return {
            ...guild,
            treasury: guild?.treasury ?? guild?.guildFunds ?? 0,
            contribution: guild?.contribution ?? guild?.myContribution ?? 0,
            myContribution: guild?.myContribution ?? guild?.contribution ?? 0,
            name: guild?.guildName || guild?.name || '宗门',
            leaderName: guild?.leaderName || leaderMember?.playerName || leaderMember?.nickname || ''
        };
    }

    async getGuildList() {
        const response = await gameAPI.getGuildList();
        if (!response?.success) {
            throw new Error(response?.message || '加载宗门列表失败');
        }
        return unwrapGuildList(response.data).map(guild => this.normalizeGuild(guild));
    }

    async getMyGuild() {
        const response = await gameAPI.getMyGuild();
        if (!response?.success) {
            if (response?.message) {
                throw new Error(response.message);
            }
            return null;
        }
        return response.data ? this.normalizeGuild(response.data) : null;
    }

    async getGuildDetail(guildId) {
        const response = await gameAPI.get(`/guild/${guildId}`);
        if (!response?.success) {
            throw new Error(response?.message || '加载宗门详情失败');
        }
        const detail = response.data || {};
        return {
            ...detail,
            guild: detail.guild ? this.normalizeGuild(detail.guild, detail.members || []) : null
        };
    }

    async createGuild(name, description = '') {
        const response = await gameAPI.createGuild(name, description);
        if (!response?.success) {
            throw new Error(response?.message || '创建宗门失败');
        }
        return response.data;
    }

    async applyGuild(guildId) {
        const response = await gameAPI.applyGuild(guildId);
        if (!response?.success) {
            throw new Error(response?.message || '申请加入失败');
        }
        return response.data;
    }

    async leaveGuild() {
        const response = await gameAPI.leaveGuild();
        if (!response?.success) {
            throw new Error(response?.message || '退出宗门失败');
        }
        return response.data;
    }

    async donateGuild(amount) {
        const response = await gameAPI.donateGuild(amount);
        if (!response?.success) {
            throw new Error(response?.message || '捐献失败');
        }
        return response.data;
    }
}

export const guildService = new GuildService();
