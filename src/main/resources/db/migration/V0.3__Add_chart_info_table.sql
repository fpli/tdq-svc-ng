DROP TABLE IF EXISTS `t_chart_info`;

CREATE TABLE `t_chart_info`
(
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `name`        VARCHAR(255)  NOT NULL,
    `description` VARCHAR(1000) NULL,
    `mode`        TINYINT       NOT NULL,
    `metric_keys` VARCHAR(255)  NOT NULL,
    `exp`         VARCHAR(255)  NULL,
    `view_cfg`    JSON,
    `created_by`  VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1000;