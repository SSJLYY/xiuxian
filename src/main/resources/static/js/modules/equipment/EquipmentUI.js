/**
 * 装备模块 - UI 渲染层
 */
import { equipmentService } from './EquipmentService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { escapeHtml } from '../../core/utils/Security.js';

export class EquipmentUI {
    constructor() {
        this.equipmentSlots = ['weapon', 'helmet', 'armor', 'accessory'];
    }

    init() {
        this.setupElements();
        this.bindEvents();
        this.loadEquipment();
    }

    setupElements() {
        this.elements = {
            equipmentContainer: document.getElementById('equipmentContainer'),
            equipmentStats: document.getElementById('equipmentStats')
        };
    }

    bindEvents() {
        if (this.elements.equipmentContainer) {
            this.elements.equipmentContainer.addEventListener('click', e => {
                const slotElement = e.target.closest('[data-slot]');
                const slot = slotElement?.dataset?.slot;
                if (slot && this.elements.equipmentContainer.contains(slotElement)) {
                    this.handleSlotClick(slot);
                }
            });
        }
    }

    async loadEquipment() {
        loading.show();
        try {
            await equipmentService.loadEquipment();
            this.renderEquipment();
        } catch {
            toast.error('加载装备失败');
        } finally {
            loading.hide();
        }
    }

    renderEquipment() {
        if (!this.elements.equipmentContainer) {
            return;
        }

        const slotNames = {
            weapon: '武器',
            helmet: '头盔',
            armor: '护甲',
            accessory: '饰品'
        };

        this.elements.equipmentContainer.innerHTML = this.equipmentSlots.map(slot => {
            const item = equipmentService.getEquipmentBySlot(slot);
            return `
                <div class="equipment-slot ${slot} ${item ? 'equipped' : 'empty'}" data-slot="${slot}">
                    <div class="slot-label">${slotNames[slot]}</div>
                    <div class="slot-content">
                        ${item ? this.renderEquippedItem(item) : '<span class="empty-text">空</span>'}
                    </div>
                </div>
            `;
        }).join('');
    }

    renderEquippedItem(item) {
        return `
            <div class="equipped-item ${item.itemQuality}">
                <img src="${item.itemIcon}" alt="${escapeHtml(item.itemName)}">
                <div class="item-tooltip">${escapeHtml(item.itemName)}</div>
            </div>
        `;
    }

    async handleSlotClick(slot) {
        const item = equipmentService.getEquipmentBySlot(slot);
        if (item) {
            if (confirm(`要卸下 ${item.itemName} 吗？`)) {
                await equipmentService.unequipItem(item.id);
                this.renderEquipment();
            }
        } else {
            toast.info('当前版本暂不支持从此页面直接穿戴装备');
        }
    }
}

export const equipmentUI = new EquipmentUI();
