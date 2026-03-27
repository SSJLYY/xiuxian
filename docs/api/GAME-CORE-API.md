# 游戏核心 API

> 覆盖：认证系统、玩家/修炼、战斗、技能、装备、背包、商店、离线奖励、任务

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-27

---

## 认证系统

### 玩家注册
```
POST /api/auth/register
```
**无需认证**

**请求体：**
```json
{
  "username": "testuser",
  "password": "testpass123",
  "email": "test@example.com",
  "nickname": "青云剑客"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | ✅ | 用户名，4-20位字母数字 |
| password | string | ✅ | 密码，6-32位 |
| email | string | ✅ | 邮箱 |
| nickname | string | ✅ | 游戏昵称，2-16位 |

**响应 data：**
```json
{
  "userId": 1,
  "username": "testuser",
  "nickname": "青云剑客",
  "message": "注册成功，已获得新手礼包"
}
```

---

### 玩家登录
```
POST /api/auth/login
```
**无需认证**

**请求体：**
```json
{
  "username": "testuser",
  "password": "testpass123"
}
```

**响应 data：**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 1,
  "nickname": "青云剑客",
  "expiresIn": 86400000
}
```

---

### 登出
```
POST /api/auth/logout
```
**需要认证**

**响应 data：** `null`

---

### 获取当前用户信息
```
GET /api/auth/me
```
**需要认证**

**响应 data：**
```json
{
  "userId": 1,
  "username": "testuser",
  "nickname": "青云剑客",
  "email": "test@example.com",
  "role": "PLAYER",
  "createdAt": "2026-01-01T00:00:00"
}
```

---

## 玩家与修炼

### 获取玩家档案
```
GET /api/player/profile
```
**需要认证**

**响应 data：**
```json
{
  "playerId": 1,
  "nickname": "青云剑客",
  "level": 5,
  "experience": 3200,
  "realm": "练气期",
  "realmLevel": 5,
  "spiritStones": 2000,
  "attack": 120,
  "defense": 80,
  "hp": 500,
  "maxHp": 500,
  "mp": 200,
  "maxMp": 200,
  "speed": 100,
  "cultivationSpeed": 1.0,
  "isCultivating": false,
  "cultivationStartTime": null,
  "combatPower": 1500
}
```

---

### 开始修炼
```
POST /api/player/cultivate
```
**需要认证**

**响应 data：**
```json
{
  "startTime": "2026-03-24T10:00:00",
  "cultivationSpeed": 1.2,
  "expectedExpPerHour": 360
}
```

**错误码：**
- `1203` ALREADY_CULTIVATING — 已在修炼中

---

### 停止修炼
```
POST /api/player/stop-cultivate
```
**需要认证**

**响应 data：**
```json
{
  "duration": 3600,
  "gainedExp": 360,
  "gainedSpiritStones": 120,
  "currentLevel": 5
}
```

**错误码：**
- `1204` NOT_CULTIVATING — 未在修炼中

---

### 获取离线奖励
```
GET /api/player/offline-rewards
```
**需要认证**

**响应 data：**
```json
{
  "offlineDuration": 28800,
  "gainedExp": 8640,
  "gainedSpiritStones": 2880,
  "maxDuration": 86400
}
```

---

### 领取离线奖励
```
POST /api/player/claim-offline-rewards
```
**需要认证**

**响应 data：** 同上（实际发放的奖励）

---

### 境界突破

#### 检查是否可突破
```
GET /api/player/breakthrough/can
```
**需要认证**

**响应 data：**
```json
{
  "canBreakthrough": true,
  "currentRealm": "练气期",
  "nextRealm": "筑基期",
  "requiredLevel": 10,
  "requiredSpiritStones": 5000,
  "currentSpiritStones": 6000
}
```

#### 执行突破
```
POST /api/player/breakthrough
```
**需要认证**

**响应 data：**
```json
{
  "success": true,
  "newRealm": "筑基期",
  "attributeBonus": {
    "attack": 50,
    "defense": 30,
    "hp": 200
  },
  "nextRetryTime": null
}
```

失败时：
```json
{
  "success": false,
  "message": "心魔战斗失败，道心受损",
  "nextRetryTime": "2026-03-24T11:00:00"
}
```

