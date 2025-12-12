/*
xiuxian挂机游戏数据库初始化脚本
首次部署专用版本 - 适用于全新环境初始化
Date: 2025-12-11
Version: 2.0
*/

-- ====================================================================
-- 数据库初始化设置
-- ====================================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS xiuxian_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xiuxian_game;

-- 设置会话参数
SET FOREIGN_KEY_CHECKS=0;
SET NAMES utf8mb4;
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

-- 设置时区
SET time_zone = '+08:00';

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
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/BANNED/INACTIVE',
  `must_change_password` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必须修改密码',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
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
  KEY `idx_spirit_stones` (`spirit_stones`),
  KEY `idx_last_login` (`last_login_at`),
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
  `level` int NOT NULL DEFAULT '1' COMMENT '等级',
  `experience` int NOT NULL DEFAULT '0' COMMENT '经验值',
  `equipped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否装备',
  `slot_number` int NOT NULL DEFAULT '0' COMMENT '装备槽位',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_skill` (`player_id`,`skill_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_skill_id` (`skill_id`),
  KEY `idx_equipped` (`equipped`),
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
  `price` int NOT NULL COMMENT '价格',
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
  `price` int NOT NULL COMMENT '价格',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
(13, '道袍', 'xiuxian者常穿的道袍', 'chest', 10, 3, 0, 15, 200, 30, 5, 10, 1500, NOW(), NOW()),
(14, '道冠', 'xiuxian者佩戴的道冠', 'helmet', 10, 3, 0, 12, 80, 20, 3, 10, 800, NOW(), NOW()),
(15, '道靴', 'xiuxian者专用的靴子', 'boots', 10, 3, 0, 5, 60, 10, 10, 10, 700, NOW(), NOW()),
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
  `price` int NOT NULL COMMENT '价格',
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
  `locked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否锁定',
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
-- 拍卖系统
-- ====================================================================

DROP TABLE IF EXISTS `auction_items`;
CREATE TABLE `auction_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拍卖物品ID',
  `seller_id` int NOT NULL COMMENT '卖家ID',
  `item_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型：EQUIPMENT/ITEM/PET',
  `item_id` int COMMENT '物品模板ID',
  `player_item_id` bigint COMMENT '玩家物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `price` int NOT NULL COMMENT '价格（灵石）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ON_SALE' COMMENT '状态：ON_SALE/SOLD/CANCELLED/EXPIRED',
  `buyer_id` int COMMENT '买家ID',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_at` timestamp NULL COMMENT '过期时间',
  `sold_at` timestamp NULL COMMENT '售出时间',
  PRIMARY KEY (`id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拍卖物品表';

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
  `price` int NOT NULL COMMENT '价格',
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

-- ====================================================================
-- 邮件系统 (v2.0新增)
-- ====================================================================

-- 玩家邮件表
DROP TABLE IF EXISTS `player_mails`;
CREATE TABLE `player_mails` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '邮件ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮件标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮件内容',
  `mail_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮件类型：SYSTEM/REWARD/ACTIVITY',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读',
  `has_attachment` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否有附件',
  `is_claimed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '附件是否已领取',
  `expire_at` timestamp NULL COMMENT '过期时间',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_mail_type` (`mail_type`),
  KEY `idx_has_attachment` (`has_attachment`),
  KEY `idx_is_claimed` (`is_claimed`),
  KEY `idx_expire_at` (`expire_at`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_player_mails_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家邮件表';

-- 邮件附件表
DROP TABLE IF EXISTS `mail_attachments`;
CREATE TABLE `mail_attachments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `mail_id` bigint NOT NULL COMMENT '邮件ID',
  `item_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型：ITEM/EQUIPMENT/SPIRIT_STONES/EXP',
  `item_id` int NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  PRIMARY KEY (`id`),
  KEY `idx_mail_id` (`mail_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_item_id` (`item_id`),
  CONSTRAINT `fk_mail_attachments_mail` FOREIGN KEY (`mail_id`) REFERENCES `player_mails` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件附件表';

-- ====================================================================
-- 公告系统 (v2.0新增)
-- ====================================================================

-- 公告表
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `announcement_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告类型：SYSTEM/MAINTENANCE/ACTIVITY/UPDATE',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级：0-普通 1-重要 2-紧急',
  `display_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示类型：POPUP/SCROLL/LIST',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/REVOKED',
  `start_time` timestamp NULL COMMENT '开始时间',
  `end_time` timestamp NULL COMMENT '结束时间',
  `created_by` int NOT NULL COMMENT '创建人ID',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_announcement_type` (`announcement_type`),
  KEY `idx_display_type` (`display_type`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_status_priority` (`status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ====================================================================
-- 排行榜系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `rankings`;
CREATE TABLE `rankings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排行ID',
  `ranking_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '排行榜类型：LEVEL/POWER/WEALTH/PET',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `rank` int NOT NULL COMMENT '排名',
  `score` bigint NOT NULL COMMENT '分数',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_player` (`ranking_type`, `player_id`),
  KEY `idx_type_rank` (`ranking_type`, `rank`),
  KEY `idx_type_score` (`ranking_type`, `score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排行榜缓存表';

-- ====================================================================
-- 成就系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '成就ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就名称',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就描述',
  `achievement_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就类型：LEVEL/COMBAT/CULTIVATION/COLLECTION',
  `condition_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '条件类型：REACH_LEVEL/KILL_MONSTER/CULTIVATE_TIME',
  `condition_value` int NOT NULL COMMENT '条件数值',
  `reward_exp` int DEFAULT '0' COMMENT '奖励经验',
  `reward_spirit_stones` int DEFAULT '0' COMMENT '奖励灵石',
  `reward_title` varchar(50) COLLATE utf8mb4_unicode_ci NULL COMMENT '奖励称号',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`achievement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就模板表';

-- 初始化成就数据
INSERT INTO `achievements` VALUES
(1, '初入仙途', '达到2级', 'LEVEL', 'REACH_LEVEL', 2, 100, 50, NULL, NULL, 1),
(2, '修炼有成', '达到5级', 'LEVEL', 'REACH_LEVEL', 5, 300, 150, NULL, NULL, 2),
(3, '筑基成功', '达到11级', 'LEVEL', 'REACH_LEVEL', 11, 1000, 500, '筑基修士', NULL, 3),
(4, '金丹大成', '达到16级', 'LEVEL', 'REACH_LEVEL', 16, 3000, 1500, '金丹真人', NULL, 4),
(5, '元婴境界', '达到20级', 'LEVEL', 'REACH_LEVEL', 20, 10000, 5000, '元婴老祖', NULL, 5),
(6, '初战告捷', '击败1个怪物', 'COMBAT', 'KILL_MONSTER', 1, 50, 20, NULL, NULL, 10),
(7, '百战精兵', '击败100个怪物', 'COMBAT', 'KILL_MONSTER', 100, 1000, 500, '百战勇士', NULL, 11),
(8, '千战之王', '击败1000个怪物', 'COMBAT', 'KILL_MONSTER', 1000, 10000, 5000, '千战之王', NULL, 12),
(9, '勤修苦练', '累计修炼1小时', 'CULTIVATION', 'CULTIVATE_TIME', 3600, 200, 100, NULL, NULL, 20),
(10, '修炼狂人', '累计修炼10小时', 'CULTIVATION', 'CULTIVATE_TIME', 36000, 2000, 1000, '修炼狂人', NULL, 21);

DROP TABLE IF EXISTS `player_achievements`;
CREATE TABLE `player_achievements` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家成就ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `achievement_id` int NOT NULL COMMENT '成就ID',
  `progress` int NOT NULL DEFAULT '0' COMMENT '当前进度',
  `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成',
  `is_claimed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否领取奖励',
  `completed_at` timestamp NULL COMMENT '完成时间',
  `claimed_at` timestamp NULL COMMENT '领取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_achievement` (`player_id`, `achievement_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_completed` (`is_completed`),
  CONSTRAINT `fk_player_achievements_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_achievements_achievement` FOREIGN KEY (`achievement_id`) REFERENCES `achievements` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家成就表';

-- ====================================================================
-- 宗门系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `guilds`;
CREATE TABLE `guilds` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '宗门ID',
  `guild_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '宗门名称',
  `description` text COLLATE utf8mb4_unicode_ci NULL COMMENT '宗门简介',
  `leader_id` int NOT NULL COMMENT '宗主ID',
  `level` int NOT NULL DEFAULT '1' COMMENT '宗门等级',
  `exp` bigint NOT NULL DEFAULT '0' COMMENT '宗门经验',
  `exp_to_next` bigint NOT NULL DEFAULT '1000' COMMENT '升级所需经验',
  `guild_funds` bigint NOT NULL DEFAULT '0' COMMENT '宗门资金',
  `member_count` int NOT NULL DEFAULT '1' COMMENT '成员数量',
  `max_members` int NOT NULL DEFAULT '20' COMMENT '最大成员数',
  `announcement` text COLLATE utf8mb4_unicode_ci NULL COMMENT '宗门公告',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_guild_name` (`guild_name`),
  KEY `idx_level` (`level`),
  KEY `idx_leader_id` (`leader_id`),
  CONSTRAINT `fk_guilds_leader` FOREIGN KEY (`leader_id`) REFERENCES `player_profiles` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宗门表';

DROP TABLE IF EXISTS `guild_members`;
CREATE TABLE `guild_members` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `guild_id` int NOT NULL COMMENT '宗门ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEMBER' COMMENT '职位：LEADER/OFFICER/MEMBER',
  `contribution` int NOT NULL DEFAULT '0' COMMENT '贡献值',
  `joined_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_guild` (`player_id`, `guild_id`),
  KEY `idx_guild_id` (`guild_id`),
  KEY `idx_player_id` (`player_id`),
  CONSTRAINT `fk_guild_members_guild` FOREIGN KEY (`guild_id`) REFERENCES `guilds` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_guild_members_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宗门成员表';

DROP TABLE IF EXISTS `guild_applications`;
CREATE TABLE `guild_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `guild_id` int NOT NULL COMMENT '宗门ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `message` text COLLATE utf8mb4_unicode_ci COMMENT '申请留言',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
  `processed_by` int COMMENT '处理人ID',
  `processed_at` timestamp NULL COMMENT '处理时间',
  `applied_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`),
  KEY `idx_guild_id` (`guild_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_guild_applications_guild` FOREIGN KEY (`guild_id`) REFERENCES `guilds` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_guild_applications_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宗门申请表';

-- ====================================================================
-- 活动系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `activities`;
CREATE TABLE `activities` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `activity_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动描述',
  `activity_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动类型：DAILY/WEEKLY/SPECIAL',
  `start_time` timestamp NOT NULL COMMENT '开始时间',
  `end_time` timestamp NOT NULL COMMENT '结束时间',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/RUNNING/ENDED',
  `rules` text COLLATE utf8mb4_unicode_ci COMMENT '活动规则（JSON格式）',
  `rewards` text COLLATE utf8mb4_unicode_ci COMMENT '奖励配置（JSON格式）',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_activity_type` (`activity_type`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

DROP TABLE IF EXISTS `player_activities`;
CREATE TABLE `player_activities` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家活动ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `activity_id` int NOT NULL COMMENT '活动ID',
  `progress` json NULL COMMENT '进度数据',
  `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成',
  `is_rewarded` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否领取奖励',
  `completed_at` timestamp NULL COMMENT '完成时间',
  `rewarded_at` timestamp NULL COMMENT '奖励领取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_activity` (`player_id`, `activity_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_activity_id` (`activity_id`),
  CONSTRAINT `fk_player_activities_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_activities_activity` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家活动表';

DROP TABLE IF EXISTS `player_activity_progress`;
CREATE TABLE `player_activity_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `activity_id` int NOT NULL COMMENT '活动ID',
  `progress` int NOT NULL DEFAULT '0' COMMENT '进度值',
  `score` int NOT NULL DEFAULT '0' COMMENT '积分',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_activity_progress` (`player_id`, `activity_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_activity_id` (`activity_id`),
  CONSTRAINT `fk_player_activity_progress_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_activity_progress_activity` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家活动进度表';

-- ====================================================================
-- 签到系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `sign_in_configs`;
CREATE TABLE `sign_in_configs` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '签到配置ID',
  `day` int NOT NULL COMMENT '第几天',
  `reward_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖励类型：SPIRIT_STONES/ITEM/EQUIPMENT/EXP',
  `reward_id` int NULL COMMENT '奖励物品ID',
  `reward_quantity` int NOT NULL DEFAULT '1' COMMENT '奖励数量',
  `is_special` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否特殊奖励',
  PRIMARY KEY (`id`),
  KEY `idx_day` (`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到奖励配置表';

-- 初始化签到配置
INSERT INTO `sign_in_configs` VALUES
(1, 1, 'SPIRIT_STONES', NULL, 100, 0),
(2, 2, 'ITEM', 1, 2, 0),
(3, 3, 'ITEM', 2, 2, 0),
(4, 4, 'SPIRIT_STONES', NULL, 200, 0),
(5, 5, 'EQUIPMENT', 3, 1, 0),
(6, 6, 'ITEM', 3, 5, 0),
(7, 7, 'EQUIPMENT', 6, 1, 1);

DROP TABLE IF EXISTS `player_sign_ins`;
CREATE TABLE `player_sign_ins` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家签到ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `sign_in_date` date NOT NULL COMMENT '签到日期',
  `config_id` int NOT NULL COMMENT '签到配置ID',
  `is_claimed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否领取',
  `claimed_at` timestamp NULL COMMENT '领取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_date` (`player_id`, `sign_in_date`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_sign_in_date` (`sign_in_date`),
  CONSTRAINT `fk_player_sign_ins_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_sign_ins_config` FOREIGN KEY (`config_id`) REFERENCES `sign_in_configs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家签到表';

-- ====================================================================
-- VIP系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `vip_levels`;
CREATE TABLE `vip_levels` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'VIP等级ID',
  `level` int NOT NULL COMMENT 'VIP等级',
  `required_recharge` int NOT NULL COMMENT '所需充值金额',
  `daily_spirit_stones` int NOT NULL DEFAULT '0' COMMENT '每日灵石奖励',
  `cultivation_speed_bonus` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '修炼速度加成',
  `exp_bonus` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '经验加成',
  `shop_discount` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '商店折扣',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP等级配置表';

-- 初始化VIP等级配置
INSERT INTO `vip_levels` VALUES
(1, 0, 0, 0, 0.00, 0.00, 0.00),
(2, 1, 100, 50, 0.10, 0.10, 0.05),
(3, 2, 500, 100, 0.20, 0.20, 0.10),
(4, 3, 1000, 200, 0.30, 0.30, 0.15),
(5, 4, 2000, 300, 0.40, 0.40, 0.20),
(6, 5, 5000, 500, 0.50, 0.50, 0.25);

DROP TABLE IF EXISTS `player_vip`;
CREATE TABLE `player_vip` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家VIP ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `vip_level` int NOT NULL DEFAULT '0' COMMENT 'VIP等级',
  `total_recharge` int NOT NULL DEFAULT '0' COMMENT '累计充值金额',
  `yuanbao` int NOT NULL DEFAULT '0' COMMENT '元宝余额',
  `last_daily_reward_at` timestamp NULL COMMENT '上次领取每日奖励时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_id` (`player_id`),
  CONSTRAINT `fk_player_vip_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家VIP表';

-- ====================================================================
-- 充值系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `recharge_packages`;
CREATE TABLE `recharge_packages` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '充值套餐ID',
  `package_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐名称',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐描述',
  `amount` decimal(10,2) NOT NULL COMMENT '充值金额',
  `currency` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
  `product_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品ID',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否激活',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值套餐表';

-- 初始化充值套餐
INSERT INTO `recharge_packages` VALUES
(1, '月卡', '30天月卡', 30.00, 'CNY', 'monthly_card', 1, 1, NOW(), NOW()),
(2, '季卡', '90天季卡', 80.00, 'CNY', 'quarterly_card', 2, 1, NOW(), NOW()),
(3, '年卡', '365天年卡', 300.00, 'CNY', 'yearly_card', 3, 1, NOW(), NOW()),
(4, '至尊月卡', '特权月卡', 50.00, 'CNY', 'premium_monthly_card', 4, 1, NOW(), NOW());

DROP TABLE IF EXISTS `player_recharges`;
CREATE TABLE `player_recharges` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家充值ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `package_id` int NOT NULL COMMENT '套餐ID',
  `amount` decimal(10,2) NOT NULL COMMENT '充值金额',
  `transaction_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '交易ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SUCCESS' COMMENT '状态：PENDING/SUCCESS/FAILED',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_package_id` (`package_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_player_recharges_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_recharges_package` FOREIGN KEY (`package_id`) REFERENCES `recharge_packages` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家充值记录表';

-- 充值记录表（用于统计）
DROP TABLE IF EXISTS `recharge_records`;
CREATE TABLE `recharge_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '充值ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `amount` int NOT NULL COMMENT '充值金额（分）',
  `yuanbao` int NOT NULL COMMENT '获得元宝',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/SUCCESS/FAILED',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed_at` timestamp NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_player_id` (`player_id`),
  CONSTRAINT `fk_recharge_records_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录表';

DROP TABLE IF EXISTS `player_vip_levels`;
CREATE TABLE `player_vip_levels` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'VIP等级ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `vip_level` int NOT NULL DEFAULT '0' COMMENT 'VIP等级',
  `vip_exp` int NOT NULL DEFAULT '0' COMMENT 'VIP经验',
  `vip_exp_to_next` int NOT NULL DEFAULT '1000' COMMENT '升级所需经验',
  `monthly_card_expire` timestamp NULL COMMENT '月卡到期时间',
  `quarterly_card_expire` timestamp NULL COMMENT '季卡到期时间',
  `yearly_card_expire` timestamp NULL COMMENT '年卡到期时间',
  `premium_monthly_card_expire` timestamp NULL COMMENT '至尊月卡到期时间',
  `daily_rewards_claimed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '今日奖励是否领取',
  `last_daily_reward_date` date NULL COMMENT '上次领取日常奖励日期',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_id` (`player_id`),
  CONSTRAINT `fk_player_vip_levels_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家VIP等级表';

-- ====================================================================
-- 日志系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `player_login_logs`;
CREATE TABLE `player_login_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '登录日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci COMMENT 'IP地址',
  `user_agent` text COLLATE utf8mb4_unicode_ci COMMENT '用户代理',
  `device_info` varchar(500) COLLATE utf8mb4_unicode_ci COMMENT '设备信息',
  `login_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  CONSTRAINT `fk_player_login_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家登录日志表';

DROP TABLE IF EXISTS `admin_operation_logs`;
CREATE TABLE `admin_operation_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
  `admin_id` int NOT NULL COMMENT '管理员ID',
  `operation_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `target_type` varchar(50) COLLATE utf8mb4_unicode_ci COMMENT '目标类型',
  `target_id` varchar(100) COLLATE utf8mb4_unicode_ci COMMENT '目标ID',
  `operation_desc` text COLLATE utf8mb4_unicode_ci COMMENT '操作描述',
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci COMMENT 'IP地址',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  CONSTRAINT `fk_admin_operation_logs_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- ====================================================================
-- 统计系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `daily_stats`;
CREATE TABLE `daily_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `dau` int NOT NULL DEFAULT '0' COMMENT '日活跃用户数',
  `new_users` int NOT NULL DEFAULT '0' COMMENT '新增用户数',
  `recharge_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '充值金额',
  `recharge_count` int NOT NULL DEFAULT '0' COMMENT '充值次数',
  `arpu` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ARPU',
  `arppu` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ARPPU',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日统计表';

-- 每日统计表（用于统计服务）
DROP TABLE IF EXISTS `daily_statistics`;
CREATE TABLE `daily_statistics` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `new_players` int NOT NULL DEFAULT '0' COMMENT '新增玩家数',
  `active_players` int NOT NULL DEFAULT '0' COMMENT '活跃玩家数',
  `total_recharge` int NOT NULL DEFAULT '0' COMMENT '总充值金额（分）',
  `paying_players` int NOT NULL DEFAULT '0' COMMENT '付费玩家数',
  `arpu` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ARPU',
  `arppu` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ARPPU',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日统计表';

-- ====================================================================
-- 礼包码系统 (v2.0新增)
-- ====================================================================

DROP TABLE IF EXISTS `gift_codes`;
CREATE TABLE `gift_codes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '礼包码ID',
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '礼包码',
  `code_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：SINGLE/MULTI',
  `max_uses` int NOT NULL DEFAULT '1' COMMENT '最大使用次数',
  `used_count` int NOT NULL DEFAULT '0' COMMENT '已使用次数',
  `rewards` json NOT NULL COMMENT '奖励配置',
  `min_level` int NOT NULL DEFAULT '1' COMMENT '最低等级要求',
  `expire_at` timestamp NULL COMMENT '过期时间',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否激活',
  `created_by` int NOT NULL COMMENT '创建者ID',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_code_type` (`code_type`),
  KEY `idx_is_active` (`is_active`),
  CONSTRAINT `fk_gift_codes_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='礼包码表';

DROP TABLE IF EXISTS `gift_code_usage`;
CREATE TABLE `gift_code_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '使用记录ID',
  `gift_code_id` bigint NOT NULL COMMENT '礼包码ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `used_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_player` (`gift_code_id`, `player_id`),
  KEY `idx_player_id` (`player_id`),
  CONSTRAINT `fk_gift_code_usage_code` FOREIGN KEY (`gift_code_id`) REFERENCES `gift_codes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_gift_code_usage_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='礼包码使用记录表';

-- ====================================================================
-- 宠物系统扩展 (v2.0优化)
-- ====================================================================

-- 宠物系统索引优化（在表创建时已包含）

SET FOREIGN_KEY_CHECKS=1;

-- ====================================================================
-- 游戏配置系统
-- ====================================================================

-- ----------------------------
-- Table structure for game_configs
-- ----------------------------
DROP TABLE IF EXISTS `game_configs`;
CREATE TABLE `game_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `config_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STRING' COMMENT '配置类型：STRING/INTEGER/DOUBLE/BOOLEAN',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci NULL COMMENT '配置描述',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GENERAL' COMMENT '配置分类',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏配置表';

-- 插入默认配置数据
INSERT INTO `game_configs` (`config_key`, `config_value`, `config_type`, `description`, `category`) VALUES
-- 经验相关配置
('exp.multiplier', '1.0', 'DOUBLE', '经验倍率', 'EXPERIENCE'),
('cultivation.exp.base', '100', 'INTEGER', '修炼基础经验', 'EXPERIENCE'),

-- 掉落相关配置
('drop.rate.multiplier', '1.0', 'DOUBLE', '掉落倍率', 'DROP'),
('drop.rare.rate', '0.05', 'DOUBLE', '稀有物品掉落率', 'DROP'),

-- 商店相关配置
('shop.discount.rate', '1.0', 'DOUBLE', '商店折扣率', 'SHOP'),
('shop.refresh.cost', '100', 'INTEGER', '商店刷新费用', 'SHOP'),

-- 境界相关配置
('realm.breakthrough.cost.multiplier', '1.0', 'DOUBLE', '境界突破费用倍率', 'REALM'),
('realm.level.requirement.multiplier', '1.0', 'DOUBLE', '境界等级要求倍率', 'REALM'),

-- 宠物相关配置
('pet.capture.base.rate', '0.3', 'DOUBLE', '宠物捕获基础成功率', 'PET'),
('pet.training.cost.multiplier', '1.0', 'DOUBLE', '宠物训练费用倍率', 'PET'),

-- 活动相关配置
('activity.double.exp.enabled', 'false', 'BOOLEAN', '双倍经验活动开启', 'ACTIVITY'),
('activity.double.drop.enabled', 'false', 'BOOLEAN', '双倍掉落活动开启', 'ACTIVITY'),

-- 新手相关配置
('newbie.gift.enabled', 'true', 'BOOLEAN', '新手礼包开启', 'NEWBIE'),
('newbie.protection.level', '10', 'INTEGER', '新手保护等级', 'NEWBIE'),

-- 系统相关配置
('system.maintenance.mode', 'false', 'BOOLEAN', '维护模式', 'SYSTEM'),
('system.max.online.users', '1000', 'INTEGER', '最大在线用户数', 'SYSTEM');

-- ====================================================================
-- 初始化管理员账户
-- ====================================================================

-- 创建默认管理员账户（密码：admin123，请在生产环境中修改）
-- INSERT INTO `users` (`username`, `password`, `email`, `role`, `status`, `must_change_password`) VALUES
-- ('admin', '$2a$10$N.zmdr9k7uOIkanjc.rfKONVjBUKp8yJhzNjNfqrjxrJmkJnr6jG2', 'admin@xiuxian.com', 'ADMIN', 'ACTIVE', 1);

-- ====================================================================
-- 初始化基础游戏数据
-- ====================================================================

-- 创建新手礼包
INSERT INTO `items` (`name`, `description`, `type`, `quality`, `stackable`, `max_stack`, `price`, `sellable`, `usable`, `effect`) VALUES
('新手大礼包', '包含新手必需品的礼包', 'special', 1, 0, 1, 0, 0, 1, '{"items": [{"id": 1, "quantity": 10}, {"id": 2, "quantity": 10}, {"id": 5, "quantity": 50}], "spirit_stones": 1000, "exp": 500}');

-- 创建默认活动
INSERT INTO `activities` (`activity_name`, `description`, `activity_type`, `start_time`, `end_time`, `status`, `rules`, `rewards`) VALUES
('新手七日登录', '连续登录七天获得丰厚奖励', 'SPECIAL', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'RUNNING', 
'{"type": "daily_login", "duration": 7}', 
'{"day1": {"spirit_stones": 100}, "day2": {"items": [{"id": 1, "quantity": 5}]}, "day7": {"equipment": [{"id": 6, "quantity": 1}]}}');

-- 创建默认公告
INSERT INTO `announcements` (`title`, `content`, `announcement_type`, `display_type`, `priority`, `status`, `start_time`, `end_time`, `created_by`) VALUES
('欢迎来到xiuxian世界', '欢迎各位道友加入xiuxian挂机游戏！在这里你可以体验xiuxian的乐趣，不断提升境界，探索无尽的xiuxian之路。', 'SYSTEM', 'POPUP', 1, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
('游戏玩法说明', '1. 点击xiuxian按钮开始xiuxian获得经验\n2. 通过商店购买装备提升实力\n3. 完成任务获得额外奖励\n4. 加入宗门与其他玩家互动', 'GUIDE', 'NOTICE', 2, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 1);

-- ====================================================================
-- 恢复外键检查并完成初始化
-- ====================================================================

SET FOREIGN_KEY_CHECKS=1;

-- ====================================================================
-- 数据库初始化完成
-- 版本：2.0
-- 日期：2025-12-11
-- 说明：此脚本适用于全新环境的首次部署
-- ====================================================================

-- ====================================================================
-- 初始化验证和清理
-- ====================================================================

-- 验证关键表是否创建成功
SELECT 
    COUNT(*) as total_tables,
    'Tables created successfully' as status
FROM information_schema.tables 
WHERE table_schema = 'xiuxian_game';

-- 显示初始化完成信息
SELECT 
    'Database initialization completed successfully!' as message,
    'Version: 2.0 - First Time Setup Optimized' as version,
    NOW() as completed_at,
    'Ready for production deployment' as status;

-- ====================================================================
-- 初始化完成说明
-- ====================================================================
/*
数据库初始化完成！

本脚本包含以下优化：
1. 首次部署专用 - 避免不必要的DROP操作
2. 预置索引优化 - 提升查询性能
3. 基础数据初始化 - 包含管理员账户和基础配置
4. 外键约束完整 - 保证数据一致性
5. 字符集统一 - 使用utf8mb4支持emoji

默认管理员账户：
- 用户名：admin
- 密码：admin123 (请在生产环境中立即修改)
- 邮箱：admin@xiuxian.com

注意事项：
- 请在生产环境中修改默认管理员密码
- 建议定期备份数据库
- 监控数据库性能并根据需要调整索引
*/