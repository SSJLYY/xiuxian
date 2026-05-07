---
name: xiuxian-project-context
description: 建立 xiuxian 项目的整体上下文，快速定位代码入口、模块边界、运行方式、文档入口和排查起点。用于用户要求通读项目、先理解仓库、找某个功能在哪、判断改动落点、梳理项目结构、准备接手开发或启动排障时。
---

# Xiuxian Project Context

## 目标

先把这个仓库是什么、怎么跑、代码主要落在哪、前后端怎么对应、文档从哪里读明白，再进入定位或改代码。

## 项目基线

- 仓库类型：Spring Boot 单体应用，静态前端资源由后端直接托管。
- 后端栈：Java 8、Spring Boot 2.7.18、MyBatis-Plus、Spring Security、JWT、Redis、Log4j2。
- 前端栈：原生 HTML + JS + CSS，没有独立前端工程。
- 默认端口：`8082`
- 统一响应：`ApiResponse<T>`
- 双端认证：玩家端 `/api/*` 与管理端 `/api/admin/*` 分离。

## 先读哪里

按这个顺序读取，避免一上来全仓乱扫：

1. `README.md`
2. `docs/README.md`
3. `docs/guides/GETTING-STARTED.md`
4. `docs/architecture/BACKEND-ARCHITECTURE.md`
5. `docs/api/API-OVERVIEW.md`
6. `src/main/java/com/xiuxian/game/XiuxianGameApplication.java`
7. `src/main/resources/application.properties`

如果问题涉及玩法、数值、成长线，再看：

- `docs/design/GDD-修仙挂机游戏设计文档.md`
- `docs/standards/OPTIMIZATION-NOTES.md`

如果需要更完整的项目结构或验证清单，按需读取：

- `references/project-map.md`
- `references/verification.md`

## 关键目录

### 后端

- 启动类：`src/main/java/com/xiuxian/game/XiuxianGameApplication.java`
- 根包：`src/main/java/com/xiuxian/game`
- 公共层：`common`
- DTO：`dto`
- 校验：`validation`
- 业务模块：`modules/*`

高频业务模块：

- `player`
- `cultivation`
- `combat`
- `skill`
- `pet`
- `equipment`
- `quest`
- `guild`
- `auction`
- `ranking`
- `map`
- `activity`
- `giftcode`
- `offline`
- `admin`

默认排查链路：

`controller -> service -> mapper -> entity`

### 前端

- 根目录：`src/main/resources/static`
- 顶层页面：`login.html`、`game.html`、`adminLogin.html`
- 页面目录：`pages/game`、`pages/admin`
- JS 总入口：`js`
- API 封装：`js/core/api`
- 页面逻辑：`js/pages`
- 业务模块：`js/modules`

默认页面排查链路：

`HTML -> js/pages 或 js/modules -> js/core/api -> controller -> service`

## 运行与验证
验证命令和注意事项不要堆在回答正文里；需要时读取 `references/verification.md`。

## 高价值事实

- `application.properties` 使用 `UTF-8`，但仓库里历史上出现过中文乱码文件，看到中文异常时要考虑编码污染而不是只看逻辑。
- 这个项目很多问题不是页面模板本身，而是前端字段名、`GameApi.js`/服务层归一化、DTO 形状不一致。
- 玩家端和管理端的认证、接口前缀、登录页都不同，排查 401/403/跳登录时必须先区分端别。
- 缓存与降级存在于这个仓库里，查数据不一致时要同时考虑 DB、Redis、本地回退逻辑。

## 输出要求

做项目理解类回答时，优先按这个结构输出：

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

## 关键路径
- `...`
- `...`

## 风险与依赖
- ...
```

## 分流

- 要理解仓库或找入口：用本技能。
- 要定位 bug 根因：转 `xiuxian-bug-locate`。
- 要梳理页面到接口调用链：转 `xiuxian-api-trace`。
- 要制定实现方案或改功能：转 `xiuxian-feature-implementation`。
