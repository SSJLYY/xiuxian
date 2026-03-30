# 前端开发快速参考卡

> 新架构日常开发速查手册

## 🚀 快速开始

### 引入新模块
```javascript
// 方式1: 直接导入
import { playerService } from './modules/player/index.js';
const player = await playerService.getCurrentPlayer();

// 方式2: 动态导入
const { inventoryUI } = await import('./modules/inventory/index.js');
await inventoryUI.init();
```

### 使用组件
```javascript
// Toast消息
import { toast } from './components/Toast.js';
toast.success('操作成功');
toast.error('操作失败');
toast.warning('请注意');
toast.info('提示信息');

// Modal模态框
import { Modal } from './components/Modal.js';
const confirmed = await Modal.confirm('确定要删除吗?');
await Modal.alert('操作完成');

// Loading加载
import { loading } from './components/Loading.js';
loading.showPage('加载中...');
loading.hidePage();
loading.show('#container');
loading.hide('#container');
```

### API调用
```javascript
// 游戏API
import { gameAPI } from './core/api/GameApi.js';
const player = await gameAPI.getCurrentPlayer();

// 管理API
import { adminAPI } from './core/api/AdminApi.js';
const players = await adminAPI.getPlayers();
```

### 工具函数
```javascript
import { FormatUtils } from './core/utils/FormatUtils.js';
import { escapeHtml } from './core/utils/Security.js';

// 格式化
const formatted = FormatUtils.formatNumber(1234567); // "1,234,567"
const stones = FormatUtils.formatSpiritStones(100000000); // "1.00亿"
const time = FormatUtils.formatTime(3665); // "1小时1分钟5秒"

// 安全
const safe = escapeHtml('<script>alert("xss")</script>');
```

## 📁 模块结构模板

### 创建新模块
```javascript
// modules/newmodule/NewModuleService.js
import { gameAPI } from '../../core/api/GameApi.js';
import { toast } from '../../components/Toast.js';

class NewModuleService {
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

    formatData(data) {
        return { /* 格式化 */ };
    }
}

const newModuleService = new NewModuleService();
export { newModuleService };
```

```javascript
// modules/newmodule/NewModuleUI.js
import { newModuleService } from './NewModuleService.js';
import { loading } from '../../components/Loading.js';

class NewModuleUI {
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
            const data = await newModuleService.loadData();
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

const newModuleUI = new NewModuleUI();
export { newModuleUI };
```

```javascript
// modules/newmodule/index.js
import { newModuleService } from './NewModuleService.js';
import { newModuleUI } from './NewModuleUI.js';

export { newModuleService, newModuleUI };
export default { service: newModuleService, ui: newModuleUI };
```

## 🎨 CSS变量

```css
/* 颜色 */
--color-primary: #1a1a2e;
--color-gold: #d4af37;
--color-text: #e8e8e8;
--color-success: #4caf50;
--color-error: #f44336;
--color-warning: #ff9800;
--color-info: #2196f3;

/* 间距 */
--spacing-sm: 8px;
--spacing-md: 12px;
--spacing-lg: 16px;
--spacing-xl: 20px;

/* 圆角 */
--radius-sm: 4px;
--radius-md: 8px;
--radius-lg: 12px;

/* 阴影 */
--shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.1);
--shadow-md: 0 4px 8px rgba(0, 0, 0, 0.15);
--shadow-lg: 0 8px 16px rgba(0, 0, 0, 0.2);
```

## 🛠️ 常用代码片段

### API调用模板
```javascript
async function fetchData() {
    try {
        loading.show();
        const response = await gameAPI.someMethod();
        if (!response.success) {
            throw new Error(response.message);
        }
        // 处理数据
        return response.data;
    } catch (error) {
        console.error('操作失败:', error);
        toast.error(error.message);
        throw error;
    } finally {
        loading.hide();
    }
}
```

