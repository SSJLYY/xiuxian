# 后端架构总览

> 本文档描述修仙挂机游戏后端的技术架构、分层设计和核心设计决策。
> 适合：新加入的后端开发者、代码审查者、做架构扩展时参考。

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-04-17

---

## 技术栈

| 层 | 技术 | 版本 | 说明 |
|----|------|------|------|
| 语言 | Java | 1.8 | |
| 框架 | Spring Boot | 2.7.18 | 内嵌 Tomcat |
| 安全 | Spring Security + JWT | 5.7.x | 无状态认证 |
| ORM | MyBatis-Plus | 3.5.3.1 | 代码生成、条件构造器 |
| 数据库 | MySQL | 8.0+ | |
| 连接池 | HikariCP | 4.0.x | |
| **缓存** | **Redis + Lettuce** | **6.0+** | **主缓存层，支持自动降级到本地缓存** |
| 日志 | Log4j2 | 2.17.x | 异步日志 |
| 构建 | Maven | 3.6+ | |

---

## 包结构

重构后采用**四包模块化架构**，职责清晰，严格隔离：

```
com.xiuxian.game/
├── XiuxianGameApplication.java   # 启动类（@MapperScan 扫描 22 个业务模块 + 根 mapper 包）
│
├── common/         # 公共基础设施（42 个文件）
│   ├── annotation/     # 自定义注解
│   │   ├── DataSource.java         # 多数据源切换
│   │   └── RateLimit.java          # 接口限流
│   ├── aspect/         # AOP 切面
│   │   ├── DataSourceAspect.java   # 数据源切换切面
│   │   └── RateLimitAspect.java    # 限流切面
│   ├── config/         # Spring 配置（16 个）
│   │   ├── SecurityConfig.java         # Spring Security 主配置
│   │   ├── CorsConfig.java             # 跨域配置
│   │   ├── MybatisPlusConfig.java      # MyBatis-Plus 分页插件
│   │   ├── RedisConfig.java            # Redis 连接池、序列化、CacheManager
│   │   ├── DataSourceConfig.java       # 动态数据源配置
│   │   ├── RoutingDataSource.java      # 路由数据源
│   │   ├── DegradeConfig.java          # 降级开关配置
│   │   ├── SentinelConfig.java         # Sentinel 流控配置
│   │   ├── GameBalanceConfig.java      # 游戏平衡参数
│   │   ├── DataInitializer.java        # 启动时数据初始化
│   │   ├── AsyncConfig.java            # 异步任务线程池
│   │   ├── MetricsConfig.java          # 指标监控配置
│   │   ├── GameHealthIndicator.java    # Actuator 健康检查
│   │   ├── JacksonConfig.java          # JSON 序列化
│   │   ├── LoggingAspect.java          # 全局日志切面
│   │   └── WebConfig.java              # Web MVC 配置
│   ├── exception/      # 异常体系
│   │   ├── BusinessException.java      # 业务异常基类
│   │   ├── ErrorCode.java              # 错误码枚举
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   ├── security/       # 安全认证（5 个）
│   │   ├── JwtTokenProvider.java           # JWT 生成与验证
│   │   ├── JwtAuthenticationFilter.java    # 游戏端 JWT 过滤器
│   │   ├── AdminSecurityFilter.java        # 管理端安全过滤器
│   │   ├── SecurityFilter.java             # 通用安全过滤器
│   │   └── CustomUserDetailsService.java   # 用户认证服务
│   └── util/           # 工具类（14 个）
│       ├── LogUtils.java           # 结构化日志 + MDC 链路追踪
│       ├── RequestUtils.java       # 客户端 IP 解析（含代理链）
│       ├── CacheUtils.java         # Redis 直接操作（ZSet/Hash/分布式锁）
│       ├── GameBalanceUtils.java   # 游戏平衡数值计算
│       ├── GameCalculator.java     # 通用数值计算器
│       ├── GameConstants.java      # 游戏全局常量
│       ├── DegradeUtils.java       # 降级工具
│       ├── PerformanceMonitor.java # 性能监控
│       ├── RealmUtil.java          # 境界工具
│       ├── RateLimiter.java        # 本地限流器
│       ├── JsonUtil.java           # JSON 工具
│       ├── DateUtil.java           # 日期工具
│       ├── PageUtil.java           # 分页工具
│       └── Java8Compatibility.java # Java 8 兼容工具
│
├── modules/        # 业务模块（22 个，221 个文件）
│   │   # 每个模块包含 controller / entity / mapper / service 四层
│   ├── player/         # 玩家模块（认证/档案/物品/登录日志）4C+4E+4M+5S=17
│   ├── combat/         # 战斗模块（PVE战斗/怪物/战斗日志）1C+3E+3M+2S=9
│   ├── cultivation/    # 修炼模块（仅 entity+mapper，无 controller/service）
│   ├── equipment/      # 装备模块（装备管理/背包）2C+2E+2M+3S=9（含3个Service）
│   ├── skill/          # 技能模块（学习/连招/技能商店）1C+5E+5M+2S=13
│   ├── pet/            # 宠物模块（培养/进化/技能/训练）1C+7E+7M+1S=16
│   ├── quest/          # 任务模块（每日/每周/每月任务）1C+2E+2M+2S=7
│   ├── shop/           # 商城模块（物品购买）1C+2E+2M+2S=7
│   ├── achievement/    # 成就模块（成就/管理员成就）2C+2E+2M+1S=7（含Admin Controller）
│   ├── guild/          # 宗门模块（公会/公会BOSS）2C+5E+5M+2S=14
│   ├── ranking/        # 排行榜模块 1C+1E+1M+1S=4
│   ├── auction/        # 拍卖行模块 1C+1E+1M+1S=4
│   ├── mail/           # 邮件模块（含异步邮件）1C+2E+2M+2S=7
│   ├── narrative/      # 叙事模块（NPC/对话树/世界观/离线叙事）4C+10E+10M+4S=28
│   ├── map/            # 地图模块（地图节点/玩家进度）1C+2E+2M+1S=6
│   ├── offline/        # 离线挂机模块 1C+1E+1M+1S=4
│   ├── checkin/        # 签到模块 1C+1E+1M+1S=4
│   ├── activity/       # 活动模块 2C+2E+2M+1S=7（含Admin Controller）
│   ├── giftcode/       # 礼包码模块 1C+2E+2M+1S=6
│   ├── announcement/   # 公告模块 2C+1E+1M+1S=5（含Admin Controller）
│   ├── vip/            # VIP模块（充值/等级权益）1C+3E+3M+2S=9
│   └── admin/          # 管理后台模块（16 个 Service，13 个 Controller）共 35 个文件
│
├── dto/            # 统一数据传输对象（29 个文件）
│   ├── request/        # 请求 DTO（12 个，含 @Valid 校验注解）
│   │   ├── LoginRequest.java / RegisterRequest.java
│   │   ├── AdminLoginRequest.java
│   │   ├── SkillLearnRequest.java / SkillUpgradeRequest.java / SkillEquipRequest.java
│   │   ├── ShopBuyRequest.java
│   │   ├── ItemAddRequest.java / ItemRemoveRequest.java / ItemUseRequest.java
│   │   └── QuestClaimRequest.java / QuestProgressUpdateRequest.java
│   ├── response/       # 响应 DTO（14 个）
│   │   ├── ApiResponse.java / AdminApiResponse.java   # 统一响应包装
│   │   ├── LoginResponse.java / AdminLoginResponse.java
│   │   ├── CombatResult.java           # 战斗结果
│   │   ├── PetCombatBonus.java         # 宠物战斗加成
│   │   ├── PlayerEquipmentResponse.java / PlayerItemResponse.java
│   │   ├── PlayerSkillResponse.java / SkillResponse.java
│   │   ├── PlayerQuestResponse.java / PlayerQuestDetailResponse.java / QuestResponse.java
│   │   ├── ShopItemResponse.java
│   │   └── OfflineRewardResponse.java
│   ├── PetEvolutionResult.java         # 宠物进化结果
│   ├── SkillComboResult.java           # 技能连招结果
│   ├── ListAuctionRequest.java         # 拍卖列表查询请求（替代Controller内部类）
│   └── SystemMailRequest.java          # 系统邮件请求（封装6参数）
│
└── validation/     # 启动校验框架（39 个文件）
    # 应用启动时自动校验 API 响应与数据库 Schema 的一致性
    ├── StartupValidationService.java   # 校验总入口（ApplicationRunner）
    ├── SchemaAnalyzer.java             # 数据库 Schema 分析
    ├── FieldMappingValidator.java      # 字段映射校验
    ├── MissingFieldDetector.java       # 缺失字段检测
    ├── APIResponseValidator.java       # API 响应结构校验
    ├── TypeStandardizationService.java # 类型标准化
    ├── DataConsistencyValidator.java   # 数据一致性校验
    └── ValidationErrorLogger.java      # 校验错误日志记录
```

