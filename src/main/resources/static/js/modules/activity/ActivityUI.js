import { activityService } from './ActivityService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { formatUtils } from '../../core/utils/FormatUtils.js';
import { escapeHtml } from '../../core/utils/Security.js';

function resolveStatus(activity) {
    const now = Date.now();
    const startTime = new Date(activity.startTime).getTime();
    const endTime = new Date(activity.endTime).getTime();

    if (Number.isFinite(startTime) && now < startTime) {
        return { className: 'not-started', text: '未开始', ongoing: false };
    }
    if (Number.isFinite(endTime) && now > endTime) {
        return { className: 'ended', text: '已结束', ongoing: false };
    }
    return { className: 'ongoing', text: '进行中', ongoing: true };
}

function buildProgressText(activity) {
    if (activity.target > 0) {
        return activity.progressDisplay;
    }
    return activity.participated ? `${activity.progress}` : '0';
}

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
        this.elements.activityTabs.forEach(tab => {
            tab.addEventListener('click', event => {
                this.switchTab(event.target.dataset.activityTab);
            });
        });
    }

    switchTab(tabName) {
        this.elements.activityTabs.forEach(tab => {
            tab.classList.toggle('active', tab.dataset.activityTab === tabName);
        });

        if (this.elements.activitiesContainer) {
            this.elements.activitiesContainer.style.display = tabName === 'all' ? 'block' : 'none';
        }
        if (this.elements.myActivitiesContainer) {
            this.elements.myActivitiesContainer.style.display = tabName === 'all' ? 'none' : 'block';
        }
    }

    async loadActivities() {
        loading.show();
        try {
            await activityService.refreshData();
            this.renderActivities();
            this.renderMyActivities();
        } catch {
            toast.error('加载活动失败');
        } finally {
            loading.hide();
        }
    }

    renderActivities() {
        const container = this.elements.activitiesContainer;
        if (!container) {
            return;
        }

        if (activityService.activities.length === 0) {
            container.innerHTML = '<p>暂无活动</p>';
            return;
        }

        container.innerHTML = `
            <div class="activities-list">
                ${activityService.activities.map(activity => this.renderActivityCard(activity)).join('')}
            </div>
        `;

        this.bindActionButtons(container);
    }

    renderActivityCard(activity) {
        const status = resolveStatus(activity);
        const progressText = buildProgressText(activity);
        const progressWidth = activity.progressPercent ?? 0;
        const startText = formatUtils.formatDateTime(activity.startTime);
        const endText = formatUtils.formatDateTime(activity.endTime);

        return `
            <div class="activity-card ${status.className}">
                <div class="activity-header">
                    <h4>${escapeHtml(activity.name || '')}</h4>
                    <span class="activity-status ${status.className}">${escapeHtml(status.text)}</span>
                </div>
                <div class="activity-body">
                    <p class="activity-desc">${escapeHtml(activity.description || '')}</p>
                    <div class="activity-time">
                        <span>开始时间：${escapeHtml(startText)}</span>
                        <span>结束时间：${escapeHtml(endText)}</span>
                    </div>
                    <div class="activity-progress">
                        <div class="progress-info">
                            <span>进度：${escapeHtml(progressText)}</span>
                        </div>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${progressWidth}%"></div>
                        </div>
                    </div>
                </div>
                <div class="activity-footer">
                    <div class="activity-reward">
                        <span>奖励：${escapeHtml(activity.rewardDescription || '暂无奖励')}</span>
                    </div>
                    <div class="activity-actions">
                        ${this.renderActionButtons(activity, status.ongoing)}
                    </div>
                </div>
            </div>
        `;
    }

    renderMyActivities() {
        const container = this.elements.myActivitiesContainer;
        if (!container) {
            return;
        }

        if (activityService.myActivities.length === 0) {
            container.innerHTML = '<p>暂无已参与活动</p>';
            return;
        }

        container.innerHTML = `
            <div class="my-activities-list">
                ${activityService.myActivities.map(activity => this.renderMyActivityCard(activity)).join('')}
            </div>
        `;

        this.bindActionButtons(container);
    }

    renderMyActivityCard(activity) {
        const progressText = buildProgressText(activity);

        return `
            <div class="my-activity-card">
                <div class="activity-info">
                    <h4>${escapeHtml(activity.name || '')}</h4>
                    <p>${escapeHtml(activity.description || '')}</p>
                    <div class="activity-stats">
                        <span>进度：${escapeHtml(progressText)}</span>
                        <span>奖励：${escapeHtml(activity.rewardDescription || '暂无奖励')}</span>
                    </div>
                </div>
                <div class="activity-status">
                    ${activity.claimed
                        ? '<span class="status claimed">已领取</span>'
                        : activity.canClaim
                            ? `<button class="btn btn-sm btn-success" data-action="claim" data-activity-id="${activity.id}">领取奖励</button>`
                            : activity.completed
                                ? '<span class="status completed">可领取</span>'
                                : '<span class="status in-progress">进行中</span>'}
                </div>
            </div>
        `;
    }

    renderActionButtons(activity, isOngoing) {
        if (activity.claimed) {
            return '<button class="btn btn-disabled" disabled>已领取</button>';
        }
        if (activity.canClaim) {
            return `<button class="btn btn-success" data-action="claim" data-activity-id="${activity.id}">领取奖励</button>`;
        }
        if (isOngoing && !activity.participated) {
            return `<button class="btn btn-primary" data-action="participate" data-activity-id="${activity.id}">参与活动</button>`;
        }
        if (activity.participated) {
            return '<button class="btn btn-disabled" disabled>已参与</button>';
        }
        return '';
    }

    bindActionButtons(container) {
        container.querySelectorAll('[data-action="participate"]').forEach(button => {
            button.addEventListener('click', event => this.handleParticipate(event.currentTarget.dataset.activityId));
        });

        container.querySelectorAll('[data-action="claim"]').forEach(button => {
            button.addEventListener('click', event => this.handleClaim(event.currentTarget.dataset.activityId));
        });
    }

    async handleParticipate(activityId) {
        loading.show();
        try {
            await activityService.participateActivity(activityId);
            await this.loadActivities();
        } catch {
            toast.error('参与活动失败');
        } finally {
            loading.hide();
        }
    }

    async handleClaim(activityId) {
        loading.show();
        try {
            await activityService.claimReward(activityId);
            await this.loadActivities();
        } catch {
            toast.error('领取奖励失败');
        } finally {
            loading.hide();
        }
    }
}

export const activityUI = new ActivityUI();
