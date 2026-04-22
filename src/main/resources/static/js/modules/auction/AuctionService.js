import { gameAPI } from '../../core/api/GameApi.js';

export class AuctionService {
    async getAuctionItems(filters = {}) {
        const response = await gameAPI.getAuctionItems(filters);
        if (!response?.success) throw new Error(response?.message || '加载拍卖品失败');
        return response.data?.records || [];
    }

    async getMyAuctionItems() {
        const response = await gameAPI.getMyAuctionItems();
        if (!response?.success) throw new Error(response?.message || '加载我的拍卖失败');
        return response.data || [];
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
