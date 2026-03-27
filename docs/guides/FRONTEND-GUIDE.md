# 前端开发指南

> 本项目前端使用**原生 JavaScript（ES6+）**，无框架依赖。  
> 本文档覆盖：文件结构、模块划分、API 调用规范、状态管理、样式规范。

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-27

---

## 技术说明

| 技术 | 版本/说明 |
|------|-----------|
| JavaScript | ES6+，原生，无框架 |
| CSS | CSS3 + Tailwind CSS 3.x（CDN） |
| 字体 | Google Fonts（思源宋体，古风主题） |
| 图标 | Font Awesome 6.4.0（CDN） |
| 音频 | Web Audio API（程序化合成，无音频文件依赖）|

---

## 文件结构

```
src/main/resources/static/
├── css/
│   └── modern-style.css     # 主样式文件（5000+ 行，含响应式、动画、叙事系统）
│
├── js/
│   # 基础架构层
│   ├── auth.js              # 游戏端认证管理（login/logout/token）
│   ├── api.js               # 所有游戏 API 调用封装
│   ├── utils.js             # 通用工具函数
│   ├── logger.js            # 前端日志工具
│   ├── main.js              # 主入口，模块初始化
│   ├── modules.js           # 模块注册与数据加载
│
│   # 游戏功能层
│   ├── game.js              # 游戏主逻辑（修炼、状态刷新）
│   ├── skills.js            # 技能系统
│   ├── pets.js              # 宠物系统
│   ├── inventory.js         # 背包系统
│   ├── guild.js             # 宗门系统
│   ├── ranking.js           # 排行榜
│   ├── achievement.js       # 成就系统
│   ├── mail.js              # 邮件系统
│   ├── announcement.js      # 公告系统
│   ├── auction.js           # 拍卖行
│   ├── vip.js               # VIP 系统
│   ├── activity.js          # 活动系统
│   ├── checkin.js           # 签到系统
│
│   # 增强功能层
│   ├── tutorial.js          # 新手引导（5步串行）
│   ├── pet-hunger-monitor.js # 宠物饱食度监控悬浮组件
│   ├── breakthrough-evolution.js # 境界突破 + 宠物进化 UI
│   ├── combo-pokedex.js     # 技能连招 + 宠物图鉴
│   ├── narrative.js         # 叙事系统（NPC对话、传说图鉴、离线事件）
│   ├── game-map.js          # 地图系统
│   ├── guild-boss.js        # 宗门 BOSS
│   ├── achievement-panel.js # 成就面板（game.html 内嵌版）
│   ├── mobile-interaction.js # 移动端手势、通知、加载状态
│
│   # 战斗增强层
│   ├── enhanced_combat.js   # 增强战斗界面逻辑
│   ├── combat-visual-feedback.js # 伤害飘字、屏幕震动
│   ├── performance-tools.js  # 性能监控工具栏（开发调试用）
│
│   # 音频系统
│   ├── audio-engine.js      # Web Audio API 引擎（程序化音效）
│   ├── audio-integration.js # 游戏事件 → 音频触发集成
│   ├── audio-settings.js    # 音量控制面板
│
│   # 管理后台
│   ├── admin-auth.js        # 管理员认证（独立系统）
│   ├── admin-api.js         # 管理员 API 调用
│   ├── admin.js             # 管理后台主逻辑
│   ├── config-management.js # 配置管理
│   ├── log-management.js    # 日志查看
│   ├── modern-ui.js         # 管理后台 UI 增强
│
├── images/                  # 静态图片资源
│
# HTML 页面
├── game.html                # 游戏主界面（核心页面）
├── login.html               # 玩家登录/注册
├── enhanced_combat.html     # 增强战斗界面
├── pets.html                # 宠物管理页
├── equipment.html           # 装备管理
├── guild.html               # 宗门页面
├── ranking.html             # 排行榜
├── achievement.html         # 成就页面
├── mail.html                # 邮件中心
├── auction.html             # 拍卖行
├── vip.html                 # VIP 页面
├── activity.html            # 活动中心
├── admin.html               # 管理后台
├── adminLogin.html          # 管理员登录
└── index.html               # 首页（重定向到 login.html）
```

---

## API 调用规范

所有后端请求**必须**通过 `api.js` 中封装的函数调用，不要在业务 JS 文件里直接写 `fetch`。

### api.js 使用方式

```javascript
// ✅ 正确：使用 api.js 封装的函数
const profile = await api.getPlayerProfile();
const result = await api.startCombat(monsterId);

// ❌ 错误：直接写 fetch（无统一 Token 注入和错误处理）
const resp = await fetch('/api/player/profile', {
  headers: { 'Authorization': 'Bearer ' + token }
});
```

### 向 api.js 添加新接口

```javascript
// 在 api.js 中，找到对应分类区域添加
// 格式：async 函数名(参数) { return apiCall(方法, 路径, 请求体); }

async checkPetEvolution(playerPetId) {
    return apiCall('GET', `/api/pets/evolution/check/${playerPetId}`);
},

async evolvePet(playerPetId) {
    return apiCall('POST', `/api/pets/evolution/evolve/${playerPetId}`);
},
```

---

