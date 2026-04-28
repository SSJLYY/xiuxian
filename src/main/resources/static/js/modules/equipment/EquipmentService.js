import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class EquipmentService {
    constructor() {
        this.currentEquipment = {};
        this.equipmentSlots = ['weapon', 'helmet', 'armor', 'accessory'];
    }

    normalizeSlot(item) {
        const rawSlot = String(item?.slot || item?.type || '').toLowerCase();
        if (['weapon', 'helmet', 'armor', 'accessory'].includes(rawSlot)) {
            return rawSlot;
        }
        if (rawSlot.includes('weapon') || rawSlot.includes('武器')) return 'weapon';
        if (rawSlot.includes('helmet') || rawSlot.includes('头盔')) return 'helmet';
        if (rawSlot.includes('armor') || rawSlot.includes('护甲') || rawSlot.includes('防具')) return 'armor';
        if (rawSlot.includes('accessory') || rawSlot.includes('饰品')) return 'accessory';
        return null;
    }

    normalizeEquipment(item) {
        const slot = this.normalizeSlot(item);
        return {
            ...item,
            id: item?.id,
            slot,
            itemName: item?.name || item?.itemName || '未知装备',
            itemDescription: item?.description || item?.itemDescription || '',
            itemQuality: item?.quality ?? item?.itemQuality ?? 1,
            itemIcon: item?.icon || '/images/items/default.png'
        };
    }

    async loadEquipment() {
        try {
            const response = await gameAPI.getEquippedEquipmentDetails();
            if (!response?.success) {
                throw new Error(response?.message || '加载装备失败');
            }
            const equippedItems = Array.isArray(response.data) ? response.data : [];
            this.currentEquipment = equippedItems.reduce((acc, item) => {
                const normalized = this.normalizeEquipment(item);
                if (normalized.slot) {
                    acc[normalized.slot] = normalized;
                }
                return acc;
            }, {});
            return this.currentEquipment;
        } catch (error) {
            toast.error('加载装备失败: ' + error.message);
            throw error;
        }
    }

    async equipItem(playerEquipmentId, slot = null) {
        try {
            const response = await gameAPI.equipItem(playerEquipmentId, slot);
            if (!response?.success) {
                throw new Error(response?.message || '装备失败');
            }
            toast.success('装备成功');
            await this.loadEquipment();
            return response.data;
        } catch (error) {
            toast.error('装备失败: ' + error.message);
            throw error;
        }
    }

    async unequipItem(playerEquipmentId) {
        try {
            const response = await gameAPI.unequipItem(playerEquipmentId);
            if (!response?.success) {
                throw new Error(response?.message || '卸下失败');
            }
            toast.success('卸下成功');
            await this.loadEquipment();
            return response.data;
        } catch (error) {
            toast.error('卸下失败: ' + error.message);
            throw error;
        }
    }

    getEquipmentBySlot(slot) {
        return this.currentEquipment?.[slot] || null;
    }
}

export const equipmentService = new EquipmentService();
export default equipmentService;
