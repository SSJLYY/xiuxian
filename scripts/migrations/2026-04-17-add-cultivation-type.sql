-- =====================================================
-- 玩家前台 UI Bug 修复 - 数据库迁移脚本
-- 执行日期：2026-04-17
-- 作者：shaun.sheng
-- =====================================================

-- Bug #3: 添加 cultivation_type 字段到 player_profiles 表
-- 用于存储玩家选择的修炼类型（normal/intensive/meditation）
ALTER TABLE player_profiles 
ADD COLUMN IF NOT EXISTS cultivation_type VARCHAR(20) DEFAULT 'normal' COMMENT '修炼类型：normal-普通，intensive-闭关，meditation-冥想'
AFTER cultivation_speed;

-- 为现有数据设置默认值
UPDATE player_profiles 
SET cultivation_type = 'normal' 
WHERE cultivation_type IS NULL;

-- 添加索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_cultivation_type ON player_profiles(cultivation_type);

-- 验证迁移结果
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    COLUMN_DEFAULT, 
    IS_NULLABLE, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'player_profiles'
  AND COLUMN_NAME = 'cultivation_type';

-- =====================================================
-- 迁移完成
-- 受影响的表：player_profiles
-- 新增字段：cultivation_type VARCHAR(20) DEFAULT 'normal'
-- 影响行数：所有现有玩家（设置为默认值'normal'）
-- =====================================================
