DROP TABLE IF EXISTS `t_metric_info`;

CREATE TABLE `t_metric_info`
(
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `metric_key`        VARCHAR(255)  NOT NULL,
    `metric_name`       VARCHAR(255)  NOT NULL,
    `description`       VARCHAR(1000) NULL,
    `stage`             VARCHAR(255)  NULL,
    `category`          VARCHAR(255)  NULL,
    `level`             VARCHAR(255)  NOT NULL,
    `source`            VARCHAR(255)  NULL,
    `value_type`        TINYINT       NOT NULL,
    `collect_interval`  VARCHAR(100)  NOT NULL,
    `dimension`         VARCHAR(255)  NULL ,
    `dimension_src_tbl` VARCHAR(255)  NULL,
    `dimension_val_col` VARCHAR(255)  NULL,
    `status`            TINYINT       NOT NULL DEFAULT 1,
    `version`           INT           NOT NULL DEFAULT 1,
    `created_by`        VARCHAR(255)  NULL COMMENT 'Created By',
    `updated_by`        VARCHAR(255)  NULL COMMENT 'Updated By',
    `create_time`       TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time`       TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY metric_key_uni (`metric_key`)
) AUTO_INCREMENT = 1000;