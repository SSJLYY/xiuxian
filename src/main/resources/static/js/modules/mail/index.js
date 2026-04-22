export { mailService } from './MailService.js';
export { mailUI } from './MailUI.js';

export function mountMailGlobals() {
    window.mailUI = mailUI;
    window.refreshMails = async function() { return mailUI.loadMails(); };
    window.markAllAsRead = async function() { return mailUI.markAllAsRead(); };
    window.openMail = async function(mailId) { return mailUI.openMail(mailId); };
    window.claimAttachment = async function(mailId) { return mailUI.claimAttachment(mailId); };
    window.closeMailDetail = function() {
        const modal = document.getElementById('mailDetailModal');
        if (modal) modal.style.display = 'none';
    };
    window.goBack = function() {
        window.location.href = 'index.html';
    };
    return mailUI;
}
