export { achievementService } from './AchievementService.js';
export { achievementUI } from './AchievementUI.js';

export function mountAchievementGlobals() {
    window.achievementUI = achievementUI;
    window.switchAchievementTab = function(tab) {
        return achievementUI.switchTab(tab);
    };
    window.claimAchievement = async function(id) {
        return achievementUI.claimAchievement(id);
    };
    return achievementUI;
}
