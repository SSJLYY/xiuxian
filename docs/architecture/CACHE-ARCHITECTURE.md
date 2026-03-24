# 缓存架构设计

> 本文档描述修仙挂机游戏的缓存体系：技术选型、双层缓存架构、缓存空间划分、降级策略和使用规范。

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-24

---

## 1. 概述

### 1.1 技术选型

| 组件 | 版本 | 用途 |
|------|------|------|
| Spring Boot Starter Data Redis | 2.7.x | Redis 客户端集成 |
| Lettuce | 6.x（内置） | 连接池，支持异步/响应式 |
| Redisson（可选） | 3.x | 分布式锁高级特性 |
| Spring Cache (`@EnableCaching`) | 2.7.x | 声明式缓存注解支持 |

连接池配置（`application.properties`）：

```properties
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.database=0
spring.redis.timeout=3000ms
spring.redis.lettuce.pool.max-active=50
spring.redis.lettuce.pool.max-idle=20
spring.redis.lettuce.pool.min-idle=5
spring.redis.lettuce.pool.max-wait=3000ms
```

---

## 2. 双层缓存架构

```
业务代码
    │
    ▼
RedisCacheService / CacheUtils
    │
    ├── [Redis 可用] ──► Redis（主缓存）
    │                      持久化、分布式共享、大容量
    │
    └── [Redis 不可用 / 降级开关开启] ──► 本地内存缓存（ConcurrentHashMap）
                                          进程内、低延迟、容量有限
```

### 2.1 核心组件

| 类 | 包 | 职责 |
|----|-----|------|
| `RedisConfig` | `config` | RedisTemplate 和 CacheManager Bean 定义，序列化配置 |
| `RedisCacheService` | `service` | 双层缓存的核心服务，负责写入/读取/降级逻辑 |
| `CacheUtils` | `util` | 直接操作 Redis 的工具类，提供 String/ZSet/Hash 操作 |
| `CacheService` | `service` | 纯本地内存缓存服务（独立使用，或作为降级后备） |
| `DegradeConfig` | `config` | 降级开关配置，通过 `combat.fallback.enabled` 控制 |
| `GameHealthIndicator` | `config` | Actuator 健康检查扩展，暴露 Redis 状态 |

### 2.2 序列化方案

```
Key   →  StringRedisSerializer（字符串，人类可读）
Value →  Jackson2JsonRedisSerializer（JSON，支持 Java 8 时间类型）
```

`ObjectMapper` 开启了 `DefaultTyping.NON_FINAL`，序列化 JSON 时会在根字段带上类型信息，确保反序列化时类型正确还原。

---

## 3. 缓存空间（CacheManager 命名空间）

Spring Cache 通过 `@Cacheable`/`@CacheEvict` 注解使用以下命名缓存空间：

| 缓存名 | Key 前缀 | TTL | 典型数据 |
|--------|----------|-----|----------|
| `playerCache` | `xiuxian:player:` | 30 分钟 | 玩家基础信息、修炼状态 |
| `tokenCache` | `xiuxian:token:` | 24 小时 | JWT Token 黑名单 / 有效 Token |
| `rankingCache` | `xiuxian:ranking:` | 5 分钟 | 排行榜榜单数据 |
| `auctionCache` | `xiuxian:auction:` | 1 分钟 | 拍卖行列表（高频变动） |
| `combatCache` | `xiuxian:combat:` | 10 秒 | 战斗计算中间结果 |
| `configCache` | `xiuxian:config:` | 1 小时 | 游戏静态配置（掉率/公式参数） |
| `narrativeCache` | `xiuxian:narrative:` | 10 分钟 | NPC 对话树 / 传说图鉴 |
| _默认_ | — | 30 分钟 | 未显式配置的缓存 |

> **使用方式**：在 Service 方法上加注解，Spring 自动处理缓存穿透和回填。

```java
// 示例：读取玩家信息时自动缓存
@Cacheable(value = "playerCache", key = "#playerId")
public PlayerVO getPlayerInfo(Integer playerId) { ... }

// 示例：更新后自动驱逐缓存
@CacheEvict(value = "playerCache", key = "#playerId")
public void updatePlayer(Integer playerId, PlayerUpdateDTO dto) { ... }
```

---

## 4. 手动缓存键规范（CacheService.CacheKeys）

`CacheService` 中定义了手动操作 `RedisCacheService` 时的 Key 常量，命名格式统一为 `业务:标识`：

| 常量 | Key 格式 | 说明 |
|------|----------|------|
| `RANKING_PREFIX` | `ranking:{type}` | 排行榜，type = power/level/guild |
| `ANNOUNCEMENT_LIST` | `announcement:list` | 公告列表 |
| `ANNOUNCEMENT_PREFIX` | `announcement:{id}` | 单条公告 |
| `CONFIG_PREFIX` | `config:{key}` | 游戏配置项 |
| `PLAYER_PROFILE_PREFIX` | `player:profile:{playerId}` | 玩家简要信息 |
| `GUILD_PREFIX` | `guild:{guildId}` | 宗门数据 |
| `ACTIVITY_LIST` | `activity:list` | 活动列表 |

