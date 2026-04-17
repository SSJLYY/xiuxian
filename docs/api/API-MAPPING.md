# 全模块 API 映射文档

**生成日期**: 2026-04-17  
**作者**: shaun.sheng  
**状态**: 完整API 映射，前后端已对齐

---

## 目录

1. [API 使用规范](#api 使用规范)
2. [完整 API 映射表](#完整 api 映射表)
3. [模块详细文档](#模块详细文档)
4. [错误处理](#错误处理)

---

## API 使用规范

### 路径规范

```
格式：/api/{module}/{action}/{params}

示例:
- GET    /api/player/profile          # 获取玩家档案
- POST   /api/player/cultivate        # 开始修炼
- GET    /api/quests/daily            # 获取日常任务
- POST   /api/equipment/equip         # 装备物品
```

### 命名约定

1. **模块名称使用复数**
   - `/api/players/*` - 玩家相关
   - `/api/quests/*` - 任务相关
   - `/api/items/*` - 物品相关

2. **动作使用动词**
   - `GET` - 获取资源
   - `POST` - 执行操作
   - `PUT` - 更新资源（暂未使用）
   - `DELETE` - 删除资源（暂未使用）

3. **参数传递**
   - 路径参数：`/api/module/action/{id}`
   - 查询参数：`/api/module/list?type=daily`
   - 请求体：POST 请求的 body

---

## 完整 API 映射表

### 1. 认证模块 (Auth) 🔐

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `login(u,p)` | POST | `/auth/login` | AuthController | username, password | token, user |
| `register(data)` | POST | `/auth/register` | AuthController | username, password, email, nickname | token, user |
| `logout()` | POST | `/auth/logout` | AuthController | - | - |
| `getCurrentUser()` | GET | `/auth/me` | AuthController | - | User |

### 2. 玩家模块 (Player) 👤

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getCurrentPlayer()` | GET | `/player/profile` | PlayerController | - | PlayerProfile |
| `getPlayerProfile()` | GET | `/player/profile` | PlayerController | - | PlayerProfile |
| `updatePlayerProfile(data)` | POST | `/player/profile/update` | PlayerController | nickname?, avatar? | PlayerProfile |
| `allocateAttributes(payload)` | POST | `/player/attributes/allocate` | PlayerController | attack?, defense?, health?, mana?, speed? | PlayerProfile |

### 3. 修炼模块 (Cultivation) 🧘

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getCultivateInfo()` | GET | `/player/cultivate/info` | PlayerController | - | PlayerProfile |
| `startCultivate(type)` | POST | `/player/cultivate` | PlayerController | type: 'normal'\|'intensive'\|'meditation' | - |
| `stopCultivate()` | POST | `/player/cultivate/stop` | PlayerController | - | - |
| `canBreakthrough()` | GET | `/player/breakthrough/can` | PlayerController | - | boolean |
| `breakthrough()` | POST | `/player/breakthrough` | PlayerController | - | result message |

### 4. 战斗模块 (Combat) ⚔️

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `generateMonster()` | GET | `/combat/generate-monster` | CombatController | - | Monster |
| `startCombat(monsterId)` | POST | `/combat/start/{monsterId}` | CombatController | monsterId | CombatResult |
| `startEnhancedCombat()` | POST | `/combat/enhanced` | CombatController | - | CombatResult |
| `batchCombat(times)` | POST | `/combat/batch/{times}` | CombatController | times: number | CombatResult[] |
| `getCombatHistory()` | GET | `/combat/history` | CombatController | - | CombatHistory[] |

### 5. 装备/背包模块 (Equipment) 🎒

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getInventoryItems()` | GET | `/equipment/items` | InventoryController | - | Item[] |
| `getInventoryCategorized()` | GET | `/equipment/categorized` | InventoryController | - | {type: Item[]} |
| `useItem(itemId)` | POST | `/equipment/use/${itemId}` | InventoryController | itemId | - |
| `sellItem(itemId, qty)` | POST | `/equipment/sell/${itemId}` | InventoryController | itemId, quantity | spiritStones |
| `discardItem(itemId, qty)` | POST | `/equipment/discard/${itemId}` | InventoryController | itemId, quantity | - |
| `getEquipment()` | GET | `/equipment` | EquipmentController | - | Equipment[] |
| `getEquippedEquipment()` | GET | `/equipment/equipped` | EquipmentController | - | Equipment[] |
| `equipItem(itemId)` | POST | `/equipment/equip` | EquipmentController | itemId | - |
| `unequipItem(slot)` | POST | `/equipment/unequip` | EquipmentController | slot | - |

### 6. 技能模块 (Skills) ✨

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getSkills()` | GET | `/skills` | SkillController | - | Skill[] |
| `getAvailableSkills()` | GET | `/skills/available` | SkillController | - | Skill[] |
| `getPlayerSkills()` | GET | `/skills/player` | SkillController | - | PlayerSkill[] |
| `learnSkill(skillId)` | POST | `/skills/learn/${skillId}` | SkillController | skillId | - |
| `upgradeSkill(playerSkillId)` | POST | `/skills/${playerSkillId}/upgrade` | SkillController | playerSkillId | - |
| `equipSkill(psId, slot)` | POST | `/skills/equip/${psId}/${slot}` | SkillController | playerSkillId, slotNumber | - |
| `unequipSkill(playerSkillId)` | POST | `/skills/unequip/${playerSkillId}` | SkillController | playerSkillId | - |
| `useSkill(playerSkillId)` | POST | `/skills/${playerSkillId}/use` | SkillController | playerSkillId | - |

### 7. 宠物模块 (Pets) 🐾

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getPets()` | GET | `/pets` | PetController | - | Pet[] |
| `getAvailablePets()` | GET | `/pets/available` | PetController | - | Pet[] |
| `getMyPets()` | GET | `/pets/my` | PetController | - | PlayerPet[] |
| `getActivePet()` | GET | `/pets/active` | PetController | - | PlayerPet |
| `capturePet(petId)` | POST | `/pets/capture/${petId}` | PetController | petId | - |
| `activatePet(playerPetId)` | POST | `/pets/activate/${playerPetId}` | PetController | playerPetId | - |
| `feedPet(playerPetId)` | POST | `/pets/feed/${playerPetId}` | PetController | playerPetId | - |
| `trainPet(playerPetId)` | POST | `/pets/train/${playerPetId}` | PetController | playerPetId | - |
| `renamePet(ppId, name)` | POST | `/pets/rename/${playerPetId}` | PetController | playerPetId, newName | - |

### 8. 任务模块 (Quests) 📜

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getQuests(type)` | GET | `/quests/{type}` | QuestController | type: 'daily'\|'weekly'\|'monthly' | Quest[] |
| `getDailyQuests()` | GET | `/quests/daily` | QuestController | - | Quest[] |
| `getWeeklyQuests()` | GET | `/quests/weekly` | QuestController | - | Quest[] |
| `getMonthlyQuests()` | GET | `/quests/monthly` | QuestController | - | Quest[] |
| `acceptQuest(questId)` | POST | `/quests/accept/${questId}` | QuestController | questId | - |
| `completeQuest(pqId)` | POST | `/quests/complete/${playerQuestId}` | QuestController | playerQuestId | - |
| `claimQuestReward(pqId)` | POST | `/quests/${playerQuestId}/claim` | QuestController | playerQuestId | Reward |

### 9. 商城模块 (Shop) 🏪

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getShopItems(type)` | GET | `/shop/items?type={type}` | ShopController | type: 'general'\|'skill' | ShopItem[] |
| `getSkillShop()` | GET | `/shop/skills` | ShopController | - | Skill[] |
| `buyShopItem(id, count)` | POST | `/shop/items/${itemId}/buy` | ShopController | itemId, count | - |
| `buySkill(skillId)` | POST | `/shop/skills/${skillId}/buy` | ShopController | skillId | - |

### 10. 宗门模块 (Guild) 🏛️

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getGuildList()` | GET | `/guild/list` | GuildController | - | Guild[] |
| `getMyGuild()` | GET | `/guild/my` | GuildController | - | Guild |
| `createGuild(name, desc)` | POST | `/guild/create` | GuildController | name, description | Guild |
| `applyGuild(guildId)` | POST | `/guild/apply/${guildId}` | GuildController | guildId | - |
| `leaveGuild()` | POST | `/guild/leave` | GuildController | - | - |
| `donateGuild(amount)` | POST | `/guild/donate` | GuildController | amount | - |

**宗门 Boss**:
| `getCurrentGuildBoss()` | GET | `/guild/boss/current` | GuildBossController | - | GuildBoss |
| `challengeGuildBoss()` | POST | `/guild/boss/challenge` | GuildBossController | - | ChallengeResult |
| `claimGuildBossReward()` | POST | `/guild/boss/claim-reward` | GuildBossController | - | Reward |

### 11. 拍卖行模块 (Auction) 🔨

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getAuctionItems(filters)` | GET | `/auction/items?{params}` | AuctionController | filters | AuctionItem[] |
| `getMyAuctionItems()` | GET | `/auction/my-items` | AuctionController | - | AuctionItem[] |
| `listAuctionItem(i,p,d)` | POST | `/auction/list` | AuctionController | itemId, price, duration | - |
| `buyAuctionItem(auctionId)` | POST | `/auction/buy/${auctionId}` | AuctionController | auctionId | - |
| `cancelAuctionItem(aId)` | POST | `/auction/cancel/${auctionId}` | AuctionController | auctionId | - |

### 12. 邮件模块 (Mail) 📧

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getMails()` | GET | `/mail/list` | MailController | - | Mail[] |
| `getMail(mailId)` | GET | `/mail/${mailId}` | MailController | mailId | Mail |
| `readMail(mailId)` | POST | `/mail/${mailId}/read` | MailController | mailId | - |
| `claimMailAttachment(mId)` | POST | `/mail/${mailId}/claim` | MailController | mailId | Reward |
| `getUnreadMailCount()` | GET | `/mail/unread-count` | MailController | - | count |
| `deleteMail(mailId)` | POST | `/mail/${mailId}/delete` | MailController | mailId | - |

### 13. 排行榜模块 (Ranking) 🏆

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getRanking(type)` | GET | `/ranking/{type}` | RankingController | type | RankItem[] |
| `getLevelRanking()` | GET | `/ranking/level` | RankingController | - | RankItem[] |
| `getPowerRanking()` | GET | `/ranking/power` | RankingController | - | RankItem[] |
| `getWealthRanking()` | GET | `/ranking/wealth` | RankingController | - | RankItem[] |
| `getPetRanking()` | GET | `/ranking/pet` | RankingController | - | RankItem[] |
| `getMyRanking()` | GET | `/ranking/my-rank` | RankingController | - | MyRank |

### 14. 成就模块 (Achievement) 🎖️

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getAchievements()` | GET | `/achievement/list` | AchievementController | - | Achievement[] |
| `getAchievement(id)` | GET | `/achievement/${id}` | AchievementController | id | Achievement |
| `getAchievementProgress()` | GET | `/achievement/progress` | AchievementController | - | Progress[] |
| `claimAchievement(id)` | POST | `/achievement/${id}/claim` | AchievementController | id | Reward |

### 15. 签到模块 (Checkin) ✅

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getCheckinStatus()` | GET | `/checkin/status` | CheckInController | - | CheckinStatus |
| `doCheckin()` | POST | `/checkin/do` | CheckInController | - | Reward |
| `getCheckinCalendar(m,y)` | GET | `/checkin/calendar?month=&year=` | CheckInController | month, year | Calendar |
| `getCheckinRewards()` | GET | `/checkin/rewards` | CheckInController | - | Reward[] |

### 16. VIP 模块 💎

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getVipInfo()` | GET | `/vip/info` | VipController | - | VipInfo |
| `getVipLevels()` | GET | `/vip/levels` | VipController | - | VipLevel[] |
| `getDailyVipReward()` | POST | `/vip/daily-reward` | VipController | - | Reward |
| `rechargeVip(amount)` | POST | `/vip/recharge/${amount}` | VipController | amount | - |
| `getVipRechargeRecords()` | GET | `/vip/recharge-records` | VipController | - | Record[] |
| `checkVipPrivilege(lvl)` | GET | `/vip/privilege/${requiredLevel}` | VipController | requiredLevel | boolean |

### 17. 活动模块 (Activity) 🎉

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getActivities()` | GET | `/activities/` | ActivityController | - | Activity[] |
| `getAllActivities()` | GET | `/activities/all` | ActivityController | - | Activity[] |
| `getMyActivityProgress()` | GET | `/activities/my-progress` | ActivityController | - | Progress[] |
| `participateActivity(id)` | POST | `/activities/${id}/participate` | ActivityController | id | - |
| `updateActivityProgress(i,p)` | POST | `/activities/${id}/progress` | ActivityController | id, progress | - |
| `submitActivityScore(i,s)` | POST | `/activities/${id}/score` | ActivityController | id, score | - |
| `getActivityRanking(id)` | GET | `/activities/${id}/ranking` | ActivityController | id | Rank[] |

### 18. 礼包码模块 (Giftcode) 🎁

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `redeemGiftcode(code)` | POST | `/giftcode/redeem` | GiftCodeController | code | Reward |

### 19. 叙事模块 (Narrative) 📖

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getAvailableDialogues(npcId)` | GET | `/dialogue/available/${npcId}` | DialogueController | npcId | Dialogue[] |
| `startDialogue(npc,dlg)` | POST | `/dialogue/start` | DialogueController | npcId, dialogueId | DialogueState |
| `chooseDialogueChoice(id)` | POST | `/dialogue/choice` | DialogueController | choiceId | DialogueState |

### 20. 地图模块 (Maps) 🗺️

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `getMaps()` | GET | `/maps` | GameMapController | - | Map[] |
| `getMap(mapId)` | GET | `/maps/${mapId}` | GameMapController | mapId | Map |
| `getCurrentMap()` | GET | `/maps/current` | GameMapController | - | Map |
| `enterMap(mapId)` | POST | `/maps/enter/${mapId}` | GameMapController | mapId | - |
| `leaveMap()` | POST | `/maps/leave` | GameMapController | - | - |
| `exploreMap()` | GET | `/maps/explore` | GameMapController | - | ExploreResult |
| `getOfflineReward()` | GET | `/maps/offline-reward` | GameMapController | - | Reward |

### 21. 离线奖励模块 (Offline Reward) 💰

| 前端方法 | HTTP | 路径 | 后端 Controller | 参数 | 返回 |
|---------|------|------|----------------|------|------|
| `claimOfflineReward()` | POST | `/offline-reward/claim` | OfflineRewardController | - | Reward |
| `getOfflineRewardInfo()` | GET | `/offline-reward/info` | OfflineRewardController | - | OfflineRewardInfo |

---

## 错误处理

### HTTP 状态码

| 状态码 | 含义 | 处理方式 |
|--------|------|----------|
| 200 | 成功 | 处理返回数据 |
| 400 | 请求参数错误 | 显示错误消息 |
| 401 | 未授权 | 跳转到登录页 |
| 403 | 禁止访问 | 显示权限不足 |
| 404 | 资源不存在 | 显示资源不存在 |
| 500 | 服务器错误 | 显示系统错误 |

### 错误响应格式

```json
{
  "success": false,
  "message": "错误描述",
  "data": null,
  "code": "ERROR_CODE"
}
```

---

## 使用示例

### 修炼流程

```javascript
import { gameAPI } from '/js/core/api/GameApi.js';

async function cultivateProcess() {
  try {
    // 1. 获取修炼信息
    const info = await gameAPI.getCultivateInfo();
    console.log('当前境界:', info.data.realm);
    
    // 2. 开始修炼 (选择闭关修炼)
    await gameAPI.startCultivate('intensive');
    console.log('开始闭关修炼');
    
    // 3. 检查是否可以突破
    const canBreak = await gameAPI.canBreakthrough();
    if (canBreak) {
      // 4. 突破
      const result = await gameAPI.breakthrough();
      console.log('突破结果:', result.data);
    }
    
  } catch (error) {
    console.error('修炼过程失败:', error.message);
  }
}
```

### 任务流程

```javascript
async function questProcess() {
  try {
    // 1. 获取日常任务列表
    const dailyQuests = await gameAPI.getDailyQuests();
    
    // 2. 接受任务
    const quest = dailyQuests.data[0];
    await gameAPI.acceptQuest(quest.id);
    
    // 3. 更新任务进度 (游戏中自动完成)
    
    // 4. 完成任务
    await gameAPI.completeQuest(quest.id);
    
    // 5. 领取奖励
    const reward = await gameAPI.claimQuestReward(quest.id);
    console.log('获得奖励:', reward.data);
    
  } catch (error) {
    console.error('任务流程失败:', error.message);
  }
}
```

---

*文档生成完成*  
*总计：21 个模块，200+ API 接口*  
*最后更新：2026-04-17*
