// 游戏逻辑管理
class GameManager {
    constructor() {
        this.isCultivating = false;
        this.cultivationTimer = null;
        this.cultivationTime = 0;
        this.currentShopType = 'general';
        this.init();
    }

    // 初始化
    init() {
        this.bindEvents();
        // 只在游戏页面显示时才加载商店数据
        if (authManager && authManager.isAuthenticated) {
            this.loadShopData();
        }
    }

    // 绑定事件
    bindEvents() {
        // 登录表单提交
        document.getElementById('loginForm').addEventListener('submit', login);
        
        // 注册表单提交
        document.getElementById('registerForm').addEventListener('submit', register);
    }

    // 开始修炼
    async startCultivation() {
        if (this.isCultivating) {
            showToast('正在修炼中...', 'warning');
            return;
        }

        try {
            const response = await gameAPI.startCultivation();
            if (response.success) {
                this.isCultivating = true;
                this.cultivationTime = 0;
                this.startCultivationTimer();
                showToast('开始修炼', 'success');
            }
        } catch (error) {
            showToast('修炼失败: ' + error.message, 'error');
        }
    }

    // 开始修炼计时器
    startCultivationTimer() {
        const statusElement = document.getElementById('cultivationStatus');
        const timeElement = document.getElementById('cultivationTime');
        
        if (statusElement) {
            statusElement.style.display = 'block';
        }
        
        this.cultivationTimer = setInterval(() => {
            this.cultivationTime++;
            if (timeElement) {
                timeElement.textContent = this.cultivationTime;
            }
            
            // 每30秒尝试获取一次修炼结果
            if (this.cultivationTime % 30 === 0) {
                this.checkCultivationResult();
            }
        }, 1000);
    }

    // 检查修炼结果
    async checkCultivationResult() {
        try {
            const response = await gameAPI.getPlayerProfile();
            if (response.success) {
                authManager.player = response.data;
                authManager.updatePlayerUI();
                
                // 检查是否升级
                if (response.data.level > authManager.player.level) {
                    showToast(`恭喜升级到 ${response.data.level} 级！`, 'success');
                }
            }
        } catch (error) {
            console.error('Failed to check cultivation result:', error);
        }
    }

    // 停止修炼
    stopCultivation() {
        if (this.cultivationTimer) {
            clearInterval(this.cultivationTimer);
            this.cultivationTimer = null;
        }
        
        this.isCultivating = false;
        this.cultivationTime = 0;
        
        const statusElement = document.getElementById('cultivationStatus');
        if (statusElement) {
            statusElement.style.display = 'none';
        }
        
        showToast('修炼结束', 'info');
    }

    // 领取离线奖励
    async claimOfflineReward() {
        try {
            const response = await gameAPI.claimOfflineRewards();
            if (response.success) {
                const { offline_duration, exp_gained, spirit_stones_gained } = response.data;
                
                if (offline_duration > 0) {
                    const hours = Math.floor(offline_duration / 3600);
                    const minutes = Math.floor((offline_duration % 3600) / 60);
                    
                    showToast(
                        `离线 ${hours}小时${minutes}分钟\n获得经验: ${exp_gained}\n获得灵石: ${spirit_stones_gained}`,
                        'success'
                    );
                    
                    // 刷新玩家数据
                    await authManager.refreshPlayer();
                } else {
                    showToast('暂无离线奖励可领取', 'info');
                }
            }
        } catch (error) {
            showToast('领取离线奖励失败: ' + error.message, 'error');
        }
    }

    // 加载商店数据
    async loadShopData() {
        try {
            const response = await gameAPI.getShopItems(this.currentShopType);
            if (response.success) {
                this.renderShopItems(response.data);
            }
        } catch (error) {
            console.error('Failed to load shop data:', error);
        }
    }

    // 渲染商店物品
    renderShopItems(items) {
        const shopItems = document.getElementById('shopItems');
        shopItems.innerHTML = '';

        items.forEach(item => {
            const itemElement = document.createElement('div');
            itemElement.className = 'shop-item';
            
            const itemName = item.name || (item.equipment ? item.equipment.name : '未知物品');
            const itemPrice = item.price;
            const itemStock = item.stock;
            
            itemElement.innerHTML = `
                <div class="shop-item-name">${itemName}</div>
                <div class="shop-item-price">${itemPrice} 灵石</div>
                <div class="shop-item-stock">库存: ${itemStock}</div>
                <button class="btn btn-primary" onclick="buyItem(${item.id})" ${itemStock <= 0 ? 'disabled' : ''}>
                    ${itemStock <= 0 ? '售罄' : '购买'}
                </button>
            `;
            
            shopItems.appendChild(itemElement);
        });
    }

