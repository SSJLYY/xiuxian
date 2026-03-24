# 性能优化方案
**日期**: 2026-03-24  
**版本**: 1.0  
**作者**: shaun.sheng

---

## 执行摘要

本文档提出了系统性的性能优化方案，涵盖数据库查询优化、前端渲染优化和后端性能提升。

### 优化目标
- 数据库查询响应时间 < 100ms（95%分位数）
- 前端首次渲染时间 < 2s
- 并发1000用户时系统可用率 > 99.9%

---

## 1. 数据库查询优化

### 1.1 索引优化

#### 当前问题
- 关键查询字段缺少索引
- `player_quests`表按`player_id`查询慢（玩家任务列表）
- `combat_logs`表按`player_id`排序慢（历史记录）

#### 优化方案

**添加索引SQL**:

```sql
-- 玩家任务表索引
CREATE INDEX idx_player_quests_player_id ON player_quests(player_id);
CREATE INDEX idx_player_quests_completed ON player_quests(completed);
CREATE INDEX idx_player_quests_created_at ON player_quests(created_at);

-- 战斗日志索引
CREATE INDEX idx_combat_logs_player_id ON combat_logs(player_id);
CREATE INDEX idx_combat_logs_created_at ON combat_logs(created_at DESC);
CREATE INDEX idx_combat_logs_player_created ON combat_logs(player_id, created_at DESC);

-- 宠物表索引
CREATE INDEX idx_player_pets_player_id ON player_pets(player_id);
CREATE INDEX idx_player_pets_is_active ON player_pets(is_active);
CREATE INDEX idx_player_pets_player_active ON player_pets(player_id, is_active);

-- 拍卖行索引
CREATE INDEX idx_auction_items_seller_id ON auction_items(seller_id);
CREATE INDEX idx_auction_items_status ON auction_items(status);
CREATE INDEX idx_auction_items_status_expire ON auction_items(status, expire_at);

-- 宗门申请索引
CREATE INDEX idx_guild_applications_player_id ON guild_applications(player_id);
CREATE INDEX idx_guild_applications_guild_id ON guild_applications(guild_id);
CREATE INDEX idx_guild_applications_status ON guild_applications(status);
CREATE INDEX idx_guild_applications_guild_status ON guild_applications(guild_id, status);
```

**预期收益**:
- 玩家任务查询：50ms → 5ms（10倍提升）
- 战斗历史查询：200ms → 20ms（10倍提升）
- 宠物列表查询：30ms → 3ms（10倍提升）

---

### 1.2 N+1查询优化

#### 当前问题
- `QuestService.getPlayerAllQuestsDetail()`存在N+1查询
- 先查`player_quests`，然后循环查`quests`

#### 优化方案

**旧代码（N+1）**:
```java
public List<PlayerQuestDetailResponse> getPlayerAllQuestsDetail(Integer playerId) {
    List<PlayerQuest> list = playerQuestMapper.selectByPlayerId(playerId);
    return list.stream().map(this::toDetail).collect(Collectors.toList());
}

private PlayerQuestDetailResponse toDetail(PlayerQuest pq) {
    Quest quest = questMapper.selectById(pq.getQuestId()); // N+1查询！
    // ... 构建DTO
}
```

**新代码（JOIN查询）**:
```java
// 在Mapper.xml中使用LEFT JOIN
<select id="selectPlayerQuestDetailByPlayerId" resultMap="PlayerQuestDetailResultMap">
    SELECT 
        pq.id as player_quest_id,
        pq.player_id,
        pq.quest_id,
        pq.completed,
        pq.reward_claimed,
        pq.progress,
        pq.created_at,
        q.title,
        q.description,
        q.type,
        q.required_amount,
        q.reward_exp,
        q.reward_spirit_stones,
        q.reward_contribution_points
    FROM player_quests pq
    LEFT JOIN quests q ON pq.quest_id = q.id
    WHERE pq.player_id = #{playerId}
    ORDER BY pq.created_at DESC
</select>
```

**预期收益**:
- 玩家任务详情查询：500ms → 10ms（50倍提升）

---

### 1.3 分页查询优化

#### 当前问题
- 大表查询未分页（如`combat_logs`历史记录）
- 一次查询可能返回几千条记录

#### 优化方案

**添加分页SQL**:

