import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class AuctionService {
    async listItems() {
        try {
            const response = await gameAPI.getAuctionItems({});
            if (response.success) return response.data;
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载拍卖物品失败: ' + error.message);
            throw error;
        }
    }

    async listItem(itemId, startingPrice, buyoutPrice) {
        try {
            const response = await gameAPI.listAuctionItem(itemId, startingPrice);
            if (response.success) {
                toast.success('上架成功');
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('上架失败：' + error.message);
            throw error;
        }
    }

    async buyItem(auctionId) {
        try {
            const response = await gameAPI.buyAuctionItem(auctionId);
            if (response.success) {
                toast.success('购买成功');
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('购买失败: ' + error.message);
            throw error;
        }
    }

    async cancelListing(auctionId) {
        try {
            const response = await gameAPI.cancelAuctionItem(auctionId);
            if (response.success) {
                toast.success('下架成功');
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('下架失败: ' + error.message);
            throw error;
        }
    }
}

export const auctionService = new AuctionService();
