# 后端编码规范

> 本规范是代码审查的基准线。新功能提交前请对照检查。  
> 更新日期：2026-03-25（代码 v2 同步）

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-25（代码 v2 同步）

---

## 1. 异常处理

### ✅ 正确做法
```java
// 抛出业务异常
throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES, "当前灵石：" + current + "，需要：" + required);

// 捕获后转换
try {
    externalService.call();
} catch (ExternalException e) {
    LogUtils.error(log, "外部服务调用失败", e, "service", "XXX");
    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
}
```

### ❌ 错误做法
```java
// 错误 1：裸 RuntimeException 会被兜底处理器捕获，丢失语义
throw new RuntimeException("灵石不足");

// 错误 2：直接吞异常
try { ... } catch (Exception e) { /* 什么都不做 */ }

// 错误 3：打印堆栈但不处理
} catch (Exception e) { e.printStackTrace(); }
```

### GlobalExceptionHandler 顺序规则
```java
// ✅ 子类处理器必须在父类之前
@ExceptionHandler(BusinessException.class)    // 先
@ExceptionHandler(RuntimeException.class)     // ❌ 不要加这个！
@ExceptionHandler(Exception.class)            // 最后兜底
```

---

## 2. 密码与安全

```java
// ✅ 正确：使用 BCrypt 验证
if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
    throw new BusinessException(ErrorCode.WRONG_PASSWORD);
}

// ❌ 错误：明文比对
if (!user.getPassword().equals(inputPassword)) { ... }

// ❌ 错误：MD5（不安全）
if (!MD5.encode(inputPassword).equals(storedPassword)) { ... }
```

---

## 3. 并发安全

```java
// ✅ 正确：ThreadLocalRandom（线程安全）
int result = ThreadLocalRandom.current().nextInt(100);

// ❌ 错误：new Random()（Service 是单例，会有线程安全问题）
Random random = new Random();
int result = random.nextInt(100);

// ❌ 错误：static Random 也不推荐（竞争问题）
private static final Random RANDOM = new Random();
```

---

## 4. 事务管理

```java
// ✅ 正确：只在需要原子写操作的方法上加事务
@Transactional
public void consumeSpiritStones(Long playerId, int amount) {
    PlayerProfile player = playerMapper.selectById(playerId);
    if (player.getSpiritStones() < amount) {
        throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
    }
    player.setSpiritStones(player.getSpiritStones() - amount);
    playerMapper.updateById(player);
    // 记录流水日志
    transactionLogMapper.insert(...);
}

// ❌ 错误：在查询+更新的外层入口加事务（长事务）
@Transactional
public PlayerDashboardVO getDashboard(Long playerId) {
    // 大量查询 + 可能的更新 = 长事务，占用数据库连接
}
```

**只读查询**不需要 `@Transactional`，除非需要事务隔离级别保证。

---

## 5. 日志规范

```java
@Slf4j  // 或 private static final Logger log = LogManager.getLogger(XXX.class);

// ✅ 使用 LogUtils（支持 MDC 链路追踪）
LogUtils.info(log, "玩家开始修炼", "playerId", playerId, "speed", cultivationSpeed);
LogUtils.debug(log, "战斗回合计算", "round", round, "damage", damage);
LogUtils.warn(log, "宠物饱食度过低", "petId", petId, "hunger", hunger);
LogUtils.error(log, "修炼收益计算失败", exception, "playerId", playerId);

// 日志级别选择：
// info  → 请求入口、关键业务完成（如"登录成功"、"订单创建"）
// debug → 中间计算步骤（循环内、条件分支结果）
// warn  → 业务降级、参数校验警告、预期内的异常情况
// error → 非预期异常、需要人工介入的情况
```

**禁止**：
- 在循环内打 `info` 日志（高频操作会撑爆日志文件）
- 在 `error` 日志里不传 `exception` 对象（丢失堆栈信息）

---

## 6. Controller 设计

Controller 只负责：
1. 参数接收与 `@Valid` 校验
2. 从 SecurityContext 获取当前用户
3. 调用 Service
4. 包装 `ApiResponse` 返回

