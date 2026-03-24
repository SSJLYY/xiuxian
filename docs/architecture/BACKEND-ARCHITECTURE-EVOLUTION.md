# 修仙挂机游戏 - 后端架构升级方案

> 本文档为系统架构升级提供渐进式路径，兼顾扩展性提升与稳定性保障。
> 方案设计遵循：**最小化风险 → 逐步演进 → 持续优化**

**作者**: shaun.sheng  
**日期**: 2026-03-24  
**版本**: 1.0

---

## 📊 当前系统诊断

### 技术现状

| 维度 | 当前状态 | 评估 |
|------|----------|------|
| 架构模式 | 单体应用 (Spring Boot) | 适用于当前规模 |
| 编程语言 | Java 1.8 | 建议升级到 11+ |
| 数据层 | MyBatis-Plus + MySQL 8.0 | 基础扎实 |
| 安全 | Spring Security + JWT | 架构合理 |
| 缓存 | 未启用 (Spring Cache simple) | 需优化 |
| 部署 | Docker Compose | 容器化良好 |
| 监控 | 基础 Actuator | 需增强 |

### 规模指标

- **代码量**: 317 Java 文件
- **API**: 44 Controller, 62 Mapper
- **业务模块**: 50+ Service
- **数据库表**: 40+ 张

### 潜在瓶颈识别

| 模块 | 风险点 | 优先级 |
|------|--------|--------|
| 战斗系统 | 高频计算，数据库IO密集 | 🔴 高 |
| 排行榜 | 全表扫描，实时性要求 | 🔴 高 |
| 拍卖行 | 并发竞拍，数据一致性 | 🟡 中 |
| 宗门BOSS | 多玩家同时挑战 | 🟡 中 |
| 缓存层 | 无缓存，DB压力大 | 🔴 高 |

---

## 🎯 架构演进路线图

```
阶段一: 基础设施加固 ✅ 已完成 (1-2周)
    │
    ├── 1.1 Redis 缓存接入 ✅
    ├── 1.2 数据库读写分离 ✅
    ├── 1.3 监控体系完善 ✅
    └── 1.4 限流/熔断降级 ✅

阶段二: 模块化重构 (2-4周)
    │
    ├── 2.1 包结构按领域重组
    ├── 2.2 API网关引入
    ├── 2.3 配置中心外置
    └── 2.4 分布式任务调度

阶段三: 服务化探索 (长期)
    │
    ├── 3.1 核心服务剥离
    ├── 3.2 服务注册与发现
    └── 3.3 分布式事务方案
```

---

## 🚀 阶段一：基础设施加固

### 1.1 Redis 缓存接入

**目标**: 减少数据库压力，提升热点数据访问速度

**实施方案**:

```yaml
# application.yml 配置示例
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 5
        max-wait: 3000ms
```

**缓存策略设计**:

| 业务数据 | 缓存策略 | TTL | 失效方式 |
|----------|----------|-----|----------|
| 用户Token | String | 24h | 主动删除 |
| 玩家基础信息 | Hash | 30min | LRU |
| 排行榜数据 | Sorted Set | 5min | 定时刷新 |
| 拍卖行物品 | Hash | 1min | 实时更新 |
| 宗门BOSS状态 | String | 实时 | 事件驱动 |
| 游戏配置 | String | 1h | 配置变更 |

**Spring Cache 抽象封装**:

```java
@Configuration
@EnableCaching
public class RedisCacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 按业务划分不同缓存空间
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        
        // 玩家数据缓存 - 较长TTL
        configs.put("playerCache", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class))));
        
        // 战斗缓存 - 极短TTL
        configs.put("combatCache", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(10))
                .disableCachingNullValues());
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(configs.get("playerCache"))
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
```

### 1.2 数据库读写分离

**目标**: 分离读写压力，提升查询性能

**架构图**:

```
                    ┌─────────────┐
                    │   App 应用   │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              ▼                         ▼
       ┌─────────────┐          ┌─────────────┐
       │   主库写节点   │          │   从库读节点   │
       │  (Master)    │  ──同步──▶│  (Slave x2) │
       └─────────────┘          └─────────────┘
```

**实施方案**:

1. **Spring Boot 多数据源配置**:

```java
@Configuration
public class DataSourceConfig {
    
    @Primary
    @Bean(name = "masterDataSource")
    @ConfigurationProperties("spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean(name = "slaveDataSource")
    @ConfigurationProperties("spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource master,
            @Qualifier("slaveDataSource") DataSource slave) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", master);
        targetDataSources.put("slave", slave);
        
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(master);
        return routingDataSource;
    }
}
```

2. **MyBatis-Plus 读写分离路由**:

```java
public class RoutingDataSource extends AbstractRoutingDataSource {
    
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    
    public static void setDataSource(String name) {
        CONTEXT_HOLDER.set(name);
    }
    
    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }
    
    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }
    
    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = CONTEXT_HOLDER.get();
        // 写操作走主库，读操作走从库（默认）
        return dataSource != null ? dataSource : "slave";
    }
}
```

3. **Service 层使用注解切换数据源**:

```java
@Service
public class PlayerService {
    
    @Autowired
    private PlayerMapper playerMapper;
    
    // 强制走主库（写入后立即读取场景）
    @DataSource("master")
    public Player createPlayer(Player player) {
        playerMapper.insert(player);
        return player; // 立即返回需要走主库
    }
    
    // 默认走从库（读取场景）
    public Player getPlayerById(Long playerId) {
        return playerMapper.selectById(playerId);
    }
}
```

### 1.3 监控体系完善

**目标**: 全链路可观测，问题快速定位

**技术选型**:

| 组件 | 用途 | 引入方式 |
|------|------|----------|
| Micrometer | 指标采集 | spring-boot-starter-actuator |
| Prometheus | 指标存储 | 独立部署/Docker |
| Grafana | 可视化面板 | 独立部署/Docker |
| SkyWalking | 链路追踪 | Java Agent |

**关键指标采集**:

```java
@RestController
@RequiredArgsConstructor
public class MetricsController {
    
    private final MeterRegistry meterRegistry;
    
    @GetMapping("/metrics/game")
    public Map<String, Object> gameMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // 战斗QPS
        metrics.put("combat_qps", meterRegistry.counter("game.combat.total").count());
        
        // 平均响应时间
        metrics.put("avg_response_time", 
                meterRegistry.timer("game.api.response").mean());
        
        // 在线玩家数
        metrics.put("online_players", 
                meterRegistry.gauge("game.players.online").gauge().value());
        
        // 活跃连接数
        metrics.put("active_connections", 
                meterRegistry.gauge("db.connections.active").gauge().value());
        
        return metrics;
    }
}
```

**Grafana 仪表盘关键面板**:

1. **系统概览**: CPU/内存/磁盘/网络
2. **应用健康**: QPS/响应时间/错误率
3. **业务指标**: 在线人数/战斗次数/付费转化
4. **数据库**: 连接池/慢查询/死锁
5. **缓存**: 命中率/内存使用

### 1.4 限流与熔断降级

**目标**: 保护系统不被过载，保障核心功能可用

**技术选型**:

- **限流**: Sentinel / Bucket4j
- **熔断**: Resilience4j / Sentinel

**Sentinel 集成方案**:

```java
@Configuration
public class SentinelConfig {
    
    @PostConstruct
    public void init() {
        // 初始化规则
        List<FlowRule> rules = new ArrayList<>();
        
        // 战斗接口限流 - 每秒100次
        FlowRule combatRule = new FlowRule("/api/combat/*");
        combatRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        combatRule.setCount(100);
        combatRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        combatRule.setMaxQueueingTimeMs(500);
        rules.add(combatRule);
        
        // 核心接口限流 - 每秒200次
        FlowRule coreRule = new FlowRule("/api/player/*");
        coreRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        coreRule.setCount(200);
        rules.add(coreRule);
        
        FlowRuleManager.loadRules(rules);
    }
}
```

**服务降级策略**:

| 场景 | 降级策略 | 实现方式 |
|------|----------|----------|
| 战斗高峰期 | 降低掉落率，保战斗成功 | 降级开关 |
| 排行榜查询 | 返回缓存数据 | 降级开关 |
| 拍卖行竞拍 | 返回"繁忙，稍后重试" | 熔断器 |
| 非核心推送 | 关闭/延迟推送 | 开关 |
| 战斗日志 | 异步写入，降级为内存缓冲 | 降级 |

