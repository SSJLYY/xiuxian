import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class ShopService {
    constructor() {
        this.shopItems = [];
        this.myOrders = [];
    }

    normalizeShopItem(item) {
        return {
            ...item,
            id: item?.id,
            name: item?.itemName || item?.equipmentName || item?.name || '未知商品',
            description: item?.itemDescription || item?.equipmentDescription || item?.description || '',
            category: item?.itemType || item?.equipmentType || item?.shopType || 'general',
            quality: item?.itemQuality || item?.equipmentQuality || item?.quality || 'common',
            price: item?.priceSpiritStones ?? item?.price ?? 0,
            stock: item?.stock ?? -1,
            image: '/images/items/default.png'
        };
    }

    async getShopItems(category = 'all') {
        try {
            const response = await gameAPI.getShopItems(category);
            if (!response?.success) {
                throw new Error(response?.message || '加载商品列表失败');
            }
            this.shopItems = (response.data || []).map(item => this.normalizeShopItem(item));
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
        this.myOrders = [];
        return this.myOrders;
    }

    getItemById(itemId) {
        return this.shopItems.find(item => String(item.id) === String(itemId)) || null;
    }
}

export const shopService = new ShopService();
export default shopService;
