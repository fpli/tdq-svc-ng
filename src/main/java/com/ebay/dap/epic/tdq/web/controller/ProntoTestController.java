package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.pronto.PageMetricDoc;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/pronto")
@Hidden
public class ProntoTestController {


    @Autowired
    private ElasticsearchOperations esOperations;

    @GetMapping("/page_metric/{id}")
    public ResponseEntity<PageMetricDoc> test(@PathVariable("id") String docId) {
        return ResponseEntity.ok(esOperations.get(docId, PageMetricDoc.class));

    }

}