### 模块间依赖规范

```
modules.A  →  modules.B（通过 B 的 Service 接口）  ✅
modules.A  →  modules.B.mapper（直接调用 Mapper） ❌ 禁止！
modules.*  →  common.*（公共组件）                 ✅
modules.*  →  dto.*（统一 DTO）                    ✅
```

---

## 请求处理流程

```
HTTP 请求
    │
    ▼
[CORS Filter]                       # 跨域预检处理
    │
    ▼
[JwtAuthenticationFilter]           # 解析 JWT，设置 SecurityContext
    │
    ▼
[RateLimitAspect]                   # AOP 限流检查（@RateLimit 注解）
    │
    ▼
[Controller]                        # 参数校验（@Valid）、路由分发
    │
    ▼
[Service]                           # 业务逻辑、事务管理
    │
    ▼
[Mapper]                            # 数据库操作（MyBatis-Plus）
    │
    ▼
[MySQL]
    │
    ▼（异常路径）
[GlobalExceptionHandler]            # 统一异常 → ApiResponse 响应
```

---

## 缓存层

项目引入了 Redis 双层缓存架构，Service 层可通过两种方式使用缓存：

**方式一：声明式注解缓存（Spring Cache）**
```java
@Cacheable(value = "playerCache", key = "#playerId")    // 自动回填
@CacheEvict(value = "playerCache", key = "#playerId")   // 自动驱逐
```

