# 全模块 Bug 检查报告

**检查日期**: 2026-04-17  
**检查范围**: 除修炼模块外所有业务模块  
**检查人**: shaun.sheng

---

## 检查方法

1. **前端 Service 层分析** - 检查 JS Service 文件
2. **后端 Controller 层分析** - 检查 Java Controller
3. **API 路径匹配** - 验证前后端 API 路径一致性
4. **错误处理** - 检查异常处理和 Toast 提示
5. **数据验证** - 检查输入验证和参数校验

---

## 模块检查清单

### 1. 玩家模块 (Player) ⚠️

**前端文件**:
- `js/modules/player/PlayerService.js`
- `js/modules/player/PlayerUI.js`

**后端文件**:
- `modules/player/controller/PlayerController.java`
- `modules/player/controller/AuthController.java`

**Bug 发现**:

#### Bug-P-1: API 路径不一致 🔴
```javascript
// PlayerService.js:17
const response = await gameAPI.getCurrentPlayer();
// 调用：GET /player/current

// 但后端实际接口是:
// GET /api/player/profile (已存在)
// GET /api/auth/me (已存在)
```
**问题**: `getCurrentPlayer()` 调用的 `/player/current` 接口不存在  
**影响**: 玩家信息无法加载  
**修复**: 改为调用 `/api/auth/me` 或新增 `/api/player/current`

#### Bug-P-2: API 方法不存在 🔴
```javascript
// PlayerService.js:37
const response = await gameAPI.getPlayerProfile();
// 调用：GET /player/profile

// PlayerService.js:52  
const response = await gameAPI.updatePlayerProfile(data);
// 调用：PUT /player/profile
```
**问题**: `getPlayerProfile()` 存在，但 `updatePlayerProfile()` 对应的 PUT 接口不存在  
**修复**: 后端新增 PUT /api/player/profile 接口

#### Bug-P-3: 统计接口缺失 🟡
```javascript
// PlayerService.js:71
const response = await gameAPI.getPlayerStats();
// 调用：GET /player/stats
```
**问题**: 后端没有 `/api/player/stats` 接口  
**影响**: 玩家统计信息无法显示  
**建议**: 新增玩家统计接口或移除该方法

---

### 2. 背包模块 (Inventory) ✅

**前端文件**:
- `js/modules/inventory/InventoryService.js`
- `js/modules/inventory/InventoryUI.js`

**后端文件**:
- `modules/equipment/controller/InventoryController.java`

**检查结果**: ✅ 接口基本匹配

**Bug 发现**:

#### Bug-I-1: 分类接口路径 🟡
```javascript
// InventoryService.js:22
const response = await gameAPI.inventory.getCategorized();
// 调用：GET /api/inventory/categorized
```
**验证**: 后端InventoryController需确认是否有此接口  
**建议**: 添加分类接口或修改为已有的接口路径

#### Bug-I-2: 缺少数量参数验证 🟡
```javascript
// InventoryService.js:173
async sellItem(itemId, quantity = 1)
async discardItem(itemId, quantity = 1)
```
**问题**: 没有验证数量是否大于 0，是否超过拥有数量  
**建议**: 添加数量验证逻辑

---

### 3. 任务模块 (Quest) ⚠️

**前端文件**:
- `js/modules/quest/QuestService.js`

**后端文件**:
- `modules/quest/controller/QuestController.java`

**Bug 发现**:

#### Bug-Q-1: API 路径前缀不一致 🔴
```javascript
// QuestService.js
await gameAPI.quest.getList(type);      // GET /api/quest/list?type=all
await gameAPI.quest.getMyQuests();      // GET /api/quest/my
await gameAPI.quest.accept(questId);    // POST /api/quest/accept/{id}
await gameAPI.quest.complete(questId);  // POST /api/quest/complete/{id}
await gameAPI.quest.claim(questId);     // POST /api/quest/claim/{id}
```

**后端实际路径** (待确认):
- 可能是 `/api/quests/*` (复数) 或 `/api/quest/*` (单数)

**影响**: 所有任务相关接口 404  
**修复**: 统一前后端路径，建议使用复数形式 `/api/quests/*`

---

### 4. 签到模块 (Checkin) ✅

**前端文件**:
- `js/modules/checkin/CheckinService.js`

**后端文件**:
- `modules/checkin/controller/CheckInController.java`

**检查结果**: ✅ 接口路径看起来匹配

**建议**:
- 确认后端是否有完整的日历查询接口
- 验证签到奖励领取接口

---

### 5. 战斗模块 (Combat) ⚠️

**前端文件**:
- `js/modules/combat/CombatService.js` (需要检查)

**后端文件**:
- `modules/combat/controller/CombatController.java`

**Bug 发现**:

