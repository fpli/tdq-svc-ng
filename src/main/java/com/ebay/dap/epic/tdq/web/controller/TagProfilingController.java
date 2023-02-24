package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.vo.TagCardItemVo;
import com.ebay.dap.epic.tdq.data.vo.TagDetailFilterQueryVO;
import com.ebay.dap.epic.tdq.data.vo.TagDetailVO;
import com.ebay.dap.epic.tdq.data.vo.TagDimensionQueryVO;
import com.ebay.dap.epic.tdq.data.vo.TagMetaDataVO;
import com.ebay.dap.epic.tdq.data.vo.TagRecordInfo;
import com.ebay.dap.epic.tdq.service.TagProfilingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping(value = "/api/tagLevel")
@Tag(name = "Tag Profiling API", description = "Tag profiling related API")
public class TagProfilingController {

    @Autowired
    private TagProfilingService tagProfilingService;

    private LocalDate getDate() {
        final LocalDate date;
        if (LocalDateTime.now().getHour() < 10) {
            date = LocalDate.now().minusDays(2);
        } else {
            date = LocalDate.now().minusDays(1);
        }
        return date;
    }

    @PostMapping(path = "/retrieveDimensionsOfTag")
    public String retrieveDimensionsOfTag(@RequestBody TagDimensionQueryVO tagDimensionQueryVO) {
        return tagProfilingService.retrieveDimensionsOfTag(tagDimensionQueryVO);
    }

    @PostMapping(path = "/queryTagDetail")
    public List<TagDetailVO> queryTagDetail(@RequestBody TagDetailFilterQueryVO tagDetailFilterQueryVO) {
        return tagProfilingService.queryTagDetail(tagDetailFilterQueryVO);
    }

    @GetMapping(path = "/getTagMetadata")
    public TagMetaDataVO getTagMetaData(String tagName, @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dt) {
        return tagProfilingService.getTagMetaData(tagName, dt);
    }

    @GetMapping(path = "/getMapping")
    public List<Map.Entry<String, Integer>> getMapping() {
        return tagProfilingService.getMapping();
    }

    @PostMapping(path = "/getDailyTagsSize")
    public Map<String, Double> getDailyTagsSize(@RequestBody List<String> tagNameList) {
        log.info("tagNameList: {}", tagNameList);
        return tagProfilingService.getDailyTagsSize(tagNameList, getDate());
    }

    @GetMapping("/getTagCardItemVo")
    public List<TagCardItemVo> getTagCardItemVo(@RequestParam int n) throws ExecutionException, InterruptedException {
        return tagProfilingService.getTagCardItemVo(getDate(), n);
    }

    @PostMapping("/getTagTable")
    public List<TagRecordInfo> getTagTable(@RequestBody Set<String> tagNameList) {
        return tagProfilingService.getTagTable(tagNameList, getDate());
    }

    @GetMapping(path = "/refreshTagInfos")
    public String refreshTagInfos(@RequestParam(defaultValue = "0.9995", name = "score") double TH_ANOMALY_SCORE, @RequestParam(defaultValue = "100") int topN, @RequestParam(defaultValue = "3000") int tagByVolumeTopN) {
        tagProfilingService.configAbnormalTag(TH_ANOMALY_SCORE, topN, tagByVolumeTopN);
        tagProfilingService.refresh();
        return "Successfully";
    }
}