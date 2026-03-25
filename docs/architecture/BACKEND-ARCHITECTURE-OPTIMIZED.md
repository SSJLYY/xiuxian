# 修仙挂机游戏 - 后端架构优化方案（2H4G个人服务器版）

> 本文档针对2H4G个人服务器环境，设计精简高效的单体应用架构方案。
> 核心原则：**单体应用 + 领域模块化 + 轻量级基础设施**
> 避免：Spring Cloud、Nacos、Kubernetes、分布式事务等过度设计。

**作者**: shaun.sheng  
**日期**: 2026-03-24  
**版本**: 1.0

---

## 📊 现状评估

### 技术栈评估

| 组件 | 版本 | 评估 | 优化空间 |
|------|------|------|----------|
| **架构模式** | Spring Boot单体应用 | ✅ 适合个人项目 | 模块边界补全（1%） |
| **编程语言** | Java 1.8 | ⚠️ 偏老 | 建议升级到11+（可选） |
| **框架** | Spring Boot 2.7.18 | ✅ 稳定版 | 保持现状 |
| **ORM** | MyBatis-Plus 3.5.3.1 | ✅ 基础扎实 | 索引优化、连接池调优 |
| **安全** | Spring Security + JWT | ✅ 架构合理 | 保持现状 |
| **缓存** | Redis + Lettuce + 本地降级 | ✅ 完善方案 | TTL细化、连接池调优 |
| **限流/熔断** | Sentinel 1.8.6 | ✅ 已引入 | 参数调优（适配2H4G） |
| **监控** | Actuator + Prometheus | ✅ 轻量级 | 业务指标补充 |
| **数据库** | MySQL 8.0 | ✅ 良好 | 索引优化、连接池调优 |
| **连接池** | HikariCP | ✅ 高性能 | 参数调优（适配2H4G） |

### 规模指标

- **代码量**: 317 Java 文件
- **API**: 44 Controller，62 Mapper
- **业务模块**: 50+ Service
- **数据库表**: 40+ 张
- **前端文件**: 40 JS，20 HTML

### 潜在瓶颈识别

| 模块 | 风险点 | 优先级 | 优化方案 |
|------|--------|--------|----------|
| **战斗系统** | 高频计算，数据库IO密集 | 🔴 高 | 缓存细化、限流调优 |
| **排行榜** | 全表扫描，实时性要求 | 🔴 高 | 索引优化、缓存缩短TTL |
| **拍卖行** | 并发竞拍，数据一致性 | 🟡 中 | 缓存缩短TTL、限流保护 |
| **宗门BOSS** | 多玩家同时挑战 | 🟡 中 | 索引优化、缓存策略 |
| **邮件系统** | 未读邮件查询 | 🟡 中 | 索引优化、缓存策略 |
| **模块依赖** | 可能存在跨模块调用Mapper | 🟢 低 | 模块边界规范化 |

---

## 🎯 核心设计原则

### 不需要引入的组件（避免过度设计）

基于2H4G个人服务器的资源限制，**不需要**引入以下组件：

| 组件 | 理由 |
|------|------|
| ❌ Spring Cloud Gateway | 单体应用无需网关，Nginx可代理 |
| ❌ Spring Cloud Config | 单体应用配置集中管理，无需配置中心 |
| ❌ Nacos/Eureka | 单体应用无需服务注册与发现 |
| ❌ Kubernetes | 2H4G服务器无法支撑K8s集群 |
| ❌ TCC分布式事务 | 单体应用本地事务足够 |
| ❌ Saga分布式事务 | 单体应用无需复杂事务方案 |
| ❌ 多数据源读写分离 | 单库已满足需求，增加复杂度 |
| ❌ 服务网格（Istio） | 单体应用无需服务间调用 |

### 应该强化的架构点

| 架构点 | 当前状态 | 优化方向 |
|--------|----------|----------|
| ✅ 模块化架构 | 领域模块划分99% | 补全模块边界清晰度（1%） |
| ✅ 数据库索引 | 基础索引 | 添加高频查询索引 |
| ✅ 数据库连接池 | HikariCP默认配置 | 适配2H4G环境调优 |
| ✅ Redis缓存 | 双层缓存已实现 | TTL细化、连接池调优 |
| ✅ Sentinel限流/熔断 | 已引入但参数未调优 | 适配2H4G环境降低阈值 |
| ✅ JVM参数 | 默认配置 | 适配2H4G环境调优 |
| ✅ 监控指标 | 基础Actuator | 补充关键业务指标 |

