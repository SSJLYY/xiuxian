# 修仙挂机游戏项目 - 长期记忆

## 项目基本信息
- **技术栈**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1 + Spring Security + JWT + Redis（Lettuce）+ 原生 JS (ES6+)
- **端口**：8082，**数据库**：MySQL 8.0 `xiuxian_game`，**包名**：com.xiuxian.game
- **缓存**：Redis 6.0+（主层）+ ConcurrentHashMap（降级层）
- **规模**：~334 Java 文件，22 业务模块，40 JS，20 HTML

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

## 历史审计结论（均已修复，仅供参考）

### 已完成审计（2026-03-25 ～ 2026-03-27）
| 审计类别 | 结论 |
|---------|------|
| 模块边界 | ✅ 非admin模块跨Mapper违规：0；通配符import：0 |
| 安全审计 | ✅ BCrypt密码、JWT日志截断、XSS修复、SQL注入全量#{} |
| 并发安全 | ✅ ThreadLocalRandom、原子SQL、乐观锁@Version |
| 数据一致性 | ✅ @Transactional rollbackFor、异步事务修复、异常不被吞 |
| 异常处理 | ✅ 140+裸异常→BusinessException、空catch注释、error日志带e |
| Admin乱码 | ✅ 36个Java文件全量清零 |
| 全量编译 | ✅ BUILD SUCCESS（334个Java文件，2026-03-27） |

### 重构已完成（2026-03-27）
- **方法拆分**：CombatService.startCombat（256行拆分）、AuctionService.listItem/buyItem、PlayerService.createNewPlayer、GuildService.createGuild、GuildBossService.challengeBoss/buildBossVO
- **DTO封装**：新增 `ListAuctionRequest`（替代AuctionController内部类）、`SystemMailRequest`（封装6参数）
- **Bug修复**：QuestService删除525-1024行重复类定义

### 代码质量审计（2026-03-27）
- **模块边界**：✅ PASS，非admin模块0违规
- **Controller薄层**：✅ PASS，无业务逻辑泄漏
- **IP获取**：✅ PASS，统一使用 RequestUtils.getClientIp()
- **返回格式**：✅ PASS，统一 ApiResponse<T>
- **JavaDoc**：⚠️ 待深度扫描，部分Service缺少方法级注释
- **Magic Number**：⚠️ 待扫描（已知：5000灵石突破费、70%心魔率等硬编码）
- **循环内日志**：⚠️ 待扫描
- **通配符import**：✅ 已清零（2026-03-25）

*最后更新：2026-03-27*
