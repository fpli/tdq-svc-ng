DROP TABLE IF EXISTS `t_report_metadata_summary`;

CREATE TABLE `t_report_metadata_summary`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `metric_id`      BIGINT       NOT NULL,
    `domain`         VARCHAR(255) NOT NULL,
    `metadata_type`  VARCHAR(255) NOT NULL,
    `event_type`     VARCHAR(255) NOT NULL,
    `all_experience` BIGINT       NOT NULL,
    `dweb`           BIGINT       NOT NULL,
    `mweb`           BIGINT       NOT NULL,
    `webview`        BIGINT       NOT NULL,
    `android`        BIGINT       NOT NULL,
    `ios`            BIGINT       NOT NULL,
    `dt`             DATE         NOT NULL,
    `created_by`     VARCHAR(255) NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`     VARCHAR(255) NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time`    TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time`    TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1000;