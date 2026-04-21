-- =====================================================
-- 第十二轮性能优化：添加缺失的关键复合索引
-- 执行时间：2026-04-20
-- 基于实际存在的表结构
-- 兼容 MySQL 8.0.x（所有小版本）
-- =====================================================

-- 清理之前可能残留的存储过程
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DROP PROCEDURE IF EXISTS add_unique_index_if_not_exists;

DELIMITER $$

-- 创建添加索引的存储过程
CREATE PROCEDURE add_index_if_not_exists(IN idx_name VARCHAR(64), IN table_name VARCHAR(64), IN idx_def TEXT)
BEGIN
    DECLARE table_count INT DEFAULT 0;
    DECLARE index_count INT DEFAULT 0;
    
    -- 检查表是否存在
    SELECT COUNT(*) INTO table_count
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name;
    
    IF table_count = 0 THEN
        SELECT CONCAT('表 ', table_name, ' 不存在，跳过') AS result;
    ELSE
        -- 检查索引是否已存在
        SELECT COUNT(*) INTO index_count
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = table_name
          AND INDEX_NAME = idx_name;
        
        IF index_count = 0 THEN
            SET @sql = CONCAT('ALTER TABLE ', table_name, ' ADD INDEX ', idx_name, ' ', idx_def);
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            SELECT CONCAT('成功添加 ', table_name, '.', idx_name) AS result;
        ELSE
            SELECT CONCAT(idx_name, ' 索引已存在，跳过') AS result;
        END IF;
    END IF;
END$$

-- 创建添加唯一索引的存储过程
CREATE PROCEDURE add_unique_index_if_not_exists(IN idx_name VARCHAR(64), IN table_name VARCHAR(64), IN idx_def TEXT)
BEGIN
    DECLARE table_count INT DEFAULT 0;
    DECLARE index_count INT DEFAULT 0;
    
    -- 检查表是否存在
    SELECT COUNT(*) INTO table_count
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name;
    
    IF table_count = 0 THEN
        SELECT CONCAT('表 ', table_name, ' 不存在，跳过') AS result;
    ELSE
        -- 检查索引是否已存在
        SELECT COUNT(*) INTO index_count
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = table_name
          AND INDEX_NAME = idx_name;
        
        IF index_count = 0 THEN
            SET @sql = CONCAT('ALTER TABLE ', table_name, ' ADD UNIQUE INDEX ', idx_name, ' ', idx_def);
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            SELECT CONCAT('成功添加 ', table_name, '.', idx_name, ' (UNIQUE)') AS result;
        ELSE
            SELECT CONCAT(idx_name, ' 唯一索引已存在，跳过') AS result;
        END IF;
    END IF;
END$$

DELIMITER ;

-- ==================== 执行索引创建 ====================

-- 战斗日志表复合索引
CALL add_index_if_not_exists('idx_combat_player_time', 'combat_logs', '(player_id, created_at DESC)');

-- 玩家物品表复合索引
CALL add_index_if_not_exists('idx_player_item', 'player_items', '(player_id, item_id)');

-- 玩家邮件表复合索引
CALL add_index_if_not_exists('idx_player_status_time', 'player_mails', '(player_id, status, created_at DESC)');

-- 玩家技能表唯一索引
CALL add_unique_index_if_not_exists('uk_player_skill', 'player_skills', '(player_id, skill_id)');

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DROP PROCEDURE IF EXISTS add_unique_index_if_not_exists;

-- 验证
SELECT '验证新创建的索引:' AS message;
SHOW INDEX FROM combat_logs WHERE Key_name LIKE 'idx_%' OR Key_name LIKE 'uk_%';
SHOW INDEX FROM player_items WHERE Key_name LIKE 'idx_%' OR Key_name LIKE 'uk_%';
SHOW INDEX FROM player_mails WHERE Key_name LIKE 'idx_%' OR Key_name LIKE 'uk_%';
SHOW INDEX FROM player_skills WHERE Key_name LIKE 'idx_%' OR Key_name LIKE 'uk_%';

-- =====================================================
-- 迁移完成
-- 影响的表：combat_logs, player_items, player_mails, player_skills
-- =====================================================
