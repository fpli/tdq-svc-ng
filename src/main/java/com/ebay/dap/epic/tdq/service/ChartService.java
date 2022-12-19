package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.vo.ChartDataVO;
import com.ebay.dap.epic.tdq.data.vo.ChartVO;

import java.time.LocalDate;
import java.util.List;

public interface ChartService {

    List<ChartVO> listChartInfoEntities();

    ChartDataVO retrieveChartData(Long id, LocalDate date) throws Exception;

    default LocalDate getYesterday(){
        return LocalDate.now().minusDays(2);
    }
}
