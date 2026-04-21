-- --------------------------------------------------------
-- 添加 game_configs 表
-- 日期：2026-04-21
-- 说明：创建游戏配置表，用于存储游戏运行时的各种配置项
-- --------------------------------------------------------

-- 创建游戏配置表
CREATE TABLE IF NOT EXISTS `game_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置 ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STRING' COMMENT '配置类型：STRING/INT/LONG/DOUBLE/BOOLEAN/JSON',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置描述',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'GENERAL' COMMENT '配置分类：GENERAL/COMBAT/CULTIVATION/SYSTEM/EVENT',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_config_key` (`config_key`) USING BTREE COMMENT '配置键唯一索引',
  KEY `idx_category` (`category`) USING BTREE COMMENT '分类索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏配置表' ROW_FORMAT=DYNAMIC;

-- 初始化默认配置数据
INSERT INTO `game_configs` (`config_key`, `config_value`, `config_type`, `description`, `category`) VALUES
('game.max_offline_hours', '24', 'INT', '最大离线时长（小时）', 'SYSTEM'),
('game.max_level', '1000', 'INT', '游戏最大等级', 'SYSTEM'),
('combat.base_exp', '10', 'INT', '战斗基础经验', 'COMBAT'),
('combat.drop_rate', '1.0', 'DOUBLE', '掉落率倍数', 'COMBAT'),
('cultivation.base_exp_per_second', '1.0', 'DOUBLE', '修炼基础经验/秒', 'CULTIVATION'),
('cultivation.max_time_hours', '24', 'INT', '最大修炼时长（小时）', 'CULTIVATION'),
('cultivation.spirit_stones_rate', '0.1', 'DOUBLE', '灵石获取率（个/秒）', 'CULTIVATION'),
('guild.max_members', '50', 'INT', '公会最大成员数', 'SYSTEM'),
('guild.creation_cost', '10000', 'INT', '公会创建消耗灵石', 'SYSTEM'),
('shop.refresh_interval_hours', '24', 'INT', '商店刷新间隔（小时）', 'SYSTEM')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- --------------------------------------------------------
-- 迁移完成
-- --------------------------------------------------------
