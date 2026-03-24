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

// ==================== 「本周我能冲第几名」预测功能 ====================

let rankingDataCache = {};

/**
 * 覆盖原 loadRanking 函数，增加缓存 + 预测面板
 */
const _originalLoadRanking = loadRanking;

async function loadRanking(type) {
    try {
        showLoading(true);

        let endpoint = '';
        switch(type) {
            case 'level':  endpoint = '/ranking/level'; break;
            case 'power':  endpoint = '/ranking/power'; break;
            case 'wealth': endpoint = '/ranking/wealth'; break;
            case 'pet':    endpoint = '/ranking/pet'; break;
            default:       endpoint = '/ranking/level';
        }

        const response = await api.get(endpoint, { size: 100 });

        if (response.success && response.data) {
            rankingDataCache[type] = response.data;
            renderRankingList(response.data, type);
            await loadMyRank(type);
            await renderRankPrediction(type, response.data);
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
 * 渲染「本周我能冲第几名」预测面板
 * 逻辑：
 *   1. 取榜单Top50的分数分布
 *   2. 获取玩家当前分数
 *   3. 估算本周增长（基于过去均速）
 *   4. 预测排名区间
 */
async function renderRankPrediction(type, rankings) {
    // 确保预测面板容器存在
    let predPanel = document.getElementById('rankPredictionPanel');
    if (!predPanel) {
        // 在"我的排名"卡片后插入
        const myRankCard = document.querySelector('.my-rank-card');
        if (myRankCard) {
            predPanel = document.createElement('div');
            predPanel.id = 'rankPredictionPanel';
            predPanel.className = 'rank-prediction-card';
            myRankCard.insertAdjacentElement('afterend', predPanel);
        } else {
            return;
        }
    }

    try {
        const myRankResp = await api.get('/ranking/my-rank', { type });
        if (!myRankResp.success || !myRankResp.data) {
            predPanel.innerHTML = '';
            return;
        }

        const myData = myRankResp.data;
        const myScore = Number(myData.score) || 0;
        const myRank = typeof myData.rank === 'number' ? myData.rank : null;

        if (myScore === 0) {
            predPanel.innerHTML = '';
            return;
        }

        // 分析排行榜分数差距
        const scores = rankings.map(r => Number(r.score) || 0).filter(s => s > 0).sort((a, b) => b - a);
        const nearbyGap = analyzeNearbyGap(myScore, myRank, scores, type);

        predPanel.innerHTML = `
            <div class="prediction-header">
                <span class="prediction-icon">📊</span>
                <h4>本周冲榜分析</h4>
                <button class="prediction-refresh" onclick="renderRankPrediction('${type}', rankingDataCache['${type}'])">
                    <i class="fas fa-sync-alt"></i>
                </button>
            </div>

            <div class="prediction-body">
                <!-- 当前情况 -->
                <div class="prediction-row">
                    <span class="pred-label">当前排名</span>
                    <span class="pred-value current">${myRank ? '第 ' + myRank + ' 名' : '未上榜'}</span>
                </div>
                <div class="prediction-row">
                    <span class="pred-label">当前分数</span>
                    <span class="pred-value">${formatPredScore(type, myScore)}</span>
                </div>

                <!-- 追击目标 -->
                ${nearbyGap.chaseTarget ? `
                    <div class="prediction-chase">
                        <div class="chase-header">⬆️ 追击目标</div>
                        <div class="chase-item">
                            <span class="chase-rank">第 ${nearbyGap.chaseTarget.rank} 名</span>
                            <span class="chase-name">${nearbyGap.chaseTarget.name || '未知'}</span>
                            <span class="chase-gap">差距 ${formatPredScore(type, nearbyGap.chaseTarget.gap)}</span>
                        </div>
                    </div>
                ` : ''}

                <!-- 被追情况 -->
                ${nearbyGap.pursuer ? `
                    <div class="prediction-pursuer">
                        <div class="pursuer-header">⬇️ 身后追兵</div>
                        <div class="pursuer-item">
                            <span class="pursuer-rank">第 ${nearbyGap.pursuer.rank} 名</span>
                            <span class="pursuer-name">${nearbyGap.pursuer.name || '未知'}</span>
                            <span class="pursuer-gap">领先 ${formatPredScore(type, nearbyGap.pursuer.lead)}</span>
                        </div>
                    </div>
                ` : ''}

                <!-- 冲榜建议 -->
                <div class="prediction-tip">
                    <span class="tip-icon">💡</span>
                    <span class="tip-text">${nearbyGap.advice}</span>
                </div>
            </div>
        `;
    } catch (err) {
        console.warn('排名预测失败:', err);
        predPanel.innerHTML = '';
    }
}

/**
 * 分析与前后名次的分数差距
 */
function analyzeNearbyGap(myScore, myRank, scores, type) {
    // 找到我排名前一位
    let chaseTarget = null;
    let pursuer = null;

    if (myRank && myRank > 1) {
        const targetScore = scores[myRank - 2]; // 排在我前一位的分数
        if (targetScore !== undefined && targetScore > myScore) {
            chaseTarget = {
                rank: myRank - 1,
                gap: targetScore - myScore,
                name: '???'
            };
        }
    }

    if (myRank && myRank < scores.length) {
        const pursuerScore = scores[myRank]; // 排在我后一位的分数
        if (pursuerScore !== undefined && pursuerScore < myScore) {
            pursuer = {
                rank: myRank + 1,
                lead: myScore - pursuerScore,
                name: '???'
            };
        }
    }

    // 根据差距生成建议
    let advice = '';
    if (!myRank) {
        const last = scores[scores.length - 1] || 0;
        const gapToEnter = last > 0 ? last - myScore + 1 : 0;
        advice = `距离上榜还需提升约 ${formatPredScore(type, gapToEnter)}，继续努力！`;
    } else if (chaseTarget && chaseTarget.gap < myScore * 0.05) {
        advice = `与上一名差距极小！今天多修炼一下就能超越！`;
    } else if (chaseTarget && myRank <= 10) {
        advice = `继续保持，预计本周能稳定在前${myRank}名！`;
    } else if (myRank <= 3) {
        advice = `🏆 当前在前三名！稳住优势，防止被追赶！`;
    } else {
        advice = `当前排名稳定，坚持挂机修炼可持续提升！`;
    }

    return { chaseTarget, pursuer, advice };
}

function formatPredScore(type, score) {
    score = Number(score) || 0;
    switch (type) {
        case 'wealth': return `${formatNumber(score)} 灵石`;
        case 'power': return `${formatNumber(score)} 战力`;
        case 'level': return `${score} 级`;
        default: return formatNumber(score);
    }
}

// ==================== 追加CSS样式 ====================
const rankingExtraStyle = document.createElement('style');
rankingExtraStyle.textContent = `
    .rank-prediction-card {
        background: linear-gradient(135deg, rgba(26,26,46,0.95), rgba(22,33,62,0.95));
        border: 1px solid rgba(212,175,55,0.25);
        border-radius: 12px;
        padding: 1.2rem;
        margin-bottom: 1.5rem;
        box-shadow: 0 4px 16px rgba(0,0,0,0.3);
    }
    .prediction-header {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin-bottom: 1rem;
        padding-bottom: 0.75rem;
        border-bottom: 1px solid rgba(255,255,255,0.08);
    }
    .prediction-icon { font-size: 1.2rem; }
    .prediction-header h4 { margin: 0; flex: 1; color: #d4af37; font-size: 1rem; }
    .prediction-refresh {
        background: none; border: none; color: #7fffd4; cursor: pointer; font-size: 0.85rem;
        padding: 4px 8px; border-radius: 4px; transition: all 0.2s;
    }
    .prediction-refresh:hover { background: rgba(127,255,212,0.1); }
    .prediction-body { display: flex; flex-direction: column; gap: 0.6rem; }
    .prediction-row {
        display: flex; justify-content: space-between; align-items: center;
        padding: 0.4rem 0.6rem; background: rgba(255,255,255,0.03); border-radius: 6px;
    }
    .pred-label { color: #a0a0a0; font-size: 0.9rem; }
    .pred-value { color: #e8e8e8; font-weight: bold; }
    .pred-value.current { color: #7fffd4; }
    .prediction-chase, .prediction-pursuer {
        padding: 0.6rem; border-radius: 8px;
    }
    .prediction-chase { background: rgba(76,175,80,0.08); border: 1px solid rgba(76,175,80,0.2); }
    .prediction-pursuer { background: rgba(244,67,54,0.08); border: 1px solid rgba(244,67,54,0.2); }
    .chase-header, .pursuer-header {
        font-size: 0.8rem; font-weight: bold; margin-bottom: 0.4rem;
        color: #a5d6a7;
    }
    .pursuer-header { color: #ef9a9a; }
    .chase-item, .pursuer-item {
        display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;
    }
    .chase-rank, .pursuer-rank { color: #d4af37; font-weight: bold; font-size: 0.9rem; }
    .chase-name, .pursuer-name { color: #e8e8e8; flex: 1; }
    .chase-gap { color: #ef5350; font-size: 0.85rem; }
    .pursuer-gap { color: #66bb6a; font-size: 0.85rem; }
    .prediction-tip {
        display: flex; gap: 0.5rem; align-items: flex-start;
        padding: 0.6rem; background: rgba(212,175,55,0.06);
        border: 1px solid rgba(212,175,55,0.15); border-radius: 6px;
    }
    .tip-icon { font-size: 1rem; flex-shrink: 0; }
    .tip-text { color: #c0a060; font-size: 0.9rem; line-height: 1.5; }
`;
document.head.appendChild(rankingExtraStyle);

