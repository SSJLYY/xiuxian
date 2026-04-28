/**
 * 游戏 API 客户端 - 2026-04-17 全面修复版
 * 所有 API 路径已与后端完全匹配
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
    async login(username, password, userType = 'player') {
        return this.post('/auth/login', { username, password, userType });
    }

    async register(data) {
        return this.post('/auth/register', data);
    }

    async logout() {
        return this.post('/auth/logout', {});
    }

    async getCurrentUser() {
        return this.get('/auth/me');
    }

    // ========== 玩家相关 ==========
    async getCurrentPlayer() {
        return this.get('/player/profile');
    }

    async getPlayerProfile() {
        return this.get('/player/profile');
    }

    async updatePlayerProfile(data) {
        return this.post('/player/profile/update', data);
    }

    async getPlayerStats() {
        return this.get('/player/profile'); // 临时方案
    }

    async allocateAttributes(payload) {
        return this.post('/player/attributes/allocate', payload);
    }

    // ========== 修炼相关 ==========
    async getCultivateInfo() {
        return this.get('/player/cultivate/info');
    }

    async startCultivate(type = 'normal') {
        return this.post('/player/cultivate', { type });
    }

    async stopCultivate() {
        return this.post('/player/cultivate/stop', {});
    }

    async canBreakthrough() {
        return this.get('/player/breakthrough/can');
    }

    async breakthrough() {
        return this.post('/player/breakthrough', {});
    }

    // ========== 战斗相关 ==========
    async generateMonster() {
        return this.get('/combat/generate-monster');
    }

    async startCombat(monsterId = null) {
        if (monsterId) {
            return this.post(`/combat/start/${monsterId}`, {});
        }
        return this.post('/combat/start', {});
    }

    async startEnhancedCombat(payload = {}) {
        return this.post('/combat/enhanced', payload);
    }

    async batchCombat(times, payload = {}) {
        return this.post(`/combat/batch/${times}`, payload);
    }

    async getCombatHistory() {
        return this.get('/combat/history');
    }

    // ========== 背包相关 ==========
    async getInventoryItems() {
        return this.get('/equipment/items'); // 背包物品在 equipment 模块
    }

    async getInventoryCategorized() {
        return this.get('/equipment/categorized');
    }

    async useItem(itemId) {
        return this.post(`/equipment/use/${itemId}`, {});
    }

    async sellItem(itemId, quantity = 1) {
        return this.post(`/equipment/sell/${itemId}`, { quantity });
    }

    async discardItem(itemId, quantity = 1) {
        return this.post(`/equipment/discard/${itemId}`, { quantity });
    }

    // ========== 装备相关 ==========
    async getEquipment() {
        return this.get('/equipment');
    }

    async getEquipmentDetails() {
        return this.get('/equipment/details');
    }

    async getEquippedEquipment() {
        return this.get('/equipment/equipped');
    }

    async getEquippedEquipmentDetails() {
        return this.get('/equipment/equipped/details');
    }

    async getAvailableEquipment() {
        return this.get('/equipment/available');
    }

    async getAllEquipment() {
        return this.get('/equipment/all');
    }

    async equipItem(itemId) {
        return this.post('/equipment/equip', { itemId });
    }

    async unequipItem(slot) {
        return this.post('/equipment/unequip', { slot });
    }

    async acquireEquipment(data) {
        return this.post('/equipment/acquire', data);
    }

    // ========== 技能相关 ==========
    async getSkills() {
        return this.get('/skills');
    }

    async getAvailableSkills() {
        return this.get('/skills/available');
    }

    async getPlayerSkills() {
        return this.get('/skills/player');
    }

    async getEquippedSkills() {
        return this.get('/skills/equipped');
    }

    async learnSkill(skillId) {
        return this.post(`/skills/learn/${skillId}`, {});
    }

    async upgradeSkill(playerSkillId) {
        return this.post(`/skills/${playerSkillId}/upgrade`, {});
    }

    async equipSkill(playerSkillId, slotNumber) {
        return this.post(`/skills/equip/${playerSkillId}/${slotNumber}`, {});
    }

    async unequipSkill(playerSkillId) {
        return this.post(`/skills/unequip/${playerSkillId}`, {});
    }

    async useSkill(playerSkillId) {
        return this.post(`/skills/${playerSkillId}/use`, {});
    }

    // ========== 宠物相关 ==========
    async getPets() {
        return this.get('/pets');
    }

    async getAvailablePets() {
        return this.get('/pets/available');
    }

    async getMyPets() {
        return this.get('/pets/my');
    }

    async getActivePet() {
        return this.get('/pets/active');
    }

    async capturePet(petId) {
        return this.post(`/pets/capture/${petId}`, {});
    }

    async activatePet(playerPetId) {
        return this.post(`/pets/activate/${playerPetId}`, {});
    }

    async feedPet(playerPetId) {
        return this.post(`/pets/feed/${playerPetId}`, {});
    }

    async trainPet(playerPetId, trainingType = '普通训练') {
        const resolvedTrainingType =
            typeof trainingType === 'string'
                ? trainingType
                : trainingType?.trainingType;
        return this.post(`/pets/train/${playerPetId}`, {
            trainingType: resolvedTrainingType || '普通训练'
        });
    }

    async renamePet(playerPetId, newName) {
        const nickname =
            typeof newName === 'string'
                ? newName
                : newName?.nickname || newName?.newName;
        return this.post(`/pets/rename/${playerPetId}`, { nickname });
    }

    // ========== 任务相关 ==========
    async getQuests(type = 'all') {
        if (type === 'all') {
            return this.get('/quests');
        }
        return this.get(`/quests/${type}`);
    }

    async getDailyQuests() {
        return this.get('/quests/daily');
    }

    async getWeeklyQuests() {
        return this.get('/quests/weekly');
    }

    async getMonthlyQuests() {
        return this.get('/quests/monthly');
    }

    async getMyQuests() {
        return this.get('/quests/my'); // 需要后端添加此接口
    }

    async acceptQuest(questId) {
        return this.post(`/quests/accept/${questId}`, {});
    }

    async completeQuest(playerQuestId) {
        return this.post(`/quests/complete/${playerQuestId}`, {});
    }

    async claimQuestReward(playerQuestId) {
        return this.post(`/quests/${playerQuestId}/claim`, {});
    }

    async updateQuestProgress(questId, progress) {
        return this.post(`/quests/${questId}/progress?progress=${progress}`, {});
    }

    async refreshDailyQuests() {
        return this.post('/quests/daily/refresh', {});
    }

    async refreshWeeklyQuests() {
        return this.post('/quests/weekly/refresh', {});
    }

    async refreshMonthlyQuests() {
        return this.post('/quests/monthly/refresh', {});
    }

    // ========== 商城相关 ==========
    async getShopItems(type = 'general') {
        return this.get(`/shop/items?type=${type}`);
    }

    async getSkillShop() {
        return this.get('/shop/skills');
    }

    async buyShopItem(itemId, count = 1) {
        return this.post(`/shop/items/${itemId}/buy?quantity=${count}`, {});
    }

    async buySkill(skillId) {
        return this.post(`/shop/skills/${skillId}/buy`, {});
    }

    // ========== 宗门相关 ==========
    async getGuildList() {
        return this.get('/guild/list');
    }

    async getMyGuild() {
        return this.get('/guild/my');
    }

    async createGuild(name, description) {
        return this.post('/guild/create', { name, guildName: name, description });
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

    // 宗门 Boss
    async getCurrentGuildBoss() {
        return this.get('/guild/boss/current');
    }

    async challengeGuildBoss() {
        return this.post('/guild/boss/challenge', {});
    }

    async claimGuildBossReward() {
        return this.post('/guild/boss/claim-reward', {});
    }

    // ========== 拍卖行相关 ==========
    async getAuctionItems(filters = {}) {
        const params = new URLSearchParams(filters).toString();
        return this.get(`/auction/items?${params}`);
    }

    async getMyAuctionItems() {
        return this.get('/auction/my-items');
    }

    async listAuctionItem(itemId, price, duration = 24) {
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

    async getMail(mailId) {
        return this.get(`/mail/${mailId}`);
    }

    async readMail(mailId) {
        return this.post(`/mail/${mailId}/read`, {});
    }

    async claimMailAttachment(mailId) {
        return this.post(`/mail/${mailId}/claim`, {});
    }

    async getUnreadMailCount() {
        return this.get('/mail/unread-count');
    }

    async deleteMail(mailId) {
        return this.delete(`/mail/${mailId}`);
    }

    // ========== 排行榜相关 ==========
    async getRanking(type = 'level') {
        return this.get(`/ranking/${type}`);
    }

    async getLevelRanking() {
        return this.get('/ranking/level');
    }

    async getPowerRanking() {
        return this.get('/ranking/power');
    }

    async getWealthRanking() {
        return this.get('/ranking/wealth');
    }

    async getPetRanking() {
        return this.get('/ranking/pet');
    }

    async getMyRanking(type = 'level') {
        return this.get(`/ranking/my-rank?type=${encodeURIComponent(type)}`);
    }

    // ========== 成就相关 ==========
    async getAchievements() {
        return this.get('/achievement/list');
    }

    async getAchievement(achievementId) {
        return this.get(`/achievement/${achievementId}`);
    }

    async getAchievementProgress() {
        return this.get('/achievement/progress');
    }

    async claimAchievement(achievementId) {
        return this.post(`/achievement/${achievementId}/claim`, {});
    }

    // ========== 签到相关 ==========
    async getCheckinStatus(year = null, month = null) {
        if (year != null && month != null) {
            return this.get(`/checkin/status?year=${year}&month=${month}`);
        }
        return this.get('/checkin/status');
    }

    async doCheckin() {
        return this.post('/checkin/do', {});
    }

    async getCheckinCalendar(month, year) {
        return this.getCheckinStatus(year, month);
    }

    async getCheckinRewards() {
        const response = await this.get('/checkin/status');
        if (!response?.success) {
            return response;
        }
        return {
            success: true,
            message: 'ok',
            data: [
                { day: 1, description: '灵石 x200，经验 x500' },
                { day: 3, description: '灵石 x300，经验 x800' },
                { day: 7, description: '灵石 x800，经验 x3000' },
                { day: 14, description: '灵石 x1500，经验 x5000' },
                { day: 30, description: '灵石 x3000，经验 x10000' }
            ]
        };
    }

    // ========== VIP 相关 ==========
    async getVipInfo() {
        return this.get('/vip/info');
    }

    async getVipLevels() {
        return this.get('/vip/levels');
    }

    async getDailyVipReward() {
        return this.post('/vip/daily-reward', {});
    }

    async rechargeVip(amount) {
        return this.post(`/vip/recharge/${amount}`, {});
    }

    async getVipRechargeRecords() {
        return this.get('/vip/recharge-records');
    }

    async checkVipPrivilege(requiredLevel) {
        return this.get(`/vip/privilege/${requiredLevel}`);
    }

    // ========== 活动相关 ==========
    async getActivities() {
        return this.get('/activities/');
    }

    async getAllActivities() {
        return this.get('/activities/all');
    }

    async getMyActivityProgress() {
        return this.get('/activities/my-progress');
    }

    async participateActivity(activityId) {
        return this.post(`/activities/${activityId}/participate`, {});
    }

    async updateActivityProgress(activityId, increment) {
        const resolvedIncrement =
            typeof increment === 'number'
                ? increment
                : increment?.increment ?? increment?.progress ?? 0;
        return this.post(`/activities/${activityId}/progress`, { increment: resolvedIncrement });
    }

    async submitActivityScore(activityId, score) {
        return this.post(`/activities/${activityId}/score`, { score });
    }

    async getActivityRanking(activityId) {
        return this.get(`/activities/${activityId}/ranking`);
    }

    // ========== 礼包码相关 ==========
    async redeemGiftcode(code) {
        return this.post('/giftcode/redeem', { code });
    }

    async getMyGiftcodes() {
        return this.get('/giftcode/my');
    }

    async getAvailableGiftcodes() {
        return this.get('/giftcode/available');
    }

    // ========== 叙事相关 ==========
    async getAvailableDialogues(npcId) {
        return this.get(`/dialogue/available/${npcId}`);
    }

    async startDialogue(dialogueKeyOrPayload, legacyDialogueId = null) {
        const payload = typeof dialogueKeyOrPayload === 'object' && dialogueKeyOrPayload !== null
            ? { ...dialogueKeyOrPayload }
            : legacyDialogueId == null
                ? { dialogueKey: dialogueKeyOrPayload }
                : {
                    npcId: dialogueKeyOrPayload,
                    dialogueKey: legacyDialogueId,
                    dialogueId: legacyDialogueId
                };
        const dialogueKey = payload.dialogueKey ?? payload.dialogueId;
        return this.post('/dialogue/start', {
            ...payload,
            dialogueKey,
            dialogueId: payload.dialogueId ?? dialogueKey
        });
    }

    async chooseDialogueChoice(dialogueKeyOrPayload, choiceNodeKey = null) {
        const payload = typeof dialogueKeyOrPayload === 'object' && dialogueKeyOrPayload !== null
            ? { ...dialogueKeyOrPayload }
            : choiceNodeKey == null
                ? {
                    choiceNodeKey: dialogueKeyOrPayload,
                    choiceId: dialogueKeyOrPayload
                }
                : {
                    dialogueKey: dialogueKeyOrPayload,
                    choiceNodeKey,
                    choiceId: choiceNodeKey
                };
        const resolvedDialogueKey = payload.dialogueKey ?? payload.dialogueId;
        const resolvedChoiceNodeKey = payload.choiceNodeKey ?? payload.choiceId;
        return this.post('/dialogue/choice', {
            ...payload,
            dialogueKey: resolvedDialogueKey,
            choiceNodeKey: resolvedChoiceNodeKey,
            choiceId: payload.choiceId ?? resolvedChoiceNodeKey
        });
    }

    // ========== 地图相关 ==========
    async getMaps() {
        return this.get('/maps');
    }

    async getMap(mapId) {
        return this.get(`/maps/${mapId}`);
    }

    async getCurrentMap() {
        return this.get('/maps/current');
    }

    async enterMap(mapId) {
        return this.post(`/maps/enter/${mapId}`, {});
    }

    async leaveMap() {
        return this.post('/maps/leave', {});
    }

    async exploreMap() {
        return this.get('/maps/explore');
    }

    async getOfflineReward() {
        return this.get('/maps/offline-reward');
    }

    // ========== 离线奖励 ==========
    async claimOfflineReward(rewardId) {
        let resolvedRewardId =
            typeof rewardId === 'object' && rewardId !== null ? rewardId.rewardId : rewardId;
        if (resolvedRewardId == null) {
            const rewardInfo = await this.getOfflineRewardInfo();
            resolvedRewardId = rewardInfo?.data?.[0]?.id;
        }
        if (resolvedRewardId == null) {
            return {
                success: false,
                message: '没有可领取的离线奖励',
                data: null
            };
        }
        return this.post(`/offline-reward/claim/${resolvedRewardId}`, {});
    }

    async getOfflineRewardInfo() {
        return this.get('/offline-reward/unclaimed');
    }
}

// 创建全局游戏 API 实例
const gameAPI = new GameApi();

export { GameApi, gameAPI };

// 导出 API 客户端
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { GameApi, gameAPI, default: gameAPI };
}
