# 技能系统开发指南

## 开发环境准备

### 1. 项目结构
```
xiuxian-game/
├── skills/                          # 技能系统文档
│   ├── README.md                    # 系统概述
│   ├── skill-config.md              # 技能配置
│   ├── implementation.md            # 实现文档
│   ├── test-guide.md                # 测试指南
│   └── development-guide.md         # 开发指南
├── src/main/java/com/xiuxian/game/
│   ├── entity/
│   │   ├── Skill.java               # 技能实体
│   │   └── PlayerSkill.java         # 玩家技能实体
│   ├── service/
│   │   └── SkillService.java        # 技能服务
│   └── controller/
│       └── SkillController.java     # 技能控制器
└── src/main/resources/
    └── init-database.sql            # 数据库初始化
```

### 2. 开发工具
- **IDE**: IntelliJ IDEA 或 Eclipse
- **数据库**: MySQL 8.0
- **版本控制**: Git
- **构建工具**: Maven

## 技能系统开发流程

### 1. 需求分析
确定新技能的需求：
- 技能名称和描述
- 技能类型（攻击、防御、治疗等）
- 元素属性（金、木、水、火、土等）
- 基础数值（伤害、冷却、消耗等）
- 解锁条件（等级、灵石等）

### 2. 数据库设计
在 `skills` 表中添加新技能记录：
```sql
INSERT INTO skills (name, description, skill_type, element, ...) 
VALUES ('新技能', '技能描述', 'attack', '火', ...);
```

### 3. 后端开发

#### 3.1 实体类
技能实体类位于 `src/main/java/com/xiuxian/game/entity/Skill.java`，无需修改。

#### 3.2 服务类
技能服务类位于 `src/main/java/com/xiuxian/game/service/SkillService.java`。

**添加新方法示例**:
```java
/**
 * 新技能效果计算
 */
public double calculateNewSkillEffect(PlayerSkill playerSkill) {
    Skill skill = skillMapper.selectById(playerSkill.getSkillId());
    
    // 新技能特殊逻辑
    double baseEffect = skill.getBaseDamage() != null ? skill.getBaseDamage() : 0;
    double levelEffect = playerSkill.getLevel() * 0.1; // 每级增加10%效果
    
    return baseEffect * (1 + levelEffect);
}
```

#### 3.3 控制器类
技能控制器类位于 `src/main/java/com/xiuxian/game/controller/SkillController.java`。

**添加新接口示例**:
```java
/**
 * 新技能接口
 */
@PostMapping("/new-skill/{playerSkillId}")
public ResponseEntity<ApiResponse<Double>> newSkillEffect(@PathVariable Integer playerSkillId) {
    try {
        PlayerProfile player = getCurrentPlayerProfile();
        List<PlayerSkill> playerSkills = skillService.getPlayerSkills(player.getId());
        PlayerSkill targetSkill = playerSkills.stream()
                .filter(ps -> ps.getId().equals(playerSkillId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("玩家技能不存在"));
        
        double effect = skillService.calculateNewSkillEffect(targetSkill);
        return ResponseEntity.ok(ApiResponse.success("计算成功", effect));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
}
```

### 4. 前端开发

#### 4.1 技能页面
创建或修改 `skills.html` 页面：
```html
<div id="skillsPage">
    <h2>技能系统</h2>
    
    <!-- 技能列表 -->
    <div id="skillsList">
        <!-- 动态生成技能列表 -->
    </div>
    
    <!-- 玩家技能 -->
    <div id="playerSkills">
        <!-- 动态生成玩家技能列表 -->
    </div>
    
    <!-- 技能操作 -->
    <div id="skillActions">
        <button onclick="learnSkill()">学习技能</button>
        <button onclick="upgradeSkill()">升级技能</button>
        <button onclick="equipSkill()">装备技能</button>
    </div>
</div>
```

#### 4.2 技能逻辑
创建或修改 `skill.js` 文件：
```javascript
class SkillManager {
    constructor() {
        this.skills = [];
        this.playerSkills = [];
    }
    
    // 加载技能列表
    async loadSkills() {
        const response = await gameAPI.getAllSkills();
        if (response.success) {
            this.skills = response.data;
            this.renderSkills();
        }
    }
    
    // 加载玩家技能
    async loadPlayerSkills() {
        const response = await gameAPI.getPlayerSkills();
        if (response.success) {
            this.playerSkills = response.data;
            this.renderPlayerSkills();
        }
    }
    
    // 学习技能
    async learnSkill(skillId) {
        const response = await gameAPI.learnSkill(skillId);
        if (response.success) {
            this.showToast('技能学习成功');
            await this.loadPlayerSkills();
        } else {
            this.showToast('技能学习失败: ' + response.message);
        }
    }
    
    // 渲染技能列表
    renderSkills() {
        const container = document.getElementById('skillsList');
        container.innerHTML = '';
        
        this.skills.forEach(skill => {
            const skillElement = document.createElement('div');
            skillElement.className = 'skill-item';
            skillElement.innerHTML = `
                <h3>${skill.name}</h3>
                <p>${skill.description}</p>
                <p>类型: ${skill.skillType} | 元素: ${skill.element}</p>
                <p>解锁等级: ${skill.unlockLevel}</p>
                <button onclick="skillManager.learnSkill(${skill.id})">学习</button>
            `;
            container.appendChild(skillElement);
        });
    }
    
    // 渲染玩家技能
    renderPlayerSkills() {
        const container = document.getElementById('playerSkills');
        container.innerHTML = '';
        
        this.playerSkills.forEach(playerSkill => {
            const skillElement = document.createElement('div');
            skillElement.className = 'player-skill-item';
            skillElement.innerHTML = `
                <h3>${playerSkill.skill.name}</h3>
                <p>等级: ${playerSkill.level}</p>
                <p>经验: ${playerSkill.experience}</p>
                <button onclick="skillManager.upgradeSkill(${playerSkill.id})">升级</button>
                <button onclick="skillManager.equipSkill(${playerSkill.id})">装备</button>
            `;
            container.appendChild(skillElement);
        });
    }
}

// 初始化技能管理器
const skillManager = new SkillManager();
```

