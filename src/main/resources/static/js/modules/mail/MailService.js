import { gameAPI } from '../../core/api/GameApi.js';

export class MailService {
    normalizeMail(mail) {
        const attachments = mail?.attachments || [];
        return {
            ...mail,
            attachments,
            hasAttachment: Boolean(mail?.hasAttachment ?? attachments.length > 0),
            isClaimed: Boolean(mail?.isClaimed)
        };
    }

    async getMails() {
        const response = await gameAPI.getMails();
        if (!response?.success) {
            throw new Error(response?.message || '加载邮件失败');
        }
        return (response.data?.records || response.data?.list || []).map(mail => this.normalizeMail(mail));
    }

    async getMail(mailId) {
        const response = await gameAPI.getMail(mailId);
        if (!response?.success) {
            throw new Error(response?.message || '加载邮件详情失败');
        }
        const detail = response.data || {};
        return this.normalizeMail({
            ...(detail.mail || {}),
            attachments: detail.attachments || []
        });
    }

    async markAllAsRead() {
        const mails = await this.getMails();
        await Promise.all(
            mails
                .filter(mail => !mail.isRead)
                .map(mail => gameAPI.readMail(mail.id))
        );
        return true;
    }

    async claimAttachment(mailId) {
        const response = await gameAPI.claimMailAttachment(mailId);
        if (!response?.success) {
            throw new Error(response?.message || '领取附件失败');
        }
        return response.data;
    }
}

export const mailService = new MailService();
