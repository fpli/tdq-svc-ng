package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.data.dto.ChartInfoDTO;
import com.ebay.dap.epic.tdq.data.vo.ChartPreviewVO;
import com.ebay.dap.epic.tdq.data.vo.DashboardPreviewVO;
import com.ebay.dap.epic.tdq.service.ChartService;
import com.ebay.dap.epic.tdq.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ChartService chartService;

    @Override
    public DashboardPreviewVO getDashboardPreview() {
        DashboardPreviewVO vo = new DashboardPreviewVO();
        long ts = System.currentTimeMillis();
        vo.setRefreshTime(ts);

        List<ChartInfoDTO> chartInfoDTOList = chartService.listAllChartInfo();
        for (ChartInfoDTO chartInfoDTO : chartInfoDTOList) {
            ChartPreviewVO chartPreviewVO = new ChartPreviewVO();
            chartPreviewVO.setChartId(chartInfoDTO.getChartId());
            chartPreviewVO.setTitle(chartInfoDTO.getTitle());
            chartPreviewVO.setDescription(chartInfoDTO.getDescription());
            chartPreviewVO.setDisplayOrder(chartInfoDTO.getDispOrder());
            chartPreviewVO.setUpdateTime(ts);
            vo.getChartList().add(chartPreviewVO);
        }

        return vo;
    }
}
