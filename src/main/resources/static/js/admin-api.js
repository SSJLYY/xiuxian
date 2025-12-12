// 管理员API配置 - 独立于游戏API
const ADMIN_API_CONFIG = {
    baseURL: '/api/admin',
    timeout: 10000
};

// 管理员HTTP请求客户端
class AdminApiClient {
    constructor() {
        this.baseURL = ADMIN_API_CONFIG.baseURL;
        this.token = localStorage.getItem('adminToken'); // 使用独立的adminToken
    }

    setToken(token) {
        this.token = token;
        localStorage.setItem('adminToken', token);
    }

    clearToken() {
        this.token = null;
        localStorage.removeItem('adminToken');
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
            console.log(`管理员API请求: ${method} ${this.baseURL}${url}`);
            const response = await fetch(this.baseURL + url, config);

            // 处理401未授权
            if (response.status === 401) {
                this.clearToken();
                console.error('管理员401未授权，清除token');
                return {
                    success: false,
                    code: 401,
                    message: '管理员认证失败，请重新登录',
                    data: null
                };
            }

            // 处理403禁止访问
            if (response.status === 403) {
                console.error('管理员403禁止访问');
                return {
                    success: false,
                    code: 403,
                    message: '管理员权限不足，访问被拒绝',
                    data: null
                };
            }

            // 处理其他HTTP错误
            if (!response.ok) {
                const errorText = await response.text();
                let errorData;
                try {
                    errorData = JSON.parse(errorText);
                } catch {
                    errorData = { message: `HTTP ${response.status}: ${response.statusText}` };
                }

                console.error(`管理员API错误 ${response.status}:`, errorData);
                return {
                    success: false,
                    code: response.status,
                    message: errorData.message || `HTTP ${response.status}`,
                    data: null
                };
            }

            // 解析成功响应
            const responseData = await response.json();
            console.log(`管理员API响应成功: ${method} ${url}`, responseData);
            
            // 处理不同的响应格式
            if (responseData.hasOwnProperty('success')) {
                // AdminApiResponse格式
                return responseData;
            } else if (responseData.hasOwnProperty('code')) {
                // ApiResponse格式，转换为统一格式
                return {
                    success: responseData.code === 200,
                    code: responseData.code,
                    message: responseData.message,
                    data: responseData.data
                };
            } else {
                // 其他格式，假设成功
                return {
                    success: true,
                    code: 200,
                    message: 'success',
                    data: responseData
                };
            }

        } catch (error) {
            console.error('管理员API请求异常:', method, url, error);
            return {
                success: false,
                code: -1,
                message: error.message || '网络请求失败，请检查网络连接',
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

// 创建管理员API客户端实例
const adminApi = new AdminApiClient();

// 管理员API方法
const adminAPI = {
    // 认证相关
    async login(username, password) {
        const response = await adminApi.post('/auth/login', { username, password });
        if (response.success && response.data?.token) {
            adminApi.setToken(response.data.token);
        }
        return response;
    },

    async validateToken() {
        return await adminApi.get('/auth/validate');
    },

    async logout() {
        const response = await adminApi.post('/auth/logout');
        adminApi.clearToken();
        return response;
    },

    async getCurrentAdmin() {
        return await adminApi.get('/auth/me');
    },

    // 仪表板统计
    async getDashboardStats() {
        return await adminApi.get('/dashboard/stats');
    },

    // 玩家管理
    async getPlayers(params = {}) {
        return await adminApi.get('/players', params);
    },

    async getPlayerDetail(playerId) {
        return await adminApi.get(`/players/${playerId}`);
    },

    async banPlayer(playerId, reason = '') {
        return await adminApi.post(`/players/${playerId}/ban`, { reason });
    },

    async unbanPlayer(playerId) {
        return await adminApi.post(`/players/${playerId}/unban`);
    },

    // 内容管理
    async getContentStats() {
        return await adminApi.get('/content/stats');
    },

    async getContentList(type, params = {}) {
        return await adminApi.get(`/content/${type}`, params);
    },

    async createContent(type, data) {
        return await adminApi.post(`/content/${type}`, data);
    },

    async updateContent(type, id, data) {
        return await adminApi.put(`/content/${type}/${id}`, data);
    },

    async deleteContent(type, id) {
        return await adminApi.delete(`/content/${type}/${id}`);
    },

    // 数据统计
    async getOverallStats() {
        return await adminApi.get('/statistics/overall');
    },

    async getRecentStats(days = 7) {
        return await adminApi.get(`/statistics/recent?days=${days}`);
    },

    async getRevenueStats(days = 7) {
        return await adminApi.get(`/statistics/revenue?days=${days}`);
    },

    async getPlayerGrowthStats(days = 7) {
        return await adminApi.get(`/statistics/player-growth?days=${days}`);
    },

    // 系统监控
    async getMonitoringInfo() {
        return await adminApi.get('/monitoring/all');
    },

    // 反馈管理
    async getFeedbackList(params = {}) {
        return await adminApi.get('/feedback', params);
    },

    async markFeedbackAsRead(feedbackId) {
        return await adminApi.post(`/feedback/${feedbackId}/read`);
    },

    async replyFeedback(feedbackId, replyContent) {
        return await adminApi.post(`/feedback/${feedbackId}/reply`, { replyContent });
    },

    async deleteFeedback(feedbackId) {
        return await adminApi.delete(`/feedback/${feedbackId}`);
    },

    // 日志管理
    async getLogs(params = {}) {
        return await adminApi.get('/logs', params);
    },

    // 配置管理
    async getConfigs() {
        return await adminApi.get('/configs');
    },

    async updateConfig(key, value) {
        return await adminApi.put(`/configs/${key}`, { value });
    }
};

// 导出到全局
window.adminAPI = adminAPI;
window.adminApi = adminApi;