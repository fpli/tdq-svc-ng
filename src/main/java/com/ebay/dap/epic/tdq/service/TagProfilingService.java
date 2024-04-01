package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.vo.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface TagProfilingService {
    String retrieveDimensionsOfTag(TagDimensionQueryVO tagDimensionQueryVO);

    TagMetaDataVO getTagMetaData(String tagName, LocalDate dt);

    List<TagDetailVO> queryTagDetail(TagDetailFilterQueryVO tagDetailFilterQueryVO);

    List<Map.Entry<String, Integer>> getMapping();

    Map<String, Double> getDailyTagsSize(List<String> tags, LocalDate date);

    List<TagCardItemVo> getTagCardItemVo(LocalDate dt, int n) throws ExecutionException, InterruptedException;

    List<TagRecordInfo> getTagTable(Set<String> tagNameList, LocalDate date);

    void configAbnormalTag(double TH_ANOMALY_SCORE, int topN, int tagByVolumeTopN);

    void refresh();

    void cjsTagAlerting(LocalDate date, List<String> tagList) throws Exception;
}
