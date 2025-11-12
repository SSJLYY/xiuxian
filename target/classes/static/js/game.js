// 游戏管理器 - 修复版，完全使用真实API，禁止降级
class GameManager {
    constructor() {
        this.isCultivating = false;
        this.cultivationTimer = null;
        this.cultivationTime = 0;
        this.lastCultivationStart = null;
        this.isInitialized = false;
        this.dataRefreshTimer = null;
    }

    // 初始化 - 只在认证后调用
    async init() {
        if (this.isInitialized) return;
        
        this.bindEvents();
        await this.initCultivationStatus();
        this.isInitialized = true;
    }

    // 初始化修炼状态 - 页面刷新时自动停止修炼
    async initCultivationStatus() {
        try {
            const profileResponse = await gameAPI.getCurrentPlayerProfile();
            console.log('初始化修炼状态响应:', profileResponse);
            
            if (!profileResponse.success) {
                throw new Error('获取玩家资料失败: ' + profileResponse.message);
            }
            
            const profile = profileResponse.data;
            console.log('初始化修炼状态数据:', profile);
            this.isCultivating = profile.isCultivating || false;
            
            console.log('后端修炼状态:', this.isCultivating);
            
            if (this.isCultivating) {
                // 页面刷新时自动停止修炼
                console.log('检测到玩家正在修炼中，自动停止修炼');
                try {
                    await this.stopCultivation();
                    console.log('自动停止修炼成功');
                } catch (stopError) {
                    console.error('自动停止修炼失败:', stopError);
                    
                    // 如果停止失败，尝试重置后端修炼状态
                    try {
                        console.log('尝试重置后端修炼状态');
                        await gameAPI.resetCultivation();
                        console.log('后端修炼状态重置成功');
                    } catch (resetError) {
                        console.error('重置后端修炼状态失败:', resetError);
                    }
                    
                    // 强制重置前端状态
                    this.isCultivating = false;
                    this.stopCultivationTimer();
                    this.cultivationTime = 0;
                    this.updateCultivationStatus('点击开始修炼');
                    
                    // 强制更新按钮状态
                    const button = document.getElementById('cultivation-btn');
                    if (button) {
                        button.innerHTML = '<i class="fas fa-play"></i> 开始修炼';
                        button.className = 'btn btn-success';
                        button.onclick = () => this.startCultivation();
                    }
                    
                    // 显示状态重置提示
                    this.showToast('修炼状态已重置，可以重新开始修炼', 'info');
                }
            } else {
                this.updateCultivationStatus('点击开始修炼');
            }
        } catch (error) {
            console.error('初始化修炼状态失败:', error);
            this.isCultivating = false;
            this.updateCultivationStatus('点击开始修炼');
        }
    }

    // 绑定事件
    bindEvents() {
        const cultivationBtn = document.getElementById('cultivation-btn');
        if (cultivationBtn) {
            cultivationBtn.addEventListener('click', () => this.toggleCultivation());
        }
    }

    // 开始修炼 - 完全使用真实API
    async startCultivation() {
        try {
            // 先检查当前修炼状态
            const profileResponse = await gameAPI.getCurrentPlayerProfile();
            console.log('开始修炼前检查后端状态响应:', profileResponse);
            
            if (!profileResponse.success) {
                throw new Error('获取玩家资料失败: ' + profileResponse.message);
            }
            
            const profile = profileResponse.data;
            console.log('开始修炼前检查后端状态数据:', profile);
            console.log('开始修炼前检查后端状态:', profile.isCultivating);
            
            if (profile.isCultivating) {
                // 如果后端显示正在修炼，但前端状态不一致，强制同步
                console.log('后端显示正在修炼中，强制同步前端状态');
                this.isCultivating = true;
                this.startCultivationTimer();
                this.updateCultivationStatus('修炼中...');
                throw new Error('已经在修炼中');
            }

            const response = await gameAPI.startCultivation();

            if (!response.success) {
                throw new Error(response.message || '开始修炼失败');
            }

            this.isCultivating = true;
            this.cultivationTime = 0;
            this.lastCultivationStart = Date.now();

            this.startCultivationTimer();
            this.updateCultivationStatus('修炼中...');

            this.showToast('开始修炼成功', 'success');

        } catch (error) {
            console.error('开始修炼失败:', error);
            this.showToast('开始修炼失败: ' + error.message, 'error');
            throw error; // 禁止降级，直接报错
        }
    }

