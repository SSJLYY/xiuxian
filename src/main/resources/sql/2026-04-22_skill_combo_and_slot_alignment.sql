UPDATE `player_skills`
SET `slot_number` = 0,
    `equipped` = 0
WHERE `slot_number` < 0 OR `slot_number` > 2;

DELETE FROM `player_skill_combo_records`
WHERE `used_at` < NOW() - INTERVAL 10 MINUTE;
