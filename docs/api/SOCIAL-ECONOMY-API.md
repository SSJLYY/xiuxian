# 社交与经济 API

> 覆盖：宗门、宗门BOSS、排行榜、成就、拍卖行、签到、邮件、公告、活动、礼包码、VIP

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-27

---

## 宗门系统

### 创建宗门
```
POST /api/guild/create
```
**需要认证**

**请求体：**
```json
{
  "name": "青云宗",
  "description": "以剑入道，心向苍天",
  "joinType": "APPLY"
}
```
`joinType`: `OPEN`（自由加入）、`APPLY`（需审核）

---

### 搜索宗门
```
GET /api/guild/search?keyword=青云&page=1&size=10
```

---

### 获取宗门详情
```
GET /api/guild/{guildId}
```

---

### 申请加入宗门
```
POST /api/guild/{guildId}/apply
```

---

### 退出宗门
```
POST /api/guild/quit
```

---

### 宗门捐献
```
POST /api/guild/donate
```
**请求体：**
```json
{ "spiritStones": 1000 }
```

---

## 宗门 BOSS

### 获取当前宗门 BOSS
```
GET /api/guild/boss/current
```
**需要认证**

**响应 data：**
```json
{
  "bossId": 1,
  "name": "妖王蚩尤",
  "level": 20,
  "currentHp": 45000,
  "maxHp": 100000,
  "type": "ELITE",
  "todayChallengeCount": 2,
  "maxDailyChallenges": 5,
  "remainingChallenges": 3,
  "resetTime": "2026-03-25T00:00:00"
}
```

---

### 挑战宗门 BOSS
```
POST /api/guild/boss/challenge
```
**需要认证**

**响应 data：**
```json
{
  "damage": 1580,
  "bossHpRemaining": 43420,
  "bossDefeated": false,
  "reward": null,
  "battleLog": [ ... ]
}
```

BOSS 被击败时：
```json
{
  "damage": 5000,
  "bossHpRemaining": 0,
  "bossDefeated": true,
  "reward": {
    "spiritStones": 5000,
    "contributionPoints": 200,
    "items": [ { "itemName": "BOSS晶核", "quantity": 1 } ]
  }
}
```

**错误码：**
- `3202` GUILD_BOSS_DAILY_LIMIT — 今日已挑战 5 次
- `3203` NOT_IN_GUILD — 未加入宗门
- `3201` GUILD_BOSS_DEAD — BOSS 已被击败

---

### 获取宗门 BOSS 历史战报
```
GET /api/guild/boss/history?limit=10
```

---

## 排行榜

### 等级排行榜
```
GET /api/ranking/level?page=1&size=20
```
**无需认证**

**响应 data：**
```json
[
  {
    "rank": 1,
    "playerId": 5,
    "nickname": "剑帝",
    "level": 20,
    "realm": "元婴期",
    "combatPower": 15000
  }
]
```

---

### 战力排行榜
```
GET /api/ranking/power?page=1&size=20
```

### 财富排行榜
```
GET /api/ranking/wealth?page=1&size=20
```

### 我的排名
```
GET /api/ranking/my-rank
```
**需要认证**

**响应 data：**
```json
{
  "levelRank": 45,
  "powerRank": 38,
  "wealthRank": 120
}
```

---

## 成就系统

### 获取成就列表
```
GET /api/achievement/list?category=COMBAT
```
**需要认证**

`category` 可选：`ALL`、`COMBAT`、`CULTIVATION`、`PET`、`SOCIAL`

### 领取成就奖励
```
POST /api/achievement/{achievementId}/claim
```

### 获取成就进度
```
GET /api/achievement/progress
```

---

## 签到系统

### 签到
```
POST /api/checkin/do
```
**需要认证**

**响应 data：**
```json
{
  "consecutiveDays": 7,
  "reward": {
    "spiritStones": 500,
    "exp": 300,
    "items": [ { "itemName": "突破丹", "quantity": 1 } ]
  },
  "nextReward": {
    "day": 8,
    "spiritStones": 600,
    "exp": 350
  }
}
```

**错误码：**
- `3300` ALREADY_CHECKED_IN — 今日已签到

---

### 获取签到状态
```
GET /api/checkin/status
```
**需要认证**

**响应 data：**
```json
{
  "checkedInToday": false,
  "consecutiveDays": 6,
  "currentMonthCalendar": [
    { "day": 1, "checkedIn": true },
    { "day": 2, "checkedIn": true },
    ...
  ],
  "milestones": [
    { "day": 7, "reward": "突破丹×1", "reached": false },
    { "day": 14, "reward": "神兽召唤符×1", "reached": false },
    { "day": 30, "reward": "传说宠物蛋×1", "reached": false }
  ]
}
```

---

## 邮件系统

### 获取邮件列表
```
GET /api/mail/list?page=1&size=20
```

### 获取邮件详情
```
GET /api/mail/{mailId}
```

### 领取邮件附件
```
POST /api/mail/{mailId}/claim
```

### 删除邮件
```
DELETE /api/mail/{mailId}
```

### 获取未读邮件数量
```
GET /api/mail/unread-count
```

---

## 公告系统

### 获取公告列表
```
GET /api/announcement/list?page=1&size=10
```
**无需认证**

### 获取公告详情
```
GET /api/announcement/{id}
```

### 标记公告已读
```
POST /api/announcement/{id}/read
```
**需要认证**

---

## 活动系统

### 获取活动列表
```
GET /api/activity/list
```

### 获取活动详情
```
GET /api/activity/{activityId}
```

### 参与活动
```
POST /api/activity/{activityId}/join
```

### 领取活动奖励
```
POST /api/activity/{activityId}/claim-reward
```

---

## 礼包码

### 兑换礼包码
```
POST /api/gift-code/redeem
```
**需要认证**

**请求体：**
```json
{ "code": "XIUXIAN2026" }
```

### 获取兑换历史
```
GET /api/gift-code/history
```

---

## 拍卖行

### 获取拍卖列表
```
GET /api/auction/list?itemType=EQUIPMENT&page=1&size=20
```

### 上架物品
```
POST /api/auction/sell
```
**请求体：**
```json
{
  "itemId": 15,
  "startPrice": 500,
  "buyoutPrice": 2000,
  "durationHours": 24
}
```

### 购买物品
```
POST /api/auction/{auctionId}/buy
```

### 取消拍卖
```
DELETE /api/auction/{auctionId}
```

### 我的拍卖
```
GET /api/auction/my-sales
```

---

## VIP 系统

### 获取 VIP 信息
```
GET /api/vip/info
```

**响应 data：**
```json
{
  "vipLevel": 2,
  "totalRecharge": 648,
  "privileges": [
    "修炼速度+20%",
    "每日额外签到奖励",
    "商店9折优惠"
  ],
  "nextLevelRequiredRecharge": 1298,
  "dailyRewardAvailable": true
}
```

### 获取 VIP 特权列表
```
GET /api/vip/privileges
```

### 领取 VIP 每日奖励
```
POST /api/vip/daily-reward
```

### 充值
```
POST /api/vip/recharge
```
**请求体：**
```json
{ "amount": 648, "paymentMethod": "ALIPAY" }
```
