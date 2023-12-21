package com.ebay.dap.epic.tdq.data.pronto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Document(indexName = "prod.metric.rt.page", createIndex = false)
public class PageMetricDoc {

    @Id
    private String id;

    @Field(name = "metric_key", type = FieldType.Keyword)
    private String metricKey;

    @Field(name = "metric_time", type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant metricTime;

    @Field(name = "dt", type = FieldType.Date)
    private LocalDate dt;

    @Field(name = "hr", type = FieldType.Integer)
    private Integer hr;

    @Field(name = "page_id", type = FieldType.Keyword)
    private Integer pageId;

    @Field(name = "rt_event_cnt", type = FieldType.Long)
    private Long rtEventCnt;

    @Field(name = "acc_rt_event_cnt", type = FieldType.Long)
    private Long accRtEventCnt;

    @Field(name = "batch_event_cnt", type = FieldType.Long)
    private Long batchEventCnt;
}
