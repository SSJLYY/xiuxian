/**
 * 宗门BOSS协作系统 - 前端实现
 * 提供BOSS信息展示、协作挑战、伤害排行、奖励领取
 */

class GuildBossSystem {
    constructor() {
        this.bossData = null;
        this.refreshInterval = null;
    }

    /**
     * 初始化宗门BOSS模块
     */
    async init() {
        const container = document.getElementById('guild-boss-container');
        if (!container) return;
        await this.loadBossData();
        this.startAutoRefresh();
    }

    /**
     * 加载BOSS数据
     */
    async loadBossData() {
        try {
            const response = await api.get('/guild/boss/current');
            if (response.success) {
                this.bossData = response.data;
                this.render();
            } else {
                this.renderError(response.message || '获取BOSS信息失败');
            }
        } catch (error) {
            this.renderError(error.message || '网络错误');
        }
    }

    /**
     * 渲染BOSS界面
     */
    render() {
        const container = document.getElementById('guild-boss-container');
        if (!container || !this.bossData) return;

        const boss = this.bossData;
        const isAlive = boss.status === 'ALIVE';
        const healthPct = boss.healthPercent || 0;
        const healthColor = healthPct > 50 ? '#4caf50' : healthPct > 25 ? '#ff9800' : '#f44336';

        container.innerHTML = `
            <div class="guild-boss-panel">
                <!-- BOSS信息区 -->
                <div class="boss-info-section">
                    <div class="boss-header">
                        <div class="boss-avatar ${isAlive ? 'alive' : 'defeated'}">
                            ${isAlive ? '👹' : '💀'}
                        </div>
                        <div class="boss-basic">
                            <h3 class="boss-name">${boss.name}</h3>
                            <span class="boss-level">Lv.${boss.level}</span>
                            <span class="boss-status-badge ${isAlive ? 'alive' : 'defeated'}">
                                ${isAlive ? '⚔️ 存活中' : '✅ 已击败'}
                            </span>
                        </div>
                    </div>
                    
                    <p class="boss-desc">${boss.description || ''}</p>
                    
                    <!-- 血条 -->
                    <div class="boss-health-bar-wrap">
                        <div class="boss-health-label">
                            <span>生命值</span>
                            <span>${this.formatNumber(boss.currentHealth)} / ${this.formatNumber(boss.maxHealth)}</span>
                        </div>
                        <div class="boss-health-bar">
                            <div class="boss-health-fill" 
                                 style="width:${healthPct}%; background:${healthColor}; transition: width 0.5s ease">
                            </div>
                        </div>
                        <div class="health-percent">${healthPct}%</div>
                    </div>
                    
                    <!-- 奖励预览 -->
                    <div class="boss-rewards-preview">
                        <span class="reward-item">💎 灵石 ${this.formatNumber(boss.rewardSpiritStones)}</span>
                        <span class="reward-item">⭐ 经验 ${this.formatNumber(boss.rewardExp)}</span>
                        <span class="reward-item">👥 ${boss.totalParticipants || 0} 人参战</span>
                    </div>
                </div>

                <!-- 我的参战数据 -->
                <div class="my-contribution-section">
                    <h4>我的贡献</h4>
                    <div class="contribution-stats">
                        <div class="contrib-stat">
                            <span class="stat-label">我的伤害</span>
                            <span class="stat-value highlight">${this.formatNumber(boss.myDamage || 0)}</span>
                        </div>
                        <div class="contrib-stat">
                            <span class="stat-label">伤害占比</span>
                            <span class="stat-value">${boss.myDamageRatio || '0%'}</span>
                        </div>
                        <div class="contrib-stat">
                            <span class="stat-label">贡献排名</span>
                            <span class="stat-value rank">第 ${boss.myRank || '-'} 名</span>
                        </div>
                        <div class="contrib-stat">
                            <span class="stat-label">今日剩余次数</span>
                            <span class="stat-value ${boss.remainingAttempts > 0 ? 'available' : 'exhausted'}">
                                ${boss.remainingAttempts || 0} / ${boss.maxDailyAttempts || 5}
                            </span>
                        </div>
                    </div>

                    <!-- 操作按钮 -->
                    <div class="boss-actions">
                        ${isAlive && (boss.remainingAttempts || 0) > 0 ? `
                            <button class="btn-challenge" onclick="guildBossSystem.challenge()">
                                <span class="btn-icon">⚔️</span>
                                <span>发起攻击</span>
                            </button>
                        ` : isAlive ? `
                            <button class="btn-challenge disabled" disabled>
                                <span class="btn-icon">⏳</span>
                                <span>今日次数已用完</span>
                            </button>
                        ` : ''}
                        
                        ${boss.canClaimReward ? `
                            <button class="btn-claim-reward" onclick="guildBossSystem.claimReward()">
                                <span class="btn-icon">🎁</span>
                                <span>领取击败奖励</span>
                            </button>
                        ` : boss.rewardClaimed ? `
                            <button class="btn-claim-reward claimed" disabled>
                                <span class="btn-icon">✅</span>
                                <span>奖励已领取</span>
                            </button>
                        ` : ''}
                    </div>
                    
                    ${!isAlive ? `
                        <div class="boss-next-spawn">
                            <i class="fas fa-clock"></i>
                            下次BOSS: ${this.formatDateTime(boss.nextSpawnAt)}
                        </div>
                    ` : ''}
                </div>

                <!-- 伤害排行榜 -->
                <div class="damage-ranking-section">
                    <h4>⚔️ 伤害排行榜</h4>
                    <div class="damage-ranking-list">
                        ${(boss.damageRanking || []).map(entry => `
                            <div class="damage-rank-item ${entry.rank <= 3 ? 'top-rank' : ''}">
                                <span class="rank-badge">${entry.rank === 1 ? '🥇' : entry.rank === 2 ? '🥈' : entry.rank === 3 ? '🥉' : entry.rank}</span>
                                <span class="player-name">${entry.playerName || '未知'}</span>
                                <span class="damage-value">${this.formatNumber(entry.damage)}</span>
                                <span class="damage-ratio">${entry.ratio}</span>
                            </div>
                        `).join('')}
                        ${(!boss.damageRanking || boss.damageRanking.length === 0) ? 
                            '<div class="empty-ranking">暂无参战记录，率先发起攻击！</div>' : ''}
                    </div>
                    <div class="total-damage-bar">
                        <span class="label">宗门总伤害：</span>
                        <span class="value">${this.formatNumber(boss.totalDamage || 0)}</span>
                    </div>
                </div>
            </div>
        `;
    }

