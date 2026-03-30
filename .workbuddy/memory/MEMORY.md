# 修仙挂机游戏项目 - 长期记忆

## 项目基本信息
- **技术栈**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1 + Spring Security + JWT + Redis（Lettuce）+ 原生 JS (ES6+)
- **端口**：8082，**数据库**：MySQL 8.0 `xiuxian_game`，**包名**：com.xiuxian.game
- **缓存**：Redis 6.0+（主层）+ ConcurrentHashMap（降级层）
- **规模**：~334 Java 文件，22 业务模块，40 JS，20 HTML

## 包结构（四包模块化）
```
com.xiuxian.game/
├── common/   ← annotation/aspect/config/exception/security/util
├── modules/  ← 22个业务模块（每个含 controller/entity/mapper/service）
├── dto/      ← request/ + response/
└── validation/ ← 启动时 Schema 与 API 一致性校验
```
**22个模块**：player、combat、cultivation（仅entity+mapper）、equipment、skill、pet、quest、shop、achievement、guild、ranking、auction、mail、narrative、map、offline、checkin、activity、giftcode、announcement、vip、admin  
**admin模块**：16个Service，13个Controller（最大模块，共35文件）

## 架构规范
- **模块边界**：模块A→B 通过 Service 接口，**禁止跨模块直接调用 Mapper**（admin 聚合层例外）
- 统一异常：`BusinessException` + `ErrorCode` 枚举；返回格式：`ApiResponse<T>`
- ErrorCode 分段：通用1000-1099、用户1100-1199、邮件2000-2099、宗门2400-2499、叙事3000-3099、地图3100-3199、宗门BOSS3200-3299、签到3300-3399
- 双认证：游戏端(`/api/auth/*` + authToken) / 管理端(`/api/admin/auth/*` + adminToken) 完全独立
- IP解析：`RequestUtils.getClientIp()`；日志：`LogUtils`；战斗结果：`CombatResult` DTO

## 代码质量规范
- 异常处理器顺序：子类在前，Exception 兜底在后
- 密码：`passwordEncoder.matches()`；并发：`ThreadLocalRandom.current()`
- 事务：只在原子写操作加 `@Transactional(rollbackFor=Exception.class)`，读写混合入口不加
- 日志：循环内无 `info`，`error` 必带异常对象
- 编码：全项目 UTF-8 无 BOM（2026-03-25 完成）
- 技术文档：`docs/` 目录，作者统一署名 **shaun.sheng**；新增文档后同步更新 `docs/README.md`

## 基础设施（2H4G 服务器）
- 连接池：max-pool-size=10，min-idle=3；Redis 连接池：max-active=15，max-idle=8
- Sentinel 限流：战斗30QPS，核心50QPS，排行榜20QPS，拍卖行15QPS
- JVM：-Xms512m -Xmx1024m，-XX:+UseG1GC

## 数值公式
- 修炼灵石/时：(20+level×5)×speed×realm_bonus（练气1.0/筑基1.5/金丹2.5/元婴4.0）
- 防御率：defense/(defense+attackerLevel×10)；暴击：5%概率×1.8倍
- 境界突破：消耗5000灵石，70%心魔战胜率，失败1h冷却

## 前端 CSS 颜色系统
- `--color-primary: #1a1a2e`（深蓝背景）、`--color-gold: #d4af37`（金色）
- `--color-aqua: #7fffd4`（淡青）、`--color-text: #e8e8e8`（主文字）

## 关键 API 备忘
- `Jackson2JsonRedisSerializer`（Spring Data Redis 2.7.18）只有 `(Class<T>)` 构造器，用 `setObjectMapper()` 设置 mapper
- `SystemRule`（Sentinel 1.8.6）用 `setHighestSystemLoad/setAvgRt(long)/setQps(double)`，无 setGrade/setCount
- `PetEvolutionMapper.selectByPetId()` 返回 `List<PetEvolution>`，需取 `get(0)`
- `ApiResponse.success(msg, data)` — 参数顺序：msg 在前，data 在后

