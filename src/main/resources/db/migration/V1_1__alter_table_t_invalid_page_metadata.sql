-- add two columns pool_notification and resource_id
alter table t_invalid_page_metadata
add column pool_notification varchar(255) after pool_name,
add column resource_id varchar(255) after pool_notification;