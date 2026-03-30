/**
 * 修炼模块 - UI渲染层
 */
import { cultivateService } from './CultivateService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { formatUtils } from '../../core/utils/FormatUtils.js';
import { modal } from '../../components/Modal.js';

export class CultivateUI {
    constructor() {
        this.cultivateTimer = null;
        this.refreshInterval = 1000;
    }

    init() {
        this.setupElements();
        this.bindEvents();
        this.loadCultivateData();
        this.startAutoRefresh();
    }

    setupElements() {
        this.elements = {
            currentRealm: document.getElementById('currentRealm'),
            currentLevel: document.getElementById('currentLevel'),
            currentExp: document.getElementById('currentExp'),
            expToNextLevel: document.getElementById('expToNextLevel'),
            expProgressBar: document.getElementById('expProgressBar'),
            cultivateSpeed: document.getElementById('cultivateSpeed'),
            spiritStonesPerHour: document.getElementById('spiritStonesPerHour'),
            startCultivateBtn: document.getElementById('startCultivateBtn'),
            stopCultivateBtn: document.getElementById('stopCultivateBtn'),
            breakthroughBtn: document.getElementById('breakthroughBtn'),
            cultivateStatus: document.getElementById('cultivateStatus'),
            cultivatedTime: document.getElementById('cultivatedTime')
        };
    }

    bindEvents() {
        if (this.elements.startCultivateBtn) {
            this.elements.startCultivateBtn.addEventListener('click', () => this.showCultivateTypeDialog());
        }

        if (this.elements.stopCultivateBtn) {
            this.elements.stopCultivateBtn.addEventListener('click', () => this.handleStopCultivate());
        }

        if (this.elements.breakthroughBtn) {
            this.elements.breakthroughBtn.addEventListener('click', () => this.handleBreakthrough());
        }
    }

    async loadCultivateData() {
        try {
            await cultivateService.getCultivateInfo();
            this.renderCultivateInfo();
        } catch (error) {
            console.error('加载修炼数据失败:', error);
        }
    }