**注意**：手动操作缓存时统一使用上述常量，禁止在业务代码中硬编码 Key 字符串。

---

## 5. 降级策略

### 5.1 降级触发条件

| 触发方式 | 说明 |
|----------|------|
| Redis 连接异常 | `RedisCacheService` 捕获异常后自动将 `redisAvailable` 标记为 `false` |
| 配置开关 | `combat.fallback.enabled=true`，强制全局降级到本地缓存 |

### 5.2 降级流程

```
写操作：Redis 写失败 → 降级到 localCache（ConcurrentHashMap + TTL）
读操作：Redis 不可用 → 读 localCache → 未命中返回 null
删除操作：同时删 Redis 和 localCache
```

### 5.3 Redis 自动恢复

`RedisCacheService` 内置每 **30 秒**一次的 Redis 健康检测（PING 探活），Redis 恢复后自动将 `redisAvailable` 置回 `true`，流量透明切回。

### 5.4 降级配置示例

```properties
# application.properties
combat.fallback.enabled=true        # 强制全局降级（测试/压测场景）
combat.fallback.enabled=false       # 正常模式（生产默认）
```

---

## 6. CacheUtils — 直接操作 Redis

当业务需要直接操作 Redis 数据结构（不走 Spring Cache 注解），使用 `CacheUtils`：

### 基础操作

```java
// 注入
@Autowired
private CacheUtils cacheUtils;

// 设置（永不过期）
cacheUtils.set("key", value);

// 设置（带 TTL）
cacheUtils.setEx("key", value, 300);          // 300 秒
cacheUtils.set("key", value, 5, TimeUnit.MINUTES);

// 读取
SomeType result = cacheUtils.get("key");

// 删除
cacheUtils.delete("key");
cacheUtils.deleteByPrefix("ranking:");        // 前缀批量删除
```

### 分布式锁

```java
String lockKey = "lock:player:pay:" + playerId;
String lockValue = UUID.randomUUID().toString();

// 获取锁（5 秒自动过期，防死锁）
Boolean locked = cacheUtils.tryLock(lockKey, lockValue, 5, TimeUnit.SECONDS);
if (Boolean.TRUE.equals(locked)) {
    try {
        // 执行需要加锁的业务逻辑
    } finally {
        cacheUtils.unlock(lockKey, lockValue);  // 释放时校验 value，防止误删
    }
}
```

> **注意**：`unlock` 使用 Lua 脚本原子执行"检查 value 再删除"，避免并发误删其他线程的锁。

### ZSet 排行榜

```java
// 更新分数
cacheUtils.zAdd("ranking:power", playerId, 99999.0);

// 查询排名（从大到小，0-based）
Long rank = cacheUtils.zRevRank("ranking:power", playerId);

// 获取 Top 100
Set<Object> top100 = cacheUtils.zRevRange("ranking:power", 0, 99);
```

---

## 7. 健康检查

应用集成了 Spring Boot Actuator，`GameHealthIndicator` 在 `/actuator/health` 端点中暴露 Redis 状态：

```json
// Redis 正常时
{
  "status": "UP",
  "components": {
    "gameHealth": {
      "status": "UP",
      "details": {
        "redis": "OK",
        "cache": "OK"
      }
    }
  }
}

// Redis 断开时（已降级到本地缓存）
{
  "status": "DOWN",
  "components": {
    "gameHealth": {
      "status": "DOWN",
      "details": {
        "redis": "ERROR",
        "cache": "DEGRADED",
        "reason": "Redis connection failed, using local cache"
      }
    }
  }
}
```

---

## 8. 本地开发启动 Redis

### macOS / Linux

```bash
# 安装（macOS）
brew install redis

# 启动
redis-server

# 验证
redis-cli ping   # 返回 PONG 即成功
```

### Windows

```powershell
# 方式一：WSL 内运行
wsl redis-server

# 方式二：Docker（推荐）
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

### Docker Compose（项目内置）

项目 `docker-compose.yml` 已包含 Redis 服务，直接启动即可：

```bash
docker-compose up -d redis
```

---

## 9. 注意事项 & 常见陷阱

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| `ClassCastException` 反序列化失败 | 类结构变化导致 JSON 类型信息不匹配 | 变更 VO 类后用 `deleteByPrefix` 清空对应缓存 |
| 本地缓存数据不一致 | 多实例部署时每台机器本地缓存独立 | 生产多实例必须依赖 Redis，不能依赖本地缓存做数据共享 |
| `keys *` 性能问题 | `CacheUtils.deleteByPrefix` 内部调用 `KEYS` 命令 | 生产大数据量时改用 `SCAN` 游标方式遍历 |
| 缓存雪崩 | 大批 Key 同一时间过期 | TTL 加随机偏移量：`baseTtl + random(30)` 秒 |
| 分布式锁重入 | `tryLock` 不支持可重入 | 需要可重入锁时改用 Redisson 的 `RLock` |
