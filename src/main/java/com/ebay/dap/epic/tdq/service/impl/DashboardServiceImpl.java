package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.dto.ChartInfoDTO;
import com.ebay.dap.epic.tdq.data.entity.DashboardEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.DashboardMapper;
import com.ebay.dap.epic.tdq.data.vo.ChartPreviewVO;
import com.ebay.dap.epic.tdq.data.vo.DashboardPreviewVO;
import com.ebay.dap.epic.tdq.service.ChartService;
import com.ebay.dap.epic.tdq.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ChartService chartService;

    @Autowired
    private DashboardMapper dashboardMapper;

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

    @Override
    public DashboardPreviewVO getDashboardPreview(List<Integer> chartIds) {
        DashboardPreviewVO vo = new DashboardPreviewVO();
        long ts = System.currentTimeMillis();
        vo.setRefreshTime(ts);

        List<ChartInfoDTO> chartInfoDTOList = chartService.listChartInfo(chartIds);
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

    @Override
    public DashboardPreviewVO getDashboardPreview(String groupName) {
        if ("default".equalsIgnoreCase(groupName)) {
            return getDashboardPreview();
        }
        LambdaQueryWrapper<DashboardEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(DashboardEntity::getName, groupName);
        DashboardEntity dashboardEntity = dashboardMapper.selectOne(lambdaQueryWrapper);
        if (dashboardEntity != null) {
            String chartIds = dashboardEntity.getChartList();
            if (chartIds != null && !chartIds.isEmpty()) {
                String[] chartIdArray = chartIds.split(",");
                List<Integer> chartIdsList = new ArrayList<>();
                for (String chartId : chartIdArray) {
                    chartIdsList.add(Integer.parseInt(chartId.strip()));
                }
                return getDashboardPreview(chartIdsList);
            }
        }
        throw new IllegalArgumentException("No dashboard found for name: " + groupName);
    }
}
