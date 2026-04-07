# 修仙挂机游戏项目 - 长期记忆

## 项目基本信息
- **技术栈**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1 + Spring Security + JWT + Redis（Lettuce）+ 原生 JS (ES6+)
- **端口**：8082，**数据库**：MySQL 8.0 `xiuxian_game`，**包名**：com.xiuxian.game
- **缓存**：Redis 6.0+（主层）+ ConcurrentHashMap（降级层）
- **规模**：~335 Java 文件，22 业务模块；前端 ~113 JS，47 HTML（重构后）

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
- **模块边界**：模块A→B 通过 Service 接口，**禁止跨模块直接调用 Mapper**（admin 聚合层例外）
- 统一异常：`BusinessException` + `ErrorCode` 枚举；返回格式：`ApiResponse<T>`
- ErrorCode 分段：通用1000-1099、用户1100-1199、邮件2000-2099、宗门2400-2499、叙事3000-3099、地图3100-3199、宗门BOSS3200-3299、签到3300-3399
- 双认证：游戏端(`/api/auth/*` + authToken) / 管理端(`/api/admin/auth/*` + adminToken) 完全独立
- IP解析：`RequestUtils.getClientIp()`；日志：`LogUtils`；战斗结果：`CombatResult` DTO

## 代码质量规范
- 异常处理器顺序：子类在前，Exception 兜底在后
- 密码：`passwordEncoder.matches()`；并发：`ThreadLocalRandom.current()`
- 事务：只在原子写操作加 `@Transactional(rollbackFor=Exception.class)`，读写混合入口不加
- 日志：循环内无 `info`，`error` 必带异常对象
- 编码：全项目 UTF-8 无 BOM（2026-03-25 完成）
- 技术文档：`docs/` 目录，作者统一署名 **shaun.sheng**；新增文档后同步更新 `docs/README.md`

## 基础设施（2H4G 服务器）
- 连接池：max-pool-size=10，min-idle=3；Redis 连接池：max-active=15，max-idle=8
- Sentinel 限流：战斗30QPS，核心50QPS，排行榜20QPS，拍卖行15QPS
- JVM：-Xms512m -Xmx1024m，-XX:+UseG1GC

## 数值公式
- 修炼灵石/时：(20+level×5)×speed×realm_bonus（练气1.0/筑基1.5/金丹2.5/元婴4.0）
- 防御率：defense/(defense+attackerLevel×10)；暴击：5%概率×1.8倍
- 境界突破：消耗5000灵石，70%心魔战胜率，失败1h冷却

## 前端 CSS 颜色系统
- `--color-primary: #1a1a2e`（深蓝背景）、`--color-gold: #d4af37`（金色）
- `--color-aqua: #7fffd4`（淡青）、`--color-text: #e8e8e8`（主文字）

## 关键 API 备忘
- `Jackson2JsonRedisSerializer`（Spring Data Redis 2.7.18）只有 `(Class<T>)` 构造器，用 `setObjectMapper()` 设置 mapper
- `SystemRule`（Sentinel 1.8.6）用 `setHighestSystemLoad/setAvgRt(long)/setQps(double)`，无 setGrade/setCount
- `PetEvolutionMapper.selectByPetId()` 返回 `List<PetEvolution>`，需取 `get(0)`
- `ApiResponse.success(msg, data)` — 参数顺序：msg 在前，data 在后

## 数据库规范
- **SQL目录**：`src/main/resources/sql/`
- **三文件结构**：`001-schema.sql`(表结构+索引)、`002-data.sql`(地图+叙事数据)、`003-updates.sql`(后续更新)
- **执行顺序**：001 → 002 → 003

## 历史审计结论（均已修复）
| 审计类别 | 状态 |
|---------|------|
| 模块边界 | ✅ 非admin模块跨Mapper违规0；通配符import0 |
| 安全审计 | ✅ BCrypt/JWT截断/XSS/SQL注入全量#{} |
| 并发安全 | ✅ ThreadLocalRandom、原子SQL、乐观锁@Version |
| 数据一致性 | ✅ @Transactional rollbackFor、异步事务修复 |
| 异常处理 | ✅ 140+裸异常→BusinessException；error日志带e |
| Admin乱码 | ✅ 36个Java文件全量清零 |
| 全量编译 | ✅ BUILD SUCCESS（2026-04-07，含新增修复） |
| IP黑名单绕过 | ✅ SecurityFilter /api/auth/ 豁免已移除 |
| 反序列化安全 | ✅ LaissezFaireSubTypeValidator → BasicPolymorphicTypeValidator |
| Integer != Bug | ✅ AuctionService 3处 Integer引用比较 → Objects.equals() |
| cancelAuction事务顺序 | ✅ 先原子SQL再扣费 |
| processExpiredAuctions | ✅ 提取 processOneExpiredAuction 独立事务 |
| Magic Number | ✅ PlayerService 提取10个命名常量 |
| LoggingAspect切点 | ✅ 修正为 modules.*.controller/service 路径 |
| tokenCache TTL | ✅ 24h → 2h（与JWT对齐） |

## 重构完成（2026-03-27 ~ 2026-04-07）
- **方法拆分**：CombatService.startCombat、AuctionService.listItem/buyItem、PlayerService.createNewPlayer、GuildService/GuildBossService
- **DTO封装**：ListAuctionRequest、SystemMailRequest
- **Bug修复**：QuestService重复类定义、AuctionService死代码、预存编译错误×4

## 待处理（下一轮审计）
- ⚠️ JavaDoc：部分Service方法缺少注释
- ⚠️ 循环内日志：待扫描 info 级别日志
- ⚠️ Controller @Valid/@Validated：参数校验注解覆盖情况待扫描
- ⚠️ JWT Secret：application-optimized.properties 中硬编码，建议迁移到环境变量

## 前端架构（2026-03-27 完成，100%）
- 平铺40个JS → 分层架构：core(api/storage/utils) + components + modules(20个) + pages
- 新增 ~113 JS、24 CSS、23 HTML；路由系统 Router.js（SPA）
- 关键UI：game.html（背包/商城/任务/宗门/拍卖行/宠物/技能/地图/签到/成就）
- 文档：`docs/frontend-refactoring-guide.md` 等8个文档

*最后更新：2026-04-07*
