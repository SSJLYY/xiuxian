# 修仙挂机游戏项目 - 长期记忆

## 项目基本信息
- **技术栈**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1 + Spring Security + JWT + Redis（Lettuce）+ 原生 JS (ES6+)
- **端口**：8082（本地），8082（Docker）
- **数据库**：MySQL 8.0，数据库名 xiuxian_game
- **缓存**：Redis 6.0+（主层）+ 本地 ConcurrentHashMap（自动降级层），详见 `docs/architecture/CACHE-ARCHITECTURE.md`
- **包名**：com.xiuxian.game
- **规模**：317 Java 文件，44 Controller，50+ Service，62 Mapper，40 JS文件，20 HTML页面

## 架构规范（已确认）
- 统一异常体系：`BusinessException` + `ErrorCode` 枚举，不要用裸 `RuntimeException`
- ErrorCode 分段：通用1000-1099、用户1100-1199、邮件2000-2099、宗门2400-2499、叙事3000-3099、地图3100-3199、宗门BOSS3200-3299、签到3300-3399
- 返回格式：统一 `ApiResponse<T>` 包装
- 日志工具：`LogUtils` 提供结构化日志和 MDC 链路追踪
- IP 解析：统一使用 `RequestUtils.getClientIp()`
- 战斗结果：使用 `CombatResult` DTO，不要用 `Map<String, Object>`
- 双认证系统：游戏端(`/api/auth/*` + `authToken`)和管理端(`/api/admin/auth/*` + `adminToken`)完全独立

## 代码质量规范
- **异常处理器顺序**：子类在前，Exception 兜底在后，绝对不要单独加 RuntimeException 处理器
- **密码安全**：一律用 `passwordEncoder.matches()`，禁止明文比对
- **并发安全**：Service 是单例，随机数用 `ThreadLocalRandom.current()`，不要 `new Random()` 或 `Math.random()`
- **事务边界**：只在真正需要写操作的方法上加 `@Transactional`，不要在读+写混合的服务入口方法上加
- **日志分级**：关键入口/出口用 `info`，中间步骤用 `debug`，不要大段 info 日志
- **日志工具**：使用 `LogUtils` 而非直接 `e.printStackTrace()`

## 关键设计文档（均已迁移到 docs/design/）
- `docs/design/GDD-修仙挂机游戏设计文档.md` — 5大设计支柱、数值公式、玩法机制
- `docs/design/NARRATIVE-DESIGN-DOCUMENT.md` — 苍玄界世界观、6个核心NPC、四幕主线
- `docs/design/LEVEL-DESIGN-DOCUMENT.md` — 地图/关卡设计
- `docs/design/AUDIO-DESIGN-DOCUMENT.md` — 音频规格、Web Audio API实现
- `docs/standards/ART-PIPELINE-STANDARDS.md` — 资产预算、颜色系统（主色调深蓝#1a1a2e、金色#d4af37）

## 团队工作规范（2026-03-24 确认）
1. **新成员入手点**：从 `docs/guides/GETTING-STARTED.md` 开始
2. **写新功能前**：先看 `docs/standards/BACKEND-CODING-STANDARDS.md`
3. **提交PR前**：必须完成 [代码审查标准](./CODE-REVIEW-STANDARDS.md) 中的自查清单
4. **每次 PR**：顺手更新对应的 API 文档（文档欠债会越积越多）
5. **文档作者**：所有文档统一署名 **shaun.sheng**

## 代码审查机制（2026-03-24 建立）
### 优先级定义
| 级别 | 说明 | 处理方式 |
|------|------|----------|
| 🔴 Blocker | 安全漏洞、数据风险、并发问题 | 必须修复才能合并 |
| 🟡 Major | 性能问题、测试缺失、命名混乱 | 应该修复 |
| 💭 Minor | 风格建议、文档缺失 | 可选修复 |

### 审查文档（均已创建）
- `docs/standards/CODE-REVIEW-STANDARDS.md` — 完整检查清单（后端8类+前端3类）
- `docs/standards/CODE-REVIEW-PROCESS.md` — PR流程、角色职责、工具使用
- `docs/standards/CODE-REVIEW-TEMPLATES.md` — 标准化审查评论模板（含Blocker/Major/Minor模板）

### 关键检查点
- 异常处理：必须用 `BusinessException(ErrorCode.XXX)`
- 并发安全：必须用 `ThreadLocalRandom.current()`
- 事务边界：只在原子写操作上加 `@Transactional`
- 日志规范：循环内无 `info`，`error` 必须带异常对象

## 技术文档体系（2026-03-24 建立，同日多次整理完善）

**文档规范（每次新建/修改文档都要检查）**：
1. 所有文档作者统一署名 **shaun.sheng**，格式：`**作者**: shaun.sheng`
2. 根目录只保留 `README.md`，所有技术文档放入 `docs/` 对应子目录
3. 每次新增文档后同步更新 `docs/README.md` 导航表格

完整文档结构（截至 2026-03-24 最新）：