    renderError(msg) {
        const container = document.getElementById('guild-boss-container');
        if (!container) return;
        container.innerHTML = `
            <div class="guild-boss-error">
                <p>${msg.includes('宗门') ? msg : '⚠️ 你尚未加入宗门，无法参与BOSS讨伐'}</p>
                ${!msg.includes('宗门') ? '<button onclick="showModule(\'guild\')">前往宗门</button>' : ''}
            </div>
        `;
    }

    /**
     * 挑战BOSS
     */
    async challenge() {
        const btn = document.querySelector('.btn-challenge');
        if (btn) btn.disabled = true;

        try {
            const response = await api.post('/guild/boss/challenge');
            if (response.success) {
                const data = response.data;
                
                // 造成伤害动画
                this.showDamageAnimation(data.damage);
                
                if (data.bossDefeated) {
                    this.showVictoryEffect();
                }
                
                this.showToast(response.message || `造成 ${this.formatNumber(data.damage)} 伤害！`, 'success');
                
                // 延迟刷新，让动画播完
                setTimeout(() => this.loadBossData(), 800);
                
                // 刷新玩家数据
                if (window.authManager?.loadPlayerProfile) {
                    window.authManager.loadPlayerProfile();
                }
            } else {
                this.showToast(response.message || '攻击失败', 'error');
                if (btn) btn.disabled = false;
            }
        } catch (e) {
            this.showToast(e.message, 'error');
            if (btn) btn.disabled = false;
        }
    }

    /**
     * 领取奖励
     */
    async claimReward() {
        try {
            const response = await api.post('/guild/boss/claim-reward');
            if (response.success) {
                const { spiritStones, exp, message } = response.data;
                this.showToast(`${message} 灵石+${spiritStones}, 经验+${exp}`, 'success');
                await this.loadBossData();
                if (window.authManager?.loadPlayerProfile) {
                    window.authManager.loadPlayerProfile();
                }
            } else {
                this.showToast(response.message || '领取失败', 'error');
            }
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    }

    /**
     * 伤害数字飘动效果
     */
    showDamageAnimation(damage) {
        const container = document.getElementById('guild-boss-container');
        if (!container) return;
        const el = document.createElement('div');
        el.className = 'boss-damage-float';
        el.textContent = `-${this.formatNumber(damage)}`;
        container.appendChild(el);
        setTimeout(() => el.remove(), 1200);
    }

    showVictoryEffect() {
        const container = document.getElementById('guild-boss-container');
        if (!container) return;
        const overlay = document.createElement('div');
        overlay.className = 'boss-defeated-overlay';
        overlay.innerHTML = '<div class="victory-text">⚔️ BOSS已击败！</div>';
        container.appendChild(overlay);
        setTimeout(() => overlay.remove(), 2500);
    }

    startAutoRefresh() {
        // 每30秒自动刷新一次BOSS状态
        this.refreshInterval = setInterval(() => {
            if (document.getElementById('guild-boss-container')) {
                this.loadBossData();
            } else {
                this.stopAutoRefresh();
            }
        }, 30000);
    }

    stopAutoRefresh() {
        if (this.refreshInterval) {
            clearInterval(this.refreshInterval);
            this.refreshInterval = null;
        }
    }

    formatNumber(n) {
        n = Number(n) || 0;
        if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿';
        if (n >= 10000) return (n / 10000).toFixed(1) + '万';
        return n.toLocaleString();
    }

    formatDateTime(dt) {
        if (!dt) return '未知';
        return new Date(dt).toLocaleDateString('zh-CN') + ' ' + 
               new Date(dt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    }

    showToast(msg, type = 'info') {
        if (window.authManager?.showToast) window.authManager.showToast(msg, type);
    }
}

// 初始化
const guildBossSystem = new GuildBossSystem();
window.guildBossSystem = guildBossSystem;

console.log('[GuildBoss] 宗门BOSS系统已加载');