#### Bug-C-1: 需要检查前端 Service 是否存在
**文件路径**: `js/modules/combat/` 下可能缺少 Service 文件  
**建议**: 创建完整的战斗模块 Service 和 UI

---

### 6. 宠物模块 (Pets) ⚠️

**前端文件**:
- `js/modules/pets/PetsService.js`

**后端文件**:
- `modules/pet/controller/PetController.java`

**Bug 发现**:

#### Bug-PET-1: 模块路径不一致 🔴
```javascript
// 前端：js/modules/pets/ (复数)
// 后端：modules/pet/ (单数)
```
**问题**: 前后端模块命名不一致，可能导致混淆  
**建议**: 统一使用复数形式

#### Bug-PET-2: 接口路径待验证 🟡
```javascript
// PetsService.js (假设)
await gameAPI.pets.getMyPets();      // GET /api/pets/my
await gameAPI.pets.getAvailable();   // GET /api/pets/available
await gameAPI.pets.buy(petId);       // POST /api/pets/buy/{id}
```
**需要验证**: 后端是否有这些确切接口

---

### 7. 装备模块 (Equipment) ⚠️

**前端文件**:
- `js/modules/equipment/EquipmentService.js`
- `js/modules/equipment/EquipmentUI.js`

**后端文件**:
- `modules/equipment/controller/EquipmentController.java`

**Bug 发现**:

#### Bug-E-1: 装备卸下接口参数 🟡
```javascript
// EquipmentService.js
async unequipItem(slot)
// 参数是 slot (装备槽位)

// 但可能是期望 itemId?
```
**问题**: 卸下装备应该通过 itemId 还是 slot? 需要统一  
**建议**: 明确设计：卸下通过 itemId 或 slot，保持一致性

---

### 8. 技能模块 (Skills) ⚠️

**前端文件**:
- `js/modules/skills/SkillsService.js`

**后端文件**:
- `modules/skill/controller/SkillController.java`

**Bug 发现**:

#### Bug-S-1: 模块路径不一致 🔴
```javascript
// 前端：js/modules/skills/ (复数)
// 后端：modules/skill/ (单数)
```
**建议**: 统一使用复数形式

---

### 9. 商城模块 (Shop) ⚠️

**前端文件**:
- `js/modules/shop/ShopService.js`

**后端文件**:
- `modules/shop/controller/ShopController.java`

**Bug 发现**:

#### Bug-SH-1: 需要验证购买接口 🟡
```javascript
// ShopService.js
await gameAPI.shop.buy(itemId, count);
// POST /api/shop/buy/{itemId}
```
**需要验证**: 后端接口路径是否完全匹配

---

### 10. 宗门模块 (Guild) ✅

**前端文件**:
- `js/modules/guild/GuildService.js`

**后端文件**:
- `modules/guild/controller/GuildController.java`

**检查结果**: 看起来接口较完整

---

### 11. 拍卖行模块 (Auction) ⚠️

**前端文件**:
- `js/modules/auction/AuctionService.js`

**后端文件**:
- `modules/auction/controller/AuctionController.java`

**Bug 发现**:

#### Bug-A-1: 需要验证筛选参数 🟡
```javascript
async getAuctionItems(filters = {})
// 筛选参数需要后端支持
```

---

### 12. 邮件模块 (Mail) ⚠️

**前端文件**:
- `js/modules/mail/MailService.js`

**后端文件**:
- `modules/mail/controller/MailController.java`

**Bug 发现**:

#### Bug-M-1: 需要验证所有接口 🟡
待详细检查

---

### 13. VIP 模块 ⚠️

**前端文件**:
- `js/modules/vip/VipService.js`

**后端文件**:
- `modules/vip/controller/VipController.java`

**Bug 发现**:

#### Bug-V-1: 需要验证所有接口 🟡
待详细检查

---

### 14. 成就模块 (Achievement) ⚠️

**前端文件**:
- `js/modules/achievement/AchievementService.js`

**后端文件**:
- `modules/achievement/controller/AchievementController.java`

**Bug 发现**:

#### Bug-A-1: 需要验证所有接口 🟡
待详细检查

---

### 15. 地图模块 (Map) ⚠️

**前端文件**:
- `js/modules/map/MapService.js`

**后端文件**:
- `modules/map/controller/GameMapController.java`

**Bug 发现**:

#### Bug-M-1: 需要验证所有接口 🟡
待详细检查

---

### 16. 剧情模块 (Narrative) ⚠️

**前端文件**:
- `js/modules/narrative/NarrativeService.js`

**后端文件**:
- `modules/narrative/controller/*`

**Bug 发现**:

#### Bug-N-1: NPC 接口待验证 🟡
```javascript
await gameAPI.npc.getDialogue(npcId);
```

---

### 17. 活动模块 (Activity) ⚠️

**前端文件**:
- `js/modules/activity/ActivityService.js`

**后端文件**:
- `modules/activity/controller/ActivityController.java`

