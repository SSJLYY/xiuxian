/**
 * 游戏API客户端
 * 封装所有游戏相关的API调用
 */

import { ApiClient } from './ApiClient.js';

class GameApi extends ApiClient {
    constructor() {
        super({
            baseURL: '/api',
            timeout: 10000
        });
    }

    // ========== 认证相关 ==========
    async login(username, password) {
        return this.post('/auth/login', { username, password });
    }

    async register(username, password, confirmPassword) {
        return this.post('/auth/register', { username, password, confirmPassword });
    }

    async logout() {
        return this.post('/auth/logout', {});
    }

    async getCurrentPlayer() {
        return this.get('/player/current');
    }

    // ========== 玩家相关 ==========
    async getPlayerProfile() {
        return this.get('/player/profile');
    }

    async updatePlayerProfile(data) {
        return this.put('/player/profile', data);
    }

    async getPlayerStats() {
        return this.get('/player/stats');
    }

    // ========== 修炼相关 ==========
    async startCultivation() {
        return this.post('/cultivation/start', {});
    }

    async stopCultivation() {
        return this.post('/cultivation/stop', {});
    }

    async getCultivationStatus() {
        return this.get('/cultivation/status');
    }

    async breakthrough() {
        return this.post('/cultivation/breakthrough', {});
    }

    async resetCultivation() {
        return this.post('/cultivation/reset', {});
    }

    // ========== 战斗相关 ==========
    async startCombat(enemyId) {
        return this.post('/combat/start', { enemyId });
    }

    async getCombatResult(combatId) {
        return this.get(`/combat/result/${combatId}`);
    }

    async getEnemyList() {
        return this.get('/combat/enemies');
    }

    // ========== 背包相关 ==========
    async getInventory() {
        return this.get('/inventory/items');
    }

    async useItem(itemId) {
        return this.post(`/inventory/use/${itemId}`, {});
    }

    async sellItem(itemId, count = 1) {
        return this.post(`/inventory/sell/${itemId}`, { count });
    }

    // ========== 装备相关 ==========
    async getEquipment() {
        return this.get('/equipment/equipped');
    }

    async equipItem(itemId) {
        return this.post('/equipment/equip', { itemId });
    }

    async unequipItem(slot) {
        return this.post('/equipment/unequip', { slot });
    }

    async getAvailableEquipment() {
        return this.get('/equipment/available');
    }

    // ========== 技能相关 ==========
    async getSkills() {
        return this.get('/skills/my');
    }

    async getSkillShop() {
        return this.get('/skills/shop');
    }

    async buySkill(skillId) {
        return this.post(`/skills/buy/${skillId}`, {});
    }

    async useSkill(skillId) {
        return this.post(`/skills/use/${skillId}`, {});
    }

    // ========== 宠物相关 ==========
    async getPets() {
        return this.get('/pets/my');
    }

    async getAvailablePets() {
        return this.get('/pets/available');
    }

    async buyPet(petId) {
        return this.post(`/pets/buy/${petId}`, {});
    }

    async feedPet(petId) {
        return this.post(`/pets/feed/${petId}`, {});
    }

    // ========== 任务相关 ==========
    async getQuests(type = 'daily') {
        return this.get(`/quests?type=${type}`);
    }

    async acceptQuest(questId) {
        return this.post(`/quests/accept/${questId}`, {});
    }

    async completeQuest(questId) {
        return this.post(`/quests/complete/${questId}`, {});
    }

    async claimReward(questId) {
        return this.post(`/quests/claim/${questId}`, {});
    }

    // ========== 商城相关 ==========
    async getShopItems(type = 'general') {
        return this.get(`/shop/items?type=${type}`);
    }

    async buyShopItem(itemId, count = 1) {
        return this.post(`/shop/buy/${itemId}`, { count });
    }

    // ========== 宗门相关 ==========
    async getGuildList() {
        return this.get('/guild/list');
    }

    async getMyGuild() {
        return this.get('/guild/my');
    }

    async createGuild(name, description) {
        return this.post('/guild/create', { name, description });
    }

    async applyGuild(guildId) {
        return this.post(`/guild/apply/${guildId}`, {});
    }

    async leaveGuild() {
        return this.post('/guild/leave', {});
    }

    async donateGuild(amount) {
        return this.post('/guild/donate', { amount });
    }

    // ========== 拍卖行相关 ==========
    async getAuctionItems(filters = {}) {
        const params = new URLSearchParams(filters).toString();
        return this.get(`/auction/items?${params}`);
    }

    async getMyAuctionItems() {
        return this.get('/auction/my-items');
    }

    async listAuctionItem(itemId, price, duration) {
        return this.post('/auction/list', { itemId, price, duration });
    }

    async buyAuctionItem(auctionId) {
        return this.post(`/auction/buy/${auctionId}`, {});
    }

    async cancelAuctionItem(auctionId) {
        return this.post(`/auction/cancel/${auctionId}`, {});
    }

    // ========== 邮件相关 ==========
    async getMails() {
        return this.get('/mail/list');
    }

    async readMail(mailId) {
        return this.post(`/mail/read/${mailId}`, {});
    }

    async collectMailAttachment(mailId) {
        return this.post(`/mail/collect/${mailId}`, {});
    }

    async deleteMail(mailId) {
        return this.delete(`/mail/${mailId}`);
    }

    // ========== 排行榜相关 ==========
    async getRanking(type = 'level') {
        return this.get(`/ranking?type=${type}`);
    }

    async getMyRanking() {
        return this.get('/ranking/my');
    }

    // ========== 成就相关 ==========
    async getAchievements() {
        return this.get('/achievement/list');
    }

    async claimAchievement(achievementId) {
        return this.post(`/achievement/claim/${achievementId}`, {});
    }

    // ========== 签到相关 ==========
    async getCheckinStatus() {
        return this.get('/checkin/status');
    }

    async doCheckin() {
        return this.post('/checkin/do', {});
    }

    // ========== VIP相关 ==========
    async getVipInfo() {
        return this.get('/vip/info');
    }

    // ========== 活动相关 ==========
    async getActivities() {
        return this.get('/activity/list');
    }

    async participateActivity(activityId) {
        return this.post(`/activity/participate/${activityId}`, {});
    }

    // ========== 叙事相关 ==========
    async getNpcList() {
        return this.get('/npc/list');
    }

    async interactNpc(npcId) {
        return this.post(`/npc/interact/${npcId}`, {});
    }

    // ========== 地图相关 ==========
    async getMapList() {
        return this.get('/maps/list');
    }

    async getCurrentMap() {
        return this.get('/maps/current');
    }

    async exploreMap(mapId) {
        return this.post(`/maps/explore/${mapId}`, {});
    }
}

// 创建全局游戏API实例
const gameAPI = new GameApi();

// 导出API客户端
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { GameApi, gameAPI };
}
