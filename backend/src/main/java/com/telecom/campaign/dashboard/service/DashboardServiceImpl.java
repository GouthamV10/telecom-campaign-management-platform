package com.telecom.campaign.dashboard.service;

import com.telecom.campaign.campaign.repository.CampaignRepository;
import com.telecom.campaign.common.enums.CampaignStatus;
import com.telecom.campaign.dashboard.dto.DashboardStatsResponse;
import com.telecom.campaign.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(CampaignRepository campaignRepository, UserRepository userRepository) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DashboardStatsResponse getStats() {
        log.info("Fetching dashboard statistics");

        long totalCampaigns = campaignRepository.count();
        long activeCampaigns = campaignRepository.countByStatus(CampaignStatus.ACTIVE);
        long draftCampaigns = campaignRepository.countByStatus(CampaignStatus.DRAFT);
        long completedCampaigns = campaignRepository.countByStatus(CampaignStatus.COMPLETED);
        long pausedCampaigns = campaignRepository.countByStatus(CampaignStatus.PAUSED);
        long cancelledCampaigns = campaignRepository.countByStatus(CampaignStatus.CANCELLED);
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();

        return DashboardStatsResponse.builder()
                .totalCampaigns(totalCampaigns)
                .activeCampaigns(activeCampaigns)
                .draftCampaigns(draftCampaigns)
                .completedCampaigns(completedCampaigns)
                .pausedCampaigns(pausedCampaigns)
                .cancelledCampaigns(cancelledCampaigns)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .build();
    }
}
