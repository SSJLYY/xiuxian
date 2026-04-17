/**
 * 商城模块 - 业务逻辑层
 */
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
            if (response.success) {
                this.shopItems = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载商品列表失败: ' + error.message);
            throw error;
        }
    }

    async buyItem(itemId, quantity = 1) {
        try {
            const response = await gameAPI.buyShopItem(itemId, quantity);
            if (response.success) {
                toast.success('购买成功!');
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('购买失败: ' + error.message);
            throw error;
        }
    }

    async getMyOrders() {
        try {
            const response = await gameAPI.getShopItems();
            if (response.success) {
                this.myOrders = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载订单失败：' + error.message);
            throw error;
        }
    }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载订单失败: ' + error.message);
            throw error;
        }
    }

    getItemById(itemId) {
        return this.shopItems.find(item => item.id === itemId) || null;
    }
}

export const shopService = new ShopService();
