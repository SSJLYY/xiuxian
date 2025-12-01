# 修仙挂机游戏 - 快速启动指南

## 5分钟快速启动

### 前置要求
- Java 1.8+
- MySQL 8.0+
- Maven 3.6+

### 步骤1: 数据库初始化（2分钟）
```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 执行以下SQL
CREATE DATABASE xiuxian_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xiuxian_game;
SOURCE src/main/resources/init-database.sql;
EXIT;
```

### 步骤2: 配置应用（1分钟）
编辑 `src/main/resources/application.properties`:
```properties
# 修改数据库密码
spring.datasource.password=YOUR_PASSWORD
```

### 步骤3: 启动应用（2分钟）
```bash
# 启动应用
mvn spring-boot:run

# 等待启动完成，看到以下日志表示成功：
# Started XiuxianGameApplication in X.XXX seconds
```

### 步骤4: 访问游戏
打开浏览器访问: http://localhost:8080/login.html

## 第一次使用

### 1. 注册账号
- 用户名: 任意（3-20个字符）
- 密码: 任意（6-20个字符）
- 邮箱: 任意有效邮箱格式
- 昵称: 游戏中显示的名称

### 2. 开始修炼
- 点击"开始修炼"按钮
- 等待一段时间
- 点击"停止修炼"查看收益

### 3. 查看属性
- 等级、境界、经验
- 攻击、防御、生命、法力、速度
- 灵石、修炼点数

### 4. 探索功能
- 任务系统：完成任务获得奖励
- 商店系统：购买物品和装备
- 技能系统：学习和升级技能
- 宠物系统：捕获和培养宠物

## 常见问题

### Q: 启动失败怎么办？
A: 检查以下几点：
1. MySQL服务是否启动
2. 数据库密码是否正确
3. 端口8080是否被占用
4. Java版本是否正确

### Q: 无法注册怎么办？
A: 检查：
1. 用户名是否已存在
2. 邮箱格式是否正确
3. 密码长度是否符合要求

### Q: 修炼没有收益？
A: 确保：
1. 修炼时间足够长（至少10秒）
2. 正确点击了"停止修炼"
3. 刷新页面查看最新数据

### Q: 如何重置数据？
A: 执行以下SQL：
```sql
USE xiuxian_game;
DELETE FROM player_profiles WHERE user_id IN (SELECT id FROM users WHERE username = 'YOUR_USERNAME');
DELETE FROM users WHERE username = 'YOUR_USERNAME';
```

## 游戏机制说明

### 修炼系统
- 每秒获得1点经验（基础）
- 修炼速度影响经验获得
- 累计经验达到要求时自动升级

### 升级系统
- 每级提升属性：攻击+5, 防御+3, 生命+20, 法力+10, 速度+1
- 升级所需经验翻倍
- 境界突破获得额外奖励

### 境界系统
- 1-10级：练气期
- 11-15级：筑基期
- 16-19级：金丹期
- 20级+：元婴期
- 境界突破奖励：属性点+5, 技能点+1

### 任务系统
- 每日任务：每天刷新
- 每周任务：每周刷新
- 每月任务：每月刷新
- 完成任务获得经验、灵石、贡献点

### 商店系统
- 物品商店：购买消耗品和材料
- 装备商店：购买装备
- 技能商店：学习新技能

### 宠物系统
- 捕获宠物：通过战斗或特殊途径
- 培养宠物：喂食、训练提升属性
- 宠物战斗：宠物可以参与战斗
- 宠物技能：宠物拥有独特技能

## 进阶玩法

### 1. 快速升级
- 持续修炼积累经验
- 完成每日任务
- 使用经验丹

### 2. 属性优化
- 合理分配属性点
- 装备高品质装备
- 学习被动技能

### 3. 资源管理
- 灵石用于购买物品和装备
- 贡献点用于兑换稀有物品
- 修炼点数用于提升修炼速度

### 4. 战斗策略
- 提升攻击力和防御力
- 学习战斗技能
- 培养强力宠物

## 技术支持

### 查看日志
```bash
# 实时查看日志
tail -f logs/xiuxian-game.log

# 搜索错误
grep ERROR logs/xiuxian-game.log
```

### 数据库查询
```sql
-- 查看玩家信息
SELECT * FROM player_profiles WHERE nickname = 'YOUR_NICKNAME';

-- 查看修炼日志
SELECT * FROM cultivation_logs WHERE player_id = YOUR_PLAYER_ID ORDER BY created_at DESC LIMIT 10;

-- 查看任务进度
SELECT * FROM player_quests WHERE player_id = YOUR_PLAYER_ID;
```

### 性能监控
```bash
# 检查应用进程
ps aux | grep java

# 检查端口占用
netstat -an | grep 8080

# 检查内存使用
free -h
```

## 更多资源

- 详细部署指南: DEPLOYMENT_CHECKLIST_UPDATED.md
- 系统测试指南: SYSTEM_TEST_GUIDE.md
- API文档: api-test.http
- 更新日志: update-0.1.3.md

## 反馈与建议

如果遇到问题或有改进建议，请：
1. 查看日志文件
2. 检查数据库状态
3. 参考故障排查文档
4. 联系开发团队

祝游戏愉快！
