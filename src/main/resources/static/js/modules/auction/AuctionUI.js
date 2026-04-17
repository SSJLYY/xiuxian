import { auctionService } from './AuctionService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { escapeHtml } from '../../core/utils/Security.js';

export class AuctionUI {
    init() {
        this.loadAuctions();
    }

    async loadAuctions() {
        loading.show();
        try {
            const items = await auctionService.listItems();
            this.renderAuctions(items);
        } catch (error) {
            toast.error('加载失败');
        } finally {
            loading.hide();
        }
    }

    renderAuctions(items) {
        const container = document.getElementById('auctionContainer');
        if (!container) return;

        if (items.length === 0) {
            container.innerHTML = '<p>暂无拍卖物品</p>';
            return;
        }

        container.innerHTML = items.map(item => `
            <div class="auction-card">
                <div class="item-info">
                    <h4>${escapeHtml(item.itemName)}</h4>
                    <p>起拍价: ${escapeHtml(item.startingPrice)} 灵石</p>
                    <p>一口价: ${escapeHtml(item.buyoutPrice)} 灵石</p>
                </div>
                <button class="btn btn-primary" data-action="buy" data-auction-id="${item.id}">购买</button>
            </div>
        `).join('');

        container.querySelectorAll('[data-action="buy"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleBuy(e.target.dataset.auctionId));
        });
    }

    async handleBuy(auctionId) {
        loading.show();
        try {
            await auctionService.buyItem(auctionId);
            await this.loadAuctions();
        } catch (error) {
            toast.error('购买失败');
        } finally {
            loading.hide();
        }
    }
}

export const auctionUI = new AuctionUI();
