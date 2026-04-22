ALTER TABLE `player_profiles`
ADD COLUMN `breakthrough_cooldown_until` timestamp NULL DEFAULT NULL COMMENT '境界突破冷却结束时间' AFTER `last_cultivation_end`;

CREATE INDEX `idx_player_profiles_breakthrough_cooldown`
ON `player_profiles` (`breakthrough_cooldown_until`);

UPDATE `player_profiles`
SET `breakthrough_cooldown_until` = NULL
WHERE `breakthrough_cooldown_until` IS NOT NULL;
