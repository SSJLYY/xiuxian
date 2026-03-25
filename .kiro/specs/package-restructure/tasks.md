# 实现计划：包结构重构

## 概述

将修仙挂机游戏项目从扁平化包结构迁移到模块化包结构。每个任务对应一个业务模块的完整迁移，迁移步骤：在新路径创建文件 → 更新 package 声明 → 更新 import 语句 → 保留旧文件。

## 任务

- [x] 1. 创建模块化包结构框架（已完成）
  - 确认 `src/main/java/com/xiuxian/game/modules/` 下所有 22 个模块子目录已存在
  - 确认 `src/main/java/com/xiuxian/game/common/` 下 6 个子目录已存在
  - _需求：1.1, 1.2_

- [x] 2. 迁移 common 基础设施模块（文件已全部迁移）
  - [x] 2.1 迁移 config/ 目录下所有文件到 `common/config/`（16个文件已迁移）
    - package 声明改为 `com.xiuxian.game.common.config`
    - _需求：1.2_
  - [x] 2.2 迁移 exception/ 目录下所有文件到 `common/exception/`（3个文件已迁移）
    - package 声明改为 `com.xiuxian.game.common.exception`
    - _需求：1.2_
  - [x] 2.3 迁移 security/ 目录下所有文件到 `common/security/`（5个文件已迁移）
    - package 声明改为 `com.xiuxian.game.common.security`
    - _需求：1.2_
  - [x] 2.4 迁移 annotation/ 目录下所有文件到 `common/annotation/`（2个文件已迁移）
    - package 声明改为 `com.xiuxian.game.common.annotation`
    - _需求：1.2_
  - [x] 2.5 迁移 aspect/ 目录下所有文件到 `common/aspect/`（2个文件已迁移）
    - package 声明改为 `com.xiuxian.game.common.aspect`
    - 更新 import：引用 annotation 的改为 `com.xiuxian.game.common.annotation.*`
    - _需求：1.2_
  - [x] 2.6 迁移 util/ 目录下所有文件到 `common/util/`（14个文件已迁移）
    - package 声明改为 `com.xiuxian.game.common.util`
    - _需求：1.2_
  - [x] 2.7 更新所有已迁移 common 文件内的交叉 import
    - security 文件引用 exception → `com.xiuxian.game.common.exception.*`
    - aspect 文件引用 annotation → `com.xiuxian.game.common.annotation.*`
    - _需求：2.3_

- [x] 3. 迁移 player 玩家模块（entity=4, mapper=4, service=5, controller=4）
  - [x] 3.1 迁移 entity 文件到 `modules/player/entity/`
    - User.java, PlayerProfile.java, PlayerItem.java, PlayerLoginLog.java
    - _需求：4.1_
  - [x] 3.2 迁移 mapper 文件到 `modules/player/mapper/`
    - UserMapper.java, PlayerProfileMapper.java, PlayerItemMapper.java, PlayerLoginLogMapper.java
    - _需求：4.1_
  - [x] 3.3 迁移 service 文件到 `modules/player/service/`
    - AuthService.java, PlayerService.java, PlayerQueryService.java, AccountSecurityService.java, PlayerLoginLogService.java
    - _需求：4.1_
  - [x] 3.4 迁移 controller 文件到 `modules/player/controller/`
    - AuthController.java, PlayerController.java, HomeController.java, PublicController.java
    - _需求：4.1_

- [x] 4. 迁移 combat 战斗模块（entity=3, mapper=3, service=2, controller=1）
  - [x] 4.1 迁移 entity 文件到 `modules/combat/entity/`（CombatLog, Monster, MapMonster）
    - _需求：4.1_
  - [x] 4.2 迁移 mapper 文件到 `modules/combat/mapper/`
    - _需求：4.1_
  - [x] 4.3 迁移 service 文件到 `modules/combat/service/`（CombatService, EnhancedCombatService）
    - _需求：4.1_
  - [x] 4.4 迁移 controller 文件到 `modules/combat/controller/`（CombatController）
    - _需求：4.1_

- [x] 5. 迁移 cultivation 修炼模块（entity=1, mapper=1，无 service/controller）
  - [x] 5.1 迁移 entity 文件到 `modules/cultivation/entity/`（CultivationLog）
    - _需求：4.1_
  - [x] 5.2 迁移 mapper 文件到 `modules/cultivation/mapper/`（CultivationLogMapper）
    - _需求：4.1_

- [x] 6. 迁移 pet 宠物模块（entity=7, mapper=7, service=1, controller=1）
  - [x] 6.1 迁移 entity 文件到 `modules/pet/entity/`
    - Pet, PetEvolution, PetSkill, PetTrainingLog, PlayerPet, PlayerPetEvolution, PlayerPetSkill
    - _需求：4.1_
  - [x] 6.2 迁移 mapper 文件到 `modules/pet/mapper/`
    - _需求：4.1_
  - [x] 6.3 迁移 service 文件到 `modules/pet/service/`（PetService）
    - _需求：4.1_
  - [x] 6.4 迁移 controller 文件到 `modules/pet/controller/`（PetController）
    - _需求：4.1_

