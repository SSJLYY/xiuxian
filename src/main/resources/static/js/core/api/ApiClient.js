/**
 * 基础API客户端类
 * 所有API客户端的基类,提供通用的HTTP请求功能
 */

import { HttpUtils } from '../utils/HttpUtils.js';
import { AuthStorage } from '../storage/AuthStorage.js';

class ApiClient {
    /**
     * 构造函数
     * @param {Object} config - 配置对象
     * @param {string} config.baseURL - API基础URL
     * @param {number} config.timeout - 请求超时时间(毫秒)
     */
    constructor(config = {}) {
        this.http = new HttpUtils({
            baseURL: config.baseURL || '/api',
            timeout: config.timeout || 10000
        });

        // 自动设置认证token
        this.syncAuthToken();
    }

    /**
     * 同步认证token
     */
    syncAuthToken() {
        const token = AuthStorage.getToken();
        if (token) {
            this.http.setAuthToken(token);
        }
    }

    /**
     * 重新设置认证token(登录/刷新token后调用)
     */
    updateAuthToken() {
        this.syncAuthToken();
    }

    /**
     * 清除认证token(退出登录后调用)
     */
    clearAuthToken() {
        this.http.clearAuthToken();
    }

    /**
     * 通用请求方法
     * @param {string} method - HTTP方法
     * @param {string} endpoint - API端点
     * @param {Object} data - 请求数据
     * @param {Object} options - 额外选项
     * @returns {Promise<Object>} API响应
     */
    async request(method, endpoint, data = null, options = {}) {
        // 确保token是最新的
        this.syncAuthToken();

        // 发送请求
        const response = await this.http.request(method, endpoint, data, options);

        // 检查是否需要重新登录
        if (response.code === 401 || response.code === 403) {
            // 清除认证信息
            AuthStorage.clearAuth();
            this.clearAuthToken();

            // 跳转到登录页
            if (window.location.pathname !== '/login.html') {
                window.location.href = '/login.html';
            }
        }

        return response;
    }

    /**
     * GET请求
     */
    async get(endpoint, options = {}) {
        return this.request('GET', endpoint, null, options);
    }

    /**
     * POST请求
     */
    async post(endpoint, data, options = {}) {
        return this.request('POST', endpoint, data, options);
    }

    /**
     * PUT请求
     */
    async put(endpoint, data, options = {}) {
        return this.request('PUT', endpoint, data, options);
    }

    /**
     * DELETE请求
     */
    async delete(endpoint, options = {}) {
        return this.request('DELETE', endpoint, null, options);
    }

    /**
     * 处理API响应(统一的响应格式处理)
     * @param {Object} response - API响应对象
     * @param {boolean} throwOnError - 错误时是否抛出异常
     * @returns {Object} 处理后的响应
     */
    handleResponse(response, throwOnError = false) {
        if (!response.success && throwOnError) {
            throw new Error(response.message || '操作失败');
        }

        return response;
    }

    /**
     * 批量请求
     * @param {Array} requests - 请求数组 [{method, endpoint, data}]
     * @returns {Promise<Array>} 响应数组
     */
    async batchRequest(requests) {
        return Promise.all(
            requests.map(req => this.request(req.method, req.endpoint, req.data))
        );
    }
}

// 导出基础API客户端
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ApiClient;
}
