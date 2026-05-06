---
name: xiuxian-project-context
description: 快速建立对修仙挂机游戏仓库的整体认知，并为功能定位、代码落点、启动排障、文档对齐提供项目级上下文。适用于用户要求理解项目、开始接手开发、定位模块、分析改动影响或快速找到实现入口时。
---

# Xiuxian Project Context

## 目标

在进入这个仓库时，先把“这是个什么项目、代码在哪、从哪启动、改动会落到哪里”看明白，避免一上来盲读文件或乱改路径。

## 适用场景

在以下场景优先使用本技能：
- 用户说“通读项目”“先了解这个仓库”“看看这个项目怎么做的”
- 需要定位某个功能在哪实现
- 需要判断改动会影响后端、前端、配置还是文档
- 需要快速给出接手建议、开发入口或排障入口
- 需要在开始编码前先建立项目上下文

## 项目事实基线

- 项目类型：修仙挂机 RPG，含玩家端与管理后台双端。
- 后端主栈：Java 8 + Spring Boot 2.7.18 + MyBatis-Plus + Spring Security + JWT。
- 基础设施：MySQL 8、Redis、Log4j2、Maven、Actuator、Sentinel。
- 前端主栈：原生 HTML + JS + CSS，静态资源由 Spring Boot 直接托管。
- 默认开发端口：`8082`
- 核心玩法：修炼、战斗、技能、宠物、宗门、拍卖行、排行榜、成就、叙事、地图、离线收益。
- 认证模式：玩家端与管理端 JWT 隔离，接口前缀和登录流程分开。

## 目录与模块锚点

### 1) 文档入口
- 总入口：`docs/README.md`
- 快速启动：`docs/guides/GETTING-STARTED.md`
- 后端架构：`docs/architecture/BACKEND-ARCHITECTURE.md`
- API 总览：`docs/api/API-OVERVIEW.md`
- 设计基线：`docs/design/GDD-修仙挂机游戏设计文档.md`

规则：技术事实优先看 `docs/`，根目录 `README.md` 主要用于快速进入项目。

### 2) 后端入口
- 启动类：`src/main/java/com/xiuxian/game/XiuxianGameApplication.java`
- 主包根：`src/main/java/com/xiuxian/game`
- 四层结构：
  - `common`：配置、安全、AOP、异常、工具
  - `modules`：业务模块主体
  - `dto`：请求/响应对象
  - `validation`：启动校验与一致性校验

### 3) 业务模块定位
优先从 `src/main/java/com/xiuxian/game/modules` 下查找对应域：
- `player`：玩家、认证、档案、背包基础
- `combat`：战斗、怪物、战斗日志
- `skill`：技能学习、升级、连招
- `pet`：宠物培养、进化、参战
- `guild`：宗门、公会 Boss
- `auction`：拍卖行
- `ranking`：排行榜
- `achievement`：成就
- `narrative`：NPC、对话、世界观
- `map`：地图与进度
- `offline`：离线收益
- `admin`：管理后台

默认落点顺序：`controller -> service -> mapper -> entity`。

### 4) 前端入口
主目录：`src/main/resources/static`
- `js/core`：基础能力与全局支撑
- `js/modules`：业务模块脚本
- `js/pages`：页面级逻辑
- `css/core` / `css/modules`：样式分层
- `pages/game`：玩家端页面
- `pages/admin`：管理端页面
- 顶层页面常见入口：`login.html`、`game.html`、`adminLogin.html`

如果是页面行为问题，优先看页面 HTML，再看对应 `js/pages` 与 `js/modules`。

### 5) 配置与运行入口
- 应用配置：`src/main/resources/application.properties`
- 本地启动说明：`README.md`、`docs/guides/GETTING-STARTED.md`
- Linux/macOS 脚本：`start.sh`
- Windows 脚本：`start.bat`
- 容器编排：`docker-compose.yml`
- Docker 构建：`Dockerfile`

重点环境依赖：MySQL、Redis、JWT、管理员默认账号、CORS、日志目录。

## 常用命令速查

### 本地构建与启动
```bash
mvn clean package -DskipTests
java -jar target/xiuxian-game.jar
mvn test
```

### 启动脚本
```bash
# Windows
start.bat
stop.bat

# Linux/macOS
./start.sh build
./start.sh start
./start.sh stop
./start.sh status
```

### Docker / Compose
```bash
docker-compose up -d
docker-compose up -d mysql redis
docker-compose logs -f xiuxian-game
```

