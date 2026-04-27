/**
 * 格式化工具类
 * 提供各种数据格式化功能
 */

class FormatUtils {
    /**
     * 格式化数字(千分位分隔)
     * @param {number} num - 数字
     * @returns {string} 格式化后的字符串
     */
    static formatNumber(num) {
        if (num == null) return '0';
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }

    /**
     * 格式化灵石数量
     * @param {number} amount - 灵石数量
     * @returns {string} 格式化后的字符串
     */
    static formatSpiritStones(amount) {
        if (amount == null) return '0';

        if (amount >= 100000000) {
            return `${(amount / 100000000).toFixed(2)}亿`;
        } else if (amount >= 10000) {
            return `${(amount / 10000).toFixed(2)}万`;
        }

        return this.formatNumber(amount);
    }

    /**
     * 格式化经验值
     * @param {number} exp - 经验值
     * @returns {string} 格式化后的字符串
     */
    static formatExp(exp) {
        if (exp == null) return '0';
        return this.formatNumber(exp);
    }

    /**
     * 格式化时间(秒 -> 时分秒)
     * @param {number} seconds - 秒数
     * @returns {string} 格式化后的时间字符串
     */
    static formatTime(seconds) {
        if (seconds == null || seconds < 0) return '0秒';

        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = Math.floor(seconds % 60);

        const parts = [];

        if (hours > 0) {
            parts.push(`${hours}小时`);
        }

        if (minutes > 0) {
            parts.push(`${minutes}分钟`);
        }

        if (secs > 0 || parts.length === 0) {
            parts.push(`${secs}秒`);
        }

        return parts.join('');
    }

    /**
     * 格式化日期时间
     * @param {Date|string|number} date - 日期对象或时间戳
     * @param {boolean} includeTime - 是否包含时间
     * @returns {string} 格式化后的日期字符串
     */
    static formatDateTime(date, includeTime = true) {
        if (!date) return '';

        const d = new Date(date);

        if (isNaN(d.getTime())) return '';

        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');

        if (!includeTime) {
            return `${year}-${month}-${day}`;
        }

        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        const seconds = String(d.getSeconds()).padStart(2, '0');

        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }

    /**
     * 格式化倒计时(秒 -> HH:MM:SS)
     * @param {number} seconds - 秒数
     * @returns {string} 倒计时字符串
     */
    static formatCountdown(seconds) {
        if (seconds == null || seconds < 0) return '00:00:00';

        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = Math.floor(seconds % 60);

        const h = String(hours).padStart(2, '0');
        const m = String(minutes).padStart(2, '0');
        const s = String(secs).padStart(2, '0');

        return `${h}:${m}:${s}`;
    }

    /**
     * 格式化百分比
     * @param {number} value - 数值
     * @param {number} total - 总数
     * @param {number} decimals - 小数位数
     * @returns {string} 百分比字符串
     */
    static formatPercent(value, total, decimals = 1) {
        if (total == null || total === 0) return '0%';

        const percent = (value / total) * 100;
        return `${percent.toFixed(decimals)}%`;
    }

    /**
     * 截断文本(添加省略号)
     * @param {string} text - 原始文本
     * @param {number} maxLength - 最大长度
     * @returns {string} 截断后的文本
     */
    static truncateText(text, maxLength) {
        if (!text || text.length <= maxLength) return text || '';
        return text.substring(0, maxLength) + '...';
    }

    /**
     * 格式化战斗日志
     * @param {Object} log - 战斗日志对象
     * @returns {string} 格式化后的日志文本
     */
    static formatCombatLog(log) {
        if (!log) return '';

        const timestamp = log.timestamp ? new Date(log.timestamp).toLocaleTimeString() : '';
        const action = log.action || '';
        const target = log.target || '';
        const damage = log.damage !== undefined ? log.damage : '';
        const result = log.result || '';

        return `[${timestamp}] ${action} ${target} ${damage} ${result}`;
    }
}

const formatUtils = FormatUtils;

export { FormatUtils, formatUtils };

// 导出工具类
if (typeof module !== 'undefined' && module.exports) {
    module.exports = FormatUtils;
}
