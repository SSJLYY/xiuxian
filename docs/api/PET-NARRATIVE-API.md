# 宠物与叙事 API

> 覆盖：宠物系统、宠物进化、NPC、对话、传说图鉴、叙事标记、离线叙事事件

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-24

---

## 宠物系统

### 获取所有宠物模板
```
GET /api/pets
```
**需要认证**

**响应 data：** 宠物模板列表

```json
[
  {
    "petId": 1,
    "name": "小灵猫",
    "type": "灵兽",
    "rarity": "COMMON",
    "captureRate": 80,
    "unlockLevel": 1,
    "baseAttack": 30,
    "baseDefense": 20,
    "baseHp": 150,
    "baseSpeed": 90,
    "growthRate": 1.0,
    "description": "新手宠物，忠实可靠"
  }
]
```

**稀有度说明：**

| 值 | 名称 | 捕获率 | 成长率 |
|----|------|--------|--------|
| COMMON | 普通 | 80% | 1.0 |
| RARE | 稀有 | 60% | 1.2 |
| EPIC | 史诗 | 30-35% | 1.3 |
| LEGENDARY | 传说 | 10-15% | 1.5-1.6 |
| MYTHIC | 神话 | 5% | 2.0 |

---

### 获取可捕获宠物（按玩家等级过滤）
```
GET /api/pets/available
```
**需要认证**

---

### 获取我的宠物列表
```
GET /api/pets/my
```
**需要认证**

**响应 data：**
```json
[
  {
    "playerPetId": 1,
    "petId": 1,
    "name": "小灵猫",
    "nickname": "毛球",
    "level": 5,
    "experience": 120,
    "loyalty": 75,
    "hunger": 80,
    "isActive": true,
    "isLocked": false,
    "attack": 48,
    "defense": 32,
    "hp": 240,
    "speed": 117,
    "winCount": 10,
    "lossCount": 2,
    "skills": [
      { "skillId": 1, "skillName": "撕咬", "level": 2 }
    ]
  }
]
```

---

### 获取出战宠物
```
GET /api/pets/active
```
**需要认证**

---

### 捕获宠物
```
POST /api/pets/capture/{petId}
```
**需要认证**

**路径参数：** `petId` — 宠物模板 ID

**响应 data：**
```json
{
  "success": true,
  "playerPetId": 5,
  "petName": "小灵猫",
  "rarity": "COMMON"
}
```

失败时（随机判定捕获失败）：
```json
{
  "success": false,
  "message": "宠物逃脱了"
}
```

**错误码：**
- `1502` PET_LEVEL_LOCKED — 玩家等级不足
- `1503` PET_ALREADY_MAX — 同种宠物已有3只

---

### 设置出战宠物
```
POST /api/pets/activate/{playerPetId}
```
**需要认证**

---

### 喂食宠物
```
POST /api/pets/feed/{playerPetId}
```
**需要认证**

**响应 data：**
```json
{
  "newHunger": 100,
  "loyaltyGain": 5,
  "newLoyalty": 80
}
```

---

### 训练宠物
```
POST /api/pets/train/{playerPetId}
```
**需要认证**

**请求体：**
```json
{ "trainingType": "攻击" }
```

`trainingType` 可选值：`攻击`、`防御`、`速度`

**错误码：**
- `1504` PET_HUNGER_TOO_LOW — 饱食度低于 20，无法训练

---

### 重命名宠物
```
POST /api/pets/rename/{playerPetId}
```
**请求体：**
```json
{ "nickname": "毛球" }
```

---

### 锁定/解锁宠物
```
POST /api/pets/toggle-lock/{playerPetId}
```

---

### 释放宠物
```
DELETE /api/pets/release/{playerPetId}
```

---

### 获取训练记录
```
GET /api/pets/training-logs/{playerPetId}?limit=10
```

---

## 宠物进化系统

### 检查进化条件
```
GET /api/pets/evolution/check/{playerPetId}
```
**需要认证**

**响应 data：**
```json
{
  "canEvolve": false,
  "conditions": [
    { "condition": "等级", "required": 10, "current": 8, "met": false },
    { "condition": "忠诚度", "required": 80, "current": 75, "met": false },
    { "condition": "进化石", "required": 1, "current": 0, "met": false }
  ]
}
```

