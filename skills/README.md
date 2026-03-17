# 技能系统 (Skills System)

## 系统概述

技能系统是修仙游戏的核心玩法之一，玩家可以通过学习、升级和使用技能来提升战斗力和修炼效率。

## 技能类型

### 1. 攻击技能 (Attack)
- **特点**: 造成伤害的技能
- **示例**: 火球术、地刺术、风刃术
- **属性**: 金、木、水、火、土

### 2. 防御技能 (Defense)
- **特点**: 提供防护或减少伤害
- **示例**: 水盾术
- **效果**: 减少受到的伤害

### 3. 治疗技能 (Heal)
- **特点**: 恢复生命值
- **示例**: 治疗术
- **效果**: 恢复玩家生命值

### 4. 修炼技能 (Cultivation)
- **特点**: 提升修炼效率
- **示例**: 基础功法
- **效果**: 增加修炼速度和经验获取

### 5. 辅助技能 (Support)
- **特点**: 提供各种增益效果
- **示例**: 速度提升、攻击力增益等

## 技能属性

### 基础属性
- **ID**: 技能唯一标识
- **名称**: 技能名称
- **描述**: 技能描述
- **等级**: 当前等级
- **最大等级**: 技能可达到的最高等级

### 战斗属性
- **基础伤害**: 技能的基础伤害值
- **每级伤害增长**: 每升一级增加的伤害
- **冷却时间**: 技能释放后的冷却时间（秒）
- **法力消耗**: 释放技能所需的法力值

### 要求属性
- **解锁等级**: 玩家等级要求
- **所需灵石**: 学习技能所需的灵石数量

### 加成属性
- **生命值加成**: 被动增加的生命值
- **法力值加成**: 被动增加的法力值
- **攻击力加成**: 被动增加的攻击力
- **防御力加成**: 被动增加的防御力
- **速度加成**: 被动增加的速度

## 现有技能列表

### 1. 基础功法 (ID: 1)
- **类型**: 修炼
- **元素**: 无
- **描述**: 提升基础修炼速度
- **解锁等级**: 1
- **价格**: 免费
- **效果**: 提升修炼速度

### 2. 火球术 (ID: 2)
- **类型**: 攻击
- **元素**: 火
- **描述**: 基础火系攻击法术
- **解锁等级**: 5
- **价格**: 1000 灵石
- **基础伤害**: 10
- **冷却时间**: 5 秒
- **法力消耗**: 10

### 3. 治疗术 (ID: 3)
- **类型**: 治疗
- **元素**: 木
- **描述**: 恢复生命值的法术
- **解锁等级**: 3
- **价格**: 800 灵石
- **基础伤害**: 20 (治疗量)
- **冷却时间**: 8 秒
- **法力消耗**: 15

### 4. 水盾术 (ID: 4)
- **类型**: 防御
- **元素**: 水
- **描述**: 创造一个水盾，减少受到的伤害
- **解锁等级**: 8
- **价格**: 1200 灵石
- **冷却时间**: 10 秒
- **法力消耗**: 15

### 5. 地刺术 (ID: 5)
- **类型**: 攻击
- **元素**: 土
- **描述**: 从地面召唤尖刺，对敌人造成土属性伤害
- **解锁等级**: 12
- **价格**: 1500 灵石
- **基础伤害**: 25
- **冷却时间**: 5 秒
- **法力消耗**: 20

### 6. 风刃术 (ID: 6)
- **类型**: 攻击
- **元素**: 风
- **描述**: 释放锋利的风刃，对敌人造成风属性伤害
- **解锁等级**: 10
- **价格**: 1300 灵石
- **基础伤害**: 15
- **冷却时间**: 2 秒
- **法力消耗**: 8

## 技能系统功能

### 1. 学习技能
- 玩家可以消耗灵石学习新技能
- 需要满足等级要求
- 每个技能只能学习一次

### 2. 技能升级
- 通过使用技能获得经验
- 消耗灵石或技能点升级
- 提升技能效果和伤害

