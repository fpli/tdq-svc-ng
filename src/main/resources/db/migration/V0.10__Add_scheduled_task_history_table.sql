DROP TABLE IF EXISTS `scheduled_task_history`;

CREATE TABLE `scheduled_task_history`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `task`          VARCHAR(255) NOT NULL,
    `host`          VARCHAR(255) NOT NULL,
    `start_time`    TIMESTAMP    NOT NULL,
    `end_time`      TIMESTAMP    NULL,
    `running_sec`   BIGINT       NULL,
    `status`        VARCHAR(255) NOT NULL,
    `error_msg`     VARCHAR(255) NULL,
    `error_details` TEXT         NULL,
    `create_time`   TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time`   TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
)