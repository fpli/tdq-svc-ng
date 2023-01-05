package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.vo.report.MetadataDetailVo;
import com.ebay.dap.epic.tdq.data.vo.report.MetadataSummaryVo;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<MetadataSummaryVo> getClickSummary();

    List<MetadataSummaryVo> getModuleSummary();

    List<MetadataDetailVo> getClickDetail(String domain, Integer metricId, LocalDate dt);

    List<MetadataDetailVo> getModuleDetail(String domain, Integer metricId, LocalDate dt);
}
