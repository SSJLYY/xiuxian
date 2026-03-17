# 技能配置文档

## 技能分类体系

### 1. 按功能分类
- **攻击类**: 造成伤害
- **防御类**: 提供防护
- **治疗类**: 恢复生命
- **修炼类**: 提升修炼效率
- **辅助类**: 提供增益效果

### 2. 按元素分类
- **金**: 锐利、穿透
- **木**: 生命、恢复
- **水**: 防御、流动
- **火**: 爆发、伤害
- **土**: 坚固、厚重
- **风**: 速度、敏捷
- **雷**: 麻痹、爆发
- **冰**: 减速、控制

## 技能详细配置

### 基础功法 (ID: 1)
```yaml
id: 1
name: "基础功法"
description: "提升基础修炼速度"
skill_type: "cultivation"
element: "无"
level: 1
max_level: 100
base_damage: 0.05
damage_per_level: 0.01
cooldown: 0
mana_cost: 0
unlock_level: 1
required_spirit_stones: 0
health_bonus: 0
mana_bonus: 0
attack_bonus: 0
defense_bonus: 0
speed_bonus: 0
active: true
```

**效果说明**:
- 每级提升 1% 修炼速度
- 最高可提升 100% 修炼速度

### 火球术 (ID: 2)
```yaml
id: 2
name: "火球术"
description: "基础火系攻击法术"
skill_type: "attack"
element: "火"
level: 1
max_level: 50
base_damage: 10
damage_per_level: 2
cooldown: 5
mana_cost: 10
unlock_level: 5
required_spirit_stones: 1000
health_bonus: 0
mana_bonus: 0
attack_bonus: 0
defense_bonus: 0
speed_bonus: 0
active: true
```

**伤害计算**:
```
总伤害 = 基础伤害 + (等级 - 1) × 每级伤害增长
示例: 等级 10 时 = 10 + 9 × 2 = 28 伤害
```

### 治疗术 (ID: 3)
```yaml
id: 3
name: "治疗术"
description: "恢复生命值的法术"
skill_type: "heal"
element: "木"
level: 1
max_level: 30
base_damage: 20
damage_per_level: 1.5
cooldown: 8
mana_cost: 15
unlock_level: 3
required_spirit_stones: 800
health_bonus: 0
mana_bonus: 0
attack_bonus: 0
defense_bonus: 0
speed_bonus: 0
active: true
```

**治疗计算**:
```
治疗量 = 基础治疗 + (等级 - 1) × 每级治疗增长
示例: 等级 5 时 = 20 + 4 × 1.5 = 26 治疗量
```

### 水盾术 (ID: 4)
```yaml
id: 4
name: "水盾术"
description: "创造一个水盾，减少受到的伤害"
skill_type: "defense"
element: "水"
level: 1
max_level: 10
base_damage: 0
damage_per_level: 0
cooldown: 10
mana_cost: 15
unlock_level: 8
required_spirit_stones: 1200
health_bonus: 0
mana_bonus: 0
attack_bonus: 0
defense_bonus: 0
speed_bonus: 0
active: true
```

**效果说明**:
- 减少受到的伤害
- 持续时间: 10 秒
- 减伤比例: 随技能等级提升

### 地刺术 (ID: 5)
```yaml
id: 5
name: "地刺术"
description: "从地面召唤尖刺，对敌人造成土属性伤害"
skill_type: "attack"
element: "土"
level: 1
max_level: 10
base_damage: 25
damage_per_level: 10
cooldown: 5
mana_cost: 20
unlock_level: 12
required_spirit_stones: 1500
health_bonus: 0
mana_bonus: 0
attack_bonus: 0
defense_bonus: 0
speed_bonus: 0
active: true
```

**伤害计算**:
```
总伤害 = 基础伤害 + (等级 - 1) × 每级伤害增长
示例: 等级 5 时 = 25 + 4 × 10 = 65 伤害
```

### 风刃术 (ID: 6)
```yaml
id: 6
name: "风刃术"
description: "释放锋利的风刃，对敌人造成风属性伤害"
skill_type: "attack"
element: "风"
level: 1
max_level: 10
base_damage: 15
damage_per_level: 7
cooldown: 2
mana_cost: 8
unlock_level: 10
required_spirit_stones: 1300
health_bonus: 0
mana_bonus: 0
attack_bonus: 0
defense_bonus: 0
speed_bonus: 0
active: true
```

