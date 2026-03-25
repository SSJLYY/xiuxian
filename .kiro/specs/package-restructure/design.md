# 设计文档：包结构重构

## 概述

本次重构将项目从按技术层分类的扁平化结构迁移到按业务模块分类的模块化结构。迁移策略为渐进式：每次迁移一个模块，新旧文件并存，全部完成后统一清理旧文件。

## 架构

### 目标包结构

```
com.xiuxian.game/
├── common/
│   ├── config/        ← 原 config/
│   ├── exception/     ← 原 exception/
│   ├── security/      ← 原 security/
│   ├── annotation/    ← 原 annotation/
│   ├── aspect/        ← 原 aspect/
│   └── util/          ← 原 util/
├── modules/
│   ├── player/        ← 玩家模块
│   ├── combat/        ← 战斗模块
│   ├── cultivation/   ← 修炼模块
│   ├── pet/           ← 宠物模块
│   ├── equipment/     ← 装备模块
│   ├── skill/         ← 技能模块
│   ├── quest/         ← 任务模块
│   ├── achievement/   ← 成就模块
│   ├── guild/         ← 宗门模块
│   ├── ranking/       ← 排行榜模块
│   ├── auction/       ← 拍卖行模块
│   ├── narrative/     ← 叙事模块
│   ├── mail/          ← 邮件模块
│   ├── shop/          ← 商城模块
│   ├── checkin/       ← 签到模块
│   ├── activity/      ← 活动模块
│   ├── giftcode/      ← 礼包码模块
│   ├── offline/       ← 离线收益模块
│   ├── map/           ← 地图模块
│   ├── announcement/  ← 公告模块
│   ├── vip/           ← VIP模块
│   └── admin/         ← 运营管理模块
├── dto/               ← 保持不变（跨模块共用）
└── XiuxianGameApplication.java
```

### 每个模块的子包结构

```
modules/<module>/
├── controller/
├── service/
├── mapper/
└── entity/
```

## 迁移策略

### 迁移步骤（每个文件）

1. 在新路径创建文件
2. 修改第一行 `package` 声明为新路径
3. 更新 `import` 语句，将已迁移类的旧包路径替换为新包路径
4. 保留旧文件（暂时）

### import 更新规则

- 已迁移到 `common` 的类：`com.xiuxian.game.config.*` → `com.xiuxian.game.common.config.*`
- 已迁移到 `modules` 的类：`com.xiuxian.game.entity.Xxx` → `com.xiuxian.game.modules.<module>.entity.Xxx`
- dto 类保持不变：`com.xiuxian.game.dto.*` 不需要更新

### 迁移顺序原则

优先迁移被依赖少的模块，减少 import 更新量：
1. common（基础设施，被所有模块依赖）
2. 独立业务模块（ranking、announcement、map 等依赖少）
3. 复杂业务模块（player、admin 等依赖多）

## 各模块文件归属

### common 模块

| 子包 | 来源 |
|------|------|
| common/config/ | 原 config/ 所有文件 |
| common/exception/ | 原 exception/ 所有文件 |
| common/security/ | 原 security/ 所有文件 |
| common/annotation/ | 原 annotation/ 所有文件 |
| common/aspect/ | 原 aspect/ 所有文件 |
| common/util/ | 原 util/ 所有文件 |

### player 模块

| 层 | 文件 |
|----|------|
| entity | User, PlayerProfile, PlayerItem, PlayerLoginLog |
| mapper | UserMapper, PlayerProfileMapper, PlayerItemMapper, PlayerLoginLogMapper |
| service | AuthService, PlayerService, PlayerLoginLogService, PlayerQueryService, AccountSecurityService |
| controller | AuthController, PlayerController, HomeController, PublicController |

### combat 模块

| 层 | 文件 |
|----|------|
| entity | CombatLog, Monster, MapMonster |
| mapper | CombatLogMapper, MonsterMapper, MapMonsterMapper |
| service | CombatService, EnhancedCombatService |
| controller | CombatController |

