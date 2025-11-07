-- 修仙挂机游戏 MySQL 完整数据库初始化脚本
-- 版本: Java 8 + Spring Boot 2.7 + MySQL 8.0
-- 创建时间: 2025-10-31

-- 创建数据库
CREATE DATABASE IF NOT EXISTS xiuxian_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xiuxian_game;

-- ================================================
-- 1. 用户相关表
-- ================================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 玩家档案表
CREATE TABLE IF NOT EXISTS player_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    exp BIGINT NOT NULL DEFAULT 0,
    realm VARCHAR(50) NOT NULL DEFAULT '练气期',
    cultivation_speed DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    spirit_stones BIGINT NOT NULL DEFAULT 1000,
    cultivation_points BIGINT NOT NULL DEFAULT 0,
    contribution_points BIGINT NOT NULL DEFAULT 0,
    last_online_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_cultivation_time BIGINT NOT NULL DEFAULT 0,
    attack INT NOT NULL DEFAULT 10,
    defense INT NOT NULL DEFAULT 10,
    health INT NOT NULL DEFAULT 100,
    mana INT NOT NULL DEFAULT 50,
    speed INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_level (level),
    INDEX idx_realm (realm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 2. 游戏内容表
-- ================================================

-- 技能表
CREATE TABLE IF NOT EXISTS skills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    level INT NOT NULL,
    max_level INT NOT NULL,
    base_damage DOUBLE NOT NULL,
    damage_per_level DOUBLE NOT NULL,
    cooldown INT NOT NULL,
    mana_cost INT NOT NULL,
    skill_type VARCHAR(20) NOT NULL,
    element VARCHAR(10) NOT NULL,
    unlock_level INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_skill_type (skill_type),
    INDEX idx_element (element),
    INDEX idx_unlock_level (unlock_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 装备表
CREATE TABLE IF NOT EXISTS equipments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    level INT NOT NULL,
    quality INT NOT NULL,
    attack_bonus INT NOT NULL DEFAULT 0,
    defense_bonus INT NOT NULL DEFAULT 0,
    health_bonus INT NOT NULL DEFAULT 0,
    mana_bonus INT NOT NULL DEFAULT 0,
    speed_bonus INT NOT NULL DEFAULT 0,
    required_level INT NOT NULL,
    price BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_equipment_type (type),
    INDEX idx_quality (quality),
    INDEX idx_required_level (required_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 物品表
CREATE TABLE IF NOT EXISTS items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    quality INT NOT NULL,
    stackable BOOLEAN NOT NULL,
    max_stack INT NOT NULL,
    price BIGINT NOT NULL,
    sellable BOOLEAN NOT NULL,
    usable BOOLEAN NOT NULL,
    effect JSON,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_item_type (type),
    INDEX idx_quality (quality),
    INDEX idx_stackable (stackable)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 任务表
CREATE TABLE IF NOT EXISTS quests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    requirements JSON,
    rewards JSON,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_quest_type (type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商店物品表
CREATE TABLE IF NOT EXISTS shop_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shop_type VARCHAR(20) NOT NULL,
    item_id INT,
    equipment_id INT,
    price BIGINT NOT NULL,
    stock INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (equipment_id) REFERENCES equipments(id) ON DELETE CASCADE,
    INDEX idx_shop_type (shop_type),
    INDEX idx_item_id (item_id),
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 修炼境界表
CREATE TABLE IF NOT EXISTS cultivation_levels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    level INT NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    min_exp BIGINT NOT NULL,
    max_exp BIGINT NOT NULL,
    health_bonus INT NOT NULL,
    mana_bonus INT NOT NULL,
    attack_bonus INT NOT NULL,
    defense_bonus INT NOT NULL,
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 3. 玩家数据关联表
-- ================================================

-- 玩家技能表
CREATE TABLE IF NOT EXISTS player_skills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    skill_id INT NOT NULL,
    current_level INT NOT NULL DEFAULT 1,
    experience INT NOT NULL DEFAULT 0,
    is_equipped BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (player_id) REFERENCES player_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE KEY uk_player_skill (player_id, skill_id),
    INDEX idx_player_id (player_id),
    INDEX idx_skill_id (skill_id),
    INDEX idx_is_equipped (is_equipped)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 玩家装备表
CREATE TABLE IF NOT EXISTS player_equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    equipment_id INT NOT NULL,
    slot_type VARCHAR(20) NOT NULL,
    is_equipped BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (player_id) REFERENCES player_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (equipment_id) REFERENCES equipments(id) ON DELETE CASCADE,
    INDEX idx_player_id (player_id),
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_slot_type (slot_type),
    INDEX idx_is_equipped (is_equipped)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 玩家物品表
CREATE TABLE IF NOT EXISTS player_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (player_id) REFERENCES player_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    UNIQUE KEY uk_player_item (player_id, item_id),
    INDEX idx_player_id (player_id),
    INDEX idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 玩家任务表
CREATE TABLE IF NOT EXISTS player_quests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    quest_id INT NOT NULL,
    progress JSON,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    is_claimed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (player_id) REFERENCES player_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
    UNIQUE KEY uk_player_quest (player_id, quest_id),
    INDEX idx_player_id (player_id),
    INDEX idx_quest_id (quest_id),
    INDEX idx_is_completed (is_completed),
    INDEX idx_is_claimed (is_claimed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 修炼日志表
CREATE TABLE IF NOT EXISTS cultivation_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    cultivation_time INT NOT NULL,
    exp_gained INT NOT NULL,
    cultivation_points_gained INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (player_id) REFERENCES player_profiles(id) ON DELETE CASCADE,
    INDEX idx_player_id (player_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 4. 插入基础数据
-- ================================================

-- 插入基础技能数据
INSERT INTO skills (name, description, level, max_level, base_damage, damage_per_level, cooldown, mana_cost, skill_type, element, unlock_level, created_at, updated_at) VALUES
('基础功法', '提升基础修炼速度', 1, 100, 0.05, 0.01, 0, 0, 'cultivation', '无', 1, NOW(), NOW()),
('火球术', '基础火系攻击法术', 1, 50, 10.0, 2.0, 5, 10, 'attack', '火', 5, NOW(), NOW()),
('治疗术', '恢复生命值的法术', 1, 30, 20.0, 1.5, 8, 15, 'heal', '木', 3, NOW(), NOW()),
('水盾术', '创造一个水盾，减少受到的伤害', 1, 10, 0.0, 0.0, 10, 15, 'defense', '水', 8, NOW(), NOW()),
('地刺术', '从地面召唤尖刺，对敌人造成土属性伤害', 1, 10, 25.0, 10.0, 5, 20, 'attack', '土', 12, NOW(), NOW()),
('风刃术', '释放锋利的风刃，对敌人造成风属性伤害', 1, 10, 15.0, 7.0, 2, 8, 'attack', '风', 10, NOW(), NOW());

-- 插入基础装备数据
INSERT INTO equipments (name, description, type, level, quality, attack_bonus, defense_bonus, health_bonus, mana_bonus, speed_bonus, required_level, price, created_at, updated_at) VALUES
-- 1-10级装备
('木剑', '普通的木制法剑', 'weapon', 1, 1, 5, 0, 0, 0, 0, 1, 100, NOW(), NOW()),
('布袍', '简单的修炼道袍', 'chest', 1, 1, 0, 5, 50, 0, 0, 1, 150, NOW(), NOW()),
('草帽', '简单的草制帽子', 'helmet', 1, 1, 0, 2, 20, 0, 0, 1, 80, NOW(), NOW()),
('布鞋', '轻便的布制鞋子', 'boots', 1, 1, 0, 1, 10, 0, 2, 1, 60, NOW(), NOW()),
('木盾', '简单的木制盾牌', 'shield', 1, 1, 0, 8, 30, 0, 0, 1, 120, NOW(), NOW()),
-- 5-10级装备
('铁剑', '坚固的铁制长剑', 'weapon', 5, 2, 15, 0, 0, 0, 0, 5, 500, NOW(), NOW()),
('皮甲', '轻便的皮制护甲', 'chest', 5, 2, 0, 10, 100, 0, 0, 5, 600, NOW(), NOW()),
('铁盔', '坚固的铁制头盔', 'helmet', 5, 2, 0, 8, 50, 0, 0, 5, 400, NOW(), NOW()),
('皮靴', '结实的皮制靴子', 'boots', 5, 2, 0, 3, 30, 0, 5, 5, 350, NOW(), NOW()),
('铁盾', '坚固的铁制盾牌', 'shield', 5, 2, 0, 15, 80, 0, 0, 5, 550, NOW(), NOW()),
('玉符', '低级灵力护符', 'ring', 5, 2, 0, 0, 30, 20, 5, 5, 300, NOW(), NOW()),
-- 10-15级装备
('法杖', '蕴含灵力的法杖', 'weapon', 10, 3, 25, 0, 0, 50, 0, 10, 1200, NOW(), NOW()),
('道袍', '修仙者常穿的道袍', 'chest', 10, 3, 0, 15, 200, 30, 5, 10, 1500, NOW(), NOW()),
('道冠', '修仙者佩戴的道冠', 'helmet', 10, 3, 0, 12, 80, 20, 3, 10, 800, NOW(), NOW()),
('道靴', '修仙者专用的靴子', 'boots', 10, 3, 0, 5, 60, 10, 10, 10, 700, NOW(), NOW()),
('护心镜', '保护心脏的护镜', 'shield', 10, 3, 0, 20, 150, 10, 2, 10, 1000, NOW(), NOW()),
('灵戒', '蕴含灵力的戒指', 'ring', 10, 3, 5, 5, 50, 50, 10, 10, 800, NOW(), NOW()),
-- 15-20级装备
('银剑', '锋利的银制长剑', 'weapon', 15, 3, 35, 0, 0, 10, 2, 15, 2000, NOW(), NOW()),
('银甲', '闪亮的银制铠甲', 'chest', 15, 3, 0, 25, 300, 20, 5, 15, 2500, NOW(), NOW()),
('银盔', '精致的银制头盔', 'helmet', 15, 3, 0, 18, 120, 15, 5, 15, 1200, NOW(), NOW()),
('银靴', '轻便的银制靴子', 'boots', 15, 3, 0, 8, 80, 10, 15, 15, 1000, NOW(), NOW()),
('银盾', '坚固的银制盾牌', 'shield', 15, 3, 0, 30, 200, 5, 3, 15, 1800, NOW(), NOW()),
('银戒', '高级灵力戒指', 'ring', 15, 3, 8, 8, 80, 80, 15, 15, 1500, NOW(), NOW()),
-- 20-25级装备
('金剑', '珍贵的金制长剑', 'weapon', 20, 4, 50, 0, 0, 20, 5, 20, 3500, NOW(), NOW()),
('金甲', '华丽的金制铠甲', 'chest', 20, 4, 0, 35, 400, 30, 8, 20, 4000, NOW(), NOW()),
('金盔', '华丽的金制头盔', 'helmet', 20, 4, 0, 25, 150, 20, 8, 20, 2000, NOW(), NOW()),
('金靴', '华丽的金制靴子', 'boots', 20, 4, 0, 12, 100, 15, 20, 20, 1800, NOW(), NOW()),
('金盾', '华丽的金制盾牌', 'shield', 20, 4, 0, 40, 250, 10, 5, 20, 3000, NOW(), NOW()),
('金戒', '顶级灵力戒指', 'ring', 20, 4, 12, 12, 100, 100, 20, 20, 2500, NOW(), NOW());

-- 插入基础物品数据
INSERT INTO items (name, description, type, quality, stackable, max_stack, price, sellable, usable, effect, created_at, updated_at) VALUES
-- 消耗品
('疗伤丹', '恢复生命值的丹药', 'consumable', 1, 1, 99, 50, 1, 1, '{"heal": 50}', NOW(), NOW()),
('回灵丹', '恢复灵力的丹药', 'consumable', 1, 1, 99, 50, 1, 1, '{"restore_mana": 50}', NOW(), NOW()),
('经验丹', '提升经验值的丹药', 'consumable', 2, 1, 50, 200, 1, 1, '{"exp": 100}', NOW(), NOW()),
('突破丹', '帮助突破境界的丹药', 'consumable', 3, 1, 10, 1000, 1, 1, '{"breakthrough": 1}', NOW(), NOW()),
-- 材料
('灵草', '蕴含灵力的草药', 'material', 1, 1, 999, 10, 1, 0, '{}', NOW(), NOW()),
('灵石', '蕴含纯净灵力的石头', 'material', 2, 1, 999, 100, 1, 0, '{}', NOW(), NOW()),
('妖丹', '妖兽内丹，炼器材料', 'material', 3, 1, 99, 500, 1, 0, '{}', NOW(), NOW()),
('仙草', '传说中的仙草', 'material', 4, 1, 10, 2000, 1, 0, '{}', NOW(), NOW()),
-- 特殊物品
('新手礼包', '包含基础装备和物品的礼包', 'special', 1, 0, 1, 0, 0, 1, '{"items": [{"id": 1, "quantity": 1}, {"id": 2, "quantity": 5}]}', NOW(), NOW()),
('修炼心得', '记录修炼感悟的书籍', 'book', 2, 0, 1, 500, 1, 1, '{"cultivation_speed": 1.1}', NOW(), NOW());

-- 插入任务数据
INSERT INTO quests (title, description, type, requirements, rewards, is_active, created_at, updated_at) VALUES
('初入修仙', '完成新手引导，开始修仙之路', 'MAIN', '{"level": 1}', '{"exp": 100, "money": 100}', 1, NOW(), NOW()),
('每日修炼', '完成今日的修炼任务', 'DAILY', '{"cultivation_time": 3600}', '{"exp": 50, "money": 50}', 1, NOW(), NOW()),
('收集灵草', '收集10株灵草', 'DAILY', '{"collect_items": {"material": 1, "quantity": 10}}', '{"exp": 30, "money": 30}', 1, NOW(), NOW()),
('击败妖兽', '击败一只妖兽', 'DAILY', '{"kill_monsters": 1}', '{"exp": 80, "money": 80}', 1, NOW(), NOW()),
('商店购物', '在商店购买一件物品', 'DAILY', '{"buy_items": 1}', '{"exp": 20, "money": 20}', 1, NOW(), NOW());

-- 插入商店数据
INSERT INTO shop_items (shop_type, item_id, equipment_id, price, stock, is_active, created_at, updated_at) VALUES
-- 杂货铺
('general', 1, NULL, 50, 100, 1, NOW(), NOW()),
('general', 2, NULL, 50, 100, 1, NOW(), NOW()),
('general', 5, NULL, 10, 500, 1, NOW(), NOW()),
('general', 6, NULL, 100, 100, 1, NOW(), NOW()),
-- 功法阁
('skill', NULL, NULL, 1000, 10, 1, NOW(), NOW()),
('skill', NULL, NULL, 2000, 5, 1, NOW(), NOW()),
('skill', NULL, NULL, 5000, 1, 1, NOW(), NOW()),
-- 炼器铺
('equipment', NULL, 1, 100, 50, 1, NOW(), NOW()),
('equipment', NULL, 2, 150, 50, 1, NOW(), NOW()),
('equipment', NULL, 6, 500, 20, 1, NOW(), NOW()),
('equipment', NULL, 11, 1200, 10, 1, NOW(), NOW());

-- 插入修炼境界数据
INSERT INTO cultivation_levels (level, name, min_exp, max_exp, health_bonus, mana_bonus, attack_bonus, defense_bonus) VALUES
(1, '练气期一层', 0, 100, 100, 50, 0, 0),
(2, '练气期二层', 100, 250, 120, 60, 2, 1),
(3, '练气期三层', 250, 450, 140, 70, 4, 2),
(4, '练气期四层', 450, 700, 160, 80, 6, 3),
(5, '练气期五层', 700, 1000, 180, 90, 8, 4),
(6, '练气期六层', 1000, 1350, 200, 100, 10, 5),
(7, '练气期七层', 1350, 1750, 220, 110, 12, 6),
(8, '练气期八层', 1750, 2200, 240, 120, 14, 7),
(9, '练气期九层', 2200, 2700, 260, 130, 16, 8),
(10, '练气期十层', 2700, 3250, 280, 140, 18, 9),
(11, '筑基期一层', 3250, 4000, 350, 200, 25, 15),
(12, '筑基期二层', 4000, 4900, 400, 230, 30, 18),
(13, '筑基期三层', 4900, 6000, 450, 260, 35, 21),
(14, '筑基期四层', 6000, 7300, 500, 290, 40, 24),
(15, '筑基期五层', 7300, 8800, 550, 320, 45, 27),
(16, '金丹期一层', 8800, 11000, 700, 400, 60, 35),
(17, '金丹期二层', 11000, 13500, 800, 450, 70, 40),
(18, '金丹期三层', 13500, 16500, 900, 500, 80, 45),
(19, '金丹期四层', 16500, 20000, 1000, 550, 90, 50),
(20, '元婴期一层', 20000, 25000, 1300, 700, 120, 70);

-- ================================================
-- 5. 创建视图和存储过程（可选）
-- ================================================

-- 玩家综合信息视图
CREATE OR REPLACE VIEW v_player_summary AS
SELECT 
    pp.id,
    pp.user_id,
    u.username,
    pp.nickname,
    pp.level,
    pp.exp,
    pp.realm,
    pp.spirit_stones,
    pp.cultivation_points,
    pp.contribution_points,
    pp.attack,
    pp.defense,
    pp.health,
    pp.mana,
    pp.speed,
    pp.total_cultivation_time,
    pp.last_online_time,
    pp.created_at,
    pp.updated_at,
    (pp.attack + COALESCE(equ.attack_bonus, 0)) as total_attack,
    (pp.defense + COALESCE(equ.defense_bonus, 0)) as total_defense,
    (pp.health + COALESCE(equ.health_bonus, 0)) as total_health,
    (pp.mana + COALESCE(equ.mana_bonus, 0)) as total_mana,
    (pp.speed + COALESCE(equ.speed_bonus, 0)) as total_speed
FROM player_profiles pp
JOIN users u ON pp.user_id = u.id
LEFT JOIN (
    SELECT 
        player_id,
        SUM(attack_bonus) as attack_bonus,
        SUM(defense_bonus) as defense_bonus,
        SUM(health_bonus) as health_bonus,
        SUM(mana_bonus) as mana_bonus,
        SUM(speed_bonus) as speed_bonus
    FROM player_equipment pe
    JOIN equipments e ON pe.equipment_id = e.id
    WHERE pe.is_equipped = TRUE
    GROUP BY player_id
) equ ON pp.id = equ.player_id;

-- ================================================
-- 6. 完成
-- ================================================

-- 提交事务
COMMIT;

-- 显示创建成功的表
SHOW TABLES;

-- 显示各表的数据量
SELECT 
    TABLE_NAME as '表名',
    TABLE_ROWS as '数据行数'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'xiuxian_game'
ORDER BY TABLE_NAME;

-- ================================================
-- 数据库初始化完成！
-- ================================================
