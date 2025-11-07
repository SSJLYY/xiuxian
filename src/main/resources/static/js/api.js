// API 配置文件
const API_CONFIG = {
    baseURL: '/api',
    timeout: 10000
};

// HTTP 请求封装
class ApiClient {
    constructor() {
        this.baseURL = API_CONFIG.baseURL;
        this.token = localStorage.getItem('authToken');
    }

    // 设置认证令牌
    setToken(token) {
        this.token = token;
        localStorage.setItem('authToken', token);
    }

    // 清除认证令牌
    clearToken() {
        this.token = null;
        localStorage.removeItem('authToken');
    }

    // 通用请求方法
    async request(method, url, data = null, options = {}) {
        const config = {
            method,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        };

        if (this.token) {
            config.headers['Authorization'] = `Bearer ${this.token}`;
        }

        if (data && method !== 'GET') {
            config.body = JSON.stringify(data);
        } else if (data && method === 'GET') {
            const params = new URLSearchParams(data);
            url += '?' + params.toString();
        }

        try {
            showLoading(true);
            const response = await fetch(this.baseURL + url, config);
            
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}`);
            }

            const result = await response.json();
            showLoading(false);
            return result;
        } catch (error) {
            showLoading(false);
            console.error('API Request Error:', error);
            showToast(error.message || '网络请求失败', 'error');
            throw error;
        }
    }

    // GET 请求
    async get(url, params = {}) {
        return this.request('GET', url, params);
    }

    // POST 请求
    async post(url, data = {}) {
        return this.request('POST', url, data);
    }

    // PUT 请求
    async put(url, data = {}) {
        return this.request('PUT', url, data);
    }

    // DELETE 请求
    async delete(url) {
        return this.request('DELETE', url);
    }
}

// 创建 API 客户端实例
const api = new ApiClient();

// 游戏 API 方法
const gameAPI = {
    // 认证相关
    async login(username, password) {
        const response = await api.post('/auth/login', { username, password });
        if (response.success) {
            api.setToken(response.data.token);
        }
        return response;
    },

    async register(username, nickname, email, password) {
        return await api.post('/auth/register', { username, nickname, email, password });
    },

    async getCurrentUser() {
        return await api.get('/auth/me');
    },

    // 玩家相关
    async getPlayerProfile() {
        return await api.get('/player/profile');
    },

    async startCultivation() {
        return await api.post('/player/cultivate');
    },

    async claimOfflineRewards() {
        return await api.post('/player/claim-offline-rewards');
    },

    // 技能相关
    async getSkills() {
        return await api.get('/skills');
    },

    async learnSkill(skillId) {
        return await api.post('/skills/learn', { skillId });
    },

    async upgradeSkill(skillId) {
        return await api.post('/skills/upgrade', { skillId });
    },

    async equipSkill(skillId) {
        return await api.post('/skills/equip', { skillId });
    },

    // 装备相关
    async getEquipment() {
        return await api.get('/equipment');
    },

    async equipItem(equipmentId) {
        return await api.post('/equipment/equip', { equipmentId });
    },

    async unequipItem(slotType) {
        return await api.post('/equipment/unequip', { slotType });
    },

    async repairEquipment(equipmentId) {
        return await api.post('/equipment/repair', { equipmentId });
    },

    // 背包相关
    async getInventory() {
        return await api.get('/inventory');
    },

    async useItem(itemId) {
        return await api.post('/inventory/use-item', { itemId });
    },

    async addItem(itemId, quantity = 1) {
        return await api.post('/inventory/add-item', { itemId, quantity });
    },

    async removeItem(itemId, quantity = 1) {
        return await api.post('/inventory/remove-item', { itemId, quantity });
    },

    // 任务相关
    async getQuests() {
        return await api.get('/quests');
    },

    async updateQuestProgress(questId, progress) {
        return await api.post('/quests/update-progress', { questId, progress });
    },

    async claimQuestReward(questId) {
        return await api.post('/quests/claim-reward', { questId });
    },

    // 商店相关
    async getShopItems(shopType) {
        return await api.get(`/shop/${shopType}`);
    },

    async buyItem(shopItemId, quantity = 1) {
        return await api.post('/shop/buy', { shopItemId, quantity });
    },

    async sellItem(itemId, quantity = 1) {
        return await api.post('/shop/sell', { itemId, quantity });
    }
};

// 导出 API
window.gameAPI = gameAPI;
window.api = api;