---
name: xiuxian-feature-implementation
description: 为修仙挂机项目设计并拆解功能实现方案，输出需求边界、后端/前端/配置/文档落地映射、实施步骤、影响范围和验收标准。适用于用户要求新增功能、改造现有能力、先出实现方案再编码时。
---

# Xiuxian Feature Implementation

## 目标

把“要做一个功能”拆成可直接执行的实施方案：改哪些层、落哪些文件、依赖哪些接口、需要补哪些测试和文档，避免空泛建议和边界失控。

## 触发场景

在以下场景优先使用本技能：
- 用户说“帮我实现一个功能”
- 需要先给方案，再进入编码
- 需要判断改动落在后端、前端、配置还是文档
- 需要拆分接口、页面、数据结构和联动范围
- 需要给出明确的验收标准和本轮不做项

## 输入前提

最好至少具备下面一项：
- 需求描述
- 涉及页面 / 模块 / API
- 期望行为或业务规则
- 限制条件（是否改数据库、是否允许改接口、是否必须兼容旧逻辑）

如果信息不完整，优先补：
1. 功能目标
2. 预期输入输出
3. 影响对象（玩家端 / 管理端 / 双端）
4. 本轮是否允许改表、改接口、改配置

## 项目事实基线

- 本项目是玩家端 + 管理后台双端协作仓库。
- 后端主栈：Java 8 + Spring Boot 2.7.18 + MyBatis-Plus。
- 前端主栈：原生 HTML + JS + CSS，主目录在 `src/main/resources/static`。
- 业务模块集中在 `src/main/java/com/xiuxian/game/modules`。
- 文档真相源在 `docs/`，尤其是：
  - `docs/README.md`
  - `docs/architecture/BACKEND-ARCHITECTURE.md`
  - `docs/api/API-OVERVIEW.md`
  - `docs/design/GDD-修仙挂机游戏设计文档.md`
- 常见约束：统一响应 `ApiResponse<T>`、跨模块优先走 service、玩法改动默认考虑双端联动。

## 代码与模块锚点

### 后端落点
- 控制器：`src/main/java/com/xiuxian/game/modules/*/controller`
- 服务层：`src/main/java/com/xiuxian/game/modules/*/service`
- 数据访问：`src/main/java/com/xiuxian/game/modules/*/mapper`
- 实体：`src/main/java/com/xiuxian/game/modules/*/entity`
- DTO：`src/main/java/com/xiuxian/game/dto`

### 前端落点
- 顶层入口：`login.html`、`game.html`、`adminLogin.html`
- 玩家端页面：`src/main/resources/static/pages/game`
- 管理端页面：`src/main/resources/static/pages/admin`
- 模块脚本：`src/main/resources/static/js/modules/<domain>/`
- 旧式页面逻辑：`src/main/resources/static/js/game.js`、`src/main/resources/static/js/*.js`
- 新式 API 封装：`src/main/resources/static/js/core/api/GameApi.js`、`AdminApi.js`

### 高价值模块
- 玩家与认证：`player`
- 战斗：`combat`
- 技能：`skill`
- 宠物：`pet`
- 装备/背包：`equipment`
- 任务：`quest`
- 宗门：`guild`
- 拍卖：`auction`
- 地图：`map`
- 排行榜：`ranking`
- 管理后台：`admin`、`announcement`、`activity`

## 工作流程

1. 明确需求边界
   - 功能目标是什么
   - 用户成功标准是什么
   - 本轮不做什么

2. 识别功能类型
   - 纯后端
   - 纯前端
   - 双端联动
   - 配置 / 数据驱动
   - 管理后台配套

3. 做落点映射
   - 后端：controller / service / mapper / entity / dto
   - 前端：page / js/modules / js/core/api / css
   - 数据：表结构 / 初始化数据 / 配置项
   - 文档：docs 下对应文档

4. 拆实现步骤
   - 数据模型或配置
   - 接口设计
   - 业务逻辑
   - 前端交互与展示
   - 测试与回归
   - 文档同步

5. 标记风险和依赖
   - 权限与认证
   - 缓存和状态一致性
   - 数值平衡与参数来源
   - 旧逻辑兼容
   - 跨模块调用边界

6. 输出实施清单
   - 代码修改清单
   - 测试补齐清单
   - 文档同步清单
   - 验收标准

## 输出模板

```markdown
## 需求目标
- 要实现：...
- 成功标准：...
- 本轮不做：...

## 落地映射
- 后端：`...`
- 前端：`...`
- 配置 / 数据：`...`
- 测试：`...`
- 文档：`...`

## 实现步骤
1. 数据与模型
   - ...
2. 接口与服务
   - ...
3. 前端页面与交互
   - ...
4. 测试与回归
   - ...
5. 文档同步
   - ...

## 影响范围
- 现有模块：...
- 新增模块：...
- 是否双端联动：是 / 否
- 兼容性风险：...

## 验收标准
- [ ] 接口可用
- [ ] 页面流程可走通
- [ ] 核心业务规则满足预期
- [ ] 测试补齐
- [ ] 文档同步
```

## 约束

- 不给空泛方案，必须落到具体目录或模块。
- 默认先判断是否双端联动，不要只盯一侧实现。
- 跨模块调用优先走 service，避免直接跨模块访问 mapper。
- DTO、响应结构、权限边界要单列说明。
- 涉及战斗、收益、掉落、成长、突破等玩法时，必须标注参数来源。
- 每个实施方案必须包含测试补齐项。
- 每个实施方案必须包含文档同步项。
- 每个实施方案必须有“本轮不做”，避免范围蔓延。
- 如果需求不清，先收敛边界，再谈代码实现。

## 本技能不替代什么

- 不替代 `xiuxian-project-context`：那个技能负责项目认知和目录入口。
- 不替代 `xiuxian-bug-locate`：那个技能负责定位问题与根因候选。
- 不替代 `xiuxian-api-trace`：那个技能负责梳理现有调用链。
- 本技能只负责“需求拆解、代码落点、实施步骤、验收标准”。