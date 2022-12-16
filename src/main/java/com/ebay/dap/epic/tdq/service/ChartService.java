package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.vo.ChartDataVO;

import java.time.LocalDate;

public interface ChartService {

    ChartDataVO retrieveChartData(Long id, LocalDate date) throws Exception;

    default LocalDate getYesterday(){
        return LocalDate.now().minusDays(2);
    }
}
