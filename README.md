# 🎮 修仙挂机游戏 (Xiuxian Idle Game)

一个基于Spring Boot + 原生JavaScript的修仙主题挂机游戏，支持玩家修炼、战斗、装备、宠物等完整游戏系统。采用前后端分离架构，提供丰富的修仙体验和完整的游戏生态。

[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.3.1-blue.svg)](https://baomidou.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.1.0-red.svg)](CHANGELOG.md)

## 🎮 游戏特色

- **🧙‍♂️ 修仙主题**：体验从凡人到仙人的修仙之旅，完整的境界体系
- **⏰ 挂机玩法**：离线也能获得收益，真正的放置类游戏体验
- **🔮 技能系统**：学习各种修仙技能，提升战斗力和修炼效率
- **📋 任务系统**：完成每日、每周、每月任务获得丰厚奖励
- **⚔️ 装备系统**：收集和强化装备，提升角色属性
- **🐾 宠物系统**：捕获、培养各种灵兽，成为修仙路上的伙伴
- **🏪 商城系统**：购买物品、装备和技能，加速成长
- **⚡ 战斗系统**：挑战各种怪物，获得经验和奖励
- **🌟 境界提升**：从练气期到元婴期的完整修仙境界体系
- **💎 离线奖励**：离线时间越长，奖励越丰厚

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
├── src/main/java/com/xiuxian/game/
│   ├── XiuxianGameApplication.java    # 主启动类
│   ├── controller/                    # REST API控制器层
│   │   ├── AuthController.java        # 用户认证
│   │   ├── PlayerController.java      # 玩家管理
│   │   ├── PetController.java         # 宠物系统
│   │   ├── SkillController.java       # 技能系统
│   │   ├── QuestController.java       # 任务系统
│   │   ├── EquipmentController.java   # 装备系统
│   │   ├── ShopController.java        # 商城系统
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

### 📋 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 1.8+ | 推荐使用OpenJDK 8或Oracle JDK 8 |
| **Maven** | 3.6+ | 用于项目构建和依赖管理 |
| **MySQL** | 8.0+ | 数据库服务器，需要支持utf8mb4字符集 |
| **内存** | 2GB+ | 推荐4GB以上，确保流畅运行 |
| **磁盘** | 1GB+ | 包含数据库文件和日志文件 |
| **浏览器** | 现代浏览器 | 支持ES6+，推荐Chrome/Firefox/Edge |

### ⚡ 一键启动（推荐）

#### Windows用户
```bash
# 1. 初始化数据库
init-db.bat

# 2. 启动应用
start.bat
```

#### Linux/macOS用户
```bash
# 1. 初始化数据库
chmod +x init-db.sh && ./init-db.sh

# 2. 启动应用  
chmod +x start.sh && ./start.sh
```

### 🔧 手动配置

#### 1. 数据库配置
```sql
-- 创建数据库
CREATE DATABASE xiuxian_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入初始数据
mysql -u root -p xiuxian_game < src/main/resources/init-database.sql
```

#### 2. 修改配置文件
编辑 `src/main/resources/application.properties`：
```properties
# 数据库连接（根据实际情况修改）
spring.datasource.url=jdbc:mysql://localhost:3306/xiuxian_game?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=your_username
spring.datasource.password=your_password

# 应用端口（可选修改）
server.port=8081
```

#### 3. 启动应用
```bash
# 克隆项目
git clone https://github.com/SSJLYY/xiuxian.git
cd xiuxian-game

# 编译并启动
mvn clean spring-boot:run
```

#### 4. 访问游戏
- 🌐 游戏地址：http://localhost:8081/login.html
- 📊 监控地址：http://localhost:8081/actuator/health
- 👑 管理后台：http://localhost:8081/admin.html

### 🐳 Docker部署（推荐生产环境）

#### 单容器部署
```bash
# 1. 构建应用
mvn clean package -DskipTests

# 2. 构建Docker镜像
docker build -t xiuxian-game:latest .

# 3. 运行容器
docker run -d \
  --name xiuxian-game \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://your-mysql-host:3306/xiuxian_game" \
  -e SPRING_DATASOURCE_USERNAME="your-username" \
  -e SPRING_DATASOURCE_PASSWORD="your-password" \
  xiuxian-game:latest
```

#### Docker Compose部署
```bash
# 1. 使用docker-compose.yml一键部署
docker-compose up -d

# 2. 查看运行状态
docker-compose ps

# 3. 查看日志
docker-compose logs -f xiuxian-game

# 4. 停止服务
docker-compose down
```

#### 生产环境配置
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  xiuxian-game:
    image: xiuxian-game:latest
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JAVA_OPTS=-Xms512m -Xmx1024m
    volumes:
      - ./logs:/app/logs
      - ./config:/app/config
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
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
- **技能学习**：消耗灵石学习各种修仙技能
- **技能升级**：通过使用和修炼提升技能等级
- **技能类型**：修炼类、攻击类、防御类、辅助类
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

### 🌟 新手引导
1. **注册账号**：填写用户名、密码、邮箱和昵称完成注册
2. **首次登录**：系统自动创建玩家档案，获得新手礼包
3. **开始修炼**：点击"开始修炼"按钮，体验挂机修炼系统
4. **完成任务**：查看任务列表，完成"初次修炼"等新手任务
5. **学习技能**：使用获得的灵石学习"基础功法"提升修炼速度
6. **装备物品**：在商店购买基础装备，提升角色属性

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
```bash
# 解决步骤
1. 检查MySQL服务状态
   systemctl status mysql  # Linux
   net start mysql         # Windows

2. 验证数据库配置
   - 检查 application.properties 中的数据库URL、用户名、密码
   - 确认数据库 xiuxian_game 已创建

3. 测试数据库连接
   mysql -h 47.103.87.55 -u root -p xiuxian_game
```

#### 2. 端口占用问题
**问题现象**：启动时提示端口8081已被占用
```bash
# Windows解决方案
netstat -ano | findstr :8081
taskkill /PID <进程ID> /F

# Linux解决方案
lsof -i :8081
kill -9 <进程ID>

# 或修改配置文件中的端口
server.port=8082
```

#### 3. 前端资源加载失败
**问题现象**：页面样式错乱，JavaScript功能异常
```bash
# 解决步骤
1. 清除浏览器缓存 (Ctrl+F5)
2. 检查控制台错误信息
3. 验证静态资源路径配置
4. 确认防火墙未阻止静态资源访问
```

#### 4. JWT Token过期
**问题现象**：登录后频繁跳转到登录页面
```bash
# 解决方案
1. 检查系统时间是否正确
2. 调整JWT过期时间配置
   jwt.expiration=86400000  # 24小时
3. 清除浏览器localStorage中的token
```

#### 5. 宠物系统异常
**问题现象**：宠物捕获失败或显示异常
```bash
# 解决步骤
1. 检查玩家等级是否满足宠物解锁要求
2. 验证宠物数据是否正确初始化
3. 查看后端日志中的详细错误信息
```

### 🔍 调试工具

#### 日志查看
```bash
# 实时查看应用日志
tail -f logs/xiuxian-game.log

# 查看错误日志
grep "ERROR" logs/xiuxian-game.log

# 查看SQL执行日志
tail -f logs/xiuxian-game-sql.log
```

#### 数据库调试
```sql
-- 检查玩家数据
SELECT * FROM player_profiles WHERE nickname = 'YOUR_NICKNAME';

-- 检查宠物数据
SELECT pp.*, p.name FROM player_pets pp 
JOIN pets p ON pp.pet_id = p.id 
WHERE pp.player_id = YOUR_PLAYER_ID;

-- 检查任务进度
SELECT pq.*, q.title FROM player_quests pq 
JOIN quests q ON pq.quest_id = q.id 
WHERE pq.player_id = YOUR_PLAYER_ID;
```

#### API测试
```bash
# 测试登录接口
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 测试玩家信息接口
curl -X GET http://localhost:8081/api/player/profile \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 📝 日志管理

#### 日志文件结构
```
logs/
├── xiuxian-game.log                    # 当前应用日志
├── xiuxian-game-2024-11-28.log.gz     # 历史应用日志（按日期）
├── xiuxian-game-error.log             # 当前错误日志
├── xiuxian-game-error-2024-11-28.log.gz # 历史错误日志
├── xiuxian-game-sql.log               # 当前SQL日志
├── xiuxian-game-sql-2024-11-28.log.gz # 历史SQL日志
├── xiuxian-game-performance.log       # 当前性能日志
└── xiuxian-game-performance-2024-11-28.log.gz # 历史性能日志
```

#### 日志特性
- ⏰ **自动切分**: 每天0点自动切分日志文件
- 🗜️ **自动压缩**: 历史日志自动压缩为.gz格式
- 🗑️ **自动清理**: 应用日志保留30天，SQL/性能日志保留7天
- 📊 **分类存储**: 按类型分别存储，便于分析

#### 日志查看
```bash
# 使用日志查看工具（推荐）
view-logs.bat        # Windows
./view-logs.sh       # Linux/macOS

# 手动查看日志
tail -f logs/xiuxian-game.log                    # 实时查看应用日志
grep "ERROR" logs/xiuxian-game.log               # 搜索错误日志
zcat logs/xiuxian-game-2024-11-28.log.gz | less # 查看历史日志
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

🎮 **开始你的修仙之旅吧！从凡人到仙人，一切皆有可能！**


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
curl -X POST http://localhost:8081/api/pets/capture/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# 2. 喂食宠物
curl -X POST http://localhost:8081/api/pets/feed/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# 3. 训练宠物攻击
curl -X POST http://localhost:8081/api/pets/train/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"trainingType":"攻击"}'

# 4. 设置出战宠物
curl -X POST http://localhost:8081/api/pets/activate/1 \
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
- [完整测试指南](TESTING_GUIDE.md)
- [项目更新指南](PROJECT_UPDATE_GUIDE.md)
- [部署检查清单](DEPLOYMENT_CHECKLIST.md)
- [代码优化总结](OPTIMIZATION_SUMMARY.md)
- [API测试文件](api-test.http)

## 🎨 前端页面

### 主要页面
- `index.html` - 游戏主页(修炼、技能、任务)
- `pets.html` - 宠物系统页面 🆕
- `login.html` - 登录页面
- `cultivate.html` - 修炼专用页面
- `admin.html` - 管理后台

### 宠物系统页面功能
- **我的宠物**: 查看和管理已拥有的宠物
- **捕获宠物**: 浏览和捕获新宠物
- **出战宠物**: 查看出战宠物详情,快速喂食和训练
- **宠物操作**: 喂食、训练、重命名、锁定、释放等

### 访问方式
1. 启动应用后访问: http://localhost:8081
2. 登录后点击顶部"🐾 宠物系统"按钮
3. 或直接访问: http://localhost:8081/pets.html
