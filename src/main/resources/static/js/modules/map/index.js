export { mapService } from './MapService.js';
export { mapUI } from './MapUI.js';

export function mountMapGlobals() {
    window.mapUI = mapUI;
    window.switchMapTab = function(tab) {
        return mapUI.switchGameTab(tab);
    };
    window.loadCurrentMap = async function() {
        return mapUI.loadCurrentMap();
    };
    window.loadMapList = async function() {
        return mapUI.loadMapList();
    };
    window.enterMap = async function(mapId) {
        return mapUI.enterMap(mapId);
    };
    window.exploreMap = async function() {
        return mapUI.exploreMap();
    };
    return mapUI;
}