### 3. 技能装备
- 玩家可以装备多个技能
- 装备槽位数量有限
- 战斗时只能使用已装备的技能

### 4. 技能使用
- 战斗中使用技能造成伤害或提供增益
- 消耗法力值
- 受冷却时间限制

### 5. 技能商店
- 商店提供各种技能购买
- 不同技能有不同价格和要求
- 部分稀有技能需要特殊条件解锁

## API 接口

### 获取技能列表
```bash
GET /api/skills
```

### 获取可学习技能
```bash
GET /api/skills/available
```

### 获取玩家技能
```bash
GET /api/skills/player
```

### 学习技能
```bash
POST /api/skills/learn/{skillId}
```

### 升级技能
```bash
POST /api/skills/{playerSkillId}/upgrade
```

### 装备技能
```bash
POST /api/skills/equip/{playerSkillId}/{slotNumber}
```

### 卸下技能
```bash
POST /api/skills/unequip/{playerSkillId}
```

### 使用技能
```bash
POST /api/skills/{playerSkillId}/use
```

## 数据库结构

### skills 表（技能模板）
```sql
CREATE TABLE skills (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,           -- 技能名称
  description TEXT,                     -- 技能描述
  level INT DEFAULT 1,                  -- 当前等级
  max_level INT DEFAULT 10,             -- 最大等级
  base_damage DOUBLE DEFAULT 0,         -- 基础伤害
  damage_per_level DOUBLE DEFAULT 0,    -- 每级伤害增长
  cooldown INT DEFAULT 0,               -- 冷却时间（秒）
  mana_cost INT DEFAULT 0,              -- 法力消耗
  skill_type VARCHAR(50),               -- 技能类型（攻击、防御、治疗等）
  element VARCHAR(50),                  -- 元素属性（金、木、水、火、土等）
  unlock_level INT DEFAULT 1,           -- 解锁等级
  required_spirit_stones INT DEFAULT 0, -- 所需灵石
  health_bonus INT DEFAULT 0,           -- 生命值加成
  mana_bonus INT DEFAULT 0,             -- 法力值加成
  attack_bonus INT DEFAULT 0,           -- 攻击力加成
  defense_bonus INT DEFAULT 0,          -- 防御力加成
  speed_bonus INT DEFAULT 0,            -- 速度加成
  icon VARCHAR(255),                    -- 图标
  animation VARCHAR(255),               -- 动画
  active BOOLEAN DEFAULT true,          -- 是否启用
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_skill_type (skill_type),
  KEY idx_element (element),
  KEY idx_unlock_level (unlock_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### player_skills 表（玩家技能）
```sql
CREATE TABLE player_skills (
  id INT PRIMARY KEY AUTO_INCREMENT,
  player_id INT NOT NULL,               -- 玩家ID
  skill_id INT NOT NULL,                -- 技能ID
  level INT DEFAULT 1,                  -- 技能等级
  experience INT DEFAULT 0,             -- 技能经验
  equipped BOOLEAN DEFAULT false,       -- 是否装备
  slot_number INT DEFAULT 0,            -- 装备槽位
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_player_skill (player_id, skill_id),
  KEY idx_player_id (player_id),
  KEY idx_skill_id (skill_id),
  KEY idx_equipped (equipped),
  FOREIGN KEY (player_id) REFERENCES player_profiles(id) ON DELETE CASCADE,
  FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### skill_shop 表（技能商店）
```sql
CREATE TABLE skill_shop (
  id INT PRIMARY KEY AUTO_INCREMENT,
  skill_id INT NOT NULL,                -- 技能ID
  price INT NOT NULL,                   -- 价格
  required_level INT DEFAULT 1,         -- 所需等级
  available BOOLEAN DEFAULT true,       -- 是否可用
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_skill_id (skill_id),
  KEY idx_available (available),
  FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 未来扩展

- [ ] 技能符文系统
- [ ] 技能组合系统
- [ ] 技能觉醒系统
- [ ] 技能传承系统
- [ ] 技能共鸣系统