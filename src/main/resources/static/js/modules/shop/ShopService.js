import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class ShopService {
    constructor() {
        this.shopItems = [];
        this.myOrders = [];
    }

    async getShopItems(category = 'all') {
        try {
            const response = await gameAPI.getShopItems(category);
            if (!response?.success) {
                throw new Error(response?.message || '加载商品列表失败');
            }
            this.shopItems = response.data || [];
            return this.shopItems;
        } catch (error) {
            toast.error('加载商品列表失败: ' + error.message);
            throw error;
        }
    }

    async buyItem(itemId, quantity = 1) {
        try {
            const response = await gameAPI.buyShopItem(itemId, quantity);
            if (!response?.success) {
                throw new Error(response?.message || '购买失败');
            }
            toast.success('购买成功');
            return response.data;
        } catch (error) {
            toast.error('购买失败: ' + error.message);
            throw error;
        }
    }

    async getMyOrders() {
        // 当前后端尚未提供商城订单查询接口，返回空列表以避免把商品数据误当订单渲染。
        this.myOrders = [];
        return this.myOrders;
    }

    getItemById(itemId) {
        return this.shopItems.find(item => String(item.id) === String(itemId)) || null;
    }
}

export const shopService = new ShopService();
export default shopService;