### 常见排查
```bash
# Windows 端口排查
netstat -ano | findstr :8082
taskkill /PID <PID> /F

# Linux 端口排查
lsof -i :8082

# Redis 检查
redis-cli ping

# MySQL 检查
mysql -u root -p
```

### 关键配置文件
- 应用配置：`src/main/resources/application.properties`
- 启动说明：`docs/guides/GETTING-STARTED.md`
- 容器配置：`docker-compose.yml`
- 日志目录：`logs/`

## 高频页面/API 对照表

| 页面/入口 | 主要前端脚本 | 核心 API / 模块 | 关键路径 |
|---|---|---|---|
| `login.html` | `js/auth.js`、`js/api.js` | `/api/auth/login` `/api/auth/register` `/api/auth/me` | `src/main/resources/static/login.html` |
| `game.html` 顶层主界面 | `js/game.js`、`js/modules/*` | 玩家端主入口，承接修炼/战斗/背包/宠物/技能等模块 | `src/main/resources/static/game.html` |
| `game.html` 修炼 | `js/game.js`、`js/modules/cultivate/*` | `/api/player/cultivate` `/api/player/cultivate/stop` | `src/main/resources/static/js/modules/cultivate/` |
| `game.html` 战斗 | `js/game.js`、`js/modules/combat/*` | `/api/combat/*` | `src/main/resources/static/js/modules/combat/` |
| `game.html` 背包 | `js/game.js`、`js/modules/inventory/*` | `/api/inventory*` | `src/main/resources/static/js/modules/inventory/` |
| `game.html` 技能 | `js/modules/skills/*` | `/api/skills*` | `src/main/resources/static/js/modules/skills/` |
| `game.html` 宠物 | `js/modules/pets/*`、`js/modules/petEvolution/*` | `/api/pets*` | `src/main/resources/static/js/modules/pets/` |
| `game.html` 任务 | `js/game.js`、`js/modules/quest/*` | `/api/quest*` | `src/main/resources/static/js/modules/quest/` |
| `game.html` 宗门 | `js/modules/guild/*` | `/api/guild*` `/api/guild-boss*` | `src/main/resources/static/js/modules/guild/` |
| `game.html` 拍卖行 | `js/modules/auction/*` | `/api/auction*` | `src/main/resources/static/js/modules/auction/` |
| `game.html` 地图 | `js/modules/map/*` | `/api/map*` | `src/main/resources/static/js/modules/map/` |
| `game.html` 排行榜 | `js/modules/ranking/*` | `/api/ranking*` `/api/public/leaderboard` | `src/main/resources/static/js/modules/ranking/` |
| `adminLogin.html` | `js/admin-auth.js`、`js/admin-api.js` | `/api/admin/auth/*` | `src/main/resources/static/adminLogin.html` |
| `pages/admin/index.html` | `js/core/api/AdminApi.js` | `/api/admin/dashboard/stats` `/api/admin/players` `/api/admin/announcement/*` | `src/main/resources/static/pages/admin/index.html` |

## 模块到页面映射表

| 后端模块 | 页面/入口 | 前端脚本锚点 | 说明 |
|---|---|---|---|
| `player` | `login.html`、`game.html`、`pages/game/player.html` | `js/auth.js`、`js/modules/player/*` | 认证、玩家资料、主界面玩家信息 |
| `combat` | `game.html`、`pages/game/combat.html` | `js/game.js`、`js/modules/combat/*` | 战斗、怪物、战斗日志 |
| `skill` | `game.html`、`pages/game/skills.html` | `js/modules/skills/*` | 技能学习、升级、装备、连招 |
| `pet` | `game.html`、`pages/game/pets.html` | `js/modules/pets/*`、`js/modules/petEvolution/*` | 宠物、进化、捕获、参战 |
| `quest` | `pages/game/quest.html` | `js/modules/quest/*` | 每日/每周/每月任务 |
| `shop` | `pages/game/shop.html` | `js/modules/shop/*` | 商城与购买 |
| `guild` | `pages/game/guild.html` | `js/modules/guild/*` | 宗门、公会 Boss |
| `auction` | `pages/game/auction.html` | `js/modules/auction/*` | 拍卖行 |
| `ranking` | `pages/game/ranking.html` | `js/modules/ranking/*` | 排行榜 |
| `achievement` | `pages/game/achievement.html` | `js/modules/achievement/*` | 成就系统 |
| `mail` | `pages/game/mail.html` | `js/modules/mail/*` | 邮件与系统奖励 |
| `map` | `pages/game/map.html` | `js/modules/map/*` | 地图与节点进度 |
| `narrative` | `pages/game/narrative.html` | `js/modules/narrative/*`、`js/modules/lore/*` | NPC、对话、图鉴 |
| `vip` | `pages/game/vip.html` | `js/modules/vip/*` | VIP 与充值权益 |
| `checkin` | `pages/game/checkin.html` | `js/modules/checkin/*` | 每日签到 |
| `activity` | `pages/game/activity.html` | `js/modules/activity/*` | 活动系统 |
| `giftcode` | `pages/game/giftcode.html` | `js/modules/giftcode/*` | 礼包码兑换 |
| `equipment` | `pages/game/equipment.html`、`pages/game/inventory.html` | `js/modules/equipment/*`、`js/modules/inventory/*` | 装备与背包联动 |
| `admin` / `announcement` | `pages/admin/index.html` | `js/core/api/AdminApi.js`、`js/admin-api.js` | 管理后台、公告与系统管理 |
| `offline` | 无独立高频页面，挂靠主游戏流程 | `js/game.js` | 偏后端支撑，负责离线收益计算与领取 |

