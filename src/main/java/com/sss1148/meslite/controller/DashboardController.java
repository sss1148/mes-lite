package com.sss1148.meslite.controller;

import com.sss1148.meslite.common.ApiResponse;
import com.sss1148.meslite.service.DashboardService;
import com.sss1148.meslite.vo.OeeResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/oee/{orderNo}")
    public ApiResponse<OeeResult> oee(@PathVariable String orderNo) {
        return ApiResponse.ok(dashboardService.oee(orderNo));
    }
}