    // 停止修炼 - 完全使用真实API
    async stopCultivation() {
        try {
            // 先检查当前修炼状态
            const profileResponse = await gameAPI.getCurrentPlayerProfile();
            
            if (!profileResponse.success) {
                throw new Error('获取玩家资料失败: ' + profileResponse.message);
            }
            
            const profile = profileResponse.data;
            
            if (!profile.isCultivating) {
                this.isCultivating = false;
                this.stopCultivationTimer();
                this.cultivationTime = 0;
                this.updateCultivationStatus('点击开始修炼');
                console.log('玩家当前没有在修炼，直接更新状态');
                return;
            }

            const response = await gameAPI.stopCultivation();

            if (!response.success) {
                throw new Error(response.message || '停止修炼失败');
            }

            this.isCultivating = false;
            this.stopCultivationTimer();
            this.cultivationTime = 0;
            this.updateCultivationStatus('点击开始修炼');

            // 停止修炼后刷新玩家数据，显示经验变化
            if (window.authManager && window.authManager.loadPlayerProfile) {
                await window.authManager.loadPlayerProfile();
            }

            this.showToast('停止修炼成功', 'info');

        } catch (error) {
            console.error('停止修炼失败:', error);
            // 即使后端停止失败，也要强制更新前端状态
            this.isCultivating = false;
            this.stopCultivationTimer();
            this.cultivationTime = 0;
            this.updateCultivationStatus('点击开始修炼');
            
            this.showToast('停止修炼失败: ' + error.message, 'error');
            throw error; // 禁止降级，直接报错
        }
    }

    // 切换修炼状态
    async toggleCultivation() {
        if (this.isCultivating) {
            await this.stopCultivation();
        } else {
            await this.startCultivation();
        }
    }

    // 开始修炼计时器
    startCultivationTimer() {
        this.cultivationTimer = setInterval(() => {
            this.cultivationTime++;
            this.updateCultivationDisplay();
            
            // 每30秒刷新一次玩家数据，显示经验变化
            if (this.cultivationTime % 30 === 0) {
                this.refreshPlayerData();
            }
        }, 1000);
    }

    // 停止修炼计时器
    stopCultivationTimer() {
        if (this.cultivationTimer) {
            clearInterval(this.cultivationTimer);
            this.cultivationTimer = null;
        }
        
        // 停止数据刷新计时器
        if (this.dataRefreshTimer) {
            clearInterval(this.dataRefreshTimer);
            this.dataRefreshTimer = null;
        }
    }

    // 刷新玩家数据
    async refreshPlayerData() {
        try {
            if (window.authManager && window.authManager.loadPlayerProfile) {
                await window.authManager.loadPlayerProfile();
                console.log('玩家数据已刷新');
            }
        } catch (error) {
            console.error('刷新玩家数据失败:', error);
        }
    }

    // 更新修炼显示
    updateCultivationDisplay() {
        const timeElement = document.getElementById('cultivationTime');
        if (timeElement) {
            timeElement.textContent = this.cultivationTime;
        }

        const statusElement = document.getElementById('cultivationStatus');
        if (statusElement) {
            statusElement.textContent = `修炼中... ${this.cultivationTime}秒`;
        }

        // 每10秒添加一次日志
        if (this.cultivationTime % 10 === 0) {
            this.addCultivationLog(`修炼进行中... ${this.cultivationTime}秒`);
        }
    }

