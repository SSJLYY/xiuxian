/**
 * HTTP请求工具类
 * 封装常用的HTTP请求方法
 */

class HttpUtils {
    /**
     * 构造函数
     * @param {Object} config - 配置对象
     */
    constructor(config = {}) {
        this.baseURL = config.baseURL || '';
        this.timeout = config.timeout || 10000;
        this.defaultHeaders = config.defaultHeaders || {
            'Content-Type': 'application/json'
        };
    }

    /**
     * 设置认证token
     * @param {string} token - JWT token
     */
    setAuthToken(token) {
        this.defaultHeaders['Authorization'] = `Bearer ${token}`;
    }

    /**
     * 清除认证token
     */
    clearAuthToken() {
        delete this.defaultHeaders['Authorization'];
    }

    /**
     * 发送HTTP请求
     * @param {string} method - HTTP方法(GET/POST/PUT/DELETE)
     * @param {string} url - 请求URL
     * @param {Object} data - 请求数据
     * @param {Object} options - 额外选项
     * @returns {Promise<Object>} 响应数据
     */
    async request(method, url, data = null, options = {}) {
        const config = {
            method,
            headers: { ...this.defaultHeaders }
        };

        if (data && method !== 'GET') {
            config.body = JSON.stringify(data);
        }

        const fullUrl = this.baseURL + url;

        try {
            console.log(`HTTP请求: ${method} ${fullUrl}`);

            // 创建超时控制器
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), this.timeout);

            const response = await fetch(fullUrl, {
                ...config,
                ...options,
                signal: controller.signal
            });

            clearTimeout(timeoutId);

            // 处理响应
            return await this.handleResponse(response);

        } catch (error) {
            console.error('HTTP请求失败:', error);

            if (error.name === 'AbortError') {
                return {
                    success: false,
                    message: '请求超时,请稍后重试',
                    data: null
                };
            }

            return {
                success: false,
                message: '网络错误,请检查网络连接',
                data: null
            };
        }
    }

    /**
     * 处理HTTP响应
     * @param {Response} response - Fetch Response对象
     * @returns {Promise<Object>} 格式化的响应数据
     */
    async handleResponse(response) {
        // 处理401未授权
        if (response.status === 401) {
            this.clearAuthToken();
            return {
                success: false,
                message: '未授权,请重新登录',
                code: 401,
                data: null
            };
        }

        // 处理403禁止访问
        if (response.status === 403) {
            return {
                success: false,
                message: '权限不足,访问被拒绝',
                code: 403,
                data: null
            };
        }

        // 处理404未找到
        if (response.status === 404) {
            return {
                success: false,
                message: '请求的资源不存在',
                code: 404,
                data: null
            };
        }

        // 处理其他HTTP错误
        if (!response.ok) {
            const errorText = await response.text();
            return {
                success: false,
                message: `HTTP ${response.status}: ${response.statusText}`,
                code: response.status,
                data: null
            };
        }

        // 处理成功响应
        const text = await response.text();
        if (!text) {
            return {
                success: true,
                message: '操作成功',
                data: null
            };
        }

        try {
            return JSON.parse(text);
        } catch (error) {
            return {
                success: true,
                message: text,
                data: null
            };
        }
    }

    /**
     * GET请求
     */
    async get(url, options = {}) {
        return this.request('GET', url, null, options);
    }

    /**
     * POST请求
     */
    async post(url, data, options = {}) {
        return this.request('POST', url, data, options);
    }

    /**
     * PUT请求
     */
    async put(url, data, options = {}) {
        return this.request('PUT', url, data, options);
    }

    /**
     * DELETE请求
     */
    async delete(url, options = {}) {
        return this.request('DELETE', url, null, options);
    }
}

// 导出工具类
if (typeof module !== 'undefined' && module.exports) {
    module.exports = HttpUtils;
}
