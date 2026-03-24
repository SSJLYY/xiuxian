# 快速上手指南

> 完成本指南后，你将拥有一个在本地完整运行的修仙挂机游戏实例，包括游戏前端和管理后台。  
> **预计时间**：30 分钟（网络良好情况下）

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-24

---

## 前置条件

在开始前，确认你已安装以下工具：

| 工具 | 版本要求 | 检查命令 |
|------|---------|----------|
| Java JDK | 1.8+ | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| MySQL | 8.0+ | `mysql --version` |
| Git | 任意版本 | `git --version` |

> **可选**：Docker + Docker Compose（用于一键启动，跳过数据库安装步骤）

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

## 步骤 3：配置应用

编辑 `src/main/resources/application.properties`，修改数据库连接信息：

```properties
# 数据库连接
spring.datasource.url=jdbc:mysql://localhost:3306/xiuxian_game?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的数据库密码

# 服务端口（默认 8082）
server.port=8082
```

> **注意**：不要提交含真实密码的配置文件。生产环境使用环境变量覆盖。

---

## 步骤 4：构建并启动

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

## 步骤 5：访问游戏

| 页面 | 地址 | 账号 |
|------|------|------|
| 玩家游戏登录 | http://localhost:8082/login.html | 可在页面注册 |
| 管理员后台 | http://localhost:8082/adminLogin.html | admin / admin123 |
| 游戏主界面 | http://localhost:8082/game.html | 登录后自动跳转 |

> ⚠️ **生产环境必须修改默认管理员密码！**

---

## 常见问题

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

## 下一步

- [后端架构总览](../architecture/BACKEND-ARCHITECTURE.md) — 了解项目的技术设计
- [后端编码规范](../standards/BACKEND-CODING-STANDARDS.md) — 开始写代码前必读
- [API 总览](../api/API-OVERVIEW.md) — 接口文档入口
