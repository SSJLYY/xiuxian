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

// 修复后的前端API调用（完全去模拟化）
const gameAPI = {
    // 认证相关
    async login(username, password) {
        const response = await api.post('/auth/login', { username, password });
        if (response.success && response.data && response.data.token) {
            api.setToken(response.data.token);
        }
        return response;
    },

    async register(userData) {
        return await api.post('/auth/register', userData);
    },

    async getCurrentUser() {
        return await api.get('/auth/me');
    },

    async logout() {
        const response = await api.post('/auth/logout');
        api.clearToken();
        return response;
    },

    // 玩家相关
    async getPlayerProfile() {
        return await api.get('/player/profile');
    },

    // 修炼相关
    async startCultivation() {
        return await api.post('/player/cultivate');
    },

    async stopCultivation() {
        return await api.post('/player/cultivate/stop');
    },

    async claimOfflineRewards() {
        return await api.post('/player/claim-offline-rewards');
    },

    async addExperience(amount) {
        return await api.post('/player/add-experience', { amount: amount });
    },

    async addSpiritStones(amount) {
        return await api.post('/player/add-spirit-stones', { amount: amount });
    },

    // 以下端点在后端不存在，直接返回错误
    async getCultivationProgress() {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async saveCultivationProgress(data) {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async getSkills() {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async useSkill(skillId) {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async getEquipment() {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async equipItem(itemId) {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async getInventory() {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async useItem(itemId) {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async getQuests() {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async acceptQuest(questId) {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async completeQuest(questId) {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async quickSave() {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    },

    async checkLevelUp() {
        return {
            success: false,
            message: 'API端点不存在',
            data: null
        };
    }
};

// 导出 API
window.gameAPI = gameAPI;
window.api = api;