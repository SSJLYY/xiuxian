# 技能系统实现文档

## 系统架构

### 后端架构
```
SkillController (API层)
  ↓
SkillService (业务逻辑层)
  ↓
SkillMapper (数据访问层)
  ↓
Database (skills, player_skills 表)
```

### 前端架构
```
skill.html (技能页面)
  ↓
skill.js (技能逻辑)
  ↓
api.js (API调用)
  ↓
Backend API
```

## 核心类定义

### Skill 实体类
位置: `src/main/java/com/xiuxian/game/entity/Skill.java`

```java
@TableName("skills")
public class Skill {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String name;              // 技能名称
    private String description;       // 技能描述
    private Integer level;            // 当前等级
    private Integer maxLevel;         // 最大等级
    private Double baseDamage;        // 基础伤害
    private Double damagePerLevel;    // 每级伤害增长
    private Integer cooldown;         // 冷却时间(秒)
    private Integer manaCost;         // 法力消耗
    private String skillType;         // 技能类型
    private String element;           // 元素属性
    private Integer unlockLevel;      // 解锁等级
    private Integer requiredSpiritStones; // 所需灵石
    private Integer healthBonus;      // 生命值加成
    private Integer manaBonus;        // 法力值加成
    private Integer attackBonus;      // 攻击力加成
    private Integer defenseBonus;     // 防御力加成
    private Integer speedBonus;       // 速度加成
    private String icon;              // 图标
    private String animation;         // 动画
    private Boolean active;           // 是否启用
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间
}
```

### PlayerSkill 实体类
位置: `src/main/java/com/xiuxian/game/entity/PlayerSkill.java`

```java
@TableName("player_skills")
public class PlayerSkill {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer playerId;         // 玩家ID
    private Integer skillId;          // 技能ID
    private Integer level;            // 技能等级
    private Integer experience;       // 技能经验
    private Boolean equipped;         // 是否装备
    private Integer slotNumber;       // 装备槽位
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间
}
```

## 核心服务类

### SkillService
位置: `src/main/java/com/xiuxian/game/service/SkillService.java`

#### 主要方法

1. **获取所有技能**
```java
public List<Skill> getAllSkills() {
    return skillMapper.selectList(null);
}
```

2. **获取可学习技能**
```java
public List<Skill> getAvailableSkills(Integer playerId) {
    PlayerProfile player = playerProfileMapper.selectById(playerId);
    return skillMapper.selectByUnlockLevelLessThanEqual(player.getLevel());
}
```

3. **学习技能**
```java
public PlayerSkill learnSkill(Integer skillId, Integer playerId) {
    // 1. 验证技能存在
    Skill skill = skillMapper.selectById(skillId);
    if (skill == null) {
        throw new IllegalArgumentException("技能不存在");
    }
    
    // 2. 验证等级要求
    PlayerProfile player = playerProfileMapper.selectById(playerId);
    if (skill.getUnlockLevel() > player.getLevel()) {
        throw new IllegalArgumentException("等级不足");
    }
    
    // 3. 验证是否已学习
    PlayerSkill existing = playerSkillMapper.selectByPlayerIdAndSkillId(playerId, skillId);
    if (existing != null) {
        throw new IllegalArgumentException("已学习该技能");
    }
    
    // 4. 扣除灵石
    if (skill.getRequiredSpiritStones() > 0) {
        if (player.getSpiritStones() < skill.getRequiredSpiritStones()) {
            throw new IllegalArgumentException("灵石不足");
        }
        player.setSpiritStones(player.getSpiritStones() - skill.getRequiredSpiritStones());
        playerProfileMapper.updateById(player);
    }
    
    // 5. 创建玩家技能
    PlayerSkill playerSkill = PlayerSkill.builder()
        .playerId(playerId)
        .skillId(skillId)
        .level(1)
        .experience(0)
        .equipped(false)
        .slotNumber(0)
        .build();
    
    playerSkillMapper.insert(playerSkill);
    return playerSkill;
}
```

