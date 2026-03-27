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

 Date: 27/03/2026 14:47:27
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
-- View structure for v_player_summary
-- ----------------------------
DROP VIEW IF EXISTS `v_player_summary`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_player_summary` AS select `pp`.`id` AS `id`,`pp`.`user_id` AS `user_id`,`u`.`username` AS `username`,`pp`.`nickname` AS `nickname`,`pp`.`level` AS `level`,`pp`.`exp` AS `exp`,`pp`.`exp_to_next` AS `exp_to_next`,`pp`.`realm` AS `realm`,`pp`.`spirit_stones` AS `spirit_stones`,`pp`.`cultivation_points` AS `cultivation_points`,`pp`.`contribution_points` AS `contribution_points`,`pp`.`attack` AS `attack`,`pp`.`defense` AS `defense`,`pp`.`health` AS `health`,`pp`.`mana` AS `mana`,`pp`.`speed` AS `speed`,`pp`.`total_cultivation_time` AS `total_cultivation_time`,`pp`.`last_online_time` AS `last_online_time`,`pp`.`created_at` AS `created_at`,`pp`.`updated_at` AS `updated_at`,(`pp`.`attack` + coalesce(`equ`.`attack_bonus`,0)) AS `total_attack`,(`pp`.`defense` + coalesce(`equ`.`defense_bonus`,0)) AS `total_defense`,(`pp`.`health` + coalesce(`equ`.`health_bonus`,0)) AS `total_health`,(`pp`.`mana` + coalesce(`equ`.`mana_bonus`,0)) AS `total_mana`,(`pp`.`speed` + coalesce(`equ`.`speed_bonus`,0)) AS `total_speed` from ((`player_profiles` `pp` join `users` `u` on((`pp`.`user_id` = `u`.`id`))) left join (select `pe`.`player_id` AS `player_id`,sum(`e`.`attack_bonus`) AS `attack_bonus`,sum(`e`.`defense_bonus`) AS `defense_bonus`,sum(`e`.`health_bonus`) AS `health_bonus`,sum(`e`.`mana_bonus`) AS `mana_bonus`,sum(`e`.`speed_bonus`) AS `speed_bonus` from (`player_equipment` `pe` join `equipments` `e` on((`pe`.`equipment_id` = `e`.`id`))) where (`pe`.`is_equipped` = 1) group by `pe`.`player_id`) `equ` on((`pp`.`id` = `equ`.`player_id`)));

SET FOREIGN_KEY_CHECKS = 1;
