CREATE TABLE IF NOT EXISTS `t_alert_suppression_page_cfg`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `page_id`       INT          NOT NULL,
    `suppress_util` TIMESTAMP    NOT NULL,
    `created_by`    VARCHAR(255) NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`    VARCHAR(255) NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time`   TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time`   TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
)