---

## 🏗️ 模块化架构补全

### 当前包结构（已完成99%）

```
com.xiuxian.game/
├── common/                      # 公共层
│   ├── annotation/              # 自定义注解
│   ├── aspect/                  # AOP切面
│   ├── config/                  # 配置类
│   ├── dto/                     # 数据传输对象
│   ├── exception/              # 异常体系
│   ├── security/                # 安全层
│   └── util/                    # 工具类
│
├── modules/                     # 业务模块（按领域划分）
│   ├── player/                  # 玩家模块
│   ├── combat/                  # 战斗模块
│   ├── cultivation/             # 修炼模块
│   ├── pet/                     # 宠物模块
│   ├── skill/                   # 技能模块
│   ├── equipment/               # 装备模块
│   ├── quest/                   # 任务模块
│   ├── guild/                   # 宗门模块
│   ├── auction/                 # 拍卖模块
│   ├── ranking/                 # 排行榜模块
│   ├── mail/                    # 邮件模块
│   ├── narrative/               # 叙事模块
│   ├── offline/                 # 离线奖励模块
│   ├── announcement/            # 公告模块
│   ├── activity/                # 活动模块
│   ├── giftcode/                # 礼包码模块
│   ├── checkin/                 # 签到模块
│   └── admin/                   # 管理后台模块
│
└── XiuxianGameApplication.java
```

### 模块间依赖规范（关键补全）

#### 原则

1. **单向依赖**：模块A→模块B通过Service接口调用，禁止跨模块直接调用Mapper
2. **接口隔离**：每个模块暴露清晰的Service接口，内部实现细节隐藏
3. **循环依赖**：绝对禁止A→B→A的依赖链

#### 正确示例（✅）

```java
// CombatService通过Service接口依赖player和pet模块
@Service
public class CombatService {
    
    @Autowired
    private PlayerService playerService;  // ✅ 依赖player模块的Service接口
    
    @Autowired
    private PetService petService;       // ✅ 依赖pet模块的Service接口
    
    @Autowired
    private CombatMapper combatMapper;   // ✅ 只操作本模块的Mapper
    
    public CombatResult fight(Long playerId, Long monsterId) {
        PlayerVO player = playerService.getPlayerById(playerId);  // 通过Service接口
        PetVO pet = petService.getPetByPlayerId(playerId);         // 通过Service接口
        // 战斗计算逻辑...
        return combatResult;
    }
}
```

#### 错误示例（❌）

```java
// CombatService直接跨模块调用Mapper，破坏了模块封装
@Service
public class CombatService {
    
    @Autowired
    private PlayerMapper playerMapper;  // ❌ 禁止！跨模块调用Mapper
    
    @Autowired
    private PetMapper petMapper;        // ❌ 禁止！跨模块调用Mapper
    
    public void someMethod() {
        playerMapper.selectById(1L);     // ❌ 破坏了player模块的封装
        petMapper.selectById(1L);       // ❌ 破坏了pet模块的封装
    }
}
```

#### 模块接口定义规范

```java
// PlayerService接口定义（暴露给外部模块使用）
public interface PlayerService {
    
    /**
     * 获取玩家信息
     */
    PlayerVO getPlayerById(Long playerId);
    
    /**
     * 获取玩家简要信息（供其他模块使用）
     */
    PlayerProfileVO getPlayerProfile(Long playerId);
    
    /**
     * 更新玩家信息
     */
    void updatePlayer(PlayerUpdateDTO dto);
    
    /**
     * 扣除灵石
     */
    void deductSpiritStones(Long playerId, int amount);
}

// PlayerService实现（内部细节）
@Service
public class PlayerServiceImpl implements PlayerService {
    
    @Autowired
    private PlayerMapper playerMapper;  // ✅ 只操作本模块的Mapper
    
    @Autowired
    private BackpackMapper backpackMapper;
    
    @Override
    @Transactional
    public void deductSpiritStones(Long playerId, int amount) {
        // 实现细节...
    }
}
```

---

## 💾 数据库优化

### 关键索引设计

基于高频业务场景，建议添加以下索引：

#### 玩家表优化

```sql
-- 玩家基础信息查询（高频）
CREATE INDEX idx_players_user_id ON players(user_id, deleted_at);

-- 玩家等级排行榜查询
CREATE INDEX idx_players_realm_level ON players(realm_id, level DESC, deleted_at);

-- 玩家名查询
CREATE INDEX idx_players_name ON players(player_name, deleted_at);
```

