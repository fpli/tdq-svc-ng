package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.service.MetricService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Hidden
public class TestController {

    @Autowired
    MetricService metricService;

    @Autowired
    ElasticsearchOperations esOperations;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/test_auth")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/test_es_get")
    public ResponseEntity<MetricDoc> testESGet() {
        IndexCoordinates idx = IndexCoordinates.of("my-index-000001");

        MetricDoc metricDoc = esOperations.get("1", MetricDoc.class, idx);

        return ResponseEntity.ok(metricDoc);
    }
}
