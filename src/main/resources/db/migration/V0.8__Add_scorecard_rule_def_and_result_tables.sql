-- Scorecard rule definition and result tables

-- Replace the `t_scorecard_check_item` with new table name
DROP TABLE IF EXISTS `t_scorecard_check_item`;

-- rule definition table
DROP TABLE IF EXISTS `t_scorecard_groovy_rule_def`;
CREATE TABLE `t_scorecard_groovy_rule_def`
(
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `name`           VARCHAR(255)  NOT NULL,
    `metric_keys`    VARCHAR(255)  NOT NULL,
    `category`       VARCHAR(255)  NOT NULL,
    `sub_category1`  VARCHAR(255)  NULL,
    `sub_category2`  VARCHAR(255)  NULL,
    `execute_order`  INT4          NULL,
    `default_weight` DECIMAL(3, 2) NOT NULL,
    `groovy_script`  VARCHAR(1000) NOT NULL,
    `created_by`     VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`     VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time`    TIMESTAMP     NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time`    TIMESTAMP     NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1000;


-- rule result table
DROP TABLE IF EXISTS `t_scorecard_rule_result`;
CREATE TABLE `t_scorecard_rule_result`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `domain`      VARCHAR(255) NOT NULL,
    `rule_id`     BIGINT       NOT NULL,
    `score`       INT          NOT NULL,
    `dt`          DATE         NOT NULL,
    `created_by`  VARCHAR(255) NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255) NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1000;


-- category result table
DROP TABLE IF EXISTS `t_scorecard_category_result`;
CREATE TABLE `t_scorecard_category_result`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `domain`      VARCHAR(255) NOT NULL,
    `category`    VARCHAR(255) NOT NULL,
    `sub_total`   INT          NOT NULL,
    `final_score` INT          NOT NULL,
    `dt`          DATE         NOT NULL,
    `created_by`  VARCHAR(255) NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255) NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP    NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1000;