package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.vo.*;
import com.ebay.dap.epic.tdq.service.TagProfilingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping(value = "/api/tagLevel")
@Api(value = "TagLevelController", tags = {"tag Level"})
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

    @ApiOperation(value = "retrieve Dimensions Of Tag", notes = "retrieve Dimensions Of Tag")
    @PostMapping(path = "/retrieveDimensionsOfTag")
    @ApiImplicitParam(paramType = "body", dataTypeClass = TagDimensionQueryVO.class)
    public String retrieveDimensionsOfTag(@RequestBody TagDimensionQueryVO tagDimensionQueryVO) {
        return tagProfilingService.retrieveDimensionsOfTag(tagDimensionQueryVO);
    }

    @ApiOperation(value = "retrieve tag details", notes = "retrieve tag details")
    @PostMapping(path = "/queryTagDetail")
    @ApiImplicitParam(paramType = "body", dataTypeClass = TagDetailFilterQueryVO.class)
    public List<TagDetailVO> queryTagDetail(@RequestBody TagDetailFilterQueryVO tagDetailFilterQueryVO) {
        return tagProfilingService.queryTagDetail(tagDetailFilterQueryVO);
    }


    @ApiOperation(value = "get tag metadata", notes = "get tag metadata")
    @GetMapping(path = "/getTagMetadata")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tagName", dataType = "String", required = true),
            @ApiImplicitParam(paramType = "query", name = "date", dataType = "Date", example = "2022-08-07", required = true)
    })
    public TagMetaDataVO getTagMetaData(String tagName, @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dt) {
        return tagProfilingService.getTagMetaData(tagName, dt);
    }

    @ApiOperation(value = "get tag mapping", notes = "get tag mapping")
    @GetMapping(path = "/getMapping")
    public List<Map.Entry<String, Integer>> getMapping() {
        return tagProfilingService.getMapping();
    }

    @ApiOperation(value = "get daily tags size", notes = "get daily tags size")
    @PostMapping(path = "/getDailyTagsSize")
    public Map<String, Double> getDailyTagsSize(@RequestBody List<String> tagNameList) {
        log.info("tagNameList: {}", tagNameList);
        return tagProfilingService.getDailyTagsSize(tagNameList, getDate());
    }

    @ApiOperation(value = "get tag card items info", notes = "get tag card items info")
    @GetMapping("/getTagCardItemVo")
    public List<TagCardItemVo> getTagCardItemVo(@RequestParam int n) throws ExecutionException, InterruptedException {
        return tagProfilingService.getTagCardItemVo(getDate(), n);
    }

    @ApiOperation(value = "get tag table items info", notes = "get tag table items info")
    @PostMapping("/getTagTable")
    public List<TagRecordInfo> getTagTable(@RequestBody Set<String> tagNameList) {
        return tagProfilingService.getTagTable(tagNameList, getDate());
    }

    @ApiOperation(value = "refresh Tag Infos", notes = "refresh tag information")
    @GetMapping(path = "/refreshTagInfos")
    public String refreshTagInfos(@RequestParam(defaultValue = "0.9995", name = "score") double TH_ANOMALY_SCORE, @RequestParam(defaultValue = "100") int topN, @RequestParam(defaultValue = "3000") int tagByVolumeTopN) {
        tagProfilingService.configAbnormalTag(TH_ANOMALY_SCORE, topN, tagByVolumeTopN);
        tagProfilingService.refresh();
        return "Successfully";
    }
}