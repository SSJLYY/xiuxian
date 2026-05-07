/**
 * 礼包码模块 - UI 渲染层
 */
import { giftcodeService } from './GiftcodeService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';

export class GiftcodeUI {
    init() {
        this.setupElements();
        this.bindEvents();
        this.loadGiftcodeData();
    }

    setupElements() {
        this.elements = {
            redeemForm: document.getElementById('redeemForm'),
            codeInput: document.getElementById('codeInput'),
            redeemBtn: document.getElementById('redeemBtn'),
            myCodesContainer: document.getElementById('myCodesContainer'),
            availableCodesContainer: document.getElementById('availableCodesContainer')
        };
    }

    bindEvents() {
        if (this.elements.redeemForm) {
            this.elements.redeemForm.addEventListener('submit', e => {
                e.preventDefault();
                this.handleRedeem();
            });
        }
    }

    async loadGiftcodeData() {
        loading.show();
        try {
            await Promise.all([
                giftcodeService.getMyCodes(),
                giftcodeService.getAvailableCodes()
            ]);
            this.renderMyCodes();
            this.renderAvailableCodes();
        } catch {
            toast.error('加载礼包码数据失败');
        } finally {
            loading.hide();
        }
    }

    renderMyCodes() {
        const container = this.elements.myCodesContainer;
        if (!container) {
            return;
        }

        if (giftcodeService.myCodes.length === 0) {
            container.innerHTML = '<p>暂无兑换记录</p>';
            return;
        }

        container.innerHTML = `
            <div class="my-codes-list">
                ${giftcodeService.myCodes.map(record => `
                    <div class="code-record">
                        <div class="code-info">
                            <div class="code">兑换码: ${record.codeMasked}</div>
                            <div class="time">兑换时间: ${new Date(record.redeemedAt).toLocaleString()}</div>
                        </div>
                        <div class="code-rewards">
                            <div class="reward-label">奖励:</div>
                            ${(record.rewards || []).map(reward => `
                                <span class="reward-item">${reward.description}</span>
                            `).join('')}
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    renderAvailableCodes() {
        const container = this.elements.availableCodesContainer;
        if (!container) {
            return;
        }

        const availableCodes = giftcodeService.availableCodes || [];
        if (availableCodes.length === 0) {
            container.innerHTML = '<p>暂无可用礼包码</p>';
            return;
        }

        container.innerHTML = `
            <div class="available-codes-list">
                ${availableCodes.map(code => `
                    <div class="available-code">
                        <div class="code-title">${code.title}</div>
                        <div class="code-desc">${code.description}</div>
                        <div class="code-rewards">
                            <span>奖励: ${code.rewardDescription}</span>
                        </div>
                        ${code.expiryDate ? `
                            <div class="code-expiry">
                                <span>有效期至: ${new Date(code.expiryDate).toLocaleString()}</span>
                            </div>
                        ` : ''}
                    </div>
                `).join('')}
            </div>
        `;
    }

    async handleRedeem() {
        const code = this.elements.codeInput.value.trim();
        if (!code) {
            toast.error('请输入兑换码');
            return;
        }

        loading.show();
        try {
            await giftcodeService.redeemCode(code);
            this.elements.codeInput.value = '';
            await this.loadGiftcodeData();
        } catch {
            toast.error('兑换失败');
        } finally {
            loading.hide();
        }
    }
}

export const giftcodeUI = new GiftcodeUI();
