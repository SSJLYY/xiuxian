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
- 事务：只在原子写操作加 `@Transactional`，读写混合入口不加
- 日志：循环内无 `info`，`error` 必带异常对象

## 技术文档体系
- 根目录只留 `README.md`；所有技术文档在 `docs/`；作者统一署名 **shaun.sheng**
- 新增文档后同步更新 `docs/README.md` 导航表格
- 关键文档：`docs/design/GDD-*.md`、`docs/architecture/BACKEND-ARCHITECTURE.md`、`docs/standards/BACKEND-CODING-STANDARDS.md`、`docs/standards/CODE-REVIEW-*.md`

## 基础设施优化（2H4G 服务器适配）
- 连接池：max-pool-size=10，min-idle=3
- Redis 连接池：max-active=15，max-idle=8
- TTL：rankingCache 3min，auctionCache 30s，configCache 2h
- Sentinel 限流：战斗30QPS，核心50QPS，排行榜20QPS，拍卖行15QPS；熔断：异常比例50%
- JVM：-Xms512m -Xmx1024m，-XX:+UseG1GC，-XX:MaxGCPauseMillis=200

## 模块边界重构（2026-03-25，持续进行中）

### 已新增的 Service 接口方法
| Service | 新增方法 | 供哪个模块使用 |
|---------|----------|----------------|
| `PlayerService` | getPlayerProfileById/savePlayerProfile/背包CRUD/统计/反作弊/排行榜 | 所有需要玩家数据的模块 |
| `ItemService`（shop模块） | getItemById | equipment、pet、combat 模块 |
| `CombatService` | getMapMonsters/getMonsterById | map 模块 |
| `MailService` | getFeedbackList/getFeedbackById/markFeedbackAsRead/deleteFeedback/replyToFeedback | admin 模块 |
| `SkillService` | getSkillById/getPlayerSkillByPlayerAndSkill | combat(EnhancedCombat) 模块 |
| `EquipmentService` | grantEquipmentDirectly | mail 模块（奖励发放） |

### 已修复的跨模块 Mapper 违规（本轮全量）
- `CombatService`、`QuestService`、`AchievementService`、`RankingService`、`ShopService`
- `SkillService`、`SkillShopService`（含重复字段BUG）、`RechargeService`/`VipService`
- `CheckInService`、`GiftCodeService`、`GuildService`
- `OfflineNarrativeService`、`OfflineRewardService`
- `EquipmentService`、`InventoryService`、`EnhancedInventoryService`
- `GameMapService`（MapMonsterMapper/MonsterMapper→CombatService）
- `AdminFeedbackService`（PlayerMailMapper→MailService）
- `AntiFraudService`（PlayerLoginLogMapper/UserMapper→PlayerService）
- `NarrativeService`、`PetService`（cross-module mappers→PlayerService/ItemService）
- `MailService`（PlayerProfileMapper/PlayerItemMapper/PlayerEquipmentMapper → Service接口）
- `EnhancedCombatService`（PlayerProfileMapper/PlayerSkillMapper/SkillMapper/ItemMapper/PlayerItemMapper→Service接口）
- `GuildBossService`（PlayerProfileMapper→PlayerService）

### 待处理 / 已知状态
- `AsyncStatisticsService`（admin）— `RechargeRecordMapper`(vip) **HAS FIELD VIOLATION**，需修复
- `AdminPlayerService`（admin）— `PlayerProfileMapper`/`UserMapper` — admin层务实例外，可保留
- `AdminStatisticsService`、`AdminContentService`、`AdminDashboardService` — 通配符import，admin层例外，低优先级
- `AuctionService` — 通配符 import + 跨模块 Mapper（PlayerProfileMapper/PlayerItemMapper/PlayerEquipmentMapper/PlayerPetMapper/ItemMapper/EquipmentMapper/PetMapper）**已修复（2026-03-25）**
- `AsyncStatisticsService` — RechargeRecordMapper(vip) **已修复**，改为通过 `RechargeService.getSuccessRechargesByDateRange()`
- `AdminContentService`、`AdminDashboardService`、`AdminPlayerService`、`AdminStatisticsService` — admin 聚合层务实例外，保留直接 Mapper 访问；**通配符 import 已全部清理为精确 import（2026-03-25）**

