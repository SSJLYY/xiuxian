# 前端迁移快速指南

> 5分钟了解如何将旧代码迁移到新架构

## 🎯 迁移目标

将平铺的JS文件迁移到模块化架构,实现:
- ✅ 职责分离(Service + UI)
- ✅ 模块化(import/export)
- ✅ 代码复用(组件层)
- ✅ 易于维护(清晰的目录结构)

## 📋 迁移步骤

### 步骤1: 识别要迁移的模块

在 `js/` 目录下找到要迁移的文件,例如:
- `inventory.js` → 背包模块
- `combat.js` → 战斗模块
- `skills.js` → 技能模块

### 步骤2: 创建模块目录

```bash
# 在js/modules/下创建新模块目录
mkdir js/modules/inventory
```

### 步骤3: 分离Service层和UI层

#### 旧代码示例 (inventory.js)
```javascript
// 旧代码: 业务逻辑和UI混杂
async function loadInventory() {
    const response = await fetch('/api/inventory/items', {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        }
    });
    const data = await response.json();

    const list = document.getElementById('inventoryList');
    list.innerHTML = '';

    data.items.forEach(item => {
        const div = document.createElement('div');
        div.innerHTML = `<h3>${item.name}</h3><p>${item.description}</p>`;
        list.appendChild(div);
    });
}
```

#### 新代码结构

**Step 3a: 创建 InventoryService.js**
```javascript
// js/modules/inventory/InventoryService.js
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

class InventoryService {
    /**
     * 获取背包物品列表
     */
    async getInventory() {
        try {
            const response = await gameAPI.getInventory();
            if (!response.success) {
                throw new Error(response.message);
            }
            return response.data;
        } catch (error) {
            console.error('获取背包物品失败:', error);
            toast.error('获取背包物品失败');
            throw error;
        }
    }

    /**
     * 使用物品
     */
    async useItem(itemId) {
        try {
            const response = await gameAPI.useItem(itemId);
            if (!response.success) {
                throw new Error(response.message);
            }
            toast.success('物品使用成功');
            return response.data;
        } catch (error) {
            console.error('使用物品失败:', error);
            toast.error(error.message);
            throw error;
        }
    }

    /**
     * 出售物品
     */
    async sellItem(itemId, count = 1) {
        try {
            const response = await gameAPI.sellItem(itemId, count);
            if (!response.success) {
                throw new Error(response.message);
            }
            toast.success('物品出售成功');
            return response.data;
        } catch (error) {
            console.error('出售物品失败:', error);
            toast.error(error.message);
            throw error;
        }
    }

    /**
     * 格式化物品信息
     */
    formatItem(item) {
        return {
            id: item.id,
            name: item.name,
            description: item.description,
            icon: item.icon,
            count: item.count,
            quality: this.getQualityName(item.quality),
            price: FormatUtils.formatSpiritStones(item.price)
        };
    }

    /**
     * 获取品质名称
     */
    getQualityName(quality) {
        const qualityMap = {
            1: '普通',
            2: '优秀',
            3: '稀有',
            4: '史诗',
            5: '传说'
        };
        return qualityMap[quality] || '普通';
    }
}

const inventoryService = new InventoryService();

export { inventoryService };
```

