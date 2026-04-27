/**
 * 管理后台API客户端
 * 封装所有管理后台相关的API调用
 */

import { ApiClient } from './ApiClient.js';

class AdminApi extends ApiClient {
    constructor() {
        super({
            baseURL: '/api/admin',
            timeout: 10000
        });
    }

    // ========== 管理员认证 ==========
    async login(username, password) {
        return this.post('/auth/login', { username, password });
    }

    async logout() {
        return this.post('/auth/logout', {});
    }

    async getAdminInfo() {
        return this.get('/auth/me');
    }

    // ========== 玩家管理 ==========
    async getPlayers(page = 1, size = 20, keyword = '') {
        const params = new URLSearchParams({ page, size });
        if (keyword) {
            params.set('nickname', keyword);
        }
        return this.get(`/players?${params.toString()}`);
    }

    async getPlayerDetail(playerId) {
        return this.get(`/players/${playerId}`);
    }

    async banPlayer(playerId, reason) {
        return this.post(`/players/${playerId}/ban`, { reason });
    }

    async unbanPlayer(playerId) {
        return this.post(`/players/${playerId}/unban`, {});
    }

    async updatePlayerSpiritStones(playerId, amount) {
        return this.post(`/players/${playerId}/spirit-stones`, { amount });
    }

    // ========== 系统邮件 ==========
    async sendSystemMail(title, content, reward = null, recipients = null) {
        const attachments = Array.isArray(reward)
            ? reward
            : reward?.attachments ?? (reward ? [reward] : []);
        const recipientList = Array.isArray(recipients)
            ? recipients.filter(recipient => recipient != null)
            : recipients != null
                ? [recipients]
                : [];
        const basePayload = {
            title,
            content,
            mailType: 'SYSTEM',
            attachments
        };

        if (recipientList.length > 1) {
            return this.post('/mail/send-batch', {
                ...basePayload,
                playerIds: recipientList
            });
        }

        return this.post('/mail/send', {
            ...basePayload,
            playerId: recipientList[0]
        });
    }

    async getSystemMailList(page = 1, size = 20) {
        const params = new URLSearchParams({ page, size }).toString();
        return this.get(`/mail/system?${params}`);
    }

    // ========== 物品管理 ==========
    async getItems(page = 1, size = 20) {
        const params = new URLSearchParams({ page, size }).toString();
        return this.get(`/items?${params}`);
    }

    async createItem(item) {
        return this.post('/items', item);
    }

    async updateItem(itemId, item) {
        return this.put(`/items/${itemId}`, item);
    }

    async deleteItem(itemId) {
        return this.delete(`/items/${itemId}`);
    }

    // ========== 技能管理 ==========
    async getSkillList(page = 1, size = 20) {
        const params = new URLSearchParams({ page, size }).toString();
        return this.get(`/skills?${params}`);
    }

    async createSkill(skill) {
        return this.post('/skills', skill);
    }

    async updateSkill(skillId, skill) {
        return this.put(`/skills/${skillId}`, skill);
    }

    async deleteSkill(skillId) {
        return this.delete(`/skills/${skillId}`);
    }

    // ========== 怪物管理 ==========
    async getMonsterList(page = 1, size = 20) {
        const params = new URLSearchParams({ page, size }).toString();
        return this.get(`/monsters?${params}`);
    }

    async createMonster(monster) {
        return this.post('/monsters', monster);
    }

    async updateMonster(monsterId, monster) {
        return this.put(`/monsters/${monsterId}`, monster);
    }

    async deleteMonster(monsterId) {
        return this.delete(`/monsters/${monsterId}`);
    }

    // ========== 配置管理 ==========
    async getSystemConfig() {
        return this.get('/config');
    }

    async updateSystemConfig(config) {
        return this.put('/config', config);
    }

    // ========== 公告管理 ==========
    async getAnnouncements(page = 1, size = 20, status = '') {
        const params = new URLSearchParams({ page, size });
        if (status) {
            params.set('status', status);
        }
        return this.get(`/announcement/list?${params.toString()}`);
    }

    async createAnnouncement(announcement) {
        return this.post('/announcement', announcement);
    }

    async updateAnnouncement(id, announcement) {
        return this.put(`/announcement/${id}`, announcement);
    }

    async deleteAnnouncement(id) {
        return this.delete(`/announcement/${id}`);
    }

    async publishAnnouncement(id) {
        return this.post(`/announcement/${id}/publish`, {});
    }

    async revokeAnnouncement(id) {
        return this.post(`/announcement/${id}/revoke`, {});
    }

    async getAnnouncementDetail(id) {
        return this.get(`/announcement/${id}`);
    }

    async getAnnouncementStats() {
        return this.get('/announcement/stats');
    }

    // ========== 活动管理 ==========
    async getActivities(page = 1, size = 20) {
        const params = new URLSearchParams({ page, size }).toString();
        return this.get(`/activities?${params}`);
    }

    async createActivity(activity) {
        return this.post('/activities', activity);
    }

    async updateActivity(activityId, activity) {
        return this.put(`/activities/${activityId}`, activity);
    }

    async deleteActivity(activityId) {
        return this.delete(`/activities/${activityId}`);
    }

    // ========== 兑换码管理 ==========
    async getGiftCodes(page = 1, size = 20) {
        const params = new URLSearchParams({ page, size }).toString();
        return this.get(`/gift-codes?${params}`);
    }

    async createGiftCode(code, reward, expiry) {
        return this.post('/gift-codes', { code, reward, expiry });
    }

    async deleteGiftCode(codeId) {
        return this.delete(`/gift-codes/${codeId}`);
    }

    // ========== 宗门管理 ==========
    async getGuilds(page = 1, size = 20) {
        const params = new URLSearchParams({ page, size }).toString();
        return this.get(`/guilds?${params}`);
    }

    async updateGuild(guildId, data) {
        return this.put(`/guilds/${guildId}`, data);
    }

    async deleteGuild(guildId) {
        return this.delete(`/guilds/${guildId}`);
    }

    // ========== 拍卖行管理 ==========
    async getAuctionList(page = 1, size = 20) {
        const params = new URLSearchParams({ page, size }).toString();
        return this.get(`/auction?${params}`);
    }

    async cancelAuction(auctionId) {
        return this.post(`/auction/${auctionId}/cancel`, {});
    }

    // ========== 统计数据 ==========
    async getStatistics() {
        return this.get('/statistics/overall');
    }

    async getPlayerStatistics() {
        return this.get('/statistics/player-growth');
    }

    async getEconomyStatistics() {
        return this.get('/statistics/revenue');
    }

    async getSystemStats() {
        const response = await this.get('/dashboard/stats');
        if (!response?.success) {
            throw new Error(response?.message || '加载系统统计失败');
        }
        return response.data || {};
    }

    async getRecentPlayers(limit = 10) {
        const response = await this.getPlayers(1, limit);
        if (!response?.success) {
            throw new Error(response?.message || '加载最新玩家失败');
        }
        return response.data?.records || [];
    }

    // ========== 日志管理 ==========
    async getOperationLogs(page = 1, size = 50, type = null) {
        const params = new URLSearchParams({ page, size, type }).toString();
        return this.get(`/logs/operations?${params}`);
    }

    async getCombatLogs(page = 1, size = 50, playerId = null) {
        const params = new URLSearchParams({ page, size, playerId }).toString();
        return this.get(`/logs/combat?${params}`);
    }
}

// 创建全局管理API实例
const adminAPI = new AdminApi();

export { AdminApi, adminAPI };

// 导出API客户端
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { AdminApi, adminAPI };
}
