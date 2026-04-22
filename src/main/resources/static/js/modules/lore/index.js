export { loreService } from './LoreService.js';
export { loreUI } from './LoreUI.js';

export function mountLoreGlobals() {
    window.loreUI = loreUI;
    window.switchLoreTab = function(tab) { return loreUI.switchTab(tab); };
    window.loadLoreEntries = async function(filter = 'all') { return loreUI.loadEntries(filter); };
    return loreUI;
}
