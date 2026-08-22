package com.telecom.campaign.campaign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignAnalyticsResponse {

    private Long campaignId;
    private String campaignName;
    private String status;
    private String managerName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long durationDays;
    private long totalDurationDays;
    private double progressPercentage;
    private List<StatusChangeRecord> statusHistory;
    private long daysRemaining;
    private boolean isOverdue;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusChangeRecord {
        private String fromStatus;
        private String toStatus;
        private LocalDateTime changedAt;
        private String changedBy;
    }
}
