/**
 * 活动模块 - UI渲染层
 */
import { activityService } from './ActivityService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { formatUtils } from '../../core/utils/FormatUtils.js';

export class ActivityUI {
    init() {
        this.setupElements();
        this.bindEvents();
        this.loadActivities();
    }

    setupElements() {
        this.elements = {
            activitiesContainer: document.getElementById('activitiesContainer'),
            myActivitiesContainer: document.getElementById('myActivitiesContainer'),
            activityTabs: document.querySelectorAll('[data-tab="activity"]')
        };
    }

    bindEvents() {
        // 标签页切换
        this.elements.activityTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                this.switchTab(e.target.dataset.activityTab);
            });
        });
    }

    switchTab(tabName) {
        this.elements.activityTabs.forEach(tab => {
            tab.classList.toggle('active', tab.dataset.activityTab === tabName);
        });

        if (tabName === 'all') {
            this.elements.activitiesContainer.style.display = 'block';
            this.elements.myActivitiesContainer.style.display = 'none';
        } else {
            this.elements.activitiesContainer.style.display = 'none';
            this.elements.myActivitiesContainer.style.display = 'block';
        }
    }

    async loadActivities() {
        loading.show();
        try {
            await Promise.all([
                activityService.getActivities(),
                activityService.getMyActivities()
            ]);
            this.renderActivities();
            this.renderMyActivities();
        } catch (error) {
            toast.error('加载活动数据失败');
        } finally {
            loading.hide();
        }
    }

    renderActivities() {
        const container = this.elements.activitiesContainer;
        if (!container) return;

        if (activityService.activities.length === 0) {
            container.innerHTML = '<p>暂无活动</p>';
            return;
        }

        container.innerHTML = `
            <div class="activities-list">
                ${activityService.activities.map(activity => this.renderActivityCard(activity)).join('')}
            </div>
        `;

        // 绑定事件
        container.querySelectorAll('[data-action="participate"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleParticipate(e.target.dataset.activityId));
        });

        container.querySelectorAll('[data-action="claim"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleClaim(e.target.dataset.activityId));
        });
    }

    renderActivityCard(activity) {
        const now = new Date();
        const startTime = new Date(activity.startTime);
        const endTime = new Date(activity.endTime);
        const isOngoing = now >= startTime && now <= endTime;
        const isEnded = now > endTime;
        const isNotStarted = now < startTime;

        let statusClass = 'not-started';
        let statusText = '未开始';
        if (isOngoing) {
            statusClass = 'ongoing';
            statusText = '进行中';
        } else if (isEnded) {
            statusClass = 'ended';
            statusText = '已结束';
        }

        return `
            <div class="activity-card ${statusClass}">
                <div class="activity-header">
                    <h4>${activity.name}</h4>
                    <span class="activity-status ${statusClass}">${statusText}</span>
                </div>
                <div class="activity-body">
                    <p class="activity-desc">${activity.description}</p>
                    <div class="activity-time">
                        <span>开始: ${formatUtils.formatDateTime(startTime)}</span>
                        <span>结束: ${formatUtils.formatDateTime(endTime)}</span>
                    </div>
                    <div class="activity-progress">
                        <div class="progress-info">
                            <span>进度: ${activity.progress}/${activity.target}</span>
                        </div>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${(activity.progress / activity.target) * 100}%"></div>
                        </div>
                    </div>
                </div>
                <div class="activity-footer">
                    <div class="activity-reward">
                        <span>奖励: ${activity.rewardDescription}</span>
                    </div>
                    <div class="activity-actions">
                        ${isOngoing && !activity.participated ?
                            `<button class="btn btn-primary" data-action="participate" data-activity-id="${activity.id}">参与</button>` : ''}
                        ${activity.canClaim ?
                            `<button class="btn btn-success" data-action="claim" data-activity-id="${activity.id}">领取奖励</button>` : ''}
                        ${activity.claimed ?
                            `<button class="btn btn-disabled" disabled>已领取</button>` : ''}
                    </div>
                </div>
            </div>
        `;
    }

    renderMyActivities() {
        const container = this.elements.myActivitiesContainer;
        if (!container) return;

        if (activityService.myActivities.length === 0) {
            container.innerHTML = '<p>暂无参与的活动</p>';
            return;
        }

        container.innerHTML = `
            <div class="my-activities-list">
                ${activityService.myActivities.map(activity => this.renderMyActivityCard(activity)).join('')}
            </div>
        `;
    }

    renderMyActivityCard(activity) {
        return `
            <div class="my-activity-card">
                <div class="activity-info">
                    <h4>${activity.name}</h4>
                    <p>${activity.description}</p>
                    <div class="activity-stats">
                        <span>进度: ${activity.progress}/${activity.target}</span>
                        <span>奖励: ${activity.rewardDescription}</span>
                    </div>
                </div>
                <div class="activity-status">
                    ${activity.claimed ?
                        '<span class="status claimed">已领取</span>' :
                        activity.canClaim ?
                        `<button class="btn btn-sm btn-success" data-action="claim" data-activity-id="${activity.id}">领取奖励</button>` :
                        '<span class="status in-progress">进行中</span>'
                    }
                </div>
            </div>
        `;
    }

    async handleParticipate(activityId) {
        loading.show();
        try {
            await activityService.participateActivity(activityId);
            await this.loadActivities();
        } catch (error) {
            toast.error('参与失败');
        } finally {
            loading.hide();
        }
    }

    async handleClaim(activityId) {
        loading.show();
        try {
            await activityService.claimReward(activityId);
            await this.loadActivities();
        } catch (error) {
            toast.error('领取失败');
        } finally {
            loading.hide();
        }
    }
}

export const activityUI = new ActivityUI();
