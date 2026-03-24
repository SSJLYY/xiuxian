# 代码审查标准与规范

> 本规范定义代码审查的检查清单、优先级标准和审查流程。所有提交必须通过审查才能合并。

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-24

---

## 1. 审查优先级定义

### 🔴 Blocker（阻塞级）— 必须修复

以下问题**必须**在合并前解决：

| 检查项 | 说明 | 示例 |
|--------|------|------|
| 安全漏洞 | SQL注入、XSS、认证绕过、敏感信息泄露 | 用户输入直接拼接SQL |
| 数据风险 | 可能导致数据丢失、损坏或不一致 | 无事务的转账操作 |
| 并发问题 | 竞态条件、死锁、线程安全问题 | Service中使用`new Random()` |
| API破坏 | 破坏现有API契约或向后兼容性 | 删除已发布接口的参数 |
| 关键路径缺失错误处理 | 核心业务流程无异常处理 | 支付流程无try-catch |

### 🟡 Major（重要级）— 应该修复

以下问题**应该**修复， reviewer可酌情放行：

| 检查项 | 说明 | 示例 |
|--------|------|------|
| 输入验证缺失 | 未校验用户输入的范围和格式 | 未检查负数ID |
| 命名混乱 | 方法/变量命名无法表达意图 | `doSomething()` |
| 关键测试缺失 | 核心逻辑无单元测试 | 战斗计算无测试 |
| N+1查询 | 循环中查询数据库 | 循环内调用mapper |
| 重复代码 | 明显可提取的重复逻辑 | 多处相同的计算逻辑 |

### 💭 Minor（建议级）— 可选修复

以下问题可记录但不强制修复：

| 检查项 | 说明 | 示例 |
|--------|------|------|
| 风格不一致 | 缩进、空格等（如无linter） | 混用tab和空格 |
| 文档缺失 | 公共方法缺少JavaDoc | 复杂算法无注释 |
| 次要命名 | 变量名可更清晰 | `i` → `index` |
| 过度设计 | 不必要的抽象层 | 简单CRUD用策略模式 |

---

## 2. 后端代码审查清单

### 2.1 异常处理 ✅

```java
// ✅ 正确：使用业务异常
throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);

// ❌ Blocker：裸RuntimeException
throw new RuntimeException("灵石不足");

// ❌ Blocker：吞异常
try { ... } catch (Exception e) { /* 空 */ }

// ❌ Major：打印但不处理
try { ... } catch (Exception e) { e.printStackTrace(); }
```

**检查点**：
- [ ] 所有业务异常使用 `BusinessException(ErrorCode.XXX)`
- [ ] 无裸 `RuntimeException` 抛出
- [ ] 无空 catch 块
- [ ] 使用 `LogUtils` 记录异常，而非 `e.printStackTrace()`

### 2.2 安全规范 🔐

```java
// ✅ 正确：BCrypt验证
if (!passwordEncoder.matches(raw, encoded)) { ... }

// ❌ Blocker：明文比对
if (user.getPassword().equals(input)) { ... }

// ❌ Blocker：SQL注入风险
String sql = "SELECT * FROM users WHERE name = '" + name + "'";
```

**检查点**：
- [ ] 密码验证使用 `passwordEncoder.matches()`
- [ ] 无SQL拼接（使用预编译语句或MyBatis参数）
- [ ] 敏感操作有权限检查
- [ ] 无硬编码密钥/密码

### 2.3 并发安全 ⚡

```java
// ✅ 正确：ThreadLocalRandom
int result = ThreadLocalRandom.current().nextInt(100);

// ❌ Blocker：Service单例中使用new Random()
Random random = new Random();

// ❌ Major：static Random（竞争问题）
private static final Random RANDOM = new Random();
```

**检查点**：
- [ ] 随机数使用 `ThreadLocalRandom.current()`
- [ ] Service中无不安全的共享状态
- [ ] 并发集合使用正确（`ConcurrentHashMap` vs `HashMap`）

### 2.4 事务管理 📦

```java
// ✅ 正确：只在写操作上加事务
@Transactional
public void consumeSpiritStones(Long playerId, int amount) {
    // 查询 + 更新（原子操作）
}

// ❌ Major：在查询入口加事务（长事务）
@Transactional
public PlayerDashboardVO getDashboard(Long playerId) {
    // 大量查询，不必要的事务
}
```

**检查点**：
- [ ] `@Transactional` 只在需要原子写的方法上
- [ ] 读多写少的方法不加事务
- [ ] 事务方法不调用外部HTTP/RPC（避免长事务）

### 2.5 日志规范 📝

