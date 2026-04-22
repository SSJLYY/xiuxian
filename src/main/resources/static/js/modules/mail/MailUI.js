import { mailService } from './MailService.js';

function escapeText(value) {
    return window.escapeHtml ? window.escapeHtml(value) : String(value ?? '');
}

function showToast(message, type = 'info') {
    if (window.moduleManager?.showToast) {
        window.moduleManager.showToast(message, type);
        return;
    }
    if (window.authManager?.showToast) {
        window.authManager.showToast(message, type);
        return;
    }
    console.log(`[${type}] ${message}`);
}

export class MailUI {
    constructor() {
        this.mails = [];
    }

    async init() {
        return this.loadMails();
    }

    async loadMails() {
        const container = document.getElementById('mailList') || document.getElementById('mailItems');
        if (!container) return;
        container.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载邮件中...</p></div>';
        try {
            this.mails = await mailService.getMails();
            this.renderMails();
        } catch (error) {
            container.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    renderMails() {
        const container = document.getElementById('mailList') || document.getElementById('mailItems');
        const unreadEl = document.getElementById('unreadCount');
        const totalEl = document.getElementById('totalCount');
        if (!container) return;
        if (unreadEl) unreadEl.textContent = this.mails.filter(mail => !mail.isRead).length;
        if (totalEl) totalEl.textContent = this.mails.length;
        if (this.mails.length === 0) {
            container.innerHTML = '<div class="empty-state">暂无邮件</div>';
            return;
        }
        container.innerHTML = this.mails.map(mail => `
            <div class="mail-item ${mail.isRead ? '' : 'unread'}" onclick="openMail(${mail.id})">
                <div class="mail-header">
                    <h4>${escapeText(mail.title || '邮件')}</h4>
                    <span class="mail-time">${new Date(mail.createdAt || Date.now()).toLocaleString('zh-CN')}</span>
                </div>
                <p class="mail-content">${escapeText(mail.content || '')}</p>
                ${mail.hasAttachment ? '<div class="mail-attachment"><i class="fas fa-paperclip"></i> 有附件</div>' : ''}
                ${!mail.isRead ? '<div class="unread-indicator">未读</div>' : ''}
            </div>
        `).join('');
    }

    async openMail(mailId) {
        try {
            const mail = await mailService.getMail(mailId);
            const modal = document.getElementById('mailDetailModal');
            const content = document.getElementById('mailDetailContent');
            if (modal && content) {
                const attachments = mail.attachments || [];
                content.innerHTML = `
                    <h3>${escapeText(mail.title || '邮件')}</h3>
                    <div class="mail-meta">
                        <span>时间: ${new Date(mail.createdAt || Date.now()).toLocaleString('zh-CN')}</span>
                    </div>
                    <div class="mail-body">${escapeText(mail.content || '')}</div>
                    ${attachments.length > 0 ? `
                        <div class="mail-attachments">
                            <h4>附件</h4>
                            ${attachments.map(att => `<div>${escapeText(att.itemName || att.name || '附件')} x${att.quantity || 1}</div>`).join('')}
                            ${!mail.isClaimed ? `<button class="btn btn-primary" onclick="claimAttachment(${mail.id})">领取附件</button>` : '<div class="text-green-400">附件已领取</div>'}
                        </div>
                    ` : ''}
                `;
                modal.style.display = 'block';
            } else {
                alert(`【${mail.title || '邮件'}】\n\n${mail.content || ''}`);
            }
            await this.loadMails();
        } catch (error) {
            showToast('打开邮件失败: ' + error.message, 'error');
        }
    }

    async markAllAsRead() {
        await mailService.markAllAsRead();
        showToast('所有邮件已标记为已读', 'success');
        return this.loadMails();
    }

    async claimAttachment(mailId) {
        await mailService.claimAttachment(mailId);
        showToast('附件领取成功', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        return this.loadMails();
    }
}

export const mailUI = new MailUI();