```java
// ✅ 正确：薄 Controller
@PostMapping("/cultivate")
public ApiResponse<CultivationResult> startCultivate(@AuthenticationPrincipal UserDetails user) {
    Long playerId = ((GameUserDetails) user).getPlayerId();
    CultivationResult result = playerService.startCultivate(playerId);
    return ApiResponse.success(result);
}

// ❌ 错误：把业务逻辑写在 Controller 里
@PostMapping("/cultivate")
public ApiResponse<CultivationResult> startCultivate(...) {
    PlayerProfile player = playerMapper.selectById(playerId);  // 直接查数据库
    if (player.isCultivating()) {
        return ApiResponse.error(ErrorCode.ALREADY_CULTIVATING);
    }
    // ... 大量业务逻辑
}
```

---

## 7. 客户端 IP 获取

```java
// ✅ 正确：统一使用 RequestUtils
String ip = RequestUtils.getClientIp(request);

// ❌ 错误：在 Controller 里手动解析（不处理代理链）
String ip = request.getRemoteAddr();
```

---

## 8. 战斗结果传递

```java
// ✅ 正确：使用 CombatResult DTO
CombatResult result = combatService.battle(playerId, monsterId);
return ApiResponse.success(result);

// ❌ 错误：用 Map 传递（类型不安全，文档化困难）
Map<String, Object> result = new HashMap<>();
result.put("win", true);
result.put("damage", 150);
```

---

## 9. 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| Entity | `XxxEntity` → 实际用无后缀 | `PlayerProfile`, `Pet` |
| DTO（请求） | `XxxRequest` | `LoginRequest`, `CaptureRequest` |
| DTO（响应） | `XxxVO` 或 `XxxResult` | `PlayerProfileVO`, `CombatResult` |
| Service | `XxxService` | `PlayerService`, `CombatService` |
| Mapper | `XxxMapper` | `PlayerMapper`, `PetMapper` |
| Controller | `XxxController` | `PlayerController` |
| 方法命名 | 动词+名词 | `startCultivate()`, `captureAPet()` |

---

## 10. 模块化架构规范

项目采用 `common / modules / dto / validation` 四包结构，**新代码必须放入正确位置**：

| 代码类型 | 正确位置 | 错误位置 |
|---------|---------|---------|
| Controller / Entity / Mapper / Service | `modules/{模块名}/` | 根包下 |
| 公共配置 / 安全认证 / 工具类 | `common/{config|security|util}/` | 各模块内 |
| 请求/响应 DTO | `dto/request/` 或 `dto/response/` | `modules/` 内 |
| AOP 切面 | `common/aspect/` | 各模块内 |
| 自定义注解 | `common/annotation/` | 各模块内 |

**模块间依赖规则**（强制）：
```java
// ✅ 正确：模块 A 调用模块 B 的 Service 接口
@Autowired
private PetService petService;   // 在 CombatService 中引用宠物模块

// ❌ 禁止：跨模块直接调用 Mapper
@Autowired
private PetMapper petMapper;     // 在 CombatService 中！—— 破坏模块边界
```

**新模块标准目录结构**：
```
modules/your-module/
├── controller/
│   └── YourModuleController.java
├── entity/
│   └── YourEntity.java
├── mapper/
│   └── YourMapper.java
└── service/
    └── YourService.java
```

新增模块后在 `XiuxianGameApplication.java` 的 `@MapperScan` 中添加：
```java
"com.xiuxian.game.modules.your-module.mapper"
```

---

## 11. 代码审查清单

提交 PR 前自查：

- [ ] 所有业务异常使用 `BusinessException(ErrorCode.XXX)` 抛出
- [ ] 随机数使用 `ThreadLocalRandom.current()`
- [ ] 密码验证使用 `passwordEncoder.matches()`
- [ ] `@Transactional` 只加在真正需要原子写的方法上
- [ ] 日志分级正确（循环内无 `info`，`error` 带异常对象）
- [ ] Controller 无业务逻辑（只有参数校验+Service调用+结果包装）
- [ ] IP 获取使用 `RequestUtils.getClientIp()`
- [ ] 新 ErrorCode 已在 `common/exception/ErrorCode.java` 和 [ErrorCode 手册](../standards/ERROR-CODE-REFERENCE.md) 中登记
- [ ] 新代码放入正确模块目录（`modules/{模块名}/`），未跨模块直接调用 Mapper
- [ ] 相关 API 文档已更新

---

## 12. 相关文档

- **[代码审查标准](./CODE-REVIEW-STANDARDS.md)** — 完整的审查检查清单和优先级定义
- **[代码审查流程](./CODE-REVIEW-PROCESS.md)** — PR流程、角色职责、工具使用
- **[代码审查模板](./CODE-REVIEW-TEMPLATES.md)** — 标准化的审查评论模板
