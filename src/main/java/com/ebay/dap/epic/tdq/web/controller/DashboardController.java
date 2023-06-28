package com.ebay.dap.epic.tdq.web.controller;


import com.ebay.dap.epic.tdq.data.vo.DashboardPreviewVO;
import com.ebay.dap.epic.tdq.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Slf4j
@Tag(name = "Dashboard API", description = "Dashboard related API")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /***
     * currently every user will see the same dashboard view
     */
    @GetMapping("/main/preview")
    public ResponseEntity<DashboardPreviewVO> showDashboard() throws Exception {

        return ResponseEntity.ok(dashboardService.getDashboardPreview());
    }


}
