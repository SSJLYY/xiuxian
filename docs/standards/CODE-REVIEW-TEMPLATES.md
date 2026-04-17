# 代码审查模板

> 提供标准化的审查评论模板，确保审查质量一致性和沟通效率。

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-04-17

---

## 1. 单行评论模板

### 1.1 Blocker级别

```markdown
🔴 **Blocker: [问题类型]**

[具体问题描述]

**原因：**
[为什么这是Blocker级别问题]

**建议修复：**
```java
[修复后的代码示例]
```

**参考：**
[相关文档链接或最佳实践]
```

**示例：**
```markdown
🔴 **Blocker: SQL注入风险**

第45行：用户输入直接拼接到SQL语句中。

**原因：**
攻击者可以通过构造特殊输入执行任意SQL命令，如：
`name = "'; DROP TABLE users; --"`

**建议修复：**
```java
// 使用MyBatis参数绑定
@Select("SELECT * FROM users WHERE name = #{name}")
User findByName(@Param("name") String name);
```

**参考：**
- [OWASP SQL注入防护](https://owasp.org/www-community/attacks/SQL_Injection)
- 项目规范：[后端编码规范](./BACKEND-CODING-STANDARDS.md#安全规范)
```

### 1.2 Major级别

```markdown
🟡 **Major: [问题类型]**

[问题描述]

**影响：**
[不修复可能带来的问题]

**建议：**
```java
[改进代码示例]
```

**可选方案：**
- 方案A：[描述]
- 方案B：[描述]
```

**示例：**
```markdown
🟡 **Major: N+1查询问题**

`getPlayerQuestsDetail()`方法存在N+1查询。

**影响：**
如果玩家有50个任务，会产生51次数据库查询，响应时间随任务数线性增长。

**建议：**
```java
// 使用JOIN一次性查询
<select id="selectPlayerQuestDetail" resultMap="...">
    SELECT pq.*, q.* FROM player_quests pq
    LEFT JOIN quests q ON pq.quest_id = q.id
    WHERE pq.player_id = #{playerId}
</select>
```

**可选方案：**
- 方案A：使用MyBatis的嵌套结果映射
- 方案B：先批量查询所有quest，再内存关联
```

### 1.3 Minor级别

```markdown
💭 **Minor: [问题类型]**

[建议内容]

**考虑：**
[可选的改进方向]
```

**示例：**
```markdown
💭 **Minor: 变量命名**

变量`i`在嵌套循环中可能产生混淆。

**考虑：**
```java
// 可以改为更具描述性的名称
for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
    for (int colIndex = 0; colIndex < cols; colIndex++) {
        // ...
    }
}
```
```

### 1.4 肯定评论

```markdown
✅ **[优点类型]**

[具体描述]

**亮点：**
[为什么这是好的实践]
```

**示例：**
```markdown
✅ **设计模式应用**

缓存策略使用了读写分离模式，非常清晰。

**亮点：**
- 读取走缓存，写入先更新DB再删缓存
- 避免了缓存与数据库不一致的问题
- 代码结构清晰，易于维护
```

---

## 2. PR总结模板

### 2.1 标准总结模板

```markdown
## 审查总结

### 总体印象
[一句话概括：代码质量、设计思路、变更规模]

### 关键问题
| 优先级 | 问题 | 文件 | 行号 | 建议 |
|--------|------|------|------|------|
| 🔴 | [问题简述] | [文件名] | [行号] | [修复建议] |
| 🟡 | [问题简述] | [文件名] | [行号] | [修复建议] |
| 💭 | [建议简述] | [文件名] | [行号] | [可选改进] |

### 优点
- [列出代码中的亮点]

### 测试覆盖
- [ ] 单元测试覆盖新增逻辑
- [ ] 集成测试覆盖关键路径
- [ ] 边界情况有测试

### 文档更新
- [ ] API文档已更新
- [ ] 代码注释完整
- [ ] 变更日志已记录

### 结论
- [ ] **Approved** - 可以直接合并
- [ ] **Comment** - 有建议但不强制修改
- [ ] **Request Changes** - 需要修复后再次审查

**下一步：**
[具体的修复要求或合并指导]
```

