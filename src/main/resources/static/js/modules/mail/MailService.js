import { gameAPI } from '../../core/api/GameApi.js';

export class MailService {
    async getMails() {
        const response = await gameAPI.getMails();
        if (!response?.success) throw new Error(response?.message || '加载邮件失败');
        return response.data || [];
    }

    async getMail(mailId) {
        const response = await gameAPI.getMail(mailId);
        if (!response?.success) throw new Error(response?.message || '加载邮件详情失败');
        return response.data;
    }

    async markAllAsRead() {
        const response = await gameAPI.post('/mail/mark-all-read');
        if (!response?.success) throw new Error(response?.message || '全部标记已读失败');
        return response.data;
    }

    async claimAttachment(mailId) {
        const response = await gameAPI.claimMailAttachment(mailId);
        if (!response?.success) throw new Error(response?.message || '领取附件失败');
        return response.data;
    }
}

export const mailService = new MailService();