## 认证系统

游戏端认证完全由 `auth.js` 管理：

```javascript
// 检查是否已登录（任何需要认证的页面顶部调用）
if (!auth.isLoggedIn()) {
    window.location.href = '/login.html';
    return;
}

// 获取当前用户信息
const user = auth.getCurrentUser();  // { userId, nickname, token }

// 登出
auth.logout();
```

**Token 存储**：`localStorage.authToken`（游戏端）

> ⚠️ 管理端使用 `admin-auth.js` 和 `localStorage.adminToken`，两套系统完全独立。

---

## 模块系统（modules.js）

`game.html` 是单页面应用，通过 `modules.js` 管理各功能模块的切换与数据加载：

```javascript
// 注册模块（在 modules.js 的 moduleRegistry 中）
const moduleRegistry = {
    cultivation: { loadFn: loadCultivationData, container: '#cultivation-module' },
    skills:      { loadFn: loadSkillsData,      container: '#skills-module' },
    pets:        { loadFn: loadPetsData,         container: '#pets-module' },
    combat:      { loadFn: loadCombatData,       container: '#combat-module' },
    // ... 更多模块
};

// 切换到某模块
modules.switchTo('pets');
```

### 新增模块的步骤

1. 在 `game.html` 中添加导航项和容器 div：
   ```html
   <button onclick="modules.switchTo('myModule')">我的模块</button>
   <div id="myModule-module" class="module-container hidden">...</div>
   ```

2. 在 `modules.js` 中注册：
   ```javascript
   myModule: {
       loadFn: loadMyModuleData,
       container: '#myModule-module'
   }
   ```

3. 实现 `loadMyModuleData()` 函数（建议在独立的 `my-module.js` 文件中）

4. 在 `main.js` 的 `initializeModules()` 中初始化需要的系统

---

## 样式规范

### CSS 变量（颜色系统）

```css
/* 在 modern-style.css 开头定义，业务代码直接引用 */
:root {
  --color-primary: #1a1a2e;      /* 深蓝背景 */
  --color-gold: #d4af37;         /* 金色（主要强调色）*/
  --color-aqua: #7fffd4;         /* 淡青（次要强调色）*/
  --color-text: #e8e8e8;         /* 主文本色 */
  --color-text-dim: #a0a0a0;     /* 次要文本色 */
  --color-danger: #ff4757;       /* 危险/红色 */
  --color-success: #2ed573;      /* 成功/绿色 */
  --color-warning: #ffa502;      /* 警告/橙色 */
}
```

**禁止**在业务代码中硬编码颜色值（如 `color: #d4af37`），统一使用 CSS 变量。

### 响应式断点

```css
/* 已在 modern-style.css 中定义的6个断点 */
@media (max-width: 480px)   { /* 小手机 */ }
@media (max-width: 767px)   { /* 手机 */ }
@media (max-width: 1023px)  { /* 平板 */ }
@media (max-width: 1199px)  { /* 平板横屏 */ }
@media (min-width: 1200px)  { /* 桌面 */ }
@media (min-width: 1600px)  { /* 超宽屏 */ }
```

### 动画使用

```css
/* 使用已定义的动画类，不要重复定义 */
.fade-in    { animation: fadeIn 0.3s ease; }
.fade-in-up { animation: fadeInUp 0.4s ease; }
.slide-up   { animation: slideUp 0.3s ease; }
.pulse      { animation: pulse 2s infinite; }
```

---

## 通知系统

```javascript
// 使用 NotificationSystem（由 mobile-interaction.js 提供）
notification.success('宠物捕获成功！');
notification.error('灵石不足！');
notification.warning('宠物饱食度过低');
notification.info('明日重置时间：00:00');
```

---

## 音频事件

```javascript
// 触发游戏音效（audio-integration.js 自动监听游戏状态）
// 也可手动触发：
gameAudio.play('combat:attack');
gameAudio.play('combat:critical');
gameAudio.play('breakthrough:success');
gameAudio.play('pet:levelup');
gameAudio.play('ui:button_click');

// 更新自适应音乐参数
gameAudio.updateGameState({
    combatIntensity: 0.8,    // 0-1
    realmLevel: 2,           // 0: 练气, 1: 筑基, 2: 金丹, 3: 元婴
    playerHealth: 0.6,       // 0-1
    inBreakthrough: false
});
```

---

## 性能注意事项

- **DOM 操作**：批量修改 DOM 前先 `detach`，修改完成后 `reattach`
- **定时器**：使用完后务必 `clearInterval` / `clearTimeout`（特别是模块切换时）
- **事件监听**：组件销毁时移除监听器，防止内存泄漏
- **飘字特效**：最多同时显示 20 个，超过时自动回收（`combat-visual-feedback.js` 已处理）
- **移动端**：特效层级默认关闭（Tier3），不要在移动端开启大量 CSS 动画

---

## 调试工具

```javascript
// 性能监控工具（右下角工具栏）
// 开发环境自动启用，生产环境设置 DEBUG_MODE = false 关闭

// 音频调试 HUD
Alt+D      // 开启音频调试 HUD
M          // 静音/取消静音
Alt+A      // 打开音量设置面板
```