---

### 执行宠物进化
```
POST /api/pets/evolution/evolve/{playerPetId}
```
**需要认证**

**响应 data：**
```json
{
  "success": true,
  "newName": "灵猫",
  "newRarity": "RARE",
  "attributeBonus": {
    "attack": 30,
    "defense": 20,
    "hp": 100
  },
  "newSkills": [
    { "skillId": 10, "skillName": "疾风抓" }
  ]
}
```

**错误码：**
- `1505` PET_EVOLUTION_NOT_READY — 未满足进化条件

---

### 获取宠物进化信息
```
GET /api/pets/evolution/info/{playerPetId}
```

---

## NPC 系统

### 获取所有 NPC 列表
```
GET /api/npc/list
```
**需要认证**

**响应 data：**
```json
[
  {
    "npcId": 1,
    "name": "苏玄清",
    "title": "天剑宗掌门",
    "location": "天剑宗",
    "description": "严肃而慈悲的修仙前辈",
    "affinityLevel": 45,
    "isUnlocked": true,
    "availableDialogues": 3
  }
]
```

---

### 获取 NPC 详情
```
GET /api/npc/{npcId}
```
**需要认证**

---

## 对话系统

### 获取对话树（开始对话）
```
GET /api/dialogue/start/{npcId}
```
**需要认证**

**响应 data：**
```json
{
  "dialogueId": "su_first_meet",
  "currentNodeId": "node_001",
  "speakerName": "苏玄清",
  "text": "你便是那位天资卓越的后辈？",
  "emotion": "curious",
  "choices": [
    {
      "choiceId": "c1",
      "text": "正是晚辈，见过掌门师父",
      "requiresAffinity": 0
    },
    {
      "choiceId": "c2",
      "text": "天资算不上，只是比别人更努力些",
      "requiresAffinity": 0
    }
  ]
}
```

---

### 推进对话（选择选项）
```
POST /api/dialogue/choose
```
**需要认证**

**请求体：**
```json
{
  "dialogueId": "su_first_meet",
  "currentNodeId": "node_001",
  "choiceId": "c1"
}
```

**响应 data：** 下一个对话节点（格式同上），当 `choices` 为空时代表对话结束。

**错误码：**
- `3002` DIALOGUE_CONDITION_NOT_MET — 好感度不足，该分支不可选

---

### 获取 NPC 日常对话
```
GET /api/dialogue/daily/{npcId}
```

---

## 传说图鉴

### 获取传说条目列表
```
GET /api/lore/list
```
**需要认证**

**响应 data：**
```json
[
  {
    "loreId": 1,
    "title": "苍玄界的起源",
    "category": "世界",
    "depth": 1,
    "isUnlocked": true,
    "summary": "苍玄界由古仙帝尸骸化成，灵气浓郁...",
    "unlockCondition": "默认解锁"
  }
]
```

**depth 说明：**
- `1` — 表层传说（所有玩家可见）
- `2` — 参与传说（需要探索触发）
- `3` — 深层传说（稀有条件解锁）

---

### 获取传说条目详情
```
GET /api/lore/{loreId}
```

---

### 获取玩家已收集的传说
```
GET /api/lore/my-collection
```

---

## 叙事系统（Narrative）

### 获取玩家叙事标记
```
GET /api/narrative/flags
```
**需要认证**

**响应 data：**
```json
{
  "flags": {
    "met_su_xuan_qing": true,
    "completed_tutorial": true,
    "chose_firm_heart": false
  }
}
```

---

### 获取离线叙事事件
```
GET /api/narrative/offline-events
```
**需要认证**

**响应 data：**
```json
[
  {
    "eventId": 1,
    "type": "encounter",
    "title": "山中奇遇",
    "description": "离线修炼时，你在山林中遇到了一只受伤的神兽幼崽...",
    "reward": {
      "spiritStones": 200,
      "items": []
    },
    "npcDialogue": "看来你与灵兽颇有缘分。"
  }
]
```

---

### 领取离线叙事事件奖励
```
POST /api/narrative/offline-events/{eventId}/claim
```
