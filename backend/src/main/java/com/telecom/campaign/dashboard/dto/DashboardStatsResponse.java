package com.telecom.campaign.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private long totalCampaigns;
    private long activeCampaigns;
    private long draftCampaigns;
    private long completedCampaigns;
    private long pausedCampaigns;
    private long cancelledCampaigns;
    private long totalUsers;
    private long activeUsers;
}