### 5. API 接口开发

#### 5.1 添加新接口
在 `SkillController` 中添加新接口：
```java
/**
 * 新技能效果计算接口
 */
@GetMapping("/{playerSkillId}/new-effect")
public ResponseEntity<ApiResponse<Double>> calculateNewEffect(@PathVariable Integer playerSkillId) {
    try {
        PlayerProfile player = getCurrentPlayerProfile();
        List<PlayerSkill> playerSkills = skillService.getPlayerSkills(player.getId());
        PlayerSkill targetSkill = playerSkills.stream()
                .filter(ps -> ps.getId().equals(playerSkillId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("玩家技能不存在"));
        
        double effect = skillService.calculateNewSkillEffect(targetSkill);
        return ResponseEntity.ok(ApiResponse.success("计算成功", effect));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
}
```

#### 5.2 更新 API 调用
在 `api.js` 中添加新接口调用：
```javascript
// 新技能效果计算
async calculateNewEffect(playerSkillId) {
    return await api.get(`/skills/${playerSkillId}/new-effect`);
}
```

### 6. 测试开发

#### 6.1 单元测试
创建测试类 `SkillServiceTest.java`：
```java
@Test
public void testCalculateNewSkillEffect() {
    // 准备测试数据
    PlayerSkill playerSkill = new PlayerSkill();
    playerSkill.setLevel(5);
    
    Skill skill = new Skill();
    skill.setBaseDamage(10.0);
    
    // 执行测试
    double effect = skillService.calculateNewSkillEffect(playerSkill);
    
    // 验证结果
    assertEquals(15.0, effect, 0.01);
}
```

#### 6.2 集成测试
创建集成测试：
```java
@Test
public void testSkillWorkflow() {
    // 1. 学习技能
    PlayerSkill learned = skillService.learnSkill(2, 1);
    assertNotNull(learned);
    
    // 2. 升级技能
    PlayerSkill upgraded = skillService.upgradeSkill(learned.getId(), 1);
    assertEquals(2, upgraded.getLevel());
    
    // 3. 装备技能
    PlayerSkill equipped = skillService.equipSkill(learned.getId(), 1, 1);
    assertTrue(equipped.getEquipped());
}
```

## 新技能开发示例

### 示例：添加"雷电术"技能

#### 1. 数据库添加
```sql
INSERT INTO skills (name, description, skill_type, element, level, max_level, 
                   base_damage, damage_per_level, cooldown, mana_cost, 
                   unlock_level, required_spirit_stones, active) 
VALUES ('雷电术', '召唤雷电攻击敌人', 'attack', '雷', 1, 30, 
        30, 5, 6, 25, 15, 2000, 1);
```

#### 2. 技能商店添加
```sql
INSERT INTO skill_shop (skill_id, price, required_level, available) 
VALUES (7, 2000, 15, true);
```

#### 3. 后端逻辑（如果需要特殊效果）
在 `SkillService` 中添加雷电术特殊效果：
```java
/**
 * 雷电术特殊效果：麻痹敌人
 */
public void applyThunderEffect(Integer playerId, Integer targetId) {
    // 麻痹逻辑：使敌人下回合无法行动
    // 实现具体麻痹逻辑
}
```

#### 4. 前端展示
在技能页面添加雷电术展示：
```javascript
// 雷电术特殊效果显示
if (skill.element === '雷') {
    skillElement.innerHTML += '<p class="thunder-effect">⚡ 麻痹效果: 敌人下回合无法行动</p>';
}
```

## 开发注意事项

### 1. 代码规范
- 遵循 Java 命名规范
- 添加必要的注释
- 保持代码格式一致

### 2. 数据库规范
- 使用外键约束
- 添加索引优化查询
- 保持数据一致性

### 3. 安全规范
- 验证玩家权限
- 防止 SQL 注入
- 验证输入数据

### 4. 性能规范
- 避免 N+1 查询
- 使用缓存优化
- 批量操作优化

## 调试技巧

### 1. 日志调试
```java
log.debug("玩家ID: {}, 技能ID: {}", playerId, skillId);
log.info("学习技能成功: {}", playerSkill);
```

### 2. 数据库调试
```sql
-- 查看技能数据
SELECT * FROM skills WHERE name LIKE '%雷电%';

-- 查看玩家技能
SELECT * FROM player_skills WHERE player_id = 1;

-- 查看技能商店
SELECT * FROM skill_shop WHERE available = true;
```

### 3. API 调试
使用 Postman 或 curl 测试 API：
```bash
curl -X GET http://localhost:8081/api/skills \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 部署说明

### 1. 开发环境
```bash
# 编译项目
mvn clean package -DskipTests

# 启动应用
java -jar target/xiuxian-game.jar
```

### 2. 生产环境
```bash
# 使用 Docker Compose
docker-compose up -d

# 查看日志
docker-compose logs -f xiuxian-game
```

## 常见问题

### Q1: 如何添加新技能？
A: 按照"新技能开发示例"步骤操作

### Q2: 技能伤害计算不正确？
A: 检查 `SkillService.calculateSkillDamage()` 方法

### Q3: 技能升级消耗异常？
A: 检查 `GameCalculator.calculateSkillUpgradeCost()` 方法

### Q4: 前端不显示技能？
A: 检查 API 接口是否正确返回数据