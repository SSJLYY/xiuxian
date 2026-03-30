# 前端新架构 - 完整文件清单

> 本文档列出新架构创建的所有文件,方便查找和参考。

## 📁 目录树

```
static/
├── js/
│   ├── core/                              # 核心层
│   │   ├── api/                          # API客户端
│   │   │   ├── ApiClient.js             (285行) 基础HTTP客户端
│   │   │   ├── GameApi.js               (267行) 游戏端API
│   │   │   └── AdminApi.js              (273行) 管理端API
│   │   ├── storage/                      # 数据持久化
│   │   │   ├── Storage.js               (175行) localStorage封装
│   │   │   └── AuthStorage.js           (115行) 认证信息管理
│   │   └── utils/                        # 工具函数
│   │       ├── Security.js              (85行) XSS防护
│   │       ├── HttpUtils.js             (265行) HTTP工具
│   │       └── FormatUtils.js           (180行) 格式化工具
│   │
│   ├── components/                       # 可复用组件
│   │   ├── Modal.js                     (215行) 模态框组件
│   │   ├── Toast.js                     (160行) 消息提示组件
│   │   └── Loading.js                   (245行) 加载动画组件
│   │
│   ├── modules/                          # 业务模块层
│   │   ├── player/                      # 玩家模块
│   │   │   ├── PlayerService.js        (185行) 玩家业务逻辑
│   │   │   ├── PlayerUI.js             (220行) 玩家UI渲染
│   │   │   └── index.js                 (25行) 模块入口
│   │   ├── combat/                      # 战斗模块(待创建)
│   │   ├── inventory/                   # 背包模块(待创建)
│   │   ├── equipment/                   # 装备模块(待创建)
│   │   ├── skills/                      # 技能模块(待创建)
│   │   ├── pets/                        # 宠物模块(待创建)
│   │   ├── quest/                       # 任务模块(待创建)
│   │   ├── shop/                        # 商城模块(待创建)
│   │   ├── guild/                       # 宗门模块(待创建)
│   │   ├── auction/                     # 拍卖行模块(待创建)
│   │   ├── mail/                        # 邮件模块(待创建)
│   │   ├── ranking/                     # 排行榜模块(待创建)
│   │   ├── achievement/                 # 成就模块(待创建)
│   │   ├── checkin/                     # 签到模块(待创建)
│   │   ├── vip/                         # VIP模块(待创建)
│   │   ├── activity/                    # 活动模块(待创建)
│   │   ├── narrative/                   # 叙事模块(待创建)
│   │   └── map/                         # 地图模块(待创建)
│   │
│   ├── pages/                            # 页面入口(待创建)
│   │   ├── GamePage.js
│   │   └── AdminPage.js
│   │
│   ├── App.js                            (250行) 应用主入口
│   └── main.js                           (50行) 主入口文件
│
├── css/
│   ├── core/                             # 核心样式
│   │   ├── variables.css                (140行) CSS变量定义
│   │   ├── reset.css                    (240行) 样式重置
│   │   └── base.css                     (420行) 基础样式
│   │
│   ├── components/                       # 组件样式
│   │   ├── modal.css                    (120行) 模态框样式
│   │   ├── toast.css                    (100行) Toast样式
│   │   └── loading.css                  (90行) Loading样式
│   │
│   └── modules/                          # 模块样式(待创建)
│       ├── player.css
│       ├── combat.css
│       └── ...
│
├── game-new.html                         (300行) 新架构游戏页面示例
│
└── templates/                            # 模板文件(待创建)
    └── fragments/
```

## 📄 文件详细说明

### 核心层 (core/)

#### API层 (core/api/)

| 文件 | 行数 | 功能 | 关键方法 |
|------|------|------|----------|
| **ApiClient.js** | 285 | 基础HTTP客户端 | request(), get(), post(), put(), delete() |
| **GameApi.js** | 267 | 游戏端API封装 | 40+个游戏API方法(玩家/修炼/战斗/背包等) |
| **AdminApi.js** | 273 | 管理端API封装 | 30+个管理API方法(玩家管理/系统邮件等) |

**特点**:
- 统一的错误处理
- 自动token管理
- 超时控制
- 401/403自动跳转

#### 存储层 (core/storage/)

| 文件 | 行数 | 功能 | 关键方法 |
|------|------|------|----------|
| **Storage.js** | 175 | localStorage封装 | set(), get(), remove(), clear(), has() |
| **AuthStorage.js** | 115 | 认证信息管理 | setToken(), getToken(), isLoggedIn() |

**特点**:
- 类型安全
- 前缀管理
- 容量检查
- 错误处理

#### 工具层 (core/utils/)

| 文件 | 行数 | 功能 | 关键方法 |
|------|------|------|----------|
| **Security.js** | 85 | XSS防护 | escapeHtml(), escapeUrl(), safeSetText() |
| **HttpUtils.js** | 265 | HTTP工具 | request(), handleResponse() |
| **FormatUtils.js** | 180 | 格式化工具 | formatNumber(), formatTime(), formatExp() |

