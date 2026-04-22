export { petsService } from './PetsService.js';
export { petsUI } from './PetsUI.js';

export function mountPetsGlobals() {
    window.petsUI = petsUI;
    return petsUI;
}
