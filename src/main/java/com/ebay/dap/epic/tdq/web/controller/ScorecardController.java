package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.service.scorecard.ExecutionEngine;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/scorecard")
@Slf4j
@Tag(name = "Scorecard API", description = "Scorecard related API")
public class ScorecardController {

    @Autowired
    private ExecutionEngine executionEngine;

    @Hidden
    @PostMapping(path = "/run")
    public ResponseEntity<Void> run(@RequestBody LocalDate dt) {
        log.info("Trigger scorecard execution using API");
        
        return ResponseEntity.ok().build();
    }

}
