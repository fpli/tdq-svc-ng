DROP TABLE IF EXISTS `t_report_metadata_detail`;

CREATE TABLE `t_report_metadata_detail`
(
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `metric_id`           BIGINT        NOT NULL,
    `domain`              VARCHAR(255)  NOT NULL,
    `event_type`          VARCHAR(255)  NOT NULL,
    `page_ids`            VARCHAR(1000) NULL,
    `metadata_type`       VARCHAR(255)  NOT NULL,
    `metadata_id`         VARCHAR(255)  NULL,
    `metadata_name`       VARCHAR(255)  NULL,
    `metadata_desc`       VARCHAR(255)  NULL,
    `element_instance_id` VARCHAR(1000) NULL,
    `sample_path`         VARCHAR(1000) NULL,
    `sample_element`      VARCHAR(1000) NULL,
    `exp`                 VARCHAR(100)  NOT NULL,
    `traffic_cnt`         BIGINT        NOT NULL,
    `traffic_p_cnt`       BIGINT        NOT NULL,
    `sample_url`          VARCHAR(1000) NULL,
    `dt`                  DATE          NOT NULL,
    `created_by`          VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`          VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time`         TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time`         TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1000;