## 问题类型分流

- 如果目标是“理解项目 / 找代码入口 / 判断改动落点”，使用本技能。
- 如果目标是“定位 bug 根因 / 给出验证链路”，转入 `xiuxian-bug-locate`。
- 如果目标是“做实现方案 / 拆代码落点 / 制定验收”，转入 `xiuxian-feature-implementation`。
- 如果目标是“追页面动作、接口调用链、前后端链路”，转入 `xiuxian-api-trace`。

## 典型工作流程

### 场景 A：用户要“先理解项目”
1. 先读 `README.md`
2. 再读 `docs/README.md`
3. 补 `docs/architecture/BACKEND-ARCHITECTURE.md`
4. 如果是玩法理解，再读 `docs/design/GDD-修仙挂机游戏设计文档.md`
5. 输出项目定位、技术栈、目录结构、关键模块

### 场景 B：用户要“定位某功能在哪”
1. 先判断是玩家端、管理端、后端接口、配置还是文档问题
2. 从 `modules/<domain>` 找后端
3. 从 `static/pages/*` 与 `static/js/*` 找前端
4. 如果涉及接口联动，沿 `controller -> service -> mapper` 往下看
5. 输出具体路径与依赖模块

### 场景 C：用户要“准备改代码”
1. 先收敛改动目标与影响范围
2. 明确落点是后端 / 前端 / 配置 / SQL / 文档
3. 优先查对应规范文档
4. 先给实现方案，再进入编码
5. 编码后补测试、补文档、补记忆

### 场景 D：用户要“启动或排障”
1. 看 `docs/guides/GETTING-STARTED.md`
2. 核对 `application.properties`
3. 检查 MySQL / Redis / 端口 8082
4. 本地用 `mvn clean package -DskipTests` + `java -jar ...` 或启动脚本
5. Docker 场景看 `docker-compose.yml`

## 项目约束

- 新增玩法默认是双端协同任务：后端接口 + 前端页面/脚本一起看。
- 跨模块调用优先走 service，避免直接跨模块依赖 mapper。
- API 默认统一响应包装：`ApiResponse<T>`。
- 玩家端与管理端认证隔离，排查登录问题时不要混看接口。
- Redis 是主缓存层，但存在自动降级逻辑；缓存问题要同时考虑 Redis 和本地回退。
- 配置修改优先环境变量覆盖，避免把真实密码硬编码提交到仓库。
- 数值、掉落、收益、战斗平衡类改动，优先回看 GDD 与优化记录，避免只盯代码。
- 文档变更要同步到 `docs/`，不要只改代码不改文档。

## 输出模板

需要对项目做判断时，优先按下面格式输出：

```markdown
## 项目定位
- ...

## 本次任务落点
- 后端：...
- 前端：...
- 配置：...
- 文档：...

## 建议先读
1. `...`
2. `...`
3. `...`

## 关键修改路径
- `...`
- `...`

## 风险与依赖
- 风险：...
  - 依赖/应对：...
```

## 本技能不替代什么

- 不替代 `project-development-direction`：那个技能负责规划开发阶段与里程碑。
- 不替代 `xiuxian-development-direction`：那个技能负责修仙玩法路线和系统优先级。
- 不替代 `xiuxian-bug-locate`：那个技能负责故障定位与根因候选。
- 不替代 `xiuxian-feature-implementation`：那个技能负责功能实施方案与代码落地。
- 不替代 `xiuxian-api-trace`：那个技能负责接口与页面动作的调用链追踪。
- 本技能只负责“项目上下文、目录落点、接手入口、排障入口”。