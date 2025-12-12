// 成就系统脚本
let allAchievements = [];
let currentFilter = 'all';

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log('成就页面加载完成');
    initAchievementPage();
});

/**
 * 初始化成就页面
 */
async function initAchievementPage() {
    try {
        // 加载成就数据
        await loadAchievements();
        
        // 加载成就统计
        await loadAchievementStats();
    } catch (error) {
        console.error('初始化成就页面失败:', error);
        showToast('初始化失败，请刷新页面重试', 'error');
    }
}

/**
 * 加载成就列表
 */
async function loadAchievements() {
    try {
        showLoading(true);
        
        const response = await api.get('/achievement/list');
        
        if (response.success && response.data) {
            allAchievements = response.data;
            renderAchievements(allAchievements);
        } else {
            showToast(response.message || '获取成就列表失败', 'error');
            renderEmptyState();
        }
    } catch (error) {
        console.error('加载成就列表失败:', error);
        showToast('加载成就列表失败: ' + error.message, 'error');
        renderEmptyState();
    } finally {
        showLoading(false);
    }
}

/**
 * 加载成就统计
 */
async function loadAchievementStats() {
    try {
        const response = await api.get('/achievement/progress');
        
        if (response.success && response.data) {
            updateStatsDisplay(response.data);
        }
    } catch (error) {
        console.error('加载成就统计失败:', error);
    }
}

/**
 * 更新统计显示
 */
function updateStatsDisplay(stats) {
    document.getElementById('totalAchievements').textContent = stats.totalCount || 0;
    document.getElementById('completedAchievements').textContent = stats.completedCount || 0;
    document.getElementById('claimedAchievements').textContent = stats.claimedCount || 0;
    document.getElementById('completionRate').textContent = stats.completionRate + '%' || '0%';
}

/**
 * 渲染成就列表
 */
function renderAchievements(achievements) {
    const achievementList = document.getElementById('achievementList');
    
    if (!achievements || achievements.length === 0) {
        renderEmptyState();
        return;
    }
    
    let html = '';
    
    achievements.forEach(achievement => {
        const progressPercent = calculateProgressPercent(achievement);
        const statusClass = getStatusClass(achievement);
        const statusText = getStatusText(achievement);
        const iconClass = getIconClass(achievement);
        
        html += `
            <div class="achievement-card ${statusClass}" data-id="${achievement.id}" data-type="${achievement.achievementType}">
                <div class="achievement-status-badge ${statusClass}">
                    ${statusText}
                </div>
                
                <div class="achievement-icon">
                    <i class="fa ${iconClass}" style="color: white;"></i>
                </div>
                
                <div class="achievement-title">
                    ${escapeHtml(achievement.name)}
                </div>
                
                <div class="achievement-description">
                    ${escapeHtml(achievement.description)}
                </div>
                
                <div class="achievement-progress-container">
                    <div class="achievement-progress-bar">
                        <div class="achievement-progress-fill" style="width: ${progressPercent}%"></div>
                    </div>
                    <div class="achievement-progress-text">
                        ${achievement.progress || 0} / ${achievement.conditionValue}
                    </div>
                </div>
                
                <div class="achievement-rewards">
                    ${renderRewards(achievement)}
                </div>
                
                ${renderActionButton(achievement)}
            </div>
        `;
    });
    
    achievementList.innerHTML = html;
    
    // 为新完成的成就添加动画
    setTimeout(() => {
        achievements.forEach(achievement => {
            if (achievement.isCompleted && !achievement.isClaimed) {
                const card = document.querySelector(`.achievement-card[data-id="${achievement.id}"]`);
                if (card) {
                    card.classList.add('new-completion');
                }
            }
        });
    }, 100);
}

/**
 * 计算进度百分比
 */
function calculateProgressPercent(achievement) {
    if (!achievement.conditionValue || achievement.conditionValue === 0) {
        return 0;
    }
    
    const progress = achievement.progress || 0;
    const percent = Math.min((progress / achievement.conditionValue) * 100, 100);
    return Math.round(percent);
}

/**
 * 获取状态类名
 */
function getStatusClass(achievement) {
    if (achievement.isClaimed) {
        return 'claimed';
    } else if (achievement.isCompleted) {
        return 'completed';
    } else {
        return 'in-progress';
    }
}