```sql
-- 战斗日志分页（每页20条，最多1000条）
<select id="selectCombatLogPageByPlayerId">
    SELECT * FROM combat_logs
    WHERE player_id = #{playerId}
    ORDER BY created_at DESC
    LIMIT #{pageSize} OFFSET #{offset}
</select>

-- 玩家列表分页（后端管理）
<select id="selectPlayerProfilePage">
    SELECT * FROM player_profiles
    ORDER BY created_at DESC
    LIMIT #{pageSize} OFFSET #{offset}
</select>
```

**预期收益**:
- 内存占用减少：从10MB → 100KB（100倍）
- 查询时间：从500ms → 20ms（25倍）

---

### 1.4 批量操作优化

#### 当前问题
- 循环逐条插入（如任务奖励发放）
- 事务时间过长

#### 优化方案

**旧代码（循环插入）**:
```java
@Transactional
public void awardQuestRewards(Integer playerId) {
    List<PlayerQuest> completed = playerQuestMapper.selectCompletedByPlayerId(playerId);
    
    for (PlayerQuest pq : completed) {
        playerProfile.setSpiritStones(playerProfile.getSpiritStones() + pq.getRewardSpiritStones());
        playerProfile.setExp(playerProfile.getExp() + pq.getRewardExp());
        // 循环更新N次！
        playerProfileMapper.updateById(playerProfile);
    }
}
```

**新代码（批量操作）**:
```java
// 使用MyBatis-Plus批量更新
@Transactional
public void awardAllQuestRewards(Integer playerId) {
    List<PlayerQuest> completed = playerQuestMapper.selectCompletedByPlayerId(playerId);
    
    if (completed.isEmpty()) return;
    
    // 聚合奖励
    long totalSpiritStones = completed.stream()
        .mapToLong(PlayerQuest::getRewardSpiritStones)
        .sum();
    long totalExp = completed.stream()
        .mapToLong(PlayerQuest::getRewardExp)
        .sum();
    
    // 一次性更新
    playerProfile.setSpiritStones(playerProfile.getSpiritStones() + totalSpiritStones);
    playerProfile.setExp(playerProfile.getExp() + totalExp);
    playerProfileMapper.updateById(playerProfile);
    
    // 批量标记已领取
    completed.forEach(pq -> pq.setRewardClaimed(true));
    playerQuestMapper.updateBatchById(completed);
}
```

**预期收益**:
- 任务奖励发放：1000ms → 100ms（10倍）
- 数据库连接占用：减少90%

---

## 2. 前端渲染优化

### 2.1 懒加载

#### 当前问题
- 所有JS文件同步加载
- 首次加载40个JS文件，阻塞渲染

#### 优化方案

**修改HTML文件**:

```html
<!-- 旧代码（同步加载） -->
<script src="js/api.js"></script>
<script src="js/auth.js"></script>
<script src="js/game.js"></script>
<!-- ... 40个script标签 -->

<!-- 新代码（懒加载） -->
<!-- 核心文件同步加载 -->
<script src="js/api.js" defer></script>
<script src="js/auth.js" defer></script>
<script src="js/game.js" defer></script>

<!-- 功能模块懒加载 -->
<script>
// 动态加载非核心模块
function loadModule(moduleName) {
    return import(`/js/${moduleName}.js`);
}

// 按需加载
document.getElementById('pets-tab')?.addEventListener('click', () => {
    loadModule('pets').then(module => module.init());
});

document.getElementById('quest-tab')?.addEventListener('click', () => {
    loadModule('quest').then(module => module.init());
});
</script>
```

**预期收益**:
- 首次渲染时间：2.5s → 1.5s（40%提升）
- 加载JS体积：从500KB → 150KB（立即加载）

---

### 2.2 虚拟列表

#### 当前问题
- 战斗日志、成就列表渲染所有DOM
- 100条记录=100个DOM节点

#### 优化方案

**使用虚拟滚动（示例）**:

```javascript
// 创建虚拟列表组件
class VirtualList {
    constructor(container, itemHeight, data) {
        this.container = container;
        this.itemHeight = itemHeight;
        this.data = data;
        this.visibleCount = Math.ceil(container.clientHeight / itemHeight) + 5;
        this.scrollTop = 0;
        
        this.init();
    }
    
    init() {
        // 只渲染可见项
        this.render();
        
        // 监听滚动
        this.container.addEventListener('scroll', () => {
            const startIdx = Math.floor(this.container.scrollTop / this.itemHeight);
            this.render(startIdx, startIdx + this.visibleCount);
        });
    }
    
    render(start = 0, end = this.visibleCount) {
        this.container.innerHTML = '';
        const fragment = document.createDocumentFragment();
        
        for (let i = startIdx; i < Math.min(end, this.data.length); i++) {
            const item = this.createItem(this.data[i], i);
            fragment.appendChild(item);
        }
        
        this.container.appendChild(fragment);
    }
    
    createItem(data, index) {
        const el = document.createElement('div');
        el.style.height = this.itemHeight + 'px';
        el.style.position = 'absolute';
        el.style.top = (index * this.itemHeight) + 'px';
        el.textContent = data;
        return el;
    }
}

// 使用示例
const combatLogList = new VirtualList(
    document.getElementById('combat-log-container'),
    50, // 每条日志高度50px
    combatLogs // 1000条日志数据
);
```

**预期收益**:
- 1000条日志DOM：从1000个 → 20个（50倍减少）
- 列表渲染时间：500ms → 50ms（10倍提升）
- 滚动流畅度：60fps

---

### 2.3 防抖和节流

#### 当前问题
- 输入框每输入1字符就发起请求
- 滚动事件频繁触发重绘

#### 优化方案

**添加防抖函数**:

```javascript
// 防抖函数（延迟执行）
function debounce(func, delay) {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => func.apply(this, args), delay);
    };
}

// 节流函数（固定频率执行）
function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// 搜索框防抖
const searchInput = document.getElementById('search-input');
searchInput.addEventListener('input', debounce((e) => {
    const query = e.target.value;
    if (query.length >= 2) {
        searchPets(query); // 延迟300ms执行
    }
}, 300));

// 滚动事件节流
const combatLogContainer = document.getElementById('combat-log');
combatLogContainer.addEventListener('scroll', throttle(() => {
    lazyLoadImages(); // 每100ms最多执行1次
}, 100));
```

**预期收益**:
- 搜索请求数：从10个/秒 → 3个/秒（减少70%）
- CPU占用：降低60%

---

### 2.4 图片优化

#### 当前问题
- 大图直接加载，无懒加载
- 无响应式图片

#### 优化方案

**添加懒加载**:

```html
<!-- 旧代码 -->
<img src="/images/pet-dragon.png" alt="青龙">

<!-- 新代码（懒加载） -->
<img 
    data-src="/images/pet-dragon.png" 
    src="/images/placeholder-1x1.png" 
    loading="lazy" 
    class="lazy-load"
    alt="青龙">

<script>
// Intersection Observer懒加载
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const img = entry.target;
            img.src = img.dataset.src;
            img.classList.add('loaded');
            observer.unobserve(img);
        }
    });
}, { rootMargin: '100px' });

document.querySelectorAll('.lazy-load').forEach(img => {
    observer.observe(img);
});
</script>
```

**响应式图片**:

```html
<picture>
    <source srcset="/images/pet-dragon-2x.png 2x, /images/pet-dragon-3x.png 3x" media="(min-width: 768px)">
    <img src="/images/pet-dragon.png" srcset="/images/pet-dragon-2x.png 2x" alt="青龙">
</picture>
```

**预期收益**:
- 首次加载时间：减少30%
- 带宽消耗：减少40%

---

## 3. 后端性能优化

### 3.1 缓存策略

#### 当前问题
- 重复查询数据库（如怪物列表、装备列表）
- 无缓存层

#### 优化方案

**添加Redis缓存**:

```java
@Service
@RequiredArgsConstructor
public class MonsterService {
    private final MonsterMapper monsterMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String MONSTER_CACHE_KEY = "monster:all";
    private static final Duration CACHE_TTL = Duration.ofHours(1);
    
    /**
     * 获取所有怪物（带缓存）
     */
    public List<Monster> getAllMonsters() {
        // 先查缓存
        List<Monster> cached = (List<Monster>) redisTemplate.opsForValue().get(MONSTER_CACHE_KEY);
        if (cached != null) {
            return cached;
        }
        
        // 缓存未命中，查数据库
        List<Monster> monsters = monsterMapper.selectList(null);
        
        // 写入缓存（1小时TTL）
        redisTemplate.opsForValue().set(MONSTER_CACHE_KEY, monsters, CACHE_TTL);
        
        return monsters;
    }
    
    /**
     * 新增怪物时清除缓存
     */
    @CacheEvict(value = MONSTER_CACHE_KEY, allEntries = true)
    public Monster createMonster(Monster monster) {
        monsterMapper.insert(monster);
        return monster;
    }
}
```