```java
@Service
public class CombatService {
    
    @Value("${combat.drop.rate.multiplier:1.0}")
    private double dropRateMultiplier;
    
    @SentinelResource(value = "combat", 
            fallback = "combatFallback",
            blockHandler = "combatBlockHandler")
    public CombatResult fight(Long playerId, Long monsterId) {
        // 正常战斗逻辑
        return calculateCombat(playerId, monsterId);
    }
    
    // 降级逻辑
    public CombatResult combatFallback(Long playerId, Long monsterId) {
        // 降级：返回简化战斗结果，保证核心体验
        return CombatResult.quickFight(playerId, monsterId);
    }
    
    // 熔断逻辑
    public CombatResult combatBlockHandler(Long playerId, Long monsterId, BlockException e) {
        throw new BusinessException(ErrorCode.SERVER_BUSY);
    }
}
```

---

## 🔧 阶段二：模块化重构

### 2.1 包结构按领域重组

**目标**: 降低耦合，为未来服务化打基础

**当前包结构** (平铺式):
```
com.xiuxian.game/
├── controller/   (44个)
├── service/      (51个)
├── mapper/       (62个)
└── entity/       (62个)
```

**目标包结构** (领域驱动):
```
com.xiuxian.game/
├── common/                      # 公共层
│   ├── annotation/
│   ├── aspect/
│   ├── config/
│   ├── dto/
│   ├── exception/
│   ├── security/
│   └── util/
│
├── modules/                     # 业务模块（未来可独立部署）
│   ├── player/                 # 玩家模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── entity/
│   │
│   ├── combat/                 # 战斗模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── entity/
│   │
│   ├── cultivation/            # 修炼模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── entity/
│   │
│   ├── pet/                    # 宠物模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── entity/
│   │
│   ├── social/                 # 社交模块（宗门/拍卖/排行）
│   │   ├── guild/
│   │   ├── auction/
│   │   └── ranking/
│   │
│   ├── narrative/              # 叙事模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── entity/
│   │
│   └── admin/                  # 运营管理模块
│       ├── controller/
│       ├── service/
│       └── entity/
│
└── XiuxianGameApplication.java
```

**重构策略**:

1. **渐进式迁移**: 按模块逐个移动，保持系统可用
2. **包可见性**: 模块间通过API通信，内部细节隐藏
3. **依赖管理**: 依赖关系只能单向（外层→内层），禁止循环依赖

### 2.2 API 网关引入

**目标**: 统一入口、认证鉴权、限流、路由

**技术选型**: Spring Cloud Gateway / Kong

