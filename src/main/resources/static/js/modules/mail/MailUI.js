/**
 * 邮件模块 - UI渲染层
 */
import { mailService } from './MailService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { formatUtils } from '../../core/utils/FormatUtils.js';
import { modal } from '../../components/Modal.js';
import { escapeHtml } from '../../core/utils/Security.js';

export class MailUI {
    init() {
        this.setupElements();
        this.bindEvents();
        this.loadMails();
    }

    setupElements() {
        this.elements = {
            mailListContainer: document.getElementById('mailListContainer'),
            unreadCount: document.getElementById('unreadCount'),
            deleteAllReadBtn: document.getElementById('deleteAllReadBtn'),
            mailTabs: document.querySelectorAll('[data-tab="mail"]')
        };
    }

    bindEvents() {
        if (this.elements.deleteAllReadBtn) {
            this.elements.deleteAllReadBtn.addEventListener('click', () => this.handleDeleteAllRead());
        }

        // 标签页切换
        this.elements.mailTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                this.switchTab(e.target.dataset.mailTab);
            });
        });
    }

    switchTab(tabName) {
        this.elements.mailTabs.forEach(tab => {
            tab.classList.toggle('active', tab.dataset.mailTab === tabName);
        });

        // 如果有标签页功能,可以实现未读/已读/全部切换
        this.loadMails(tabName);
    }

    async loadMails(type = 'all') {
        loading.show();
        try {
            await mailService.getMails();
            this.renderMails();
            this.updateUnreadCount();
        } catch (error) {
            toast.error('加载邮件失败');
        } finally {
            loading.hide();
        }
    }

    renderMails(mails = mailService.mails) {
        const container = this.elements.mailListContainer;
        if (!container) return;

        if (mails.length === 0) {
            container.innerHTML = '<p>暂无邮件</p>';
            return;
        }

        container.innerHTML = `
            <div class="mail-list">
                ${mails.map(mail => this.renderMailCard(mail)).join('')}
            </div>
        `;

        // 绑定事件
        container.querySelectorAll('[data-action="read"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleRead(e.target.dataset.mailId));
        });

        container.querySelectorAll('[data-action="delete"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleDelete(e.target.dataset.mailId));
        });

        container.querySelectorAll('[data-action="claim"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleClaim(e.target.dataset.mailId));
        });
    }

    renderMailCard(mail) {
        const readClass = mail.read ? 'read' : 'unread';
        const hasAttachment = mail.attachment && mail.attachment.length > 0;
        const attachmentClaimed = mail.attachmentClaimed;

        return `
            <div class="mail-card ${readClass}">
                <div class="mail-header">
                    <div class="mail-sender">${escapeHtml(mail.sender)}</div>
                    <div class="mail-time">${formatUtils.formatDateTime(new Date(mail.sentAt))}</div>
                </div>
                <div class="mail-body">
                    <div class="mail-subject">${escapeHtml(mail.subject)}</div>
                    <div class="mail-content">${escapeHtml(mail.content.substring(0, 100))}${mail.content.length > 100 ? '...' : ''}</div>
                    ${hasAttachment ? `
                        <div class="mail-attachment">
                            <span class="attachment-icon">*</span>
                            <span class="attachment-text">
                                ${attachmentClaimed ? '附件已领取' : escapeHtml(mail.attachment.map(a => a.name).join(', '))}
                            </span>
                        </div>
                    ` : ''}
                </div>
                <div class="mail-actions">
                    ${!mail.read ? `
                        <button class="btn btn-sm btn-primary" data-action="read" data-mail-id="${mail.id}">阅读</button>
                    ` : ''}
                    ${hasAttachment && !attachmentClaimed ? `
                        <button class="btn btn-sm btn-success" data-action="claim" data-mail-id="${mail.id}">领取附件</button>
                    ` : ''}
                    <button class="btn btn-sm btn-danger" data-action="delete" data-mail-id="${mail.id}">删除</button>
                </div>
            </div>
        `;
    }

    updateUnreadCount() {
        if (this.elements.unreadCount) {
            const count = mailService.unreadCount;
            this.elements.unreadCount.textContent = count;
            this.elements.unreadCount.style.display = count > 0 ? 'inline' : 'none';
        }
    }

    async handleRead(mailId) {
        const mail = mailService.getMailById(mailId);
        if (!mail) return;

        const detailHtml = `
            <div class="mail-detail">
                <div class="mail-detail-header">
                    <div class="sender">${escapeHtml(mail.sender)}</div>
                    <div class="time">${formatUtils.formatDateTime(new Date(mail.sentAt))}</div>
                </div>
                <div class="mail-detail-subject">${escapeHtml(mail.subject)}</div>
                <div class="mail-detail-content">${escapeHtml(mail.content)}</div>
                ${mail.attachment && mail.attachment.length > 0 ? `
                    <div class="mail-detail-attachment">
                        <h4>附件</h4>
                        <div class="attachment-list">
                            ${mail.attachment.map(item => `
                                <div class="attachment-item">
                                    <span class="item-name">${escapeHtml(item.name)}</span>
                                    <span class="item-quantity">x${escapeHtml(item.quantity)}</span>
                                </div>
                            `).join('')}
                        </div>
                        ${!mail.attachmentClaimed ? `
                            <button class="btn btn-success" id="claimAttachmentBtn">领取附件</button>
                        ` : `
                            <div class="attachment-claimed">附件已领取</div>
                        `}
                    </div>
                ` : ''}
            </div>
        `;

        modal.show({
            title: '邮件详情',
            content: detailHtml,
            showCancel: false,
            confirmText: '关闭'
        });

        // 绑定领取附件按钮
        const claimBtn = document.getElementById('claimAttachmentBtn');
        if (claimBtn) {
            claimBtn.addEventListener('click', () => {
                this.handleClaim(mailId);
                modal.hide();
            });
        }
    }

    async handleReadInternal(mailId) {
        loading.show();
        try {
            await mailService.readMail(mailId);
            await this.loadMails();
        } catch (error) {
            toast.error('读取邮件失败');
        } finally {
            loading.hide();
        }
    }

    async handleDelete(mailId) {
        if (!confirm('确定要删除这封邮件吗?')) return;

        loading.show();
        try {
            await mailService.deleteMail(mailId);
        } catch (error) {
            toast.error('删除失败');
        } finally {
            loading.hide();
        }
    }

    async handleClaim(mailId) {
        loading.show();
        try {
            await mailService.claimAttachment(mailId);
        } catch (error) {
            toast.error('领取附件失败');
        } finally {
            loading.hide();
        }
    }

    async handleDeleteAllRead() {
        const readMails = mailService.mails.filter(m => m.read);
        if (readMails.length === 0) {
            toast.info('没有已读邮件');
            return;
        }

        if (!confirm(`确定要删除所有已读邮件(${readMails.length}封)吗?`)) return;

        loading.show();
        try {
            await mailService.deleteAllReadMails();
        } catch (error) {
            toast.error('删除失败');
        } finally {
            loading.hide();
        }
    }
}

export const mailUI = new MailUI();
