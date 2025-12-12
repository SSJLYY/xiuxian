// 主游戏逻辑文件

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已经登录
    const token = localStorage.getItem('token');
    if (token) {
        // 如果已登录，显示游戏页面
        document.getElementById('loginPage').style.display = 'none';
        document.getElementById('gamePage').style.display = 'flex';
        loadPlayerData();
        loadAnnouncements();
    } else {
        // 如果未登录，显示登录页面
        document.getElementById('loginPage').style.display = 'flex';
        document.getElementById('gamePage').style.display = 'none';
    }
    
    // 初始化模块系统
    initializeModules();
});

// 初始化模块系统
function initializeModules() {
    // 设置默认显示的模块
    showModule('dashboard');
    
    // 初始化导航栏
    initializeNavigation();
}

// 显示登录表单
function showLogin() {
    document.getElementById('loginForm').style.display = 'block';
    document.getElementById('registerForm').style.display = 'none';
    document.querySelector('.tabs .tab-btn:nth-child(1)').classList.add('active');
    document.querySelector('.tabs .tab-btn:nth-child(2)').classList.remove('active');
}

// 显示注册表单
function showRegister() {
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('registerForm').style.display = 'block';
    document.querySelector('.tabs .tab-btn:nth-child(1)').classList.remove('active');
    document.querySelector('.tabs .tab-btn:nth-child(2)').classList.add('active');
}