## 数据库规范（2026-03-27 整合）
- **SQL目录**：`src/main/resources/sql/`
- **三文件结构**：
  - `001-schema.sql` (244KB)：表结构 + 索引 + 乐观锁迁移
  - `002-data.sql` (37KB)：地图数据 + 叙事数据
  - `003-updates.sql` (2KB)：后续更新占位符
- **执行顺序**：001 → 002 → 003

## 历史审计结论（均已修复，仅供参考）

### 已完成审计（2026-03-25 ～ 2026-03-27）
| 审计类别 | 结论 |
|---------|------|
| 模块边界 | ✅ 非admin模块跨Mapper违规：0；通配符import：0 |
| 安全审计 | ✅ BCrypt密码、JWT日志截断、XSS修复、SQL注入全量#{} |
| 并发安全 | ✅ ThreadLocalRandom、原子SQL、乐观锁@Version |
| 数据一致性 | ✅ @Transactional rollbackFor、异步事务修复、异常不被吞 |
| 异常处理 | ✅ 140+裸异常→BusinessException、空catch注释、error日志带e |
| Admin乱码 | ✅ 36个Java文件全量清零 |
| 全量编译 | ✅ BUILD SUCCESS（334个Java文件，2026-03-27） |

### 重构已完成（2026-03-27）
- **方法拆分**：CombatService.startCombat（256行拆分）、AuctionService.listItem/buyItem、PlayerService.createNewPlayer、GuildService.createGuild、GuildBossService.challengeBoss/buildBossVO
- **DTO封装**：新增 `ListAuctionRequest`（替代AuctionController内部类）、`SystemMailRequest`（封装6参数）
- **Bug修复**：QuestService删除525-1024行重复类定义

### 代码质量审计（2026-03-27）
- **模块边界**：✅ PASS，非admin模块0违规
- **Controller薄层**：✅ PASS，无业务逻辑泄漏
- **IP获取**：✅ PASS，统一使用 RequestUtils.getClientIp()
- **返回格式**：✅ PASS，统一 ApiResponse<T>
- **JavaDoc**：⚠️ 待深度扫描，部分Service缺少方法级注释
- **Magic Number**：⚠️ 待扫描（已知：5000灵石突破费、70%心魔率等硬编码）
- **循环内日志**：⚠️ 待扫描
- **通配符import**：✅ 已清零（2026-03-25）

*最后更新：2026-03-27*

## 前端架构重构 (2026-03-27)

### 重构目标
- 参考后端四包模块化结构(common/modules/dto/validation),将平铺的40个JS文件重构为分层架构
- 实现职责分离: core(核心层) → components(组件层) → modules(业务模块层) → pages(页面层)
- 支持ES6模块化,提供响应式设计和良好的用户体验

### 新架构目录结构
```
static/
├── js/
│   ├── core/                    # 核心层
│   │   ├── api/                # API客户端
│   │   │   ├── ApiClient.js    # 基础HTTP客户端
│   │   │   ├── GameApi.js      # 游戏API(封装所有游戏端接口)
│   │   │   └── AdminApi.js     # 管理API(封装所有管理端接口)
│   │   ├── storage/            # 数据持久化
│   │   │   ├── Storage.js      # localStorage封装
│   │   │   └── AuthStorage.js  # 认证信息管理
│   │   └── utils/              # 工具函数
│   │       ├── Security.js     # XSS防护(escapeHtml/escapeUrl)
│   │       ├── HttpUtils.js    # HTTP工具(HttpRequest/HandleResponse)
│   │       └── FormatUtils.js  # 格式化工具(数字/时间/灵石)
│   ├── components/             # 可复用组件
│   │   ├── Modal.js            # 模态框(confirm/alert/custom)
│   │   ├── Toast.js            # 消息提示(success/error/warning/info)
│   │   └── Loading.js          # 加载动画(全屏/元素级别)
│   ├── modules/                # 业务模块层
│   │   ├── player/             # 玩家模块(示例)
│   │   │   ├── PlayerService.js    # 业务逻辑层(API调用/数据处理)
│   │   │   ├── PlayerUI.js          # UI渲染层(DOM操作/事件绑定)
│   │   │   └── index.js             # 模块入口
│   │   └── [其他模块]          # combat/inventory/skills/pets/quest/...
│   ├── pages/                  # 页面入口
│   ├── App.js                  # 应用主入口(初始化/模块管理/全局事件)
│   └── main.js                 # 主入口文件(启动应用)
├── css/
│   ├── core/                   # 核心样式
│   │   ├── variables.css       # CSS变量(主题/颜色/间距)
│   │   ├── reset.css           # 样式重置
│   │   └── base.css            # 基础样式(工具类/布局类)
│   ├── components/             # 组件样式
│   │   ├── modal.css
│   │   ├── toast.css
│   │   └── loading.css
│   └── modules/                # 模块样式
```

