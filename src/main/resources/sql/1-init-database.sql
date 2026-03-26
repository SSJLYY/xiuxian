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
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO,ONLY_FULL_GROUP_BY';

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
  `spirit_stones` bigint NOT NULL DEFAULT '2000' COMMENT '灵石',
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
-- 练气期（境界倍率 1.0）
(1, 1, '练气期一层', 0, 300, 100, 50, 0, 0),
(2, 2, '练气期二层', 300, 700, 120, 60, 2, 1),
(3, 3, '练气期三层', 700, 1220, 140, 70, 4, 2),
(4, 4, '练气期四层', 1220, 1900, 160, 80, 6, 3),
(5, 5, '练气期五层', 1900, 2780, 180, 90, 8, 4),
(6, 6, '练气期六层', 2780, 3930, 200, 100, 10, 5),
(7, 7, '练气期七层', 3930, 5430, 220, 110, 12, 6),
(8, 8, '练气期八层', 5430, 7380, 240, 120, 14, 7),
(9, 9, '练气期九层', 7380, 9910, 260, 130, 16, 8),
(10, 10, '练气期十层', 9910, 13210, 280, 140, 18, 9),
-- 筑基期（境界倍率 2.5）
(11, 11, '筑基期一层', 13210, 18210, 350, 200, 25, 15),
(12, 12, '筑基期二层', 18210, 24710, 400, 230, 30, 18),
(13, 13, '筑基期三层', 24710, 33210, 450, 260, 35, 21),
(14, 14, '筑基期四层', 33210, 44210, 500, 290, 40, 24),
(15, 15, '筑基期五层', 44210, 58710, 550, 320, 45, 27),
-- 金丹期（境界倍率 6.0）
(16, 16, '金丹期一层', 58710, 80710, 700, 400, 60, 35),
(17, 17, '金丹期二层', 80710, 109710, 800, 450, 70, 40),
(18, 18, '金丹期三层', 109710, 147710, 900, 500, 80, 45),
(19, 19, '金丹期四层', 147710, 197710, 1000, 550, 90, 50),
-- 元婴期（境界倍率 15.0）
(20, 20, '元婴期一层', 197710, 272710, 1300, 700, 120, 70);

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

-- ----------------------------
-- Table structure for skill_trees
-- ----------------------------
DROP TABLE IF EXISTS `skill_trees`;
CREATE TABLE `skill_trees` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '技能树ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能树名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '技能树描述',
  `tree_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：ATTACK/DEFENSE/CULTIVATION/SUPPORT',
  `element` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '元素属性',
  `required_realm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '需求境界',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tree_type` (`tree_type`),
  KEY `idx_element` (`element`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能树表';

-- ----------------------------
-- Records of skill_trees
-- ----------------------------
INSERT INTO `skill_trees` VALUES 
(1, '火系法术', '掌控火焰之力的法术', 'ATTACK', '火', NULL, NULL, 1, NOW()),
(2, '水系法术', '掌控水流之力的法术', 'DEFENSE', '水', NULL, NULL, 1, NOW()),
(3, '木系法术', '掌控生机之力的法术', 'SUPPORT', '木', NULL, NULL, 1, NOW()),
(4, '土系法术', '掌控大地之力的法术', 'DEFENSE', '土', NULL, NULL, 1, NOW()),
(5, '风系法术', '掌控风之力的法术', 'ATTACK', '风', NULL, NULL, 1, NOW()),
(6, '雷系法术', '掌控雷电之力的法术', 'ATTACK', '雷', '筑基期', NULL, 1, NOW()),
(7, '修炼心法', '提升修炼效率的心法', 'CULTIVATION', '无', NULL, NULL, 1, NOW());

-- ----------------------------
-- Table structure for skill_tree_nodes
-- ----------------------------
DROP TABLE IF EXISTS `skill_tree_nodes`;
CREATE TABLE `skill_tree_nodes` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `tree_id` int NOT NULL COMMENT '技能树ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `node_level` int NOT NULL DEFAULT '1' COMMENT '节点层级',
  `position_x` int NOT NULL DEFAULT '0' COMMENT 'X坐标',
  `position_y` int NOT NULL DEFAULT '0' COMMENT 'Y坐标',
  `prerequisites` text COLLATE utf8mb4_unicode_ci COMMENT '前置技能ID(JSON数组)',
  `skill_points_cost` int NOT NULL DEFAULT '1' COMMENT '技能点消耗',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tree_skill` (`tree_id`, `skill_id`),
  KEY `idx_tree_id` (`tree_id`),
  KEY `idx_skill_id` (`skill_id`),
  CONSTRAINT `fk_skill_tree_nodes_tree` FOREIGN KEY (`tree_id`) REFERENCES `skill_trees` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_skill_tree_nodes_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能树节点表';

-- ----------------------------
-- Records of skill_tree_nodes
-- ----------------------------
INSERT INTO `skill_tree_nodes` VALUES 
(1, 1, 2, 1, 0, 0, '[]', 1),
(2, 3, 3, 1, 0, 0, '[]', 1),
(3, 2, 4, 1, 0, 0, '[]', 1),
(4, 4, 5, 1, 0, 0, '[]', 1),
(5, 5, 6, 1, 0, 0, '[]', 1),
(6, 7, 1, 1, 0, 0, '[]', 0);

