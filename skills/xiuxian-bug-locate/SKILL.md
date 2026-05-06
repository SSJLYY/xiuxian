---
name: xiuxian-bug-locate
description: 定位修仙挂机项目中的页面、接口、登录、数据和数值类问题，输出问题归类、首要定位链路、根因候选与验证步骤。适用于用户要求排查 bug、分析异常、解释为什么功能不生效、接口报错或页面行为异常时。
---

# Xiuxian Bug Locate

## 目标

把模糊的“这个有问题”收敛成清晰的定位结论：问题属于哪一类、最该先查哪条链路、最可能的根因是什么、下一步该如何验证。

## 触发场景

在以下场景优先使用本技能：
- 用户说“帮我定位这个 bug”
- 某页面打不开、按钮没反应、数据不显示
- 某接口返回 401 / 403 / 404 / 500
- 登录后跳回登录页、权限异常、token 失效
- 数据没更新、奖励没到账、状态没变化
- 数值、掉落、收益、战斗结果异常
- 启动报错、配置不生效、环境依赖异常

## 输入前提

最好至少具备下面一项：
- 页面名 / 页面路径
- 接口路径 / 错误码
- 模块名
- 报错日志 / 控制台错误
- 复现步骤

如果信息不足，优先补这 4 项：
1. 复现步骤
2. 实际现象 vs 预期结果
3. 页面或接口入口
4. 是否只在某环境复现

## 项目事实基线

- 玩家端 API 基础前缀：`/api`
- 管理端 API 基础前缀：`/api/admin`
- 玩家登录页：`src/main/resources/static/login.html`
- 玩家主界面：`src/main/resources/static/game.html`
- 管理员登录页：`src/main/resources/static/adminLogin.html`
- 管理后台主页：`src/main/resources/static/pages/admin/index.html`
- 后端控制器根目录：`src/main/java/com/xiuxian/game/modules/*/controller`
- 默认后端链路：`controller -> service -> mapper -> entity`
- 页面问题默认链路：`HTML -> js/pages 或 js/modules -> API -> controller -> service`

## 代码与模块锚点

### 页面与登录相关
- 玩家登录：`js/auth.js`、`js/api.js`
- 管理登录：`js/admin-auth.js`、`js/admin-api.js`
- 主游戏：`js/game.js`、`js/modules/*`

### 常见业务模块
- 玩家/认证：`modules/player`
- 战斗：`modules/combat`
- 技能：`modules/skill`
- 宠物：`modules/pet`
- 背包/装备：`modules/equipment`
- 任务：`modules/quest`
- 宗门：`modules/guild`
- 拍卖：`modules/auction`
- 地图：`modules/map`
- 离线收益：`modules/offline`
- 管理后台：`modules/admin`、`modules/announcement`、`modules/activity`

### 横切关注点
- 认证：`common/security/*`、`modules/player/controller/AuthController.java`、`modules/admin/controller/AdminAuthController.java`
- 配置：`src/main/resources/application.properties`
- 缓存与降级：Redis + 本地回退逻辑
- 统一响应：`ApiResponse<T>`

## 问题分类速查

- `401 / 跳回登录页`：优先看 token、登录态、鉴权过滤器、玩家端/管理端是否混用
- `403`：优先看角色权限、管理接口、前端是否误调 admin 接口
- `404`：优先看路由映射、静态资源路径、前端请求 URL 是否写错
- `500`：优先看 controller/service 异常分支、空指针、DTO 字段不匹配、数据库数据异常
- `页面无响应`：优先看 HTML 事件绑定、JS 初始化、请求是否真正发出
- `数据不显示`：优先看响应字段、前端渲染字段、DTO 与实体映射
- `奖励/状态不生效`：优先看 service 业务分支、事务、缓存、异步刷新
- `数值异常`：优先看 GDD、配置、收益/伤害/掉落参数来源

## 工作流程

1. 明确问题表象
   - 记录现象、预期、实际结果、影响页面/接口、是否稳定复现

2. 先做问题分类
   - 启动类
   - 鉴权类
   - 接口类
   - 页面交互类
   - 数据一致性类
   - 数值/逻辑类

3. 建立最短定位链路
   - 页面问题：`HTML -> JS -> API -> controller -> service`
   - 接口问题：`controller -> service -> mapper -> DB`
   - 登录/权限问题：`登录页 -> 登录接口 -> token 存储 -> 鉴权过滤器`
   - 配置问题：`application.properties -> 启动类 -> 外部依赖`

4. 缩小可疑范围
   - 找最近落点模块
   - 看参数校验、权限校验、缓存分支、异常抛出点、空值处理

5. 输出根因候选
   - 至少给出 2-3 个高概率原因
   - 每个原因都必须配“依据 + 验证方式”

6. 给出下一步验证建议
   - 先读哪些文件
   - 先看哪些日志 / 响应 / 参数 / 表字段
   - 若修复，提醒影响范围和回归点

## 输出模板

```markdown
## 问题概述
- 现象：...
- 预期：...
- 实际：...
- 类型：启动 / 鉴权 / 接口 / 页面 / 数据 / 数值逻辑

## 首要定位路径
1. `...`
2. `...`
3. `...`

## 可疑链路
- 前端：`...`
- 后端：`...`
- 配置/依赖：`...`

## 高概率根因
1. 根因候选 A
   - 依据：...
   - 验证方式：...
2. 根因候选 B
   - 依据：...
   - 验证方式：...
3. 根因候选 C
   - 依据：...
   - 验证方式：...

## 影响范围
- 可能影响模块：...
- 是否涉及双端联动：是 / 否
- 是否涉及缓存 / 权限 / 配置：...

## 下一步建议
- 先检查：...
- 再验证：...
- 若修复：需同步关注 `测试 / 文档 / 配置`
```

## 约束

- 不凭空断言根因，必须给出“依据 + 验证方式”。
- 页面类问题不能只看前端或只看后端，至少走一遍完整请求链。
- 登录与权限类问题必须先区分玩家端和管理端。
- 接口类问题默认沿 `controller -> service -> mapper` 走一遍。
- 涉及缓存时必须考虑 Redis 与本地降级逻辑。
- 涉及数值异常时，必须回看设计/配置来源，不只盯代码。
- 输出里必须包含“首要定位路径”和“影响范围”。
- 如果缺少复现信息，要先指出还缺什么，不要假装已经定位完成。

## 本技能不替代什么

- 不替代 `xiuxian-project-context`：那个技能负责项目整体认知与目录落点。
- 不替代 `xiuxian-feature-implementation`：那个技能负责需求实施方案与代码改动设计。
- 不替代 `xiuxian-api-trace`：那个技能负责完整调用链展开。
- 本技能只负责“问题归类、定位链路、根因候选、验证建议”。