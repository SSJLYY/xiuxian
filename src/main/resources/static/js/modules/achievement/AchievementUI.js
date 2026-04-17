import { achievementService } from './AchievementService.js';
import { loading } from '../../components/Loading.js';
import { escapeHtml } from '../../core/utils/Security.js';

export class AchievementUI {
    init() {
        this.loadAchievements();
    }

    async loadAchievements() {
        loading.show();
        try {
            const data = await achievementService.getAchievements();
            this.renderAchievements(data);
        } catch (error) {
            console.error('加载失败', error);
        } finally {
            loading.hide();
        }
    }

    renderAchievements(achievements) {
        const container = document.getElementById('achievementContainer');
        if (!container) return;

        container.innerHTML = achievements.map(achievement => `
            <div class="achievement-card ${achievement.completed ? 'completed' : ''}">
                <h4>${escapeHtml(achievement.name)}</h4>
                <p>${escapeHtml(achievement.description)}</p>
                <div class="progress">
                    <div class="progress-bar" style="width: ${achievement.progress}%"></div>
                </div>
                ${achievement.canClaim ? '<button class="btn btn-primary" data-action="claim" data-id="' + achievement.id + '">领取</button>' : ''}
            </div>
        `).join('');

        container.querySelectorAll('[data-action="claim"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleClaim(e.target.dataset.id));
        });
    }

    async handleClaim(id) {
        await achievementService.claimAchievement(id);
        await this.loadAchievements();
    }
}

export const achievementUI = new AchievementUI();