**错误码：**
- `1205` BREAKTHROUGH_NOT_READY — 未达到突破条件
- `1206` BREAKTHROUGH_COOLDOWN — 突破冷却中

---

## 战斗系统

### 获取可战斗怪物列表
```
GET /api/combat/monsters
```
**需要认证**

**响应 data：** 怪物列表（按等级过滤适合玩家当前境界的）

```json
[
  {
    "monsterId": 1,
    "name": "妖狐",
    "level": 3,
    "type": "NORMAL",
    "attack": 50,
    "defense": 20,
    "hp": 200,
    "expReward": 80,
    "spiritStonesReward": 15
  }
]
```

---

### 发起战斗
```
POST /api/combat/battle/{monsterId}
```
**需要认证**

**路径参数：**
- `monsterId` — 怪物 ID

**响应 data：**
```json
{
  "win": true,
  "rounds": 5,
  "playerHpRemaining": 320,
  "gainedExp": 80,
  "gainedSpiritStones": 18,
  "droppedItems": [
    { "itemId": 5, "itemName": "妖狐皮", "quantity": 1 }
  ],
  "battleLog": [
    { "round": 1, "attacker": "player", "damage": 78, "isCritical": false, "targetHp": 122 },
    { "round": 1, "attacker": "monster", "damage": 35, "isCritical": false, "targetHp": 465 }
  ]
}
```

**新手保护机制**：前 3 场战斗怪物属性自动降低 50%。

---

### 获取战斗日志
```
GET /api/combat/logs?limit=20
```
**需要认证**

---

## 技能系统

### 获取所有可学技能
```
GET /api/skills
```
**需要认证**

### 获取玩家已学技能
```
GET /api/skills/player
```
**需要认证**

### 学习技能
```
POST /api/skills/learn/{skillId}
```
**需要认证**

### 使用技能
```
POST /api/skills/{playerSkillId}/use
```
**需要认证**

### 升级技能
```
POST /api/skills/{playerSkillId}/upgrade
```
**需要认证**

### 技能连招

#### 获取可用连招
```
GET /api/skills/combos/available
```

#### 获取所有激活连招
```
GET /api/skills/combos/all
```

#### 连招统计
```
GET /api/skills/combos/stats
```

#### 检测连招触发
```
POST /api/skills/combos/check
```
**请求体：**
```json
{ "skillId": 3 }
```
**响应 data：**
```json
{
  "triggered": true,
  "comboName": "剑气纵横",
  "bonusDamage": 150,
  "comboDescription": "连续使用剑气三次触发"
}
```

---

## 装备系统

### 获取玩家装备
```
GET /api/equipment
```

### 装备物品
```
POST /api/equipment/equip/{itemId}
```

### 卸下装备
```
POST /api/equipment/unequip/{itemId}
```

### 强化装备
```
POST /api/equipment/enhance/{equipmentId}
```
**响应 data：**
```json
{
  "success": true,
  "enhanceLevel": 5,
  "attackBonus": 20,
  "cost": 500
}
```

---

## 背包系统

### 获取背包物品
```
GET /api/inventory/items
```

### 使用物品
```
POST /api/inventory/use/{itemId}
```
**请求体：**
```json
{ "quantity": 1 }
```

---

## 商城系统

### 获取商店物品
```
GET /api/shop/items?shopType=ITEM
```

`shopType` 可选值：`ITEM`（道具）、`EQUIPMENT`（装备）

### 购买物品
```
POST /api/shop/buy/{shopItemId}
```
**请求体：**
```json
{ "quantity": 1 }
```

### 获取技能商店
```
GET /api/shop/skills
```

### 购买技能
```
POST /api/shop/buy-skill/{skillId}
```

---

## 任务系统

### 获取玩家任务列表
```
GET /api/quests
```

### 领取任务奖励
```
POST /api/quests/{playerQuestId}/claim
```

### 更新任务进度
```
POST /api/quests/progress/by-type
```
**请求体：**
```json
{
  "questType": "COMBAT",
  "amount": 1
}
```
`questType` 可选值：`CULTIVATION`、`COMBAT`、`COLLECTION`、`GROWTH`
