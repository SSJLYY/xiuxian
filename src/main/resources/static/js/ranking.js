// 排行榜脚本
let currentRankingType = 'level';
let currentPlayerId = null;

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log('排行榜页面加载完成');
    initRankingPage();
});

/**
 * 初始化排行榜页面
 */
async function initRankingPage() {
    try {
        // 获取当前玩家信息
        const playerResponse = await api.get('/player/profile');
        if (playerResponse.success && playerResponse.data) {
            currentPlayerId = playerResponse.data.id;
            console.log('当前玩家ID:', currentPlayerId);
        }
        
        // 加载默认排行榜（等级榜）
        await loadRanking('level');
    } catch (error) {
        console.error('初始化排行榜失败:', error);
        showToast('初始化失败，请刷新页面重试', 'error');
    }
}

/**
 * 切换排行榜标签
 */
async function switchRankingTab(type) {
    currentRankingType = type;
    
    // 更新标签样式
    document.querySelectorAll('.ranking-tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    const activeBtn = document.querySelector(`.ranking-tab-btn[data-type="${type}"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }
    
    // 加载对应的排行榜
    await loadRanking(type);
}

/**
 * 加载排行榜数据
 */
async function loadRanking(type) {
    try {
        showLoading(true);
        
        // 根据类型获取对应的API端点
        let endpoint = '';
        switch(type) {
            case 'level':
                endpoint = '/ranking/level';
                break;
            case 'power':
                endpoint = '/ranking/power';
                break;
            case 'wealth':
                endpoint = '/ranking/wealth';
                break;
            case 'pet':
                endpoint = '/ranking/pet';
                break;
            default:
                endpoint = '/ranking/level';
        }
        
        // 获取排行榜数据
        const response = await api.get(endpoint, { size: 100 });
        
        if (response.success && response.data) {
            renderRankingList(response.data, type);
            await loadMyRank(type);
        } else {
            showToast(response.message || '获取排行榜失败', 'error');
            renderEmptyList();
        }
    } catch (error) {
        console.error('加载排行榜失败:', error);
        showToast('加载排行榜失败: ' + error.message, 'error');
        renderEmptyList();
    } finally {
        showLoading(false);
    }
}

/**
 * 渲染排行榜列表
 */
function renderRankingList(rankings, type) {
    const rankingList = document.getElementById('rankingList');
    
    if (!rankings || rankings.length === 0) {
        renderEmptyList();
        return;
    }
    
    let html = '';
    
    rankings.forEach((ranking, index) => {
        const rank = ranking.rank || (index + 1);
        const isTopThree = rank <= 3;
        const isCurrentPlayer = currentPlayerId && ranking.playerId === currentPlayerId;
        
        // 获取排名图标
        const rankDisplay = getRankDisplay(rank);
        
        // 获取分数显示
        const scoreDisplay = getScoreDisplay(type, ranking);
        
        // 构建排行项HTML
        html += `
            <div class="ranking-item ${isTopThree ? 'top-rank' : ''} ${isCurrentPlayer ? 'current-player' : ''}" data-rank="${rank}">
                <div class="rank-badge ${isTopThree ? 'rank-' + rank : ''}">
                    ${rankDisplay}
                </div>
                <div class="player-info-section">
                    <div class="player-name-display">
                        ${escapeHtml(ranking.playerName || '未知玩家')}
                        ${isCurrentPlayer ? '<span class="me-badge">我</span>' : ''}
                    </div>
                    <div class="player-realm-display">
                        ${escapeHtml(ranking.realm || '练气期')}
                    </div>
                </div>
                <div class="score-display">
                    ${scoreDisplay}
                </div>
            </div>
        `;
    });
    
    rankingList.innerHTML = html;
}

/**
 * 渲染空列表
 */
function renderEmptyList() {
    const rankingList = document.getElementById('rankingList');
    rankingList.innerHTML = `
        <div class="empty-state">
            <i class="fa fa-inbox" style="font-size: 48px; color: #a0aec0; margin-bottom: 16px;"></i>
            <p style="color: #718096; font-size: 16px;">暂无排名数据</p>
        </div>
    `;
}

/**
 * 加载我的排名
 */
async function loadMyRank(type) {
    try {
        const response = await api.get('/ranking/my-rank', { type: type });
        
        const myRankDisplay = document.getElementById('myRankDisplay');
        
        if (response.success && response.data) {
            const rank = response.data.rank;
            
            if (rank && rank !== '未上榜') {
                myRankDisplay.textContent = `第 ${rank} 名`;
                myRankDisplay.className = 'text-2xl font-bold text-accent';
                
                // 如果在前10名，高亮显示
                if (typeof rank === 'number' && rank <= 10) {
                    myRankDisplay.className = 'text-2xl font-bold gradient-text';
                }
            } else {
                myRankDisplay.textContent = '未上榜';
                myRankDisplay.className = 'text-2xl font-bold text-gray-500';
            }
        } else {
            myRankDisplay.textContent = '未上榜';
            myRankDisplay.className = 'text-2xl font-bold text-gray-500';
        }
    } catch (error) {
        console.error('加载我的排名失败:', error);
        const myRankDisplay = document.getElementById('myRankDisplay');
        myRankDisplay.textContent = '加载失败';
        myRankDisplay.className = 'text-2xl font-bold text-gray-500';
    }
}

/**
 * 获取排名显示
 */
function getRankDisplay(rank) {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return `<span class="rank-number">${rank}</span>`;
}

/**
 * 获取分数显示
 */
function getScoreDisplay(type, ranking) {
    const score = ranking.score || 0;
    
    switch(type) {
        case 'level':
            return `<span class="score-label">等级</span> <span class="score-value">${score}</span>`;
        case 'power':
            return `<span class="score-label">战力</span> <span class="score-value">${formatNumber(score)}</span>`;
        case 'wealth':
            return `<span class="score-label">灵石</span> <span class="score-value">${formatNumber(score)}</span>`;
        case 'pet':
            return `<span class="score-label">宠物战力</span> <span class="score-value">${formatNumber(score)}</span>`;
        default:
            return `<span class="score-value">${score}</span>`;
    }
}

/**
 * 格式化数字
 */
function formatNumber(num) {
    if (typeof num !== 'number') {
        num = parseInt(num) || 0;
    }
    
    if (num >= 100000000) {
        return (num / 100000000).toFixed(1) + '亿';
    } else if (num >= 10000) {
        return (num / 10000).toFixed(1) + '万';
    }
    return num.toLocaleString();
}

/**
 * HTML转义
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * 显示消息提示
 */
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    if (!toast || !toastMessage) return;
    
    toastMessage.textContent = message;
    toast.className = `toast ${type}`;
    toast.classList.remove('hidden');
    
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}

/**
 * 显示/隐藏加载动画
 */
function showLoading(show) {
    const loading = document.getElementById('loading');
    if (loading) {
        if (show) {
            loading.classList.remove('hidden');
        } else {
            loading.classList.add('hidden');
        }
    }
}
