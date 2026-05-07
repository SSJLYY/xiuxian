import { checkinService } from './CheckinService.js';

function showToast(message, type = 'info') {
    if (window.moduleManager?.showToast) {
        window.moduleManager.showToast(message, type);
        return;
    }
    if (window.authManager?.showToast) {
        window.authManager.showToast(message, type);
        return;
    }
    console.log(`[${type}] ${message}`);
}

export class CheckinUI {
    constructor() {
        this.currentMonth = new Date();
    }

    async init() {
        return this.loadStatus();
    }

    async loadStatus() {
        try {
            const year = this.currentMonth.getFullYear();
            const month = this.currentMonth.getMonth() + 1;
            const status = await checkinService.getStatus(year, month);
            this.renderCalendar(status);
            this.updateStats(status);
            const btn = document.getElementById('checkin-btn');
            if (btn) {
                if (status.checkedToday) {
                    btn.disabled = true;
                    btn.innerHTML = '<i class="fa-solid fa-check"></i> 今日已签到';
                } else {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="fa-solid fa-calendar-check"></i> 今日签到';
                }
            }
        } catch (error) {
            showToast(`加载签到状态失败: ${error.message}`, 'error');
        }
    }

    renderCalendar(status) {
        const grid = document.getElementById('checkin-days-grid');
        const title = document.getElementById('checkin-month-title');
        if (!grid) return;
        const now = this.currentMonth;
        if (title) title.textContent = `${now.getFullYear()}年 ${now.getMonth() + 1}月`;
        const year = now.getFullYear();
        const month = now.getMonth();
        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const realToday = new Date();
        const today = realToday.getDate();
        const checkedDays = (status.calendar || []).filter(cell => cell.checked).map(cell => cell.day);
        let html = '';
        for (let i = 0; i < firstDay; i++) html += '<div></div>';
        for (let d = 1; d <= daysInMonth; d++) {
            const isToday = d === today && now.getMonth() === realToday.getMonth() && now.getFullYear() === realToday.getFullYear();
            const isChecked = checkedDays.includes(d);
            const rewardIcons = { 0: '⚔️', 1: '🛡️', 2: '❤️' };
            const rewardType = (d - 1) % 3;
            html += `
                <div class="checkin-day ${isToday ? 'today' : ''} ${isChecked ? 'checked' : ''}" style="aspect-ratio:1;display:flex;flex-direction:column;align-items:center;justify-content:center;border-radius:8px;background:${isChecked ? 'rgba(46,204,113,0.15)' : isToday ? 'rgba(212,175,55,0.15)' : 'rgba(255,255,255,0.03)'};border:1px solid ${isChecked ? 'rgba(46,204,113,0.3)' : isToday ? 'rgba(212,175,55,0.3)' : 'transparent'};">
                    <span class="text-sm ${isToday ? 'font-bold' : ''}" style="color:${isToday ? 'var(--accent-gold)' : 'var(--text-light)'};">${d}</span>
                    ${isChecked ? '<span class="text-xs checkin-mark">✓</span>' : `<span class="text-xs checkin-reward-icon">${rewardIcons[rewardType]}</span>`}
                </div>
            `;
        }
        grid.innerHTML = html;
    }

    updateStats(status) {
        const el1 = document.getElementById('checkin-consecutive-days');
        const el2 = document.getElementById('checkin-total-days');
        const el3 = document.getElementById('checkin-today-reward');
        if (el1) el1.textContent = status.consecutiveDays || 0;
        if (el2) el2.textContent = status.totalCheckedThisMonth || 0;
        if (el3) el3.textContent = status.todayRewardStones || 0;
    }

    async changeMonth(delta) {
        this.currentMonth.setMonth(this.currentMonth.getMonth() + delta);
        return this.loadStatus();
    }

    async doCheckIn() {
        const btn = document.getElementById('checkin-btn');
        if (btn) {
            btn.disabled = true;
            btn.textContent = '签到中...';
        }
        try {
            const res = await checkinService.doCheckin();
            showToast(res.message || '签到成功', 'success');
            if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
            await this.loadStatus();
        } catch (error) {
            showToast(`签到失败: ${error.message}`, 'error');
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = '<i class="fa-solid fa-calendar-check"></i> 今日签到';
            }
        }
    }
}

export const checkinUI = new CheckinUI();
