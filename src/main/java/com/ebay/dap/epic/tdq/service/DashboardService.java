package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.vo.DashboardPreviewVO;

import java.util.List;

public interface DashboardService {

    DashboardPreviewVO getDashboardPreview();

    DashboardPreviewVO getDashboardPreview(List<Integer> chartIds);

    DashboardPreviewVO getDashboardPreview(String groupName);
}
