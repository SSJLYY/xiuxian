export { petEvolutionService } from './PetEvolutionService.js';
export { petEvolutionUI } from './PetEvolutionUI.js';

export function mountPetEvolutionGlobals() {
    window.petEvolutionUI = petEvolutionUI;
    window.loadEvolutionInfo = async function() { return petEvolutionUI.loadEvolutionInfo(); };
    window.doEvolution = async function(playerPetId) { return petEvolutionUI.doEvolution(playerPetId); };
    return petEvolutionUI;
}