```java
// ✅ 正确：使用LogUtils
LogUtils.info(log, "玩家开始修炼", "playerId", playerId);
LogUtils.error(log, "操作失败", exception, "param", value);

// ❌ Major：循环内打info
for (Item item : items) {
    log.info("处理物品: {}", item.getId()); // 高频！
}

// ❌ Major：error不带异常
log.error("系统错误"); // 丢失堆栈！
```

**检查点**：
- [ ] 使用 `LogUtils` 而非直接 `System.out`
- [ ] 循环内无 `info` 级别日志
- [ ] `error` 日志必须带异常对象
- [ ] 日志级别选择正确（info/debug/warn/error）

### 2.6 Controller规范 🎯

```java
// ✅ 正确：薄Controller
@PostMapping("/cultivate")
public ApiResponse<CultivationResult> cultivate(@AuthenticationPrincipal UserDetails user) {
    Long playerId = ((GameUserDetails) user).getPlayerId();
    return ApiResponse.success(playerService.startCultivate(playerId));
}

// ❌ Major：Controller里写业务逻辑
@PostMapping("/cultivate")
public ApiResponse<?> cultivate(...) {
    PlayerProfile player = playerMapper.selectById(playerId);
    if (player.isCultivating()) { ... } // 业务逻辑！
}
```

**检查点**：
- [ ] Controller只负责：参数接收、获取用户、调用Service、包装返回
- [ ] 无业务逻辑在Controller中
- [ ] IP获取使用 `RequestUtils.getClientIp()`
- [ ] 返回统一使用 `ApiResponse<T>`

### 2.7 数据库查询 📊

```java
// ✅ 正确：JOIN避免N+1
<select id="selectPlayerQuestDetail" resultMap="...">
    SELECT pq.*, q.* FROM player_quests pq
    LEFT JOIN quests q ON pq.quest_id = q.id
    WHERE pq.player_id = #{playerId}
</select>

// ❌ Major：N+1查询
public List<Detail> getDetails(Integer playerId) {
    List<PlayerQuest> list = mapper.selectByPlayerId(playerId);
    return list.stream()
        .map(pq -> {
            Quest q = questMapper.selectById(pq.getQuestId()); // N+1！
            return toDetail(pq, q);
        })
        .collect(Collectors.toList());
}
```

**检查点**：
- [ ] 无N+1查询（使用JOIN或批量查询）
- [ ] 大表查询有分页（LIMIT/OFFSET）
- [ ] 查询字段有索引（检查执行计划）
- [ ] 批量操作使用 `updateBatchById` 而非循环

### 2.8 ErrorCode规范 🔢

```java
// ✅ 正确：使用ErrorCode枚举
throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);

// ❌ Major：硬编码错误码
throw new BusinessException(1201, "灵石不足");

// ❌ Major：新增ErrorCode未登记
// 在ErrorCode.java添加了NEW_ERROR，但未更新文档
```

**检查点**：
- [ ] 使用 `ErrorCode.XXX` 枚举，非硬编码数字
- [ ] 新增ErrorCode在 `ErrorCode.java` 和手册中登记
- [ ] 错误码分段符合规范（1000通用、1100用户...）

---

## 3. 前端代码审查清单

### 3.1 JavaScript规范

```javascript
// ✅ 正确：使用const/let，避免var
const playerId = getCurrentPlayerId();
let count = 0;

// ❌ Major：使用var
var name = 'player';

// ✅ 正确：使用async/await
async function loadPlayerData() {
    const data = await api.getPlayer(playerId);
    return data;
}

// ❌ Minor：回调地狱
api.getPlayer(playerId, function(data) {
    api.getQuests(data.id, function(quests) {
        // ...
    });
});
```

**检查点**：
- [ ] 使用 `const`/`let`，避免 `var`
- [ ] 异步操作使用 `async/await`
- [ ] 事件监听有移除（防止内存泄漏）
- [ ] DOM查询有缓存（避免重复查询）

### 3.2 性能优化

```javascript
// ✅ 正确：防抖处理
const search = debounce((query) => {
    api.search(query);
}, 300);

// ❌ Major：无防抖的输入监听
input.addEventListener('input', (e) => {
    api.search(e.target.value); // 每字符都请求！
});

// ✅ 正确：事件委托
list.addEventListener('click', (e) => {
    if (e.target.matches('.item')) {
        handleItemClick(e.target);
    }
});

// ❌ Minor：循环绑定事件
items.forEach(item => {
    item.addEventListener('click', handleClick); // 100个=100个监听
});
```

**检查点**：
- [ ] 输入搜索有防抖（debounce）
- [ ] 滚动事件有节流（throttle）
- [ ] 大量列表使用事件委托
- [ ] 图片懒加载（loading="lazy"）