### PowerShell 处理注意事项
- 中文路径 + `ReadAllText(..., UTF8)` 会乱码，用 `Get-Item(".\relative\path").FullName` 获取路径可避免
- PS1 heredoc `@"..."@` 在 Windows 有 `\r\n` vs `\n` 问题，多行替换优先用 `replace_in_file`
- 单行 ASCII 替换可用 PS1 `.Replace()`；中文路径在 PS1 文件中使用字面量会被 GBK 解析，需走 Get-Item 相对路径

### 新增 Service 接口方法（本轮）
| Service | 新增方法 | 供哪个模块使用 |
|---------|----------|----------------|
| `RechargeService` | getSuccessRechargesByDateRange | admin(AsyncStatistics) |
| `EquipmentService` | getEquipmentById/getPlayerEquipmentById/deletePlayerEquipment | auction 模块 |
| `PetService` | getPetById/getPlayerPetById/grantPetDirectly/deletePlayerPet | auction 模块 |

### 模块边界重构最终状态（2026-03-25，全量完成）
- **非 admin 模块 HAS FIELD - VIOLATION: 0**（全部清零）
- **通配符 mapper import: 0**（所有 Service 文件均已改为精确 import）
- admin 聚合层（4个服务：AdminContentService/AdminDashboardService/AdminPlayerService/AdminStatisticsService）保留直接 Mapper 访问，记录为务实例外；通配符 import 已清理完毕
- `PlayerService → PlayerItemMapper` 是合理的（PlayerItemMapper 在 player 包内，非跨模块）
- 扫描工具：`scan_modules.ps1`（基于 import 语句精确判断）

## 数值公式
- 修炼灵石/时：(20+level×5)×speed×realm_bonus（练气1.0/筑基1.5/金丹2.5/元婴4.0）
- 防御率：defense/(defense+attackerLevel×10)；暴击：5%概率×1.8倍
- 境界突破：消耗5000灵石，70%心魔战胜率，失败1h冷却
- 战斗掉落：(10+怪物Lv×2)×类型倍率(普通1/精英2.5/BOSS6)×等级差修正

## 前端 CSS 颜色系统
- `--color-primary: #1a1a2e`（深蓝背景）、`--color-gold: #d4af37`（金色）
- `--color-aqua: #7fffd4`（淡青）、`--color-text: #e8e8e8`（主文字）

## 项目编码规范（基础规范，必须遵守）
- **全项目统一 UTF-8 无 BOM 编码**，覆盖所有文件类型：`.java`、`.yml`、`.yaml`、`.properties`、`.xml`、`.sql`、`.conf`、`.sh`、`.bat`
- **已完成全量转换（2026-03-25）**：346 个文件全部验证为有效 UTF-8（无 BOM = 0，非 UTF-8 = 0）
- **历史问题**：项目曾混用 GBK + UTF-8 BOM 两种编码，导致大量"未结束的字符串文字"编译错误；已全部修复
- **新建文件必须用 UTF-8 无 BOM**；IDE 需配置 File Encoding = UTF-8，不勾选 "Add BOM"
- **pom.xml 须声明**：`<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>` + `<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>`
- PowerShell 批量编码处理脚本逻辑：用 `[System.IO.File]::ReadAllBytes()` + 严格 UTF-8 解码检测，BOM 去除用字节截断（`$rawBytes[3..($rawBytes.Length-1)]`）而非 Encoding API
- Maven 编译参数：`-Dfile.encoding=UTF-8`（已在 settings.xml 的 JDK8 profile 中配置）

## Admin 模块乱码修复（2026-03-26）
- **修复范围**：`modules/admin` 目录下 36 个 Java 文件，全部清零乱码
- **修复文件数**：36 个文件（含 controller 13 个、service 15 个、mapper 2 个、entity 3 个 + 其他 3 个）
- **乱码类型**：① 注释行乱码（已删除乱码注释行）② 第一行 package 前缀乱码（已重建 package 声明）③ 字符串字面量乱码（log/return/throw/message 中的中文替换为合理占位文本）
- **修复方式**：Python 脚本分批完整重写（fix_entities.py、fix_batch.py、fix_admin_final.py）
- **最终状态**：扫描 36 文件，乱码行 = 0，All clean（2026-03-26 二轮扫描确认）
- **本轮新修复（2026-03-26 第二轮）**：AdminAuthService、AdminMonitoringService、AdminMailController、AdminMonitoringController、AdminPlayerController、AdminSecurityController、AdminStatisticsController（共 7 个文件，全量重写，JavaDoc 注释、字符串字面量全部替换为规范中文）

*最后更新：2026-03-26（admin 模块 36 文件乱码全量清零，二轮确认）*
