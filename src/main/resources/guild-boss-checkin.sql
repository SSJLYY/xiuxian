-- =====================================================
-- 宗门BOSS系统 & 每日签到系统 建表脚本
-- 2026-03-24
-- =====================================================

-- 宗门BOSS表
CREATE TABLE IF NOT EXISTS `guild_bosses` (
    `id`                   INT          NOT NULL AUTO_INCREMENT,
    `name`                 VARCHAR(64)  NOT NULL COMMENT 'BOSS名称',
    `description`          VARCHAR(512) DEFAULT NULL COMMENT 'BOSS描述',
    `level`                INT          NOT NULL DEFAULT 5 COMMENT 'BOSS等级',
    `max_health`           BIGINT       NOT NULL DEFAULT 500000 COMMENT '最大生命值',
    `current_health`       BIGINT       NOT NULL DEFAULT 500000 COMMENT '当前生命值',
    `attack`               INT          NOT NULL DEFAULT 1000,
    `defense`              INT          NOT NULL DEFAULT 200,
    `guild_id`             INT          NOT NULL COMMENT '所属宗门',
    `status`               VARCHAR(16)  NOT NULL DEFAULT 'ALIVE' COMMENT 'ALIVE/DEFEATED',
    `reward_spirit_stones` INT          NOT NULL DEFAULT 5000 COMMENT '灵石奖励总量',
    `reward_exp`           INT          NOT NULL DEFAULT 8000 COMMENT '经验奖励总量',
    `reward_item_id`       INT          DEFAULT NULL COMMENT '特殊道具奖励',
    `spawned_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `defeated_at`          DATETIME     DEFAULT NULL,
    `next_spawn_at`        DATETIME     DEFAULT NULL COMMENT '下次刷新时间',
    `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_guild_status` (`guild_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宗门BOSS';

-- 宗门BOSS挑战记录
CREATE TABLE IF NOT EXISTS `guild_boss_challenges` (
    `id`                    INT     NOT NULL AUTO_INCREMENT,
    `boss_id`               INT     NOT NULL,
    `player_id`             INT     NOT NULL,
    `damage_dealt`          BIGINT  NOT NULL DEFAULT 0 COMMENT '累计造成伤害',
    `today_attempts`        INT     NOT NULL DEFAULT 0 COMMENT '今日挑战次数',
    `last_challenge_at`     DATETIME DEFAULT NULL,
    `reward_claimed`        TINYINT(1) NOT NULL DEFAULT 0,
    `personal_reward_stones` INT    DEFAULT NULL COMMENT '按贡献分配的灵石',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_boss_player` (`boss_id`, `player_id`),
    KEY `idx_boss_damage` (`boss_id`, `damage_dealt` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宗门BOSS挑战记录';

-- 玩家每日签到记录
CREATE TABLE IF NOT EXISTS `player_check_ins` (
    `id`                    INT     NOT NULL AUTO_INCREMENT,
    `player_id`             INT     NOT NULL,
    `check_in_date`         DATETIME NOT NULL COMMENT '签到日期（当天00:00:00）',
    `consecutive_days`      INT     NOT NULL DEFAULT 1 COMMENT '当时连续签到天数',
    `reward_spirit_stones`  INT     NOT NULL DEFAULT 0 COMMENT '本次获得灵石',
    `reward_exp`            INT     NOT NULL DEFAULT 0 COMMENT '本次获得经验',
    `is_makeup`             TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否补签',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_player_date` (`player_id`, `check_in_date`),
    KEY `idx_player_date` (`player_id`, `check_in_date` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家签到记录';