### 3.3 CSS规范

```css
/* ✅ 正确：使用CSS变量 */
.card {
    background: var(--color-primary);
    color: var(--color-text);
}

/* ❌ Major：硬编码颜色 */
.card {
    background: #1a1a2e;
}

/* ✅ 正确：响应式设计 */
@media (max-width: 768px) {
    .container { flex-direction: column; }
}
```

**检查点**：
- [ ] 使用CSS变量（`--color-*`）
- [ ] 颜色符合WCAG AA对比度标准
- [ ] 响应式布局适配移动端
- [ ] 动画使用 `transform` 和 `opacity`（GPU加速）

---

## 4. 审查流程

### 4.1 PR提交前自查

提交PR前，作者必须完成以下检查：

```markdown
## PR自查清单

- [ ] 代码通过本地编译，无错误
- [ ] 新增/修改的功能有基本测试
- [ ] 所有业务异常使用 `BusinessException(ErrorCode.XXX)`
- [ ] 随机数使用 `ThreadLocalRandom.current()`
- [ ] 密码验证使用 `passwordEncoder.matches()`
- [ ] `@Transactional` 只加在真正需要原子写的方法上
- [ ] 日志分级正确（循环内无info，error带异常）
- [ ] Controller无业务逻辑
- [ ] 新增ErrorCode已在代码和文档中登记
- [ ] 相关API文档已更新
- [ ] 代码符合项目编码规范
```

### 4.2 审查角色

| 角色 | 职责 | 要求 |
|------|------|------|
| **作者** | 提交代码、回应评论、修复问题 | 完成自查清单 |
| **审查者** | 检查代码、提出意见、批准/拒绝 | 熟悉相关模块 |
| **观察者** | 学习代码、提出建议（可选） | 任何团队成员 |

### 4.3 审查时间规范

- **响应时间**：审查者应在24小时内开始审查
- **单次审查时长**：建议30-60分钟，不超过2小时
- **修复时间**：Blocker应在24小时内修复，Major可协商

### 4.4 审查沟通规范

**提问而非指责**：
```markdown
❌ "这段代码有问题，会N+1查询"
✅ "这里是否考虑用JOIN优化？如果有N条记录会产生N+1查询"

❌ "命名太差"
✅ "这个方法名`doIt()`能否更具体？比如`calculateDamage()`"
```

**解释原因**：
```markdown
✅ "建议将这个方法提取出来，因为：
   1. 目前两处代码逻辑相同
   2. 如果后续修改容易遗漏
   3. 提取后可单独测试"
```

**认可好代码**：
```markdown
✅ "这个缓存策略设计得很好，读写分离的思路很清晰 👍"
```

---

## 5. 审查模板

### 5.1 审查总结模板

```markdown
## 审查总结

### 总体印象
[简要描述代码质量、设计思路]

### 关键问题
| 优先级 | 问题 | 位置 | 建议 |
|--------|------|------|------|
| 🔴 | SQL注入风险 | UserMapper.java:45 | 使用预编译语句 |
| 🟡 | N+1查询 | OrderService.java:78 | 使用JOIN优化 |
| 💭 | 命名不清晰 | Utils.java:23 | `process()` → `validateInput()` |

### 优点
- [列出代码中的亮点]

### 建议
- [可选的改进建议]

### 结论
- [ ] 批准合并
- [ ] 修复后批准
- [ ] 需要再次审查
```

### 5.2 单行评论模板

```markdown
🔴 **Blocker: [问题类型]**
[具体问题描述]

**原因：** [为什么这是问题]

**建议：**
```java
[修复后的代码示例]
```
```

---

## 6. 常见问题速查

### Q: 发现历史代码有问题，要一起修吗？
A: 遵循"童子军规则"：如果修改相关代码，顺手修复；如果无关，另提PR。

### Q: 审查意见有分歧怎么办？
A: 1) 先讨论技术方案；2) 无法达成一致时，由资深开发者或架构师决定；3) 记录决策原因。

### Q: 紧急修复可以跳过审查吗？
A: 生产事故可以事后审查，但必须在24小时内补审查记录。

### Q: 如何成为审查者？
A: 熟悉项目规范后，从观察开始，逐步参与。建议每个开发者都参与审查。

---

## 附录：相关文档

- [后端编码规范](./BACKEND-CODING-STANDARDS.md)
- [ErrorCode手册](./ERROR-CODE-REFERENCE.md)
- [性能优化指南](./PERFORMANCE-GUIDE.md)
- [前端开发指南](../guides/FRONTEND-GUIDE.md)
