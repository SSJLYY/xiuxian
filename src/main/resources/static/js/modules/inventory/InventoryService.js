/**
 * 背包模块 - 业务逻辑层
 */
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

    /**
     * 加载背包物品
     */
    async loadInventoryItems() {
        try {
            const response = await gameAPI.getInventoryCategorized();
            if (response.success) {
                this.categorizedItems = response.data;
                this.applyFilter();
                return response.data;
            } else {
                toast.error('获取背包物品失败：' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('获取背包物品失败：' + error.message);
            throw error;
        }
    }
        } catch (error) {
            toast.error('获取背包物品失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 应用过滤器
     */
    applyFilter() {
        let allItems = [];
        for (const type in this.categorizedItems) {
            allItems = allItems.concat(this.categorizedItems[type]);
        }

        // 类型过滤
        if (this.filter.type) {
            allItems = allItems.filter(item => item.itemType === this.filter.type);
        }

        // 搜索过滤
        if (this.filter.searchTerm) {
            allItems = allItems.filter(item =>
                item.itemName.toLowerCase().includes(this.filter.searchTerm.toLowerCase()) ||
                item.itemDescription.toLowerCase().includes(this.filter.searchTerm.toLowerCase())
            );
        }

        // 排序
        allItems.sort((a, b) => {
            let comparison = 0;
            const sortBy = this.filter.sortBy;

            switch (sortBy) {
                case 'quality':
                    comparison = a.itemQuality - b.itemQuality;
                    break;
                case 'quantity':
                    comparison = a.quantity - b.quantity;
                    break;
                case 'created':
                    comparison = new Date(a.createdAt) - new Date(b.createdAt);
                    break;
                default:
                    const typeOrder = { '装备': 1, '消耗品': 2, '材料': 3, '任务物品': 4 };
                    const aTypeOrder = typeOrder[a.itemType] || 5;
                    const bTypeOrder = typeOrder[b.itemType] || 5;
                    comparison = aTypeOrder - bTypeOrder || a.itemQuality - b.itemQuality;
            }

            return this.filter.order === 'desc' ? -comparison : comparison;
        });

        this.currentItems = allItems;
        return allItems;
    }

    /**
     * 使用物品
     */
    async useItem(itemId) {
        try {
            const response = await gameAPI.useItem(itemId);
            if (response.success) {
                toast.success('使用物品成功');
                await this.loadInventoryItems();
                return response.data;
            } else {
                toast.error('使用物品失败：' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('使用物品失败：' + error.message);
            throw error;
        }
    }

    async equipItem(itemId) {
        try {
            const response = await gameAPI.equipItem(itemId);
            if (response.success) {
                toast.success('装备成功');
                await this.loadInventoryItems();
                return response.data;
            } else {
                toast.error('装备失败：' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('装备失败：' + error.message);
            throw error;
        }
    }

    async unequipItem(itemId) {
        try {
            const response = await gameAPI.unequipItem(itemId);
            if (response.success) {
                toast.success('卸下成功');
                await this.loadInventoryItems();
                return response.data;
            } else {
                toast.error('卸下失败：' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('卸下失败：' + error.message);
            throw error;
        }
    }

    async sellItem(itemId, quantity = 1) {
        try {
            const response = await gameAPI.sellItem(itemId, quantity);
            if (response.success) {
                toast.success(`出售成功，获得 ${response.data.spiritStones} 灵石`);
                await this.loadInventoryItems();
                return response.data;
            } else {
                toast.error('出售失败：' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('出售失败：' + error.message);
            throw error;
        }
    }

    async discardItem(itemId, quantity = 1) {
        if (!confirm('确定要丢弃这个物品吗？此操作不可恢复!')) {
            return;
        }

        try {
            const response = await gameAPI.discardItem(itemId, quantity);
            if (response.success) {
                toast.success('丢弃成功');
                await this.loadInventoryItems();
                return response.data;
            } else {
                toast.error('丢弃失败：' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('丢弃失败：' + error.message);
            throw error;
        }
    }
        } catch (error) {
            toast.error('使用物品失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 装备物品
     */
    async equipItem(itemId) {
        try {
            const response = await gameAPI.inventory.equipItem(itemId);
            if (response.success) {
                toast.success('装备成功');
                await this.loadInventoryItems();
                return response.data;
            } else {
                toast.error('装备失败: ' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('装备失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 卸下装备
     */
    async unequipItem(itemId) {
        try {
            const response = await gameAPI.inventory.unequipItem(itemId);
            if (response.success) {
                toast.success('卸下成功');
                await this.loadInventoryItems();
                return response.data;
            } else {
                toast.error('卸下失败: ' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('卸下失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 出售物品
     */
    async sellItem(itemId, quantity = 1) {
        try {
            const response = await gameAPI.inventory.sellItem(itemId, quantity);
            if (response.success) {
                toast.success(`出售成功,获得 ${response.data.spiritStones} 灵石`);
                await this.loadInventoryItems();
                return response.data;
            } else {
                toast.error('出售失败: ' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('出售失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 丢弃物品
     */
    async discardItem(itemId, quantity = 1) {
        if (!confirm('确定要丢弃这个物品吗?此操作不可恢复!')) {
            return;
        }

        try {
            const response = await gameAPI.inventory.discardItem(itemId, quantity);
            if (response.success) {
                toast.success('丢弃成功');
                await this.loadInventoryItems();
                return response.data;
            } else {
                toast.error('丢弃失败: ' + response.message);
                throw new Error(response.message);
            }
        } catch (error) {
            toast.error('丢弃失败: ' + error.message);
            throw error;
        }
    }

    /**
     * 获取背包统计
     */
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

        for (const type in this.categorizedItems) {
            const items = this.categorizedItems[type];
            stats.totalItems += items.length;
            stats.byType[type] = items.length;

            items.forEach(item => {
                const quality = item.itemQuality?.toLowerCase() || 'common';
                if (stats.byQuality[quality] !== undefined) {
                    stats.byQuality[quality]++;
                }
            });
        }

        return stats;
    }

    /**
     * 设置过滤器
     */
    setFilter(filter) {
        this.filter = { ...this.filter, ...filter };
        this.applyFilter();
    }
}

export const inventoryService = new InventoryService();
