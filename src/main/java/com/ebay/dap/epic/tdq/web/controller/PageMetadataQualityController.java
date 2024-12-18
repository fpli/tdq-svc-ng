package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.entity.InvalidPageMetadataEntity;
import com.ebay.dap.epic.tdq.data.vo.BaseGeneralVO;
import com.ebay.dap.epic.tdq.service.PageMetadataQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/page/metadata/")
@Slf4j
@Tag(name = "page metadata API", description = "page metadata API")
public class PageMetadataQualityController {

    @Autowired
    private PageMetadataQualityService pageMetadataQualityService;

    @Operation(summary = "fill page metadata data")
    @GetMapping("fillData")
    public String fillData(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date){
        int i = pageMetadataQualityService.dailyTask(date);
        log.info("{} pages have been filled on {}", i, date);
        return "done";
    }

    @Operation(summary = "listAllInvalidPage")
    @GetMapping("listAllInvalidPage")
    public BaseGeneralVO<InvalidPageMetadataEntity> listAllInvalidPage(){
        return pageMetadataQualityService.listAllUnregisterPage();
    }

    @Operation(summary = "trigger invalid page notification")
    @GetMapping("triggerNotification")
    public String triggerNotification(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date){
        pageMetadataQualityService.dailyNotifyApplicationOwner(date);
        return "done";
    }

}