**方式二：手动操作**
```java
@Autowired RedisCacheService redisCacheService;  // 双层缓存，自动降级
@Autowired CacheUtils cacheUtils;                // 直接操作 Redis（ZSet/Hash等）
```

Redis 不可用时自动降级到本地内存缓存（`ConcurrentHashMap`），每 30 秒探活一次，恢复后自动切回。

> 详见 [缓存架构文档](./CACHE-ARCHITECTURE.md)

---

## 统一响应格式

所有 API 返回 `ApiResponse<T>` 包装，结构如下：

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

失败时：
```json
{
  "success": false,
  "code": 1001,
  "message": "用户名已存在",
  "data": null
}
```

**规范**：
- 永远不要直接 `return null`，用 `ApiResponse.error(ErrorCode.XXX)` 返回语义错误
- HTTP 状态码：业务错误统一返回 `200`，仅系统级错误（500）使用非 200

---

## 异常体系

### 抛出方式

```java
// ✅ 正确：抛出 BusinessException
throw new BusinessException(ErrorCode.USER_NOT_FOUND);
throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES, "需要" + cost + "灵石");

// ❌ 错误：不要用裸 RuntimeException
throw new RuntimeException("用户不存在");
```

### 处理链

`GlobalExceptionHandler` 按以下顺序处理（子类优先）：

