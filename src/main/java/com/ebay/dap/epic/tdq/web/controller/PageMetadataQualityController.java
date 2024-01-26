package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.entity.UnregisterPageMetadataEntity;
import com.ebay.dap.epic.tdq.data.vo.BaseGeneralVO;
import com.ebay.dap.epic.tdq.service.PageMetadataQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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

    @Operation(summary = "listAllUnregisteredPage")
    @GetMapping("listAllUnregisteredPage")
    public BaseGeneralVO<UnregisterPageMetadataEntity> listAllUnregisteredPage(@RequestParam(name = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date){
        if (null == date){
            date = LocalDate.now().minusDays(2);
        }
        if (date.isAfter(LocalDate.now())){
            throw new IllegalArgumentException("the parameter data: " + date + " can't be later than today.");
        }
        return pageMetadataQualityService.listAllUnregisterPage(date);
    }

}
