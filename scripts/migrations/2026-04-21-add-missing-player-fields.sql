-- =====================================================
-- 添加 player_profiles 表缺失的字段
-- 执行日期：2026-04-21
-- 兼容 MySQL 8.0.x
-- =====================================================

DELIMITER $$

-- 通用添加字段存储过程
CREATE PROCEDURE add_column_if_not_exists(IN table_name VARCHAR(64), IN column_name VARCHAR(64), IN column_def TEXT, IN after_column VARCHAR(64))
BEGIN
    DECLARE column_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO column_count
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND COLUMN_NAME = column_name;
    
    IF column_count = 0 THEN
        IF after_column IS NOT NULL AND after_column != '' THEN
            SET @sql = CONCAT('ALTER TABLE ', table_name, 
                             ' ADD COLUMN ', column_name, ' ', column_def,
                             ' AFTER ', after_column);
        ELSE
            SET @sql = CONCAT('ALTER TABLE ', table_name, 
                             ' ADD COLUMN ', column_name, ' ', column_def);
        END IF;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('成功添加 ', table_name, '.', column_name) AS result;
    ELSE
        SELECT CONCAT(column_name, ' 字段已存在，跳过') AS result;
    END IF;
END$$

DELIMITER ;

-- 添加 avatar 字段
CALL add_column_if_not_exists('player_profiles', 'avatar', 'VARCHAR(255) DEFAULT NULL COMMENT ''玩家头像 URL''', 'nickname');

-- 添加 total_battles 字段
CALL add_column_if_not_exists('player_profiles', 'total_battles', 'INT DEFAULT 0 COMMENT ''总战斗次数''', 'last_online_time');

-- 添加 max_health 字段
CALL add_column_if_not_exists('player_profiles', 'max_health', 'INT DEFAULT 100 COMMENT ''最大生命值''', 'health');

-- 添加 max_mana 字段
CALL add_column_if_not_exists('player_profiles', 'max_mana', 'INT DEFAULT 50 COMMENT ''最大法力值''', 'mana');

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- 验证
SELECT '=== 验证新添加的字段 ===' AS message;
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'player_profiles'
  AND COLUMN_NAME IN ('avatar', 'total_battles', 'max_health', 'max_mana');


ALTER TABLE player_profiles ADD COLUMN avatar VARCHAR(255) DEFAULT NULL COMMENT '玩家头像 URL' AFTER nickname;
ALTER TABLE player_profiles ADD COLUMN total_battles INT DEFAULT 0 COMMENT '总战斗次数' AFTER last_online_time;
ALTER TABLE player_profiles ADD COLUMN max_health INT DEFAULT 100 COMMENT '最大生命值' AFTER health;
ALTER TABLE player_profiles ADD COLUMN max_mana INT DEFAULT 50 COMMENT '最大法力值' AFTER mana;
-- =====================================================
-- 迁移完成
-- =====================================================
