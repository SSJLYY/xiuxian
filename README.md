# 修仙挂机游戏

> 一款基于 Spring Boot + 原生 JS 的修仙主题挂机游戏，支持修炼、战斗、宠物、宗门等完整游戏系统。

[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 快速开始

**前置条件**：Java 8+、MySQL 8.0+、Maven 3.6+

```bash
# 1. 克隆并初始化数据库
git clone https://github.com/SSJLYY/xiuxian.git && cd xiuxian
mysql -u root -p xiuxian_game < src/main/resources/init-database.sql

# 2. 修改数据库密码
# 编辑 src/main/resources/application.properties

# 3. 构建并启动
mvn clean package -DskipTests
java -jar target/xiuxian-game*.jar
```

启动后访问：
- 🎮 玩家游戏：http://localhost:8082/login.html
- 👑 管理后台：http://localhost:8082/adminLogin.html（默认 admin / admin123）

> **Docker 一键启动**：`docker-compose up -d`

---

## 核心功能

| 功能 | 说明 |
|------|------|
| 修炼/挂机 | 在线/离线自动获得经验和灵石，境界突破仪式感机制 |
| 战斗系统 | 回合制战斗，含暴击、速度优势、宠物参战 |
| 宠物系统 | 10种宠物（普通→神话），捕获/培养/进化/战斗 |
| 技能连招 | 技能组合触发连招特效，COMBO 加成 |
| 宗门协作 | 创建/加入宗门，协同挑战宗门BOSS |
| 叙事系统 | 6个核心NPC，分支对话树，传说图鉴 |
| 地图系统 | 多区域探索，节点解锁，挂机地点选择 |
| 排行榜/成就 | 等级榜/战力榜/财富榜，成就徽章墙 |
| 管理后台 | 玩家管理、游戏配置、数据统计、安全控制 |

---

## 技术架构

```
前端（原生 JS + HTML）
        │ HTTP/JSON
        ▼
Spring Boot（端口 8082）
    ├── Spring Security + JWT（双认证系统）
    ├── Controller → Service → MyBatis-Plus Mapper
    └── MySQL（xiuxian_game 数据库）
```

**双认证系统**：游戏端（`/api/auth/*`）和管理端（`/api/admin/auth/*`）Token 完全隔离，互不影响。

---

## 项目规模

- **后端**：317 个 Java 文件，44 个 Controller，50+ 个 Service，62 个 Mapper
- **前端**：20 个 HTML 页面，40 个 JS 文件（含音频引擎、地图系统、叙事系统）
- **数据库**：50+ 张数据表

---

## 文档

> 📚 **[开发文档中心 →](./docs/README.md)**（架构、API、规范、数据库设计）

| 快速链接 | |
|---------|---|
| [快速上手指南](./docs/guides/GETTING-STARTED.md) | 30分钟跑通项目 |
| [后端架构总览](./docs/architecture/BACKEND-ARCHITECTURE.md) | 分层结构、设计规范 |
| [API 参考](./docs/api/API-OVERVIEW.md) | 接口文档入口 |
| [数据库设计](./docs/architecture/DATABASE-DESIGN.md) | 表结构与数值公式 |
| [后端编码规范](./docs/standards/BACKEND-CODING-STANDARDS.md) | 开发前必读 |
| [游戏设计文档](./GDD-修仙挂机游戏设计文档.md) | 玩法机制与数值平衡 |

---

## 常见问题

**数据库连接失败**：确认 MySQL 已启动，`application.properties` 中密码正确，数据库名为 `xiuxian_game`。

**端口冲突**：修改 `application.properties` 中的 `server.port`，或 `taskkill /PID <PID> /F`。

**页面样式错乱**：强制刷新（`Ctrl+Shift+R`）。

更多问题见 [完整故障排查指南](./docs/guides/GETTING-STARTED.md#常见问题)。

---

## 贡献

1. Fork 本项目，创建分支：`git checkout -b feature/xxx`
2. 提交时附上文档更新
3. 创建 Pull Request，描述变更内容和测试方式

提交规范：`feat:` / `fix:` / `docs:` / `refactor:` / `chore:`

---

## 许可证

MIT © [SSJLYY](https://github.com/SSJLYY/xiuxian)