```
docs/
├── README.md                               ← 文档导航索引（唯一入口）
├── guides/
│   ├── GETTING-STARTED.md                  ← 30分钟快速上手（含Redis启动步骤）
│   ├── FRONTEND-GUIDE.md                   ← 前端开发规范
│   └── DEPLOYMENT-CHECKLIST.md             ← 部署前质量检查、配置验证、问题排查
├── architecture/
│   ├── BACKEND-ARCHITECTURE.md             ← 后端分层结构、请求流程
│   ├── DATABASE-DESIGN.md                  ← 完整表结构、数值公式
│   ├── CACHE-ARCHITECTURE.md               ← Redis双层缓存、降级策略
│   └── BACKEND-ARCHITECTURE-EVOLUTION.md   ← 架构演进路线图（缓存→模块化→服务化）
├── api/
│   ├── API-OVERVIEW.md                     ← API通用规范（认证/响应格式）
│   ├── GAME-CORE-API.md                    ← 游戏核心接口
│   ├── PET-NARRATIVE-API.md                ← 宠物与叙事接口
│   └── SOCIAL-ECONOMY-API.md               ← 社交经济接口
├── standards/
│   ├── **CODE-REVIEW-STANDARDS.md**           ← **代码审查标准（检查清单+优先级）**
│   ├── **CODE-REVIEW-PROCESS.md**            ← **代码审查流程（PR+角色职责）**
│   ├── **CODE-REVIEW-TEMPLATES.md**          ← **代码审查模板（Blocker/Major/Minor）**
│   ├── BACKEND-CODING-STANDARDS.md         ← 10条编码规范+PR CheckList
│   ├── ERROR-CODE-REFERENCE.md             ← 全部错误码手册
│   ├── PERFORMANCE-GUIDE.md                ← 性能优化指南（DB索引/N+1/前端懒加载/缓存）
│   ├── OPTIMIZATION-NOTES.md               ← 核心数值表、配置参考、历史Bug修复记录
│   ├── ART-PIPELINE-STANDARDS.md           ← 美术管线规范
│   ├── VFX-OPTIMIZATION-GUIDE.md           ← VFX特效优化指南
│   └── COLOR-AND-ACCESSIBILITY-STANDARDS.md← 颜色与无障碍标准
└── design/
    ├── GDD-修仙挂机游戏设计文档.md           ← 5大支柱、数值公式、玩法机制
    ├── NARRATIVE-DESIGN-DOCUMENT.md         ← 苍玄界世界观、NPC、四幕主线
    ├── LEVEL-DESIGN-DOCUMENT.md             ← 地图/关卡设计
    └── AUDIO-DESIGN-DOCUMENT.md             ← 音频规格、Web Audio API实现
```

## 已实现的游戏系统（截至2026-03-24）
### 后端系统
- 认证/用户/玩家/修炼/战斗/技能/宠物/装备/背包/商店/任务/离线奖励
- 宠物进化（检查条件、执行进化、属性加成、技能解锁）
- 技能连招（3秒时间窗口，序列匹配，连招统计）
- 叙事系统（9张表，NPC/对话树/好感度/传说图鉴/叙事标记/离线事件）
- 宗门BOSS（4种BOSS模板，每日5次，按伤害比例分配奖励）
- 签到系统（11段奖励阶梯，月历展示，里程碑）
- 排行榜/成就/宗门/拍卖行/VIP/活动/礼包码/邮件/公告/配置管理

### 前端系统（40个JS文件）
- audio-engine.js（Web Audio API，程序化音效15种，6层自适应音乐，零音频文件依赖）
- audio-integration.js（零侵入集成，自动挂钩7个控制器）
- audio-settings.js（M静音/Alt+A设置/Alt+D调试HUD）
- game-map.js（世界地图，节点状态，挂机选地图）
- tutorial.js（5步新手引导链）
- pet-hunger-monitor.js（饱食度悬浮监控）
- breakthrough-evolution.js（境界突破+宠物进化UI）
- combo-pokedex.js（技能连招+宠物图鉴）
- narrative.js（NPC对话/传说图鉴/离线事件UI）
- guild-boss.js（BOSS血条动画，伤害飘字）
- checkin.js（月历签到，连续天数里程碑）
- achievement-panel.js（徽章墙+进度过滤）
- mobile-interaction.js（响应式，通知系统，加载状态）
- combat-visual-feedback.js（伤害飘字：金色输出/橙红暴击/红色受伤/绿色治疗）
- performance-tools.js（FPS/内存监控工具栏，开发用）

## 数值公式（已实现）
- 修炼灵石/时：(20 + level×5) × cultivation_speed × realm_bonus（练气1.0/筑基1.5/金丹2.5/元婴4.0）
- 战斗防御率：defense / (defense + attackerLevel×10)
- 暴击：5%概率，1.8倍伤害
- 新手保护：前3场战斗怪物属性-50%
- 宠物战斗：忠诚度影响触发率（60%/80%/100%），饱食度<20降低50%效果
- 境界突破：消耗5000灵石，70%心魔战斗胜率，失败1小时冷却
- 战斗掉落灵石：(10+怪物等级×2) × 类型倍率(普通1/精英2.5/BOSS6) × 等级差修正

## 前端CSS颜色系统（CSS变量）
- `--color-primary: #1a1a2e`（深蓝背景）
- `--color-gold: #d4af37`（金色主强调）
- `--color-aqua: #7fffd4`（淡青次强调）
- `--color-text: #e8e8e8`（主文字）
- 全部符合 WCAG AA 对比度标准

*最后更新：2026-03-24（代码审查机制建立）*
