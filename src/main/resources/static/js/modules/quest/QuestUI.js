/**
 * 任务模块 - UI渲染层
 */
import { questService } from './QuestService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';

export class QuestUI {
    constructor() {
        this.currentType = 'all';
    }

    init() {
        this.setupElements();
        this.bindEvents();
        this.loadQuestData();
    }

    setupElements() {
        this.elements = {
            questListContainer: document.getElementById('questListContainer'),
            myQuestsContainer: document.getElementById('myQuestsContainer'),
            typeFilter: document.getElementById('typeFilter'),
            questTabs: document.querySelectorAll('[data-tab="quest"]')
        };
    }

    bindEvents() {
        // 标签页切换
        this.elements.questTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                this.switchTab(e.target.dataset.questTab);
            });
        });

        // 类型筛选
        if (this.elements.typeFilter) {
            this.elements.typeFilter.addEventListener('change', (e) => {
                this.currentType = e.target.value;
                this.loadQuestList();
            });
        }
    }

    switchTab(tabName) {
        this.elements.questTabs.forEach(tab => {
            tab.classList.toggle('active', tab.dataset.questTab === tabName);
        });

        if (tabName === 'available') {
            this.elements.questListContainer.style.display = 'block';
            this.elements.myQuestsContainer.style.display = 'none';
        } else {
            this.elements.questListContainer.style.display = 'none';
            this.elements.myQuestsContainer.style.display = 'block';
        }
    }

    async loadQuestData() {
        loading.show();
        try {
            await Promise.all([
                questService.getQuestList(this.currentType),
                questService.getMyQuests()
            ]);
            this.renderQuestList();
            this.renderMyQuests();
        } catch (error) {
            toast.error('加载任务数据失败');
        } finally {
            loading.hide();
        }
    }

    async loadQuestList() {
        loading.show();
        try {
            await questService.getQuestList(this.currentType);
            this.renderQuestList();
        } catch (error) {
            toast.error('加载任务列表失败');
        } finally {
            loading.hide();
        }
    }

    renderQuestList() {
        const container = this.elements.questListContainer;
        if (!container) return;

        if (questService.quests.length === 0) {
            container.innerHTML = '<p>暂无任务</p>';
            return;
        }

        container.innerHTML = `
            <div class="quest-list">
                ${questService.quests.map(quest => this.renderQuestCard(quest, 'available')).join('')}
            </div>
        `;

        // 绑定接受按钮
        container.querySelectorAll('[data-action="accept"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleAccept(e.target.dataset.questId));
        });
    }

    renderMyQuests() {
        const container = this.elements.myQuestsContainer;
        if (!container) return;

        if (questService.myQuests.length === 0) {
            container.innerHTML = '<p>暂无进行中的任务</p>';
            return;
        }

        container.innerHTML = `
            <div class="my-quests-list">
                ${questService.myQuests.map(quest => this.renderQuestCard(quest, 'my')).join('')}
            </div>
        `;

        // 绑定完成和领取按钮
        container.querySelectorAll('[data-action="complete"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleComplete(e.target.dataset.questId));
        });

        container.querySelectorAll('[data-action="claim"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleClaim(e.target.dataset.questId));
        });
    }

    renderQuestCard(quest, type) {
        const typeLabel = this.translateType(quest.type);
        const status = quest.status || type === 'available' ? 'available' : quest.status;
        const progress = quest.progress || 0;
        const target = quest.target || 1;
        const progressPercent = Math.min((progress / target) * 100, 100);

        return `
            <div class="quest-card ${quest.priority} ${status}">
                <div class="quest-header">
                    <h4>${quest.name}</h4>
                    <span class="quest-type ${quest.type}">${typeLabel}</span>
                </div>
                <div class="quest-body">
                    <p class="quest-desc">${quest.description}</p>
                    <div class="quest-progress">
                        <div class="progress-info">
                            <span>进度: ${progress}/${target}</span>
                            <span class="progress-percent">${progressPercent.toFixed(0)}%</span>
                        </div>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${progressPercent}%"></div>
                        </div>
                    </div>
                    <div class="quest-rewards">
                        <span class="reward-label">奖励:</span>
                        ${quest.rewards.map(reward => `
                            <span class="reward-item">${reward.description}</span>
                        `).join('')}
                    </div>
                </div>
                <div class="quest-footer">
                    <div class="quest-deadline">
                        <span>截止时间: ${quest.deadline ? new Date(quest.deadline).toLocaleString() : '无限制'}</span>
                    </div>
                    <div class="quest-actions">
                        ${type === 'available' && !quest.accepted ? `
                            <button class="btn btn-primary" data-action="accept" data-quest-id="${quest.id}">接受</button>
                        ` : ''}
                        ${type === 'my' && status === 'in-progress' && progress >= target ? `
                            <button class="btn btn-success" data-action="complete" data-quest-id="${quest.id}">完成</button>
                        ` : ''}
                        ${type === 'my' && status === 'completed' && !quest.claimed ? `
                            <button class="btn btn-success" data-action="claim" data-quest-id="${quest.id}">领取奖励</button>
                        ` : ''}
                        ${status === 'claimed' ? `
                            <button class="btn btn-disabled" disabled>已领取</button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }

    translateType(type) {
        const typeMap = {
            'daily': '每日',
            'weekly': '每周',
            'monthly': '每月',
            'main': '主线'
        };
        return typeMap[type] || type;
    }

    async handleAccept(questId) {
        loading.show();
        try {
            await questService.acceptQuest(questId);
            await this.loadQuestData();
        } catch (error) {
            toast.error('接受任务失败');
        } finally {
            loading.hide();
        }
    }

    async handleComplete(questId) {
        loading.show();
        try {
            await questService.completeQuest(questId);
            await this.loadQuestData();
        } catch (error) {
            toast.error('完成任务失败');
        } finally {
            loading.hide();
        }
    }

    async handleClaim(questId) {
        loading.show();
        try {
            await questService.claimReward(questId);
            await this.loadQuestData();
        } catch (error) {
            toast.error('领取奖励失败');
        } finally {
            loading.hide();
        }
    }
}

export const questUI = new QuestUI();
