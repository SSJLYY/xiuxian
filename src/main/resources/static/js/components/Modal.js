/**
 * Modal模态框组件
 * 提供可复用的模态框功能
 */

class Modal {
    constructor(options = {}) {
        this.options = {
            title: '',
            content: '',
            size: 'medium', // small, medium, large
            showClose: true,
            showFooter: true,
            closeOnBackdrop: true,
            closeOnEsc: true,
            ...options
        };

        this.modal = null;
        this.backdrop = null;
        this.isShown = false;
    }

    /**
     * 显示模态框
     * @param {Object} options - 配置选项(可选)
     */
    show(options = {}) {
        // 合并配置
        this.options = { ...this.options, ...options };

        // 创建模态框DOM
        this.createModal();

        // 显示
        this.isShown = true;
        document.body.classList.add('modal-open');

        // 绑定事件
        this.bindEvents();

        return this;
    }

    /**
     * 隐藏模态框
     */
    hide() {
        if (!this.isShown) return;

        this.modal?.classList.remove('show');
        this.backdrop?.classList.remove('show');

        setTimeout(() => {
            this.removeModal();
            document.body.classList.remove('modal-open');
            this.isShown = false;
            if (this.options.onHidden) {
                this.options.onHidden(this);
            }
        }, 300);

        return this;
    }

    /**
     * 创建模态框DOM
     */
    createModal() {
        // 检查是否已存在
        if (this.modal && this.modal.parentElement) {
            return;
        }

        // 创建backdrop
        this.backdrop = document.createElement('div');
        this.backdrop.className = 'modal-backdrop';
        this.backdrop.innerHTML = '<div class="modal-backdrop-content"></div>';

        // 创建模态框
        this.modal = document.createElement('div');
        this.modal.className = `modal modal-${this.options.size}`;
        this.modal.innerHTML = `
            <div class="modal-content">
                ${this.options.showClose ? '<button class="modal-close">×</button>' : ''}
                ${this.options.title ? `<div class="modal-header"><h3>${this.options.title}</h3></div>` : ''}
                <div class="modal-body">${this.options.content}</div>
                ${this.options.showFooter ? this.renderFooter() : ''}
            </div>
        `;

        // 添加到DOM
        document.body.appendChild(this.backdrop);
        document.body.appendChild(this.modal);

        // 触发动画
        requestAnimationFrame(() => {
            this.backdrop.classList.add('show');
            this.modal.classList.add('show');
        });
    }

    /**
     * 渲染底部按钮
     */
    renderFooter() {
        const buttons = this.options.buttons || [];
        if (buttons.length === 0) {
            return '<div class="modal-footer"></div>';
        }

        const buttonsHtml = buttons.map(btn => {
            const classes = ['btn'];
            if (btn.type) classes.push(`btn-${btn.type}`);
            if (btn.class) classes.push(btn.class);

            return `<button class="${classes.join(' ')}" data-action="${btn.action}">${btn.text}</button>`;
        }).join('');

        return `<div class="modal-footer">${buttonsHtml}</div>`;
    }

    /**
     * 移除模态框DOM
     */
    removeModal() {
        this.modal?.remove();
        this.backdrop?.remove();
        this.modal = null;
        this.backdrop = null;
    }