**Step 3b: 创建 InventoryUI.js**
```javascript
// js/modules/inventory/InventoryUI.js
import { inventoryService } from './InventoryService.js';
import { loading } from '../../components/Loading.js';
import { escapeHtml } from '../../core/utils/Security.js';

class InventoryUI {
    constructor() {
        this.currentItems = [];
        this.isInitialized = false;
    }

    /**
     * 初始化
     */
    async init() {
        if (this.isInitialized) return;

        try {
            // 绑定UI元素
            this.bindElements();

            // 加载背包数据
            await this.loadInventory();

            // 绑定事件
            this.bindEvents();

            this.isInitialized = true;
            console.log('背包模块初始化成功');
        } catch (error) {
            console.error('背包模块初始化失败:', error);
        }
    }

    /**
     * 绑定UI元素
     */
    bindElements() {
        this.elements = {
            inventoryList: document.getElementById('inventoryList'),
            emptyState: document.getElementById('inventoryEmpty')
        };
    }

    /**
     * 加载背包数据
     */
    async loadInventory() {
        try {
            loading.show(this.elements.inventoryList, '加载中...');

            // 调用Service层
            const data = await inventoryService.getInventory();
            this.currentItems = data.items || [];

            // 格式化数据
            const displayItems = this.currentItems.map(item =>
                inventoryService.formatItem(item)
            );

            // 更新UI
            this.renderItems(displayItems);

        } catch (error) {
            console.error('加载背包失败:', error);
            this.showErrorState();
        } finally {
            loading.hide(this.elements.inventoryList);
        }
    }

    /**
     * 渲染物品列表
     */
    renderItems(items) {
        if (!items || items.length === 0) {
            this.showEmptyState();
            return;
        }

        this.showListState();

        const html = items.map(item => `
            <div class="inventory-item" data-item-id="${item.id}">
                <div class="item-icon">
                    <img src="${escapeHtml(item.icon)}" alt="${escapeHtml(item.name)}">
                </div>
                <div class="item-info">
                    <h3 class="item-name quality-${item.quality}">
                        ${escapeHtml(item.name)}
                        <span class="item-count">x${item.count}</span>
                    </h3>
                    <p class="item-desc">${escapeHtml(item.description)}</p>
                    <p class="item-price">${item.price} 灵石</p>
                </div>
                <div class="item-actions">
                    <button class="btn btn-sm btn-primary use-btn">
                        <i class="fas fa-hand"></i> 使用
                    </button>
                    <button class="btn btn-sm btn-secondary sell-btn">
                        <i class="fas fa-coins"></i> 出售
                    </button>
                </div>
            </div>
        `).join('');

        this.elements.inventoryList.innerHTML = html;
    }

    /**
     * 绑定事件
     */
    bindEvents() {
        // 使用物品
        const useButtons = this.elements.inventoryList?.querySelectorAll('.use-btn');
        useButtons?.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const itemEl = e.target.closest('.inventory-item');
                const itemId = itemEl.dataset.itemId;
                this.handleUseItem(itemId);
            });
        });

        // 出售物品
        const sellButtons = this.elements.inventoryList?.querySelectorAll('.sell-btn');
        sellButtons?.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const itemEl = e.target.closest('.inventory-item');
                const itemId = itemEl.dataset.itemId;
                this.handleSellItem(itemId);
            });
        });
    }

    /**
     * 处理使用物品
     */
    async handleUseItem(itemId) {
        try {
            loading.show(this.elements.inventoryList, '使用中...');
            await inventoryService.useItem(itemId);
            await this.loadInventory(); // 重新加载
        } catch (error) {
            console.error('使用物品失败:', error);
        } finally {
            loading.hide(this.elements.inventoryList);
        }
    }

    /**
     * 处理出售物品
     */
    async handleSellItem(itemId) {
        try {
            loading.show(this.elements.inventoryList, '出售中...');
            await inventoryService.sellItem(itemId);
            await this.loadInventory(); // 重新加载
        } catch (error) {
            console.error('出售物品失败:', error);
        } finally {
            loading.hide(this.elements.inventoryList);
        }
    }

    /**
     * 显示空状态
     */
    showEmptyState() {
        this.elements.inventoryList.style.display = 'none';
        if (this.elements.emptyState) {
            this.elements.emptyState.style.display = 'block';
        }
    }

    /**
     * 显示列表状态
     */
    showListState() {
        this.elements.inventoryList.style.display = 'block';
        if (this.elements.emptyState) {
            this.elements.emptyState.style.display = 'none';
        }
    }

    /**
     * 显示错误状态
     */
    showErrorState() {
        this.elements.inventoryList.innerHTML = `
            <div class="error-state">
                <i class="fas fa-exclamation-circle"></i>
                <p>加载失败,请稍后重试</p>
                <button class="btn btn-primary" onclick="location.reload()">
                    重新加载
                </button>
            </div>
        `;
    }

    /**
     * 刷新背包
     */
    async refresh() {
        await this.loadInventory();
    }

    /**
     * 销毁模块
     */
    destroy() {
        this.elements = null;
        this.currentItems = [];
        this.isInitialized = false;
    }
}

const inventoryUI = new InventoryUI();

export { inventoryUI };
```

