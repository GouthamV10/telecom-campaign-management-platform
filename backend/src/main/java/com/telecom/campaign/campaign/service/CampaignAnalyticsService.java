package com.telecom.campaign.campaign.service;

import com.telecom.campaign.campaign.dto.CampaignAnalyticsResponse;
import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.common.enums.CampaignStatus;

import java.util.List;

public interface CampaignAnalyticsService {

    CampaignAnalyticsResponse getCampaignAnalytics(Long campaignId);

    List<CampaignResponse> getExpiringCampaigns(int days);

    long countByStatus(CampaignStatus status);

    double getCompletionRate();

    double getActiveCampaignsPercentage();
}