**Spring Cloud Gateway 方案**:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: game-api
          uri: lb://xiuxian-game
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
            - name: JwtVerify
              args:
                secret: ${JWT_SECRET}
        - id: admin-api
          uri: lb://xiuxian-game
          predicates:
            - Path=/admin/**
          filters:
            - StripPrefix=1
            - AdminJwtVerify
```

**网关核心功能**:

| 功能 | 说明 |
|------|------|
| 路由管理 | 根据路径前缀转发到对应服务 |
| 认证校验 | JWT Token 验证（游戏端/管理端） |
| 限流 | 基于Redis的令牌桶限流 |
| 日志 | 请求/响应日志，链路追踪ID传递 |
| 熔断 | 下游服务故障时快速失败 |

### 2.3 配置中心外置

**目标**: 集中管理配置，动态刷新

**技术选型**: Spring Cloud Config / Nacos

**Nacos 方案优势**:

- 支持配置变更推送（Bus）
- 支持命名空间隔离（游戏/测试/开发）
- 支持配置版本管理

**配置分层设计**:

```
Nacos 配置命名空间
│
├── xiuxian-game-prod
│   ├── application.yml         # 公共配置
│   ├── game-api.yml            # 游戏API配置
│   └── admin-api.yml           # 管理API配置
│
├── xiuxian-game-test
│   └── ...
│
└── xiuxian-game-dev
    └── ...
```

### 2.4 分布式任务调度

**目标**: 定时任务统一管理，支持集群执行

**技术选型**: XXL-Job / Elastic-Job

**适用场景**:

| 任务 | 频率 | 调度策略 |
|------|------|----------|
| 排行榜刷新 | 5分钟 | 分片广播 |
| 离线收益计算 | 10分钟 | 单机 |
| 宗门BOSS重置 | 每日0点 | 单机 |
| 签到重置 | 每日4点 | 单机 |
| 统计数据汇总 | 每日1点 | 分片广播 |
| 缓存预热 | 每日5点 | 单机 |

---

## 🏗️ 阶段三：服务化探索（可选）

> ⚠️  仅建议在团队规模扩大、系统复杂度达到单体上限时考虑

### 3.1 核心服务剥离策略

**拆分原则**:

| 维度 | 标准 |
|------|------|
| 业务边界 | 独立业务域，无强事务依赖 |
| 团队负责 | 独立团队可维护 |
| 部署频率 | 独立迭代 |
| 资源消耗 | CPU/内存使用可独立评估 |

**推荐拆分顺序**:

```
第1批（低风险）
├── 用户服务 (User Service)
│   └── 认证、注册、个人信息
│   
├── 配置服务 (Config Service)
│   └── 游戏配置、系统配置
│
└── 消息服务 (Message Service)
    └── 邮件、公告、系统通知

第2批（中风险）
├── 玩家服务 (Player Service)
│   └── 玩家数据、背包、成就
│
└── 战斗服务 (Combat Service)
    └── 战斗计算、怪物AI

第3批（高风险）
├── 社交服务 (Social Service)
│   └── 宗门、好友、排行榜
│
└── 经济服务 (Economy Service)
    └── 商城、拍卖、交易
```

### 3.2 服务注册与发现

**技术选型**: Nacos / Consul / Eureka

**Nacos 推荐理由**:

- 同时支持配置中心和服务注册
- 国内生态成熟
- 与 Spring Cloud 集成良好

### 3.3 分布式事务方案

| 方案 | 适用场景 | 复杂度 | 性能 |
|------|----------|--------|------|
| TCC | 强一致性 | 高 | 高 |
| Saga | 最终一致 | 中 | 高 |
| 可靠消息 | 最终一致 | 中 | 中 |
| 2PC | 强一致 | 高 | 低 |

**本项目推荐**: 可靠消息 + 最终一致

- 战斗结果 → 发放奖励（可靠消息）
- 拍卖成交 → 资产转移（TCC）
- 宗门转让 → 状态变更（Saga）

---

## 📋 实施建议

### 优先级排序

| 序号 | 任务 | 状态 | 预估工时 | 收益 | 风险 |
|------|------|------|----------|------|------|
| 1 | Redis 缓存接入 | ✅ 已完成 | 3天 | ⭐⭐⭐⭐ | 低 |
| 2 | 监控体系完善 | ✅ 已完成 | 2天 | ⭐⭐⭐ | 低 |
| 3 | 限流/熔断降级 | ✅ 已完成 | 3天 | ⭐⭐⭐⭐ | 中 |
| 4 | 数据库读写分离 | ✅ 已完成 | 5天 | ⭐⭐⭐⭐ | 中 |
| 5 | 包结构领域重组 | ⏳ 待进行 | 1周 | ⭐⭐⭐ | 中 |
| 6 | API 网关引入 | ⏳ 待进行 | 2周 | ⭐⭐⭐⭐ | 高 |
| 7 | 配置中心 | ⏳ 待进行 | 1周 | ⭐⭐⭐ | 中 |

### 关键里程碑

- **Week 1-2**: ✅ Redis 缓存 + 监控 + 限流完成
- **Week 3-4**: ✅ 读写分离 + 熔断降级完成
- **Week 5-6**: 包结构重组 + API 网关

### 风险控制

1. **灰度发布**: 每项变更先切 5% 流量验证
2. **回滚方案**: 每次变更准备回滚脚本
3. **监控告警**: 变更后 24 小时密切监控
4. **文档同步**: 架构变更同步更新文档

---

## ✅ 总结

本方案提供**渐进式架构升级**路径，核心思路：

1. **先强身健体**（基础设施）: 缓存+监控+限流，快速提升稳定性
2. **再疏通经络**（模块化）: 领域划分+API网关，提升可维护性
3. **最后强筋骨**（服务化）: 按需拆分，独立部署

**推荐执行路径**:

```
当前单体 ──▶ 阶段一完成 ──▶ 阶段二完成 ──▶ 阶段三（可选）
  (运行中)    (高性能)      (高可维护)     (分布式)
```

如团队规模 < 5 人，建议**仅执行阶段一**，已能获得良好扩展性和稳定性。

---

*文档版本: 1.0 | 最后更新: 2026-03-24*
