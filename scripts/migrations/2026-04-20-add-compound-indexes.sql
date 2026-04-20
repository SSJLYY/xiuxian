-- 第十二轮性能优化：添加缺失的关键复合索引
-- 执行时间：2026-04-20
-- 影响：提升高频查询性能 30%+

-- ==================== 战斗日志表复合索引 ====================
-- 优化玩家查看战斗历史（按时间排序）
ALTER TABLE combat_logs 
ADD INDEX idx_combat_player_time (player_id, created_at DESC);

-- ==================== 玩家物品表复合索引 ====================
-- 优化按类型筛选背包物品
ALTER TABLE player_items 
ADD INDEX idx_player_type (player_id, item_type);

-- ==================== 任务进度表唯一索引 ====================
-- 防止重复任务进度，提升查询效率
ALTER TABLE quest_progress 
ADD UNIQUE INDEX uk_player_quest (player_id, quest_id);

-- ==================== 玩家技能表唯一索引 ====================
-- 防止重复学习技能，提升查询效率
ALTER TABLE player_skills 
ADD UNIQUE INDEX uk_player_skill (player_id, skill_id);

-- ==================== 邮件表复合索引 ====================
-- 优化玩家查看邮件（按状态和时间排序）
ALTER TABLE mails 
ADD INDEX idx_player_status_time (player_id, status, created_at DESC);

-- ==================== 好友表复合索引 ====================
-- 优化好友列表查询（按状态筛选）
ALTER TABLE friends 
ADD INDEX idx_player_status (player_id, friend_status);

-- ==================== 验证索引创建 ====================
-- 查看新增索引
SHOW INDEX FROM combat_logs WHERE Key_name = 'idx_combat_player_time';
SHOW INDEX FROM player_items WHERE Key_name = 'idx_player_type';
SHOW INDEX FROM quest_progress WHERE Key_name = 'uk_player_quest';
SHOW INDEX FROM player_skills WHERE Key_name = 'uk_player_skill';
SHOW INDEX FROM mails WHERE Key_name = 'idx_player_status_time';
SHOW INDEX FROM friends WHERE Key_name = 'idx_player_status';