1. `BusinessException` → 取 ErrorCode 的 code 和 message
2. `MethodArgumentNotValidException` → 参数校验失败，返回第一条错误信息
3. `AccessDeniedException` → 权限不足
4. `Exception` → 兜底，返回 500

> ⚠️ **不要添加 `RuntimeException` 处理器**——它会吃掉 `BusinessException`。

---

## 双认证系统

项目游戏端和管理端使用**完全独立**的认证体系：

| 特性 | 游戏端 | 管理端 |
|------|--------|--------|
| 登录接口 | `POST /api/auth/login` | `POST /api/admin/auth/login` |
| Token 存储 Key | `authToken` | `adminToken` |
| JWT 过滤器 | `JwtAuthenticationFilter` | `AdminSecurityFilter` |
| Security 配置 | `SecurityConfig`（含双端规则） | `SecurityConfig`（同一配置类） |
| 用户角色 | `PLAYER` | `ADMIN` |

两套系统**互不影响**——游戏端 Token 无法访问管理端接口，反之亦然。

---

## 事务规范

```java
// ✅ 正确：只在"写"操作方法上加事务
@Transactional
public void transferSpiritStones(Long fromId, Long toId, int amount) {
    // ...
}

// ❌ 错误：在读+写混合的外层入口方法加事务（会造成长事务问题）
@Transactional
public PlayerProfileVO getPlayerWithStats(Long playerId) {
    // 读操作 + 可能的懒加载
}
```

**原则**：事务边界尽可能小，只包裹必须原子的写操作。

---

## 日志规范

使用 `LogUtils` 工具类，而非直接使用 `Logger`：

```java
// 结构化日志（自动包含 MDC 链路追踪 ID）
LogUtils.info(log, "用户登录成功", "userId", userId, "ip", ip);
LogUtils.error(log, "战斗计算异常", e, "playerId", playerId);

// 日志分级原则：
// - info：请求入口/出口、关键业务完成
// - debug：中间计算步骤、条件分支
// - warn：业务降级、预期内的异常情况
// - error：非预期异常、需要人工介入
```

---

## 工具类说明

| 类 | 用途 | 注意 |
|----|------|------|
| `RequestUtils.getClientIp()` | 获取客户端真实 IP | 已处理 X-Forwarded-For 代理链 |
| `LogUtils` | 结构化日志 + MDC | 包含链路追踪 ID |
| `JwtTokenProvider` | JWT 生成/解析/验证 | 不要在 Service 层直接操作 JWT |
| `ThreadLocalRandom.current()` | 随机数 | Service 是单例，**禁止** `new Random()` |
| `RedisCacheService` | 双层缓存读写 | Redis 优先，失败自动降级到本地缓存 |
| `CacheUtils` | 直接操作 Redis | 支持 String/ZSet/Hash/分布式锁 |
| `Java8Compatibility` | Java 8 兼容工具 | 模拟 Java 9+ 的 mapOf()/listOf()/toList()/isEmpty() |

---

## 扩展新系统的标准流程

1. **建模块目录**：在 `modules/` 下新建 `your-module/{controller,entity,mapper,service}/`
2. **数据库**：在 `init-database.sql` 添加建表语句（同时更新 `docs/architecture/DATABASE-DESIGN.md`）
3. **Entity**：在 `modules/your-module/entity/` 创建实体类
4. **Mapper**：在 `modules/your-module/mapper/` 创建 Mapper 接口，并在 `XiuxianGameApplication.java` 的 `@MapperScan` 中添加包路径
5. **ErrorCode**：在 `common/exception/ErrorCode.java` 中分配新段错误码（参考 [ErrorCode 手册](../standards/ERROR-CODE-REFERENCE.md)）
6. **Service**：在 `modules/your-module/service/` 创建业务逻辑类，加上 `@Service`，无状态设计
7. **DTO**：在 `dto/request/` 和 `dto/response/` 分别创建请求/响应 DTO（全局共享）
8. **Controller**：在 `modules/your-module/controller/` 创建路由层，只做参数校验和结果包装
9. **文档**：更新 `docs/api/` 对应的 API 文档

