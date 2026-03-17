# 🎮 Xiuxian Idle (Xiuxian Idle Game)

一个基于Spring Boot + 原生JavaScript的xiuxian主题挂机游戏，支持玩家xiuxian、战斗、装备、宠物等完整游戏系统。采用前后端分离架构，提供丰富的xiuxian体验和完整的游戏生态。

[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.3.1-blue.svg)](https://baomidou.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.1.0-red.svg)](CHANGELOG.md)

## 🎮 特色

### 🎯 核心玩法
- **🧙‍♂️ xiuxian主题**：体验从凡人到仙人的xiuxian之旅，完整的境界体系
- **⏰ 挂机玩法**：离线也能获得收益，真正的放置类游戏体验
- **🔮 技能系统**：学习各种xiuxian技能，提升战斗力和xiuxian效率
- **📋 任务系统**：完成每日、每周、每月任务获得丰厚奖励
- **⚔️ 装备系统**：收集和强化装备，提升角色属性
- **🐾 宠物系统**：捕获、培养各种灵兽，成为xiuxian路上的伙伴
- **🏪 商城系统**：购买物品、装备和技能，加速成长
- **⚡ 战斗系统**：挑战各种怪物，获得经验和奖励
- **🌟 境界提升**：从练气期到元婴期的完整xiuxian境界体系
- **💎 离线奖励**：离线时间越长，奖励越丰厚

### 🆕 新增功能 (v1.1.0)
- **📧 邮件系统**：接收系统邮件、活动奖励和补偿物品
- **📢 公告系统**：获取游戏更新、活动信息和重要通知
- **🏆 排行榜系统**：等级榜、战力榜、财富榜等多维度排名
- **🎖️ 成就系统**：完成成就获得奖励和特殊称号
- **🏛️ 宗门系统**：创建或加入宗门，与其他玩家协作
- **🛒 拍卖行系统**：与其他玩家交易装备、材料和宠物
- **💎 VIP系统**：充值获得元宝和VIP特权
- **🎉 活动系统**：参与限时活动获得特殊奖励
- **🎁 礼包码系统**：兑换礼包码获得游戏奖励
- **⚙️ 配置管理**：管理员可灵活调整游戏参数
- **🛡️ 安全机制**：防作弊、频率限制、IP黑名单
- **📊 数据统计**：详细的运营数据和玩家行为分析

## 🏗️ 系统架构

### 🔐 双认证系统架构
项目采用完全分离的双认证系统设计，游戏系统和管理系统互不干扰：

```
┌─────────────────────────────────────────────────────────────┐
│                    xiuxian挂机游戏系统                           │
├─────────────────────────────────────────────────────────────┤
│  前端层                                                      │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   游戏前端       │    │   管理前端       │                │
│  │  login.html     │    │  adminLogin.html│                │
│  │  index.html     │    │  admin.html     │                │
│  │  js/auth.js     │    │  js/admin-auth.js│               │
│  │  js/api.js      │    │  js/admin-api.js│                │
│  │  authToken      │    │  adminToken     │                │
│  └─────────────────┘    └─────────────────┘                │
├─────────────────────────────────────────────────────────────┤
│  后端层                                                      │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   游戏API       │    │   管理API       │                │
│  │  /api/auth/*    │    │  /api/admin/auth/*│              │
│  │  /api/player/*  │    │  /api/admin/*   │                │
│  │  AuthService    │    │  AdminAuthService│               │
│  │  SecurityFilter │    │  AdminSecurityFilter│            │
│  └─────────────────┘    └─────────────────┘                │
├─────────────────────────────────────────────────────────────┤
│  数据层                                                      │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                MySQL数据库                              ││
│  │  users表 (role字段区分PLAYER/ADMIN)                     ││
│  │  player_profiles, pets, skills... (游戏数据)            ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### 🔑 认证系统对比
| 特性 | 游戏认证系统 | 管理员认证系统 |
|------|-------------|---------------|
| **前端文件** | `js/auth.js`, `js/api.js` | `js/admin-auth.js`, `js/admin-api.js` |
| **后端控制器** | `AuthController` | `AdminAuthController` |
| **服务层** | `AuthService` | `AdminAuthService` |
| **安全过滤器** | `SecurityFilter` | `AdminSecurityFilter` |
| **Token存储** | `localStorage.authToken` | `localStorage.adminToken` |
| **API前缀** | `/api/auth/*`, `/api/player/*` | `/api/admin/auth/*`, `/api/admin/*` |
| **用户角色** | `PLAYER` | `ADMIN` |
| **登录页面** | `login.html` | `adminLogin.html` |
| **主页面** | `index.html` | `admin.html` |

### 🛡️ 安全隔离特性
- **Token隔离**：游戏token和管理员token完全独立
- **API隔离**：不同的API路径和控制器
- **权限隔离**：独立的权限验证逻辑
- **前端隔离**：独立的JavaScript认证管理器
- **会话隔离**：互不影响的用户会话

## 🛠️ 技术栈

### 🔧 后端技术栈
| 技术 | 版本 | 用途 | 特点 |
|------|------|------|------|
| **Java** | 1.8 | 开发语言 | 稳定可靠，生态丰富 |
| **Spring Boot** | 2.7.18 | 应用框架 | 快速开发，自动配置 |
| **Spring Security** | 5.7.x | 安全框架 | JWT认证，权限控制 |
| **MyBatis-Plus** | 3.5.3.1 | ORM框架 | 代码生成，条件构造器 |
| **MySQL** | 8.0+ | 关系数据库 | 高性能，事务支持 |
| **HikariCP** | 4.0.x | 连接池 | 高性能数据库连接池 |
| **Log4j2** | 2.17.x | 日志框架 | 异步日志，性能优异 |
| **Maven** | 3.6+ | 构建工具 | 依赖管理，项目构建 |

### 🎨 前端技术栈
| 技术 | 版本 | 用途 | 特点 |
|------|------|------|------|
| **JavaScript** | ES6+ | 开发语言 | 原生JS，无框架依赖 |
| **HTML5** | 5.0 | 页面结构 | 语义化标签，现代特性 |
| **CSS3** | 3.0 | 样式设计 | 动画效果，响应式布局 |
| **Tailwind CSS** | 3.x | CSS框架 | 原子化CSS，快速开发 |
| **Font Awesome** | 6.4.0 | 图标库 | 丰富图标，矢量图标 |
| **Google Fonts** | - | 字体库 | 中文字体，古风设计 |

### 🔨 开发工具
| 工具 | 用途 | 说明 |
|------|------|------|
| **IntelliJ IDEA** | 开发IDE | 推荐的Java开发环境 |
| **Git** | 版本控制 | 分布式版本管理 |
| **MySQL Workbench** | 数据库管理 | 可视化数据库工具 |
| **Postman** | API测试 | 接口测试和调试 |
| **Docker** | 容器化 | 应用打包和部署 |

### 🏗️ 架构特点
- **前后端分离**：RESTful API设计，前后端独立开发部署
- **分层架构**：Controller-Service-Mapper三层架构，职责清晰
- **模块化设计**：功能模块独立，便于维护和扩展
- **统一异常处理**：全局异常处理机制，统一错误响应格式
- **AOP切面编程**：日志记录、性能监控、权限校验
- **事务管理**：声明式事务，保证数据一致性

## 📦 项目结构

```
xiuxian-game/
├── skills/                            # 技能系统文档
│   ├── README.md                      # 技能系统概述
│   ├── skill-config.md                # 技能配置文档
│   ├── implementation.md              # 技能实现文档
│   ├── test-guide.md                  # 技能测试指南
│   └── development-guide.md           # 技能开发指南
├── src/main/java/com/xiuxian/game/
│   ├── XiuxianGameApplication.java    # 主启动类
│   ├── controller/                    # REST API控制器层
│   │   ├── AuthController.java        # 用户认证
│   │   ├── PlayerController.java      # 玩家管理
│   │   ├── PetController.java         # 宠物系统
│   │   ├── SkillController.java       # 技能系统
│   │   ├── QuestController.java       # 任务系统
│   │   ├── EquipmentController.java   # 装备系统
│   │   └── CombatController.java      # 战斗系统
│   ├── service/                       # 业务逻辑层
│   │   ├── PlayerService.java         # 玩家服务
│   │   ├── PetService.java           # 宠物服务
│   │   ├── SkillService.java         # 技能服务
│   │   ├── QuestService.java         # 任务服务
│   │   └── AuthService.java          # 认证服务
│   ├── mapper/                        # MyBatis数据访问层
│   ├── entity/                        # JPA实体类
│   │   ├── User.java                 # 用户实体
│   │   ├── PlayerProfile.java        # 玩家档案
│   │   ├── Pet.java                  # 宠物模板
│   │   ├── PlayerPet.java            # 玩家宠物
│   │   ├── Skill.java                # 技能
│   │   └── Equipment.java            # 装备
│   ├── dto/                          # 数据传输对象
│   │   ├── request/                  # 请求DTO
│   │   └── response/                 # 响应DTO
│   ├── config/                       # 配置类
│   │   ├── SecurityConfig.java       # 安全配置
│   │   ├── CorsConfig.java          # 跨域配置
│   │   └── MybatisPlusConfig.java   # MyBatis配置
│   ├── security/                     # 安全相关
│   │   ├── JwtTokenProvider.java     # JWT工具
│   │   └── JwtAuthenticationFilter.java # JWT过滤器
│   ├── exception/                    # 异常处理
│   └── util/                         # 工具类
├── src/main/resources/
│   ├── static/                       # 前端静态资源
│   │   ├── index.html               # 游戏主页
│   │   ├── login.html               # 登录页面
│   │   ├── pets.html                # 宠物系统页面
│   │   ├── cultivate.html           # 修炼页面
│   │   ├── admin.html               # 管理后台
│   │   ├── css/style.css            # 样式文件
│   │   ├── js/                      # JavaScript文件
│   │   │   ├── main.js              # 主要逻辑
│   │   │   ├── api.js               # API调用
│   │   │   ├── auth.js              # 认证逻辑
│   │   │   ├── game.js              # 游戏逻辑
│   │   │   ├── pets.js              # 宠物系统
│   │   │   └── utils.js             # 工具函数
│   │   └── images/                  # 图片资源
│   ├── init-database.sql            # 数据库初始化脚本
│   ├── application.properties       # 应用配置
│   └── log4j2-spring.xml           # 日志配置
├── logs/                            # 日志文件目录
├── target/                          # Maven构建输出
├── pom.xml                          # Maven依赖配置
├── Dockerfile                       # Docker镜像配置
├── docker-compose.yml               # Docker编排配置
├── start.bat                        # Windows启动脚本
├── start.sh                         # Linux/macOS启动脚本
└── README.md                        # 项目文档
```

## 🚀 快速开始

#### 1. 环境准备
- ☕ Java 8+ (推荐Java 11)
- 🐬 MySQL 5.7+ (推荐MySQL 8.0)
- 📦 Maven 3.6+ (用于构建项目)
- 🐳 Docker & Docker Compose (推荐用于快速启动)

#### 2. 快速启动（推荐使用 Docker）

使用 Docker Compose 一键启动所有服务：

```bash
# 在项目根目录执行
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f xiuxian-game
```

这将启动：
- MySQL 8.0 数据库（端口 3306）
- Redis 缓存服务（端口 6379，可选）
- 修仙游戏应用（端口 8081）

#### 3. 本地启动（不使用 Docker）

##### 3.1 安装并配置 MySQL
1. 下载并安装 MySQL 8.0
2. 创建数据库：
   ```sql
   CREATE DATABASE xiuxian_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 执行初始化脚本：
   ```bash
   mysql -u root -p xiuxian_game < src/main/resources/init-database.sql
   ```

##### 3.2 配置数据库连接
编辑 `src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/xiuxian_game?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的密码
```

##### 3.3 项目构建与启动
```bash
# 编译项目
mvn clean package -DskipTests

# 启动应用
java -jar target/xiuxian-game.jar
```

#### 4. 访问游戏
- 🎮 玩家游戏登录：http://localhost:8081/login.html (Docker) 或 http://localhost:8082/login.html (本地)
- 👑 管理员后台登录：http://localhost:8081/admin.html (Docker) 或 http://localhost:8082/admin.html (本地)



### 🐳 Docker部署（推荐生产环境）

#### 开发环境快速启动
```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看应用日志
docker-compose logs -f xiuxian-game
```

#### 生产环境配置
```
# docker-compose.prod.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: xiuxian-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123456}
      MYSQL_DATABASE: xiuxian_game
      TZ: Asia/Shanghai
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init-database.sql:/docker-entrypoint-initdb.d/init-database.sql:ro
    ports:
      - "3306:3306"
    networks:
      - xiuxian-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  xiuxian-game:
    build: .
    container_name: xiuxian-game
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/xiuxian_game?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123456}
      SERVER_PORT: 8082
      TZ: Asia/Shanghai
    ports:
      - "8082:8082"
    networks:
      - xiuxian-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://192.168.215.144:8082/login.html"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

volumes:
  mysql_data:

networks:
  xiuxian-network:
    driver: bridge
```

启动生产环境：
```bash
# 使用生产环境配置启动
docker-compose -f docker-compose.prod.yml up -d

# 停止服务
docker-compose -f docker-compose.prod.yml down
```

## 🎯 核心功能

### 👤 用户认证系统
- **用户注册/登录**：支持用户名、邮箱注册，JWT Token认证
- **安全机制**：密码加密存储，Token过期自动刷新
- **权限管理**：普通用户和管理员角色区分

### 🧙‍♂️ 玩家系统
- **档案管理**：昵称、等级、经验、境界等基础信息
- **属性系统**：攻击、防御、生命、法力、速度五大属性
- **境界体系**：练气期(1-10级) → 筑基期(11-15级) → 金丹期(16-19级) → 元婴期(20级+)
- **资源管理**：灵石、修炼点数、贡献点、属性点、技能点

### ⚡ 修炼系统
- **在线修炼**：实时获得经验和修炼点数
- **离线收益**：离线时间自动计算修炼收益
- **境界突破**：达到等级要求自动突破境界，获得额外奖励
- **修炼速度**：可通过技能和装备提升修炼效率

### 🔮 技能系统
- **技能学习**：消耗灵石学习各种xiuxian技能
- **技能升级**：通过使用和xiuxian提升技能等级
- **技能类型**：xiuxian类、攻击类、防御类、辅助类
- **技能效果**：提升属性、增加修炼速度、战斗加成

### 🐾 宠物系统 (v1.1.0新增)
- **宠物捕获**：根据等级解锁不同稀有度的宠物
- **宠物培养**：喂食、训练提升宠物属性和忠诚度
- **宠物类型**：灵兽、妖兽、神兽三大类型
- **稀有度系统**：普通、稀有、史诗、传说、神话五个等级
- **宠物技能**：每个宠物拥有独特的战斗技能

### 📋 任务系统
- **每日任务**：每天刷新，完成获得经验和灵石
- **每周任务**：每周刷新，奖励更加丰厚
- **每月任务**：每月刷新，提供大量贡献点
- **任务类型**：修炼类、战斗类、收集类、成长类

### ⚔️ 装备系统
- **装备类型**：武器、胸甲、头盔、靴子、盾牌、戒指六大类
- **品质等级**：1-5星品质，品质越高属性加成越强
- **装备强化**：消耗材料强化装备，提升属性
- **套装效果**：同系列装备提供额外套装加成

### 🏪 商城系统
- **物品商店**：购买消耗品、材料、特殊物品
- **装备商店**：购买各种品质的装备
- **技能商店**：学习新技能和升级现有技能
- **多货币支持**：灵石、贡献点多种货币

### ⚔️ 战斗系统
- **PVE战斗**：挑战各种怪物和BOSS
- **战斗机制**：回合制战斗，考虑攻击、防御、速度
- **奖励系统**：获得经验、灵石、装备掉落
- **怪物类型**：普通、精英、BOSS三种类型

### 💎 离线奖励系统
- **离线计算**：根据离线时间自动计算修炼收益
- **奖励上限**：最多24小时离线奖励，防止无限累积
- **奖励类型**：经验、灵石、修炼点数

## 🔧 API接口文档

### 🔐 认证接口
| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| `POST` | `/api/auth/register` | 用户注册 | username, password, email, nickname |
| `POST` | `/api/auth/login` | 用户登录 | username, password |
| `POST` | `/api/auth/logout` | 用户登出 | - |

### 👤 玩家接口
| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| `GET` | `/api/player/profile` | 获取玩家信息 | - |
| `POST` | `/api/player/cultivate` | 开始修炼 | - |
| `POST` | `/api/player/stop-cultivate` | 停止修炼 | - |
| `GET` | `/api/player/offline-rewards` | 获取离线收益 | - |
| `POST` | `/api/player/claim-offline-rewards` | 领取离线奖励 | - |

### 🔮 技能接口
| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| `GET` | `/api/skills` | 获取所有技能 | - |
| `GET` | `/api/skills/player` | 获取玩家技能 | - |
| `POST` | `/api/skills/learn/{skillId}` | 学习技能 | skillId |
| `POST` | `/api/skills/{playerSkillId}/use` | 使用技能 | playerSkillId |
| `POST` | `/api/skills/{playerSkillId}/upgrade` | 升级技能 | playerSkillId |

### 🐾 宠物接口
| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| `GET` | `/api/pets` | 获取所有宠物模板 | - |
| `GET` | `/api/pets/available` | 获取可捕获宠物 | - |
| `GET` | `/api/pets/my` | 获取我的宠物 | - |
| `GET` | `/api/pets/active` | 获取出战宠物 | - |
| `POST` | `/api/pets/capture/{petId}` | 捕获宠物 | petId |
| `POST` | `/api/pets/activate/{playerPetId}` | 设置出战宠物 | playerPetId |
| `POST` | `/api/pets/feed/{playerPetId}` | 喂食宠物 | playerPetId |
| `POST` | `/api/pets/train/{playerPetId}` | 训练宠物 | playerPetId, trainingType |
| `POST` | `/api/pets/rename/{playerPetId}` | 重命名宠物 | playerPetId, nickname |
| `DELETE` | `/api/pets/release/{playerPetId}` | 释放宠物 | playerPetId |

### 📋 任务接口
| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| `GET` | `/api/quests` | 获取玩家任务 | - |
| `POST` | `/api/quests/{playerQuestId}/claim` | 领取任务奖励 | playerQuestId |
| `POST` | `/api/quests/progress/by-type` | 更新任务进度 | questType, amount |

### ⚔️ 装备接口
| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| `GET` | `/api/equipment` | 获取玩家装备 | - |
| `POST` | `/api/equipment/equip/{itemId}` | 装备物品 | itemId |
| `POST` | `/api/equipment/unequip/{itemId}` | 卸下装备 | itemId |
| `POST` | `/api/equipment/enhance/{equipmentId}` | 强化装备 | equipmentId |

### 🏪 商城接口
| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| `GET` | `/api/shop/items` | 获取商店物品 | shopType |
| `POST` | `/api/shop/buy/{shopItemId}` | 购买物品 | shopItemId, quantity |
| `GET` | `/api/shop/skills` | 获取技能商店 | - |
| `POST` | `/api/shop/buy-skill/{skillId}` | 购买技能 | skillId |

### ⚔️ 战斗接口
| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| `GET` | `/api/combat/monsters` | 获取可战斗怪物 | - |
| `POST` | `/api/combat/battle/{monsterId}` | 开始战斗 | monsterId |
| `GET` | `/api/combat/logs` | 获取战斗日志 | limit |

### 🎒 背包接口
| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| `GET` | `/api/inventory/items` | 获取背包物品 | - |
| `POST` | `/api/inventory/use/{itemId}` | 使用物品 | itemId, quantity |

## 🎮 游戏玩法指南

### 🎮 快速体验宠物系统

```
# 1. 登录游戏
curl -X POST http://192.168.215.144:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'

# 2. 查看可捕获的宠物列表
curl -X GET http://192.168.215.144:8082/api/pets/templates \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 3. 捕获一只宠物（假设宠物ID为1）
curl -X POST http://192.168.215.144:8082/api/pets/capture/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 4. 查看我的宠物列表
curl -X GET http://192.168.215.144:8082/api/pets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 5. 喂养宠物（假设宠物ID为1）
curl -X POST http://192.168.215.144:8082/api/pets/feed/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 6. 训练宠物（假设宠物ID为1）
curl -X POST http://192.168.215.144:8082/api/pets/train/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 7. 激活宠物出战（假设宠物ID为1）
curl -X POST http://192.168.215.144:8082/api/pets/activate/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

访问宠物管理页面: http://192.168.215.144:8082/pets.html

### 🌐 访问地址汇总

1. 🎮 玩家游戏登录: http://192.168.215.144:8082/login.html
2. 👑 管理员后台登录: http://192.168.215.144:8082/adminLogin.html
3. 🐾 宠物系统页面: http://192.168.215.144:8082/pets.html

## 🗄️ 数据库设计

### 📊 核心数据表

#### 用户系统
- **users** - 用户基础信息表
- **player_profiles** - 玩家游戏档案表

#### 修炼系统  
- **cultivation_levels** - 修炼境界配置表
- **cultivation_logs** - 修炼日志记录表

#### 技能系统
- **skills** - 技能模板表
- **player_skills** - 玩家技能表
- **skill_shop** - 技能商店表

#### 宠物系统
- **pets** - 宠物模板表
- **player_pets** - 玩家宠物表
- **pet_skills** - 宠物技能表
- **pet_training_logs** - 宠物训练日志表

#### 装备系统
- **equipments** - 装备模板表
- **player_equipment** - 玩家装备表

#### 物品系统
- **items** - 物品模板表
- **player_items** - 玩家物品表

#### 任务系统
- **quests** - 任务模板表
- **player_quests** - 玩家任务表

#### 战斗系统
- **monsters** - 怪物模板表
- **combat_logs** - 战斗日志表

#### 商城系统
- **shop_items** - 商店物品表

#### 其他系统
- **offline_rewards** - 离线奖励表

### 🔗 数据关系设计
```
users (1) ←→ (1) player_profiles
player_profiles (1) ←→ (N) player_pets
player_profiles (1) ←→ (N) player_skills  
player_profiles (1) ←→ (N) player_equipment
player_profiles (1) ←→ (N) player_items
player_profiles (1) ←→ (N) player_quests
pets (1) ←→ (N) player_pets
skills (1) ←→ (N) player_skills
equipments (1) ←→ (N) player_equipment
```

## 🔐 登录系统

### 双入口登录设计 - 完全分离的认证系统
游戏提供两个完全独立的登录入口，前后端认证系统完全分离：

#### 🎮 玩家游戏登录 (`/login.html`)
- **功能**：用户注册、玩家登录
- **特点**：xiuxian主题设计，降低游玩门槛
- **跳转**：登录成功后直接进入游戏主页 (`index.html`)
- **体验**：无需选择角色，直接开始xiuxian之旅
- **认证系统**：使用 `/api/auth/*` 接口，存储 `authToken`

#### 👑 管理员后台登录 (`/adminLogin.html`)
- **功能**：管理员专用登录入口
- **特点**：专业的管理后台风格设计，独立的前端认证逻辑
- **跳转**：登录成功后跳转到管理后台 (`admin.html`)
- **安全**：增强的安全验证，所有操作记录
- **监控**：集成系统监控功能，无需额外监控地址
- **认证系统**：使用 `/api/admin/auth/*` 接口，存储 `adminToken`

### 系统分离特点
| 系统 | 前端文件 | 后端接口 | Token存储 | 认证过滤器 |
|------|----------|----------|-----------|------------|
| 游戏系统 | `js/auth.js`, `js/api.js` | `/api/auth/*` | `authToken` | `SecurityFilter` |
| 管理系统 | `js/admin-auth.js`, `js/admin-api.js` | `/api/admin/auth/*` | `adminToken` | `AdminSecurityFilter` |

### 访问地址
| 页面类型 | URL | 适用用户 | 功能 | 认证系统 |
|----------|-----|----------|------|----------|
| 玩家游戏登录 | `/login.html` | 普通玩家 | 游戏登录、注册 | 游戏认证系统 |
| 管理员后台登录 | `/adminLogin.html` | 管理员 | 后台管理登录 | 管理员认证系统 |

### 默认账户
- **管理员账户**：admin / admin123 (独立认证系统，请在生产环境中修改)
- **普通玩家**：可通过注册页面创建 (游戏认证系统)

### 🌟 新手引导
1. **直接开始游戏**：访问 `/login.html` 进行注册或登录
2. **注册账号**：在普通用户登录页面填写用户名、密码、邮箱和昵称完成注册
3. **首次登录**：系统自动创建玩家档案，获得新手礼包
4. **开始修炼**：点击"开始修炼"按钮，体验挂机修炼系统
5. **完成任务**：查看任务列表，完成"初次修炼"等新手任务
6. **学习技能**：使用获得的灵石学习"基础功法"提升修炼速度
7. **装备物品**：在商店购买基础装备，提升角色属性

### 🚀 进阶玩法
- **合理规划修炼时间**：长时间修炼获得更多经验和资源
- **优先完成每日任务**：每日任务提供稳定的经验和灵石收入
- **技能搭配策略**：学习适合当前境界的技能，平衡修炼和战斗能力
- **装备升级路线**：优先强化武器和胸甲，提升核心属性
- **宠物培养计划**：选择合适的宠物进行重点培养
- **资源管理技巧**：合理分配灵石在技能学习和装备购买之间

### 💡 高级策略
- **境界突破时机**：在境界突破前准备充足的资源
- **宠物捕获策略**：根据自身等级选择合适稀有度的宠物
- **战斗效率优化**：选择合适等级的怪物进行战斗
- **离线收益最大化**：合理安排上线时间，充分利用离线奖励

## 🔒 安全特性

### 🛡️ 认证与授权
- **JWT Token认证**：无状态的Token认证机制，支持自动刷新
- **密码加密存储**：使用BCrypt算法加密存储用户密码
- **角色权限控制**：区分普通用户和管理员权限
- **API接口保护**：所有业务接口都需要有效Token访问

### � 数据安全
- **SQL注入防护**：使用MyBatis-Plus预编译语句防止SQL注入
- **XSS防护**：前端输入验证和后端数据过滤
- **CORS跨域配置**：严格的跨域资源共享策略
- **敏感信息保护**：数据库密码等敏感配置加密存储

### 🚫 业务安全
- **请求频率限制**：防止恶意刷接口
- **数据完整性校验**：关键业务数据的完整性验证
- **异常处理机制**：统一的异常处理，避免敏感信息泄露

## 📊 性能优化

### 🚀 数据库优化
- **连接池配置**：HikariCP高性能数据库连接池
- **索引优化**：关键查询字段建立合适索引
- **分页查询**：大数据量查询使用分页避免内存溢出
- **读写分离**：支持主从数据库读写分离（可选）

### ⚡ 应用性能
- **缓存策略**：静态资源浏览器缓存，热点数据内存缓存
- **异步处理**：耗时操作使用异步处理提升响应速度
- **连接复用**：HTTP连接复用减少网络开销
- **资源压缩**：静态资源Gzip压缩

### 📈 监控与日志
- **分级日志管理**：应用日志、SQL日志、性能日志分类存储
- **日志轮转**：自动按日期切分和压缩历史日志
- **性能监控**：关键接口性能监控和报警
- **健康检查**：应用健康状态实时监控

## 🐛 故障排除

### ❗ 常见问题

#### 1. 数据库连接问题
**问题现象**：应用启动失败，提示数据库连接错误
```
# 解决步骤
1. 检查MySQL服务状态
   systemctl status mysql  # Linux
   net start mysql         # Windows

2. 验证数据库连接参数
   mysql -u root -p -h localhost xiuxian_game

3. 检查数据库初始化
   # 确认init-database.sql已正确执行
   SELECT COUNT(*) FROM users;  # 应返回大于0的结果
```

#### 2. 端口占用问题
**问题现象**：启动时提示端口6000已被占用
```
# Windows解决方案
netstat -ano | findstr :8082
taskkill /PID <进程ID> /F

# Linux/macOS解决方案
lsof -i :8082
kill -9 <进程ID>
```

#### 3. 前端资源加载失败
**问题现象**：页面样式错乱，JavaScript功能异常
```
# 解决步骤
1. 清除浏览器缓存 (Ctrl+F5)
2. 检查控制台错误信息
3. 确认静态资源路径配置
   # application.properties中应包含:
   spring.resources.static-locations=classpath:/static/
```

#### 4. JWT Token过期
**问题现象**：登录后频繁跳转到登录页面
```
# 解决方案
1. 检查系统时间是否正确
2. 调整JWT过期时间配置
   # application.properties中:
   jwt.expiration=86400  # 24小时
```

#### 5. 宠物系统异常
**问题现象**：宠物捕获失败或显示异常
```
# 解决步骤
1. 检查玩家等级是否满足宠物解锁要求
2. 验证宠物数据是否正确初始化
   SELECT COUNT(*) FROM pets;  # 应返回大于0的结果
3. 检查宠物相关接口是否正常
   curl -X GET http://192.168.215.144:8082/api/pets/templates
```

### 🔍 调试工具

#### 日志查看
```
# 实时查看应用日志
tail -f logs/xiuxian-game.log

# Windows PowerShell查看日志
Get-Content logs\xiuxian-game.log -Wait

# Docker环境下查看日志
docker logs -f xiuxian-game
```

#### 数据库调试
```
-- 检查玩家数据
SELECT * FROM player_profiles WHERE nickname = 'YOUR_NICKNAME';

-- 检查装备数据
SELECT COUNT(*) FROM player_equipment WHERE player_id = YOUR_PLAYER_ID;

-- 检查技能数据
SELECT * FROM player_skills WHERE player_id = YOUR_PLAYER_ID;
```

#### API测试
```
# 测试登录接口
curl -X POST http://192.168.215.144:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'

# 测试获取玩家信息
curl -X GET http://192.168.215.144:8082/api/player/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 测试修炼接口
curl -X POST http://192.168.215.144:8082/api/player/cultivate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 性能监控
```
# 查看内存使用情况
jstat -gc PID

# 查看线程情况
jstack PID

# Docker资源使用情况
docker stats xiuxian-game
```

### 📝 日志管理

#### 日志配置
- 📊 **统一管理**: 所有日志集中输出到 `logs/xiuxian-game.log`
- 🕒 **自动轮转**: 每日生成新日志文件，避免单文件过大
- 📦 **分类存储**: 按类型分别存储，便于分析

#### 日志查看
```
# 使用日志查看工具（推荐）
view-logs.bat        # Windows
./view-logs.sh       # Linux/macOS

# 或直接查看日志文件
tail -f logs/xiuxian-game.log    # 实时查看
less logs/xiuxian-game.log       # 分页查看
```

#### 日志级别说明
- 🟢 **INFO**: 一般操作信息，如启动完成、请求处理等
- 🟡 **WARN**: 警告信息，如参数校验失败、缓存未命中等
- 🔴 **ERROR**: 错误信息，如数据库连接失败、空指针异常等
- 🟣 **DEBUG**: 调试信息，开发阶段启用，生产环境关闭

#### 故障排查流程
```
1. 查看最近错误日志
   grep "ERROR" logs/xiuxian-game.log | tail -n 10

2. 根据错误类型定位问题
   # 数据库相关错误 -> 检查数据库连接
   # 参数校验错误 -> 检查API请求参数
   # 权限相关错误 -> 检查JWT Token有效性

3. 复现问题并收集详细日志
   # 调整日志级别为DEBUG
   # 重新执行操作
   # 收集完整错误堆栈信息
```

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

## 📞 联系方式

| 联系方式 | 地址 | 说明 |
|----------|------|------|
| 🏠 **项目主页** | https://github.com/SSJLYY/xiuxian | 项目源码和文档 |
| 🐛 **问题反馈** | https://github.com/SSJLYY/xiuxian/issues | Bug报告和功能建议 |
| 📧 **邮箱联系** | shaun88@88.com | 技术交流和合作 |
| 💬 **讨论区** | https://github.com/SSJLYY/xiuxian/discussions | 社区讨论和经验分享 |

## 🤝 参与贡献

我们欢迎所有形式的贡献！无论是代码、文档、测试还是建议都非常宝贵。

### 贡献方式
1. **🍴 Fork项目** - 点击右上角Fork按钮
2. **🌿 创建分支** - `git checkout -b feature/AmazingFeature`
3. **💻 提交代码** - `git commit -m 'Add some AmazingFeature'`
4. **📤 推送分支** - `git push origin feature/AmazingFeature`
5. **🔄 提交PR** - 创建Pull Request

### 贡献指南
- 遵循现有的代码风格和命名规范
- 为新功能添加相应的测试用例
- 更新相关文档和README
- 确保所有测试通过
- 提供清晰的提交信息

### 开发环境搭建
```bash
# 1. 克隆项目
git clone https://github.com/SSJLYY/xiuxian.git
cd xiuxian

# 2. 安装依赖
mvn clean install

# 3. 配置数据库
# 修改 application.properties 中的数据库配置

# 4. 初始化数据库
mysql -u root -p < src/main/resources/init-database.sql

# 5. 启动应用
mvn spring-boot:run
```

---

⭐ **如果这个项目对你有帮助，请给个Star支持一下！**

🎮 **开始你的xiuxian之旅吧！从凡人到仙人，一切皆有可能！**


## 🐾 宠物系统 (v1.1.0新增)

### 系统概述
宠物系统是游戏的核心玩法之一，玩家可以捕获、培养、训练各种灵兽、妖兽和神兽。

### 宠物类型
- **灵兽**: 修炼有成的动物，属性平衡
- **妖兽**: 强大的野兽，攻击力强
- **神兽**: 传说中的神兽，全能型

### 宠物稀有度
| 稀有度 | 颜色 | 捕获率 | 成长率 |
|--------|------|--------|--------|
| 普通 | 白色 | 80% | 1.0 |
| 稀有 | 绿色 | 60% | 1.2 |
| 史诗 | 蓝色 | 30-35% | 1.3 |
| 传说 | 紫色 | 10-15% | 1.5-1.6 |
| 神话 | 橙色 | 5% | 2.0 |

### 初始宠物列表
1. **小灵猫** (普通) - 新手宠物，易于捕获
2. **灵狐** (稀有) - 速度型宠物
3. **冰霜狼** (史诗) - 平衡型宠物
4. **雷鹰** (史诗) - 速度型宠物
5. **火麒麟** (传说) - 攻击型神兽
6. **金翅大鹏** (传说) - 速度型神兽
7. **青龙** (神话) - 四大神兽之一
8. **白虎** (神话) - 四大神兽之一
9. **玄武** (神话) - 四大神兽之一
10. **朱雀** (神话) - 四大神兽之一

### 宠物功能

#### 1. 捕获宠物
```
POST /api/pets/capture/{petId}
```
- 根据宠物捕获率随机判定
- 需要达到宠物解锁等级
- 同种宠物最多拥有3只

#### 2. 宠物培养
- **喂食**: 恢复饱食度，提升忠诚度
- **训练**: 提升攻击/防御/速度属性
- **升级**: 根据成长率自动提升属性

#### 3. 宠物属性
- **等级**: 通过训练和战斗提升
- **忠诚度**: 影响战斗表现（0-100）
- **饱食度**: 需要定期喂食（0-100）
- **战斗统计**: 记录战斗次数和胜率

#### 4. 宠物技能
初始包含8种宠物技能：
- 撕咬（基础攻击）
- 火焰吐息（火系攻击）
- 雷霆一击（雷系攻击）
- 铁壁防御（防御技能）
- 疾风步（速度提升）
- 治愈之光（恢复技能）
- 狂暴（攻击提升）
- 冰封（冰系攻击）

### 宠物API接口

```bash
# 获取所有宠物模板
GET /api/pets

# 获取可捕获的宠物
GET /api/pets/available

# 获取我的宠物
GET /api/pets/my

# 获取出战宠物
GET /api/pets/active

# 捕获宠物
POST /api/pets/capture/{petId}

# 设置出战宠物
POST /api/pets/activate/{playerPetId}

# 喂食宠物
POST /api/pets/feed/{playerPetId}

# 训练宠物
POST /api/pets/train/{playerPetId}
Body: { "trainingType": "攻击" } # 或 "防御"、"速度"

# 重命名宠物
POST /api/pets/rename/{playerPetId}
Body: { "nickname": "新名字" }

# 锁定/解锁宠物
POST /api/pets/toggle-lock/{playerPetId}

# 释放宠物
DELETE /api/pets/release/{playerPetId}

# 获取训练记录
GET /api/pets/training-logs/{playerPetId}?limit=10
```

### 使用示例

```bash
# 1. 捕获小灵猫（ID=1，捕获率80%）
curl -X POST http://192.168.215.144:8082/api/pets/capture/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# 2. 喂食宠物
curl -X POST http://192.168.215.144:8082/api/pets/feed/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# 3. 训练宠物攻击
curl -X POST http://192.168.215.144:8082/api/pets/train/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"trainingType":"攻击"}'

# 4. 设置出战宠物
curl -X POST http://192.168.215.144:8082/api/pets/activate/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 游戏机制

#### 忠诚度系统
- 初始忠诚度: 50
- 喂食: +5
- 训练: +2
- 忠诚度影响战斗表现

#### 饱食度系统
- 初始饱食度: 100
- 训练消耗: -10
- 喂食恢复: +30
- 饱食度<20时无法训练

#### 成长率系统
- 成长率影响升级时的属性提升
- 神话宠物成长率最高（2.0）
- 普通宠物成长率最低（1.0）

### 未来扩展
- [ ] 宠物战斗系统
- [ ] 宠物技能使用
- [ ] 宠物进化系统
- [ ] 宠物繁殖系统
- [ ] 宠物装备系统
- [ ] 宠物图鉴系统

详细文档请参考: [宠物系统文档](PET_SYSTEM_README.md)

## 📝 更新日志

### v1.1.0 (2025-11-27)
- ✨ 新增完整的宠物系统
  - 10种初始宠物
  - 8种宠物技能
  - 捕获、培养、训练功能
  - 忠诚度和饱食度系统
- 🐛 修复PlayerProfile重复方法定义
- 📝 完善代码注释和日志输出
- 🎨 优化代码结构和异常处理
- 📚 新增详细的系统文档

### v1.0.0
- 🎉 项目初始版本
- ✨ 实现用户系统
- ✨ 实现玩家系统
- ✨ 实现修炼系统
- ✨ 实现技能系统
- ✨ 实现任务系统

## 📚 相关文档

- [宠物系统详细文档](PET_SYSTEM_README.md)
- [前端更新说明](FRONTEND_UPDATE.md)

- [项目更新指南](PROJECT_UPDATE_GUIDE.md)
- [部署检查清单](DEPLOYMENT_CHECKLIST.md)
- [代码优化总结](OPTIMIZATION_SUMMARY.md)


## 🎨 前端页面

### 主要页面
- `index.html` - 游戏主页(修炼、技能、任务)
- `pets.html` - 宠物系统页面 🆕
- `login.html` - 玩家游戏登录页面
- `adminLogin.html` - 管理员后台登录页面 🆕
- `cultivate.html` - 修炼专用页面
- `admin.html` - 管理后台(集成系统监控)

### 宠物系统页面功能
- **我的宠物**: 查看和管理已拥有的宠物
- **捕获宠物**: 浏览和捕获新宠物
- **出战宠物**: 查看出战宠物详情,快速喂食和训练
- **宠物操作**: 喂食、训练、重命名、锁定、释放等

### 访问方式
1. 🎮 玩家登录: http://192.168.215.144:8082/login.html
2. 登录后点击顶部"🐾 宠物系统"按钮
3. 或直接访问: http://192.168.215.144:8082/pets.html

## 📚 API文档

### 🔐 认证接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/auth/login` | POST | 用户登录 | 公开 |
| `/api/auth/register` | POST | 用户注册 | 公开 |
| `/api/auth/logout` | POST | 用户登出 | 需要认证 |
| `/api/auth/me` | GET | 获取当前用户信息 | 需要认证 |

### 📧 邮件系统接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/mail/list` | GET | 获取邮件列表 | 需要认证 |
| `/api/mail/{mailId}` | GET | 获取邮件详情 | 需要认证 |
| `/api/mail/{mailId}/claim` | POST | 领取邮件附件 | 需要认证 |
| `/api/mail/{mailId}` | DELETE | 删除邮件 | 需要认证 |
| `/api/mail/unread-count` | GET | 获取未读邮件数量 | 需要认证 |

### 📢 公告系统接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/announcement/list` | GET | 获取公告列表 | 公开 |
| `/api/announcement/{id}` | GET | 获取公告详情 | 公开 |
| `/api/announcement/{id}/read` | POST | 标记公告已读 | 需要认证 |

### 🏆 排行榜接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/ranking/level` | GET | 获取等级排行榜 | 公开 |
| `/api/ranking/power` | GET | 获取战力排行榜 | 公开 |
| `/api/ranking/wealth` | GET | 获取财富排行榜 | 公开 |
| `/api/ranking/my-rank` | GET | 获取玩家排名 | 需要认证 |

### 🎖️ 成就系统接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/achievement/list` | GET | 获取成就列表 | 需要认证 |
| `/api/achievement/{id}` | GET | 获取成就详情 | 需要认证 |
| `/api/achievement/{id}/claim` | POST | 领取成就奖励 | 需要认证 |
| `/api/achievement/progress` | GET | 获取成就进度 | 需要认证 |

### 🏛️ 宗门系统接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/guild/create` | POST | 创建宗门 | 需要认证 |
| `/api/guild/search` | GET | 搜索宗门 | 需要认证 |
| `/api/guild/{guildId}` | GET | 获取宗门详情 | 需要认证 |
| `/api/guild/{guildId}/apply` | POST | 申请加入宗门 | 需要认证 |
| `/api/guild/quit` | POST | 退出宗门 | 需要认证 |
| `/api/guild/donate` | POST | 宗门捐献 | 需要认证 |

### 🛒 拍卖行接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/auction/list` | GET | 获取拍卖列表 | 需要认证 |
| `/api/auction/sell` | POST | 上架物品 | 需要认证 |
| `/api/auction/{id}/buy` | POST | 购买物品 | 需要认证 |
| `/api/auction/{id}` | DELETE | 取消拍卖 | 需要认证 |
| `/api/auction/my-sales` | GET | 获取我的拍卖 | 需要认证 |

### 💎 VIP系统接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/vip/info` | GET | 获取VIP信息 | 需要认证 |
| `/api/vip/privileges` | GET | 获取VIP特权列表 | 需要认证 |
| `/api/vip/daily-reward` | POST | 领取VIP每日奖励 | 需要认证 |
| `/api/vip/recharge` | POST | 充值 | 需要认证 |

### 🎉 活动系统接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/activity/list` | GET | 获取活动列表 | 需要认证 |
| `/api/activity/{id}` | GET | 获取活动详情 | 需要认证 |
| `/api/activity/{id}/join` | POST | 参与活动 | 需要认证 |
| `/api/activity/{id}/claim-reward` | POST | 领取活动奖励 | 需要认证 |

### 🎁 礼包码接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/gift-code/redeem` | POST | 兑换礼包码 | 需要认证 |
| `/api/gift-code/history` | GET | 获取兑换历史 | 需要认证 |

### 🛡️ 管理员接口 (独立认证系统)

#### 管理员认证接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/admin/auth/login` | POST | 管理员登录 | 公开 |
| `/api/admin/auth/validate` | GET | 验证管理员token | 需要管理员认证 |
| `/api/admin/auth/logout` | POST | 管理员登出 | 需要管理员认证 |
| `/api/admin/auth/me` | GET | 获取当前管理员信息 | 需要管理员认证 |

#### 管理员业务接口
| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/admin/dashboard/stats` | GET | 获取仪表板统计 | 管理员 |
| `/api/admin/players` | GET | 获取玩家列表 | 管理员 |
| `/api/admin/players/{id}/ban` | POST | 封禁玩家 | 管理员 |
| `/api/admin/content/stats` | GET | 获取内容统计 | 管理员 |
| `/api/admin/mail/send` | POST | 发送邮件 | 管理员 |
| `/api/admin/announcement` | POST | 创建公告 | 管理员 |
| `/api/admin/gift-code/generate` | POST | 生成礼包码 | 管理员 |
| `/api/admin/config/list` | GET | 获取配置列表 | 管理员 |
| `/api/admin/config/update` | PUT | 更新配置 | 管理员 |
| `/api/admin/logs/player-login` | GET | 获取登录日志 | 管理员 |
| `/api/admin/security/blacklist` | GET | 获取IP黑名单 | 管理员 |

## 🗄️ 数据库设计

### 📊 核心表结构

#### 用户系统
- `users` - 用户账号表
- `player_profiles` - 玩家档案表

#### 邮件系统
- `player_mails` - 玩家邮件表
- `mail_attachments` - 邮件附件表

#### 公告系统
- `announcements` - 公告表

#### 排行榜系统
- `rankings` - 排行榜缓存表

#### 成就系统
- `achievements` - 成就模板表
- `player_achievements` - 玩家成就表

#### 宗门系统
- `guilds` - 宗门表
- `guild_members` - 宗门成员表
- `guild_applications` - 宗门申请表

#### 拍卖行系统
- `auction_items` - 拍卖物品表

#### VIP系统
- `vip_levels` - VIP等级配置表
- `player_vip` - 玩家VIP表
- `recharge_records` - 充值记录表

#### 活动系统
- `activities` - 活动配置表
- `player_activity_progress` - 玩家活动进度表

#### 礼包码系统
- `gift_codes` - 礼包码表
- `gift_code_usage` - 礼包码使用记录表

#### 日志系统
- `player_login_logs` - 玩家登录日志表
- `admin_operation_logs` - 管理员操作日志表
- `daily_statistics` - 每日统计表

#### 配置系统
- `game_configs` - 游戏配置表

## 🚀 性能优化

### 📈 缓存机制
- **排行榜缓存**：30分钟缓存，定时刷新
- **公告缓存**：10分钟缓存，修改时清除
- **配置缓存**：启动时加载，修改时刷新

### ⚡ 异步处理
- **邮件发送**：异步批量发送，避免阻塞
- **排行榜更新**：异步定时计算，提高响应速度
- **统计数据**：异步聚合，减少数据库压力
- **日志记录**：异步写入，提高性能

### 🗃️ 数据库优化
- **索引优化**：为常用查询字段建立索引
- **分页查询**：避免全表扫描
- **连接池配置**：HikariCP高性能连接池
- **慢查询监控**：记录和优化慢查询

### 🛡️ 安全机制
- **请求频率限制**：基于令牌桶算法的限流
- **异常行为检测**：自动检测和处理异常操作
- **IP黑名单**：阻止恶意IP访问
- **单点登录**：防止账号重复登录
- **操作日志**：记录所有关键操作

## 📋 部署指南

### 🐳 Docker部署（推荐）

1. **构建镜像**
```bash
# 构建应用镜像
docker build -t xiuxian-game:latest .
```

2. **使用Docker Compose部署**
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f app
```

3. **访问应用**
- 🎮 玩家游戏登录：http://192.168.215.144:8082/login.html
- 👑 管理员后台登录：http://192.168.215.144:8082/adminLogin.html

4. **验证系统分离**
```bash
# 测试游戏登录API
curl -X POST http://192.168.215.144:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass","userType":"player"}'

# 测试管理员登录API
curl -X POST http://192.168.215.144:8082/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 🔧 手动部署

1. **环境要求**
   - Java 1.8+
   - MySQL 8.0+
   - Maven 3.6+

2. **数据库初始化**
```bash
# 创建数据库
mysql -u root -p < src/main/resources/init-database.sql

# 执行优化脚本
mysql -u root -p xiuxian_game < src/main/resources/database-optimization.sql
```

3. **配置文件**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/xiuxian_game
spring.datasource.username=root
spring.datasource.password=your_password

# 日志配置
logging.level.com.xiuxian.game=INFO
logging.file.path=./logs
```

4. **构建和运行**
```bash
# 构建项目
mvn clean package -DskipTests

# 运行应用
java -jar target/xiuxian-game-1.1.0.jar
```

## 🔧 配置说明

### 🎮 游戏配置
管理员可以通过配置管理页面调整以下参数：

#### 经验相关
- `exp.multiplier` - 经验倍率
- `cultivation.exp.base` - 修炼基础经验

#### 掉落相关
- `drop.rate.multiplier` - 掉落倍率
- `drop.rare.rate` - 稀有物品掉落率

#### 商店相关
- `shop.discount.rate` - 商店折扣率
- `shop.refresh.cost` - 商店刷新费用

#### 活动相关
- `activity.double.exp.enabled` - 双倍经验活动
- `activity.double.drop.enabled` - 双倍掉落活动

#### 系统相关
- `system.maintenance.mode` - 维护模式
- `system.max.online.users` - 最大在线用户数

### 🛡️ 安全配置
- **频率限制**：登录5次/分钟，注册3次/5分钟
- **会话管理**：24小时过期，单点登录
- **日志保留**：登录日志90天，操作日志180天

## 📊 监控和日志

### 📈 系统监控
- **在线用户数**：实时统计
- **API响应时间**：性能监控
- **数据库连接**：连接池状态
- **缓存命中率**：缓存效果

### 📝 日志管理
- **应用日志**：xiuxian-game.log
- **错误日志**：xiuxian-game-error.log
- **SQL日志**：xiuxian-game-sql.log
- **性能日志**：xiuxian-game-performance.log

### 📊 数据统计
- **DAU/MAU**：日活/月活用户
- **留存率**：1日、7日、30日留存
- **ARPU/ARPPU**：平均收入指标
- **付费率**：付费用户比例

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进项目！

### 📝 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式
- refactor: 重构
- test: 测试相关
- chore: 构建过程或辅助工具的变动

### 🔄 开发流程
1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

---

**🎮 开始你的xiuxian之旅吧！**