/**
 * 邮件模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class MailService {
    constructor() {
        this.mails = [];
        this.unreadCount = 0;
    }

    async getMails() {
        try {
            const response = await gameAPI.getMails();
            if (response.success) {
                this.mails = response.data;
                this.unreadCount = this.mails.filter(m => !m.read).length;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载邮件失败: ' + error.message);
            throw error;
        }
    }

    async readMail(mailId) {
        try {
            const response = await gameAPI.readMail(mailId);
            if (response.success) {
                await this.getMails();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('读取邮件失败: ' + error.message);
            throw error;
        }
    }

    async deleteMail(mailId) {
        try {
            const response = await gameAPI.deleteMail(mailId);
            if (response.success) {
                toast.success('删除邮件成功');
                await this.getMails();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('删除邮件失败: ' + error.message);
            throw error;
        }
    }

    async claimAttachment(mailId) {
        try {
            const response = await gameAPI.claimMailAttachment(mailId);
            if (response.success) {
                toast.success('领取附件成功');
                await this.getMails();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('领取附件失败: ' + error.message);
            throw error;
        }
    }

    async deleteAllReadMails() {
        try {
            const response = await gameAPI.getMails();
            if (response.success) {
                toast.success('删除已读邮件成功');
                await this.getMails();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('删除失败: ' + error.message);
            throw error;
        }
    }

    getMailById(mailId) {
        return this.mails.find(m => m.id === mailId) || null;
    }
}

export const mailService = new MailService();