---

## 架构决策与演进

### 为什么选择 Spring Boot 2.7.18？

**技术选型背景**：
- **稳定性优先**：2.7.x 是 Spring Boot 2.x 的最后一代 LTS 版本，社区支持完善
- **Java 8 兼容**：团队技术栈以 Java 8 为主，无需升级到 Java 11/17
- **生态成熟**：MyBatis-Plus、Spring Security 等依赖版本稳定
- **迁移成本低**：从早期版本平滑升级，无需大规模重构

**权衡考虑**：
- 暂不升级到 Spring Boot 3.x（需要 Java 17+）
- 等待核心依赖（MyBatis-Plus）完全兼容 Spring Boot 3.x 后再评估

### 为什么采用四层架构（Controller-Service-Mapper-Entity）？

**设计原则**：
1. **职责单一**：每层只做一件事
   - Controller：参数校验、路由分发、响应包装
   - Service：业务逻辑、事务管理、数据转换
   - Mapper：数据库 CRUD、条件构造
   - Entity：数据载体、数据库映射

2. **依赖倒置**：上层依赖下层接口，而非实现
   - Controller → Service 接口（而非 ServiceImpl）
   - Service → Mapper 接口（而非直接 SQL）

3. **可测试性**：每层可独立 Mock 测试
   - Controller 测试：Mock Service
   - Service 测试：Mock Mapper
   - 单元测试覆盖率高

**历史演进**：
- v1：三层架构（Controller-Service-Entity），直接操作 Entity
- v2：引入 Mapper 层，分离数据库操作
- v3：引入 DTO 层，分离 API 响应与数据库实体
- v4（当前）：完善验证层，启动时自动校验 API 与数据库一致性

### 为什么使用 MyBatis-Plus 而非 JPA？

**技术对比**：

| 特性 | MyBatis-Plus | JPA/Hibernate |
|------|-------------|--------------|
| 学习曲线 | 低（SQL 基础） | 中（需理解 HQL、实体关系） |
| SQL 控制力 | 强（可手写优化） | 弱（自动 ORM） |
| 性能调优 | 灵活（可精确优化） | 受限（N+1 问题） |
| 复杂查询 | 简单（Condition 构造器） | 复杂（Criteria API） |
| 代码生成 | 官方支持 | 需第三方工具 |

**决策理由**：
- 游戏业务包含大量复杂查询（排行榜、战斗计算、数值公式）
- 需要精确控制 SQL 性能（批量操作、索引优化）
- 团队更熟悉 SQL 而非 HQL

### 为什么采用模块化包结构？

**架构演进**：
- v1：按技术分层分包（所有 Controller 在一起）
- v2：按业务模块分包（每个模块独立的四层）

**优势**：
1. **职责清晰**：每个模块只做一件事
2. **低耦合**：模块间通过 Service 接口通信，禁止跨模块直接访问 Mapper
3. **易维护**：修改功能只需关注单个模块
4. **可扩展**：新增模块不影响现有代码
5. **团队协作**：不同开发者负责不同模块，减少冲突

**模块间依赖规则**：
```
✅ modules.A 的 Service → modules.B 的 Service（通过接口）
❌ modules.A 的 Service → modules.B 的 Mapper（禁止！）
✅ modules.* → common.*（公共组件，无环依赖）
```

### 缓存架构设计决策

**为什么使用 Redis 双层缓存？**

**需求背景**：
- 游戏数据读取频繁（玩家属性、装备、技能）
- 数据库连接数有限（HikariCP 最大 20 连接）
- 响应时间要求 < 100ms