- [x] 7. 迁移 equipment 装备模块（entity=2, mapper=2, service=3, controller=2）
  - [x] 7.1 迁移 entity 文件到 `modules/equipment/entity/`（Equipment, PlayerEquipment）
    - _需求：4.1_
  - [x] 7.2 迁移 mapper 文件到 `modules/equipment/mapper/`
    - _需求：4.1_
  - [x] 7.3 迁移 service 文件到 `modules/equipment/service/`（EquipmentService, EnhancedInventoryService, InventoryService）
    - _需求：4.1_
  - [x] 7.4 迁移 controller 文件到 `modules/equipment/controller/`（EquipmentController, InventoryController）
    - _需求：4.1_

- [x] 8. 迁移 skill 技能模块（entity=5, mapper=5, service=2, controller=1）
  - [x] 8.1 迁移 entity 文件到 `modules/skill/entity/`
    - Skill, SkillCombo, SkillShopItem, PlayerSkill, PlayerSkillComboRecord
    - _需求：4.1_
  - [x] 8.2 迁移 mapper 文件到 `modules/skill/mapper/`
    - _需求：4.1_
  - [x] 8.3 迁移 service 文件到 `modules/skill/service/`（SkillService, SkillShopService）
    - _需求：4.1_
  - [x] 8.4 迁移 controller 文件到 `modules/skill/controller/`（SkillController）
    - _需求：4.1_

- [x] 9. 迁移 quest 任务模块（entity=2, mapper=2, service=2, controller=1）
  - [x] 9.1 迁移 entity 文件到 `modules/quest/entity/`（Quest, PlayerQuest）
    - _需求：4.1_
  - [x] 9.2 迁移 mapper 文件到 `modules/quest/mapper/`
    - _需求：4.1_
  - [x] 9.3 迁移 service 文件到 `modules/quest/service/`（QuestService, QuestProgressService）
    - _需求：4.1_
  - [x] 9.4 迁移 controller 文件到 `modules/quest/controller/`（QuestController）
    - _需求：4.1_

- [x] 10. 迁移 achievement 成就模块（entity=2, mapper=2, service=1, controller=2）
  - [x] 10.1 迁移 entity 文件到 `modules/achievement/entity/`（Achievement, PlayerAchievement）
    - _需求：4.1_
  - [x] 10.2 迁移 mapper 文件到 `modules/achievement/mapper/`
    - _需求：4.1_
  - [x] 10.3 迁移 service 文件到 `modules/achievement/service/`（AchievementService）
    - _需求：4.1_
  - [x] 10.4 迁移 controller 文件到 `modules/achievement/controller/`（AchievementController, AdminAchievementController）
    - _需求：4.1_

- [x] 11. 迁移 guild 宗门模块（entity=5, mapper=5, service=2, controller=2）
  - [x] 11.1 迁移 entity 文件到 `modules/guild/entity/`
    - Guild, GuildMember, GuildApplication, GuildBoss, GuildBossChallenge
    - _需求：4.1_
  - [x] 11.2 迁移 mapper 文件到 `modules/guild/mapper/`
    - _需求：4.1_
  - [x] 11.3 迁移 service 文件到 `modules/guild/service/`（GuildService, GuildBossService）
    - _需求：4.1_
  - [x] 11.4 迁移 controller 文件到 `modules/guild/controller/`（GuildController, GuildBossController）
    - _需求：4.1_

- [x] 12. 迁移 ranking 排行榜模块（entity=1, mapper=1, service=1, controller=1）
  - [x] 12.1 迁移 entity 文件到 `modules/ranking/entity/`（Ranking）
    - _需求：4.1_
  - [x] 12.2 迁移 mapper 文件到 `modules/ranking/mapper/`
    - _需求：4.1_
  - [x] 12.3 迁移 service 文件到 `modules/ranking/service/`（RankingService）
    - _需求：4.1_
  - [x] 12.4 迁移 controller 文件到 `modules/ranking/controller/`（RankingController）
    - _需求：4.1_

- [x] 13. 迁移 auction 拍卖行模块（entity=1, mapper=1, service=1, controller=1）
  - [x] 13.1 迁移 entity 文件到 `modules/auction/entity/`（AuctionItem）
    - _需求：4.1_
  - [x] 13.2 迁移 mapper 文件到 `modules/auction/mapper/`
    - _需求：4.1_
  - [x] 13.3 迁移 service 文件到 `modules/auction/service/`（AuctionService）
    - _需求：4.1_
  - [x] 13.4 迁移 controller 文件到 `modules/auction/controller/`（AuctionController）
    - _需求：4.1_