**Step 3c: 创建 index.js**
```javascript
// js/modules/inventory/index.js
import { inventoryService } from './InventoryService.js';
import { inventoryUI } from './InventoryUI.js';

export {
    inventoryService,
    inventoryUI
};

export default {
    service: inventoryService,
    ui: inventoryUI
};
```

### 步骤4: 更新HTML页面

```html
<!-- 旧方式 -->
<script src="js/inventory.js"></script>

<!-- 新方式: 在main.js中动态导入 -->
<script type="module" src="js/main.js"></script>
```

```javascript
// 在App.js中注册模块
async initBusinessModules() {
    // 动态导入背包模块
    const { inventoryUI } = await import('./modules/inventory/index.js');
    await inventoryUI.init();
}
```

### 步骤5: 创建模块样式(可选)

```css
/* css/modules/inventory.css */
.inventory-item {
    display: flex;
    padding: 16px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    margin-bottom: 12px;
}

.item-icon img {
    width: 64px;
    height: 64px;
    border-radius: 8px;
}

.quality-5 {
    color: #ff9800; /* 传说 */
}

.quality-4 {
    color: #9c27b0; /* 史诗 */
}
```

## 🔄 迁移检查清单

在完成迁移后,检查以下项目:

- [ ] Service层只包含业务逻辑,不包含DOM操作
- [ ] UI层只包含DOM操作,不包含业务逻辑
- [ ] 使用ES6模块(import/export)
- [ ] 错误处理统一使用try/catch + toast.error
- [ ] 使用loading组件显示加载状态
- [ ] 使用escapeHtml防止XSS
- [ ] 所有公共方法都有注释
- [ ] 创建了index.js导出公共接口
- [ ] 更新了HTML页面引用
- [ ] 测试所有功能正常工作

## 📚 迁移模板

你可以复制以下模板快速创建新模块:

```javascript
// ModuleService.js 模板
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

class ModuleService {
    async loadData() {
        try {
            const response = await gameAPI.getModuleData();
            if (!response.success) {
                throw new Error(response.message);
            }
            return response.data;
        } catch (error) {
            console.error('加载数据失败:', error);
            toast.error('加载数据失败');
            throw error;
        }
    }
}

const moduleService = new ModuleService();
export { moduleService };
```

```javascript
// ModuleUI.js 模板
import { moduleService } from './ModuleService.js';
import { loading } from '../../components/Loading.js';

class ModuleUI {
    async init() {
        this.bindElements();
        await this.loadData();
        this.bindEvents();
    }

    bindElements() {
        this.elements = {
            container: document.getElementById('moduleContainer')
        };
    }

    async loadData() {
        loading.show(this.elements.container);
        try {
            const data = await moduleService.loadData();
            this.renderData(data);
        } finally {
            loading.hide(this.elements.container);
        }
    }

    renderData(data) {
        // 渲染数据
    }

    bindEvents() {
        // 绑定事件
    }
}

const moduleUI = new ModuleUI();
export { moduleUI };
```

```javascript
// index.js 模板
import { moduleService } from './ModuleService.js';
import { moduleUI } from './ModuleUI.js';

export { moduleService, moduleUI };
export default { service: moduleService, ui: moduleUI };
```

## 🎯 快速命令

```bash
# 创建新模块目录
mkdir js/modules/newmodule

# 创建Service层
# 复制并修改 ModuleService.js 模板

# 创建UI层
# 复制并修改 ModuleUI.js 模板

# 创建入口文件
# 复制 index.js 模板
```

## 💡 常见问题

### Q1: 旧代码要不要删除?
**A**: 暂时保留,等新架构稳定后再删除。可以逐步迁移,新旧代码可以共存。

### Q2: 如何处理全局变量?
**A**: 将全局变量改为模块内部变量,通过export导出需要暴露的接口。

### Q3: 如何处理组件间的通信?
**A**: 使用事件系统或通过Service层共享数据。复杂场景可以考虑使用状态管理库。

### Q4: 如何优化性能?
**A**: 使用动态import按需加载,避免首屏加载过多代码。

### Q5: 如何调试模块化代码?
**A**: 使用Chrome DevTools的Sources面板,模块化的代码可以设置断点调试。

---

**相关文档**:
- [前端重构指南](./frontend-refactoring-guide.md)
- [架构对比](./frontend-architecture-comparison.md)

**作者**: shaun.sheng
**创建日期**: 2026-03-27
