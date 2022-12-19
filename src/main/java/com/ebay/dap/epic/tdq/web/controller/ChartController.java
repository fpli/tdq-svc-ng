package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.vo.ChartDataVO;
import com.ebay.dap.epic.tdq.data.vo.ChartVO;
import com.ebay.dap.epic.tdq.service.ChartService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/chart")
@Slf4j
public class ChartController {

    @Autowired
    private ChartService chartService;

    @GetMapping("/retrieveChartData/{id}")
    @ApiOperation(value = "retrieve Chart Data", notes = "retrieve Chart Data")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(paramType = "query", name = "date", dataType = "Date", example = "2022-08-07", format = "yyyy-MM-dd")
    })
    public ChartDataVO retrieveChartData(@PathVariable Long id, LocalDate date) throws Exception {
        if (null == date){
            date = chartService.getYesterday();
        }
        return chartService.retrieveChartData(id, date);
    }

    @GetMapping("/listChartInfoEntities")
    @ApiOperation(value = "list all Chart info", notes = "list all chart info")
    public List<ChartVO> listChartInfoEntities(){
        return chartService.listChartInfoEntities();
    }
}
