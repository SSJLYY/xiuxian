-- =====================================================
-- 添加 cultivation_type 字段到 player_profiles 表
-- 执行日期：2026-04-17
-- 兼容 MySQL 8.0.x（所有小版本）
-- =====================================================

DELIMITER $$

-- 创建添加字段的存储过程
CREATE PROCEDURE add_cultivation_type_if_not_exists(IN tableName VARCHAR(64), IN columnName VARCHAR(64))
BEGIN
    DECLARE column_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO column_count
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = tableName
      AND COLUMN_NAME = columnName;
    
    IF column_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, 
                         ' ADD COLUMN ', columnName, 
                         ' VARCHAR(20) DEFAULT ''normal'' COMMENT ''修炼类型：normal-普通，intensive-闭关，meditation-冥想'' AFTER cultivation_speed');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        SELECT CONCAT('成功添加 ', tableName, '.', columnName, ' 字段') AS result;
    ELSE
        SELECT CONCAT(tableName, '.', columnName, ' 字段已存在，跳过') AS result;
    END IF;
END$$

-- 创建添加索引的存储过程
CREATE PROCEDURE add_cultivation_type_index_if_not_exists()
BEGIN
    DECLARE index_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO index_count
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'player_profiles'
      AND INDEX_NAME = 'idx_cultivation_type';
    
    IF index_count = 0 THEN
        ALTER TABLE player_profiles ADD INDEX idx_cultivation_type(cultivation_type);
        SELECT '成功添加 idx_cultivation_type 索引' AS result;
    ELSE
        SELECT 'idx_cultivation_type 索引已存在，跳过' AS result;
    END IF;
END$$

DELIMITER ;

-- 执行存储过程添加字段
CALL add_cultivation_type_if_not_exists('player_profiles', 'cultivation_type');

-- 执行存储过程添加索引
CALL add_cultivation_type_index_if_not_exists();

-- 设置默认值
UPDATE player_profiles SET cultivation_type = 'normal' WHERE cultivation_type IS NULL;

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_cultivation_type_if_not_exists;
DROP PROCEDURE IF EXISTS add_cultivation_type_index_if_not_exists;

-- 验证
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'player_profiles'
  AND COLUMN_NAME = 'cultivation_type';