// 登录功能
async function login(event) {
    event.preventDefault();
    
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    
    if (!username || !password) {
        showToast('请输入用户名和密码', 'error');
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await api.post('/auth/login', {
            username: username,
            password: password
        });
        
        if (response.success) {
            // 保存token到localStorage
            localStorage.setItem('token', response.data.token);
            
            // 显示游戏页面
            document.getElementById('loginPage').style.display = 'none';
            document.getElementById('gamePage').style.display = 'flex';
            
            // 加载玩家数据
            loadPlayerData();
            loadAnnouncements();
            
            // 初始化导航栏
            initializeNavigation();
            
            showToast('登录成功', 'success');
        } else {
            showToast('登录失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('登录失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 注册功能
async function register(event) {
    event.preventDefault();
    
    const username = document.getElementById('registerUsername').value;
    const nickname = document.getElementById('registerNickname').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    const confirmPassword = document.getElementById('registerConfirmPassword').value;
    
    // 基本验证
    if (!username || !nickname || !email || !password || !confirmPassword) {
        showToast('请填写所有字段', 'error');
        return;
    }
    
    if (password !== confirmPassword) {
        showToast('密码不匹配', 'error');
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await api.post('/auth/register', {
            username: username,
            nickname: nickname,
            email: email,
            password: password
        });
        
        if (response.success) {
            showToast('注册成功，请登录', 'success');
            showLogin();
        } else {
            showToast('注册失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('注册失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// 加载玩家数据
async function loadPlayerData() {
    try {
        const response = await api.get('/player/profile');
        if (response.success) {
            updatePlayerStats(response.data);
        } else {
            showToast('获取玩家数据失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('获取玩家数据失败: ' + error.message, 'error');
    }
}

// 更新玩家状态显示
function updatePlayerStats(profile) {
    document.getElementById('playerName').textContent = profile.nickname;
    document.getElementById('playerLevel').textContent = profile.level;
    document.getElementById('playerRealm').textContent = profile.realm;
    document.getElementById('playerExp').textContent = profile.currentExp;
    document.getElementById('expToNext').textContent = profile.expToNextLevel;
    document.getElementById('playerSpiritStones').textContent = profile.spiritStones;
    document.getElementById('playerHealth').textContent = profile.health;
    document.getElementById('playerMana').textContent = profile.mana;
    document.getElementById('playerAttack').textContent = profile.attack;
    document.getElementById('playerDefense').textContent = profile.defense;
    
    // 更新经验条
    const expPercent = (profile.currentExp / profile.expToNextLevel) * 100;
    document.getElementById('expProgress').style.width = expPercent + '%';
    document.getElementById('expText').textContent = profile.currentExp + '/' + profile.expToNextLevel;
}

// 开始修炼
function startCultivation() {
    document.getElementById('cultivation-btn').style.display = 'none';
    document.getElementById('stop-cultivation-btn').style.display = 'inline-block';
    document.getElementById('cultivationStatus').textContent = '修炼中...';
    
    // 每隔一段时间自动获取经验
    cultivationInterval = setInterval(async () => {
        try {
            const response = await api.post('/player/cultivate');
            if (response.success) {
                const result = response.data;
                updatePlayerStats(result.profile);
                
                // 添加修炼日志
                const logElement = document.getElementById('cultivation-log');
                const logEntry = document.createElement('p');
                logEntry.textContent = result.message;
                logElement.appendChild(logEntry);
                logElement.scrollTop = logElement.scrollHeight;
            } else {
                showToast('修炼失败: ' + response.message, 'error');
            }
        } catch (error) {
            showToast('修炼失败: ' + error.message, 'error');
        }
    }, 5000); // 每5秒修炼一次
}

// 停止修炼
function stopCultivation() {
    clearInterval(cultivationInterval);
    document.getElementById('cultivation-btn').style.display = 'inline-block';
    document.getElementById('stop-cultivation-btn').style.display = 'none';
    document.getElementById('cultivationStatus').textContent = '修炼已停止';
}

// 退出登录
function logout() {
    // 清除token
    localStorage.removeItem('token');
    
    // 显示登录页面
    document.getElementById('loginPage').style.display = 'flex';
    document.getElementById('gamePage').style.display = 'none';
    
    // 清空表单
    document.getElementById('loginUsername').value = '';
    document.getElementById('loginPassword').value = '';
    
    showToast('已退出登录', 'info');
}

// 显示消息提示
function showToast(message, type = 'info') {
    // 使用现代UI的通知系统
    if (window.simpleUI) {
        window.simpleUI.showNotification(message, type);
        return;
    }
    
    // 创建简单的toast通知
    const toast = document.createElement('div');
    toast.className = `toast-notification ${type}`;
    toast.textContent = message;
    
    // 样式
    Object.assign(toast.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        padding: '12px 20px',
        borderRadius: '8px',
        color: 'white',
        fontWeight: '500',
        zIndex: '10000',
        opacity: '0',
        transform: 'translateY(-20px)',
        transition: 'all 0.3s ease'
    });
    
    // 根据类型设置背景色
    const colors = {
        success: '#10b981',
        error: '#ef4444',
        warning: '#f59e0b',
        info: '#3b82f6'
    };
    toast.style.backgroundColor = colors[type] || colors.info;
    
    document.body.appendChild(toast);
    
    // 显示动画
    requestAnimationFrame(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    });
    
    // 3秒后移除
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-20px)';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

// 显示加载动画
function showLoading(show) {
    const loading = document.getElementById('loading');
    if (show) {
        loading.classList.remove('hidden');
    } else {
        loading.classList.add('hidden');
    }
}

// 加载公告
async function loadAnnouncements() {
    try {
        const response = await api.get('/announcement/latest');
        if (response.success && response.data) {
            showAnnouncement(response.data);
        }
    } catch (error) {
        console.error('获取公告失败:', error);
    }
}

// 显示公告
function showAnnouncement(announcement) {
    const banner = document.getElementById('announcementBanner');
    const text = document.getElementById('announcementText');
    
    text.textContent = announcement.content;
    banner.style.display = 'block';
}

// 关闭公告
function closeAnnouncement() {
    document.getElementById('announcementBanner').style.display = 'none';
}

// 模块切换功能
function showModule(moduleName) {
    // 隐藏所有模块
    const modules = document.querySelectorAll('.module');
    modules.forEach(module => {
        module.style.display = 'none';
    });
    
    // 显示选中的模块
    const activeModule = document.getElementById(`${moduleName}-module`);
    if (activeModule) {
        activeModule.style.display = 'grid';
    }
    
    // 更新导航标签状态
    const navTabs = document.querySelectorAll('.nav-tab');
    navTabs.forEach(tab => {
        tab.classList.remove('active');
    });
    
    const activeTab = document.querySelector(`.nav-tab[data-module="${moduleName}"]`);
    if (activeTab) {
        activeTab.classList.add('active');
    }
    
    // 根据模块加载相应数据
    switch(moduleName) {
        case 'dashboard':
            // 仪表盘数据已经在主循环中更新
            break;
        case 'combat':
            if (typeof loadCombatData === 'function') {
                loadCombatData();
            }
            break;
        case 'inventory':
            if (typeof loadInventoryData === 'function') {
                loadInventoryData();
            }
            break;
        case 'quests':
            if (typeof loadQuestsData === 'function') {
                loadQuestsData();
            }
            break;
        case 'skills':
            if (typeof loadSkillsData === 'function') {
                loadSkillsData();
            }
            break;
        case 'shop':
            if (typeof loadShopData === 'function') {
                loadShopData();
            }
            break;
        case 'guild':
            if (typeof loadGuildData === 'function') {
                loadGuildData();
            }
            break;
        case 'ranking':
            if (typeof loadRankingData === 'function') {
                loadRankingData();
            }
            break;
        case 'achievements':
            if (typeof loadAchievementsData === 'function') {
                loadAchievementsData();
            }
            break;
        case 'mail':
            window.location.href = 'mail.html';
            break;
        case 'pets':
            window.location.href = 'pets.html';
            break;
    }
}

// 添加拍卖行、VIP、活动和增强战斗入口到导航栏
function initializeNavigation() {
    // 在现有的导航栏中添加拍卖行链接
    const navTabs = document.querySelector('.nav-tabs');
    if (navTabs) {
        // 检查是否已经添加了拍卖行标签，避免重复添加
        const existingAuctionTab = document.querySelector('.nav-tab[data-module="auction"]');
        if (!existingAuctionTab) {
            const auctionTab = document.createElement('li');
            auctionTab.className = 'nav-tab';
            auctionTab.dataset.module = 'auction';
            auctionTab.innerHTML = `
                <button onclick="navigateToAuction()">
                    <i class="fas fa-gavel"></i> 拍卖行
                </button>
            `;
            navTabs.appendChild(auctionTab);
        }
        
        // 检查是否已经添加了VIP标签，避免重复添加
        const existingVipTab = document.querySelector('.nav-tab[data-module="vip"]');
        if (!existingVipTab) {
            const vipTab = document.createElement('li');
            vipTab.className = 'nav-tab';
            vipTab.dataset.module = 'vip';
            vipTab.innerHTML = `
                <button onclick="navigateToVip()">
                    <i class="fas fa-crown"></i> VIP
                </button>
            `;
            navTabs.appendChild(vipTab);
        }
        
        // 检查是否已经添加了活动标签，避免重复添加
        const existingActivityTab = document.querySelector('.nav-tab[data-module="activity"]');
        if (!existingActivityTab) {
            const activityTab = document.createElement('li');
            activityTab.className = 'nav-tab';
            activityTab.dataset.module = 'activity';
            activityTab.innerHTML = `
                <button onclick="navigateToActivity()">
                    <i class="fas fa-calendar"></i> 活动
                </button>
            `;
            navTabs.appendChild(activityTab);
        }
        
        // 检查是否已经添加了增强战斗标签，避免重复添加
        const existingEnhancedCombatTab = document.querySelector('.nav-tab[data-module="enhanced-combat"]');
        if (!existingEnhancedCombatTab) {
            const combatTab = document.createElement('li');
            combatTab.className = 'nav-tab';
            combatTab.dataset.module = 'enhanced-combat';
            combatTab.innerHTML = `
                <button onclick="navigateToEnhancedCombat()">
                    <i class="fas fa-fist-raised"></i> 增强战斗
                </button>
            `;
            navTabs.appendChild(combatTab);
        }
    }
}

// 导航到拍卖行
function navigateToAuction() {
    window.location.href = 'auction.html';
}

// 导航到VIP页面
function navigateToVip() {
    window.location.href = 'vip.html';
}

// 导航到活动页面
function navigateToActivity() {
    window.location.href = 'activity.html';
}

// 导航到增强战斗页面
function navigateToEnhancedCombat() {
    window.location.href = 'enhanced_combat.html';
}

// 显示礼包码兑换模态框
function showRedeemGiftCodeModal() {
    document.getElementById('giftCodeModal').style.display = 'block';
}

// 关闭礼包码兑换模态框
function closeGiftCodeModal() {
    document.getElementById('giftCodeModal').style.display = 'none';
    document.getElementById('giftCodeInput').value = '';
}

// 兑换礼包码
async function redeemGiftCode() {
    const giftCode = document.getElementById('giftCodeInput').value.trim();
    
    if (!giftCode) {
        showToast('请输入礼包码', 'error');
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await api.post('/giftcode/redeem?code=' + encodeURIComponent(giftCode));
        if (response.success) {
            showToast('礼包码兑换成功！奖励已发放到您的邮箱。', 'success');
            closeGiftCodeModal();
            // 如果在邮件模块，刷新邮件
            if (currentModule === 'mail') {
                loadMails(1);
            }
        } else {
            showToast('兑换失败: ' + response.message, 'error');
        }
    } catch (error) {
        showToast('兑换失败: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}
