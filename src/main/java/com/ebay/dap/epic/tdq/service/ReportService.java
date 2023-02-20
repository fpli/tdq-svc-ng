package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.vo.report.MetadataDetailVo;
import com.ebay.dap.epic.tdq.data.vo.report.MetadataSummaryVo;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    /**
     * Get Click metadata coverage summary report for a particular date
     *
     * @param dt
     * @return
     */
    List<MetadataSummaryVo> getClickSummary(LocalDate dt);

    /**
     * Get Module metadata coverage summary report for a particular date
     *
     * @param dt
     * @return
     */
    List<MetadataSummaryVo> getModuleSummary(LocalDate dt);

    /**
     * Get Click metadata coverage detail report
     *
     * @param domain
     * @param metricId
     * @param dt
     * @return
     */
    List<MetadataDetailVo> getClickDetail(String domain, Integer metricId, LocalDate dt);

    /**
     * Get Module metadata coverage detail report
     *
     * @param domain
     * @param metricId
     * @param dt
     * @return
     */
    List<MetadataDetailVo> getModuleDetail(String domain, Integer metricId, LocalDate dt);
}
