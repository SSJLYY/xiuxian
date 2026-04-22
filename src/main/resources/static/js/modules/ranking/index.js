export { rankingService } from './RankingService.js';
export { rankingUI } from './RankingUI.js';

export function mountRankingGlobals() {
    window.rankingUI = rankingUI;
    window.switchRankingTab = function(tab) {
        return rankingUI.switchTab(tab);
    };
    return rankingUI;
}
