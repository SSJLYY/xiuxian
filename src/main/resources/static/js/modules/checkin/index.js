export { checkinService } from './CheckinService.js';
export { checkinUI } from './CheckinUI.js';

export function mountCheckinGlobals() {
    window.checkinUI = checkinUI;
    window.changeCheckinMonth = function(delta) { return checkinUI.changeMonth(delta); };
    window.doCheckIn = async function() { return checkinUI.doCheckIn(); };
    window.checkInSystem = checkinUI;
    return checkinUI;
}
