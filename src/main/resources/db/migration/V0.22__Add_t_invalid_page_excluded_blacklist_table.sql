
create table if not exists `t_invalid_page_excluded_blacklist` (
    `id`          INT           PRIMARY KEY AUTO_INCREMENT,
    `page_id`     INT       NOT NULL COMMENT 'page id',
    `created_by`  VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time'
);

insert into `t_invalid_page_excluded_blacklist` (`page_id`) values (5499);