#### 战斗表优化

```sql
-- 玩家战斗记录查询（高频）
CREATE INDEX idx_combat_player_time ON combat_logs(player_id, create_time DESC);

-- 战斗结果统计查询
CREATE INDEX idx_combat_result_time ON combat_logs(result, create_time DESC);
```

#### 排行榜优化

```sql
-- 战力排行榜（全表扫描优化）
CREATE INDEX idx_ranking_power ON player_stats(power DESC);

-- 等级排行榜（全表扫描优化）
CREATE INDEX idx_ranking_level ON player_stats(level DESC);
```

#### 拍卖行优化

```sql
-- 拍卖物品列表查询（高频）
CREATE INDEX idx_auction_status_time ON auctions(status, end_time);

-- 拍卖物品查询
CREATE INDEX idx_auction_item ON auctions(item_id, price ASC);
```

#### 宗门BOSS优化

```sql
-- 宗门BOSS挑战记录查询（多玩家同时挑战）
CREATE INDEX idx_guild_boss_time ON guild_boss_records(guild_id, create_time);
```

#### 邮件表优化

```sql
-- 未读邮件查询（高频）
CREATE INDEX idx_mail_unread ON mails(receiver_id, read_status, create_time DESC);
```

### 数据库连接池优化（适配2H4G）

```properties
# application.properties - 针对低资源环境优化

# HikariCP连接池（降低连接数，节省内存）
spring.datasource.hikari.maximum-pool-size=10          # 降低至10（原50）
spring.datasource.hikari.minimum-idle=3                # 降低至3（原5）
spring.datasource.hikari.max-lifetime=1200000          # 20分钟（原30分钟）
spring.datasource.hikari.idle-timeout=300000            # 5分钟
spring.datasource.hikari.connection-timeout=30000      # 30秒（原3秒）
spring.datasource.hikari.pool-name=HikariCP-Xiuxian

# MyBatis-Plus优化
mybatis-plus.configuration.default-executor-type=REUSE # 复用Statement
mybatis-plus.configuration.statement-timeout=10          # SQL超时10秒
mybatis-plus.configuration.cache-enabled=false          # 关闭二级缓存（已有Redis缓存）
```

---

## 🚀 Redis缓存优化

### 缓存空间TTL优化（基于实际业务）

| 缓存空间 | 当前TTL | 建议TTL | 变化 | 理由 |
|----------|---------|---------|------|------|
| `playerCache` | 30分钟 | 30分钟 | 不变 | 玩家数据变更不频繁，合理 |
| `tokenCache` | 24小时 | 24小时 | 不变 | Token过期时间，合理 |
| `rankingCache` | 5分钟 | 3分钟 | ⬇️ 缩短 | 排行榜变化快，缩短TTL保证实时性 |
| `auctionCache` | 1分钟 | 30秒 | ⬇️ 缩短 | 拍卖行高频变动，缩短TTL保证实时性 |
| `combatCache` | 10秒 | 10秒 | 不变 | 战斗缓存极短，合理 |
| `configCache` | 1小时 | 2小时 | ⬆️ 延长 | 配置变更很少，可延长TTL减少Redis压力 |
| `narrativeCache` | 10分钟 | 1小时 | ⬆️ 延长 | NPC对话很少变化，可延长TTL |
| `mailCache` | 未设置 | 5分钟 | ➕ 新增 | 邮件列表查询，添加缓存 |
| `bossCache` | 未设置 | 1分钟 | ➕ 新增 | 宗门BOSS状态查询，添加缓存 |

### Redis连接池优化（适配2H4G）

```properties
# Redis连接池（降低连接数，节省内存）
spring.redis.lettuce.pool.max-active=15        # 降低至15（原50）
spring.redis.lettuce.pool.max-idle=8          # 降低至8（原20）
spring.redis.lettuce.pool.min-idle=3          # 保持3
spring.redis.lettuce.pool.max-wait=5000       # 5秒（原3秒）
spring.redis.lettuce.pool.time-between-eviction-runs=30000  # 30秒
```

### 缓存策略细化

