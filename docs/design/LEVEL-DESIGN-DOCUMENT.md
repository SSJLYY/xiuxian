# 修仙挂机游戏 - 关卡设计文档 (LDD)

**版本**: v1.0  
**日期**: 2026-03-23  
**作者**: shaun.sheng  
**状态**: 设计完成 - 待实现

---

## 目录

1. [设计意图](#1-设计意图)
2. [世界地图架构](#2-世界地图架构)
3. [关卡节奏设计](#3-关卡节奏设计)
4. [环境叙事规格](#4-环境叙事规格)
5. [难度曲线设计](#5-难度曲线设计)
6. [技术实现规格](#6-技术实现规格)
7. [数据库表结构](#7-数据库表结构)
8. [关卡数据填充](#8-关卡数据填充)

---

## 1. 设计意图

### 1.1 核心设计目标

> **让每个地图都成为"值得记住的地方"**

- **导航可理解性**：玩家始终知道自己在哪、该去哪、怎么去
- **节奏控制**：通过地图设计控制游戏张力曲线
- **环境叙事**：无需文字，场景本身讲述世界故事
- **选择重量**：安全vs风险、效率vs探索的真实权衡

### 1.2 与GDD支柱的对齐

| GDD支柱 | 关卡设计支撑 |
|---------|-------------|
| 每次上线有新鲜感 | 地图解锁进度+随机遭遇事件 |
| 选择有重量 | 多条路线（安全/风险/隐藏） |
| 成长有节奏感 | 清晰的地图里程碑+突破仪式感 |
| 挂机有意义 | 离线地图影响收益类型 |
| 社交有杠杆 | 组队副本+宗门领地战 |

### 1.3 情感弧线地图

```
青云镇      天剑宗      荒野        妖兽林      万法阁      上古秘境
(安全/归属) → (秩序/成长) → (危险/机遇) → (恐惧/共生) → (知识/选择) → (超越/传承)
   好奇        挑战         迷惘         顿悟         责任         超越
```

---

## 2. 世界地图架构

### 2.1 地图层级总览

```
苍玄界
├── 新手区域（练气期）
│   ├── 青云镇 [SAFE] — 出生点
│   ├── 后山 [NORMAL] — 练气1-3
│   ├── 天剑宗外门 [SAFE] — 城镇
│   ├── 试剑峰 [NORMAL] — 练气4-7
│   └── 荒野外围 [NORMAL] — 练气8-10
│
├── 进阶区域（筑基期）
│   ├── 荒野深处 [DANGEROUS] — 筑基1-3
│   ├── 妖兽林外围 [DANGEROUS] — 筑基1-3
│   ├── 妖兽林深处 [DANGEROUS] — 筑基4-5
│   └── 山贼营地 [DUNGEON] — 副本
│
├── 高阶区域（金丹期）
│   ├── 黑雾深谷 [DANGEROUS] — 金丹1-2
│   ├── 邪修密地 [DUNGEON] — 副本
│   └── 万法阁 [SAFE] — 可选宗门
│
└── 终极区域（元婴期）
    ├── 上古秘境 [BOSS] — 元婴
    └── 天堑之巅 [FINAL] — 结局
```

### 2.2 地图类型定义

| 类型 | 代码 | 特征 | 离线风险 |
|------|------|------|---------|
| 安全区 | SAFE | 无战斗，恢复状态 | 无风险 |
| 普通区 | NORMAL | 标准怪物，稳定收益 | 无风险 |
| 危险区 | DANGEROUS | 精英怪，高收益 | 可能受伤损失 |
| 副本 | DUNGEON | 多波次战斗，一次性奖励 | 不可离线 |
| BOSS区 | BOSS | 单人/团队BOSS | 不可离线 |

### 2.3 地图解锁流程

```
[创建角色]
    ↓
[青云镇] — 自动解锁，新手引导
    ↓ 完成"初次修炼"任务
[后山] — 练气1层解锁
    ↓ 达到练气3层
[天剑宗外门] — 苏玄清引导
    ↓ 达到练气4层
[试剑峰] — 剑无痕试炼
    ↓ 达到练气7层+通过试炼
[荒野外围] — 解锁
    ↓ 达到练气10层+突破筑基
[妖兽林外围] — 自动解锁
    ↓ 筑基期探索
[妖兽林深处] — 筑基4层解锁
    ↓ 达到金丹期
[黑雾深谷] — 解锁
    ↓ 金丹期任务
[万法阁] — 可选加入
    ↓ 达到元婴期
[上古秘境] — 消耗秘境令进入
```

---

## 3. 关卡节奏设计

### 3.1 整体进度节奏表

| 阶段 | 天数 | 地图 | 目标等级 | 核心体验 |
|------|------|------|---------|---------|
| 觉醒 | Day 0-1 | 青云镇→后山 | 练气1-3 | 学习基础，建立归属 |
| 试炼 | Day 2-3 | 天剑宗→试剑峰 | 练气4-7 | 首次挑战，战胜师兄 |
| 突破 | Day 4-5 | 荒野 | 练气8-10 | 积累，准备心魔 |
| 蜕变 | Day 6-7 | 心魔空间→筑基 | 筑基1 | 仪式感突破 |
| 探索 | Day 8-12 | 妖兽林外围 | 筑基2-3 | 环境叙事展开 |
| 深入 | Day 13-15 | 妖兽林深处 | 筑基4-5 | 遭遇战设计 |
| 抉择 | Day 16-20 | 黑雾+万法阁 | 金丹1-2 | 关键剧情选择 |
| 真相 | Day 21-26 | 邪修密地 | 金丹3-4 | 世界观揭示 |
| 超越 | Day 27+ | 上古秘境 | 元婴 | 史诗结局 |

### 3.2 单个地图的节奏节拍

以**妖兽林外围**为例：

```
时间线 | 事件 | 张力 | 设计意图
-------|------|------|----------
0:00   | 进入地图 | 低 | 环境描述建立氛围
0:30   | 首次遭遇（2妖狼） | 中 | 热身战斗
1:30   | 探索发现（草药点） | 低 | 奖励探索行为
3:00   | 中段遭遇（精英+2小怪） | 高 | 技能检验
5:00   | 休息点 | 低 | 调整策略
7:00   | Boss遭遇（妖狼王） | 极高 | 高潮战斗
10:00  | 完成奖励 | 释放 | 成就感+解锁下一区域
```

### 3.3 遭遇战设计规格

每个地图的遭遇战遵循"3+1"模式：

| 遭遇类型 | 数量 | 配置 | 战术选项 |
|---------|------|------|---------|
| 普通遭遇 | 3场 | 2-3只普通怪 | 直接战斗/使用技能 |
| 精英遭遇 | 1场 | 1精英+1-2小怪 | 集火精英/先清小怪 |
| 休息点 | 1个 | 无战斗 | 恢复/调整/离开 |
| Boss战 | 1场 | 1BOSS | 全技能检验 |

---

## 4. 环境叙事规格

### 4.1 地图氛围关键词

| 地图 | 主色调 | 关键词 | 叙事主题 |
|------|--------|--------|----------|
| 青云镇 | #F5E6C8+#7CB342 | 安宁、神秘、起点 | "一切开始的地方" |
| 天剑宗 | #E0E0E0+#1565C0 | 秩序、纪律、传承 | "正道之路" |
| 荒野 | #D7CCC8+#C62828 | 危险、机遇、孤独 | "弱肉强食" |
| 妖兽林 | #33691E+#4A148C | 原始、恐惧、共生 | "自然的回应" |
| 黑雾深谷 | #212121+#6A1B9A | 压抑、秘密、腐蚀 | "被封印的过去" |
| 万法阁 | #FFF8E1+#212121 | 知识、禁忌、选择 | "智慧的双刃" |

### 4.2 场景描述模板

#### 进入场景（建立氛围）

```
【青云镇】
"夕阳将青云镇的屋檐染成金色。镇口的古井旁，
几个孩童正在追逐嬉戏。远处天剑宗的山峰若隐若现，
仿佛守护着什么秘密。你感到一种奇异的安宁——
这里是你修仙之路的起点。"

【妖兽林外围】
"你踏入妖兽林的外围，阳光被茂密的树冠切割成碎片。
空气中弥漫着潮湿的腐叶气息，远处传来不知名野兽的低吼。
你的灵猫竖起耳朵，尾巴微微颤抖——它感觉到了什么。"

【黑雾深谷】
"黑雾像活物一样缠绕着你的脚踝。每一步都发出
令人不安的"咯吱"声——那是被腐蚀的落叶。
你手中的剑开始发出微弱的嗡鸣，仿佛在警告你什么。
这里不应该有人类踏足。"
```

#### 遭遇怪物（建立紧张）

```
【妖兽林-妖狼遭遇】
"前方的灌木丛突然晃动，三只妖狼窜出！
它们的眼睛泛着不自然的红光，嘴角滴落着涎水。
这不是普通的野兽——它们被黑雾侵蚀了。"

【黑雾深谷-变异妖兽】
"雾气中浮现出一个巨大的轮廓。当它走近时，
你倒吸一口冷气——那曾经是一只灵鹿，但现在
它的角上长满了黑色的晶体，眼睛是两个空洞。
它发出一声不像任何活物的嘶吼，向你冲来。"
```

#### 战斗胜利（释放+奖励）

```
【妖兽林-胜利】
"妖狼倒下，身体逐渐化为黑雾消散。
你在它们倒下的地方发现了一些妖兽材料。
林中的雾气似乎淡了一些……但深处的咆哮声更近了。"

【黑雾深谷-胜利】
"变异灵鹿倒下，黑色的晶体碎裂成粉末。
你注意到，它倒下的地方，草木正在恢复绿色——
仿佛它的死亡释放了某种被囚禁的生命力。
你感到一丝悲哀，但更多的是决心。"
```

### 4.3 环境叙事线索索引

| 地图 | 线索 | 发现条件 | 关联传说 |
|------|------|---------|---------|
| 青云镇古井 | 灵纹裂缝 | 练气5层+夜间 | L010-苍玄仙帝 |
| 天剑宗剑冢 | 封印符文 | 筑基期+内门 | L005-封魔之战 |
| 妖兽林深处 | 断裂石碑 | 筑基期+探索 | L005-封魔之战 |
| 黑雾深谷 | 变异妖兽 | 金丹期 | L009-五灵脉秘密 |
| 万法阁 | 空白书页 | 金丹期+好感 | L012-飞升真相 |

---

## 5. 难度曲线设计

### 5.1 动态难度公式

```
怪物等级 = 地图基础等级 + min(玩家等级 - 推荐等级, 5) × 0.5

怪物属性倍率：
- 玩家等级 < 推荐等级-3：×0.8（新手保护）
- 推荐等级-3 ≤ 玩家等级 ≤ 推荐等级+3：×1.0（标准）
- 玩家等级 > 推荐等级+3：×1.2（挑战模式）

胜率目标：
- 普通区：70-80%
- 危险区：60-70%
- 副本：50-60%
- BOSS：40-50%
```

### 5.2 地图难度参数表

| 地图 | 推荐等级 | 基础怪物等级 | 精英出现率 | BOSS等级 |
|------|---------|-------------|-----------|---------|
| 后山 | 1-3 | 1-3 | 0% | - |
| 试剑峰 | 4-7 | 4-7 | 10% | - |
| 荒野外围 | 8-10 | 8-10 | 15% | 12(山贼头目) |
| 荒野深处 | 11-13 | 11-13 | 20% | 15(荒野巨兽) |
| 妖兽林外围 | 11-13 | 11-13 | 25% | - |
| 妖兽林深处 | 14-15 | 14-15 | 30% | 16(妖狼王) |
| 黑雾深谷 | 16-17 | 16-17 | 40% | 18(黑雾领主) |
| 上古秘境 | 20 | 20 | 50% | 22(上古守卫) |

### 5.3 收益曲线

```
基础灵石收益 = 10 + 地图等级 × 2
经验倍率 = 1.0 + (危险等级 × 0.1)

离线收益公式：
安全区：基础收益 × 1.0 × 小时数
普通区：基础收益 × 1.1 × 小时数
危险区：基础收益 × 1.3 × 小时数 × (0.9^受伤次数)

受伤概率（危险区离线）：
- 0-6小时：0%
- 6-12小时：10%
- 12-18小时：25%
- 18-24小时：40%
```

---

## 6. 技术实现规格

### 6.1 前端界面结构

```
【地图界面】
┌─────────────────────────────────────────┐
│  🗺️ 苍玄界地图          当前：妖兽林外围    │
├─────────────────────────────────────────┤
│                                         │
│     [青云镇]───[天剑宗]───[荒野]        │
│        │                    │           │
│     [古井]               [妖兽林] ⭐    │
│        │                    │           │
│     [? ? ?]             [深谷]🔒        │
│                                         │
│  ⭐ = 当前位置   🔒 = 未解锁   ? = 隐藏  │
│                                         │
├─────────────────────────────────────────┤
│  【妖兽林外围】                          │
│  危险等级：⚠️⚠️⚠️                         │
│  推荐等级：筑基期1-3层                    │
│                                         │
│  环境：阳光难以穿透茂密的树冠，空气中弥漫 │
│  着潮湿的气息。远处传来野兽的低吼...      │
│                                         │
│  [开始探索] [离线挂机] [返回安全区]       │
└─────────────────────────────────────────┘
```

### 6.2 API接口设计

```java
// 地图相关接口
GET /api/maps — 获取所有地图列表
GET /api/maps/{id} — 获取地图详情
GET /api/maps/current — 获取当前所在地图
POST /api/maps/enter/{id} — 进入地图
POST /api/maps/leave — 离开当前地图
GET /api/maps/explore — 探索当前地图（触发遭遇）
GET /api/maps/offline-reward — 领取离线收益

// 玩家地图进度
GET /api/player/maps — 获取玩家地图进度
GET /api/player/maps/unlock/{id} — 解锁地图
```

### 6.3 关键算法

```java
// 遭遇战生成算法
public Encounter generateEncounter(int mapId, int playerLevel) {
    Map map = mapService.getById(mapId);
    
    // 1. 确定遭遇类型
    EncounterType type = rollEncounterType(map.getDangerLevel());
    
    // 2. 选择怪物
    List<MapMonster> candidates = mapMonsterMapper.selectByMapId(mapId);
    List<Monster> monsters = selectMonsters(candidates, type, playerLevel);
    
    // 3. 生成场景描述
    String sceneText = narrativeService.generateSceneText(map, type);
    
    return new Encounter(type, monsters, sceneText);
}

// 离线收益计算
public OfflineReward calculateOfflineReward(int playerId, int hours) {
    PlayerMapProgress progress = getCurrentMap(playerId);
    Map map = mapService.getById(progress.getMapId());
    
    // 基础收益
    int baseSpiritStones = map.getBaseSpiritStones() * hours;
    int baseExp = calculateBaseExp(playerId) * hours;
    
    // 危险区风险计算
    int injuryCount = 0;
    if (map.getOfflineRisk()) {
        injuryCount = calculateInjuryCount(hours);
        baseSpiritStones *= Math.pow(0.9, injuryCount);
    }
    
    // 随机事件
    List<RandomEvent> events = rollRandomEvents(map, hours);
    
    return new OfflineReward(baseSpiritStones, baseExp, injuryCount, events);
}
```

---

## 7. 数据库表结构

### 7.1 地图主表

```sql
CREATE TABLE `game_maps` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '地图ID',
  `name` varchar(100) NOT NULL COMMENT '地图名称',
  `description` text COMMENT '地图描述',
  `region` varchar(50) NOT NULL COMMENT '所属区域',
  `map_type` varchar(20) NOT NULL COMMENT '类型：SAFE/NORMAL/DANGEROUS/BOSS',
  `required_level` int DEFAULT 1 COMMENT '需求等级',
  `required_realm` varchar(50) DEFAULT NULL COMMENT '需求境界',
  `unlock_condition` text COMMENT '解锁条件描述',
  `prev_map_id` int DEFAULT NULL COMMENT '前置地图ID',
  `base_spirit_stones` int DEFAULT 10 COMMENT '基础灵石收益/小时',
  `exp_modifier` decimal(3,2) DEFAULT 1.00 COMMENT '经验倍率',
  `danger_level` int DEFAULT 1 COMMENT '危险等级1-5',
  `offline_risk` tinyint(1) DEFAULT 0 COMMENT '离线是否有风险',
  `theme_color` varchar(20) DEFAULT NULL COMMENT '主题色',
  `ambience_text` text COMMENT '环境氛围文本',
  `enter_text` text COMMENT '进入场景文本',
  `victory_text` text COMMENT '胜利场景文本',
  `position_x` int DEFAULT 0 COMMENT '地图坐标X',
  `position_y` int DEFAULT 0 COMMENT '地图坐标Y',
  `active` tinyint(1) DEFAULT 1,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_region` (`region`),
  KEY `idx_map_type` (`map_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏地图表';
```

### 7.2 地图怪物配置表

```sql
CREATE TABLE `map_monsters` (
  `id` int NOT NULL AUTO_INCREMENT,
  `map_id` int NOT NULL COMMENT '地图ID',
  `monster_id` int NOT NULL COMMENT '怪物ID',
  `spawn_rate` decimal(5,2) DEFAULT 100.00 COMMENT '出现概率%',
  `min_level` int DEFAULT 1 COMMENT '最小等级',
  `max_level` int DEFAULT 1 COMMENT '最大等级',
  `is_elite` tinyint(1) DEFAULT 0 COMMENT '是否为精英',
  `spawn_weight` int DEFAULT 1 COMMENT '权重',
  `encounter_text` text COMMENT '遭遇描述文本',
  PRIMARY KEY (`id`),
  KEY `idx_map_id` (`map_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地图怪物配置';
```

### 7.3 玩家地图进度表

```sql
CREATE TABLE `player_map_progress` (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `map_id` int NOT NULL,
  `is_unlocked` tinyint(1) DEFAULT 0,
  `is_current` tinyint(1) DEFAULT 0 COMMENT '是否为当前所在地图',
  `first_enter_at` timestamp NULL,
  `last_enter_at` timestamp NULL,
  `total_kills` int DEFAULT 0,
  `total_time_spent` int DEFAULT 0 COMMENT '累计停留时间(分钟)',
  `offline_start_at` timestamp NULL COMMENT '离线挂机开始时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_map` (`player_id`, `map_id`),
  KEY `idx_player_current` (`player_id`, `is_current`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家地图进度';
```

---

## 8. 关卡数据填充

详见 `map-data.sql` 文件，包含：

- 12张地图基础数据
- 地图连接关系
- 怪物配置
- 环境叙事文本

---

*文档更新记录*
- **v1.0 (2026-03-23)**: 首版创建，包含完整关卡系统设计方案
