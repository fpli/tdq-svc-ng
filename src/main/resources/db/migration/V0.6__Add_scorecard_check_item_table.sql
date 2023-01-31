DROP TABLE IF EXISTS `t_scorecard_check_item`;

CREATE TABLE `t_scorecard_check_item`
(
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `metric_key`    VARCHAR(255)  NOT NULL,
    `category`      VARCHAR(255)  NOT NULL,
    `execute_order` INT4          NOT NULL,
    `script`        VARCHAR(1000) NULL,
    `created_by`    VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`    VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time`   TIMESTAMP     NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time`   TIMESTAMP     NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1000;