### cultivation 模块

| 层 | 文件 |
|----|------|
| entity | CultivationLog |
| mapper | CultivationLogMapper |
| service | （修炼逻辑在 PlayerService 中，无独立 service） |
| controller | （无独立 controller，通过 PlayerController） |

### pet 模块

| 层 | 文件 |
|----|------|
| entity | Pet, PetEvolution, PetSkill, PetTrainingLog, PlayerPet, PlayerPetEvolution, PlayerPetSkill |
| mapper | PetMapper, PetEvolutionMapper, PetSkillMapper, PetTrainingLogMapper, PlayerPetMapper, PlayerPetEvolutionMapper, PlayerPetSkillMapper |
| service | PetService |
| controller | PetController |

### equipment 模块

| 层 | 文件 |
|----|------|
| entity | Equipment, PlayerEquipment |
| mapper | EquipmentMapper, PlayerEquipmentMapper |
| service | EquipmentService, EnhancedInventoryService, InventoryService |
| controller | EquipmentController, InventoryController |

### skill 模块

| 层 | 文件 |
|----|------|
| entity | Skill, SkillCombo, SkillShopItem, PlayerSkill, PlayerSkillComboRecord |
| mapper | SkillMapper, SkillComboMapper, SkillShopMapper, PlayerSkillMapper, PlayerSkillComboRecordMapper |
| service | SkillService, SkillShopService |
| controller | SkillController |

### quest 模块

| 层 | 文件 |
|----|------|
| entity | Quest, PlayerQuest |
| mapper | QuestMapper, PlayerQuestMapper |
| service | QuestService, QuestProgressService |
| controller | QuestController |

### achievement 模块

| 层 | 文件 |
|----|------|
| entity | Achievement, PlayerAchievement |
| mapper | AchievementMapper, PlayerAchievementMapper |
| service | AchievementService |
| controller | AchievementController, AdminAchievementController |

### guild 模块

| 层 | 文件 |
|----|------|
| entity | Guild, GuildMember, GuildApplication, GuildBoss, GuildBossChallenge |
| mapper | GuildMapper, GuildMemberMapper, GuildApplicationMapper, GuildBossMapper, GuildBossChallengeMapper |
| service | GuildService, GuildBossService |
| controller | GuildController, GuildBossController |

### ranking 模块

| 层 | 文件 |
|----|------|
| entity | Ranking |
| mapper | RankingMapper |
| service | RankingService |
| controller | RankingController |

### auction 模块

| 层 | 文件 |
|----|------|
| entity | AuctionItem |
| mapper | AuctionItemMapper |
| service | AuctionService |
| controller | AuctionController |

### narrative 模块

| 层 | 文件 |
|----|------|
| entity | DialogueTree, DialogueNode, Npc, NpcDailyDialogue, LoreEntry, OfflineNarrativeEvent, PlayerDialogueState, PlayerNarrativeFlag, PlayerNpcRelation, PlayerLoreCollection |
| mapper | DialogueTreeMapper, DialogueNodeMapper, NpcMapper, NpcDailyDialogueMapper, LoreEntryMapper, OfflineNarrativeEventMapper, PlayerDialogueStateMapper, PlayerNarrativeFlagMapper, PlayerNpcRelationMapper, PlayerLoreCollectionMapper |
| service | NarrativeService, NpcService, LoreService, OfflineNarrativeService |
| controller | NarrativeController, NpcController, LoreController, DialogueController |

### mail 模块

| 层 | 文件 |
|----|------|
| entity | PlayerMail, MailAttachment |
| mapper | PlayerMailMapper, MailAttachmentMapper |
| service | MailService, AsyncMailService |
| controller | MailController |

### shop 模块

| 层 | 文件 |
|----|------|
| entity | ShopItem, Item |
| mapper | ShopItemMapper, ItemMapper |
| service | ShopService |
| controller | ShopController |

### checkin 模块

