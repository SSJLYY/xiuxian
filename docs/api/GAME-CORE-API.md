# 游戏核心 API

> 覆盖：认证系统、玩家/修炼、战斗、技能、装备、背包、商店、离线奖励、任务

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-04-17

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

> 💡 **技术说明**：
> - JWT Token 有效期为 24 小时（86400000 毫秒）
> - Token 使用 HS512 算法签名，密钥长度 512 位
> - Token 中包含 userId、username、role 三个 claims
> - 登出操作会将 Token 加入黑名单（Redis 缓存），直到自然过期
> - 双 Token 系统：游戏端和管理端使用不同的密钥签名，物理隔离

### 安全机制

**密码安全**：
- 密码使用 BCrypt 加密，cost factor 为 10
- 传输过程使用 HTTPS 加密（生产环境）
- 登录失败 5 次后账号锁定 30 分钟

**Token 安全**：
- Token 使用 HttpOnly Cookie 存储（推荐）或 localStorage 存储
- Token 中包含 IP 地址指纹，防止盗用
- 敏感操作（如修改密码）需要重新验证

**频率限制**：
- 注册：同一 IP 每小时最多 5 次
- 登录：同一账号每分钟最多 3 次
- 游戏接口：每账号每秒最多 10 次请求

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
- `1203` ALREADY_CULTIVATING — 已在修炼中
- `1204` NOT_CULTIVATING — 未在修炼中

### 修炼机制详解

**修炼速度计算公式**：
```
实际修炼速度 = 基础速度 × (1 + 灵气浓郁度 + 心境加成 + 功法加成 + 宠物加成)
```

**灵气浓郁度**：
- 新手村：+0%
- 普通秘境：+20%
- 高级秘境：+50%
- 宗门洞天：+100%

**心境加成**：
- 心境平和（HP>80%）：+10%
- 心境紊乱（HP<30%）：-20%

**离线收益**：
- 离线时间上限：24 小时
- 离线收益 = 在线收益 × 50%
- 领取离线奖励后刷新 24 小时计时

### 突破机制详解

**突破成功率**：
```
基础成功率 = 50%
等级修正 = (当前等级 - 突破要求等级) × 5%
心境修正 = 心境值 / 100 × 10%
总成功率 = 基础成功率 + 等级修正 + 心境修正
```

**突破失败惩罚**：
- 损失当前境界 10% 修为
- 心境受损（24 小时内无法再次突破）
- 有小概率走火入魔（随机 attribute 下降）

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

### 战斗机制详解

**回合制战斗流程**：
1. 速度判定：速度快的一方先手
2. 攻击方选择技能（或普通攻击）
3. 伤害计算：`最终伤害 = (攻击方攻击力 - 防御方防御力) × 技能倍率 × 暴击系数`
4. 伤害减免：防御力按 1:1 减免物理伤害
5. 暴击判定：暴击率 = (速度 - 对方速度) / 100 + 基础暴击率 (5%)
6. 闪避判定：闪避率 = 速度差 / 200（最高 20%）
7. 血量归零判定：一方 HP ≤ 0 时战斗结束

**战斗 AI**：
- 玩家：手动选择技能或使用智能战斗（自动释放 CD 最短的技能）
- 怪物：根据血量选择技能（血量<30% 时优先使用治疗技能）

### 伤害计算公式

```
基础伤害 = 攻击力 - 防御力（最小为 1）
技能伤害 = 基础伤害 × 技能倍率
暴击伤害 = 技能伤害 × 2.0
最终伤害 = 技能伤害 × (1 + 暴击率)
```

**属性克制**：
- 金克木：+20% 伤害
- 木克土：+20% 伤害
- 土克水：+20% 伤害
- 水克火：+20% 伤害
- 火克金：+20% 伤害

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

### 技能系统技术说明

**技能升级机制**：
- 技能经验通过使用技能获得
- 升级消耗：`灵石 = 基础消耗 × 当前等级`
- 技能等级上限：10 级
- 技能点获取：境界突破时获得

**连招系统规则**：
- 连招触发：连续使用指定技能组合
- 连招计数：技能使用后5秒内使用下一个技能
- 连招重置：超时未使用技能或战斗结束
- 连招奖励：额外伤害、附加效果、减少 CD

**技能分类**：
| 类型 | 说明 | 示例 |
|------|------|------|
| 主动技能 | 需要手动释放 | 剑气、火球术 |
| 被动技能 | 自动生效 | 暴击提升、速度加成 |
| Buff 技能 | 暂时提升属性 | 狂暴、护盾 |
| Debuff 技能 | 降低敌方属性 | 虚弱、减速 |

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

**强化规则**：
- 强化等级上限：+15
- 强化材料：强化石
- 强化成功率：100%（+1~+5），80%（+6~+10），50%（+11~+15）
- 强化失败：掉级（强化石消失，装备等级-1~-3）
- 强化属性：每件装备部位固定，衣服加防御，武器加攻击等
- 强化转移：装备更换时强化等级保留

