# 修仙挂机游戏 - 优化架构方案（2H4G个人服务器版）

> 本文档针对2H4G个人服务器环境，设计精简高效的架构方案。
> 核心原则：**单体应用 + 领域模块化 + 轻量级基础设施**

**作者**: shaun.sheng
**日期**: 2026-03-24
**服务器**: 2核4G，已部署Halo博客

---

## 📋 需求分析

### 现状评估

| 维度 | 当前状态 | 评估 |
|------|----------|------|
| 架构模式 | Spring Boot 单体应用 | ✅ 适合个人项目 |
| 包结构 | 已按业务模块划分（99%完成） | ✅ 领域驱动基础良好 |
| 技术栈 | Java 1.8 + Spring Boot 2.7.18 + MyBatis-Plus | ⚠️ Java 1.8偏老 |
| 数据库 | MySQL 8.0 | ✅ 良好 |
| 缓存 | Redis + 本地降级（已实现） | ✅ 已有完善方案 |
| 监控 | Spring Boot Actuator + Prometheus | ✅ 已接入 |
| 限流/熔断 | Sentinel（已引入） | ✅ 已引入 |

### 服务器资源分配建议

```
总资源：2核4G
├── Halo博客：        0.5核1G
├── MySQL：           0.5核1G
├── Redis：           0.3核0.5G
├── Java应用（JVM）：  0.7核1.5G
└── 系统/其他：       剩余资源
```

**JVM参数建议**：
```bash
java -Xms512m -Xmx1024m -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+PrintGCDetails \
     -XX:+PrintGCDateStamps \
     -jar xiuxian-game.jar
```

---

## 🎯 架构设计原则

### 1. 单体优先，拒绝过度设计

❌ **不引入的组件**（资源占用大，个人项目不必要）：
- Spring Cloud全家桶（Gateway/Config/Discovery）
- Nacos注册中心（单节点无意义）
- Kubernetes（过度复杂）
- 多数据源读写分离（单库已足够）

✅ **保留的核心组件**：
- Spring Boot单体应用
- MyBatis-Plus ORM
- Spring Security + JWT认证
- Redis双层缓存（主+本地降级）
- Sentinel限流/熔断
- Actuator监控

### 2. 模块化包结构（已完成99%）

```
com.xiuxian.game/
├── XiuxianGameApplication.java   # 启动类

├── common/                      # 公共层
│   ├── annotation/              # 自定义注解（@RateLimit, @DataSource）
│   ├── aspect/                  # AOP切面
│   ├── config/                  # 配置类
│   ├── dto/                     # 数据传输对象
│   ├── exception/               # 异常体系（BusinessException + ErrorCode）
│   ├── security/                # 安全过滤器（JWT）
│   └── util/                    # 工具类

├── modules/                     # 业务模块（按领域划分）
│   ├── player/                  # 玩家模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── entity/
│   ├── combat/                   # 战斗模块
│   ├── cultivation/             # 修炼模块
│   ├── pet/                      # 宠物模块
│   ├── equipment/                # 装备模块
│   ├── skill/                    # 技能模块
│   ├── quest/                    # 任务模块
│   ├── guild/                    # 宗门模块
│   ├── auction/                  # 拍卖模块
│   ├── ranking/                  # 排行榜模块
│   ├── mail/                     # 邮件模块
│   ├── narrative/                # 叙事模块
│   ├── offline/                  # 离线奖励模块
│   ├── activity/                 # 活动模块
│   ├── giftcode/                 # 礼包码模块
│   ├── checkin/                  # 签到模块
│   ├── admin/                    # 管理后台模块
│   └── ...                       # 其他模块

└── validation/                  # 数据校验器
```

**模块化原则**：
1. 模块内部高内聚：controller/service/mapper/entity在同一个模块包内
2. 模块间低耦合：通过Service接口通信，禁止跨模块直接调用Mapper
3. 依赖单向：上层（controller）→ 中层（service）→ 下层（mapper/entity）

---

## 🏗️ 核心架构优化建议

### 1. 模块依赖关系优化

**问题诊断**：
当前可能存在模块间循环依赖或直接跨模块调用Mapper的情况。

**优化方案**：

```java
// ❌ 错误：跨模块直接调用Mapper（破坏封装）
@Service
public class CombatService {
    @Autowired
    private PlayerMapper playerMapper;  // 不应该直接依赖player模块的Mapper
}

// ✅ 正确：通过Service接口通信
@Service
public class CombatService {
    @Autowired
    private PlayerService playerService;  // 依赖player模块的Service接口
}
```

