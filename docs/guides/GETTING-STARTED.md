# 快速上手指南

> 完成本指南后，你将拥有一个在本地完整运行的修仙挂机游戏实例，包括游戏前端和管理后台。  
> **预计时间**：30 分钟（网络良好情况下）

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-27

---

## 前置条件

在开始前，确认你已安装以下工具：

| 工具 | 版本要求 | 检查命令 |
|------|---------|----------|
| Java JDK | 1.8+ | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| MySQL | 8.0+ | `mysql --version` |
| Redis | 6.0+ | `redis-cli --version` |
| Git | 任意版本 | `git --version` |

> **可选**：Docker + Docker Compose（用于一键启动，跳过数据库和 Redis 安装步骤）

---

## 步骤 1：克隆项目

```bash
git clone https://github.com/SSJLYY/xiuxian.git
cd xiuxian
```

---

## 步骤 2：初始化数据库

### 方式 A：本地 MySQL（推荐开发用）

```bash
# 登录 MySQL
mysql -u root -p

# 在 MySQL 命令行中执行
CREATE DATABASE xiuxian_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

# 导入初始化脚本
mysql -u root -p xiuxian_game < src/main/resources/init-database.sql
```

验证导入成功：
```bash
mysql -u root -p -e "USE xiuxian_game; SELECT COUNT(*) AS tables FROM information_schema.tables WHERE table_schema='xiuxian_game';"
# 应该返回 50+ 张表
```

### 方式 B：Docker 一键启动（无需安装 MySQL）

```bash
docker-compose up -d mysql
# 等待约 30 秒数据库就绪
```

---

## 步骤 3：启动 Redis

项目引入了 Redis 作为主缓存层，**启动应用前必须先保证 Redis 可访问**。

### 方式 A：本地安装

```bash
# macOS
brew install redis
brew services start redis

# Ubuntu / Debian
sudo apt-get install redis-server
sudo systemctl start redis

# 验证
redis-cli ping   # 返回 PONG 即成功
```

### 方式 B：Docker（推荐，零安装）

```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

### 方式 C：Docker Compose（项目内置）

```bash
docker-compose up -d redis
```

> **无需密码**：默认配置 `spring.redis.password=` 为空，本地开发直接启动即可。  
> **Redis 不可用时**：应用不会崩溃，会自动降级到进程内本地缓存，但多实例部署下数据不共享，详见 [缓存架构](../architecture/CACHE-ARCHITECTURE.md)。

---

## 步骤 4：配置应用

编辑 `src/main/resources/application.properties`，修改数据库和 Redis 连接信息：

```properties
# 数据库连接
spring.datasource.url=jdbc:mysql://localhost:3306/xiuxian_game?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的数据库密码

# Redis 连接（本地默认无需修改）
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.password=         # 本地无密码留空

# 服务端口（默认 8082）
server.port=8082
```

> **注意**：不要提交含真实密码的配置文件。生产环境使用环境变量覆盖。

---

## 步骤 5：构建并启动

```bash
# 编译（跳过测试加速构建）
mvn clean package -DskipTests

# 启动应用
java -jar target/xiuxian-game*.jar
```

或者使用快捷脚本：
```bash
# Windows
start.bat

# Linux/macOS
./start.sh
```

看到如下日志说明启动成功：
```
Started XiuxianGameApplication in X.XXX seconds
```

---

## 步骤 6：访问游戏

| 页面 | 地址 | 账号 |
|------|------|------|
| 玩家游戏登录 | http://localhost:8082/login.html | 可在页面注册 |
| 管理员后台 | http://localhost:8082/adminLogin.html | admin / admin123 |
| 游戏主界面 | http://localhost:8082/game.html | 登录后自动跳转 |

> ⚠️ **生产环境必须修改默认管理员密码！**

---

## 常见问题

### Redis 连接失败
```
# 错误信息：Unable to connect to Redis
```
**原因**：Redis 服务未启动。

**解决**：
1. 确认 Redis 正在运行：`redis-cli ping`（应返回 PONG）
2. 如果不想安装 Redis，可以用 Docker：`docker run -d --name redis -p 6379:6379 redis:7-alpine`
3. 应用仍可启动但会降级到本地缓存，适合临时开发调试

---

### 数据库连接失败
```
# 错误信息：Communications link failure
```
**原因**：数据库未启动或连接参数错误。

**解决**：
1. 确认 MySQL 正在运行：`systemctl status mysql`（Linux）或检查服务管理器（Windows）
2. 用 `mysql -u root -p` 手动连接，确认密码正确
3. 确认数据库名称为 `xiuxian_game`

---

### 端口被占用
```
# 错误信息：Address already in use: 8082
```
**解决**：
```bash
# Windows
netstat -ano | findstr :8082
taskkill /PID <PID> /F

# Linux
lsof -i :8082 | awk 'NR>1{print $2}' | xargs kill -9
```

或修改 `application.properties` 中的 `server.port` 为其他端口。

---

### 前端页面样式错乱
**解决**：强制刷新浏览器缓存（`Ctrl+Shift+R` 或 `Cmd+Shift+R`）

---

### 登录后频繁跳回登录页
**原因**：JWT Token 过期时间配置问题或系统时间不同步。

**解决**：
```properties
# application.properties
jwt.expiration=86400000   # 24小时（毫秒）
```

---

## 代码审查机制

本项目采用严格的代码审查机制保证代码质量：

### 必读文档
- **[代码审查标准](../standards/CODE-REVIEW-STANDARDS.md)** — 审查检查清单和优先级定义
- **[代码审查流程](../standards/CODE-REVIEW-PROCESS.md)** — PR流程和角色职责
- **[代码审查模板](../standards/CODE-REVIEW-TEMPLATES.md)** — 标准化审查评论模板

### 提交PR前自查
```bash
# 1. 本地测试通过
mvn test

# 2. 对照审查清单检查
# 见 CODE-REVIEW-STANDARDS.md 第2节

# 3. 确保无Blocker级别问题
```

### 审查优先级
| 级别 | 说明 | 处理方式 |
|------|------|----------|
| 🔴 Blocker | 安全漏洞、数据风险、并发问题 | 必须修复 |
| 🟡 Major | 性能问题、测试缺失、命名混乱 | 应该修复 |
| 💭 Minor | 风格建议、文档缺失 | 可选修复 |

---

## 下一步

- [后端架构总览](../architecture/BACKEND-ARCHITECTURE.md) — 了解项目的技术设计
- [缓存架构](../architecture/CACHE-ARCHITECTURE.md) — Redis 双层缓存与降级策略
- [后端编码规范](../standards/BACKEND-CODING-STANDARDS.md) — 开始写代码前必读
- **[代码审查标准](../standards/CODE-REVIEW-STANDARDS.md)** — 提交PR前必读
- [API 总览](../api/API-OVERVIEW.md) — 接口文档入口
