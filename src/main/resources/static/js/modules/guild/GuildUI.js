import { guildService } from './GuildService.js';

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

function hasGamePageLayout() {
    return !!document.getElementById('guild-module');
}

function roleText(role) {
    if (role === 'LEADER') return '宗主';
    if (role === 'ELDER' || role === 'OFFICER') return '长老';
    return '成员';
}

export class GuildUI {
    constructor() {
        this.myGuild = null;
        this.guildList = [];
    }

    async init() {
        return hasGamePageLayout() ? this.initGamePage() : this.initStandalonePage();
    }

    async initGamePage() {
        await this.loadGuildOverview();
    }

    async initStandalonePage() {
        this.bindStandaloneEvents();
        await this.loadStandaloneData();
    }

    bindStandaloneEvents() {
        document.querySelectorAll('.guild-tab-btn').forEach(btn => {
            btn.onclick = () => this.switchStandaloneTab(btn.dataset.tab);
        });
    }

    async loadGuildOverview() {
        try {
            this.myGuild = await guildService.getMyGuild();
            this.toggleGameMyGuildEntry(Boolean(this.myGuild));
            if (this.myGuild) {
                this.renderGameMyGuildSummary(this.myGuild);
            } else {
                this.hideGameMyGuildSummary();
            }
            await this.switchGameTab('list');
        } catch (error) {
            this.hideGameMyGuildSummary();
            showToast(`加载宗门数据失败: ${error.message}`, 'error');
        }
    }

