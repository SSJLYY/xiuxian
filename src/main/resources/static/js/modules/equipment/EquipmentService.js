import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class EquipmentService {
    constructor() {
        this.currentEquipment = {};
        this.equipmentSlots = ['weapon', 'helmet', 'armor', 'accessory'];
    }

    async loadEquipment() {
        try {
            const response = await gameAPI.getEquipment();
            if (!response?.success) {
                throw new Error(response?.message || '加载装备失败');
            }
            this.currentEquipment = response.data || {};
            return this.currentEquipment;
        } catch (error) {
            toast.error('加载装备失败: ' + error.message);
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
            await this.loadEquipment();
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
