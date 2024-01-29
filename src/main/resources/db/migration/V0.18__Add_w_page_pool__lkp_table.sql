
create table if not exists `w_page_pool_lkp` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `page_id`     BIGINT       NOT NULL COMMENT 'Page Id',
    `traffic`     BIGINT       NOT NULL COMMENT 'event count of the same page id',
    `pool_name`   VARCHAR(255) NOT NULL COMMENT 'pool name',
    `dt`          date         NOT NULL COMMENT 'record date'
);