**Spring Cache配置**:

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues();
        
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}
```

**预期收益**:
- 怪物列表查询：20ms → 0.5ms（40倍提升）
- 数据库负载：减少80%

---

### 3.2 异步处理

#### 当前问题
- 离线奖励计算同步执行（可能耗时1-2秒）
- 邮件发送同步执行

#### 优化方案

**异步任务**:

```java
@Service
@RequiredArgsConstructor
public class OfflineRewardService {
    private final PlayerService playerService;
    
    /**
     * 异步计算离线奖励
     */
    @Async
    @Transactional
    public CompletableFuture<OfflineReward> calculateOfflineRewardAsync(Integer playerId) {
        PlayerProfile profile = playerService.getPlayerProfile(playerId);
        
        // 计算离线时长
        LocalDateTime lastOnline = profile.getLastOnlineTime();
        long offlineHours = ChronoUnit.HOURS.between(lastOnline, LocalDateTime.now());
        
        // 计算收益（耗时操作）
        OfflineReward reward = calculateReward(profile, offlineHours);
        
        // 保存奖励
        saveOfflineReward(playerId, reward);
        
        return CompletableFuture.completedFuture(reward);
    }
    
    /**
     * 异步发送邮件
     */
    @Async
    public void sendWelcomeMailAsync(Integer playerId) {
        // 邮件发送逻辑
        mailService.sendMail(playerId, "欢迎！", "...");
    }
}
```

**启用异步**:

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

**预期收益**:
- 离线奖励计算：从1.5s → 50ms（用户感知）
- 并发处理能力：从100 → 500用户/秒

---

### 3.3 连接池优化

#### 当前问题
- 数据库连接池配置可能不足
- 高并发时连接等待

#### 优化方案

**application.yml配置**:

```yaml
spring:
  datasource:
    hikari:
      # 连接池大小
      minimum-idle: 10
      maximum-pool-size: 50
      # 连接超时
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      # 连接测试
      connection-test-query: SELECT 1
      # 性能调优
      auto-commit: false
      pool-name: XiuxianHikariCP
```

**预期收益**:
- 高并发响应时间：从500ms → 100ms
- 连接等待时间：减少90%

---

## 4. 监控和诊断

### 4.1 APM监控

**添加性能监控**:

```java
@Component
public class PerformanceMonitor {
    
    @Around("execution(* com.xiuxian.game.service..*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = pjp.getSignature().getName();
        
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            
            // 记录慢查询（>100ms）
            if (duration > 100) {
                log.warn("SLOW METHOD: {} took {}ms", methodName, duration);
            }
            
            return result;
        } catch (Exception e) {
            log.error("METHOD ERROR: {} failed", methodName, e);
            throw e;
        }
    }
}
```

### 4.2 数据库慢查询日志

**MySQL配置**:

```ini
[mysqld]
# 慢查询阈值（秒）
long_query_time = 0.1

# 记录慢查询
slow_query_log = /var/log/mysql/slow-query.log

# 记录未使用索引的查询
log_queries_not_using_indexes = 1
```

---

## 5. 实施计划

### 阶段1（1周）
- ✅ 数据库索引添加
- ✅ N+1查询优化
- ✅ 前端懒加载

### 阶段2（1周）
- ✅ 虚拟列表实现
- ✅ 缓存策略实施
- ✅ 异步任务改造

### 阶段3（1周）
- ✅ 防抖节流应用
- ✅ 图片优化
- ✅ 连接池调优
- ✅ 监控系统上线

---

## 6. 预期效果

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 数据库查询响应（P95） | 200ms | 20ms | 90% |
| 前端首次渲染 | 2.5s | 1.2s | 52% |
| 并发用户能力 | 100/秒 | 500/秒 | 400% |
| 数据库负载 | 100% | 20% | 80% |
| 内存占用 | 2GB | 1GB | 50% |

---

## 结论

本性能优化方案涵盖了数据库、前端、后端全栈优化，预期可将系统性能提升3-10倍，支持更高并发和更好用户体验。

**关键成果**:
- ✅ 数据库优化：索引+分页+批量操作
- ✅ 前端优化：懒加载+虚拟列表+防抖节流
- ✅ 后端优化：缓存+异步+连接池
- ✅ 监控体系：APM+慢查询日志

**下一步**: 按阶段计划实施，每个阶段后进行性能基准测试验证效果。
