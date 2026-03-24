# 后端架构总览

> 本文档描述修仙挂机游戏后端的技术架构、分层设计和核心设计决策。
> 适合：新加入的后端开发者、代码审查者、做架构扩展时参考。

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-24

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

```
com.xiuxian.game/
├── XiuxianGameApplication.java   # 启动类
│
├── controller/     # REST 控制器（44 个）
│   ├── 游戏系统:  AuthController, PlayerController, CombatController,
│   │             SkillController, PetController, QuestController,
│   │             EquipmentController, InventoryController, ShopController,
│   │             OfflineRewardController
│   ├── 社交系统:  GuildController, GuildBossController, AuctionController,
│   │             RankingController, AchievementController
│   ├── 运营系统:  MailController, AnnouncementController, ActivityController,
│   │             GiftCodeController, VipController, CheckInController
│   ├── 叙事系统:  NpcController, DialogueController, LoreController,
│   │             NarrativeController
│   ├── 地图系统:  GameMapController
│   └── 管理后台:  Admin* 系列（14 个）
│
├── service/        # 业务逻辑层（50+ 个）
├── mapper/         # MyBatis Mapper 接口（62 个）
├── entity/         # 数据库实体（62 个）
├── dto/            # 数据传输对象（29 个）
│   ├── request/    # 请求 DTO（含参数校验注解）
│   └── response/   # 响应 DTO
│
├── config/         # Spring 配置类
│   ├── SecurityConfig.java         # Spring Security 主配置
│   ├── AdminSecurityConfig.java    # 管理员安全配置（独立）
│   ├── CorsConfig.java             # 跨域配置
│   ├── MybatisPlusConfig.java      # MyBatis-Plus 分页插件
│   ├── RedisConfig.java            # Redis 连接池、序列化、CacheManager
│   ├── DegradeConfig.java          # 降级开关配置
│   └── GameHealthIndicator.java    # Actuator 健康检查（含 Redis 状态）
│
├── security/       # 安全层
│   ├── JwtTokenProvider.java           # JWT 生成与验证
│   ├── JwtAuthenticationFilter.java    # 游戏端 JWT 过滤器
│   ├── AdminJwtAuthenticationFilter.java # 管理端 JWT 过滤器
│   └── CustomUserDetailsService.java   # 用户认证服务
│
├── exception/      # 异常体系
│   ├── BusinessException.java      # 业务异常基类
│   ├── ErrorCode.java              # 错误码枚举
│   └── GlobalExceptionHandler.java # 全局异常处理器
│
├── annotation/     # 自定义注解（限流、权限等）
├── aspect/         # AOP 切面（日志、限流）
├── util/           # 工具类（11 个）
└── validation/     # 自定义校验器（39 个）
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
| JWT 过滤器 | `JwtAuthenticationFilter` | `AdminJwtAuthenticationFilter` |
| Security 配置 | `SecurityConfig` | `AdminSecurityConfig` |
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

---

## 扩展新系统的标准流程

1. **数据库**：在 `init-database.sql` 添加建表语句（同时更新 `docs/architecture/DATABASE-DESIGN.md`）
2. **Entity**：在 `entity/` 创建实体类，继承 `BaseEntity`（含 createTime/updateTime）
3. **Mapper**：在 `mapper/` 创建 Mapper 接口
4. **ErrorCode**：在 `ErrorCode.java` 中分配新段的错误码（参考 [ErrorCode 手册](../standards/ERROR-CODE-REFERENCE.md)）
5. **Service**：业务逻辑，加上 `@Service`，无状态设计
6. **DTO**：在 `dto/request/` 和 `dto/response/` 分别创建请求/响应 DTO
7. **Controller**：路由层，只做参数校验和结果包装，不写业务逻辑
8. **文档**：更新 `docs/api/` 对应的 API 文档