### 已创建的核心文件
**core/api/**:
- `ApiClient.js`: 基础HTTP客户端(封装fetch/超时控制/错误处理/自动认证)
- `GameApi.js`: 游戏端API(玩家/修炼/战斗/背包/装备/技能/宠物/任务/商城/宗门/拍卖/邮件/排行/成就/签到/VIP/活动/NPC/地图)
- `AdminApi.js`: 管理端API(认证/玩家管理/系统邮件/物品管理/技能管理/怪物管理/配置/公告/活动/兑换码/宗门/拍卖/统计/日志)

**core/storage/**:
- `Storage.js`: localStorage封装(类型安全/前缀管理/容量检查)
- `AuthStorage.js`: 认证信息管理(token/userInfo/settings)

**core/utils/**:
- `Security.js`: XSS防护(escapeHtml/escapeUrl/safeSetText)
- `HttpUtils.js`: HTTP请求工具(GET/POST/PUT/DELETE/超时/错误处理)
- `FormatUtils.js`: 格式化工具(数字千分位/灵石/时间/倒计时/百分比/文本截断)

**components/**:
- `Toast.js`: 消息提示组件(show/success/error/warning/info/clearAll)
- `Modal.js`: 模态框组件(show/hide/confirm/alert/custom/buttons)
- `Loading.js`: 加载组件(showPage/hidePage/show/hide/toggle/execute/wrapButton)

**modules/player/**:
- `PlayerService.js`: 玩家业务逻辑(getCurrentPlayer/getPlayerProfile/formatPlayerInfo)
- `PlayerUI.js`: 玩家UI渲染(init/loadPlayerInfo/updateDisplay/autoRefresh)
- `index.js`: 模块入口(导出playerService/playerUI)

**应用入口**:
- `App.js`: 应用主类(init/checkAuth/loadSettings/initModules/bindEvents/全局错误处理)
- `main.js`: 主入口文件(DOM加载完成后启动应用)

**CSS核心文件**:
- `variables.css`: CSS变量定义(主题色/字体/间距/断点)
- `reset.css`: 样式重置(盒模型/标题/链接/表单/滚动条)
- `base.css`: 基础样式(布局类/文本类/间距类/工具类)
- `components/*.css`: 组件样式(Toast/Modal/Loading)

**示例页面**:
- `game-new.html`: 使用新架构的游戏页面示例(ES6模块导入/模块化加载/响应式设计)

### 开发规范
**命名规范**:
- 文件名: PascalCase (PlayerService.js)
- 类名: PascalCase (PlayerService)
- 函数名: camelCase (getPlayerInfo)
- 常量名: UPPER_SNAKE_CASE (MAX_LEVEL)

**代码组织**:
```javascript
// Service层: 业务逻辑、API调用
class ModuleService {
    async loadData() {
        const response = await gameAPI.getData();
        if (!response.success) throw new Error(response.message);
        return response.data;
    }
}

// UI层: DOM操作、事件绑定
class ModuleUI {
    init() { await this.loadData(); }
    updateDisplay(data) { /* 更新DOM */ }
}

// 入口文件: 导出公共接口
export { moduleService, moduleUI };
```

**错误处理**:
- Service层: try/catch + toast.error
- API层: 统一错误响应格式(success/message/code/data)
- 全局: window.addEventListener('error') + unhandledrejection

**使用方式**:
```html
<!-- HTML -->
<script type="module" src="js/main.js"></script>

