// VIP系统JavaScript逻辑

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
        case 'vip-info':
            loadVipInfo();
            break;
        case 'recharge':
            // 不需要额外加载数据
            break;
        case 'privileges':
            loadVipLevels();
            break;
    }
}

// 加载玩家VIP信息
async function loadVipInfo() {
    showLoading(true);
    
    try {
        const response = await api.get('/vip/info');
        if (response.success) {
            displayVipInfo(response.data);
        } else {
            showToast('获取VIP信息失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取VIP信息失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 显示VIP信息
function displayVipInfo(vipInfo) {
    // 更新VIP等级
    document.getElementById('currentVipLevel').textContent = vipInfo.vipLevel || 0;
    document.getElementById('totalRecharge').textContent = vipInfo.totalRecharge || 0;
    document.getElementById('playerYuanbao').textContent = vipInfo.yuanbao || 0;
    
    // 更新VIP卡片样式
    const vipCard = document.getElementById('vipCard');
    vipCard.className = `vip-level-${vipInfo.vipLevel || 0} rounded-xl p-6 text-center shadow-lg`;
    
    // 更新进度条
    const progressText = document.getElementById('rechargeProgressText');
    const progressBar = document.getElementById('rechargeProgressBar');
    
    // 获取所有VIP等级配置以计算进度
    api.get('/vip/levels')
        .then(response => {
            if (response.success) {
                const levels = response.data;
                levels.sort((a, b) => a.level - b.level);
                
                // 找到当前等级和下一等级
                let currentLevel = null;
                let nextLevel = null;
                
                for (let i = 0; i < levels.length; i++) {
                    if (levels[i].level === vipInfo.vipLevel) {
                        currentLevel = levels[i];
                        nextLevel = i < levels.length - 1 ? levels[i + 1] : null;
                        break;
                    }
                }
                
                if (!currentLevel) {
                    currentLevel = levels.find(l => l.level === 0) || levels[0];
                }
                
                // 计算进度
                if (nextLevel) {
                    const current = vipInfo.totalRecharge || 0;
                    const required = nextLevel.requiredRecharge;
                    const progress = Math.min(100, Math.round((current / required) * 100));
                    
                    progressText.textContent = `${current} / ${required}`;
                    progressBar.style.width = `${progress}%`;
                } else {
                    // 已达到最高等级
                    progressText.textContent = '已达到最高等级';
                    progressBar.style.width = '100%';
                }
            }
        })
        .catch(error => {
            console.error('获取VIP等级配置失败:', error);
            // 使用简化的进度计算
            if (vipInfo.vipLevel >= 9) {
                progressText.textContent = '已达到最高等级';
                progressBar.style.width = '100%';
            } else {
                const current = vipInfo.totalRecharge || 0;
                const nextLevel = (vipInfo.vipLevel + 1) * 1000; // 简化：每级需要1000充值
                const percent = Math.min(100, Math.round((current / nextLevel) * 100));
                
                progressText.textContent = `${current} / ${nextLevel}`;
                progressBar.style.width = `${percent}%`;
            }
        });
}

// 加载VIP等级配置
async function loadVipLevels() {
    showLoading(true);
    
    try {
        const response = await api.get('/vip/levels');
        if (response.success) {
            displayVipLevels(response.data);
        } else {
            showToast('获取VIP等级配置失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取VIP等级配置失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 显示VIP等级配置
function displayVipLevels(levels) {
    const container = document.getElementById('vipPrivilegesList');
    if (!container) return;
    
    if (levels.length === 0) {
        container.innerHTML = '<p class="text-center py-8 text-gray-500">暂无VIP等级配置</p>';
        return;
    }
    
    // 按等级排序
    levels.sort((a, b) => a.level - b.level);
    
    container.innerHTML = levels.map(level => `
        <div class="mb-6">
            <h3 class="text-lg font-bold mb-3 flex items-center">
                <i class="fas fa-crown mr-2 ${getVipLevelColor(level.level)}"></i>
                VIP ${level.level}
                ${level.level === 0 ? '(免费)' : `(需充值${level.requiredRecharge}灵石)`}
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-3 pl-4">
                <div class="flex items-start">
                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 flex-shrink-0"></i>
                    <span>每日奖励: ${level.dailySpiritStones || 0} 灵石</span>
                </div>
                <div class="flex items-start">
                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 flex-shrink-0"></i>
                    <span>修炼速度加成: ${(level.cultivationSpeedBonus * 100).toFixed(0)}%</span>
                </div>
                <div class="flex items-start">
                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 flex-shrink-0"></i>
                    <span>经验获取加成: ${(level.expBonus * 100).toFixed(0)}%</span>
                </div>
                <div class="flex items-start">
                    <i class="fas fa-check-circle text-green-500 mt-1 mr-2 flex-shrink-0"></i>
                    <span>商城折扣: ${(level.shopDiscount * 100).toFixed(0)}%</span>
                </div>
            </div>
        </div>
    `).join('');
}

// 获取VIP等级颜色类
function getVipLevelColor(level) {
    switch (level) {
        case 0: return 'text-gray-400';
        case 1: return 'text-bronze';
        case 2: return 'text-silver';
        case 3: return 'text-gold';
        default: return 'text-purple-500';
    }
}

// 领取每日奖励
async function claimDailyReward() {
    showLoading(true);
    
    try {
        const response = await api.post('/vip/daily-reward');
        if (response.success) {
            if (response.data.success) {
                showToast('领取成功！奖励已通过邮件发送给您', 'success');
                // 更新按钮状态
                const btn = document.getElementById('dailyRewardBtn');
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-check"></i> 今日已领取';
                document.getElementById('dailyRewardStatus').textContent = '今日奖励已领取，明日再来';
            } else {
                showToast('今日已领取过奖励', 'info');
                // 更新按钮状态
                const btn = document.getElementById('dailyRewardBtn');
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-check"></i> 今日已领取';
                document.getElementById('dailyRewardStatus').textContent = '今日奖励已领取，明日再来';
            }
            // 重新加载VIP信息
            loadVipInfo();
        } else {
            showToast('领取失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('领取失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 充值
async function recharge(amount) {
    if (!confirm(`确定要充值${amount / 100}元吗？`)) {
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await api.post(`/vip/recharge/${amount}`);
        if (response.success) {
            showToast('充值成功！', 'success');
            // 重新加载VIP信息
            loadVipInfo();
            // 切换到VIP信息标签页
            showTab('vip-info');
        } else {
            showToast('充值失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('充值失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 检查VIP特权
async function checkVipPrivilege(requiredLevel) {
    try {
        const response = await api.get(`/vip/privilege/${requiredLevel}`);
        if (response.success) {
            return response.data.hasPrivilege;
        }
    } catch (error) {
        console.error('检查VIP特权失败:', error);
    }
    return false;
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