4. **升级技能**
```java
public PlayerSkill upgradeSkill(Integer playerSkillId, Integer playerId) {
    PlayerSkill playerSkill = playerSkillMapper.selectById(playerSkillId);
    if (playerSkill == null || !playerSkill.getPlayerId().equals(playerId)) {
        throw new IllegalArgumentException("玩家技能不存在");
    }
    
    Skill skill = skillMapper.selectById(playerSkill.getSkillId());
    if (playerSkill.getLevel() >= skill.getMaxLevel()) {
        throw new IllegalArgumentException("技能已达到最大等级");
    }
    
    // 计算升级消耗
    long upgradeCost = gameCalculator.calculateSkillUpgradeCost(playerSkill.getLevel());
    
    PlayerProfile player = playerProfileMapper.selectById(playerId);
    if (player.getSpiritStones() < upgradeCost) {
        throw new IllegalArgumentException("灵石不足");
    }
    
    // 扣除灵石并升级
    player.setSpiritStones(player.getSpiritStones() - upgradeCost);
    playerProfileMapper.updateById(player);
    
    playerSkill.setLevel(playerSkill.getLevel() + 1);
    playerSkillMapper.updateById(playerSkill);
    
    return playerSkill;
}
```

5. **装备技能**
```java
public PlayerSkill equipSkill(Integer playerSkillId, Integer slotNumber, Integer playerId) {
    PlayerSkill playerSkill = playerSkillMapper.selectById(playerSkillId);
    if (playerSkill == null || !playerSkill.getPlayerId().equals(playerId)) {
        throw new IllegalArgumentException("玩家技能不存在");
    }
    
    // 检查槽位是否已被占用
    PlayerSkill existingInSlot = playerSkillMapper.selectByPlayerIdAndSlotNumber(playerId, slotNumber);
    if (existingInSlot != null) {
        // 卸下已装备的技能
        existingInSlot.setEquipped(false);
        existingInSlot.setSlotNumber(0);
        playerSkillMapper.updateById(existingInSlot);
    }
    
    // 装备新技能
    playerSkill.setEquipped(true);
    playerSkill.setSlotNumber(slotNumber);
    playerSkillMapper.updateById(playerSkill);
    
    return playerSkill;
}
```

6. **计算技能伤害**
```java
public double calculateSkillDamage(PlayerSkill playerSkill) {
    Skill skill = skillMapper.selectById(playerSkill.getSkillId());
    
    // 基础伤害 + 等级加成
    double baseDamage = skill.getBaseDamage() != null ? skill.getBaseDamage() : 0;
    double damagePerLevel = skill.getDamagePerLevel() != null ? skill.getDamagePerLevel() : 0;
    
    double totalDamage = baseDamage + (playerSkill.getLevel() - 1) * damagePerLevel;
    
    // 玩家属性加成
    PlayerProfile player = playerProfileMapper.selectById(playerSkill.getPlayerId());
    double attackBonus = player.getAttack() + player.getEquipmentAttackBonus();
    
    // 最终伤害 = 基础伤害 × (1 + 攻击加成百分比)
    return totalDamage * (1 + attackBonus * 0.01);
}
```

## API 接口定义

### SkillController
位置: `src/main/java/com/xiuxian/game/controller/SkillController.java`

#### 接口列表

1. **获取所有技能**
```
GET /api/skills
响应: List<Skill>
```

2. **获取可学习技能**
```
GET /api/skills/available
响应: List<Skill>
```

3. **获取玩家技能**
```
GET /api/skills/player
响应: List<SkillResponse>
```

4. **学习技能**
```
POST /api/skills/learn/{skillId}
响应: PlayerSkill
```

5. **升级技能**
```
POST /api/skills/{playerSkillId}/upgrade
响应: PlayerSkill
```

6. **装备技能**
```
POST /api/skills/equip/{playerSkillId}/{slotNumber}
响应: PlayerSkill
```

7. **卸下技能**
```
POST /api/skills/unequip/{playerSkillId}
响应: PlayerSkill
```

8. **使用技能**
```
POST /api/skills/{playerSkillId}/use
响应: String (成功信息)
```

9. **技能点升级**
```
POST /api/skills/{playerSkillId}/upgrade-by-points
响应: PlayerSkill
```

## 前端实现

### 技能页面 (skill.html)
- 显示技能列表
- 显示玩家已学习技能
- 提供学习、升级、装备操作

