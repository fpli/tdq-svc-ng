-- Scorecard domain lkp tables

-- domain lkp table
DROP TABLE IF EXISTS `t_scorecard_domain_lkp`;
CREATE TABLE `t_scorecard_domain_lkp`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `name`        VARCHAR(255) NOT NULL,
    `created_by`  VARCHAR(255) NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255) NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY name_uni (`name`)
) AUTO_INCREMENT = 1000;


-- domain weight config table
DROP TABLE IF EXISTS `t_scorecard_domain_weight_cfg`;
CREATE TABLE `t_scorecard_domain_weight_cfg`
(
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `domain_name` VARCHAR(255)  NOT NULL,
    `rule_id`     BIGINT        NOT NULL,
    `weight`      DECIMAL(3, 2) NOT NULL,
    `created_by`  VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP     NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP     NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`),
    FOREIGN KEY fk_rule_id (`rule_id`) REFERENCES t_scorecard_groovy_rule_def (`id`)
) AUTO_INCREMENT = 1000;

-- enable 6 domains for MVP version
INSERT INTO `t_scorecard_domain_lkp` (`name`)
VALUES ('Checkout'),
       ('HomePage'),
       ('ViewItem'),
       ('MyEbay'),
       ('SignIn'),
       ('Search');

