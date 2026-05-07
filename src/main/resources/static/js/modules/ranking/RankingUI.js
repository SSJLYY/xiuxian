import { rankingService } from './RankingService.js';

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

function formatNumber(num) {
    const value = Number(num) || 0;
    if (value >= 100000000) return `${(value / 100000000).toFixed(1)}亿`;
    if (value >= 10000) return `${(value / 10000).toFixed(1)}万`;
    return value.toLocaleString();
}

function hasGameLayout() {
    return !!document.getElementById('ranking-module');
}

export class RankingUI {
    constructor() {
        this.currentType = 'level';
    }

    async init() {
        return this.loadRanking(this.currentType);
    }

    async switchTab(type) {
        this.currentType = type;
        if (hasGameLayout()) {
            document.querySelectorAll('#ranking-module .tab-btn').forEach(btn => {
                btn.classList.toggle('active', btn.dataset.rankTab === type);
            });
        } else {
            document.querySelectorAll('.ranking-tab-btn').forEach(btn => {
                btn.classList.toggle('active', btn.dataset.type === type);
            });
        }
        return this.loadRanking(type);
    }

    async loadRanking(type = this.currentType) {
        const listContainer = document.getElementById('rankingList');
        if (!listContainer) return;
        listContainer.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载排行榜中...</p></div>';
        try {
            const [ranking, myRank] = await Promise.all([
                rankingService.getRanking(type),
                rankingService.getMyRank(type)
            ]);
            this.renderRankingList(ranking, type);
            this.renderMyRank(myRank);
        } catch (error) {
            listContainer.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
            showToast(`加载排行榜失败: ${error.message}`, 'error');
        }
    }

    renderMyRank(myRank) {
        const display = document.getElementById('myRankDisplay');
        if (!display) return;
        display.textContent = myRank?.rank ? `第 ${myRank.rank} 名` : '未上榜';
    }

    renderRankingList(rankings, type) {
        const container = document.getElementById('rankingList');
        if (!container) return;
        if (!rankings.length) {
            container.innerHTML = '<div class="empty-state">暂无排名数据</div>';
            return;
        }
        container.innerHTML = rankings.map((ranking, index) => {
            const rank = ranking.rank || index + 1;
            return `
                <div class="ranking-item ${rank <= 3 ? 'top-rank' : ''}" data-rank="${rank}">
                    <div class="rank-badge ${rank <= 3 ? `rank-${rank}` : ''}">${rank <= 3 ? ['🥇', '🥈', '🥉'][rank - 1] : `<span class="rank-number">${rank}</span>`}</div>
                    <div class="player-info-section">
                        <div class="player-name-display">${escapeText(ranking.playerName || '未知玩家')}</div>
                        <div class="player-realm-display">${escapeText(ranking.realm || '练气期')}</div>
                    </div>
                    <div class="score-display">${this.scoreDisplay(type, ranking.score || 0)}</div>
                </div>
            `;
        }).join('');
    }

    scoreDisplay(type, score) {
        const labelMap = { level: '等级', power: '战力', wealth: '灵石', pet: '宠物战力' };
        return `<span class="score-label">${labelMap[type] || '分数'}</span> <span class="score-value">${type === 'level' ? score : formatNumber(score)}</span>`;
    }
}

export const rankingUI = new RankingUI();