```java
// 排行榜缓存（缩短TTL，保证实时性）
@Cacheable(value = "rankingCache", key = "'power:' + #type", unless = "#result == null")
public List<PlayerVO> getRanking(String type) {
    // 查询逻辑...
}

// 拍卖行缓存（缩短TTL，保证实时性）
@Cacheable(value = "auctionCache", key = "'list:' + #page + ':' + #size", unless = "#result == null")
public List<AuctionVO> getAuctionList(int page, int size) {
    // 查询逻辑...
}

// 邮件列表缓存（新增）
@Cacheable(value = "mailCache", key = "'unread:' + #playerId", unless = "#result == null")
public List<MailVO> getUnreadMails(Long playerId) {
    // 查询逻辑...
}

// 宗门BOSS状态缓存（新增）
@Cacheable(value = "bossCache", key = "'status:' + #bossId", unless = "#result == null")
public BossStatusVO getBossStatus(Long bossId) {
    // 查询逻辑...
}
```

---

## 🔒 Sentinel限流/熔断调优

### 限流规则优化（适配2H4G）

#### 原限流规则（不适合2H4G）

```java
// 战斗接口限流 - 100 QPS（对2H4G服务器过高）
FlowRule combatRule = new FlowRule("/api/combat/*");
combatRule.setCount(100);
rules.add(combatRule);

// 核心接口限流 - 200 QPS（对2H4G服务器过高）
FlowRule coreRule = new FlowRule("/api/player/*");
coreRule.setCount(200);
rules.add(coreRule);
```

#### 优化后限流规则（适配2H4G）

```java
@Configuration
public class SentinelConfig {
    
    @PostConstruct
    public void init() {
        List<FlowRule> rules = new ArrayList<>();
        
        // 战斗接口限流 - 降低至30 QPS（原100）
        FlowRule combatRule = new FlowRule("/api/combat/*");
        combatRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        combatRule.setCount(30);  // 降低至30，适配2H4G
        combatRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        combatRule.setMaxQueueingTimeMs(500);
        rules.add(combatRule);
        
        // 核心接口限流 - 降低至50 QPS（原200）
        FlowRule coreRule = new FlowRule("/api/player/*");
        coreRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        coreRule.setCount(50);  // 降低至50，适配2H4G
        coreRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        coreRule.setMaxQueueingTimeMs(500);
        rules.add(coreRule);
        
        // 排行榜接口限流 - 20 QPS
        FlowRule rankingRule = new FlowRule("/api/ranking/*");
        rankingRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rankingRule.setCount(20);
        rankingRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rankingRule.setMaxQueueingTimeMs(500);
        rules.add(rankingRule);
        
        // 拍卖行接口限流 - 15 QPS
        FlowRule auctionRule = new FlowRule("/api/auction/*");
        auctionRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        auctionRule.setCount(15);
        auctionRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        auctionRule.setMaxQueueingTimeMs(500);
        rules.add(auctionRule);
        
        // 宗门BOSS接口限流 - 10 QPS
        FlowRule bossRule = new FlowRule("/api/guild/boss/*");
        bossRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        bossRule.setCount(10);
        bossRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        bossRule.setMaxQueueingTimeMs(500);
        rules.add(bossRule);
        
        FlowRuleManager.loadRules(rules);
    }
}
```

### 降级规则优化

```java
@Configuration
public class DegradeConfig {
    
    @PostConstruct
    public void init() {
        List<DegradeRule> rules = new ArrayList<>();
        
        // 战斗接口熔断 - 异常比例50%熔断
        DegradeRule combatDegrade = new DegradeRule();
        combatDegrade.setResource("combat");
        combatDegrade.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        combatDegrade.setCount(0.5);  // 异常比例50%
        combatDegrade.setTimeWindow(60);  // 熔断时长60秒
        combatDegrade.setMinRequestAmount(10);  // 最小请求数10
        rules.add(combatDegrade);
        
        // 排行榜接口熔断 - 慢调用比例50%熔断
        DegradeRule rankingDegrade = new DegradeRule();
        rankingDegrade.setResource("ranking");
        rankingDegrade.setGrade(RuleConstant.DEGRADE_GRADE_SLOW_REQUEST_RATIO);
        rankingDegrade.setCount(0.5);
        rankingDegrade.setTimeWindow(60);
        rankingDegrade.setSlowRatioThreshold(0.5);
        rankingDegrade.setMinRequestAmount(5);
        rankingDegrade.setStatIntervalMs(10000);  // 统计时长10秒
        rules.add(rankingDegrade);
        
        // 拍卖行接口熔断 - 异常数熔断
        DegradeRule auctionDegrade = new DegradeRule();
        auctionDegrade.setResource("auction");
        auctionDegrade.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        auctionDegrade.setCount(5);  // 5次异常触发熔断
        auctionDegrade.setTimeWindow(60);
        auctionDegrade.setMinRequestAmount(5);
        rules.add(auctionDegrade);
        
        DegradeRuleManager.loadRules(rules);
    }
}
```

