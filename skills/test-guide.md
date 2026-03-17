# 技能系统测试指南

## 测试环境准备

### 1. 启动服务
```bash
# 使用 Docker Compose 启动
docker-compose up -d

# 或者本地启动
start.bat start  # Windows
./start.sh start # Linux/macOS
```

### 2. 访问地址
- **游戏登录**: http://localhost:8081/login.html
- **技能页面**: http://localhost:8081/skills.html (如果存在)

## API 接口测试

### 1. 获取所有技能
```bash
curl -X GET http://localhost:8081/api/skills
```

**预期结果**: 返回所有技能列表

### 2. 获取可学习技能
```bash
curl -X GET http://localhost:8081/api/skills/available \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**预期结果**: 返回玩家当前等级可学习的技能列表

### 3. 获取玩家技能
```bash
curl -X GET http://localhost:8081/api/skills/player \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**预期结果**: 返回玩家已学习的技能列表

### 4. 学习技能
```bash
curl -X POST http://localhost:8081/api/skills/learn/2 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**预期结果**: 
- 成功: 返回学习的玩家技能信息
- 失败: 返回错误信息（等级不足、灵石不足等）

### 5. 升级技能
```bash
curl -X POST http://localhost:8081/api/skills/1/upgrade \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**预期结果**: 
- 成功: 返回升级后的玩家技能信息
- 失败: 返回错误信息（灵石不足、已达最大等级等）

### 6. 装备技能
```bash
curl -X POST http://localhost:8081/api/skills/equip/1/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**预期结果**: 
- 成功: 返回装备成功的玩家技能信息
- 失败: 返回错误信息

### 7. 卸下技能
```bash
curl -X POST http://localhost:8081/api/skills/unequip/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**预期结果**: 
- 成功: 返回卸下成功的玩家技能信息
- 失败: 返回错误信息

### 8. 使用技能
```bash
curl -X POST http://localhost:8081/api/skills/1/use \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**预期结果**: 
- 成功: 返回"技能使用成功"
- 失败: 返回错误信息

### 9. 技能点升级
```bash
curl -X POST http://localhost:8081/api/skills/1/upgrade-by-points \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**预期结果**: 
- 成功: 返回升级后的玩家技能信息
- 失败: 返回错误信息（技能点不足等）

## 功能测试

### 1. 学习技能流程
1. 登录游戏
2. 查看可学习技能列表
3. 选择一个技能学习
4. 验证灵石扣除
5. 验证技能出现在玩家技能列表

### 2. 升级技能流程
1. 选择一个已学习的技能
2. 点击升级按钮
3. 验证灵石扣除
4. 验证技能等级提升
5. 验证技能伤害/效果提升

### 3. 装备技能流程
1. 选择一个已学习的技能
2. 选择装备槽位
3. 点击装备按钮
4. 验证技能出现在装备栏
5. 验证其他技能被自动卸下（如果槽位已被占用）

### 4. 技能使用流程
1. 进入战斗
2. 选择已装备的技能
3. 点击使用技能
4. 验证技能效果（伤害、治疗等）
5. 验证法力值扣除
6. 验证冷却时间生效

### 5. 技能商店流程
1. 进入技能商店
2. 查看可购买技能
3. 选择一个技能购买
4. 验证灵石扣除
5. 验证技能添加到玩家技能列表

## 边界测试

### 1. 等级不足测试
- 尝试学习等级要求高于当前等级的技能
- 预期结果: 返回"等级不足"错误

### 2. 灵石不足测试
- 尝试学习灵石要求高于当前灵石的技能
- 预期结果: 返回"灵石不足"错误

### 3. 已学习测试
- 尝试重复学习已学习的技能
- 预期结果: 返回"已学习该技能"错误

### 4. 最大等级测试
- 尝试升级已达到最大等级的技能
- 预期结果: 返回"技能已达到最大等级"错误

### 5. 技能点不足测试
- 尝试使用技能点升级但技能点不足
- 预期结果: 返回"技能点不足"错误

## 性能测试

### 1. 大量技能学习
- 批量学习多个技能
- 验证系统响应时间
- 验证数据库操作正确性

### 2. 高频技能使用
- 连续使用技能多次
- 验证冷却时间正确生效
- 验证法力值正确扣除

### 3. 技能升级性能
- 批量升级多个技能
- 验证系统响应时间
- 验证灵石扣除正确性

## 数据验证

### 1. 数据库验证
```sql
-- 查看所有技能
SELECT * FROM skills;

-- 查看玩家技能
SELECT * FROM player_skills WHERE player_id = ?;

-- 查看技能商店
SELECT * FROM skill_shop;
```

### 2. 业务逻辑验证
- 验证技能伤害计算正确
- 验证升级消耗计算正确
- 验证技能经验获取正确

## 前端测试

### 1. 技能页面测试
1. 打开技能页面
2. 查看技能列表是否正确显示
3. 查看玩家技能是否正确显示
4. 测试学习、升级、装备按钮功能

### 2. 战斗页面测试
1. 进入战斗页面
2. 查看已装备技能是否正确显示
3. 测试技能使用功能
4. 验证技能效果显示

## 自动化测试

### 1. 单元测试
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

### 2. 集成测试
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

## 测试报告模板

### 测试用例列表
| 用例ID | 测试项 | 测试步骤 | 预期结果 | 实际结果 | 状态 |
|--------|--------|----------|----------|----------|------|
| SKILL-001 | 学习技能 | 1. 登录游戏<br>2. 选择技能学习 | 成功学习技能 | | |
| SKILL-002 | 升级技能 | 1. 选择已学习技能<br>2. 点击升级 | 技能等级提升 | | |
| SKILL-003 | 装备技能 | 1. 选择技能<br>2. 选择槽位<br>3. 点击装备 | 技能装备成功 | | |

### 测试总结
- **测试总数**: XX
- **通过数**: XX
- **失败数**: XX
- **通过率**: XX%

### 问题列表
| 问题ID | 问题描述 | 严重程度 | 状态 |
|--------|----------|----------|------|
| ISSUE-001 | XXX | 高 | 待修复 |

## 测试注意事项

1. **数据备份**: 测试前备份数据库
2. **测试环境**: 使用测试账号，不要使用生产环境数据
3. **日志记录**: 开启详细日志记录
4. **性能监控**: 监控系统资源使用情况
5. **边界测试**: 重点关注边界条件测试