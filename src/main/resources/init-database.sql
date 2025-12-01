/*
修仙挂机游戏数据库初始化脚本
清理版本 - 移除重复表定义，统一字段命名规范
Date: 2025-11-27
*/

SET FOREIGN_KEY_CHECKS=0;
SET NAMES utf8mb4;

-- ====================================================================
-- 用户系统
-- ====================================================================

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(加密)',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
  `must_change_password` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必须修改密码',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ====================================================================
-- 玩家系统
-- ====================================================================

-- ----------------------------
-- Table structure for player_profiles
-- ----------------------------
DROP TABLE IF EXISTS `player_profiles`;
CREATE TABLE `player_profiles` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `nickname` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `level` int NOT NULL DEFAULT '1' COMMENT '等级',
  `exp` bigint NOT NULL DEFAULT '0' COMMENT '当前经验',
  `exp_to_next` bigint NOT NULL DEFAULT '100' COMMENT '升级所需经验',
  `realm` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '练气期' COMMENT '境界',
  `cultivation_speed` decimal(10,2) NOT NULL DEFAULT '1.00' COMMENT '修炼速度',
  `spirit_stones` bigint NOT NULL DEFAULT '1000' COMMENT '灵石',
  `cultivation_points` bigint NOT NULL DEFAULT '0' COMMENT '修炼点数',
  `contribution_points` bigint NOT NULL DEFAULT '0' COMMENT '贡献点',
  `attribute_points` int NOT NULL DEFAULT '0' COMMENT '属性点',
  `skill_points` int NOT NULL DEFAULT '0' COMMENT '技能点',
  `last_online_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后在线时间',
  `last_login_at` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `total_cultivation_time` bigint NOT NULL DEFAULT '0' COMMENT '总修炼时间(秒)',
  `is_cultivating` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否正在修炼',
  `last_cultivation_start` timestamp NULL DEFAULT NULL COMMENT '最后修炼开始时间',
  `last_cultivation_end` timestamp NULL DEFAULT NULL COMMENT '最后修炼结束时间',
  `attack` int NOT NULL DEFAULT '10' COMMENT '基础攻击力',
  `defense` int NOT NULL DEFAULT '5' COMMENT '基础防御力',
  `health` int NOT NULL DEFAULT '100' COMMENT '基础生命值',
  `mana` int NOT NULL DEFAULT '50' COMMENT '基础法力值',
  `speed` int NOT NULL DEFAULT '10' COMMENT '基础速度',
  `equipment_attack_bonus` int NOT NULL DEFAULT '0' COMMENT '装备攻击加成',
  `equipment_defense_bonus` int NOT NULL DEFAULT '0' COMMENT '装备防御加成',
  `equipment_health_bonus` int NOT NULL DEFAULT '0' COMMENT '装备生命加成',
  `equipment_mana_bonus` int NOT NULL DEFAULT '0' COMMENT '装备法力加成',
  `equipment_speed_bonus` int NOT NULL DEFAULT '0' COMMENT '装备速度加成',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_level` (`level`),
  KEY `idx_realm` (`realm`),
  KEY `idx_last_online` (`last_online_time`),
  CONSTRAINT `fk_player_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家档案表';

-- ====================================================================
-- 修炼系统
-- ====================================================================

-- ----------------------------
-- Table structure for cultivation_levels
-- ----------------------------
DROP TABLE IF EXISTS `cultivation_levels`;
CREATE TABLE `cultivation_levels` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '境界ID',
  `level` int NOT NULL COMMENT '等级',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '境界名称',
  `min_exp` bigint NOT NULL COMMENT '最小经验',
  `max_exp` bigint NOT NULL COMMENT '最大经验',
  `health_bonus` int NOT NULL COMMENT '生命值加成',
  `mana_bonus` int NOT NULL COMMENT '法力值加成',
  `attack_bonus` int NOT NULL COMMENT '攻击力加成',
  `defense_bonus` int NOT NULL COMMENT '防御力加成',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level` (`level`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='修炼境界表';

-- ----------------------------
-- Records of cultivation_levels
-- ----------------------------
INSERT INTO `cultivation_levels` VALUES 
(1, 1, '练气期一层', 0, 100, 100, 50, 0, 0),
(2, 2, '练气期二层', 100, 250, 120, 60, 2, 1),
(3, 3, '练气期三层', 250, 450, 140, 70, 4, 2),
(4, 4, '练气期四层', 450, 700, 160, 80, 6, 3),
(5, 5, '练气期五层', 700, 1000, 180, 90, 8, 4),
(6, 6, '练气期六层', 1000, 1350, 200, 100, 10, 5),
(7, 7, '练气期七层', 1350, 1750, 220, 110, 12, 6),
(8, 8, '练气期八层', 1750, 2200, 240, 120, 14, 7),
(9, 9, '练气期九层', 2200, 2700, 260, 130, 16, 8),
(10, 10, '练气期十层', 2700, 3250, 280, 140, 18, 9),
(11, 11, '筑基期一层', 3250, 4000, 350, 200, 25, 15),
(12, 12, '筑基期二层', 4000, 4900, 400, 230, 30, 18),
(13, 13, '筑基期三层', 4900, 6000, 450, 260, 35, 21),
(14, 14, '筑基期四层', 6000, 7300, 500, 290, 40, 24),
(15, 15, '筑基期五层', 7300, 8800, 550, 320, 45, 27),
(16, 16, '金丹期一层', 8800, 11000, 700, 400, 60, 35),
(17, 17, '金丹期二层', 11000, 13500, 800, 450, 70, 40),
(18, 18, '金丹期三层', 13500, 16500, 900, 500, 80, 45),
(19, 19, '金丹期四层', 16500, 20000, 1000, 550, 90, 50),
(20, 20, '元婴期一层', 20000, 25000, 1300, 700, 120, 70);

-- ----------------------------
-- Table structure for cultivation_logs
-- ----------------------------
DROP TABLE IF EXISTS `cultivation_logs`;
CREATE TABLE `cultivation_logs` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `cultivation_time` int NOT NULL COMMENT '修炼时长(秒)',
  `cultivation_duration` bigint NOT NULL COMMENT '修炼持续时间(毫秒)',
  `exp_gained` int NOT NULL COMMENT '获得经验',
  `cultivation_points_gained` int NOT NULL COMMENT '获得修炼点',
  `spirit_stones_gained` int NOT NULL COMMENT '获得灵石',
  `is_offline` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否离线修炼',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_cultivation_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='修炼日志表';

-- ====================================================================
-- 技能系统
-- ====================================================================

-- ----------------------------
-- Table structure for skills
-- ----------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '技能ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '技能描述',
  `level` int NOT NULL DEFAULT '1' COMMENT '技能等级',
  `max_level` int NOT NULL DEFAULT '10' COMMENT '最大等级',
  `base_damage` double DEFAULT '0' COMMENT '基础伤害',
  `damage_per_level` double DEFAULT '0' COMMENT '每级伤害增长',
  `cooldown` int DEFAULT '0' COMMENT '冷却时间(秒)',
  `mana_cost` int DEFAULT '0' COMMENT '法力消耗',
  `skill_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '技能类型',
  `element` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '元素属性',
  `unlock_level` int DEFAULT '1' COMMENT '解锁等级',
  `required_spirit_stones` int DEFAULT '0' COMMENT '需要的灵石数量',
  `health_bonus` int DEFAULT '0' COMMENT '生命值加成',
  `mana_bonus` int DEFAULT '0' COMMENT '法力值加成',
  `attack_bonus` int DEFAULT '0' COMMENT '攻击力加成',
  `defense_bonus` int DEFAULT '0' COMMENT '防御力加成',
  `speed_bonus` int DEFAULT '0' COMMENT '速度加成',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `animation` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '动画',
  `active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_skill_type` (`skill_type`),
  KEY `idx_element` (`element`),
  KEY `idx_unlock_level` (`unlock_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能表';

-- ----------------------------
-- Records of skills
-- ----------------------------
INSERT INTO `skills` VALUES 
(1, '基础功法', '提升基础修炼速度', 1, 100, 0.05, 0.01, 0, 0, 'cultivation', '无', 1, 0, 0, 0, 0, 0, 0, NULL, NULL, 1, NOW(), NOW()),
(2, '火球术', '基础火系攻击法术', 1, 50, 10, 2, 5, 10, 'attack', '火', 5, 1000, 0, 0, 0, 0, 0, NULL, NULL, 1, NOW(), NOW()),
(3, '治疗术', '恢复生命值的法术', 1, 30, 20, 1.5, 8, 15, 'heal', '木', 3, 800, 0, 0, 0, 0, 0, NULL, NULL, 1, NOW(), NOW()),
(4, '水盾术', '创造一个水盾，减少受到的伤害', 1, 10, 0, 0, 10, 15, 'defense', '水', 8, 1200, 0, 0, 0, 0, 0, NULL, NULL, 1, NOW(), NOW()),
(5, '地刺术', '从地面召唤尖刺，对敌人造成土属性伤害', 1, 10, 25, 10, 5, 20, 'attack', '土', 12, 1500, 0, 0, 0, 0, 0, NULL, NULL, 1, NOW(), NOW()),
(6, '风刃术', '释放锋利的风刃，对敌人造成风属性伤害', 1, 10, 15, 7, 2, 8, 'attack', '风', 10, 1300, 0, 0, 0, 0, 0, NULL, NULL, 1, NOW(), NOW());

-- ----------------------------
-- Table structure for player_skills
-- ----------------------------
DROP TABLE IF EXISTS `player_skills`;
CREATE TABLE `player_skills` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家技能ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `current_level` int NOT NULL DEFAULT '1' COMMENT '当前等级',
  `level` int NOT NULL DEFAULT '1' COMMENT '等级',
  `experience` int NOT NULL DEFAULT '0' COMMENT '经验值',
  `is_equipped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否装备',
  `equipped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否装备',
  `slot_number` int NOT NULL DEFAULT '0' COMMENT '装备槽位',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_skill` (`player_id`,`skill_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_skill_id` (`skill_id`),
  KEY `idx_is_equipped` (`is_equipped`),
  CONSTRAINT `fk_player_skills_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家技能表';

-- ----------------------------
-- Table structure for skill_shop
-- ----------------------------
DROP TABLE IF EXISTS `skill_shop`;
CREATE TABLE `skill_shop` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商店ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `price` bigint NOT NULL COMMENT '价格',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `available` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_skill_id` (`skill_id`),
  KEY `idx_available` (`available`),
  CONSTRAINT `fk_skill_shop_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能商店表';

-- ----------------------------
-- Records of skill_shop
-- ----------------------------
INSERT INTO `skill_shop` VALUES 
(1, 1, 500, 1, 1, NOW(), NOW()),
(2, 2, 1000, 5, 1, NOW(), NOW()),
(3, 3, 800, 3, 1, NOW(), NOW()),
(4, 4, 1200, 8, 1, NOW(), NOW()),
(5, 5, 1500, 12, 1, NOW(), NOW()),
(6, 6, 1300, 10, 1, NOW(), NOW());

-- ====================================================================
-- 装备系统
-- ====================================================================

-- ----------------------------
-- Table structure for equipments
-- ----------------------------
DROP TABLE IF EXISTS `equipments`;
CREATE TABLE `equipments` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '装备ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备名称',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备描述',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备类型',
  `level` int NOT NULL COMMENT '装备等级',
  `quality` int NOT NULL COMMENT '品质',
  `attack_bonus` int NOT NULL DEFAULT '0' COMMENT '攻击加成',
  `defense_bonus` int NOT NULL DEFAULT '0' COMMENT '防御加成',
  `health_bonus` int NOT NULL DEFAULT '0' COMMENT '生命加成',
  `mana_bonus` int NOT NULL DEFAULT '0' COMMENT '法力加成',
  `speed_bonus` int NOT NULL DEFAULT '0' COMMENT '速度加成',
  `required_level` int NOT NULL COMMENT '需求等级',
  `price` bigint NOT NULL COMMENT '价格',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_type` (`type`),
  KEY `idx_quality` (`quality`),
  KEY `idx_required_level` (`required_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='装备表';

-- ----------------------------
-- Records of equipments
-- ----------------------------
INSERT INTO `equipments` VALUES 
(1, '木剑', '普通的木制法剑', 'weapon', 1, 1, 5, 0, 0, 0, 0, 1, 100, NOW(), NOW()),
(2, '布袍', '简单的修炼道袍', 'chest', 1, 1, 0, 5, 50, 0, 0, 1, 150, NOW(), NOW()),
(3, '草帽', '简单的草制帽子', 'helmet', 1, 1, 0, 2, 20, 0, 0, 1, 80, NOW(), NOW()),
(4, '布鞋', '轻便的布制鞋子', 'boots', 1, 1, 0, 1, 10, 0, 2, 1, 60, NOW(), NOW()),
(5, '木盾', '简单的木制盾牌', 'shield', 1, 1, 0, 8, 30, 0, 0, 1, 120, NOW(), NOW()),
(6, '铁剑', '坚固的铁制长剑', 'weapon', 5, 2, 15, 0, 0, 0, 0, 5, 500, NOW(), NOW()),
(7, '皮甲', '轻便的皮制护甲', 'chest', 5, 2, 0, 10, 100, 0, 0, 5, 600, NOW(), NOW()),
(8, '铁盔', '坚固的铁制头盔', 'helmet', 5, 2, 0, 8, 50, 0, 0, 5, 400, NOW(), NOW()),
(9, '皮靴', '结实的皮制靴子', 'boots', 5, 2, 0, 3, 30, 0, 5, 5, 350, NOW(), NOW()),
(10, '铁盾', '坚固的铁制盾牌', 'shield', 5, 2, 0, 15, 80, 0, 0, 5, 550, NOW(), NOW()),
(11, '玉符', '低级灵力护符', 'ring', 5, 2, 0, 0, 30, 20, 5, 5, 300, NOW(), NOW()),
(12, '法杖', '蕴含灵力的法杖', 'weapon', 10, 3, 25, 0, 0, 50, 0, 10, 1200, NOW(), NOW()),
(13, '道袍', '修仙者常穿的道袍', 'chest', 10, 3, 0, 15, 200, 30, 5, 10, 1500, NOW(), NOW()),
(14, '道冠', '修仙者佩戴的道冠', 'helmet', 10, 3, 0, 12, 80, 20, 3, 10, 800, NOW(), NOW()),
(15, '道靴', '修仙者专用的靴子', 'boots', 10, 3, 0, 5, 60, 10, 10, 10, 700, NOW(), NOW()),
(16, '护心镜', '保护心脏的护镜', 'shield', 10, 3, 0, 20, 150, 10, 2, 10, 1000, NOW(), NOW()),
(17, '灵戒', '蕴含灵力的戒指', 'ring', 10, 3, 5, 5, 50, 50, 10, 10, 800, NOW(), NOW()),
(18, '银剑', '锋利的银制长剑', 'weapon', 15, 3, 35, 0, 0, 10, 2, 15, 2000, NOW(), NOW()),
(19, '银甲', '闪亮的银制铠甲', 'chest', 15, 3, 0, 25, 300, 20, 5, 15, 2500, NOW(), NOW()),
(20, '银盔', '精致的银制头盔', 'helmet', 15, 3, 0, 18, 120, 15, 5, 15, 1200, NOW(), NOW()),
(21, '银靴', '轻便的银制靴子', 'boots', 15, 3, 0, 8, 80, 10, 15, 15, 1000, NOW(), NOW()),
(22, '银盾', '坚固的银制盾牌', 'shield', 15, 3, 0, 30, 200, 5, 3, 15, 1800, NOW(), NOW()),
(23, '银戒', '高级灵力戒指', 'ring', 15, 3, 8, 8, 80, 80, 15, 15, 1500, NOW(), NOW()),
(24, '金剑', '珍贵的金制长剑', 'weapon', 20, 4, 50, 0, 0, 20, 5, 20, 3500, NOW(), NOW()),
(25, '金甲', '华丽的金制铠甲', 'chest', 20, 4, 0, 35, 400, 30, 8, 20, 4000, NOW(), NOW()),
(26, '金盔', '华丽的金制头盔', 'helmet', 20, 4, 0, 25, 150, 20, 8, 20, 2000, NOW(), NOW()),
(27, '金靴', '华丽的金制靴子', 'boots', 20, 4, 0, 12, 100, 15, 20, 20, 1800, NOW(), NOW()),
(28, '金盾', '华丽的金制盾牌', 'shield', 20, 4, 0, 40, 250, 10, 5, 20, 3000, NOW(), NOW()),
(29, '金戒', '顶级灵力戒指', 'ring', 20, 4, 12, 12, 100, 100, 20, 20, 2500, NOW(), NOW());

-- ----------------------------
-- Table structure for player_equipment
-- ----------------------------
DROP TABLE IF EXISTS `player_equipment`;
CREATE TABLE `player_equipment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家装备ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `equipment_id` int NOT NULL COMMENT '装备ID',
  `slot` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备槽位',
  `is_equipped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否装备',
  `durability` int NOT NULL DEFAULT '100' COMMENT '当前耐久',
  `max_durability` int NOT NULL DEFAULT '100' COMMENT '最大耐久',
  `enhance_level` int NOT NULL DEFAULT '0' COMMENT '强化等级',
  `enhance_attack_bonus` int NOT NULL DEFAULT '0' COMMENT '强化攻击加成',
  `enhance_defense_bonus` int NOT NULL DEFAULT '0' COMMENT '强化防御加成',
  `enhance_health_bonus` int NOT NULL DEFAULT '0' COMMENT '强化生命加成',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_slot` (`slot`),
  KEY `idx_is_equipped` (`is_equipped`),
  CONSTRAINT `fk_player_equipment_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_equipment_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家装备表';

-- ====================================================================
-- 物品系统
-- ====================================================================

-- ----------------------------
-- Table structure for items
-- ----------------------------
DROP TABLE IF EXISTS `items`;
CREATE TABLE `items` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '物品ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品名称',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品描述',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型',
  `quality` int NOT NULL COMMENT '品质',
  `stackable` tinyint(1) NOT NULL COMMENT '是否可堆叠',
  `max_stack` int NOT NULL COMMENT '最大堆叠数',
  `price` bigint NOT NULL COMMENT '价格',
  `sellable` tinyint(1) NOT NULL COMMENT '是否可出售',
  `usable` tinyint(1) NOT NULL COMMENT '是否可使用',
  `effect` json DEFAULT NULL COMMENT '效果',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_type` (`type`),
  KEY `idx_quality` (`quality`),
  KEY `idx_stackable` (`stackable`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品表';

-- ----------------------------
-- Records of items
-- ----------------------------
INSERT INTO `items` VALUES 
(1, '疗伤丹', '恢复生命值的丹药', 'consumable', 1, 1, 99, 50, 1, 1, '{"heal": 50}', NOW(), NOW()),
(2, '回灵丹', '恢复灵力的丹药', 'consumable', 1, 1, 99, 50, 1, 1, '{"restore_mana": 50}', NOW(), NOW()),
(3, '经验丹', '提升经验值的丹药', 'consumable', 2, 1, 50, 200, 1, 1, '{"exp": 100}', NOW(), NOW()),
(4, '突破丹', '帮助突破境界的丹药', 'consumable', 3, 1, 10, 1000, 1, 1, '{"breakthrough": 1}', NOW(), NOW()),
(5, '灵草', '蕴含灵力的草药', 'material', 1, 1, 999, 10, 1, 0, '{}', NOW(), NOW()),
(6, '灵石', '蕴含纯净灵力的石头', 'material', 2, 1, 999, 100, 1, 0, '{}', NOW(), NOW()),
(7, '妖丹', '妖兽内丹，炼器材料', 'material', 3, 1, 99, 500, 1, 0, '{}', NOW(), NOW()),
(8, '仙草', '传说中的仙草', 'material', 4, 1, 10, 2000, 1, 0, '{}', NOW(), NOW()),
(9, '新手礼包', '包含基础装备和物品的礼包', 'special', 1, 0, 1, 0, 0, 1, '{"items": [{"id": 1, "quantity": 1}, {"id": 2, "quantity": 5}]}', NOW(), NOW()),
(10, '修炼心得', '记录修炼感悟的书籍', 'book', 2, 0, 1, 500, 1, 1, '{"cultivation_speed": 1.1}', NOW(), NOW());

-- ----------------------------
-- Table structure for player_items
-- ----------------------------
DROP TABLE IF EXISTS `player_items`;
CREATE TABLE `player_items` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家物品ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_item` (`player_id`,`item_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_item_id` (`item_id`),
  CONSTRAINT `fk_player_items_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_items_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家物品表';

-- ====================================================================
-- 任务系统
-- ====================================================================

-- ----------------------------
-- Table structure for quests
-- ----------------------------
DROP TABLE IF EXISTS `quests`;
CREATE TABLE `quests` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '任务描述',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型',
  `required_amount` int NOT NULL DEFAULT '1' COMMENT '需求数量',
  `reward_exp` int NOT NULL DEFAULT '0' COMMENT '奖励经验',
  `reward_spirit_stones` int NOT NULL DEFAULT '0' COMMENT '奖励灵石',
  `reward_contribution_points` int NOT NULL DEFAULT '0' COMMENT '奖励贡献点',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_quest_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- ----------------------------
-- Records of quests
-- ----------------------------
INSERT INTO `quests` VALUES 
(1, '每日修炼', '完成一次修炼', 'DAILY', 1, 100, 50, 10, NOW(), NOW()),
(2, '每日收集灵石', '获得100灵石', 'DAILY', 100, 120, 80, 12, NOW(), NOW()),
(3, '每周修炼进度', '累计修炼300秒', 'WEEKLY', 300, 800, 500, 50, NOW(), NOW()),
(4, '每周升级一次', '提升1级', 'WEEKLY', 1, 1000, 600, 60, NOW(), NOW()),
(5, '每月突破境界', '完成10次修炼', 'MONTHLY', 10, 3000, 2000, 200, NOW(), NOW());

-- ----------------------------
-- Table structure for player_quests
-- ----------------------------
DROP TABLE IF EXISTS `player_quests`;
CREATE TABLE `player_quests` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家任务ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `current_progress` int NOT NULL DEFAULT '0' COMMENT '当前进度',
  `completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成',
  `reward_claimed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否领取奖励',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_quest` (`player_id`,`quest_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_quest_id` (`quest_id`),
  KEY `idx_completed` (`completed`),
  KEY `idx_reward_claimed` (`reward_claimed`),
  CONSTRAINT `fk_player_quests_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_quests_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家任务表';

-- ====================================================================
-- 商店系统
-- ====================================================================

-- ----------------------------
-- Table structure for shop_items
-- ----------------------------
DROP TABLE IF EXISTS `shop_items`;
CREATE TABLE `shop_items` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商店物品ID',
  `shop_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商店类型',
  `item_id` int DEFAULT NULL COMMENT '物品ID',
  `equipment_id` int DEFAULT NULL COMMENT '装备ID',
  `price` bigint NOT NULL COMMENT '价格',
  `price_spirit_stones` int NOT NULL DEFAULT '0' COMMENT '灵石价格',
  `price_contribution_points` int NOT NULL DEFAULT '0' COMMENT '贡献点价格',
  `stock` int NOT NULL COMMENT '库存',
  `is_available` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_type` (`shop_type`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_is_available` (`is_available`),
  CONSTRAINT `fk_shop_items_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_shop_items_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商店物品表';

-- ----------------------------
-- Records of shop_items
-- ----------------------------
INSERT INTO `shop_items` VALUES 
(1, 'general', 1, NULL, 50, 50, 0, 100, 1, NOW(), NOW()),
(2, 'general', 2, NULL, 50, 50, 0, 100, 1, NOW(), NOW()),
(3, 'general', 5, NULL, 10, 10, 0, 500, 1, NOW(), NOW()),
(4, 'general', 6, NULL, 100, 100, 0, 100, 1, NOW(), NOW()),
(5, 'equipment', NULL, 1, 100, 100, 0, 50, 1, NOW(), NOW()),
(6, 'equipment', NULL, 2, 150, 150, 0, 50, 1, NOW(), NOW()),
(7, 'equipment', NULL, 6, 500, 500, 0, 20, 1, NOW(), NOW()),
(8, 'equipment', NULL, 12, 1200, 1200, 0, 10, 1, NOW(), NOW());

-- ====================================================================
-- 战斗系统
-- ====================================================================

-- ----------------------------
-- Table structure for monsters
-- ----------------------------
DROP TABLE IF EXISTS `monsters`;
CREATE TABLE `monsters` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '怪物ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '怪物名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '怪物描述',
  `level` int NOT NULL DEFAULT '1' COMMENT '等级',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '普通' COMMENT '类型',
  `health` int NOT NULL DEFAULT '100' COMMENT '生命值',
  `attack` int NOT NULL DEFAULT '10' COMMENT '攻击力',
  `defense` int NOT NULL DEFAULT '5' COMMENT '防御力',
  `speed` int NOT NULL DEFAULT '10' COMMENT '速度',
  `exp_reward` int NOT NULL DEFAULT '50' COMMENT '经验奖励',
  `spirit_stones_reward` int NOT NULL DEFAULT '10' COMMENT '灵石奖励',
  `drop_rate` int NOT NULL DEFAULT '10' COMMENT '掉落率',
  `drop_equipment_id` int DEFAULT NULL COMMENT '掉落装备ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_monster_level` (`level`),
  KEY `idx_monster_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='怪物表';

-- ----------------------------
-- Records of monsters
-- ----------------------------
INSERT INTO `monsters` VALUES 
(1, '野狼', '常见的野生狼', 1, '普通', 100, 10, 5, 10, 50, 10, 10, 1, NOW(), NOW()),
(2, '山贼', '路边的小贼', 2, '普通', 120, 12, 6, 12, 60, 12, 10, 2, NOW(), NOW()),
(3, '妖怪', '低级妖怪', 3, '普通', 150, 15, 8, 15, 80, 15, 10, 3, NOW(), NOW()),
(4, '邪修', '修炼邪法的修士', 5, '普通', 200, 20, 10, 18, 120, 20, 15, 6, NOW(), NOW()),
(5, '狂暴野狼', '狂暴的野生狼', 5, '精英', 300, 30, 15, 20, 200, 40, 20, 7, NOW(), NOW()),
(6, '山贼头目', '贼寇的首领', 8, '精英', 450, 40, 20, 25, 300, 60, 25, 12, NOW(), NOW()),
(7, '狼王', 'BOSS级别的狼群首领', 10, 'BOSS', 800, 60, 30, 30, 500, 100, 50, 18, NOW(), NOW()),
(8, '千年妖怪', '修炼千年的强大妖怪', 15, 'BOSS', 1500, 90, 45, 40, 800, 150, 50, 24, NOW(), NOW());

-- ----------------------------
-- Table structure for combat_logs
-- ----------------------------
DROP TABLE IF EXISTS `combat_logs`;
CREATE TABLE `combat_logs` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '战斗日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `monster_id` int DEFAULT NULL COMMENT '怪物ID',
  `result` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '战斗结果',
  `rounds` int NOT NULL DEFAULT '0' COMMENT '回合数',
  `exp_gained` int NOT NULL DEFAULT '0' COMMENT '获得经验',
  `spirit_stones_gained` int NOT NULL DEFAULT '0' COMMENT '获得灵石',
  `equipment_dropped` int DEFAULT NULL COMMENT '掉落装备ID',
  `battle_details` text COLLATE utf8mb4_unicode_ci COMMENT '战斗详情',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_combat_player_id` (`player_id`),
  KEY `idx_combat_created_at` (`created_at`),
  CONSTRAINT `fk_combat_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='战斗日志表';

-- ====================================================================
-- 离线奖励系统
-- ====================================================================

-- ----------------------------
-- Table structure for offline_rewards
-- ----------------------------
DROP TABLE IF EXISTS `offline_rewards`;
CREATE TABLE `offline_rewards` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '离线奖励ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `offline_minutes` int NOT NULL DEFAULT '0' COMMENT '离线分钟数',
  `exp_gained` int NOT NULL DEFAULT '0' COMMENT '获得经验',
  `spirit_stones_gained` int NOT NULL DEFAULT '0' COMMENT '获得灵石',
  `claimed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否领取',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `claimed_at` timestamp NULL DEFAULT NULL COMMENT '领取时间',
  PRIMARY KEY (`id`),
  KEY `idx_offline_player_id` (`player_id`),
  KEY `idx_offline_claimed` (`claimed`),
  CONSTRAINT `fk_offline_rewards_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='离线奖励表';

-- ====================================================================
-- 宠物系统
-- ====================================================================

-- ----------------------------
-- Table structure for pets
-- ----------------------------
DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '宠物名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '宠物描述',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '宠物类型',
  `rarity` int NOT NULL DEFAULT '1' COMMENT '稀有度',
  `base_attack` int NOT NULL DEFAULT '0' COMMENT '基础攻击力',
  `base_defense` int NOT NULL DEFAULT '0' COMMENT '基础防御力',
  `base_health` int NOT NULL DEFAULT '0' COMMENT '基础生命值',
  `base_speed` int NOT NULL DEFAULT '0' COMMENT '基础速度',
  `growth_rate` decimal(10,2) NOT NULL DEFAULT '1.00' COMMENT '成长率',
  `skill_id` int DEFAULT NULL COMMENT '宠物技能ID',
  `unlock_level` int NOT NULL DEFAULT '1' COMMENT '解锁等级',
  `capture_rate` decimal(5,2) NOT NULL DEFAULT '50.00' COMMENT '捕获概率',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_type` (`type`),
  KEY `idx_pet_rarity` (`rarity`),
  KEY `idx_pet_unlock_level` (`unlock_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物模板表';

-- ----------------------------
-- Records of pets
-- ----------------------------
INSERT INTO `pets` VALUES 
(1, '灵狐', '修炼千年的灵狐，擅长速度和敏捷', '灵兽', 2, 15, 10, 150, 25, 1.20, NULL, 10, 60.00, NULL, 1, NOW(), NOW()),
(2, '火麒麟', '传说中的神兽，拥有强大的火焰之力', '神兽', 4, 50, 40, 500, 20, 1.50, NULL, 50, 10.00, NULL, 1, NOW(), NOW()),
(3, '青龙', '四大神兽之一，掌控风雷之力', '神兽', 5, 80, 60, 800, 30, 2.00, NULL, 100, 5.00, NULL, 1, NOW(), NOW()),
(4, '白虎', '四大神兽之一，拥有无匹的攻击力', '神兽', 5, 100, 50, 700, 35, 2.00, NULL, 100, 5.00, NULL, 1, NOW(), NOW()),
(5, '玄武', '四大神兽之一，防御力惊人', '神兽', 5, 40, 100, 1000, 15, 2.00, NULL, 100, 5.00, NULL, 1, NOW(), NOW()),
(6, '朱雀', '四大神兽之一，掌控火焰', '神兽', 5, 90, 50, 750, 40, 2.00, NULL, 100, 5.00, NULL, 1, NOW(), NOW()),
(7, '小灵猫', '可爱的灵猫，适合新手培养', '灵兽', 1, 8, 5, 100, 20, 1.00, NULL, 1, 80.00, NULL, 1, NOW(), NOW()),
(8, '雷鹰', '掌控雷电的猛禽', '妖兽', 3, 35, 20, 300, 45, 1.30, NULL, 30, 30.00, NULL, 1, NOW(), NOW()),
(9, '冰霜狼', '来自极北之地的冰狼', '妖兽', 3, 30, 25, 350, 30, 1.30, NULL, 25, 35.00, NULL, 1, NOW(), NOW()),
(10, '金翅大鹏', '速度极快的神鸟', '神兽', 4, 60, 35, 600, 50, 1.60, NULL, 70, 15.00, NULL, 1, NOW(), NOW());

-- ----------------------------
-- Table structure for player_pets
-- ----------------------------
DROP TABLE IF EXISTS `player_pets`;
CREATE TABLE `player_pets` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家宠物ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `pet_id` int NOT NULL COMMENT '宠物模板ID',
  `nickname` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '宠物昵称',
  `level` int NOT NULL DEFAULT '1' COMMENT '宠物等级',
  `exp` bigint NOT NULL DEFAULT '0' COMMENT '宠物经验',
  `exp_to_next` bigint NOT NULL DEFAULT '100' COMMENT '升级所需经验',
  `attack` int NOT NULL DEFAULT '0' COMMENT '当前攻击力',
  `defense` int NOT NULL DEFAULT '0' COMMENT '当前防御力',
  `health` int NOT NULL DEFAULT '0' COMMENT '当前生命值',
  `max_health` int NOT NULL DEFAULT '0' COMMENT '最大生命值',
  `speed` int NOT NULL DEFAULT '0' COMMENT '当前速度',
  `loyalty` int NOT NULL DEFAULT '50' COMMENT '忠诚度',
  `hunger` int NOT NULL DEFAULT '100' COMMENT '饱食度',
  `is_active` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否出战',
  `is_locked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否锁定',
  `total_battles` int NOT NULL DEFAULT '0' COMMENT '总战斗次数',
  `total_wins` int NOT NULL DEFAULT '0' COMMENT '总胜利次数',
  `last_feed_time` timestamp NULL DEFAULT NULL COMMENT '最后喂食时间',
  `last_train_time` timestamp NULL DEFAULT NULL COMMENT '最后训练时间',
  `captured_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捕获时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_pet_player_id` (`player_id`),
  KEY `idx_player_pet_pet_id` (`pet_id`),
  KEY `idx_player_pet_active` (`is_active`),
  CONSTRAINT `fk_player_pets_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_pets_pet` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家宠物表';

-- ----------------------------
-- Table structure for pet_skills
-- ----------------------------
DROP TABLE IF EXISTS `pet_skills`;
CREATE TABLE `pet_skills` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '宠物技能ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '技能描述',
  `skill_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能类型',
  `base_damage` double NOT NULL DEFAULT '0' COMMENT '基础伤害',
  `damage_multiplier` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '伤害倍率',
  `cooldown` int NOT NULL DEFAULT '0' COMMENT '冷却时间',
  `energy_cost` int NOT NULL DEFAULT '0' COMMENT '能量消耗',
  `unlock_pet_level` int NOT NULL DEFAULT '1' COMMENT '宠物解锁等级',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_skill_type` (`skill_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物技能表';

-- ----------------------------
-- Records of pet_skills
-- ----------------------------
INSERT INTO `pet_skills` VALUES 
(1, '撕咬', '基础物理攻击', '攻击', 20, 1.00, 3, 10, 1, 1, NOW(), NOW()),
(2, '火焰吐息', '喷射火焰攻击敌人', '攻击', 50, 1.50, 8, 30, 10, 1, NOW(), NOW()),
(3, '雷霆一击', '召唤雷电攻击', '攻击', 80, 2.00, 12, 50, 20, 1, NOW(), NOW()),
(4, '治愈之光', '恢复主人生命值', '辅助', 0, 0.00, 15, 40, 15, 1, NOW(), NOW()),
(5, '护盾', '为主人提供护盾', '防御', 0, 0.00, 20, 35, 12, 1, NOW(), NOW());

-- ----------------------------
-- Table structure for player_pet_skills
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_skills`;
CREATE TABLE `player_pet_skills` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家宠物技能ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `pet_skill_id` int NOT NULL COMMENT '宠物技能ID',
  `skill_level` int NOT NULL DEFAULT '1' COMMENT '技能等级',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_pet_skill` (`player_pet_id`,`pet_skill_id`),
  KEY `idx_player_pet_id` (`player_pet_id`),
  KEY `idx_pet_skill_id` (`pet_skill_id`),
  CONSTRAINT `fk_player_pet_skills_player_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_pet_skills_pet_skill` FOREIGN KEY (`pet_skill_id`) REFERENCES `pet_skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家宠物技能表';

-- ----------------------------
-- Table structure for pet_training_logs
-- ----------------------------
DROP TABLE IF EXISTS `pet_training_logs`;
CREATE TABLE `pet_training_logs` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '训练日志ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `training_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '训练类型',
  `exp_gained` int NOT NULL DEFAULT '0' COMMENT '获得经验',
  `attribute_improved` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '提升的属性',
  `improvement_value` int NOT NULL DEFAULT '0' COMMENT '提升值',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_pet_id` (`player_pet_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_pet_training_logs_player_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物训练日志表';

-- ====================================================================
-- 视图
-- ====================================================================

-- ----------------------------
-- View structure for v_player_summary
-- ----------------------------
DROP VIEW IF EXISTS `v_player_summary`;
CREATE VIEW `v_player_summary` AS 
SELECT 
  pp.id,
  pp.user_id,
  u.username,
  pp.nickname,
  pp.level,
  pp.exp,
  pp.exp_to_next,
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
  (pp.attack + COALESCE(equ.attack_bonus, 0)) AS total_attack,
  (pp.defense + COALESCE(equ.defense_bonus, 0)) AS total_defense,
  (pp.health + COALESCE(equ.health_bonus, 0)) AS total_health,
  (pp.mana + COALESCE(equ.mana_bonus, 0)) AS total_mana,
  (pp.speed + COALESCE(equ.speed_bonus, 0)) AS total_speed
FROM player_profiles pp
JOIN users u ON pp.user_id = u.id
LEFT JOIN (
  SELECT 
    pe.player_id,
    SUM(e.attack_bonus) AS attack_bonus,
    SUM(e.defense_bonus) AS defense_bonus,
    SUM(e.health_bonus) AS health_bonus,
    SUM(e.mana_bonus) AS mana_bonus,
    SUM(e.speed_bonus) AS speed_bonus
  FROM player_equipment pe
  JOIN equipments e ON pe.equipment_id = e.id
  WHERE pe.is_equipped = 1
  GROUP BY pe.player_id
) equ ON pp.id = equ.player_id;

SET FOREIGN_KEY_CHECKS=1;




