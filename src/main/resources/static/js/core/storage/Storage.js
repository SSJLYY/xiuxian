/**
 * 本地存储工具类
 * 封装localStorage操作,提供类型安全的存储API
 */

class Storage {
    /**
     * 构造函数
     * @param {string} prefix - 存储键前缀
     */
    constructor(prefix = 'xiuxian_') {
        this.prefix = prefix;
        this.isEnabled = this.checkStorageEnabled();
    }

    /**
     * 检查localStorage是否可用
     * @returns {boolean} 是否可用
     */
    checkStorageEnabled() {
        try {
            const testKey = '__storage_test__';
            localStorage.setItem(testKey, 'test');
            localStorage.removeItem(testKey);
            return true;
        } catch (error) {
            console.error('localStorage不可用:', error);
            return false;
        }
    }

    /**
     * 生成存储键
     * @param {string} key - 原始键
     * @returns {string} 带前缀的键
     */
    getKey(key) {
        return this.prefix + key;
    }

    /**
     * 存储数据
     * @param {string} key - 键
     * @param {*} value - 值
     * @returns {boolean} 是否成功
     */
    set(key, value) {
        if (!this.isEnabled) {
            console.warn('localStorage不可用,无法存储数据');
            return false;
        }

        try {
            const serialized = JSON.stringify(value);
            localStorage.setItem(this.getKey(key), serialized);
            return true;
        } catch (error) {
            console.error('存储数据失败:', error);
            return false;
        }
    }

    /**
     * 获取数据
     * @param {string} key - 键
     * @param {*} defaultValue - 默认值
     * @returns {*} 存储的值或默认值
     */
    get(key, defaultValue = null) {
        if (!this.isEnabled) {
            return defaultValue;
        }

        try {
            const item = localStorage.getItem(this.getKey(key));
            if (item === null) {
                return defaultValue;
            }
            return JSON.parse(item);
        } catch (error) {
            console.error('获取数据失败:', error);
            return defaultValue;
        }
    }

    /**
     * 删除数据
     * @param {string} key - 键
     * @returns {boolean} 是否成功
     */
    remove(key) {
        if (!this.isEnabled) {
            return false;
        }

        try {
            localStorage.removeItem(this.getKey(key));
            return true;
        } catch (error) {
            console.error('删除数据失败:', error);
            return false;
        }
    }

    /**
     * 清空所有数据
     * @returns {boolean} 是否成功
     */
    clear() {
        if (!this.isEnabled) {
            return false;
        }

        try {
            // 只删除带前缀的键
            const keys = [];
            for (let i = 0; i < localStorage.length; i++) {
                const key = localStorage.key(i);
                if (key && key.startsWith(this.prefix)) {
                    keys.push(key);
                }
            }

            keys.forEach(key => localStorage.removeItem(key));
            return true;
        } catch (error) {
            console.error('清空数据失败:', error);
            return false;
        }
    }

    /**
     * 检查键是否存在
     * @param {string} key - 键
     * @returns {boolean} 是否存在
     */
    has(key) {
        if (!this.isEnabled) {
            return false;
        }

        return localStorage.getItem(this.getKey(key)) !== null;
    }

    /**
     * 获取所有键
     * @returns {string[]} 键列表
     */
    keys() {
        if (!this.isEnabled) {
            return [];
        }

        const result = [];
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (key && key.startsWith(this.prefix)) {
                result.push(key.substring(this.prefix.length));
            }
        }
        return result;
    }

    /**
     * 获取存储大小(字节)
     * @returns {number} 存储大小
     */
    getSize() {
        if (!this.isEnabled) {
            return 0;
        }

        let size = 0;
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (key && key.startsWith(this.prefix)) {
                size += key.length + localStorage.getItem(key).length;
            }
        }
        return size * 2; // UTF-16每个字符占2字节
    }
}

// 创建全局存储实例
const storage = new Storage();

// 导出工具类
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { Storage, storage };
}
