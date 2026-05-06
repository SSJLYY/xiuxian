-- Fix rankings queries against reserved column names and harden map progress uniqueness.

START TRANSACTION;

DELETE p1
FROM player_map_progress p1
INNER JOIN player_map_progress p2
    ON p1.player_id = p2.player_id
   AND p1.map_id = p2.map_id
   AND p1.id < p2.id;

ALTER TABLE player_map_progress
    ADD UNIQUE INDEX uk_player_map_progress (player_id, map_id);

COMMIT;