### 2.2 示例总结

```markdown
## 审查总结

### 总体印象
本次PR实现了宠物进化系统，整体代码结构清晰，设计合理。变更规模适中（约300行），测试覆盖较好。

### 关键问题
| 优先级 | 问题 | 文件 | 行号 | 建议 |
|--------|------|------|------|------|
| 🔴 | 事务边界过大 | PetService.java | 156 | 将查询逻辑移出事务 |
| 🟡 | 魔法数字 | PetEvolutionChecker.java | 45 | 提取为常量 |
| 🟡 | 缺少边界测试 | PetEvolutionTest.java | - | 添加等级上限测试 |
| 💭 | 方法过长 | PetService.java | 89-120 | 可拆分为小方法 |

### 优点
- ✅ 进化条件检查逻辑清晰，使用策略模式可扩展
- ✅ 错误处理完善，使用了正确的ErrorCode
- ✅ 数据库查询优化，避免了N+1问题
- ✅ 代码注释详细，关键逻辑有说明

### 测试覆盖
- [x] 单元测试覆盖新增逻辑
- [x] 集成测试覆盖关键路径
- [ ] 边界情况有测试（建议补充满级进化测试）

### 文档更新
- [x] API文档已更新
- [x] 代码注释完整
- [ ] 变更日志已记录（请补充）

### 结论
- [ ] **Approved**
- [ ] **Comment**
- [x] **Request Changes**

**下一步：**
1. 修复Blocker：缩小PetService.evolve()的事务边界
2. 修复Major：提取魔法数字为常量
3. 补充边界测试和变更日志
4. 修复后@我重新审查
```

---

## 3. 常见问题模板

### 3.1 安全问题

**SQL注入**
```markdown
🔴 **Blocker: SQL注入风险**

用户输入直接拼接到SQL语句中，存在严重的安全风险。

**攻击示例：**
```java
String sql = "SELECT * FROM users WHERE name = '" + userName + "'";
// 输入: '; DROP TABLE users; --
// 结果: SELECT * FROM users WHERE name = ''; DROP TABLE users; --'
```

**修复方案：**
```java
// 方案1: 使用预编译语句
@Select("SELECT * FROM users WHERE name = #{name}")
User findByName(@Param("name") String name);

// 方案2: 使用MyBatis-Plus条件构造器
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("name", userName); // 自动参数化
```

**参考：** [OWASP SQL注入防护指南](https://owasp.org/www-community/attacks/SQL_Injection)
```

**密码安全**
```markdown
🔴 **Blocker: 明文密码比对**

代码中使用明文比对密码，违反安全规范。

**当前代码：**
```java
if (!user.getPassword().equals(inputPassword)) {
    throw new BusinessException(ErrorCode.WRONG_PASSWORD);
}
```

**修复方案：**
```java
@Autowired
private PasswordEncoder passwordEncoder;

if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
    throw new BusinessException(ErrorCode.WRONG_PASSWORD);
}
```

**参考：** 项目规范 [后端编码规范#密码与安全](./BACKEND-CODING-STANDARDS.md#密码与安全)
```

### 3.2 并发问题

**线程安全**
```markdown
🔴 **Blocker: 线程安全问题**

Service是单例，使用`new Random()`会导致多线程竞争。

**当前代码：**
```java
public int randomDamage() {
    Random random = new Random(); // 每次调用都创建，且非线程安全
    return random.nextInt(100);
}
```

**修复方案：**
```java
public int randomDamage() {
    return ThreadLocalRandom.current().nextInt(100);
}
```

**原因：**
- `Random`不是线程安全的
- `ThreadLocalRandom`每个线程独立，性能更好
- 避免在Service中维护可变状态

**参考：** 项目规范 [后端编码规范#并发安全](./BACKEND-CODING-STANDARDS.md#并发安全)
```

