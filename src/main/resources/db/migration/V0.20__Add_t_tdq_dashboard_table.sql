
create table if not exists `t_tdq_dashboard` (
    `id`          INT           PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(255)  NOT NULL COMMENT 'dashboard name',
    `description` VARCHAR(255)  NULL COMMENT 'description for this dashboard',
    `chart_list`  VARCHAR(255)  NULL COMMENT 'chart ids',
    `created_by`  VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`  VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time` TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    UNIQUE KEY dashboard_name_unique (`name`)
);

insert into `t_tdq_dashboard` (`name`, `description`) values ('default', 'default dashboard');

insert into `t_tdq_dashboard` (`name`, `description`, `chart_list`) values ('bot', 'bot dashboard', '1009,1010,1012,1013,1015,1016,1025,1026,1027,1028,1036,1037,1038,1039');

insert into `t_tdq_dashboard` (`name`, `description`, `chart_list`) values ('cjs', 'cjs dashboard', '1029,1030,1031,1032,1033,1034,1035');

insert into `t_tdq_dashboard` (`name`, `description`, `chart_list`) values ('domain', 'domain dashboard', '1011,1017');