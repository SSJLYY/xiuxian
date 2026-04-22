export { questService } from './QuestService.js';
export { questUI } from './QuestUI.js';

export function mountQuestGlobals() {
    window.questUI = questUI;
    window.switchQuestTab = function(tab) { return questUI.switchTab(tab); };
    window.claimQuest = async function(questId) { return questUI.claimQuest(questId); };
    return questUI;
}