    // 更新修炼状态
    updateCultivationStatus(status) {
        const statusElement = document.getElementById('cultivationStatus');
        const button = document.getElementById('cultivation-btn');
        const timeElement = document.getElementById('cultivationTime');

        if (statusElement) {
            statusElement.textContent = status;
        }

        if (timeElement) {
            if (this.isCultivating) {
                timeElement.textContent = this.cultivationTime;
            } else {
                timeElement.textContent = '0';
            }
        }

        if (button) {
            if (this.isCultivating) {
                button.innerHTML = '<i class="fas fa-stop"></i> 停止修炼';
                button.className = 'btn btn-danger';
                button.onclick = () => this.stopCultivation();
            } else {
                button.innerHTML = '<i class="fas fa-play"></i> 开始修炼';
                button.className = 'btn btn-success';
                button.onclick = () => this.startCultivation();
            }
        }
    }

    // 添加修炼日志
    addCultivationLog(message) {
        const logElement = document.getElementById('cultivation-log');
        if (logElement) {
            const logEntry = document.createElement('p');
            logEntry.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;
            logElement.appendChild(logEntry);
            logElement.scrollTop = logElement.scrollHeight;

            // 限制日志数量
            if (logElement.children.length > 50) {
                logElement.removeChild(logElement.firstChild);
            }
        }
    }

    // 领取离线奖励 - 使用后端存在的API
    async claimOfflineRewards() {
        try {
            const response = await gameAPI.claimOfflineRewards();

            if (!response.success) {
                throw new Error(response.message || '领取离线奖励失败');
            }

            this.showToast('离线奖励领取成功', 'success');

            // 刷新玩家数据
            if (window.authManager) {
                await window.authManager.loadPlayerProfile();
            }

        } catch (error) {
            console.error('领取离线奖励失败:', error);
            this.showToast('领取离线奖励失败: ' + error.message, 'error');
            throw error; // 禁止降级，直接报错
        }
    }

    // 显示消息提示
    showToast(message, type = 'info', duration = 3000) {
        if (window.authManager && window.authManager.showToast) {
            window.authManager.showToast(message, type, duration);
        } else {
            const toast = document.createElement('div');
            toast.className = `toast ${type}`;
            toast.textContent = message;

            Object.assign(toast.style, {
                position: 'fixed',
                top: '20px',
                right: '20px',
                background: this.getToastColor(type),
                color: 'white',
                padding: '15px 20px',
                borderRadius: '8px',
                boxShadow: '0 5px 15px rgba(0,0,0,0.2)',
                zIndex: '1001',
                animation: 'slideInRight 0.3s ease',
                maxWidth: '300px',
                wordWrap: 'break-word'
            });

            document.body.appendChild(toast);

            setTimeout(() => {
                toast.style.animation = 'slideOutRight 0.3s ease';
                setTimeout(() => {
                    if (toast.parentElement) {
                        toast.parentElement.removeChild(toast);
                    }
                }, 300);
            }, duration);
        }
    }

    // 获取toast颜色
    getToastColor(type) {
        const colors = {
            info: '#3498db',
            success: '#2ecc71',
            warning: '#f39c12',
            error: '#e74c3c'
        };
        return colors[type] || colors.info;
    }
}

// 游戏管理器实例（延迟初始化）
let gameManager = null;

// 初始化游戏管理器（在认证成功后调用）
window.initGameManager = async function() {
    if (!gameManager) {
        gameManager = new GameManager();
        await gameManager.init();
        
        // 设置全局函数
        window.startCultivation = () => gameManager.startCultivation();
        window.stopCultivation = () => gameManager.stopCultivation();
        window.toggleCultivation = () => gameManager.toggleCultivation();
        window.claimOfflineRewards = () => gameManager.claimOfflineRewards();
        window.resetCultivation = () => gameManager.resetCultivation();
        
        // 导出到全局作用域
        window.gameManager = gameManager;
    }
    return gameManager;
};

// 获取游戏管理器实例
window.getGameManager = function() {
    return gameManager;
};