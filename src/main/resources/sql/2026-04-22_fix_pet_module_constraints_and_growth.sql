ALTER TABLE `player_pet_evolution`
ADD INDEX `idx_player_pet_evolution_player_pet_id` (`player_pet_id`);

ALTER TABLE `player_pet_evolution`
DROP INDEX `uk_player_pet_evolution`;

ALTER TABLE `player_pet_evolution`
ADD UNIQUE INDEX `uk_player_pet_evolution_stage` (`player_pet_id`, `current_stage`);

UPDATE `player_pets` pp
JOIN `pets` p ON p.`id` = pp.`pet_id`
SET pp.`exp_to_next` = CASE
    WHEN pp.`exp_to_next` IS NULL OR pp.`exp_to_next` <= 0 THEN 100 + (pp.`level` - 1) * 20
    ELSE pp.`exp_to_next`
END,
pp.`attack` = CASE WHEN pp.`attack` = 0 THEN p.`base_attack` + GREATEST(pp.`level` - 1, 0) * 2 ELSE pp.`attack` END,
pp.`defense` = CASE WHEN pp.`defense` = 0 THEN p.`base_defense` + GREATEST(pp.`level` - 1, 0) * 1 ELSE pp.`defense` END,
pp.`health` = CASE WHEN pp.`health` = 0 THEN p.`base_health` + GREATEST(pp.`level` - 1, 0) * 10 ELSE pp.`health` END,
pp.`max_health` = CASE WHEN pp.`max_health` = 0 THEN p.`base_health` + GREATEST(pp.`level` - 1, 0) * 10 ELSE pp.`max_health` END,
pp.`speed` = CASE WHEN pp.`speed` = 0 THEN p.`base_speed` + GREATEST(pp.`level` - 1, 0) * 1 ELSE pp.`speed` END;
