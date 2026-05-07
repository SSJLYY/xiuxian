---
name: xiuxian-api-trace
description: 追踪 xiuxian 项目中页面动作到接口、控制器、服务和数据返回的完整链路，核对前后端字段契约、请求封装、响应结构和页面渲染依赖。用于用户要求分析某个按钮、页面、模块、接口是怎么串起来的，或排查页面和接口对不上、字段不一致、请求是否真正发出时。
---

# Xiuxian API Trace

## 目标

把一个页面动作或接口调用展开成完整调用链，明确每一层做了什么。

## 默认追踪路径

### 玩家端页面动作

`HTML -> js/pages 或 js/modules -> js/core/api/GameApi.js 或相关封装 -> controller -> service -> mapper/DTO -> ApiResponse<T> -> 页面渲染`

### 管理端页面动作

`HTML -> 管理端 JS 封装 -> /api/admin/... -> controller -> service -> mapper/DTO -> ApiResponse<T> -> 页面渲染`

## 先回答这几个问题

1. 入口页面是哪一个文件。
2. 入口事件由哪个 JS 文件绑定。
3. JS 最终请求了哪个 URL、方法、参数。
4. 对应哪个 controller 方法。
5. service 返回了什么数据结构。
6. 页面最终依赖哪些字段渲染。

## 仓库内高价值提醒

- 这个仓库很多错不在接口不存在，而在字段名或包装结构和页面预期不一致。
- `GameApi.js` 和各模块 service/adapter 层是高频错位点。
- 技能相关接口要重点核对 `playerSkillId`。
- 玩家端与管理端接口前缀不同，别串线。
- 业务失败常走 HTTP 200 + 业务 `code`，不要只盯 HTTP 状态码。

## 常查目录

- `src/main/resources/static/*.html`
- `src/main/resources/static/pages/game`
- `src/main/resources/static/pages/admin`
- `src/main/resources/static/js/core/api`
- `src/main/resources/static/js/pages`
- `src/main/resources/static/js/modules`
- `src/main/java/com/xiuxian/game/modules/*/controller`
- `src/main/java/com/xiuxian/game/modules/*/service`
- `src/main/java/com/xiuxian/game/dto`

如果要按清单逐项展开，读取 `references/trace-checklist.md`。

## 追踪步骤

1. 找到用户动作所在页面和按钮。
2. 找到事件绑定与调用入口。
3. 找到实际请求 URL、请求方法、参数构造。
4. 对应到 controller。
5. 顺着 service 看数据来源与返回 DTO。
6. 回到前端核对页面使用的字段和空值处理。
7. 如果不一致，明确是请求错、返回错、映射错，还是渲染错。

## 输出模板

```markdown
## 调用入口
- 页面：`...`
- 事件：`...`
- JS 文件：`...`

## 请求链路
1. `...`
2. `...`
3. `...`

## 后端落点
- Controller：`...`
- Service：`...`
- DTO / 返回结构：`...`

## 页面依赖字段
- `...`

## 契约核对结论
- 一致 / 不一致
- 不一致点：...

## 影响范围
- ...
```

## 约束

- 不能只给接口路径，必须把页面入口和渲染字段一起说清。
- 不能只看 controller，必须回到页面确认字段消费方式。
- 出现“接口返回正常但页面不显示”时，优先检查字段名、包装层级、空值兼容。
