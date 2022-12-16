package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.vo.ChartDataVO;
import com.ebay.dap.epic.tdq.service.ChartService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/chart")
@Slf4j
public class ChartController {

    @Autowired
    private ChartService chartService;

    @GetMapping("/retrieveChartData")
    @ApiOperation(value = "retrieve Chart Data", notes = "retrieve Chart Data")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = "query", name = "date", dataType = "Date", example = "2022-08-07", format = "yyyy-MM-dd")
    })
    public ChartDataVO retrieveChartData(@PathVariable Long id, @RequestParam LocalDate date) throws Exception {
        if (null == date){
            date = chartService.getYesterday();
        }
        return chartService.retrieveChartData(id, date);
    }
}
