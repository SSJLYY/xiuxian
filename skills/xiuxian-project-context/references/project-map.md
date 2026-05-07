# Project Map

## 核心技术

- 后端：Java 8、Spring Boot 2.7.18、MyBatis-Plus、Spring Security、JWT、Redis、Log4j2
- 前端：原生 HTML、JS、CSS
- 运行方式：Spring Boot 直接托管静态资源
- 默认端口：`8082`

## 入口文件

- 启动类：`src/main/java/com/xiuxian/game/XiuxianGameApplication.java`
- 配置：`src/main/resources/application.properties`
- 玩家登录页：`src/main/resources/static/login.html`
- 玩家主界面：`src/main/resources/static/game.html`
- 管理登录页：`src/main/resources/static/adminLogin.html`

## 主要文档

- `README.md`
- `docs/README.md`
- `docs/guides/GETTING-STARTED.md`
- `docs/architecture/BACKEND-ARCHITECTURE.md`
- `docs/api/API-OVERVIEW.md`
- `docs/design/GDD-修仙挂机游戏设计文档.md`

## 后端目录

- 根包：`src/main/java/com/xiuxian/game`
- 公共层：`common`
- DTO：`dto`
- 校验：`validation`
- 业务模块：`modules/*`

## 高频后端模块

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

## 前端目录

- 根目录：`src/main/resources/static`
- 页面目录：`pages/game`、`pages/admin`
- API 封装：`js/core/api`
- 页面逻辑：`js/pages`
- 业务模块：`js/modules`

## 高频前端模块

- `player`
- `combat`
- `skills`
- `pets`
- `petEvolution`
- `inventory`
- `guild`
- `mail`
- `map`
- `quest`
- `shop`
- `ranking`
- `activity`
- `giftcode`
- `vip`

## 默认排查链路

- 页面：`HTML -> js/pages 或 js/modules -> js/core/api -> controller -> service`
- 接口：`controller -> service -> mapper -> entity`
