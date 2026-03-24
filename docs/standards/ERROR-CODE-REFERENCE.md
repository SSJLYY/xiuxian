# ErrorCode 错误码手册

> 所有业务异常必须通过 `BusinessException(ErrorCode.XXX)` 抛出。  
> 禁止使用裸字符串或裸数字作为错误码。

**作者**: shaun.sheng &nbsp;|&nbsp; **最后更新**: 2026-03-24

---

## 错误码分段规则

| 段 | 范围 | 系统 |
|----|------|------|
| 通用 | 1000–1099 | 通用错误 |
| 用户/认证 | 1100–1199 | 注册、登录、权限 |
| 玩家 | 1200–1299 | 玩家数据、修炼 |
| 战斗 | 1300–1399 | 战斗系统 |
| 技能 | 1400–1499 | 技能学习、使用 |
| 宠物 | 1500–1599 | 宠物捕获、培养 |
| 装备 | 1600–1699 | 装备强化、穿戴 |
| 物品/背包 | 1700–1799 | 背包、物品使用 |
| 任务 | 1800–1899 | 任务领取、进度 |
| 商店 | 1900–1999 | 购买、刷新 |
| 邮件 | 2000–2099 | 邮件、附件 |
| 公告 | 2100–2199 | 公告 |
| 排行榜 | 2200–2299 | 排行榜 |
| 成就 | 2300–2399 | 成就 |
| 宗门 | 2400–2499 | 宗门管理、成员 |
| 拍卖行 | 2500–2599 | 拍卖物品、竞价 |
| VIP | 2600–2699 | VIP等级、充值 |
| 活动 | 2700–2799 | 活动参与、奖励 |
| 礼包码 | 2800–2899 | 礼包码兑换 |
| 叙事系统 | 3000–3099 | NPC、对话、传说 |
| 地图系统 | 3100–3199 | 地图、关卡 |
| 宗门BOSS | 3200–3299 | BOSS挑战、奖励 |
| 签到系统 | 3300–3399 | 签到、奖励 |
| 管理系统 | 9000–9099 | 管理员操作 |

---

## 通用错误码（1000–1099）

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 1000 | `PARAM_ERROR` | 请求参数错误 |
| 1001 | `SYSTEM_ERROR` | 系统内部错误 |
| 1002 | `OPERATION_TOO_FREQUENT` | 操作过于频繁 |
| 1003 | `DATA_NOT_FOUND` | 数据不存在 |
| 1004 | `PERMISSION_DENIED` | 权限不足 |

## 用户/认证（1100–1199）

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 1100 | `USER_NOT_FOUND` | 用户不存在 |
| 1101 | `USER_ALREADY_EXISTS` | 用户名已被注册 |
| 1102 | `EMAIL_ALREADY_EXISTS` | 邮箱已被注册 |
| 1103 | `WRONG_PASSWORD` | 密码错误 |
| 1104 | `TOKEN_EXPIRED` | Token 已过期 |
| 1105 | `TOKEN_INVALID` | Token 无效 |
| 1106 | `USER_BANNED` | 账号已被封禁 |
| 1107 | `ADMIN_NOT_FOUND` | 管理员不存在 |

## 玩家（1200–1299）

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 1200 | `PLAYER_NOT_FOUND` | 玩家档案不存在 |
| 1201 | `INSUFFICIENT_SPIRIT_STONES` | 灵石不足 |
| 1202 | `INSUFFICIENT_CONTRIBUTION` | 贡献点不足 |
| 1203 | `ALREADY_CULTIVATING` | 已在修炼中 |
| 1204 | `NOT_CULTIVATING` | 未在修炼中 |
| 1205 | `BREAKTHROUGH_NOT_READY` | 未达到突破条件 |
| 1206 | `BREAKTHROUGH_COOLDOWN` | 突破冷却中 |
| 1207 | `LEVEL_TOO_LOW` | 等级不足 |

## 战斗（1300–1399）

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 1300 | `MONSTER_NOT_FOUND` | 怪物不存在 |
| 1301 | `ALREADY_IN_COMBAT` | 已在战斗中 |
| 1302 | `COMBAT_COOLDOWN` | 战斗冷却中 |

## 宠物（1500–1599）

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 1500 | `PET_NOT_FOUND` | 宠物不存在 |
| 1501 | `PET_CAPTURE_FAILED` | 宠物捕获失败 |
| 1502 | `PET_LEVEL_LOCKED` | 等级不足，无法解锁该宠物 |
| 1503 | `PET_ALREADY_MAX` | 同种宠物已达上限 |
| 1504 | `PET_HUNGER_TOO_LOW` | 饱食度不足，无法训练 |
| 1505 | `PET_EVOLUTION_NOT_READY` | 未满足进化条件 |

## 叙事系统（3000–3099）

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 3000 | `NPC_NOT_FOUND` | NPC 不存在 |
| 3001 | `DIALOGUE_NOT_FOUND` | 对话节点不存在 |
| 3002 | `DIALOGUE_CONDITION_NOT_MET` | 对话触发条件未满足 |
| 3003 | `LORE_NOT_FOUND` | 传说条目不存在 |
| 3004 | `NARRATIVE_FLAG_NOT_FOUND` | 叙事标记不存在 |
| 3005 | `OFFLINE_EVENT_NOT_FOUND` | 离线事件不存在 |
| 3006 | `AFFINITY_INSUFFICIENT` | 好感度不足 |

## 地图系统（3100–3199）

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 3100 | `MAP_NODE_NOT_FOUND` | 地图节点不存在 |
| 3101 | `MAP_NODE_LOCKED` | 地图节点未解锁 |
| 3102 | `MAP_NODE_ALREADY_ACTIVE` | 已在该地图挂机 |
| 3103 | `MAP_LEVEL_REQUIRED` | 需要更高等级才能进入 |
| 3104 | `MAP_ALREADY_EXPLORING` | 已在探索中 |
| 3105 | `MAP_EXPLORE_COOLDOWN` | 探索冷却中 |

## 宗门 BOSS（3200–3299）

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 3200 | `GUILD_BOSS_NOT_FOUND` | BOSS 不存在 |
| 3201 | `GUILD_BOSS_DEAD` | BOSS 已被击败 |
| 3202 | `GUILD_BOSS_DAILY_LIMIT` | 今日挑战次数已达上限（5次）|
| 3203 | `NOT_IN_GUILD` | 未加入宗门 |
| 3204 | `GUILD_BOSS_COOLDOWN` | BOSS 挑战冷却中 |

## 签到系统（3300–3399）

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 3300 | `ALREADY_CHECKED_IN` | 今日已签到 |
| 3301 | `CHECK_IN_REWARD_NOT_FOUND` | 签到奖励配置不存在 |

---

## 添加新错误码的步骤

1. 确认所属系统段，找到对应范围内的下一个可用值
2. 在 `src/main/java/com/xiuxian/game/exception/ErrorCode.java` 中添加枚举值：
   ```java
   NEW_ERROR(3105, "错误描述信息"),
   ```
3. 在本文档对应段中添加记录
4. 确保 PR 描述中提到了新增的错误码

> 不要在已有段之外随意添加——这会破坏分段约定，让维护者无法快速定位错误来源。
