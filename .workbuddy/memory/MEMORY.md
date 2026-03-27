# 修仙挂机游戏项目 - 长期记忆

## 项目基本信息
- **技术栈**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1 + Spring Security + JWT + Redis（Lettuce）+ 原生 JS (ES6+)
- **端口**：8082，**数据库**：MySQL 8.0 `xiuxian_game`，**包名**：com.xiuxian.game
- **缓存**：Redis 6.0+（主层）+ ConcurrentHashMap（降级层），详见 `docs/architecture/CACHE-ARCHITECTURE.md`
- **规模**：~332 Java 文件，22 业务模块，42 common，29 dto，39 validation，40 JS，20 HTML

## 包结构（四包模块化）
```
com.xiuxian.game/
├── common/   ← annotation/aspect/config/exception/security/util
├── modules/  ← 22个业务模块（每个含 controller/entity/mapper/service）
├── dto/      ← request/ + response/
└── validation/ ← 启动时 Schema 与 API 一致性校验
```
**22个模块**：player、combat、cultivation（仅entity+mapper）、equipment、skill、pet、quest、shop、achievement、guild、ranking、auction、mail、narrative、map、offline、checkin、activity、giftcode、announcement、vip、admin
**admin模块**：16个Service，13个Controller（最大模块，共35文件）

## 架构规范
- **模块边界**：模块A→B 通过 Service 接口，**禁止跨模块直接调用 Mapper**
- admin 聚合层（AdminPlayerService、AdminContentService 等）允许直接使用下级模块 Mapper（务实例外）
- 统一异常：`BusinessException` + `ErrorCode` 枚举；返回格式：`ApiResponse<T>`
- ErrorCode 分段：通用1000-1099、用户1100-1199、邮件2000-2099、宗门2400-2499、叙事3000-3099、地图3100-3199、宗门BOSS3200-3299、签到3300-3399
- 双认证：游戏端(`/api/auth/*` + authToken) / 管理端(`/api/admin/auth/*` + adminToken) 完全独立
- 日志工具：`LogUtils`；IP解析：`RequestUtils.getClientIp()`；战斗结果：`CombatResult` DTO

## 代码质量规范
- 异常处理器顺序：子类在前，Exception 兜底在后
- 密码：`passwordEncoder.matches()`；并发：`ThreadLocalRandom.current()`
- 事务：只在原子写操作加 `@Transactional(rollbackFor=Exception.class)`，读写混合入口不加
- 日志：循环内无 `info`，`error` 必带异常对象
- 编码：全项目 UTF-8 无 BOM（已完成全量转换 2026-03-25）
- 技术文档：`docs/` 目录，作者统一署名 **shaun.sheng**；新增文档后同步更新 `docs/README.md`

## 基础设施优化（2H4G 服务器适配）
- 连接池：max-pool-size=10，min-idle=3；Redis 连接池：max-active=15，max-idle=8
- TTL：rankingCache 3min，auctionCache 30s，configCache 2h
- Sentinel 限流：战斗30QPS，核心50QPS，排行榜20QPS，拍卖行15QPS；熔断：异常比例50%
- JVM：-Xms512m -Xmx1024m，-XX:+UseG1GC，-XX:MaxGCPauseMillis=200

## 数值公式
- 修炼灵石/时：(20+level×5)×speed×realm_bonus（练气1.0/筑基1.5/金丹2.5/元婴4.0）
- 防御率：defense/(defense+attackerLevel×10)；暴击：5%概率×1.8倍
- 境界突破：消耗5000灵石，70%心魔战胜率，失败1h冷却
- 战斗掉落：(10+怪物Lv×2)×类型倍率(普通1/精英2.5/BOSS6)×等级差修正

## 前端 CSS 颜色系统
- `--color-primary: #1a1a2e`（深蓝背景）、`--color-gold: #d4af37`（金色）
- `--color-aqua: #7fffd4`（淡青）、`--color-text: #e8e8e8`（主文字）

## 已完成的审计与修复

### 安全审计（2026-03-26）
- P0: AdminAuthService 明文密码 → BCrypt；application.properties 密码哈希化
- P1: JWT token 日志截断为前8字符；全项目 XSS 修复（22 JS + admin.html）
- SQL注入 ✅ 全量 `#{}`；认证 ✅ 双认证独立

### 并发安全审计（2026-03-26）
- ✅ 随机数：全部 ThreadLocalRandom；线程安全：无静态可变状态
- P1: 拍卖行 TOCTOU → 原子SQL；宗门BOSS血量 → 原子SQL；宗门成员计数/资金 → 原子SQL
- P2: 灵石/经验 → 4个Entity加 `@Version` 乐观锁 + MybatisPlusConfig 注册 OptimisticLockerInnerInterceptor
- P3: DegradeConfig.strategies → unmodifiableMap
- SQL迁移脚本：`docs/sql/add-optimistic-lock-columns.sql`

### 数据一致性审计（2026-03-26）
- P1: RankingService @Async+@Transactional 事务失效 → rollbackFor + 内联逻辑
- P2: ActivityService/VipService 无事务 → 加 @Transactional；GuildService rollbackFor 统一
- P2: PlayerService.stopCultivate 异常被吞 → log.error + 完整异常对象
- P3: MailService/PlayerService/SkillService 移除不必要 @Transactional

