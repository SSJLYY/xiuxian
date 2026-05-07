import { auctionService } from './AuctionService.js';

function escapeText(value) {
    return window.escapeHtml ? window.escapeHtml(value) : String(value ?? '');
}

function showToast(message, type = 'info') {
    if (window.moduleManager?.showToast) {
        window.moduleManager.showToast(message, type);
        return;
    }
    if (window.authManager?.showToast) {
        window.authManager.showToast(message, type);
        return;
    }
    console.log(`[${type}] ${message}`);
}

function formatAuctionTime(endTime) {
    if (!endTime) return '-';
    const now = Date.now();
    const end = new Date(endTime).getTime();
    const diff = Math.max(0, Math.floor((end - now) / 1000));
    const h = Math.floor(diff / 3600);
    const m = Math.floor((diff % 3600) / 60);
    const s = diff % 60;
    if (h > 0) return `${h}小时${m}分钟`;
    if (m > 0) return `${m}分钟${s}秒`;
    return `${s}秒`;
}

export class AuctionUI {
    constructor() {
        this.currentMyStatus = '';
    }

    async init() {
        return this.switchTab('browse');
    }

    async switchTab(tab) {
        document.querySelectorAll('#auction-module .tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.auctionTab === tab);
        });
        const browsePanel = document.getElementById('auction-browse-panel');
        const minePanel = document.getElementById('auction-mine-panel');
        if (browsePanel) browsePanel.style.display = tab === 'browse' ? '' : 'none';
        if (minePanel) minePanel.style.display = tab === 'mine' ? '' : 'none';
        return tab === 'browse' ? this.loadAuctionItems() : this.loadMyAuctionItems(this.currentMyStatus);
    }

    async loadAuctionItems() {
        const panel = document.getElementById('auction-items-list') || document.getElementById('auctionItemsContainer');
        if (!panel) return;
        panel.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载拍卖商品中...</p></div>';
        try {
            const filters = {
                itemType: document.getElementById('auction-type-filter')?.value || '',
                minPrice: document.getElementById('auction-min-price')?.value || '',
                maxPrice: document.getElementById('auction-max-price')?.value || ''
            };
            const items = await auctionService.getAuctionItems(filters);
            if (!items.length) {
                panel.innerHTML = '<div class="empty-state">拍卖市场暂无商品</div>';
                return;
            }
            panel.innerHTML = items.map(item => `
                <div class="auction-item-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                    <div class="flex items-center justify-between mb-2">
                        <h4 class="font-semibold">${escapeText(item.itemName || '未知物品')}</h4>
                        <span class="text-xs text-muted">${formatAuctionTime(item.endTime)}</span>
                    </div>
                    <div class="text-sm text-muted mb-2">${escapeText(item.description || '暂无描述')}</div>
                    <div class="font-bold mb-3" style="color:var(--accent-gold);"><i class="fa-solid fa-gem"></i> ${item.currentPrice || item.startPrice || 0}</div>
                    <button class="btn btn-sm w-full btn-primary" onclick="buyAuctionItem(${item.id})">竞拍 / 购买</button>
                </div>
            `).join('');
        } catch (error) {
            panel.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    async loadMyAuctionItems(status = '') {
        this.currentMyStatus = status;
        const panel = document.getElementById('auction-my-items-list') || document.getElementById('myAuctionItems');
        if (!panel) return;
        panel.innerHTML = '<div class="loading-spinner"><div class="spinner"></div><p>加载我的拍卖中...</p></div>';
        try {
            const allItems = await auctionService.getMyAuctionItems();
            const items = status ? allItems.filter(item => item.status === status) : allItems;
            if (!items.length) {
                panel.innerHTML = '<div class="empty-state">您当前没有拍卖中的物品</div>';
                return;
            }
            panel.innerHTML = items.map(item => `
                <div class="auction-item-card p-4 rounded" style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);">
                    <div class="flex items-center justify-between mb-2">
                        <h4 class="font-semibold">${escapeText(item.itemName || '未知物品')}</h4>
                        <span class="text-xs text-muted">${escapeText(item.status || 'ON_SALE')}</span>
                    </div>
                    <div class="text-sm text-muted mb-2">${escapeText(item.description || '暂无描述')}</div>
                    <div class="font-bold mb-3" style="color:var(--accent-gold);"><i class="fa-solid fa-gem"></i> ${item.currentPrice || item.startPrice || 0}</div>
                    <button class="btn btn-sm w-full btn-danger" onclick="cancelAuctionItem(${item.id})">取消拍卖</button>
                </div>
            `).join('');
        } catch (error) {
            panel.innerHTML = `<div class="empty-state">加载失败: ${escapeText(error.message)}</div>`;
        }
    }

    async buyAuctionItem(auctionId) {
        await auctionService.buyAuctionItem(auctionId);
        showToast('购买成功', 'success');
        if (window.authManager?.loadPlayerProfile) await window.authManager.loadPlayerProfile();
        return this.loadAuctionItems();
    }

    async cancelAuctionItem(auctionId) {
        await auctionService.cancelAuctionItem(auctionId);
        showToast('已取消拍卖', 'info');
        return this.loadMyAuctionItems(this.currentMyStatus);
    }
}

export const auctionUI = new AuctionUI();
