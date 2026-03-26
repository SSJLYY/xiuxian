-- ============================================================
-- 并发安全修复 - 乐观锁版本字段迁移
-- @author shaun.sheng
-- @date 2026-03-26
-- ============================================================

-- 1. player_profiles 添加乐观锁 version 字段
ALTER TABLE player_profiles ADD COLUMN IF NOT EXISTS `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';

-- 2. guilds 添加乐观锁 version 字段
ALTER TABLE guilds ADD COLUMN IF NOT EXISTS `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';

-- 3. guild_bosses 添加乐观锁 version 字段
ALTER TABLE guild_bosses ADD COLUMN IF NOT EXISTS `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';

-- 4. guild_boss_challenges 添加乐观锁 version 字段
ALTER TABLE guild_boss_challenges ADD COLUMN IF NOT EXISTS `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';