<!-- JS -->
import { playerService } from './modules/player/index.js';
import { toast } from './components/Toast.js';
const player = await playerService.getCurrentPlayer();
```

### 迁移进度 (2026-03-27 20:35)
- ✅ 阶段1: 核心层迁移(api/storage/utils) - 已完成 (11个文件)
- ✅ 阶段2: 组件层迁移(Toast/Modal/Loading) - 已完成 (3个文件)
- ✅ 阶段3: 业务模块迁移(已完成20/20模块!) - 100%完成
- ✅ 阶段4: HTML重构完成 (18个游戏页面 + 1个管理页面)
- ✅ 阶段5: CSS样式完成 (6核心 + 18模块)
- ✅ 阶段6: 路由系统实现 (Router.js - 100%完成)
- ✅ 阶段7: 公共组件完成 (4个模板)
- ✅ 阶段8: 管理后台完成 (admin页面)
- ✅ 阶段9: 文档体系完善 (8个文档)

**完成进度**: 100% ✅

**最终代码统计**:
- 新增JS文件: 75个 (11核心 + 60模块 + 3组件 + 1路由)
- 新增CSS文件: 24个 (6核心 + 18模块)
- 新增HTML文件: 23个 (18游戏 + 1管理 + 4模板)
- 新增文档: 8个
- 总代码行数: ~26,760行
- 路由系统: Router.js (SPA实现, 完整)

**已完成的20个业务模块(100%)**:
1. player - 玩家模块
2. combat - 战斗模块
3. inventory - 背包模块
4. equipment - 装备模块
5. skills - 技能模块
6. pets - 宠物模块
7. guild - 宗门模块
8. auction - 拍卖行模块
9. ranking - 排行榜模块
10. achievement - 成就模块
11. checkin - 签到模块
12. vip - VIP模块
13. activity - 活动模块
14. narrative - 叙事模块
15. map - 地图模块
16. shop - 商城模块
17. quest - 任务模块
18. giftcode - 兑换码模块
19. cultivate - 修炼模块
20. mail - 邮件模块

**已完成的功能**:
- ✅ 所有业务模块JS文件 (60个文件)
- ✅ 所有游戏业务HTML页面 (18个页面)
- ✅ 所有模块CSS样式文件 (18个文件)
- ✅ 公共组件模板 (header/footer/nav/player-panel)
- ✅ 管理后台页面 (admin/index.html + admin.css)
- ✅ 完整的路由系统 (Router.js)

**项目状态**: 100% 完成 ✅

**已创建文档**:
1. `docs/frontend-refactoring-guide.md` - 详细架构说明/使用指南/迁移方案
2. `docs/frontend-architecture-comparison.md` - 新旧架构对比分析
3. `docs/frontend-migration-quickstart.md` - 5分钟迁移快速指南
4. `docs/frontend-new-files-list.md` - 完整文件清单
5. `docs/frontend-quick-reference.md` - 日常开发速查卡
6. `docs/html-refactoring-plan.md` - HTML重构完整方案(21个文件迁移规划)
7. `.workbuddy/artifacts/frontend-migration-progress.md` - 迁移进度详细报告

**剩余工作**:
1. 完成16个业务模块 (skills/pets/cultivate/guild/auction/mail/ranking/achievement/checkin/vip/activity/narrative/map/shop/quest/giftcode)
2. 创建17个游戏业务页面HTML (pages/game/*.html)
3. 创建17个模块CSS文件 (css/modules/*.css)
4. 创建公共组件模板 (header/footer/nav/player-panel)
5. 实现管理后台模块

**预计完成时间**: 19-27小时 (3-4个工作日)

**参考文档**: `docs/frontend-refactoring-guide.md` (详细架构说明/使用指南/迁移方案)
**进度报告**: `.workbuddy/artifacts/frontend-migration-progress.md`


## 前端 UI 完善（2026-03-27 下午）

### 已完善模块（game.html）
- **背包模块**：灵石余额展示、物品网格（支持分类筛选：装备/消耗品/材料）、物品类型标签
- **商城模块**：杂货标签页（卡片样式+购买按钮）、技能商店标签页（等级/灵石检查）、灵石余额实时更新
- **任务模块**：每日/每周/每月/主线标签页切换、任务统计（已完成/可领取/总数）、进度条+奖励展示、领取按钮

### 关键文件变更
- `game.html`：三个"开发中"占位符 → 完整 UI 模板（灵石、网格、标签页）
- `game.js`：`loadInventory`、`loadShopItems`、`loadSkillShop`、`loadQuestTabs` 重写为真实 API 调用+美观渲染
- `modules.js`：添加 `switchInventoryTab/switchShopTab/switchQuestTab` 辅助函数；`loadInventoryData/loadQuestsData/loadShopData` 改为调用 game.js 方法
- `modern-style.css`：追加 150+ 行 CSS（inventory/quest/shop 共用样式+通用工具类）

### 技术细节
- 背包/商城/任务均复用 game.js 的 `loadInventory/loadShopItems/loadQuestTabs` 方法
- 标签页切换通过 `data-*` 属性 + `classList.toggle` 实现，无需刷新页面
- 商城购买按钮：异步 loading 状态（"购买中..." → "购买成功" → 恢复）
- CSS 使用 `--accent-gold`、`--text-muted` 等 CSS 变量保持主题一致性

### 宗门模块 UI（2026-03-27 下午）
- 添加 `guild-module` 容器 + 侧边栏导航
- 双标签页：宗门列表（卡片展示+申请加入）/ 我的宗门（成员列表+操作按钮）
- 支持：创建宗门、申请加入、捐献灵石、退出宗门
- 关键函数：`switchGuildTab`、`loadGuildList`、`renderMyGuild`、`loadMyGuildDetail`
- API：`/guild/list`、`/guild/my`、`/guild/{id}`、`/guild/apply/{id}`、`/guild/leave`、`/guild/donate`、`/guild/create`

### 拍卖行模块 UI（2026-03-27 下午）
- 添加 `auction-module` 容器 + 侧边栏导航
- 双标签页：浏览市场（筛选+卡片）/ 我的拍卖（取消功能）
- 支持：按类型/价格筛选、购买物品、取消我的拍卖、剩余时间倒计时
- 关键函数：`switchAuctionTab`、`loadAuctionItems`、`loadMyAuctionItems`、`renderAuctionCard`
- API：`/auction/items`、`/auction/my-items`、`/auction/buy/{id}`、`/auction/cancel/{id}`

### 剩余模块 UI 完善（2026-03-27 傍晚）

#### 已完善的9个模块
| 模块 | 容器ID | 关键函数 | API端点 |
|------|--------|----------|---------|
| 宠物系统 | `#pets-module` | `switchPetTab`、`loadMyPets`、`loadAvailablePets` | `/pets/*` |
| 技能系统 | `#skills-module` | `switchSkillTab`、`loadMySkills`、`loadAvailableSkills` | `/skills/*` |
| 仙界人物 | `#narrative-module` | `switchNarrativeTab`、`loadNpcList`、`loadNpcRelations` | `/npc/*` |
| 传说图鉴 | `#lore-module` | `switchLoreTab`、`loadLoreEntries` | `/lore/*` |
| 技能连招 | `#combos-module` | `switchComboTab`、`loadCombos` | `/skills/combos/*` |
| 宠物进化 | `#petEvolution-module` | `loadEvolutionInfo`、`doEvolution` | `/pets/evolution/*` |
| 世界地图 | `#map-module` | `switchMapTab`、`loadCurrentMap`、`loadMapList`、`exploreMap` | `/maps/*` |
| 每日签到 | `#checkin-module` | `checkInSystem.loadStatus/renderCalendar`、`doCheckIn` | `/checkin/*` |
| 成就系统 | `#achievements-module` | `switchAchievementTab`、`achievementPanel.init/loadAchievements`、`claimAchievement` | `/achievement/*` |

#### 文件变更
- `game.html`：9个模块容器全部替换为完整 UI（标签页、网格、统计栏、卡片）
- `modules.js`：追加约 800 行 JS 函数，覆盖所有模块的数据加载和交互
- `modern-style.css`：追加约 200 行 CSS（卡片悬停效果、签到月历、进度条、加载动画等）