    renderCultivateInfo() {
        const info = cultivateService.cultivateInfo;
        if (!info) return;

        // 当前境界
        if (this.elements.currentRealm) {
            this.elements.currentRealm.textContent = info.realm || '练气';
        }

        // 当前等级
        if (this.elements.currentLevel) {
            this.elements.currentLevel.textContent = `${info.level}层`;
        }

        // 当前经验
        if (this.elements.currentExp) {
            this.elements.currentExp.textContent = info.currentExp || 0;
        }

        // 下一级所需经验
        if (this.elements.expToNextLevel) {
            const nextLevelExp = info.nextLevelExp || 1;
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

        // 修炼速度
        if (this.elements.cultivateSpeed) {
            const speed = cultivateService.getCultivationSpeed(info.realm);
            this.elements.cultivateSpeed.textContent = `x${speed}`;
        }

        // 每小时消耗灵石
        if (this.elements.spiritStonesPerHour) {
            const level = info.level || 1;
            const speed = cultivateService.getCultivationSpeed(info.realm);
            const stonesPerHour = (20 + level * 5) * speed;
            this.elements.spiritStonesPerHour.textContent = formatUtils.formatSpiritStones(stonesPerHour);
        }

        // 修炼状态
        if (this.elements.cultivateStatus) {
            if (info.isCultivating) {
                this.elements.cultivateStatus.textContent = '修炼中';
                this.elements.cultivateStatus.className = 'status cultivating';
            } else {
                this.elements.cultivateStatus.textContent = '未修炼';
                this.elements.cultivateStatus.className = 'status idle';
            }
        }

        // 已修炼时间
        if (this.elements.cultivatedTime) {
            this.elements.cultivatedTime.textContent = formatUtils.formatDuration(info.cultivatedTime || 0);
        }

        // 按钮状态
        if (this.elements.startCultivateBtn) {
            this.elements.startCultivateBtn.disabled = info.isCultivating;
            this.elements.startCultivateBtn.classList.toggle('disabled', info.isCultivating);
        }

        if (this.elements.stopCultivateBtn) {
            this.elements.stopCultivateBtn.disabled = !info.isCultivating;
            this.elements.stopCultivateBtn.classList.toggle('disabled', !info.isCultivating);
        }

        if (this.elements.breakthroughBtn) {
            const canBreakthrough = info.canBreakthrough || false;
            this.elements.breakthroughBtn.disabled = !canBreakthrough;
            this.elements.breakthroughBtn.classList.toggle('disabled', !canBreakthrough);
        }
    }

    showCultivateTypeDialog() {
        const dialogHtml = `
            <div class="cultivate-types">
                <div class="cultivate-type" data-type="normal">
                    <h4>普通修炼</h4>
                    <p>修炼速度 x1.0</p>
                    <p>适合日常修炼</p>
                </div>
                <div class="cultivate-type" data-type="intensive">
                    <h4>闭关修炼</h4>
                    <p>修炼速度 x1.5</p>
                    <p>消耗更多灵石</p>
                </div>
                <div class="cultivate-type" data-type="meditation">
                    <h4>冥想修炼</h4>
                    <p>修炼速度 x2.0</p>
                    <p>消耗大量灵石</p>
                </div>
            </div>
        `;

        modal.show({
            title: '选择修炼方式',
            content: dialogHtml,
            showCancel: true,
            confirmText: '取消'
        });

        document.querySelectorAll('.cultivate-type').forEach(type => {
            type.addEventListener('click', (e) => {
                const cultivateType = e.currentTarget.dataset.type;
                this.handleStartCultivate(cultivateType);
                modal.hide();
            });
        });
    }

    async handleStartCultivate(type) {
        loading.show();
        try {
            await cultivateService.startCultivate(type);
            await this.loadCultivateData();
        } catch (error) {
            toast.error('开始修炼失败');
        } finally {
            loading.hide();
        }
    }

    async handleStopCultivate() {
        if (!confirm('确定要停止修炼吗?')) return;

        loading.show();
        try {
            await cultivateService.stopCultivate();
            await this.loadCultivateData();
        } catch (error) {
            toast.error('停止修炼失败');
        } finally {
            loading.hide();
        }
    }

    async handleBreakthrough() {
        const info = cultivateService.cultivateInfo;
        if (!info) return;

        const cost = 5000; // 突破消耗灵石
        const successRate = 0.7; // 成功率70%

        const dialogHtml = `
            <div class="breakthrough-info">
                <h3>境界突破</h3>
                <div class="current-info">
                    <p>当前境界: ${info.realm} ${info.level}层</p>
                    <p>目标境界: ${this.getNextRealm(info.realm, info.level)}</p>
                </div>
                <div class="breakthrough-cost">
                    <p>消耗: ${formatUtils.formatSpiritStones(cost)}</p>
                    <p>成功率: ${(successRate * 100).toFixed(0)}%</p>
                </div>
                <div class="breakthrough-warning">
                    <p>⚠️ 突破失败将冷却1小时</p>
                </div>
            </div>
        `;

        modal.show({
            title: '确认突破',
            content: dialogHtml,
            confirmText: '突破',
            showCancel: true,
            onConfirm: async () => {
                await this.handleBreakthroughAction();
            }
        });
    }

    getNextRealm(realm, level) {
        const realmOrder = ['练气', '筑基', '金丹', '元婴'];
        const realmIndex = realmOrder.indexOf(realm);

        if (level < 9) {
            return `${realm} ${level + 1}层`;
        } else if (realmIndex < realmOrder.length - 1) {
            return `${realmOrder[realmIndex + 1]} 1层`;
        } else {
            return '已达最高境界';
        }
    }

    async handleBreakthroughAction() {
        loading.show();
        try {
            await cultivateService.breakthrough();
            await this.loadCultivateData();
        } catch (error) {
            toast.error('突破失败');
        } finally {
            loading.hide();
        }
    }

    startAutoRefresh() {
        this.cultivateTimer = setInterval(async () => {
            const info = cultivateService.cultivateInfo;
            if (info?.isCultivating) {
                await this.loadCultivateData();
            }
        }, this.refreshInterval);
    }

    stopAutoRefresh() {
        if (this.cultivateTimer) {
            clearInterval(this.cultivateTimer);
            this.cultivateTimer = null;
        }
    }
}

export const cultivateUI = new CultivateUI();