### 装备品质与套装

**品质分类**：
| 品质 | 颜色 | 属性加成 | 掉落概率 |
|------|------|---------|---------|
| 白色 | 普通 | 基础属性 | 60% |
| 绿色 | 优秀 | +10% | 25% |
| 蓝色 | 精良 | +20% | 10% |
| 紫色 | 史诗 | +30% | 4% |
| 橙色 | 传说 | +50% | 0.9% |
| 红色 | 神话 | +100% | 0.1% |

**套装效果**：
- 2 件套：激活初级套装效果（+5% 属性）
- 4 件套：激活中级套装效果（+10% 属性）
- 6 件套：激活高级套装效果（+20% 属性 + 特殊效果）

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

### 背包规则

**背包容量**：
- 初始容量：20 格
- 可扩充：最多 100 格
- 扩充消耗：灵石或元宝

**物品堆叠**：
- 消耗品：最多 999 个/格
- 装备：不可堆叠
- 材料：最多 9999 个/格

**物品分类**：
| 分类 | 说明 | 示例 |
|------|------|------|
| 消耗品 | 使用后消失 | 丹药、食物 |
| 装备 | 可装备或强化 | 武器、衣服 |
| 材料 | 用于合成或强化 | 矿石、皮革 |
| 任务物品 | 任务相关 | 信物、卷轴 |
| 其他 | 无法归类 | 宠物蛋、礼包 |

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

### 商城技术说明

**商品刷新机制**：
- 免费刷新：每天 3 次
- 元宝刷新：10 元宝/次
- 刷新池：根据玩家等级动态调整

**限购规则**：
- 每日限购：部分道具每天限购 1-5 次
- VIP 限购：VIP 玩家额外限购次数
- 终身限购：稀有道具终身限购 1 次

**价格波动**：
- 基础价格：系统设定
- 动态折扣：根据玩家活跃度浮动（±20%）
- 批量购买：购买 10 个以上享受 95 折

**货币体系**：
| 货币 | 获取方式 | 用途 |
|------|---------|------|
| 灵石 | 修炼、战斗、任务 | 基础消费 |
| 元宝 | 充值、成就奖励 | 高级消费 |
| 宗门贡献 | 宗门任务、捐献 | 宗门商店 |
| 武道点数 | 武道大会 | 兑换稀有道具 |

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

### 任务系统技术说明

**任务分类**：
| 任务类型 | 说明 | 示例 |
|---------|------|------|
| CULTIVATION | 修炼类任务 | 累计修炼 1 小时、突破到筑基期 |
| COMBAT | 战斗类任务 | 击败 10 个怪物、完成 5 次连招 |
| COLLECTION | 收集类任务 | 收集 10 个铁矿石、寻找 5 株灵草 |
| GROWTH | 成长类任务 | 提升等级到 10 级、学会 3 个技能 |
| DAILY | 每日任务 | 每日登录、每日修炼、每日战斗 |
| ACHIEVEMENT | 成就任务 | 首次突破、首次暴击 999、击败 BOSS |

**任务刷新机制**：
- 主线任务：一次性，完成即消失
- 支线任务：可重复，有冷却时间
- 每日任务：每日凌晨 5 点重置
- 限时活动：活动期间内可重复

**任务奖励计算**：
```
基础奖励 = 任务难度系数 × 玩家等级
难度系数：简单 1.0，普通 1.5，困难 2.0，地狱 3.0
星级奖励 = 基础奖励 × 星级（1-5 星）
额外奖励 = 基础奖励 × VIP 等级 × 10%
总奖励 = 基础奖励 + 星级奖励 + 额外奖励
```

**任务追踪**：
- 自动追踪：任务进度实时更新
- 多任务并行：最多同时追踪 10 个任务
- 任务共享：同类型任务进度共享（如击败怪物同时计入多个任务）

---

## 性能优化建议

**客户端缓存策略**：
- 静态数据（技能、怪物、物品模板）：启动时加载，每日刷新
- 动态数据（玩家属性、背包）：实时请求，30 秒内相同请求缓存
- 战斗日志：分页加载，每页 20 条，最多保留 100 条

**批量操作优化**：
- 批量使用物品：单次请求可使用多个物品
- 批量领取任务奖励：一次性领取多个已完成任务
- 批量出售物品：一次性出售背包中多个物品

**离线同步**：
- 本地缓存：记录最后同步时间戳
- 增量同步：仅拉取变更数据
- 冲突解决：以服务器数据为准，本地提示差异

---

**文档维护说明**：
- 本 API 文档与代码保持同步，每次 API 变更需同时更新文档
- 示例代码可直接复制用于测试
- 如遇 API 与文档不一致，以代码为准，并请提 Issue 通知维护者

*最后更新：2026-04-17*