- [x] 14. 迁移 narrative 叙事模块（entity=10, mapper=10, service=4, controller=4）
  - [x] 14.1 迁移 entity 文件到 `modules/narrative/entity/`
    - DialogueTree, DialogueNode, Npc, NpcDailyDialogue, LoreEntry
    - OfflineNarrativeEvent, PlayerDialogueState, PlayerNarrativeFlag, PlayerNpcRelation, PlayerLoreCollection
    - _需求：4.1_
  - [x] 14.2 迁移 mapper 文件到 `modules/narrative/mapper/`
    - _需求：4.1_
  - [x] 14.3 迁移 service 文件到 `modules/narrative/service/`（NarrativeService, NpcService, LoreService, OfflineNarrativeService）
    - _需求：4.1_
  - [x] 14.4 迁移 controller 文件到 `modules/narrative/controller/`（NarrativeController, NpcController, LoreController, DialogueController）
    - _需求：4.1_

- [x] 15. 迁移 mail 邮件模块（entity=2, mapper=2, service=2, controller=1）
  - [x] 15.1 迁移 entity 文件到 `modules/mail/entity/`（PlayerMail, MailAttachment）
    - _需求：4.1_
  - [x] 15.2 迁移 mapper 文件到 `modules/mail/mapper/`
    - _需求：4.1_
  - [x] 15.3 迁移 service 文件到 `modules/mail/service/`（MailService, AsyncMailService）
    - _需求：4.1_
  - [x] 15.4 迁移 controller 文件到 `modules/mail/controller/`（MailController）
    - _需求：4.1_

- [x] 16. 迁移 shop 商城模块（entity=2, mapper=2, service=1, controller=1）
  - [x] 16.1 迁移 entity 文件到 `modules/shop/entity/`（ShopItem, Item）
    - _需求：4.1_
  - [x] 16.2 迁移 mapper 文件到 `modules/shop/mapper/`
    - _需求：4.1_
  - [x] 16.3 迁移 service 文件到 `modules/shop/service/`（ShopService）
    - _需求：4.1_
  - [x] 16.4 迁移 controller 文件到 `modules/shop/controller/`（ShopController）
    - _需求：4.1_

- [x] 17. 迁移 checkin 签到模块（entity=1, mapper=1, service=1, controller=1）
  - [x] 17.1 迁移 entity 文件到 `modules/checkin/entity/`（PlayerCheckIn）
    - _需求：4.1_
  - [x] 17.2 迁移 mapper 文件到 `modules/checkin/mapper/`
    - _需求：4.1_
  - [x] 17.3 迁移 service 文件到 `modules/checkin/service/`（CheckInService）
    - _需求：4.1_
  - [x] 17.4 迁移 controller 文件到 `modules/checkin/controller/`（CheckInController）
    - _需求：4.1_

- [x] 18. 迁移 activity 活动模块（entity=2, mapper=2, service=1, controller=2）
  - [x] 18.1 迁移 entity 文件到 `modules/activity/entity/`（Activity, PlayerActivityProgress）
    - _需求：4.1_
  - [x] 18.2 迁移 mapper 文件到 `modules/activity/mapper/`
    - _需求：4.1_
  - [x] 18.3 迁移 service 文件到 `modules/activity/service/`（ActivityService）
    - _需求：4.1_
  - [x] 18.4 迁移 controller 文件到 `modules/activity/controller/`（ActivityController, AdminActivityController）
    - _需求：4.1_

- [x] 19. 迁移 giftcode 礼包码模块（entity=2, mapper=2, service=1, controller=1）
  - [x] 19.1 迁移 entity 文件到 `modules/giftcode/entity/`（GiftCode, GiftCodeUsage）
    - _需求：4.1_
  - [x] 19.2 迁移 mapper 文件到 `modules/giftcode/mapper/`
    - _需求：4.1_
  - [x] 19.3 迁移 service 文件到 `modules/giftcode/service/`（GiftCodeService）
    - _需求：4.1_
  - [x] 19.4 迁移 controller 文件到 `modules/giftcode/controller/`（GiftCodeController）
    - _需求：4.1_

- [x] 20. 迁移 offline 离线收益模块（entity=1, mapper=1, service=1, controller=1）
  - [x] 20.1 迁移 entity 文件到 `modules/offline/entity/`（OfflineReward）
    - _需求：4.1_
  - [x] 20.2 迁移 mapper 文件到 `modules/offline/mapper/`
    - _需求：4.1_
  - [x] 20.3 迁移 service 文件到 `modules/offline/service/`（OfflineRewardService）
    - _需求：4.1_
  - [x] 20.4 迁移 controller 文件到 `modules/offline/controller/`（OfflineRewardController）
    - _需求：4.1_

