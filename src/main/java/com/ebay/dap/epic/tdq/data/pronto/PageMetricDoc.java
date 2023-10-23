package com.ebay.dap.epic.tdq.data.pronto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

// FIXME: fix index name
@Data
@Document(indexName = "my-index", createIndex = false)
public class PageMetricDoc {

    @Id
    private String id;

    @Field(name = "metric_key", type = FieldType.Text)
    private String metricKey;

    @Field(name = "metric_time", type = FieldType.Long)
    private LocalDateTime metricTime;

    @Field(name = "dt", type = FieldType.Text)
    private String dt;

    @Field(name = "hr", type = FieldType.Integer)
    private Integer hr;

    @Field(name = "create_time", type = FieldType.Text)
    private LocalDateTime createTime;

    @Field(name = "page_id", type = FieldType.Integer)
    private Integer pageId;

    @Field(name = "event_cnt", type = FieldType.Long)
    private Long eventCnt;
}
