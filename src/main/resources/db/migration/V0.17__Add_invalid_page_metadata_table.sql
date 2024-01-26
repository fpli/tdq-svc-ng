
create TABLE if not exists `t_invalid_page_metadata`
(
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `page_id`          BIGINT        NOT NULL COMMENT 'Page Id',
    `environment`      VARCHAR(255)  NULL COMMENT 'environment',
    `life_cycle_state` VARCHAR(255)  NULL COMMENT 'life cycle state',
    `pool_name`        VARCHAR(255)  NOT NULL COMMENT 'pool name',
    `dt`               date          NOT NULL COMMENT 'record date',
    `event_cnt`        BIGINT        NOT NULL COMMENT 'event count',
    `event_pct`        double        NOT NULL COMMENT 'event count percentage',
    `app_owner`        VARCHAR(255)  NULL COMMENT 'application owner',
    `app_notification` VARCHAR(255)  NULL COMMENT 'application notify email',
    `app_jira`         VARCHAR(255)  NULL COMMENT 'application jira link',
    `created_by`       VARCHAR(255)  NULL COMMENT 'Created By' DEFAULT 'sys',
    `updated_by`       VARCHAR(255)  NULL COMMENT 'Updated By' DEFAULT 'sys',
    `create_time`      TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time`      TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1;