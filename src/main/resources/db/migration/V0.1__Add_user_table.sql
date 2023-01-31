DROP TABLE IF EXISTS `t_user`;

CREATE TABLE `t_user`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `username`        VARCHAR(255) NOT NULL,
    `first_name`      VARCHAR(255) NULL,
    `last_name`       VARCHAR(255) NULL,
    `email`           VARCHAR(255) NULL,
    `roles`           VARCHAR(255) NOT NULL,
    `is_active`       TINYINT      NOT NULL,
    `last_login_time` TIMESTAMP    NOT NULL,
    `created_by`      VARCHAR(255) NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`      VARCHAR(255) NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time`     TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time`     TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY username_uni (`username`)
) AUTO_INCREMENT = 1000;