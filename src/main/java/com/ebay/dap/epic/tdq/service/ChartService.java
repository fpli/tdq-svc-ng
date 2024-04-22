package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.dto.ChartInfoDTO;
import com.ebay.dap.epic.tdq.data.vo.ChartDataVO;
import com.ebay.dap.epic.tdq.data.vo.ChartPreviewDataVO;
import com.ebay.dap.epic.tdq.data.vo.ChartVO;

import java.time.LocalDate;
import java.util.List;

public interface ChartService {

    List<ChartVO> listChartInfoEntities();

    ChartDataVO retrieveChartData(Long id, LocalDate date) throws Exception;


    ChartPreviewDataVO getChartData(Long id) throws Exception;


    List<ChartInfoDTO> listAllChartInfo();

    List<ChartInfoDTO> listChartInfo(List<Integer> chartIds);
}