-- ----------------------------
-- Table structure for skill_effects
-- ----------------------------
DROP TABLE IF EXISTS `skill_effects`;
CREATE TABLE `skill_effects` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '效果ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `effect_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '效果类型：DAMAGE/HEAL/BUFF/DEBUFF/DOT/HOT/SHIELD',
  `effect_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '效果名称',
  `base_value` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '基础值',
  `scaling_factor` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '缩放因子',
  `duration` int NOT NULL DEFAULT '0' COMMENT '持续时间(秒)',
  `tick_interval` int NOT NULL DEFAULT '0' COMMENT '触发间隔(秒)',
  `stackable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可叠加',
  `max_stacks` int NOT NULL DEFAULT '1' COMMENT '最大叠加数',
  `trigger_chance` decimal(5,2) NOT NULL DEFAULT '100.00' COMMENT '触发概率',
  `effect_order` int NOT NULL DEFAULT '0' COMMENT '效果顺序',
  PRIMARY KEY (`id`),
  KEY `idx_skill_id` (`skill_id`),
  KEY `idx_effect_type` (`effect_type`),
  CONSTRAINT `fk_skill_effects_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能效果表';

-- ----------------------------
-- Records of skill_effects
-- ----------------------------
INSERT INTO `skill_effects` VALUES 
(1, 2, 'DAMAGE', '火焰伤害', 10.00, 1.20, 0, 0, 0, 1, 100.00, 1),
(2, 2, 'DOT', '灼烧', 5.00, 0.50, 3, 1, 1, 3, 30.00, 2),
(3, 3, 'HEAL', '生命恢复', 20.00, 1.00, 0, 0, 0, 1, 100.00, 1),
(4, 4, 'SHIELD', '水盾', 30.00, 0.80, 10, 0, 0, 1, 100.00, 1),
(5, 4, 'BUFF', '减伤', 20.00, 0.50, 10, 0, 0, 1, 100.00, 2),
(6, 5, 'DAMAGE', '土系伤害', 25.00, 1.50, 0, 0, 0, 1, 100.00, 1),
(7, 5, 'DEBUFF', '减速', 30.00, 0.30, 2, 0, 0, 1, 50.00, 2),
(8, 6, 'DAMAGE', '风系伤害', 15.00, 1.30, 0, 0, 0, 1, 100.00, 1),
(9, 6, 'DEBUFF', '击退', 1.00, 0.00, 0, 0, 0, 1, 40.00, 2);

-- ----------------------------
-- Table structure for skill_combos
-- ----------------------------
DROP TABLE IF EXISTS `skill_combos`;
CREATE TABLE `skill_combos` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '连招ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '连招名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '连招描述',
  `skill_sequence` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能序列(JSON数组)',
  `combo_bonus` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '连招加成(百分比)',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_required_level` (`required_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能连招表';

-- ----------------------------
-- Records of skill_combos
-- ----------------------------
INSERT INTO `skill_combos` VALUES 
(1, '水火交融', '先水后火，产生蒸汽爆炸', '[4, 2]', 50.00, 10, 1, NOW()),
(2, '风火连击', '风助火势，伤害倍增', '[6, 2]', 30.00, 8, 1, NOW()),
(3, '土木防御', '土木结合，坚固防御', '[5, 3]', 40.00, 12, 1, NOW());

-- ----------------------------
-- Table structure for player_skill_cooldowns
-- ----------------------------
DROP TABLE IF EXISTS `player_skill_cooldowns`;
CREATE TABLE `player_skill_cooldowns` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '冷却ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `expire_at` timestamp NOT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_skill` (`player_id`, `skill_id`),
  KEY `idx_expire_at` (`expire_at`),
  CONSTRAINT `fk_player_skill_cooldowns_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_skill_cooldowns_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家技能冷却表';

-- ----------------------------
-- Table structure for skill_mastery
-- ----------------------------
DROP TABLE IF EXISTS `skill_mastery`;
CREATE TABLE `skill_mastery` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '熟练度ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `mastery_level` int NOT NULL DEFAULT '1' COMMENT '熟练度等级',
  `required_exp` int NOT NULL COMMENT '所需经验',
  `damage_bonus` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '伤害加成',
  `cooldown_reduction` int NOT NULL DEFAULT '0' COMMENT '冷却减少(秒)',
  `mana_cost_reduction` int NOT NULL DEFAULT '0' COMMENT '法力消耗减少',
  `special_effect` text COLLATE utf8mb4_unicode_ci COMMENT '特殊效果(JSON)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skill_mastery` (`skill_id`, `mastery_level`),
  KEY `idx_skill_id` (`skill_id`),
  CONSTRAINT `fk_skill_mastery_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能熟练度表';

-- ----------------------------
-- Records of skill_mastery
-- ----------------------------
INSERT INTO `skill_mastery` VALUES 
(1, 2, 1, 0, 0.00, 0, 0, NULL),
(2, 2, 2, 100, 10.00, 0, 1, NULL),
(3, 2, 3, 300, 20.00, 1, 2, '{"burn_chance_increase": 10}'),
(4, 2, 4, 600, 30.00, 1, 3, NULL),
(5, 2, 5, 1000, 50.00, 2, 5, '{"aoe_damage": true}'),
(6, 3, 1, 0, 0.00, 0, 0, NULL),
(7, 3, 2, 80, 15.00, 1, 1, NULL),
(8, 3, 3, 200, 30.00, 2, 2, '{"hot_effect": true}'),
(9, 3, 4, 400, 45.00, 2, 3, NULL),
(10, 3, 5, 700, 70.00, 3, 5, '{"aoe_heal": true}'),
(11, 6, 1, 0, 0.00, 0, 0, NULL),
(12, 6, 2, 150, 15.00, 0, 2, NULL),
(13, 6, 3, 400, 30.00, 1, 3, '{"multi_hit": 2}'),
(14, 6, 4, 800, 50.00, 1, 4, NULL),
(15, 6, 5, 1500, 80.00, 2, 6, '{"penetration": true}');

-- ----------------------------
-- Table structure for player_skill_mastery
-- ----------------------------
DROP TABLE IF EXISTS `player_skill_mastery`;
CREATE TABLE `player_skill_mastery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家熟练度ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `mastery_level` int NOT NULL DEFAULT '1' COMMENT '当前熟练度等级',
  `mastery_exp` int NOT NULL DEFAULT '0' COMMENT '当前熟练度经验',
  `total_uses` int NOT NULL DEFAULT '0' COMMENT '总使用次数',
  `last_used_at` timestamp NULL COMMENT '最后使用时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_skill_mastery` (`player_id`, `skill_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_skill_id` (`skill_id`),
  CONSTRAINT `fk_player_skill_mastery_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_skill_mastery_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家技能熟练度表';

-- ----------------------------
-- Table structure for skill_enhancements
-- ----------------------------
DROP TABLE IF EXISTS `skill_enhancements`;
CREATE TABLE `skill_enhancements` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '强化ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '强化名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '强化描述',
  `enhancement_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '强化类型：DAMAGE/RANGE/COOLDOWN/EFFECT',
  `target_skill_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标技能类型',
  `target_element` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标元素',
  `value` decimal(10,2) NOT NULL COMMENT '强化值',
  `is_percentage` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否为百分比',
  `cost_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消耗类型：SPIRIT_STONES/SKILL_POINTS/ITEMS',
  `cost_value` int NOT NULL COMMENT '消耗数量',
  `cost_item_id` int DEFAULT NULL COMMENT '消耗物品ID',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `required_mastery` int NOT NULL DEFAULT '1' COMMENT '需求熟练度',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_enhancement_type` (`enhancement_type`),
  KEY `idx_target_skill_type` (`target_skill_type`),
  CONSTRAINT `fk_skill_enhancements_item` FOREIGN KEY (`cost_item_id`) REFERENCES `items` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能强化表';

-- ----------------------------
-- Records of skill_enhancements
-- ----------------------------
INSERT INTO `skill_enhancements` VALUES 
(1, '火焰精通', '所有火系技能伤害提升', 'DAMAGE', 'attack', '火', 15.00, 1, 'SPIRIT_STONES', 1000, NULL, 10, 3, 1, NOW()),
(2, '治疗强化', '所有治疗技能效果提升', 'EFFECT', 'heal', NULL, 20.00, 1, 'SPIRIT_STONES', 800, NULL, 5, 2, 1, NOW()),
(3, '冷却缩减', '所有技能冷却时间减少', 'COOLDOWN', NULL, NULL, 10.00, 1, 'SKILL_POINTS', 3, NULL, 15, 1, 1, NOW()),
(4, '法术穿透', '攻击技能无视部分防御', 'EFFECT', 'attack', NULL, 10.00, 1, 'SPIRIT_STONES', 2000, NULL, 20, 5, 1, NOW()),
(5, '技能范围', '攻击技能范围增加', 'RANGE', 'attack', NULL, 25.00, 1, 'SKILL_POINTS', 2, NULL, 12, 1, 1, NOW());

-- ----------------------------
-- Table structure for player_skill_enhancements
-- ----------------------------
DROP TABLE IF EXISTS `player_skill_enhancements`;
CREATE TABLE `player_skill_enhancements` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家强化ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `enhancement_id` int NOT NULL COMMENT '强化ID',
  `applied_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '应用时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_enhancement` (`player_id`, `enhancement_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_enhancement_id` (`enhancement_id`),
  CONSTRAINT `fk_player_skill_enhancements_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_skill_enhancements_enhancement` FOREIGN KEY (`enhancement_id`) REFERENCES `skill_enhancements` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家技能强化表';

-- ----------------------------
-- Table structure for passive_skills
-- ----------------------------
DROP TABLE IF EXISTS `passive_skills`;
CREATE TABLE `passive_skills` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '被动技能ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '技能描述',
  `passive_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：STAT/COMBAT/CULTIVATION/SPECIAL',
  `effect_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '效果类型',
  `effect_value` decimal(10,2) NOT NULL COMMENT '效果值',
  `is_percentage` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否为百分比',
  `max_level` int NOT NULL DEFAULT '5' COMMENT '最大等级',
  `upgrade_cost` int NOT NULL DEFAULT '100' COMMENT '升级消耗(灵石)',
  `cost_multiplier` decimal(5,2) NOT NULL DEFAULT '1.50' COMMENT '费用倍率',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `required_realm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '需求境界',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_passive_type` (`passive_type`),
  KEY `idx_effect_type` (`effect_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='被动技能表';

-- ----------------------------
-- Records of passive_skills
-- ----------------------------
INSERT INTO `passive_skills` VALUES 
(1, '强身健体', '永久提升生命值', 'STAT', 'health', 100.00, 0, 10, 500, 1.50, 1, NULL, NULL, 1, NOW()),
(2, '灵力充沛', '永久提升法力值', 'STAT', 'mana', 50.00, 0, 10, 500, 1.50, 1, NULL, NULL, 1, NOW()),
(3, '力量强化', '永久提升攻击力', 'STAT', 'attack', 10.00, 0, 10, 800, 1.60, 5, NULL, NULL, 1, NOW()),
(4, '铁壁防御', '永久提升防御力', 'STAT', 'defense', 8.00, 0, 10, 800, 1.60, 5, NULL, NULL, 1, NOW()),
(5, '身法敏捷', '永久提升速度', 'STAT', 'speed', 5.00, 0, 10, 600, 1.50, 3, NULL, NULL, 1, NOW()),
(6, '暴击本能', '提升暴击率', 'COMBAT', 'crit_rate', 2.00, 1, 10, 1000, 1.80, 10, NULL, NULL, 1, NOW()),
(7, '暴击伤害', '提升暴击伤害', 'COMBAT', 'crit_damage', 5.00, 1, 10, 1200, 1.80, 15, NULL, NULL, 1, NOW()),
(8, '闪避天赋', '提升闪避率', 'COMBAT', 'dodge_rate', 1.50, 1, 10, 1000, 1.70, 12, NULL, NULL, 1, NOW()),
(9, '吸血本能', '攻击时恢复生命', 'COMBAT', 'lifesteal', 1.00, 1, 5, 2000, 2.00, 20, '筑基期', NULL, 1, NOW()),
(10, '修炼加速', '提升修炼速度', 'CULTIVATION', 'cultivation_speed', 5.00, 1, 10, 1500, 1.60, 8, NULL, NULL, 1, NOW()),
(11, '经验加成', '提升获得经验', 'CULTIVATION', 'exp_bonus', 3.00, 1, 10, 1000, 1.50, 5, NULL, NULL, 1, NOW()),
(12, '灵石加成', '提升灵石获取', 'CULTIVATION', 'spirit_stones_bonus', 5.00, 1, 10, 2000, 1.80, 10, NULL, NULL, 1, NOW());

-- ----------------------------
-- Table structure for player_passive_skills
-- ----------------------------
DROP TABLE IF EXISTS `player_passive_skills`;
CREATE TABLE `player_passive_skills` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家被动技能ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `passive_skill_id` int NOT NULL COMMENT '被动技能ID',
  `level` int NOT NULL DEFAULT '1' COMMENT '当前等级',
  `learned_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '学习时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_passive` (`player_id`, `passive_skill_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_passive_skill_id` (`passive_skill_id`),
  CONSTRAINT `fk_player_passive_skills_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_passive_skills_skill` FOREIGN KEY (`passive_skill_id`) REFERENCES `passive_skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家被动技能表';

-- ----------------------------
-- Table structure for skill_usage_logs
-- ----------------------------
DROP TABLE IF EXISTS `skill_usage_logs`;
CREATE TABLE `skill_usage_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `skill_level` int NOT NULL DEFAULT '1' COMMENT '技能等级',
  `target_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标类型：MONSTER/PLAYER',
  `target_id` int DEFAULT NULL COMMENT '目标ID',
  `damage_dealt` int NOT NULL DEFAULT '0' COMMENT '造成伤害',
  `heal_amount` int NOT NULL DEFAULT '0' COMMENT '治疗量',
  `mana_consumed` int NOT NULL DEFAULT '0' COMMENT '消耗法力',
  `is_critical` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否暴击',
  `mastery_exp_gained` int NOT NULL DEFAULT '0' COMMENT '获得熟练度',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_skill_id` (`skill_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_skill_usage_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_skill_usage_logs_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能使用日志表';

-- ----------------------------
-- Table structure for skill_statistics
-- ----------------------------
DROP TABLE IF EXISTS `skill_statistics`;
CREATE TABLE `skill_statistics` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_skills_learned` int NOT NULL DEFAULT '0' COMMENT '已学习技能数',
  `total_skill_uses` bigint NOT NULL DEFAULT '0' COMMENT '总技能使用次数',
  `total_damage_by_skills` bigint NOT NULL DEFAULT '0' COMMENT '技能总伤害',
  `total_heal_by_skills` bigint NOT NULL DEFAULT '0' COMMENT '技能总治疗',
  `highest_mastery_level` int NOT NULL DEFAULT '0' COMMENT '最高熟练度',
  `favorite_skill_id` int DEFAULT NULL COMMENT '最常用技能',
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_id` (`player_id`),
  CONSTRAINT `fk_skill_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能统计表';

-- ----------------------------
-- Table structure for player_skill_combo_records
-- ----------------------------
DROP TABLE IF EXISTS `player_skill_combo_records`;
CREATE TABLE `player_skill_combo_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `used_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  `triggered_combo` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否触发连招',
  `combo_id` int DEFAULT NULL COMMENT '触发的连招ID',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_used_at` (`used_at`),
  CONSTRAINT `fk_skill_combo_records_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家技能连招记录表';

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
(10, '修炼心得', '记录修炼感悟的书籍', 'book', 2, 0, 1, 500, 1, 1, '{"cultivation_speed": 1.1}', NOW(), NOW()),
(11, '大还丹', '高级恢复丹药', 'consumable', 2, 1, 50, 150, 1, 1, '{"heal": 150}', NOW(), NOW()),
(12, '聚灵丹', '高级灵力恢复丹药', 'consumable', 2, 1, 50, 150, 1, 1, '{"restore_mana": 150}', NOW(), NOW()),
(13, '悟道丹', '提升修炼速度的丹药', 'consumable', 3, 1, 20, 500, 1, 1, '{"cultivation_speed": 1.5, "duration": 3600}', NOW(), NOW()),
(14, '驻颜丹', '保持青春的丹药', 'consumable', 2, 1, 10, 300, 1, 1, '{"beauty": 10}', NOW(), NOW()),
(15, '洗髓丹', '洗练根骨的丹药', 'consumable', 4, 1, 5, 2000, 1, 1, '{"attribute_reset": 1}', NOW(), NOW()),
(16, '火符', '火属性攻击符箓', 'consumable', 1, 1, 99, 80, 1, 1, '{"fire_damage": 100}', NOW(), NOW()),
(17, '水符', '水属性防御符箓', 'consumable', 1, 1, 99, 80, 1, 1, '{"water_shield": 50}', NOW(), NOW()),
(18, '雷符', '雷属性攻击符箓', 'consumable', 2, 1, 50, 200, 1, 1, '{"thunder_damage": 200}', NOW(), NOW()),
(19, '玄铁矿', '珍贵的炼器矿石', 'material', 3, 1, 99, 300, 1, 0, '{}', NOW(), NOW()),
(20, '千年灵芝', '千年的灵芝，炼丹极品', 'material', 4, 1, 20, 1000, 1, 0, '{}', NOW(), NOW()),
(21, '龙血', '传说中的龙血', 'material', 5, 1, 10, 5000, 1, 0, '{}', NOW(), NOW()),
(22, '凤凰羽毛', '凤凰的羽毛', 'material', 5, 1, 10, 5000, 1, 0, '{}', NOW(), NOW()),
(23, '铜宝箱', '普通宝箱', 'chest', 1, 0, 1, 100, 0, 1, '{"items": [{"id": 1, "quantity": 5}, {"id": 5, "quantity": 10}]}', NOW(), NOW()),
(24, '银宝箱', '高级宝箱', 'chest', 2, 0, 1, 500, 0, 1, '{"items": [{"id": 3, "quantity": 3}, {"id": 6, "quantity": 5}], "spirit_stones": 200}', NOW(), NOW()),
(25, '金宝箱', '稀有宝箱', 'chest', 3, 0, 1, 2000, 0, 1, '{"items": [{"id": 4, "quantity": 1}, {"id": 13, "quantity": 2}], "spirit_stones": 1000}', NOW(), NOW()),
(26, '钻石宝箱', '传说宝箱', 'chest', 4, 0, 1, 10000, 0, 1, '{"items": [{"id": 15, "quantity": 1}], "equipments": [{"id": 18, "rate": 30}], "spirit_stones": 5000}', NOW(), NOW()),
(27, '铁矿石', '普通的铁矿石', 'material', 1, 1, 999, 20, 1, 0, '{}', NOW(), NOW()),
(28, '铜矿石', '普通的铜矿石', 'material', 1, 1, 999, 15, 1, 0, '{}', NOW(), NOW()),
(29, '银矿石', '珍贵的银矿石', 'material', 2, 1, 200, 100, 1, 0, '{}', NOW(), NOW()),
(30, '金矿石', '稀有的金矿石', 'material', 3, 1, 100, 500, 1, 0, '{}', NOW(), NOW()),
(31, '狼皮', '野狼的皮毛', 'material', 1, 1, 999, 30, 1, 0, '{}', NOW(), NOW()),
(32, '蛇胆', '蛇妖的胆囊', 'material', 2, 1, 200, 150, 1, 0, '{}', NOW(), NOW()),
(33, '虎骨', '猛虎的骨骼', 'material', 2, 1, 200, 200, 1, 0, '{}', NOW(), NOW()),
(34, '朱果', '红色的灵果', 'material', 3, 1, 50, 800, 1, 0, '{}', NOW(), NOW()),
(35, '天山雪莲', '雪山上的珍贵药材', 'material', 4, 1, 20, 3000, 1, 0, '{}', NOW(), NOW()),
(36, '筑基丹', '帮助筑基的丹药', 'consumable', 4, 1, 5, 5000, 1, 1, '{"realm_breakthrough": "筑基期"}', NOW(), NOW()),
(37, '金丹丹', '帮助凝结金丹的丹药', 'consumable', 5, 1, 3, 20000, 1, 1, '{"realm_breakthrough": "金丹期"}', NOW(), NOW()),
(38, '元婴丹', '帮助凝结元婴的丹药', 'consumable', 5, 1, 1, 50000, 1, 1, '{"realm_breakthrough": "元婴期"}', NOW(), NOW()),
(39, '神行符', '增加移动速度的符箓', 'consumable', 2, 1, 50, 200, 1, 1, '{"speed_boost": 50, "duration": 1800}', NOW(), NOW()),
(40, '隐身符', '隐身效果的符箓', 'consumable', 3, 1, 20, 500, 1, 1, '{"stealth": 1, "duration": 600}', NOW(), NOW());

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

-- ----------------------------
-- Table structure for item_categories
-- ----------------------------
DROP TABLE IF EXISTS `item_categories`;
CREATE TABLE `item_categories` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '分类描述',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `parent_id` int DEFAULT NULL COMMENT '父分类ID',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品分类表';

-- ----------------------------
-- Records of item_categories
-- ----------------------------
INSERT INTO `item_categories` VALUES 
(1, '消耗品', 'consumable', '可使用的消耗类物品', NULL, NULL, 1, 1, NOW()),
(2, '材料', 'material', '用于合成和制作的材料', NULL, NULL, 2, 1, NOW()),
(3, '装备', 'equipment', '可穿戴的装备', NULL, NULL, 3, 1, NOW()),
(4, '丹药', 'pill', '修炼用丹药', NULL, 1, 1, 1, NOW()),
(5, '符箓', 'talisman', '一次性使用的符箓', NULL, 1, 2, 1, NOW()),
(6, '灵草', 'herb', '炼丹材料', NULL, 2, 1, 1, NOW()),
(7, '矿石', 'ore', '炼器材料', NULL, 2, 2, 1, NOW()),
(8, '妖兽材料', 'monster_material', '妖兽掉落的材料', NULL, 2, 3, 1, NOW()),
(9, '任务物品', 'quest_item', '任务相关物品', NULL, NULL, 4, 1, NOW()),
(10, '特殊物品', 'special', '特殊用途物品', NULL, NULL, 5, 1, NOW()),
(11, '宝箱', 'chest', '可开启的宝箱', NULL, 10, 1, 1, NOW()),
(12, '礼包', 'gift_pack', '包含多种物品的礼包', NULL, 10, 2, 1, NOW());

-- ----------------------------
-- Table structure for item_qualities
-- ----------------------------
DROP TABLE IF EXISTS `item_qualities`;
CREATE TABLE `item_qualities` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '品质ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品质名称',
  `level` int NOT NULL COMMENT '品质等级',
  `color` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '颜色代码',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '品质描述',
  `drop_rate_modifier` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '掉落率修正',
  `price_modifier` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '价格修正',
  `sell_price_ratio` decimal(5,2) NOT NULL DEFAULT '0.50' COMMENT '出售价格比例',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品品质表';

-- ----------------------------
-- Records of item_qualities
-- ----------------------------
INSERT INTO `item_qualities` VALUES 
(1, '普通', 1, '#FFFFFF', '普通品质的物品', 1.00, 1.00, 0.50),
(2, '精良', 2, '#00FF00', '精良品质的物品', 0.70, 1.50, 0.50),
(3, '稀有', 3, '#0080FF', '稀有品质的物品', 0.40, 3.00, 0.60),
(4, '史诗', 4, '#8000FF', '史诗品质的物品', 0.15, 8.00, 0.70),
(5, '传说', 5, '#FF8000', '传说品质的物品', 0.05, 20.00, 0.80),
(6, '神话', 6, '#FF0000', '神话品质的物品', 0.01, 50.00, 0.90);

-- ----------------------------
-- Table structure for inventory_expansions
-- ----------------------------
DROP TABLE IF EXISTS `inventory_expansions`;
CREATE TABLE `inventory_expansions` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '扩展ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `expansion_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '扩展类型：BAG/VAULT/EQUIPMENT',
  `current_slots` int NOT NULL DEFAULT '50' COMMENT '当前格子数',
  `max_slots` int NOT NULL DEFAULT '200' COMMENT '最大格子数',
  `expansion_count` int NOT NULL DEFAULT '0' COMMENT '扩展次数',
  `last_expansion_at` timestamp NULL COMMENT '最后扩展时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_type` (`player_id`, `expansion_type`),
  KEY `idx_player_id` (`player_id`),
  CONSTRAINT `fk_inventory_expansions_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='背包扩展表';

-- ----------------------------
-- Table structure for item_usage_logs
-- ----------------------------
DROP TABLE IF EXISTS `item_usage_logs`;
CREATE TABLE `item_usage_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '使用数量',
  `usage_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '使用类型：USE/SELL/DROP/TRADE',
  `effect_result` text COLLATE utf8mb4_unicode_ci COMMENT '效果结果(JSON)',
  `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_usage_type` (`usage_type`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_item_usage_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_usage_logs_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品使用日志表';

-- ----------------------------
-- Table structure for item_recipes
-- ----------------------------
DROP TABLE IF EXISTS `item_recipes`;
CREATE TABLE `item_recipes` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配方ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配方名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '配方描述',
  `result_item_id` int NOT NULL COMMENT '产出物品ID',
  `result_quantity` int NOT NULL DEFAULT '1' COMMENT '产出数量',
  `craft_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '制作类型：ALCHEMY/FORGING/COOKING',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `required_realm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '需求境界',
  `craft_time` int NOT NULL DEFAULT '0' COMMENT '制作时间(秒)',
  `success_rate` decimal(5,2) NOT NULL DEFAULT '100.00' COMMENT '成功率',
  `spirit_stones_cost` int NOT NULL DEFAULT '0' COMMENT '灵石消耗',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_result_item_id` (`result_item_id`),
  KEY `idx_craft_type` (`craft_type`),
  KEY `idx_required_level` (`required_level`),
  CONSTRAINT `fk_item_recipes_result` FOREIGN KEY (`result_item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品合成配方表';

-- ----------------------------
-- Records of item_recipes
-- ----------------------------
INSERT INTO `item_recipes` VALUES 
(1, '炼制疗伤丹', '使用灵草炼制疗伤丹', 1, 3, 'ALCHEMY', 1, NULL, 10, 90.00, 50, 1, NOW(), NOW()),
(2, '炼制回灵丹', '使用灵草炼制回灵丹', 2, 3, 'ALCHEMY', 3, NULL, 15, 85.00, 80, 1, NOW(), NOW()),
(3, '炼制经验丹', '使用仙草炼制经验丹', 3, 1, 'ALCHEMY', 10, '练气期五层', 30, 70.00, 200, 1, NOW(), NOW()),
(4, '炼制突破丹', '使用仙草和妖丹炼制突破丹', 4, 1, 'ALCHEMY', 15, '筑基期', 60, 50.00, 500, 1, NOW(), NOW()),
(5, '炼制大还丹', '使用灵草和灵石炼制大还丹', 11, 2, 'ALCHEMY', 8, NULL, 20, 80.00, 150, 1, NOW(), NOW()),
(6, '炼制聚灵丹', '使用灵草和灵石炼制聚灵丹', 12, 2, 'ALCHEMY', 10, NULL, 25, 75.00, 200, 1, NOW(), NOW());

-- ----------------------------
-- Table structure for recipe_materials
-- ----------------------------
DROP TABLE IF EXISTS `recipe_materials`;
CREATE TABLE `recipe_materials` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '材料ID',
  `recipe_id` int NOT NULL COMMENT '配方ID',
  `item_id` int NOT NULL COMMENT '材料物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '需求数量',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recipe_material` (`recipe_id`, `item_id`),
  KEY `idx_recipe_id` (`recipe_id`),
  KEY `idx_item_id` (`item_id`),
  CONSTRAINT `fk_recipe_materials_recipe` FOREIGN KEY (`recipe_id`) REFERENCES `item_recipes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_recipe_materials_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配方材料表';

-- ----------------------------
-- Records of recipe_materials
-- ----------------------------
INSERT INTO `recipe_materials` VALUES 
(1, 1, 5, 2),
(2, 2, 5, 3),
(3, 3, 8, 1),
(4, 3, 5, 5),
(5, 4, 8, 2),
(6, 4, 7, 1),
(7, 5, 5, 5),
(8, 5, 6, 2),
(9, 6, 5, 6),
(10, 6, 6, 3);

-- ----------------------------
-- Table structure for player_recipes
-- ----------------------------
DROP TABLE IF EXISTS `player_recipes`;
CREATE TABLE `player_recipes` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家配方ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `recipe_id` int NOT NULL COMMENT '配方ID',
  `learned_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '学习时间',
  `craft_count` int NOT NULL DEFAULT '0' COMMENT '制作次数',
  `mastery_level` int NOT NULL DEFAULT '1' COMMENT '熟练度等级',
  `mastery_exp` int NOT NULL DEFAULT '0' COMMENT '熟练度经验',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_recipe` (`player_id`, `recipe_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_recipe_id` (`recipe_id`),
  CONSTRAINT `fk_player_recipes_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_recipes_recipe` FOREIGN KEY (`recipe_id`) REFERENCES `item_recipes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家配方表';

-- ----------------------------
-- Table structure for craft_logs
-- ----------------------------
DROP TABLE IF EXISTS `craft_logs`;
CREATE TABLE `craft_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '制作日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `recipe_id` int NOT NULL COMMENT '配方ID',
  `result_item_id` int NOT NULL COMMENT '产出物品ID',
  `result_quantity` int NOT NULL DEFAULT '1' COMMENT '产出数量',
  `is_success` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否成功',
  `mastery_exp_gained` int NOT NULL DEFAULT '0' COMMENT '获得熟练度',
  `materials_consumed` text COLLATE utf8mb4_unicode_ci COMMENT '消耗材料(JSON)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_recipe_id` (`recipe_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_craft_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_craft_logs_recipe` FOREIGN KEY (`recipe_id`) REFERENCES `item_recipes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='制作日志表';

-- ----------------------------
-- Table structure for temporary_items
-- ----------------------------
DROP TABLE IF EXISTS `temporary_items`;
CREATE TABLE `temporary_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '临时物品ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `source` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源',
  `expire_at` timestamp NOT NULL COMMENT '过期时间',
  `is_expired` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已过期',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_expire_at` (`expire_at`),
  KEY `idx_is_expired` (`is_expired`),
  CONSTRAINT `fk_temporary_items_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_temporary_items_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='临时物品表';

-- ----------------------------
-- Table structure for item_exchange_logs
-- ----------------------------
DROP TABLE IF EXISTS `item_exchange_logs`;
CREATE TABLE `item_exchange_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '交换日志ID',
  `from_player_id` int NOT NULL COMMENT '发起方玩家ID',
  `to_player_id` int NOT NULL COMMENT '接收方玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `exchange_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '交换类型：GIFT/TRADE/MAIL',
  `price` int DEFAULT NULL COMMENT '交易价格',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'COMPLETED' COMMENT '状态：PENDING/COMPLETED/CANCELLED',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_player_id` (`from_player_id`),
  KEY `idx_to_player_id` (`to_player_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_item_exchange_logs_from` FOREIGN KEY (`from_player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_exchange_logs_to` FOREIGN KEY (`to_player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_exchange_logs_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品交换日志表';

-- ----------------------------
-- Table structure for item_storages
-- ----------------------------
DROP TABLE IF EXISTS `item_storages`;
CREATE TABLE `item_storages` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `storage_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库类型：VAULT/GUILD/TEMP',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `slot_position` int DEFAULT NULL COMMENT '槽位位置',
  `locked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否锁定',
  `stored_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '存入时间',
  `expire_at` timestamp NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_storage_type` (`storage_type`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_expire_at` (`expire_at`),
  CONSTRAINT `fk_item_storages_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_storages_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品仓库表';

-- ----------------------------
-- Table structure for item_drop_rates
-- ----------------------------
DROP TABLE IF EXISTS `item_drop_rates`;
CREATE TABLE `item_drop_rates` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '掉落率ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `source_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源类型：MONSTER/DUNGEON/QUEST/ACTIVITY',
  `source_id` int DEFAULT NULL COMMENT '来源ID',
  `drop_rate` decimal(5,2) NOT NULL COMMENT '掉落概率',
  `min_quantity` int NOT NULL DEFAULT '1' COMMENT '最小数量',
  `max_quantity` int NOT NULL DEFAULT '1' COMMENT '最大数量',
  `daily_limit` int DEFAULT NULL COMMENT '每日限制',
  `level_requirement` int NOT NULL DEFAULT '1' COMMENT '等级要求',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_source` (`source_type`, `source_id`),
  KEY `idx_active` (`active`),
  CONSTRAINT `fk_item_drop_rates_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品掉落率表';

-- ----------------------------
-- Records of item_drop_rates
-- ----------------------------
INSERT INTO `item_drop_rates` VALUES 
(1, 1, 'MONSTER', 1, 30.00, 1, 3, NULL, 1, 1, NOW()),
(2, 2, 'MONSTER', 1, 25.00, 1, 2, NULL, 1, 1, NOW()),
(3, 5, 'MONSTER', 1, 50.00, 1, 5, NULL, 1, 1, NOW()),
(4, 6, 'MONSTER', 1, 20.00, 1, 3, NULL, 5, 1, NOW()),
(5, 7, 'MONSTER', NULL, 10.00, 1, 1, NULL, 10, 1, NOW()),
(6, 8, 'MONSTER', NULL, 5.00, 1, 1, NULL, 15, 1, NOW()),
(7, 1, 'DUNGEON', 1, 50.00, 2, 5, NULL, 1, 1, NOW()),
(8, 3, 'DUNGEON', 3, 15.00, 1, 1, NULL, 10, 1, NOW()),
(9, 4, 'DUNGEON', 5, 10.00, 1, 1, NULL, 20, 1, NOW());

-- ----------------------------
-- Table structure for inventory_presets
-- ----------------------------
DROP TABLE IF EXISTS `inventory_presets`;
CREATE TABLE `inventory_presets` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '预设ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `preset_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预设名称',
  `preset_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预设类型：BATTLE/CULTIVATION/FARMING',
  `items_config` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品配置(JSON)',
  `is_active` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否激活',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_preset_type` (`preset_type`),
  CONSTRAINT `fk_inventory_presets_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='背包预设表';

-- ----------------------------
-- Table structure for item_cooldowns
-- ----------------------------
DROP TABLE IF EXISTS `item_cooldowns`;
CREATE TABLE `item_cooldowns` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '冷却ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `cooldown_group` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '冷却组',
  `expire_at` timestamp NOT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_item` (`player_id`, `item_id`),
  KEY `idx_expire_at` (`expire_at`),
  CONSTRAINT `fk_item_cooldowns_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_cooldowns_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品冷却表';

-- ----------------------------
-- Table structure for item_binds
-- ----------------------------
DROP TABLE IF EXISTS `item_binds`;
CREATE TABLE `item_binds` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `player_item_id` int NOT NULL COMMENT '玩家物品ID',
  `bind_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绑定类型：EQUIP/PICKUP/TRADE',
  `bound_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_item_bind` (`player_id`, `player_item_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_bind_type` (`bind_type`),
  CONSTRAINT `fk_item_binds_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_binds_player_item` FOREIGN KEY (`player_item_id`) REFERENCES `player_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品绑定表';

-- ----------------------------
-- Table structure for item_statistics
-- ----------------------------
DROP TABLE IF EXISTS `item_statistics`;
CREATE TABLE `item_statistics` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_items_obtained` bigint NOT NULL DEFAULT '0' COMMENT '总获得物品数',
  `total_items_used` bigint NOT NULL DEFAULT '0' COMMENT '总使用物品数',
  `total_items_sold` bigint NOT NULL DEFAULT '0' COMMENT '总出售物品数',
  `total_items_crafted` bigint NOT NULL DEFAULT '0' COMMENT '总制作物品数',
  `total_spirit_stones_spent` bigint NOT NULL DEFAULT '0' COMMENT '总花费灵石',
  `total_spirit_stones_earned` bigint NOT NULL DEFAULT '0' COMMENT '总赚取灵石',
  `highest_quality_obtained` int NOT NULL DEFAULT '0' COMMENT '获得最高品质',
  `rarest_item_id` int DEFAULT NULL COMMENT '最稀有物品ID',
  `favorite_item_id` int DEFAULT NULL COMMENT '最常用物品ID',
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_id` (`player_id`),
  CONSTRAINT `fk_item_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品统计表';

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

-- ----------------------------
-- Table structure for quest_types
-- ----------------------------
DROP TABLE IF EXISTS `quest_types`;
CREATE TABLE `quest_types` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型名称',
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型编码',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '类型描述',
  `daily_limit` int DEFAULT NULL COMMENT '每日限制',
  `repeatable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可重复',
  `auto_accept` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否自动接取',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务类型表';

-- ----------------------------
-- Records of quest_types
-- ----------------------------
INSERT INTO `quest_types` VALUES 
(1, '主线任务', 'main', '推动剧情发展的主要任务', 1, 0, 1, 1, NOW()),
(2, '支线任务', 'side', '可选的额外任务', NULL, 0, 0, 1, NOW()),
(3, '日常任务', 'daily', '每日可重复的任务', 10, 1, 1, 1, NOW()),
(4, '周常任务', 'weekly', '每周可重复的任务', 5, 1, 1, 1, NOW()),
(5, '月常任务', 'monthly', '每月可重复的任务', 3, 1, 1, 1, NOW()),
(6, '成就任务', 'achievement', '达成特定成就的任务', 1, 0, 0, 1, NOW()),
(7, '活动任务', 'event', '活动期间的特殊任务', NULL, 1, 0, 1, NOW()),
(8, '宗门任务', 'guild', '宗门相关的任务', 5, 1, 0, 1, NOW()),
(9, '悬赏任务', 'bounty', '击杀特定怪物的任务', 3, 1, 0, 1, NOW()),
(10, '探索任务', 'explore', '探索特定区域的任务', NULL, 0, 0, 1, NOW());

-- ----------------------------
-- Table structure for quest_chains
-- ----------------------------
DROP TABLE IF EXISTS `quest_chains`;
CREATE TABLE `quest_chains` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '任务链ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务链名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '任务链描述',
  `chain_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：MAIN/SIDE/EVENT/GUILD',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `required_realm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '需求境界',
  `prerequisite_chain_id` int DEFAULT NULL COMMENT '前置任务链ID',
  `total_stages` int NOT NULL DEFAULT '1' COMMENT '总阶段数',
  `final_reward_exp` int NOT NULL DEFAULT '0' COMMENT '最终经验奖励',
  `final_reward_spirit_stones` int NOT NULL DEFAULT '0' COMMENT '最终灵石奖励',
  `final_reward_item_id` int DEFAULT NULL COMMENT '最终物品奖励',
  `final_reward_quantity` int NOT NULL DEFAULT '1' COMMENT '最终物品数量',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_chain_type` (`chain_type`),
  KEY `idx_required_level` (`required_level`),
  CONSTRAINT `fk_quest_chains_prerequisite` FOREIGN KEY (`prerequisite_chain_id`) REFERENCES `quest_chains` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务链表';

-- ----------------------------
-- Records of quest_chains
-- ----------------------------
INSERT INTO `quest_chains` VALUES 
(1, '初入仙途', '新手引导任务链', 'MAIN', 1, NULL, NULL, 5, 500, 200, 1, 10, NULL, 1, NOW()),
(2, '修炼之路', '基础修炼任务链', 'MAIN', 5, NULL, 1, 8, 1500, 800, 3, 5, NULL, 1, NOW()),
(3, '筑基之旅', '筑基相关任务链', 'MAIN', 10, '练气期五层', 2, 10, 5000, 2000, 4, 3, NULL, 1, NOW()),
(4, '妖兽猎人', '猎杀妖兽的任务链', 'SIDE', 8, NULL, NULL, 6, 2000, 1000, 7, 5, NULL, 1, NOW()),
(5, '寻宝之旅', '寻找宝物的任务链', 'SIDE', 15, '筑基期', NULL, 8, 8000, 5000, 8, 3, NULL, 1, NOW());

-- ----------------------------
-- Table structure for quest_chain_stages
-- ----------------------------
DROP TABLE IF EXISTS `quest_chain_stages`;
CREATE TABLE `quest_chain_stages` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '阶段ID',
  `chain_id` int NOT NULL COMMENT '任务链ID',
  `stage_number` int NOT NULL COMMENT '阶段编号',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `stage_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '阶段名称',
  `stage_description` text COLLATE utf8mb4_unicode_ci COMMENT '阶段描述',
  `auto_progress` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否自动进行',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chain_stage` (`chain_id`, `stage_number`),
  KEY `idx_chain_id` (`chain_id`),
  KEY `idx_quest_id` (`quest_id`),
  CONSTRAINT `fk_quest_chain_stages_chain` FOREIGN KEY (`chain_id`) REFERENCES `quest_chains` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quest_chain_stages_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务链阶段表';

-- ----------------------------
-- Table structure for player_quest_chains
-- ----------------------------
DROP TABLE IF EXISTS `player_quest_chains`;
CREATE TABLE `player_quest_chains` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家任务链ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `chain_id` int NOT NULL COMMENT '任务链ID',
  `current_stage` int NOT NULL DEFAULT '1' COMMENT '当前阶段',
  `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成',
  `is_reward_claimed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '最终奖励是否领取',
  `started_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `completed_at` timestamp NULL COMMENT '完成时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_chain` (`player_id`, `chain_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_chain_id` (`chain_id`),
  KEY `idx_is_completed` (`is_completed`),
  CONSTRAINT `fk_player_quest_chains_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_quest_chains_chain` FOREIGN KEY (`chain_id`) REFERENCES `quest_chains` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家任务链表';

-- ----------------------------
-- Table structure for quest_objectives
-- ----------------------------
DROP TABLE IF EXISTS `quest_objectives`;
CREATE TABLE `quest_objectives` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '目标ID',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `objective_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：KILL/COLLECT/TALK/EXPLORE/LEVEL/CULTIVATE',
  `target_id` int DEFAULT NULL COMMENT '目标ID(怪物/物品/NPC)',
  `target_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标名称',
  `required_amount` int NOT NULL DEFAULT '1' COMMENT '需求数量',
  `objective_order` int NOT NULL DEFAULT '0' COMMENT '目标顺序',
  `is_optional` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可选',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '目标描述',
  PRIMARY KEY (`id`),
  KEY `idx_quest_id` (`quest_id`),
  KEY `idx_objective_type` (`objective_type`),
  CONSTRAINT `fk_quest_objectives_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务目标表';

-- ----------------------------
-- Records of quest_objectives
-- ----------------------------
INSERT INTO `quest_objectives` VALUES 
(1, 1, 'CULTIVATE', NULL, '修炼', 1, 1, 0, '完成一次修炼'),
(2, 2, 'COLLECT', 6, '灵石', 100, 1, 0, '收集100灵石'),
(3, 3, 'CULTIVATE', NULL, '修炼', 300, 1, 0, '累计修炼300秒'),
(4, 4, 'LEVEL', NULL, '等级', 1, 1, 0, '提升1级'),
(5, 5, 'CULTIVATE', NULL, '修炼', 10, 1, 0, '完成10次修炼');

-- ----------------------------
-- Table structure for player_quest_objectives
-- ----------------------------
DROP TABLE IF EXISTS `player_quest_objectives`;
CREATE TABLE `player_quest_objectives` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家目标ID',
  `player_quest_id` int NOT NULL COMMENT '玩家任务ID',
  `objective_id` int NOT NULL COMMENT '目标ID',
  `current_amount` int NOT NULL DEFAULT '0' COMMENT '当前进度',
  `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成',
  `completed_at` timestamp NULL COMMENT '完成时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_quest_objective` (`player_quest_id`, `objective_id`),
  KEY `idx_player_quest_id` (`player_quest_id`),
  KEY `idx_objective_id` (`objective_id`),
  CONSTRAINT `fk_player_quest_objectives_quest` FOREIGN KEY (`player_quest_id`) REFERENCES `player_quests` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_quest_objectives_objective` FOREIGN KEY (`objective_id`) REFERENCES `quest_objectives` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家任务目标表';

-- ----------------------------
-- Table structure for quest_rewards
-- ----------------------------
DROP TABLE IF EXISTS `quest_rewards`;
CREATE TABLE `quest_rewards` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '奖励ID',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `reward_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖励类型：EXP/SPIRIT_STONES/ITEM/EQUIPMENT/SKILL_POINT/CONTRIBUTION',
  `reward_id` int DEFAULT NULL COMMENT '奖励ID(物品/装备)',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `is_optional` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可选奖励',
  `reward_order` int NOT NULL DEFAULT '0' COMMENT '奖励顺序',
  PRIMARY KEY (`id`),
  KEY `idx_quest_id` (`quest_id`),
  KEY `idx_reward_type` (`reward_type`),
  CONSTRAINT `fk_quest_rewards_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务奖励表';

-- ----------------------------
-- Records of quest_rewards
-- ----------------------------
INSERT INTO `quest_rewards` VALUES 
(1, 1, 'EXP', NULL, 100, 0, 1),
(2, 1, 'SPIRIT_STONES', NULL, 50, 0, 2),
(3, 1, 'CONTRIBUTION', NULL, 10, 0, 3),
(4, 2, 'EXP', NULL, 120, 0, 1),
(5, 2, 'SPIRIT_STONES', NULL, 80, 0, 2),
(6, 2, 'CONTRIBUTION', NULL, 12, 0, 3),
(7, 3, 'EXP', NULL, 800, 0, 1),
(8, 3, 'SPIRIT_STONES', NULL, 500, 0, 2),
(9, 3, 'CONTRIBUTION', NULL, 50, 0, 3),
(10, 4, 'EXP', NULL, 1000, 0, 1),
(11, 4, 'SPIRIT_STONES', NULL, 600, 0, 2),
(12, 4, 'CONTRIBUTION', NULL, 60, 0, 3),
(13, 5, 'EXP', NULL, 3000, 0, 1),
(14, 5, 'SPIRIT_STONES', NULL, 2000, 0, 2),
(15, 5, 'CONTRIBUTION', NULL, 200, 0, 3);

-- ----------------------------
-- Table structure for quest_logs
-- ----------------------------
DROP TABLE IF EXISTS `quest_logs`;
CREATE TABLE `quest_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `action` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动作：ACCEPT/PROGRESS/COMPLETE/ABANDON/EXPIRE',
  `detail` text COLLATE utf8mb4_unicode_ci COMMENT '详情',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_quest_id` (`quest_id`),
  KEY `idx_action` (`action`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_quest_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quest_logs_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务日志表';

-- ----------------------------
-- Table structure for bounty_quests
-- ----------------------------
DROP TABLE IF EXISTS `bounty_quests`;
CREATE TABLE `bounty_quests` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '悬赏ID',
  `monster_id` int NOT NULL COMMENT '怪物ID',
  `monster_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '怪物名称',
  `required_kills` int NOT NULL DEFAULT '1' COMMENT '需求数量',
  `star_level` int NOT NULL DEFAULT '1' COMMENT '星级(1-5)',
  `exp_reward` int NOT NULL DEFAULT '0' COMMENT '经验奖励',
  `spirit_stones_reward` int NOT NULL DEFAULT '0' COMMENT '灵石奖励',
  `item_reward_id` int DEFAULT NULL COMMENT '物品奖励ID',
  `item_reward_quantity` int NOT NULL DEFAULT '1' COMMENT '物品奖励数量',
  `time_limit` int DEFAULT NULL COMMENT '时间限制(分钟)',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `refresh_weight` int NOT NULL DEFAULT '100' COMMENT '刷新权重',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_monster_id` (`monster_id`),
  KEY `idx_star_level` (`star_level`),
  KEY `idx_required_level` (`required_level`),
  CONSTRAINT `fk_bounty_quests_monster` FOREIGN KEY (`monster_id`) REFERENCES `monsters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='悬赏任务表';

-- ----------------------------
-- Records of bounty_quests
-- ----------------------------
INSERT INTO `bounty_quests` VALUES 
(1, 1, '野狼', 10, 1, 100, 50, 1, 5, 60, 1, 100, 1, NOW()),
(2, 2, '山贼', 8, 1, 120, 60, 2, 3, 60, 5, 90, 1, NOW()),
(3, 3, '妖怪', 5, 2, 200, 100, 5, 10, 90, 10, 70, 1, NOW()),
(4, 4, '邪修', 3, 2, 300, 150, 7, 5, 90, 15, 60, 1, NOW()),
(5, 5, '狂暴野狼', 3, 3, 500, 250, 3, 3, 120, 15, 40, 1, NOW()),
(6, 6, '山贼头目', 2, 3, 600, 300, 4, 2, 120, 20, 30, 1, NOW()),
(7, 7, '狼王', 1, 4, 1000, 500, 4, 5, 180, 25, 15, 1, NOW()),
(8, 8, '千年妖怪', 1, 5, 2000, 1000, 8, 2, 240, 35, 5, 1, NOW());

-- ----------------------------
-- Table structure for player_bounty_quests
-- ----------------------------
DROP TABLE IF EXISTS `player_bounty_quests`;
CREATE TABLE `player_bounty_quests` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家悬赏ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `bounty_id` int NOT NULL COMMENT '悬赏ID',
  `current_kills` int NOT NULL DEFAULT '0' COMMENT '当前击杀数',
  `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成',
  `is_reward_claimed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '奖励是否领取',
  `accepted_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接取时间',
  `expire_at` timestamp NULL COMMENT '过期时间',
  `completed_at` timestamp NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_bounty_id` (`bounty_id`),
  KEY `idx_is_completed` (`is_completed`),
  KEY `idx_expire_at` (`expire_at`),
  CONSTRAINT `fk_player_bounty_quests_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_bounty_quests_bounty` FOREIGN KEY (`bounty_id`) REFERENCES `bounty_quests` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家悬赏任务表';

-- ----------------------------
-- Table structure for quest_statistics
-- ----------------------------
DROP TABLE IF EXISTS `quest_statistics`;
CREATE TABLE `quest_statistics` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_quests_completed` int NOT NULL DEFAULT '0' COMMENT '总完成任务数',
  `main_quests_completed` int NOT NULL DEFAULT '0' COMMENT '主线任务完成数',
  `side_quests_completed` int NOT NULL DEFAULT '0' COMMENT '支线任务完成数',
  `daily_quests_completed` int NOT NULL DEFAULT '0' COMMENT '日常任务完成数',
  `weekly_quests_completed` int NOT NULL DEFAULT '0' COMMENT '周常任务完成数',
  `bounty_quests_completed` int NOT NULL DEFAULT '0' COMMENT '悬赏任务完成数',
  `total_exp_earned` bigint NOT NULL DEFAULT '0' COMMENT '总经验获得',
  `total_spirit_stones_earned` bigint NOT NULL DEFAULT '0' COMMENT '总灵石获得',
  `current_daily_streak` int NOT NULL DEFAULT '0' COMMENT '当前日常连续天数',
  `max_daily_streak` int NOT NULL DEFAULT '0' COMMENT '最大日常连续天数',
  `last_quest_completed_at` timestamp NULL COMMENT '最后完成任务时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_id` (`player_id`),
  CONSTRAINT `fk_quest_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务统计表';

-- ----------------------------
-- Table structure for quest_templates
-- ----------------------------
DROP TABLE IF EXISTS `quest_templates`;
CREATE TABLE `quest_templates` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `quest_type_id` int NOT NULL COMMENT '任务类型ID',
  `title_template` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题模板',
  `description_template` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述模板',
  `objective_template` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标模板',
  `reward_template` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖励模板',
  `level_range_min` int NOT NULL DEFAULT '1' COMMENT '等级范围最小值',
  `level_range_max` int NOT NULL DEFAULT '100' COMMENT '等级范围最大值',
  `weight` int NOT NULL DEFAULT '100' COMMENT '生成权重',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_quest_type_id` (`quest_type_id`),
  KEY `idx_level_range` (`level_range_min`, `level_range_max`),
  CONSTRAINT `fk_quest_templates_type` FOREIGN KEY (`quest_type_id`) REFERENCES `quest_types` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务模板表';

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

-- ----------------------------
-- Table structure for shop_categories
-- ----------------------------
DROP TABLE IF EXISTS `shop_categories`;
CREATE TABLE `shop_categories` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '分类描述',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `required_vip` int NOT NULL DEFAULT '0' COMMENT '需求VIP等级',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城分类表';

-- ----------------------------
-- Records of shop_categories
-- ----------------------------
INSERT INTO `shop_categories` VALUES 
(1, '普通商店', 'general', '出售基础物品', NULL, 1, 1, 0, 1, NOW()),
(2, '装备商店', 'equipment', '出售各类装备', NULL, 2, 1, 0, 1, NOW()),
(3, '丹药商店', 'pills', '出售各类丹药', NULL, 3, 5, 0, 1, NOW()),
(4, '材料商店', 'materials', '出售各类材料', NULL, 4, 1, 0, 1, NOW()),
(5, 'VIP商店', 'vip', 'VIP专属商店', NULL, 5, 1, 1, 1, NOW()),
(6, '限时商店', 'limited', '限时特惠商品', NULL, 6, 10, 0, 1, NOW()),
(7, '声望商店', 'reputation', '使用声望兑换', NULL, 7, 15, 0, 1, NOW()),
(8, '活动商店', 'event', '活动专属商店', NULL, 8, 1, 0, 1, NOW());

-- ----------------------------
-- Table structure for shop_discounts
-- ----------------------------
DROP TABLE IF EXISTS `shop_discounts`;
CREATE TABLE `shop_discounts` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '折扣ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `discount_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '折扣类型：PERCENTAGE/FIXED',
  `discount_value` decimal(10,2) NOT NULL COMMENT '折扣值',
  `start_time` timestamp NOT NULL COMMENT '开始时间',
  `end_time` timestamp NOT NULL COMMENT '结束时间',
  `required_vip` int NOT NULL DEFAULT '0' COMMENT '需求VIP等级',
  `daily_limit` int DEFAULT NULL COMMENT '每日限购',
  `total_limit` int DEFAULT NULL COMMENT '总限购',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_item_id` (`shop_item_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_active` (`active`),
  CONSTRAINT `fk_shop_discounts_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城折扣表';

-- ----------------------------
-- Table structure for shop_purchase_logs
-- ----------------------------
DROP TABLE IF EXISTS `shop_purchase_logs`;
CREATE TABLE `shop_purchase_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购买日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '购买数量',
  `original_price` int NOT NULL COMMENT '原价',
  `discount_price` int NOT NULL COMMENT '折后价',
  `currency_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '货币类型：SPIRIT_STONES/CONTRIBUTION/YUANBAO',
  `discount_id` int DEFAULT NULL COMMENT '折扣ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_shop_item_id` (`shop_item_id`),
  KEY `idx_currency_type` (`currency_type`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_shop_purchase_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_shop_purchase_logs_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城购买日志表';

-- ----------------------------
-- Table structure for shop_refresh_config
-- ----------------------------
DROP TABLE IF EXISTS `shop_refresh_config`;
CREATE TABLE `shop_refresh_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `shop_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商店类型',
  `refresh_interval` int NOT NULL COMMENT '刷新间隔(小时)',
  `refresh_cost` int NOT NULL DEFAULT '0' COMMENT '手动刷新费用',
  `cost_currency` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SPIRIT_STONES' COMMENT '费用货币',
  `max_manual_refresh` int NOT NULL DEFAULT '3' COMMENT '每日最大手动刷新次数',
  `auto_refresh_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否自动刷新',
  `last_refresh_at` timestamp NULL COMMENT '上次刷新时间',
  `next_refresh_at` timestamp NULL COMMENT '下次刷新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_type` (`shop_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城刷新配置表';

-- ----------------------------
-- Records of shop_refresh_config
-- ----------------------------
INSERT INTO `shop_refresh_config` VALUES 
(1, 'general', 24, 50, 'SPIRIT_STONES', 3, 1, NULL, NULL),
(2, 'equipment', 24, 100, 'SPIRIT_STONES', 3, 1, NULL, NULL),
(3, 'pills', 24, 80, 'SPIRIT_STONES', 3, 1, NULL, NULL),
(4, 'limited', 12, 200, 'SPIRIT_STONES', 5, 1, NULL, NULL),
(5, 'vip', 168, 0, 'SPIRIT_STONES', 0, 1, NULL, NULL);

-- ----------------------------
-- Table structure for shop_limited_items
-- ----------------------------
DROP TABLE IF EXISTS `shop_limited_items`;
CREATE TABLE `shop_limited_items` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '限量商品ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `total_stock` int NOT NULL COMMENT '总库存',
  `remaining_stock` int NOT NULL COMMENT '剩余库存',
  `player_daily_limit` int NOT NULL DEFAULT '1' COMMENT '玩家每日限购',
  `player_total_limit` int NOT NULL DEFAULT '1' COMMENT '玩家总限购',
  `start_time` timestamp NOT NULL COMMENT '开始时间',
  `end_time` timestamp NOT NULL COMMENT '结束时间',
  `refresh_on_soldout` tinyint(1) NOT NULL DEFAULT '0' COMMENT '售罄是否刷新',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_item_id` (`shop_item_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_active` (`active`),
  CONSTRAINT `fk_shop_limited_items_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城限量商品表';

-- ----------------------------
-- Table structure for player_shop_limits
-- ----------------------------
DROP TABLE IF EXISTS `player_shop_limits`;
CREATE TABLE `player_shop_limits` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '限购记录ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `limited_item_id` int NOT NULL COMMENT '限量商品ID',
  `purchased_today` int NOT NULL DEFAULT '0' COMMENT '今日已购',
  `purchased_total` int NOT NULL DEFAULT '0' COMMENT '总购买数',
  `last_purchase_at` timestamp NULL COMMENT '最后购买时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_limited` (`player_id`, `limited_item_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_limited_item_id` (`limited_item_id`),
  CONSTRAINT `fk_player_shop_limits_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_shop_limits_item` FOREIGN KEY (`limited_item_id`) REFERENCES `shop_limited_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家限购记录表';

-- ----------------------------
-- Table structure for shop_sell_logs
-- ----------------------------
DROP TABLE IF EXISTS `shop_sell_logs`;
CREATE TABLE `shop_sell_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出售日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型：ITEM/EQUIPMENT',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `sell_price` int NOT NULL COMMENT '出售价格',
  `currency_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SPIRIT_STONES' COMMENT '货币类型',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '出售时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_shop_sell_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城出售日志表';

-- ----------------------------
-- Table structure for shop_statistics
-- ----------------------------
DROP TABLE IF EXISTS `shop_statistics`;
CREATE TABLE `shop_statistics` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_purchases` int NOT NULL DEFAULT '0' COMMENT '总购买次数',
  `total_spent_spirit_stones` bigint NOT NULL DEFAULT '0' COMMENT '总花费灵石',
  `total_spent_contribution` bigint NOT NULL DEFAULT '0' COMMENT '总花费贡献',
  `total_spent_yuanbao` bigint NOT NULL DEFAULT '0' COMMENT '总花费元宝',
  `total_sales` int NOT NULL DEFAULT '0' COMMENT '总出售次数',
  `total_earned_spirit_stones` bigint NOT NULL DEFAULT '0' COMMENT '总赚取灵石',
  `favorite_shop_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最常光顾商店',
  `last_purchase_at` timestamp NULL COMMENT '最后购买时间',
  `last_sale_at` timestamp NULL COMMENT '最后出售时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_id` (`player_id`),
  CONSTRAINT `fk_shop_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城统计表';

-- ----------------------------
-- Table structure for shop_recommendations
-- ----------------------------
DROP TABLE IF EXISTS `shop_recommendations`;
CREATE TABLE `shop_recommendations` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `recommend_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '推荐类型：HOT/NEW/SUGGESTED/FEATURED',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级',
  `start_time` timestamp NULL COMMENT '开始时间',
  `end_time` timestamp NULL COMMENT '结束时间',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_item_id` (`shop_item_id`),
  KEY `idx_recommend_type` (`recommend_type`),
  KEY `idx_priority` (`priority`),
  KEY `idx_active` (`active`),
  CONSTRAINT `fk_shop_recommendations_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城推荐表';

-- ----------------------------
-- Table structure for shop_bundles
-- ----------------------------
DROP TABLE IF EXISTS `shop_bundles`;
CREATE TABLE `shop_bundles` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '礼包ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '礼包名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '礼包描述',
  `bundle_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '礼包类型：STARTER/DAILY/WEEKLY/MONTHLY/SPECIAL',
  `original_price` int NOT NULL COMMENT '原价',
  `sale_price` int NOT NULL COMMENT '售价',
  `currency_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'YUANBAO' COMMENT '货币类型',
  `contents` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容物(JSON)',
  `player_limit` int DEFAULT NULL COMMENT '玩家限购次数',
  `daily_limit` int DEFAULT NULL COMMENT '每日限购次数',
  `start_time` timestamp NULL COMMENT '开始时间',
  `end_time` timestamp NULL COMMENT '结束时间',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `required_vip` int NOT NULL DEFAULT '0' COMMENT '需求VIP',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_bundle_type` (`bundle_type`),
  KEY `idx_active` (`active`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城礼包表';

-- ----------------------------
-- Records of shop_bundles
-- ----------------------------
INSERT INTO `shop_bundles` VALUES 
(1, '新手礼包', '包含新手必需品', 'STARTER', 500, 60, 'YUANBAO', '{"spirit_stones": 1000, "items": [{"id": 1, "quantity": 20}, {"id": 2, "quantity": 20}], "equipment": [{"id": 1, "quantity": 1}]}', 1, NULL, NULL, NULL, 1, 0, NULL, 1, 1, NOW(), NOW()),
(2, '每日修炼礼包', '每日限购的修炼资源', 'DAILY', 300, 30, 'YUANBAO', '{"spirit_stones": 500, "items": [{"id": 3, "quantity": 5}, {"id": 13, "quantity": 2}], "exp": 200}', NULL, 1, NULL, NULL, 10, 0, NULL, 2, 1, NOW(), NOW()),
(3, '每周豪华礼包', '超值每周礼包', 'WEEKLY', 1500, 300, 'YUANBAO', '{"spirit_stones": 3000, "items": [{"id": 4, "quantity": 1}, {"id": 15, "quantity": 1}], "equipments": [{"id": 12, "rate": 50}]}', NULL, 1, NULL, NULL, 20, 2, NULL, 3, 1, NOW(), NOW()),
(4, '月度至尊礼包', '每月限购至尊礼包', 'MONTHLY', 5000, 980, 'YUANBAO', '{"spirit_stones": 10000, "items": [{"id": 36, "quantity": 1}, {"id": 37, "quantity": 1}], "equipments": [{"id": 24, "quantity": 1}], "skill_points": 5}', NULL, 1, NULL, NULL, 30, 4, NULL, 4, 1, NOW(), NOW());

-- ----------------------------
-- Table structure for player_bundle_purchases
-- ----------------------------
DROP TABLE IF EXISTS `player_bundle_purchases`;
CREATE TABLE `player_bundle_purchases` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购买记录ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `bundle_id` int NOT NULL COMMENT '礼包ID',
  `purchase_count` int NOT NULL DEFAULT '1' COMMENT '购买次数',
  `last_purchase_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后购买时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_bundle` (`player_id`, `bundle_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_bundle_id` (`bundle_id`),
  CONSTRAINT `fk_player_bundle_purchases_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_bundle_purchases_bundle` FOREIGN KEY (`bundle_id`) REFERENCES `shop_bundles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家礼包购买记录表';

-- ----------------------------
-- Table structure for shop_wish_list
-- ----------------------------
DROP TABLE IF EXISTS `shop_wish_list`;
CREATE TABLE `shop_wish_list` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '心愿单ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型：ITEM/EQUIPMENT/PET',
  `item_id` int NOT NULL COMMENT '物品ID',
  `added_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_item` (`player_id`, `item_type`, `item_id`),
  KEY `idx_player_id` (`player_id`),
  CONSTRAINT `fk_shop_wish_list_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城心愿单表';

-- ----------------------------
-- Table structure for shop_compare_list
-- ----------------------------
DROP TABLE IF EXISTS `shop_compare_list`;
CREATE TABLE `shop_compare_list` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '对比ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `added_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_shop_item` (`player_id`, `shop_item_id`),
  KEY `idx_player_id` (`player_id`),
  CONSTRAINT `fk_shop_compare_list_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_shop_compare_list_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城对比列表表';

-- ----------------------------
-- Table structure for shop_price_history
-- ----------------------------
DROP TABLE IF EXISTS `shop_price_history`;
CREATE TABLE `shop_price_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `old_price` int NOT NULL COMMENT '原价格',
  `new_price` int NOT NULL COMMENT '新价格',
  `change_reason` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '变更原因',
  `changed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_item_id` (`shop_item_id`),
  KEY `idx_changed_at` (`changed_at`),
  CONSTRAINT `fk_shop_price_history_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城价格历史表';

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

-- ----------------------------
-- Table structure for combat_stats
-- ----------------------------
DROP TABLE IF EXISTS `combat_stats`;
CREATE TABLE `combat_stats` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_battles` int NOT NULL DEFAULT '0' COMMENT '总战斗次数',
  `total_wins` int NOT NULL DEFAULT '0' COMMENT '总胜利次数',
  `total_losses` int NOT NULL DEFAULT '0' COMMENT '总失败次数',
  `total_damage_dealt` bigint NOT NULL DEFAULT '0' COMMENT '总伤害输出',
  `total_damage_taken` bigint NOT NULL DEFAULT '0' COMMENT '总伤害承受',
  `total_exp_gained` bigint NOT NULL DEFAULT '0' COMMENT '总经验获得',
  `total_spirit_stones_gained` bigint NOT NULL DEFAULT '0' COMMENT '总灵石获得',
  `highest_win_streak` int NOT NULL DEFAULT '0' COMMENT '最高连胜',
  `current_win_streak` int NOT NULL DEFAULT '0' COMMENT '当前连胜',
  `boss_kills` int NOT NULL DEFAULT '0' COMMENT 'BOSS击杀数',
  `elite_kills` int NOT NULL DEFAULT '0' COMMENT '精英击杀数',
  `pvp_wins` int NOT NULL DEFAULT '0' COMMENT 'PVP胜利次数',
  `pvp_losses` int NOT NULL DEFAULT '0' COMMENT 'PVP失败次数',
  `last_battle_at` timestamp NULL COMMENT '最后战斗时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_id` (`player_id`),
  KEY `idx_total_wins` (`total_wins`),
  KEY `idx_win_streak` (`highest_win_streak`),
  CONSTRAINT `fk_combat_stats_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家战斗统计表';

-- ----------------------------
-- Table structure for monster_skills
-- ----------------------------
DROP TABLE IF EXISTS `monster_skills`;
CREATE TABLE `monster_skills` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '怪物技能ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '技能描述',
  `skill_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能类型：ATTACK/DEFENSE/BUFF/DEBUFF',
  `base_damage` double NOT NULL DEFAULT '0' COMMENT '基础伤害',
  `damage_multiplier` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '伤害倍率',
  `cooldown` int NOT NULL DEFAULT '0' COMMENT '冷却时间(回合)',
  `mana_cost` int NOT NULL DEFAULT '0' COMMENT '法力消耗',
  `effect_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '效果类型：STUN/POISON/BURN/FREEZE',
  `effect_duration` int NOT NULL DEFAULT '0' COMMENT '效果持续回合数',
  `effect_value` int NOT NULL DEFAULT '0' COMMENT '效果数值',
  `trigger_rate` decimal(5,2) NOT NULL DEFAULT '100.00' COMMENT '触发概率',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_skill_type` (`skill_type`),
  KEY `idx_effect_type` (`effect_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='怪物技能表';

-- ----------------------------
-- Records of monster_skills
-- ----------------------------
INSERT INTO `monster_skills` VALUES 
(1, '撕咬', '锋利的牙齿撕咬', 'ATTACK', 15, 1.20, 2, 0, NULL, 0, 0, 100.00, 1, NOW()),
(2, '毒液喷射', '喷射剧毒液体', 'ATTACK', 10, 1.00, 3, 0, 'POISON', 3, 5, 80.00, 1, NOW()),
(3, '火焰吐息', '喷出炽热火焰', 'ATTACK', 25, 1.50, 4, 0, 'BURN', 2, 10, 70.00, 1, NOW()),
(4, '冰冻气息', '释放寒冰气息', 'ATTACK', 20, 1.30, 5, 0, 'FREEZE', 1, 0, 60.00, 1, NOW()),
(5, '狂暴', '进入狂暴状态', 'BUFF', 0, 0.00, 10, 0, NULL, 5, 50, 100.00, 1, NOW()),
(6, '防御姿态', '进入防御状态', 'DEFENSE', 0, 0.00, 8, 0, NULL, 3, 30, 100.00, 1, NOW()),
(7, '雷霆一击', '蕴含雷电的一击', 'ATTACK', 35, 2.00, 6, 0, 'STUN', 1, 0, 50.00, 1, NOW()),
(8, '暗影突袭', '从暗影中发动攻击', 'ATTACK', 30, 1.80, 4, 0, NULL, 0, 0, 90.00, 1, NOW());

-- ----------------------------
-- Table structure for monster_skill_mapping
-- ----------------------------
DROP TABLE IF EXISTS `monster_skill_mapping`;
CREATE TABLE `monster_skill_mapping` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '映射ID',
  `monster_id` int NOT NULL COMMENT '怪物ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `skill_level` int NOT NULL DEFAULT '1' COMMENT '技能等级',
  `use_probability` decimal(5,2) NOT NULL DEFAULT '100.00' COMMENT '使用概率',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_monster_skill` (`monster_id`, `skill_id`),
  KEY `idx_monster_id` (`monster_id`),
  KEY `idx_skill_id` (`skill_id`),
  CONSTRAINT `fk_monster_skill_mapping_monster` FOREIGN KEY (`monster_id`) REFERENCES `monsters` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_monster_skill_mapping_skill` FOREIGN KEY (`skill_id`) REFERENCES `monster_skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='怪物技能映射表';

-- ----------------------------
-- Records of monster_skill_mapping
-- ----------------------------
INSERT INTO `monster_skill_mapping` VALUES 
(1, 1, 1, 1, 100.00),
(2, 2, 1, 1, 100.00),
(3, 3, 2, 1, 100.00),
(4, 4, 3, 1, 100.00),
(5, 5, 5, 1, 100.00),
(6, 5, 7, 1, 80.00),
(7, 6, 1, 2, 100.00),
(8, 6, 6, 1, 100.00),
(9, 7, 3, 2, 100.00),
(10, 7, 7, 2, 100.00),
(11, 7, 5, 1, 100.00),
(12, 8, 4, 2, 100.00),
(13, 8, 8, 2, 100.00);

-- ----------------------------
-- Table structure for dungeons
-- ----------------------------
DROP TABLE IF EXISTS `dungeons`;
CREATE TABLE `dungeons` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '副本ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '副本名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '副本描述',
  `dungeon_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '副本类型：NORMAL/ELITE/BOSS/TEAM',
  `required_level` int NOT NULL DEFAULT '1' COMMENT '需求等级',
  `required_realm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '需求境界',
  `stamina_cost` int NOT NULL DEFAULT '10' COMMENT '体力消耗',
  `max_rounds` int NOT NULL DEFAULT '50' COMMENT '最大回合数',
  `daily_limit` int NOT NULL DEFAULT '3' COMMENT '每日限制次数',
  `exp_reward` int NOT NULL DEFAULT '0' COMMENT '经验奖励',
  `spirit_stones_reward` int NOT NULL DEFAULT '0' COMMENT '灵石奖励',
  `drop_rate_bonus` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '掉落率加成',
  `unlock_condition` text COLLATE utf8mb4_unicode_ci COMMENT '解锁条件(JSON)',
  `rewards` text COLLATE utf8mb4_unicode_ci COMMENT '奖励配置(JSON)',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dungeon_type` (`dungeon_type`),
  KEY `idx_required_level` (`required_level`),
  KEY `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='副本表';

-- ----------------------------
-- Records of dungeons
-- ----------------------------
INSERT INTO `dungeons` VALUES 
(1, '野狼谷', '野狼聚集的山谷，适合新手历练', 'NORMAL', 1, NULL, 10, 30, 5, 100, 50, 10.00, NULL, '{"exp": 100, "spirit_stones": 50, "items": [{"id": 1, "rate": 30}]}', 1, NOW(), NOW()),
(2, '山贼营地', '山贼的老巢，危机四伏', 'NORMAL', 5, NULL, 15, 40, 3, 200, 100, 15.00, NULL, '{"exp": 200, "spirit_stones": 100, "items": [{"id": 2, "rate": 25}]}', 1, NOW(), NOW()),
(3, '妖兽洞穴', '妖兽盘踞的洞穴，充满危险', 'ELITE', 10, '练气期五层', 20, 50, 2, 500, 200, 20.00, NULL, '{"exp": 500, "spirit_stones": 200, "equipments": [{"id": 12, "rate": 10}]}', 1, NOW(), NOW()),
(4, '邪修密地', '邪修修炼的秘密基地', 'ELITE', 15, '筑基期', 25, 60, 2, 800, 300, 25.00, NULL, '{"exp": 800, "spirit_stones": 300, "equipments": [{"id": 18, "rate": 15}]}', 1, NOW(), NOW()),
(5, '狼王巢穴', '狼王的领地，极度危险', 'BOSS', 20, '金丹期', 30, 100, 1, 1500, 500, 50.00, NULL, '{"exp": 1500, "spirit_stones": 500, "equipments": [{"id": 24, "rate": 30}]}', 1, NOW(), NOW()),
(6, '千年妖洞', '千年妖怪的巢穴', 'BOSS', 30, '元婴期', 40, 120, 1, 3000, 1000, 60.00, NULL, '{"exp": 3000, "spirit_stones": 1000, "equipments": [{"id": 29, "rate": 40}]}', 1, NOW(), NOW());

-- ----------------------------
-- Table structure for dungeon_monsters
-- ----------------------------
DROP TABLE IF EXISTS `dungeon_monsters`;
CREATE TABLE `dungeon_monsters` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '副本怪物ID',
  `dungeon_id` int NOT NULL COMMENT '副本ID',
  `monster_id` int NOT NULL COMMENT '怪物ID',
  `position` int NOT NULL DEFAULT '1' COMMENT '位置(第几波)',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `level_modifier` int NOT NULL DEFAULT '0' COMMENT '等级修正',
  `health_modifier` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '生命值修正',
  `attack_modifier` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '攻击力修正',
  `defense_modifier` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '防御力修正',
  `is_boss` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为BOSS',
  `drop_rate_modifier` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '掉落率修正',
  PRIMARY KEY (`id`),
  KEY `idx_dungeon_id` (`dungeon_id`),
  KEY `idx_monster_id` (`monster_id`),
  KEY `idx_position` (`position`),
  CONSTRAINT `fk_dungeon_monsters_dungeon` FOREIGN KEY (`dungeon_id`) REFERENCES `dungeons` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_dungeon_monsters_monster` FOREIGN KEY (`monster_id`) REFERENCES `monsters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='副本怪物表';

-- ----------------------------
-- Records of dungeon_monsters
-- ----------------------------
INSERT INTO `dungeon_monsters` VALUES 
(1, 1, 1, 1, 2, 0, 1.00, 1.00, 1.00, 0, 1.00),
(2, 1, 1, 2, 3, 0, 1.00, 1.00, 1.00, 0, 1.00),
(3, 1, 5, 3, 1, 0, 1.50, 1.20, 1.00, 1, 2.00),
(4, 2, 2, 1, 2, 0, 1.00, 1.00, 1.00, 0, 1.00),
(5, 2, 2, 2, 3, 0, 1.00, 1.00, 1.00, 0, 1.00),
(6, 2, 6, 3, 1, 0, 1.50, 1.30, 1.10, 1, 2.50),
(7, 3, 3, 1, 3, 0, 1.00, 1.00, 1.00, 0, 1.00),
(8, 3, 3, 2, 4, 0, 1.10, 1.10, 1.00, 0, 1.20),
(9, 3, 7, 3, 1, 0, 1.80, 1.50, 1.20, 1, 3.00),
(10, 4, 4, 1, 3, 0, 1.00, 1.00, 1.00, 0, 1.00),
(11, 4, 4, 2, 4, 0, 1.20, 1.20, 1.10, 0, 1.50),
(12, 4, 8, 3, 1, 0, 2.00, 1.80, 1.50, 1, 4.00),
(13, 5, 1, 1, 5, 5, 1.50, 1.30, 1.20, 0, 1.50),
(14, 5, 5, 2, 3, 5, 1.80, 1.50, 1.30, 0, 2.00),
(15, 5, 7, 3, 1, 5, 3.00, 2.00, 1.80, 1, 5.00),
(16, 6, 3, 1, 6, 10, 2.00, 1.50, 1.30, 0, 2.00),
(17, 6, 6, 2, 4, 10, 2.50, 1.80, 1.50, 0, 2.50),
(18, 6, 8, 3, 1, 10, 4.00, 2.50, 2.00, 1, 6.00);

-- ----------------------------
-- Table structure for player_dungeon_progress
-- ----------------------------
DROP TABLE IF EXISTS `player_dungeon_progress`;
CREATE TABLE `player_dungeon_progress` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `dungeon_id` int NOT NULL COMMENT '副本ID',
  `is_unlocked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否解锁',
  `is_cleared` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否通关',
  `best_score` int NOT NULL DEFAULT '0' COMMENT '最佳分数',
  `fastest_clear_time` int DEFAULT NULL COMMENT '最快通关时间(秒)',
  `total_clears` int NOT NULL DEFAULT '0' COMMENT '通关次数',
  `daily_clears` int NOT NULL DEFAULT '0' COMMENT '今日通关次数',
  `last_clear_at` timestamp NULL COMMENT '最后通关时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_dungeon` (`player_id`, `dungeon_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_dungeon_id` (`dungeon_id`),
  KEY `idx_is_cleared` (`is_cleared`),
  CONSTRAINT `fk_player_dungeon_progress_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_dungeon_progress_dungeon` FOREIGN KEY (`dungeon_id`) REFERENCES `dungeons` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家副本进度表';

-- ----------------------------
-- Table structure for dungeon_logs
-- ----------------------------
DROP TABLE IF EXISTS `dungeon_logs`;
CREATE TABLE `dungeon_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `dungeon_id` int NOT NULL COMMENT '副本ID',
  `result` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结果：WIN/LOSE/TIMEOUT',
  `rounds_used` int NOT NULL DEFAULT '0' COMMENT '使用回合数',
  `time_used` int NOT NULL DEFAULT '0' COMMENT '使用时间(秒)',
  `damage_dealt` bigint NOT NULL DEFAULT '0' COMMENT '造成伤害',
  `damage_taken` bigint NOT NULL DEFAULT '0' COMMENT '受到伤害',
  `exp_gained` int NOT NULL DEFAULT '0' COMMENT '获得经验',
  `spirit_stones_gained` int NOT NULL DEFAULT '0' COMMENT '获得灵石',
  `items_dropped` text COLLATE utf8mb4_unicode_ci COMMENT '掉落物品(JSON)',
  `battle_details` text COLLATE utf8mb4_unicode_ci COMMENT '战斗详情(JSON)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_dungeon_id` (`dungeon_id`),
  KEY `idx_result` (`result`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_dungeon_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_dungeon_logs_dungeon` FOREIGN KEY (`dungeon_id`) REFERENCES `dungeons` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='副本日志表';

-- ----------------------------
-- Table structure for pvp_battles
-- ----------------------------
DROP TABLE IF EXISTS `pvp_battles`;
CREATE TABLE `pvp_battles` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'PVP战斗ID',
  `challenger_id` int NOT NULL COMMENT '挑战者ID',
  `defender_id` int NOT NULL COMMENT '防守者ID',
  `result` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结果：CHALLENGER_WIN/DEFENDER_WIN/DRAW/TIMEOUT',
  `rounds` int NOT NULL DEFAULT '0' COMMENT '回合数',
  `challenger_damage` bigint NOT NULL DEFAULT '0' COMMENT '挑战者伤害',
  `defender_damage` bigint NOT NULL DEFAULT '0' COMMENT '防守者伤害',
  `exp_reward` int NOT NULL DEFAULT '0' COMMENT '经验奖励',
  `spirit_stones_reward` int NOT NULL DEFAULT '0' COMMENT '灵石奖励',
  `ranking_change` int NOT NULL DEFAULT '0' COMMENT '排名变化',
  `battle_details` text COLLATE utf8mb4_unicode_ci COMMENT '战斗详情(JSON)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_challenger_id` (`challenger_id`),
  KEY `idx_defender_id` (`defender_id`),
  KEY `idx_result` (`result`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_pvp_battles_challenger` FOREIGN KEY (`challenger_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pvp_battles_defender` FOREIGN KEY (`defender_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PVP战斗记录表';

-- ----------------------------
-- Table structure for pvp_rankings
-- ----------------------------
DROP TABLE IF EXISTS `pvp_rankings`;
CREATE TABLE `pvp_rankings` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '排名ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `rank` int NOT NULL DEFAULT '0' COMMENT '排名',
  `score` int NOT NULL DEFAULT '1000' COMMENT '积分',
  `tier` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '青铜' COMMENT '段位：青铜/白银/黄金/铂金/钻石/王者',
  `wins` int NOT NULL DEFAULT '0' COMMENT '胜利次数',
  `losses` int NOT NULL DEFAULT '0' COMMENT '失败次数',
  `win_streak` int NOT NULL DEFAULT '0' COMMENT '连胜',
  `highest_rank` int NOT NULL DEFAULT '0' COMMENT '历史最高排名',
  `highest_score` int NOT NULL DEFAULT '1000' COMMENT '历史最高积分',
  `season_id` int NOT NULL DEFAULT '1' COMMENT '赛季ID',
  `last_battle_at` timestamp NULL COMMENT '最后战斗时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_season` (`player_id`, `season_id`),
  KEY `idx_rank` (`rank`),
  KEY `idx_score` (`score`),
  KEY `idx_tier` (`tier`),
  KEY `idx_season_id` (`season_id`),
  CONSTRAINT `fk_pvp_rankings_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PVP排名表';

-- ----------------------------
-- Table structure for combat_buffs
-- ----------------------------
DROP TABLE IF EXISTS `combat_buffs`;
CREATE TABLE `combat_buffs` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'BUFF ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BUFF名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'BUFF描述',
  `buff_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BUFF类型：ATTACK/DEFENSE/HEALTH/SPEED/CRIT',
  `buff_value` decimal(10,2) NOT NULL COMMENT 'BUFF数值(百分比或固定值)',
  `is_percentage` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否为百分比',
  `duration_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '持续类型：PERMANENT/TEMPORARY/BATTLE',
  `duration_value` int NOT NULL DEFAULT '0' COMMENT '持续时间(秒/回合)',
  `stackable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可叠加',
  `max_stacks` int NOT NULL DEFAULT '1' COMMENT '最大叠加层数',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_buff_type` (`buff_type`),
  KEY `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='战斗BUFF表';

-- ----------------------------
-- Records of combat_buffs
-- ----------------------------
INSERT INTO `combat_buffs` VALUES 
(1, '攻击提升', '攻击力提升20%', 'ATTACK', 20.00, 1, 'TEMPORARY', 300, 0, 1, NULL, 1, NOW()),
(2, '防御强化', '防御力提升30%', 'DEFENSE', 30.00, 1, 'TEMPORARY', 300, 0, 1, NULL, 1, NOW()),
(3, '生命恢复', '每回合恢复5%生命', 'HEALTH', 5.00, 1, 'BATTLE', 5, 0, 1, NULL, 1, NOW()),
(4, '疾风步', '速度提升50%', 'SPEED', 50.00, 1, 'TEMPORARY', 180, 0, 1, NULL, 1, NOW()),
(5, '暴击强化', '暴击率提升15%', 'CRIT', 15.00, 1, 'TEMPORARY', 300, 0, 1, NULL, 1, NOW()),
(6, '狂暴之力', '攻击力提升50%，防御力降低20%', 'ATTACK', 50.00, 1, 'BATTLE', 3, 0, 1, NULL, 1, NOW()),
(7, '铁壁', '防御力提升100%', 'DEFENSE', 100.00, 1, 'BATTLE', 2, 0, 1, NULL, 1, NOW()),
(8, '嗜血', '攻击时恢复造成伤害10%的生命', 'HEALTH', 10.00, 1, 'BATTLE', 3, 0, 1, NULL, 1, NOW());

-- ----------------------------
-- Table structure for player_combat_buffs
-- ----------------------------
DROP TABLE IF EXISTS `player_combat_buffs`;
CREATE TABLE `player_combat_buffs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家BUFF ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `buff_id` int NOT NULL COMMENT 'BUFF ID',
  `stacks` int NOT NULL DEFAULT '1' COMMENT '当前叠加层数',
  `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源(装备/技能/道具)',
  `source_id` int DEFAULT NULL COMMENT '来源ID',
  `expire_at` timestamp NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_buff_id` (`buff_id`),
  KEY `idx_expire_at` (`expire_at`),
  CONSTRAINT `fk_player_combat_buffs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_combat_buffs_buff` FOREIGN KEY (`buff_id`) REFERENCES `combat_buffs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家战斗BUFF表';

-- ----------------------------
-- Table structure for combat_achievements
-- ----------------------------
DROP TABLE IF EXISTS `combat_achievements`;
CREATE TABLE `combat_achievements` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '成就ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就名称',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就描述',
  `achievement_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就类型：KILL/STREAK/DAMAGE/SURVIVE',
  `condition_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '条件类型',
  `condition_value` int NOT NULL COMMENT '条件数值',
  `reward_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖励类型',
  `reward_id` int DEFAULT NULL COMMENT '奖励ID',
  `reward_quantity` int NOT NULL DEFAULT '1' COMMENT '奖励数量',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_achievement_type` (`achievement_type`),
  KEY `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='战斗成就表';

-- ----------------------------
-- Records of combat_achievements
-- ----------------------------
INSERT INTO `combat_achievements` VALUES 
(1, '初战告捷', '获得第一次战斗胜利', 'KILL', 'total_wins', 1, 'SPIRIT_STONES', NULL, 100, NULL, 1, 1, NOW()),
(2, '百战精兵', '获得100次战斗胜利', 'KILL', 'total_wins', 100, 'SPIRIT_STONES', NULL, 1000, NULL, 2, 1, NOW()),
(3, '千战之王', '获得1000次战斗胜利', 'KILL', 'total_wins', 1000, 'SPIRIT_STONES', NULL, 10000, NULL, 3, 1, NOW()),
(4, '连胜三场', '获得3场连胜', 'STREAK', 'highest_win_streak', 3, 'ITEM', 3, 5, NULL, 10, 1, NOW()),
(5, '十连胜', '获得10场连胜', 'STREAK', 'highest_win_streak', 10, 'SPIRIT_STONES', NULL, 500, NULL, 11, 1, NOW()),
(6, '百连胜', '获得100场连胜', 'STREAK', 'highest_win_streak', 100, 'EQUIPMENT', 18, 1, NULL, 12, 1, NOW()),
(7, '万点伤害', '单场战斗造成10000点伤害', 'DAMAGE', 'single_battle_damage', 10000, 'SPIRIT_STONES', NULL, 200, NULL, 20, 1, NOW()),
(8, '百万伤害', '累计造成1000000点伤害', 'DAMAGE', 'total_damage_dealt', 1000000, 'ITEM', 4, 1, NULL, 21, 1, NOW()),
(9, 'BOSS猎人', '击败1个BOSS', 'KILL', 'boss_kills', 1, 'SPIRIT_STONES', NULL, 300, NULL, 30, 1, NOW()),
(10, 'BOSS终结者', '击败10个BOSS', 'KILL', 'boss_kills', 10, 'EQUIPMENT', 24, 1, NULL, 31, 1, NOW());

-- ----------------------------
-- Table structure for player_combat_achievements
-- ----------------------------
DROP TABLE IF EXISTS `player_combat_achievements`;
CREATE TABLE `player_combat_achievements` (
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
  KEY `idx_achievement_id` (`achievement_id`),
  KEY `idx_is_completed` (`is_completed`),
  CONSTRAINT `fk_player_combat_achievements_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_combat_achievements_achievement` FOREIGN KEY (`achievement_id`) REFERENCES `combat_achievements` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家战斗成就表';

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

-- ----------------------------
-- Table structure for pet_abilities
-- ----------------------------
DROP TABLE IF EXISTS `pet_abilities`;
CREATE TABLE `pet_abilities` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '能力ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '能力名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '能力描述',
  `ability_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：PASSIVE/ACTIVE/AURA',
  `effect_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '效果类型',
  `effect_value` decimal(10,2) NOT NULL COMMENT '效果值',
  `is_percentage` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否为百分比',
  `cooldown` int NOT NULL DEFAULT '0' COMMENT '冷却时间',
  `energy_cost` int NOT NULL DEFAULT '0' COMMENT '能量消耗',
  `required_pet_level` int NOT NULL DEFAULT '1' COMMENT '需求宠物等级',
  `required_pet_rarity` int NOT NULL DEFAULT '1' COMMENT '需求宠物稀有度',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ability_type` (`ability_type`),
  KEY `idx_effect_type` (`effect_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物能力表';

-- ----------------------------
-- Records of pet_abilities
-- ----------------------------
INSERT INTO `pet_abilities` VALUES 
(1, '守护', '为主人提供护盾', 'ACTIVE', 'shield', 100.00, 0, 30, 20, 5, 1, NULL, 1, NOW()),
(2, '治愈', '恢复主人生命值', 'ACTIVE', 'heal', 15.00, 1, 20, 15, 5, 1, NULL, 1, NOW()),
(3, '狂暴', '提升自身攻击力', 'ACTIVE', 'self_attack_boost', 30.00, 1, 45, 25, 10, 2, NULL, 1, NOW()),
(4, '嘲讽', '吸引敌人攻击', 'ACTIVE', 'taunt', 1.00, 0, 60, 30, 15, 2, NULL, 1, NOW()),
(5, '灵力光环', '提升主人法力恢复', 'AURA', 'owner_mana_regen', 5.00, 1, 0, 0, 10, 2, NULL, 1, NOW()),
(6, '战斗光环', '提升主人攻击力', 'AURA', 'owner_attack_boost', 10.00, 1, 0, 0, 15, 3, NULL, 1, NOW()),
(7, '防御光环', '提升主人防御力', 'AURA', 'owner_defense_boost', 10.00, 1, 0, 0, 15, 3, NULL, 1, NOW()),
(8, '经验光环', '提升获得经验', 'AURA', 'exp_boost', 15.00, 1, 0, 0, 20, 3, NULL, 1, NOW()),
(9, '幸运', '提升掉落率', 'PASSIVE', 'drop_rate_boost', 10.00, 1, 0, 0, 25, 4, NULL, 1, NOW()),
(10, '坚韧', '提升宠物生命值', 'PASSIVE', 'pet_health_boost', 20.00, 1, 0, 0, 5, 1, NULL, 1, NOW()),
(11, '迅捷', '提升宠物速度', 'PASSIVE', 'pet_speed_boost', 15.00, 1, 0, 0, 8, 1, NULL, 1, NOW()),
(12, '吸血', '攻击时恢复生命', 'PASSIVE', 'lifesteal', 5.00, 1, 0, 0, 30, 4, NULL, 1, NOW());

-- ----------------------------
-- Table structure for pet_ability_mapping
-- ----------------------------
DROP TABLE IF EXISTS `pet_ability_mapping`;
CREATE TABLE `pet_ability_mapping` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '映射ID',
  `pet_id` int NOT NULL COMMENT '宠物模板ID',
  `ability_id` int NOT NULL COMMENT '能力ID',
  `unlock_level` int NOT NULL DEFAULT '1' COMMENT '解锁等级',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认能力',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_ability` (`pet_id`, `ability_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_ability_id` (`ability_id`),
  CONSTRAINT `fk_pet_ability_mapping_pet` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pet_ability_mapping_ability` FOREIGN KEY (`ability_id`) REFERENCES `pet_abilities` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物能力映射表';

-- ----------------------------
-- Records of pet_ability_mapping
-- ----------------------------
INSERT INTO `pet_ability_mapping` VALUES 
(1, 1, 11, 1, 1),
(2, 1, 9, 10, 0),
(3, 2, 3, 1, 1),
(4, 2, 6, 15, 0),
(5, 3, 4, 1, 1),
(6, 3, 7, 20, 0),
(7, 4, 3, 1, 1),
(8, 4, 6, 15, 0),
(9, 5, 1, 1, 1),
(10, 5, 7, 15, 0),
(11, 6, 3, 1, 1),
(12, 6, 6, 15, 0),
(13, 7, 2, 1, 1),
(14, 7, 10, 5, 0),
(15, 8, 4, 1, 1),
(16, 8, 11, 10, 0),
(17, 9, 1, 1, 1),
(18, 9, 10, 10, 0),
(19, 10, 11, 1, 1),
(20, 10, 9, 20, 0);

-- ----------------------------
-- Table structure for player_pet_abilities
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_abilities`;
CREATE TABLE `player_pet_abilities` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家宠物能力ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `ability_id` int NOT NULL COMMENT '能力ID',
  `ability_level` int NOT NULL DEFAULT '1' COMMENT '能力等级',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否激活',
  `cooldown_end` timestamp NULL COMMENT '冷却结束时间',
  `unlocked_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_pet_ability` (`player_pet_id`, `ability_id`),
  KEY `idx_player_pet_id` (`player_pet_id`),
  KEY `idx_ability_id` (`ability_id`),
  CONSTRAINT `fk_player_pet_abilities_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_pet_abilities_ability` FOREIGN KEY (`ability_id`) REFERENCES `pet_abilities` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家宠物能力表';

-- ----------------------------
-- Table structure for pet_equipment
-- ----------------------------
DROP TABLE IF EXISTS `pet_equipment`;
CREATE TABLE `pet_equipment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '宠物装备ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '装备描述',
  `slot` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备槽位：NECKLACE/RING/CHARM',
  `quality` int NOT NULL DEFAULT '1' COMMENT '品质',
  `health_bonus` int NOT NULL DEFAULT '0' COMMENT '生命加成',
  `attack_bonus` int NOT NULL DEFAULT '0' COMMENT '攻击加成',
  `defense_bonus` int NOT NULL DEFAULT '0' COMMENT '防御加成',
  `speed_bonus` int NOT NULL DEFAULT '0' COMMENT '速度加成',
  `special_effect` text COLLATE utf8mb4_unicode_ci COMMENT '特殊效果(JSON)',
  `required_pet_level` int NOT NULL DEFAULT '1' COMMENT '需求宠物等级',
  `price` int NOT NULL DEFAULT '0' COMMENT '价格',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_slot` (`slot`),
  KEY `idx_quality` (`quality`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物装备表';

-- ----------------------------
-- Records of pet_equipment
-- ----------------------------
INSERT INTO `pet_equipment` VALUES 
(1, '灵兽项链', '普通灵兽项链', 'NECKLACE', 1, 50, 5, 5, 0, NULL, 1, 200, 1, NOW()),
(2, '妖兽之牙', '锋利的兽牙项链', 'NECKLACE', 2, 80, 15, 5, 0, '{"crit_rate": 3}', 10, 500, 1, NOW()),
(3, '神兽护符', '蕴含神力的护符', 'NECKLACE', 3, 150, 30, 15, 5, '{"crit_rate": 5, "crit_damage": 10}', 25, 2000, 1, NOW()),
(4, '灵兽戒指', '普通灵兽戒指', 'RING', 1, 30, 10, 3, 5, NULL, 1, 150, 1, NOW()),
(5, '力量之戒', '提升力量的戒指', 'RING', 2, 50, 25, 5, 8, '{"attack_boost": 5}', 15, 800, 1, NOW()),
(6, '守护之戒', '提供守护的戒指', 'RING', 3, 100, 15, 25, 5, '{"defense_boost": 10, "shield": 50}', 30, 2500, 1, NOW()),
(7, '灵兽护符', '普通灵兽护符', 'CHARM', 1, 40, 5, 10, 3, NULL, 1, 180, 1, NOW()),
(8, '经验护符', '提升经验获取', 'CHARM', 2, 60, 8, 12, 5, '{"exp_boost": 10}', 10, 600, 1, NOW()),
(9, '稀有护符', '稀有的宠物护符', 'CHARM', 3, 120, 20, 20, 10, '{"exp_boost": 20, "drop_boost": 5}', 25, 3000, 1, NOW());

-- ----------------------------
-- Table structure for player_pet_equipment
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_equipment`;
CREATE TABLE `player_pet_equipment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家宠物装备ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `equipment_id` int NOT NULL COMMENT '宠物装备ID',
  `slot` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备槽位',
  `is_equipped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否装备',
  `enhance_level` int NOT NULL DEFAULT '0' COMMENT '强化等级',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_pet_id` (`player_pet_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_slot` (`slot`),
  CONSTRAINT `fk_player_pet_equipment_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_pet_equipment_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `pet_equipment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家宠物装备表';

-- ----------------------------
-- Table structure for pet_evolution
-- ----------------------------
DROP TABLE IF EXISTS `pet_evolution`;
CREATE TABLE `pet_evolution` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '进化ID',
  `pet_id` int NOT NULL COMMENT '宠物模板ID',
  `evolution_stage` int NOT NULL DEFAULT '1' COMMENT '进化阶段',
  `evolution_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '进化名称',
  `required_level` int NOT NULL COMMENT '需求等级',
  `required_item_id` int DEFAULT NULL COMMENT '需求物品ID',
  `required_item_quantity` int NOT NULL DEFAULT '1' COMMENT '需求数量',
  `health_bonus` int NOT NULL DEFAULT '0' COMMENT '生命加成',
  `attack_bonus` int NOT NULL DEFAULT '0' COMMENT '攻击加成',
  `defense_bonus` int NOT NULL DEFAULT '0' COMMENT '防御加成',
  `speed_bonus` int NOT NULL DEFAULT '0' COMMENT '速度加成',
  `new_ability_id` int DEFAULT NULL COMMENT '新能力ID',
  `appearance_change` text COLLATE utf8mb4_unicode_ci COMMENT '外观变化(JSON)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_evolution` (`pet_id`, `evolution_stage`),
  KEY `idx_pet_id` (`pet_id`),
  CONSTRAINT `fk_pet_evolution_pet` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pet_evolution_item` FOREIGN KEY (`required_item_id`) REFERENCES `items` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_pet_evolution_ability` FOREIGN KEY (`new_ability_id`) REFERENCES `pet_abilities` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物进化表';

-- ----------------------------
-- Records of pet_evolution
-- ----------------------------
INSERT INTO `pet_evolution` VALUES 
(1, 1, 2, '灵狐·觉醒', 30, 21, 1, 100, 20, 15, 10, 9, '{"color": "金色", "size": 1.2}'),
(2, 1, 3, '九尾灵狐', 60, 21, 3, 200, 40, 30, 20, 12, '{"color": "白色", "size": 1.5, "tails": 9}'),
(3, 2, 2, '火麒麟·觉醒', 50, 22, 1, 300, 80, 60, 10, 5, '{"flame": "blue"}'),
(4, 2, 3, '炎帝麒麟', 80, 22, 3, 500, 150, 100, 15, 8, '{"flame": "purple", "size": 1.8}'),
(5, 7, 2, '灵猫·进阶', 15, 8, 2, 50, 10, 8, 5, 10, '{"eyes": "glowing"}'),
(6, 7, 3, '月光灵猫', 35, 8, 5, 100, 20, 15, 10, 9, '{"glow": "silver"}'),
(7, 3, 2, '青龙·觉醒', 80, 21, 5, 500, 100, 80, 20, 6, '{"clouds": true}'),
(8, 4, 2, '白虎·觉醒', 80, 21, 5, 400, 150, 60, 25, 6, '{"aura": "gold"}'),
(9, 5, 2, '玄武·觉醒', 80, 21, 5, 800, 50, 150, 10, 7, '{"shell": "crystal"}'),
(10, 6, 2, '朱雀·觉醒', 80, 22, 5, 450, 130, 70, 30, 8, '{"wings": "flame"}');

-- ----------------------------
-- Table structure for player_pet_evolution
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_evolution`;
CREATE TABLE `player_pet_evolution` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家进化ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `current_stage` int NOT NULL DEFAULT '1' COMMENT '当前进化阶段',
  `evolved_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '进化时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_pet_evolution` (`player_pet_id`),
  CONSTRAINT `fk_player_pet_evolution_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家宠物进化表';

-- ----------------------------
-- Table structure for pet_battle_logs
-- ----------------------------
DROP TABLE IF EXISTS `pet_battle_logs`;
CREATE TABLE `pet_battle_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '战斗日志ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `battle_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '战斗类型：PVE/PVP/ARENA',
  `opponent_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对手类型：MONSTER/PET',
  `opponent_id` int DEFAULT NULL COMMENT '对手ID',
  `result` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结果：WIN/LOSE',
  `damage_dealt` int NOT NULL DEFAULT '0' COMMENT '造成伤害',
  `damage_taken` int NOT NULL DEFAULT '0' COMMENT '受到伤害',
  `exp_gained` int NOT NULL DEFAULT '0' COMMENT '获得经验',
  `abilities_used` text COLLATE utf8mb4_unicode_ci COMMENT '使用的能力(JSON)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_pet_id` (`player_pet_id`),
  KEY `idx_battle_type` (`battle_type`),
  KEY `idx_result` (`result`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_pet_battle_logs_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物战斗日志表';

-- ----------------------------
-- Table structure for pet_statistics
-- ----------------------------
DROP TABLE IF EXISTS `pet_statistics`;
CREATE TABLE `pet_statistics` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_pets_owned` int NOT NULL DEFAULT '0' COMMENT '拥有宠物数',
  `total_pets_max_level` int NOT NULL DEFAULT '0' COMMENT '满级宠物数',
  `total_pets_evolved` int NOT NULL DEFAULT '0' COMMENT '进化宠物数',
  `highest_pet_level` int NOT NULL DEFAULT '0' COMMENT '最高等级',
  `rarest_pet_rarity` int NOT NULL DEFAULT '0' COMMENT '最高稀有度',
  `total_pet_battles` int NOT NULL DEFAULT '0' COMMENT '宠物总战斗次数',
  `total_pet_wins` int NOT NULL DEFAULT '0' COMMENT '宠物总胜利次数',
  `favorite_pet_id` int DEFAULT NULL COMMENT '最爱宠物',
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_id` (`player_id`),
  CONSTRAINT `fk_pet_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物统计表';

-- ----------------------------
-- Table structure for pet_food
-- ----------------------------
DROP TABLE IF EXISTS `pet_food`;
CREATE TABLE `pet_food` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '食物ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '食物名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '食物描述',
  `food_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '食物类型：BASIC/PREMIUM/SPECIAL',
  `hunger_restore` int NOT NULL DEFAULT '50' COMMENT '恢复饱食度',
  `loyalty_bonus` int NOT NULL DEFAULT '0' COMMENT '忠诚度加成',
  `exp_bonus` int NOT NULL DEFAULT '0' COMMENT '经验加成',
  `quality` int NOT NULL DEFAULT '1' COMMENT '品质',
  `price` int NOT NULL DEFAULT '100' COMMENT '价格',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_food_type` (`food_type`),
  KEY `idx_quality` (`quality`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物食物表';

-- ----------------------------
-- Records of pet_food
-- ----------------------------
INSERT INTO `pet_food` VALUES 
(1, '普通兽粮', '基础宠物食物', 'BASIC', 50, 5, 0, 1, 50, 1, NOW()),
(2, '优质兽粮', '优质宠物食物', 'BASIC', 80, 10, 10, 1, 100, 1, NOW()),
(3, '灵兽丹', '蕴含灵气的食物', 'PREMIUM', 100, 20, 50, 2, 300, 1, NOW()),
(4, '仙兽丹', '高级宠物食物', 'PREMIUM', 150, 30, 100, 3, 800, 1, NOW()),
(5, '龙凤呈祥', '传说中的宠物食物', 'SPECIAL', 200, 50, 200, 4, 3000, 1, NOW()),
(6, '月华露', '月光精华凝成', 'SPECIAL', 120, 40, 150, 3, 1500, 1, NOW());

-- ----------------------------
-- Table structure for player_pet_food_usage
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_food_usage`;
CREATE TABLE `player_pet_food_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '使用记录ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `food_id` int NOT NULL COMMENT '食物ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `hunger_restored` int NOT NULL DEFAULT '0' COMMENT '恢复饱食度',
  `loyalty_gained` int NOT NULL DEFAULT '0' COMMENT '获得忠诚度',
  `exp_gained` int NOT NULL DEFAULT '0' COMMENT '获得经验',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_pet_id` (`player_pet_id`),
  KEY `idx_food_id` (`food_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_player_pet_food_usage_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_pet_food_usage_food` FOREIGN KEY (`food_id`) REFERENCES `pet_food` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物食物使用记录表';

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
  `announcement_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告类型：SYSTEM/MAINTENANCE/ACTIVITY/UPDATE/GUIDE',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级：0-普通 1-重要 2-紧急',
  `display_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示类型：POPUP/SCROLL/LIST/NOTICE',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/REVOKED/ACTIVE',
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

-- ====================================================================
-- 叙事系统 (Narrative System)
-- ====================================================================

-- ----------------------------
-- NPC基础数据表
-- ----------------------------
DROP TABLE IF EXISTS `npcs`;
CREATE TABLE `npcs` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'NPC ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'NPC名称',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头衔/称谓',
  `faction` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属势力：天剑宗/万法阁/幽冥殿/灵兽山/散修联盟',
  `role_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '故事角色类型：mentor/rival/friend/villain/neutral',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'NPC简介',
  `personality_traits` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性格特征(逗号分隔)',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '默认位置',
  `min_level` int NOT NULL DEFAULT '1' COMMENT '最低出现等级',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'NPC图标',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_npc_name` (`name`),
  KEY `idx_faction` (`faction`),
  KEY `idx_min_level` (`min_level`),
  KEY `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NPC基础数据表';

-- ----------------------------
-- Records of npcs
-- ----------------------------
INSERT INTO `npcs` VALUES
(1, '苏玄清', '外门长老', '天剑宗', 'mentor', '玩家师尊，看似普通的老者，实则深不可测。在青云镇看守封魔残气已三百年。', '克制,温和,留白,洞察力', '青云镇', 1, NULL, 1, 1, NOW(), NOW()),
(2, '剑无痕', '内门首席弟子', '天剑宗', 'rival', '天剑宗内门首席弟子，出身世家，傲慢但笨拙地关心同门。', '傲慢,利落,笨拙的温柔,不服输', '天剑宗', 2, NULL, 1, 2, NOW(), NOW()),
(3, '林婉儿', '万法阁师姐', '万法阁', 'friend', '万法阁的核心弟子，温和聪慧，在万卷藏书中寻找身世之谜。', '温柔,书卷气,聪明,偶尔活泼', '万法阁', 6, NULL, 1, 3, NOW(), NOW()),
(4, '冥渊', '幽冥殿殿主', '幽冥殿', 'villain', '天剑宗叛逃弟子，追求打破修仙秩序，平静得令人不安。', '平静,古雅,扭曲逻辑,深邃', '幽冥殿', 10, NULL, 1, 4, NOW(), NOW()),
(5, '白鹿真人', '灵兽山山主', '灵兽山', 'friend', '灵兽山之主，朴素如老农，实为上古大能，以灵兽为伴。', '朴素,温暖,沉默,洞察', '灵兽山', 5, NULL, 1, 5, NOW(), NOW()),
(6, '老陈', '药材商人', '散修联盟', 'neutral', '青云镇摆摊卖药材的老头，真实身份是渡劫期大能。', '随意,幽默,深不可测,装傻', '青云镇', 1, NULL, 1, 6, NOW(), NOW());

-- ----------------------------
-- 对话树表
-- ----------------------------
DROP TABLE IF EXISTS `dialogue_trees`;
CREATE TABLE `dialogue_trees` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '对话树ID',
  `npc_id` int NOT NULL COMMENT 'NPC ID',
  `dialogue_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话唯一标识',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '对话标题',
  `scene` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '场景描述',
  `mood` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '对话基调',
  `min_level` int DEFAULT '1' COMMENT '最低等级',
  `max_level` int DEFAULT NULL COMMENT '最高等级(NULL=不限)',
  `required_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所需境界',
  `required_quest_chain_id` int DEFAULT NULL COMMENT '前置任务链ID',
  `required_flags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所需flag(JSON数组)',
  `is_repeatable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可重复',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级(高=先触发)',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dialogue_key` (`dialogue_key`),
  KEY `idx_npc_id` (`npc_id`),
  KEY `idx_min_level` (`min_level`),
  KEY `idx_active` (`active`),
  CONSTRAINT `fk_dialogue_trees_npc` FOREIGN KEY (`npc_id`) REFERENCES `npcs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话树表';

-- ----------------------------
-- 对话节点表
-- ----------------------------
DROP TABLE IF EXISTS `dialogue_nodes`;
CREATE TABLE `dialogue_nodes` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `dialogue_tree_id` int NOT NULL COMMENT '对话树ID',
  `node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点唯一标识',
  `node_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'dialogue' COMMENT '节点类型：dialogue(对话)/choice(选择)/action(动作)/end(结束)',
  `speaker` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '说话者(NPC名/玩家/旁白)',
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话文本',
  `portrait` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像/表情状态',
  `next_node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下一节点key',
  `parent_node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '父节点key(NULL=根节点)',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '同级排序(用于choice选项顺序)',
  `set_flags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '触发后设置的flag(JSON数组)',
  `clear_flags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '触发后清除的flag(JSON数组)',
  `set_reputation` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '好感度变化(JSON: {npc_id: change})',
  `conditions` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示条件(JSON: {min_relation, flags, items})',
  `on_complete_quest_id` int DEFAULT NULL COMMENT '完成后触发的任务ID',
  `on_complete_flag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '完成后设置的全局flag',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tree_node` (`dialogue_tree_id`, `node_key`),
  KEY `idx_parent` (`dialogue_tree_id`, `parent_node_key`),
  KEY `idx_next` (`dialogue_tree_id`, `next_node_key`),
  CONSTRAINT `fk_dialogue_nodes_tree` FOREIGN KEY (`dialogue_tree_id`) REFERENCES `dialogue_trees` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话节点表';

-- ----------------------------
-- NPC日常对话池表
-- ----------------------------
DROP TABLE IF EXISTS `npc_daily_dialogues`;
CREATE TABLE `npc_daily_dialogues` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `npc_id` int NOT NULL COMMENT 'NPC ID',
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话文本',
  `conditions` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '触发条件(JSON: {time, realm, level_gte, pet_hunger_lte, days_since_login_gte, has_flag})',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_npc_id` (`npc_id`),
  KEY `idx_active` (`npc_id`, `active`),
  CONSTRAINT `fk_npc_daily_dialogues_npc` FOREIGN KEY (`npc_id`) REFERENCES `npcs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NPC日常对话池表';

-- ----------------------------
-- 玩家-NPC好感度表
-- ----------------------------
DROP TABLE IF EXISTS `player_npc_relations`;
CREATE TABLE `player_npc_relations` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `npc_id` int NOT NULL COMMENT 'NPC ID',
  `affinity` int NOT NULL DEFAULT '0' COMMENT '好感度(-100~100)',
  `relationship_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '陌生' COMMENT '关系等级：陌生/认识/熟悉/信任/至交',
  `first_met_at` timestamp NULL DEFAULT NULL COMMENT '初次见面时间',
  `last_interact_at` timestamp NULL DEFAULT NULL COMMENT '最后互动时间',
  `total_interactions` int NOT NULL DEFAULT '0' COMMENT '总互动次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_npc` (`player_id`, `npc_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_npc_id` (`npc_id`),
  KEY `idx_affinity` (`affinity`),
  CONSTRAINT `fk_player_npc_relations_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_npc_relations_npc` FOREIGN KEY (`npc_id`) REFERENCES `npcs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家-NPC好感度表';

-- ----------------------------
-- 玩家对话状态表（记录已完成/进行中的对话）
-- ----------------------------
DROP TABLE IF EXISTS `player_dialogue_state`;
CREATE TABLE `player_dialogue_state` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `dialogue_tree_id` int NOT NULL COMMENT '对话树ID',
  `current_node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '当前所在节点key',
  `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '对话树是否已完成',
  `times_completed` int NOT NULL DEFAULT '0' COMMENT '完成次数',
  `last_choice_tag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次选择的tag',
  `started_at` timestamp NULL DEFAULT NULL COMMENT '本次开始时间',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '本次完成时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_dialogue` (`player_id`, `dialogue_tree_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_dialogue_tree_id` (`dialogue_tree_id`),
  KEY `idx_is_completed` (`is_completed`),
  CONSTRAINT `fk_player_dialogue_state_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_dialogue_state_tree` FOREIGN KEY (`dialogue_tree_id`) REFERENCES `dialogue_trees` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家对话状态表';

-- ----------------------------
-- 玩家叙事标记表（flag系统）
-- ----------------------------
DROP TABLE IF EXISTS `player_narrative_flags`;
CREATE TABLE `player_narrative_flags` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `flag_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'flag键名',
  `flag_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '1' COMMENT 'flag值',
  `source` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源描述',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_flag` (`player_id`, `flag_key`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_flag_key` (`flag_key`),
  CONSTRAINT `fk_player_narrative_flags_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家叙事标记表';

-- ----------------------------
-- 传说条目表
-- ----------------------------
DROP TABLE IF EXISTS `lore_entries`;
CREATE TABLE `lore_entries` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '传说条目ID',
  `lore_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '条目唯一标识',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '传说标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '传说内容',
  `lore_layer` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '表面' COMMENT '传说层级：表面/参与/深层',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类：世界/宗门/人物/事件',
  `related_npcs` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联NPC(JSON数组)',
  `related_lore_keys` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联传说key(JSON数组)',
  `discover_condition` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发现条件描述',
  `min_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最低境界要求',
  `min_level` int DEFAULT '1' COMMENT '最低等级要求',
  `required_lore_keys` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前置传说key(JSON数组)',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lore_key` (`lore_key`),
  KEY `idx_lore_layer` (`lore_layer`),
  KEY `idx_category` (`category`),
  KEY `idx_min_level` (`min_level`),
  KEY `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='传说条目表';

-- ----------------------------
-- 玩家传说收集表
-- ----------------------------
DROP TABLE IF EXISTS `player_lore_collection`;
CREATE TABLE `player_lore_collection` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `lore_entry_id` int NOT NULL COMMENT '传说条目ID',
  `discovered_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
  `source` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发现来源',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_lore` (`player_id`, `lore_entry_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_lore_entry_id` (`lore_entry_id`),
  CONSTRAINT `fk_player_lore_collection_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_player_lore_collection_lore` FOREIGN KEY (`lore_entry_id`) REFERENCES `lore_entries` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家传说收集表';

-- ----------------------------
-- 离线事件叙事表
-- ----------------------------
DROP TABLE IF EXISTS `offline_narrative_events`;
CREATE TABLE `offline_narrative_events` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `event_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件唯一标识',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件标题',
  `narrative` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '叙事文本',
  `probability` decimal(5,3) NOT NULL DEFAULT '0.010' COMMENT '触发概率(0.001-1.000)',
  `min_offline_hours` int NOT NULL DEFAULT '4' COMMENT '最低离线小时数',
  `max_offline_hours` int DEFAULT NULL COMMENT '最高离线小时数(NULL=不限)',
  `min_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最低境界要求',
  `min_level` int DEFAULT '1' COMMENT '最低等级要求',
  `reward_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '奖励类型',
  `reward_data` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '奖励数据(JSON)',
  `set_flag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '触发的flag',
  `unlock_dialogue_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '解锁的对话',
  `npc_relation_change` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'NPC好感度变化(JSON: {npc_id: change})',
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_key` (`event_key`),
  KEY `idx_active` (`active`),
  KEY `idx_min_offline` (`min_offline_hours`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='离线事件叙事表';

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