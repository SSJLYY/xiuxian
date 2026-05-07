import { inventoryService } from './InventoryService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { modal } from '../../components/Modal.js';

export class InventoryUI {
    constructor() {
        this.currentItems = [];
        this.selectedItem = null;
    }

    init() {
        this.setupElements();
        this.bindEvents();
        this.loadItems();
    }

    setupElements() {
        this.elements = {
            itemsContainer: document.getElementById('itemsContainer'),
            typeFilter: document.getElementById('typeFilter'),
            searchInput: document.getElementById('searchInput'),
            sortSelect: document.getElementById('sortSelect'),
            orderSelect: document.getElementById('orderSelect'),
            totalItems: document.getElementById('totalItems'),
            capacity: document.getElementById('capacity'),
            inventoryStats: document.getElementById('inventoryStats')
        };
    }

    bindEvents() {
        if (this.elements.typeFilter) {
            this.elements.typeFilter.addEventListener('change', () => this.handleFilterChange());
        }

        if (this.elements.searchInput) {
            this.elements.searchInput.addEventListener('input', () => this.handleFilterChange());
        }

        if (this.elements.sortSelect) {
            this.elements.sortSelect.addEventListener('change', () => this.handleFilterChange());
        }

        if (this.elements.orderSelect) {
            this.elements.orderSelect.addEventListener('change', () => this.handleFilterChange());
        }
    }

    handleFilterChange() {
        this.currentItems = inventoryService.setFilter({
            type: this.elements.typeFilter?.value || '',
            searchTerm: this.elements.searchInput?.value || '',
            sortBy: this.elements.sortSelect?.value || 'quality',
            order: this.elements.orderSelect?.value || 'desc'
        });

        this.renderItems();
    }

    async loadItems() {
        loading.show();
        try {
            await inventoryService.loadInventoryItems();
            this.currentItems = inventoryService.currentItems;
            this.renderItems();
            this.updateStats();
        } catch (error) {
            toast.error('加载背包失败');
        } finally {
            loading.hide();
        }
    }

    renderItems() {
        if (!this.elements.itemsContainer) return;

        if (this.currentItems.length === 0) {
            this.elements.itemsContainer.innerHTML = `
                <div class="empty-state">
                    <p>背包为空</p>
                </div>
            `;
            return;
        }

        this.elements.itemsContainer.innerHTML = this.currentItems.map(item => `
            <div class="inventory-item ${item.itemQuality || 'common'}" data-item-id="${item.playerItemId}">
                <div class="item-icon">
                    <img src="${item.itemIcon || '/images/items/default.png'}" alt="${item.itemName}">
                </div>
                <div class="item-info">
                    <div class="item-name">${item.itemName}</div>
                    <div class="item-type">${item.itemType || item.itemTypeCode || ''}</div>
                    <div class="item-quantity">x${item.quantity}</div>
                </div>
                <div class="item-actions">
                    ${this.renderItemActions(item)}
                </div>
            </div>
        `).join('');

        this.elements.itemsContainer.querySelectorAll('.inventory-item').forEach(el => {
            el.addEventListener('click', e => {
                if (e.target.closest('.item-actions')) return;
                this.showItemDetail(el.dataset.itemId);
            });
        });
    }

    renderItemActions(item) {
        const actions = [];

        if (item.usable || item.itemTypeCode === 'CONSUMABLE' || item.itemType === '消耗品') {
            actions.push(`<button class="btn btn-sm btn-primary" data-action="use" data-item-id="${item.playerItemId}">使用</button>`);
        }

        actions.push(`<button class="btn btn-sm btn-warning" data-action="sell" data-item-id="${item.playerItemId}">出售</button>`);

        return actions.join('');
    }

    async showItemDetail(playerItemId) {
        const item = this.currentItems.find(i => i.playerItemId == playerItemId);
        if (!item) return;

        const detailHtml = `
            <div class="item-detail">
                <div class="item-detail-header">
                    <img src="${item.itemIcon || '/images/items/default.png'}" alt="${item.itemName}">
                    <h3>${item.itemName}</h3>
                </div>
                <div class="item-detail-body">
                    <p><strong>类型:</strong> ${item.itemType || item.itemTypeCode || ''}</p>
                    <p><strong>品质:</strong> ${item.itemQuality}</p>
                    <p><strong>数量:</strong> ${item.quantity}</p>
                    <p><strong>描述:</strong> ${item.itemDescription}</p>
                    ${item.itemStats ? this.renderItemStats(item.itemStats) : ''}
                </div>
                <div class="item-detail-actions">
                    ${this.renderItemActions(item)}
                </div>
            </div>
        `;

        modal.show({
            title: '物品详情',
            content: detailHtml,
            onConfirm: () => modal.hide()
        });

        this.bindItemActionButtons();
    }

    renderItemStats(stats) {
        return `
            <div class="item-stats">
                <h4>属性加成</h4>
                ${Object.entries(stats).map(([key, value]) => `
                    <div class="stat-item">
                        <span>${this.translateStat(key)}:</span>
                        <span>${value > 0 ? '+' : ''}${value}</span>
                    </div>
                `).join('')}
            </div>
        `;
    }

    translateStat(key) {
        const statMap = {
            attack: '攻击力',
            defense: '防御力',
            health: '生命值',
            mana: '法力值',
            speed: '速度'
        };
        return statMap[key] || key;
    }

    bindItemActionButtons() {
        const modalContainer = document.querySelector('.modal');
        if (!modalContainer) return;

        modalContainer.querySelectorAll('.btn').forEach(btn => {
            btn.addEventListener('click', async e => {
                const action = e.target.dataset.action;
                const itemId = parseInt(e.target.dataset.itemId, 10);
                await this.handleItemAction(action, itemId);
            });
        });
    }

    async handleItemAction(action, itemId) {
        loading.show();
        try {
            switch (action) {
                case 'use':
                    await inventoryService.useItem(itemId);
                    break;
                case 'equip':
                    await inventoryService.equipItem(itemId);
                    break;
                case 'sell':
                    await inventoryService.sellItem(itemId);
                    break;
                case 'discard':
                    await inventoryService.discardItem(itemId);
                    break;
                default:
                    break;
            }
            modal.hide();
            await this.loadItems();
        } catch (error) {
            toast.error(error?.message || '操作失败');
        } finally {
            loading.hide();
        }
    }

    updateStats() {
        const stats = inventoryService.getInventoryStats();

        if (this.elements.totalItems) {
            this.elements.totalItems.textContent = stats.totalItems;
        }

        if (this.elements.capacity) {
            this.elements.capacity.textContent = `${stats.totalItems}/100`;
        }

        if (this.elements.inventoryStats) {
            this.elements.inventoryStats.innerHTML = `
                <div class="stat-group">
                    <h4>按类型统计</h4>
                    ${Object.entries(stats.byType).map(([type, count]) => `
                        <div class="stat-item">
                            <span>${type}:</span>
                            <span>${count}</span>
                        </div>
                    `).join('')}
                </div>
                <div class="stat-group">
                    <h4>按品质统计</h4>
                    ${Object.entries(stats.byQuality).map(([quality, count]) => `
                        <div class="stat-item ${quality}">
                            <span>${this.translateQuality(quality)}:</span>
                            <span>${count}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }
    }

    translateQuality(quality) {
        const qualityMap = {
            common: '普通',
            uncommon: '优秀',
            rare: '稀有',
            epic: '史诗',
            legendary: '传说'
        };
        return qualityMap[quality] || quality;
    }
}

export const inventoryUI = new InventoryUI();
