/**
 * Toast消息提示组件
 * 提供轻量级的消息提示功能
 */

class Toast {
    constructor() {
        this.container = null;
        this.init();
    }

    /**
     * 初始化Toast容器
     */
    init() {
        // 检查是否已存在容器
        this.container = document.getElementById('toast-container');
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.id = 'toast-container';
            this.container.className = 'toast-container';
            document.body.appendChild(this.container);
        }
    }

    /**
     * 显示 Toast 消息
     * @param {string} message - 消息内容
     * @param {string} type - 消息类型 (success/error/warning/info)
     * @param {number} duration - 显示时长 (毫秒),0 表示不自动消失
     */
    show(message, type = 'info', duration = 3000) {
        // XSS 防护：转义消息内容
        const escapedMessage = this.escapeHtml(message);
        
        // 创建 Toast 元素
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <div class="toast-icon">
                ${this.getIcon(type)}
            </div>
            <div class="toast-message">${escapedMessage}</div>
            <button class="toast-close" onclick="this.parentElement.remove()">×</button>
        `;

        // 添加到容器
        this.container.appendChild(toast);

        // 触发动画
        requestAnimationFrame(() => {
            toast.classList.add('show');
        });

        // 自动消失
        if (duration > 0) {
            setTimeout(() => {
                this.hide(toast);
            }, duration);
        }

        return toast;
    }

    /**
     * 隐藏Toast
     * @param {HTMLElement} toast - Toast元素
     */
    hide(toast) {
        if (!toast) return;

        toast.classList.remove('show');
        toast.classList.add('hide');

        // 等待动画完成后移除元素
        setTimeout(() => {
            if (toast.parentElement) {
                toast.remove();
            }
        }, 300);
    }

    /**
     * XSS 防护：转义 HTML 特殊字符
     * @param {string} text - 原始文本
     * @returns {string} 转义后的文本
     */
    escapeHtml(text) {
        if (text == null) return '';
        const str = String(text);
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return str.replace(/[&<>"']/g, c => map[c]);
    }

    /**
     * 获取图标
     * @param {string} type - 消息类型
     * @returns {string} 图标 HTML
     */
    getIcon(type) {
        const icons = {
            success: '<i class="fas fa-check-circle"></i>',
            error: '<i class="fas fa-times-circle"></i>',
            warning: '<i class="fas fa-exclamation-triangle"></i>',
            info: '<i class="fas fa-info-circle"></i>'
        };
        return icons[type] || icons.info;
    }

    /**
     * 成功消息
     */
    success(message, duration = 3000) {
        return this.show(message, 'success', duration);
    }

    /**
     * 错误消息
     */
    error(message, duration = 5000) {
        return this.show(message, 'error', duration);
    }

    /**
     * 警告消息
     */
    warning(message, duration = 4000) {
        return this.show(message, 'warning', duration);
    }

    /**
     * 信息消息
     */
    info(message, duration = 3000) {
        return this.show(message, 'info', duration);
    }

    /**
     * 清除所有Toast
     */
    clearAll() {
        if (this.container) {
            this.container.innerHTML = '';
        }
    }
}

// 创建全局Toast实例
const toast = new Toast();

// 导出Toast组件
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { Toast, toast };
}
