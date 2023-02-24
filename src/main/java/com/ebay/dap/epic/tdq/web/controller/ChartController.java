package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.common.util.TDQDateUtil;
import com.ebay.dap.epic.tdq.data.vo.ChartDataVO;
import com.ebay.dap.epic.tdq.data.vo.ChartVO;
import com.ebay.dap.epic.tdq.service.ChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chart API", description = "Chart related API")
public class ChartController {

    @Autowired
    private ChartService chartService;

    @Operation(summary = "retrieve chart data")
    @GetMapping("/retrieveChartData/{id}")
    public ChartDataVO retrieveChartData(@PathVariable Long id, LocalDate date) throws Exception {
        if (null == date || date.isAfter(TDQDateUtil.getYesterday())) {
            date = TDQDateUtil.getYesterday();
        }
        return chartService.retrieveChartData(id, date);
    }

    @Operation(summary = "list all chart info")
    @GetMapping("/listChartInfo")
    public List<ChartVO> listChartInfo() {
        return chartService.listChartInfoEntities();
    }
}
