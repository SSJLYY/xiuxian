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

    normalizeQualityLabel(quality) {
        const mapping = {
            1: 'common',
            2: 'uncommon',
            3: 'rare',
            4: 'epic',
            5: 'legendary'
        };
        return mapping[Number(quality)] || 'common';
    }

    normalizeItemType(rawType, fallbackType = '') {
        const source = String(rawType || fallbackType || '').trim();
        const upper = source.toUpperCase();
        const mappings = {
            EQUIPMENT: { code: 'EQUIPMENT', name: '装备' },
            CONSUMABLE: { code: 'CONSUMABLE', name: '消耗品' },
            MATERIAL: { code: 'MATERIAL', name: '材料' },
            QUEST: { code: 'QUEST', name: '任务物品' },
            QUEST_ITEM: { code: 'QUEST', name: '任务物品' },
            SKILL: { code: 'SKILL', name: '技能' },
            PET: { code: 'PET', name: '宠物' },
            装备: { code: 'EQUIPMENT', name: '装备' },
            消耗品: { code: 'CONSUMABLE', name: '消耗品' },
            材料: { code: 'MATERIAL', name: '材料' },
            任务物品: { code: 'QUEST', name: '任务物品' },
            技能: { code: 'SKILL', name: '技能' },
            宠物: { code: 'PET', name: '宠物' }
        };
        return mappings[upper] || mappings[source] || {
            code: upper || source || 'UNKNOWN',
            name: source || '未知'
        };
    }

    normalizeItem(item, category = '') {
        const itemType = this.normalizeItemType(item?.itemType, category);
        return {
            ...item,
            id: item?.id,
            playerItemId: item?.id,
            itemId: item?.itemId ?? item?.id,
            itemName: item?.itemName || '未知物品',
            itemDescription: item?.itemDescription || '',
            itemType: itemType.name,
            itemTypeCode: itemType.code,
            itemQualityLevel: Number(item?.itemQuality ?? 1),
            itemQuality: this.normalizeQualityLabel(item?.itemQuality),
            itemIcon: '/images/items/default.png',
            itemStats: null
        };
    }

    async loadInventoryItems() {
        try {
            const response = await gameAPI.getInventoryCategorized();
            if (!response?.success) {
                throw new Error(response?.message || '获取背包物品失败');
            }
            this.categorizedItems = Object.entries(response.data || {}).reduce((acc, [key, items]) => {
                acc[key] = (items || []).map(item => this.normalizeItem(item, key));
                return acc;
            }, {});
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
            const normalizedFilter = this.normalizeItemType(this.filter.type);
            allItems = allItems.filter(item =>
                item.itemType === this.filter.type ||
                item.itemTypeCode === normalizedFilter.code
            );
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
                    const typeOrder = { EQUIPMENT: 1, CONSUMABLE: 2, MATERIAL: 3, QUEST: 4, SKILL: 5, PET: 6 };
                    const aTypeOrder = typeOrder[a.itemTypeCode] || 99;
                    const bTypeOrder = typeOrder[b.itemTypeCode] || 99;
                    comparison = aTypeOrder - bTypeOrder;
                }
            }

            return this.filter.order === 'desc' ? -comparison : comparison;
        });

        this.currentItems = allItems;
        return allItems;
    }

    async useItem(playerItemId) {
        try {
            const response = await gameAPI.useItem(playerItemId);
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

    async equipItem() {
        toast.error('当前版本请转到装备页面进行穿戴');
        throw new Error('请转到装备页面进行穿戴');
    }

    async unequipItem(playerEquipmentId) {
        try {
            const response = await gameAPI.unequipItem(playerEquipmentId);
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

    async sellItem(playerItemId, quantity = 1) {
        if (!playerItemId || quantity < 1) {
            toast.error('参数无效');
            return null;
        }

        try {
            const response = await gameAPI.sellItem(playerItemId, quantity);
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

    async discardItem(playerItemId, quantity = 1) {
        if (!playerItemId || quantity < 1) {
            toast.error('参数无效');
            return null;
        }

        if (!confirm('确定要丢弃该物品吗？此操作不可恢复。')) {
            return null;
        }

        try {
            const response = await gameAPI.discardItem(playerItemId, quantity);
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
