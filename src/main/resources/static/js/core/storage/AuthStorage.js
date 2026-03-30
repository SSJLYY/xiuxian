/**
 * 认证信息存储工具类
 * 专门用于存储和管理认证相关的信息
 */

import { storage } from './Storage.js';

class AuthStorage {
    /**
     * 存储认证token
     * @param {string} token - JWT token
     */
    static setToken(token) {
        storage.set('authToken', token);
    }

    /**
     * 获取认证token
     * @returns {string|null} token
     */
    static getToken() {
        return storage.get('authToken', null);
    }

    /**
     * 删除认证token
     */
    static removeToken() {
        storage.remove('authToken');
    }

    /**
     * 检查是否已登录
     * @returns {boolean} 是否已登录
     */
    static isLoggedIn() {
        return this.getToken() !== null;
    }

    /**
     * 存储用户信息
     * @param {Object} userInfo - 用户信息对象
     */
    static setUserInfo(userInfo) {
        storage.set('userInfo', userInfo);
    }

    /**
     * 获取用户信息
     * @returns {Object|null} 用户信息
     */
    static getUserInfo() {
        return storage.get('userInfo', null);
    }

    /**
     * 删除用户信息
     */
    static removeUserInfo() {
        storage.remove('userInfo');
    }

    /**
     * 存储游戏设置
     * @param {Object} settings - 设置对象
     */
    static setSettings(settings) {
        storage.set('gameSettings', settings);
    }

    /**
     * 获取游戏设置
     * @param {Object} defaultSettings - 默认设置
     * @returns {Object} 设置对象
     */
    static getSettings(defaultSettings = {}) {
        return storage.get('gameSettings', defaultSettings);
    }

    /**
     * 清除所有认证信息(退出登录时调用)
     */
    static clearAuth() {
        this.removeToken();
        this.removeUserInfo();
        // 保留游戏设置,不清除
    }

    /**
     * 完全清除(包括设置)
     */
    static clearAll() {
        storage.clear();
    }
}

// 导出工具类
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AuthStorage;
}