**检查清单**：
- [ ] 模块A的Controller/Service是否直接注入了模块B的Mapper？
- [ ] 是否存在A→B→A的循环依赖？
- [ ] 每个模块是否有清晰的对外接口（Service）？

### 2. 服务层接口化

**当前问题**：Service层可能缺少接口定义，导致耦合度高。

**优化方案**：

```java
// player模块
public interface PlayerService {
    PlayerVO getPlayerById(Long playerId);
    void updatePlayer(PlayerUpdateDTO dto);
}

@Service
public class PlayerServiceImpl implements PlayerService {
    // 实现细节...
}

// combat模块依赖PlayerService
@Service
public class CombatService {
    @Autowired
    private PlayerService playerService;  // 依赖接口，而非实现
}
```

**收益**：
- 降低模块间耦合
- 便于未来模块独立部署（如果需要）
- 便于Mock测试

### 3. 数据库索引优化（2H4G关键）

**瓶颈识别**：
- 战斗系统：高频写入战斗日志
- 排行榜：全表扫描
- 拍卖行：并发查询/更新

**索引优化示例**：

```sql
-- 玩家表
CREATE INDEX idx_user_deleted ON users(deleted_at);  -- 软删除过滤

-- 战斗记录表
CREATE INDEX idx_combat_player_time ON combat_logs(player_id, create_time);
CREATE INDEX idx_combat_time ON combat_logs(create_time DESC);  -- 按时间查询

-- 排行榜相关
CREATE INDEX idx_player_power ON players(power DESC);  -- 战力排行
CREATE INDEX idx_player_level ON players(level DESC);   -- 等级排行

-- 拍卖行
CREATE INDEX idx_auction_status_time ON auctions(status, end_time);
CREATE INDEX idx_auction_item ON auctions(item_id);
```

### 4. Redis缓存策略（已实现，验证是否合理）

**当前架构**（已实现）：
- 主缓存：Redis（Lettuce连接池）
- 降级缓存：本地ConcurrentHashMap
- 自动降级：Redis不可用时自动切换

**验证要点**：

```java
// 检查缓存命中率是否合理
// 1. 热点数据是否都加上了缓存
@Cacheable(value = "playerCache", key = "#playerId", unless = "#result == null")
public PlayerVO getPlayerById(Long playerId) { ... }

// 2. 更新操作是否驱逐缓存
@CacheEvict(value = "playerCache", key = "#dto.playerId")
public void updatePlayer(PlayerUpdateDTO dto) { ... }

// 3. 缓存TTL是否合理
// playerCache: 30分钟（合理）
// rankingCache: 5分钟（合理）
// auctionCache: 1分钟（合理）
// combatCache: 10秒（合理）
```

**JVM内存优化**：
```
Redis连接池配置（application.yml）：
spring:
  redis:
    lettuce:
      pool:
        max-active: 20   # 2核4G环境，不需要50
        max-idle: 10    # 降低到10
        min-idle: 3
```

### 5. 限流/熔断策略（已引入，验证配置）

**Sentinel配置检查**：

```java
// 战斗接口限流（核心入口）
FlowRule combatRule = new FlowRule();
combatRule.setResource("combat");
combatRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
combatRule.setCount(50);  // 2核服务器，建议50 QPS，而非100
combatRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

// 熔断器配置
DegradeRule degradeRule = new DegradeRule();
degradeRule.setResource("combat");
degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
degradeRule.setCount(10);  // 10次异常后熔断
degradeRule.setTimeWindow(10);  // 10秒时间窗口
```

**降级策略**：
```java
@SentinelResource(
    value = "combat",
    fallback = "combatFallback",  // 业务降级
    blockHandler = "combatBlockHandler"  // 流控降级
)
public CombatResult fight(Long playerId, Long monsterId) {
    // 正常逻辑
}

// 降级：简化战斗计算，减少DB查询
public CombatResult combatFallback(Long playerId, Long monsterId) {
    return quickFight(playerId, monsterId);
}
```

---

## 📊 监控与告警（轻量级）

### 1. Actuator端点配置

```properties
# application.properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

### 2. 关键指标

```java
@RestController
@RequestMapping("/actuator/game")
public class GameMetricsController {

    @Autowired
    private MeterRegistry meterRegistry;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> metrics = new HashMap<>();

        // 业务指标
        metrics.put("online_players", meterRegistry.gauge("game.players.online", 0));
        metrics.put("combat_qps", meterRegistry.counter("game.combat.total").count());

