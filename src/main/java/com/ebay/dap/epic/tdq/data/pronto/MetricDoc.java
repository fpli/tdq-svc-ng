package com.ebay.dap.epic.tdq.data.pronto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Data
@Document(indexName = "tdq.batch.profiling.metric.prod", createIndex = false)
public class MetricDoc {

    @Id
    private String id;

    @Field(name = "metric_key", type = FieldType.Text)
    private String metricKey;

    @Field(name = "dt", type = FieldType.Date, format = DateFormat.date)
    private LocalDate dt;

//    @Field(name = "dimension", type = FieldType.Object)
//    private MetricDimension dimension;
    @Field(name = "dimension", type = FieldType.Auto)
    private Map<String, Object> dimension;

    @Field(name = "value", type = FieldType.Auto)
    private BigDecimal value;

    @Field(name = "labels", type = FieldType.Auto)
    private Set<String> labels;

}