| 层 | 文件 |
|----|------|
| entity | PlayerCheckIn |
| mapper | PlayerCheckInMapper |
| service | CheckInService |
| controller | CheckInController |

### activity 模块

| 层 | 文件 |
|----|------|
| entity | Activity, PlayerActivityProgress |
| mapper | ActivityMapper, PlayerActivityProgressMapper |
| service | ActivityService |
| controller | ActivityController |

### giftcode 模块

| 层 | 文件 |
|----|------|
| entity | GiftCode, GiftCodeUsage |
| mapper | GiftCodeMapper, GiftCodeUsageMapper |
| service | GiftCodeService |
| controller | GiftCodeController |

### offline 模块

| 层 | 文件 |
|----|------|
| entity | OfflineReward |
| mapper | OfflineRewardMapper |
| service | OfflineRewardService |
| controller | OfflineRewardController |

### map 模块

| 层 | 文件 |
|----|------|
| entity | GameMap |
| mapper | GameMapMapper |
| service | GameMapService |
| controller | GameMapController |

### announcement 模块

| 层 | 文件 |
|----|------|
| entity | Announcement |
| mapper | AnnouncementMapper |
| service | AnnouncementService |
| controller | AnnouncementController, AdminAnnouncementController |

### vip 模块

| 层 | 文件 |
|----|------|
| entity | VipLevel, PlayerVip, RechargeRecord |
| mapper | VipLevelMapper, PlayerVipMapper, RechargeRecordMapper |
| service | VipService, RechargeService |
| controller | VipController |

### admin 模块

| 层 | 文件 |
|----|------|
| entity | AdminOperationLog, GameConfig, DailyStatistics |
| mapper | AdminOperationLogMapper, GameConfigMapper, DailyStatisticsMapper |
| service | AdminAuthService, AdminDashboardService, AdminPlayerService, AdminStatisticsService, AdminContentService, AdminFeedbackService, AdminMonitoringService, AdminOperationLogService, AntiFraudService, AntiFraudCleanupService, AsyncStatisticsService, CacheService, GameConfigService, LogCleanupService, RateLimiterCleanupService, RedisCacheService, SecurityCleanupService |
| controller | AdminController, AdminAuthController, AdminDashboardController, AdminPlayerController, AdminConfigController, AdminContentController, AdminFeedbackController, AdminGiftCodeController, AdminLogController, AdminMailController, AdminMonitoringController, AdminSecurityController, AdminStatisticsController |

## 正确性属性

*属性是在系统所有有效执行中应保持为真的特征或行为——本质上是关于系统应该做什么的形式化陈述。*

### 属性 1：编译完整性

*对于任意* 迁移状态（部分迁移或全部迁移），项目应始终能通过 `mvn compile` 编译。
**验证：需求 2.1**

### 属性 2：package 声明一致性

*对于任意* 已迁移的文件，其 package 声明应与文件的实际物理路径完全一致。
**验证：需求 2.2**

### 属性 3：业务逻辑不变性

*对于任意* 迁移前后的同一文件，除 package 声明和 import 语句外，所有代码行应完全相同。
**验证：需求 3.1**

### 属性 4：文件归属完整性

*对于任意* 原扁平化结构中的文件，迁移完成后应在且仅在一个目标模块中存在。
**验证：需求 4.1, 4.2**

## 错误处理

- **import 遗漏**：迁移文件时若遗漏更新 import，编译器会报错，按错误提示修复即可
- **循环依赖**：模块间应避免循环依赖，若发现则将共用类提升到 common 或 dto
- **文件遗漏**：每个模块迁移完成后通过编译验证确认无遗漏

## 测试策略

本次重构为纯代码迁移，不涉及业务逻辑变更，测试策略以编译验证为主：

- **编译验证**：每个模块迁移完成后执行 `mvn compile` 确认编译通过
- **单元测试**：若项目有现有单元测试，迁移后执行 `mvn test` 确认测试通过
- **属性验证**：通过代码审查确认 package 声明与文件路径一致、业务逻辑未改变
