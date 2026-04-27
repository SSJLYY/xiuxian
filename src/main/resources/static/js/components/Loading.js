/**
 * Loading加载组件
 * 提供页面和元素级别的加载状态
 */

class Loading {
    constructor() {
        this.loadingElements = new Map();
        this.pageLoader = null;
    }

    /**
     * 显示全屏加载
     * @param {string} message - 加载提示文字
     */
    showPage(message = '加载中...') {
        // 检查是否已存在
        if (this.pageLoader) {
            return this;
        }

        // 创建加载元素
        this.pageLoader = document.createElement('div');
        this.pageLoader.className = 'page-loader';
        this.pageLoader.innerHTML = `
            <div class="loader-content">
                <div class="loader-spinner"></div>
                <div class="loader-text">${message}</div>
            </div>
        `;

        // 添加到页面
        document.body.appendChild(this.pageLoader);
        document.body.classList.add('page-loading');

        return this;
    }

    /**
     * 隐藏全屏加载
     */
    hidePage() {
        if (!this.pageLoader) {
            return this;
        }

        document.body.classList.remove('page-loading');
        this.pageLoader.remove();
        this.pageLoader = null;

        return this;
    }

    /**
     * 显示元素加载状态
     * @param {HTMLElement|string} target - 目标元素或选择器
     * @param {string} message - 加载提示
     */
    show(target, message = '加载中...') {
        const element = typeof target === 'string'
            ? document.querySelector(target)
            : target;

        if (!element) {
            console.warn('Loading target not found:', target);
            return this;
        }

        // 保存原始内容
        const originalContent = element.innerHTML;
        const loaderId = this.generateId();

        // 创建加载遮罩
        const loader = document.createElement('div');
        loader.className = 'element-loader';
        loader.innerHTML = `
            <div class="loader-overlay">
                <div class="loader-spinner small"></div>
                ${message ? `<div class="loader-text">${message}</div>` : ''}
            </div>
        `;

        // 保存原始内容和loader
        this.loadingElements.set(loaderId, {
            element,
            originalContent,
            loader
        });

        // 显示加载状态
        element.style.position = 'relative';
        element.appendChild(loader);

        return this;
    }

    /**
     * 隐藏元素加载状态
     * @param {HTMLElement|string} target - 目标元素或选择器
     */
    hide(target) {
        const element = typeof target === 'string'
            ? document.querySelector(target)
            : target;

        if (!element) {
            console.warn('Loading target not found:', target);
            return this;
        }

        // 查找并移除对应的loader
        for (const [loaderId, data] of this.loadingElements.entries()) {
            if (data.element === element) {
                data.loader.remove();
                this.loadingElements.delete(loaderId);
                break;
            }
        }

        return this;
    }

    /**
     * 切换加载状态
     * @param {HTMLElement|string} target - 目标元素或选择器
     * @param {boolean} loading - 是否显示加载
     * @param {string} message - 加载提示
     */
    toggle(target, loading, message = '加载中...') {
        return loading ? this.show(target, message) : this.hide(target);
    }

    /**
     * 隐藏所有加载状态
     */
    hideAll() {
        // 隐藏所有元素加载
        for (const [loaderId, data] of this.loadingElements.entries()) {
            data.loader.remove();
        }
        this.loadingElements.clear();

        // 隐藏页面加载
        this.hidePage();

        return this;
    }

    /**
     * 生成唯一ID
     */
    generateId() {
        return `loader-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    }

    // ========== 静态方法 ==========

    /**
     * 异步执行并显示加载
     * @param {Promise} promise - 异步操作
     * @param {Object} options - 配置选项
     * @returns {Promise} 异步操作结果
     */
    static async execute(promise, options = {}) {
        const {
            target = null,
            message = '加载中...',
            page = false
        } = options;

        const loading = new Loading();

        try {
            if (page) {
                loading.showPage(message);
            } else if (target) {
                loading.show(target, message);
            }

            const result = await promise;
            return result;
        } finally {
            loading.hideAll();
        }
    }

    /**
     * 创建加载按钮包装器
     * @param {HTMLElement} button - 按钮元素
     * @param {Function} handler - 点击处理函数
     * @param {Object} options - 配置选项
     */
    static wrapButton(button, handler, options = {}) {
        const {
            loadingText = '处理中...',
            disabled = true
        } = options;

        const originalText = button.textContent;
        let isLoading = false;

        const handleClick = async (e) => {
            if (isLoading) {
                e.preventDefault();
                return;
            }

            try {
                isLoading = true;

                // 更新按钮状态
                if (disabled) {
                    button.disabled = true;
                }
                button.textContent = loadingText;
                button.classList.add('loading');

                // 执行处理函数
                await handler(e, button);

            } finally {
                isLoading = false;

                // 恢复按钮状态
                if (disabled) {
                    button.disabled = false;
                }
                button.textContent = originalText;
                button.classList.remove('loading');
            }
        };

        button.addEventListener('click', handleClick);

        // 返回清理函数
        return () => {
            button.removeEventListener('click', handleClick);
        };
    }
}

// 创建全局Loading实例
const loading = new Loading();

export { Loading, loading };

// 导出Loading组件
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { Loading, loading };
}