### 3.3 性能问题

**N+1查询**
```markdown
🟡 **Major: N+1查询问题**

循环中查询数据库，性能随数据量线性下降。

**当前代码：**
```java
public List<QuestDetail> getQuestDetails(Integer playerId) {
    List<PlayerQuest> quests = playerQuestMapper.selectByPlayerId(playerId);
    return quests.stream()
        .map(q -> {
            Quest quest = questMapper.selectById(q.getQuestId()); // N+1!
            return toDetail(q, quest);
        })
        .collect(Collectors.toList());
}
```

**修复方案：**
```java
// Mapper.xml中使用JOIN
<select id="selectQuestDetails" resultMap="QuestDetailResult">
    SELECT pq.*, q.title, q.description, q.reward_exp
    FROM player_quests pq
    LEFT JOIN quests q ON pq.quest_id = q.id
    WHERE pq.player_id = #{playerId}
</select>
```

**性能对比：**
- 当前：50个任务 = 51次查询
- 优化后：50个任务 = 1次查询

**参考：** [性能优化指南#N+1查询优化](./PERFORMANCE-GUIDE.md#N+1查询优化)
```

**长事务**
```markdown
🟡 **Major: 事务边界过大**

方法包含大量查询和少量更新，导致长事务占用连接。

**当前代码：**
```java
@Transactional
public DashboardVO getDashboard(Integer playerId) {
    // 大量查询...
    PlayerProfile profile = playerMapper.selectById(playerId);
    List<Quest> quests = questMapper.selectByPlayerId(playerId);
    List<Item> items = itemMapper.selectByPlayerId(playerId);
    // ... 更多查询
    
    // 少量更新
    playerMapper.updateLastLogin(playerId);
    
    return buildDashboard(profile, quests, items);
}
```

**修复方案：**
```java
public DashboardVO getDashboard(Integer playerId) {
    // 查询不需要事务
    PlayerProfile profile = playerMapper.selectById(playerId);
    List<Quest> quests = questMapper.selectByPlayerId(playerId);
    // ...
    
    // 更新单独抽离
    updateLastLogin(playerId);
    
    return buildDashboard(profile, quests, items);
}

@Transactional
private void updateLastLogin(Integer playerId) {
    playerMapper.updateLastLogin(playerId);
}
```

**参考：** 项目规范 [后端编码规范#事务管理](./BACKEND-CODING-STANDARDS.md#事务管理)
```

### 3.4 代码风格问题

**异常处理**
```markdown
🟡 **Major: 异常处理不当**

捕获异常后未处理或仅打印堆栈。

**当前代码：**
```java
try {
    processPayment(order);
} catch (Exception e) {
    e.printStackTrace(); // ❌ 仅打印，未处理
}
```

**修复方案：**
```java
try {
    processPayment(order);
} catch (PaymentException e) {
    LogUtils.error(log, "支付处理失败", e, "orderId", order.getId());
    throw new BusinessException(ErrorCode.PAYMENT_FAILED);
} catch (Exception e) {
    LogUtils.error(log, "支付处理异常", e, "orderId", order.getId());
    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
}
```

**参考：** 项目规范 [后端编码规范#异常处理](./BACKEND-CODING-STANDARDS.md#异常处理)
```

**日志规范**
```markdown
🟡 **Major: 日志使用不当**

循环内打印info级别日志，可能导致日志文件膨胀。

**当前代码：**
```java
for (Item item : items) {
    log.info("处理物品: {}", item.getId()); // 高频操作！
    processItem(item);
}
```

**修复方案：**
```java
log.info("开始批量处理物品, 数量: {}", items.size());

for (Item item : items) {
    log.debug("处理物品: {}", item.getId()); // debug级别
    processItem(item);
}

