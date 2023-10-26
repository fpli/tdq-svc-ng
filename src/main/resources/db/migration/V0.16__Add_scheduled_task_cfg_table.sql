CREATE TABLE IF NOT EXISTS `scheduled_task_cfg`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `task`        VARCHAR(255) NOT NULL,
    `inactive`    TINYINT      NOT NULL,
    `created_by`  VARCHAR(255) NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255) NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
)