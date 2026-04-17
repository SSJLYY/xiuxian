/**
 * VIP模块 - UI渲染层
 */
import { vipService } from './VipService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { modal } from '../../components/Modal.js';
import { escapeHtml } from '../../core/utils/Security.js';

export class VipUI {
    init() {
        this.setupElements();
        this.bindEvents();
        this.loadVipData();
    }

    setupElements() {
        this.elements = {
            currentVipLevel: document.getElementById('currentVipLevel'),
            currentExp: document.getElementById('currentExp'),
            expToNextLevel: document.getElementById('expToNextLevel'),
            expProgressBar: document.getElementById('expProgressBar'),
            vipBenefits: document.getElementById('vipBenefits'),
            dailyRewardBtn: document.getElementById('dailyRewardBtn'),
            rechargeBtn: document.getElementById('rechargeBtn'),
            vipLevelsContainer: document.getElementById('vipLevelsContainer')
        };
    }

    bindEvents() {
        if (this.elements.dailyRewardBtn) {
            this.elements.dailyRewardBtn.addEventListener('click', () => this.handleClaimDailyReward());
        }

        if (this.elements.rechargeBtn) {
            this.elements.rechargeBtn.addEventListener('click', () => this.showRechargeModal());
        }
    }

    async loadVipData() {
        loading.show();
        try {
            await Promise.all([
                vipService.getVipInfo(),
                vipService.getVipLevels(),
                vipService.getVipBenefits()
            ]);

            this.renderVipInfo();
            this.renderVipLevels();
            this.renderVipBenefits();
        } catch (error) {
            toast.error('加载VIP数据失败');
        } finally {
            loading.hide();
        }
    }

    renderVipInfo() {
        const info = vipService.vipInfo;
        if (!info) return;

        // 当前VIP等级
        if (this.elements.currentVipLevel) {
            this.elements.currentVipLevel.textContent = `VIP ${info.level}`;
        }

        // 经验值
        if (this.elements.currentExp) {
            this.elements.currentExp.textContent = info.currentExp || 0;
        }

        // 下一级所需经验
        if (this.elements.expToNextLevel) {
            const nextLevelExp = info.nextLevelExp || 0;
            const currentExp = info.currentExp || 0;
            this.elements.expToNextLevel.textContent = `${currentExp}/${nextLevelExp}`;
        }

        // 经验进度条
        if (this.elements.expProgressBar) {
            const nextLevelExp = info.nextLevelExp || 1;
            const currentExp = info.currentExp || 0;
            const progress = Math.min((currentExp / nextLevelExp) * 100, 100);
            this.elements.expProgressBar.style.width = `${progress}%`;
        }

        // 每日奖励按钮状态
        if (this.elements.dailyRewardBtn) {
            if (info.claimedDailyReward) {
                this.elements.dailyRewardBtn.textContent = '今日已领取';
                this.elements.dailyRewardBtn.disabled = true;
                this.elements.dailyRewardBtn.classList.add('disabled');
            } else {
                this.elements.dailyRewardBtn.textContent = '领取每日奖励';
                this.elements.dailyRewardBtn.disabled = false;
                this.elements.dailyRewardBtn.classList.remove('disabled');
            }
        }
    }

    renderVipLevels() {
        const container = this.elements.vipLevelsContainer;
        if (!container) return;

        const levels = vipService.vipLevels;
        const currentLevel = vipService.vipInfo?.level || 0;

        if (levels.length === 0) {
            container.innerHTML = '<p>暂无VIP等级信息</p>';
            return;
        }

        container.innerHTML = `
            <div class="vip-levels-list">
                ${levels.map(level => {
                    const isCurrent = level.level === currentLevel;
                    const isReached = level.level <= currentLevel;
                    const levelClass = isCurrent ? 'current' : (isReached ? 'reached' : 'locked');

                    return `
                        <div class="vip-level-card ${levelClass}">
                            <div class="level-number">VIP ${escapeHtml(level.level)}</div>
                            <div class="level-exp">所需经验: ${escapeHtml(level.requiredExp)}</div>
                            <div class="level-benefits">
                                ${level.benefits.map(benefit => `<span>${escapeHtml(benefit)}</span>`).join('')}
                            </div>
                            ${isCurrent ? '<div class="current-badge">当前等级</div>' : ''}
                        </div>
                    `;
                }).join('')}
            </div>
        `;
    }

    renderVipBenefits() {
        const container = this.elements.vipBenefits;
        if (!container) return;

        const benefits = vipService.vipBenefits || [];
        if (benefits.length === 0) {
            container.innerHTML = '<p>暂无特权信息</p>';
            return;
        }

        container.innerHTML = `
            <h3>VIP特权</h3>
            <div class="benefits-grid">
                ${benefits.map(benefit => `
                    <div class="benefit-item">
                        <div class="benefit-icon">*</div>
                        <div class="benefit-name">${escapeHtml(benefit.name)}</div>
                        <div class="benefit-desc">${escapeHtml(benefit.description)}</div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    async handleClaimDailyReward() {
        loading.show();
        try {
            await vipService.claimDailyReward();
            await this.loadVipData();
        } catch (error) {
            toast.error('领取失败');
        } finally {
            loading.hide();
        }
    }

    showRechargeModal() {
        const modalHtml = `
            <div class="recharge-options">
                <div class="recharge-option" data-amount="10">
                    <span class="amount">10元</span>
                    <span class="vip-exp">+1000 经验</span>
                </div>
                <div class="recharge-option" data-amount="50">
                    <span class="amount">50元</span>
                    <span class="vip-exp">+5000 经验</span>
                </div>
                <div class="recharge-option" data-amount="100">
                    <span class="amount">100元</span>
                    <span class="vip-exp">+10000 经验</span>
                </div>
                <div class="recharge-option" data-amount="500">
                    <span class="amount">500元</span>
                    <span class="vip-exp">+50000 经验</span>
                </div>
            </div>
        `;

        modal.show({
            title: 'VIP充值',
            content: modalHtml,
            confirmText: '取消',
            showCancel: false
        });

        document.querySelectorAll('.recharge-option').forEach(option => {
            option.addEventListener('click', (e) => {
                const amount = parseInt(e.currentTarget.dataset.amount);
                this.handleRecharge(amount);
                modal.hide();
            });
        });
    }

    async handleRecharge(amount) {
        if (!confirm(`确定要充值 ${amount} 元吗?`)) return;

        loading.show();
        try {
            await vipService.recharge(amount);
            await this.loadVipData();
        } catch (error) {
            toast.error('充值失败');
        } finally {
            loading.hide();
        }
    }
}

export const vipUI = new VipUI();
