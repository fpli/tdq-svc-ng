-- domain weight config table
DROP TABLE IF EXISTS `t_email_cfg`;
CREATE TABLE `t_email_cfg`
(
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `subject`     VARCHAR(255)  NOT NULL,
    `recipient`   VARCHAR(255)  NOT NULL,
    `cc`          VARCHAR(255)  NOT NULL,
    `created_by`  VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP     NULL                      DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP     NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1000;

-- 1000 enable 6 domains for MVP version
INSERT INTO `t_email_cfg` (subject, recipient, cc)
VALUES ('TDQ Alerts - Page Profiling Abnormal Alert(Customer)', 'DL-eBay-PA-dev-CCOE@ebay.com', 'DL-eBay-Tracking-Behavior-Data@ebay.com');

-- 1001 DL-eBay-Tracking-Data-Quality@ebay.com
INSERT INTO `t_email_cfg` (subject, recipient, cc)
VALUES ('TDQ Alerts - Page Profiling Abnormal Alert', 'DL-eBay-Tracking-Data-Quality@ebay.com', 'fangpli@ebay.com, yxiao6@ebay.com');

-- 1002 TDQ Alerts - UTP Daily Click Volume
INSERT INTO `t_email_cfg` (subject, recipient, cc)
VALUES ('TDQ Alerts - UTP Daily Click Volume', 'DL-eBay-Tracking-Data-Quality-Dev@ebay.com', 'fangpli@ebay.com');