/*
 Navicat Premium Dump SQL

 Source Server         : 内网-rocky9-软件测试
 Source Server Type    : MySQL
 Source Server Version : 80408 (8.4.8)
 Source Host           : 192.168.215.110:3306
 Source Schema         : xiuxian_game

 Target Server Type    : MySQL
 Target Server Version : 80408 (8.4.8)
 File Encoding         : 65001

 Date: 27/03/2026 14:49:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for achievements
-- ----------------------------
DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '成就ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就描述',
  `achievement_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就类型：LEVEL/COMBAT/CULTIVATION/COLLECTION',
  `condition_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '条件类型：REACH_LEVEL/KILL_MONSTER/CULTIVATE_TIME',
  `condition_value` int NOT NULL COMMENT '条件数值',
  `reward_exp` int NULL DEFAULT 0 COMMENT '奖励经验',
  `reward_spirit_stones` int NULL DEFAULT 0 COMMENT '奖励灵石',
  `reward_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '奖励称号',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`achievement_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成就模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of achievements
-- ----------------------------
INSERT INTO `achievements` VALUES (1, '初入仙途', '达到2级', 'LEVEL', 'REACH_LEVEL', 2, 100, 50, NULL, NULL, 1);
INSERT INTO `achievements` VALUES (2, '修炼有成', '达到5级', 'LEVEL', 'REACH_LEVEL', 5, 300, 150, NULL, NULL, 2);
INSERT INTO `achievements` VALUES (3, '筑基成功', '达到11级', 'LEVEL', 'REACH_LEVEL', 11, 1000, 500, '筑基修士', NULL, 3);
INSERT INTO `achievements` VALUES (4, '金丹大成', '达到16级', 'LEVEL', 'REACH_LEVEL', 16, 3000, 1500, '金丹真人', NULL, 4);
INSERT INTO `achievements` VALUES (5, '元婴境界', '达到20级', 'LEVEL', 'REACH_LEVEL', 20, 10000, 5000, '元婴老祖', NULL, 5);
INSERT INTO `achievements` VALUES (6, '初战告捷', '击败1个怪物', 'COMBAT', 'KILL_MONSTER', 1, 50, 20, NULL, NULL, 10);
INSERT INTO `achievements` VALUES (7, '百战精兵', '击败100个怪物', 'COMBAT', 'KILL_MONSTER', 100, 1000, 500, '百战勇士', NULL, 11);
INSERT INTO `achievements` VALUES (8, '千战之王', '击败1000个怪物', 'COMBAT', 'KILL_MONSTER', 1000, 10000, 5000, '千战之王', NULL, 12);
INSERT INTO `achievements` VALUES (9, '勤修苦练', '累计修炼1小时', 'CULTIVATION', 'CULTIVATE_TIME', 3600, 200, 100, NULL, NULL, 20);
INSERT INTO `achievements` VALUES (10, '修炼狂人', '累计修炼10小时', 'CULTIVATION', 'CULTIVATE_TIME', 36000, 2000, 1000, '修炼狂人', NULL, 21);

-- ----------------------------
-- Table structure for activities
-- ----------------------------
DROP TABLE IF EXISTS `activities`;
CREATE TABLE `activities`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `activity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动描述',
  `activity_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动类型：DAILY/WEEKLY/SPECIAL',
  `start_time` timestamp NOT NULL COMMENT '开始时间',
  `end_time` timestamp NOT NULL COMMENT '结束时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/RUNNING/ENDED',
  `rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动规则（JSON格式）',
  `rewards` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '奖励配置（JSON格式）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_type`(`activity_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activities
-- ----------------------------

-- ----------------------------
-- Table structure for admin_operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `admin_operation_logs`;
CREATE TABLE `admin_operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
  `admin_id` int NOT NULL COMMENT '管理员ID',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标类型',
  `target_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标ID',
  `operation_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作描述',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  CONSTRAINT `fk_admin_operation_logs_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_operation_logs
-- ----------------------------

-- ----------------------------
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `announcement_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告类型：SYSTEM/MAINTENANCE/ACTIVITY/UPDATE/GUIDE',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级：0-普通 1-重要 2-紧急',
  `display_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示类型：POPUP/SCROLL/LIST/NOTICE',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/REVOKED/ACTIVE',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `created_by` int NOT NULL COMMENT '创建人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_announcement_type`(`announcement_type` ASC) USING BTREE,
  INDEX `idx_display_type`(`display_type` ASC) USING BTREE,
  INDEX `idx_end_time`(`end_time` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_status_priority`(`status` ASC, `priority` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcements
-- ----------------------------

-- ----------------------------
-- Table structure for auction_items
-- ----------------------------
DROP TABLE IF EXISTS `auction_items`;
CREATE TABLE `auction_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拍卖物品ID',
  `seller_id` int NOT NULL COMMENT '卖家ID',
  `item_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型：EQUIPMENT/ITEM/PET',
  `item_id` int NULL DEFAULT NULL COMMENT '物品模板ID',
  `player_item_id` bigint NULL DEFAULT NULL COMMENT '玩家物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `price` int NOT NULL COMMENT '价格（灵石）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ON_SALE' COMMENT '状态：ON_SALE/SOLD/CANCELLED/EXPIRED',
  `buyer_id` int NULL DEFAULT NULL COMMENT '买家ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `sold_at` timestamp NULL DEFAULT NULL COMMENT '售出时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_item_type`(`item_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_expire_at`(`expire_at` ASC) USING BTREE,
  INDEX `idx_auction_items_status_expire`(`status` ASC, `expire_at` ASC) USING BTREE,
  INDEX `idx_auction_items_item_price`(`item_id` ASC, `price` ASC) USING BTREE,
  INDEX `idx_auction_items_seller_time`(`seller_id` ASC, `created_at` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '拍卖物品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of auction_items
-- ----------------------------

-- ----------------------------
-- Table structure for bounty_quests
-- ----------------------------
DROP TABLE IF EXISTS `bounty_quests`;
CREATE TABLE `bounty_quests`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '悬赏ID',
  `monster_id` int NOT NULL COMMENT '怪物ID',
  `monster_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '怪物名称',
  `required_kills` int NOT NULL DEFAULT 1 COMMENT '需求数量',
  `star_level` int NOT NULL DEFAULT 1 COMMENT '星级(1-5)',
  `exp_reward` int NOT NULL DEFAULT 0 COMMENT '经验奖励',
  `spirit_stones_reward` int NOT NULL DEFAULT 0 COMMENT '灵石奖励',
  `item_reward_id` int NULL DEFAULT NULL COMMENT '物品奖励ID',
  `item_reward_quantity` int NOT NULL DEFAULT 1 COMMENT '物品奖励数量',
  `time_limit` int NULL DEFAULT NULL COMMENT '时间限制(分钟)',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `refresh_weight` int NOT NULL DEFAULT 100 COMMENT '刷新权重',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_monster_id`(`monster_id` ASC) USING BTREE,
  INDEX `idx_star_level`(`star_level` ASC) USING BTREE,
  INDEX `idx_required_level`(`required_level` ASC) USING BTREE,
  CONSTRAINT `fk_bounty_quests_monster` FOREIGN KEY (`monster_id`) REFERENCES `monsters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '悬赏任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bounty_quests
-- ----------------------------
INSERT INTO `bounty_quests` VALUES (1, 1, '野狼', 10, 1, 100, 50, 1, 5, 60, 1, 100, 1, '2026-03-27 14:43:17');
INSERT INTO `bounty_quests` VALUES (2, 2, '山贼', 8, 1, 120, 60, 2, 3, 60, 5, 90, 1, '2026-03-27 14:43:17');
INSERT INTO `bounty_quests` VALUES (3, 3, '妖怪', 5, 2, 200, 100, 5, 10, 90, 10, 70, 1, '2026-03-27 14:43:17');
INSERT INTO `bounty_quests` VALUES (4, 4, '邪修', 3, 2, 300, 150, 7, 5, 90, 15, 60, 1, '2026-03-27 14:43:17');
INSERT INTO `bounty_quests` VALUES (5, 5, '狂暴野狼', 3, 3, 500, 250, 3, 3, 120, 15, 40, 1, '2026-03-27 14:43:17');
INSERT INTO `bounty_quests` VALUES (6, 6, '山贼头目', 2, 3, 600, 300, 4, 2, 120, 20, 30, 1, '2026-03-27 14:43:17');
INSERT INTO `bounty_quests` VALUES (7, 7, '狼王', 1, 4, 1000, 500, 4, 5, 180, 25, 15, 1, '2026-03-27 14:43:17');
INSERT INTO `bounty_quests` VALUES (8, 8, '千年妖怪', 1, 5, 2000, 1000, 8, 2, 240, 35, 5, 1, '2026-03-27 14:43:17');

-- ----------------------------
-- Table structure for combat_achievements
-- ----------------------------
DROP TABLE IF EXISTS `combat_achievements`;
CREATE TABLE `combat_achievements`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '成就ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就描述',
  `achievement_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就类型：KILL/STREAK/DAMAGE/SURVIVE',
  `condition_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '条件类型',
  `condition_value` int NOT NULL COMMENT '条件数值',
  `reward_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖励类型',
  `reward_id` int NULL DEFAULT NULL COMMENT '奖励ID',
  `reward_quantity` int NOT NULL DEFAULT 1 COMMENT '奖励数量',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_achievement_type`(`achievement_type` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '战斗成就表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of combat_achievements
-- ----------------------------
INSERT INTO `combat_achievements` VALUES (1, '初战告捷', '获得第一次战斗胜利', 'KILL', 'total_wins', 1, 'SPIRIT_STONES', NULL, 100, NULL, 1, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_achievements` VALUES (2, '百战精兵', '获得100次战斗胜利', 'KILL', 'total_wins', 100, 'SPIRIT_STONES', NULL, 1000, NULL, 2, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_achievements` VALUES (3, '千战之王', '获得1000次战斗胜利', 'KILL', 'total_wins', 1000, 'SPIRIT_STONES', NULL, 10000, NULL, 3, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_achievements` VALUES (4, '连胜三场', '获得3场连胜', 'STREAK', 'highest_win_streak', 3, 'ITEM', 3, 5, NULL, 10, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_achievements` VALUES (5, '十连胜', '获得10场连胜', 'STREAK', 'highest_win_streak', 10, 'SPIRIT_STONES', NULL, 500, NULL, 11, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_achievements` VALUES (6, '百连胜', '获得100场连胜', 'STREAK', 'highest_win_streak', 100, 'EQUIPMENT', 18, 1, NULL, 12, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_achievements` VALUES (7, '万点伤害', '单场战斗造成10000点伤害', 'DAMAGE', 'single_battle_damage', 10000, 'SPIRIT_STONES', NULL, 200, NULL, 20, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_achievements` VALUES (8, '百万伤害', '累计造成1000000点伤害', 'DAMAGE', 'total_damage_dealt', 1000000, 'ITEM', 4, 1, NULL, 21, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_achievements` VALUES (9, 'BOSS猎人', '击败1个BOSS', 'KILL', 'boss_kills', 1, 'SPIRIT_STONES', NULL, 300, NULL, 30, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_achievements` VALUES (10, 'BOSS终结者', '击败10个BOSS', 'KILL', 'boss_kills', 10, 'EQUIPMENT', 24, 1, NULL, 31, 1, '2026-03-27 14:43:20');

-- ----------------------------
-- Table structure for combat_buffs
-- ----------------------------
DROP TABLE IF EXISTS `combat_buffs`;
CREATE TABLE `combat_buffs`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'BUFF ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BUFF名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'BUFF描述',
  `buff_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BUFF类型：ATTACK/DEFENSE/HEALTH/SPEED/CRIT',
  `buff_value` decimal(10, 2) NOT NULL COMMENT 'BUFF数值(百分比或固定值)',
  `is_percentage` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否为百分比',
  `duration_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '持续类型：PERMANENT/TEMPORARY/BATTLE',
  `duration_value` int NOT NULL DEFAULT 0 COMMENT '持续时间(秒/回合)',
  `stackable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可叠加',
  `max_stacks` int NOT NULL DEFAULT 1 COMMENT '最大叠加层数',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_buff_type`(`buff_type` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '战斗BUFF表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of combat_buffs
-- ----------------------------
INSERT INTO `combat_buffs` VALUES (1, '攻击提升', '攻击力提升20%', 'ATTACK', 20.00, 1, 'TEMPORARY', 300, 0, 1, NULL, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_buffs` VALUES (2, '防御强化', '防御力提升30%', 'DEFENSE', 30.00, 1, 'TEMPORARY', 300, 0, 1, NULL, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_buffs` VALUES (3, '生命恢复', '每回合恢复5%生命', 'HEALTH', 5.00, 1, 'BATTLE', 5, 0, 1, NULL, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_buffs` VALUES (4, '疾风步', '速度提升50%', 'SPEED', 50.00, 1, 'TEMPORARY', 180, 0, 1, NULL, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_buffs` VALUES (5, '暴击强化', '暴击率提升15%', 'CRIT', 15.00, 1, 'TEMPORARY', 300, 0, 1, NULL, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_buffs` VALUES (6, '狂暴之力', '攻击力提升50%，防御力降低20%', 'ATTACK', 50.00, 1, 'BATTLE', 3, 0, 1, NULL, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_buffs` VALUES (7, '铁壁', '防御力提升100%', 'DEFENSE', 100.00, 1, 'BATTLE', 2, 0, 1, NULL, 1, '2026-03-27 14:43:20');
INSERT INTO `combat_buffs` VALUES (8, '嗜血', '攻击时恢复造成伤害10%的生命', 'HEALTH', 10.00, 1, 'BATTLE', 3, 0, 1, NULL, 1, '2026-03-27 14:43:20');

-- ----------------------------
-- Table structure for combat_logs
-- ----------------------------
DROP TABLE IF EXISTS `combat_logs`;
CREATE TABLE `combat_logs`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '战斗日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `monster_id` int NULL DEFAULT NULL COMMENT '怪物ID',
  `result` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '战斗结果',
  `rounds` int NOT NULL DEFAULT 0 COMMENT '回合数',
  `exp_gained` int NOT NULL DEFAULT 0 COMMENT '获得经验',
  `spirit_stones_gained` int NOT NULL DEFAULT 0 COMMENT '获得灵石',
  `equipment_dropped` int NULL DEFAULT NULL COMMENT '掉落装备ID',
  `battle_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '战斗详情',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_combat_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_combat_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_combat_logs_player_time`(`player_id` ASC, `created_at` DESC) USING BTREE,
  INDEX `idx_combat_logs_monster_time`(`monster_id` ASC, `created_at` DESC) USING BTREE,
  CONSTRAINT `fk_combat_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '战斗日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of combat_logs
-- ----------------------------

-- ----------------------------
-- Table structure for combat_stats
-- ----------------------------
DROP TABLE IF EXISTS `combat_stats`;
CREATE TABLE `combat_stats`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_battles` int NOT NULL DEFAULT 0 COMMENT '总战斗次数',
  `total_wins` int NOT NULL DEFAULT 0 COMMENT '总胜利次数',
  `total_losses` int NOT NULL DEFAULT 0 COMMENT '总失败次数',
  `total_damage_dealt` bigint NOT NULL DEFAULT 0 COMMENT '总伤害输出',
  `total_damage_taken` bigint NOT NULL DEFAULT 0 COMMENT '总伤害承受',
  `total_exp_gained` bigint NOT NULL DEFAULT 0 COMMENT '总经验获得',
  `total_spirit_stones_gained` bigint NOT NULL DEFAULT 0 COMMENT '总灵石获得',
  `highest_win_streak` int NOT NULL DEFAULT 0 COMMENT '最高连胜',
  `current_win_streak` int NOT NULL DEFAULT 0 COMMENT '当前连胜',
  `boss_kills` int NOT NULL DEFAULT 0 COMMENT 'BOSS击杀数',
  `elite_kills` int NOT NULL DEFAULT 0 COMMENT '精英击杀数',
  `pvp_wins` int NOT NULL DEFAULT 0 COMMENT 'PVP胜利次数',
  `pvp_losses` int NOT NULL DEFAULT 0 COMMENT 'PVP失败次数',
  `last_battle_at` timestamp NULL DEFAULT NULL COMMENT '最后战斗时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_total_wins`(`total_wins` ASC) USING BTREE,
  INDEX `idx_win_streak`(`highest_win_streak` ASC) USING BTREE,
  CONSTRAINT `fk_combat_stats_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家战斗统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of combat_stats
-- ----------------------------

-- ----------------------------
-- Table structure for craft_logs
-- ----------------------------
DROP TABLE IF EXISTS `craft_logs`;
CREATE TABLE `craft_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '制作日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `recipe_id` int NOT NULL COMMENT '配方ID',
  `result_item_id` int NOT NULL COMMENT '产出物品ID',
  `result_quantity` int NOT NULL DEFAULT 1 COMMENT '产出数量',
  `is_success` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否成功',
  `mastery_exp_gained` int NOT NULL DEFAULT 0 COMMENT '获得熟练度',
  `materials_consumed` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消耗材料(JSON)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_recipe_id`(`recipe_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_craft_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_craft_logs_recipe` FOREIGN KEY (`recipe_id`) REFERENCES `item_recipes` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '制作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of craft_logs
-- ----------------------------

-- ----------------------------
-- Table structure for cultivation_levels
-- ----------------------------
DROP TABLE IF EXISTS `cultivation_levels`;
CREATE TABLE `cultivation_levels`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '境界ID',
  `level` int NOT NULL COMMENT '等级',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '境界名称',
  `min_exp` bigint NOT NULL COMMENT '最小经验',
  `max_exp` bigint NOT NULL COMMENT '最大经验',
  `health_bonus` int NOT NULL COMMENT '生命值加成',
  `mana_bonus` int NOT NULL COMMENT '法力值加成',
  `attack_bonus` int NOT NULL COMMENT '攻击力加成',
  `defense_bonus` int NOT NULL COMMENT '防御力加成',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_level`(`level` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '修炼境界表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cultivation_levels
-- ----------------------------
INSERT INTO `cultivation_levels` VALUES (1, 1, '练气期一层', 0, 300, 100, 50, 0, 0);
INSERT INTO `cultivation_levels` VALUES (2, 2, '练气期二层', 300, 700, 120, 60, 2, 1);
INSERT INTO `cultivation_levels` VALUES (3, 3, '练气期三层', 700, 1220, 140, 70, 4, 2);
INSERT INTO `cultivation_levels` VALUES (4, 4, '练气期四层', 1220, 1900, 160, 80, 6, 3);
INSERT INTO `cultivation_levels` VALUES (5, 5, '练气期五层', 1900, 2780, 180, 90, 8, 4);
INSERT INTO `cultivation_levels` VALUES (6, 6, '练气期六层', 2780, 3930, 200, 100, 10, 5);
INSERT INTO `cultivation_levels` VALUES (7, 7, '练气期七层', 3930, 5430, 220, 110, 12, 6);
INSERT INTO `cultivation_levels` VALUES (8, 8, '练气期八层', 5430, 7380, 240, 120, 14, 7);
INSERT INTO `cultivation_levels` VALUES (9, 9, '练气期九层', 7380, 9910, 260, 130, 16, 8);
INSERT INTO `cultivation_levels` VALUES (10, 10, '练气期十层', 9910, 13210, 280, 140, 18, 9);
INSERT INTO `cultivation_levels` VALUES (11, 11, '筑基期一层', 13210, 18210, 350, 200, 25, 15);
INSERT INTO `cultivation_levels` VALUES (12, 12, '筑基期二层', 18210, 24710, 400, 230, 30, 18);
INSERT INTO `cultivation_levels` VALUES (13, 13, '筑基期三层', 24710, 33210, 450, 260, 35, 21);
INSERT INTO `cultivation_levels` VALUES (14, 14, '筑基期四层', 33210, 44210, 500, 290, 40, 24);
INSERT INTO `cultivation_levels` VALUES (15, 15, '筑基期五层', 44210, 58710, 550, 320, 45, 27);
INSERT INTO `cultivation_levels` VALUES (16, 16, '金丹期一层', 58710, 80710, 700, 400, 60, 35);
INSERT INTO `cultivation_levels` VALUES (17, 17, '金丹期二层', 80710, 109710, 800, 450, 70, 40);
INSERT INTO `cultivation_levels` VALUES (18, 18, '金丹期三层', 109710, 147710, 900, 500, 80, 45);
INSERT INTO `cultivation_levels` VALUES (19, 19, '金丹期四层', 147710, 197710, 1000, 550, 90, 50);
INSERT INTO `cultivation_levels` VALUES (20, 20, '元婴期一层', 197710, 272710, 1300, 700, 120, 70);

-- ----------------------------
-- Table structure for cultivation_logs
-- ----------------------------
DROP TABLE IF EXISTS `cultivation_logs`;
CREATE TABLE `cultivation_logs`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `cultivation_time` int NOT NULL COMMENT '修炼时长(秒)',
  `cultivation_duration` bigint NOT NULL COMMENT '修炼持续时间(毫秒)',
  `exp_gained` int NOT NULL COMMENT '获得经验',
  `cultivation_points_gained` int NOT NULL COMMENT '获得修炼点',
  `spirit_stones_gained` int NOT NULL COMMENT '获得灵石',
  `is_offline` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否离线修炼',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_cultivation_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '修炼日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cultivation_logs
-- ----------------------------

-- ----------------------------
-- Table structure for daily_statistics
-- ----------------------------
DROP TABLE IF EXISTS `daily_statistics`;
CREATE TABLE `daily_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `new_players` int NOT NULL DEFAULT 0 COMMENT '新增玩家数',
  `active_players` int NOT NULL DEFAULT 0 COMMENT '活跃玩家数',
  `total_recharge` int NOT NULL DEFAULT 0 COMMENT '总充值金额（分）',
  `paying_players` int NOT NULL DEFAULT 0 COMMENT '付费玩家数',
  `arpu` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'ARPU',
  `arppu` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'ARPPU',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '每日统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for daily_stats
-- ----------------------------
DROP TABLE IF EXISTS `daily_stats`;
CREATE TABLE `daily_stats`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `dau` int NOT NULL DEFAULT 0 COMMENT '日活跃用户数',
  `new_users` int NOT NULL DEFAULT 0 COMMENT '新增用户数',
  `recharge_amount` decimal(15, 2) NOT NULL DEFAULT 0.00 COMMENT '充值金额',
  `recharge_count` int NOT NULL DEFAULT 0 COMMENT '充值次数',
  `arpu` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'ARPU',
  `arppu` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'ARPPU',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '每日统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_stats
-- ----------------------------

-- ----------------------------
-- Table structure for dialogue_nodes
-- ----------------------------
DROP TABLE IF EXISTS `dialogue_nodes`;
CREATE TABLE `dialogue_nodes`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `dialogue_tree_id` int NOT NULL COMMENT '对话树ID',
  `node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点唯一标识',
  `node_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'dialogue' COMMENT '节点类型：dialogue(对话)/choice(选择)/action(动作)/end(结束)',
  `speaker` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '说话者(NPC名/玩家/旁白)',
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话文本',
  `portrait` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像/表情状态',
  `next_node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '下一节点key',
  `parent_node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '父节点key(NULL=根节点)',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '同级排序(用于choice选项顺序)',
  `set_flags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发后设置的flag(JSON数组)',
  `clear_flags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发后清除的flag(JSON数组)',
  `set_reputation` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '好感度变化(JSON: {npc_id: change})',
  `conditions` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '显示条件(JSON: {min_relation, flags, items})',
  `on_complete_quest_id` int NULL DEFAULT NULL COMMENT '完成后触发的任务ID',
  `on_complete_flag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '完成后设置的全局flag',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tree_node`(`dialogue_tree_id` ASC, `node_key` ASC) USING BTREE,
  INDEX `idx_parent`(`dialogue_tree_id` ASC, `parent_node_key` ASC) USING BTREE,
  INDEX `idx_next`(`dialogue_tree_id` ASC, `next_node_key` ASC) USING BTREE,
  CONSTRAINT `fk_dialogue_nodes_tree` FOREIGN KEY (`dialogue_tree_id`) REFERENCES `dialogue_trees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 67 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '对话节点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dialogue_nodes
-- ----------------------------
INSERT INTO `dialogue_nodes` VALUES (1, 1, 'start', 'dialogue', '苏玄清', '你就是激活这枚玉简的人？', 'su_curious', 'player_choice_1', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (2, 1, 'player_choice_1', 'choice', '玩家', '如何回应？', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (3, 1, 'choice_righteous', 'choice', '玩家', '这是你的东西？我还给你。', NULL, 'su_response_righteous', 'player_choice_1', 1, NULL, NULL, '{\"1\": 5}', NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (4, 1, 'su_response_righteous', 'dialogue', '苏玄清', '（笑着摇头）它不是我的了。或者说……它从来就不是我的。', 'su_warm', 'su_response_righteous_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (5, 1, 'su_response_righteous_2', 'dialogue', '苏玄清', '它等了很久。等一个合适的人。', 'su_serious', 'merge_path', NULL, 0, '[\"first_impression_righteous\"]', NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (6, 1, 'choice_curious', 'choice', '玩家', '这玉简是什么？你怎么知道是我？', NULL, 'su_response_curious', 'player_choice_1', 2, NULL, NULL, '{\"1\": 3}', NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (7, 1, 'su_response_curious', 'dialogue', '苏玄清', '（举步走近，目光锐利）好问题。', 'su_sharp', 'su_response_curious_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (8, 1, 'su_response_curious_2', 'dialogue', '苏玄清', '因为这枚玉简在过去三百年里，从未对任何人产生过反应。', 'su_serious', 'merge_path', NULL, 0, '[\"first_impression_curious\"]', NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (9, 1, 'choice_cautious', 'choice', '玩家', '……（保持沉默，握紧玉简。）', NULL, 'su_response_cautious', 'player_choice_1', 3, NULL, NULL, '{\"1\": 4}', NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (10, 1, 'su_response_cautious', 'dialogue', '苏玄清', '（停步，微微一笑）不说话？好。不说话的人往往更值得信任。', 'su_warm', 'su_response_cautious_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (11, 1, 'su_response_cautious_2', 'dialogue', '苏玄清', '我叫苏玄清。你可以叫我……师父。如果你愿意的话。', 'su_warm', 'merge_path', NULL, 0, '[\"first_impression_cautious\"]', NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (12, 1, 'merge_path', 'dialogue', '苏玄清', '我住在镇子东边的山脚下。如果你想学修炼……明天日出前到那里找我。', 'su_calm', 'merge_path_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (13, 1, 'merge_path_2', 'dialogue', '苏玄清', '（转身走了两步，又停下）对了——别吃那玉简。上一个碰到它的人……嗯。不提了。', 'su_back', NULL, NULL, 0, '[\"met_su_xuan_qing\"]', NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (14, 2, 'start', 'dialogue', '剑无痕', '所以这就是新来的？', 'jian_scan', 'jian_scan_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (15, 2, 'jian_scan_2', 'dialogue', '剑无痕', '（上下打量）练气二层。嗯。', 'jian_unimpressed', 'jian_scan_3', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (16, 2, 'jian_scan_3', 'dialogue', '剑无痕', '师父，您确定没看走眼？', 'jian_mock', 'player_choice', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (17, 2, 'player_choice', 'choice', '玩家', '如何回应剑无痕？', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (18, 2, 'choice_polite', 'choice', '玩家', '师兄好，请多指教。', NULL, 'jian_polite_response', 'player_choice', 1, NULL, NULL, '{\"2\": 3}', NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (19, 2, 'jian_polite_response', 'dialogue', '剑无痕', '（微微皱眉）客气话就不必了。看实力。三天后的宗门试炼，别给我丢人。', 'jian_cold', 'end', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (20, 2, 'choice_confident', 'choice', '玩家', '实力不够，可以练。态度不够，练也白练。', NULL, 'jian_confident_response', 'player_choice', 2, NULL, NULL, '{\"2\": 5}', NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (21, 2, 'jian_confident_response', 'dialogue', '剑无痕', '（挑眉）呵。有意思。那我们看看，是你嘴硬还是剑硬。三天后见。', 'jian_amused', 'end', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (22, 2, 'choice_naive', 'choice', '玩家', '……你是谁？', NULL, 'jian_naive_response', 'player_choice', 3, NULL, NULL, '{\"2\": 2}', NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (23, 2, 'jian_naive_response', 'dialogue', '剑无痕', '（愣了一下，然后笑了一声）行吧，看来师父什么都没跟你说。', 'jian_laugh', 'jian_naive_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (24, 2, 'jian_naive_2', 'dialogue', '剑无痕', '我是剑无痕。天剑宗内门首席弟子。你可以理解为……你未来的天花板。', 'jian_arrogant', 'end', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (25, 3, 'start', 'dialogue', '白鹿真人', '（蹲在灵猫旁边）嘿，小家伙，你爪子好了没？', 'bai_lu_warm', 'bai_lu_noticed', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (26, 3, 'bai_lu_noticed', 'dialogue', '白鹿真人', '哦？你就是苏老头的新弟子？（站起身，拍拍膝盖上的土）', 'bai_lu_curious', 'bai_lu_observe', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (27, 3, 'bai_lu_observe', 'dialogue', '白鹿真人', '这只小猫跟着你呢。它不跟一般人。', 'bai_lu_insight', 'player_choice', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (28, 3, 'player_choice', 'choice', '玩家', '如何回应？', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (29, 3, 'choice_humble', 'choice', '玩家', '它受伤了，我只是帮了它一下。', NULL, 'bai_lu_humble_response', 'player_choice', 1, NULL, NULL, '{\"5\": 4}', NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (30, 3, 'bai_lu_humble_response', 'dialogue', '白鹿真人', '帮了一下？（笑着摇头）年轻人，你对\"帮\"的理解太轻了。', 'bai_lu_smile', 'bai_lu_humble_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (31, 3, 'bai_lu_humble_2', 'dialogue', '白鹿真人', '它不只是留下了——它选择了你。灵兽的直觉比人准得多。好好待它。', 'bai_lu_serious', 'end', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (32, 3, 'choice_utility', 'choice', '玩家', '我想把它训练成战斗灵兽。', NULL, 'bai_lu_utility_response', 'player_choice', 2, NULL, NULL, '{\"5\": 1}', NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (33, 3, 'bai_lu_utility_response', 'dialogue', '白鹿真人', '（表情变了一下，然后恢复）战斗灵兽？嗯……', 'bai_lu_caution', 'bai_lu_utility_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (34, 3, 'bai_lu_utility_2', 'dialogue', '白鹿真人', '灵兽不只是武器。你记住这一点，以后会感谢我的。', 'bai_lu_warning', 'end', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (35, 3, 'choice_casual', 'choice', '玩家', '它跟来就来吧，我不介意多个伴。', NULL, 'bai_lu_casual_response', 'player_choice', 3, NULL, NULL, '{\"5\": 5}', NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (36, 3, 'bai_lu_casual_response', 'dialogue', '白鹿真人', '（咧嘴笑）好！我就喜欢这种态度。', 'bai_lu_happy', 'bai_lu_casual_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (37, 3, 'bai_lu_casual_2', 'dialogue', '白鹿真人', '你看，它尾巴翘起来了——它也喜欢你。', 'bai_lu_warm', 'end', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:43:31');
INSERT INTO `dialogue_nodes` VALUES (38, 7, 'start', 'dialogue', '林婉儿', '（抱着一摞书从走廊拐角出来，差点撞上你）啊！——对不起对不起！', 'lin_surprised', 'lin_meet_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (39, 7, 'lin_meet_2', 'dialogue', '林婉儿', '（扶稳书本，好奇地打量你）你是……苏长老的弟子？练气期就突破到筑基了？', 'lin_curious', 'lin_meet_3', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (40, 7, 'lin_meet_3', 'dialogue', '林婉儿', '了不起！我听说苏长老已经三百年没收弟子了。你是第一个。', 'lin_warm', 'lin_invite', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (41, 7, 'lin_invite', 'dialogue', '林婉儿', '对了，我叫林婉儿，是万法阁的。三天后万法阁有一个法术交流会——你要不要来？', 'lin_inviting', 'player_choice', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (42, 7, 'player_choice', 'choice', '玩家', '如何回应？', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (43, 7, 'choice_go', 'choice', '玩家', '法术交流会？听起来很有趣，我去。', NULL, 'lin_go_response', 'player_choice', 1, '[\"agreed_wanfa_exchange\"]', NULL, '{\"3\": 5}', NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (44, 7, 'lin_go_response', 'dialogue', '林婉儿', '太好了！（眼睛亮了起来）那我帮你登记。万法阁在天剑宗东边，有个很大的藏书楼——你不会找不到的！', 'lin_happy', 'lin_go_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (45, 7, 'lin_go_2', 'dialogue', '林婉儿', '（低头翻书，自言自语）对了……那本《万法总纲》放哪了……（抬头）啊你还在？没事没事，三天后见！', 'lin_absent', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (46, 7, 'choice_maybe', 'choice', '玩家', '我考虑一下，三天后给你答复。', NULL, 'lin_maybe_response', 'player_choice', 2, NULL, NULL, '{\"3\": 2}', NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (47, 7, 'lin_maybe_response', 'dialogue', '林婉儿', '当然可以！不着急。（微笑）万法阁的门永远为好学之人敞开。', 'lin_understanding', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (48, 8, 'start', 'dialogue', '苏玄清', '（煮茶中）来了？坐。茶刚泡好。', 'su_warm', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (49, 9, 'start', 'dialogue', '老陈', '嘿，年轻人！要药材不？自己种的。灵气保证足！（竖起大拇指）', 'chen_grin', 'chen_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (50, 9, 'chen_2', 'dialogue', '老陈', '什么？你说市场价更便宜？那你去市场买啊！（嗑瓜子）……开玩笑的，我给你打八折。', 'chen_joking', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:45:37');
INSERT INTO `dialogue_nodes` VALUES (51, 4, 'start', 'dialogue', '苏玄清', '准备好了？盘膝坐下。闭上眼。', 'su_calm', 'cultivate_step_1', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (52, 4, 'cultivate_step_1', 'dialogue', '苏玄清', '感受灵气。像溪水流过手指一样——不要用力去抓，让它自然流过。', 'su_guiding', 'cultivate_step_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (53, 4, 'cultivate_step_2', 'dialogue', '旁白', '一股微凉的气流从四面八方汇聚，轻轻拂过你的皮肤……', NULL, 'cultivate_step_3', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (54, 4, 'cultivate_step_3', 'dialogue', '苏玄清', '（煮茶中，头也不回）感觉到了？那就是灵气。从今天起，它会成为你的一部分。', 'su_calm', 'cultivate_end', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (55, 4, 'cultivate_end', 'dialogue', '苏玄清', '修行路漫漫。慢慢来，别急。为师在这等你。', 'su_warm', NULL, NULL, 0, '[\"started_cultivation\"]', NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (56, 5, 'start', 'dialogue', '苏玄清', '（看了你一眼）气息比昨天稳了。快要圆满了？', 'su_observing', 'breakthrough_step_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (57, 5, 'breakthrough_step_2', 'dialogue', '苏玄清', '练气十层圆满之后，你需要面对一样东西——心魔。', 'su_serious', 'breakthrough_step_3', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (58, 5, 'breakthrough_step_3', 'dialogue', '苏玄清', '心魔不是怪物。它是你内心深处……你最不愿面对的那个自己。', 'su_deep', 'breakthrough_step_4', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (59, 5, 'breakthrough_step_4', 'dialogue', '苏玄清', '（沉默良久）每个人都要过这一关。为师当年也……算了，不提往事了。', 'su_memory', 'breakthrough_step_5', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (60, 5, 'breakthrough_step_5', 'dialogue', '苏玄清', '记住——心魔说的每一句话，都不是真的。但它们听起来会像是真的。', 'su_warning', 'breakthrough_end', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (61, 5, 'breakthrough_end', 'dialogue', '苏玄清', '准备好了就告诉我。不急。', 'su_warm', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (62, 6, 'start', 'dialogue', '苏玄清', '（你走进洞府时，苏玄清正对着一面空白的墙壁出神。）', 'su_distant', 'secret_hint_2', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (63, 6, 'secret_hint_2', 'dialogue', '苏玄清', '……回来了？修炼进展不错。（停顿）筑基……嗯。', 'su_vague', 'secret_hint_3', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (64, 6, 'secret_hint_3', 'dialogue', '苏玄清', '（转身煮茶，背对着你）你知道吗，这个世界上有些地方……不是不想去，是不能去。', 'su_heavy', 'secret_hint_4', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (65, 6, 'secret_hint_4', 'dialogue', '苏玄清', '天剑宗的剑冢，万法阁的藏经阁最上层，妖兽林深处的黑雾谷……', 'su_listing', 'secret_hint_5', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');
INSERT INTO `dialogue_nodes` VALUES (66, 6, 'secret_hint_5', 'dialogue', '苏玄清', '（递给你一杯茶）别去。至少……不是现在。', 'su_warning', NULL, NULL, 0, '[\"su_mentioned_forbidden_places\"]', NULL, NULL, NULL, NULL, NULL, '2026-03-27 14:46:14');

-- ----------------------------
-- Table structure for dialogue_trees
-- ----------------------------
DROP TABLE IF EXISTS `dialogue_trees`;
CREATE TABLE `dialogue_trees`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '对话树ID',
  `npc_id` int NOT NULL COMMENT 'NPC ID',
  `dialogue_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话唯一标识',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '对话标题',
  `scene` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '场景描述',
  `mood` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '对话基调',
  `min_level` int NULL DEFAULT 1 COMMENT '最低等级',
  `max_level` int NULL DEFAULT NULL COMMENT '最高等级(NULL=不限)',
  `required_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所需境界',
  `required_quest_chain_id` int NULL DEFAULT NULL COMMENT '前置任务链ID',
  `required_flags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所需flag(JSON数组)',
  `is_repeatable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可重复',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级(高=先触发)',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dialogue_key`(`dialogue_key` ASC) USING BTREE,
  INDEX `idx_npc_id`(`npc_id` ASC) USING BTREE,
  INDEX `idx_min_level`(`min_level` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE,
  CONSTRAINT `fk_dialogue_trees_npc` FOREIGN KEY (`npc_id`) REFERENCES `npcs` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '对话树表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dialogue_trees
-- ----------------------------
INSERT INTO `dialogue_trees` VALUES (1, 1, 'su_xuan_qing_first_meeting', '玉简之缘', '青云镇口', 'mysterious_warm', 1, NULL, NULL, NULL, NULL, 0, 10, 1, '2026-03-27 14:43:31', '2026-03-27 14:43:31');
INSERT INTO `dialogue_trees` VALUES (2, 2, 'jian_wuhen_first_meeting', '师兄弟初遇', '苏玄清洞府前', 'cold_curious', 2, NULL, NULL, NULL, NULL, 0, 9, 1, '2026-03-27 14:43:31', '2026-03-27 14:43:31');
INSERT INTO `dialogue_trees` VALUES (3, 5, 'bai_lu_first_meeting', '灵兽之缘', '天剑宗后山', 'warm_insightful', 6, NULL, NULL, NULL, NULL, 0, 8, 1, '2026-03-27 14:43:31', '2026-03-27 14:43:31');
INSERT INTO `dialogue_trees` VALUES (4, 1, 'su_xuan_qing_cultivation_guide', '修炼入门', '苏玄清洞府', 'patient_guiding', 1, NULL, NULL, NULL, '[\"met_su_xuan_qing\"]', 0, 8, 1, '2026-03-27 14:43:31', '2026-03-27 14:43:31');
INSERT INTO `dialogue_trees` VALUES (5, 1, 'su_xuan_qing_breakthrough_prep', '突破前夜', '苏玄清洞府', 'serious_caring', 8, NULL, NULL, NULL, NULL, 0, 9, 1, '2026-03-27 14:43:31', '2026-03-27 14:43:31');
INSERT INTO `dialogue_trees` VALUES (6, 1, 'su_xuan_qing_secret_hint', '师尊的沉默', '苏玄清洞府', 'heavy_mysterious', 12, NULL, NULL, NULL, '[\"broke_through_once\"]', 0, 8, 1, '2026-03-27 14:43:31', '2026-03-27 14:43:31');
INSERT INTO `dialogue_trees` VALUES (7, 3, 'lin_wan_er_invitation', '万法阁之邀', '天剑宗走廊', 'warm_intriguing', 11, NULL, NULL, NULL, NULL, 0, 7, 1, '2026-03-27 14:43:31', '2026-03-27 14:43:31');
INSERT INTO `dialogue_trees` VALUES (8, 1, 'su_xuan_qing_daily', '师尊闲谈', '苏玄清洞府', 'warm_casual', 1, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-03-27 14:43:31', '2026-03-27 14:43:31');
INSERT INTO `dialogue_trees` VALUES (9, 6, 'old_chen_daily', '老陈闲聊', '青云镇集市', 'casual_humorous', 1, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-03-27 14:43:31', '2026-03-27 14:43:31');

-- ----------------------------
-- Table structure for dungeon_logs
-- ----------------------------
DROP TABLE IF EXISTS `dungeon_logs`;
CREATE TABLE `dungeon_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `dungeon_id` int NOT NULL COMMENT '副本ID',
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结果：WIN/LOSE/TIMEOUT',
  `rounds_used` int NOT NULL DEFAULT 0 COMMENT '使用回合数',
  `time_used` int NOT NULL DEFAULT 0 COMMENT '使用时间(秒)',
  `damage_dealt` bigint NOT NULL DEFAULT 0 COMMENT '造成伤害',
  `damage_taken` bigint NOT NULL DEFAULT 0 COMMENT '受到伤害',
  `exp_gained` int NOT NULL DEFAULT 0 COMMENT '获得经验',
  `spirit_stones_gained` int NOT NULL DEFAULT 0 COMMENT '获得灵石',
  `items_dropped` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '掉落物品(JSON)',
  `battle_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '战斗详情(JSON)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_dungeon_id`(`dungeon_id` ASC) USING BTREE,
  INDEX `idx_result`(`result` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_dungeon_logs_dungeon` FOREIGN KEY (`dungeon_id`) REFERENCES `dungeons` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_dungeon_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '副本日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dungeon_logs
-- ----------------------------

-- ----------------------------
-- Table structure for dungeon_monsters
-- ----------------------------
DROP TABLE IF EXISTS `dungeon_monsters`;
CREATE TABLE `dungeon_monsters`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '副本怪物ID',
  `dungeon_id` int NOT NULL COMMENT '副本ID',
  `monster_id` int NOT NULL COMMENT '怪物ID',
  `position` int NOT NULL DEFAULT 1 COMMENT '位置(第几波)',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `level_modifier` int NOT NULL DEFAULT 0 COMMENT '等级修正',
  `health_modifier` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '生命值修正',
  `attack_modifier` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '攻击力修正',
  `defense_modifier` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '防御力修正',
  `is_boss` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为BOSS',
  `drop_rate_modifier` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '掉落率修正',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dungeon_id`(`dungeon_id` ASC) USING BTREE,
  INDEX `idx_monster_id`(`monster_id` ASC) USING BTREE,
  INDEX `idx_position`(`position` ASC) USING BTREE,
  CONSTRAINT `fk_dungeon_monsters_dungeon` FOREIGN KEY (`dungeon_id`) REFERENCES `dungeons` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_dungeon_monsters_monster` FOREIGN KEY (`monster_id`) REFERENCES `monsters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '副本怪物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dungeon_monsters
-- ----------------------------
INSERT INTO `dungeon_monsters` VALUES (1, 1, 1, 1, 2, 0, 1.00, 1.00, 1.00, 0, 1.00);
INSERT INTO `dungeon_monsters` VALUES (2, 1, 1, 2, 3, 0, 1.00, 1.00, 1.00, 0, 1.00);
INSERT INTO `dungeon_monsters` VALUES (3, 1, 5, 3, 1, 0, 1.50, 1.20, 1.00, 1, 2.00);
INSERT INTO `dungeon_monsters` VALUES (4, 2, 2, 1, 2, 0, 1.00, 1.00, 1.00, 0, 1.00);
INSERT INTO `dungeon_monsters` VALUES (5, 2, 2, 2, 3, 0, 1.00, 1.00, 1.00, 0, 1.00);
INSERT INTO `dungeon_monsters` VALUES (6, 2, 6, 3, 1, 0, 1.50, 1.30, 1.10, 1, 2.50);
INSERT INTO `dungeon_monsters` VALUES (7, 3, 3, 1, 3, 0, 1.00, 1.00, 1.00, 0, 1.00);
INSERT INTO `dungeon_monsters` VALUES (8, 3, 3, 2, 4, 0, 1.10, 1.10, 1.00, 0, 1.20);
INSERT INTO `dungeon_monsters` VALUES (9, 3, 7, 3, 1, 0, 1.80, 1.50, 1.20, 1, 3.00);
INSERT INTO `dungeon_monsters` VALUES (10, 4, 4, 1, 3, 0, 1.00, 1.00, 1.00, 0, 1.00);
INSERT INTO `dungeon_monsters` VALUES (11, 4, 4, 2, 4, 0, 1.20, 1.20, 1.10, 0, 1.50);
INSERT INTO `dungeon_monsters` VALUES (12, 4, 8, 3, 1, 0, 2.00, 1.80, 1.50, 1, 4.00);
INSERT INTO `dungeon_monsters` VALUES (13, 5, 1, 1, 5, 5, 1.50, 1.30, 1.20, 0, 1.50);
INSERT INTO `dungeon_monsters` VALUES (14, 5, 5, 2, 3, 5, 1.80, 1.50, 1.30, 0, 2.00);
INSERT INTO `dungeon_monsters` VALUES (15, 5, 7, 3, 1, 5, 3.00, 2.00, 1.80, 1, 5.00);
INSERT INTO `dungeon_monsters` VALUES (16, 6, 3, 1, 6, 10, 2.00, 1.50, 1.30, 0, 2.00);
INSERT INTO `dungeon_monsters` VALUES (17, 6, 6, 2, 4, 10, 2.50, 1.80, 1.50, 0, 2.50);
INSERT INTO `dungeon_monsters` VALUES (18, 6, 8, 3, 1, 10, 4.00, 2.50, 2.00, 1, 6.00);

-- ----------------------------
-- Table structure for dungeons
-- ----------------------------
DROP TABLE IF EXISTS `dungeons`;
CREATE TABLE `dungeons`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '副本ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '副本名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '副本描述',
  `dungeon_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '副本类型：NORMAL/ELITE/BOSS/TEAM',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `required_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '需求境界',
  `stamina_cost` int NOT NULL DEFAULT 10 COMMENT '体力消耗',
  `max_rounds` int NOT NULL DEFAULT 50 COMMENT '最大回合数',
  `daily_limit` int NOT NULL DEFAULT 3 COMMENT '每日限制次数',
  `exp_reward` int NOT NULL DEFAULT 0 COMMENT '经验奖励',
  `spirit_stones_reward` int NOT NULL DEFAULT 0 COMMENT '灵石奖励',
  `drop_rate_bonus` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '掉落率加成',
  `unlock_condition` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '解锁条件(JSON)',
  `rewards` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '奖励配置(JSON)',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dungeon_type`(`dungeon_type` ASC) USING BTREE,
  INDEX `idx_required_level`(`required_level` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '副本表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dungeons
-- ----------------------------
INSERT INTO `dungeons` VALUES (1, '野狼谷', '野狼聚集的山谷，适合新手历练', 'NORMAL', 1, NULL, 10, 30, 5, 100, 50, 10.00, NULL, '{\"exp\": 100, \"spirit_stones\": 50, \"items\": [{\"id\": 1, \"rate\": 30}]}', 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `dungeons` VALUES (2, '山贼营地', '山贼的老巢，危机四伏', 'NORMAL', 5, NULL, 15, 40, 3, 200, 100, 15.00, NULL, '{\"exp\": 200, \"spirit_stones\": 100, \"items\": [{\"id\": 2, \"rate\": 25}]}', 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `dungeons` VALUES (3, '妖兽洞穴', '妖兽盘踞的洞穴，充满危险', 'ELITE', 10, '练气期五层', 20, 50, 2, 500, 200, 20.00, NULL, '{\"exp\": 500, \"spirit_stones\": 200, \"equipments\": [{\"id\": 12, \"rate\": 10}]}', 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `dungeons` VALUES (4, '邪修密地', '邪修修炼的秘密基地', 'ELITE', 15, '筑基期', 25, 60, 2, 800, 300, 25.00, NULL, '{\"exp\": 800, \"spirit_stones\": 300, \"equipments\": [{\"id\": 18, \"rate\": 15}]}', 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `dungeons` VALUES (5, '狼王巢穴', '狼王的领地，极度危险', 'BOSS', 20, '金丹期', 30, 100, 1, 1500, 500, 50.00, NULL, '{\"exp\": 1500, \"spirit_stones\": 500, \"equipments\": [{\"id\": 24, \"rate\": 30}]}', 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `dungeons` VALUES (6, '千年妖洞', '千年妖怪的巢穴', 'BOSS', 30, '元婴期', 40, 120, 1, 3000, 1000, 60.00, NULL, '{\"exp\": 3000, \"spirit_stones\": 1000, \"equipments\": [{\"id\": 29, \"rate\": 40}]}', 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');

-- ----------------------------
-- Table structure for equipments
-- ----------------------------
DROP TABLE IF EXISTS `equipments`;
CREATE TABLE `equipments`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '装备ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备描述',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备类型',
  `level` int NOT NULL COMMENT '装备等级',
  `quality` int NOT NULL COMMENT '品质',
  `attack_bonus` int NOT NULL DEFAULT 0 COMMENT '攻击加成',
  `defense_bonus` int NOT NULL DEFAULT 0 COMMENT '防御加成',
  `health_bonus` int NOT NULL DEFAULT 0 COMMENT '生命加成',
  `mana_bonus` int NOT NULL DEFAULT 0 COMMENT '法力加成',
  `speed_bonus` int NOT NULL DEFAULT 0 COMMENT '速度加成',
  `required_level` int NOT NULL COMMENT '需求等级',
  `price` int NOT NULL COMMENT '价格',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_equipment_type`(`type` ASC) USING BTREE,
  INDEX `idx_quality`(`quality` ASC) USING BTREE,
  INDEX `idx_required_level`(`required_level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '装备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of equipments
-- ----------------------------
INSERT INTO `equipments` VALUES (1, '木剑', '普通的木制法剑', 'weapon', 1, 1, 5, 0, 0, 0, 0, 1, 100, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (2, '布袍', '简单的修炼道袍', 'chest', 1, 1, 0, 5, 50, 0, 0, 1, 150, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (3, '草帽', '简单的草制帽子', 'helmet', 1, 1, 0, 2, 20, 0, 0, 1, 80, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (4, '布鞋', '轻便的布制鞋子', 'boots', 1, 1, 0, 1, 10, 0, 2, 1, 60, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (5, '木盾', '简单的木制盾牌', 'shield', 1, 1, 0, 8, 30, 0, 0, 1, 120, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (6, '铁剑', '坚固的铁制长剑', 'weapon', 5, 2, 15, 0, 0, 0, 0, 5, 500, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (7, '皮甲', '轻便的皮制护甲', 'chest', 5, 2, 0, 10, 100, 0, 0, 5, 600, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (8, '铁盔', '坚固的铁制头盔', 'helmet', 5, 2, 0, 8, 50, 0, 0, 5, 400, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (9, '皮靴', '结实的皮制靴子', 'boots', 5, 2, 0, 3, 30, 0, 5, 5, 350, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (10, '铁盾', '坚固的铁制盾牌', 'shield', 5, 2, 0, 15, 80, 0, 0, 5, 550, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (11, '玉符', '低级灵力护符', 'ring', 5, 2, 0, 0, 30, 20, 5, 5, 300, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (12, '法杖', '蕴含灵力的法杖', 'weapon', 10, 3, 25, 0, 0, 50, 0, 10, 1200, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (13, '道袍', 'xiuxian者常穿的道袍', 'chest', 10, 3, 0, 15, 200, 30, 5, 10, 1500, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (14, '道冠', 'xiuxian者佩戴的道冠', 'helmet', 10, 3, 0, 12, 80, 20, 3, 10, 800, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (15, '道靴', 'xiuxian者专用的靴子', 'boots', 10, 3, 0, 5, 60, 10, 10, 10, 700, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (16, '护心镜', '保护心脏的护镜', 'shield', 10, 3, 0, 20, 150, 10, 2, 10, 1000, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (17, '灵戒', '蕴含灵力的戒指', 'ring', 10, 3, 5, 5, 50, 50, 10, 10, 800, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (18, '银剑', '锋利的银制长剑', 'weapon', 15, 3, 35, 0, 0, 10, 2, 15, 2000, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (19, '银甲', '闪亮的银制铠甲', 'chest', 15, 3, 0, 25, 300, 20, 5, 15, 2500, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (20, '银盔', '精致的银制头盔', 'helmet', 15, 3, 0, 18, 120, 15, 5, 15, 1200, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (21, '银靴', '轻便的银制靴子', 'boots', 15, 3, 0, 8, 80, 10, 15, 15, 1000, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (22, '银盾', '坚固的银制盾牌', 'shield', 15, 3, 0, 30, 200, 5, 3, 15, 1800, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (23, '银戒', '高级灵力戒指', 'ring', 15, 3, 8, 8, 80, 80, 15, 15, 1500, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (24, '金剑', '珍贵的金制长剑', 'weapon', 20, 4, 50, 0, 0, 20, 5, 20, 3500, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (25, '金甲', '华丽的金制铠甲', 'chest', 20, 4, 0, 35, 400, 30, 8, 20, 4000, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (26, '金盔', '华丽的金制头盔', 'helmet', 20, 4, 0, 25, 150, 20, 8, 20, 2000, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (27, '金靴', '华丽的金制靴子', 'boots', 20, 4, 0, 12, 100, 15, 20, 20, 1800, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (28, '金盾', '华丽的金制盾牌', 'shield', 20, 4, 0, 40, 250, 10, 5, 20, 3000, '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `equipments` VALUES (29, '金戒', '顶级灵力戒指', 'ring', 20, 4, 12, 12, 100, 100, 20, 20, 2500, '2026-03-27 14:43:15', '2026-03-27 14:43:15');

-- ----------------------------
-- Table structure for game_maps
-- ----------------------------
DROP TABLE IF EXISTS `game_maps`;
CREATE TABLE `game_maps`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '地图ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地图名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '地图描述',
  `region` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属区域',
  `map_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地图类型: SAFE/NORMAL/DANGEROUS/DUNGEON/BOSS',
  `required_level` int NULL DEFAULT 1 COMMENT '需求等级',
  `required_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '需求境界',
  `unlock_condition` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '解锁条件描述',
  `prev_map_id` int NULL DEFAULT NULL COMMENT '前置地图ID',
  `base_spirit_stones` int NULL DEFAULT 10 COMMENT '基础灵石收益/小时',
  `exp_modifier` decimal(5, 2) NULL DEFAULT 1.00 COMMENT '经验倍率',
  `danger_level` int NULL DEFAULT 1 COMMENT '危险等级 1-5',
  `offline_risk` tinyint(1) NULL DEFAULT 0 COMMENT '离线是否有风险: 0-无 1-有',
  `theme_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主题颜色十六进制值',
  `ambience_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '环境氛围文本',
  `enter_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '进入场景文本',
  `victory_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '胜利场景文本',
  `position_x` int NULL DEFAULT 0 COMMENT '地图坐标X',
  `position_y` int NULL DEFAULT 0 COMMENT '地图坐标Y',
  `active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_map_type`(`map_type` ASC) USING BTREE,
  INDEX `idx_region`(`region` ASC) USING BTREE,
  INDEX `idx_required_level`(`required_level` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '游戏地图主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_maps
-- ----------------------------
INSERT INTO `game_maps` VALUES (1, '青云镇', '一个灵气稀薄但安宁的小镇，坐落在天剑宗山脚。这里是无数修仙者故事的起点。', '新手区域', 'SAFE', 1, NULL, '创建角色自动解锁', NULL, 8, 1.00, 1, 0, '#F5E6C8', '夕阳将青云镇的屋檐染成金色。镇口的古井旁，几个孩童正在追逐嬉戏。', '你站在青云镇的镇口，感受着这份凡尘的安宁。远处天剑宗的山峰若隐若现，仿佛守护着什么秘密。', '你完成了在青云镇的修炼，感到体内灵气更加充盈。是时候踏上真正的修仙之路了。', 0, 0, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (2, '后山', '青云镇后的荒山，灵气比镇上稍浓，是一些低阶妖兽的栖息地。', '新手区域', 'NORMAL', 1, NULL, '完成\"初次修炼\"任务', 1, 10, 1.05, 1, 0, '#8D6E63', '山风带来草木的清香，偶尔能听到远处野兽的嚎叫。这里的灵气比镇上浓郁一些。', '你踏入后山的小径，周围的树木逐渐茂密。阳光透过树叶洒下斑驳的光影，你能感觉到灵气在空气中流动。', '你清理了后山的妖兽，获得了一些修炼材料。这里的灵气已经被你吸收得差不多了。', 0, -1, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (3, '天剑宗外门', '天剑宗的外门区域，弟子们在此修炼基础功法。秩序井然，灵气充沛。', '新手区域', 'SAFE', 4, NULL, '达到练气3层', 2, 15, 1.10, 1, 0, '#E0E0E0', '整齐的石板路，肃穆的建筑，空气中弥漫着淡淡的檀香。这里是修仙正道的起点。', '你走进天剑宗外门，看到弟子们在广场上练剑。一位长老向你点头致意——这里欢迎每一位求道者。', '你在天剑宗外门的修炼告一段落。你的剑法更加纯熟，道心也更加坚定。', 1, -1, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (4, '试剑峰', '天剑宗的试炼之地，外门弟子需在此通过考核才能继续深造。', '新手区域', 'NORMAL', 4, NULL, '达到练气4层+剑无痕试炼', 3, 18, 1.15, 2, 0, '#B0BEC5', '山峰陡峭，剑痕遍布岩石。每一块石头都见证过无数弟子的汗水与努力。', '你攀登试剑峰，感受到越来越强的剑意压迫。这是天剑宗的考验——只有意志坚定者才能通过。', '你通过了试剑峰的考验！剑无痕在山顶等你，眼中闪过一丝认可。你证明了自己的实力。', 2, -1, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (5, '荒野外围', '天剑宗外的荒野地带，人迹罕至，妖兽横行。是练气后期弟子的试炼场。', '新手区域', 'NORMAL', 8, NULL, '达到练气7层', 4, 22, 1.20, 2, 0, '#D7CCC8', '荒凉的景色延伸到天际，偶尔能看到废弃的营地和散落的武器。这里曾经是战场。', '你踏入荒野，风声在耳边呼啸。这里没有宗门的庇护，只有强者才能生存。你握紧了手中的剑。', '你在荒野中杀出一条血路。这里的妖兽已经不能阻挡你的脚步，你感到自己即将突破。', 3, -1, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (6, '荒野深处', '荒野的更深处，灵气更加狂暴，妖兽也更加凶猛。', '进阶区域', 'DANGEROUS', 11, '筑基期', '达到筑基期', 5, 28, 1.30, 3, 1, '#A1887F', '地面开始出现裂痕，空气中弥漫着硫磺的气息。这里的灵气狂暴而不稳定。', '你深入荒野，周围的景色变得更加荒凉。远处传来震耳欲聋的兽吼，地面微微颤抖。', '你战胜了荒野深处的强大妖兽，获得了珍贵的材料。这里的狂暴灵气已经不能影响你的心境。', 4, -1, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (7, '妖兽林外围', '妖兽林的外围区域，阳光难以穿透茂密的树冠。普通妖兽在此栖息。', '进阶区域', 'DANGEROUS', 11, '筑基期', '达到筑基期', 5, 30, 1.35, 3, 1, '#33691E', '阳光被树冠切割成碎片，空气中弥漫着潮湿的腐叶气息。远处传来不知名野兽的低吼。', '你踏入妖兽林的外围，周围的树木高大而古老。你的灵猫竖起耳朵，尾巴微微颤抖——它感觉到了什么。', '你清理了妖兽林外围的妖兽，获得了妖兽材料。林中的雾气似乎淡了一些……但深处的咆哮声更近了。', 3, -2, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (8, '妖兽林深处', '妖兽林的深处，黑雾弥漫，强大的妖兽在此盘踞。只有金丹期以下的修士会来此历练。', '进阶区域', 'DANGEROUS', 14, '筑基期四层', '达到筑基4层', 7, 35, 1.45, 4, 1, '#1B5E20', '黑雾像轻纱一样缠绕在树干上，能见度很低。你听到四面八方传来的窸窣声，却无法确定方位。', '你深入妖兽林，黑雾越来越浓。这里的妖兽已经被黑雾侵蚀，变得异常凶猛。你必须小心行事。', '你战胜了妖兽林深处的妖狼王！黑雾在你周围退散，露出一片相对清澈的空地。你感到自己的实力又精进了一步。', 3, -3, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (9, '山贼营地', '荒野中的山贼营地，是一处小型副本。山贼首领据说有筑基期的实力。', '进阶区域', 'DUNGEON', 10, NULL, '荒野外围随机触发或任务解锁', 5, 0, 1.50, 3, 0, '#5D4037', '破旧的帐篷和木栅栏，空气中弥漫着酒气和血腥味。这里是法外之徒的聚集地。', '你发现了一个山贼营地！山贼们正在分赃，没有注意到你的靠近。这是一个突袭的好机会。', '你击败了山贼首领，营地中的山贼四散奔逃。你在首领的帐篷中找到了一些战利品。', 4, 0, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (10, '妖兽洞穴', '妖兽林中的洞穴，是精英妖兽的巢穴。洞穴深处据说有珍贵的妖兽内丹。', '进阶区域', 'DUNGEON', 13, '筑基期三层', '达到筑基3层', 7, 0, 1.60, 4, 0, '#263238', '洞穴入口散发着腐臭的气息，黑暗中传来滴水的声音和某种生物的呼吸声。', '你站在妖兽洞穴的入口，深吸一口气。洞穴深处传来低沉的咆哮，那是妖兽首领的警告。', '你击败了洞穴深处的妖兽首领，获得了珍贵的妖兽内丹！你的实力得到了显著提升。', 3, -4, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (11, '黑雾深谷', '妖兽林最深处的深谷，永远弥漫着黑雾。据说这里是封魔之战的遗迹。', '高阶区域', 'DANGEROUS', 16, '金丹期', '达到金丹期', 8, 45, 1.60, 5, 1, '#212121', '黑雾像活物一样缠绕着你的脚踝。你手中的剑开始发出微弱的嗡鸣，仿佛在警告你什么。', '你踏入黑雾深谷，能见度不足十步。这里的黑雾不仅是视觉的障碍，更在侵蚀你的灵力。你必须速战速决。', '你战胜了黑雾中的强大存在！黑雾在你周围退散，露出一片被腐蚀的土地。你感到一种莫名的悲哀——这里曾经是一片美丽的森林。', 3, -5, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (12, '邪修密地', '邪修修炼的秘密基地，是一处危险副本。邪修们在此进行禁忌的实验。', '高阶区域', 'DUNGEON', 17, '金丹期二层', '达到金丹2层', 11, 0, 1.80, 5, 0, '#311B92', '阴森的地下通道，墙壁上刻满了诡异的符文。空气中弥漫着血腥味和某种腐败的甜香。', '你潜入邪修密地，小心翼翼地避开巡逻的邪修。这里的邪恶气息让你的灵力运转都有些不畅。', '你摧毁了邪修密地的核心法阵，邪修们四散奔逃。你在密地深处发现了一些关于\"封魔之战\"的秘密文献。', 5, -3, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (13, '万法阁', '四大宗门之一的万法阁，是一座活着的建筑。这里收藏着苍玄界最丰富的典籍。', '高阶区域', 'SAFE', 16, NULL, '筑基期后可选择加入', 3, 50, 1.50, 1, 0, '#FFF8E1', '书架高耸入云，书籍自动排列组合。空气中弥漫着墨香和淡淡的灵气。知识在这里是活的。', '你走进万法阁，感到无数知识的重量。书架在你走近时自动排列，仿佛在欢迎你——或者，在考验你。', '你在万法阁的修炼让你对修仙之道有了更深的理解。这里的每一本书都可能改变你的命运。', 6, -2, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (14, '上古秘境', '传说中的上古秘境，只有元婴期修士才能进入。这里藏着苍玄界最大的秘密。', '终极区域', 'BOSS', 20, '元婴期', '达到元婴期+消耗秘境令', 11, 0, 2.00, 5, 0, '#FFD700', '古老的建筑残骸散落在四周，空气中弥漫着远古的气息。这里的时间仿佛静止了。', '你踏入上古秘境，感到一股强大的威压。这里的每一块石头都见证了苍玄界的历史——包括那个不该被提及的真相。', '你通过了上古秘境的考验！在这里，你找到了关于\"飞升\"的真相——以及另一条路的可能性。', 7, -4, 1, '2026-03-27 14:43:31');
INSERT INTO `game_maps` VALUES (15, '天堑之巅', '苍玄界的最高点，也是游戏的最终场景。在这里，你将做出最终的选择。', '终极区域', 'BOSS', 20, '元婴期', '完成主线剧情', 14, 0, 2.50, 5, 0, '#E1F5FE', '云海在脚下翻滚，天劫的雷霆在头顶轰鸣。这里是苍玄界的顶点，也是你修仙之路的终点——或者，起点。', '你站在天堑之巅，所有你认识的人都在这里。苏玄清、剑无痕、林婉儿、白鹿真人……他们都在等待你的选择。', '你做出了自己的选择。无论结果如何，你已经走完了这条修仙之路。苏玄清微笑着看着你：\"去吧。不管你选了什么路——别忘了回头看看。\"', 7, -6, 1, '2026-03-27 14:43:31');

-- ----------------------------
-- Table structure for gift_code_usage
-- ----------------------------
DROP TABLE IF EXISTS `gift_code_usage`;
CREATE TABLE `gift_code_usage`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '使用记录ID',
  `gift_code_id` bigint NOT NULL COMMENT '礼包码ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `used_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code_player`(`gift_code_id` ASC, `player_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_gift_code_usage_code` FOREIGN KEY (`gift_code_id`) REFERENCES `gift_codes` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_gift_code_usage_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '礼包码使用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gift_code_usage
-- ----------------------------

-- ----------------------------
-- Table structure for gift_codes
-- ----------------------------
DROP TABLE IF EXISTS `gift_codes`;
CREATE TABLE `gift_codes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '礼包码ID',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '礼包码',
  `code_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：SINGLE/MULTI',
  `max_uses` int NOT NULL DEFAULT 1 COMMENT '最大使用次数',
  `used_count` int NOT NULL DEFAULT 0 COMMENT '已使用次数',
  `rewards` json NOT NULL COMMENT '奖励配置',
  `min_level` int NOT NULL DEFAULT 1 COMMENT '最低等级要求',
  `expire_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
  `created_by` int NOT NULL COMMENT '创建者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_code_type`(`code_type` ASC) USING BTREE,
  INDEX `idx_is_active`(`is_active` ASC) USING BTREE,
  INDEX `fk_gift_codes_creator`(`created_by` ASC) USING BTREE,
  CONSTRAINT `fk_gift_codes_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '礼包码表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gift_codes
-- ----------------------------

-- ----------------------------
-- Table structure for guild_applications
-- ----------------------------
DROP TABLE IF EXISTS `guild_applications`;
CREATE TABLE `guild_applications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `guild_id` int NOT NULL COMMENT '宗门ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '申请留言',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
  `processed_by` int NULL DEFAULT NULL COMMENT '处理人ID',
  `processed_at` timestamp NULL DEFAULT NULL COMMENT '处理时间',
  `applied_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_guild_id`(`guild_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_guild_applications_guild` FOREIGN KEY (`guild_id`) REFERENCES `guilds` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_guild_applications_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宗门申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of guild_applications
-- ----------------------------

-- ----------------------------
-- Table structure for guild_boss_challenges
-- ----------------------------
DROP TABLE IF EXISTS `guild_boss_challenges`;
CREATE TABLE `guild_boss_challenges`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `boss_id` int NOT NULL,
  `player_id` int NOT NULL,
  `damage_dealt` bigint NOT NULL DEFAULT 0 COMMENT '累计造成伤害',
  `today_attempts` int NOT NULL DEFAULT 0 COMMENT '今日挑战次数',
  `last_challenge_at` datetime NULL DEFAULT NULL,
  `reward_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `personal_reward_stones` int NULL DEFAULT NULL COMMENT '按贡献分配的灵石',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_boss_player`(`boss_id` ASC, `player_id` ASC) USING BTREE,
  INDEX `idx_boss_damage`(`boss_id` ASC, `damage_dealt` DESC) USING BTREE,
  INDEX `idx_guild_boss_challenges_guild_time`(`boss_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_guild_boss_challenges_player_time`(`player_id` ASC, `created_at` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宗门BOSS挑战记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of guild_boss_challenges
-- ----------------------------

-- ----------------------------
-- Table structure for guild_bosses
-- ----------------------------
DROP TABLE IF EXISTS `guild_bosses`;
CREATE TABLE `guild_bosses`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'BOSS名称',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'BOSS描述',
  `level` int NOT NULL DEFAULT 5 COMMENT 'BOSS等级',
  `max_health` bigint NOT NULL DEFAULT 500000 COMMENT '最大生命值',
  `current_health` bigint NOT NULL DEFAULT 500000 COMMENT '当前生命值',
  `attack` int NOT NULL DEFAULT 1000,
  `defense` int NOT NULL DEFAULT 200,
  `guild_id` int NOT NULL COMMENT '所属宗门',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ALIVE' COMMENT 'ALIVE/DEFEATED',
  `reward_spirit_stones` int NOT NULL DEFAULT 5000 COMMENT '灵石奖励总量',
  `reward_exp` int NOT NULL DEFAULT 8000 COMMENT '经验奖励总量',
  `reward_item_id` int NULL DEFAULT NULL COMMENT '特殊道具奖励',
  `spawned_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `defeated_at` datetime NULL DEFAULT NULL,
  `next_spawn_at` datetime NULL DEFAULT NULL COMMENT '下次刷新时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_guild_status`(`guild_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宗门BOSS' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of guild_bosses
-- ----------------------------

-- ----------------------------
-- Table structure for guild_members
-- ----------------------------
DROP TABLE IF EXISTS `guild_members`;
CREATE TABLE `guild_members`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `guild_id` int NOT NULL COMMENT '宗门ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEMBER' COMMENT '职位：LEADER/OFFICER/MEMBER',
  `contribution` int NOT NULL DEFAULT 0 COMMENT '贡献值',
  `joined_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_guild`(`player_id` ASC, `guild_id` ASC) USING BTREE,
  INDEX `idx_guild_id`(`guild_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_guild_members_guild` FOREIGN KEY (`guild_id`) REFERENCES `guilds` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_guild_members_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宗门成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of guild_members
-- ----------------------------

-- ----------------------------
-- Table structure for guilds
-- ----------------------------
DROP TABLE IF EXISTS `guilds`;
CREATE TABLE `guilds`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '宗门ID',
  `guild_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '宗门名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '宗门简介',
  `leader_id` int NOT NULL COMMENT '宗主ID',
  `level` int NOT NULL DEFAULT 1 COMMENT '宗门等级',
  `exp` bigint NOT NULL DEFAULT 0 COMMENT '宗门经验',
  `exp_to_next` bigint NOT NULL DEFAULT 1000 COMMENT '升级所需经验',
  `guild_funds` bigint NOT NULL DEFAULT 0 COMMENT '宗门资金',
  `member_count` int NOT NULL DEFAULT 1 COMMENT '成员数量',
  `max_members` int NOT NULL DEFAULT 20 COMMENT '最大成员数',
  `announcement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '宗门公告',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_guild_name`(`guild_name` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  CONSTRAINT `fk_guilds_leader` FOREIGN KEY (`leader_id`) REFERENCES `player_profiles` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宗门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of guilds
-- ----------------------------

-- ----------------------------
-- Table structure for inventory_expansions
-- ----------------------------
DROP TABLE IF EXISTS `inventory_expansions`;
CREATE TABLE `inventory_expansions`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '扩展ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `expansion_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '扩展类型：BAG/VAULT/EQUIPMENT',
  `current_slots` int NOT NULL DEFAULT 50 COMMENT '当前格子数',
  `max_slots` int NOT NULL DEFAULT 200 COMMENT '最大格子数',
  `expansion_count` int NOT NULL DEFAULT 0 COMMENT '扩展次数',
  `last_expansion_at` timestamp NULL DEFAULT NULL COMMENT '最后扩展时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_type`(`player_id` ASC, `expansion_type` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_inventory_expansions_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '背包扩展表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_expansions
-- ----------------------------

-- ----------------------------
-- Table structure for inventory_presets
-- ----------------------------
DROP TABLE IF EXISTS `inventory_presets`;
CREATE TABLE `inventory_presets`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '预设ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `preset_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预设名称',
  `preset_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预设类型：BATTLE/CULTIVATION/FARMING',
  `items_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品配置(JSON)',
  `is_active` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否激活',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_preset_type`(`preset_type` ASC) USING BTREE,
  CONSTRAINT `fk_inventory_presets_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '背包预设表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_presets
-- ----------------------------

-- ----------------------------
-- Table structure for item_binds
-- ----------------------------
DROP TABLE IF EXISTS `item_binds`;
CREATE TABLE `item_binds`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `player_item_id` int NOT NULL COMMENT '玩家物品ID',
  `bind_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绑定类型：EQUIP/PICKUP/TRADE',
  `bound_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_item_bind`(`player_id` ASC, `player_item_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_bind_type`(`bind_type` ASC) USING BTREE,
  INDEX `fk_item_binds_player_item`(`player_item_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_binds_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_item_binds_player_item` FOREIGN KEY (`player_item_id`) REFERENCES `player_items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品绑定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_binds
-- ----------------------------

-- ----------------------------
-- Table structure for item_categories
-- ----------------------------
DROP TABLE IF EXISTS `item_categories`;
CREATE TABLE `item_categories`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '分类描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `parent_id` int NULL DEFAULT NULL COMMENT '父分类ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_categories
-- ----------------------------
INSERT INTO `item_categories` VALUES (1, '消耗品', 'consumable', '可使用的消耗类物品', NULL, NULL, 1, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (2, '材料', 'material', '用于合成和制作的材料', NULL, NULL, 2, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (3, '装备', 'equipment', '可穿戴的装备', NULL, NULL, 3, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (4, '丹药', 'pill', '修炼用丹药', NULL, 1, 1, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (5, '符箓', 'talisman', '一次性使用的符箓', NULL, 1, 2, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (6, '灵草', 'herb', '炼丹材料', NULL, 2, 1, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (7, '矿石', 'ore', '炼器材料', NULL, 2, 2, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (8, '妖兽材料', 'monster_material', '妖兽掉落的材料', NULL, 2, 3, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (9, '任务物品', 'quest_item', '任务相关物品', NULL, NULL, 4, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (10, '特殊物品', 'special', '特殊用途物品', NULL, NULL, 5, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (11, '宝箱', 'chest', '可开启的宝箱', NULL, 10, 1, 1, '2026-03-27 14:43:15');
INSERT INTO `item_categories` VALUES (12, '礼包', 'gift_pack', '包含多种物品的礼包', NULL, 10, 2, 1, '2026-03-27 14:43:15');

-- ----------------------------
-- Table structure for item_cooldowns
-- ----------------------------
DROP TABLE IF EXISTS `item_cooldowns`;
CREATE TABLE `item_cooldowns`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '冷却ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `cooldown_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '冷却组',
  `expire_at` timestamp NOT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_item`(`player_id` ASC, `item_id` ASC) USING BTREE,
  INDEX `idx_expire_at`(`expire_at` ASC) USING BTREE,
  INDEX `fk_item_cooldowns_item`(`item_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_cooldowns_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_item_cooldowns_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品冷却表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_cooldowns
-- ----------------------------

-- ----------------------------
-- Table structure for item_drop_rates
-- ----------------------------
DROP TABLE IF EXISTS `item_drop_rates`;
CREATE TABLE `item_drop_rates`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '掉落率ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源类型：MONSTER/DUNGEON/QUEST/ACTIVITY',
  `source_id` int NULL DEFAULT NULL COMMENT '来源ID',
  `drop_rate` decimal(5, 2) NOT NULL COMMENT '掉落概率',
  `min_quantity` int NOT NULL DEFAULT 1 COMMENT '最小数量',
  `max_quantity` int NOT NULL DEFAULT 1 COMMENT '最大数量',
  `daily_limit` int NULL DEFAULT NULL COMMENT '每日限制',
  `level_requirement` int NOT NULL DEFAULT 1 COMMENT '等级要求',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_source`(`source_type` ASC, `source_id` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE,
  CONSTRAINT `fk_item_drop_rates_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品掉落率表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_drop_rates
-- ----------------------------
INSERT INTO `item_drop_rates` VALUES (1, 1, 'MONSTER', 1, 30.00, 1, 3, NULL, 1, 1, '2026-03-27 14:43:16');
INSERT INTO `item_drop_rates` VALUES (2, 2, 'MONSTER', 1, 25.00, 1, 2, NULL, 1, 1, '2026-03-27 14:43:16');
INSERT INTO `item_drop_rates` VALUES (3, 5, 'MONSTER', 1, 50.00, 1, 5, NULL, 1, 1, '2026-03-27 14:43:16');
INSERT INTO `item_drop_rates` VALUES (4, 6, 'MONSTER', 1, 20.00, 1, 3, NULL, 5, 1, '2026-03-27 14:43:16');
INSERT INTO `item_drop_rates` VALUES (5, 7, 'MONSTER', NULL, 10.00, 1, 1, NULL, 10, 1, '2026-03-27 14:43:16');
INSERT INTO `item_drop_rates` VALUES (6, 8, 'MONSTER', NULL, 5.00, 1, 1, NULL, 15, 1, '2026-03-27 14:43:16');
INSERT INTO `item_drop_rates` VALUES (7, 1, 'DUNGEON', 1, 50.00, 2, 5, NULL, 1, 1, '2026-03-27 14:43:16');
INSERT INTO `item_drop_rates` VALUES (8, 3, 'DUNGEON', 3, 15.00, 1, 1, NULL, 10, 1, '2026-03-27 14:43:16');
INSERT INTO `item_drop_rates` VALUES (9, 4, 'DUNGEON', 5, 10.00, 1, 1, NULL, 20, 1, '2026-03-27 14:43:16');

-- ----------------------------
-- Table structure for item_exchange_logs
-- ----------------------------
DROP TABLE IF EXISTS `item_exchange_logs`;
CREATE TABLE `item_exchange_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '交换日志ID',
  `from_player_id` int NOT NULL COMMENT '发起方玩家ID',
  `to_player_id` int NOT NULL COMMENT '接收方玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `exchange_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '交换类型：GIFT/TRADE/MAIL',
  `price` int NULL DEFAULT NULL COMMENT '交易价格',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'COMPLETED' COMMENT '状态：PENDING/COMPLETED/CANCELLED',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_player_id`(`from_player_id` ASC) USING BTREE,
  INDEX `idx_to_player_id`(`to_player_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_item_exchange_logs_from` FOREIGN KEY (`from_player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_item_exchange_logs_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_item_exchange_logs_to` FOREIGN KEY (`to_player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品交换日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_exchange_logs
-- ----------------------------

-- ----------------------------
-- Table structure for item_qualities
-- ----------------------------
DROP TABLE IF EXISTS `item_qualities`;
CREATE TABLE `item_qualities`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '品质ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品质名称',
  `level` int NOT NULL COMMENT '品质等级',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '颜色代码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '品质描述',
  `drop_rate_modifier` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '掉落率修正',
  `price_modifier` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '价格修正',
  `sell_price_ratio` decimal(5, 2) NOT NULL DEFAULT 0.50 COMMENT '出售价格比例',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_level`(`level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品品质表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_qualities
-- ----------------------------
INSERT INTO `item_qualities` VALUES (1, '普通', 1, '#FFFFFF', '普通品质的物品', 1.00, 1.00, 0.50);
INSERT INTO `item_qualities` VALUES (2, '精良', 2, '#00FF00', '精良品质的物品', 0.70, 1.50, 0.50);
INSERT INTO `item_qualities` VALUES (3, '稀有', 3, '#0080FF', '稀有品质的物品', 0.40, 3.00, 0.60);
INSERT INTO `item_qualities` VALUES (4, '史诗', 4, '#8000FF', '史诗品质的物品', 0.15, 8.00, 0.70);
INSERT INTO `item_qualities` VALUES (5, '传说', 5, '#FF8000', '传说品质的物品', 0.05, 20.00, 0.80);
INSERT INTO `item_qualities` VALUES (6, '神话', 6, '#FF0000', '神话品质的物品', 0.01, 50.00, 0.90);

-- ----------------------------
-- Table structure for item_recipes
-- ----------------------------
DROP TABLE IF EXISTS `item_recipes`;
CREATE TABLE `item_recipes`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配方ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配方名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配方描述',
  `result_item_id` int NOT NULL COMMENT '产出物品ID',
  `result_quantity` int NOT NULL DEFAULT 1 COMMENT '产出数量',
  `craft_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '制作类型：ALCHEMY/FORGING/COOKING',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `required_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '需求境界',
  `craft_time` int NOT NULL DEFAULT 0 COMMENT '制作时间(秒)',
  `success_rate` decimal(5, 2) NOT NULL DEFAULT 100.00 COMMENT '成功率',
  `spirit_stones_cost` int NOT NULL DEFAULT 0 COMMENT '灵石消耗',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_result_item_id`(`result_item_id` ASC) USING BTREE,
  INDEX `idx_craft_type`(`craft_type` ASC) USING BTREE,
  INDEX `idx_required_level`(`required_level` ASC) USING BTREE,
  CONSTRAINT `fk_item_recipes_result` FOREIGN KEY (`result_item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品合成配方表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_recipes
-- ----------------------------
INSERT INTO `item_recipes` VALUES (1, '炼制疗伤丹', '使用灵草炼制疗伤丹', 1, 3, 'ALCHEMY', 1, NULL, 10, 90.00, 50, 1, '2026-03-27 14:43:16', '2026-03-27 14:43:16');
INSERT INTO `item_recipes` VALUES (2, '炼制回灵丹', '使用灵草炼制回灵丹', 2, 3, 'ALCHEMY', 3, NULL, 15, 85.00, 80, 1, '2026-03-27 14:43:16', '2026-03-27 14:43:16');
INSERT INTO `item_recipes` VALUES (3, '炼制经验丹', '使用仙草炼制经验丹', 3, 1, 'ALCHEMY', 10, '练气期五层', 30, 70.00, 200, 1, '2026-03-27 14:43:16', '2026-03-27 14:43:16');
INSERT INTO `item_recipes` VALUES (4, '炼制突破丹', '使用仙草和妖丹炼制突破丹', 4, 1, 'ALCHEMY', 15, '筑基期', 60, 50.00, 500, 1, '2026-03-27 14:43:16', '2026-03-27 14:43:16');
INSERT INTO `item_recipes` VALUES (5, '炼制大还丹', '使用灵草和灵石炼制大还丹', 11, 2, 'ALCHEMY', 8, NULL, 20, 80.00, 150, 1, '2026-03-27 14:43:16', '2026-03-27 14:43:16');
INSERT INTO `item_recipes` VALUES (6, '炼制聚灵丹', '使用灵草和灵石炼制聚灵丹', 12, 2, 'ALCHEMY', 10, NULL, 25, 75.00, 200, 1, '2026-03-27 14:43:16', '2026-03-27 14:43:16');

-- ----------------------------
-- Table structure for item_statistics
-- ----------------------------
DROP TABLE IF EXISTS `item_statistics`;
CREATE TABLE `item_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_items_obtained` bigint NOT NULL DEFAULT 0 COMMENT '总获得物品数',
  `total_items_used` bigint NOT NULL DEFAULT 0 COMMENT '总使用物品数',
  `total_items_sold` bigint NOT NULL DEFAULT 0 COMMENT '总出售物品数',
  `total_items_crafted` bigint NOT NULL DEFAULT 0 COMMENT '总制作物品数',
  `total_spirit_stones_spent` bigint NOT NULL DEFAULT 0 COMMENT '总花费灵石',
  `total_spirit_stones_earned` bigint NOT NULL DEFAULT 0 COMMENT '总赚取灵石',
  `highest_quality_obtained` int NOT NULL DEFAULT 0 COMMENT '获得最高品质',
  `rarest_item_id` int NULL DEFAULT NULL COMMENT '最稀有物品ID',
  `favorite_item_id` int NULL DEFAULT NULL COMMENT '最常用物品ID',
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for item_storages
-- ----------------------------
DROP TABLE IF EXISTS `item_storages`;
CREATE TABLE `item_storages`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `storage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库类型：VAULT/GUILD/TEMP',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `slot_position` int NULL DEFAULT NULL COMMENT '槽位位置',
  `locked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否锁定',
  `stored_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '存入时间',
  `expire_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_storage_type`(`storage_type` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_expire_at`(`expire_at` ASC) USING BTREE,
  CONSTRAINT `fk_item_storages_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_item_storages_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品仓库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_storages
-- ----------------------------

-- ----------------------------
-- Table structure for item_usage_logs
-- ----------------------------
DROP TABLE IF EXISTS `item_usage_logs`;
CREATE TABLE `item_usage_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '使用数量',
  `usage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '使用类型：USE/SELL/DROP/TRADE',
  `effect_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '效果结果(JSON)',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_usage_type`(`usage_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_item_usage_logs_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_item_usage_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品使用日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_usage_logs
-- ----------------------------

-- ----------------------------
-- Table structure for items
-- ----------------------------
DROP TABLE IF EXISTS `items`;
CREATE TABLE `items`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '物品ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品描述',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型',
  `quality` int NOT NULL COMMENT '品质',
  `stackable` tinyint(1) NOT NULL COMMENT '是否可堆叠',
  `max_stack` int NOT NULL COMMENT '最大堆叠数',
  `price` int NOT NULL COMMENT '价格',
  `sellable` tinyint(1) NOT NULL COMMENT '是否可出售',
  `usable` tinyint(1) NOT NULL COMMENT '是否可使用',
  `effect` json NULL COMMENT '效果',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_item_type`(`type` ASC) USING BTREE,
  INDEX `idx_quality`(`quality` ASC) USING BTREE,
  INDEX `idx_stackable`(`stackable` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of items
-- ----------------------------
INSERT INTO `items` VALUES (1, '疗伤丹', '恢复生命值的丹药', 'consumable', 1, 1, 99, 50, 1, 1, '{\"heal\": 50}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (2, '回灵丹', '恢复灵力的丹药', 'consumable', 1, 1, 99, 50, 1, 1, '{\"restore_mana\": 50}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (3, '经验丹', '提升经验值的丹药', 'consumable', 2, 1, 50, 200, 1, 1, '{\"exp\": 100}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (4, '突破丹', '帮助突破境界的丹药', 'consumable', 3, 1, 10, 1000, 1, 1, '{\"breakthrough\": 1}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (5, '灵草', '蕴含灵力的草药', 'material', 1, 1, 999, 10, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (6, '灵石', '蕴含纯净灵力的石头', 'material', 2, 1, 999, 100, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (7, '妖丹', '妖兽内丹，炼器材料', 'material', 3, 1, 99, 500, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (8, '仙草', '传说中的仙草', 'material', 4, 1, 10, 2000, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (9, '新手礼包', '包含基础装备和物品的礼包', 'special', 1, 0, 1, 0, 0, 1, '{\"items\": [{\"id\": 1, \"quantity\": 1}, {\"id\": 2, \"quantity\": 5}]}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (10, '修炼心得', '记录修炼感悟的书籍', 'book', 2, 0, 1, 500, 1, 1, '{\"cultivation_speed\": 1.1}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (11, '大还丹', '高级恢复丹药', 'consumable', 2, 1, 50, 150, 1, 1, '{\"heal\": 150}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (12, '聚灵丹', '高级灵力恢复丹药', 'consumable', 2, 1, 50, 150, 1, 1, '{\"restore_mana\": 150}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (13, '悟道丹', '提升修炼速度的丹药', 'consumable', 3, 1, 20, 500, 1, 1, '{\"duration\": 3600, \"cultivation_speed\": 1.5}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (14, '驻颜丹', '保持青春的丹药', 'consumable', 2, 1, 10, 300, 1, 1, '{\"beauty\": 10}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (15, '洗髓丹', '洗练根骨的丹药', 'consumable', 4, 1, 5, 2000, 1, 1, '{\"attribute_reset\": 1}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (16, '火符', '火属性攻击符箓', 'consumable', 1, 1, 99, 80, 1, 1, '{\"fire_damage\": 100}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (17, '水符', '水属性防御符箓', 'consumable', 1, 1, 99, 80, 1, 1, '{\"water_shield\": 50}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (18, '雷符', '雷属性攻击符箓', 'consumable', 2, 1, 50, 200, 1, 1, '{\"thunder_damage\": 200}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (19, '玄铁矿', '珍贵的炼器矿石', 'material', 3, 1, 99, 300, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (20, '千年灵芝', '千年的灵芝，炼丹极品', 'material', 4, 1, 20, 1000, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (21, '龙血', '传说中的龙血', 'material', 5, 1, 10, 5000, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (22, '凤凰羽毛', '凤凰的羽毛', 'material', 5, 1, 10, 5000, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (23, '铜宝箱', '普通宝箱', 'chest', 1, 0, 1, 100, 0, 1, '{\"items\": [{\"id\": 1, \"quantity\": 5}, {\"id\": 5, \"quantity\": 10}]}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (24, '银宝箱', '高级宝箱', 'chest', 2, 0, 1, 500, 0, 1, '{\"items\": [{\"id\": 3, \"quantity\": 3}, {\"id\": 6, \"quantity\": 5}], \"spirit_stones\": 200}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (25, '金宝箱', '稀有宝箱', 'chest', 3, 0, 1, 2000, 0, 1, '{\"items\": [{\"id\": 4, \"quantity\": 1}, {\"id\": 13, \"quantity\": 2}], \"spirit_stones\": 1000}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (26, '钻石宝箱', '传说宝箱', 'chest', 4, 0, 1, 10000, 0, 1, '{\"items\": [{\"id\": 15, \"quantity\": 1}], \"equipments\": [{\"id\": 18, \"rate\": 30}], \"spirit_stones\": 5000}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (27, '铁矿石', '普通的铁矿石', 'material', 1, 1, 999, 20, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (28, '铜矿石', '普通的铜矿石', 'material', 1, 1, 999, 15, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (29, '银矿石', '珍贵的银矿石', 'material', 2, 1, 200, 100, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (30, '金矿石', '稀有的金矿石', 'material', 3, 1, 100, 500, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (31, '狼皮', '野狼的皮毛', 'material', 1, 1, 999, 30, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (32, '蛇胆', '蛇妖的胆囊', 'material', 2, 1, 200, 150, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (33, '虎骨', '猛虎的骨骼', 'material', 2, 1, 200, 200, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (34, '朱果', '红色的灵果', 'material', 3, 1, 50, 800, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (35, '天山雪莲', '雪山上的珍贵药材', 'material', 4, 1, 20, 3000, 1, 0, '{}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (36, '筑基丹', '帮助筑基的丹药', 'consumable', 4, 1, 5, 5000, 1, 1, '{\"realm_breakthrough\": \"筑基期\"}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (37, '金丹丹', '帮助凝结金丹的丹药', 'consumable', 5, 1, 3, 20000, 1, 1, '{\"realm_breakthrough\": \"金丹期\"}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (38, '元婴丹', '帮助凝结元婴的丹药', 'consumable', 5, 1, 1, 50000, 1, 1, '{\"realm_breakthrough\": \"元婴期\"}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (39, '神行符', '增加移动速度的符箓', 'consumable', 2, 1, 50, 200, 1, 1, '{\"duration\": 1800, \"speed_boost\": 50}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');
INSERT INTO `items` VALUES (40, '隐身符', '隐身效果的符箓', 'consumable', 3, 1, 20, 500, 1, 1, '{\"stealth\": 1, \"duration\": 600}', '2026-03-27 14:43:15', '2026-03-27 14:43:15');

-- ----------------------------
-- Table structure for lore_entries
-- ----------------------------
DROP TABLE IF EXISTS `lore_entries`;
CREATE TABLE `lore_entries`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '传说条目ID',
  `lore_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '条目唯一标识',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '传说标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '传说内容',
  `lore_layer` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '表面' COMMENT '传说层级：表面/参与/深层',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类：世界/宗门/人物/事件',
  `related_npcs` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联NPC(JSON数组)',
  `related_lore_keys` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联传说key(JSON数组)',
  `discover_condition` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发现条件描述',
  `min_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最低境界要求',
  `min_level` int NULL DEFAULT 1 COMMENT '最低等级要求',
  `required_lore_keys` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '前置传说key(JSON数组)',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_lore_key`(`lore_key` ASC) USING BTREE,
  INDEX `idx_lore_layer`(`lore_layer` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_min_level`(`min_level` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '传说条目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lore_entries
-- ----------------------------
INSERT INTO `lore_entries` VALUES (1, 'L001', '苍玄界概览', '苍玄界由五大灵脉支撑，灵脉交汇处形成仙城。世界分为凡人居住的下界和修仙者争夺的上界，以天堑分隔。修仙者通过吸收灵气提升修为，目标是渡天劫、飞升仙界。', '表面', '世界', NULL, NULL, '自动获得', NULL, 1, NULL, 'fa-globe', 1, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (2, 'L002', '五大灵脉', '五条灵脉分布在苍玄界各处，分别以金、木、水、火、土五行命名。每条灵脉都是一个灵气极为充沛的区域，围绕灵脉建立的仙城是修仙者的聚集地。', '表面', '世界', NULL, '[\"L009\"]', '天剑宗藏经阁一楼', NULL, 1, NULL, 'fa-fire', 2, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (3, 'L003', '四大宗门', '天剑宗——以剑入道，追求极致力量；万法阁——博采众长，研究万法之源；幽冥殿——以身为炉，炼化万物；灵兽山——人兽共生，追求自然和谐。四大宗门维持着苍玄界的秩序。', '表面', '宗门', NULL, '[\"L007\"]', '天剑宗入门仪式', NULL, 1, NULL, 'fa-landmark', 3, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (4, 'L004', '天剑宗', '苍玄界最强的宗门之一，建立在一条中型灵脉之上，已有千年历史。宗门以剑道闻名，弟子修炼讲究一剑破万法。内门弟子驻于山腰，外门弟子分布于山脚的青云镇。', '表面', '宗门', '[\"苏玄清\",\"剑无痕\"]', '[\"L005\"]', '天剑宗入门仪式', NULL, 1, NULL, 'fa-bolt', 4, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (5, 'L005', '封魔之战', '三千年前的远古战争。传说当时有\"魔物\"入侵苍玄界，四大宗门联手封印。战争留下了巨大的伤疤——天堑。幽冥殿被认为与\"魔物\"有染，被逐出中原。', '参与', '事件', NULL, '[\"L009\",\"L010\"]', '妖兽林深处探索', '筑基期', 11, NULL, 'fa-skull-crossbones', 5, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (6, 'L006', '灵兽起源', '灵兽并非普通的野兽——它们是远古时代就存在的生灵，部分灵兽拥有数千年甚至更久的记忆。灵兽山的白鹿真人似乎知道更多……', '参与', '世界', '[\"白鹿真人\"]', NULL, '白鹿真人好感度>60', '筑基期', 11, NULL, 'fa-paw', 6, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (7, 'L007', '万法阁秘辛', '万法阁的创始人原本是幽冥殿的弟子，因理念不合叛逃。两宗的恩怨已延续三千年。林婉儿的身世似乎与此有关……', '参与', '宗门', '[\"林婉儿\"]', NULL, '林婉儿好感度>60', '筑基期', 11, NULL, 'fa-book', 7, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (8, 'L008', '苏玄清的过去', '苏玄清，天剑宗外门长老。表面上是一个温和的退休老者，实际上实力深不可测。三百年前，他在一次事件中失去了挚友——他唯一输过的对手。他在青云镇的任务似乎不只是教导弟子……', '参与', '人物', '[\"苏玄清\"]', NULL, '苏玄清好感度>80', '金丹期', 16, NULL, 'fa-user-secret', 8, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (9, 'L009', '五灵脉的秘密', '五条灵脉并非自然形成——而是上古五位大能以自身道基铸就。这意味着每条灵脉都蕴含着一位远古强者的毕生修为。灵脉的波动似乎在逐年增强……', '深层', '世界', NULL, '[\"L010\"]', '金丹期 + 收集L002+L005', '金丹期', 16, NULL, 'fa-yin-yang', 9, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (10, 'L010', '苍玄仙帝', '苍玄界本身是一位陨落的仙人的尸骸化成——苍玄仙帝。十万年前，仙帝陨落，其身躯化为山川大地，其灵力化为灵脉，其残存的意识……至今仍在沉睡。', '深层', '世界', NULL, '[\"L011\"]', '金丹期 + 探索青云镇古井', '金丹期', 16, NULL, 'fa-crown', 10, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (11, 'L011', '天劫真相', '天劫不是天道对修士的考验——而是苍玄仙帝残存意识对\"可能的继承者\"的筛选。每一次天劫，都是仙帝在寻找一个能承载其意志的容器。', '深层', '世界', NULL, '[\"L012\"]', '元婴期 + 收集L009+L010', '元婴期', 20, NULL, 'fa-bolt', 11, 1, '2026-03-27 14:45:37');
INSERT INTO `lore_entries` VALUES (12, 'L012', '飞升的真相', '所谓飞升，其实是被仙帝残意识吞噬——真正的永生，需要找到另一条路。散修联盟的\"老陈\"似乎知道更多……', '深层', '世界', '[\"老陈\"]', NULL, '老陈好感度>90', '元婴期', 20, NULL, 'fa-dove', 12, 1, '2026-03-27 14:45:37');

-- ----------------------------
-- Table structure for mail_attachments
-- ----------------------------
DROP TABLE IF EXISTS `mail_attachments`;
CREATE TABLE `mail_attachments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `mail_id` bigint NOT NULL COMMENT '邮件ID',
  `item_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型：ITEM/EQUIPMENT/SPIRIT_STONES/EXP',
  `item_id` int NULL DEFAULT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_mail_id`(`mail_id` ASC) USING BTREE,
  INDEX `idx_item_type`(`item_type` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  CONSTRAINT `fk_mail_attachments_mail` FOREIGN KEY (`mail_id`) REFERENCES `player_mails` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '邮件附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mail_attachments
-- ----------------------------

-- ----------------------------
-- Table structure for map_monsters
-- ----------------------------
DROP TABLE IF EXISTS `map_monsters`;
CREATE TABLE `map_monsters`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `map_id` int NOT NULL COMMENT '地图ID',
  `monster_id` int NOT NULL COMMENT '怪物ID（关联 monsters 表）',
  `spawn_rate` decimal(5, 2) NULL DEFAULT 100.00 COMMENT '出现概率%',
  `min_level` int NULL DEFAULT 1 COMMENT '最小等级',
  `max_level` int NULL DEFAULT 1 COMMENT '最大等级',
  `is_elite` tinyint(1) NULL DEFAULT 0 COMMENT '是否为精英怪: 0-否 1-是',
  `spawn_weight` int NULL DEFAULT 1 COMMENT '刷新权重（随机选择用）',
  `encounter_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '遭遇描述文本',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_map_id`(`map_id` ASC) USING BTREE,
  INDEX `idx_monster_id`(`monster_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '地图怪物配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of map_monsters
-- ----------------------------
INSERT INTO `map_monsters` VALUES (1, 2, 1, 100.00, 1, 2, 0, 3, '一只野狼从灌木丛中窜出，它的眼中闪烁着饥饿的光芒。');
INSERT INTO `map_monsters` VALUES (2, 2, 2, 80.00, 1, 3, 0, 2, '你发现了一只正在觅食的野猪，它看到你后发出了威胁的哼声。');
INSERT INTO `map_monsters` VALUES (3, 4, 1, 100.00, 4, 6, 0, 3, '试剑峰上的幻化成妖兽的灵气向你袭来！');
INSERT INTO `map_monsters` VALUES (4, 4, 3, 60.00, 5, 7, 0, 2, '一只山豹从岩石后跃出，它的速度比你预想的更快。');
INSERT INTO `map_monsters` VALUES (5, 4, 1, 30.00, 6, 7, 1, 1, '一只体型巨大的妖狼挡住了你的去路，它的眼中闪烁着智慧的光芒。');
INSERT INTO `map_monsters` VALUES (6, 5, 4, 100.00, 8, 10, 0, 3, '一群荒野强盗从沙丘后冲出，他们挥舞着锈迹斑斑的武器。');
INSERT INTO `map_monsters` VALUES (7, 5, 5, 70.00, 9, 10, 0, 2, '一只沙蝎从地下钻出，它的尾刺闪烁着诡异的光芒。');
INSERT INTO `map_monsters` VALUES (8, 5, 4, 25.00, 10, 10, 1, 1, '强盗头目骑着一只巨大的沙虫出现，他狞笑着看着你。');
INSERT INTO `map_monsters` VALUES (9, 6, 5, 100.00, 11, 13, 0, 3, '狂暴的沙蝎群向你涌来，它们的数量比你想象的更多。');
INSERT INTO `map_monsters` VALUES (10, 6, 6, 80.00, 12, 13, 0, 2, '一只岩石巨人从地下升起，它的身体由坚硬的岩石构成。');
INSERT INTO `map_monsters` VALUES (11, 6, 6, 35.00, 13, 13, 1, 1, '荒野巨兽出现了！它的每一步都让大地颤抖。');
INSERT INTO `map_monsters` VALUES (12, 7, 7, 100.00, 11, 13, 0, 3, '三只妖狼从树丛中窜出，它们的眼睛泛着不自然的红光。');
INSERT INTO `map_monsters` VALUES (13, 7, 8, 70.00, 12, 13, 0, 2, '一只妖狐在雾中若隐若现，它的眼中闪烁着狡黠的光芒。');
INSERT INTO `map_monsters` VALUES (14, 7, 7, 30.00, 13, 13, 1, 1, '妖狼群的首领出现了，它的体型比普通妖狼大了一倍。');
INSERT INTO `map_monsters` VALUES (15, 8, 7, 100.00, 14, 15, 0, 3, '更多的妖狼从黑雾中现身，它们已经被黑雾完全侵蚀。');
INSERT INTO `map_monsters` VALUES (16, 8, 8, 80.00, 14, 15, 1, 2, '一只巨大的妖狐从黑雾中走出，它的九条尾巴在空中舞动。');
INSERT INTO `map_monsters` VALUES (17, 8, 9, 50.00, 15, 15, 0, 2, '一只被黑雾侵蚀的灵熊向你冲来，它的眼中只有疯狂。');
INSERT INTO `map_monsters` VALUES (18, 11, 9, 100.00, 16, 17, 0, 3, '黑雾中浮现出变异妖兽的身影，它们的形态已经扭曲。');
INSERT INTO `map_monsters` VALUES (19, 11, 10, 80.00, 16, 17, 1, 2, '一只黑雾凝聚成的妖兽向你扑来，它没有实体，却能造成伤害。');
INSERT INTO `map_monsters` VALUES (20, 11, 10, 40.00, 17, 17, 1, 1, '黑雾领主从深谷深处升起，它是这里所有黑雾的源头。');

-- ----------------------------
-- Table structure for map_random_events
-- ----------------------------
DROP TABLE IF EXISTS `map_random_events`;
CREATE TABLE `map_random_events`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `map_id` int NOT NULL COMMENT '地图ID',
  `event_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件类型: discovery/npc/combat/story/danger',
  `event_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '事件描述',
  `probability` decimal(5, 2) NULL DEFAULT 5.00 COMMENT '触发概率%',
  `min_level` int NULL DEFAULT 1 COMMENT '最低等级要求',
  `reward_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '奖励类型: spirit_stones/exp/item/pet_capture/lore/none',
  `reward_value` int NULL DEFAULT 0 COMMENT '奖励数值',
  `active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_map_id`(`map_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '地图随机事件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of map_random_events
-- ----------------------------
INSERT INTO `map_random_events` VALUES (1, 1, 'discovery', '古井异象', '你在古井旁发现了一丝异常的灵气波动，似乎有什么东西在呼唤你。', 3.00, 5, 'spirit_stones', 50, 1);
INSERT INTO `map_random_events` VALUES (2, 1, 'npc', '老陈的茶', '老陈邀请你喝茶，在闲聊中你学到了一些修仙的窍门。', 5.00, 1, 'exp', 20, 1);
INSERT INTO `map_random_events` VALUES (3, 2, 'discovery', '灵草', '你在山崖边发现了一株灵草，它散发着淡淡的荧光。', 8.00, 1, 'item', 1, 1);
INSERT INTO `map_random_events` VALUES (4, 2, 'combat', '妖兽袭击', '一只隐藏的妖兽突然向你发起袭击！', 10.00, 2, 'spirit_stones', 30, 1);
INSERT INTO `map_random_events` VALUES (5, 7, 'discovery', '受伤的灵狐', '你发现了一只受伤的灵狐，它警惕地看着你。', 5.00, 11, 'pet_capture', 1, 1);
INSERT INTO `map_random_events` VALUES (6, 7, 'story', '神秘石碑', '你在林中发现了一块刻有古老文字的石碑。', 2.00, 12, 'lore', 1, 1);
INSERT INTO `map_random_events` VALUES (7, 8, 'danger', '黑雾爆发', '周围的黑雾突然变得浓重，你感到呼吸困难！', 15.00, 14, 'none', 0, 1);
INSERT INTO `map_random_events` VALUES (8, 11, 'story', '远古记忆', '黑雾中闪过一些画面——那是三千年前的封魔之战。', 5.00, 16, 'lore', 1, 1);
INSERT INTO `map_random_events` VALUES (9, 11, 'danger', '魔气侵蚀', '你感到一股邪恶的力量在侵蚀你的心神！', 20.00, 16, 'none', 0, 1);

-- ----------------------------
-- Table structure for monster_skill_mapping
-- ----------------------------
DROP TABLE IF EXISTS `monster_skill_mapping`;
CREATE TABLE `monster_skill_mapping`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '映射ID',
  `monster_id` int NOT NULL COMMENT '怪物ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `skill_level` int NOT NULL DEFAULT 1 COMMENT '技能等级',
  `use_probability` decimal(5, 2) NOT NULL DEFAULT 100.00 COMMENT '使用概率',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_monster_skill`(`monster_id` ASC, `skill_id` ASC) USING BTREE,
  INDEX `idx_monster_id`(`monster_id` ASC) USING BTREE,
  INDEX `idx_skill_id`(`skill_id` ASC) USING BTREE,
  CONSTRAINT `fk_monster_skill_mapping_monster` FOREIGN KEY (`monster_id`) REFERENCES `monsters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_monster_skill_mapping_skill` FOREIGN KEY (`skill_id`) REFERENCES `monster_skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '怪物技能映射表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monster_skill_mapping
-- ----------------------------
INSERT INTO `monster_skill_mapping` VALUES (1, 1, 1, 1, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (2, 2, 1, 1, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (3, 3, 2, 1, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (4, 4, 3, 1, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (5, 5, 5, 1, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (6, 5, 7, 1, 80.00);
INSERT INTO `monster_skill_mapping` VALUES (7, 6, 1, 2, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (8, 6, 6, 1, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (9, 7, 3, 2, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (10, 7, 7, 2, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (11, 7, 5, 1, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (12, 8, 4, 2, 100.00);
INSERT INTO `monster_skill_mapping` VALUES (13, 8, 8, 2, 100.00);

-- ----------------------------
-- Table structure for monster_skills
-- ----------------------------
DROP TABLE IF EXISTS `monster_skills`;
CREATE TABLE `monster_skills`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '怪物技能ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '技能描述',
  `skill_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能类型：ATTACK/DEFENSE/BUFF/DEBUFF',
  `base_damage` double NOT NULL DEFAULT 0 COMMENT '基础伤害',
  `damage_multiplier` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '伤害倍率',
  `cooldown` int NOT NULL DEFAULT 0 COMMENT '冷却时间(回合)',
  `mana_cost` int NOT NULL DEFAULT 0 COMMENT '法力消耗',
  `effect_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '效果类型：STUN/POISON/BURN/FREEZE',
  `effect_duration` int NOT NULL DEFAULT 0 COMMENT '效果持续回合数',
  `effect_value` int NOT NULL DEFAULT 0 COMMENT '效果数值',
  `trigger_rate` decimal(5, 2) NOT NULL DEFAULT 100.00 COMMENT '触发概率',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_skill_type`(`skill_type` ASC) USING BTREE,
  INDEX `idx_effect_type`(`effect_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '怪物技能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monster_skills
-- ----------------------------
INSERT INTO `monster_skills` VALUES (1, '撕咬', '锋利的牙齿撕咬', 'ATTACK', 15, 1.20, 2, 0, NULL, 0, 0, 100.00, 1, '2026-03-27 14:43:19');
INSERT INTO `monster_skills` VALUES (2, '毒液喷射', '喷射剧毒液体', 'ATTACK', 10, 1.00, 3, 0, 'POISON', 3, 5, 80.00, 1, '2026-03-27 14:43:19');
INSERT INTO `monster_skills` VALUES (3, '火焰吐息', '喷出炽热火焰', 'ATTACK', 25, 1.50, 4, 0, 'BURN', 2, 10, 70.00, 1, '2026-03-27 14:43:19');
INSERT INTO `monster_skills` VALUES (4, '冰冻气息', '释放寒冰气息', 'ATTACK', 20, 1.30, 5, 0, 'FREEZE', 1, 0, 60.00, 1, '2026-03-27 14:43:19');
INSERT INTO `monster_skills` VALUES (5, '狂暴', '进入狂暴状态', 'BUFF', 0, 0.00, 10, 0, NULL, 5, 50, 100.00, 1, '2026-03-27 14:43:19');
INSERT INTO `monster_skills` VALUES (6, '防御姿态', '进入防御状态', 'DEFENSE', 0, 0.00, 8, 0, NULL, 3, 30, 100.00, 1, '2026-03-27 14:43:19');
INSERT INTO `monster_skills` VALUES (7, '雷霆一击', '蕴含雷电的一击', 'ATTACK', 35, 2.00, 6, 0, 'STUN', 1, 0, 50.00, 1, '2026-03-27 14:43:19');
INSERT INTO `monster_skills` VALUES (8, '暗影突袭', '从暗影中发动攻击', 'ATTACK', 30, 1.80, 4, 0, NULL, 0, 0, 90.00, 1, '2026-03-27 14:43:19');

-- ----------------------------
-- Table structure for monsters
-- ----------------------------
DROP TABLE IF EXISTS `monsters`;
CREATE TABLE `monsters`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '怪物ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '怪物名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '怪物描述',
  `level` int NOT NULL DEFAULT 1 COMMENT '等级',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '普通' COMMENT '类型',
  `health` int NOT NULL DEFAULT 100 COMMENT '生命值',
  `attack` int NOT NULL DEFAULT 10 COMMENT '攻击力',
  `defense` int NOT NULL DEFAULT 5 COMMENT '防御力',
  `speed` int NOT NULL DEFAULT 10 COMMENT '速度',
  `exp_reward` int NOT NULL DEFAULT 50 COMMENT '经验奖励',
  `spirit_stones_reward` int NOT NULL DEFAULT 10 COMMENT '灵石奖励',
  `drop_rate` int NOT NULL DEFAULT 10 COMMENT '掉落率',
  `drop_equipment_id` int NULL DEFAULT NULL COMMENT '掉落装备ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_monster_level`(`level` ASC) USING BTREE,
  INDEX `idx_monster_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '怪物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monsters
-- ----------------------------
INSERT INTO `monsters` VALUES (1, '野狼', '常见的野生狼', 1, '普通', 100, 10, 5, 10, 50, 10, 10, 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `monsters` VALUES (2, '山贼', '路边的小贼', 2, '普通', 120, 12, 6, 12, 60, 12, 10, 2, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `monsters` VALUES (3, '妖怪', '低级妖怪', 3, '普通', 150, 15, 8, 15, 80, 15, 10, 3, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `monsters` VALUES (4, '邪修', '修炼邪法的修士', 5, '普通', 200, 20, 10, 18, 120, 20, 15, 6, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `monsters` VALUES (5, '狂暴野狼', '狂暴的野生狼', 5, '精英', 300, 30, 15, 20, 200, 40, 20, 7, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `monsters` VALUES (6, '山贼头目', '贼寇的首领', 8, '精英', 450, 40, 20, 25, 300, 60, 25, 12, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `monsters` VALUES (7, '狼王', 'BOSS级别的狼群首领', 10, 'BOSS', 800, 60, 30, 30, 500, 100, 50, 18, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `monsters` VALUES (8, '千年妖怪', '修炼千年的强大妖怪', 15, 'BOSS', 1500, 90, 45, 40, 800, 150, 50, 24, '2026-03-27 14:43:19', '2026-03-27 14:43:19');

-- ----------------------------
-- Table structure for npc_daily_dialogues
-- ----------------------------
DROP TABLE IF EXISTS `npc_daily_dialogues`;
CREATE TABLE `npc_daily_dialogues`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `npc_id` int NOT NULL COMMENT 'NPC ID',
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话文本',
  `conditions` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发条件(JSON: {time, realm, level_gte, pet_hunger_lte, days_since_login_gte, has_flag})',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_npc_id`(`npc_id` ASC) USING BTREE,
  INDEX `idx_active`(`npc_id` ASC, `active` ASC) USING BTREE,
  CONSTRAINT `fk_npc_daily_dialogues_npc` FOREIGN KEY (`npc_id`) REFERENCES `npcs` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'NPC日常对话池表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of npc_daily_dialogues
-- ----------------------------
INSERT INTO `npc_daily_dialogues` VALUES (1, 1, '起了？今日灵气尚可，适合修炼。别浪费了。', NULL, 3, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (2, 1, '（煮茶中）夜深了还修炼？年轻人精力旺盛是好事……但茶凉了就不好喝了。', '{\"time\": \"night\"}', 2, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (3, 1, '你的气息比昨天稳了。快要圆满了？……别急。急不得。', '{\"level_gte\": 8}', 4, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (4, 1, '你的灵兽看起来不太开心。为师不是说了——灵兽和弟子一样，都要上心。', '{\"pet_hunger_lte\": 20}', 5, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (5, 1, '两天没见。去哪了？（停顿）……不是为师想你了。是茶凉了没人喝。', '{\"days_since_login_gte\": 2}', 6, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (6, 1, '突破之后，感觉如何？（不等回答）都会过去的。不论是好是坏。', '{\"has_flag\": \"broke_through_once\"}', 3, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (7, 2, '又来了？行吧。别拖后腿就行。', NULL, 2, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (8, 2, '（擦拭佩剑）你的剑法……还需要多练。我说的是实话。', NULL, 1, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (9, 2, '（你走近时，剑无痕抬头看了你一眼，又低下头继续擦剑）……有事？', NULL, 3, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (10, 3, '（从书堆后面探出头）哦？是你啊。稍等，让我找到这一页……好了！怎么了？', NULL, 2, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (11, 3, '你知道吗？万法阁有一本书，最后一页是空白的。不是没写完——是故意留白的。', '{\"min_relation\": 41}', 4, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (12, 3, '别担心灵石不够，办法总比困难多。至少……大概吧。大概率吧。六四开。', NULL, 3, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (13, 6, '走了又来了？', NULL, 1, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (14, 6, '修炼累了喝点茶。别问我哪来的茶叶，问就是自己种的。', NULL, 2, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (15, 6, '飞升？那玩意儿啊……（嗑瓜子）就跟考试一样，考上了不一定好，考不上也不一定坏。关键是——你想不想考。', '{\"min_relation\": 21}', 5, 1, '2026-03-27 14:45:37');
INSERT INTO `npc_daily_dialogues` VALUES (16, 6, '（看着远方）你看那片云，像不像一只手？（停顿）……算了，我老眼昏花。', NULL, 3, 1, '2026-03-27 14:45:37');

-- ----------------------------
-- Table structure for npcs
-- ----------------------------
DROP TABLE IF EXISTS `npcs`;
CREATE TABLE `npcs`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'NPC ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'NPC名称',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头衔/称谓',
  `faction` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属势力：天剑宗/万法阁/幽冥殿/灵兽山/散修联盟',
  `role_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '故事角色类型：mentor/rival/friend/villain/neutral',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'NPC简介',
  `personality_traits` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性格特征(逗号分隔)',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认位置',
  `min_level` int NOT NULL DEFAULT 1 COMMENT '最低出现等级',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'NPC图标',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_npc_name`(`name` ASC) USING BTREE,
  INDEX `idx_faction`(`faction` ASC) USING BTREE,
  INDEX `idx_min_level`(`min_level` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'NPC基础数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of npcs
-- ----------------------------
INSERT INTO `npcs` VALUES (1, '苏玄清', '外门长老', '天剑宗', 'mentor', '玩家师尊，看似普通的老者，实则深不可测。在青云镇看守封魔残气已三百年。', '克制,温和,留白,洞察力', '青云镇', 1, NULL, 1, 1, '2026-03-27 14:43:23', '2026-03-27 14:43:23');
INSERT INTO `npcs` VALUES (2, '剑无痕', '内门首席弟子', '天剑宗', 'rival', '天剑宗内门首席弟子，出身世家，傲慢但笨拙地关心同门。', '傲慢,利落,笨拙的温柔,不服输', '天剑宗', 2, NULL, 1, 2, '2026-03-27 14:43:23', '2026-03-27 14:43:23');
INSERT INTO `npcs` VALUES (3, '林婉儿', '万法阁师姐', '万法阁', 'friend', '万法阁的核心弟子，温和聪慧，在万卷藏书中寻找身世之谜。', '温柔,书卷气,聪明,偶尔活泼', '万法阁', 6, NULL, 1, 3, '2026-03-27 14:43:23', '2026-03-27 14:43:23');
INSERT INTO `npcs` VALUES (4, '冥渊', '幽冥殿殿主', '幽冥殿', 'villain', '天剑宗叛逃弟子，追求打破修仙秩序，平静得令人不安。', '平静,古雅,扭曲逻辑,深邃', '幽冥殿', 10, NULL, 1, 4, '2026-03-27 14:43:23', '2026-03-27 14:43:23');
INSERT INTO `npcs` VALUES (5, '白鹿真人', '灵兽山山主', '灵兽山', 'friend', '灵兽山之主，朴素如老农，实为上古大能，以灵兽为伴。', '朴素,温暖,沉默,洞察', '灵兽山', 5, NULL, 1, 5, '2026-03-27 14:43:23', '2026-03-27 14:43:23');
INSERT INTO `npcs` VALUES (6, '老陈', '药材商人', '散修联盟', 'neutral', '青云镇摆摊卖药材的老头，真实身份是渡劫期大能。', '随意,幽默,深不可测,装傻', '青云镇', 1, NULL, 1, 6, '2026-03-27 14:43:23', '2026-03-27 14:43:23');

-- ----------------------------
-- Table structure for offline_narrative_events
-- ----------------------------
DROP TABLE IF EXISTS `offline_narrative_events`;
CREATE TABLE `offline_narrative_events`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `event_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件唯一标识',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '事件标题',
  `narrative` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '叙事文本',
  `probability` decimal(5, 3) NOT NULL DEFAULT 0.010 COMMENT '触发概率(0.001-1.000)',
  `min_offline_hours` int NOT NULL DEFAULT 4 COMMENT '最低离线小时数',
  `max_offline_hours` int NULL DEFAULT NULL COMMENT '最高离线小时数(NULL=不限)',
  `min_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最低境界要求',
  `min_level` int NULL DEFAULT 1 COMMENT '最低等级要求',
  `reward_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '奖励类型',
  `reward_data` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '奖励数据(JSON)',
  `set_flag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发的flag',
  `unlock_dialogue_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '解锁的对话',
  `npc_relation_change` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'NPC好感度变化(JSON: {npc_id: change})',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_event_key`(`event_key` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE,
  INDEX `idx_min_offline`(`min_offline_hours` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '离线事件叙事表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of offline_narrative_events
-- ----------------------------
INSERT INTO `offline_narrative_events` VALUES (1, 'injured_spirit_fox', '受伤的灵狐', '你离线期间，一只受伤的灵狐偷偷溜进了你的洞府。它蜷缩在你的修炼蒲团旁，等你回来时，它已经用灵力为自己疗了伤——但看起来很虚弱。', 0.010, 8, NULL, NULL, 1, NULL, NULL, 'found_injured_fox', NULL, '{\"5\": 3}', 1, 1, '2026-03-27 14:45:37');
INSERT INTO `offline_narrative_events` VALUES (2, 'mysterious_letter', '神秘来信', '你的洞府门缝里塞了一封信。没有署名，只有一句话——\"剑冢之下，有你想要的东西。切记：不是所有锁都该打开。\"', 0.005, 24, NULL, '筑基期', 11, NULL, NULL, 'received_mysterious_letter', NULL, NULL, 1, 2, '2026-03-27 14:45:37');
INSERT INTO `offline_narrative_events` VALUES (3, 'old_chen_tea', '老陈的茶', '老陈来过你的洞府。桌上多了一壶茶和一张纸条——\"修炼累了喝点茶。别问我哪来的茶叶，问就是自己种的。（画了一个笑脸）\"', 0.015, 4, NULL, NULL, 1, NULL, NULL, NULL, NULL, '{\"6\": 5}', 1, 3, '2026-03-27 14:45:37');
INSERT INTO `offline_narrative_events` VALUES (4, 'spirit_vine_bloom', '灵藤花开', '洞府外的一株灵藤在你离线期间开了花。灵花散发着淡雅的香气，闻到它的人会感到修炼时灵气流转更加顺畅。', 0.008, 12, NULL, NULL, 1, NULL, NULL, 'spirit_vine_bloomed', NULL, NULL, 1, 4, '2026-03-27 14:45:37');
INSERT INTO `offline_narrative_events` VALUES (5, 'sword_echo', '剑鸣之夜', '深夜，你的佩剑突然自行发出嗡鸣。剑身微微发光，上面浮现出几个模糊的古字。你辨认出其中两个字——\"……醒来……\"', 0.003, 48, NULL, '筑基期', 11, NULL, NULL, 'sword_resonance', NULL, NULL, 1, 5, '2026-03-27 14:45:37');

-- ----------------------------
-- Table structure for offline_rewards
-- ----------------------------
DROP TABLE IF EXISTS `offline_rewards`;
CREATE TABLE `offline_rewards`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '离线奖励ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `offline_minutes` int NOT NULL DEFAULT 0 COMMENT '离线分钟数',
  `exp_gained` int NOT NULL DEFAULT 0 COMMENT '获得经验',
  `spirit_stones_gained` int NOT NULL DEFAULT 0 COMMENT '获得灵石',
  `claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否领取',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `claimed_at` timestamp NULL DEFAULT NULL COMMENT '领取时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_offline_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_offline_claimed`(`claimed` ASC) USING BTREE,
  CONSTRAINT `fk_offline_rewards_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '离线奖励表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of offline_rewards
-- ----------------------------

-- ----------------------------
-- Table structure for offline_risk_events
-- ----------------------------
DROP TABLE IF EXISTS `offline_risk_events`;
CREATE TABLE `offline_risk_events`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `map_id` int NOT NULL COMMENT '地图ID',
  `event_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '事件描述',
  `min_offline_hours` int NULL DEFAULT 6 COMMENT '最小离线时长（小时）',
  `max_offline_hours` int NULL DEFAULT 24 COMMENT '最大离线时长（小时）',
  `probability` decimal(5, 2) NULL DEFAULT 10.00 COMMENT '触发概率%',
  `spirit_stone_loss_percent` int NULL DEFAULT 10 COMMENT '灵石损失百分比',
  `exp_loss_percent` int NULL DEFAULT 0 COMMENT '经验损失百分比',
  `active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '离线风险事件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of offline_risk_events
-- ----------------------------
INSERT INTO `offline_risk_events` VALUES (1, 6, '妖兽袭击', '你离线期间，营地遭到了妖兽的袭击，部分物资被毁。', 6, 12, 10.00, 10, 0, 1);
INSERT INTO `offline_risk_events` VALUES (2, 6, '强盗掠夺', '山贼发现了你的营地，抢走了一些灵石。', 12, 18, 25.00, 20, 0, 1);
INSERT INTO `offline_risk_events` VALUES (3, 7, '妖狼围攻', '妖狼群在你的营地周围徘徊，你不得不消耗灵石布置防御。', 6, 12, 15.00, 15, 0, 1);
INSERT INTO `offline_risk_events` VALUES (4, 8, '黑雾侵蚀', '黑雾侵蚀了你的防护法阵，修复需要消耗灵石。', 6, 12, 20.00, 15, 0, 1);
INSERT INTO `offline_risk_events` VALUES (5, 8, '妖兽夜袭', '深夜，一只强大的妖兽袭击了你的营地。', 12, 18, 30.00, 25, 0, 1);
INSERT INTO `offline_risk_events` VALUES (6, 11, '魔气入侵', '魔气入侵了你的防护，你不得不消耗大量灵石净化。', 6, 12, 25.00, 20, 0, 1);
INSERT INTO `offline_risk_events` VALUES (7, 11, '黑雾暴动', '黑雾突然暴动，你险些迷失其中。', 12, 18, 40.00, 30, 0, 1);

-- ----------------------------
-- Table structure for passive_skills
-- ----------------------------
DROP TABLE IF EXISTS `passive_skills`;
CREATE TABLE `passive_skills`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '被动技能ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '技能描述',
  `passive_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：STAT/COMBAT/CULTIVATION/SPECIAL',
  `effect_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '效果类型',
  `effect_value` decimal(10, 2) NOT NULL COMMENT '效果值',
  `is_percentage` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否为百分比',
  `max_level` int NOT NULL DEFAULT 5 COMMENT '最大等级',
  `upgrade_cost` int NOT NULL DEFAULT 100 COMMENT '升级消耗(灵石)',
  `cost_multiplier` decimal(5, 2) NOT NULL DEFAULT 1.50 COMMENT '费用倍率',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `required_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '需求境界',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_passive_type`(`passive_type` ASC) USING BTREE,
  INDEX `idx_effect_type`(`effect_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '被动技能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of passive_skills
-- ----------------------------
INSERT INTO `passive_skills` VALUES (1, '强身健体', '永久提升生命值', 'STAT', 'health', 100.00, 0, 10, 500, 1.50, 1, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (2, '灵力充沛', '永久提升法力值', 'STAT', 'mana', 50.00, 0, 10, 500, 1.50, 1, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (3, '力量强化', '永久提升攻击力', 'STAT', 'attack', 10.00, 0, 10, 800, 1.60, 5, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (4, '铁壁防御', '永久提升防御力', 'STAT', 'defense', 8.00, 0, 10, 800, 1.60, 5, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (5, '身法敏捷', '永久提升速度', 'STAT', 'speed', 5.00, 0, 10, 600, 1.50, 3, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (6, '暴击本能', '提升暴击率', 'COMBAT', 'crit_rate', 2.00, 1, 10, 1000, 1.80, 10, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (7, '暴击伤害', '提升暴击伤害', 'COMBAT', 'crit_damage', 5.00, 1, 10, 1200, 1.80, 15, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (8, '闪避天赋', '提升闪避率', 'COMBAT', 'dodge_rate', 1.50, 1, 10, 1000, 1.70, 12, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (9, '吸血本能', '攻击时恢复生命', 'COMBAT', 'lifesteal', 1.00, 1, 5, 2000, 2.00, 20, '筑基期', NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (10, '修炼加速', '提升修炼速度', 'CULTIVATION', 'cultivation_speed', 5.00, 1, 10, 1500, 1.60, 8, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (11, '经验加成', '提升获得经验', 'CULTIVATION', 'exp_bonus', 3.00, 1, 10, 1000, 1.50, 5, NULL, NULL, 1, '2026-03-27 14:43:14');
INSERT INTO `passive_skills` VALUES (12, '灵石加成', '提升灵石获取', 'CULTIVATION', 'spirit_stones_bonus', 5.00, 1, 10, 2000, 1.80, 10, NULL, NULL, 1, '2026-03-27 14:43:14');

-- ----------------------------
-- Table structure for pet_abilities
-- ----------------------------
DROP TABLE IF EXISTS `pet_abilities`;
CREATE TABLE `pet_abilities`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '能力ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '能力名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '能力描述',
  `ability_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：PASSIVE/ACTIVE/AURA',
  `effect_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '效果类型',
  `effect_value` decimal(10, 2) NOT NULL COMMENT '效果值',
  `is_percentage` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否为百分比',
  `cooldown` int NOT NULL DEFAULT 0 COMMENT '冷却时间',
  `energy_cost` int NOT NULL DEFAULT 0 COMMENT '能量消耗',
  `required_pet_level` int NOT NULL DEFAULT 1 COMMENT '需求宠物等级',
  `required_pet_rarity` int NOT NULL DEFAULT 1 COMMENT '需求宠物稀有度',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ability_type`(`ability_type` ASC) USING BTREE,
  INDEX `idx_effect_type`(`effect_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物能力表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_abilities
-- ----------------------------
INSERT INTO `pet_abilities` VALUES (1, '守护', '为主人提供护盾', 'ACTIVE', 'shield', 100.00, 0, 30, 20, 5, 1, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (2, '治愈', '恢复主人生命值', 'ACTIVE', 'heal', 15.00, 1, 20, 15, 5, 1, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (3, '狂暴', '提升自身攻击力', 'ACTIVE', 'self_attack_boost', 30.00, 1, 45, 25, 10, 2, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (4, '嘲讽', '吸引敌人攻击', 'ACTIVE', 'taunt', 1.00, 0, 60, 30, 15, 2, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (5, '灵力光环', '提升主人法力恢复', 'AURA', 'owner_mana_regen', 5.00, 1, 0, 0, 10, 2, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (6, '战斗光环', '提升主人攻击力', 'AURA', 'owner_attack_boost', 10.00, 1, 0, 0, 15, 3, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (7, '防御光环', '提升主人防御力', 'AURA', 'owner_defense_boost', 10.00, 1, 0, 0, 15, 3, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (8, '经验光环', '提升获得经验', 'AURA', 'exp_boost', 15.00, 1, 0, 0, 20, 3, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (9, '幸运', '提升掉落率', 'PASSIVE', 'drop_rate_boost', 10.00, 1, 0, 0, 25, 4, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (10, '坚韧', '提升宠物生命值', 'PASSIVE', 'pet_health_boost', 20.00, 1, 0, 0, 5, 1, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (11, '迅捷', '提升宠物速度', 'PASSIVE', 'pet_speed_boost', 15.00, 1, 0, 0, 8, 1, NULL, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_abilities` VALUES (12, '吸血', '攻击时恢复生命', 'PASSIVE', 'lifesteal', 5.00, 1, 0, 0, 30, 4, NULL, 1, '2026-03-27 14:43:21');

-- ----------------------------
-- Table structure for pet_ability_mapping
-- ----------------------------
DROP TABLE IF EXISTS `pet_ability_mapping`;
CREATE TABLE `pet_ability_mapping`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '映射ID',
  `pet_id` int NOT NULL COMMENT '宠物模板ID',
  `ability_id` int NOT NULL COMMENT '能力ID',
  `unlock_level` int NOT NULL DEFAULT 1 COMMENT '解锁等级',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认能力',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pet_ability`(`pet_id` ASC, `ability_id` ASC) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `idx_ability_id`(`ability_id` ASC) USING BTREE,
  CONSTRAINT `fk_pet_ability_mapping_ability` FOREIGN KEY (`ability_id`) REFERENCES `pet_abilities` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_pet_ability_mapping_pet` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物能力映射表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_ability_mapping
-- ----------------------------
INSERT INTO `pet_ability_mapping` VALUES (1, 1, 11, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (2, 1, 9, 10, 0);
INSERT INTO `pet_ability_mapping` VALUES (3, 2, 3, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (4, 2, 6, 15, 0);
INSERT INTO `pet_ability_mapping` VALUES (5, 3, 4, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (6, 3, 7, 20, 0);
INSERT INTO `pet_ability_mapping` VALUES (7, 4, 3, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (8, 4, 6, 15, 0);
INSERT INTO `pet_ability_mapping` VALUES (9, 5, 1, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (10, 5, 7, 15, 0);
INSERT INTO `pet_ability_mapping` VALUES (11, 6, 3, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (12, 6, 6, 15, 0);
INSERT INTO `pet_ability_mapping` VALUES (13, 7, 2, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (14, 7, 10, 5, 0);
INSERT INTO `pet_ability_mapping` VALUES (15, 8, 4, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (16, 8, 11, 10, 0);
INSERT INTO `pet_ability_mapping` VALUES (17, 9, 1, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (18, 9, 10, 10, 0);
INSERT INTO `pet_ability_mapping` VALUES (19, 10, 11, 1, 1);
INSERT INTO `pet_ability_mapping` VALUES (20, 10, 9, 20, 0);

-- ----------------------------
-- Table structure for pet_battle_logs
-- ----------------------------
DROP TABLE IF EXISTS `pet_battle_logs`;
CREATE TABLE `pet_battle_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '战斗日志ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `battle_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '战斗类型：PVE/PVP/ARENA',
  `opponent_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对手类型：MONSTER/PET',
  `opponent_id` int NULL DEFAULT NULL COMMENT '对手ID',
  `result` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结果：WIN/LOSE',
  `damage_dealt` int NOT NULL DEFAULT 0 COMMENT '造成伤害',
  `damage_taken` int NOT NULL DEFAULT 0 COMMENT '受到伤害',
  `exp_gained` int NOT NULL DEFAULT 0 COMMENT '获得经验',
  `abilities_used` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '使用的能力(JSON)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_pet_id`(`player_pet_id` ASC) USING BTREE,
  INDEX `idx_battle_type`(`battle_type` ASC) USING BTREE,
  INDEX `idx_result`(`result` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_pet_battle_logs_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物战斗日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_battle_logs
-- ----------------------------

-- ----------------------------
-- Table structure for pet_equipment
-- ----------------------------
DROP TABLE IF EXISTS `pet_equipment`;
CREATE TABLE `pet_equipment`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '宠物装备ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '装备描述',
  `slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备槽位：NECKLACE/RING/CHARM',
  `quality` int NOT NULL DEFAULT 1 COMMENT '品质',
  `health_bonus` int NOT NULL DEFAULT 0 COMMENT '生命加成',
  `attack_bonus` int NOT NULL DEFAULT 0 COMMENT '攻击加成',
  `defense_bonus` int NOT NULL DEFAULT 0 COMMENT '防御加成',
  `speed_bonus` int NOT NULL DEFAULT 0 COMMENT '速度加成',
  `special_effect` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '特殊效果(JSON)',
  `required_pet_level` int NOT NULL DEFAULT 1 COMMENT '需求宠物等级',
  `price` int NOT NULL DEFAULT 0 COMMENT '价格',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_slot`(`slot` ASC) USING BTREE,
  INDEX `idx_quality`(`quality` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物装备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_equipment
-- ----------------------------
INSERT INTO `pet_equipment` VALUES (1, '灵兽项链', '普通灵兽项链', 'NECKLACE', 1, 50, 5, 5, 0, NULL, 1, 200, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_equipment` VALUES (2, '妖兽之牙', '锋利的兽牙项链', 'NECKLACE', 2, 80, 15, 5, 0, '{\"crit_rate\": 3}', 10, 500, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_equipment` VALUES (3, '神兽护符', '蕴含神力的护符', 'NECKLACE', 3, 150, 30, 15, 5, '{\"crit_rate\": 5, \"crit_damage\": 10}', 25, 2000, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_equipment` VALUES (4, '灵兽戒指', '普通灵兽戒指', 'RING', 1, 30, 10, 3, 5, NULL, 1, 150, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_equipment` VALUES (5, '力量之戒', '提升力量的戒指', 'RING', 2, 50, 25, 5, 8, '{\"attack_boost\": 5}', 15, 800, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_equipment` VALUES (6, '守护之戒', '提供守护的戒指', 'RING', 3, 100, 15, 25, 5, '{\"defense_boost\": 10, \"shield\": 50}', 30, 2500, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_equipment` VALUES (7, '灵兽护符', '普通灵兽护符', 'CHARM', 1, 40, 5, 10, 3, NULL, 1, 180, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_equipment` VALUES (8, '经验护符', '提升经验获取', 'CHARM', 2, 60, 8, 12, 5, '{\"exp_boost\": 10}', 10, 600, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_equipment` VALUES (9, '稀有护符', '稀有的宠物护符', 'CHARM', 3, 120, 20, 20, 10, '{\"exp_boost\": 20, \"drop_boost\": 5}', 25, 3000, 1, '2026-03-27 14:43:21');

-- ----------------------------
-- Table structure for pet_evolution
-- ----------------------------
DROP TABLE IF EXISTS `pet_evolution`;
CREATE TABLE `pet_evolution`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '进化ID',
  `pet_id` int NOT NULL COMMENT '宠物模板ID',
  `evolution_stage` int NOT NULL DEFAULT 1 COMMENT '进化阶段',
  `evolution_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '进化名称',
  `required_level` int NOT NULL COMMENT '需求等级',
  `required_item_id` int NULL DEFAULT NULL COMMENT '需求物品ID',
  `required_item_quantity` int NOT NULL DEFAULT 1 COMMENT '需求数量',
  `health_bonus` int NOT NULL DEFAULT 0 COMMENT '生命加成',
  `attack_bonus` int NOT NULL DEFAULT 0 COMMENT '攻击加成',
  `defense_bonus` int NOT NULL DEFAULT 0 COMMENT '防御加成',
  `speed_bonus` int NOT NULL DEFAULT 0 COMMENT '速度加成',
  `new_ability_id` int NULL DEFAULT NULL COMMENT '新能力ID',
  `appearance_change` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '外观变化(JSON)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pet_evolution`(`pet_id` ASC, `evolution_stage` ASC) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `fk_pet_evolution_item`(`required_item_id` ASC) USING BTREE,
  INDEX `fk_pet_evolution_ability`(`new_ability_id` ASC) USING BTREE,
  CONSTRAINT `fk_pet_evolution_ability` FOREIGN KEY (`new_ability_id`) REFERENCES `pet_abilities` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_pet_evolution_item` FOREIGN KEY (`required_item_id`) REFERENCES `items` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_pet_evolution_pet` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物进化表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_evolution
-- ----------------------------
INSERT INTO `pet_evolution` VALUES (1, 1, 2, '灵狐·觉醒', 30, 21, 1, 100, 20, 15, 10, 9, '{\"color\": \"金色\", \"size\": 1.2}');
INSERT INTO `pet_evolution` VALUES (2, 1, 3, '九尾灵狐', 60, 21, 3, 200, 40, 30, 20, 12, '{\"color\": \"白色\", \"size\": 1.5, \"tails\": 9}');
INSERT INTO `pet_evolution` VALUES (3, 2, 2, '火麒麟·觉醒', 50, 22, 1, 300, 80, 60, 10, 5, '{\"flame\": \"blue\"}');
INSERT INTO `pet_evolution` VALUES (4, 2, 3, '炎帝麒麟', 80, 22, 3, 500, 150, 100, 15, 8, '{\"flame\": \"purple\", \"size\": 1.8}');
INSERT INTO `pet_evolution` VALUES (5, 7, 2, '灵猫·进阶', 15, 8, 2, 50, 10, 8, 5, 10, '{\"eyes\": \"glowing\"}');
INSERT INTO `pet_evolution` VALUES (6, 7, 3, '月光灵猫', 35, 8, 5, 100, 20, 15, 10, 9, '{\"glow\": \"silver\"}');
INSERT INTO `pet_evolution` VALUES (7, 3, 2, '青龙·觉醒', 80, 21, 5, 500, 100, 80, 20, 6, '{\"clouds\": true}');
INSERT INTO `pet_evolution` VALUES (8, 4, 2, '白虎·觉醒', 80, 21, 5, 400, 150, 60, 25, 6, '{\"aura\": \"gold\"}');
INSERT INTO `pet_evolution` VALUES (9, 5, 2, '玄武·觉醒', 80, 21, 5, 800, 50, 150, 10, 7, '{\"shell\": \"crystal\"}');
INSERT INTO `pet_evolution` VALUES (10, 6, 2, '朱雀·觉醒', 80, 22, 5, 450, 130, 70, 30, 8, '{\"wings\": \"flame\"}');

-- ----------------------------
-- Table structure for pet_food
-- ----------------------------
DROP TABLE IF EXISTS `pet_food`;
CREATE TABLE `pet_food`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '食物ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '食物名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '食物描述',
  `food_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '食物类型：BASIC/PREMIUM/SPECIAL',
  `hunger_restore` int NOT NULL DEFAULT 50 COMMENT '恢复饱食度',
  `loyalty_bonus` int NOT NULL DEFAULT 0 COMMENT '忠诚度加成',
  `exp_bonus` int NOT NULL DEFAULT 0 COMMENT '经验加成',
  `quality` int NOT NULL DEFAULT 1 COMMENT '品质',
  `price` int NOT NULL DEFAULT 100 COMMENT '价格',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_food_type`(`food_type` ASC) USING BTREE,
  INDEX `idx_quality`(`quality` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物食物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_food
-- ----------------------------
INSERT INTO `pet_food` VALUES (1, '普通兽粮', '基础宠物食物', 'BASIC', 50, 5, 0, 1, 50, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_food` VALUES (2, '优质兽粮', '优质宠物食物', 'BASIC', 80, 10, 10, 1, 100, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_food` VALUES (3, '灵兽丹', '蕴含灵气的食物', 'PREMIUM', 100, 20, 50, 2, 300, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_food` VALUES (4, '仙兽丹', '高级宠物食物', 'PREMIUM', 150, 30, 100, 3, 800, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_food` VALUES (5, '龙凤呈祥', '传说中的宠物食物', 'SPECIAL', 200, 50, 200, 4, 3000, 1, '2026-03-27 14:43:21');
INSERT INTO `pet_food` VALUES (6, '月华露', '月光精华凝成', 'SPECIAL', 120, 40, 150, 3, 1500, 1, '2026-03-27 14:43:21');

-- ----------------------------
-- Table structure for pet_skills
-- ----------------------------
DROP TABLE IF EXISTS `pet_skills`;
CREATE TABLE `pet_skills`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '宠物技能ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '技能描述',
  `skill_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能类型',
  `base_damage` double NOT NULL DEFAULT 0 COMMENT '基础伤害',
  `damage_multiplier` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '伤害倍率',
  `cooldown` int NOT NULL DEFAULT 0 COMMENT '冷却时间',
  `energy_cost` int NOT NULL DEFAULT 0 COMMENT '能量消耗',
  `unlock_pet_level` int NOT NULL DEFAULT 1 COMMENT '宠物解锁等级',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_skill_type`(`skill_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物技能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_skills
-- ----------------------------
INSERT INTO `pet_skills` VALUES (1, '撕咬', '基础物理攻击', '攻击', 20, 1.00, 3, 10, 1, 1, '2026-03-27 14:43:21', '2026-03-27 14:43:21');
INSERT INTO `pet_skills` VALUES (2, '火焰吐息', '喷射火焰攻击敌人', '攻击', 50, 1.50, 8, 30, 10, 1, '2026-03-27 14:43:21', '2026-03-27 14:43:21');
INSERT INTO `pet_skills` VALUES (3, '雷霆一击', '召唤雷电攻击', '攻击', 80, 2.00, 12, 50, 20, 1, '2026-03-27 14:43:21', '2026-03-27 14:43:21');
INSERT INTO `pet_skills` VALUES (4, '治愈之光', '恢复主人生命值', '辅助', 0, 0.00, 15, 40, 15, 1, '2026-03-27 14:43:21', '2026-03-27 14:43:21');
INSERT INTO `pet_skills` VALUES (5, '护盾', '为主人提供护盾', '防御', 0, 0.00, 20, 35, 12, 1, '2026-03-27 14:43:21', '2026-03-27 14:43:21');

-- ----------------------------
-- Table structure for pet_statistics
-- ----------------------------
DROP TABLE IF EXISTS `pet_statistics`;
CREATE TABLE `pet_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_pets_owned` int NOT NULL DEFAULT 0 COMMENT '拥有宠物数',
  `total_pets_max_level` int NOT NULL DEFAULT 0 COMMENT '满级宠物数',
  `total_pets_evolved` int NOT NULL DEFAULT 0 COMMENT '进化宠物数',
  `highest_pet_level` int NOT NULL DEFAULT 0 COMMENT '最高等级',
  `rarest_pet_rarity` int NOT NULL DEFAULT 0 COMMENT '最高稀有度',
  `total_pet_battles` int NOT NULL DEFAULT 0 COMMENT '宠物总战斗次数',
  `total_pet_wins` int NOT NULL DEFAULT 0 COMMENT '宠物总胜利次数',
  `favorite_pet_id` int NULL DEFAULT NULL COMMENT '最爱宠物',
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_pet_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for pet_training_logs
-- ----------------------------
DROP TABLE IF EXISTS `pet_training_logs`;
CREATE TABLE `pet_training_logs`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '训练日志ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `training_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '训练类型',
  `exp_gained` int NOT NULL DEFAULT 0 COMMENT '获得经验',
  `attribute_improved` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提升的属性',
  `improvement_value` int NOT NULL DEFAULT 0 COMMENT '提升值',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_pet_id`(`player_pet_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_pet_training_logs_player_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物训练日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_training_logs
-- ----------------------------

-- ----------------------------
-- Table structure for pets
-- ----------------------------
DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '宠物名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '宠物描述',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '宠物类型',
  `rarity` int NOT NULL DEFAULT 1 COMMENT '稀有度',
  `base_attack` int NOT NULL DEFAULT 0 COMMENT '基础攻击力',
  `base_defense` int NOT NULL DEFAULT 0 COMMENT '基础防御力',
  `base_health` int NOT NULL DEFAULT 0 COMMENT '基础生命值',
  `base_speed` int NOT NULL DEFAULT 0 COMMENT '基础速度',
  `growth_rate` decimal(10, 2) NOT NULL DEFAULT 1.00 COMMENT '成长率',
  `skill_id` int NULL DEFAULT NULL COMMENT '宠物技能ID',
  `unlock_level` int NOT NULL DEFAULT 1 COMMENT '解锁等级',
  `capture_rate` decimal(5, 2) NOT NULL DEFAULT 50.00 COMMENT '捕获概率',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_type`(`type` ASC) USING BTREE,
  INDEX `idx_pet_rarity`(`rarity` ASC) USING BTREE,
  INDEX `idx_pet_unlock_level`(`unlock_level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pets
-- ----------------------------
INSERT INTO `pets` VALUES (1, '灵狐', '修炼千年的灵狐，擅长速度和敏捷', '灵兽', 2, 15, 10, 150, 25, 1.20, NULL, 10, 60.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');
INSERT INTO `pets` VALUES (2, '火麒麟', '传说中的神兽，拥有强大的火焰之力', '神兽', 4, 50, 40, 500, 20, 1.50, NULL, 50, 10.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');
INSERT INTO `pets` VALUES (3, '青龙', '四大神兽之一，掌控风雷之力', '神兽', 5, 80, 60, 800, 30, 2.00, NULL, 100, 5.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');
INSERT INTO `pets` VALUES (4, '白虎', '四大神兽之一，拥有无匹的攻击力', '神兽', 5, 100, 50, 700, 35, 2.00, NULL, 100, 5.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');
INSERT INTO `pets` VALUES (5, '玄武', '四大神兽之一，防御力惊人', '神兽', 5, 40, 100, 1000, 15, 2.00, NULL, 100, 5.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');
INSERT INTO `pets` VALUES (6, '朱雀', '四大神兽之一，掌控火焰', '神兽', 5, 90, 50, 750, 40, 2.00, NULL, 100, 5.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');
INSERT INTO `pets` VALUES (7, '小灵猫', '可爱的灵猫，适合新手培养', '灵兽', 1, 8, 5, 100, 20, 1.00, NULL, 1, 80.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');
INSERT INTO `pets` VALUES (8, '雷鹰', '掌控雷电的猛禽', '妖兽', 3, 35, 20, 300, 45, 1.30, NULL, 30, 30.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');
INSERT INTO `pets` VALUES (9, '冰霜狼', '来自极北之地的冰狼', '妖兽', 3, 30, 25, 350, 30, 1.30, NULL, 25, 35.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');
INSERT INTO `pets` VALUES (10, '金翅大鹏', '速度极快的神鸟', '神兽', 4, 60, 35, 600, 50, 1.60, NULL, 70, 15.00, NULL, 1, '2026-03-27 14:43:20', '2026-03-27 14:43:20');

-- ----------------------------
-- Table structure for player_achievements
-- ----------------------------
DROP TABLE IF EXISTS `player_achievements`;
CREATE TABLE `player_achievements`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家成就ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `achievement_id` int NOT NULL COMMENT '成就ID',
  `progress` int NOT NULL DEFAULT 0 COMMENT '当前进度',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `is_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否领取奖励',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `claimed_at` timestamp NULL DEFAULT NULL COMMENT '领取时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_achievement`(`player_id` ASC, `achievement_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_completed`(`is_completed` ASC) USING BTREE,
  INDEX `fk_player_achievements_achievement`(`achievement_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_achievements_achievement` FOREIGN KEY (`achievement_id`) REFERENCES `achievements` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_achievements_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家成就表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_achievements
-- ----------------------------

-- ----------------------------
-- Table structure for player_activities
-- ----------------------------
DROP TABLE IF EXISTS `player_activities`;
CREATE TABLE `player_activities`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家活动ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `activity_id` int NOT NULL COMMENT '活动ID',
  `progress` json NULL COMMENT '进度数据',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `is_rewarded` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否领取奖励',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `rewarded_at` timestamp NULL DEFAULT NULL COMMENT '奖励领取时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_activity`(`player_id` ASC, `activity_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_activities_activity` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_activities_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_activities
-- ----------------------------

-- ----------------------------
-- Table structure for player_activity_progress
-- ----------------------------
DROP TABLE IF EXISTS `player_activity_progress`;
CREATE TABLE `player_activity_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `activity_id` int NOT NULL COMMENT '活动ID',
  `progress` int NOT NULL DEFAULT 0 COMMENT '进度值',
  `score` int NOT NULL DEFAULT 0 COMMENT '积分',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_activity_progress`(`player_id` ASC, `activity_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_activity_progress_activity` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_activity_progress_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家活动进度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_activity_progress
-- ----------------------------

-- ----------------------------
-- Table structure for player_bounty_quests
-- ----------------------------
DROP TABLE IF EXISTS `player_bounty_quests`;
CREATE TABLE `player_bounty_quests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家悬赏ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `bounty_id` int NOT NULL COMMENT '悬赏ID',
  `current_kills` int NOT NULL DEFAULT 0 COMMENT '当前击杀数',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `is_reward_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '奖励是否领取',
  `accepted_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接取时间',
  `expire_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_bounty_id`(`bounty_id` ASC) USING BTREE,
  INDEX `idx_is_completed`(`is_completed` ASC) USING BTREE,
  INDEX `idx_expire_at`(`expire_at` ASC) USING BTREE,
  CONSTRAINT `fk_player_bounty_quests_bounty` FOREIGN KEY (`bounty_id`) REFERENCES `bounty_quests` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_bounty_quests_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家悬赏任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_bounty_quests
-- ----------------------------

-- ----------------------------
-- Table structure for player_bundle_purchases
-- ----------------------------
DROP TABLE IF EXISTS `player_bundle_purchases`;
CREATE TABLE `player_bundle_purchases`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购买记录ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `bundle_id` int NOT NULL COMMENT '礼包ID',
  `purchase_count` int NOT NULL DEFAULT 1 COMMENT '购买次数',
  `last_purchase_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后购买时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_bundle`(`player_id` ASC, `bundle_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_bundle_id`(`bundle_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_bundle_purchases_bundle` FOREIGN KEY (`bundle_id`) REFERENCES `shop_bundles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_bundle_purchases_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家礼包购买记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_bundle_purchases
-- ----------------------------

-- ----------------------------
-- Table structure for player_check_ins
-- ----------------------------
DROP TABLE IF EXISTS `player_check_ins`;
CREATE TABLE `player_check_ins`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `check_in_date` datetime NOT NULL COMMENT '签到日期（当天00:00:00）',
  `consecutive_days` int NOT NULL DEFAULT 1 COMMENT '当时连续签到天数',
  `reward_spirit_stones` int NOT NULL DEFAULT 0 COMMENT '本次获得灵石',
  `reward_exp` int NOT NULL DEFAULT 0 COMMENT '本次获得经验',
  `is_makeup` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否补签',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_date`(`player_id` ASC, `check_in_date` ASC) USING BTREE,
  INDEX `idx_player_date`(`player_id` ASC, `check_in_date` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '玩家签到记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_check_ins
-- ----------------------------

-- ----------------------------
-- Table structure for player_combat_achievements
-- ----------------------------
DROP TABLE IF EXISTS `player_combat_achievements`;
CREATE TABLE `player_combat_achievements`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家成就ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `achievement_id` int NOT NULL COMMENT '成就ID',
  `progress` int NOT NULL DEFAULT 0 COMMENT '当前进度',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `is_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否领取奖励',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `claimed_at` timestamp NULL DEFAULT NULL COMMENT '领取时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_achievement`(`player_id` ASC, `achievement_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_achievement_id`(`achievement_id` ASC) USING BTREE,
  INDEX `idx_is_completed`(`is_completed` ASC) USING BTREE,
  CONSTRAINT `fk_player_combat_achievements_achievement` FOREIGN KEY (`achievement_id`) REFERENCES `combat_achievements` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_combat_achievements_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家战斗成就表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_combat_achievements
-- ----------------------------

-- ----------------------------
-- Table structure for player_combat_buffs
-- ----------------------------
DROP TABLE IF EXISTS `player_combat_buffs`;
CREATE TABLE `player_combat_buffs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家BUFF ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `buff_id` int NOT NULL COMMENT 'BUFF ID',
  `stacks` int NOT NULL DEFAULT 1 COMMENT '当前叠加层数',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源(装备/技能/道具)',
  `source_id` int NULL DEFAULT NULL COMMENT '来源ID',
  `expire_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_buff_id`(`buff_id` ASC) USING BTREE,
  INDEX `idx_expire_at`(`expire_at` ASC) USING BTREE,
  CONSTRAINT `fk_player_combat_buffs_buff` FOREIGN KEY (`buff_id`) REFERENCES `combat_buffs` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_combat_buffs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家战斗BUFF表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_combat_buffs
-- ----------------------------

-- ----------------------------
-- Table structure for player_dialogue_state
-- ----------------------------
DROP TABLE IF EXISTS `player_dialogue_state`;
CREATE TABLE `player_dialogue_state`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `dialogue_tree_id` int NOT NULL COMMENT '对话树ID',
  `current_node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前所在节点key',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '对话树是否已完成',
  `times_completed` int NOT NULL DEFAULT 0 COMMENT '完成次数',
  `last_choice_tag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一次选择的tag',
  `started_at` timestamp NULL DEFAULT NULL COMMENT '本次开始时间',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '本次完成时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_dialogue`(`player_id` ASC, `dialogue_tree_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_dialogue_tree_id`(`dialogue_tree_id` ASC) USING BTREE,
  INDEX `idx_is_completed`(`is_completed` ASC) USING BTREE,
  CONSTRAINT `fk_player_dialogue_state_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_dialogue_state_tree` FOREIGN KEY (`dialogue_tree_id`) REFERENCES `dialogue_trees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家对话状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_dialogue_state
-- ----------------------------

-- ----------------------------
-- Table structure for player_dungeon_progress
-- ----------------------------
DROP TABLE IF EXISTS `player_dungeon_progress`;
CREATE TABLE `player_dungeon_progress`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `dungeon_id` int NOT NULL COMMENT '副本ID',
  `is_unlocked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否解锁',
  `is_cleared` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否通关',
  `best_score` int NOT NULL DEFAULT 0 COMMENT '最佳分数',
  `fastest_clear_time` int NULL DEFAULT NULL COMMENT '最快通关时间(秒)',
  `total_clears` int NOT NULL DEFAULT 0 COMMENT '通关次数',
  `daily_clears` int NOT NULL DEFAULT 0 COMMENT '今日通关次数',
  `last_clear_at` timestamp NULL DEFAULT NULL COMMENT '最后通关时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_dungeon`(`player_id` ASC, `dungeon_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_dungeon_id`(`dungeon_id` ASC) USING BTREE,
  INDEX `idx_is_cleared`(`is_cleared` ASC) USING BTREE,
  CONSTRAINT `fk_player_dungeon_progress_dungeon` FOREIGN KEY (`dungeon_id`) REFERENCES `dungeons` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_dungeon_progress_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家副本进度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_dungeon_progress
-- ----------------------------

-- ----------------------------
-- Table structure for player_equipment
-- ----------------------------
DROP TABLE IF EXISTS `player_equipment`;
CREATE TABLE `player_equipment`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家装备ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `equipment_id` int NOT NULL COMMENT '装备ID',
  `slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备槽位',
  `is_equipped` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否装备',
  `durability` int NOT NULL DEFAULT 100 COMMENT '当前耐久',
  `max_durability` int NOT NULL DEFAULT 100 COMMENT '最大耐久',
  `enhance_level` int NOT NULL DEFAULT 0 COMMENT '强化等级',
  `enhance_attack_bonus` int NOT NULL DEFAULT 0 COMMENT '强化攻击加成',
  `enhance_defense_bonus` int NOT NULL DEFAULT 0 COMMENT '强化防御加成',
  `enhance_health_bonus` int NOT NULL DEFAULT 0 COMMENT '强化生命加成',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_equipment_id`(`equipment_id` ASC) USING BTREE,
  INDEX `idx_slot`(`slot` ASC) USING BTREE,
  INDEX `idx_is_equipped`(`is_equipped` ASC) USING BTREE,
  CONSTRAINT `fk_player_equipment_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_equipment_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家装备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_equipment
-- ----------------------------

-- ----------------------------
-- Table structure for player_items
-- ----------------------------
DROP TABLE IF EXISTS `player_items`;
CREATE TABLE `player_items`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家物品ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `locked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否锁定',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_item`(`player_id` ASC, `item_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_items_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_items_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家物品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_items
-- ----------------------------

-- ----------------------------
-- Table structure for player_login_logs
-- ----------------------------
DROP TABLE IF EXISTS `player_login_logs`;
CREATE TABLE `player_login_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '登录日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理',
  `device_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备信息',
  `login_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_login_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_login_logs
-- ----------------------------

-- ----------------------------
-- Table structure for player_lore_collection
-- ----------------------------
DROP TABLE IF EXISTS `player_lore_collection`;
CREATE TABLE `player_lore_collection`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `lore_entry_id` int NOT NULL COMMENT '传说条目ID',
  `discovered_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
  `source` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发现来源',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_lore`(`player_id` ASC, `lore_entry_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_lore_entry_id`(`lore_entry_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_lore_collection_lore` FOREIGN KEY (`lore_entry_id`) REFERENCES `lore_entries` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_lore_collection_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家传说收集表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_lore_collection
-- ----------------------------

-- ----------------------------
-- Table structure for player_mails
-- ----------------------------
DROP TABLE IF EXISTS `player_mails`;
CREATE TABLE `player_mails`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '邮件ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮件标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮件内容',
  `mail_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮件类型：SYSTEM/REWARD/ACTIVITY',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `has_attachment` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否有附件',
  `is_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '附件是否已领取',
  `expire_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_mail_type`(`mail_type` ASC) USING BTREE,
  INDEX `idx_has_attachment`(`has_attachment` ASC) USING BTREE,
  INDEX `idx_is_claimed`(`is_claimed` ASC) USING BTREE,
  INDEX `idx_expire_at`(`expire_at` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_player_mails_unread`(`player_id` ASC, `is_read` ASC, `created_at` DESC) USING BTREE,
  INDEX `idx_player_mails_player_time`(`player_id` ASC, `created_at` DESC) USING BTREE,
  CONSTRAINT `fk_player_mails_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家邮件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_mails
-- ----------------------------

-- ----------------------------
-- Table structure for player_map_progress
-- ----------------------------
DROP TABLE IF EXISTS `player_map_progress`;
CREATE TABLE `player_map_progress`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `map_id` int NOT NULL COMMENT '地图ID',
  `is_unlocked` tinyint(1) NULL DEFAULT 0 COMMENT '是否已解锁: 0-否 1-是',
  `is_current` tinyint(1) NULL DEFAULT 0 COMMENT '是否为当前所在地图: 0-否 1-是',
  `first_enter_at` datetime NULL DEFAULT NULL COMMENT '首次进入时间',
  `last_enter_at` datetime NULL DEFAULT NULL COMMENT '最后进入时间',
  `total_kills` int NULL DEFAULT 0 COMMENT '累计击杀数',
  `total_time_spent` int NULL DEFAULT 0 COMMENT '累计停留时间(分钟)',
  `offline_start_at` datetime NULL DEFAULT NULL COMMENT '离线挂机开始时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_map_id`(`map_id` ASC) USING BTREE,
  INDEX `idx_player_map`(`player_id` ASC, `map_id` ASC) USING BTREE,
  INDEX `idx_is_current`(`player_id` ASC, `is_current` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家地图进度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_map_progress
-- ----------------------------

-- ----------------------------
-- Table structure for player_narrative_flags
-- ----------------------------
DROP TABLE IF EXISTS `player_narrative_flags`;
CREATE TABLE `player_narrative_flags`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `flag_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'flag键名',
  `flag_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '1' COMMENT 'flag值',
  `source` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源描述',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_flag`(`player_id` ASC, `flag_key` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_flag_key`(`flag_key` ASC) USING BTREE,
  CONSTRAINT `fk_player_narrative_flags_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家叙事标记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_narrative_flags
-- ----------------------------

-- ----------------------------
-- Table structure for player_npc_relations
-- ----------------------------
DROP TABLE IF EXISTS `player_npc_relations`;
CREATE TABLE `player_npc_relations`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `npc_id` int NOT NULL COMMENT 'NPC ID',
  `affinity` int NOT NULL DEFAULT 0 COMMENT '好感度(-100~100)',
  `relationship_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '陌生' COMMENT '关系等级：陌生/认识/熟悉/信任/至交',
  `first_met_at` timestamp NULL DEFAULT NULL COMMENT '初次见面时间',
  `last_interact_at` timestamp NULL DEFAULT NULL COMMENT '最后互动时间',
  `total_interactions` int NOT NULL DEFAULT 0 COMMENT '总互动次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_npc`(`player_id` ASC, `npc_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_npc_id`(`npc_id` ASC) USING BTREE,
  INDEX `idx_affinity`(`affinity` ASC) USING BTREE,
  CONSTRAINT `fk_player_npc_relations_npc` FOREIGN KEY (`npc_id`) REFERENCES `npcs` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_npc_relations_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家-NPC好感度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_npc_relations
-- ----------------------------

-- ----------------------------
-- Table structure for player_passive_skills
-- ----------------------------
DROP TABLE IF EXISTS `player_passive_skills`;
CREATE TABLE `player_passive_skills`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家被动技能ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `passive_skill_id` int NOT NULL COMMENT '被动技能ID',
  `level` int NOT NULL DEFAULT 1 COMMENT '当前等级',
  `learned_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '学习时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_passive`(`player_id` ASC, `passive_skill_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_passive_skill_id`(`passive_skill_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_passive_skills_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_passive_skills_skill` FOREIGN KEY (`passive_skill_id`) REFERENCES `passive_skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家被动技能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_passive_skills
-- ----------------------------

-- ----------------------------
-- Table structure for player_pet_abilities
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_abilities`;
CREATE TABLE `player_pet_abilities`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家宠物能力ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `ability_id` int NOT NULL COMMENT '能力ID',
  `ability_level` int NOT NULL DEFAULT 1 COMMENT '能力等级',
  `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
  `cooldown_end` timestamp NULL DEFAULT NULL COMMENT '冷却结束时间',
  `unlocked_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_pet_ability`(`player_pet_id` ASC, `ability_id` ASC) USING BTREE,
  INDEX `idx_player_pet_id`(`player_pet_id` ASC) USING BTREE,
  INDEX `idx_ability_id`(`ability_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_pet_abilities_ability` FOREIGN KEY (`ability_id`) REFERENCES `pet_abilities` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_pet_abilities_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家宠物能力表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_pet_abilities
-- ----------------------------

-- ----------------------------
-- Table structure for player_pet_equipment
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_equipment`;
CREATE TABLE `player_pet_equipment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家宠物装备ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `equipment_id` int NOT NULL COMMENT '宠物装备ID',
  `slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '装备槽位',
  `is_equipped` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否装备',
  `enhance_level` int NOT NULL DEFAULT 0 COMMENT '强化等级',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_pet_id`(`player_pet_id` ASC) USING BTREE,
  INDEX `idx_equipment_id`(`equipment_id` ASC) USING BTREE,
  INDEX `idx_slot`(`slot` ASC) USING BTREE,
  CONSTRAINT `fk_player_pet_equipment_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `pet_equipment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_pet_equipment_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家宠物装备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_pet_equipment
-- ----------------------------

-- ----------------------------
-- Table structure for player_pet_evolution
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_evolution`;
CREATE TABLE `player_pet_evolution`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家进化ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `current_stage` int NOT NULL DEFAULT 1 COMMENT '当前进化阶段',
  `evolved_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '进化时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_pet_evolution`(`player_pet_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_pet_evolution_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家宠物进化表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_pet_evolution
-- ----------------------------

-- ----------------------------
-- Table structure for player_pet_food_usage
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_food_usage`;
CREATE TABLE `player_pet_food_usage`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '使用记录ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `food_id` int NOT NULL COMMENT '食物ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `hunger_restored` int NOT NULL DEFAULT 0 COMMENT '恢复饱食度',
  `loyalty_gained` int NOT NULL DEFAULT 0 COMMENT '获得忠诚度',
  `exp_gained` int NOT NULL DEFAULT 0 COMMENT '获得经验',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_pet_id`(`player_pet_id` ASC) USING BTREE,
  INDEX `idx_food_id`(`food_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_player_pet_food_usage_food` FOREIGN KEY (`food_id`) REFERENCES `pet_food` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_pet_food_usage_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物食物使用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_pet_food_usage
-- ----------------------------

-- ----------------------------
-- Table structure for player_pet_skills
-- ----------------------------
DROP TABLE IF EXISTS `player_pet_skills`;
CREATE TABLE `player_pet_skills`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家宠物技能ID',
  `player_pet_id` int NOT NULL COMMENT '玩家宠物ID',
  `pet_skill_id` int NOT NULL COMMENT '宠物技能ID',
  `skill_level` int NOT NULL DEFAULT 1 COMMENT '技能等级',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_pet_skill`(`player_pet_id` ASC, `pet_skill_id` ASC) USING BTREE,
  INDEX `idx_player_pet_id`(`player_pet_id` ASC) USING BTREE,
  INDEX `idx_pet_skill_id`(`pet_skill_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_pet_skills_pet_skill` FOREIGN KEY (`pet_skill_id`) REFERENCES `pet_skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_pet_skills_player_pet` FOREIGN KEY (`player_pet_id`) REFERENCES `player_pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家宠物技能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_pet_skills
-- ----------------------------

-- ----------------------------
-- Table structure for player_pets
-- ----------------------------
DROP TABLE IF EXISTS `player_pets`;
CREATE TABLE `player_pets`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家宠物ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `pet_id` int NOT NULL COMMENT '宠物模板ID',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '宠物昵称',
  `level` int NOT NULL DEFAULT 1 COMMENT '宠物等级',
  `exp` bigint NOT NULL DEFAULT 0 COMMENT '宠物经验',
  `exp_to_next` bigint NOT NULL DEFAULT 100 COMMENT '升级所需经验',
  `attack` int NOT NULL DEFAULT 0 COMMENT '当前攻击力',
  `defense` int NOT NULL DEFAULT 0 COMMENT '当前防御力',
  `health` int NOT NULL DEFAULT 0 COMMENT '当前生命值',
  `max_health` int NOT NULL DEFAULT 0 COMMENT '最大生命值',
  `speed` int NOT NULL DEFAULT 0 COMMENT '当前速度',
  `loyalty` int NOT NULL DEFAULT 50 COMMENT '忠诚度',
  `hunger` int NOT NULL DEFAULT 100 COMMENT '饱食度',
  `is_active` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否出战',
  `is_locked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否锁定',
  `total_battles` int NOT NULL DEFAULT 0 COMMENT '总战斗次数',
  `total_wins` int NOT NULL DEFAULT 0 COMMENT '总胜利次数',
  `last_feed_time` timestamp NULL DEFAULT NULL COMMENT '最后喂食时间',
  `last_train_time` timestamp NULL DEFAULT NULL COMMENT '最后训练时间',
  `captured_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捕获时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_pet_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_player_pet_pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `idx_player_pet_active`(`is_active` ASC) USING BTREE,
  INDEX `idx_player_pets_player_active`(`player_id` ASC, `is_active` ASC) USING BTREE,
  CONSTRAINT `fk_player_pets_pet` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_pets_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家宠物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_pets
-- ----------------------------

-- ----------------------------
-- Table structure for player_profiles
-- ----------------------------
DROP TABLE IF EXISTS `player_profiles`;
CREATE TABLE `player_profiles`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `level` int NOT NULL DEFAULT 1 COMMENT '等级',
  `exp` bigint NOT NULL DEFAULT 0 COMMENT '当前经验',
  `exp_to_next` bigint NOT NULL DEFAULT 100 COMMENT '升级所需经验',
  `realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '练气期' COMMENT '境界',
  `cultivation_speed` decimal(10, 2) NOT NULL DEFAULT 1.00 COMMENT '修炼速度',
  `spirit_stones` bigint NOT NULL DEFAULT 2000 COMMENT '灵石',
  `cultivation_points` bigint NOT NULL DEFAULT 0 COMMENT '修炼点数',
  `contribution_points` bigint NOT NULL DEFAULT 0 COMMENT '贡献点',
  `attribute_points` int NOT NULL DEFAULT 0 COMMENT '属性点',
  `skill_points` int NOT NULL DEFAULT 0 COMMENT '技能点',
  `last_online_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后在线时间',
  `last_login_at` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `total_cultivation_time` bigint NOT NULL DEFAULT 0 COMMENT '总修炼时间(秒)',
  `is_cultivating` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否正在修炼',
  `last_cultivation_start` timestamp NULL DEFAULT NULL COMMENT '最后修炼开始时间',
  `last_cultivation_end` timestamp NULL DEFAULT NULL COMMENT '最后修炼结束时间',
  `attack` int NOT NULL DEFAULT 10 COMMENT '基础攻击力',
  `defense` int NOT NULL DEFAULT 5 COMMENT '基础防御力',
  `health` int NOT NULL DEFAULT 100 COMMENT '基础生命值',
  `mana` int NOT NULL DEFAULT 50 COMMENT '基础法力值',
  `speed` int NOT NULL DEFAULT 10 COMMENT '基础速度',
  `equipment_attack_bonus` int NOT NULL DEFAULT 0 COMMENT '装备攻击加成',
  `equipment_defense_bonus` int NOT NULL DEFAULT 0 COMMENT '装备防御加成',
  `equipment_health_bonus` int NOT NULL DEFAULT 0 COMMENT '装备生命加成',
  `equipment_mana_bonus` int NOT NULL DEFAULT 0 COMMENT '装备法力加成',
  `equipment_speed_bonus` int NOT NULL DEFAULT 0 COMMENT '装备速度加成',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_realm`(`realm` ASC) USING BTREE,
  INDEX `idx_last_online`(`last_online_time` ASC) USING BTREE,
  INDEX `idx_spirit_stones`(`spirit_stones` ASC) USING BTREE,
  INDEX `idx_last_login`(`last_login_at` ASC) USING BTREE,
  INDEX `idx_player_profiles_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_player_profiles_realm_level`(`realm` ASC, `level` DESC) USING BTREE,
  INDEX `idx_player_profiles_cultivate_time`(`total_cultivation_time` DESC) USING BTREE,
  CONSTRAINT `fk_player_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_profiles
-- ----------------------------

-- ----------------------------
-- Table structure for player_quest_chains
-- ----------------------------
DROP TABLE IF EXISTS `player_quest_chains`;
CREATE TABLE `player_quest_chains`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家任务链ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `chain_id` int NOT NULL COMMENT '任务链ID',
  `current_stage` int NOT NULL DEFAULT 1 COMMENT '当前阶段',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `is_reward_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '最终奖励是否领取',
  `started_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_chain`(`player_id` ASC, `chain_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_chain_id`(`chain_id` ASC) USING BTREE,
  INDEX `idx_is_completed`(`is_completed` ASC) USING BTREE,
  CONSTRAINT `fk_player_quest_chains_chain` FOREIGN KEY (`chain_id`) REFERENCES `quest_chains` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_quest_chains_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家任务链表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_quest_chains
-- ----------------------------

-- ----------------------------
-- Table structure for player_quest_objectives
-- ----------------------------
DROP TABLE IF EXISTS `player_quest_objectives`;
CREATE TABLE `player_quest_objectives`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家目标ID',
  `player_quest_id` int NOT NULL COMMENT '玩家任务ID',
  `objective_id` int NOT NULL COMMENT '目标ID',
  `current_amount` int NOT NULL DEFAULT 0 COMMENT '当前进度',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_quest_objective`(`player_quest_id` ASC, `objective_id` ASC) USING BTREE,
  INDEX `idx_player_quest_id`(`player_quest_id` ASC) USING BTREE,
  INDEX `idx_objective_id`(`objective_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_quest_objectives_objective` FOREIGN KEY (`objective_id`) REFERENCES `quest_objectives` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_quest_objectives_quest` FOREIGN KEY (`player_quest_id`) REFERENCES `player_quests` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家任务目标表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_quest_objectives
-- ----------------------------

-- ----------------------------
-- Table structure for player_quests
-- ----------------------------
DROP TABLE IF EXISTS `player_quests`;
CREATE TABLE `player_quests`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家任务ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `current_progress` int NOT NULL DEFAULT 0 COMMENT '当前进度',
  `completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `reward_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否领取奖励',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_quest`(`player_id` ASC, `quest_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_quest_id`(`quest_id` ASC) USING BTREE,
  INDEX `idx_completed`(`completed` ASC) USING BTREE,
  INDEX `idx_reward_claimed`(`reward_claimed` ASC) USING BTREE,
  INDEX `idx_player_quests_player_status`(`player_id` ASC, `completed` ASC, `quest_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_quests_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_quests_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_quests
-- ----------------------------

-- ----------------------------
-- Table structure for player_recharges
-- ----------------------------
DROP TABLE IF EXISTS `player_recharges`;
CREATE TABLE `player_recharges`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家充值ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `package_id` int NOT NULL COMMENT '套餐ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '充值金额',
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '交易ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SUCCESS' COMMENT '状态：PENDING/SUCCESS/FAILED',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_transaction_id`(`transaction_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_package_id`(`package_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_player_recharges_package` FOREIGN KEY (`package_id`) REFERENCES `recharge_packages` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_recharges_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家充值记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_recharges
-- ----------------------------

-- ----------------------------
-- Table structure for player_recipes
-- ----------------------------
DROP TABLE IF EXISTS `player_recipes`;
CREATE TABLE `player_recipes`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家配方ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `recipe_id` int NOT NULL COMMENT '配方ID',
  `learned_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '学习时间',
  `craft_count` int NOT NULL DEFAULT 0 COMMENT '制作次数',
  `mastery_level` int NOT NULL DEFAULT 1 COMMENT '熟练度等级',
  `mastery_exp` int NOT NULL DEFAULT 0 COMMENT '熟练度经验',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_recipe`(`player_id` ASC, `recipe_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_recipe_id`(`recipe_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_recipes_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_recipes_recipe` FOREIGN KEY (`recipe_id`) REFERENCES `item_recipes` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家配方表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_recipes
-- ----------------------------

-- ----------------------------
-- Table structure for player_shop_limits
-- ----------------------------
DROP TABLE IF EXISTS `player_shop_limits`;
CREATE TABLE `player_shop_limits`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '限购记录ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `limited_item_id` int NOT NULL COMMENT '限量商品ID',
  `purchased_today` int NOT NULL DEFAULT 0 COMMENT '今日已购',
  `purchased_total` int NOT NULL DEFAULT 0 COMMENT '总购买数',
  `last_purchase_at` timestamp NULL DEFAULT NULL COMMENT '最后购买时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_limited`(`player_id` ASC, `limited_item_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_limited_item_id`(`limited_item_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_shop_limits_item` FOREIGN KEY (`limited_item_id`) REFERENCES `shop_limited_items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_shop_limits_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家限购记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_shop_limits
-- ----------------------------

-- ----------------------------
-- Table structure for player_sign_ins
-- ----------------------------
DROP TABLE IF EXISTS `player_sign_ins`;
CREATE TABLE `player_sign_ins`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家签到ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `sign_in_date` date NOT NULL COMMENT '签到日期',
  `config_id` int NOT NULL COMMENT '签到配置ID',
  `is_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否领取',
  `claimed_at` timestamp NULL DEFAULT NULL COMMENT '领取时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_date`(`player_id` ASC, `sign_in_date` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_sign_in_date`(`sign_in_date` ASC) USING BTREE,
  INDEX `fk_player_sign_ins_config`(`config_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_sign_ins_config` FOREIGN KEY (`config_id`) REFERENCES `sign_in_configs` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_sign_ins_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家签到表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_sign_ins
-- ----------------------------

-- ----------------------------
-- Table structure for player_skill_combo_records
-- ----------------------------
DROP TABLE IF EXISTS `player_skill_combo_records`;
CREATE TABLE `player_skill_combo_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `used_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  `triggered_combo` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否触发连招',
  `combo_id` int NULL DEFAULT NULL COMMENT '触发的连招ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_used_at`(`used_at` ASC) USING BTREE,
  CONSTRAINT `fk_skill_combo_records_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家技能连招记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_skill_combo_records
-- ----------------------------

-- ----------------------------
-- Table structure for player_skill_cooldowns
-- ----------------------------
DROP TABLE IF EXISTS `player_skill_cooldowns`;
CREATE TABLE `player_skill_cooldowns`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '冷却ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `expire_at` timestamp NOT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_skill`(`player_id` ASC, `skill_id` ASC) USING BTREE,
  INDEX `idx_expire_at`(`expire_at` ASC) USING BTREE,
  INDEX `fk_player_skill_cooldowns_skill`(`skill_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_skill_cooldowns_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_skill_cooldowns_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家技能冷却表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_skill_cooldowns
-- ----------------------------

-- ----------------------------
-- Table structure for player_skill_enhancements
-- ----------------------------
DROP TABLE IF EXISTS `player_skill_enhancements`;
CREATE TABLE `player_skill_enhancements`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家强化ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `enhancement_id` int NOT NULL COMMENT '强化ID',
  `applied_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '应用时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_enhancement`(`player_id` ASC, `enhancement_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_enhancement_id`(`enhancement_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_skill_enhancements_enhancement` FOREIGN KEY (`enhancement_id`) REFERENCES `skill_enhancements` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_skill_enhancements_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家技能强化表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_skill_enhancements
-- ----------------------------

-- ----------------------------
-- Table structure for player_skill_mastery
-- ----------------------------
DROP TABLE IF EXISTS `player_skill_mastery`;
CREATE TABLE `player_skill_mastery`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '玩家熟练度ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `mastery_level` int NOT NULL DEFAULT 1 COMMENT '当前熟练度等级',
  `mastery_exp` int NOT NULL DEFAULT 0 COMMENT '当前熟练度经验',
  `total_uses` int NOT NULL DEFAULT 0 COMMENT '总使用次数',
  `last_used_at` timestamp NULL DEFAULT NULL COMMENT '最后使用时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_skill_mastery`(`player_id` ASC, `skill_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_skill_id`(`skill_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_skill_mastery_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_skill_mastery_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家技能熟练度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_skill_mastery
-- ----------------------------

-- ----------------------------
-- Table structure for player_skills
-- ----------------------------
DROP TABLE IF EXISTS `player_skills`;
CREATE TABLE `player_skills`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家技能ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `level` int NOT NULL DEFAULT 1 COMMENT '等级',
  `experience` int NOT NULL DEFAULT 0 COMMENT '经验值',
  `equipped` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否装备',
  `slot_number` int NOT NULL DEFAULT 0 COMMENT '装备槽位',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_skill`(`player_id` ASC, `skill_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_skill_id`(`skill_id` ASC) USING BTREE,
  INDEX `idx_equipped`(`equipped` ASC) USING BTREE,
  INDEX `idx_player_skills_player_level`(`player_id` ASC, `level` DESC) USING BTREE,
  CONSTRAINT `fk_player_skills_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_player_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家技能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_skills
-- ----------------------------

-- ----------------------------
-- Table structure for player_vip
-- ----------------------------
DROP TABLE IF EXISTS `player_vip`;
CREATE TABLE `player_vip`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '玩家VIP ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `vip_level` int NOT NULL DEFAULT 0 COMMENT 'VIP等级',
  `total_recharge` int NOT NULL DEFAULT 0 COMMENT '累计充值金额',
  `yuanbao` int NOT NULL DEFAULT 0 COMMENT '元宝余额',
  `last_daily_reward_at` timestamp NULL DEFAULT NULL COMMENT '上次领取每日奖励时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_vip_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家VIP表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_vip
-- ----------------------------

-- ----------------------------
-- Table structure for player_vip_levels
-- ----------------------------
DROP TABLE IF EXISTS `player_vip_levels`;
CREATE TABLE `player_vip_levels`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'VIP等级ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `vip_level` int NOT NULL DEFAULT 0 COMMENT 'VIP等级',
  `vip_exp` int NOT NULL DEFAULT 0 COMMENT 'VIP经验',
  `vip_exp_to_next` int NOT NULL DEFAULT 1000 COMMENT '升级所需经验',
  `monthly_card_expire` timestamp NULL DEFAULT NULL COMMENT '月卡到期时间',
  `quarterly_card_expire` timestamp NULL DEFAULT NULL COMMENT '季卡到期时间',
  `yearly_card_expire` timestamp NULL DEFAULT NULL COMMENT '年卡到期时间',
  `premium_monthly_card_expire` timestamp NULL DEFAULT NULL COMMENT '至尊月卡到期时间',
  `daily_rewards_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '今日奖励是否领取',
  `last_daily_reward_date` date NULL DEFAULT NULL COMMENT '上次领取日常奖励日期',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_vip_levels_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家VIP等级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_vip_levels
-- ----------------------------

-- ----------------------------
-- Table structure for pvp_battles
-- ----------------------------
DROP TABLE IF EXISTS `pvp_battles`;
CREATE TABLE `pvp_battles`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'PVP战斗ID',
  `challenger_id` int NOT NULL COMMENT '挑战者ID',
  `defender_id` int NOT NULL COMMENT '防守者ID',
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结果：CHALLENGER_WIN/DEFENDER_WIN/DRAW/TIMEOUT',
  `rounds` int NOT NULL DEFAULT 0 COMMENT '回合数',
  `challenger_damage` bigint NOT NULL DEFAULT 0 COMMENT '挑战者伤害',
  `defender_damage` bigint NOT NULL DEFAULT 0 COMMENT '防守者伤害',
  `exp_reward` int NOT NULL DEFAULT 0 COMMENT '经验奖励',
  `spirit_stones_reward` int NOT NULL DEFAULT 0 COMMENT '灵石奖励',
  `ranking_change` int NOT NULL DEFAULT 0 COMMENT '排名变化',
  `battle_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '战斗详情(JSON)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_challenger_id`(`challenger_id` ASC) USING BTREE,
  INDEX `idx_defender_id`(`defender_id` ASC) USING BTREE,
  INDEX `idx_result`(`result` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_pvp_battles_challenger` FOREIGN KEY (`challenger_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_pvp_battles_defender` FOREIGN KEY (`defender_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'PVP战斗记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pvp_battles
-- ----------------------------

-- ----------------------------
-- Table structure for pvp_rankings
-- ----------------------------
DROP TABLE IF EXISTS `pvp_rankings`;
CREATE TABLE `pvp_rankings`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '排名ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `rank` int NOT NULL DEFAULT 0 COMMENT '排名',
  `score` int NOT NULL DEFAULT 1000 COMMENT '积分',
  `tier` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '青铜' COMMENT '段位：青铜/白银/黄金/铂金/钻石/王者',
  `wins` int NOT NULL DEFAULT 0 COMMENT '胜利次数',
  `losses` int NOT NULL DEFAULT 0 COMMENT '失败次数',
  `win_streak` int NOT NULL DEFAULT 0 COMMENT '连胜',
  `highest_rank` int NOT NULL DEFAULT 0 COMMENT '历史最高排名',
  `highest_score` int NOT NULL DEFAULT 1000 COMMENT '历史最高积分',
  `season_id` int NOT NULL DEFAULT 1 COMMENT '赛季ID',
  `last_battle_at` timestamp NULL DEFAULT NULL COMMENT '最后战斗时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_season`(`player_id` ASC, `season_id` ASC) USING BTREE,
  INDEX `idx_rank`(`rank` ASC) USING BTREE,
  INDEX `idx_score`(`score` ASC) USING BTREE,
  INDEX `idx_tier`(`tier` ASC) USING BTREE,
  INDEX `idx_season_id`(`season_id` ASC) USING BTREE,
  CONSTRAINT `fk_pvp_rankings_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'PVP排名表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pvp_rankings
-- ----------------------------

-- ----------------------------
-- Table structure for quest_chain_stages
-- ----------------------------
DROP TABLE IF EXISTS `quest_chain_stages`;
CREATE TABLE `quest_chain_stages`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '阶段ID',
  `chain_id` int NOT NULL COMMENT '任务链ID',
  `stage_number` int NOT NULL COMMENT '阶段编号',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `stage_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '阶段名称',
  `stage_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '阶段描述',
  `auto_progress` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否自动进行',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_chain_stage`(`chain_id` ASC, `stage_number` ASC) USING BTREE,
  INDEX `idx_chain_id`(`chain_id` ASC) USING BTREE,
  INDEX `idx_quest_id`(`quest_id` ASC) USING BTREE,
  CONSTRAINT `fk_quest_chain_stages_chain` FOREIGN KEY (`chain_id`) REFERENCES `quest_chains` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_quest_chain_stages_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务链阶段表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest_chain_stages
-- ----------------------------

-- ----------------------------
-- Table structure for quest_chains
-- ----------------------------
DROP TABLE IF EXISTS `quest_chains`;
CREATE TABLE `quest_chains`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '任务链ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务链名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务链描述',
  `chain_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：MAIN/SIDE/EVENT/GUILD',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `required_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '需求境界',
  `prerequisite_chain_id` int NULL DEFAULT NULL COMMENT '前置任务链ID',
  `total_stages` int NOT NULL DEFAULT 1 COMMENT '总阶段数',
  `final_reward_exp` int NOT NULL DEFAULT 0 COMMENT '最终经验奖励',
  `final_reward_spirit_stones` int NOT NULL DEFAULT 0 COMMENT '最终灵石奖励',
  `final_reward_item_id` int NULL DEFAULT NULL COMMENT '最终物品奖励',
  `final_reward_quantity` int NOT NULL DEFAULT 1 COMMENT '最终物品数量',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chain_type`(`chain_type` ASC) USING BTREE,
  INDEX `idx_required_level`(`required_level` ASC) USING BTREE,
  INDEX `fk_quest_chains_prerequisite`(`prerequisite_chain_id` ASC) USING BTREE,
  CONSTRAINT `fk_quest_chains_prerequisite` FOREIGN KEY (`prerequisite_chain_id`) REFERENCES `quest_chains` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务链表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest_chains
-- ----------------------------
INSERT INTO `quest_chains` VALUES (1, '初入仙途', '新手引导任务链', 'MAIN', 1, NULL, NULL, 5, 500, 200, 1, 10, NULL, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_chains` VALUES (2, '修炼之路', '基础修炼任务链', 'MAIN', 5, NULL, 1, 8, 1500, 800, 3, 5, NULL, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_chains` VALUES (3, '筑基之旅', '筑基相关任务链', 'MAIN', 10, '练气期五层', 2, 10, 5000, 2000, 4, 3, NULL, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_chains` VALUES (4, '妖兽猎人', '猎杀妖兽的任务链', 'SIDE', 8, NULL, NULL, 6, 2000, 1000, 7, 5, NULL, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_chains` VALUES (5, '寻宝之旅', '寻找宝物的任务链', 'SIDE', 15, '筑基期', NULL, 8, 8000, 5000, 8, 3, NULL, 1, '2026-03-27 14:43:17');

-- ----------------------------
-- Table structure for quest_logs
-- ----------------------------
DROP TABLE IF EXISTS `quest_logs`;
CREATE TABLE `quest_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动作：ACCEPT/PROGRESS/COMPLETE/ABANDON/EXPIRE',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详情',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_quest_id`(`quest_id` ASC) USING BTREE,
  INDEX `idx_action`(`action` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_quest_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_quest_logs_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest_logs
-- ----------------------------

-- ----------------------------
-- Table structure for quest_objectives
-- ----------------------------
DROP TABLE IF EXISTS `quest_objectives`;
CREATE TABLE `quest_objectives`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '目标ID',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `objective_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：KILL/COLLECT/TALK/EXPLORE/LEVEL/CULTIVATE',
  `target_id` int NULL DEFAULT NULL COMMENT '目标ID(怪物/物品/NPC)',
  `target_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标名称',
  `required_amount` int NOT NULL DEFAULT 1 COMMENT '需求数量',
  `objective_order` int NOT NULL DEFAULT 0 COMMENT '目标顺序',
  `is_optional` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可选',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '目标描述',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_quest_id`(`quest_id` ASC) USING BTREE,
  INDEX `idx_objective_type`(`objective_type` ASC) USING BTREE,
  CONSTRAINT `fk_quest_objectives_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务目标表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest_objectives
-- ----------------------------
INSERT INTO `quest_objectives` VALUES (1, 1, 'CULTIVATE', NULL, '修炼', 1, 1, 0, '完成一次修炼');
INSERT INTO `quest_objectives` VALUES (2, 2, 'COLLECT', 6, '灵石', 100, 1, 0, '收集100灵石');
INSERT INTO `quest_objectives` VALUES (3, 3, 'CULTIVATE', NULL, '修炼', 300, 1, 0, '累计修炼300秒');
INSERT INTO `quest_objectives` VALUES (4, 4, 'LEVEL', NULL, '等级', 1, 1, 0, '提升1级');
INSERT INTO `quest_objectives` VALUES (5, 5, 'CULTIVATE', NULL, '修炼', 10, 1, 0, '完成10次修炼');

-- ----------------------------
-- Table structure for quest_rewards
-- ----------------------------
DROP TABLE IF EXISTS `quest_rewards`;
CREATE TABLE `quest_rewards`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '奖励ID',
  `quest_id` int NOT NULL COMMENT '任务ID',
  `reward_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖励类型：EXP/SPIRIT_STONES/ITEM/EQUIPMENT/SKILL_POINT/CONTRIBUTION',
  `reward_id` int NULL DEFAULT NULL COMMENT '奖励ID(物品/装备)',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `is_optional` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可选奖励',
  `reward_order` int NOT NULL DEFAULT 0 COMMENT '奖励顺序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_quest_id`(`quest_id` ASC) USING BTREE,
  INDEX `idx_reward_type`(`reward_type` ASC) USING BTREE,
  CONSTRAINT `fk_quest_rewards_quest` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务奖励表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest_rewards
-- ----------------------------
INSERT INTO `quest_rewards` VALUES (1, 1, 'EXP', NULL, 100, 0, 1);
INSERT INTO `quest_rewards` VALUES (2, 1, 'SPIRIT_STONES', NULL, 50, 0, 2);
INSERT INTO `quest_rewards` VALUES (3, 1, 'CONTRIBUTION', NULL, 10, 0, 3);
INSERT INTO `quest_rewards` VALUES (4, 2, 'EXP', NULL, 120, 0, 1);
INSERT INTO `quest_rewards` VALUES (5, 2, 'SPIRIT_STONES', NULL, 80, 0, 2);
INSERT INTO `quest_rewards` VALUES (6, 2, 'CONTRIBUTION', NULL, 12, 0, 3);
INSERT INTO `quest_rewards` VALUES (7, 3, 'EXP', NULL, 800, 0, 1);
INSERT INTO `quest_rewards` VALUES (8, 3, 'SPIRIT_STONES', NULL, 500, 0, 2);
INSERT INTO `quest_rewards` VALUES (9, 3, 'CONTRIBUTION', NULL, 50, 0, 3);
INSERT INTO `quest_rewards` VALUES (10, 4, 'EXP', NULL, 1000, 0, 1);
INSERT INTO `quest_rewards` VALUES (11, 4, 'SPIRIT_STONES', NULL, 600, 0, 2);
INSERT INTO `quest_rewards` VALUES (12, 4, 'CONTRIBUTION', NULL, 60, 0, 3);
INSERT INTO `quest_rewards` VALUES (13, 5, 'EXP', NULL, 3000, 0, 1);
INSERT INTO `quest_rewards` VALUES (14, 5, 'SPIRIT_STONES', NULL, 2000, 0, 2);
INSERT INTO `quest_rewards` VALUES (15, 5, 'CONTRIBUTION', NULL, 200, 0, 3);

-- ----------------------------
-- Table structure for quest_statistics
-- ----------------------------
DROP TABLE IF EXISTS `quest_statistics`;
CREATE TABLE `quest_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_quests_completed` int NOT NULL DEFAULT 0 COMMENT '总完成任务数',
  `main_quests_completed` int NOT NULL DEFAULT 0 COMMENT '主线任务完成数',
  `side_quests_completed` int NOT NULL DEFAULT 0 COMMENT '支线任务完成数',
  `daily_quests_completed` int NOT NULL DEFAULT 0 COMMENT '日常任务完成数',
  `weekly_quests_completed` int NOT NULL DEFAULT 0 COMMENT '周常任务完成数',
  `bounty_quests_completed` int NOT NULL DEFAULT 0 COMMENT '悬赏任务完成数',
  `total_exp_earned` bigint NOT NULL DEFAULT 0 COMMENT '总经验获得',
  `total_spirit_stones_earned` bigint NOT NULL DEFAULT 0 COMMENT '总灵石获得',
  `current_daily_streak` int NOT NULL DEFAULT 0 COMMENT '当前日常连续天数',
  `max_daily_streak` int NOT NULL DEFAULT 0 COMMENT '最大日常连续天数',
  `last_quest_completed_at` timestamp NULL DEFAULT NULL COMMENT '最后完成任务时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_quest_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for quest_templates
-- ----------------------------
DROP TABLE IF EXISTS `quest_templates`;
CREATE TABLE `quest_templates`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `quest_type_id` int NOT NULL COMMENT '任务类型ID',
  `title_template` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题模板',
  `description_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述模板',
  `objective_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标模板',
  `reward_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖励模板',
  `level_range_min` int NOT NULL DEFAULT 1 COMMENT '等级范围最小值',
  `level_range_max` int NOT NULL DEFAULT 100 COMMENT '等级范围最大值',
  `weight` int NOT NULL DEFAULT 100 COMMENT '生成权重',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_quest_type_id`(`quest_type_id` ASC) USING BTREE,
  INDEX `idx_level_range`(`level_range_min` ASC, `level_range_max` ASC) USING BTREE,
  CONSTRAINT `fk_quest_templates_type` FOREIGN KEY (`quest_type_id`) REFERENCES `quest_types` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest_templates
-- ----------------------------

-- ----------------------------
-- Table structure for quest_types
-- ----------------------------
DROP TABLE IF EXISTS `quest_types`;
CREATE TABLE `quest_types`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '类型描述',
  `daily_limit` int NULL DEFAULT NULL COMMENT '每日限制',
  `repeatable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可重复',
  `auto_accept` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动接取',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest_types
-- ----------------------------
INSERT INTO `quest_types` VALUES (1, '主线任务', 'main', '推动剧情发展的主要任务', 1, 0, 1, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_types` VALUES (2, '支线任务', 'side', '可选的额外任务', NULL, 0, 0, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_types` VALUES (3, '日常任务', 'daily', '每日可重复的任务', 10, 1, 1, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_types` VALUES (4, '周常任务', 'weekly', '每周可重复的任务', 5, 1, 1, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_types` VALUES (5, '月常任务', 'monthly', '每月可重复的任务', 3, 1, 1, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_types` VALUES (6, '成就任务', 'achievement', '达成特定成就的任务', 1, 0, 0, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_types` VALUES (7, '活动任务', 'event', '活动期间的特殊任务', NULL, 1, 0, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_types` VALUES (8, '宗门任务', 'guild', '宗门相关的任务', 5, 1, 0, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_types` VALUES (9, '悬赏任务', 'bounty', '击杀特定怪物的任务', 3, 1, 0, 1, '2026-03-27 14:43:17');
INSERT INTO `quest_types` VALUES (10, '探索任务', 'explore', '探索特定区域的任务', NULL, 0, 0, 1, '2026-03-27 14:43:17');

-- ----------------------------
-- Table structure for quests
-- ----------------------------
DROP TABLE IF EXISTS `quests`;
CREATE TABLE `quests`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务描述',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型',
  `required_amount` int NOT NULL DEFAULT 1 COMMENT '需求数量',
  `reward_exp` int NOT NULL DEFAULT 0 COMMENT '奖励经验',
  `reward_spirit_stones` int NOT NULL DEFAULT 0 COMMENT '奖励灵石',
  `reward_contribution_points` int NOT NULL DEFAULT 0 COMMENT '奖励贡献点',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_quest_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quests
-- ----------------------------
INSERT INTO `quests` VALUES (1, '每日修炼', '完成一次修炼', 'DAILY', 1, 100, 50, 10, '2026-03-27 14:43:16', '2026-03-27 14:43:16');
INSERT INTO `quests` VALUES (2, '每日收集灵石', '获得100灵石', 'DAILY', 100, 120, 80, 12, '2026-03-27 14:43:16', '2026-03-27 14:43:16');
INSERT INTO `quests` VALUES (3, '每周修炼进度', '累计修炼300秒', 'WEEKLY', 300, 800, 500, 50, '2026-03-27 14:43:16', '2026-03-27 14:43:16');
INSERT INTO `quests` VALUES (4, '每周升级一次', '提升1级', 'WEEKLY', 1, 1000, 600, 60, '2026-03-27 14:43:16', '2026-03-27 14:43:16');
INSERT INTO `quests` VALUES (5, '每月突破境界', '完成10次修炼', 'MONTHLY', 10, 3000, 2000, 200, '2026-03-27 14:43:16', '2026-03-27 14:43:16');

-- ----------------------------
-- Table structure for rankings
-- ----------------------------
DROP TABLE IF EXISTS `rankings`;
CREATE TABLE `rankings`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排行ID',
  `ranking_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '排行榜类型：LEVEL/POWER/WEALTH/PET',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `rank` int NOT NULL COMMENT '排名',
  `score` bigint NOT NULL COMMENT '分数',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_player`(`ranking_type` ASC, `player_id` ASC) USING BTREE,
  INDEX `idx_type_rank`(`ranking_type` ASC, `rank` ASC) USING BTREE,
  INDEX `idx_type_score`(`ranking_type` ASC, `score` ASC) USING BTREE,
  INDEX `idx_rankings_type_score`(`ranking_type` ASC, `score` DESC) USING BTREE,
  INDEX `idx_rankings_type_rank`(`ranking_type` ASC, `rank` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '排行榜缓存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rankings
-- ----------------------------

-- ----------------------------
-- Table structure for recharge_packages
-- ----------------------------
DROP TABLE IF EXISTS `recharge_packages`;
CREATE TABLE `recharge_packages`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '充值套餐ID',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '套餐描述',
  `amount` decimal(10, 2) NOT NULL COMMENT '充值金额',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
  `product_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_is_active`(`is_active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '充值套餐表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recharge_packages
-- ----------------------------
INSERT INTO `recharge_packages` VALUES (1, '月卡', '30天月卡', 30.00, 'CNY', 'monthly_card', 1, 1, '2026-03-27 14:43:23', '2026-03-27 14:43:23');
INSERT INTO `recharge_packages` VALUES (2, '季卡', '90天季卡', 80.00, 'CNY', 'quarterly_card', 2, 1, '2026-03-27 14:43:23', '2026-03-27 14:43:23');
INSERT INTO `recharge_packages` VALUES (3, '年卡', '365天年卡', 300.00, 'CNY', 'yearly_card', 3, 1, '2026-03-27 14:43:23', '2026-03-27 14:43:23');
INSERT INTO `recharge_packages` VALUES (4, '至尊月卡', '特权月卡', 50.00, 'CNY', 'premium_monthly_card', 4, 1, '2026-03-27 14:43:23', '2026-03-27 14:43:23');

-- ----------------------------
-- Table structure for recharge_records
-- ----------------------------
DROP TABLE IF EXISTS `recharge_records`;
CREATE TABLE `recharge_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '充值ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `amount` int NOT NULL COMMENT '充值金额（分）',
  `yuanbao` int NOT NULL COMMENT '获得元宝',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/SUCCESS/FAILED',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_recharge_records_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '充值记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recharge_records
-- ----------------------------

-- ----------------------------
-- Table structure for recipe_materials
-- ----------------------------
DROP TABLE IF EXISTS `recipe_materials`;
CREATE TABLE `recipe_materials`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '材料ID',
  `recipe_id` int NOT NULL COMMENT '配方ID',
  `item_id` int NOT NULL COMMENT '材料物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '需求数量',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_recipe_material`(`recipe_id` ASC, `item_id` ASC) USING BTREE,
  INDEX `idx_recipe_id`(`recipe_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  CONSTRAINT `fk_recipe_materials_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_recipe_materials_recipe` FOREIGN KEY (`recipe_id`) REFERENCES `item_recipes` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '配方材料表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe_materials
-- ----------------------------
INSERT INTO `recipe_materials` VALUES (1, 1, 5, 2);
INSERT INTO `recipe_materials` VALUES (2, 2, 5, 3);
INSERT INTO `recipe_materials` VALUES (3, 3, 8, 1);
INSERT INTO `recipe_materials` VALUES (4, 3, 5, 5);
INSERT INTO `recipe_materials` VALUES (5, 4, 8, 2);
INSERT INTO `recipe_materials` VALUES (6, 4, 7, 1);
INSERT INTO `recipe_materials` VALUES (7, 5, 5, 5);
INSERT INTO `recipe_materials` VALUES (8, 5, 6, 2);
INSERT INTO `recipe_materials` VALUES (9, 6, 5, 6);
INSERT INTO `recipe_materials` VALUES (10, 6, 6, 3);

-- ----------------------------
-- Table structure for shop_bundles
-- ----------------------------
DROP TABLE IF EXISTS `shop_bundles`;
CREATE TABLE `shop_bundles`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '礼包ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '礼包名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '礼包描述',
  `bundle_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '礼包类型：STARTER/DAILY/WEEKLY/MONTHLY/SPECIAL',
  `original_price` int NOT NULL COMMENT '原价',
  `sale_price` int NOT NULL COMMENT '售价',
  `currency_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'YUANBAO' COMMENT '货币类型',
  `contents` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容物(JSON)',
  `player_limit` int NULL DEFAULT NULL COMMENT '玩家限购次数',
  `daily_limit` int NULL DEFAULT NULL COMMENT '每日限购次数',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `required_vip` int NOT NULL DEFAULT 0 COMMENT '需求VIP',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_bundle_type`(`bundle_type` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城礼包表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_bundles
-- ----------------------------
INSERT INTO `shop_bundles` VALUES (1, '新手礼包', '包含新手必需品', 'STARTER', 500, 60, 'YUANBAO', '{\"spirit_stones\": 1000, \"items\": [{\"id\": 1, \"quantity\": 20}, {\"id\": 2, \"quantity\": 20}], \"equipment\": [{\"id\": 1, \"quantity\": 1}]}', 1, NULL, NULL, NULL, 1, 0, NULL, 1, 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `shop_bundles` VALUES (2, '每日修炼礼包', '每日限购的修炼资源', 'DAILY', 300, 30, 'YUANBAO', '{\"spirit_stones\": 500, \"items\": [{\"id\": 3, \"quantity\": 5}, {\"id\": 13, \"quantity\": 2}], \"exp\": 200}', NULL, 1, NULL, NULL, 10, 0, NULL, 2, 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `shop_bundles` VALUES (3, '每周豪华礼包', '超值每周礼包', 'WEEKLY', 1500, 300, 'YUANBAO', '{\"spirit_stones\": 3000, \"items\": [{\"id\": 4, \"quantity\": 1}, {\"id\": 15, \"quantity\": 1}], \"equipments\": [{\"id\": 12, \"rate\": 50}]}', NULL, 1, NULL, NULL, 20, 2, NULL, 3, 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');
INSERT INTO `shop_bundles` VALUES (4, '月度至尊礼包', '每月限购至尊礼包', 'MONTHLY', 5000, 980, 'YUANBAO', '{\"spirit_stones\": 10000, \"items\": [{\"id\": 36, \"quantity\": 1}, {\"id\": 37, \"quantity\": 1}], \"equipments\": [{\"id\": 24, \"quantity\": 1}], \"skill_points\": 5}', NULL, 1, NULL, NULL, 30, 4, NULL, 4, 1, '2026-03-27 14:43:19', '2026-03-27 14:43:19');

-- ----------------------------
-- Table structure for shop_categories
-- ----------------------------
DROP TABLE IF EXISTS `shop_categories`;
CREATE TABLE `shop_categories`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '分类描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `required_vip` int NOT NULL DEFAULT 0 COMMENT '需求VIP等级',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_categories
-- ----------------------------
INSERT INTO `shop_categories` VALUES (1, '普通商店', 'general', '出售基础物品', NULL, 1, 1, 0, 1, '2026-03-27 14:43:18');
INSERT INTO `shop_categories` VALUES (2, '装备商店', 'equipment', '出售各类装备', NULL, 2, 1, 0, 1, '2026-03-27 14:43:18');
INSERT INTO `shop_categories` VALUES (3, '丹药商店', 'pills', '出售各类丹药', NULL, 3, 5, 0, 1, '2026-03-27 14:43:18');
INSERT INTO `shop_categories` VALUES (4, '材料商店', 'materials', '出售各类材料', NULL, 4, 1, 0, 1, '2026-03-27 14:43:18');
INSERT INTO `shop_categories` VALUES (5, 'VIP商店', 'vip', 'VIP专属商店', NULL, 5, 1, 1, 1, '2026-03-27 14:43:18');
INSERT INTO `shop_categories` VALUES (6, '限时商店', 'limited', '限时特惠商品', NULL, 6, 10, 0, 1, '2026-03-27 14:43:18');
INSERT INTO `shop_categories` VALUES (7, '声望商店', 'reputation', '使用声望兑换', NULL, 7, 15, 0, 1, '2026-03-27 14:43:18');
INSERT INTO `shop_categories` VALUES (8, '活动商店', 'event', '活动专属商店', NULL, 8, 1, 0, 1, '2026-03-27 14:43:18');

-- ----------------------------
-- Table structure for shop_compare_list
-- ----------------------------
DROP TABLE IF EXISTS `shop_compare_list`;
CREATE TABLE `shop_compare_list`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '对比ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `added_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_shop_item`(`player_id` ASC, `shop_item_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `fk_shop_compare_list_item`(`shop_item_id` ASC) USING BTREE,
  CONSTRAINT `fk_shop_compare_list_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_shop_compare_list_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城对比列表表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_compare_list
-- ----------------------------

-- ----------------------------
-- Table structure for shop_discounts
-- ----------------------------
DROP TABLE IF EXISTS `shop_discounts`;
CREATE TABLE `shop_discounts`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '折扣ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `discount_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '折扣类型：PERCENTAGE/FIXED',
  `discount_value` decimal(10, 2) NOT NULL COMMENT '折扣值',
  `start_time` timestamp NOT NULL COMMENT '开始时间',
  `end_time` timestamp NOT NULL COMMENT '结束时间',
  `required_vip` int NOT NULL DEFAULT 0 COMMENT '需求VIP等级',
  `daily_limit` int NULL DEFAULT NULL COMMENT '每日限购',
  `total_limit` int NULL DEFAULT NULL COMMENT '总限购',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_item_id`(`shop_item_id` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_end_time`(`end_time` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE,
  CONSTRAINT `fk_shop_discounts_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城折扣表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_discounts
-- ----------------------------

-- ----------------------------
-- Table structure for shop_items
-- ----------------------------
DROP TABLE IF EXISTS `shop_items`;
CREATE TABLE `shop_items`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商店物品ID',
  `shop_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商店类型',
  `item_id` int NULL DEFAULT NULL COMMENT '物品ID',
  `equipment_id` int NULL DEFAULT NULL COMMENT '装备ID',
  `price` int NOT NULL COMMENT '价格',
  `price_spirit_stones` int NOT NULL DEFAULT 0 COMMENT '灵石价格',
  `price_contribution_points` int NOT NULL DEFAULT 0 COMMENT '贡献点价格',
  `stock` int NOT NULL COMMENT '库存',
  `is_available` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否可用',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_type`(`shop_type` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_equipment_id`(`equipment_id` ASC) USING BTREE,
  INDEX `idx_is_available`(`is_available` ASC) USING BTREE,
  CONSTRAINT `fk_shop_items_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_shop_items_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商店物品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_items
-- ----------------------------
INSERT INTO `shop_items` VALUES (1, 'general', 1, NULL, 50, 50, 0, 100, 1, '2026-03-27 14:43:18', '2026-03-27 14:43:18');
INSERT INTO `shop_items` VALUES (2, 'general', 2, NULL, 50, 50, 0, 100, 1, '2026-03-27 14:43:18', '2026-03-27 14:43:18');
INSERT INTO `shop_items` VALUES (3, 'general', 5, NULL, 10, 10, 0, 500, 1, '2026-03-27 14:43:18', '2026-03-27 14:43:18');
INSERT INTO `shop_items` VALUES (4, 'general', 6, NULL, 100, 100, 0, 100, 1, '2026-03-27 14:43:18', '2026-03-27 14:43:18');
INSERT INTO `shop_items` VALUES (5, 'equipment', NULL, 1, 100, 100, 0, 50, 1, '2026-03-27 14:43:18', '2026-03-27 14:43:18');
INSERT INTO `shop_items` VALUES (6, 'equipment', NULL, 2, 150, 150, 0, 50, 1, '2026-03-27 14:43:18', '2026-03-27 14:43:18');
INSERT INTO `shop_items` VALUES (7, 'equipment', NULL, 6, 500, 500, 0, 20, 1, '2026-03-27 14:43:18', '2026-03-27 14:43:18');
INSERT INTO `shop_items` VALUES (8, 'equipment', NULL, 12, 1200, 1200, 0, 10, 1, '2026-03-27 14:43:18', '2026-03-27 14:43:18');

-- ----------------------------
-- Table structure for shop_limited_items
-- ----------------------------
DROP TABLE IF EXISTS `shop_limited_items`;
CREATE TABLE `shop_limited_items`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '限量商品ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `total_stock` int NOT NULL COMMENT '总库存',
  `remaining_stock` int NOT NULL COMMENT '剩余库存',
  `player_daily_limit` int NOT NULL DEFAULT 1 COMMENT '玩家每日限购',
  `player_total_limit` int NOT NULL DEFAULT 1 COMMENT '玩家总限购',
  `start_time` timestamp NOT NULL COMMENT '开始时间',
  `end_time` timestamp NOT NULL COMMENT '结束时间',
  `refresh_on_soldout` tinyint(1) NOT NULL DEFAULT 0 COMMENT '售罄是否刷新',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_item_id`(`shop_item_id` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_end_time`(`end_time` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE,
  CONSTRAINT `fk_shop_limited_items_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城限量商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_limited_items
-- ----------------------------

-- ----------------------------
-- Table structure for shop_price_history
-- ----------------------------
DROP TABLE IF EXISTS `shop_price_history`;
CREATE TABLE `shop_price_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `old_price` int NOT NULL COMMENT '原价格',
  `new_price` int NOT NULL COMMENT '新价格',
  `change_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变更原因',
  `changed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_item_id`(`shop_item_id` ASC) USING BTREE,
  INDEX `idx_changed_at`(`changed_at` ASC) USING BTREE,
  CONSTRAINT `fk_shop_price_history_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城价格历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_price_history
-- ----------------------------

-- ----------------------------
-- Table structure for shop_purchase_logs
-- ----------------------------
DROP TABLE IF EXISTS `shop_purchase_logs`;
CREATE TABLE `shop_purchase_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购买日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '购买数量',
  `original_price` int NOT NULL COMMENT '原价',
  `discount_price` int NOT NULL COMMENT '折后价',
  `currency_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '货币类型：SPIRIT_STONES/CONTRIBUTION/YUANBAO',
  `discount_id` int NULL DEFAULT NULL COMMENT '折扣ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_shop_item_id`(`shop_item_id` ASC) USING BTREE,
  INDEX `idx_currency_type`(`currency_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_shop_purchase_logs_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_shop_purchase_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城购买日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_purchase_logs
-- ----------------------------

-- ----------------------------
-- Table structure for shop_recommendations
-- ----------------------------
DROP TABLE IF EXISTS `shop_recommendations`;
CREATE TABLE `shop_recommendations`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
  `shop_item_id` int NOT NULL COMMENT '商店物品ID',
  `recommend_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '推荐类型：HOT/NEW/SUGGESTED/FEATURED',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_item_id`(`shop_item_id` ASC) USING BTREE,
  INDEX `idx_recommend_type`(`recommend_type` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE,
  CONSTRAINT `fk_shop_recommendations_item` FOREIGN KEY (`shop_item_id`) REFERENCES `shop_items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城推荐表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_recommendations
-- ----------------------------

-- ----------------------------
-- Table structure for shop_refresh_config
-- ----------------------------
DROP TABLE IF EXISTS `shop_refresh_config`;
CREATE TABLE `shop_refresh_config`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `shop_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商店类型',
  `refresh_interval` int NOT NULL COMMENT '刷新间隔(小时)',
  `refresh_cost` int NOT NULL DEFAULT 0 COMMENT '手动刷新费用',
  `cost_currency` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SPIRIT_STONES' COMMENT '费用货币',
  `max_manual_refresh` int NOT NULL DEFAULT 3 COMMENT '每日最大手动刷新次数',
  `auto_refresh_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否自动刷新',
  `last_refresh_at` timestamp NULL DEFAULT NULL COMMENT '上次刷新时间',
  `next_refresh_at` timestamp NULL DEFAULT NULL COMMENT '下次刷新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_type`(`shop_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城刷新配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_refresh_config
-- ----------------------------
INSERT INTO `shop_refresh_config` VALUES (1, 'general', 24, 50, 'SPIRIT_STONES', 3, 1, NULL, NULL);
INSERT INTO `shop_refresh_config` VALUES (2, 'equipment', 24, 100, 'SPIRIT_STONES', 3, 1, NULL, NULL);
INSERT INTO `shop_refresh_config` VALUES (3, 'pills', 24, 80, 'SPIRIT_STONES', 3, 1, NULL, NULL);
INSERT INTO `shop_refresh_config` VALUES (4, 'limited', 12, 200, 'SPIRIT_STONES', 5, 1, NULL, NULL);
INSERT INTO `shop_refresh_config` VALUES (5, 'vip', 168, 0, 'SPIRIT_STONES', 0, 1, NULL, NULL);

-- ----------------------------
-- Table structure for shop_sell_logs
-- ----------------------------
DROP TABLE IF EXISTS `shop_sell_logs`;
CREATE TABLE `shop_sell_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出售日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型：ITEM/EQUIPMENT',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `sell_price` int NOT NULL COMMENT '出售价格',
  `currency_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SPIRIT_STONES' COMMENT '货币类型',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '出售时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_item_type`(`item_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_shop_sell_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城出售日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_sell_logs
-- ----------------------------

-- ----------------------------
-- Table structure for shop_statistics
-- ----------------------------
DROP TABLE IF EXISTS `shop_statistics`;
CREATE TABLE `shop_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_purchases` int NOT NULL DEFAULT 0 COMMENT '总购买次数',
  `total_spent_spirit_stones` bigint NOT NULL DEFAULT 0 COMMENT '总花费灵石',
  `total_spent_contribution` bigint NOT NULL DEFAULT 0 COMMENT '总花费贡献',
  `total_spent_yuanbao` bigint NOT NULL DEFAULT 0 COMMENT '总花费元宝',
  `total_sales` int NOT NULL DEFAULT 0 COMMENT '总出售次数',
  `total_earned_spirit_stones` bigint NOT NULL DEFAULT 0 COMMENT '总赚取灵石',
  `favorite_shop_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最常光顾商店',
  `last_purchase_at` timestamp NULL DEFAULT NULL COMMENT '最后购买时间',
  `last_sale_at` timestamp NULL DEFAULT NULL COMMENT '最后出售时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_shop_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for shop_wish_list
-- ----------------------------
DROP TABLE IF EXISTS `shop_wish_list`;
CREATE TABLE `shop_wish_list`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '心愿单ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型：ITEM/EQUIPMENT/PET',
  `item_id` int NOT NULL COMMENT '物品ID',
  `added_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_item`(`player_id` ASC, `item_type` ASC, `item_id` ASC) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_shop_wish_list_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商城心愿单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_wish_list
-- ----------------------------

-- ----------------------------
-- Table structure for sign_in_configs
-- ----------------------------
DROP TABLE IF EXISTS `sign_in_configs`;
CREATE TABLE `sign_in_configs`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '签到配置ID',
  `day` int NOT NULL COMMENT '第几天',
  `reward_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '奖励类型：SPIRIT_STONES/ITEM/EQUIPMENT/EXP',
  `reward_id` int NULL DEFAULT NULL COMMENT '奖励物品ID',
  `reward_quantity` int NOT NULL DEFAULT 1 COMMENT '奖励数量',
  `is_special` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否特殊奖励',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_day`(`day` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '签到奖励配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sign_in_configs
-- ----------------------------
INSERT INTO `sign_in_configs` VALUES (1, 1, 'SPIRIT_STONES', NULL, 100, 0);
INSERT INTO `sign_in_configs` VALUES (2, 2, 'ITEM', 1, 2, 0);
INSERT INTO `sign_in_configs` VALUES (3, 3, 'ITEM', 2, 2, 0);
INSERT INTO `sign_in_configs` VALUES (4, 4, 'SPIRIT_STONES', NULL, 200, 0);
INSERT INTO `sign_in_configs` VALUES (5, 5, 'EQUIPMENT', 3, 1, 0);
INSERT INTO `sign_in_configs` VALUES (6, 6, 'ITEM', 3, 5, 0);
INSERT INTO `sign_in_configs` VALUES (7, 7, 'EQUIPMENT', 6, 1, 1);

-- ----------------------------
-- Table structure for skill_combos
-- ----------------------------
DROP TABLE IF EXISTS `skill_combos`;
CREATE TABLE `skill_combos`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '连招ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '连招名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '连招描述',
  `skill_sequence` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能序列(JSON数组)',
  `combo_bonus` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '连招加成(百分比)',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_required_level`(`required_level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能连招表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_combos
-- ----------------------------
INSERT INTO `skill_combos` VALUES (1, '水火交融', '先水后火，产生蒸汽爆炸', '[4, 2]', 50.00, 10, 1, '2026-03-27 14:43:13');
INSERT INTO `skill_combos` VALUES (2, '风火连击', '风助火势，伤害倍增', '[6, 2]', 30.00, 8, 1, '2026-03-27 14:43:13');
INSERT INTO `skill_combos` VALUES (3, '土木防御', '土木结合，坚固防御', '[5, 3]', 40.00, 12, 1, '2026-03-27 14:43:13');

-- ----------------------------
-- Table structure for skill_effects
-- ----------------------------
DROP TABLE IF EXISTS `skill_effects`;
CREATE TABLE `skill_effects`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '效果ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `effect_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '效果类型：DAMAGE/HEAL/BUFF/DEBUFF/DOT/HOT/SHIELD',
  `effect_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '效果名称',
  `base_value` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '基础值',
  `scaling_factor` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '缩放因子',
  `duration` int NOT NULL DEFAULT 0 COMMENT '持续时间(秒)',
  `tick_interval` int NOT NULL DEFAULT 0 COMMENT '触发间隔(秒)',
  `stackable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可叠加',
  `max_stacks` int NOT NULL DEFAULT 1 COMMENT '最大叠加数',
  `trigger_chance` decimal(5, 2) NOT NULL DEFAULT 100.00 COMMENT '触发概率',
  `effect_order` int NOT NULL DEFAULT 0 COMMENT '效果顺序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_skill_id`(`skill_id` ASC) USING BTREE,
  INDEX `idx_effect_type`(`effect_type` ASC) USING BTREE,
  CONSTRAINT `fk_skill_effects_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能效果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_effects
-- ----------------------------
INSERT INTO `skill_effects` VALUES (1, 2, 'DAMAGE', '火焰伤害', 10.00, 1.20, 0, 0, 0, 1, 100.00, 1);
INSERT INTO `skill_effects` VALUES (2, 2, 'DOT', '灼烧', 5.00, 0.50, 3, 1, 1, 3, 30.00, 2);
INSERT INTO `skill_effects` VALUES (3, 3, 'HEAL', '生命恢复', 20.00, 1.00, 0, 0, 0, 1, 100.00, 1);
INSERT INTO `skill_effects` VALUES (4, 4, 'SHIELD', '水盾', 30.00, 0.80, 10, 0, 0, 1, 100.00, 1);
INSERT INTO `skill_effects` VALUES (5, 4, 'BUFF', '减伤', 20.00, 0.50, 10, 0, 0, 1, 100.00, 2);
INSERT INTO `skill_effects` VALUES (6, 5, 'DAMAGE', '土系伤害', 25.00, 1.50, 0, 0, 0, 1, 100.00, 1);
INSERT INTO `skill_effects` VALUES (7, 5, 'DEBUFF', '减速', 30.00, 0.30, 2, 0, 0, 1, 50.00, 2);
INSERT INTO `skill_effects` VALUES (8, 6, 'DAMAGE', '风系伤害', 15.00, 1.30, 0, 0, 0, 1, 100.00, 1);
INSERT INTO `skill_effects` VALUES (9, 6, 'DEBUFF', '击退', 1.00, 0.00, 0, 0, 0, 1, 40.00, 2);

-- ----------------------------
-- Table structure for skill_enhancements
-- ----------------------------
DROP TABLE IF EXISTS `skill_enhancements`;
CREATE TABLE `skill_enhancements`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '强化ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '强化名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '强化描述',
  `enhancement_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '强化类型：DAMAGE/RANGE/COOLDOWN/EFFECT',
  `target_skill_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标技能类型',
  `target_element` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标元素',
  `value` decimal(10, 2) NOT NULL COMMENT '强化值',
  `is_percentage` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否为百分比',
  `cost_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消耗类型：SPIRIT_STONES/SKILL_POINTS/ITEMS',
  `cost_value` int NOT NULL COMMENT '消耗数量',
  `cost_item_id` int NULL DEFAULT NULL COMMENT '消耗物品ID',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `required_mastery` int NOT NULL DEFAULT 1 COMMENT '需求熟练度',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_enhancement_type`(`enhancement_type` ASC) USING BTREE,
  INDEX `idx_target_skill_type`(`target_skill_type` ASC) USING BTREE,
  INDEX `fk_skill_enhancements_item`(`cost_item_id` ASC) USING BTREE,
  CONSTRAINT `fk_skill_enhancements_item` FOREIGN KEY (`cost_item_id`) REFERENCES `items` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能强化表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_enhancements
-- ----------------------------
INSERT INTO `skill_enhancements` VALUES (1, '火焰精通', '所有火系技能伤害提升', 'DAMAGE', 'attack', '火', 15.00, 1, 'SPIRIT_STONES', 1000, NULL, 10, 3, 1, '2026-03-27 14:43:14');
INSERT INTO `skill_enhancements` VALUES (2, '治疗强化', '所有治疗技能效果提升', 'EFFECT', 'heal', NULL, 20.00, 1, 'SPIRIT_STONES', 800, NULL, 5, 2, 1, '2026-03-27 14:43:14');
INSERT INTO `skill_enhancements` VALUES (3, '冷却缩减', '所有技能冷却时间减少', 'COOLDOWN', NULL, NULL, 10.00, 1, 'SKILL_POINTS', 3, NULL, 15, 1, 1, '2026-03-27 14:43:14');
INSERT INTO `skill_enhancements` VALUES (4, '法术穿透', '攻击技能无视部分防御', 'EFFECT', 'attack', NULL, 10.00, 1, 'SPIRIT_STONES', 2000, NULL, 20, 5, 1, '2026-03-27 14:43:14');
INSERT INTO `skill_enhancements` VALUES (5, '技能范围', '攻击技能范围增加', 'RANGE', 'attack', NULL, 25.00, 1, 'SKILL_POINTS', 2, NULL, 12, 1, 1, '2026-03-27 14:43:14');

-- ----------------------------
-- Table structure for skill_mastery
-- ----------------------------
DROP TABLE IF EXISTS `skill_mastery`;
CREATE TABLE `skill_mastery`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '熟练度ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `mastery_level` int NOT NULL DEFAULT 1 COMMENT '熟练度等级',
  `required_exp` int NOT NULL COMMENT '所需经验',
  `damage_bonus` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '伤害加成',
  `cooldown_reduction` int NOT NULL DEFAULT 0 COMMENT '冷却减少(秒)',
  `mana_cost_reduction` int NOT NULL DEFAULT 0 COMMENT '法力消耗减少',
  `special_effect` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '特殊效果(JSON)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_skill_mastery`(`skill_id` ASC, `mastery_level` ASC) USING BTREE,
  INDEX `idx_skill_id`(`skill_id` ASC) USING BTREE,
  CONSTRAINT `fk_skill_mastery_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能熟练度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_mastery
-- ----------------------------
INSERT INTO `skill_mastery` VALUES (1, 2, 1, 0, 0.00, 0, 0, NULL);
INSERT INTO `skill_mastery` VALUES (2, 2, 2, 100, 10.00, 0, 1, NULL);
INSERT INTO `skill_mastery` VALUES (3, 2, 3, 300, 20.00, 1, 2, '{\"burn_chance_increase\": 10}');
INSERT INTO `skill_mastery` VALUES (4, 2, 4, 600, 30.00, 1, 3, NULL);
INSERT INTO `skill_mastery` VALUES (5, 2, 5, 1000, 50.00, 2, 5, '{\"aoe_damage\": true}');
INSERT INTO `skill_mastery` VALUES (6, 3, 1, 0, 0.00, 0, 0, NULL);
INSERT INTO `skill_mastery` VALUES (7, 3, 2, 80, 15.00, 1, 1, NULL);
INSERT INTO `skill_mastery` VALUES (8, 3, 3, 200, 30.00, 2, 2, '{\"hot_effect\": true}');
INSERT INTO `skill_mastery` VALUES (9, 3, 4, 400, 45.00, 2, 3, NULL);
INSERT INTO `skill_mastery` VALUES (10, 3, 5, 700, 70.00, 3, 5, '{\"aoe_heal\": true}');
INSERT INTO `skill_mastery` VALUES (11, 6, 1, 0, 0.00, 0, 0, NULL);
INSERT INTO `skill_mastery` VALUES (12, 6, 2, 150, 15.00, 0, 2, NULL);
INSERT INTO `skill_mastery` VALUES (13, 6, 3, 400, 30.00, 1, 3, '{\"multi_hit\": 2}');
INSERT INTO `skill_mastery` VALUES (14, 6, 4, 800, 50.00, 1, 4, NULL);
INSERT INTO `skill_mastery` VALUES (15, 6, 5, 1500, 80.00, 2, 6, '{\"penetration\": true}');

-- ----------------------------
-- Table structure for skill_shop
-- ----------------------------
DROP TABLE IF EXISTS `skill_shop`;
CREATE TABLE `skill_shop`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商店ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `price` int NOT NULL COMMENT '价格',
  `required_level` int NOT NULL DEFAULT 1 COMMENT '需求等级',
  `available` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否可用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_skill_id`(`skill_id` ASC) USING BTREE,
  INDEX `idx_available`(`available` ASC) USING BTREE,
  CONSTRAINT `fk_skill_shop_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能商店表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_shop
-- ----------------------------
INSERT INTO `skill_shop` VALUES (1, 1, 500, 1, 1, '2026-03-27 14:43:13', '2026-03-27 14:43:13');
INSERT INTO `skill_shop` VALUES (2, 2, 1000, 5, 1, '2026-03-27 14:43:13', '2026-03-27 14:43:13');
INSERT INTO `skill_shop` VALUES (3, 3, 800, 3, 1, '2026-03-27 14:43:13', '2026-03-27 14:43:13');
INSERT INTO `skill_shop` VALUES (4, 4, 1200, 8, 1, '2026-03-27 14:43:13', '2026-03-27 14:43:13');
INSERT INTO `skill_shop` VALUES (5, 5, 1500, 12, 1, '2026-03-27 14:43:13', '2026-03-27 14:43:13');
INSERT INTO `skill_shop` VALUES (6, 6, 1300, 10, 1, '2026-03-27 14:43:13', '2026-03-27 14:43:13');

-- ----------------------------
-- Table structure for skill_statistics
-- ----------------------------
DROP TABLE IF EXISTS `skill_statistics`;
CREATE TABLE `skill_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `total_skills_learned` int NOT NULL DEFAULT 0 COMMENT '已学习技能数',
  `total_skill_uses` bigint NOT NULL DEFAULT 0 COMMENT '总技能使用次数',
  `total_damage_by_skills` bigint NOT NULL DEFAULT 0 COMMENT '技能总伤害',
  `total_heal_by_skills` bigint NOT NULL DEFAULT 0 COMMENT '技能总治疗',
  `highest_mastery_level` int NOT NULL DEFAULT 0 COMMENT '最高熟练度',
  `favorite_skill_id` int NULL DEFAULT NULL COMMENT '最常用技能',
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_id`(`player_id` ASC) USING BTREE,
  CONSTRAINT `fk_skill_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for skill_tree_nodes
-- ----------------------------
DROP TABLE IF EXISTS `skill_tree_nodes`;
CREATE TABLE `skill_tree_nodes`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `tree_id` int NOT NULL COMMENT '技能树ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `node_level` int NOT NULL DEFAULT 1 COMMENT '节点层级',
  `position_x` int NOT NULL DEFAULT 0 COMMENT 'X坐标',
  `position_y` int NOT NULL DEFAULT 0 COMMENT 'Y坐标',
  `prerequisites` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '前置技能ID(JSON数组)',
  `skill_points_cost` int NOT NULL DEFAULT 1 COMMENT '技能点消耗',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tree_skill`(`tree_id` ASC, `skill_id` ASC) USING BTREE,
  INDEX `idx_tree_id`(`tree_id` ASC) USING BTREE,
  INDEX `idx_skill_id`(`skill_id` ASC) USING BTREE,
  CONSTRAINT `fk_skill_tree_nodes_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_skill_tree_nodes_tree` FOREIGN KEY (`tree_id`) REFERENCES `skill_trees` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能树节点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_tree_nodes
-- ----------------------------
INSERT INTO `skill_tree_nodes` VALUES (1, 1, 2, 1, 0, 0, '[]', 1);
INSERT INTO `skill_tree_nodes` VALUES (2, 3, 3, 1, 0, 0, '[]', 1);
INSERT INTO `skill_tree_nodes` VALUES (3, 2, 4, 1, 0, 0, '[]', 1);
INSERT INTO `skill_tree_nodes` VALUES (4, 4, 5, 1, 0, 0, '[]', 1);
INSERT INTO `skill_tree_nodes` VALUES (5, 5, 6, 1, 0, 0, '[]', 1);
INSERT INTO `skill_tree_nodes` VALUES (6, 7, 1, 1, 0, 0, '[]', 0);

-- ----------------------------
-- Table structure for skill_trees
-- ----------------------------
DROP TABLE IF EXISTS `skill_trees`;
CREATE TABLE `skill_trees`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '技能树ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能树名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '技能树描述',
  `tree_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：ATTACK/DEFENSE/CULTIVATION/SUPPORT',
  `element` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '元素属性',
  `required_realm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '需求境界',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tree_type`(`tree_type` ASC) USING BTREE,
  INDEX `idx_element`(`element` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能树表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_trees
-- ----------------------------
INSERT INTO `skill_trees` VALUES (1, '火系法术', '掌控火焰之力的法术', 'ATTACK', '火', NULL, NULL, 1, '2026-03-27 14:43:13');
INSERT INTO `skill_trees` VALUES (2, '水系法术', '掌控水流之力的法术', 'DEFENSE', '水', NULL, NULL, 1, '2026-03-27 14:43:13');
INSERT INTO `skill_trees` VALUES (3, '木系法术', '掌控生机之力的法术', 'SUPPORT', '木', NULL, NULL, 1, '2026-03-27 14:43:13');
INSERT INTO `skill_trees` VALUES (4, '土系法术', '掌控大地之力的法术', 'DEFENSE', '土', NULL, NULL, 1, '2026-03-27 14:43:13');
INSERT INTO `skill_trees` VALUES (5, '风系法术', '掌控风之力的法术', 'ATTACK', '风', NULL, NULL, 1, '2026-03-27 14:43:13');
INSERT INTO `skill_trees` VALUES (6, '雷系法术', '掌控雷电之力的法术', 'ATTACK', '雷', '筑基期', NULL, 1, '2026-03-27 14:43:13');
INSERT INTO `skill_trees` VALUES (7, '修炼心法', '提升修炼效率的心法', 'CULTIVATION', '无', NULL, NULL, 1, '2026-03-27 14:43:13');

-- ----------------------------
-- Table structure for skill_usage_logs
-- ----------------------------
DROP TABLE IF EXISTS `skill_usage_logs`;
CREATE TABLE `skill_usage_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `skill_id` int NOT NULL COMMENT '技能ID',
  `skill_level` int NOT NULL DEFAULT 1 COMMENT '技能等级',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标类型：MONSTER/PLAYER',
  `target_id` int NULL DEFAULT NULL COMMENT '目标ID',
  `damage_dealt` int NOT NULL DEFAULT 0 COMMENT '造成伤害',
  `heal_amount` int NOT NULL DEFAULT 0 COMMENT '治疗量',
  `mana_consumed` int NOT NULL DEFAULT 0 COMMENT '消耗法力',
  `is_critical` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否暴击',
  `mastery_exp_gained` int NOT NULL DEFAULT 0 COMMENT '获得熟练度',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_skill_id`(`skill_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_skill_usage_logs_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_skill_usage_logs_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能使用日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_usage_logs
-- ----------------------------

-- ----------------------------
-- Table structure for skills
-- ----------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '技能ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '技能描述',
  `level` int NOT NULL DEFAULT 1 COMMENT '技能等级',
  `max_level` int NOT NULL DEFAULT 10 COMMENT '最大等级',
  `base_damage` double NULL DEFAULT 0 COMMENT '基础伤害',
  `damage_per_level` double NULL DEFAULT 0 COMMENT '每级伤害增长',
  `cooldown` int NULL DEFAULT 0 COMMENT '冷却时间(秒)',
  `mana_cost` int NULL DEFAULT 0 COMMENT '法力消耗',
  `skill_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '技能类型',
  `element` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '元素属性',
  `unlock_level` int NULL DEFAULT 1 COMMENT '解锁等级',
  `required_spirit_stones` int NULL DEFAULT 0 COMMENT '需要的灵石数量',
  `health_bonus` int NULL DEFAULT 0 COMMENT '生命值加成',
  `mana_bonus` int NULL DEFAULT 0 COMMENT '法力值加成',
  `attack_bonus` int NULL DEFAULT 0 COMMENT '攻击力加成',
  `defense_bonus` int NULL DEFAULT 0 COMMENT '防御力加成',
  `speed_bonus` int NULL DEFAULT 0 COMMENT '速度加成',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `animation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '动画',
  `active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_skill_type`(`skill_type` ASC) USING BTREE,
  INDEX `idx_element`(`element` ASC) USING BTREE,
  INDEX `idx_unlock_level`(`unlock_level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '技能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skills
-- ----------------------------
INSERT INTO `skills` VALUES (1, '基础功法', '提升基础修炼速度', 1, 100, 0.05, 0.01, 0, 0, 'cultivation', '无', 1, 0, 0, 0, 0, 0, 0, NULL, NULL, 1, '2026-03-27 14:43:12', '2026-03-27 14:43:12');
INSERT INTO `skills` VALUES (2, '火球术', '基础火系攻击法术', 1, 50, 10, 2, 5, 10, 'attack', '火', 5, 1000, 0, 0, 0, 0, 0, NULL, NULL, 1, '2026-03-27 14:43:12', '2026-03-27 14:43:12');
INSERT INTO `skills` VALUES (3, '治疗术', '恢复生命值的法术', 1, 30, 20, 1.5, 8, 15, 'heal', '木', 3, 800, 0, 0, 0, 0, 0, NULL, NULL, 1, '2026-03-27 14:43:12', '2026-03-27 14:43:12');
INSERT INTO `skills` VALUES (4, '水盾术', '创造一个水盾，减少受到的伤害', 1, 10, 0, 0, 10, 15, 'defense', '水', 8, 1200, 0, 0, 0, 0, 0, NULL, NULL, 1, '2026-03-27 14:43:12', '2026-03-27 14:43:12');
INSERT INTO `skills` VALUES (5, '地刺术', '从地面召唤尖刺，对敌人造成土属性伤害', 1, 10, 25, 10, 5, 20, 'attack', '土', 12, 1500, 0, 0, 0, 0, 0, NULL, NULL, 1, '2026-03-27 14:43:12', '2026-03-27 14:43:12');
INSERT INTO `skills` VALUES (6, '风刃术', '释放锋利的风刃，对敌人造成风属性伤害', 1, 10, 15, 7, 2, 8, 'attack', '风', 10, 1300, 0, 0, 0, 0, 0, NULL, NULL, 1, '2026-03-27 14:43:12', '2026-03-27 14:43:12');

-- ----------------------------
-- Table structure for temporary_items
-- ----------------------------
DROP TABLE IF EXISTS `temporary_items`;
CREATE TABLE `temporary_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '临时物品ID',
  `player_id` int NOT NULL COMMENT '玩家ID',
  `item_id` int NOT NULL COMMENT '物品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源',
  `expire_at` timestamp NOT NULL COMMENT '过期时间',
  `is_expired` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已过期',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_id`(`player_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_expire_at`(`expire_at` ASC) USING BTREE,
  INDEX `idx_is_expired`(`is_expired` ASC) USING BTREE,
  CONSTRAINT `fk_temporary_items_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_temporary_items_player` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '临时物品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of temporary_items
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(加密)',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/BANNED/INACTIVE',
  `must_change_password` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必须修改密码',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------

-- ----------------------------
-- Table structure for vip_levels
-- ----------------------------
DROP TABLE IF EXISTS `vip_levels`;
CREATE TABLE `vip_levels`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'VIP等级ID',
  `level` int NOT NULL COMMENT 'VIP等级',
  `required_recharge` int NOT NULL COMMENT '所需充值金额',
  `daily_spirit_stones` int NOT NULL DEFAULT 0 COMMENT '每日灵石奖励',
  `cultivation_speed_bonus` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '修炼速度加成',
  `exp_bonus` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '经验加成',
  `shop_discount` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '商店折扣',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_level`(`level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'VIP等级配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of vip_levels
-- ----------------------------
INSERT INTO `vip_levels` VALUES (1, 0, 0, 0, 0.00, 0.00, 0.00);
INSERT INTO `vip_levels` VALUES (2, 1, 100, 50, 0.10, 0.10, 0.05);
INSERT INTO `vip_levels` VALUES (3, 2, 500, 100, 0.20, 0.20, 0.10);
INSERT INTO `vip_levels` VALUES (4, 3, 1000, 200, 0.30, 0.30, 0.15);
INSERT INTO `vip_levels` VALUES (5, 4, 2000, 300, 0.40, 0.40, 0.20);
INSERT INTO `vip_levels` VALUES (6, 5, 5000, 500, 0.50, 0.50, 0.25);

-- ----------------------------
-- View structure for v_player_summary
-- ----------------------------
DROP VIEW IF EXISTS `v_player_summary`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_player_summary` AS select `pp`.`id` AS `id`,`pp`.`user_id` AS `user_id`,`u`.`username` AS `username`,`pp`.`nickname` AS `nickname`,`pp`.`level` AS `level`,`pp`.`exp` AS `exp`,`pp`.`exp_to_next` AS `exp_to_next`,`pp`.`realm` AS `realm`,`pp`.`spirit_stones` AS `spirit_stones`,`pp`.`cultivation_points` AS `cultivation_points`,`pp`.`contribution_points` AS `contribution_points`,`pp`.`attack` AS `attack`,`pp`.`defense` AS `defense`,`pp`.`health` AS `health`,`pp`.`mana` AS `mana`,`pp`.`speed` AS `speed`,`pp`.`total_cultivation_time` AS `total_cultivation_time`,`pp`.`last_online_time` AS `last_online_time`,`pp`.`created_at` AS `created_at`,`pp`.`updated_at` AS `updated_at`,(`pp`.`attack` + coalesce(`equ`.`attack_bonus`,0)) AS `total_attack`,(`pp`.`defense` + coalesce(`equ`.`defense_bonus`,0)) AS `total_defense`,(`pp`.`health` + coalesce(`equ`.`health_bonus`,0)) AS `total_health`,(`pp`.`mana` + coalesce(`equ`.`mana_bonus`,0)) AS `total_mana`,(`pp`.`speed` + coalesce(`equ`.`speed_bonus`,0)) AS `total_speed` from ((`player_profiles` `pp` join `users` `u` on((`pp`.`user_id` = `u`.`id`))) left join (select `pe`.`player_id` AS `player_id`,sum(`e`.`attack_bonus`) AS `attack_bonus`,sum(`e`.`defense_bonus`) AS `defense_bonus`,sum(`e`.`health_bonus`) AS `health_bonus`,sum(`e`.`mana_bonus`) AS `mana_bonus`,sum(`e`.`speed_bonus`) AS `speed_bonus` from (`player_equipment` `pe` join `equipments` `e` on((`pe`.`equipment_id` = `e`.`id`))) where (`pe`.`is_equipped` = 1) group by `pe`.`player_id`) `equ` on((`pp`.`id` = `equ`.`player_id`)));

SET FOREIGN_KEY_CHECKS = 1;
