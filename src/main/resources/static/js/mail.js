// 邮件系统管理器
class MailManager {
    constructor() {
        this.currentPage = 1;
        this.pageSize = 20;
        this.totalPages = 1;
        this.mails = [];
        this.init();
    }

    // 初始化
    async init() {
        console.log('初始化邮件系统');
        
        // 检查认证状态
        if (!window.authManager || !window.authManager.isAuthenticated) {
            console.warn('用户未认证，跳转到登录页');
            window.location.href = '/login.html';
            return;
        }

        // 加载邮件列表
        await this.loadMails();
    }

    // 加载邮件列表
    async loadMails(page = 1) {
        try {
            this.showLoading(true);
            this.hideEmpty();
            
            console.log(`加载邮件列表: page=${page}, size=${this.pageSize}`);
            
            const response = await fetch(`/api/mail/list?page=${page}&size=${this.pageSize}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${window.authManager.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            
            if (!result.success) {
                throw new Error(result.message || '加载邮件失败');
            }

            const pageData = result.data;
            this.mails = pageData.records || [];
            this.currentPage = pageData.current || 1;
            this.totalPages = pageData.pages || 1;
            
            console.log(`邮件加载成功: 共${pageData.total}封, 当前第${this.currentPage}页`);
            
            // 更新统计信息
            this.updateStats(pageData.total);
            
            // 渲染邮件列表
            this.renderMails();
            
            // 更新分页
            this.updatePagination();
            
            // 加载未读数量
            await this.loadUnreadCount();
            
        } catch (error) {
            console.error('加载邮件失败:', error);
            this.showToast('加载邮件失败: ' + error.message, 'error');
            this.showEmpty();
        } finally {
            this.showLoading(false);
        }
    }

    // 渲染邮件列表
    renderMails() {
        const container = document.getElementById('mailItems');
        
        if (!this.mails || this.mails.length === 0) {
            this.showEmpty();
            container.innerHTML = '';
            return;
        }

        this.hideEmpty();
        
        container.innerHTML = this.mails.map(mail => this.createMailItem(mail)).join('');
    }

    // 创建邮件项HTML
    createMailItem(mail) {
        const unreadClass = mail.isRead ? '' : 'unread';
        const claimedTag = mail.isClaimed ? '<span class="tag-claimed">已领取</span>' : '';
        const attachmentIcon = mail.hasAttachment ? '📦 ' : '';
        
        // 格式化时间
        const time = this.formatTime(mail.createdAt);
        
        // 邮件类型标签
        const typeText = this.getMailTypeText(mail.mailType);
        
        return `
            <div class="mail-item ${unreadClass}" onclick="mailManager.viewMail(${mail.id})">
                <div class="mail-item-header">
                    <div class="mail-title">
                        ${attachmentIcon}${this.escapeHtml(mail.title)}
                    </div>
                    <span class="mail-type">${typeText}</span>
                </div>
                <div class="mail-item-footer">
                    <span class="mail-time">${time}</span>
                    ${claimedTag}
                </div>
            </div>
        `;
    }

    // 查看邮件详情
    async viewMail(mailId) {
        try {
            this.showLoading(true);
            
            console.log(`查看邮件详情: mailId=${mailId}`);
            
            const response = await fetch(`/api/mail/${mailId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${window.authManager.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            
            if (!result.success) {
                throw new Error(result.message || '获取邮件详情失败');
            }

            const { mail, attachments } = result.data;
            
            console.log('邮件详情加载成功:', mail.title);
            
            // 显示邮件详情
            this.showMailDetail(mail, attachments);
            
            // 刷新列表（标记为已读）
            await this.loadMails(this.currentPage);
            
        } catch (error) {
            console.error('查看邮件详情失败:', error);
            this.showToast('查看邮件详情失败: ' + error.message, 'error');
        } finally {
            this.showLoading(false);
        }
    }

    // 显示邮件详情
    showMailDetail(mail, attachments) {
        const modal = document.getElementById('mailDetailModal');
        const content = document.getElementById('mailDetailContent');
        
        const typeText = this.getMailTypeText(mail.mailType);
        const time = this.formatTime(mail.createdAt);
        const expireTime = mail.expireAt ? this.formatTime(mail.expireAt) : '永久';
        
        // 附件列表
        let attachmentHtml = '';
        if (attachments && attachments.length > 0) {
            const attachmentItems = attachments.map(att => {
                const itemName = this.getAttachmentName(att);
                return `<li>📦 ${itemName} x${att.quantity}</li>`;
            }).join('');
            
            attachmentHtml = `
                <div class="mail-attachments">
                    <h4>📦 附件</h4>
                    <ul class="attachment-list">
                        ${attachmentItems}
                    </ul>
                </div>
            `;
        }
        
        // 操作按钮
        let actionButtons = '';
        if (mail.hasAttachment && !mail.isClaimed) {
            actionButtons += `<button class="btn btn-primary" onclick="mailManager.claimAttachment(${mail.id})">领取附件</button>`;
        }
        actionButtons += `<button class="btn btn-danger" onclick="mailManager.deleteMail(${mail.id})">删除邮件</button>`;
        
        content.innerHTML = `
            <h3>${this.escapeHtml(mail.title)}</h3>
            <div class="mail-meta">
                <span>类型: ${typeText}</span>
                <span>时间: ${time}</span>
                <span>过期: ${expireTime}</span>
            </div>
            <div class="mail-body">
                ${this.escapeHtml(mail.content)}
            </div>
            ${attachmentHtml}
            <div class="mail-actions">
                ${actionButtons}
            </div>
        `;
        
        modal.style.display = 'block';
    }

    // 关闭邮件详情
    closeMailDetail() {
        const modal = document.getElementById('mailDetailModal');
        modal.style.display = 'none';
    }

    // 领取附件
    async claimAttachment(mailId) {
        try {
            this.showLoading(true);
            
            console.log(`领取邮件附件: mailId=${mailId}`);
            
            const response = await fetch(`/api/mail/${mailId}/claim`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${window.authManager.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            
            if (!result.success) {
                throw new Error(result.message || '领取附件失败');
            }

            console.log('领取附件成功');
            this.showToast('领取附件成功！', 'success');
            
            // 关闭详情
            this.closeMailDetail();
            
            // 刷新列表
            await this.loadMails(this.currentPage);
            
            // 刷新玩家数据
            if (window.authManager && window.authManager.loadPlayerProfile) {
                await window.authManager.loadPlayerProfile();
            }
            
        } catch (error) {
            console.error('领取附件失败:', error);
            this.showToast('领取附件失败: ' + error.message, 'error');
        } finally {
            this.showLoading(false);
        }
    }

    // 删除邮件
    async deleteMail(mailId) {
        if (!confirm('确定要删除这封邮件吗？')) {
            return;
        }

        try {
            this.showLoading(true);
            
            console.log(`删除邮件: mailId=${mailId}`);
            
            const response = await fetch(`/api/mail/${mailId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${window.authManager.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            
            if (!result.success) {
                throw new Error(result.message || '删除邮件失败');
            }

            console.log('删除邮件成功');
            this.showToast('删除邮件成功', 'success');
            
            // 关闭详情
            this.closeMailDetail();
            
            // 刷新列表
            await this.loadMails(this.currentPage);
            
        } catch (error) {
            console.error('删除邮件失败:', error);
            this.showToast('删除邮件失败: ' + error.message, 'error');
        } finally {
            this.showLoading(false);
        }
    }

    // 加载未读数量
    async loadUnreadCount() {
        try {
            const response = await fetch('/api/mail/unread-count', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${window.authManager.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success) {
                    const unreadCount = result.data || 0;
                    document.getElementById('unreadCount').textContent = unreadCount;
                }
            }
        } catch (error) {
            console.error('加载未读数量失败:', error);
        }
    }

    // 更新统计信息
    updateStats(total) {
        document.getElementById('totalCount').textContent = total || 0;
    }

    // 更新分页
    updatePagination() {
        const pagination = document.getElementById('pagination');
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');
        const pageInfo = document.getElementById('pageInfo');
        
        if (this.totalPages <= 1) {
            pagination.style.display = 'none';
            return;
        }
        
        pagination.style.display = 'block';
        pageInfo.textContent = `第 ${this.currentPage} / ${this.totalPages} 页`;
        
        prevBtn.disabled = this.currentPage <= 1;
        nextBtn.disabled = this.currentPage >= this.totalPages;
    }

    // 上一页
    previousPage() {
        if (this.currentPage > 1) {
            this.loadMails(this.currentPage - 1);
        }
    }

    // 下一页
    nextPage() {
        if (this.currentPage < this.totalPages) {
            this.loadMails(this.currentPage + 1);
        }
    }

    // 刷新邮件
    async refreshMails() {
        await this.loadMails(this.currentPage);
        this.showToast('刷新成功', 'success');
    }

    // 获取邮件类型文本
    getMailTypeText(type) {
        const types = {
            'SYSTEM': '系统',
            'REWARD': '奖励',
            'ACTIVITY': '活动'
        };
        return types[type] || '未知';
    }

    // 获取附件名称
    getAttachmentName(attachment) {
        const types = {
            'SPIRIT_STONES': '灵石',
            'EXP': '经验',
            'ITEM': '物品',
            'EQUIPMENT': '装备'
        };
        return types[attachment.itemType] || '未知';
    }

    // 格式化时间
    formatTime(timestamp) {
        if (!timestamp) return '';
        
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        
        // 1分钟内
        if (diff < 60000) {
            return '刚刚';
        }
        
        // 1小时内
        if (diff < 3600000) {
            return Math.floor(diff / 60000) + '分钟前';
        }
        
        // 今天
        if (date.toDateString() === now.toDateString()) {
            return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        }
        
        // 昨天
        const yesterday = new Date(now);
        yesterday.setDate(yesterday.getDate() - 1);
        if (date.toDateString() === yesterday.toDateString()) {
            return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        }
        
        // 其他
        return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    }

    // HTML转义
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 显示空状态
    showEmpty() {
        document.getElementById('emptyState').style.display = 'block';
        document.getElementById('mailItems').style.display = 'none';
    }

    // 隐藏空状态
    hideEmpty() {
        document.getElementById('emptyState').style.display = 'none';
        document.getElementById('mailItems').style.display = 'flex';
    }

    // 显示加载状态
    showLoading(show) {
        const loading = document.getElementById('loading');
        if (loading) {
            loading.style.display = show ? 'flex' : 'none';
        }
    }

    // 显示消息提示
    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast-bubble ${type}`;
        toast.textContent = message;
        
        const count = document.querySelectorAll('.toast-bubble').length;
        const bottom = 10 + count * 36;
        
        Object.assign(toast.style, {
            position: 'fixed',
            bottom: `${bottom}px`,
            right: '16px',
            background: this.getToastColor(type),
            color: '#fff',
            padding: '6px 10px',
            borderRadius: '9999px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
            zIndex: '10001',
            maxWidth: '220px',
            fontSize: '12px',
            lineHeight: '1.2',
            opacity: '0',
            transform: 'translateY(8px)',
            transition: 'all 0.25s ease'
        });
        
        document.body.appendChild(toast);
        
        requestAnimationFrame(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateY(0)';
        });
        
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(8px)';
            setTimeout(() => {
                if (toast.parentElement) {
                    toast.parentElement.removeChild(toast);
                }
            }, 250);
        }, 3000);
    }

    // 获取toast颜色
    getToastColor(type) {
        const colors = {
            info: '#3498db',
            success: '#2ecc71',
            warning: '#f39c12',
            error: '#e74c3c'
        };
        return colors[type] || colors.info;
    }
}

// 全局函数
function goBack() {
    window.location.href = '/index.html';
}

function refreshMails() {
    if (window.mailManager) {
        window.mailManager.refreshMails();
    }
}

function previousPage() {
    if (window.mailManager) {
        window.mailManager.previousPage();
    }
}

function nextPage() {
    if (window.mailManager) {
        window.mailManager.nextPage();
    }
}

function closeMailDetail() {
    if (window.mailManager) {
        window.mailManager.closeMailDetail();
    }
}

// 点击模态框外部关闭
window.onclick = function(event) {
    const modal = document.getElementById('mailDetailModal');
    if (event.target === modal) {
        closeMailDetail();
    }
};

// 初始化邮件管理器
window.addEventListener('DOMContentLoaded', () => {
    window.mailManager = new MailManager();
});