**技术方案**：
1. **L1 缓存（Redis）**：集中式缓存，支持分布式
2. **L2 缓存（本地）**：降级缓存，Redis 失败时自动切换
3. **自动降级**：Service 层无感知，统一接口

**缓存策略**：
- 热点数据：玩家属性、装备、技能（永不过期，手动更新）
- 温数据：怪物模板、技能模板（TTL=1 小时）
- 冷数据：战斗日志、登录日志（TTL=7 天）

**性能提升**：
- 玩家属性查询：DB 50ms → Redis 5ms → 本地缓存 0.5ms
- QPS 提升：100 → 1000（10 倍）
- 数据库连接数下降：20 → 5

### 并发处理设计

**核心策略**：
1. **无状态设计**：Service 无成员变量，天然线程安全
2. **局部变量**：Random 使用 `ThreadLocalRandom.current()`
3. **并发容器**：计数器使用 `ConcurrentHashMap`
4. **异步处理**：统计、日志使用 `@Async`
5. **分布式锁**：跨服操作使用 Redis 锁

**典型场景**：
- 修炼计时：`System.currentTimeMillis()` 而非 `Date.now()`
- 随机数生成：`ThreadLocalRandom` 而非 `new Random()`
- 计数器：`AtomicLong` 而非 `long++`
- 战斗计算：纯函数，无副作用

### 性能优化里程碑

| 版本 | 优化内容 | 性能提升 |
|------|---------|---------|
| v1.0 | 初始版本，无缓存 | 基准 |
| v1.5 | 引入 Redis 缓存 | QPS +500% |
| v2.0 | 数据库索引优化 | 查询 -80% |
| v2.5 | 批量操作优化 | 写入 -60% |
| v3.0 | 双层缓存 + 自动降级 | 可用性 99.9% |
| v3.5 | N+1 查询优化 | 复杂查询 -90% |
| v4.0 | 异步统计 + 日志缓冲 | 主流程 -30% |

---

## 待优化事项

### 技术债务

1. **跨模块 Mapper 调用**（优先级：高）
   - 现状：历史代码中存在少量跨模块直接调用 Mapper
   - 影响：违反模块边界，难以测试
   - 计划：逐步重构为 Service 调用

2. **大事务**（优先级：中）
   - 现状：部分 Service 方法事务范围过大
   - 影响：数据库锁等待时间长
   - 计划：缩小事务边界，异步非核心操作

3. **日志刷屏**（优先级：低）
   - 现状：战斗日志每回合打印，信息量大
   - 影响：磁盘占用增长快
   - 计划：战斗日志改为 DEBUG 级别，定期归档

### 未来架构演进

**短期（1-3 个月）**：
- [ ] 引入 Spring Boot 3.x（评估中）
- [ ] 引入 Micrometer 指标监控
- [ ] 完善分布式追踪（Sleuth + Zipkin）

**中期（3-6 个月）**：
- [ ] 服务拆分（玩家服务、战斗服务、社交服务）
- [ ] 消息队列（RabbitMQ/Kafka）解耦异步操作
- [ ] 读写分离（主从数据库）

**长期（6-12 个月）**：
- [ ] 微服务架构（Spring Cloud）
- [ ] Kubernetes 容器化部署
- [ ] 服务网格（Istio）流量管理

---

## 参考文档

- [数据库设计](./DATABASE-DESIGN.md) - 完整表结构、ER 关系、索引
- [缓存架构](./CACHE-ARCHITECTURE.md) - Redis 双层缓存、降级策略
- [架构演进路线](./BACKEND-ARCHITECTURE-EVOLUTION.md) - 渐进式升级方案
- [性能优化指南](../standards/PERFORMANCE-GUIDE.md) - 数据库、N+1、缓存优化
- [API 总览](../api/API-OVERVIEW.md) - 接口规范、错误码

*文档最后更新：2026-04-17*
