/**
 * 每日签到系统 - 前端实现
 * 提供月历签到展示、连续签到奖励预览、签到动画
 */

class CheckInSystem {
    constructor() {
        this.status = null;
    }

    /**
     * 初始化签到模块
     */
    async init() {
        await this.loadStatus();
        this.render();
    }

    async loadStatus() {
        try {
            const response = await api.get('/checkin/status');
            if (response.success) {
                this.status = response.data;
            }
        } catch (e) {
            console.error('[CheckIn] 加载签到状态失败:', e);
        }
    }

    render() {
        const container = document.getElementById('checkin-module');
        if (!container) return;

        const s = this.status;
        if (!s) {
            container.innerHTML = '<div class="loading-state">加载签到数据中...</div>';
            return;
        }

        container.innerHTML = `
            <div class="checkin-panel">
                <!-- 顶部状态 -->
                <div class="checkin-header">
                    <div class="checkin-title">
                        <i class="fas fa-calendar-check"></i>
                        <h3>每日签到</h3>
                    </div>
                    <div class="checkin-streak">
                        <span class="streak-icon">🔥</span>
                        <span class="streak-count">${s.consecutiveDays || 0}</span>
                        <span class="streak-label">天连续签到</span>
                    </div>
                </div>

                <!-- 今日奖励预览 -->
                ${!s.checkedToday ? `
                    <div class="today-reward-preview">
                        <h4>今日签到奖励</h4>
                        <div class="reward-preview-items">
                            <div class="preview-item stones">
                                <span class="preview-icon">💎</span>
                                <span class="preview-value">+${s.todayRewardStones || 0}</span>
                                <span class="preview-label">灵石</span>
                            </div>
                            <div class="preview-item exp">
                                <span class="preview-icon">⭐</span>
                                <span class="preview-value">+${s.todayRewardExp || 0}</span>
                                <span class="preview-label">经验</span>
                            </div>
                        </div>
                        <button class="checkin-btn pulse" onclick="checkInSystem.doCheckIn()">
                            <i class="fas fa-hand-pointer"></i> 立即签到
                        </button>
                    </div>
                ` : `
                    <div class="today-checked">
                        <div class="checked-icon">✅</div>
                        <p class="checked-text">今日已签到</p>
                        <p class="checked-sub">明日奖励：灵石 +${s.todayRewardStones || 0} · 经验 +${s.todayRewardExp || 0}</p>
                    </div>
                `}

                <!-- 连续签到奖励进度条 -->
                <div class="streak-progress">
                    <h4>连续签到奖励进度</h4>
                    <div class="streak-track">
                        ${this.renderStreakMilestones(s.consecutiveDays || 0)}
                    </div>
                </div>

                <!-- 本月签到日历 -->
                <div class="checkin-calendar-section">
                    <h4>本月签到 <span class="month-stats">${s.totalCheckedThisMonth || 0}/${s.daysInMonth || 30} 天</span></h4>
                    <div class="checkin-calendar">
                        ${this.renderCalendar(s.calendar || [])}
                    </div>
                </div>
            </div>
        `;
    }

    renderStreakMilestones(currentDays) {
        const milestones = [
            { day: 1, icon: '💫', label: '1天' },
            { day: 3, icon: '🌙', label: '3天' },
            { day: 7, icon: '🎖️', label: '7天' },
            { day: 14, icon: '🌟', label: '14天' },
            { day: 30, icon: '🏆', label: '30天' },
        ];

        return milestones.map(m => {
            const reached = currentDays >= m.day;
            const current = currentDays === m.day;
            return `
                <div class="milestone-node ${reached ? 'reached' : ''} ${current ? 'current' : ''}">
                    <div class="milestone-icon">${m.icon}</div>
                    <div class="milestone-label">${m.label}</div>
                    ${current ? '<div class="milestone-current-arrow">▲</div>' : ''}
                </div>
            `;
        }).join('<div class="milestone-connector"></div>');
    }

    renderCalendar(calendar) {
        return calendar.map(cell => {
            let cls = 'calendar-day';
            if (cell.checked) cls += ' checked';
            if (cell.today) cls += ' today';
            if (cell.future) cls += ' future';
            if (cell.milestone) cls += ' milestone';

            return `
                <div class="${cls}" title="${cell.checked ? '已签到' : cell.future ? '未来' : '未签到'} ·+${cell.previewStones || 0}灵石">
                    <span class="day-num">${cell.day}</span>
                    ${cell.checked ? '<span class="day-check">✓</span>' : ''}
                    ${cell.milestone && !cell.checked && !cell.future ? '<span class="milestone-dot">★</span>' : ''}
                </div>
            `;
        }).join('');
    }

    /**
     * 执行签到
     */
    async doCheckIn() {
        const btn = document.querySelector('.checkin-btn');
        if (btn) btn.disabled = true;

        try {
            const response = await api.post('/checkin/do');
            if (response.success) {
                const data = response.data;

                // 显示签到成功动画
                this.showCheckInAnimation(data);

                // 刷新状态
                await this.loadStatus();
                this.render();

                // 刷新玩家数据（灵石/经验更新）
                if (window.authManager?.loadPlayerProfile) {
                    await window.authManager.loadPlayerProfile();
                }

                const msg = data.isMilestone ? data.milestoneMessage :
                    `签到成功！连续${data.consecutiveDays}天 · 灵石+${data.rewardSpiritStones} 经验+${data.rewardExp}`;
                this.showToast(msg, 'success');
            } else {
                this.showToast(response.message || '签到失败', 'error');
                if (btn) btn.disabled = false;
            }
        } catch (e) {
            this.showToast(e.message, 'error');
            if (btn) btn.disabled = false;
        }
    }

    showCheckInAnimation(data) {
        const container = document.getElementById('checkin-module');
        if (!container) return;

        const overlay = document.createElement('div');
        overlay.className = 'checkin-success-overlay';
        overlay.innerHTML = `
            <div class="checkin-success-content">
                <div class="success-emoji">🎉</div>
                <h3>签到成功！</h3>
                <p class="streak-info">连续签到 <strong>${data.consecutiveDays}</strong> 天</p>
                <div class="reward-display">
                    <span>💎 +${data.rewardSpiritStones}</span>
                    <span>⭐ +${data.rewardExp}</span>
                </div>
                ${data.isMilestone ? `<p class="milestone-msg">${escapeHtml(data.milestoneMessage)}</p>` : ''}
            </div>
        `;
        container.appendChild(overlay);
        setTimeout(() => overlay.remove(), 2000);
    }

    showToast(msg, type = 'info') {
        if (window.authManager?.showToast) window.authManager.showToast(msg, type);
    }
}

const checkInSystem = new CheckInSystem();
window.checkInSystem = checkInSystem;

console.log('[CheckIn] 签到系统已加载');
