UPDATE `player_pets`
SET `last_train_time` = NULL
WHERE `last_train_time` IS NOT NULL
  AND `last_train_time` > NOW();

UPDATE `pet_evolution`
SET `required_item_id` = 21,
    `required_item_quantity` = 1
WHERE `required_item_id` IS NULL OR `required_item_quantity` IS NULL OR `required_item_quantity` <= 0;

UPDATE `pet_evolution`
SET `required_item_quantity` = 1
WHERE `required_item_quantity` <= 0 OR `required_item_quantity` IS NULL;
