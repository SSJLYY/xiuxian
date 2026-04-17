import { rankingService } from './RankingService.js';
import { loading } from '../../components/Loading.js';
import { escapeHtml } from '../../core/utils/Security.js';

export class RankingUI {
    init() {
        this.loadRanking();
    }

    async loadRanking() {
        loading.show();
        try {
            const data = await rankingService.getRanking('level');
            this.renderRanking(data);
        } catch (error) {
            console.error('加载失败', error);
        } finally {
            loading.hide();
        }
    }

    renderRanking(data) {
        const container = document.getElementById('rankingContainer');
        if (!container) return;

        container.innerHTML = data.map((player, index) => `
            <div class="ranking-item rank-${index < 3 ? index + 1 : 'other'}">
                <div class="rank">${index + 1}</div>
                <div class="player-name">${escapeHtml(player.playerName)}</div>
                <div class="player-level">Lv.${escapeHtml(player.level)}</div>
            </div>
        `).join('');
    }
}

export const rankingUI = new RankingUI();