    async switchGameTab(tab) {
        document.querySelectorAll('#guild-module .tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.guildTab === tab);
        });
        const listPanel = document.getElementById('guild-list-panel');
        const myPanel = document.getElementById('guild-my-panel');
        if (listPanel) listPanel.style.display = tab === 'list' ? '' : 'none';
        if (myPanel) myPanel.style.display = tab === 'my' ? '' : 'none';
        if (tab === 'list') return this.loadGameGuildList();
        return this.loadGameMyGuildDetail();
    }

    async loadGameGuildList() {
        const panel = document.getElementById('guild-list-panel');
        if (!panel) return;
        panel.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载宗门列表...</p></div>';
        try {
            this.guildList = await guildService.getGuildList();
            panel.innerHTML = `
                <div class="guild-create-bar mb-4 p-3 rounded flex items-center justify-between" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                    <div class="text-sm text-muted">创建宗门需要 10000 灵石</div>
                    <button class="btn btn-sm btn-primary" onclick="showCreateGuildForm()"><i class="fa-solid fa-plus"></i> 创建宗门</button>
                </div>
                ${this.guildList.length === 0 ? '<div class="empty-state">暂无宗门，创建一个吧</div>' : `
                    <div class="guild-grid" style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:15px;">
                        ${this.guildList.map(guild => `
                            <div class="guild-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                                <div class="flex items-center justify-between mb-2">
                                    <h4 class="font-bold">${escapeText(guild.guildName || guild.name || '宗门')}</h4>
                                    <span class="text-xs text-muted">Lv.${guild.level || 1}</span>
                                </div>
                                <div class="text-sm text-muted mb-2">${escapeText(guild.description || '暂无描述')}</div>
                                <div class="flex gap-4 text-xs text-muted mb-3">
                                    <span><i class="fa-solid fa-users"></i> ${guild.memberCount || 0}/${guild.maxMembers || 50}</span>
                                    <span><i class="fa-solid fa-gem"></i> ${guild.guildFunds || guild.treasury || 0}</span>
                                </div>
                                <button class="btn btn-sm w-full" onclick="applyToGuild(${guild.id})">申请加入</button>
                            </div>
                        `).join('')}
                    </div>
                `}
            `;
        } catch (error) {
            panel.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    renderGameMyGuildSummary(guild) {
        const infoEl = document.getElementById('guild-my-info');
        const actionsEl = document.getElementById('guild-actions');
        if (!infoEl || !actionsEl) return;
        infoEl.style.display = '';
        actionsEl.style.display = '';
        infoEl.innerHTML = `
            <div class="flex items-center justify-between mb-2">
                <h3 class="font-bold" style="color:var(--accent-gold);"><i class="fa-solid fa-chess-rook"></i> ${escapeText(guild.guildName || guild.name || '宗门')}</h3>
                <span class="text-sm text-muted">等级 ${guild.level || 1}</span>
            </div>
            <div class="text-sm text-muted mb-2">${escapeText(guild.description || '暂无描述')}</div>
            <div class="flex gap-4 text-xs">
                <span class="text-muted"><i class="fa-solid fa-users"></i> 成员 ${guild.memberCount || 0}/${guild.maxMembers || 50}</span>
                <span class="text-muted"><i class="fa-solid fa-gem"></i> 宗门资金 ${guild.guildFunds || guild.treasury || 0}</span>
                <span class="text-muted"><i class="fa-solid fa-star"></i> 贡献 ${guild.myContribution || guild.contribution || 0}</span>
            </div>
        `;
        actionsEl.innerHTML = `
            <button class="btn btn-sm" onclick="showDonateForm()"><i class="fa-solid fa-donate"></i> 捐献灵石</button>
            <button class="btn btn-sm" onclick="switchGuildTab('my')"><i class="fa-solid fa-users"></i> 宗门成员</button>
            <button class="btn btn-sm btn-danger" onclick="leaveGuild()"><i class="fa-solid fa-sign-out-alt"></i> 退出宗门</button>
        `;
    }

    hideGameMyGuildSummary() {
        const infoEl = document.getElementById('guild-my-info');
        const actionsEl = document.getElementById('guild-actions');
        if (infoEl) infoEl.style.display = 'none';
        if (actionsEl) actionsEl.style.display = 'none';
    }

    toggleGameMyGuildEntry(visible) {
        const myBtn = document.getElementById('guild-my-tab-btn');
        if (myBtn) myBtn.style.display = visible ? '' : 'none';
    }

    async loadGameMyGuildDetail() {
        const panel = document.getElementById('guild-my-panel');
        if (!panel) return;
        panel.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载中...</p></div>';
        try {
            this.myGuild = await guildService.getMyGuild();
            if (!this.myGuild) {
                panel.innerHTML = '<div class="empty-state">您还没有加入宗门</div>';
                return;
            }
            const detail = await guildService.getGuildDetail(this.myGuild.id);
            const members = detail.members || [];
            panel.innerHTML = `
                <div class="mb-4">
                    <h4 class="font-bold mb-3"><i class="fa-solid fa-users"></i> 宗门成员</h4>
                    <div style="display:flex;flex-direction:column;gap:8px;">
                        ${members.map(member => `
                            <div class="flex items-center justify-between p-3 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                                <div class="flex items-center gap-2">
                                    <div class="font-semibold">${escapeText(member.playerName || member.nickname || '成员')}</div>
                                    <span class="text-xs text-muted">Lv.${member.level || '?'}</span>
                                </div>
                                <div class="flex items-center gap-2">
                                    <span class="text-xs text-accent">贡献 ${member.contribution || 0}</span>
                                    <span class="text-xs ${member.role === 'LEADER' ? 'text-yellow-400' : 'text-muted'}">${roleText(member.role)}</span>
                                </div>
                            </div>
                        `).join('') || '<div class="empty-state">暂无成员数据</div>'}
                    </div>
                </div>
            `;
        } catch (error) {
            panel.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    async applyToGuild(guildId) {
        await guildService.applyGuild(guildId);
        showToast('申请已提交，等待宗主审核', 'success');
    }

    async leaveGuild() {
        await guildService.leaveGuild();
        this.myGuild = null;
        showToast('已退出宗门', 'info');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        return hasGamePageLayout() ? this.loadGuildOverview() : this.loadStandaloneData();
    }

    async createGuild(name, description) {
        await guildService.createGuild(name, description);
        showToast('宗门创建成功', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        return hasGamePageLayout() ? this.loadGuildOverview() : this.loadStandaloneData();
    }

    async donateGuild(amount) {
        await guildService.donateGuild(amount);
        showToast('捐献成功', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        return hasGamePageLayout() ? this.loadGuildOverview() : this.loadStandaloneData();
    }

    async loadStandaloneData() {
        this.myGuild = await guildService.getMyGuild().catch(() => null);
        this.guildList = await guildService.getGuildList().catch(() => []);
        this.renderStandaloneMyGuild();
        this.renderStandaloneGuildList();
        if (window.guildBossSystem?.init && document.getElementById('guild-boss-container')) {
            window.guildBossSystem.init();
        }
    }

    switchStandaloneTab(tab) {
        document.querySelectorAll('.guild-tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tab);
        });
        const mapping = {
            'my-guild': 'myGuildSection',
            'guild-boss': 'guildBossSection',
            'guild-list': 'guildListSection',
            'create-guild': 'createGuildSection'
        };
        Object.entries(mapping).forEach(([key, id]) => {
            const section = document.getElementById(id);
            if (section) section.classList.toggle('active', key === tab);
        });
        if (tab === 'guild-boss' && window.guildBossSystem?.init) {
            window.guildBossSystem.init();
        }
    }

    renderStandaloneMyGuild() {
        const container = document.getElementById('myGuildContent') || document.getElementById('myGuildInfo');
        if (!container) return;
        if (!this.myGuild) {
            container.innerHTML = `
                <div class="my-guild-card">
                    <div class="text-center py-8">
                        <div class="text-xl mb-2">你还没有加入宗门</div>
                        <div class="text-sm text-muted">可以前往宗门列表申请加入，或自行创建宗门</div>
                    </div>
                </div>
            `;
            return;
        }
        const expToNext = this.myGuild.expToNext || 0;
        const exp = this.myGuild.exp || 0;
        const progress = expToNext > 0 ? Math.min(100, (exp / expToNext) * 100) : 0;
        container.innerHTML = `
            <div class="my-guild-card">
                <div class="guild-header-section">
                    <div class="guild-name-display">${escapeText(this.myGuild.guildName || this.myGuild.name || '宗门')}</div>
                    <div class="guild-level-badge">Lv.${this.myGuild.level || 1}</div>
                </div>
                <div class="text-sm mb-4">${escapeText(this.myGuild.description || '暂无简介')}</div>
                <div class="guild-stats-grid">
                    <div class="guild-stat-item"><div class="guild-stat-value">${this.myGuild.memberCount || 0}/${this.myGuild.maxMembers || 50}</div><div class="guild-stat-label">成员</div></div>
                    <div class="guild-stat-item"><div class="guild-stat-value">${this.myGuild.guildFunds || this.myGuild.treasury || 0}</div><div class="guild-stat-label">资金</div></div>
                    <div class="guild-stat-item"><div class="guild-stat-value">${this.myGuild.myContribution || this.myGuild.contribution || 0}</div><div class="guild-stat-label">贡献</div></div>
                    <div class="guild-stat-item"><div class="guild-stat-value">${progress.toFixed(1)}%</div><div class="guild-stat-label">经验</div></div>
                </div>
                <div class="guild-progress-section">
                    <div class="guild-progress-bar"><div class="guild-progress-fill" style="width:${progress}%;"></div></div>
                    <div class="guild-progress-text">${exp}/${expToNext || 100}</div>
                </div>
                <div class="flex gap-3 flex-wrap mt-4">
                    <button class="btn btn-primary" onclick="showDonateForm()">捐献灵石</button>
                    <button class="btn btn-secondary" onclick="leaveGuild()">退出宗门</button>
                </div>
            </div>
        `;
    }

    renderStandaloneGuildList() {
        const container = document.getElementById('guildListContent') || document.getElementById('guildList');
        if (!container) return;
        if (this.guildList.length === 0) {
            container.innerHTML = '<div class="empty-state">暂无宗门</div>';
            return;
        }
        container.innerHTML = this.guildList.map(guild => `
            <div class="guild-list-card">
                <div class="guild-list-header">
                    <div class="guild-list-name">${escapeText(guild.guildName || guild.name || '宗门')}</div>
                    <div class="guild-level-badge">Lv.${guild.level || 1}</div>
                </div>
                <div class="text-sm mb-3">${escapeText(guild.description || '暂无简介')}</div>
                <div class="guild-list-stats">
                    <div class="guild-list-stat"><strong>${guild.memberCount || 0}/${guild.maxMembers || 50}</strong>成员</div>
                    <div class="guild-list-stat"><strong>${guild.guildFunds || guild.treasury || 0}</strong>资金</div>
                </div>
                <button class="btn btn-primary mt-4" onclick="applyToGuild(${guild.id})">申请加入</button>
            </div>
        `).join('');
    }
}

export const guildUI = new GuildUI();