        // JVM指标
        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMxBean.getHeapMemoryUsage();
        metrics.put("jvm_heap_used_mb", heapUsage.getUsed() / 1024 / 1024);
        metrics.put("jvm_heap_max_mb", heapUsage.getMax() / 1024 / 1024);

        // 缓存指标
        metrics.put("redis_available", RedisCacheService.isRedisAvailable());

        return metrics;
    }
}
```

### 3. 简单告警（可选）

```bash
#!/bin/bash
# 简单健康检查脚本
HEALTH=$(curl -s http://localhost:8082/actuator/health | jq -r '.status')

if [ "$HEALTH" != "UP" ]; then
    echo "服务异常，发送通知..."
    # 这里可以集成邮件/钉钉/企业微信
fi
```

---

## 🔄 定时任务优化

**当前问题**：定时任务可能使用`@Scheduled`，多实例部署时会重复执行。

**优化方案**：使用Redis分布式锁

```java
@Component
public class ScheduledTasks {

    @Autowired
    private CacheUtils cacheUtils;

    // 每天凌晨刷新排行榜
    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshRanking() {
        String lockKey = "lock:ranking:refresh";
        String lockValue = UUID.randomUUID().toString();

        // 尝试获取锁（10秒超时，防止死锁）
        Boolean locked = cacheUtils.tryLock(lockKey, lockValue, 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(locked)) {
            try {
                // 执行刷新逻辑
                rankingService.refresh();
                log.info("排行榜刷新完成");
            } finally {
                cacheUtils.unlock(lockKey, lockValue);
            }
        } else {
            log.info("其他实例正在刷新排行榜，跳过");
        }
    }
}
```

---

## 🚀 部署优化

### 1. 资源隔离配置

```bash
# docker-compose.yml
version: '3.8'

services:
  xiuxian-game:
    image: xiuxian-game:latest
    deploy:
      resources:
        limits:
          cpus: '0.7'
          memory: 1.5G
        reservations:
          cpus: '0.5'
          memory: 1G
    environment:
      - JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC

  mysql:
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1G

  redis:
    deploy:
      resources:
        limits:
          cpus: '0.3'
          memory: 512M
```

### 2. JVM启动参数优化

```bash
#!/bin/bash
# start.sh
java -Xms512m -Xmx1024m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/var/log/xiuxian/heap_dump.hprof \
     -Dspring.profiles.active=prod \
     -jar xiuxian-game.jar
```

---

## 📋 实施路线图（按优先级）

### 🔴 优先级P0（立即执行）

1. **模块依赖检查**
   - 检查是否跨模块直接调用Mapper
   - 检查循环依赖
   - 建立清晰的Service接口

2. **数据库索引优化**
   - 为高频查询添加索引
   - 分析慢查询日志

3. **JVM参数优化**
   - 调整堆内存大小（512m-1024m）
   - 使用G1垃圾收集器

### 🟡 优先级P1（本周完成）

4. **限流/熔断参数调优**
   - 降低Sentinel限流阈值（100→50）
   - 完善降级策略

5. **Redis连接池优化**
   - 降低连接池大小（max-active: 50→20）

6. **定时任务分布式锁**
   - 防止多实例重复执行

### 🟢 优先级P2（下个月）

7. **监控面板搭建**
   - Grafana + Prometheus可视化

8. **日志采集**
   - ELK/EFK轻量化方案

---

## ✅ 总结

### 不需要做的事（避免过度设计）

❌ Spring Cloud全家套（Gateway/Config/Discovery）
❌ Nacos注册中心
❌ Kubernetes
❌ 多数据源读写分离
❌ 分布式事务（TCC/Saga）
❌ 服务网格

### 应该做的事（精简高效）

✅ 模块化包结构（已完成99%，补齐1%）
✅ 服务层接口化
✅ 数据库索引优化
✅ Redis缓存合理使用
✅ Sentinel限流/熔断
✅ JVM参数优化
✅ 轻量级监控

### 核心思想

> 2H4G个人服务器 + 个人自用项目 = 单体应用 + 领域模块化 + 轻量级基础设施

**架构演进路径**：

```
当前单体 → 模块化优化（P0/P1） → 性能调优（P1/P2）
```

不追求"云原生"、"微服务"等高大上概念，专注于**稳定性、可维护性、资源效率**。

---

**最后建议**：

1. 先跑起来，再优化
2. 监控数据说话，不凭感觉优化
3. 保持简单，拒绝过度设计
4. 文档同步更新

---

*文档版本: 1.0 | 作者: shaun.sheng | 最后更新: 2026-03-24*