**特点**:
- 纯函数设计
- 无副作用
- 可独立测试

### 组件层 (components/)

| 文件 | 行数 | 功能 | 使用方式 |
|------|------|------|----------|
| **Modal.js** | 215 | 模态框组件 | Modal.confirm(), Modal.alert(), new Modal() |
| **Toast.js** | 160 | 消息提示 | toast.success(), toast.error(), toast.warning() |
| **Loading.js** | 245 | 加载动画 | loading.show(), loading.hide(), Loading.execute() |

**特点**:
- 可复用
- 易于使用
- 支持自定义

### 业务模块层 (modules/)

#### 玩家模块 (modules/player/)

| 文件 | 行数 | 功能 | 关键方法 |
|------|------|------|----------|
| **PlayerService.js** | 185 | 玩家业务逻辑 | getCurrentPlayer(), getPlayerProfile(), formatPlayerInfo() |
| **PlayerUI.js** | 220 | 玩家UI渲染 | init(), loadPlayerInfo(), updatePlayerDisplay() |
| **index.js** | 25 | 模块入口 | 导出playerService, playerUI |

**特点**:
- Service/UI分离
- 自动刷新
- 错误处理

### 应用层

| 文件 | 行数 | 功能 | 关键方法 |
|------|------|------|----------|
| **App.js** | 250 | 应用主入口 | init(), checkAuth(), initModules() |
| **main.js** | 50 | 主入口文件 | DOM加载后启动应用 |

**特点**:
- 统一初始化
- 模块管理
- 全局事件处理

### CSS文件

#### 核心样式 (css/core/)

| 文件 | 行数 | 功能 |
|------|------|------|
| **variables.css** | 140 | CSS变量定义(颜色/字体/间距) |
| **reset.css** | 240 | 样式重置(盒模型/标题/表单) |
| **base.css** | 420 | 基础样式(工具类/布局类) |

#### 组件样式 (css/components/)

| 文件 | 行数 | 功能 |
|------|------|------|
| **modal.css** | 120 | 模态框样式 |
| **toast.css** | 100 | Toast消息提示样式 |
| **loading.css** | 90 | 加载动画样式 |

### 示例页面

| 文件 | 行数 | 功能 |
|------|------|------|
| **game-new.html** | 300 | 新架构游戏页面示例 |

**特点**:
- ES6模块加载
- 响应式设计
- 模块化架构

## 📊 统计数据

### 代码行数统计

| 类别 | 文件数 | 总行数 | 平均行数/文件 |
|------|--------|--------|---------------|
| **核心层JS** | 9 | 1,870 | 208 |
| **组件层JS** | 3 | 620 | 207 |
| **模块层JS** | 3 | 430 | 143 |
| **应用层JS** | 2 | 300 | 150 |
| **JS总计** | 17 | 3,220 | 189 |
| **CSS核心** | 3 | 800 | 267 |
| **CSS组件** | 3 | 310 | 103 |
| **CSS总计** | 6 | 1,110 | 185 |
| **HTML示例** | 1 | 300 | 300 |
| **总计** | 24 | 4,630 | 193 |

### 功能覆盖

- ✅ API调用: 70+ 个API方法
- ✅ 组件: 3 个可复用组件
- ✅ 工具函数: 15+ 个工具方法
- ✅ 模块: 1 个完整模块示例
- ✅ 样式: 100+ 个CSS类

## 📝 文件依赖关系

```
main.js
  └── App.js
        ├── core/api/ApiClient.js
        ├── core/api/GameApi.js
        ├── core/storage/Storage.js
        ├── core/storage/AuthStorage.js
        ├── components/Toast.js
        ├── components/Modal.js
        ├── components/Loading.js
        └── modules/player/index.js
              ├── modules/player/PlayerService.js
              │     ├── core/api/GameApi.js
              │     ├── components/Toast.js
              │     └── core/utils/FormatUtils.js
              └── modules/player/PlayerUI.js
                    ├── modules/player/PlayerService.js
                    └── components/Loading.js
```

## 🚀 使用方式

### 方式1: 在HTML中使用
```html
<script type="module" src="js/main.js"></script>
```

### 方式2: 在JS中导入
```javascript
// 导入组件
import { toast } from './components/Toast.js';

// 导入模块
import { playerService } from './modules/player/index.js';

// 使用
toast.success('操作成功');
const player = await playerService.getCurrentPlayer();
```

### 方式3: 动态导入
```javascript
// 按需加载模块
const { inventoryUI } = await import('./modules/inventory/index.js');
await inventoryUI.init();
```

## 📚 相关文档

- [前端重构指南](./frontend-refactoring-guide.md) - 详细架构说明
- [架构对比](./frontend-architecture-comparison.md) - 新旧架构对比
- [迁移快速指南](./frontend-migration-quickstart.md) - 如何迁移现有代码
- [API文档](./api/API-OVERVIEW.md) - 后端API参考

---

**作者**: shaun.sheng
**创建日期**: 2026-03-27
**最后更新**: 2026-03-27
