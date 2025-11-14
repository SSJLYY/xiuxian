/*
Navicat MySQL Data Transfer

Source Server         : 个人阿里云
Source Server Version : 80044
Source Host           : 47.103.87.55:3306
Source Database       : xiuxian_game

Target Server Type    : MYSQL
Target Server Version : 80044
File Encoding         : 65001

Date: 2025-11-13 13:47:59
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cultivation_levels
-- ----------------------------
DROP TABLE IF EXISTS `cultivation_levels`;
CREATE TABLE `cultivation_levels` (
  `id` int NOT NULL AUTO_INCREMENT,
  `level` int NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `min_exp` bigint NOT NULL,
  `max_exp` bigint NOT NULL,
  `health_bonus` int NOT NULL,
  `mana_bonus` int NOT NULL,
  `attack_bonus` int NOT NULL,
  `defense_bonus` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `level` (`level`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of cultivation_levels
-- ----------------------------
INSERT INTO `cultivation_levels` VALUES ('1', '1', '练气期一层', '0', '100', '100', '50', '0', '0');
INSERT INTO `cultivation_levels` VALUES ('2', '2', '练气期二层', '100', '250', '120', '60', '2', '1');
INSERT INTO `cultivation_levels` VALUES ('3', '3', '练气期三层', '250', '450', '140', '70', '4', '2');
INSERT INTO `cultivation_levels` VALUES ('4', '4', '练气期四层', '450', '700', '160', '80', '6', '3');
INSERT INTO `cultivation_levels` VALUES ('5', '5', '练气期五层', '700', '1000', '180', '90', '8', '4');
INSERT INTO `cultivation_levels` VALUES ('6', '6', '练气期六层', '1000', '1350', '200', '100', '10', '5');
INSERT INTO `cultivation_levels` VALUES ('7', '7', '练气期七层', '1350', '1750', '220', '110', '12', '6');
INSERT INTO `cultivation_levels` VALUES ('8', '8', '练气期八层', '1750', '2200', '240', '120', '14', '7');
INSERT INTO `cultivation_levels` VALUES ('9', '9', '练气期九层', '2200', '2700', '260', '130', '16', '8');
INSERT INTO `cultivation_levels` VALUES ('10', '10', '练气期十层', '2700', '3250', '280', '140', '18', '9');
INSERT INTO `cultivation_levels` VALUES ('11', '11', '筑基期一层', '3250', '4000', '350', '200', '25', '15');
INSERT INTO `cultivation_levels` VALUES ('12', '12', '筑基期二层', '4000', '4900', '400', '230', '30', '18');
INSERT INTO `cultivation_levels` VALUES ('13', '13', '筑基期三层', '4900', '6000', '450', '260', '35', '21');
INSERT INTO `cultivation_levels` VALUES ('14', '14', '筑基期四层', '6000', '7300', '500', '290', '40', '24');
INSERT INTO `cultivation_levels` VALUES ('15', '15', '筑基期五层', '7300', '8800', '550', '320', '45', '27');
INSERT INTO `cultivation_levels` VALUES ('16', '16', '金丹期一层', '8800', '11000', '700', '400', '60', '35');
INSERT INTO `cultivation_levels` VALUES ('17', '17', '金丹期二层', '11000', '13500', '800', '450', '70', '40');
INSERT INTO `cultivation_levels` VALUES ('18', '18', '金丹期三层', '13500', '16500', '900', '500', '80', '45');
INSERT INTO `cultivation_levels` VALUES ('19', '19', '金丹期四层', '16500', '20000', '1000', '550', '90', '50');
INSERT INTO `cultivation_levels` VALUES ('20', '20', '元婴期一层', '20000', '25000', '1300', '700', '120', '70');

-- ----------------------------
-- Table structure for cultivation_logs
-- ----------------------------
DROP TABLE IF EXISTS `cultivation_logs`;
CREATE TABLE `cultivation_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `cultivation_time` int NOT NULL,
  `exp_gained` int NOT NULL,
  `cultivation_points_gained` int NOT NULL,
  `created_at` timestamp NOT NULL,
  `cultivation_duration` bigint NOT NULL,
  `is_offline` bit(1) NOT NULL,
  `spirit_stones_gained` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `cultivation_logs_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of cultivation_logs
-- ----------------------------

-- ----------------------------
-- Table structure for equipments
-- ----------------------------
DROP TABLE IF EXISTS `equipments`;
CREATE TABLE `equipments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `level` int NOT NULL,
  `quality` int NOT NULL,
  `attack_bonus` int NOT NULL DEFAULT '0',
  `defense_bonus` int NOT NULL DEFAULT '0',
  `health_bonus` int NOT NULL DEFAULT '0',
  `mana_bonus` int NOT NULL DEFAULT '0',
  `speed_bonus` int NOT NULL DEFAULT '0',
  `required_level` int NOT NULL,
  `price` bigint NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_equipment_type` (`type`),
  KEY `idx_quality` (`quality`),
  KEY `idx_required_level` (`required_level`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of equipments
-- ----------------------------
INSERT INTO `equipments` VALUES ('1', '木剑', '普通的木制法剑', 'weapon', '1', '1', '5', '0', '0', '0', '0', '1', '100', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('2', '布袍', '简单的修炼道袍', 'chest', '1', '1', '0', '5', '50', '0', '0', '1', '150', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('3', '草帽', '简单的草制帽子', 'helmet', '1', '1', '0', '2', '20', '0', '0', '1', '80', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('4', '布鞋', '轻便的布制鞋子', 'boots', '1', '1', '0', '1', '10', '0', '2', '1', '60', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('5', '木盾', '简单的木制盾牌', 'shield', '1', '1', '0', '8', '30', '0', '0', '1', '120', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('6', '铁剑', '坚固的铁制长剑', 'weapon', '5', '2', '15', '0', '0', '0', '0', '5', '500', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('7', '皮甲', '轻便的皮制护甲', 'chest', '5', '2', '0', '10', '100', '0', '0', '5', '600', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('8', '铁盔', '坚固的铁制头盔', 'helmet', '5', '2', '0', '8', '50', '0', '0', '5', '400', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('9', '皮靴', '结实的皮制靴子', 'boots', '5', '2', '0', '3', '30', '0', '5', '5', '350', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('10', '铁盾', '坚固的铁制盾牌', 'shield', '5', '2', '0', '15', '80', '0', '0', '5', '550', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('11', '玉符', '低级灵力护符', 'ring', '5', '2', '0', '0', '30', '20', '5', '5', '300', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('12', '法杖', '蕴含灵力的法杖', 'weapon', '10', '3', '25', '0', '0', '50', '0', '10', '1200', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('13', '道袍', '修仙者常穿的道袍', 'chest', '10', '3', '0', '15', '200', '30', '5', '10', '1500', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('14', '道冠', '修仙者佩戴的道冠', 'helmet', '10', '3', '0', '12', '80', '20', '3', '10', '800', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('15', '道靴', '修仙者专用的靴子', 'boots', '10', '3', '0', '5', '60', '10', '10', '10', '700', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('16', '护心镜', '保护心脏的护镜', 'shield', '10', '3', '0', '20', '150', '10', '2', '10', '1000', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('17', '灵戒', '蕴含灵力的戒指', 'ring', '10', '3', '5', '5', '50', '50', '10', '10', '800', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('18', '银剑', '锋利的银制长剑', 'weapon', '15', '3', '35', '0', '0', '10', '2', '15', '2000', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('19', '银甲', '闪亮的银制铠甲', 'chest', '15', '3', '0', '25', '300', '20', '5', '15', '2500', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('20', '银盔', '精致的银制头盔', 'helmet', '15', '3', '0', '18', '120', '15', '5', '15', '1200', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('21', '银靴', '轻便的银制靴子', 'boots', '15', '3', '0', '8', '80', '10', '15', '15', '1000', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('22', '银盾', '坚固的银制盾牌', 'shield', '15', '3', '0', '30', '200', '5', '3', '15', '1800', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('23', '银戒', '高级灵力戒指', 'ring', '15', '3', '8', '8', '80', '80', '15', '15', '1500', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('24', '金剑', '珍贵的金制长剑', 'weapon', '20', '4', '50', '0', '0', '20', '5', '20', '3500', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('25', '金甲', '华丽的金制铠甲', 'chest', '20', '4', '0', '35', '400', '30', '8', '20', '4000', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('26', '金盔', '华丽的金制头盔', 'helmet', '20', '4', '0', '25', '150', '20', '8', '20', '2000', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('27', '金靴', '华丽的金制靴子', 'boots', '20', '4', '0', '12', '100', '15', '20', '20', '1800', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('28', '金盾', '华丽的金制盾牌', 'shield', '20', '4', '0', '40', '250', '10', '5', '20', '3000', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('29', '金戒', '顶级灵力戒指', 'ring', '20', '4', '12', '12', '100', '100', '20', '20', '2500', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `equipments` VALUES ('30', '木剑', '普通的木制法剑', 'weapon', '1', '1', '5', '0', '0', '0', '0', '1', '100', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('31', '布袍', '简单的修炼道袍', 'chest', '1', '1', '0', '5', '50', '0', '0', '1', '150', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('32', '草帽', '简单的草制帽子', 'helmet', '1', '1', '0', '2', '20', '0', '0', '1', '80', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('33', '布鞋', '轻便的布制鞋子', 'boots', '1', '1', '0', '1', '10', '0', '2', '1', '60', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('34', '木盾', '简单的木制盾牌', 'shield', '1', '1', '0', '8', '30', '0', '0', '1', '120', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('35', '铁剑', '坚固的铁制长剑', 'weapon', '5', '2', '15', '0', '0', '0', '0', '5', '500', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('36', '皮甲', '轻便的皮制护甲', 'chest', '5', '2', '0', '10', '100', '0', '0', '5', '600', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('37', '铁盔', '坚固的铁制头盔', 'helmet', '5', '2', '0', '8', '50', '0', '0', '5', '400', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('38', '皮靴', '结实的皮制靴子', 'boots', '5', '2', '0', '3', '30', '0', '5', '5', '350', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('39', '铁盾', '坚固的铁制盾牌', 'shield', '5', '2', '0', '15', '80', '0', '0', '5', '550', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('40', '玉符', '低级灵力护符', 'ring', '5', '2', '0', '0', '30', '20', '5', '5', '300', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('41', '法杖', '蕴含灵力的法杖', 'weapon', '10', '3', '25', '0', '0', '50', '0', '10', '1200', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('42', '道袍', '修仙者常穿的道袍', 'chest', '10', '3', '0', '15', '200', '30', '5', '10', '1500', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('43', '道冠', '修仙者佩戴的道冠', 'helmet', '10', '3', '0', '12', '80', '20', '3', '10', '800', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('44', '道靴', '修仙者专用的靴子', 'boots', '10', '3', '0', '5', '60', '10', '10', '10', '700', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('45', '护心镜', '保护心脏的护镜', 'shield', '10', '3', '0', '20', '150', '10', '2', '10', '1000', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('46', '灵戒', '蕴含灵力的戒指', 'ring', '10', '3', '5', '5', '50', '50', '10', '10', '800', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('47', '银剑', '锋利的银制长剑', 'weapon', '15', '3', '35', '0', '0', '10', '2', '15', '2000', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('48', '银甲', '闪亮的银制铠甲', 'chest', '15', '3', '0', '25', '300', '20', '5', '15', '2500', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('49', '银盔', '精致的银制头盔', 'helmet', '15', '3', '0', '18', '120', '15', '5', '15', '1200', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('50', '银靴', '轻便的银制靴子', 'boots', '15', '3', '0', '8', '80', '10', '15', '15', '1000', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('51', '银盾', '坚固的银制盾牌', 'shield', '15', '3', '0', '30', '200', '5', '3', '15', '1800', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('52', '银戒', '高级灵力戒指', 'ring', '15', '3', '8', '8', '80', '80', '15', '15', '1500', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('53', '金剑', '珍贵的金制长剑', 'weapon', '20', '4', '50', '0', '0', '20', '5', '20', '3500', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('54', '金甲', '华丽的金制铠甲', 'chest', '20', '4', '0', '35', '400', '30', '8', '20', '4000', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('55', '金盔', '华丽的金制头盔', 'helmet', '20', '4', '0', '25', '150', '20', '8', '20', '2000', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('56', '金靴', '华丽的金制靴子', 'boots', '20', '4', '0', '12', '100', '15', '20', '20', '1800', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('57', '金盾', '华丽的金制盾牌', 'shield', '20', '4', '0', '40', '250', '10', '5', '20', '3000', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `equipments` VALUES ('58', '金戒', '顶级灵力戒指', 'ring', '20', '4', '12', '12', '100', '100', '20', '20', '2500', '2025-10-31 18:37:50', '2025-10-31 18:37:50');

-- ----------------------------
-- Table structure for items
-- ----------------------------
DROP TABLE IF EXISTS `items`;
CREATE TABLE `items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quality` int NOT NULL,
  `stackable` tinyint(1) NOT NULL,
  `max_stack` int NOT NULL,
  `price` bigint NOT NULL,
  `sellable` tinyint(1) NOT NULL,
  `usable` tinyint(1) NOT NULL,
  `effect` json DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_item_type` (`type`),
  KEY `idx_quality` (`quality`),
  KEY `idx_stackable` (`stackable`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of items
-- ----------------------------
INSERT INTO `items` VALUES ('1', '疗伤丹', '恢复生命值的丹药', 'consumable', '1', '1', '99', '50', '1', '1', '{\"heal\": 50}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('2', '回灵丹', '恢复灵力的丹药', 'consumable', '1', '1', '99', '50', '1', '1', '{\"restore_mana\": 50}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('3', '经验丹', '提升经验值的丹药', 'consumable', '2', '1', '50', '200', '1', '1', '{\"exp\": 100}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('4', '突破丹', '帮助突破境界的丹药', 'consumable', '3', '1', '10', '1000', '1', '1', '{\"breakthrough\": 1}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('5', '灵草', '蕴含灵力的草药', 'material', '1', '1', '999', '10', '1', '0', '{}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('6', '灵石', '蕴含纯净灵力的石头', 'material', '2', '1', '999', '100', '1', '0', '{}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('7', '妖丹', '妖兽内丹，炼器材料', 'material', '3', '1', '99', '500', '1', '0', '{}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('8', '仙草', '传说中的仙草', 'material', '4', '1', '10', '2000', '1', '0', '{}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('9', '新手礼包', '包含基础装备和物品的礼包', 'special', '1', '0', '1', '0', '0', '1', '{\"items\": [{\"id\": 1, \"quantity\": 1}, {\"id\": 2, \"quantity\": 5}]}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('10', '修炼心得', '记录修炼感悟的书籍', 'book', '2', '0', '1', '500', '1', '1', '{\"cultivation_speed\": 1.1}', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `items` VALUES ('11', '疗伤丹', '恢复生命值的丹药', 'consumable', '1', '1', '99', '50', '1', '1', '{\"heal\": 50}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `items` VALUES ('12', '回灵丹', '恢复灵力的丹药', 'consumable', '1', '1', '99', '50', '1', '1', '{\"restore_mana\": 50}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `items` VALUES ('13', '经验丹', '提升经验值的丹药', 'consumable', '2', '1', '50', '200', '1', '1', '{\"exp\": 100}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `items` VALUES ('14', '突破丹', '帮助突破境界的丹药', 'consumable', '3', '1', '10', '1000', '1', '1', '{\"breakthrough\": 1}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `items` VALUES ('15', '灵草', '蕴含灵力的草药', 'material', '1', '1', '999', '10', '1', '0', '{}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `items` VALUES ('16', '灵石', '蕴含纯净灵力的石头', 'material', '2', '1', '999', '100', '1', '0', '{}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `items` VALUES ('17', '妖丹', '妖兽内丹，炼器材料', 'material', '3', '1', '99', '500', '1', '0', '{}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `items` VALUES ('18', '仙草', '传说中的仙草', 'material', '4', '1', '10', '2000', '1', '0', '{}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `items` VALUES ('19', '新手礼包', '包含基础装备和物品的礼包', 'special', '1', '0', '1', '0', '0', '1', '{\"items\": [{\"id\": 1, \"quantity\": 1}, {\"id\": 2, \"quantity\": 5}]}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `items` VALUES ('20', '修炼心得', '记录修炼感悟的书籍', 'book', '2', '0', '1', '500', '1', '1', '{\"cultivation_speed\": 1.1}', '2025-10-31 18:37:50', '2025-10-31 18:37:50');

-- ----------------------------
-- Table structure for player_equipment
-- ----------------------------
DROP TABLE IF EXISTS `player_equipment`;
CREATE TABLE `player_equipment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `equipment_id` int NOT NULL,
  `slot` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_equipped` tinyint(1) NOT NULL DEFAULT '0',
  `durability` int NOT NULL DEFAULT '100',
  `max_durability` int NOT NULL DEFAULT '100',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_slot` (`slot`),
  KEY `idx_is_equipped` (`is_equipped`),
  CONSTRAINT `player_equipment_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `player_equipment_ibfk_2` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of player_equipment
-- ----------------------------

-- ----------------------------
-- Table structure for player_equipments
-- ----------------------------
DROP TABLE IF EXISTS `player_equipments`;
CREATE TABLE `player_equipments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `durability` int NOT NULL,
  `equipped` bit(1) NOT NULL,
  `max_durability` int NOT NULL,
  `slot` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `equipment_id` int NOT NULL,
  `player_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKliafc7weyhnx9nw20mi6jvc5t` (`equipment_id`),
  KEY `FK3skbfdyiw72p83f8or2qmnif1` (`player_id`),
  CONSTRAINT `FK3skbfdyiw72p83f8or2qmnif1` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`),
  CONSTRAINT `FKliafc7weyhnx9nw20mi6jvc5t` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of player_equipments
-- ----------------------------

-- ----------------------------
-- Table structure for player_items
-- ----------------------------
DROP TABLE IF EXISTS `player_items`;
CREATE TABLE `player_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `item_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_item` (`player_id`,`item_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_item_id` (`item_id`),
  CONSTRAINT `player_items_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `player_items_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of player_items
-- ----------------------------

-- ----------------------------
-- Table structure for player_profiles
-- ----------------------------
DROP TABLE IF EXISTS `player_profiles`;
CREATE TABLE `player_profiles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `level` int NOT NULL DEFAULT '1',
  `exp` bigint NOT NULL DEFAULT '0',
  `realm` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '练气期',
  `cultivation_speed` decimal(10,2) NOT NULL DEFAULT '1.00',
  `spirit_stones` bigint NOT NULL DEFAULT '1000',
  `cultivation_points` bigint NOT NULL DEFAULT '0',
  `contribution_points` bigint NOT NULL DEFAULT '0',
  `last_online_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `total_cultivation_time` bigint NOT NULL DEFAULT '0',
  `attack` int NOT NULL DEFAULT '10',
  `defense` int NOT NULL DEFAULT '10',
  `health` int NOT NULL DEFAULT '100',
  `mana` int NOT NULL DEFAULT '50',
  `speed` int NOT NULL DEFAULT '10',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `equipment_attack_bonus` int NOT NULL DEFAULT '0',
  `equipment_defense_bonus` int NOT NULL DEFAULT '0',
  `equipment_health_bonus` int NOT NULL DEFAULT '0',
  `equipment_mana_bonus` int NOT NULL DEFAULT '0',
  `equipment_speed_bonus` int NOT NULL DEFAULT '0',
  `is_cultivating` bit(1) DEFAULT NULL,
  `last_cultivation_end` datetime(6) DEFAULT NULL,
  `last_cultivation_start` datetime(6) DEFAULT NULL,
  `exp_to_next` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_level` (`level`),
  KEY `idx_realm` (`realm`),
  CONSTRAINT `player_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of player_profiles
-- ----------------------------
INSERT INTO `player_profiles` VALUES ('4', '4', 'shaun', '601', '3702', '元婴期', '1.00', '1004', '4', '0', '2025-11-07 14:43:06', '65', '3010', '1810', '12100', '6050', '610', '2025-11-07 14:43:06', '2025-11-12 17:09:17', '0', '0', '0', '0', '0', '', '2025-11-12 17:00:20.228000', '2025-11-12 17:09:17.042000', '0');
INSERT INTO `player_profiles` VALUES ('5', '5', 'shaun1', '1', '31', '练气期', '1.00', '1000', '0', '0', '2025-11-12 11:05:59', '0', '10', '5', '100', '50', '10', '2025-11-12 11:05:59', '2025-11-12 11:06:53', '0', '0', '0', '0', '0', '\0', '2025-11-12 11:06:52.748000', '2025-11-12 11:06:32.957000', '100');

-- ----------------------------
-- Table structure for player_quests
-- ----------------------------
DROP TABLE IF EXISTS `player_quests`;
CREATE TABLE `player_quests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `quest_id` int NOT NULL,
  `progress` json DEFAULT NULL,
  `is_completed` tinyint(1) NOT NULL DEFAULT '0',
  `is_claimed` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `completed` bit(1) NOT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `current_progress` int NOT NULL,
  `reward_claimed` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_quest` (`player_id`,`quest_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_quest_id` (`quest_id`),
  KEY `idx_is_completed` (`is_completed`),
  KEY `idx_is_claimed` (`is_claimed`),
  CONSTRAINT `player_quests_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `player_quests_ibfk_2` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of player_quests
-- ----------------------------
INSERT INTO `player_quests` VALUES ('1', '4', '9', null, '0', '0', '2025-11-07 14:43:06', '2025-11-07 14:43:06', '\0', null, '0', '\0');
INSERT INTO `player_quests` VALUES ('2', '4', '5', null, '0', '0', '2025-11-07 14:43:06', '2025-11-07 14:43:06', '\0', null, '0', '\0');
INSERT INTO `player_quests` VALUES ('3', '4', '6', null, '0', '0', '2025-11-07 14:43:06', '2025-11-07 14:43:06', '\0', null, '0', '\0');
INSERT INTO `player_quests` VALUES ('4', '4', '7', null, '0', '0', '2025-11-07 14:43:06', '2025-11-07 14:43:06', '\0', null, '0', '\0');

-- ----------------------------
-- Table structure for player_skills
-- ----------------------------
DROP TABLE IF EXISTS `player_skills`;
CREATE TABLE `player_skills` (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `skill_id` int NOT NULL,
  `current_level` int NOT NULL DEFAULT '1',
  `experience` int NOT NULL DEFAULT '0',
  `is_equipped` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `equipped` bit(1) NOT NULL,
  `level` int NOT NULL,
  `slot_number` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_skill` (`player_id`,`skill_id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_skill_id` (`skill_id`),
  KEY `idx_is_equipped` (`is_equipped`),
  CONSTRAINT `player_skills_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player_profiles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `player_skills_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of player_skills
-- ----------------------------
INSERT INTO `player_skills` VALUES ('1', '4', '1', '1', '10', '0', '2025-11-12 11:04:26', '2025-11-12 11:04:26', '\0', '1', '0');

-- ----------------------------
-- Table structure for quests
-- ----------------------------
DROP TABLE IF EXISTS `quests`;
CREATE TABLE `quests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `requirements` json DEFAULT NULL,
  `rewards` json DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `required_amount` int NOT NULL,
  `reward_contribution_points` int NOT NULL,
  `reward_exp` int NOT NULL,
  `reward_spirit_stones` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_quest_type` (`type`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of quests
-- ----------------------------
INSERT INTO `quests` VALUES ('1', '初入修仙', '完成新手引导，开始修仙之路', 'main', '{\"level\": 1}', '{\"exp\": 100, \"money\": 100}', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '1', '100', '0', '0');
INSERT INTO `quests` VALUES ('2', '每日修炼', '完成今日的修炼任务', 'DAILY', '{\"cultivation_time\": 3600}', '{\"exp\": 50, \"money\": 50}', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '3600', '50', '50', '0');
INSERT INTO `quests` VALUES ('3', '收集灵草', '收集10株灵草', 'DAILY', '{\"collect_items\": {\"material\": 1, \"quantity\": 10}}', '{\"exp\": 30, \"money\": 30}', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '10', '30', '30', '0');
INSERT INTO `quests` VALUES ('4', '击败妖兽', '击败一只妖兽', 'DAILY', '{\"kill_monsters\": 1}', '{\"exp\": 80, \"money\": 80}', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '1', '80', '80', '0');
INSERT INTO `quests` VALUES ('5', '商店购物', '在商店购买一件物品', 'DAILY', '{\"buy_items\": 1}', '{\"exp\": 20, \"money\": 20}', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '1', '20', '20', '0');
INSERT INTO `quests` VALUES ('6', '初入修仙', '完成新手引导，开始修仙之路', 'DAILY', '{\"level\": 1}', '{\"exp\": 100, \"money\": 100}', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '1', '100', '0', '0');
INSERT INTO `quests` VALUES ('7', '每日修炼', '完成今日的修炼任务', 'DAILY', '{\"cultivation_time\": 3600}', '{\"exp\": 50, \"money\": 50}', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '3600', '50', '50', '0');
INSERT INTO `quests` VALUES ('8', '收集灵草', '收集10株灵草', 'DAILY', '{\"collect_items\": {\"material\": 1, \"quantity\": 10}}', '{\"exp\": 30, \"money\": 30}', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '10', '30', '30', '0');
INSERT INTO `quests` VALUES ('9', '击败妖兽', '击败一只妖兽', 'DAILY', '{\"kill_monsters\": 1}', '{\"exp\": 80, \"money\": 80}', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '1', '80', '80', '0');
INSERT INTO `quests` VALUES ('10', '商店购物', '在商店购买一件物品', 'DAILY', '{\"buy_items\": 1}', '{\"exp\": 20, \"money\": 20}', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '1', '20', '20', '0');

-- ----------------------------
-- Table structure for shop_items
-- ----------------------------
DROP TABLE IF EXISTS `shop_items`;
CREATE TABLE `shop_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_id` int DEFAULT NULL,
  `equipment_id` int DEFAULT NULL,
  `price` bigint NOT NULL,
  `stock` int NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `is_available` bit(1) NOT NULL,
  `price_contribution_points` int NOT NULL,
  `price_spirit_stones` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shop_type` (`shop_type`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_is_active` (`is_active`),
  CONSTRAINT `shop_items_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `shop_items_ibfk_2` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of shop_items
-- ----------------------------
INSERT INTO `shop_items` VALUES ('1', 'general', '1', null, '50', '100', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '50');
INSERT INTO `shop_items` VALUES ('2', 'general', '2', null, '50', '100', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '50');
INSERT INTO `shop_items` VALUES ('3', 'general', '5', null, '10', '500', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '10');
INSERT INTO `shop_items` VALUES ('4', 'general', '6', null, '100', '100', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '100');
INSERT INTO `shop_items` VALUES ('5', 'skill', null, null, '1000', '10', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '1000');
INSERT INTO `shop_items` VALUES ('6', 'skill', null, null, '2000', '5', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '2000');
INSERT INTO `shop_items` VALUES ('7', 'skill', null, null, '5000', '1', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '5000');
INSERT INTO `shop_items` VALUES ('8', 'equipment', null, '1', '100', '50', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '100');
INSERT INTO `shop_items` VALUES ('9', 'equipment', null, '2', '150', '50', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '150');
INSERT INTO `shop_items` VALUES ('10', 'equipment', null, '6', '500', '20', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '500');
INSERT INTO `shop_items` VALUES ('11', 'equipment', null, '11', '1200', '10', '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33', '\0', '0', '1200');
INSERT INTO `shop_items` VALUES ('12', 'general', '1', null, '50', '100', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '50');
INSERT INTO `shop_items` VALUES ('13', 'general', '2', null, '50', '100', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '50');
INSERT INTO `shop_items` VALUES ('14', 'general', '5', null, '10', '500', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '10');
INSERT INTO `shop_items` VALUES ('15', 'general', '6', null, '100', '100', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '100');
INSERT INTO `shop_items` VALUES ('16', 'skill', null, null, '1000', '10', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '1000');
INSERT INTO `shop_items` VALUES ('17', 'skill', null, null, '2000', '5', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '2000');
INSERT INTO `shop_items` VALUES ('18', 'skill', null, null, '5000', '1', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '5000');
INSERT INTO `shop_items` VALUES ('19', 'equipment', null, '1', '100', '50', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '100');
INSERT INTO `shop_items` VALUES ('20', 'equipment', null, '2', '150', '50', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '150');
INSERT INTO `shop_items` VALUES ('21', 'equipment', null, '6', '500', '20', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '500');
INSERT INTO `shop_items` VALUES ('22', 'equipment', null, '11', '1200', '10', '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50', '\0', '0', '1200');

-- ----------------------------
-- Table structure for skills
-- ----------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `level` int NOT NULL,
  `max_level` int NOT NULL,
  `base_damage` double NOT NULL,
  `damage_per_level` double NOT NULL,
  `cooldown` int NOT NULL,
  `mana_cost` int NOT NULL,
  `skill_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `element` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `unlock_level` int NOT NULL,
  `required_spirit_stones` int DEFAULT '0' COMMENT '需要的灵石数量',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `animation` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_skill_type` (`skill_type`),
  KEY `idx_element` (`element`),
  KEY `idx_unlock_level` (`unlock_level`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of skills
-- ----------------------------
INSERT INTO `skills` VALUES ('1', '基础功法', '提升基础修炼速度', '1', '100', '0.05', '0.01', '0', '0', 'cultivation', '无', '1', '0', NULL, NULL, '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `skills` VALUES ('2', '火球术', '基础火系攻击法术', '1', '50', '10', '2', '5', '10', 'attack', '火', '5', '0', NULL, NULL, '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `skills` VALUES ('3', '治疗术', '恢复生命值的法术', '1', '30', '20', '1.5', '8', '15', 'heal', '木', '3', '0', NULL, NULL, '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `skills` VALUES ('4', '水盾术', '创造一个水盾，减少受到的伤害', '1', '10', '0', '0', '10', '15', 'defense', '水', '8', '0', NULL, NULL, '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `skills` VALUES ('5', '地刺术', '从地面召唤尖刺，对敌人造成土属性伤害', '1', '10', '25', '10', '5', '20', 'attack', '土', '12', '0', NULL, NULL, '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `skills` VALUES ('6', '风刃术', '释放锋利的风刃，对敌人造成风属性伤害', '1', '10', '15', '7', '2', '8', 'attack', '风', '10', '0', NULL, NULL, '1', '2025-10-31 18:37:33', '2025-10-31 18:37:33');
INSERT INTO `skills` VALUES ('7', '基础功法', '提升基础修炼速度', '1', '100', '0.05', '0.01', '0', '0', 'cultivation', '无', '1', '0', NULL, NULL, '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `skills` VALUES ('8', '火球术', '基础火系攻击法术', '1', '50', '10', '2', '5', '10', 'attack', '火', '5', '0', NULL, NULL, '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `skills` VALUES ('9', '治疗术', '恢复生命值的法术', '1', '30', '20', '1.5', '8', '15', 'heal', '木', '3', '0', NULL, NULL, '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `skills` VALUES ('10', '水盾术', '创造一个水盾，减少受到的伤害', '1', '10', '0', '0', '10', '15', 'defense', '水', '8', '0', NULL, NULL, '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `skills` VALUES ('11', '地刺术', '从地面召唤尖刺，对敌人造成土属性伤害', '1', '10', '25', '10', '5', '20', 'attack', '土', '12', '0', NULL, NULL, '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50');
INSERT INTO `skills` VALUES ('12', '风刃术', '释放锋利的风刃,对敌人造成风属性伤害', '1', '10', '15', '7', '2', '8', 'attack', '风', '10', '0', NULL, NULL, '1', '2025-10-31 18:37:50', '2025-10-31 18:37:50');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_username` (`username`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('4', 'shaun', '$2a$10$3VYcas8oeb7vWVzVzpEbquZBO/ZHRD3CuXRghlWa.sKEhx8YDSlFO', 'shaun88@88.com', '2025-11-07 14:43:06', '2025-11-12 16:59:47');
INSERT INTO `users` VALUES ('5', 'shaun1', '$2a$10$MJnZPdecBDHriZlvYTPZXeX2r64y14AyX26OGc7XT9cLKCSjrFMhG', 'shaun18@88.com', '2025-11-12 11:05:59', '2025-11-12 11:06:03');

-- ----------------------------
-- View structure for v_player_summary
-- ----------------------------
DROP VIEW IF EXISTS `v_player_summary`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `v_player_summary` AS select `pp`.`id` AS `id`,`pp`.`user_id` AS `user_id`,`u`.`username` AS `username`,`pp`.`nickname` AS `nickname`,`pp`.`level` AS `level`,`pp`.`exp` AS `exp`,`pp`.`realm` AS `realm`,`pp`.`spirit_stones` AS `spirit_stones`,`pp`.`cultivation_points` AS `cultivation_points`,`pp`.`contribution_points` AS `contribution_points`,`pp`.`attack` AS `attack`,`pp`.`defense` AS `defense`,`pp`.`health` AS `health`,`pp`.`mana` AS `mana`,`pp`.`speed` AS `speed`,`pp`.`total_cultivation_time` AS `total_cultivation_time`,`pp`.`last_online_time` AS `last_online_time`,`pp`.`created_at` AS `created_at`,`pp`.`updated_at` AS `updated_at`,(`pp`.`attack` + coalesce(`equ`.`attack_bonus`,0)) AS `total_attack`,(`pp`.`defense` + coalesce(`equ`.`defense_bonus`,0)) AS `total_defense`,(`pp`.`health` + coalesce(`equ`.`health_bonus`,0)) AS `total_health`,(`pp`.`mana` + coalesce(`equ`.`mana_bonus`,0)) AS `total_mana`,(`pp`.`speed` + coalesce(`equ`.`speed_bonus`,0)) AS `total_speed` from ((`player_profiles` `pp` join `users` `u` on((`pp`.`user_id` = `u`.`id`))) left join (select `pe`.`player_id` AS `player_id`,sum(`e`.`attack_bonus`) AS `attack_bonus`,sum(`e`.`defense_bonus`) AS `defense_bonus`,sum(`e`.`health_bonus`) AS `health_bonus`,sum(`e`.`mana_bonus`) AS `mana_bonus`,sum(`e`.`speed_bonus`) AS `speed_bonus` from (`player_equipment` `pe` join `equipments` `e` on((`pe`.`equipment_id` = `e`.`id`))) where (`pe`.`is_equipped` = true) group by `pe`.`player_id`) `equ` on((`pp`.`id` = `equ`.`player_id`))) ;
