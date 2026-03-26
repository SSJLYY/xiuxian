// 活动系统JavaScript逻辑

// 显示标签页
function showTab(tabName) {
    // 隐藏所有标签页内容
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.style.display = 'none';
    });
    
    // 移除所有标签页的激活状态
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 显示选中的标签页
    document.getElementById(`${tabName}-tab`).style.display = 'block';
    
    // 激活选中的标签页按钮
    document.querySelector(`.nav-tab[data-module="${tabName}"]`).classList.add('active');
    
    // 根据标签页加载相应数据
    switch(tabName) {
        case 'current':
            loadActiveActivities();
            break;
        case 'history':
            loadAllActivities();
            break;
        case 'ranking':
            loadActivitiesForRanking();
            break;
    }
}

// 加载当前活动
async function loadActiveActivities() {
    showLoading(true);
    
    try {
        const response = await api.get('/activities/');
        if (response.success) {
            displayActiveActivities(response.data);
        } else {
            showToast('获取活动列表失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取活动列表失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 显示当前活动
function displayActiveActivities(activities) {
    const container = document.getElementById('activeActivitiesContainer');
    if (!container) return;
    
    if (activities.length === 0) {
        container.innerHTML = '<p class="text-center col-span-2 py-8 text-gray-500">暂无进行中的活动</p>';
        return;
    }
    
    container.innerHTML = activities.map(activity => `
        <div class="activity-card">
            <div class="p-4 border-b border-gray-200">
                <h3 class="font-bold text-lg">${escapeHtml(activity.name)}</h3>
                <p class="text-gray-600 text-sm">${escapeHtml(activity.description)}</p>
            </div>
            <div class="p-4">
                <div class="flex justify-between items-center mb-3">
                    <span class="text-gray-600">类型:</span>
                    <span class="font-semibold">${getActivityTypeText(activity.activityType)}</span>
                </div>
                <div class="flex justify-between items-center mb-3">
                    <span class="text-gray-600">时间:</span>
                    <span class="font-semibold">${formatDateTime(activity.startTime)} - ${formatDateTime(activity.endTime)}</span>
                </div>
                <div class="flex justify-between items-center mb-4">
                    <span class="text-gray-600">剩余时间:</span>
                    <span class="font-semibold countdown-timer" id="countdown-${activity.id}">${calculateTimeRemaining(activity.endTime)}</span>
                </div>
                <button class="btn btn-primary w-full" onclick="showActivityDetails(${activity.id})">
                    <i class="fas fa-info-circle"></i> 查看详情
                </button>
            </div>
        </div>
    `).join('');
    
    // 启动倒计时更新
    updateCountdowns(activities);
}

// 加载所有活动
async function loadAllActivities() {
    showLoading(true);
    
    try {
        const response = await api.get('/activities/all');
        if (response.success) {
            displayAllActivities(response.data);
        } else {
            showToast('获取活动列表失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取活动列表失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 显示所有活动
function displayAllActivities(activities) {
    const container = document.getElementById('historyActivitiesContainer');
    if (!container) return;
    
    if (activities.length === 0) {
        container.innerHTML = '<p class="text-center py-8 text-gray-500">暂无活动记录</p>';
        return;
    }
    
    // 按状态分类
    const activeActivities = activities.filter(a => a.status === 'ACTIVE');
    const endedActivities = activities.filter(a => a.status === 'ENDED');
    
    let html = '';
    
    if (activeActivities.length > 0) {
        html += '<h3 class="text-lg font-bold mb-3">进行中</h3>';
        html += activeActivities.map(activity => `
            <div class="activity-history-card border-l-4 border-blue-500">
                <div class="p-4">
                    <div class="flex justify-between items-start">
                        <h4 class="font-bold text-lg">${escapeHtml(activity.name)}</h4>
                        <span class="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs">进行中</span>
                    </div>
                    <p class="text-gray-600 text-sm mt-2">${escapeHtml(activity.description)}</p>
                    <div class="flex justify-between items-center mt-3">
                        <span class="text-gray-600">时间:</span>
                        <span class="font-semibold">${formatDateTime(activity.startTime)} - ${formatDateTime(activity.endTime)}</span>
                    </div>
                </div>
            </div>
        `).join('');
    }
    
    if (endedActivities.length > 0) {
        if (activeActivities.length > 0) {
            html += '<div class="my-4 border-t border-gray-200"></div>';
        }
        html += '<h3 class="text-lg font-bold mb-3">已结束</h3>';
        html += endedActivities.map(activity => `
            <div class="activity-history-card border-l-4 border-gray-500">
                <div class="p-4">
                    <div class="flex justify-between items-start">
                        <h4 class="font-bold text-lg">${escapeHtml(activity.name)}</h4>
                        <span class="px-2 py-1 bg-gray-100 text-gray-800 rounded-full text-xs">已结束</span>
                    </div>
                    <p class="text-gray-600 text-sm mt-2">${escapeHtml(activity.description)}</p>
                    <div class="flex justify-between items-center mt-3">
                        <span class="text-gray-600">时间:</span>
                        <span class="font-semibold">${formatDateTime(activity.startTime)} - ${formatDateTime(activity.endTime)}</span>
                    </div>
                </div>
            </div>
        `).join('');
    }
    
    container.innerHTML = html;
}

// 加载活动用于排行选择
async function loadActivitiesForRanking() {
    showLoading(true);
    
    try {
        const response = await api.get('/activities/all');
        if (response.success) {
            const activities = response.data.filter(a => a.status === 'ENDED');
            displayActivitiesForRanking(activities);
        } else {
            showToast('获取活动列表失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取活动列表失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 显示活动用于排行选择
function displayActivitiesForRanking(activities) {
    const select = document.getElementById('activitySelect');
    if (!select) return;
    
    if (activities.length === 0) {
        select.innerHTML = '<option value="">暂无可排行的活动</option>';
        return;
    }
    
    select.innerHTML = activities.map(activity => `
        <option value="${activity.id}">${escapeHtml(activity.name)}</option>
    `).join('');
    
    // 默认加载第一个活动的排行
    if (activities.length > 0) {
        loadActivityRanking(activities[0].id);
    }
}

// 加载活动排行
async function loadActivityRanking(activityId) {
    if (!activityId) {
        const select = document.getElementById('activitySelect');
        activityId = select.value;
    }
    
    if (!activityId) {
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await api.get(`/activities/${activityId}/ranking?limit=50`);
        if (response.success) {
            displayActivityRanking(response.data);
        } else {
            showToast('获取排行失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取排行失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 显示活动排行
function displayActivityRanking(ranking) {
    const container = document.getElementById('rankingContainer');
    if (!container) return;
    
    if (ranking.length === 0) {
        container.innerHTML = '<p class="text-center py-8 text-gray-500">暂无排行数据</p>';
        return;
    }
    
    container.innerHTML = ranking.map((entry, index) => {
        let rankClass = '';
        let rankIcon = '';
        
        if (index === 0) {
            rankClass = 'bg-yellow-100 border-yellow-500';
            rankIcon = '🥇';
        } else if (index === 1) {
            rankClass = 'bg-gray-100 border-gray-500';
            rankIcon = '🥈';
        } else if (index === 2) {
            rankClass = 'bg-orange-100 border-orange-500';
            rankIcon = '🥉';
        } else {
            rankClass = 'bg-white border-gray-200';
        }
        
        return `
            <div class="flex items-center p-3 border-l-4 ${rankClass}">
                <div class="w-8 text-center font-bold text-lg">${rankIcon || (index + 1)}</div>
                <div class="flex-1 ml-3">
                    <div class="font-medium">玩家${entry.playerId}</div>
                </div>
                <div class="text-right">
                    <div class="font-bold text-lg">${entry.score}</div>
                    <div class="text-sm text-gray-500">积分</div>
                </div>
            </div>
        `;
    }).join('');
}

// 显示活动详情
async function showActivityDetails(activityId) {
    showLoading(true);
    
    try {
        // 这里应该获取具体活动详情，简化处理
        const response = await api.get('/activities/');
        if (response.success) {
            const activities = response.data;
            const activity = activities.find(a => a.id == activityId);
            
            if (activity) {
                displayActivityDetails(activity);
            } else {
                showToast('活动不存在', 'error');
            }
        } else {
            showToast('获取活动详情失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取活动详情失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 显示活动详情模态框
function displayActivityDetails(activity) {
    const modalBody = document.getElementById('activityModalBody');
    if (!modalBody) return;
    
    modalBody.innerHTML = `
        <h2 class="text-2xl font-bold mb-4">${escapeHtml(activity.name)}</h2>
        <div class="mb-4">
            <h3 class="text-lg font-bold mb-2">活动说明</h3>
            <p class="text-gray-700">${escapeHtml(activity.description)}</p>
        </div>
        <div class="mb-4">
            <h3 class="text-lg font-bold mb-2">活动规则</h3>
            <div class="bg-gray-50 p-3 rounded">
                <pre class="whitespace-pre-wrap text-sm">${escapeHtml(activity.rules || '暂无详细规则')}</pre>
            </div>
        </div>
        <div class="mb-4">
            <h3 class="text-lg font-bold mb-2">奖励</h3>
            <div class="bg-gray-50 p-3 rounded">
                <pre class="whitespace-pre-wrap text-sm">${escapeHtml(activity.rewards || '暂无详细奖励')}</pre>
            </div>
        </div>
        <div class="flex justify-between items-center mb-4">
            <div>
                <span class="text-gray-600">类型:</span>
                <span class="font-semibold ml-2">${getActivityTypeText(activity.activityType)}</span>
            </div>
            <div>
                <span class="text-gray-600">剩余时间:</span>
                <span class="font-semibold ml-2 countdown-timer" id="modal-countdown-${activity.id}">${calculateTimeRemaining(activity.endTime)}</span>
            </div>
        </div>
        <div class="mt-6">
            <button class="btn btn-primary w-full" onclick="participateInActivity(${activity.id})">
                <i class="fas fa-flag-checkered"></i> 立即参与
            </button>
        </div>
    `;
    
    document.getElementById('activityModal').style.display = 'block';
    
    // 启动倒计时更新
    updateModalCountdown(activity);
}

// 参与活动
async function participateInActivity(activityId) {
    showLoading(true);
    closeActivityModal();
    
    try {
        const response = await api.post(`/activities/${activityId}/participate`);
        if (response.success) {
            showToast('参与活动成功！', 'success');
        } else {
            showToast('参与活动失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('参与活动失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 关闭活动详情模态框
function closeActivityModal() {
    document.getElementById('activityModal').style.display = 'none';
}

// 更新倒计时
function updateCountdowns(activities) {
    activities.forEach(activity => {
        const element = document.getElementById(`countdown-${activity.id}`);
        if (element) {
            element.textContent = calculateTimeRemaining(activity.endTime);
        }
    });
    
    // 每秒更新一次倒计时
    setTimeout(() => {
        updateCountdowns(activities);
    }, 1000);
}

// 更新模态框倒计时
function updateModalCountdown(activity) {
    const element = document.getElementById(`modal-countdown-${activity.id}`);
    if (element) {
        element.textContent = calculateTimeRemaining(activity.endTime);
        
        // 每秒更新一次倒计时
        setTimeout(() => {
            updateModalCountdown(activity);
        }, 1000);
    }
}

// 计算剩余时间
function calculateTimeRemaining(endTimeStr) {
    const endTime = new Date(endTimeStr);
    const now = new Date();
    const diff = endTime - now;
    
    if (diff <= 0) {
        return '已结束';
    }
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);
    
    if (days > 0) {
        return `${days}天${hours}小时`;
    } else if (hours > 0) {
        return `${hours}小时${minutes}分钟`;
    } else {
        return `${minutes}分${seconds}秒`;
    }
}

// 获取活动类型文本
function getActivityTypeText(type) {
    switch (type) {
        case 'DAILY_LOGIN':
            return '每日登录';
        case 'RECHARGE':
            return '充值活动';
        case 'COMBAT':
            return '战斗活动';
        case 'CULTIVATION':
            return '修炼活动';
        default:
            return type;
    }
}

// 格式化日期时间
function formatDateTime(dateTimeStr) {
    const date = new Date(dateTimeStr);
    return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

// 显示加载动画
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

// 显示消息提示
function showToast(message, type) {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    toastMessage.textContent = message;
    toast.className = 'toast ' + type;
    
    // 3秒后隐藏
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}