**特点**:
- 冷却时间短 (2秒)
- 法力消耗低 (8点)
- 适合快速连续攻击

## 技能平衡性设计

### 伤害技能平衡
1. **高伤害技能**: 高冷却、高消耗 (如地刺术)
2. **快速技能**: 低伤害、低冷却 (如风刃术)
3. **平衡技能**: 中等伤害、中等冷却 (如火球术)

### 辅助技能平衡
1. **治疗技能**: 恢复量适中，冷却较长
2. **防御技能**: 减伤效果明显，持续时间有限
3. **修炼技能**: 提升效率，需要长期投入

### 元素相克
- **火克木**: 火系技能对木系目标伤害 +20%
- **水克火**: 水系技能对火系目标伤害 +20%
- **木克土**: 木系技能对土系目标伤害 +20%
- **土克水**: 土系技能对水系目标伤害 +20%
- **金克金**: 金系技能穿透防御

## 技能升级系统

### 升级方式
1. **使用升级**: 通过战斗使用技能获得经验
2. **灵石升级**: 消耗灵石直接升级
3. **技能点升级**: 消耗技能点升级

### 升级消耗
```java
// 升级所需灵石计算
long upgradeCost = baseCost * Math.pow(1.5, currentLevel - 1);

// 技能点消耗
int skillPointCost = currentLevel / 10 + 1;
```

### 升级效果
- 伤害类技能: 提升伤害值
- 防御类技能: 提升减伤比例
- 治疗类技能: 提升治疗量
- 修炼类技能: 提升修炼效率

## 技能商店配置

### 技能商店物品
```sql
INSERT INTO skill_shop (skill_id, price, required_level, available) VALUES
(1, 0, 1, true),    -- 基础功法（免费）
(2, 1000, 5, true),  -- 火球术
(3, 800, 3, true),   -- 治疗术
(4, 1200, 8, true),  -- 水盾术
(5, 1500, 12, true), -- 地刺术
(6, 1300, 10, true); -- 风刃术
```

## 技能系统与其他系统的联动

### 与装备系统的联动
1. **装备加成**: 装备可以提供技能伤害加成
2. **技能触发**: 特定技能可以触发装备特效
3. **属性协同**: 技能属性与装备属性产生协同效果

### 与修炼系统的联动
1. **修炼技能**: 基础功法提升修炼速度
2. **经验共享**: 修炼经验可用于技能升级
3. **效率提升**: 高级技能提升修炼效率

### 与任务系统的联动
1. **技能任务**: 特定任务需要使用特定技能完成
2. **任务奖励**: 任务奖励可能包含技能点或技能书
3. **技能专精**: 完成特定任务解锁技能专精

## 未来扩展计划

### 短期扩展（1-3个月）
1. **技能符文系统**: 为技能添加额外效果
   - 符文类型：伤害符文、冷却符文、法力符文
   - 符文获取：任务奖励、商店购买、副本掉落
   
2. **技能组合**: 连续使用技能触发组合效果
   - 组合规则：特定技能顺序触发
   - 组合奖励：额外伤害、特殊效果
   
3. **技能专精**: 选择专精方向提升特定技能
   - 专精类型：伤害专精、控制专精、辅助专精
   - 专精解锁：完成特定任务

### 中期扩展（3-6个月）
1. **技能觉醒**: 达到特定条件解锁觉醒技能
   - 觉醒条件：技能等级、玩家等级、特定道具
   - 觉醒效果：技能质变、新特效
   
2. **技能传承**: 将技能传授给其他玩家
   - 传承条件：师徒关系、技能等级
   - 传承奖励：经验分享、特殊称号
   
3. **技能共鸣**: 多个技能同时使用产生共鸣
   - 共鸣条件：特定技能组合
   - 共鸣效果：范围扩大、伤害提升

### 长期扩展（6个月以上）
1. **技能领域**: 创建技能领域，持续影响范围内的目标
   - 领域类型：攻击领域、防御领域、辅助领域
   - 领域效果：持续伤害、减伤、增益
   
2. **技能融合**: 合并多个技能创造新技能
   - 融合条件：特定技能组合、融合道具
   - 融合结果：全新技能、保留部分效果
   
3. **技能神化**: 达到顶级后技能产生质变
   - 神化条件：技能满级、特殊挑战
   - 神化效果：技能质变、无视防御