### Admin 模块乱码修复（2026-03-26）
- 36 个 Java 文件全量清零乱码（Python 脚本分批完整重写）

### 模块边界重构（2026-03-25，全量完成）
- **非 admin 模块跨模块 Mapper 违规: 0**（全部清零）
- **通配符 mapper import: 0**（全部精确 import）
- admin 聚合层 4 个服务保留直接 Mapper（务实例外）
- 扫描工具：`scan_modules.ps1`

## 异常处理审计（2026-03-26）

### 审计结果
- **裸异常 throw**：17 个文件 140+ 处（主要是 `IllegalArgumentException` 用于业务参数校验）
- **空 catch 块**：1 个文件 2 处（GameConfigService.java 第202、207行，try-parse 模式）
- **error 日志不带异常对象**：1 个文件 1 处（MailService.java 第121行）

### 修复完成（2026-03-26）
- **140+ 处裸异常**：Python 脚本批量替换 `IllegalArgumentException`/`RuntimeException` → `BusinessException(ErrorCode.PARAM_ERROR, msg)`
- **2 处空 catch 块**：GameConfigService.java 添加 try-parse 惯用法注释（修正格式：注释放在 {} 内独立行）
- **1 处 error 日志**：MailService.java `log.error("msg: playerId={}", playerId, e.getMessage())` → `log.error("msg: playerId={}", playerId, e)`
- **补充修复**：PlayerController 2 处 `IllegalArgumentException` → `BusinessException`（Controller 层参数校验）

## 全量编译修复（2026-03-27，BUILD SUCCESS）

**修复前状态**：83 个编译错误（21 个文件）
**修复后状态**：0 错误，全量 BUILD SUCCESS（332 个 Java 文件）

### 修复清单
1. **GameBalanceUtils / RechargeService**：添加 `import java.util.concurrent.ThreadLocalRandom`
2. **PlayerSkillComboRecord**：添加 `@Builder @NoArgsConstructor @AllArgsConstructor`
3. **GiftCodeService**：修复包路径 `com.xiuxian.game.exception` → `com.xiuxian.game.common.exception`
4. **4个 narrative controllers**（NpcController/NarrativeController/DialogueController/LoreController）：移除 `LogUtils.info()` 改为 `log.info()`；`var` 改具体类型；`Map.of(4+)` 改用 `HashMap`
5. **PlayerService**：新增 `getPlayerProfile()`、`canBreakthrough()`、`attemptBreakthrough()` 方法
6. **EnhancedCombatService**：`getDroppedEquipmentId()` → `getDropEquipmentId()`；`getPlayerSkillById()` → `getPlayerSkillByPlayerAndSkill(playerId, skillId)`；`getSkillLevel()` → `getLevel()`
7. **PetService**：完整重写 `calculatePetCombatBonus()`，从返回 entity.PetCombatBonus 改为 dto.response.PetCombatBonus；修复 `selectAvailablePets()` 缺参；`selectActivePetByPlayerId()` → `selectActivePet()`；`BigDecimal.captureRate` 转 double；`setExp(0)` → `setExp(0L)`；`selectByPetId()` 返回 List 取 first；Map.of 超参改 HashMap；删除 entity/PetCombatBonus.java
8. **PetEvolution**：新增 `requiredLoyalty`、`evolvedPetId` 字段
9. **PetEvolutionResult**：新增 `evolution` transient 字段、`fail(msg)` 和 `success(msg, evolution)` 静态工厂方法
10. **SentinelConfig**：用 `setHighestSystemLoad/setHighestCpuUsage/setAvgRt(long)/setMaxThread(long)/setQps(double)` 替换不存在的 `setGrade/setCount`
11. **MetricsConfig**：移除 `WebMvcMetricsFilter` Bean（构造器不兼容）；添加缺失 `@Bean` import
12. **RedisConfig**：`Jackson2JsonRedisSerializer(mapper, Class)` → 单参数 `(Class)` + `setObjectMapper()`
13. **AuctionService**：`playerItemId.intValue()` → 保留 Long；`equipmentService.getPlayerEquipmentById(Long)` 直接传 Long
14. **GuildBossService**：lambda 变量 `myDamage` 改用 `final myDamageFinal`
15. **ShopService**：新增 `listAllItems()` 方法（admin 用）
16. **SkillController**：`.toList()` (Java16) → `.collect(Collectors.toList())`
17. **GameMapService**：添加 BusinessException/ErrorCode import；`boolean isElite` → `Boolean isElite`（修复 Lombok setter 名）
18. **GameMapController**：`ApiResponse.success(data, msg)` → `ApiResponse.success(msg, data)`

### 关键 API 备忘
- `Jackson2JsonRedisSerializer`（Spring Data Redis 2.7.18）只有 `(Class<T>)` 和 `(JavaType)` 构造器，用 `setObjectMapper()` 设置自定义 mapper
- `SystemRule`（Sentinel 1.8.6）无 setGrade/setCount，用 `setHighestSystemLoad/setHighestCpuUsage/setAvgRt(long)/setMaxThread(long)/setQps(double)`
- `PetEvolutionMapper.selectByPetId()` 返回 `List<PetEvolution>`，需取 `get(0)`

*最后更新：2026-03-27（全项目编译通过 BUILD SUCCESS）*
