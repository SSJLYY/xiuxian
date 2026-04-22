export { narrativeService } from './NarrativeService.js';
export { narrativeUI } from './NarrativeUI.js';

export function mountNarrativeGlobals() {
    window.narrativeUI = narrativeUI;
    window.switchNarrativeTab = function(tab) {
        return narrativeUI.switchGameTab(tab);
    };
    window.loadNpcList = async function() {
        return narrativeUI.loadNpcList();
    };
    window.loadNpcRelations = async function() {
        return narrativeUI.loadNpcRelations();
    };
    window.showNpcDetail = async function(npcId) {
        return narrativeUI.showNpcDetail(npcId);
    };
    return narrativeUI;
}
