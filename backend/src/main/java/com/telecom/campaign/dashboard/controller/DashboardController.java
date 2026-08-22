package com.telecom.campaign.dashboard.controller;

import com.telecom.campaign.common.dto.ApiResponse;
import com.telecom.campaign.dashboard.dto.DashboardStatsResponse;
import com.telecom.campaign.dashboard.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        log.info("getStats called");
        DashboardStatsResponse stats = dashboardService.getStats();
        ApiResponse<DashboardStatsResponse> response = ApiResponse.<DashboardStatsResponse>builder()
                .success(true)
                .statusCode(200)
                .message("Dashboard Stats Fetched Successfully")
                .data(stats)
                .build();
        return ResponseEntity.ok(response);
    }
}