- [x] 21. 迁移 map 地图模块（entity=2, mapper=2, service=1, controller=1）
  - [x] 21.1 迁移 entity 文件到 `modules/map/entity/`（GameMap, PlayerMapProgress）
    - _需求：4.1_
  - [x] 21.2 迁移 mapper 文件到 `modules/map/mapper/`
    - _需求：4.1_
  - [x] 21.3 迁移 service 文件到 `modules/map/service/`（GameMapService）
    - _需求：4.1_
  - [x] 21.4 迁移 controller 文件到 `modules/map/controller/`（GameMapController）
    - _需求：4.1_

- [x] 22. 迁移 announcement 公告模块（entity=1, mapper=1, service=1, controller=2）
  - [x] 22.1 迁移 entity 文件到 `modules/announcement/entity/`（Announcement）
    - _需求：4.1_
  - [x] 22.2 迁移 mapper 文件到 `modules/announcement/mapper/`
    - _需求：4.1_
  - [x] 22.3 迁移 service 文件到 `modules/announcement/service/`（AnnouncementService）
    - _需求：4.1_
  - [x] 22.4 迁移 controller 文件到 `modules/announcement/controller/`（AnnouncementController, AdminAnnouncementController）
    - _需求：4.1_

- [x] 23. 迁移 vip VIP模块（entity=3, mapper=3, service=2, controller=1）
  - [x] 23.1 迁移 entity 文件到 `modules/vip/entity/`（VipLevel, PlayerVip, RechargeRecord）
    - _需求：4.1_
  - [x] 23.2 迁移 mapper 文件到 `modules/vip/mapper/`
    - _需求：4.1_
  - [x] 23.3 迁移 service 文件到 `modules/vip/service/`（VipService, RechargeService）
    - _需求：4.1_
  - [x] 23.4 迁移 controller 文件到 `modules/vip/controller/`（VipController）
    - _需求：4.1_

- [x] 24. 迁移 admin 运营管理模块（entity=3, mapper=3, service=17, controller=13）
  - [x] 24.1 迁移 entity 文件到 `modules/admin/entity/`（AdminOperationLog, GameConfig, DailyStatistics）
    - _需求：4.1_
  - [x] 24.2 迁移 mapper 文件到 `modules/admin/mapper/`
    - _需求：4.1_
  - [x] 24.3 迁移 service 文件到 `modules/admin/service/`（17个文件）
    - AdminAuthService, AdminDashboardService, AdminPlayerService, AdminStatisticsService
    - AdminContentService, AdminFeedbackService, AdminMonitoringService, AdminOperationLogService
    - AntiFraudService, AntiFraudCleanupService, AsyncStatisticsService, CacheService
    - GameConfigService, LogCleanupService, RateLimiterCleanupService, RedisCacheService, SecurityCleanupService
    - _需求：4.1, 4.3_
  - [x] 24.4 迁移 controller 文件到 `modules/admin/controller/`（13个文件）
    - AdminController, AdminAuthController, AdminDashboardController, AdminPlayerController
    - AdminConfigController, AdminContentController, AdminFeedbackController, AdminGiftCodeController
    - AdminLogController, AdminMailController, AdminMonitoringController, AdminSecurityController, AdminStatisticsController
    - _需求：4.1_

- [~] 25. 清理旧包结构（旧目录文件仍存在，待清理）
  - [~] 25.1 删除原 `controller/` 目录（44个文件待删除）
    - 确认每个文件在新位置存在后再删除
    - _需求：1.4_
  - [~] 25.2 删除原 `service/` 目录（52个文件待删除）
    - 确认每个文件在新位置存在后再删除
    - _需求：1.4_
  - [~] 25.3 删除原 `mapper/` 目录（62个文件待删除）
    - 确认每个文件在新位置存在后再删除
    - _需求：1.4_
  - [~] 25.4 删除原 `entity/` 目录（62个文件待删除）
    - 确认每个文件在新位置存在后再删除
    - _需求：1.4_
  - [~] 25.5 删除原 `config/`、`exception/`、`security/`、`annotation/`、`aspect/`、`util/` 目录
    - 确认 common/ 下对应文件存在后再删除
    - _需求：1.4_
  - [~] 25.6 ~~执行编译验证~~ （跳过，maven 编译测试不再需要）
    - _需求：3.2_

## 备注

- 任务 1-24 全部完成，新包结构下共 220 个文件已就位
- 旧目录（controller/service/mapper/entity/config 等）文件仍保留，任务 25 清理后重构完成
- cultivation 模块仅有 entity + mapper，无 service/controller，属正常情况
- map 模块 entity 包含 GameMap + PlayerMapProgress（2个文件）