log.info("批量处理物品完成, 成功: {}, 失败: {}", successCount, failCount);
```

**参考：** 项目规范 [后端编码规范#日志规范](./BACKEND-CODING-STANDARDS.md#日志规范)
```

### 3.5 设计问题

**Controller职责**
```markdown
🟡 **Major: Controller职责过重**

Controller中编写了业务逻辑，违反分层设计。

**当前代码：**
```java
@PostMapping("/cultivate")
public ApiResponse<?> cultivate(@RequestBody CultivateRequest request) {
    PlayerProfile player = playerMapper.selectById(request.getPlayerId());
    
    if (player.getIsCultivating()) {
        return ApiResponse.error(ErrorCode.ALREADY_CULTIVATING);
    }
    
    // 计算收益...
    long exp = calculateExp(player);
    player.setExp(player.getExp() + exp);
    playerMapper.updateById(player);
    
    return ApiResponse.success();
}
```

**修复方案：**
```java
@PostMapping("/cultivate")
public ApiResponse<CultivateResult> cultivate(
        @AuthenticationPrincipal UserDetails user) {
    Long playerId = ((GameUserDetails) user).getPlayerId();
    CultivateResult result = playerService.startCultivate(playerId);
    return ApiResponse.success(result);
}
```

**Controller职责：**
1. 参数接收与校验
2. 从SecurityContext获取用户
3. 调用Service
4. 包装ApiResponse返回

**参考：** 项目规范 [后端编码规范#Controller设计](./BACKEND-CODING-STANDARDS.md#Controller设计)
```

---

## 4. 快速回复模板

### 4.1 作者回复

**已修复**
```markdown
✅ 已修复，见 commit [hash]

修改内容：[简要描述]
```

**解释说明**
```markdown
🤔 关于这个问题，我的考虑是：

[解释设计思路]

你觉得是否需要补充注释说明？
```

**请求澄清**
```markdown
❓ 能否详细说明一下这个建议？

具体是担心[某个方面]吗？
```

### 4.2 审查者回复

**确认修复**
```markdown
✅ 修复确认，问题已解决。
```

**进一步说明**
```markdown
💡 补充说明：

[更详细的解释]

可以参考[链接]
```

**同意作者观点**
```markdown
👍 有道理，保持当前实现即可。
```

---

## 5. 特殊场景模板

### 5.1 新手友好型评论

```markdown
💡 **建议（供参考）**

这里有一个可以改进的地方：

[问题描述]

**背景知识：**
[解释相关概念]

**建议方案：**
```java
[代码示例]
```

**为什么这样更好：**
[解释原因]

如果赶时间，可以后续PR优化。
```

### 5.2 架构调整评论

```markdown
🏗️ **架构建议**

当前实现可以工作，但建议考虑以下架构调整：

**当前问题：**
[描述架构层面的问题]

**建议方案：**
[描述更好的架构]

**迁移路径：**
1. 阶段1：[步骤]
2. 阶段2：[步骤]

**优先级：** 低（当前实现可接受，建议后续迭代优化）
```

### 5.3 性能优化评论

```markdown
⚡ **性能优化建议**

当前实现在[场景]下可能有性能问题：

**问题分析：**
[性能瓶颈分析]

**优化方案：**
```java
[优化后的代码]
```

**性能对比：**
| 场景 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| [场景] | [数据] | [数据] | [倍数] |

**建议：** 如果该接口调用频繁，建议优化；否则可保持当前实现。
```

---

## 附录：模板速查表

| 场景 | 模板 |
|------|------|
| Blocker问题 | `🔴 **Blocker:** [问题]` |
| Major问题 | `🟡 **Major:** [问题]` |
| Minor建议 | `💭 **Minor:** [建议]` |
| 肯定优点 | `✅ **[优点]** [描述]` |
| 已修复 | `✅ 已修复，见 commit [hash]` |
| 需要澄清 | `❓ 能否详细说明...` |
| 解释说明 | `🤔 关于这个问题...` |
