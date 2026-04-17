/**
 * 签到模块 - UI渲染层
 */
import { checkinService } from './CheckinService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { formatUtils } from '../../core/utils/FormatUtils.js';
import { escapeHtml } from '../../core/utils/Security.js';

export class CheckinUI {
    constructor() {
        this.currentMonth = new Date().getMonth() + 1;
        this.currentYear = new Date().getFullYear();
    }

    init() {
        this.setupElements();
        this.bindEvents();
        this.loadCheckinData();
    }

    setupElements() {
        this.elements = {
            checkinBtn: document.getElementById('checkinBtn'),
            checkinStatus: document.getElementById('checkinStatus'),
            calendarContainer: document.getElementById('calendarContainer'),
            rewardsContainer: document.getElementById('rewardsContainer'),
            consecutiveDays: document.getElementById('consecutiveDays'),
            totalDays: document.getElementById('totalDays')
        };
    }

    bindEvents() {
        if (this.elements.checkinBtn) {
            this.elements.checkinBtn.addEventListener('click', () => this.handleCheckin());
        }

        // 月份切换
        document.getElementById('prevMonth')?.addEventListener('click', () => {
            this.changeMonth(-1);
        });

        document.getElementById('nextMonth')?.addEventListener('click', () => {
            this.changeMonth(1);
        });
    }

    async loadCheckinData() {
        loading.show();
        try {
            await Promise.all([
                checkinService.getCheckinStatus(),
                checkinService.getCalendarData(this.currentMonth, this.currentYear),
                checkinService.getCheckinRewards()
            ]);

            this.renderCheckinStatus();
            this.renderCalendar();
            this.renderRewards();
        } catch (error) {
            toast.error('加载签到数据失败');
        } finally {
            loading.hide();
        }
    }

    renderCheckinStatus() {
        const status = checkinService.checkinStatus;
        if (!status) return;

        // 更新连续签到天数
        if (this.elements.consecutiveDays) {
            this.elements.consecutiveDays.textContent = status.consecutiveDays || 0;
        }

        // 更新总签到天数
        if (this.elements.totalDays) {
            this.elements.totalDays.textContent = status.totalDays || 0;
        }

        // 更新签到按钮状态
        if (this.elements.checkinBtn) {
            if (status.checkedToday) {
                this.elements.checkinBtn.textContent = '今日已签到';
                this.elements.checkinBtn.disabled = true;
                this.elements.checkinBtn.classList.add('disabled');
            } else {
                this.elements.checkinBtn.textContent = '立即签到';
                this.elements.checkinBtn.disabled = false;
                this.elements.checkinBtn.classList.remove('disabled');
            }
        }
    }

    renderCalendar() {
        const container = this.elements.calendarContainer;
        if (!container || !checkinService.calendarData) return;

        const data = checkinService.calendarData;
        const today = new Date();
        const isCurrentMonth = this.currentMonth === today.getMonth() + 1 && this.currentYear === today.getFullYear();

        // 生成本月日历
        let calendarHtml = `
            <div class="calendar-header">
                <button id="prevMonth" class="btn btn-sm">←</button>
                <span>${this.currentYear}年${this.currentMonth}月</span>
                <button id="nextMonth" class="btn btn-sm">→</button>
            </div>
            <div class="calendar-grid">
                <div class="calendar-day-header">日</div>
                <div class="calendar-day-header">一</div>
                <div class="calendar-day-header">二</div>
                <div class="calendar-day-header">三</div>
                <div class="calendar-day-header">四</div>
                <div class="calendar-day-header">五</div>
                <div class="calendar-day-header">六</div>
        `;

        // 计算本月第一天是星期几
        const firstDay = new Date(this.currentYear, this.currentMonth - 1, 1).getDay();
        const daysInMonth = new Date(this.currentYear, this.currentMonth, 0).getDate();

        // 填充空白
        for (let i = 0; i < firstDay; i++) {
            calendarHtml += '<div class="calendar-day empty"></div>';
        }

        // 填充日期
        for (let day = 1; day <= daysInMonth; day++) {
            const isToday = isCurrentMonth && day === today.getDate();
            const isChecked = data.checkedDays.includes(day);
            const hasReward = data.rewardDays.includes(day);
            const reward = data.rewards.find(r => r.day === day);

            let dayClass = 'calendar-day';
            if (isToday) dayClass += ' today';
            if (isChecked) dayClass += ' checked';
            if (hasReward) dayClass += ' has-reward';

            calendarHtml += `
                <div class="${dayClass}" data-day="${day}">
                    <span class="day-number">${day}</span>
                    ${hasReward ? `<span class="reward-icon">*</span>` : ''}
                    ${reward ? `<span class="reward-info">${escapeHtml(reward.description)}</span>` : ''}
                </div>
            `;
        }

        calendarHtml += '</div>';
        container.innerHTML = calendarHtml;
    }

    renderRewards() {
        const container = this.elements.rewardsContainer;
        if (!container) return;

        const rewards = checkinService.calendarData?.rewards || [];
        if (rewards.length === 0) {
            container.innerHTML = '<p>暂无奖励信息</p>';
            return;
        }

        container.innerHTML = `
            <h3>签到奖励</h3>
            <div class="rewards-list">
                ${rewards.map(reward => `
                    <div class="reward-item ${reward.claimed ? 'claimed' : ''}">
                        <div class="reward-day">第${escapeHtml(reward.day)}天</div>
                        <div class="reward-desc">${escapeHtml(reward.description)}</div>
                        <div class="reward-status">${reward.claimed ? '已领取' : '待领取'}</div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    async handleCheckin() {
        loading.show();
        try {
            await checkinService.doCheckin();
            await this.loadCheckinData();
        } catch (error) {
            toast.error('签到失败');
        } finally {
            loading.hide();
        }
    }

    async changeMonth(delta) {
        this.currentMonth += delta;
        if (this.currentMonth > 12) {
            this.currentMonth = 1;
            this.currentYear++;
        } else if (this.currentMonth < 1) {
            this.currentMonth = 12;
            this.currentYear--;
        }

        loading.show();
        try {
            await checkinService.getCalendarData(this.currentMonth, this.currentYear);
            this.renderCalendar();
        } catch (error) {
            toast.error('加载日历失败');
        } finally {
            loading.hide();
        }
    }
}

export const checkinUI = new CheckinUI();