/**
 * 获取状态文本
 */
function getStatusText(achievement) {
    if (achievement.isClaimed) {
        return '已领取';
    } else if (achievement.isCompleted) {
        return '已完成';
    } else {
        return '进行中';
    }
}

/**
 * 获取图标类名
 */
function getIconClass(achievement) {
    const typeIcons = {
        'LEVEL': 'fa-star',
        'COMBAT': 'fa-bolt',
        'CULTIVATION': 'fa-leaf',
        'COLLECTION': 'fa-cubes'
    };
    
    return typeIcons[achievement.achievementType] || 'fa-trophy';
}

/**
 * 渲染奖励
 */
function renderRewards(achievement) {
    let html = '';
    
    if (achievement.rewardExp && achievement.rewardExp > 0) {
        html += `<span class="achievement-reward-item">
            <i class="fa fa-star"></i> 经验 +${formatNumber(achievement.rewardExp)}
        </span>`;
    }
    
    if (achievement.rewardSpiritStones && achievement.rewardSpiritStones > 0) {
        html += `<span class="achievement-reward-item">
            <i class="fa fa-diamond"></i> 灵石 +${formatNumber(achievement.rewardSpiritStones)}
        </span>`;
    }
    
    if (achievement.rewardTitle) {
        html += `<span class="achievement-reward-item">
            <i class="fa fa-certificate"></i> 称号: ${escapeHtml(achievement.rewardTitle)}
        </span>`;
    }
    
    return html || '<span class="achievement-reward-item">暂无奖励</span>';
}

/**
 * 渲染操作按钮
 */
function renderActionButton(achievement) {
    if (achievement.isClaimed) {
        return `<button class="achievement-claim-btn" disabled>
            <i class="fa fa-check"></i> 已领取
        </button>`;
    } else if (achievement.isCompleted) {
        return `<button class="achievement-claim-btn" onclick="claimAchievement(${achievement.id})">
            <i class="fa fa-gift"></i> 领取奖励
        </button>`;
    } else {
        return `<button class="achievement-claim-btn" disabled>
            <i class="fa fa-hourglass-half"></i> 未完成
        </button>`;
    }
}

/**
 * 领取成就奖励
 */
async function claimAchievement(achievementId) {
    try {
        showLoading(true);
        
        const response = await api.post(`/achievement/${achievementId}/claim`);
        
        if (response.success) {
            showToast('成就奖励领取成功！', 'success');
            
            // 添加领取动画
            const card = document.querySelector(`.achievement-card[data-id="${achievementId}"]`);
            if (card) {
                card.classList.add('achievement-unlock-animation');
            }
            
            // 重新加载数据
            await loadAchievements();
            await loadAchievementStats();
        } else {
            showToast(response.message || '领取失败', 'error');
        }
    } catch (error) {
        console.error('领取成就奖励失败:', error);
        showToast('领取失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * 筛选成就
 */
function filterAchievements(filterType) {
    currentFilter = filterType;
    
    // 更新标签样式
    document.querySelectorAll('.achievement-tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    const activeBtn = document.querySelector(`.achievement-tab-btn[data-type="${filterType}"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }
    
    // 筛选数据
    let filteredAchievements = allAchievements;
    
    if (filterType === 'all') {
        filteredAchievements = allAchievements;
    } else if (filterType === 'completed') {
        filteredAchievements = allAchievements.filter(a => a.isCompleted);
    } else if (filterType === 'unclaimed') {
        filteredAchievements = allAchievements.filter(a => a.isCompleted && !a.isClaimed);
    } else {
        filteredAchievements = allAchievements.filter(a => a.achievementType === filterType);
    }
    
    // 渲染筛选后的列表
    renderAchievements(filteredAchievements);
}

/**
 * 渲染空状态
 */
function renderEmptyState() {
    const achievementList = document.getElementById('achievementList');
    achievementList.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1;">
            <i class="fa fa-trophy" style="font-size: 64px; color: #a0aec0; margin-bottom: 20px;"></i>
            <p style="color: #718096; font-size: 18px; margin-bottom: 10px;">暂无成就数据</p>
            <p style="color: #a0aec0; font-size: 14px;">继续游戏，解锁更多成就！</p>
        </div>
    `;
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