    /**
     * 绑定事件
     */
    bindEvents() {
        // 关闭按钮
        const closeBtn = this.modal?.querySelector('.modal-close');
        closeBtn?.addEventListener('click', () => this.hide());

        // 点击backdrop关闭
        if (this.options.closeOnBackdrop) {
            this.backdrop?.addEventListener('click', (e) => {
                if (e.target === this.backdrop) {
                    this.hide();
                }
            });
        }

        // ESC键关闭
        if (this.options.closeOnEsc) {
            const escHandler = (e) => {
                if (e.key === 'Escape' && this.isShown) {
                    this.hide();
                    document.removeEventListener('keydown', escHandler);
                }
            };
            document.addEventListener('keydown', escHandler);
        }

        // 底部按钮事件
        const footerButtons = this.modal?.querySelectorAll('.modal-footer button');
        footerButtons?.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const action = e.target.dataset.action;
                const buttonConfig = this.options.buttons.find(b => b.action === action);
                if (buttonConfig?.onClick) {
                    buttonConfig.onClick(e, this);
                }
            });
        });

        // 显示回调
        if (this.options.onShow) {
            this.options.onShow(this);
        }
    }

    /**
     * 设置内容
     * @param {string} content - HTML内容
     */
    setContent(content) {
        const body = this.modal?.querySelector('.modal-body');
        if (body) {
            body.innerHTML = content;
        }
    }

    /**
     * 设置标题
     * @param {string} title - 标题文本
     */
    setTitle(title) {
        const header = this.modal?.querySelector('.modal-header h3');
        if (header) {
            header.textContent = title;
        }
    }

    // ========== 静态方法 ==========

    /**
     * 确认对话框
     * @param {string} message - 确认消息
     * @param {string} title - 标题
     * @returns {Promise<boolean>} 用户选择
     */
    static confirm(message, title = '确认') {
        return new Promise((resolve) => {
            const modal = new Modal({
                title,
                content: `<p>${message}</p>`,
                size: 'small',
                buttons: [
                    {
                        text: '取消',
                        type: 'default',
                        action: 'cancel',
                        onClick: () => {
                            modal.hide();
                            resolve(false);
                        }
                    },
                    {
                        text: '确认',
                        type: 'primary',
                        action: 'confirm',
                        onClick: () => {
                            modal.hide();
                            resolve(true);
                        }
                    }
                ]
            });
            modal.show();
        });
    }

    /**
     * 提示对话框
     * @param {string} message - 提示消息
     * @param {string} title - 标题
     * @returns {Promise<void>}
     */
    static alert(message, title = '提示') {
        return new Promise((resolve) => {
            const modal = new Modal({
                title,
                content: `<p>${message}</p>`,
                size: 'small',
                buttons: [
                    {
                        text: '确定',
                        type: 'primary',
                        action: 'ok',
                        onClick: () => {
                            modal.hide();
                            resolve();
                        }
                    }
                ]
            });
            modal.show();
        });
    }

    /**
     * 自定义对话框
     * @param {Object} options - 配置选项
     * @returns {Promise<Modal>}
     */
    static custom(options) {
        return new Promise((resolve) => {
            const modal = new Modal({
                ...options,
                onShow: (instance) => {
                    if (options.onShow) {
                        options.onShow(instance);
                    }
                },
                onHidden: () => {
                    if (options.onHidden) {
                        options.onHidden();
                    }
                    resolve(modal);
                }
            });
            modal.show();
        });
    }
}

// 导出Modal组件
const modal = {
    current: null,

    show(options = {}) {
        if (this.current?.isShown) {
            this.current.removeModal();
            document.body.classList.remove('modal-open');
            this.current.isShown = false;
        }

        const buttons = [];
        const showCancel = options.showCancel ?? Boolean(options.onConfirm);
        const shouldShowPrimary = options.showFooter !== false &&
            (options.onConfirm || options.confirmText || showCancel === false);

        if (showCancel) {
            buttons.push({
                text: options.cancelText || '取消',
                type: 'default',
                action: 'cancel',
                onClick: (event, instance) => {
                    if (options.onCancel) {
                        options.onCancel(event, instance);
                    } else {
                        instance.hide();
                    }
                }
            });
        }

        if (shouldShowPrimary) {
            buttons.push({
                text: options.confirmText || '确定',
                type: options.confirmType || 'primary',
                action: 'confirm',
                onClick: (event, instance) => {
                    if (options.onConfirm) {
                        options.onConfirm(event, instance);
                    } else {
                        instance.hide();
                    }
                }
            });
        }

        const instance = new Modal({
            ...options,
            buttons,
            showFooter: buttons.length > 0 && options.showFooter !== false,
            onHidden: (modalInstance) => {
                if (this.current === modalInstance) {
                    this.current = null;
                }
                if (options.onHidden) {
                    options.onHidden(modalInstance);
                }
            }
        });

        this.current = instance.show();
        return this.current;
    },

    hide() {
        this.current?.hide();
        return this;
    }
};

export { Modal, modal };
export default Modal;

if (typeof module !== 'undefined' && module.exports) {
    module.exports = { Modal, modal, default: Modal };
}