### UI渲染模板
```javascript
function renderItem(item) {
    return `
        <div class="item">
            <h3>${escapeHtml(item.name)}</h3>
            <p>${escapeHtml(item.description)}</p>
            <button class="btn btn-primary" data-id="${item.id}">
                操作
            </button>
        </div>
    `;
}
```

### 事件绑定模板
```javascript
function bindEvents() {
    const container = this.elements.container;
    container.addEventListener('click', (e) => {
        const btn = e.target.closest('button');
        if (!btn) return;

        const action = btn.dataset.action;
        const id = btn.dataset.id;

        switch (action) {
            case 'edit':
                this.handleEdit(id);
                break;
            case 'delete':
                this.handleDelete(id);
                break;
        }
    });
}
```

### 自动刷新模板
```javascript
class AutoRefresh {
    constructor(callback, interval = 5000) {
        this.callback = callback;
        this.interval = interval;
        this.timer = null;
    }

    start() {
        if (this.timer) return;
        this.timer = setInterval(() => {
            this.callback().catch(console.error);
        }, this.interval);
    }

    stop() {
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }
    }
}
```

## 🔍 调试技巧

### 查看应用状态
```javascript
console.log(window.app); // 应用实例
console.log(window.playerModule); // 玩家模块
```

### 查看存储
```javascript
localStorage.getItem('xiuxian_authToken');
localStorage.getItem('xiuxian_userInfo');
```

### 查看组件
```javascript
console.log(window.toast);
console.log(window.modal);
console.log(window.loading);
```

## ⚠️ 常见错误

### 1. 模块导入错误
```javascript
// ❌ 错误
import { module } from './modules/module.js';

// ✅ 正确
import { module } from './modules/module/index.js';
```

### 2. 忘记await
```javascript
// ❌ 错误
const player = gameAPI.getCurrentPlayer();

// ✅ 正确
const player = await gameAPI.getCurrentPlayer();
```

### 3. 未处理错误
```javascript
// ❌ 错误
const response = await gameAPI.getData();

// ✅ 正确
try {
    const response = await gameAPI.getData();
    if (!response.success) {
        throw new Error(response.message);
    }
} catch (error) {
    console.error('操作失败:', error);
    toast.error(error.message);
}
```

### 4. XSS漏洞
```javascript
// ❌ 错误
element.innerHTML = `<div>${userInput}</div>`;

// ✅ 正确
element.textContent = userInput;
// 或
element.innerHTML = escapeHtml(userInput);
```

## 📝 代码检查清单

### 提交前检查
- [ ] 所有public方法都有注释
- [ ] 使用try/catch处理错误
- [ ] 使用escapeHtml防止XSS
- [ ] 使用loading显示加载状态
- [ ] 使用toast显示提示信息
- [ ] 响应式设计(支持移动端)
- [ ] 代码格式化
- [ ] 删除console.log(生产环境)

### 代码审查要点
- [ ] Service层不包含DOM操作
- [ ] UI层不包含业务逻辑
- [ ] 使用ES6模块
- [ ] 统一的错误处理
- [ ] 合理的命名规范

## 🚀 性能优化

### 按需加载
```javascript
// 首屏加载必要模块
import { playerUI } from './modules/player/index.js';
await playerUI.init();

// 交互时加载其他模块
async function showInventory() {
    const { inventoryUI } = await import('./modules/inventory/index.js');
    await inventoryUI.init();
}
```

### 防抖节流
```javascript
function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

// 使用
const handleInput = debounce((e) => {
    // 处理输入
}, 300);
```

## 📚 相关文档

- [前端重构指南](./frontend-refactoring-guide.md) - 详细架构说明
- [架构对比](./frontend-architecture-comparison.md) - 新旧对比
- [迁移快速指南](./frontend-migration-quickstart.md) - 迁移步骤
- [文件清单](./frontend-new-files-list.md) - 完整文件列表

---

**作者**: shaun.sheng
**创建日期**: 2026-03-27
**版本**: 1.0