### 技能逻辑 (skill.js)
```javascript
class SkillManager {
    // 加载技能列表
    async loadSkills() {
        const response = await gameAPI.getAllSkills();
        if (response.success) {
            this.renderSkills(response.data);
        }
    }
    
    // 学习技能
    async learnSkill(skillId) {
        const response = await gameAPI.learnSkill(skillId);
        if (response.success) {
            this.showToast('技能学习成功');
            await this.loadPlayerSkills();
        }
    }
    
    // 升级技能
    async upgradeSkill(playerSkillId) {
        const response = await gameAPI.upgradeSkill(playerSkillId);
        if (response.success) {
            this.showToast('技能升级成功');
            await this.loadPlayerSkills();
        }
    }
    
    // 装备技能
    async equipSkill(playerSkillId, slotNumber) {
        const response = await gameAPI.equipSkill(playerSkillId, slotNumber);
        if (response.success) {
            this.showToast('技能装备成功');
            await this.loadPlayerSkills();
        }
    }
}
```

## 数据库设计

### skills 表
```sql
CREATE TABLE skills (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  level INT DEFAULT 1,
  max_level INT DEFAULT 10,
  base_damage DOUBLE DEFAULT 0,
  damage_per_level DOUBLE DEFAULT 0,
  cooldown INT DEFAULT 0,
  mana_cost INT DEFAULT 0,
  skill_type VARCHAR(50),
  element VARCHAR(50),
  unlock_level INT DEFAULT 1,
  required_spirit_stones INT DEFAULT 0,
  health_bonus INT DEFAULT 0,
  mana_bonus INT DEFAULT 0,
  attack_bonus INT DEFAULT 0,
  defense_bonus INT DEFAULT 0,
  speed_bonus INT DEFAULT 0,
  icon VARCHAR(255),
  animation VARCHAR(255),
  active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_skill_type (skill_type),
  KEY idx_element (element),
  KEY idx_unlock_level (unlock_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### player_skills 表
```sql
CREATE TABLE player_skills (
  id INT PRIMARY KEY AUTO_INCREMENT,
  player_id INT NOT NULL,
  skill_id INT NOT NULL,
  level INT DEFAULT 1,
  experience INT DEFAULT 0,
  equipped BOOLEAN DEFAULT false,
  slot_number INT DEFAULT 0,
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

### skill_shop 表
```sql
CREATE TABLE skill_shop (
  id INT PRIMARY KEY AUTO_INCREMENT,
  skill_id INT NOT NULL,
  price INT NOT NULL,
  required_level INT DEFAULT 1,
  available BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_skill_id (skill_id),
  KEY idx_available (available),
  FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 技能计算公式

### 伤害计算
```
总伤害 = 基础伤害 + (技能等级 - 1) × 每级伤害增长
最终伤害 = 总伤害 × (1 + 玩家攻击加成百分比)
```

### 升级消耗
```
升级所需灵石 = 基础消耗 × (1.5 ^ (当前等级 - 1))
```

### 技能经验获取
```
攻击技能经验 = 基础经验 × 技能等级
防御技能经验 = 基础经验 × 技能等级 × 0.8
辅助技能经验 = 基础经验 × 技能等级 × 0.6
```

## 技能系统测试

### 单元测试
```java
@Test
public void testLearnSkill() {
    // 测试学习技能逻辑
}

@Test
public void testUpgradeSkill() {
    // 测试升级技能逻辑
}

@Test
public void testCalculateSkillDamage() {
    // 测试技能伤害计算
}
```

### 集成测试
```java
@Test
public void testSkillWorkflow() {
    // 测试完整技能工作流
    // 1. 学习技能
    // 2. 升级技能
    // 3. 装备技能
    // 4. 使用技能
}
```

### API 测试
使用 `test-skill-upgrade.sh` 脚本测试所有技能API接口。

## 未来扩展

### 短期扩展（1-3个月）
1. **技能符文系统**: 为技能添加额外效果
2. **技能组合**: 连续使用技能触发组合效果
3. **技能专精**: 选择专精方向提升特定技能

### 中期扩展（3-6个月）
1. **技能觉醒**: 达到特定条件解锁觉醒技能
2. **技能传承**: 将技能传授给其他玩家
3. **技能共鸣**: 多个技能同时使用产生共鸣

### 长期扩展（6个月以上）
1. **技能领域**: 创建技能领域，持续影响范围内的目标
2. **技能融合**: 合并多个技能创造新技能
3. **技能神化**: 达到顶级后技能产生质变