---

## 📊 JVM参数优化（适配2H4G）

### 生产环境JVM启动参数

```bash
# 针对低内存环境的JVM调优
java -Xms512m -Xmx1024m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/var/log/xiuxian/heap_dump.hprof \
     -XX:+PrintGCDetails \
     -XX:+PrintGCDateStamps \
     -Xloggc:/var/log/xiuxian/gc.log \
     -Dspring.profiles.active=prod \
     -jar xiuxian-game.jar
```

### 参数说明

| 参数 | 说明 | 理由 |
|------|------|------|
| `-Xms512m` | 初始堆内存512MB | 适配2H4G环境，启动时预留512MB |
| `-Xmx1024m` | 最大堆内存1GB | 适配2H4G环境，最大堆1GB（留1GB给系统） |
| `-XX:+UseG1GC` | 使用G1垃圾收集器 | 适合低内存环境，GC停顿时间可控 |
| `-XX:MaxGCPauseMillis=200` | 目标GC停顿200ms | 保证响应时间 |
| `-XX:+HeapDumpOnOutOfMemoryError` | OOM时生成堆转储 | 方便排查问题 |
| `-XX:HeapDumpPath` | 堆转储文件路径 | 指定堆转储目录 |
| `-XX:+PrintGCDetails` | 打印GC详细信息 | 监控GC行为 |
| `-XX:+PrintGCDateStamps` | 打印GC时间戳 | 分析GC时间 |
| `-Xloggc` | GC日志文件路径 | 指定GC日志目录 |
| `-Dspring.profiles.active=prod` | 使用生产配置 | 确保生产环境配置生效 |

### 开发环境JVM启动参数

```bash
# 开发环境（更宽松的GC策略）
java -Xms256m -Xmx512m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=300 \
     -Dspring.profiles.active=dev \
     -jar xiuxian-game.jar
```

---

## 📈 监控指标补充

### 关键业务指标采集

```java
@RestController
@RequestMapping("/actuator/game")
@RequiredArgsConstructor
public class GameMetricsController {
    
    private final MeterRegistry meterRegistry;
    private final RedisCacheService redisCacheService;
    
    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> getGameMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // 业务指标
        metrics.put("combat_qps", meterRegistry.counter("game.combat.total").count());
        metrics.put("avg_response_time", 
            meterRegistry.timer("game.api.response").mean());
        
        // 在线玩家数（从Redis获取）
        long onlinePlayers = redisCacheService.getOnlinePlayersCount();
        metrics.put("online_players", onlinePlayers);
        meterRegistry.gauge("game.players.online", onlinePlayers);
        
        // 缓存命中率
        metrics.put("redis_hit_rate", getRedisHitRate());
        
        // 数据库连接池状态
        metrics.put("db_active_connections", 
            meterRegistry.gauge("db.connections.active").gauge().value());
        
        // 活跃线程数
        metrics.put("active_threads", Thread.activeCount());
        
        return ApiResponse.success(metrics);
    }
    
    @GetMapping("/health")
    public ApiResponse<Void> healthCheck() {
        // 快速健康检查
        boolean dbOk = checkDatabase();
        boolean redisOk = checkRedis();
        
        if (dbOk && redisOk) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }
    
    private double getRedisHitRate() {
        // 获取Redis缓存命中率
        long cacheHits = meterRegistry.counter("redis.cache.hit").count();
        long cacheMisses = meterRegistry.counter("redis.cache.miss").count();
        long total = cacheHits + cacheMisses;
        return total > 0 ? (double) cacheHits / total : 0.0;
    }
    
    private boolean checkDatabase() {
        // 检查数据库连接
        try {
            // 执行简单查询验证连接
            return true;
        } catch (Exception e) {
            LogUtils.error(log, "数据库健康检查失败", e);
            return false;
        }
    }
    
    private boolean checkRedis() {
        // 检查Redis连接
        try {
            redisCacheService.ping();
            return true;
        } catch (Exception e) {
            LogUtils.error(log, "Redis健康检查失败", e);
            return false;
        }
    }
}
```

---

## 📋 实施路线图

### 阶段一：架构补全（1周）