    // 购买物品
    async buyItem(shopItemId) {
        try {
            const response = await gameAPI.buyItem(shopItemId);
            if (response.success) {
                showToast('购买成功', 'success');
                await authManager.refreshPlayer();
                this.loadShopData(); // 刷新商店数据
            }
        } catch (error) {
            showToast('购买失败: ' + error.message, 'error');
        }
    }

    // 使用物品
    async useItem(itemId, slotIndex) {
        try {
            const response = await gameAPI.useItem(itemId);
            if (response.success) {
                showToast('使用物品成功', 'success');
                await authManager.loadInventory();
            }
        } catch (error) {
            showToast('使用物品失败: ' + error.message, 'error');
        }
    }

    // 装备物品
    async equipItem(equipmentId) {
        try {
            const response = await gameAPI.equipItem(equipmentId);
            if (response.success) {
                showToast('装备成功', 'success');
                await authManager.loadEquipment();
                await authManager.refreshPlayer();
            }
        } catch (error) {
            showToast('装备失败: ' + error.message, 'error');
        }
    }

    // 卸下装备
    async unequipItem(slotType) {
        try {
            const response = await gameAPI.unequipItem(slotType);
            if (response.success) {
                showToast('卸下装备成功', 'success');
                await authManager.loadEquipment();
                await authManager.refreshPlayer();
            }
        } catch (error) {
            showToast('卸下装备失败: ' + error.message, 'error');
        }
    }

    // 学习技能
    async learnSkill(skillId) {
        try {
            const response = await gameAPI.learnSkill(skillId);
            if (response.success) {
                showToast('学习技能成功', 'success');
                await authManager.loadSkills();
                await authManager.refreshPlayer();
            }
        } catch (error) {
            showToast('学习技能失败: ' + error.message, 'error');
        }
    }

    // 升级技能
    async upgradeSkill(skillId) {
        try {
            const response = await gameAPI.upgradeSkill(skillId);
            if (response.success) {
                showToast('升级技能成功', 'success');
                await authManager.loadSkills();
                await authManager.refreshPlayer();
            }
        } catch (error) {
            showToast('升级技能失败: ' + error.message, 'error');
        }
    }

    // 领取任务奖励
    async claimQuestReward(questId) {
        try {
            const response = await gameAPI.claimQuestReward(questId);
            if (response.success) {
                showToast('领取奖励成功', 'success');
                await authManager.loadQuests();
                await authManager.refreshPlayer();
            }
        } catch (error) {
            showToast('领取奖励失败: ' + error.message, 'error');
        }
    }
}

// 创建游戏管理器实例
const gameManager = new GameManager();

// 显示面板
function showPanel(panelName) {
    // 隐藏所有面板
    document.querySelectorAll('.panel').forEach(panel => {
        panel.classList.remove('active');
    });
    
    // 显示选中的面板
    const targetPanel = document.getElementById(panelName + 'Panel');
    if (targetPanel) {
        targetPanel.classList.add('active');
    }
    
    // 更新底部导航状态
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    const activeBtn = document.querySelector(`[onclick="showPanel('${panelName}')"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }
}

// 显示商店
function showShop(shopType) {
    gameManager.currentShopType = shopType;
    
    // 更新标签页状态
    document.querySelectorAll('.shop-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    const activeTab = document.querySelector(`[onclick="showShop('${shopType}')"]`);
    if (activeTab) {
        activeTab.classList.add('active');
    }
    
    // 加载商店数据
    gameManager.loadShopData();
}

// 购买物品
function buyItem(shopItemId) {
    gameManager.buyItem(shopItemId);
}

// 开始修炼
function startCultivation() {
    gameManager.startCultivation();
}

// 领取离线奖励
function claimOfflineReward() {
    gameManager.claimOfflineReward();
}