export { petEvolutionService } from './PetEvolutionService.js';
export { petEvolutionUI } from './PetEvolutionUI.js';

export function mountPetEvolutionGlobals() {
    window.petEvolutionUI = petEvolutionUI;
    window.loadEvolutionInfo = async function() { return petEvolutionUI.loadEvolutionInfo(); };
    window.doEvolution = async function(petId) { return petEvolutionUI.doEvolution(petId); };
    return petEvolutionUI;
}
