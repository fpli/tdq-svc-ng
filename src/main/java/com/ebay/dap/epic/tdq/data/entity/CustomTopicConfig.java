package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_custom_topic_config")
@Data
public class CustomTopicConfig extends AuditableEntity {
  private String topic;
  private String pageIds; // 'page1,page2'
  private String profile;
}