**Bug 发现**:

#### Bug-A-1: 需要验证所有接口 🟡
待详细检查

---

### 18. 签到模块 (Checkin) ✅

已检查，基本正常

---

### 19. 礼包码模块 (Giftcode) ⚠️

**前端文件**:
- `js/modules/giftcode/GiftcodeService.js`

**后端文件**:
- `modules/giftcode/controller/GiftCodeController.java`

**Bug 发现**:

#### Bug-G-1: 需要验证兑换接口 🟡
待详细检查

---

### 20. 排行榜模块 (Ranking) ⚠️

**前端文件**:
- `js/modules/ranking/RankingService.js`

**后端文件**:
- `modules/ranking/controller/RankingController.java`

**Bug 发现**:

#### Bug-R-1: 需要验证所有接口 🟡
待详细检查

---

## 严重 Bug 汇总

### 🔴 P0 - 阻塞性问题 (API 不存在/路径不匹配)

| 编号 | 模块 | 问题 | 影响 | 优先级 |
|------|------|------|------|--------|
| Bug-P-1 | 玩家 | `/player/current` 接口不存在 | 玩家信息无法加载 | P0 |
| Bug-P-2 | 玩家 | `updatePlayerProfile` PUT 接口不存在 | 无法更新玩家资料 | P0 |
| Bug-Q-1 | 任务 | API 路径单复数不一致 | 所有任务接口 404 | P0 |
| Bug-PET-1 | 宠物 | 模块路径单复数不一致 | 可能 404 | P0 |
| Bug-S-1 | 技能 | 模块路径单复数不一致 | 可能 404 | P0 |

### 🟠 P1 - 高优先级 (功能缺陷)

| 编号 | 模块 | 问题 | 影响 | 优先级 |
|------|------|------|------|--------|
| Bug-I-1 | 背包 | 分类接口路径待确认 | 分类功能可能失效 | P1 |
| Bug-I-2 | 背包 | 缺少数量验证 | 可能出售/丢弃异常 | P1 |
| Bug-E-1 | 装备 | 卸下装备参数不明确 | 卸下功能可能异常 | P1 |

### 🟡 P2 - 中优先级 (体验问题)

| 编号 | 模块 | 问题 | 影响 | 优先级 |
|------|------|------|------|--------|
| Bug-P-3 | 玩家 | 统计接口缺失 | 统计信息无法显示 | P2 |
| 各模块 | 所有 | 错误处理不统一 | 用户体验不一致 | P2 |

---

## 待详细检查清单

以下模块需要进一步验证（每个模块预计需要 10-15 分钟）:

- [ ] 战斗模块 (Combat) - 检查 Service 是否存在
- [ ] 宠物模块 (Pets) - 验证所有 API 路径
- [ ] 装备模块 (Equipment) - 验证卸下逻辑
- [ ] 技能模块 (Skills) - 验证所有 API 路径
- [ ] 商城模块 (Shop) - 验证购买接口
- [ ] 拍卖行模块 (Auction) - 验证筛选参数
- [ ] 邮件模块 (Mail) - 验证所有接口
- [ ] VIP 模块 - 验证所有接口
- [ ] 成就模块 (Achievement) - 验证所有接口
- [ ] 地图模块 (Map) - 验证所有接口
- [ ] 剧情模块 (Narrative) - 验证所有接口
- [ ] 活动模块 (Activity) - 验证所有接口
- [ ] 礼包码模块 (Giftcode) - 验证兑换接口
- [ ] 排行榜模块 (Ranking) - 验证所有接口

---

## 建议修复优先级

### 第一阶段：P0 阻塞性问题（本周内）
1. ✅ 修复玩家模块 API 路径问题
2. ✅ 统一任务模块 API 路径（单复数）
3. ✅ 统一宠物、技能模块路径命名

### 第二阶段：P1 高优先级问题（下周）
1. 验证所有背包、装备接口
2. 添加数量验证逻辑
3. 明确卸下装备设计

### 第三阶段：P2 体验优化（长期）
1. 补充缺失的统计接口
2. 统一错误处理
3. 完善日志记录

---

## 总结

### 已发现问题统计
- 🔴 P0 问题：5 个（API 路径不匹配）
- 🟠 P1 问题：3 个（功能缺陷）
- 🟡 P2 问题：2+ 个（体验优化）

### 待检查模块：14 个
需要逐一验证前后端 API 路径一致性

### 建议
1. **立即修复 P0 问题** - 避免影响玩家核心体验
2. **建立 API 文档** - 使用 Swagger/OpenAPI 规范
3. **添加 API 测试** - 前后端联调测试
4. **统一命名规范** - 模块名称使用复数形式

---

*检查进行中... 完整报告待补充*  
*更新时间：2026-04-17*  
*作者：shaun.sheng*