**任务清单：**
1. ✅ 模块依赖检查
   - 检查是否存在跨模块直接调用Mapper的情况
   - 消除循环依赖
   - 建立清晰的模块间接口

2. ✅ 数据库索引优化
   - 添加玩家表索引
   - 添加战斗表索引
   - 添加排行榜索引
   - 添加拍卖行索引
   - 添加宗门BOSS索引
   - 添加邮件表索引

3. ✅ Redis缓存TTL调整
   - rankingCache: 5分钟 → 3分钟
   - auctionCache: 1分钟 → 30秒
   - configCache: 1小时 → 2小时
   - narrativeCache: 10分钟 → 1小时
   - 新增mailCache: 5分钟
   - 新增bossCache: 1分钟

4. ✅ Sentinel参数调优
   - 战斗接口限流: 100 QPS → 30 QPS
   - 核心接口限流: 200 QPS → 50 QPS
   - 新增排行榜限流: 20 QPS
   - 新增拍卖行限流: 15 QPS
   - 新增宗门BOSS限流: 10 QPS

5. ✅ JVM参数优化
   - 生产环境: -Xms512m -Xmx1024m -XX:+UseG1GC
   - 开发环境: -Xms256m -Xmx512m -XX:+UseG1GC

6. ✅ 监控指标补充
   - 添加GameMetricsController
   - 采集业务指标
   - 添加健康检查

### 阶段二：性能优化（1周）

**任务清单：**
1. 数据库连接池优化
   - maximum-pool-size: 50 → 10
   - minimum-idle: 5 → 3
   - max-lifetime: 30分钟 → 20分钟
   - connection-timeout: 3秒 → 30秒

2. Redis连接池优化
   - max-active: 50 → 15
   - max-idle: 20 → 8
   - min-idle: 保持3
   - max-wait: 3秒 → 5秒

3. 慢查询优化
   - 开启慢查询日志
   - 分析慢查询
   - 优化SQL

4. 缓存预热策略
   - 启动时预加载热点数据
   - 定时刷新缓存

### 阶段三：稳定性加固（1周）

**任务清单：**
1. 熔断降级规则完善
   - 战斗接口熔断
   - 排行榜接口熔断
   - 拍卖行接口熔断

2. 异常处理优化
   - 统一异常处理
   - 错误码规范
   - 日志规范化

3. 日志规范化
   - 使用LogUtils
   - 结构化日志
   - 链路追踪

4. 监控告警配置
   - 关键指标监控
   - 异常告警
   - 性能告警

---

## ✅ 总结

### 核心设计理念

**2H4G个人服务器 + 个人自用 = 单体应用 + 领域模块化 + 轻量级基础设施**

### 关键优化点

1. **模块化架构补全（1%）**
   - 模块间通过Service接口通信
   - 禁止跨模块直接调用Mapper
   - 消除循环依赖

2. **数据库优化（适配2H4G）**
   - 关键索引添加
   - 连接池参数调优

3. **Redis缓存细化**
   - TTL优化（基于实际业务）
   - 连接池参数调优

4. **Sentinel限流/熔断调优**
   - 降低限流阈值（适配2H4G）
   - 完善降级策略

5. **JVM参数优化**
   - 堆内存512MB-1GB
   - 使用G1垃圾收集器

6. **监控指标补充**
   - 关键业务指标采集
   - 健康检查

### 避免过度设计

❌ **不引入**（2H4G环境不适用）：
- Spring Cloud全家套
- Nacos/Eureka服务注册与发现
- Kubernetes集群
- 复杂分布式事务
- 多数据源读写分离

✅ **应该强化**（适配2H4G）：
- 领域模块边界清晰化
- 数据库索引优化
- Redis缓存策略细化
- Sentinel参数调优
- JVM参数优化
- 轻量级监控指标

### 预期收益

| 维度 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| **数据库连接数** | 50个连接 | 10个连接 | 节省80% |
| **Redis连接数** | 50个连接 | 15个连接 | 节省70% |
| **JVM堆内存** | 默认配置 | 512MB-1GB | 精准控制 |
| **接口限流** | 100-200 QPS | 10-50 QPS | 适配2H4G |
| **缓存TTL** | 固定配置 | 基于业务优化 | 精准控制 |
| **监控指标** | 基础Actuator | 业务指标补充 | 可观测性提升 |

---

*文档版本: 1.0 | 作者: shaun.sheng | 最后更新: 2026-03-24*
