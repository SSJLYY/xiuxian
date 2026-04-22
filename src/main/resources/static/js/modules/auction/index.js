export { auctionService } from './AuctionService.js';
export { auctionUI } from './AuctionUI.js';

export function mountAuctionGlobals() {
    window.auctionUI = auctionUI;
    window.switchAuctionTab = function(tab) { return auctionUI.switchTab(tab); };
    window.loadAuctionItems = async function() { return auctionUI.loadAuctionItems(); };
    window.loadMyAuctionItems = async function() { return auctionUI.loadMyAuctionItems(); };
    window.buyAuctionItem = async function(auctionId) { return auctionUI.buyAuctionItem(auctionId); };
    window.cancelAuctionItem = async function(auctionId) { return auctionUI.cancelAuctionItem(auctionId); };
    window.showTab = function(tab) {
        document.querySelectorAll('.tab-content').forEach(el => {
            el.style.display = 'none';
        });
        document.querySelectorAll('.nav-tab').forEach(el => {
            el.classList.remove('active');
        });
        const content = document.getElementById(`${tab}-tab`);
        const nav = document.querySelector(`.nav-tab[data-module="${tab}"]`);
        if (content) content.style.display = 'block';
        if (nav) nav.classList.add('active');
        if (tab === 'browse') return auctionUI.loadAuctionItems();
        if (tab === 'my-auctions') return auctionUI.loadMyAuctionItems();
    };
    window.loadMyAuctions = async function(status = '') { return auctionUI.loadMyAuctionItems(status); };
    return auctionUI;
}
