import { gameAPI } from '../../core/api/GameApi.js';

export class AuctionService {
    normalizeAuctionItem(item) {
        return {
            ...item,
            itemName: item?.itemName || `${item?.itemType || '物品'} #${item?.itemId || item?.id || ''}`.trim(),
            description: item?.description || `拍卖状态：${item?.status || 'ON_SALE'}`,
            endTime: item?.endTime || item?.expireAt || null,
            currentPrice: item?.currentPrice ?? item?.price ?? 0,
            startPrice: item?.startPrice ?? item?.price ?? 0
        };
    }

    async getAuctionItems(filters = {}) {
        const response = await gameAPI.getAuctionItems(filters);
        if (!response?.success) throw new Error(response?.message || '加载拍卖列表失败');
        return (response.data?.records || []).map(item => this.normalizeAuctionItem(item));
    }

    async getMyAuctionItems() {
        const response = await gameAPI.getMyAuctionItems();
        if (!response?.success) throw new Error(response?.message || '加载我的拍卖失败');
        return (response.data || []).map(item => this.normalizeAuctionItem(item));
    }

    async buyAuctionItem(auctionId) {
        const response = await gameAPI.buyAuctionItem(auctionId);
        if (!response?.success) throw new Error(response?.message || '购买失败');
        return response.data;
    }

    async cancelAuctionItem(auctionId) {
        const response = await gameAPI.cancelAuctionItem(auctionId);
        if (!response?.success) throw new Error(response?.message || '取消拍卖失败');
        return response.data;
    }
}

export const auctionService = new AuctionService();
