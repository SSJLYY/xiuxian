/**
 * 装备模块 - 业务逻辑层
 */
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

export class EquipmentService {
    constructor() {
        this.currentEquipment = {};
        this.equipmentSlots = ['weapon', 'helmet', 'armor', 'accessory'];
    }

    async loadEquipment() {
        try {
            const response = await gameAPI.player.getEquipment();
            if (response.success) {
                this.currentEquipment = response.data;
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('加载装备失败: ' + error.message);
            throw error;
        }
    }

    async equipItem(itemId, slot) {
        try {
            const response = await gameAPI.equipment.equip(itemId, slot);
            if (response.success) {
                toast.success('装备成功');
                await this.loadEquipment();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('装备失败: ' + error.message);
            throw error;
        }
    }

    async unequipItem(slot) {
        try {
            const response = await gameAPI.equipment.unequip(slot);
            if (response.success) {
                toast.success('卸下成功');
                await this.loadEquipment();
                return response.data;
            }
            throw new Error(response.message);
        } catch (error) {
            toast.error('卸下失败: ' + error.message);
            throw error;
        }
    }

    getEquipmentBySlot(slot) {
        return this.currentEquipment[slot] || null;
    }
}

export const equipmentService = new EquipmentService();
