package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.vo.ScorecardItemVO;
import com.ebay.dap.epic.tdq.service.ScorecardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scorecard")
@Slf4j
@Tag(name = "Scorecard API", description = "scorecard api")
public class ScoreCardController {

    @Autowired
    private ScorecardService scorecardService;

    @Operation(summary = "list Score items")
    @GetMapping("listScore")
    public List<ScorecardItemVO> listScore(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date){
        if (date.isAfter(LocalDate.now())){
            throw new IllegalArgumentException("the parameter data: " + date + " can't be later than today.");
        }
        return scorecardService.listScore(date);
    }

    @Operation(summary = "test return map")
    @GetMapping("testMap")
    public Map<String, Double> test(){
        Map<String, Double> map =  new HashMap<>();
        map.put("a", 99D);
        map.put("b", 89D);
        return map;
    }

}
