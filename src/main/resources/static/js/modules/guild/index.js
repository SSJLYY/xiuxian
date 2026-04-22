export { guildService } from './GuildService.js';
export { guildUI } from './GuildUI.js';

export function mountGuildGlobals() {
    window.guildUI = guildUI;

    window.switchGuildTab = function(tab) {
        return document.getElementById('guild-module') ? guildUI.switchGameTab(tab) : guildUI.switchStandaloneTab(tab);
    };

    window.applyToGuild = async function(guildId) {
        return guildUI.applyToGuild(guildId);
    };

    window.leaveGuild = async function() {
        if (!confirm('确定要退出宗门吗？')) return;
        return guildUI.leaveGuild();
    };

    window.showCreateGuildForm = function() {
        const name = prompt('宗门名称:');
        if (!name) return;
        const description = prompt('宗门描述 (可选):') || '';
        return guildUI.createGuild(name, description);
    };

    window.showDonateForm = function() {
        const amount = prompt('捐献灵石数量:');
        if (!amount || isNaN(amount) || parseInt(amount, 10) <= 0) return;
        return guildUI.donateGuild(parseInt(amount, 10));
    };

    window.createGuild = function() {
        const name = document.getElementById('guildNameInput')?.value?.trim();
        const description = document.getElementById('guildDescInput')?.value?.trim() || '';
        if (!name) {
            if (window.authManager?.showToast) window.authManager.showToast('请输入宗门名称', 'error');
            return;
        }
        return guildUI.createGuild(name, description);
    };

    return guildUI;
}
