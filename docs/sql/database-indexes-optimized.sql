-- ============================================
-- 修仙游戏数据库索引优化 SQL 脚本
-- 适配 2H4G 个人服务器环境
-- 作者: shaun.sheng
-- 创建日期: 2026-03-24
-- ============================================

-- 注意：执行前请先备份数据库！
-- 执行命令：mysql -u root -p xiuxian_game < database-indexes-optimized.sql

-- ============================================
-- 玩家表索引优化（高频查询）
-- ============================================

-- 玩家按用户ID查询（认证后查找玩家）
CREATE INDEX idx_players_user_id ON players(user_id, deleted_at);

-- 玩家按境界等级排序（排行榜）
CREATE INDEX idx_players_realm_level ON players(realm_id, level DESC, deleted_at);

-- 玩家按修炼时间排序（排行榜）
CREATE INDEX idx_players_cultivate_time ON players(cultivation_time DESC, deleted_at);

-- ============================================
-- 战斗表索引优化（高频写入）
-- ============================================

-- 玩家战斗记录按时间查询
CREATE INDEX idx_combat_player_time ON combat_logs(player_id, create_time DESC);

-- 战斗按怪物查询
CREATE INDEX idx_combat_monster_time ON combat_logs(monster_id, create_time DESC);

-- ============================================
-- 排行榜索引优化（全表扫描）
-- ============================================

-- 玩家战力排行榜
CREATE INDEX idx_ranking_power ON player_stats(power DESC);

-- 玩家等级排行榜
CREATE INDEX idx_ranking_level ON player_stats(level DESC);

-- 综合排行榜（战力+等级）
CREATE INDEX idx_ranking_comprehensive ON player_stats(power DESC, level DESC, player_id);

-- ============================================
-- 拍卖行索引优化（并发查询）
-- ============================================

-- 拍卖物品按状态和时间查询
CREATE INDEX idx_auction_status_time ON auctions(status, end_time);

-- 拍卖物品按物品ID和价格查询
CREATE INDEX idx_auction_item_price ON auctions(item_id, price ASC);

-- 卖家拍卖记录
CREATE INDEX idx_auction_seller_time ON auctions(seller_id, create_time DESC);

-- ============================================
-- 宗门BOSS索引优化（多玩家同时挑战）
-- ============================================

-- 宗门BOSS挑战记录
CREATE INDEX idx_guild_boss_time ON guild_boss_records(guild_id, create_time);

-- 玩家挑战BOSS记录
CREATE INDEX idx_guild_boss_player_time ON guild_boss_records(player_id, create_time DESC);

-- ============================================
-- 邮件表索引优化（未读邮件查询）
-- ============================================

-- 未读邮件查询
CREATE INDEX idx_mail_unread ON mails(receiver_id, read_status, create_time DESC);

-- 玩家邮件列表
CREATE INDEX idx_mail_player_time ON mails(receiver_id, create_time DESC);

-- ============================================
-- 宠物表索引优化
-- ============================================

-- 玩家宠物查询
CREATE INDEX idx_pet_player ON player_pets(player_id, status);

-- 宠物进化查询
CREATE INDEX idx_pet_evolution ON pet_evolutions(player_pet_id, create_time DESC);

-- ============================================
-- 任务表索引优化
-- ============================================

-- 玩家任务进度
CREATE INDEX idx_quest_player_status ON quest_progresses(player_id, status, quest_type);

-- ============================================
-- 技能表索引优化
-- ============================================

-- 玩家技能列表
CREATE INDEX idx_skill_player_level ON player_skills(player_id, skill_level DESC);

-- ============================================
-- 完成提示
-- ============================================

SELECT '✅ 索引创建完成！' AS status;
