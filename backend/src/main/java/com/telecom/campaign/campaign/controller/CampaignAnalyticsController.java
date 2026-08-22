package com.telecom.campaign.campaign.controller;

import com.telecom.campaign.campaign.dto.CampaignAnalyticsResponse;
import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.campaign.service.CampaignAnalyticsService;
import com.telecom.campaign.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns/analytics")
@Slf4j
public class CampaignAnalyticsController {

    private final CampaignAnalyticsService analyticsService;

    public CampaignAnalyticsController(CampaignAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignAnalyticsResponse>> getCampaignAnalytics(@PathVariable Long id) {
        log.info("Analytics requested for campaign id={}", id);
        CampaignAnalyticsResponse analytics = analyticsService.getCampaignAnalytics(id);
        return ResponseEntity.ok(ApiResponse.<CampaignAnalyticsResponse>builder()
                .success(true).statusCode(200).message("Campaign analytics fetched successfully").data(analytics).build());
    }

    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getExpiringCampaigns(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Fetching campaigns expiring within {} days", days);
        List<CampaignResponse> expiring = analyticsService.getExpiringCampaigns(days);
        return ResponseEntity.ok(ApiResponse.<List<CampaignResponse>>builder()
                .success(true).statusCode(200).message("Expiring campaigns fetched successfully").data(expiring).build());
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalyticsSummary() {
        log.info("Fetching analytics summary");
        Map<String, Object> summary = Map.of(
                "completionRate", analyticsService.getCompletionRate(),
                "activeCampaignsPercentage", analyticsService.getActiveCampaignsPercentage(),
                "totalActive", analyticsService.countByStatus(com.telecom.campaign.common.enums.CampaignStatus.ACTIVE),
                "totalCompleted", analyticsService.countByStatus(com.telecom.campaign.common.enums.CampaignStatus.COMPLETED),
                "totalDraft", analyticsService.countByStatus(com.telecom.campaign.common.enums.CampaignStatus.DRAFT),
                "totalCancelled", analyticsService.countByStatus(com.telecom.campaign.common.enums.CampaignStatus.CANCELLED)
        );
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true).statusCode(200).message("Analytics summary fetched successfully").data(summary).build());
    }
}
