CREATE TABLE IF NOT EXISTS `t_profiling_page_top50_lkp`
(
    id                 BIGINT AUTO_INCREMENT COMMENT 'PK ID'
        PRIMARY KEY,
    page_id            BIGINT                              NOT NULL COMMENT 'Page Id',
    page_name          VARCHAR(255)                        NULL,
    page_desc          VARCHAR(255)                        NULL,
    iframe             TINYINT(1)                          NOT NULL,
    owner              VARCHAR(255)                        NULL,
    page_fmly          VARCHAR(255)                        NULL,
    create_dt          DATE                                NOT NULL,
    page_first_seen_dt DATE                                NULL,
    page_last_seen_dt  DATE                                NULL,
    create_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL COMMENT 'Create Time',
    update_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    CONSTRAINT page_id_uni
        UNIQUE (page_id)
)
    ENGINE = InnoDB
    CHARSET = utf8;
