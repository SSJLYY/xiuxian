// API配置
const API_CONFIG = {
    baseURL: '/api',
    timeout: 10000
};

// HTTP请求客户端
class ApiClient {
    constructor() {
        this.baseURL = API_CONFIG.baseURL;
        this.token = localStorage.getItem('authToken');
    }

    setToken(token) {
        this.token = token;
        localStorage.setItem('authToken', token);
    }

    clearToken() {
        this.token = null;
        localStorage.removeItem('authToken');
    }

    async request(method, url, data = null) {
        const config = {
            method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        if (this.token) {
            config.headers['Authorization'] = `Bearer ${this.token}`;
        }

        if (data && method !== 'GET') {
            config.body = JSON.stringify(data);
        }

        try {
            const response = await fetch(this.baseURL + url, config);

            if (!response.ok) {
                // 处理401未授权和403禁止访问
                if (response.status === 401) {
                    this.clearToken();
                    throw new Error('未授权，请重新登录');
                } else if (response.status === 403) {
                    throw new Error('权限不足');
                }

                const errorText = await response.text();
                let errorData;
                try {
                    errorData = JSON.parse(errorText);
                } catch {
                    errorData = { message: `HTTP ${response.status}: ${response.statusText}` };
                }

                return {
                    success: false,
                    message: errorData.message || `HTTP ${response.status}`,
                    data: null
                };
            }

            const responseData = await response.json();
            return responseData;

        } catch (error) {
            console.error('API请求错误:', error);
            return {
                success: false,
                message: error.message || '网络请求失败',
                data: null
            };
        }
    }

    async get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;
        return this.request('GET', fullUrl);
    }

    async post(url, data = {}) {
        return this.request('POST', url, data);
    }

    async put(url, data = {}) {
        return this.request('PUT', url, data);
    }

    async delete(url) {
        return this.request('DELETE', url);
    }
}

// 创建API客户端实例
const api = new ApiClient();

// 游戏API方法
const gameAPI = {
    // 认证相关
    async login(username, password) {
        const response = await api.post('/auth/login', { username, password });
        if (response.success && response.data?.token) {
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

    async validateToken() {
        return await api.get('/auth/validate');
    },

    async logout() {
        const response = await api.post('/auth/logout');
        api.clearToken();
        return response;
    },

    // 玩家相关
    async getCurrentPlayerProfile() {
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

    async resetCultivation() {
        return await api.post('/player/reset-cultivation');
    },

    // 公共API
    async getPlayerPublicInfo(playerId) {
        return await api.get(`/public/players/${playerId}`);
    },

    async getLeaderboard() {
        return await api.get('/public/leaderboard');
    },

    // 技能相关API
    async getSkills() {
        return await api.get('/skills/player');
    },

    // 装备相关API
    async getEquipment() {
        return await api.get('/equipment/equipped');
    },

    // 背包相关API
    async getInventory() {
        return await api.get('/inventory');
    },

    // 任务相关API
    async getQuests() {
        return await api.get('/quests');
    },
    async getDailyQuests() {
        return await api.get('/quests/daily');
    },
    async refreshDailyQuests() {
        return await api.post('/quests/daily/refresh');
    },
    async getWeeklyQuests() {
        return await api.get('/quests/weekly');
    },
    async refreshWeeklyQuests() {
        return await api.post('/quests/weekly/refresh');
    },
    async getMonthlyQuests() {
        return await api.get('/quests/monthly');
    },
    async refreshMonthlyQuests() {
        return await api.post('/quests/monthly/refresh');
    },
    
    // 领取任务奖励
    async claimQuestReward(playerQuestId) {
        return await api.post(`/quests/${playerQuestId}/claim`);
    },

    // 学习技能
    async learnSkill(skillId) {
        return await api.post(`/skills/learn/${skillId}`);
    },

    // 使用技能
    async useSkill(playerSkillId) {
        return await api.post(`/skills/${playerSkillId}/use`);
    }
    ,
    async upgradeSkillByPoints(playerSkillId) {
        return await api.post(`/skills/${playerSkillId}/upgrade-by-points`);
    }
    ,
    async allocateAttributes(payload) {
        return await api.post('/player/attributes/allocate', payload);
    }
    ,
    async getSkillShop() {
        return await api.get('/shop/skills');
    }
    ,
    async buySkill(shopItemId) {
        return await api.post(`/shop/skills/${shopItemId}/buy`);
    }
    ,
    async sellSkill(playerSkillId) {
        return await api.post(`/shop/skills/sell/${playerSkillId}`);
    }
};

// 导出到全局
window.gameAPI = gameAPI;
window.api = api;

// 全局领取任务奖励函数
window.claimQuest = async function(playerQuestId) {
    try {
        const response = await gameAPI.claimQuestReward(playerQuestId);
        if (response && response.success) {
            if (window.authManager && window.authManager.loadPlayerProfile) {
                await window.authManager.loadPlayerProfile();
            }
            if (window.gameManager && window.gameManager.addCultivationLog) {
                window.gameManager.addCultivationLog('任务奖励领取成功');
            }
        } else {
            throw new Error(response?.message || '领取任务奖励失败');
        }
    } catch (error) {
        console.error('领取任务奖励失败:', error);
        if (window.gameManager && window.gameManager.addCultivationLog) {
            window.gameManager.addCultivationLog('领取任务奖励失败: ' + error.message);
        }
    }
};

// 全局技能使用函数
window.useSkill = async function(skillId) {
    try {
        const playerSkillsResponse = await gameAPI.getSkills();
        if (!playerSkillsResponse || !playerSkillsResponse.success) {
            throw new Error('获取玩家技能失败');
        }
        const existingSkill = playerSkillsResponse.data?.find(ps => ps.skill?.id === skillId);
        if (existingSkill) {
            const useResult = await gameAPI.useSkill(existingSkill.id);
            if (!useResult || !useResult.success) {
                throw new Error(useResult?.message || '技能使用失败');
            }
        } else {
            const learnResult = await gameAPI.learnSkill(skillId);
            if (!learnResult || !learnResult.success) {
                throw new Error('技能学习失败: ' + (learnResult?.message || '未知错误'));
            }
            const useResult = await gameAPI.useSkill(learnResult.data.id);
            if (!useResult || !useResult.success) {
                throw new Error(useResult?.message || '技能使用失败');
            }
        }
        if (window.authManager && window.authManager.loadPlayerProfile) {
            await window.authManager.loadPlayerProfile();
        }
        if (window.gameManager && window.gameManager.addCultivationLog) {
            window.gameManager.addCultivationLog('技能使用成功');
        }
    } catch (error) {
        console.error('技能使用失败:', error);
        if (window.gameManager && window.gameManager.addCultivationLog) {
            window.gameManager.addCultivationLog('技能使用失败: ' + error.message);
        }
    }
};