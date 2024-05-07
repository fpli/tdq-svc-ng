
create table if not exists `t_tag_blacklist` (
    `id`          INT           PRIMARY KEY AUTO_INCREMENT,
    `tag_name`    VARCHAR(255)  NOT NULL COMMENT 'dashboard name',
    `created_by`  VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    UNIQUE KEY dashboard_name_unique (`tag_name`)
);

insert into `t_tag_blacklist` (`tag_name`) values ('cjsBeta');