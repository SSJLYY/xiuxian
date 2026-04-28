/**
 * 商城模块 - UI渲染层
 */
import { shopService } from './ShopService.js';
import { toast } from '../../components/Toast.js';
import { loading } from '../../components/Loading.js';
import { modal } from '../../components/Modal.js';
import { formatUtils } from '../../core/utils/FormatUtils.js';
import { escapeHtml } from '../../core/utils/Security.js';

export class ShopUI {
    constructor() {
        this.currentCategory = 'all';
    }

    init() {
        this.setupElements();
        this.bindEvents();
        this.loadShopData();
    }

    setupElements() {
        this.elements = {
            shopContainer: document.getElementById('shopContainer'),
            categoryFilter: document.getElementById('categoryFilter'),
            searchInput: document.getElementById('searchInput'),
            myOrdersContainer: document.getElementById('myOrdersContainer'),
            shopTabs: document.querySelectorAll('[data-tab="shop"]')
        };
    }

    bindEvents() {
        // 标签页切换
        this.elements.shopTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                this.switchTab(e.target.dataset.shopTab);
            });
        });

        // 分类筛选
        if (this.elements.categoryFilter) {
            this.elements.categoryFilter.addEventListener('change', (e) => {
                this.currentCategory = e.target.value;
                this.loadShopItems();
            });
        }

        // 搜索
        if (this.elements.searchInput) {
            this.elements.searchInput.addEventListener('input', (e) => {
                this.filterItems(e.target.value);
            });
        }
    }

    switchTab(tabName) {
        this.elements.shopTabs.forEach(tab => {
            tab.classList.toggle('active', tab.dataset.shopTab === tabName);
        });

        if (tabName === 'shop') {
            this.elements.shopContainer.style.display = 'block';
            this.elements.myOrdersContainer.style.display = 'none';
        } else {
            this.elements.shopContainer.style.display = 'none';
            this.elements.myOrdersContainer.style.display = 'block';
        }
    }

    async loadShopData() {
        loading.show();
        try {
            await Promise.all([
                shopService.getShopItems(this.currentCategory),
                shopService.getMyOrders()
            ]);
            this.renderShopItems();
            this.renderOrders();
        } catch (error) {
            toast.error('加载商城数据失败');
        } finally {
            loading.hide();
        }
    }

    async loadShopItems() {
        loading.show();
        try {
            await shopService.getShopItems(this.currentCategory);
            this.renderShopItems();
        } catch (error) {
            toast.error('加载商品失败');
        } finally {
            loading.hide();
        }
    }

    renderShopItems(items = shopService.shopItems) {
        const container = this.elements.shopContainer;
        if (!container) return;

        if (items.length === 0) {
            container.innerHTML = '<p>暂无商品</p>';
            return;
        }

        container.innerHTML = `
            <div class="shop-items-grid">
                ${items.map(item => `
                    <div class="shop-item ${item.quality}">
                        <div class="item-image">
                            <img src="${item.image || '/images/items/default.png'}" alt="${escapeHtml(item.name)}">
                        </div>
                        <div class="item-info">
                            <h4>${escapeHtml(item.name)}</h4>
                            <p class="item-desc">${escapeHtml(item.description)}</p>
                            <div class="item-stats">
                                <span class="category">${escapeHtml(item.category)}</span>
                                <span class="stock">库存: ${escapeHtml(item.stock)}</span>
                            </div>
                        </div>
                        <div class="item-price">
                            <span class="price">${formatUtils.formatSpiritStones(item.price)}</span>
                        </div>
                        <div class="item-actions">
                            <button class="btn btn-primary" data-action="buy" data-item-id="${item.id}">购买</button>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;

        // 绑定购买按钮
        container.querySelectorAll('[data-action="buy"]').forEach(btn => {
            btn.addEventListener('click', (e) => this.showBuyDialog(e.target.dataset.itemId));
        });
    }

    filterItems(searchTerm) {
        if (!searchTerm) {
            this.renderShopItems();
            return;
        }

        const filtered = shopService.shopItems.filter(item =>
            String(item.name || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
            String(item.description || '').toLowerCase().includes(searchTerm.toLowerCase())
        );
        this.renderShopItems(filtered);
    }

    showBuyDialog(itemId) {
        const item = shopService.getItemById(itemId);
        if (!item) return;

        const buyHtml = `
            <div class="buy-dialog">
                <div class="item-preview">
                    <img src="${item.image || '/images/items/default.png'}" alt="${escapeHtml(item.name)}">
                    <h4>${escapeHtml(item.name)}</h4>
                    <p>${escapeHtml(item.description)}</p>
                </div>
                <div class="quantity-selector">
                    <label>购买数量:</label>
                    <div class="quantity-control">
                        <button class="btn btn-sm" id="decreaseQty">-</button>
                        <input type="number" id="buyQty" value="1" min="1" max="${item.stock}">
                        <button class="btn btn-sm" id="increaseQty">+</button>
                    </div>
                </div>
                <div class="total-price">
                    <span>总价: </span>
                    <span id="totalPrice">${formatUtils.formatSpiritStones(item.price)}</span>
                </div>
            </div>
        `;

        modal.show({
            title: '购买商品',
            content: buyHtml,
            onConfirm: () => {
                const qty = parseInt(document.getElementById('buyQty').value);
                this.handleBuy(itemId, qty);
            }
        });

        // 绑定数量控制
        const qtyInput = document.getElementById('buyQty');
        const totalPrice = document.getElementById('totalPrice');

        const updateTotalPrice = () => {
            const qty = parseInt(qtyInput.value) || 0;
            totalPrice.textContent = formatUtils.formatSpiritStones(item.price * qty);
        };

        document.getElementById('decreaseQty').addEventListener('click', () => {
            if (parseInt(qtyInput.value) > 1) {
                qtyInput.value = parseInt(qtyInput.value) - 1;
                updateTotalPrice();
            }
        });

        document.getElementById('increaseQty').addEventListener('click', () => {
            if (parseInt(qtyInput.value) < item.stock) {
                qtyInput.value = parseInt(qtyInput.value) + 1;
                updateTotalPrice();
            }
        });

        qtyInput.addEventListener('input', updateTotalPrice);
    }

    async handleBuy(itemId, quantity) {
        loading.show();
        try {
            await shopService.buyItem(itemId, quantity);
            await this.loadShopData();
            modal.hide();
        } catch (error) {
            toast.error('购买失败');
        } finally {
            loading.hide();
        }
    }

    renderOrders() {
        const container = this.elements.myOrdersContainer;
        if (!container) return;

        if (shopService.myOrders.length === 0) {
            container.innerHTML = '<p>暂无订单</p>';
            return;
        }

        container.innerHTML = `
            <div class="orders-list">
                ${shopService.myOrders.map(order => `
                    <div class="order-card">
                        <div class="order-info">
                            <div class="order-id">订单号: ${escapeHtml(order.orderId)}</div>
                            <div class="order-items">
                                ${order.items.map(item => `
                                    <div class="order-item">
                                        <span>${escapeHtml(item.name)}</span>
                                        <span>x${escapeHtml(item.quantity)}</span>
                                    </div>
                                `).join('')}
                            </div>
                            <div class="order-total">总价: ${formatUtils.formatSpiritStones(order.total)}</div>
                        </div>
                        <div class="order-status">
                            <span class="status ${order.status}">${this.translateStatus(order.status)}</span>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    translateStatus(status) {
        const statusMap = {
            'pending': '待发货',
            'completed': '已完成',
            'cancelled': '已取消'
        };
        return statusMap[status] || status;
    }
}

export const shopUI = new ShopUI();
