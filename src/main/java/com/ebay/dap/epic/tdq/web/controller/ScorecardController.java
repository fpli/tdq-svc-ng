package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.vo.ScorecardDetailVO;
import com.ebay.dap.epic.tdq.data.vo.ScorecardItemVO;
import com.ebay.dap.epic.tdq.service.ScorecardService;
import com.ebay.dap.epic.tdq.service.scorecard.ExecutionEngine;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/scorecard")
@Slf4j
@Tag(name = "Scorecard API", description = "scorecard api")
public class ScorecardController {

    @Autowired
    private ExecutionEngine executionEngine;

    @Autowired
    private ScorecardService scorecardService;

    @Hidden
    @PostMapping(path = "/run")
    public ResponseEntity<Void> run(@RequestParam("dt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dt) {
        log.info("Trigger scorecard execution using API");
        executionEngine.process(dt);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "list Score items")
    @GetMapping("listScore")
    public List<ScorecardItemVO> listScore(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date){
        if (date.isAfter(LocalDate.now())){
            throw new IllegalArgumentException("the parameter data: " + date + " can't be later than today.");
        }
        return scorecardService.listScore(date);
    }

    @Operation(summary = "fetch Available Dates")
    @GetMapping("fetchAvailableDates")
    public List<String> fetchAvailableDates(){
        return scorecardService.fetchAvailableDates();
    }

    @Operation(summary = "list Scorecard detail")
    @GetMapping("listScoreDetail")
    public ScorecardDetailVO listScoreDetail(String type, String name, @RequestParam(name = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin){
        LocalDate date = LocalDate.now().minusDays(1);
        return scorecardService.listScoreDetail(type, name, Objects.requireNonNullElseGet(begin, () -> date.minusMonths(1)), date);
    }

}
