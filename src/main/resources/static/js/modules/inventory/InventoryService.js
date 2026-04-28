import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class InventoryService {
    constructor() {
        this.currentItems = [];
        this.categorizedItems = {};
        this.filter = {
            type: '',
            searchTerm: '',
            sortBy: 'quality',
            order: 'desc'
        };
    }

    async loadInventoryItems() {
        try {
            const response = await gameAPI.getInventoryCategorized();
            if (!response?.success) {
                throw new Error(response?.message || '获取背包物品失败');
            }
            this.categorizedItems = response.data || {};
            this.currentItems = this.applyFilter();
            return this.categorizedItems;
        } catch (error) {
            toast.error('获取背包物品失败: ' + error.message);
            throw error;
        }
    }

    applyFilter() {
        let allItems = [];
        for (const type of Object.keys(this.categorizedItems || {})) {
            allItems = allItems.concat(this.categorizedItems[type] || []);
        }

        if (this.filter.type) {
            allItems = allItems.filter(item => item.itemType === this.filter.type);
        }

        if (this.filter.searchTerm) {
            const keyword = this.filter.searchTerm.toLowerCase();
            allItems = allItems.filter(item =>
                String(item.itemName || '').toLowerCase().includes(keyword) ||
                String(item.itemDescription || '').toLowerCase().includes(keyword)
            );
        }

        allItems.sort((a, b) => {
            let comparison = 0;

            switch (this.filter.sortBy) {
                case 'quality':
                    comparison = Number(a.itemQualityLevel || a.itemQuality || 0) -
                        Number(b.itemQualityLevel || b.itemQuality || 0);
                    break;
                case 'quantity':
                    comparison = Number(a.quantity || 0) - Number(b.quantity || 0);
                    break;
                case 'created':
                    comparison = new Date(a.createdAt || 0) - new Date(b.createdAt || 0);
                    break;
                default: {
                    const typeOrder = { '装备': 1, '消耗品': 2, '材料': 3, '任务物品': 4 };
                    const aTypeOrder = typeOrder[a.itemType] || 5;
                    const bTypeOrder = typeOrder[b.itemType] || 5;
                    comparison = aTypeOrder - bTypeOrder;
                }
            }

            return this.filter.order === 'desc' ? -comparison : comparison;
        });

        this.currentItems = allItems;
        return allItems;
    }

    async useItem(itemId) {
        try {
            const response = await gameAPI.useItem(itemId);
            if (!response?.success) {
                throw new Error(response?.message || '使用物品失败');
            }
            toast.success('使用物品成功');
            await this.loadInventoryItems();
            return response.data;
        } catch (error) {
            toast.error('使用物品失败: ' + error.message);
            throw error;
        }
    }

    async equipItem(itemId) {
        try {
            const response = await gameAPI.equipItem(itemId);
            if (!response?.success) {
                throw new Error(response?.message || '装备失败');
            }
            toast.success('装备成功');
            await this.loadInventoryItems();
            return response.data;
        } catch (error) {
            toast.error('装备失败: ' + error.message);
            throw error;
        }
    }

    async unequipItem(slot) {
        try {
            const response = await gameAPI.unequipItem(slot);
            if (!response?.success) {
                throw new Error(response?.message || '卸下失败');
            }
            toast.success('卸下成功');
            await this.loadInventoryItems();
            return response.data;
        } catch (error) {
            toast.error('卸下失败: ' + error.message);
            throw error;
        }
    }

    async sellItem(itemId, quantity = 1) {
        if (!itemId || quantity < 1) {
            toast.error('参数错误');
            return null;
        }

        try {
            const response = await gameAPI.sellItem(itemId, quantity);
            if (!response?.success) {
                throw new Error(response?.message || '出售失败');
            }
            toast.success(`出售成功，获得 ${response.data?.spiritStones || 0} 灵石`);
            await this.loadInventoryItems();
            return response.data;
        } catch (error) {
            toast.error('出售失败: ' + error.message);
            throw error;
        }
    }

    async discardItem(itemId, quantity = 1) {
        if (!itemId || quantity < 1) {
            toast.error('参数错误');
            return null;
        }

        if (!confirm('确定要丢弃这个物品吗？此操作不可恢复。')) {
            return null;
        }

        try {
            const response = await gameAPI.discardItem(itemId, quantity);
            if (!response?.success) {
                throw new Error(response?.message || '丢弃失败');
            }
            toast.success('丢弃成功');
            await this.loadInventoryItems();
            return response.data;
        } catch (error) {
            toast.error('丢弃失败: ' + error.message);
            throw error;
        }
    }

    getInventoryStats() {
        const stats = {
            totalItems: 0,
            byType: {},
            byQuality: {
                common: 0,
                uncommon: 0,
                rare: 0,
                epic: 0,
                legendary: 0
            }
        };

        for (const type of Object.keys(this.categorizedItems || {})) {
            const items = this.categorizedItems[type] || [];
            stats.totalItems += items.length;
            stats.byType[type] = items.length;

            items.forEach(item => {
                const quality = String(item.itemQuality || 'common').toLowerCase();
                if (stats.byQuality[quality] !== undefined) {
                    stats.byQuality[quality] += 1;
                }
            });
        }

        return stats;
    }

    setFilter(filter) {
        this.filter = { ...this.filter, ...filter };
        this.currentItems = this.applyFilter();
        return this.currentItems;
    }
}

export const inventoryService = new InventoryService();
export default inventoryService;
