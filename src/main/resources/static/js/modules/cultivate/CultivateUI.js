import { cultivateService } from './CultivateService.js';

export class CultivateUI {
    constructor() {
        this.isCultivating = false;
        this.cultivationTime = 0;
        this.cultivationTimer = null;
        this.dataRefreshTimer = null;
        this.cycleStart = 0;
        this.cycleCount = 0;
        this.cycleDurationMs = 10000;
    }

    async init() {
        await this.syncFromProfile();
        return this;
    }

    async syncFromProfile() {
        const profile = await cultivateService.getProfile();
        this.isCultivating = !!profile.isCultivating;
        if (this.isCultivating && !this.cultivationTimer) {
            this.startCultivationTimer();
        }
        if (!this.isCultivating) {
            this.stopCultivationTimer();
        }
        this.updateCultivationStatus(this.isCultivating ? '修炼中...' : '点击开始修炼');
        return profile;
    }

    async startCultivation() {
        const result = await cultivateService.startCultivation();
        if (result.alreadyCultivating) {
            this.isCultivating = true;
            this.startCultivationTimer();
            this.updateCultivationStatus('修炼中...');
            throw new Error('已经在修炼中');
        }
        this.isCultivating = true;
        this.cultivationTime = 0;
        this.cycleCount = 0;
        this.cycleStart = Date.now();
        this.startCultivationTimer();
        this.updateCultivationStatus('修炼中...');
        this.showToast('开始修炼成功', 'success');
    }

    async stopCultivation() {
        const result = await cultivateService.stopCultivation();
        this.isCultivating = false;
        this.stopCultivationTimer();
        this.cultivationTime = 0;
        this.updateCultivationStatus('点击开始修炼');
        if (window.authManager?.loadPlayerProfile) {
            await window.authManager.loadPlayerProfile();
        }
        if (!result.alreadyStopped) {
            this.showToast(
                cultivateService.formatOutcomeToast(
                    '收功完成',
                    `经验+${result.expGained || 0} 灵石+${result.spiritStonesGained || 0}`,
                    result.levelUps > 0 ? `连升${result.levelUps}级` : ''
                ),
                'success'
            );
        }
    }

    async claimOfflineRewards() {
        const result = await cultivateService.claimOfflineRewards();
        if (!result?.hasReward) {
            this.showToast(result?.message || '当前没有可领取的离线奖励', 'info');
            return;
        }
        if (window.authManager?.loadPlayerProfile) {
            await window.authManager.loadPlayerProfile();
        }
        this.showToast(
            cultivateService.formatOutcomeToast(
                '离线奖励到账',
                `经验+${result.expGained || 0} 灵石+${result.spiritStonesGained || 0}`,
                result.claimResult?.leveledUp ? `等级 ${result.claimResult.oldLevel}→${result.claimResult.newLevel}` : ''
            ),
            'success'
        );
    }

    async resetCultivation() {
        await cultivateService.resetCultivation();
        this.isCultivating = false;
        this.stopCultivationTimer();
        this.cultivationTime = 0;
        this.updateCultivationStatus('点击开始修炼');
        if (window.authManager?.loadPlayerProfile) {
            await window.authManager.loadPlayerProfile();
        }
        this.showToast('修炼状态已重置', 'info');
    }

    toggleCultivation() {
        return this.isCultivating ? this.stopCultivation() : this.startCultivation();
    }

    startCultivationTimer() {
        this.stopCultivationTimer();
        this.cycleStart = Date.now();
        this.cultivationTimer = setInterval(() => {
            const elapsed = Date.now() - this.cycleStart;
            const percent = Math.min(100, Math.floor((elapsed / this.cycleDurationMs) * 100));
            this.updateCycleProgress(percent);
            if (elapsed >= this.cycleDurationMs) {
                this.cycleStart = Date.now();
                this.cycleCount++;
                this.updateCultivationDisplay();
                this.refreshPlayerData();
            }
        }, 100);
    }

    stopCultivationTimer() {
        if (this.cultivationTimer) clearInterval(this.cultivationTimer);
        if (this.dataRefreshTimer) clearInterval(this.dataRefreshTimer);
        this.cultivationTimer = null;
        this.dataRefreshTimer = null;
    }

    async refreshPlayerData() {
        try {
            if (window.authManager?.loadPlayerProfile) {
                await window.authManager.loadPlayerProfile();
            }
        } catch (error) {
            console.error('刷新玩家数据失败:', error);
        }
    }

    updateCultivationDisplay() {
        const statusElement = document.getElementById('cultivationStatus');
        if (statusElement) {
            statusElement.textContent = `修炼中... 小周期 ${this.cycleCount} 次`;
        }
    }

    updateCycleProgress(percent) {
        const bar = document.getElementById('expProgress');
        if (bar) bar.style.width = `${percent}%`;
    }

    updateCultivationStatus(status) {
        const statusElement = document.getElementById('cultivationStatus');
        const button = document.getElementById('cultivation-btn');
        const timeElement = document.getElementById('cultivationTime');
        const stopBtn = document.getElementById('stop-cultivation-btn');

        if (statusElement) statusElement.textContent = status;
        if (timeElement) timeElement.textContent = this.isCultivating ? this.cultivationTime : '0';

        if (button) {
            button.style.display = this.isCultivating ? 'none' : '';
            button.onclick = () => this.startCultivation();
        }
        if (stopBtn) {
            stopBtn.style.display = this.isCultivating ? '' : 'none';
            stopBtn.onclick = () => this.stopCultivation();
        }
    }

    addCultivationLog(message) {
        this.showToast(message, 'info');
    }

    showToast(message, type = 'info', duration = 3000) {
        if (window.authManager?.showToast) {
            window.authManager.showToast(message, type, duration);
        } else {
            console.log(`[${type}] ${message}`);
        }
    }
}

export const cultivateUI = new CultivateUI();
