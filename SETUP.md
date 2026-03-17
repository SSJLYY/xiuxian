# 修仙游戏项目启动指南

## 环境要求

- Java 1.8+
- Docker 和 Docker Compose（推荐）
- 或者本地安装 MySQL 8.0+

## 快速启动（推荐使用 Docker）

### 1. 使用 Docker Compose 启动

```bash
# 在项目根目录执行
docker-compose up -d
```

这将启动：
- MySQL 8.0 数据库（端口 3306）
- Redis 缓存服务（端口 6379）
- 修仙游戏应用（端口 8081）

### 2. 访问游戏

- **玩家登录页面**: http://localhost:8081/login.html
- **管理后台**: http://localhost:8081/admin.html
- **API 文档**: http://localhost:8081/actuator/health

### 3. 默认账号

- **管理员账号**: admin / SecureAdminPassword2024!
- **玩家账号**: 通过注册页面创建

## 本地启动（不使用 Docker）

### 1. 安装 MySQL 8.0

1. 下载并安装 MySQL 8.0
2. 创建数据库：
   ```sql
   CREATE DATABASE xiuxian_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 执行初始化脚本：
   ```bash
   mysql -u root -p xiuxian_game < src/main/resources/init-database.sql
   ```

### 2. 配置数据库连接

编辑 `src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/xiuxian_game?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的密码
```

### 3. 启动应用

#### 使用 Maven（推荐）
```bash
# 编译项目
mvn clean package -DskipTests

# 启动应用
java -jar target/xiuxian-game.jar
```

#### 使用 IDE
直接运行 `XiuxianGameApplication.java` 的 main 方法

### 4. 访问游戏

- **玩家登录页面**: http://localhost:8082/login.html
- **管理后台**: http://localhost:8082/admin.html

## 项目结构说明

```
xiuxian-game/
├── src/main/java/com/xiuxian/game/
│   ├── controller/          # REST API 控制器
│   ├── service/            # 业务逻辑服务
│   ├── entity/             # 数据库实体
│   ├── mapper/             # MyBatis Mapper
│   ├── dto/                # 数据传输对象
│   ├── config/             # 配置类
│   └── security/           # 安全相关
├── src/main/resources/
│   ├── static/             # 前端静态资源
│   │   ├── index.html      # 游戏主页
│   │   ├── login.html      # 登录页面
│   │   ├── pets.html       # 宠物系统
│   │   └── js/             # JavaScript 文件
│   ├── init-database.sql   # 数据库初始化脚本
│   └── application.properties # 应用配置
└── docker-compose.yml      # Docker 编排配置
```

## 核心功能

### 1. 用户系统
- 用户注册/登录
- JWT Token 认证
- 管理员权限控制

### 2. 玩家系统
- 玩家档案管理
- 属性系统（攻击、防御、生命、法力、速度）
- 境界体系（练气期 → 筑基期 → 金丹期 → 元婴期）

### 3. 修炼系统
- 在线修炼获得经验
- 离线收益计算
- 境界突破

### 4. 战斗系统
- PVE 战斗
- 怪物生成
- 战斗日志

### 5. 技能系统
- 技能学习
- 技能升级
- 技能商店

### 6. 宠物系统
- 宠物捕获
- 宠物培养
- 宠物训练

### 7. 任务系统
- 每日任务
- 每周任务
- 每月任务

## API 接口

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出

### 玩家接口
- `GET /api/player/profile` - 获取玩家信息
- `POST /api/player/cultivate` - 开始修炼
- `POST /api/player/cultivate/stop` - 停止修炼

### 战斗接口
- `GET /api/combat/generate-monster` - 生成怪物
- `POST /api/combat/start` - 开始战斗
- `GET /api/combat/history` - 战斗历史

### 技能接口
- `GET /api/shop/skills` - 技能商店
- `POST /api/shop/skills/{skillId}/buy` - 购买技能

## 常见问题

### 1. 数据库连接失败
检查 MySQL 服务是否运行，数据库配置是否正确

### 2. 端口被占用
修改 `application.properties` 中的 `server.port`

### 3. 前端无法访问
检查 CORS 配置是否正确

## 技术栈

- **后端**: Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1
- **数据库**: MySQL 8.0
- **缓存**: Redis（可选）
- **前端**: 原生 HTML/CSS/JavaScript
- **认证**: JWT Token

## 开发建议

1. **数据库迁移**: 使用 Flyway 或 Liquibase 管理数据库版本
2. **API 文档**: 使用 Swagger/OpenAPI 生成 API 文档
3. **单元测试**: 添加针对核心业务逻辑的单元测试
4. **性能监控**: 使用 Spring Boot Actuator 监控应用性能

## 贡献指南

1. Fork 项目
2. 创建特性分支: `git checkout -b feature/AmazingFeature`
3. 提交更改: `git commit -m 'Add some AmazingFeature'`
4. 推送分支: `git push origin feature/AmazingFeature`
5. 提交 Pull Request

## 许可证

MIT License