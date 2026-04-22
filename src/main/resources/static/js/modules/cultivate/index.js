export { cultivateService } from './CultivateService.js';
export { cultivateUI } from './CultivateUI.js';

export function mountCultivateGlobals() {
    window.cultivateUI = cultivateUI;

    window.startCultivation = async function() {
        return cultivateUI.startCultivation();
    };

    window.stopCultivation = async function() {
        return cultivateUI.stopCultivation();
    };

    window.toggleCultivation = async function() {
        return cultivateUI.toggleCultivation();
    };

    window.claimOfflineRewards = async function() {
        return cultivateUI.claimOfflineRewards();
    };

    window.resetCultivation = async function() {
        return cultivateUI.resetCultivation();
    };

    return cultivateUI;
}
